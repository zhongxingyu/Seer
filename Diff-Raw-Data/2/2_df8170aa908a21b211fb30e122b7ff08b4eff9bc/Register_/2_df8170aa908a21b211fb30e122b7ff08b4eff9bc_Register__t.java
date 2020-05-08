 package huisken.projection.processing;
 
 import fiji.util.gui.GenericDialogPlus;
 import ij.IJ;
 import ij.Prefs;
 import ij.plugin.PlugIn;
 import ij.plugin.filter.GaussianBlur;
 import ij.process.ImageProcessor;
 
 import java.awt.Polygon;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point3f;
 
 
 public class Register_ implements PlugIn {
 
 	@Override
 	public void run(String arg) {
 		String datadir = Prefs.get("register_sphere_proj.datadir", "");
 		String outputdir = Prefs.get("register_sphere_proj.outputdir", "");
 		GenericDialogPlus gd = new GenericDialogPlus("Register sphere projections");
 		gd.addDirectoryField("Data directory", datadir);
 		gd.addDirectoryField("Output directory", outputdir);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 		datadir = gd.getNextString();
 		outputdir = gd.getNextString();
 
 		Prefs.set("register_sphere_proj.datadir", datadir);
 		Prefs.set("register_sphere_proj.outputdir", outputdir);
 		Prefs.savePreferences();
 
 		try {
 			prepareRegistration(new File(datadir), new File(outputdir));
 			register();
 		} catch(Exception e) {
 		IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private static ArrayList<Point3f> getPoints(FullerProjection proj, GaussianBlur gauss, short[] maxima) {
 		ImageProcessor fuller = proj.project(maxima);
 		gauss.blur(fuller, 1.5);
 		Polygon pt = new MaximumFinder().getMaxima(fuller, 10, true);
 
 		ArrayList<Point3f> pts = new ArrayList<Point3f>();
 		for(int i = 0; i < pt.npoints; i++) {
 			int x = pt.xpoints[i];
 			int y = pt.ypoints[i];
 
 			Point3f ptmp = new Point3f();
 			if(proj.getPointOnSphere(x, y, ptmp))
 				pts.add(ptmp);
 		}
 		return pts;
 	}
 
 	private SphericalMaxProjection smp;
 	private int nVertices;
 	private FullerProjection proj;
 	private GaussianBlur gauss;
 	private Matrix4f overall;
 	private File outputDirectory, dataDirectory, contributionsDirectory, matrixDirectory;
 	private int[] contributions;
 
 	public void prepareRegistration(File dDir, File oDir) throws IOException {
 		this.dataDirectory = dDir;
 		this.outputDirectory = oDir;
 		if(!outputDirectory.exists())
 			outputDirectory.mkdir();
 
 		File objfile = new File(dataDirectory, "Sphere.obj");
 		if(!objfile.exists())
 			throw new IllegalArgumentException("Cannot find " + objfile.getAbsolutePath());
 
 		this.contributionsDirectory = new File(outputDirectory, "contributions");
 		contributionsDirectory.mkdir();
 		this.matrixDirectory = new File(outputDirectory, "transformations");
 		matrixDirectory.mkdir();
 
 		this.smp = new SphericalMaxProjection(objfile.getAbsolutePath());
 		this.nVertices = smp.getSphere().nVertices;
 		smp.saveSphere(new File(outputDirectory, "Sphere.obj").getAbsolutePath());
 
 
 		contributions = SphericalMaxProjection.loadIntData(new File(dataDirectory, "contributions.vertices").getAbsolutePath(), nVertices);
 
 		this.proj = new FullerProjection();
 		proj.prepareForProjection(smp, 1000);
 		this.gauss = new GaussianBlur();
 
 		this.overall = new Matrix4f();
 		overall.setIdentity();
 	}
 
 	private ArrayList<Point3f> tgtPts;
 
 	public void registerTimepoint(int tp) throws IOException {
 		String basename = String.format("tp%04d", tp);
		File outputfile = new File(outputDirectory, basename + "_00.vertices");
 		File contributionsfile = new File(contributionsDirectory, basename + ".vertices");
 		System.out.println("reg: src = " + new File(dataDirectory, basename + ".vertices").getAbsolutePath());
 		short[] maxima = SphericalMaxProjection.loadShortData(new File(dataDirectory, basename + "_00.vertices").getAbsolutePath(), nVertices);
 
 		if(tp == 0) {
 			SphericalMaxProjection.saveShortData(maxima, outputfile.getAbsolutePath());
 			System.out.println("reg: tgt = " + outputfile.getAbsolutePath());
 			SphericalMaxProjection.saveIntData(contributions, contributionsfile.getAbsolutePath());
 			tgtPts = getPoints(proj, gauss, maxima);
 			return;
 		}
 
 		ArrayList<Point3f> nextTgtPts = null, srcPts = null;
 		String matName = basename + ".matrix";
 		if(!new File(matrixDirectory, matName).exists()) {
 
 			srcPts = getPoints(proj, gauss, maxima);
 			if(tgtPts == null) {
 				short[] pmaxima = SphericalMaxProjection.loadShortData(new File(dataDirectory, String.format("tp%04d_00", tp-1) + ".vertices").getAbsolutePath(), nVertices);
 				tgtPts = getPoints(proj, gauss, pmaxima);
 			}
 
 			// make a deep copy of src points, to be used as target points for the next iteration
 			nextTgtPts = new ArrayList<Point3f>(srcPts.size());
 			for(Point3f p : srcPts)
 				nextTgtPts.add(new Point3f(p));
 
 			Matrix4f mat = new Matrix4f();
 			mat.setIdentity();
 			ICPRegistration.register(tgtPts, srcPts, mat, smp.center);
 			overall.mul(mat);
 			saveTransform(overall, new File(matrixDirectory, matName).getAbsolutePath());
 		} else {
 			overall = loadTransform(new File(matrixDirectory, matName).getAbsolutePath());
 		}
 
 		if(!outputfile.exists()) {
 			maxima = smp.applyTransform(overall, maxima);
 			SphericalMaxProjection.saveShortData(maxima, outputfile.getAbsolutePath());
 		}
 
 		if(!contributionsfile.exists()) {
 			int[] con = smp.applyTransformNearestNeighbor(overall, contributions);
 			SphericalMaxProjection.saveIntData(con, contributionsfile.getAbsolutePath());
 		}
 
 		tgtPts = nextTgtPts;
 	}
 
 	public void register() throws IOException {
 		// obtain list of local maxima files
 		List<String> tmp = new ArrayList<String>();
 		for(String f : dataDirectory.list())
 			if(f.startsWith("tp") && f.endsWith(".vertices"))
 				tmp.add(f);
 		String[] files = new String[tmp.size()];
 		tmp.toArray(files);
 		Arrays.sort(files);
 
 		// register
 		for(int i = 0; i < files.length; i++) {
 			int tp = Integer.parseInt(files[i].substring(2, 6));
 			registerTimepoint(tp);
 			IJ.showProgress(i, files.length);
 		}
 		IJ.showProgress(1);
 	}
 
 	public static Matrix4f loadTransform(String path) throws IOException {
 		Matrix4f ret = new Matrix4f();
 		BufferedReader in = new BufferedReader(new FileReader(path));
 		for(int r = 0; r < 4; r++)
 			for(int c = 0; c < 4; c++)
 				ret.setElement(r, c, Float.parseFloat(in.readLine()));
 		in.close();
 		return ret;
 	}
 
 	public static void saveTransform(Matrix4f matrix, String path) throws IOException {
 		PrintWriter out = new PrintWriter(new FileWriter(path));
 		for(int r = 0; r < 4; r++)
 			for(int c = 0; c < 4; c++)
 				out.println(Float.toString(matrix.getElement(r, c)));
 		out.close();
 	}
 }
