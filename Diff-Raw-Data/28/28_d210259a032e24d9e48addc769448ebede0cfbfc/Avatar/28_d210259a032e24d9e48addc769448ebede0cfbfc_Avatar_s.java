 /*
  * Jan 20, 2006
  */
 package com.thinkparity.ophelia.browser.platform.application.display.avatar;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.SwingUtilities;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.swing.AbstractJPanel;
 
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.Resizer;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.ContentProvider;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationId;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationRegistry;
 import com.thinkparity.ophelia.browser.platform.plugin.PluginRegistry;
 import com.thinkparity.ophelia.browser.platform.util.State;
 import com.thinkparity.ophelia.browser.util.localization.JPanelLocalization;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public abstract class Avatar extends AbstractJPanel {
 
     /** The avatar's content provider. */
 	protected ContentProvider contentProvider;
 
     /** A list of the avatar's errors. */
 	protected final List<Throwable> errors;
 
 	/** The avatar input. */
 	protected Object input;
 
 	/** Localization helper utility. */
     protected final JPanelLocalization localization;
 
 	/** The thinkParity <code>PluginRegistry</code>. */
     protected final PluginRegistry pluginRegistry;
 
     /** The thinkparity application registry. */
     private final ApplicationRegistry applicationRegistry;
 
 	/** The Resizer */
     protected Resizer resizer = null;
 
     /** The avatar's scrolling policy. */
 	private final ScrollPolicy scrollPolicy;
 
     /**
      * Create Avatar.
      * 
      * @param id
      *            The <code>AvatarId</code>.
      */
     protected Avatar(final AvatarId id) {
         this(id.toString());
     }
 
     /**
 	 * Create a Avatar.
 	 * 
 	 * @param l18nContext
 	 *            The localization context.
 	 */
 	protected Avatar(final String l18nContext) {
 		this(l18nContext, ScrollPolicy.NONE);
 	}
 
 	/**
      * Create an Avatar.
      * 
      * @param l18nContext
      *            The localiztaion context.
      * @param background
      *            The avatar background colour.
      */
 	protected Avatar(final String l18nContext, final Color background) {
 		this(l18nContext, ScrollPolicy.NONE, background);
 	}
 
 	/**
 	 * Create an Avatar.
 	 * 
 	 * @param l18nContext
 	 *            The localization context.
 	 * @param scrollPolicy
 	 *            The scrolling policy.
 	 */
 	protected Avatar(final String l18nContext, final ScrollPolicy scrollPolicy) {
 		super();
         this.applicationRegistry = new ApplicationRegistry();
 		this.errors = new LinkedList<Throwable>();
         this.localization = new JPanelLocalization(l18nContext);
         this.pluginRegistry = new PluginRegistry();
 		this.scrollPolicy = scrollPolicy;
 	}
 
 	/**
 	 * Create an Avatar.
 	 * 
 	 * @param l18nContext
 	 *            The localization context.
 	 * @param scrollPolicy
 	 *            The scrolling policy.
 	 * @param background
 	 *            The background color.
 	 */
 	protected Avatar(final String l18nContext, final ScrollPolicy scrollPolicy,
 			final Color background) {
 		super(background);
         this.applicationRegistry = new ApplicationRegistry();
 		this.errors = new LinkedList<Throwable>();
         this.localization = new JPanelLocalization(l18nContext);
         this.pluginRegistry = new PluginRegistry();
 		this.scrollPolicy = scrollPolicy;
 	}
     
     /**
      * Install the resizer.
      */
     public void installResizer() {
        if (null==this.resizer) {
            this.resizer = new Resizer(getController(), this, isSupportMouseMove(), getResizeEdges());
            final List<Component> componentsThatSupportMouseMove = getComponentsThatSupportMouseMove();
            if (null!=componentsThatSupportMouseMove) {
                resizer.addComponentsThatSupportMouseMove(componentsThatSupportMouseMove);
            }
         }
     }
 
 	/**
 	 * Set an error for display.
 	 * 
 	 * @param error
 	 *            The error.
 	 */
 	public void addError(final Throwable error) { errors.add(error); }
 
 	/**
 	 * Clear all display errors.
 	 *
 	 */
 	public void clearErrors() { errors.clear(); }
 
 	/**
 	 * Determine whether or not the error has been set.
 	 * 
 	 * @return True if error has been set; false otherwise.
 	 */
 	public Boolean containsErrors() { return 0 < errors.size(); }
 
 	/**
 	 * Obtain the content provider.
 	 * 
 	 * @return The content provider.
 	 */
 	public ContentProvider getContentProvider() { return contentProvider; }
 
     /**
      * Obtain the browser application.
      * 
      * @return The browser application.
      */
 	public Browser getController() {
 	    return (Browser) applicationRegistry.get(ApplicationId.BROWSER);
 	}
 
 	/**
 	 * Obtain the error.
 	 * 
 	 * @return The error.
 	 */
 	public List<Throwable> getErrors() { return errors; }
 
 	/**
 	 * Obtain the avatar id.
 	 * 
 	 * @return The avatar id.
 	 */
 	public abstract AvatarId getId();
 
 	/**
 	 * Obtain the avatar's input.
 	 * 
 	 * @return The input.
 	 */
 	public Object getInput() { return input; }
 
 	/**
 	 * Obtain the scroll policy for the avatar.
 	 * 
 	 * @return The scroll policy for the avatar.
 	 */
 	public ScrollPolicy getScrollPolicy() { return scrollPolicy; }
 
 	/**
 	 * Obtain the avatar's state information.
 	 * 
 	 * @return The avatar's state information.
 	 */
 	public abstract State getState();
 
 	/**
      * These get and set methods are used by classes that intend to do their
      * own mouse dragging. (For example, the bottom right resize control.)
      */
     protected Boolean isResizeDragging() {
         if (null==resizer) {
             return Boolean.FALSE;
         } else {
             return resizer.isResizeDragging();
         }
     }
     
 	/**
 	 * Reload the avatar. This event is called when either the content provider
 	 * or the input has changed; or as a manual reload of the avatar.
 	 * 
 	 */
 	public void reload() {}
 
 	/**
 	 * Set the content provider.
 	 * 
 	 * @param contentProvider
 	 *            The content provider.
 	 */
 	public void setContentProvider(final ContentProvider contentProvider) {
 		Assert.assertNotNull(
 				"Cannot set a null content provider:  " + getId(), contentProvider);
 		if(this.contentProvider == contentProvider
 				|| contentProvider.equals(this.contentProvider)) { return; }
 		
 		this.contentProvider = contentProvider;
 		reload();
 	}
 
 	/**
 	 * Set the avatar's input.
 	 * 
 	 * @param input
 	 *            The avatar input.
 	 */
 	public void setInput(final Object input) {
 		Assert.assertNotNull("Cannot set null input:  " + getId(), input);
 		if(this.input == input || input.equals(this.input)) { return; }
 		
 		this.input = input;
 		reload();
 	}
 
     /**
      * These get and set methods are used by classes that intend to do their
      * own mouse dragging. (For example, the bottom right resize control.)
      */
     protected void setResizeDragging(Boolean resizerDragging) {
         if (null!=resizer) {
             resizer.setResizeDragging(resizerDragging);
         }
     }
     
     /**
      * Get the resize edges. Override this method if you don't want
      * resize support for the avatar, or if only some of the avatar
      * edges support resize.
      */
     protected Resizer.ResizeEdges getResizeEdges() {
         return Resizer.ResizeEdges.ALL_EDGES;
     }
     
     /**
      * Get the list of components that a user can drag on to move the parent.
      * Override this method for example if a dialog has a text label that
      * can be used for dragging and moving the dialog.
      */
     protected List<Component> getComponentsThatSupportMouseMove() {
         return null;
     }
     
     /**
      * Flag to indicate if dragging the centre of the control will cause moving.
      * Override this method if you don't want the avatar to support movement.
      */
     protected Boolean isSupportMouseMove() {
         return Boolean.TRUE;
     }
 
 	/**
 	 * Set the avatar state.
 	 * 
 	 * @param state
 	 *            The avatar's state information.
 	 */
 	public abstract void setState(final State state);
 
 	/**
      * Dispose of the window.
      * 
      */
     protected void disposeWindow() {
         SwingUtilities.getWindowAncestor(this).dispose();
     }
 
 	/**
      * Obtain the pluginRegistry
      *
      * @return The PluginRegistry.
      */
     protected PluginRegistry getPluginRegistry() {
         return pluginRegistry;
     }
 
     /**
      * @see JPanelLocalization#getString(String)
      * 
      */
     protected String getString(final String localKey) {
     	return localization.getString(localKey);
     }
     
     /**
      * @see JPanelLocalization#getString(String, Object[])
      * 
      */
     protected String getString(final String localKey, final Object[] arguments) {
     	return localization.getString(localKey, arguments);
     }
     
     /**
      * Causes <i>doRun.run()</i> to be executed asynchronously on the AWT event
      * dispatching thread.
      * 
      * @param doRun
      *            The runnable to execute.
      * @see SwingUtilities#invokeLater(java.lang.Runnable)
      */
 	protected void invokeLater(final Runnable doRun) {
         SwingUtilities.invokeLater(doRun);
     }
     /**
 	 * Determine whether or not the platform is running in test mode.
 	 * 
 	 * @return True if the platform is in test mode; false otherwise.
 	 */
 	protected final Boolean isDebugMode() {
 		return getController().getPlatform().isDevelopmentMode();
 	}
 
     /**
 	 * Determine whether or not the platform is running in test mode.
 	 * 
 	 * @return True if the platform is in test mode; false otherwise.
 	 */
 	protected final Boolean isTestMode() {
 		return getController().getPlatform().isTestingMode();
 	}
 
     /**
 	 * Provide a visual cue to the user that work is being done.
 	 *
 	 */
 	protected void toggleVisualFeedback(final Boolean isWorking) {
 		if(isWorking) { setIsWorking(); }
 		else { setIsNotWorking(); }
 	}
 
     private void setIsNotWorking() {
 		final Component[] components = getComponents();
 		for(final Component c : components) {
 			if(c.getClass().isAssignableFrom(JButton.class)) {
 				c.setEnabled(true);
 			}
 		}
 
 	}
 
     private void setIsWorking() {
 		final Component[] components = getComponents();
 		for(final Component c : components) {
 			if(c.getClass().isAssignableFrom(JButton.class)) {
 				c.setEnabled(false);
 			}
 		}
 	}
 
     /**
 	 * The scrolling policy for the avatar.
 	 * 
 	 */
 	public enum ScrollPolicy { BOTH, HORIZONTAL, NONE, VERTICAL }
 }
