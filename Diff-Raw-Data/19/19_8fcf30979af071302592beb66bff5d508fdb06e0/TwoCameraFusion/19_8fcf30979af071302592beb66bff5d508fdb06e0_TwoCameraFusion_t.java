 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import ij.IJ;
 import ij.plugin.PlugIn;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.vecmath.Point3f;
 
 public class TwoCameraFusion implements PlugIn {
 
 	private static final int LEFT    = TwoCameraSphericalMaxProjection.LEFT;
 	private static final int RIGHT   = TwoCameraSphericalMaxProjection.RIGHT;
 	private static final int CAMERA1 = TwoCameraSphericalMaxProjection.CAMERA1;
 	private static final int CAMERA2 = TwoCameraSphericalMaxProjection.CAMERA2;
 
 	@Override
 	public void run(String args) {
 		GenericDialogPlus gd = new GenericDialogPlus("Fuse from 2 cameras");
 		gd.addDirectoryField("Input folder", "");
 		gd.addNumericField("#angles", 1, 0);
 		gd.addNumericField("#angleInc", 45, 0);
 		gd.addCheckbox("Adjust modes to compensate for intensity differences", false);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 		try {
 			fuse(gd.getNextString(), (int)gd.getNextNumber(), (int)gd.getNextNumber(), gd.getNextBoolean(), true);
 		} catch(Exception e) {
 			IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private String inputdir;
 	private String outputdir;
 	private SphericalMaxProjection smp;
 	private FusionWeight[][][] weights;
 	private boolean saveOutput;
 	private int angleInc;
 	private int nAngles;
 
 	private static final String format = "tp%04d_a%04d_ill%d.vertices";
 	private static final boolean adjustModes = false;
 
	public void prepareFusion(String indir, int nAngles, int angleInc, boolean saveOutput) throws IOException {
 		if(!indir.endsWith(File.separator))
 			indir += File.separator;
 		this.inputdir = indir;
 		this.saveOutput = saveOutput;
		this.smp = new SphericalMaxProjection(inputdir + "Sphere.obj");
 		this.outputdir = inputdir + "fused" + File.separator;
 		this.angleInc = angleInc;
 		this.nAngles = nAngles;
 
 		// see TwoCameraSphericalMaxProjection.initSphericalMaximumProjection
 		int aperture = 90 / nAngles;
 
 		if(saveOutput) {
 			if(!new File(outputdir).exists())
 				new File(outputdir).mkdirs();
 
 			smp.saveSphere(outputdir + "Sphere.obj");
 		}
 
 		Point3f center = smp.getCenter();
 
 		weights = new FusionWeight[2][2][nAngles];
 
 		for(int a = 0; a < nAngles; a++) {
 			weights[CAMERA1][LEFT] [a] = new AngleWeighter2(AngleWeighter2.X_AXIS,  135 + a * angleInc, aperture, center);
 			weights[CAMERA1][RIGHT][a] = new AngleWeighter2(AngleWeighter2.X_AXIS, -135 + a * angleInc, aperture, center);
 			weights[CAMERA2][LEFT] [a] = new AngleWeighter2(AngleWeighter2.X_AXIS,   45 + a * angleInc, aperture, center);
 			weights[CAMERA2][RIGHT][a] = new AngleWeighter2(AngleWeighter2.X_AXIS,  -45 + a * angleInc, aperture, center);
 		}
 	}
 
 	public float[] fuse(int tp) throws IOException {
 		File out = new File(outputdir, String.format("tp%04d.vertices", tp));
 		if(out.exists())
 			return null;
 
 		int nVertices = smp.getSphere().nVertices;
 
 		float[][][][] m = new float[2][2][nAngles][];
 		for(int a = 0; a < nAngles; a++) {
 			m[CAMERA1][LEFT] [a] = SphericalMaxProjection.loadFloatData(inputdir + String.format(format, tp, 180 + a * angleInc, LEFT),  nVertices);
 			m[CAMERA1][RIGHT][a] = SphericalMaxProjection.loadFloatData(inputdir + String.format(format, tp, 180 + a * angleInc, RIGHT), nVertices);
 			m[CAMERA2][LEFT] [a] = SphericalMaxProjection.loadFloatData(inputdir + String.format(format, tp,   0 + a * angleInc, LEFT),  nVertices);
 			m[CAMERA2][RIGHT][a] = SphericalMaxProjection.loadFloatData(inputdir + String.format(format, tp,   0 + a * angleInc, RIGHT), nVertices);
 		}
 
 
 		if(adjustModes) {
 			float refmode = SphericalMaxProjection.getMode(m[CAMERA1][LEFT][0]);
 			for(int a = 0; a < nAngles; a++) {
 				float[] data = m[CAMERA1][LEFT][a];
 				float mode = SphericalMaxProjection.getMode(data);
 				SphericalMaxProjection.add(data, refmode - mode);
 
 				data = m[CAMERA1][RIGHT][a];
 				mode = SphericalMaxProjection.getMode(data);
 				SphericalMaxProjection.add(data, refmode - mode);
 
 				data = m[CAMERA2][LEFT][a];
 				mode = SphericalMaxProjection.getMode(data);
 				SphericalMaxProjection.add(data, refmode - mode);
 
 				data = m[CAMERA2][RIGHT][a];
 				mode = SphericalMaxProjection.getMode(data);
 				SphericalMaxProjection.add(data, refmode - mode);
 			}
 		}
 
 		Point3f[] vertices = smp.getSphere().getVertices();
 		float[] res = new float[vertices.length];
 		for(int v = 0; v < vertices.length; v++) {
 			Point3f vertex = vertices[v];
 			float sum = 0;
 			res[v] = 0;
 			for(int a = 0; a < nAngles; a++) {
 				float w1 = weights[CAMERA1][LEFT ][a].getWeight(vertex.x, vertex.y, vertex.z);
 				float w2 = weights[CAMERA1][RIGHT][a].getWeight(vertex.x, vertex.y, vertex.z);
 				float w3 = weights[CAMERA2][LEFT ][a].getWeight(vertex.x, vertex.y, vertex.z);
 				float w4 = weights[CAMERA2][RIGHT][a].getWeight(vertex.x, vertex.y, vertex.z);
				float m1 = m[CAMERA1][LEFT ][a][v];
				float m2 = m[CAMERA1][RIGHT][a][v];
				float m3 = m[CAMERA2][LEFT ][a][v];
				float m4 = m[CAMERA2][RIGHT][a][v];
 				sum += w1 + w2 + w3 + w4;
				res[v] += (w1 * m1 + w2 * m2 + w3 * m3 + w4 * m4);
 			}
 			if(sum != 1)
 				System.out.println("sum = " + sum);
 		}
 		if(saveOutput)
 			SphericalMaxProjection.saveFloatData(res, out.getAbsolutePath());
 		return res;
 	}
 
 	public static void fuse(String indir, int nAngles, int angleInc, final  boolean adjustModes, boolean saveOutput) throws IOException {
 		if(!indir.endsWith(File.separator))
 			indir += File.separator;
 		final String inputdir = indir;
 
 		// final SphericalMaxProjection smp = new SphericalMaxProjection(inputdir + "Sphere.obj");
 
 		final TwoCameraFusion tcf = new TwoCameraFusion();
 		tcf.prepareFusion(inputdir, nAngles, angleInc, saveOutput);
 
 		final Set<Integer> tps = new TreeSet<Integer>();
 		for(File f : new File(inputdir).listFiles()) {
 			String name = f.getName();
 			if(name.startsWith("tp") && name.endsWith(".vertices")) {
 				int n = Integer.parseInt(name.substring(2, 6));
 				tps.add(n);
 			}
 		}
 
 
 		int nTimepoints = tps.size();
 		final ArrayList<Integer> timepoints = new ArrayList<Integer>(tps);
 
 		final int nProcessors = Runtime.getRuntime().availableProcessors();
 		ExecutorService exec = Executors.newFixedThreadPool(nProcessors);
 		int nTimepointsPerThread = (int)Math.ceil(nTimepoints / (double)nProcessors);
 		for(int p = 0; p < nProcessors; p++) {
 			final int start = p * nTimepointsPerThread;
 			final int end = Math.min(nTimepoints, (p + 1) * nTimepointsPerThread);
 
 			exec.submit(new Runnable() {
 				@Override
 				public void run() {
 					for(int tp = start; tp < end; tp++) {
 						try {
 							IJ.log("Fusing timepoint " + tp);
 							tcf.fuse(timepoints.get(tp));
 						} catch(Exception e) {
 							e.printStackTrace();
 							System.out.println("Couldn't fuse timepoint " + tp);
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
 }
