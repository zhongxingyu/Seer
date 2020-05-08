 package sculptnect;
 
 import java.awt.Frame;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.Date;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 
 import joystick.Joystick;
 import joystick.JoystickManager;
 import net.java.games.input.Controller;
 
 import org.openkinect.freenect.Context;
 import org.openkinect.freenect.DepthFormat;
 import org.openkinect.freenect.DepthHandler;
 import org.openkinect.freenect.Device;
 import org.openkinect.freenect.FrameMode;
 import org.openkinect.freenect.Freenect;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 public class Sculptnect {
 	private Context kinectContext = null;
 	private Device kinect = null;
 
 	private boolean dump = false;
 	private KinectDepthRecord depthRecord = null;
 
 	public Sculptnect() {
 		// Set up Kinect
 		kinectContext = Freenect.createContext();
 		if (kinectContext.numDevices() > 0) {
 			kinect = kinectContext.openDevice(0);
 		} else {
 			System.err.println("Error, no Kinect detected.");
 		}
 
 		GLProfile.initSingleton();
 		GLProfile glp = GLProfile.getDefault();
 
 		// Set the OpenGL canvas creation parameters
 		GLCapabilities caps = new GLCapabilities(glp);
 		caps.setRedBits(8);
 		caps.setGreenBits(8);
 		caps.setBlueBits(8);
 
 		final SculptScene scene = new SculptScene();
 
 		final Frame frame = new Frame();
		GLCanvas canvas = new GLCanvas(caps);
 		canvas.addGLEventListener(scene);
 
 		// Add and start a display link
 		final FPSAnimator animator = new FPSAnimator(canvas, 60, true);
 
 		frame.add(canvas);
 		frame.setSize(800, 800);
 		frame.setVisible(true);
 		canvas.requestFocus();
 		animator.start();
 
 		// Add listener to respond to window closing
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				super.windowClosing(e);
 				exit(0);
 			}
 		});
 
 		// Add key listener
 		canvas.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent event) {
 				switch (event.getKeyCode()) {
 				case KeyEvent.VK_ESCAPE:
 					exit(0);
 					break;
 				case KeyEvent.VK_SPACE:
 					dump = true;
 					break;
 				case 'R':
 					if (depthRecord == null) {
 						try {
 							String file = new Date().getTime() + ".raw.gz";
 							depthRecord = new KinectDepthRecord(file);
 							System.out.println("Recording started to " + file);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					} else {
 						depthRecord.close();
 						depthRecord = null;
 						System.out.println("Recording stopped");
 					}
 					break;
 				case KeyEvent.VK_LEFT:
 					scene.modifyModelRotationY(-1.0f);
 					break;
 				case KeyEvent.VK_RIGHT:
 					scene.modifyModelRotationY(1.0f);
 					break;
 				case KeyEvent.VK_UP:
 					scene.modifyModelRotationX(1.0f);
 					break;
 				case KeyEvent.VK_DOWN:
 					scene.modifyModelRotationX(-1.0f);
 					break;
 				case 'O':
 					scene.removeRandomSphere();
 					break;
 				case 'F':
 					break;
 				}
 			}
 		});
 
 		// Create mouse listener
 		MouseAdapter mouseAdapter = new MouseAdapter() {
 			int prevX, prevY;
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				prevX = e.getX();
 				prevY = e.getY();
 			}
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				scene.mouseDragged(prevX, prevY, e.getX(), e.getY());
 				prevX = e.getX();
 				prevY = e.getY();
 			}
 		};
 
 		// Add the mouse listener
 		canvas.addMouseMotionListener(mouseAdapter);
 		canvas.addMouseListener(mouseAdapter);
 
 		if (kinect != null) {
 			kinect.setDepthFormat(DepthFormat.D10BIT);
 			kinect.startDepth(new DepthHandler() {
 				@Override
 				public void onFrameReceived(FrameMode arg0, ByteBuffer arg1, int arg2) {
 					if (dump) {
 						// Dump a raw depth image
 						arg1.rewind();
 						FileOutputStream fos = null;
 						try {
 							fos = new FileOutputStream(new Date().getTime() + ".raw");
 							while (arg1.remaining() > 0) {
 								fos.write(arg1.get());
 							}
 							fos.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 						dump = false;
 					}
 
 					scene.updateKinect(arg1);
 
 					if (depthRecord != null) {
 						try {
 							depthRecord.addFrame(arg1);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			});
 		} else {
 			// Load a placeholder depth image for testing without Kinect
 			try {
 				InputStream is = getClass().getClassLoader().getResourceAsStream("kinect_depth.raw");
 				byte depth[] = new byte[640 * 480 * 2];
 				is.read(depth);
 				ByteBuffer byteBuffer = ByteBuffer.wrap(depth);
 				scene.updateKinect(byteBuffer);
 
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		// Set up Playstation 2 controller
 		Joystick joystick = JoystickManager.getJoystick(Controller.Type.STICK);
 		if (joystick != null) {
 			joystick.setJoystickListener(scene);
 		}
 	}
 
 	public void exit(int exitCode) {
 		// Clean up Kinect before exiting
 		cleanup();
 
 		System.exit(exitCode);
 	}
 
 	public void cleanup() {
 		// Shut down Kinect
 		if (kinectContext != null) {
 			if (kinect != null) {
 				kinect.close();
 			}
 		}
 		kinectContext.shutdown();
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		new Sculptnect();
 	}
 }
