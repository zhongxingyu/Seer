 /*
  *                 Sun Public License Notice
  * 
  * The contents of this file are subject to the Sun Public License
  * Version 1.0 (the "License"). You may not use this file except in
  * compliance with the License. A copy of the License is available at
  * http://www.sun.com/
  * 
  * The Original Code is NetBeans. The Initial Developer of the Original
  * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
  * Microsystems, Inc. All Rights Reserved.
  */
 /*
  * FixedWizard.java
  *
  * Created on August 19, 2005, 9:11 PM
  */
 
 package org.netbeans.spi.wizard;
 
 import javax.swing.*;
 import javax.swing.text.JTextComponent;
 import javax.swing.tree.TreePath;
 import java.awt.Component;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * A convenience JPanel subclass that makes it easy to create wizard panels.
  * This class provides a number of conveniences:
  * <p/>
  * <b>Automatic listening to child components</b><br>
  * If you add an editable component (all standard Swing controls are supported)
 * to a WizardPage or a child JPanel inside it,
  * a listener is automatically attached to it.  If user input occurs, the
  * following things happen, in order:
  * <ul>
  * <li>If the <code>name</code> property of the component has been set, then
  * the value from the component (i.e. Boolean for checkboxes, selected item(s)
  * for lists/combo boxes/trees, etc.) will automatically be added to the
  * wizard settings map, with the component name as the key.</li>
  * <li>Regardless of whether the <code>name</code> property is set,
  * <code>validateContents()</code> will be called.  You can override that method
  * to enable/disable the finish button, call <code>setProblem()</code> to
  * disable navigation and display a string to the user, etc.
  * </ul>
  * <p/>
  * The above behavior can be disabled by passing <code>false</code> to the
  * appropriate constructor.  In that case, <code>validateContents</code> will
  * never be called automatically.
  * <p/>
  * If you have custom components that WizardPage will not know how to listen
  * to automatically, attach an appropriate listener to them and optionally
  * call <code>userInputReceived()</code> with the component and the event if
  * you want to run your automatic validation code.
  * <p/>
  * For convenience, this class implements the relevant methods for accessing
  * the <code>WizardController</code> and the settings map for the wizard that
  * the panel is a part of.
  * <p/>
  * Instances of WizardPage can be returned from a WizardPanelProvider;  this
  * class also offers two methods for conveniently assembling a wizard:
  * <ul>
  * <li>Pass an array of already instantiated WizardPages to
  * <code>createWizard()</code>.  Note that for large wizards, it is preferable
  * to construct the panels on demand rather than at construction time.</li>
  * <li>Construct a wizard out of WizardPages, instantiating the panels as
  * needed:  Pass an array of classes all of which
  * <ul>
  * <li>Are subclasses of WizardPage</li>
  * <li>Have a static method with the following signature:
  * <ul>
  * <li><code>public static String getDescription()</code></li>
  * </ul>
  * </li>
  * </ul>
  * </ul>
  * <p/>
  * Note that during development of a wizard, it is worthwhile to test/run with
  * assertions enabled, as there is quite a bit of validity checking via assertions
  * that can help find problems early.
  *
  * @author Tim Boudreau
  */
 public class WizardPage extends JPanel {
     private static final Logger logger =
             Logger.getLogger(WizardPage.class.getName());
 
     private final String description;
     private final String id;
 
     //Have an initial dummy map so it's never null.  We'll dump its contents
     //into the real map the first time it's set
     private Map wizardData;
     //An initial wizardController that will dump its settings into the real
     //one the first time it's set
     private WizardControllerImplementation wc = new WC();
     private WizardController controller = new WizardController(wc);
 
     //Flag to make sure we don't reenter userInputReceieved from maybeUpdateMap()
     private boolean inBeginUIChanged = false;
     //Flag to make sure we don't reenter userInputReceived because the
     //implementation of validateContents changed a component's value, triggering
     //a new event on GenericListener
     private boolean inUiChanged = false;
 
     /**
      * Construct a new WizardPage with the passed step id and description.
      * Use this constructor for WizardPages which will be constructed ahead
      * of time and passed in an array to <code>createWizard</code>.
      *
      * @param stepId          the unique ID for the step represented
      * @param stepDescription the localized description of this step
      * @param autoListen      if true, components added will automatically be
      *                        listened to for user input
      * @see #validateContents
      */
     public WizardPage(String stepId, String stepDescription, boolean autoListen) {
         id = stepId;
         description = stepDescription;
 
         if (autoListen) {
             //It will attach itself
             new GenericListener(this);
         }
 //        if (getClass() == WizardPage.class && stepId == null ||
 //                description == null) {
 //            throw new NullPointerException ("Step or ID is null"); //NOI18N
 //        }
         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); //XXX
     }
 
     public WizardPage(String stepId, String stepDescription) {
         this(stepId, stepDescription, true);
     }
 
     /**
      * Use this constructor or the default constructor if you intend to
      * pass an array of Class objects to lazily create WizardPanels.
      */
     protected WizardPage(boolean autoListen) {
         this(null, null, autoListen);
     }
 
     /**
      * Default constructor.  AutoListening will be on by default.
      */
     protected WizardPage() {
         this(true);
     }
 
     private String getID() {
         return id;
     }
 
     private String getDescription() {
         return description;
     }
 
     public void addNotify() {
         super.addNotify();
 
         renderingPage();
         inValidateContents = true;
         try {
             setProblem(validateContents(null, null));
         } finally {
             inValidateContents = false;
         }
     }
 
     private boolean inValidateContents = false;
 
     /**
      * Called whenever the page is rendered.
      * This can be used by the page as a notification
      * to load page-specific information in its fields.
      * <p/>
      * By default, this method does nothing.
      */
     protected void renderingPage() {
         // Empty
     }
 
     /**
      * Create a simple Wizard from an array of <code>WizardPage</code>s
      */
     public static Wizard createWizard(WizardPage[] contents, WizardResultProducer finisher) {
         return new WPP(contents, finisher).createWizard();
     }
 
     public static Wizard createWizard(String title, WizardPage[] contents, WizardResultProducer finisher) {
         return new WPP(title, contents, finisher).createWizard();
     }
 
     public static Wizard createWizard(String title, WizardPage[] contents) {
         return createWizard(title, contents, WizardResultProducer.NO_OP);
     }
 
     /**
      * Create a simple Wizard from an array of WizardPages, with a
      * no-op WizardResultProducer.
      */
     public static Wizard createWizard(WizardPage[] contents) {
         return createWizard(contents, WizardResultProducer.NO_OP);
     }
 
     /**
      * Create simple Wizard from an array of classes, each of which is a
      * unique subclass of WizardPage.
      */
     public static Wizard createWizard(Class[] wizardPageClasses, WizardResultProducer finisher) {
         return new CWPP(wizardPageClasses, finisher).createWizard();
     }
 
     /**
      * Create simple Wizard from an array of classes, each of which is a
      * unique subclass of WizardPage.
      */
     public static Wizard createWizard(String title, Class[] wizardPageClasses, WizardResultProducer finisher) {
         return new CWPP(wizardPageClasses, finisher).createWizard();
     }
 
     /**
      * Create simple Wizard from an array of classes, each of which is a
      * unique subclass of WizardPage.
      */
     public static Wizard createWizard(String title, Class[] wizardPageClasses) {
         return new CWPP(wizardPageClasses, 
                 WizardResultProducer.NO_OP).createWizard();
     }
     /**
      * Create a simple Wizard from an array of classes, each of which is a
      * unique subclass of WizardPage, with a
      * no-op WizardResultProducer.
      */
     public static Wizard createWizard(Class[] wizardPageClasses) {
         return createWizard(wizardPageClasses, WizardResultProducer.NO_OP);
     }
 
     /**
      * Called by createPanelForStep, with whatever map is passed.  In the
      * current impl this is always the same Map, but that is not guaranteed.
      * If any content was added by calls to putWizardData() during the
      * constructor, etc., such data is copied to the settings map the first
      * time this method is called
      */
     void setWizardDataMap(Map m) {
         if (m == null) {
             wizardData = new HashMap();
         } else {
             if (wizardData instanceof HashMap) {
                 //We're using our initial dummy map
                 m.putAll(wizardData);
             }
             wizardData = m;
         }
     }
 
     /**
      * Set the WizardController.  In the current impl, this is always the same
      * object, but the API does not guarantee that.  The first time this is
      * called, it will update the state of the passed controller to  match
      * any state that was set by components during the construction of this
      * component
      */
     void setController(WizardController controller) {
         if (controller.getImpl() instanceof WC) {
             ((WC) controller.getImpl()).configure(controller);
         }
 
         this.controller = controller;
     }
 
     /**
      * Get the WizardController for interacting with the Wizard that
      * contains this panel.
      * Return value will never be null.
      */
     private WizardController getController() {
         return controller;
     }
 
 
     /**
      * Set the problem string.  Call this method if next/finish should be
      * disabled.  The passed string will be visible to the user, and should
      * be a short, localized description of what is wrong.
      */
     protected final void setProblem(String value) {
         getController().setProblem(value);
     }
 
     /**
      * Set whether the finish, next or both buttons should be enabled,
      * assuming no problem string is set.
      *
      * @param value WizardController.MODE_CAN_CONTINUE,
      *              WizardController.MODE_CAN_FINISH or
      *              WizardController.MODE_CAN_CONTINUE_OR_FINISH;
      */
     protected final void setForwardNavigationMode(int value) {
         getController().setForwardNavigationMode(value);
     }
 
     /**
      * Disable all navigation.  Useful if some background task is being
      * completed during which no navigation should be allowed.  Use with care,
      * as it disables the cancel button as well.
      */
     protected final void setBusy(boolean busy) {
         getController().setBusy(busy);
     }
 
     /**
      * Store a value in response to user interaction with a GUI component.
      */
     protected final void putWizardData(Object key, Object value) {
         logger.fine("putWizardData " + key + "=" + value); //NOI18N
         getWizardDataMap().put(key, value);
         if (!inBeginUIChanged && !inValidateContents) {
             inValidateContents = true;
             try {
                 setProblem(validateContents(null, null));
             } finally {
                 inValidateContents = false;
             }
         }
     }
 
     /**
      * Returns all of the keys in the wizard data map.
      */
     protected final Object[] getWizardDataKeys() {
         return getWizardDataMap().keySet().toArray();
     }
 
     /**
      * Retrieve a value stored in the wizard map, which may have been
      * putWizardData there by this panel or any previous panel in the wizard which
      * contains this panel.
      */
     protected final Object getWizardData(Object key) {
         return getWizardDataMap().get(key);
     }
 
     /**
      * Determine if the wizard map contains the requested key.
      */
     protected final boolean wizardDataContainsKey(Object key) {
         return getWizardDataMap().containsKey(key);
     }
 
     /**
      * Called when an event is received from one of the components in the
      * panel that indicates user input.  Typically you won't need to touch this
      * method, unless your panel contains custom components which are not
      * subclasses of any standard Swing component, which the framework won't
      * know how to listen for changes on.  For such cases, attach a listener
      * to the custom component, and call this method with the event if you want
      * validation to run when input happens.  Automatic updating of the
      * settings map will not work for such custom components, for obvious
      * reasons, so update the settings map, if needed, in validateContents
      * for this case.
      *
      * @param source The component that the user interacted with (if it can
      *               be determined from the event) or null
      * @param event  Usually an instance of EventObject, except in the case of
      *               DocumentEvent.
      */
     protected final void userInputReceived(Component source, Object event) {
         if (inBeginUIChanged) {
             logger.fine("Ignoring recursive entry to userInputReceived while updating map");
             return;
         }
 
         //Update the map no matter what
         inBeginUIChanged = true;
 
         if (source != null) {
             try {
                 maybeUpdateMap(source);
             } finally {
                 inBeginUIChanged = false;
             }
         }
 
         //Possibly some programmatic change from checkState could cause
         //a recursive call
         if (inUiChanged) {
             logger.fine("Ignoring recursive entry to userInputReceieved from validateContents");
             return;
         }
 
         inUiChanged = true;
         inValidateContents = true;
         try {
             setProblem(validateContents(source, event));
         } finally {
             inUiChanged = false;
             inValidateContents = false;
         }
     }
 
     /**
      * Puts the value from the component in the settings map if the
      * component's name property is not null
      */
     void maybeUpdateMap(Component comp) {
         if (logger.isLoggable(Level.FINE)) {
             logger.fine("Maybe update map for " + comp.getClass().getName() +  //NOI18N
                     " named " + comp.getName()); //NOI18N
         }
 
         Object mapKey = getMapKeyFor(comp);
 
         if (mapKey != null) {
             //XXX do it even if null?
             Object value = valueFrom(comp);
             if (logger.isLoggable(Level.FINE)) {
                 logger.fine("maybeUpdateMap putting " + mapKey + "," + value +
                         " into settings"); //NOI18N
             }
             putWizardData(mapKey, value);
         }
     }
 
     /**
      * Callback for GenericListener to remove a component's value if its name
      * changes or it is removed from the panel.
      */
     void removeFromMap(Object key) {
         logger.fine("removeFromMap: " + key); //NOI18N
         getWizardDataMap().remove(key);
     }
 
     /**
      * Given an ad-hoc swing component, fetch the likely value based on its
      * state.  The default implementation handles most common swing components.
      * If you are using custom components and have assigned them names, override
      * this method to handle getting an appropriate value out of your
      * custom component and call super for the others.
      */
     protected Object valueFrom(Component comp) {
         if (comp instanceof JRadioButton || comp instanceof JCheckBox || comp instanceof JToggleButton) {
             return ((AbstractButton) comp).getModel().isSelected() ? Boolean.TRUE : Boolean.FALSE;
         } else if (comp instanceof JTree) {
             TreePath path = ((JTree) comp).getSelectionPath();
             if (path != null) {
                 return path.getLastPathComponent();
             }
         } else if (comp instanceof JList) {
             Object[] o = ((JList) comp).getSelectedValues();
             if (o != null) {
                 if (o.length > 1) {
                     return o;
                 } else if (o.length == 1) {
                     return o[0];
                 }
             }
         } else if (comp instanceof JTextComponent) {
             return ((JTextComponent) comp).getText();
         } else if (comp instanceof JComboBox) {
             return ((JComboBox) comp).getSelectedItem();
         } else if (comp instanceof JColorChooser) {
             return ((JColorChooser) comp).getSelectionModel().getSelectedColor();
         } else if (comp instanceof JSpinner) {
             return ((JSpinner) comp).getValue();
         } else if (comp instanceof JSlider) {
             return new Integer(((JSlider) comp).getValue());
         }
 
         return null;
     }
 
     /**
      * Get the map key that should be used to automatically put the value
      * represented by this component into the wizard data map.
      * <p/>
      * The default implementation returns the result of <code>c.getName()</code>,
      * which is almost always sufficient and convenient - just set the
      * component names in a GUI builder and everything will be handled.
      *
      * @return null if the component's value should not be automatically
      *         written to the wizard data map, or an object which is the key that
      *         later code will use to find this value.  By default, it returns the
      *         component's name.
      */
     protected Object getMapKeyFor(Component c) {
         return c.getName();
     }
 
     /**
      * Called when user interaction has occurred on a component contained by this
      * panel or one of its children.  Override this method to check if all of
      * the values are legal, such that the Next/Finish button should be enabled,
      * optionally calling <code>setForwardNavigationMode()</code> if warranted.
      * <p/>
      * This method also may be called with a null argument an effect of
      * calling <code>putWizardData()</code> from someplace other than within
      * this method.
      * <p/>
      * Note that this method may be called very frequently, so it is important
      * that validation code be fast.  For cases such as <code>DocumentEvent</code>s,
      * it may be desirable to delay validation with a timer, if the implementation
      * of this method is too expensive to call on each keystroke.
      * <p/>
      * Either the component, or the event, or both may be null on some calls
      * to this method (such as when it is called because the settings map
      * has been written to).
      * <p/>
      * The default implementation returns null.
      *
      * @param component The component the user interacted with, if it can be
      *                  determined.  The infrastructure does track the owners of list models
      *                  and such, and can find the associated component, so this will usually
      *                  (but not necessarily) be non-null.
      * @param event     The event object (if any) that triggered this call to
      *                  validateContents.  For most cases this will be an instance of
      *                  EventObject, and can be used to directly detect what component
      *                  the user interacted with.  Since javax.swing.text.DocumentEvent is
      *                  not a subclass of EventObject, the type of the argument is Object,
      *                  so these events may be passed.
      * @return A localized string describing why navigation should be disabled,
      *         or null if the state of the components is valid and forward navigation
      *         should be enabled.
      */
     protected String validateContents(Component component, Object event) {
         return null;
     }
 
     /**
      * Called if the user is navigating into this panel when it has already
      * been displayed at least once - the user has navigated back to this
      * panel, or back past this panel and is now navigating forward again.
      * <p/>
      * If some of the UI needs to be set up based on values from earlier
      * pages that may have changed, do that here, fetching values from the
      * settings map by calling <code>getWizardData()</code>.
      * <p/>
      * The default implementation simply calls
      * <code>validateContents (null, null)</code>.
      */
     protected void recycle() {
         setProblem(validateContents(null, null));
     }
 
     /**
      * Get the settings map into which the wizard gathers settings.
      * Return value will never be null.
      */
     final Map getWizardDataMap() {
         if (wizardData == null) {
             wizardData = new HashMap();
         }
         return wizardData;
     }
 
     static WizardPanelProvider createWizardPanelProvider (WizardPage page) {
         return new WPP (new WizardPage[] { page }, WizardResultProducer.NO_OP);
     }
 
     static WizardPanelProvider createWizardPanelProvider (WizardPage[] page) {
         return new WPP (page, WizardResultProducer.NO_OP);
     }
 
 
     /**
      * WizardPanelProvider that takes an array of already created WizardPages
      */
     static final class WPP extends WizardPanelProvider {
         private final WizardPage[] pages;
         private final WizardResultProducer finish;
 
         WPP(WizardPage[] pages, WizardResultProducer finish) {
             super(getSteps(pages), getDescriptions(pages));
 
             //Fail-fast validation - don't wait until something goes wrong
             //if the data are bad
             assert valid(pages) == null : valid(pages);
             assert finish != null;
 
             this.pages = pages;
             this.finish = finish;
         }
 
         WPP(String title, WizardPage[] pages, WizardResultProducer finish) {
             super(title, getSteps(pages), getDescriptions(pages));
 
             //Fail-fast validation - don't wait until something goes wrong
             //if the data are bad
             assert valid(pages) == null : valid(pages);
             assert finish != null;
 
             this.pages = pages;
             this.finish = finish;
         }
 
         protected JComponent createPanel(WizardController controller, String id,
                                          Map wizardData) {
             int idx = indexOfStep(id);
 
             assert idx != -1 : "Bad ID passed to createPanel: " + id; //NOI18N
 
             pages[idx].setController(controller);
             pages[idx].setWizardDataMap(wizardData);
 
             return pages[idx];
         }
 
         /**
          * Make sure we haven't been passed bogus data
          */
         private String valid(WizardPage[] pages) {
             if (new HashSet(Arrays.asList(pages)).size() != pages.length) {
                 return "Duplicate entry in array: " +  //NOI18N
                         Arrays.asList(pages);
             }
 
             for (int i = 0; i < pages.length; i++) {
                 if (pages[i] == null) {
                     return "Null entry at " + i + " in pages array"; //NOI18N
                 }
             }
 
             return null;
         }
 
         protected Object finish(Map settings) throws WizardException {
             return finish.finish(settings);
         }
 
         public boolean cancel(Map settings) {
             return finish.cancel (settings);
         }
     }
 
     /**
      * WizardPanelProvider that takes an array of WizardPage subclasses and
      * instantiates them on demand
      */
     private static final class CWPP extends WizardPanelProvider {
         private final Class[] classes;
         private final WizardResultProducer finish;
 
         CWPP(String title, Class[] classes, WizardResultProducer finish) {
             super(title, getSteps(classes), getDescriptions(classes));
             assert classes != null : "Class array may not be null";
             assert new HashSet(Arrays.asList(classes)).size() == classes.length :
                     "Duplicate entries in class array";
             assert finish != null : "WizardResultProducer may not be null";
             this.finish = finish;
             this.classes = classes;
         }
         
         CWPP(Class[] classes, WizardResultProducer finish) {
             super(getSteps(classes), getDescriptions(classes));
 
             assert classes != null : "Class array may not be null";
             assert new HashSet(Arrays.asList(classes)).size() == classes.length :
                     "Duplicate entries in class array";
             assert finish != null : "WizardResultProducer may not be null";
 
             this.classes = classes;
             this.finish = finish;
         }
 
         protected JComponent createPanel(WizardController controller, String id, Map wizardData) {
             int idx = indexOfStep(id);
 
             assert idx != -1 : "Bad ID passed to createPanel: " + id; //NOI18N
 
             try {
                 WizardPage result = (WizardPage) classes[idx].newInstance();
 
                 result.setController(controller);
                 result.setWizardDataMap(wizardData);
 
                 return result;
             } catch (Exception e) {
                 logger.log(Level.WARNING, "Could not instantiate " + classes[idx], e);
                 throw new IllegalArgumentException("Could not instantiate " + //NOI18N
                         classes[idx]);
             }
         }
 
         protected Object finish(Map settings) throws WizardException {
             return finish.finish(settings);
         }
     }
 
     /**
      * A dummy wizard controller which is used until the panel has actually
      * been put into use;  so state can be set during the constructor, etc.
      * Its state will be dumped into the real one once there is a real one.
      */
     private static final class WC implements WizardControllerImplementation {
         private String problem = null;
         private int canFinish = -1;
         private Boolean busy = null;
 
         public void setProblem(String value) {
             this.problem = value;
         }
 
         public void setForwardNavigationMode(int value) {
             switch (value) {
                 case WizardController.MODE_CAN_CONTINUE :
                 case WizardController.MODE_CAN_FINISH :
                 case WizardController.MODE_CAN_CONTINUE_OR_FINISH :
                     break;
                 default :
                     throw new IllegalArgumentException(Integer.toString(value));
             }
 
             canFinish = value;
         }
 
         public void setBusy(boolean busy) {
             this.busy = busy ? Boolean.TRUE : Boolean.FALSE;
         }
 
         void configure(WizardController other) {
             if (other == null) {
                 return;
             }
 
             if (busy != null) {
                 other.setBusy(busy.booleanValue());
             }
 
             if (canFinish != -1) {
                 other.setForwardNavigationMode(canFinish);
             }
 
             if (problem != null) {
                 other.setProblem(problem);
             }
         }
     }
 
     /**
      * Get an array of step ids from an array of WizardPages
      */
     private static String[] getSteps(WizardPage[] pages) {
         String[] result = new String[pages.length];
 
         for (int i = 0; i < pages.length; i++) {
             result[i] = pages[i].getID();
             if (result[i] == null) {
                 result[i] = getIDFromStaticMethod(pages[i].getClass());
             }
         }
 
         return result;
     }
 
     /**
      * Get an array of descriptions from an array of WizardPages
      */
     private static String[] getDescriptions(WizardPage[] pages) {
         String[] result = new String[pages.length];
 
         for (int i = 0; i < pages.length; i++) {
             result[i] = pages[i].getDescription();
             if (result[i] == null) {
                 result[i] = getDescriptionFromStaticMethod (pages[i].getClass());
             }
         }
 
         return result;
     }
 
     private static String getIDFromStaticMethod (Class clazz) {
         System.err.println("GetID by method for " + clazz);
         String result = null;
         try {
             Method m = clazz.getDeclaredMethod("getStep");
             assert m.getReturnType() == String.class;
             result = (String) m.invoke(clazz, (Object[]) null);
             if (result == null) {
                 throw new NullPointerException ("getStep may not return null");
             }
         } catch (IllegalArgumentException ex) {
             throw new IllegalStateException (ex);
         } catch (IllegalAccessException ex) {
             throw new IllegalStateException (ex);
         } catch (InvocationTargetException ex) {
             throw new IllegalStateException (ex);
         } catch (SecurityException ex) {
             throw new IllegalStateException (ex);
         } catch (NoSuchMethodException ex) {
             System.err.println("METHOD NOT FOUND");
             //do nothing
         }
         return result;
     }
 
     /**
      * Get an array of steps by looking for a static method getID() on each
      * class object passed
      */
     private static String[] getSteps(Class[] pages) {
         if (pages == null) {
             throw new NullPointerException("Null array of classes"); //NOI18N
         }
 
         String[] result = new String[pages.length];
 
         for (int i = 0; i < pages.length; i++) {
             if (pages[i] == null) {
                 throw new NullPointerException("Null at " + i + " in array " + //NOI18N
                         "of panel classes"); //NOI18N
             }
 
             if (!WizardPage.class.isAssignableFrom(pages[i])) {
                 throw new IllegalArgumentException(pages[i].getName() +
                         " is not a subclass of WizardPage"); //NOI18N
             }
             result[i] = getIDFromStaticMethod (pages[i]);
             if (result[i] == null) {
                 result[i] = pages[i].getName();
             }
         }
         System.err.println("Returning " + Arrays.asList(result));
         return result;
     }
 
 //    /** Determine if a default constructor is present for a class */
 //    private static boolean hasDefaultConstructor (Class clazz) {
 //        try {
 //            Constructor c = clazz.getConstructor(new Class[0]);
 //            return c != null;
 //        } catch (Exception e) {
 //            return false;
 //        }
 //    }
 
     /**
      * Get an array of descriptions by looking for the static method
      * getDescription() on each passed class object
      */
     private static String[] getDescriptions(Class[] pages) {
         String[] result = new String[pages.length];
 
         for (int i = 0; i < pages.length; i++) {
             result[i] = getDescriptionFromStaticMethod(pages[i]);
         }
 
         return result;
     }
 
     private static String getDescriptionFromStaticMethod(Class clazz) {
         String result = null;
         Method m;
         try {
             m = clazz.getDeclaredMethod("getDescription", (Class[]) null); //NOI18N
         } catch (Exception e) {
             throw new IllegalArgumentException("Could not find or access " + //NOI18N
                     "public static String " + clazz.getName() +  //NOI18N
                     ".getStep() - make sure it exists"); //NOI18N
         }
 
         if (m.getReturnType() != String.class) {
             throw new IllegalArgumentException("getStep has wrong " //NOI18N
                     + " return type: " + m.getReturnType() + " on " + //NOI18N
                     clazz);
         }
 
         if (!Modifier.isStatic(m.getModifiers())) {
             throw new IllegalArgumentException("getStep is not " + //NOI18N
                     "static on " + clazz); //NOI18N
         }
 
         try {
             result= (String) m.invoke(null, (Object[]) null);
         } catch (InvocationTargetException ite) {
             throw new IllegalArgumentException("Could not invoke " + //NOI18N
                     "public static String " + clazz.getName() +  //NOI18N
                     ".getStep() - make sure it exists."); //NOI18N
         } catch (IllegalAccessException iae) {
             throw new IllegalArgumentException("Could not invoke " + //NOI18N
                     "public static String " + clazz.getName() +  //NOI18N
                     ".getStep() - make sure it exists."); //NOI18N
         }
         return result;
     }
 
     /**
      * Interface that is passed to WizardPage.createWizard().  For wizards
      * created from a set of WizardPages or WizardPage subclasses, this is
      * the object that whose code will be run to create or do whatever the
      * wizard does when the user clicks the Finish button.
      */
     public static interface WizardResultProducer {
         /**
          * Conclude a wizard, doing whatever the wizard does with the data
          * gathered into the map on the various panels.
          */
         Object finish(Map wizardData) throws WizardException;
 
         /**
          * Called when the user presses the cancel button.  Almost all
          * implementations will want to return true.
          */
         boolean cancel(Map settings);
 
         /**
          * A no-op WizardResultProducer that returns null.
          */
         WizardResultProducer NO_OP = new WizardResultProducer() {
             public Object finish(Map wizardData) {
                 return null;
             }
 
             public boolean cancel (Map settings) {
                 return true;
             }
         };
     }
 
 }
