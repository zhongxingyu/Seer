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
  * Wizard.java
  *
  * Created on February 22, 2005, 2:18 PM
  */
 
 package org.netbeans.spi.wizard;
 
 import java.util.EventListener;
 import java.util.Map;
 import javax.swing.Action;
 import javax.swing.JComponent;
 
 /**
  * <b>Note:</b>It is quite rare to need to directly implement this interface.
  * Typically you will deal with it if you are implementing a UI to <i>display</i>
  * wizards;  otherwise what you probably want to do is use one of the 
  * convenience classes that handles the navigation logic.
  * <p>
  * If you want to create a Wizard with a fixed set of steps, use
  * {@link org.netbeans.spi.wizard.WizardPage WizardPage} or 
  * implement
  * {@link org.netbeans.spi.wizard.WizardPanelProvider WizardPanelProvider}
  * and call {@link org.netbeans.spi.wizard.WizardPanelProvider#createWizard 
  * createWizard} on it.
  * <p>
  * If you have a wizard that has choice-points, where depending on what the
  * user chooses on one pane, the subsequent number or set of panes will change, implement
  * <code>{@link org.netbeans.spi.wizard.WizardBranchController WizardBranchController}</code> to supply different sub-wizards 
  * depending on the user's choices, and call {@link org.netbeans.spi.wizard.WizardBranchController#createWizard createWizard}
  * to get the result.  <code>{@link org.netbeans.spi.wizard.WizardBranchController WizardBranchController}</code>
  * will let you provide a choice of {@link org.netbeans.spi.wizard.WizardPanelProvider WizardPanelProvider}s
  * depending on what the user enters.  For most cases no more is needed.
  * <p>
  * To display any <code>Wizard</code> to the user, call 
  * {@link org.netbeans.api.wizard.WizardDisplayer#show WizardDisplayer.show 
  * (someWizard)}
  * <p>
  * The one case in which it may be necessary to implement this interface 
  * directly is if you have a Wizard that for some reason needs to disable
  * the <code>Prev</code> button (this is not a good idea from a usability
  * standpoint).  For all other cases, consider using one of <code>WizardPage</code>,
  * <code>WizardPanelProvider</code> and/or <code>WizardBranchController</code> - 
  * they are much simpler to work with.
  * <hr>
  * <p>
  * This class is the Wizard interface - a Wizard is a series of one or more steps represented
  * by panels in the user interface.  Each step is identified by a String ID.
  * For each ID, the <code>navigatedTo()</code> method supplies a component to
  * be displayed to the user.
  * <p>
  * Each panel may add wizardData to a Map, which is passed to <code>navigatedTo()</code>.
  * At the conclusion of a Wizard (if/when the user presses the <code>Finish</code>
  * button), the method <code>finish (Map wizardData)</code> is invoked, and the
  * Wizard may instantiate whatever it needs to and return that.  It is up to
  * the caller to do something with the return value.
  * <p>
  * Wizards should never make any changes to their environment except in the
  * <code>finish()</code> method - until that is called, they should simply
  * collect information.
  * <p>
  * The IDs of steps of the wizard should be returned by the method
  * <code>getAllIDs()</code>.  If a Wizard contains <i>branching</i>, such
  * that all of the steps cannot be determined, it should return an array
  * of Strings terminated with the special ID <code>UNDETERMINED_STEP</code>;
  * as the set of following steps becomes known, it should fire <code>
  * stepsChange()</code> to any registered WizardListeners.
  * 
  * @author Tim Boudreau
  * @see WizardPage
  * @see WizardPanelProvider
  * @see WizardBranchController
  * @see org.netbeans.api.wizard.WizardDisplayer
  */
 public interface Wizard {
     /**
      * Constant that can be returned by <code>getForwardNavigationMode</code> to indicate
      * that the Next button can be enabled if the problem string is null.
      */
     public static final int MODE_CAN_CONTINUE = 
             WizardController.MODE_CAN_CONTINUE;
     /**
      * Constant that can be returned by <code>getForwardNavigationMode</code> to indicate
      * that the Finish button can be enabled if the problem string is null.
      */
     public static final int MODE_CAN_FINISH = 
             WizardController.MODE_CAN_FINISH;
     /**
      * Constant that can be returned by <code>getForwardNavigationMode</code> to indicate
      * that both the Finish and Next buttons can be enabled if the problem 
      * string is null.  This value is a bitmask - i.e. 
      * <code>MODE_CAN_CONTINUE_OR_FINISH == MODE_CAN_CONTINUE | 
      * MODE_CAN_FINISH</code>
      */
     public static final int MODE_CAN_CONTINUE_OR_FINISH = 
             WizardController.MODE_CAN_CONTINUE_OR_FINISH;
     
     /**
      * Special panel ID key indicating a branch point in the wizard,
      * after which the next step(s) are unknown.
      */
     public static final String UNDETERMINED_STEP = "_#UndeterminedStep";
     
     /**
      * Set which step of the wizard is currently being displayed and get
      * the component for that step.  This method is passed a Map into which
      * panels may put key/value pairs that represent user input.  This Map
      * is what the <code>finish()</code> method will use to decide what to
      * do.  
      * <p>
      * The ID passed
      * becomes the currently active step of the wizard as of this call.
      * <p>
      * If the user has already been to this step, and some key/value pairs
      * were written to the wizard data map, and the user then
      * then pressed the Back button, and later pressed Next again, the 
      * wizard data map may already contain key/value pairs for this 
      * step.  Panels whose components
      * are affected by data entered in the map in preceding steps should
      * update their UI based on the map's current contents, at the time a
      * step is re-displayed, to ensure they are in
      * sync with any changes the user may have made on preceding panels
      * since the last time this panel was shown.
      * <p>
      * If this method is called as a result of
      * the user pressing the Back button, the wizard data map will <i>not</i>
      * contain any key/value pairs added by the subsequent panels.  It
      * will only key/value pairs from
      * panels that precede this one and any written the last time this
      * panel was visited.  The wizard data map shall never contain
      * keys and values from future steps in the wizard.
      * <p>
      * Implementations are expected to return the same component if 
      * <code>navigatingTo()</code> is called repeatedly with the same ID.
      * Components should be constructed once, then reused for the lifetime
      * of the wizard.
      * <blockquote>
      * <b>Note:</b> The consequences of a later panel writing or deleting
      * a key/value pair that was put into the wizard data map by 
      * an earlier panel are
      * undefined.  Each step should use only its own unique keys, not
      * modify those from earlier steps.
      * </blockquote>
      * 
      * 
      * @param id The ID of the to-be-current panel
      * @param wizardData The map into which panels should write key/value pairs
      *   in response to user input - the place where user data is aggregated
      * @return The UI component for this step, which should be displayed in
      *   the wizard
      */
     public JComponent navigatingTo (String id, Map wizardData);
     
     /**
      * Get the String ID of the next panel.  If the Next button should be
      * disabled, or this is the final step of the wizard, return null.
      *
      * @return The unique ID of the step that follows the one currently
      *  presented in the UI, as determined by the last call to 
      *  <code>navigateTo</code>
      */
     public String getNextStep();
     /**
      * Get the String ID of the previous panel.  If the Prev button should
      * be disabled, return null.
      * @return the String ID of the step that precedes the one currently
      *  presented in this <code>Wizard</code>s UI, or null if it is either
      *  the first step or the preceding step is unknown, as determined by the last call to 
      *  <code>navigateTo</code>
      */
     public String getPreviousStep();
 
     
     /**
      * Get a human readable description of the reason the Next/Finish button
      * is not enabled (i.e. "#\foo is not a legal filename").
      * @return A localized string that describes why the Next/Finish button
      *  is not enabled, or null if one or the other or both should be enabled
      */
     public String getProblem();
     
     /**
      * Get String IDs for the entire list of known steps in the 
      * wizard (regardless of whether
      * Finish/Next can be enabled or not).  If there is a branch point in
      * the wizard and it cannot be determined what step will be next beyond
      * that point, make the final entry in the returned array the constant
      * UNDETERMINED_STEP, and fire <code>stepsChange()</code> to any listeners
      * once the later steps become known.
      * <p>
      * The return value of this method must be an array of Strings at least
      * one String in length.  If length == 1, the single step ID may not be
      * UNDETERMINED_STEP; UNDETERMINED_STEP may only be the last ID, and only
      * may be used if there is more than one step.
      *
      * @return An array of strings that constitute unique IDs of each step
      *  in the wizard.  The returned array may not contain duplicate entries.
      */
     public String[] getAllSteps();
     
     /**
      * Get a human-readable description for a given panel, as identified by
      * the passed ID.
      */
     public String getStepDescription (String id);
     
     /**
      * Add a listener for changes in the count or order of steps in this 
      * wizard and for changes in Next/Previous/Finish button enablement.
      * @param listener A listener to add
      */
     public void addWizardListener (WizardListener listener);
     
     /**
      * Remove a listener for changes in the count or order of steps in this 
      * wizard and for changes in Next/Previous/Finish button enablement.
      * @param listener A listener to remove
      */
     public void removeWizardListener (WizardListener listener);
     
     /**
      * Finish the wizard, (optionally) instantiating some Object and returning
      * it.  For cases where the map may contain wizardData too expensive to
      * validate on the fly, 
      * this method may throw a WizardException with a localized message
      * indicating the problem;  that exception can indicate a step in the
      * wizard to return to to allow the user to correct the information.
      * <p>
      * No methods on a <code>Wizard</code> instance should be called after
      * that <code>Wizard</code>'s <code>finish()</code> method has been 
      * called - the results are undefined.
      * 
     * @param settings A map containing all of the wizardData the user has
      *  entered as they traversed this wizard - presumably enough to do 
      *  whatever this method needs to do (if not, that's a bug in the 
      *  implementation of <code>Wizard</code>).
      */
     public Object finish(Map settings) throws WizardException;
     
     /** Get the title of the wizard.  
      *  @return A human-readable, localized title that should be displayed
      *   in any dialog showing this wizard
      */
     public String getTitle();
     
     /** Determine if all navigation buttons should be disabled - if the
      * wizard is currently doing some kind of progress/background processing
      * task that cannot be interrupted.
      */
     public boolean isBusy();
     
     /**
      * Get the forward navigation mode of this wizard.  This 
      * determines whether the Next button, the Finish button or both should
      * be enabled, <i>if the problem string returned from <code>getProblem</code>
      * is null</i>.  If the problem is set to non-null, the UI should disable
      * all forward navigation.
      * <p>
      * This method should never return any value but 
      * MODE_CAN_CONTINUE, MODE_CAN_FINISH or MODE_CAN_CONTINUE_OR_FINISH -
      * it is not a mechanism for disabling all forward navigation (return 
      * non null from <code>getProblem()</code> for that, or <code>true</code>
      * from <code>isBusy()</code> to temporarily disable <i>all</i> navigation).
      * <p>
      * On the final step of the wizard, this method should always return
      * MODE_CAN_FINISH.
      */
     public int getForwardNavigationMode();
     
     /**
      * Listener which can detect changes in the state of a wizard as the
      * user proceeds.
      */
     public static interface WizardListener extends EventListener {
         /**
          * Called when the number or names of the steps of the
          * wizard changes (for example, the user made a choice in one pane which
          * affects the flow of subsequent steps).
          * @param wizard The wizard whose steps have changed
          */
         public void stepsChanged(Wizard wizard);
         
         /**
          * Called when the enablement of the next/previous/finish buttons 
          * change, or the problem text changes.
          * @param wizard The wizard whose navigability has changed
          */
         public void navigabilityChanged(Wizard wizard);
     }
 }
