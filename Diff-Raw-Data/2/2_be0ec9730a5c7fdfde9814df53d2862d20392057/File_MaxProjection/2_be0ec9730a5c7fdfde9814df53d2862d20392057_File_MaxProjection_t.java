 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import huisken.projection.processing.TwoCameraSphericalMaxProjection;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.plugin.PlugIn;
 import ij.process.ImageProcessor;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point3f;
 
 public class File_MaxProjection implements PlugIn {
 
 	private double pw, ph, pd;
 	private int w, h, d;
 	private Point3f center;
 	private float radius;
 	private Matrix4f[] transformations;
 	private File outputdir, cam1dir, cam2dir;
 
 	private int nTimepoints, nAngles;
 	private int angleInc;
 	private boolean doublesided;
 	private boolean twocameras;
 	private double layerWidth;
 	private int nLayers;
 
	private static final String format = "tp%04d_a%03d" + File.separator + "%04d_ill%d.tif";
 
 	private File getFile(int tp, int camera, int angle, int z, int ill) {
 		File dir = camera == TwoCameraSphericalMaxProjection.CAMERA1 ? cam1dir : cam2dir;
 		return new File(dir, String.format(format, tp, angle, z, ill));
 	}
 
 	private ImageProcessor getImage(int tp, int camera, int angle, int z, int ill) {
 		String path = getFile(tp, camera, angle, z, ill).getAbsolutePath();
 		ImagePlus imp = IJ.openImage(path);
 		if(imp == null) {
 			System.out.println("Cannot open " + path);
 			return null;
 		}
 		return imp.getProcessor();
 	}
 
 	private boolean queryDirectories() throws IOException {
 		GenericDialogPlus gd = new GenericDialogPlus("File_MaxProjection");
 		gd.addDirectoryField("Directory_for_camera_1", "");
 		gd.addDirectoryField("Directory_for_camera_2", "");
 		gd.addDirectoryField("Output directory", "");
 		gd.addNumericField("Layer width", 140.00, 2);
 		gd.addNumericField("#Layers", 1, 0);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return false;
 
 		cam1dir = new File(gd.getNextString());
 		cam2dir = new File(gd.getNextString());
 		outputdir = new File(gd.getNextString());
 		layerWidth = gd.getNextNumber();
 		nLayers = (int)gd.getNextNumber();
 
 		if(!cam1dir.exists() || !cam1dir.isDirectory())
 			throw new RuntimeException(cam1dir + " is not a directory");
 		if(!cam2dir.exists() || !cam2dir.isDirectory())
 			throw new RuntimeException(cam2dir + " is not a directory");
 
 		if(!outputdir.exists())
 			outputdir.mkdir();
 
 		Properties properties = new Properties();
 		properties.load(new FileReader(new File(cam1dir, "RadialMaxProj.conf")));
 		nTimepoints = Integer.parseInt(properties.getProperty("nTimepoints"));
 		nAngles = Integer.parseInt(properties.getProperty("nAngles"));
 		angleInc = Integer.parseInt(properties.getProperty("angleInc"));
 		w = Integer.parseInt(properties.getProperty("w"));
 		h = Integer.parseInt(properties.getProperty("h"));
 		d = Integer.parseInt(properties.getProperty("d"));
 		pw = Double.parseDouble(properties.getProperty("pixelwidth"));
 		ph = Double.parseDouble(properties.getProperty("pixelheight"));
 		pd = Double.parseDouble(properties.getProperty("pixeldepth"));
 		double cx = Double.parseDouble(properties.getProperty("centerX"));
 		double cy = Double.parseDouble(properties.getProperty("centerY"));
 		double cz = Double.parseDouble(properties.getProperty("centerZ"));
 		center = new Point3f((float)cx, (float)cy, (float)cz);
 		radius = (float)Double.parseDouble(properties.getProperty("radius"));
 		doublesided = Boolean.parseBoolean(properties.getProperty("doublesided"));
 		twocameras = Boolean.parseBoolean(properties.getProperty("twocameras"));
 
 		if(nAngles > 1)
 			transformations = TwoCameraSphericalMaxProjection.loadTransformations(
 					new File(cam1dir, "transformations").getAbsolutePath());
 
 		return true;
 	}
 
 	@Override
 	public void run(String arg) {
 
 		try {
 			queryDirectories();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		for(int camera = TwoCameraSphericalMaxProjection.CAMERA1;
 				camera <= TwoCameraSphericalMaxProjection.CAMERA2;
 				camera++) {
 			mmsmp = null;
 			System.gc();
 			mmsmp = new TwoCameraSphericalMaxProjection(
 					outputdir.getAbsolutePath(),
 					camera,
 					angleInc, nAngles,
 					w, h, d,
 					pw, ph, pd,
 					center, radius,
 					layerWidth, nLayers,
 					transformations);
 			startAcq(camera);
 		}
 	}
 
 	private TwoCameraSphericalMaxProjection mmsmp;
 	private static final boolean SAVE_RAW = false;
 
 	private void startAcq(int camera) {
 		for(int t = 0; t < nTimepoints; t++) {
 			for(int a = 0; a < nAngles; a++) {
 				File tpDir = null;
 				if(SAVE_RAW) {
 					tpDir = new File(mmsmp.getOutputDirectory(), String.format("tp%04d_a%03d", t, a));
 					tpDir.mkdir();
 				}
 				for(int f = 0; f < d; f++) {
 					for(int ill = 0; ill < 2; ill++) {
 						int real_ill = doublesided ? ill : 0;
 						int real_cam = twocameras ? camera : TwoCameraSphericalMaxProjection.CAMERA1;
 						ImageProcessor ip = getImage(t, real_cam, a, f, real_ill);
 
 						mmsmp.process((short[])ip.getPixels(), t, a, f, ill);
 						if(SAVE_RAW)
 							IJ.save(new ImagePlus("", ip), new File(tpDir, String.format("%04d_ill%d.tif", f, ill)).getAbsolutePath());
 					}
 				}
 			}
 		}
 	}
 
 	public static void createConfFile() {
 		GenericDialogPlus gd = new GenericDialogPlus("Create config file");
 		gd.addNumericField("nTimepoints", 1, 0);
 		gd.addNumericField("nAngles", 1, 0);
 		gd.addNumericField("angleInc", 1, 0);
 		gd.addNumericField("w", 1, 0);
 		gd.addNumericField("h", 1, 0);
 		gd.addNumericField("d", 1, 0);
 		gd.addNumericField("pw", 1, 6);
 		gd.addNumericField("ph", 1, 6);
 		gd.addNumericField("pd", 1, 6);
 		gd.addNumericField("centerX", 1, 6);
 		gd.addNumericField("centerY", 1, 6);
 		gd.addNumericField("centerZ", 1, 6);
 		gd.addNumericField("radius", 1, 6);
 		gd.addCheckbox("doublesided", true);
 		gd.addCheckbox("twocameras", true);
 		gd.addFileField("output file", "");
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 
 		Properties properties = new Properties();
 		properties.setProperty("nTimepoints", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("nAngles", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("angleInc", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("w", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("h", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("d", Integer.toString((int)gd.getNextNumber()));
 		properties.setProperty("pixelwidth", Double.toString(gd.getNextNumber()));
 		properties.setProperty("pixelheight", Double.toString(gd.getNextNumber()));
 		properties.setProperty("pixeldepth", Double.toString(gd.getNextNumber()));
 		properties.setProperty("centerX", Double.toString(gd.getNextNumber()));
 		properties.setProperty("centerY", Double.toString(gd.getNextNumber()));
 		properties.setProperty("centerZ", Double.toString(gd.getNextNumber()));
 		properties.setProperty("radius", Double.toString(gd.getNextNumber()));
 		properties.setProperty("doublesided", Boolean.toString(gd.getNextBoolean()));
 		properties.setProperty("twocameras", Boolean.toString(gd.getNextBoolean()));
 
 		File outf = new File(gd.getNextString());
 		try {
 			properties.store(new FileWriter(outf), "bla");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		new ij.ImageJ();
 		createConfFile();
 	}
 }
