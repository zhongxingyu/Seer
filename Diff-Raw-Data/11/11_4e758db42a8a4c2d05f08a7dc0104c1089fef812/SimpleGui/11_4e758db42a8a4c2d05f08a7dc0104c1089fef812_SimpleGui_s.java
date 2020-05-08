 /*
  * [gui4gl] OpenGL game-oriented GUI library
  * 
  * Copyright (C) 2003 Tako Schotanus
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation; either version 2.1 of the License, or (at your
  * option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  * Created on Nov 17, 2003
  */
 package examples.gui4gl;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.text.NumberFormat;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import org.codejive.gui4gl.GLText;
 import org.codejive.gui4gl.events.GuiActionEvent;
 import org.codejive.gui4gl.events.GuiActionListener;
 import org.codejive.gui4gl.events.GuiChangeEvent;
 import org.codejive.gui4gl.events.GuiChangeListener;
 import org.codejive.gui4gl.events.GuiKeyAdapter;
 import org.codejive.gui4gl.events.GuiKeyEvent;
 import org.codejive.gui4gl.themes.Theme;
 import org.codejive.gui4gl.widgets.Button;
 import org.codejive.gui4gl.widgets.Image;
 import org.codejive.gui4gl.widgets.Screen;
 import org.codejive.gui4gl.widgets.ScrollBar;
 import org.codejive.gui4gl.widgets.ScrollContainer;
 import org.codejive.gui4gl.widgets.Text;
 import org.codejive.gui4gl.widgets.TextField;
 import org.codejive.gui4gl.widgets.Toggle;
 import org.codejive.gui4gl.widgets.ValueBar;
 import org.codejive.gui4gl.widgets.Window;
 import org.codejive.utils4gl.FrameRateCounter;
 import org.codejive.utils4gl.RenderContext;
 import org.codejive.utils4gl.SimpleFrameRateCounter;
 import org.codejive.utils4gl.textures.Texture;
 import org.codejive.utils4gl.textures.TextureReader;
 
 import net.java.games.jogl.*;
 
 /**
  * @author tako
 * @version $Revision: 254 $
  */
 public class SimpleGui implements GLEventListener {
 	GLDisplay m_display;
 	FrameRateCounter m_fpsCounter;
 	
 	RenderContext m_context;
 	Screen m_screen;
 	MenuWindow m_menuWindow;
 	InfoWindow m_infoWindow;
 	TestWindow m_testWindow;
 	
 	public static void main(String[] args) {
 		GLDisplay display = GLDisplay.createGLDisplay("Simple gui4gl example");
 		display.addGLEventListener(new SimpleGui(display));
 		display.start();
 	}
 
 	public SimpleGui(GLDisplay _display) {
 		m_display = _display;
 	}
 	
 	public void display(GLDrawable gLDrawable) {
 		final GL gl = gLDrawable.getGL();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 
 		m_fpsCounter.addFrame();
 		m_infoWindow.setFps(m_fpsCounter.getFrameRate());
 		
 		m_screen.render(m_context, null);
 	}
 
 	public void displayChanged(GLDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
 		// Not used
 	}
 
 	public void init(GLDrawable gLDrawable) {
 		final GL gl = gLDrawable.getGL();
 		final GLU glu = gLDrawable.getGLU();
 
 		gl.glEnable(GL.GL_CULL_FACE);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LEQUAL);
 		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 		gl.glEnable(GL.GL_NORMALIZE);
 
 		// Set up a render context
 		m_context = new RenderContext(gl, glu);
 		// Determine the Theme to use
 		Theme.setDefaultTheme(m_context);
 		// Create a screen that will hold all our windows
 		m_screen = new Screen();
 
 		// Create our first window
 		m_menuWindow = new MenuWindow();
 		// Make sure it's visible
 		m_menuWindow.setVisible(true);
 		// Add the window to the screen
 		m_screen.add(m_menuWindow);
 		// Activate the window making sure it will receive key presses
 		m_menuWindow.activate();
 		
 		// Create a second window
 		m_infoWindow = new InfoWindow();
 		m_infoWindow.setVisible(true);
 		m_screen.add(m_infoWindow);
 		
 		// Create a third window
 		m_testWindow = new TestWindow(m_context);
 		m_testWindow.setVisible(true);
 		m_screen.add(m_testWindow);
 		
 		// If we're interested in interactive windows the screen
 		// must receive the appriopriate events
 		gLDrawable.addKeyListener(m_screen);
 		gLDrawable.addMouseListener(m_screen);
 		gLDrawable.addMouseMotionListener(m_screen);
 		
 		// If we want to get any keys the GUI doesn't handle
 		// (no windows active for example) we need to register
 		// a listener for them
 		m_screen.addKeyListener(new GuiKeyAdapter() {
 			public void keyPressed(GuiKeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					m_display.stop();
 				}
 			}
 		});
 
 		// Initialize the GUI for subsequent rendering
 		m_screen.initRendering(m_context);
 		
 		// Create an frame rate counter
 		m_fpsCounter = new SimpleFrameRateCounter();
 	}
 
 	public void reshape(GLDrawable gLDrawable, int x, int y, int width, int height) {
 		final GL gl = gLDrawable.getGL();
 		final GLU glu = gLDrawable.getGLU();
 
 		if (height <= 0) // avoid a divide by zero error!
 			height = 1;
 		final float h = (float)width / (float)height;
 		gl.glViewport(0, 0, width, height);
 		gl.glMatrixMode(GL.GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluPerspective(45.0f, h, 1.0, 20.0);
 		gl.glMatrixMode(GL.GL_MODELVIEW);
 		gl.glLoadIdentity();
 
 		// Update the GUI because of the new dimensions
 		m_screen.updateRendering(m_context);
 	}
 
 
 	class MenuWindow extends Window {
 		public MenuWindow() {
 			super("Test Window");
 			setCenterParent(true);
 			setWidth(300);
 			setHeight(220);
 		
 			Text t = new Text("This text is being displayed inside a Text widget. Below this widget you can see several buttons");
 			t.setBounds(5, 5, 290, 40);
 			add(t);
 			
 			Button b = new Button("Resume");
 			b.setBounds(5, 45, 290, 20);
 			add(b);
 			b = new Button("Options");
 			b.setBounds(5, 65, 290, 20);
 			b.setEnabled(false);
 			add(b);
 /*
 // BEGIN TEST
 b.addKeyListener(new GuiKeyAdapter() {
 	public void keyTyped(GuiKeyEvent _event) {
 		Button button = (Button)_event.getSource();
 		switch (_event.getKeyChar()) {
 			case 'a':
 				button.setWidth(button.getWidth() - 1);
 				_event.consume();
 				break;
 			case 'd':
 				button.setWidth(button.getWidth() + 1);
 				_event.consume();
 				break;
 			case 'w':
 				button.setHeight(button.getHeight() - 1);
 				_event.consume();
 				break;
 			case 's':
 				button.setHeight(button.getHeight() + 1);
 				_event.consume();
 				break;
 		}
 	}
 });
 // END TEST
 */
 			b = new Button("Exit this example");
 			b.setBounds(5, 85, 290, 20);
 			b.addActionListener(new GuiActionListener() {
 				public void actionPerformed(GuiActionEvent _event) {
 					m_display.stop();
 				}
 			});
 			add(b);
 			
 			Toggle tg = new Toggle("Show info window", true);
 			tg.setBounds(5, 105, 290, 20);
 			add(tg);
 			tg.addChangeListener(new GuiChangeListener() {
 				public void stateChanged(GuiChangeEvent _event) {
 					Boolean value = (Boolean)_event.getValue();
 					m_infoWindow.setVisible(value.booleanValue());
 				}
 			});
 			
 			ValueBar hb = new ValueBar(0.0f, 100.0f, 5.0f, true, GLText.ALIGN_CENTER);
 			hb.setBounds(5, 130, 290, 12);
 			add(hb);
 			hb.addChangeListener(new GuiChangeListener() {
 				public void stateChanged(GuiChangeEvent _event) {
 					Float value = (Float)_event.getValue();
 					m_infoWindow.setValue(value.floatValue());
 				}
 			});
 	
 //			ValueBar vb = new ValueBar(0.0f, 100.0f, 5.0f, true, GLText.ALIGN_RIGHT);
 //			vb.setBounds(310, 5, 25, 150);
 //			add(vb);			
 			
 			ScrollBar sb = new ScrollBar(100, 10);
 			sb.setBounds(5, 145, 290, 12);
 			add(sb);
 			
 			TextField tf = new TextField("Edit me");
 			tf.setBounds(5, 165, 290, 20);
 			add(tf);
 
 			addKeyListener(new GuiKeyAdapter() {
 				public void keyPressed(GuiKeyEvent _event) {
 					switch (_event.getKeyCode()) {
 						case KeyEvent.VK_ESCAPE:
 							m_menuWindow.setVisible(false);
 							_event.consume();
 							break;
 					}
 				}
 			});
 		}
 	}
 
 	class InfoWindow extends Window {
 		Text m_fps, m_value, m_liveCount, m_mortalCount;
 		ValueBar m_gfpsHorizontal, m_gfpsVertical;
 	
 		public InfoWindow() {
 			setBounds(10, 340, 150, 100);
 			setFocusable(false);
 			setDraggable(false);
 		
 			Text t = new Text("FPS");
 			t.setBounds(5, 5, 75, 20);
 			add(t);
 			m_fps = new Text("?");
 			m_fps.setBounds(80, 5, 60, 20);
 			add(m_fps);
 
 			t = new Text("value");
 			t.setBounds(5, 25, 75, 20);
 			add(t);
 			m_value = new Text("?");
 			m_value.setBounds(80, 25, 60, 20);
 			add(m_value);
 
 			t = new Text("#live");
 			t.setBounds(5, 45, 75, 20);
 			add(t);
 			m_liveCount = new Text("?");
 			m_liveCount.setBounds(80, 45, 60, 20);
 			add(m_liveCount);
 
 			t = new Text("#mortal");
 			t.setBounds(5, 65, 75, 20);
 			add(t);
 			m_mortalCount = new Text("?");
 			m_mortalCount.setBounds(80, 65, 60, 20);
 			add(m_mortalCount);
 			
 			m_gfpsHorizontal = new ValueBar(0,700);
 			m_gfpsHorizontal.setBounds(5,85, 100, 10);
 			add(m_gfpsHorizontal);
 
 			m_gfpsVertical = new ValueBar(0,700);
 			m_gfpsVertical.setBounds(130,5, 10, 80);
 			//m_gfpsVertical.setFillColor(null); // Would leave out the background fill 
 			add(m_gfpsVertical);
 			
 		}
 	
 		public void setFps(float _fFps) {
 			NumberFormat nf = NumberFormat.getNumberInstance();
 			nf.setMinimumFractionDigits(1);
 			nf.setMaximumFractionDigits(1);
 			Float f = new Float(_fFps);
 			m_fps.setText(nf.format(f));
 			m_gfpsHorizontal.setValue(_fFps);
 			m_gfpsVertical.setValue(_fFps);
 		}
 	
 		public void setValue(float _fValue) {
 			NumberFormat nf = NumberFormat.getNumberInstance();
 			nf.setMinimumFractionDigits(1);
 			nf.setMaximumFractionDigits(1);
 			Float f = new Float(_fValue);
 			m_value.setText(nf.format(f));
 		}
 	
 		public void setLiveCount(String _sCount) {
 			m_liveCount.setText(_sCount);
 		}
 	
 		public void setMortalCount(String _sCount) {
 			m_mortalCount.setText(_sCount);
 		}
 	}
 
 	class TestWindow extends Window {
 		Text m_fps, m_value, m_liveCount, m_mortalCount;
 		ValueBar m_gfpsHorizontal, m_gfpsVertical;
 		
 		public TestWindow(RenderContext _context) {
 			super("Test Window");
 			setBounds(475, 10, 150, 125);
 
 			Texture img = null;
 			try {
 				img = TextureReader.readTexture(_context, "examples/gui4gl/toucan.png", true);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Image imgw = new Image(img);
			imgw.setBounds(0, 0, 280, 180);
//			imgw.setBounds(5, 5, 140, 90);
 //			imgw.setBackgroundColor(1.0f, 0.0f, 0.0f);
 
 			ScrollContainer sc = new ScrollContainer(imgw);
 			sc.setBounds(0, 0, 150, 100);
 			add(sc);
 		}
 	}
 }
 
 class GLDisplay {
 	private static final int DEFAULT_WIDTH = 640;
 	private static final int DEFAULT_HEIGHT = 480;
 
 	private static final int DONT_CARE = -1;
 
 	private JFrame frame;
 	private GLCanvas glCanvas;
 	private Animator animator;
 	private boolean fullscreen;
 	private int width;
 	private int height;
 	private GraphicsDevice usedDevice;
 
 	public static GLDisplay createGLDisplay(String title) {
 		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 		boolean fullscreen = false;
 		if (device.isFullScreenSupported()) {
 			int selectedOption =
 				JOptionPane.showOptionDialog(
 					null,
 					"How would you like to run this example?",
 					title,
 					JOptionPane.YES_NO_OPTION,
 					JOptionPane.QUESTION_MESSAGE,
 					null,
 					new Object[] { "Fullscreen", "Windowed" },
 					"Windowed");
 			fullscreen = selectedOption == 0;
 		}
 		return new GLDisplay(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, fullscreen);
 	}
 
 	public GLDisplay(String title, boolean _fullscreen) {
 		this(title, DEFAULT_WIDTH, DEFAULT_HEIGHT, _fullscreen);
 	}
 
 	public GLDisplay(String title, int _width, int _height, boolean _fullscreen) {
 		this.fullscreen = _fullscreen;
 		this.width = _width;
 		this.height = _height;
 		
 		glCanvas = GLDrawableFactory.getFactory().createGLCanvas(new GLCapabilities());
 		glCanvas.setSize(width, height);
 		glCanvas.setIgnoreRepaint(true);
 
 		frame = new JFrame(title);
 		frame.getContentPane().setLayout(new BorderLayout());
 		frame.getContentPane().add(glCanvas, BorderLayout.CENTER);
 
 		animator = new Animator(glCanvas);
 	}
 
 	public void start() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setSize(width, height);
 		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
 		frame.addWindowListener(new MyShutdownWindowAdapter());
 
 		if (fullscreen) {
 			frame.setUndecorated(true);
 			usedDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 			usedDevice.setFullScreenWindow(frame);
 			usedDevice.setDisplayMode(
 				findDisplayMode(
 					usedDevice.getDisplayModes(),
 					width,
 					height,
 					usedDevice.getDisplayMode().getBitDepth(),
 					usedDevice.getDisplayMode().getRefreshRate()));
 		} else {
 			frame.setVisible(true);
 		}
 
 		glCanvas.requestFocus();
 
 		animator.start();
 	}
 
 	public void stop() {
 		animator.stop();
 		if (fullscreen) {
 			usedDevice.setFullScreenWindow(null);
 			usedDevice = null;
 		}
 		frame.dispose();
 		System.exit(0);
 	}
 
 	private DisplayMode findDisplayMode(
 		DisplayMode[] displayModes,
 		int requestedWidth,
 		int requestedHeight,
 		int requestedDepth,
 		int requestedRefreshRate) {
 		// Try to find an exact match
 		DisplayMode displayMode =
 			findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, requestedDepth, requestedRefreshRate);
 
 		// Try again, ignoring the requested bit depth
 		if (displayMode == null)
 			displayMode = findDisplayModeInternal(displayModes, requestedWidth, requestedHeight, DONT_CARE, DONT_CARE);
 
 		// Try again, and again ignoring the requested bit depth and height
 		if (displayMode == null)
 			displayMode = findDisplayModeInternal(displayModes, requestedWidth, DONT_CARE, DONT_CARE, DONT_CARE);
 
 		// If all else fails try to get any display mode
 		if (displayMode == null)
 			displayMode = findDisplayModeInternal(displayModes, DONT_CARE, DONT_CARE, DONT_CARE, DONT_CARE);
 
 		return displayMode;
 	}
 
 	private DisplayMode findDisplayModeInternal(
 		DisplayMode[] displayModes,
 		int requestedWidth,
 		int requestedHeight,
 		int requestedDepth,
 		int requestedRefreshRate) {
 		DisplayMode displayModeToUse = null;
 		for (int i = 0; i < displayModes.length; i++) {
 			DisplayMode displayMode = displayModes[i];
 			if ((requestedWidth == DONT_CARE || displayMode.getWidth() == requestedWidth)
 				&& (requestedHeight == DONT_CARE || displayMode.getHeight() == requestedHeight)
 				&& (requestedHeight == DONT_CARE || displayMode.getRefreshRate() == requestedRefreshRate)
 				&& (requestedDepth == DONT_CARE || displayMode.getBitDepth() == requestedDepth))
 				displayModeToUse = displayMode;
 		}
 
 		return displayModeToUse;
 	}
 
 	public void addGLEventListener(GLEventListener glEventListener) {
 		glCanvas.addGLEventListener(glEventListener);
 	}
 
 	public void removeGLEventListener(GLEventListener glEventListener) {
 		glCanvas.removeGLEventListener(glEventListener);
 	}
 
 	public void addKeyListener(KeyListener l) {
 		glCanvas.addKeyListener(l);
 	}
 
 	public void addMouseListener(MouseListener l) {
 		glCanvas.addMouseListener(l);
 	}
 
 	public void addMouseMotionListener(MouseMotionListener l) {
 		glCanvas.addMouseMotionListener(l);
 	}
 
 	public void removeKeyListener(KeyListener l) {
 		glCanvas.removeKeyListener(l);
 	}
 
 	public void removeMouseListener(MouseListener l) {
 		glCanvas.removeMouseListener(l);
 	}
 
 	public void removeMouseMotionListener(MouseMotionListener l) {
 		glCanvas.removeMouseMotionListener(l);
 	}
 
 	private class MyShutdownWindowAdapter extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			stop();
 		}
 	}
 }
 
 /*
  * $Log$
  * Revision 1.17  2004/05/04 23:59:45  tako
  * Added ScrollBar and ScrollContainer examples.
  *
  * Revision 1.16  2004/03/17 00:52:27  tako
  * Updated the example app to include an Image widget.
  *
  * Revision 1.15  2004/03/07 18:28:51  tako
  * Updated the example to set the default theme.
  *
  * Revision 1.14  2003/12/14 00:29:24  steven
  * updated to show valuebars with their value as text
  *
  * Revision 1.13  2003/12/05 01:08:58  tako
  * Updated the example to show what a disabled widget looks like.
  *
  * Revision 1.12  2003/11/26 00:12:32  tako
  * Class is now public like it should be.
  *
  * Revision 1.11  2003/11/25 16:28:00  tako
  * All code is now subject to the Lesser GPL.
  *
  * Revision 1.10  2003/11/24 16:45:54  tako
  * Window resize now properly updates the GUI.
  * Made info window non-draggable.
  *
  * Revision 1.9  2003/11/23 01:56:43  tako
  * Minor change to enable mouse support for the GUI.
  *
  * Revision 1.8  2003/11/21 10:53:35  steven
  * NO longer makes the text widget focusable
  *
  * Revision 1.7  2003/11/21 10:03:17  steven
  * Removed warnings and added textfield editing example
  *
  * Revision 1.6  2003/11/21 01:25:00  tako
  * Updated example to make use of the new Toggle widget.
  *
  * Revision 1.5  2003/11/19 11:21:48  tako
  * Updated the code to use the new event system.
  *
  * Revision 1.4  2003/11/19 09:27:22  tako
  * Added ValueBar as slider to the menu window.
  *
  * Revision 1.3  2003/11/18 16:01:08  steven
  * Added example for a horizontal and vertical bar which showing the fps
  *
  * Revision 1.2  2003/11/18 15:54:57  tako
  * Added some silly test code.
  *
  * Revision 1.1  2003/11/17 11:05:19  tako
  * First check-in of the gui4gl example application.
  *
  */
