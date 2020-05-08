 package com.hexcore.cas.ui.toolkit;
 
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.swing.SwingUtilities;
 
 import com.hexcore.cas.math.Recti;
 import com.hexcore.cas.math.Vector2i;
import com.hexcore.cas.utilities.Log;
 import com.jogamp.opengl.util.FPSAnimator;
 
 public class Window extends Layout implements GLEventListener, MouseMotionListener, MouseListener, MouseWheelListener, KeyListener, ClipboardOwner
 {	
 	private Frame 		frame;
 	private GLCanvas	canvas;
 	private FPSAnimator	animator;
 	private	Theme		theme;
 			
 	private Vector2i	defaultMargin;
 	private Widget		focusedWidget = null;
 	private Dialog		modalDialog = null;
 	
 	private List<WindowEventListener>	eventListeners;
 	
 	private boolean[]		keyState;
 	private Timer			keyRepeatTimer = new Timer("KeyRepeatFilter");
 	private KeyRepeatFilter	keyRepeatFilter = null;
 
 	private	boolean	updateComponents = true;	
 	private boolean	initDone = false;
 	private boolean	debugLayout = false;
 	private boolean	fullscreen = false;
 	
 	private List<Recti>	clipRectangles = new ArrayList<Recti>();
 
 	// Tooltip
 	Widget		wantsTooltip;
 	long		lastMouseMove;
 	Vector2i	mousePos;
 	
 	public Window(String title, Theme theme)
 	{
 		this(title, 800, 600, theme);
 	}
 	
 	public Window(String title, int width, int height, Theme theme)
 	{
 		super(new Vector2i(width, height));
 				
 		keyState = new boolean[1024];
 		for (int i = 0; i < 1024; i++) keyState[i] = false;
 		
 		defaultMargin = new Vector2i(8, 8);
 		components = new ArrayList<Widget>();
 		eventListeners = new ArrayList<WindowEventListener>();
 		setWindow(this);
 		
 		this.theme = theme;
 		
 		//////////////////
 		
 		GLCapabilities capabilites = new GLCapabilities(GLProfile.get(GLProfile.GL2));
 		capabilites.setDoubleBuffered(true);
 		capabilites.setSampleBuffers(true);
 		capabilites.setNumSamples(4);
 		
 		canvas = new GLCanvas(capabilites);
 		canvas.setPreferredSize(new Dimension(width, height));
 		canvas.setAutoSwapBufferMode(false);
 		canvas.setFocusTraversalKeysEnabled(false);
 
 		frame = new Frame(title);
 			
 		frame.setSize(width, height);
 		frame.setResizable(true);
 		frame.addWindowListener(new WindowListener());
 		frame.add(canvas);
 		frame.pack();
 	}
 	
 	public boolean isDebugLayout()
 	{
 		return debugLayout;
 	}
 	
 	public void setDebugLayout(boolean state)
 	{
 		debugLayout = state;
 	}
 	
 	public void show()
 	{
 		frame.setVisible(true);
 		frame.addMouseListener(this);
 		frame.addMouseMotionListener(this);
 		frame.addMouseWheelListener(this);
 		frame.addKeyListener(this);
 		canvas.addKeyListener(this);
 				
 		/////////////////////
 		
 		canvas.requestFocus();
 		canvas.addGLEventListener(this);
 		canvas.addMouseListener(this);
 		canvas.addMouseMotionListener(this);
 						
 		animator = new FPSAnimator(canvas, 60);
 		animator.start();
 	}
 	
 	public void updateWidgets(float delta)
 	{
 		update(new Vector2i(), delta);
 		for (Widget component : components) component.update(new Vector2i(), delta);
 	}
 	
 	public void exit() 
 	{
 		System.out.println(eventListeners.size());
 		
       	for (WindowEventListener listener : eventListeners)
       		if (!listener.close())
       			return;
 		
 		frame.dispose();
       			
       	System.exit(0);
 	}
 	
 	public Vector2i getDefaultMargin()
 	{
 		return defaultMargin;
 	}
 
 	public Theme getTheme()
 	{
 		return theme;
 	}
 	
 	public long getTime()
 	{
 		return animator.getCurrentTime();
 	}
 
 	public FileSelectResult askUserForFileToSave(String title)
 	{
 		FileDialog dialog = new FileDialog(frame, title);
 		dialog.setMode(FileDialog.SAVE);
 		dialog.setVisible(true);
 		return new FileSelectResult(dialog.getFile(), dialog.getDirectory());
 	}
 	
 	public FileSelectResult askUserForFileToSave(String title, String extension)
 	{
 		FileDialog dialog = new FileDialog(frame, title);
 		dialog.setMode(FileDialog.SAVE);
 		dialog.setFilenameFilter(new ExtensionFilter(extension));
 		dialog.setVisible(true);
 		return new FileSelectResult(dialog.getFile(), dialog.getDirectory());
 	}
 	
 	public FileSelectResult askUserForFileToLoad(String title)
 	{
 		FileDialog dialog = new FileDialog(frame, title);
 		dialog.setMode(FileDialog.LOAD);
 		dialog.setVisible(true);
 		return new FileSelectResult(dialog.getFile(), dialog.getDirectory());
 	}
 	
 	public FileSelectResult askUserForFileToLoad(String title, String extension)
 	{
 		FileDialog dialog = new FileDialog(frame, title);
 		dialog.setMode(FileDialog.LOAD);
 		dialog.setFilenameFilter(new ExtensionFilter(extension));
 		dialog.setVisible(true);
 		return new FileSelectResult(dialog.getFile(), dialog.getDirectory());
 	}	
 	
 	public float getAspectRatio()
 	{
 		return (float)size.x / size.y;
 	}
 	
 	public void setViewport(GL gl, Vector2i position, Vector2i size)
 	{
 		GL2 gl2 = gl.getGL2();
 		
 		int width = size.x > 0 ? size.x : 1;
 		int height = size.y > 0 ? size.y : 1;
 		gl2.glViewport(position.x, this.size.y - (height + position.y), width, height);
 	}
 	
 	private void clip(final GL gl, final Recti rect)
 	{
 		GL2 gl2 = gl.getGL2();
 		gl2.glViewport(rect.position.x, this.size.y - (rect.size.y + rect.position.y), rect.size.x, rect.size.y);
         gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl2.glLoadIdentity();
         gl2.glOrtho(rect.position.x, rect.position.x + rect.size.x, 
         		rect.position.y + rect.size.y, rect.position.y, -1000.0, 1000.0);
         gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl2.glLoadIdentity();
 	}
 	
 	private void resetClip(GL gl)
 	{
 		GL2 gl2 = gl.getGL2();
 		gl2.glViewport(0, 0, size.x, size.y);
         gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl2.glLoadIdentity();
         gl2.glOrtho(0.0, size.x, size.y, 0.0, -1000.0, 1000.0);
         gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl2.glLoadIdentity();
 	}
 	
 	public void addClipRectangle(GL gl, Vector2i position, Vector2i size)
 	{
 		Recti rect = new Recti(position, size);
 		clipRectangles.add(rect);
 		
 		Recti current = clipRectangles.get(0);
 		for (int i = 1; i < clipRectangles.size(); i++)
 			current = current.intersect(clipRectangles.get(i));
 		
 		clip(gl, current);
 	}
 	
 	public void removeClipRectangle(GL gl)
 	{	
 		if (clipRectangles.isEmpty()) return;
 		
 		clipRectangles.remove(clipRectangles.size() - 1);
 		
 		if (clipRectangles.isEmpty())
 			resetClip(gl);
 		else
 		{
 			Recti current = clipRectangles.get(0);
 			for (int i = 1; i < clipRectangles.size(); i++)
 				current = current.intersect(clipRectangles.get(i));
 			
 			clip(gl, current);
 		}
 	}
 	
 	public void showModalDialog(Dialog dialog)
 	{
 		giveUpFocus(focusedWidget);
 		modalDialog = dialog;
 		modalDialog.relayout();
 	}
 	
 	public void closeModalDialog()
 	{
 		modalDialog = null;
 	}
 	
 	public void requestTooltip(Widget widget)
 	{
 		if (widget == wantsTooltip) return;
 		wantsTooltip = widget;
 	}
 	
 	public void giveUpFocus(Widget widget)
 	{
 		if (widget != focusedWidget) return;
 		
 		if (focusedWidget != null)
 		{
 			Event event = new Event(Event.Type.LOST_FOCUS);
 			focusedWidget.receiveEvent(event);
 			focusedWidget = null;
 		}
 	}
 	
 	public void requestFocus(Widget widget)
 	{
 		if ((widget == null) || !widget.canGetFocus()) return;
 		if (widget == focusedWidget) return;
 		
 		giveUpFocus(focusedWidget);
 		focusedWidget = widget;
 		
 		Event event = new Event(Event.Type.GAINED_FOCUS);
 		focusedWidget.receiveEvent(event);
 	}
 	
 	public void toggleFocus(Widget widget)
 	{
 		if (focusedWidget == widget)
 			giveUpFocus(widget);
 		else
 			requestFocus(widget);
 	}
 	
 	public void setFullscreen(boolean state)
 	{
 		fullscreen = state;
 		
 		if (fullscreen)
 			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		else
 			frame.setExtendedState(Frame.NORMAL);
 	}
 				
 	public boolean getKeyState(int keyCode)
 	{
 		if (keyCode > 1024) return false;
 		return keyState[keyCode];
 	}
 	
 	public String getClipboardText()
 	{
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		
 		String contents = "" ;
 		Transferable transferable = clipboard.getContents(null);
 		if ((transferable != null) && transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
 		{
 			try
 			{
 				contents = (String)transferable.getTransferData(DataFlavor.stringFlavor);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		return contents;
 	}
 	
 	public void setClipboardText(String text)
 	{
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		StringSelection selection = new StringSelection(text);
 		clipboard.setContents(selection, this);
 	}
 	
 	public void addListener(WindowEventListener wel)
 	{
 		eventListeners.add(wel);
 	}
 	
 	public void sendWindowEvent(Event event)
 	{
 		for (WindowEventListener listener : eventListeners)
 			listener.handleWindowEvent(event);
 		
 		canvas.display();
 	}
 	
 	@Override
 	public void relayout()
 	{
 		super.relayout();
 		if (modalDialog != null) modalDialog.relayout();
 	}
 		
 	@Override
 	public void display(GLAutoDrawable drawable)
 	{
 		Colour	backgroundColour = theme.getColour("Window", "background", new Colour(0.93f, 0.93f, 0.93f));
 		
         final GL2 gl = drawable.getGL().getGL2();
         gl.glClearColor(backgroundColour.r, backgroundColour.g, backgroundColour.b, 1.0f);
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
         
         gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glOrtho(0.0, size.x, size.y, 0.0, -1000.0, 1000.0);
         gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl.glLoadIdentity();
                 
         for (WindowEventListener listener : eventListeners)
 			listener.update(1.0f / 60.0f);
         		
         updateWidgets(1.0f / 60.0f);
         
         renderWidgets(drawable);
         
         for (WindowEventListener listener : eventListeners)
 			listener.render();
         		
         drawable.swapBuffers();
 	}
 	
 	@Override
 	public void dispose(GLAutoDrawable drawable)
 	{
 
 	}
 	
 	@Override
 	public void init(GLAutoDrawable drawable)
 	{
 		GL2 gl = drawable.getGL().getGL2();
         gl.glClearDepth(1.0f);   
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
         if (!initDone)
         {
         	for (WindowEventListener listener : eventListeners)
         		listener.initialise();
         	
         	initDone = true;
         }
 	}
 	
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
 	{
 		if (width < 800) width = 800;
 		if (height < 600) height = 600;
 		
 		size = new Vector2i(width, height);
 		
 		relayout();
 	}
 		
 	///////////
 			
 	private void renderWidgets(GLAutoDrawable drawable)
 	{
 		if (updateComponents)
 		{
 	        relayout();
 	        updateComponents = false;
 		}
 		
 		GL gl = drawable.getGL();
 		render(gl, new Vector2i(0, 0));
 		
 		if (modalDialog != null)
 		{
 			theme.renderDialogFade(gl, size);
 			
 			modalDialog.render(gl, new Vector2i(0, 0));
 		}
 		else if (focusedWidget != null) 
 			focusedWidget.renderExtras(gl, focusedWidget.getRealPosition());
		else if (wantsTooltip != null && wantsTooltip.mouseover && !wantsTooltip.getTooltip().isEmpty())
 		{
 			long diff = System.nanoTime() - lastMouseMove;
 			
 			if (diff > 1000000000)
 			{
 				Vector2i 	pos = mousePos.add(16, 16);
 				String		tooltip = wantsTooltip.getTooltip();
 				Vector2i 	textSize = theme.calculateTextSize(tooltip, Text.Size.SMALL);
 				
 				Graphics.renderRectangle(gl, pos, textSize.add(8, 8), Colour.WHITE);
 				Graphics.renderBorder(gl, pos, textSize.add(8, 8), Colour.BLACK);
 				theme.renderText(gl, wantsTooltip.getTooltip(), pos.add(4, 4), Colour.BLACK, Text.Size.SMALL);
 			}
 		}
 	}
 			
 	private void sendEvent(Event event)
 	{
 		if (modalDialog != null)
 		{
 			modalDialog.receiveEvent(event, new Vector2i(0, 0));
 			return;
 		}
 		
 		boolean handled = false;
 		if (focusedWidget != null)
 			handled = focusedWidget.receiveEventExtras(event, focusedWidget.getRealPosition());
 		
 		if (!handled)
 			for (Widget component : components)
 				component.receiveEvent(event);
 	}
 		
 	////
 		
 	private class WindowListener extends WindowAdapter
 	{
 		public void windowClosing(WindowEvent we) { exit(); }
 	}
 	
 	////
 	
 	@Override
 	public void lostOwnership(Clipboard arg0, Transferable arg1)
 	{
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e)
 	{
 		lastMouseMove = System.nanoTime();
 		mousePos = new Vector2i(e.getX(), e.getY());
 		
 		Event event = new Event(Event.Type.MOUSE_MOTION);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e)
 	{
 		lastMouseMove = System.nanoTime();
 		mousePos = new Vector2i(e.getX(), e.getY());
 		
 		Event event = new Event(Event.Type.MOUSE_MOTION);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e)
 	{
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e)
 	{
 		lastMouseMove = System.nanoTime();
 		mousePos = new Vector2i(e.getX(), e.getY());
 		
 		Event event = new Event(Event.Type.MOUSE_MOTION);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e)
 	{
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e)
 	{
 		lastMouseMove = System.nanoTime();
 		mousePos = new Vector2i(e.getX(), e.getY());
 		
 		Event event = new Event(Event.Type.MOUSE_CLICK);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.button = e.getButton();
 		event.pressed = true;
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e)
 	{
 		lastMouseMove = System.nanoTime();
 		mousePos = new Vector2i(e.getX(), e.getY());
 		
 		Event event = new Event(Event.Type.MOUSE_CLICK);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.button = e.getButton();
 		event.pressed = false;
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e)
 	{
 		Event event = new Event(Event.Type.MOUSE_SCROLL);
 		event.position = new Vector2i(e.getX(), e.getY());
 		event.amount = e.getWheelRotation() * e.getScrollAmount() * 20;
 		sendEvent(event);	
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		int	keyCode = e.getKeyCode();
 		if (keyCode < 1024) keyState[keyCode] = true;
 		
 		Event event = new Event(Event.Type.KEY_PRESS);
 		event.pressed = true;
 		event.button = keyCode;
 		event.setModifiers(e);
 		
 		if (keyRepeatFilter != null && keyRepeatFilter.event != null)
 		{
 			Event event2 = keyRepeatFilter.event;
 			
 			if (event2.button == keyCode) 
 			{
 				keyRepeatFilter.cancel();
 				keyRepeatFilter = null;
 			}
 		}
 		
 		sendEvent(event);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e)
 	{		
 		int keyCode = e.getKeyCode();
 		Event event = new Event(Event.Type.KEY_PRESS);
 		event.pressed = false;
 		event.button = keyCode;
 		event.setModifiers(e);
 		
 		keyRepeatFilter = new KeyRepeatFilter(event);
 		keyRepeatTimer.schedule(keyRepeatFilter, 6);
 	}
 	
 	public void keyReleased(Event event)
 	{
 		if (event.button < 1024) keyState[event.button] = false;
 		sendEvent(event);
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e)
 	{
 		if (e.getKeyChar() == '`') debugLayout = !debugLayout;
 		
 		Event event = new Event(Event.Type.KEY_TYPED);
 		event.button = (int)e.getKeyChar();
 		event.setModifiers(e);
 		sendEvent(event);
 	}
 	
 	class ExtensionFilter implements FilenameFilter
 	{
 		private String extension;
 		
 		public ExtensionFilter(String extension)
 		{
 			this.extension = extension;
 		}
 		
 		@Override
 		public boolean accept(File dir, String name)
 		{
 			return name.endsWith("." + extension);
 		}
 	}
 	
 	public class FileSelectResult
 	{
 		public String filename;
 		public String directory;
 		
 		public FileSelectResult(String filename, String directory)
 		{
 			this.filename = filename;
 			this.directory = directory;
 		}
 		
 		public String getFullPath()
 		{
 			if (filename == null) return null;
 			return (directory != null ? directory : "") + filename;
 		}
 		
 		public boolean isValid()
 		{
 			return filename != null;
 		}
 		
 		@Override 
 		public String toString()
 		{
 			if (filename == null) return "<None>";
 			return (directory != null ? directory : "") + filename;
 		}
 	}
 	
 	class KeyRepeatFilter extends TimerTask
 	{
 		public Event 	event;
 		
 		KeyRepeatFilter(Event event)
 		{
 			this.event = event;
 		}
 		
 		@Override
 		public boolean cancel()
 		{
 			event = null;
 			return super.cancel();
 		}
 		
 		@Override
 		public void run()
 		{
 			if (event != null)
 			{
 				final Event temp = event;
 				event = null;
 				
 				SwingUtilities.invokeLater(new Runnable() 
 				{
 					@Override
 					public void run()
 					{
 						keyReleased(temp);
 					}	
 				});
 			}
 		}
 	}
 }
