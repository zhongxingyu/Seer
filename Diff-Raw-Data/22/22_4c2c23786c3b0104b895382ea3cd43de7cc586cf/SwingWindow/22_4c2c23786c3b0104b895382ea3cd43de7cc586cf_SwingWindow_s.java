 package com.summ.sge.graphics.engine.software;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 
 import com.summ.sge.graphics.core.IFrameListener;
 import com.summ.sge.graphics.core.IKeyboardListener;
 import com.summ.sge.graphics.core.IMouseListener;
 import com.summ.sge.graphics.core.Window;
 
 public class SwingWindow extends Window {
 
	private static final int FRAME_RATE = 60;

 	private SwingRenderer mRenderer;
 
 	private IFrameListener mFrameListener;
 
 	private JFrame mFrame;
 	private SwingCanvas mCanvas;
 
 	public SwingWindow(IFrameListener frameListener) {
 		if (frameListener == null) throw new IllegalArgumentException("Frame listener cannot be null");
 
 		mRenderer = new SwingRenderer(this);
 		mFrameListener = frameListener;
 	}
 
 	@Override
 	public void open() {
 		mRenderer.start();
 	}
 
 	@Override
 	public void onInit() {
 		mFrame = new JFrame();
 		mFrame.setBounds(300, 200, 800, 600);
 		mFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		mFrame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.out.println("window closing...");
 				mRenderer.stop();
 			}
 		});
 		mFrame.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				int action = com.summ.sge.graphics.core.KeyEvent.KEY_RELEASED;
 				int key;
 
 				switch (e.getKeyCode()) {
 				case KeyEvent.VK_A:
 					key = com.summ.sge.graphics.core.KeyEvent.VK_A;
 					break;
 				case KeyEvent.VK_O:
 					key = com.summ.sge.graphics.core.KeyEvent.VK_O;
 					break;
 				case KeyEvent.VK_ESCAPE:
 					key = com.summ.sge.graphics.core.KeyEvent.VK_ESCAPE;
 					break;
 				case KeyEvent.VK_SPACE:
 					key = com.summ.sge.graphics.core.KeyEvent.VK_SPACE;
 					break;
 				default:
 					key = com.summ.sge.graphics.core.KeyEvent.VK_NONE;
 					break;
 				}
 
 				for (IKeyboardListener listener : mKeyboardListeners) {
 					listener.onKeyboardEvent(new com.summ.sge.graphics.core.KeyEvent(action, key));
 				}
 			}
 		});
 
 		mCanvas = new SwingCanvas(800, 600);
 		mCanvas.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				int action = com.summ.sge.graphics.core.MouseEvent.MOUSE_RELEASED;
 				int key;
 
 				switch (e.getButton()) {
 				case MouseEvent.BUTTON1:
 					key = com.summ.sge.graphics.core.MouseEvent.BUTTON1;
 					break;
 				case MouseEvent.BUTTON2:
 					key = com.summ.sge.graphics.core.MouseEvent.BUTTON2;
 					break;
 				case MouseEvent.BUTTON3:
 					key = com.summ.sge.graphics.core.MouseEvent.BUTTON3;
 					break;
 				default:
 					key = com.summ.sge.graphics.core.MouseEvent.NOBUTTON;
 					break;
 				}
 
 				for (IMouseListener listener : mMouseListeners) {
 					listener.onMouseEvent(new com.summ.sge.graphics.core.MouseEvent(action, key, e.getX(), e.getY()));
 				}
 			}
 		});
 
 		mFrame.add(mCanvas);
 		mFrame.setVisible(true);
 
 		mFrameListener.onCreate();
 	}
 
 	@Override
 	public void onDraw() {
 		mFrame.setTitle("FPS: " + mCanvas.getFps());
 
 		mCanvas.clear();
 
 		mFrameListener.onDrawFrame();
 
 		mCanvas.swapBuffers();
 	}
 
 	@Override
 	public void onDestroy() {
 		mFrameListener.onDestroying();
 		mFrame.setVisible(false);
 		mFrameListener.onDestroy();
 	}
 }
