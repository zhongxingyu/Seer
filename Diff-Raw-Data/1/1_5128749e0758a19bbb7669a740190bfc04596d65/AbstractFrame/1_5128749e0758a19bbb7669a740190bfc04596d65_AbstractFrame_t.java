 package com.alexrnl.commons.gui.swing;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.mvc.AbstractView;
 
 /**
  * Abstract frame.<br />
  * Factorise code commonly used when creating frames.<br />
  * Other threads may be waiting on the instance of the frame being built, the waiters will be
  * notified when the window is up and running.
  * @author Alex
  */
 public abstract class AbstractFrame extends JFrame implements AbstractView {
 	/** Logger */
 	private static Logger		lg					= Logger.getLogger(AbstractFrame.class.getName());
 	
 	/** Serial Version UID */
 	private static final long	serialVersionUID	= -3391832845968248721L;
 	
 	/** <code>true</code> when the window is ready (GUI built) */
 	private boolean				ready;
 	/** Reference to this */
 	private final AbstractFrame	frame;
 	/** Icon for frames */
 	private final Path			iconFile;
 
 	/**
 	 * Constructor #1.<br />
 	 * @param title
 	 *        the title of the frame.
 	 * @param iconFile
 	 *        the path to the icon file.
 	 * @param parameters
 	 *        the parameters that will be passed to the {@link #preInit(Object...)} method.
 	 */
 	public AbstractFrame (final String title, final Path iconFile, final Object... parameters) {
 		super();
 		ready = false;
 		frame = this;
 		this.iconFile = iconFile;
 		setTitle(title);
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("Building abstract view with parameters: " + Arrays.toString(parameters));
 		}
 		preInit(parameters);
 		SwingUtilities.invokeLater(new GuiBuilder());
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param title
 	 *        the title of the frame.
 	 * @param parameters
 	 *        the parameters that will be passed to the {@link #preInit(Object...)} method.
 	 * @see #AbstractFrame(String, Path, Object...)
 	 */
 	public AbstractFrame (final String title, final Object... parameters) {
 		this(title, null, parameters);
 	}
 	
 	/**
 	 * Execute pre-initialization process.<br />
 	 * Typically, set controllers, initialize required attributes.
 	 * @param parameters
 	 *        the parameters which were passed to the
 	 *        {@link #AbstractFrame(String, Path, Object...) constructor}.
 	 */
 	protected abstract void preInit (Object... parameters);
 
 	/**
 	 * Build all the components of the frame.<br />
 	 * Labels, buttons, menus, text fields, etc.
 	 */
 	protected abstract void build ();
 	
 	/**
 	 * Build the user interface.<br />
 	 * Frame will be:
 	 * <ul>
 	 * <li>With the icon specified in the constructor.</li>
 	 * <li>Not visible.</li>
 	 * <li>Hidden on close.</li>
 	 * <li>Minimum size set to current size after packing the frame.</li>
 	 * <li>Centered.</li>
 	 * </ul>
 	 */
 	private void buildGUI () {
 		if (iconFile != null) {
 			try {
 				setIconImage(ImageIO.read(Files.newInputStream(iconFile)));
 			} catch (final IOException e) {
 				lg.warning("Could not load icon, frame " + getTitle() + " will be iconless: "
 						+ ExceptionUtils.display(e));
 			}
 		}
 		setVisible(false);
 		setDefaultCloseOperation(HIDE_ON_CLOSE);
 		build();
 		pack();
 		setMinimumSize(getSize());
 		setLocationRelativeTo(null);
 	}
 	
 	/**
 	 * Check if the frame's GUI is built.
 	 * @return <code>true</code> if the frame is ready.
 	 */
 	public final boolean isReady () {
 		return ready;
 	}
 	
 	/**
 	 * Runnable class which build the components of the interface.
 	 * @author Alex
 	 */
 	private final class GuiBuilder implements Runnable {
 		
 		@Override
 		public void run () {
 			buildGUI();
 			
 			if (lg.isLoggable(Level.INFO)) {
 				lg.info("Frame " + getTitle() + " built");
 			}
 			ready = true;
 			synchronized (frame) {
 				frame.notifyAll();
 			}
 		}
 	}
 	
 	/**
 	 * Return the reference to the current frame.
 	 * @return the frame.
 	 */
 	protected AbstractFrame getFrame () {
 		return frame;
 	}
 	
 }
