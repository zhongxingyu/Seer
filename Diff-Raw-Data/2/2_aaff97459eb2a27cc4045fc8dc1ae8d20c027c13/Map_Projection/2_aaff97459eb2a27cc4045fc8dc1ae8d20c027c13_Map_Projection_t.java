 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import huisken.projection.viz.CustomContent;
 import huisken.projection.viz.SphereProjectionViewer;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.gui.WaitForUserDialog;
 import ij.plugin.PlugIn;
 import ij.process.ByteProcessor;
 import ij.process.ImageProcessor;
 import ij3d.Image3DUniverse;
 
 import java.awt.geom.GeneralPath;
 import java.awt.geom.PathIterator;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.media.j3d.Transform3D;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point2f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import com.jhlabs.map.proj.AugustProjection;
 import com.jhlabs.map.proj.BonneProjection;
 import com.jhlabs.map.proj.GallProjection;
 import com.jhlabs.map.proj.Kavraisky7Projection;
 import com.jhlabs.map.proj.MercatorProjection;
 import com.jhlabs.map.proj.OrthographicAzimuthalProjection;
 import com.jhlabs.map.proj.WinkelTripelProjection;
 
 
 public class Map_Projection implements PlugIn {
 
 	public static final int MERCATOR              = 0;
 	public static final int GALLPETER             = 1;
 	public static final int KAVRAYSKIY            = 2;
 	public static final int WINKEL_TRIPEL         = 3;
 	public static final int AUGUSTUS_EPICYCLOIDAL = 4;
 	public static final int BONNE                 = 5;
 	public static final int ORTHO_AZIMUTAL        = 6;
 	public static final int FULLER                = 7;
 
 	public static final String[] MAP_TYPES = new String[] {
 		"Mercator",
 		"Gall-Peter",
 		"Kavrayskiy",
 		"Winkel Tripel",
 		"Augustus Epicycloidal",
 		"Bonne",
 		"Orthogonal Azimutal",
 		"Fuller"};
 
 	@Override
 	public void run(String arg) {
 		GenericDialogPlus gd = new GenericDialogPlus("Create 2D Maps");
 		gd.addDirectoryField("Data directory", "");
 		gd.addCheckbox("Create_coastlines", false);
 		gd.addCheckbox("Create_Longitude/Latitude lines", true);
 		gd.addCheckbox("Create_camera contribution overlay", true);
 		gd.addCheckbox("Interactively choose initial transform", false);
 		gd.addNumericField("Target width", 800, 0);
 		for(int i = 0; i < MAP_TYPES.length; i++)
 			gd.addCheckbox(MAP_TYPES[i], false);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 		File datadir = new File(gd.getNextString());
 		boolean doCoast = gd.getNextBoolean();
 		boolean doLines = gd.getNextBoolean();
 		boolean doContributions = gd.getNextBoolean();
 		boolean chooseInitial = gd.getNextBoolean();
 		int tgtWidth = (int)gd.getNextNumber();
 
 		if(!datadir.isDirectory()) {
 			IJ.error(datadir + " is not a directory");
 			return;
 		}
 
 		File objfile = new File(datadir, "Sphere.obj");
 		if(!objfile.exists()) {
 			IJ.error("Cannot find " + objfile.getAbsolutePath());
 			return;
 		}
 
 		Matrix4f initial = new Matrix4f();
 		initial.setIdentity();
 
 		if(chooseInitial) {
 			File previewdir = new File(datadir, "resampled");
 			if(!previewdir.exists() || !previewdir.isDirectory())
 				previewdir = datadir;
 
 			Image3DUniverse univ = SphereProjectionViewer.show(previewdir.getAbsolutePath() + "/Sphere.obj", previewdir.getAbsolutePath(), null);
 			new WaitForUserDialog("",
 				"Please rotate the sphere to the desired orientation, then click OK").show();
 			Transform3D trans = new Transform3D();
 			CustomContent cc = (CustomContent)univ.getContent("bla");
 			cc.getLocalRotate(trans);
 			trans.get(initial);
 			univ.close();
 		} else if(new File(datadir, "initial_map_transform.mat").exists()) {
 			try {
 				initial = Register_.loadTransform(new File(datadir, "initial_map_transform.mat").getAbsolutePath());
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			Register_.saveTransform(initial, new File(datadir, "initial_map_transform.mat").getAbsolutePath());
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		for(int i = 0; i < MAP_TYPES.length; i++) {
 			if(!gd.getNextBoolean())
 				continue;
 			File outputdir = new File(datadir, MAP_TYPES[i]);
 			if(outputdir.isDirectory()) {
 				boolean cancelled = !IJ.showMessageWithCancel("Overwrite",
 						outputdir + " already exists. Overwrite?");
 				if(cancelled)
 					return;
 			} else {
 				outputdir.mkdir();
 			}
 			new File(outputdir, "contributions").mkdir();
 			new File(outputdir, "lines").mkdir();
 
 			try {
 				prepareForProjection(datadir, initial, i, outputdir, doCoast, doLines, doContributions, tgtWidth);
 				createProjections();
 			} catch(Exception e) {
 				IJ.error(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private SphericalMaxProjection smp;
 	private GeneralProjProjection proj;
 	private File datadir, outputdir;
 	private int nVertices;
 	private boolean doContributions, doLines;
 	private Matrix4f initial;
 
 	public void prepareForProjection(final File datadir, final Matrix4f initial, final int maptype, final File outputdir, final boolean doCoast, final boolean doLines, final boolean doContributions, final int tgtWidth) throws IOException {
		this.smp = new SphericalMaxProjection(new File(datadir, "Sphere.obj").getAbsolutePath(), initial);
 		this.nVertices = smp.getSphere().nVertices;
 		this.doLines = doLines;
 		this.doContributions = doContributions;
 		this.initial = initial;
 		this.datadir = datadir;
 		this.outputdir = outputdir;
 		if(!outputdir.exists())
 			outputdir.mkdirs();
 		switch(maptype) {
 			case MERCATOR:              proj = new GeneralProjProjection(new MercatorProjection());   break;
 			case GALLPETER:             proj = new GeneralProjProjection(new GallProjection()); break;
 			case KAVRAYSKIY:            proj = new GeneralProjProjection(new Kavraisky7Projection()); break;
 			case WINKEL_TRIPEL:         proj = new GeneralProjProjection(new WinkelTripelProjection()); break;
 			case AUGUSTUS_EPICYCLOIDAL: proj = new GeneralProjProjection(new AugustProjection()); break;
 			case BONNE:                 proj = new GeneralProjProjection(new BonneProjection()); break;
 			case ORTHO_AZIMUTAL:        proj = new GeneralProjProjection(new OrthographicAzimuthalProjection()); break;
 			case FULLER:                proj = new FullerProjection(); break;
 			default: throw new IllegalArgumentException("Unsupported map type: " + maptype);
 		}
 		proj.prepareForProjection(smp, tgtWidth);
 
 		if(doCoast) {
 			// create a postscript file with the coastline
 			if(new File("coast.dat").exists()) {
 				GeneralPath coast = GeneralProjProjection.readDatFile("coast.dat");
 				coast = proj.transform(coast);
 				GeneralProjProjection.savePath(coast, new File(outputdir, "coastline.ps").getAbsolutePath(), proj.getWidth(), proj.getHeight(), true);
 			}
 
 			// create a postscript file with the lines
 			GeneralPath lines = proj.createLines();
 			lines = proj.transform(lines);
 			GeneralProjProjection.savePath(lines, new File(outputdir, "lines.ps").getAbsolutePath(), proj.getWidth(), proj.getHeight(), false);
 		}
 	}
 
 	public void project(int tp) throws IOException {
 		String basename = String.format("tp%04d", tp);
 
 		File matdir = new File(datadir, "transformations");
 		File contribindir = new File(datadir, "contributions");
 		File contriboutdir = new File(outputdir, "contributions");
 		File linesdir = new File(outputdir, "lines");
 
 		File outfile = new File(outputdir, basename + ".tif");
 		File infile = new File(datadir, basename + ".vertices");
 		File matfile = new File(matdir, basename + ".matrix");
 		File contribin = new File(contribindir, basename + ".vertices");
 		File contribout = new File(contriboutdir, basename + ".tif");
 		File linesout = new File(linesdir, basename + ".tif");
 
 		if(!contribin.exists())
 			contribin = new File(datadir, "contributions.vertices");
 		short[] maxima = SphericalMaxProjection.loadShortData(infile.getAbsolutePath(), nVertices);
 //		maxima = smp.applyTransform(get90DegRot(smp), maxima);
 		ImageProcessor ip = proj.project(maxima);
 		if(doLines) {
 			GeneralPath lines = proj.createLines();
 			Matrix4f mat = new Matrix4f(initial);
 			if(matfile.exists())
 				mat.mul(Register_.loadTransform(matfile.getAbsolutePath()));
 			else
 				System.out.println(matfile + " does not exist");
 
 			// rotate the lines by 90 deg
 			Matrix4f rot = get90DegRot(smp);
 			lines = transform(smp, rot, lines);
 
 			lines = transform(smp, mat, lines);
 			lines = proj.transform(lines);
 			ImageProcessor lip = new ByteProcessor(ip.getWidth(), ip.getHeight());
 			proj.drawInto(lip, 255, 1, lines);
 			IJ.save(new ImagePlus("", lip), linesout.getAbsolutePath());
 		}
 		if(doContributions) {
 			int[] contribs = SphericalMaxProjection.loadIntData(contribin.getAbsolutePath(), nVertices);
 			ImageProcessor overlay = proj.projectColor(contribs);
 			IJ.save(new ImagePlus("", overlay), contribout.getAbsolutePath());
 		}
 		IJ.save(new ImagePlus("", ip), outfile.getAbsolutePath());
 	}
 
 	public void createProjections() {
 		// collect files
 		List<String> tmp = new ArrayList<String>();
 		for(String f : datadir.list())
 			if(f.startsWith("tp") && f.endsWith(".vertices"))
 				tmp.add(f);
 		final String[] files = new String[tmp.size()];
 		tmp.toArray(files);
 
 		final int nProcessors = Runtime.getRuntime().availableProcessors();
 		ExecutorService exec = Executors.newFixedThreadPool(nProcessors);
 		int nFilesPerThread = (int)Math.ceil(files.length / (double)nProcessors);
 
 		for(int p = 0; p < nProcessors; p++) {
 			final int start = p * nFilesPerThread;
 			final int end = Math.min(files.length, (p + 1) * nFilesPerThread);
 
 			exec.submit(new Runnable() {
 				@Override
 				public void run() {
 					for(int f = start; f < end; f++) {
 						String file = files[f];
 						try {
 							int tp = Integer.parseInt(file.substring(2, 6));
 							project(tp);
 						} catch(Exception e) {
 							System.err.println("Cannot project " + file);
 							e.printStackTrace();
 						}
 					}
 				}
 			});
 		}
 		try {
 			exec.shutdown();
 			exec.awaitTermination(300, TimeUnit.MINUTES);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Matrix4f get90DegRot(SphericalMaxProjection smp) {
 		Matrix4f rot = new Matrix4f();
 		Matrix4f cen = new Matrix4f();
 
 		rot.rotZ((float)(Math.PI / 2));
 		cen.set(new Vector3f(smp.center));
 
 		rot.mul(cen, rot);
 		cen.invert();
 		rot.mul(rot, cen);
 
 		return rot;
 	}
 
 	/**
 	 * In- and output coordinates are in degrees.
 	 * @param smp
 	 * @param trans
 	 * @param path
 	 * @return
 	 */
 	public static GeneralPath transform(SphericalMaxProjection smp, Matrix4f trans, GeneralPath path) {
 		GeneralPath out = new GeneralPath();
 		PathIterator it = path.getPathIterator(null);
 		float[] seg = new float[6];
 
 		Point2f pin = new Point2f();
 
 		while(!it.isDone()) {
 			int l = it.currentSegment(seg);
 			pin.x = seg[0];
 			pin.y = seg[1];
 			toRad(pin);
 			transform(smp, trans, pin);
 			toDeg(pin);
 
 			if(l == PathIterator.SEG_MOVETO)
 				out.moveTo(pin.x, pin.y);
 			else
 				out.lineTo(pin.x, pin.y);
 			it.next();
 		}
 		return out;
 	}
 
 	public static void toRad(Point2f polar) {
 		polar.x = (float)(polar.x / 180 * Math.PI);
 		polar.y = (float)(polar.y / 180 * Math.PI);
 	}
 
 	public static void toDeg(Point2f polar) {
 		polar.x = (float)(polar.x / Math.PI * 180);
 		polar.y = (float)(polar.y / Math.PI * 180);
 	}
 
 	public static void transform(SphericalMaxProjection smp, Matrix4f trans, Point2f polar) {
 		Point3f coord = new Point3f();
 		Point3f center = smp.getCenter();
 		float radius = smp.getRadius();
 		getPoint(center, radius, polar.x, polar.y, coord);
 		trans.transform(coord);
 		smp.getPolar(coord, polar);
 	}
 
 	public static void getPoint(Point3f center, float radius, float longitude, float latitude, Point3f ret) {
 		double sinLong = Math.sin(longitude),
 			cosLong = Math.cos(longitude),
 			sinLat = Math.sin(latitude),
 			cosLat = Math.cos(latitude);
 		ret.z = (float)(center.z + radius * cosLat * cosLong);
 		ret.x = (float)(center.x - radius * cosLat * sinLong);
 		ret.y = (float)(center.y - radius * sinLat);
 	}
 }
