 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import ij.IJ;
 import ij.plugin.PlugIn;
 import ij.process.ShortProcessor;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Properties;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.vecmath.Point3f;
 
 import neo.AT;
 import neo.BaseCameraApplication;
 
 public class TwoCamera_MaxProjection implements PlugIn {
 
 	@Override
 	public void run(String arg) {
 		String[] cChoice = new String[] {"Camera 1", "Camera 2" };
 		GenericDialogPlus gd = new GenericDialogPlus("Spherical_Max_Projection");
 		gd.addDirectoryField("Output directory", "D:\\SPIMdata");
 //		gd.addNumericField("Timepoints", 0, 0);
 		gd.addChoice("Camera", cChoice, cChoice[0]);
 //		gd.addNumericField("Center_x", 0, 3);
 //		gd.addNumericField("Center_y", 0, 3);
 //		gd.addNumericField("Center_z", 0, 3);
 //		gd.addNumericField("Radius", 0, 3);
 //		gd.addNumericField("Width", 0, 0);
 //		gd.addNumericField("Height", 0, 0);
 //		gd.addNumericField("Depth", 0, 0);
 //		gd.addNumericField("Pixel_width", 1, 5);
 //		gd.addNumericField("Pixel_height", 1, 5);
 //		gd.addNumericField("Pixel_depth", 1, 5);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 
 		File outputdir = new File(gd.getNextString());
 //		int timepoints = (int)gd.getNextNumber();
 		int camera = gd.getNextChoiceIndex();
 //		Point3f center = new Point3f(
 //			(float)gd.getNextNumber(),
 //			(float)gd.getNextNumber(),
 //			(float)gd.getNextNumber());
 //		float radius = (float)gd.getNextNumber();
 //		int w = (int)gd.getNextNumber();
 //		int h = (int)gd.getNextNumber();
 //		int d = (int)gd.getNextNumber();
 //		double pw = gd.getNextNumber();
 //		double ph = gd.getNextNumber();
 //		double pd = gd.getNextNumber();
 
 		if(!outputdir.exists() || !outputdir.isDirectory())
 			throw new RuntimeException("Output directory must be a folder");
 
 		try {
 			FileInputStream config = new FileInputStream(new File(outputdir, "SMP.xml"));
 			Properties props = new Properties();
 			props.loadFromXML(config);
 			config.close();
 
 			int w = Integer.parseInt(props.getProperty("w"));
 			int h = Integer.parseInt(props.getProperty("h"));
 			int d = Integer.parseInt(props.getProperty("d"));
 			double pw = Double.parseDouble(props.getProperty("pw"));
 			double ph = Double.parseDouble(props.getProperty("ph"));
 			double pd = Double.parseDouble(props.getProperty("pd"));
 			Point3f center = new Point3f(
 				Float.parseFloat(props.getProperty("centerx")),
 				Float.parseFloat(props.getProperty("centery")),
 				Float.parseFloat(props.getProperty("centerz")));
 			float radius = (float)Double.parseDouble(props.getProperty("radius"));
 			int timepoints = LabView.readInt("n timepoints");
 
 			// account for double-sided illumination:
 			d /= 2;
			pd *= 2;
 
 			System.out.println(w);
 			System.out.println(h);
 			System.out.println(d);
 			System.out.println(pw);
 			System.out.println(ph);
 			System.out.println(pd);
 			System.out.println(center);
 			System.out.println(radius);
 			System.out.println(timepoints);
 
 			process(outputdir.getAbsolutePath(), timepoints, camera, center, radius, w, h, d, pw, ph, pd);
 		} catch(Exception e) {
 			IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private TwoCameraSphericalMaxProjection mmsmp;
 
 	public void process(String outputdir, int timepoints, int camera, Point3f center, float radius, int w, int h, int d, double pw, double ph, double pd) {
 
 		int timepointStart = 0;
 		int timepointInc   = 1;
 		int nTimepoints    = timepoints;
 
 		mmsmp = new TwoCameraSphericalMaxProjection(
 				outputdir,
 				timepointStart, timepointInc, nTimepoints,
 				camera,
 				w, h, d,
 				pw, ph, pd,
 				center, radius);
 
 		startCamera(timepoints * d * 2); // double-sided illumination
 	}
 
 	public void startCamera(int frames) {
 		new CameraApp(frames);
 	}
 
 	public void go(AT at, int framecount) {
 		at.AT_SetEnumString("CycleMode", "Fixed");
 		at.AT_SetEnumString("ElectronicShutteringMode", "Global");
 		at.AT_SetInt("FrameCount", framecount);
 		at.AT_SetBool("SensorCooling", true);
 		at.AT_SetEnumString("PixelReadoutRate", "280 MHz");
 		at.AT_SetEnumString("TriggerMode", "External");
 
 		int w = at.AT_GetInt("AOIWidth");
 		int h = at.AT_GetInt("AOIHeight");
 		short[] data = new short[w * h];
 
 		at.AT_CreateBuffers();
 		at.AT_Command("AcquisitionStart");
 		for(int f = 0; f < framecount; f++) {
 			at.AT_NextFrame(data);
 			mmsmp.process(new ShortProcessor(w, h, data, null));
 		}
 		at.AT_Command("AcquisitionStop");
 		at.AT_DeleteBuffers();
 	}
 
 	@SuppressWarnings("serial")
 	private final class CameraApp extends BaseCameraApplication {
 
 		private JButton process;
 		private final int frames;
 
 		public CameraApp(int frames) {
 			super();
 			this.frames = frames;
 		}
 
 		@Override
 		public JPanel getPanel() {
 			process = new JButton("Go!");
 			process.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					new Thread() {
 						@Override
 						public void run() {
 							go(at, frames);
 						}
 					}.start();
 				}
 			});
 
 			// Initialize the GUI
 			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 			panel.add(process);
 			panel.setName("Projection");
 			return panel;
 		}
 	}
 }
