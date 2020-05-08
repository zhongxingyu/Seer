 /*
  * Mar 9, 2006
  */
 package com.thinkparity.browser.platform.application.window;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import com.thinkparity.browser.application.browser.BrowserWindow;
 import com.thinkparity.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.browser.application.browser.window.WindowId;
 import com.thinkparity.browser.javax.swing.AbstractJDialog;
 import com.thinkparity.browser.javax.swing.AbstractJFrame;
 import com.thinkparity.browser.javax.swing.AbstractJPanel;
 import com.thinkparity.browser.platform.application.display.avatar.Avatar;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public abstract class Window extends AbstractJDialog {
 
 	/**
 	 * @see java.io.Serializable
 	 * 
 	 */
 	private static final long serialVersionUID = 1;
 
 	/**
 	 * The panel onto which all displays are dropped.
 	 * 
 	 */
 	protected WindowPanel windowPanel;
 
 	/**
 	 * A lookup for window sizes for avatars.
 	 * 
 	 */
 	private final WindowSize windowSize;
 
 	/**
 	 * Create a Window.
 	 * 
 	 * @param l18Context
 	 *            The localization context
 	 */
 	public Window(final AbstractJFrame owner, final Boolean modal,
 			final String l18nContext) {
 		this((JFrame) owner, modal, l18nContext);
 	}
 
 	public Window(final JFrame owner, final Boolean modal,
 			final String l18nContext) {
 		super(owner, modal, l18nContext);
 		this.windowSize = new WindowSize();
 		getRootPane().setBorder(BrowserWindow.getBorder());
 		setTitle(getString("Title"));
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setUndecorated(true);
 	}
 
 	/**
 	 * Obtain the window id.
 	 * 
 	 * @return The window id.
 	 */
 	public abstract WindowId getId();
 
 	/**
 	 * Open the window.
 	 *
 	 */
 	public void open(final Avatar avatar) {
         initComponents(avatar);
         debugGeometry();
         debugLookAndFeel();
         setSize(windowSize.get(avatar.getId()));
         setLocation(calculateLocation());
         invalidate();
         setVisible(true);
 	}
 
     public void open(final AbstractJPanel jPanel, final Dimension windowSize) {
         initComponents(jPanel);
         debugGeometry();
         debugLookAndFeel();
         setSize(windowSize);
         setLocation(calculateLocation());
         invalidate();
         setVisible(true);
     }
 
 	/**
 	 * Open an avatar in the window.  Display 
 	 * @param avatar
 	 * @param errors
 	 */
 	public void openAndWait(final Avatar avatar) { openAndWait(avatar, windowSize.get(avatar.getId())); }
 
     public void openAndWait(final AbstractJPanel jPanel, final Dimension size) {
         this.addWindowListener(new WindowAdapter() {
             public void windowClosed(final WindowEvent e) {
                 synchronized(Window.this) { Window.this.notifyAll(); }
             }
         });
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 public void run() { open(jPanel, size); }
             });
         }
         catch(final InterruptedException ix) { throw new RuntimeException(ix); }
         catch(final InvocationTargetException itx) { throw new RuntimeException(itx); }
         synchronized(Window.this) {
             try { wait(); }
             catch(final InterruptedException ix) {
                 throw new RuntimeException(ix);
             }
         }
     }
 
 	/**
 	 * Calculate the location for the window based upon its owner and its size.
 	 * 
 	 * @return The location of the window centered on the owner.
 	 */
 	protected Point calculateLocation() {
 		final Dimension os = getOwner().getSize();
 		final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
 		final Dimension ws = getSize();
 		if(0 == os.width || 0 == os.height) {
 			final Point l = getLocation();
 			l.x = (ss.width - ws.width) / 2;
 			l.y = (ss.height - ws.height) / 2;
 			return l;
 		}
 		else {
 			final Point l = getOwner().getLocation();
 			l.x += (os.width - ws.width) / 2;
 			l.y += (os.height - ws.height) / 2;
 	
 			if(l.x + ws.width > (ss.width)) { l.x = ss.width - ws.width; }
 			if(l.y + ws.height > (ss.height)) { l.y = ss.height - ws.height; }
 	
 			if(l.x < 0) { l.x = 0; }
 			if(l.y < 0) { l.y = 0; }
 			return l;
 		}
 	}
 
 	protected Dimension calculateSize(final AvatarId avatarId) {
 		return windowSize.get(avatarId);
 	}
 
     
 	protected void initComponents(final Avatar avatar) {
 		avatar.reload();
 		initComponents((AbstractJPanel) avatar);
 	}
 
     protected void initComponents(final AbstractJPanel jPanel) {
        // FIXME The window functionality needs more sleep.
        if(jPanel instanceof Avatar) { ((Avatar) jPanel).reload(); }

         windowPanel = new WindowPanel();
         windowPanel.addPanel(jPanel);
 
         add(windowPanel);
     }
 }
