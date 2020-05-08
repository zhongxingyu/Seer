 package huisken.various;
 
 import huisken.network.ImageProvider;
 import huisken.network.ImageReceiver;
 import ij.ImagePlus;
 import ij.process.ByteProcessor;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import neo.AbstractCameraApplication;
 
 
 @SuppressWarnings("serial")
 public class DynamicColorMerge extends AbstractCameraApplication {
 
 	private JButton merge;
 	private JCheckBox serverCB;
 	private JTextField serverTF;
 	private JTextField portTF;
 	private JTextField minTF, maxTF;
 
 	private Thread thread;
 	private boolean running = false;
 
 	public DynamicColorMerge() {
 		super();
 	}
 
 	@Override
 	public JPanel getPanel() {
 		merge = new JButton("Merge");
 		merge.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toggleMerge();
 			}
 		});
 		serverTF = new JTextField("localhost");
 		portTF = new JTextField("4444");
 		serverCB = new JCheckBox("Server mode?");
 		minTF = new JTextField("0");
 		minTF.setColumns(5);
 		maxTF = new JTextField("4000");
 		maxTF.setColumns(5);
 
 
 		// Initialize the GUI
 		JPanel mergePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		mergePanel.add(serverCB);
 		mergePanel.add(serverTF);
 		mergePanel.add(portTF);
 		mergePanel.add(new JLabel("Min"));
 		mergePanel.add(minTF);
 		mergePanel.add(new JLabel("Max"));
 		mergePanel.add(maxTF);
 		mergePanel.add(merge);
 		mergePanel.setName("Merging");
 		return mergePanel;
 	}
 
 	public void toggleMerge() {
 		if(!running) {
 			thread = new Thread() {
 				@Override
 				public void run() {
 					running = true;
 					merge.setText("Stop");
 					String host = serverTF.getText();
 					int port = Integer.parseInt(portTF.getText());
 					boolean isServer = serverCB.isSelected();
 					if(isServer)
 						startServer(port);
 					else
 						startClient(host, port);
 				}
 			};
 			thread.start();
 		} else {
 			running = false;
 			try {
 				thread.join();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 			merge.setText("Merge");
 		}
 	}
 
 	public void startServer(final int port) {
 		// start camera
 		final ImageProvider provider = new ImageProvider();
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					provider.run(port);
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 		// start camera, each time an image is received, call provider.setImage();
 		int aw = at.AT_GetInt("AOIWidth");
 		int ah = at.AT_GetInt("AOIHeight");
 		int nPixels = aw * ah;
 		short[] pixels = new short[nPixels];
 		byte[] gray = new byte[nPixels];
 		ImageProcessor ip = new ByteProcessor(aw, ah, gray, null);
 		ImagePlus image = new ImagePlus("", ip);
 
 		at.startPreview();
 		while(running) {
 			int min = 0;
 			int max = 4000;
 			try {
 				min = Integer.parseInt(minTF.getText());
 				max = Integer.parseInt(maxTF.getText());
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 			at.nextPreviewImage(pixels);
 			convertTo8(pixels, gray, min, max);
 			provider.setImage(image);
 			System.out.println("server: provide next image");
 		}
 		at.finishPreview();
 	}
 
 	public void startClient(final String host, final int port) {
 		System.out.println("Starting client");
 		// start camera
 		final ImageReceiver receiver = new ImageReceiver();
 		try {
 			receiver.start(host, port);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		// start camera, each time an image is received, call provider.setImage();
 		int aw = at.AT_GetInt("AOIWidth");
 		int ah = at.AT_GetInt("AOIHeight");
 		int nPixels = aw * ah;
 		short[] pixels = new short[nPixels];
 		byte[] gray = new byte[nPixels];
 		ImageProcessor ip = new ByteProcessor(aw, ah, gray, null);
 
 		ImageProcessor merged = new ColorProcessor(aw, ah);
 		ImagePlus result = new ImagePlus("Merge", merged);
 		result.show();
 
 		at.startPreview();
 		while(running) {
 			int min = 0;
 			int max = 4000;
 			try {
 				min = Integer.parseInt(minTF.getText());
 				max = Integer.parseInt(maxTF.getText());
 			} catch(Exception e) {
 			}
 			at.nextPreviewImage(pixels);
 			convertTo8(pixels, gray, min, max);
 			System.out.println("Received next image from camera");
 			ImagePlus second = null;
 			try {
 				second = receiver.getImage();
 				System.out.println("Received next image from server: w = " + second.getWidth() + " h = " + second.getHeight());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			ByteProcessor r = (ByteProcessor)ip;
 			ByteProcessor g = (ByteProcessor)second.getProcessor();
 			int w = r.getWidth();
 			int h = r.getHeight();
 			int wh = w * h;
 			for(int i = 0; i < wh; i++) {
 				int red   = r.get(i);
 				int green = g.get(i);
 
 				int merge = 0xff000000 + (red << 16) + (green << 8);
 				merged.set(i, merge);
 			}
 			result.updateAndDraw();
 		}
 		try {
 			receiver.stop();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		at.finishPreview();
 	}
 
 	private static final void convertTo8(short[] in, byte[] out, int min, int max) {
 		double scale = 256.0 / (max - min + 1);
 		for(int i = 0; i < in.length; i++) {
 			int v = in[i] & 0xffff;
			v = (in[i] - min) / (max - min) * 255;
 			out[i] = (byte)v;
 		}
 	}
 }
