 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.plugin.PlugIn;
 import ij.process.ShortProcessor;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.vecmath.Point3f;
 
 import neo.AT;
 import neo.BaseCameraApplication;
 
 public class TwoCamera_MaxProjection implements PlugIn {
 
 	private static final int PORT = 1236;
 	private File outputdir;
 
 	@Override
 	public void run(String arg) {
 		File defaultdir = new File("D:\\SPIMdata");
 		File[] tmp = defaultdir.listFiles();
 		if(tmp != null && tmp.length != 0) {
 			Arrays.sort(tmp);
 			defaultdir = tmp[tmp.length - 1];
 		}
 		tmp = defaultdir.listFiles();
 		if(tmp != null && tmp.length != 0) {
 			Arrays.sort(tmp);
 			defaultdir = tmp[tmp.length - 1];
 		}
 
 		double pw = 0, ph = 0, pd = 0;
 		Point3f center = new Point3f();
 		float radius = 0;
 		int timepoints = 0;
 
 		GenericDialogPlus gd = new GenericDialogPlus("Spherical_Max_Projection");
 		String[] cChoice = new String[2];
 		cChoice[TwoCameraSphericalMaxProjection.CAMERA1] = "Camera 1";
 		cChoice[TwoCameraSphericalMaxProjection.CAMERA2] = "Camera 2";
 		gd.addDirectoryField("Output directory", defaultdir.getAbsolutePath());
 		gd.addChoice("Camera", cChoice, cChoice[0]);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 		outputdir = new File(gd.getNextString());
 		int camera = gd.getNextChoiceIndex();
 
 		if(!outputdir.exists() || !outputdir.isDirectory())
 			throw new RuntimeException("Output directory must be a folder");
 
 		try {
 			FileInputStream config = new FileInputStream(new File(outputdir, "SMP.xml"));
 			Properties props = new Properties();
 			props.loadFromXML(config);
 			config.close();
 
 			w = Integer.parseInt(props.getProperty("w", "0"));
 			h = Integer.parseInt(props.getProperty("h", "0"));
 			d = Integer.parseInt(props.getProperty("d", "0"));
 			pw = Double.parseDouble(props.getProperty("pw", "0"));
 			ph = Double.parseDouble(props.getProperty("ph", "0"));
 			pd = Double.parseDouble(props.getProperty("pd", "0"));
 			center.set(
 				Float.parseFloat(props.getProperty("centerx")),
 				Float.parseFloat(props.getProperty("centery")),
 				Float.parseFloat(props.getProperty("centerz")));
 			radius = (float)Double.parseDouble(props.getProperty("radius"));
 			timepoints = LabView.readInt("n timepoints");
 		} catch(Exception e) {
 		}
 
 
 		gd = new GenericDialogPlus("Spherical_Max_Projection");
 		gd.addNumericField("Timepoints", timepoints, 0);
 		gd.addNumericField("Angle Increment", 0, 0);
 		gd.addNumericField("#Angles", 1, 0);
 		gd.addNumericField("Center_x", center.x, 3);
 		gd.addNumericField("Center_y", center.y, 3);
 		gd.addNumericField("Center_z", center.z, 3);
 		gd.addNumericField("Radius", radius, 3);
 		gd.addNumericField("Width", w, 0);
 		gd.addNumericField("Height", h, 0);
 		gd.addNumericField("Depth", d, 0);
 		gd.addNumericField("Pixel_width", pw, 5);
 		gd.addNumericField("Pixel_height", ph, 5);
 		gd.addNumericField("Pixel_depth", pd, 5);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 
 		timepoints = (int)gd.getNextNumber();
 		int angleInc = (int)gd.getNextNumber();
 		nAngles = (int)gd.getNextNumber();
 		center.set(
 			(float)gd.getNextNumber(),
 			(float)gd.getNextNumber(),
 			(float)gd.getNextNumber());
 		radius = (float)gd.getNextNumber();
 		w = (int)gd.getNextNumber();
 		h = (int)gd.getNextNumber();
 		d = (int)gd.getNextNumber();
 		pw = gd.getNextNumber();
 		ph = gd.getNextNumber();
 		pd = gd.getNextNumber();
 
 
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
 
 		int timepointStart = 0;
 		int timepointInc   = 1;
 		nTimepoints    = timepoints;
 
 		toProcess = new short[w * h];
 
 		mmsmp = new TwoCameraSphericalMaxProjection(
 				outputdir.getAbsolutePath(),
 				timepointStart, timepointInc, nTimepoints,
 				camera,
 				angleInc, nAngles,
 				w, h, d,
 				pw, ph, pd,
 				center, radius);
 
 		cameraApp = new CameraApp(); // double-sided illumination} catch(Exception e) {
 	}
 
 	private TwoCameraSphericalMaxProjection mmsmp;
 	private CameraApp cameraApp;
 
 
 	private int w, h, d, nTimepoints, nAngles;
 	private short[] toProcess = null;
 	private static final boolean SAVE_RAW = false;
 	private boolean cameraAcquiring = false;
 	private final Object lock = new Object();
 
 	private void startAcq() {
 		exec.execute(new Runnable() {
 			@Override
 			public void run() {
 				int d2 = 2 * d;
 				AT at = cameraApp.getAT();
 				for(int t = 0; t < nTimepoints; t++) {
 					for(int a = 0; a < nAngles; a++) {
 						cameraAcquiring = true;
 						at.AT_SetInt("FrameCount", d2);
 						at.AT_Command("AcquisitionStart");
 						long start = System.currentTimeMillis();
 						File tpDir = null;
 						if(SAVE_RAW) {
 							tpDir = new File(outputdir, String.format("tp%04d_a%03d", t, a));
 							tpDir.mkdir();
 						}
 						for(int f = 0; f < d2; f++) {
 							at.AT_NextFrame(toProcess);
 							mmsmp.process(toProcess);
 							if(SAVE_RAW)
 								IJ.save(new ImagePlus("", new ShortProcessor(w, h, toProcess, null)), new File(tpDir, String.format("%04d.tif", f)).getAbsolutePath());
 						}
 						at.AT_Command("AcquisitionStop");
 						long end = System.currentTimeMillis();
 						System.out.println("Needed " + (end - start) + "ms  " + 1000f * d2 / (end - start) + " fps");
 						synchronized(lock) {
 							cameraAcquiring = false;
 							lock.notify();
 						}
 					}
 				}
 			}
 		});
 	}
 
 	public void waitForCamera() {
 		while(true) {
 			synchronized(lock) {
 				if(!cameraAcquiring)
 					break;
 				try {
 					lock.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void setup() throws IOException {
 		AT at = cameraApp.getAT();
 		at.AT_Flush();
 		// at.AT_SetEnumString("CycleMode", "Continuous");
 		at.AT_SetEnumString("CycleMode", "Fixed");
 		at.AT_SetEnumString("TriggerMode", "External Start");
 
 		at.AT_CreateBuffers();
 
 		startAcq();
 
 		ServerSocket server = new ServerSocket(PORT);
 
 		Socket client = server.accept();
 		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 		String line = null;
 		System.out.println("listening");
 		while((line = in.readLine()) != null) {
 			System.out.println("***" + line + "***");
 			if(line.equals("WAIT")) {
 				waitForCamera();
				client.getOutputStream().write("done\r\n".getBytes());
 			}
 		}
 		System.out.println("closing");
 		in.close();
 		client.close();
 		server.close();
 	}
 
 	public void done() {
 		cameraApp.getAT().AT_DeleteBuffers();
 	}
 
 	private final ExecutorService exec = Executors.newSingleThreadExecutor();
 
 	@SuppressWarnings("serial")
 	private final class CameraApp extends BaseCameraApplication {
 
 		private JButton process;
 
 		@Override
 		public JPanel getPanel() {
 			process = new JButton("Go!");
 			process.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					new Thread() {
 						@Override
 						public void run() {
 							try {
 								setup();
 							} catch(Exception ex) {
 								ex.printStackTrace();
 								IJ.error(ex.getMessage());
 								return;
 							}
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
