 package org.vaadin.teemu.wizards;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
 import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
 import org.vaadin.teemu.wizards.event.WizardProgressListener;
 import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
 import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;
 
 import com.vaadin.server.Page;
 import com.vaadin.server.Page.UriFragmentChangedEvent;
 import com.vaadin.server.Page.UriFragmentChangedListener;
 import com.vaadin.ui.AbstractOrderedLayout;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * Component for displaying multi-step wizard style user interface.
  * 
  * <p>
  * The steps of the wizard must be implementations of the {@link WizardStep}
  * interface. Use the {@link #addStep(WizardStep)} method to add these steps in
  * the same order they are supposed to be displayed.
  * </p>
  * 
  * <p>
  * The wizard also supports navigation through URI fragments. This feature is
  * disabled by default, but you can enable it using
  * {@link #setUriFragmentEnabled(boolean)} method. Each step will get a
  * generated identifier that is used as the URI fragment. If you wish to
  * override these with your own identifiers, you can add the steps using the
  * overloaded {@link #addStep(WizardStep, String)} method.
  * </p>
  * 
  * <p>
  * To react on the progress, cancellation or completion of this {@code Wizard}
  * you should add one or more listeners that implement the
  * {@link WizardProgressListener} interface. These listeners are added using the
  * {@link #addListener(WizardProgressListener)} method and removed with the
  * {@link #removeListener(WizardProgressListener)}.
  * </p>
  * 
  * @author Teemu Pöntelin / Vaadin Ltd
  */
 @SuppressWarnings("serial")
 public class Wizard extends CustomComponent implements
         UriFragmentChangedListener {
 
     /**
      * NONE = do not show any links </br> PREVIOUS = only show completed steps
      * as links </br> ALL = show all steps as links
      * 
      * @author johan
      * 
      */
     public static enum LinkMode {
         NONE, PREVIOUS, ALL
     };
 
     protected LinkMode currentLinkmode = LinkMode.NONE;
 
     protected final List<WizardStep> steps = new ArrayList<WizardStep>();
     protected final Map<String, WizardStep> idMap = new HashMap<String, WizardStep>();
 
     protected WizardStep currentStep;
     protected WizardStep lastCompletedStep;
 
     private int stepIndex = 1;
 
     protected VerticalLayout mainLayout;
     // This layout is used if the progress bar has to be shown vertically
     protected HorizontalLayout verticalProgressbarWrapper;
     protected HorizontalLayout footer;
     private Panel contentPanel;
 
     private Button nextButton;
     private Button backButton;
     private Button finishButton;
     private Button cancelButton;
 
     private Component progressBar;
 
     private static final Method WIZARD_ACTIVE_STEP_CHANGED_METHOD;
     private static final Method WIZARD_STEP_SET_CHANGED_METHOD;
     private static final Method WIZARD_COMPLETED_METHOD;
     private static final Method WIZARD_CANCELLED_METHOD;
     private final boolean isHorizontalWizardProgressBar;
     protected boolean hasVerticalStepSpacing;
 
     private boolean showProgressIndicator;
 
     private boolean uriFragmentEnabled = false;
 
     static {
         try {
             WIZARD_COMPLETED_METHOD = WizardProgressListener.class
                     .getDeclaredMethod("wizardCompleted",
                             new Class[] { WizardCompletedEvent.class });
             WIZARD_STEP_SET_CHANGED_METHOD = WizardProgressListener.class
                     .getDeclaredMethod("stepSetChanged",
                             new Class[] { WizardStepSetChangedEvent.class });
             WIZARD_ACTIVE_STEP_CHANGED_METHOD = WizardProgressListener.class
                     .getDeclaredMethod("activeStepChanged",
                             new Class[] { WizardStepActivationEvent.class });
             WIZARD_CANCELLED_METHOD = WizardProgressListener.class
                     .getDeclaredMethod("wizardCancelled",
                             new Class[] { WizardCancelledEvent.class });
         } catch (final java.lang.NoSuchMethodException e) {
             // This should never happen
             throw new java.lang.RuntimeException(
                     "Internal error finding methods in Wizard", e);
         }
     }
 
     /**
      * Initializes a new Wizard with a horizontal progress bar
      */
     public Wizard() {
         this(true, false);
     }
 
     /**
      * Initializes a new Wizard with a horizontal or vertical progress bar
      */
     public Wizard(boolean horizontalWizardProgressBar) {
         this(horizontalWizardProgressBar, false);
     }
 
     /**
      * Initializes a new Wizard with a horizontal or vertical progress bar
      * 
      * If hasVerticalStepSpacing is set to true and the steps are shown
      * vertically, they will take upp all possible space and be evenly
      * distributed. If progress bar is shown horizontally, also the indicator
      * will be shown, otherwise not
      * 
      * @param allow
      */
     public Wizard(boolean horizontalWizardProgressBar,
             boolean hasVerticalStepSpacing) {
         // If showing the progress bar horizontally, we will also most likely
         // show the progress bar, otherwise not
         this(horizontalWizardProgressBar, hasVerticalStepSpacing,
                 horizontalWizardProgressBar);
     }
 
     /**
      * Initializes a new Wizard with a horizontal or vertical progress bar
      * 
      * If hasVerticalStepSpacing is set to true and the steps are shown
      * vertically, they will take upp all possible space and be evenly
      * distributed. If showProgressIndicator is set to false, the progress
      * indicator bar will not be shown.
      * 
      * @param horizontalWizardProgressBar
      * @param hasVerticalStepSpacing
      * @param showProgressIndicator
      */
     public Wizard(boolean horizontalWizardProgressBar,
             boolean hasVerticalStepSpacing, boolean showProgressIndicator) {
         isHorizontalWizardProgressBar = horizontalWizardProgressBar;
         this.hasVerticalStepSpacing = hasVerticalStepSpacing;
         this.showProgressIndicator = showProgressIndicator;
         setStyleName("wizard");
         init();
     }
 
     private void init() {
 
         if (isHorizontalWizardProgressBar) {
             initHorizontal();
         } else {
             initVertical();
         }
 
         initDefaultProgressBar();
     }
 
     private void initVertical() {
         mainLayout = new VerticalLayout();
         verticalProgressbarWrapper = new HorizontalLayout();
 
         setCompositionRoot(mainLayout);
 
         verticalProgressbarWrapper.setSizeFull();
 
         setSizeFull();
 
         contentPanel = new Panel();
         contentPanel.setSizeFull();
 
         initControlButtons();
         initFooter();
 
         verticalProgressbarWrapper.addComponent(contentPanel);
         mainLayout.addComponent(verticalProgressbarWrapper);
         mainLayout.addComponent(footer);
         mainLayout.setComponentAlignment(footer, Alignment.BOTTOM_RIGHT);
 
         verticalProgressbarWrapper.setExpandRatio(contentPanel, 1.0f);
         mainLayout.setExpandRatio(verticalProgressbarWrapper, 1.0f);
         mainLayout.setSizeFull();
 
     }
 
     private void initHorizontal() {
 
         mainLayout = new VerticalLayout();
 
         setCompositionRoot(mainLayout);
 
         setSizeFull();
 
         contentPanel = new Panel();
         contentPanel.setSizeFull();
         contentPanel.setContent(new VerticalLayout()); // might not be needed
 
         initControlButtons();
         initFooter();
 
         mainLayout.addComponent(contentPanel);
         mainLayout.addComponent(footer);
         mainLayout.setComponentAlignment(footer, Alignment.BOTTOM_RIGHT);
 
         mainLayout.setExpandRatio(contentPanel, 1.0f);
         mainLayout.setSizeFull();
 
     }
 
     private void initFooter() {
         footer = new HorizontalLayout();
         footer.setSpacing(true);
         footer.addComponent(cancelButton);
         footer.addComponent(backButton);
         footer.addComponent(nextButton);
         footer.addComponent(finishButton);
     }
 
     private void initControlButtons() {
         nextButton = new Button("Next");
         nextButton.addClickListener(new Button.ClickListener() {
             public void buttonClick(ClickEvent event) {
                 next();
             }
         });
 
         backButton = new Button("Back");
         backButton.addClickListener(new Button.ClickListener() {
             public void buttonClick(ClickEvent event) {
                 back();
             }
         });
 
         finishButton = new Button("Finish");
         finishButton.addClickListener(new Button.ClickListener() {
             public void buttonClick(ClickEvent event) {
                 finish();
             }
         });
         finishButton.setEnabled(currentLinkmode == LinkMode.ALL);
 
         cancelButton = new Button("Cancel");
         cancelButton.addClickListener(new Button.ClickListener() {
             public void buttonClick(ClickEvent event) {
                 cancel();
             }
         });
     }
 
     private void initDefaultProgressBar() {
         WizardProgressBar progressBar = new WizardProgressBar(this,
                 isHorizontalWizardProgressBar, showProgressIndicator);
         addListener(progressBar);
         setProgressBar(progressBar);
     }
 
     public void setUriFragmentEnabled(boolean enabled) {
 
         if (uriFragmentEnabled == enabled) {
             return;
         }
 
         // at this point enabled != uriFragmentEnabled
         if (enabled) {
             Page.getCurrent().addUriFragmentChangedListener(this);
         } else {
             Page.getCurrent().removeUriFragmentChangedListener(this);
         }
 
         uriFragmentEnabled = enabled;
     }
 
     public boolean isUriFragmentEnabled() {
         return uriFragmentEnabled;
     }
 
     protected AbstractOrderedLayout getProgressBarWrapperLayout() {
         if (isHorizontalWizardProgressBar) {
             return mainLayout;
         } else {
             return verticalProgressbarWrapper;
         }
     }
 
     /**
      * Sets a {@link Component} that is displayed on top of the actual content.
      * Set to {@code null} to remove the progressBar altogether.
      * 
      * @param newHeader
      *            {@link Component} to be displayed on top of the actual content
      *            or {@code null} to remove the progressBar.
      */
     public void setProgressBar(Component newHeader) {
         if (progressBar != null) {
             if (newHeader == null) {
                 getProgressBarWrapperLayout().removeComponent(progressBar);
             } else {
                 getProgressBarWrapperLayout().replaceComponent(progressBar,
                         newHeader);
             }
         } else {
             if (newHeader != null) {
                 getProgressBarWrapperLayout().addComponentAsFirst(newHeader);
             }
         }
         progressBar = newHeader;
     }
 
     /**
      * Returns a {@link Component} that is displayed on top of the actual
      * content or {@code null} if no progressBar is specified.
      * 
      * <p>
      * By default the progressBar is a {@link WizardProgressBar} component that
      * is also registered as a {@link WizardProgressListener} to this Wizard.
      * </p>
      * 
      * @return {@link Component} that is displayed on top of the actual content
      *         or {@code null}.
      */
     public Component getProgressBar() {
         return progressBar;
     }
 
     /**
      * Adds a step to this Wizard with the given identifier. The used {@code id}
      * must be unique or an {@link IllegalArgumentException} is thrown. If you
      * don't wish to explicitly provide an identifier, you can use the
      * {@link #addStep(WizardStep)} method.
      * 
      * @param step
      * @param id
      * @throws IllegalStateException
      *             if the given {@code id} already exists.
      */
     public void addStep(WizardStep step, String id) {
         if (idMap.containsKey(id)) {
             throw new IllegalArgumentException(
                     String.format(
                             "A step with given id %s already exists. You must use unique identifiers for the steps.",
                             id));
         }
 
         steps.add(step);
         idMap.put(id, step);
         progressBar.markAsDirty();
         updateButtons();
 
         // notify listeners
         fireEvent(new WizardStepSetChangedEvent(this));
 
         // This was formerly under paintContent in V6. Activating the initial
         // step
         if (currentStep == null && !steps.isEmpty()) {
             // activate the first step
             if (checkCanStepBeActivated(steps.get(0))) {
                 activateStep(steps.get(0));
             }
         }
     }
 
     @Override
     public void beforeClientResponse(boolean initial) {
         // TODO Auto-generated method stub
         super.beforeClientResponse(initial);
     }
 
     /*
      * @Override public void paintContent(PaintTarget target) throws
      * PaintException { // make sure there is always a step selected if
      * (currentStep == null && !steps.isEmpty()) { // activate the first step if
      * (checkCanStepBeActivated(steps.get(0))) { activateStep(steps.get(0)); } }
      * super.paintContent(target); }
      */
 
     /**
      * Adds a step to this Wizard. The WizardStep will be assigned an identifier
      * automatically. If you wish to provide an explicit identifier for your
      * WizardStep, you can use the {@link #addStep(WizardStep, String)} method
      * instead.
      * 
      * @param step
      */
     public void addStep(WizardStep step) {
         addStep(step, "wizard-step-" + stepIndex++);
     }
 
     public void addListener(WizardProgressListener listener) {
         addListener(WizardCompletedEvent.class, listener,
                 WIZARD_COMPLETED_METHOD);
         addListener(WizardStepActivationEvent.class, listener,
                 WIZARD_ACTIVE_STEP_CHANGED_METHOD);
         addListener(WizardStepSetChangedEvent.class, listener,
                 WIZARD_STEP_SET_CHANGED_METHOD);
         addListener(WizardCancelledEvent.class, listener,
                 WIZARD_CANCELLED_METHOD);
     }
 
     public void removeListener(WizardProgressListener listener) {
         removeListener(WizardCompletedEvent.class, listener,
                 WIZARD_COMPLETED_METHOD);
         removeListener(WizardStepActivationEvent.class, listener,
                 WIZARD_ACTIVE_STEP_CHANGED_METHOD);
         removeListener(WizardStepSetChangedEvent.class, listener,
                 WIZARD_STEP_SET_CHANGED_METHOD);
         removeListener(WizardCancelledEvent.class, listener,
                 WIZARD_CANCELLED_METHOD);
     }
 
     public List<WizardStep> getSteps() {
         return Collections.unmodifiableList(steps);
     }
 
     /**
      * Returns {@code true} if the given step is already completed by the user.
      * 
      * @param step
      *            step to check for completion.
      * @return {@code true} if the given step is already completed.
      */
     public boolean isCompleted(WizardStep step) {
         return steps.indexOf(step) < steps.indexOf(currentStep);
     }
 
     /**
      * Returns {@code true} if the given step is the currently active step.
      * 
      * @param step
      *            step to check for.
      * @return {@code true} if the given step is the currently active step.
      */
     public boolean isActive(WizardStep step) {
         return (step == currentStep);
     }
 
     private void updateButtons() {
         if (isLastStep(currentStep)) {
             finishButton.setEnabled(true || currentLinkmode == LinkMode.ALL);
             nextButton.setEnabled(false);
         } else {
             finishButton.setEnabled(false || currentLinkmode == LinkMode.ALL);
             nextButton.setEnabled(true);
         }
         backButton.setEnabled(!isFirstStep(currentStep));
     }
 
     public Button getNextButton() {
         return nextButton;
     }
 
     public Button getBackButton() {
         return backButton;
     }
 
     public Button getFinishButton() {
         return finishButton;
     }
 
     public Button getCancelButton() {
         return cancelButton;
     }
 
     protected boolean checkCanStepBeActivated(WizardStep step) {
         if (step == null) {
             return false;
         }
 
         if (currentStep != null) {
             if (currentStep.equals(step)) {
                 // already active
                 return false;
             }
 
             // ask if we're allowed to move
             boolean advancing = steps.indexOf(step) > steps
                     .indexOf(currentStep);
 
             // TODO TODO. Here, replace currentStep with stepIndex - 1 or
             // stepIndex + 1, since that would be the currentStep if just
             // jumping one step anyways
 
             if (advancing) {
                 WizardStep curr = steps.get(steps.indexOf(step) - 1); // "current"
                                                                       // step
                 if (!curr.onAdvance()) {
                     // not allowed to advance
                     return false;
                 }
             } else {
                 WizardStep curr = steps.get(steps.indexOf(step) + 1); // "current"
                                                                       // step
                 if (!curr.onBack()) {
                     // uriFragment.setF
                     // not allowed to go back
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     /**
      * At this point, no check will be made. The step will be activated. Call
      * checkCanStepBeActivated first
      * 
      * @param step
      */
     protected void activateStep(WizardStep step) {
 
         if (currentStep != null) {
             // keep track of the last step that was completed
             int currentIndex = steps.indexOf(currentStep);
             // lastCompletedStep will not be changed if going backwards
             if (lastCompletedStep == null
                     || steps.indexOf(lastCompletedStep) < currentIndex) {
                 lastCompletedStep = currentStep;
             }
         }
 
         contentPanel.setContent(step.getContent());
         currentStep = step;
 
         updateUriFragment();
         updateButtons();
         step.onActivate(); // Extra feature
         fireEvent(new WizardStepActivationEvent(this, step));
     }
 
     protected void tryToActivateStep(String id) {
         WizardStep stepToActivate = idMap.get(id);
         if (stepToActivate != null) {
             // check that we don't go past the lastCompletedStep by using the id
             int lastCompletedIndex = lastCompletedStep == null ? -1 : steps
                     .indexOf(lastCompletedStep);
             int stepToActivateIndex = steps.indexOf(stepToActivate);
             int currIndex = steps.indexOf(currentStep);
 
             // If clicking same item, do nothing
             if (stepToActivateIndex == currIndex) {
                 return;
             }
             int inc;
             boolean movingForward = stepToActivateIndex - currIndex > 0;
             int stepsToMove = Math.abs(stepToActivateIndex - currIndex);
 
             if (movingForward) {
                 inc = 1;
             } else {
                 inc = -1;
             }
 
             WizardStep lastCheckedStep = currentStep;
             boolean success = true;
             for (int i = 1, index = currIndex; i <= stepsToMove; i++, index += inc) {
 
                 WizardStep stepToCheck = steps.get(index + inc);
 
                 // If we cannot activate next step, lets activate the lastest
                 // known checked step
                 if (!checkCanStepBeActivated(stepToCheck)) {
                     // Do not activate the current step again if we cannot move
                     // away from it.
                     if (currentStep != lastCheckedStep) {
                         activateStep(lastCheckedStep);
                     }
                     success = false;
                     break;
                 }
                 lastCheckedStep = stepToCheck;
             }
 
             if (success) {
                 activateStep(stepToActivate);
             }
 
             // We will no longer prevent a user for moving past the last step
             // using indices
             // if (lastCompletedIndex < stepIndex) {
             // activateStep(lastCompletedStep);
             // } else {
             // activateStep(stepToActivate);
             // }
         }
     }
 
     protected String getId(WizardStep step) {
         for (Map.Entry<String, WizardStep> entry : idMap.entrySet()) {
             if (entry.getValue().equals(step)) {
                 return entry.getKey();
             }
         }
         return null;
     }
 
     private void updateUriFragment() {
         if (isUriFragmentEnabled()) {
             String currentStepId = getId(currentStep);
             if (currentStepId != null && currentStepId.length() > 0) {
                 Page.getCurrent().setUriFragment(currentStepId, false);
             } else {
                 Page.getCurrent().setUriFragment(null, false);
             }
         }
     }
 
     protected boolean isFirstStep(WizardStep step) {
         if (step != null) {
             return steps.indexOf(step) == 0;
         }
         return false;
     }
 
     protected boolean isLastStep(WizardStep step) {
         if (step != null && !steps.isEmpty()) {
             return steps.indexOf(step) == (steps.size() - 1);
         }
         return false;
     }
 
     /**
      * Cancels this Wizard triggering a {@link WizardCancelledEvent}. This
      * method is called when user clicks the cancel button.
      */
     public void cancel() {
         fireEvent(new WizardCancelledEvent(this));
     }
 
     /**
      * Triggers a {@link WizardCompletedEvent} if the current step is the last
      * step and it allows advancing (see {@link WizardStep#onAdvance()}). This
      * method is called when user clicks the finish button.
      */
     public void finish() {
        if (isLastStep(currentStep)) {
            if (currentStep.onAdvance()) {
                // next (finish) allowed -> fire complete event
                fireEvent(new WizardCompletedEvent(this));
            }
         } else if (currentLinkmode == LinkMode.ALL) {
             // having LinkMode.ALL enabled, one can press finish button
             // right from start
             WizardStep lastStep = steps.get(steps.size() - 1);
 
             // Do not activate the last step if it is the current one
             if (currentStep != lastStep) {
                 // If all goes well, after this, currentStep should match
                 // lastStep
                 tryToActivateStep(getId(lastStep));
             }
 
            if (currentStep == lastStep && currentStep.onAdvance()) {
                 fireEvent(new WizardCompletedEvent(this));
             }
         }
     }
 
     /**
      * Activates the next {@link WizardStep} if the current step allows
      * advancing (see {@link WizardStep#onAdvance()}) or calls the
      * {@link #finish()} method the current step is the last step. This method
      * is called when user clicks the next button.
      */
     public void next() {
         if (isLastStep(currentStep)) {
             finish();
         } else {
             int currentIndex = steps.indexOf(currentStep);
             WizardStep step = steps.get(currentIndex + 1);
             if (checkCanStepBeActivated(step)) {
                 activateStep(step);
             }
         }
     }
 
     /**
      * Activates the previous {@link WizardStep} if the current step allows
      * going back (see {@link WizardStep#onBack()}) and the current step is not
      * the first step. This method is called when user clicks the back button.
      */
     public void back() {
         int currentIndex = steps.indexOf(currentStep);
         if (currentIndex > 0) {
             WizardStep step = steps.get(currentIndex - 1);
             if (checkCanStepBeActivated(step)) {
                 activateStep(step);
             }
         }
     }
 
     public void uriFragmentChanged(UriFragmentChangedEvent event) {
         if (isUriFragmentEnabled()) {
             String fragment = event.getUriFragment();
             if (fragment == null || fragment.equals("") && !steps.isEmpty()) {
                 // empty fragment -> set the fragment of first step
                 Page.getCurrent().setUriFragment(getId(steps.get(0)));
             } else {
                 tryToActivateStep(fragment);
             }
         }
     }
 
     /**
      * Removes the given step from this Wizard. An {@link IllegalStateException}
      * is thrown if the given step is already completed or is the currently
      * active step.
      * 
      * @param stepToRemove
      *            the step to remove.
      * @see #isCompleted(WizardStep)
      * @see #isActive(WizardStep)
      */
     public void removeStep(WizardStep stepToRemove) {
         if (idMap.containsValue(stepToRemove)) {
             for (Map.Entry<String, WizardStep> entry : idMap.entrySet()) {
                 if (entry.getValue().equals(stepToRemove)) {
                     // delegate the actual removal to the overloaded method
                     removeStep(entry.getKey());
                     return;
                 }
             }
         }
     }
 
     /**
      * Removes the step with given id from this Wizard. An
      * {@link IllegalStateException} is thrown if the given step is already
      * completed or is the currently active step.
      * 
      * @param id
      *            identifier of the step to remove.
      * @see #isCompleted(WizardStep)
      * @see #isActive(WizardStep)
      */
     public void removeStep(String id) {
         if (idMap.containsKey(id)) {
             WizardStep stepToRemove = idMap.get(id);
             if (isCompleted(stepToRemove)) {
                 throw new IllegalStateException(
                         "Already completed step cannot be removed.");
             }
             if (isActive(stepToRemove)) {
                 throw new IllegalStateException(
                         "Currently active step cannot be removed.");
             }
 
             idMap.remove(id);
             steps.remove(stepToRemove);
 
             // notify listeners
             fireEvent(new WizardStepSetChangedEvent(this));
             progressBar.markAsDirty();
         }
     }
 
     /** Returns the id for this step(that is used as uriFragment) **/
     public String getUriFragmentForStep(WizardStep step) {
         return getId(step);
     }
 
     /**
      * Sets the link mode for the Wizard. LinkMode.NONE does not show any links,
      * LinkMode.PREVIOUS only shows steps prior to the current step as links and
      * LinkMode.ALL always shows the steps as links.
      * 
      * @param linkMode
      */
     public void setLinkMode(LinkMode linkMode) {
         currentLinkmode = linkMode;
         // TODO: Will fail if setting some other linkMode if currentStep =
         // lastStep
         updateButtons();
         progressBar.markAsDirty();
     }
 
     /**
      * Sets the width (in pixels) of the progress bar if it is vertically laid
      * out. Needs the steps to be vertically laid out, otherwise this will have
      * no effect.
      * 
      * @param pixels
      */
     public void setProgressBarWidth(int pixels) {
         ((WizardProgressBar) progressBar).setPixelWidth(pixels);
     }
 
 }
