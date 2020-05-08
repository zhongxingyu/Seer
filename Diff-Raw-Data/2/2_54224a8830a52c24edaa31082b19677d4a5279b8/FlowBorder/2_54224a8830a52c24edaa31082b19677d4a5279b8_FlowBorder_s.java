 package org.amplafi.flow.web.components;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.tacos.annotations.Cached;
 
 import org.amplafi.flow.FlowConstants;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowTransition;
 import org.amplafi.flow.TransitionType;
 import org.amplafi.flow.FlowValidationResult;
 import org.amplafi.flow.web.BaseFlowComponent;
 import org.amplafi.flow.web.FlowResultHandler;
 import org.amplafi.flow.web.FlowWebUtils;
 import org.amplafi.flow.web.models.LocalizedBeanPropertySelectionModel;
 import org.apache.tapestry.IActionListener;
 import org.apache.tapestry.IMarkupWriter;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.IComponent;
 import org.apache.tapestry.annotations.Component;
 import org.apache.tapestry.annotations.InjectObject;
 import org.apache.tapestry.annotations.Parameter;
 import org.apache.tapestry.form.BeanPropertySelectionModel;
 import org.apache.tapestry.listener.ListenerInvoker;
 
 
 import static org.apache.commons.collections.CollectionUtils.*;
 
 /**
  * A border-style component for flows.
  */
 public abstract class FlowBorder extends BaseFlowComponent {
 
     @Component(type = "FlowControl", bindings = {
             "attachedFlowState=attachedFlowState", "updateComponents=ognl:componentsToRefresh" }, inheritedBindings = {
             "async", "disabled" })
     public abstract FlowControl getFlowControl();
 
     @Component(type = "FlowDebug", bindings = {
             "attachedFlowState=attachedFlowState", "disabled=!debug or disabled" })
     public abstract FlowDebug getFlowDebug();
 
     /**
      * @return true if the flow control widget should be displayed.
      */
     @Parameter
     public abstract boolean getHideFlowControl();
 
     @Override
     @Parameter(defaultValue = "true")
     public abstract boolean isAsync();
 
     /**
      * to operate in debug mode.
      *
      * @return When true, information on the current flow and flow activity will
      *         be displayed.
      *
      */
     @Parameter
     public abstract boolean getDebug();
 
     @Parameter
     public abstract List<String> getAdditionalUpdateComponents();
 
     @Parameter(defaultValue = "true")
     public abstract boolean isUsingLinkSubmit();
 
     @Parameter
     public abstract Map<String, FlowTransition> getFsFlowTransitions();
 
     /**
      * Runs after cancel/finish listener
      *
      * @param listener What to do if flow ends somehow ( cancel or finish).
      */
     @Parameter
     public abstract void setEndListener(IActionListener listener);
 
     public abstract IActionListener getEndListener();
 
     /**
      * @param listener What to do on flow cancel.
      */
     @Parameter(aliases = "onCancel")
     public abstract void setCancelListener(IActionListener listener);
 
     public abstract IActionListener getCancelListener();
 
     /**
      * @param listener What to do on next button. Default behavior it to advance
      *        the flow to the next step.
      */
     @Parameter(aliases = "onNext")
     public abstract void setNextListener(IActionListener listener);
 
     public abstract IActionListener getNextListener();
 
     /**
      * @param listener invoked when previous button is pressed. Default behavior
      *        it to move back the flow to the previous step.
      */
     @Parameter(aliases = "onPrevious")
     public abstract void setPreviousListener(IActionListener listener);
 
     public abstract IActionListener getPreviousListener();
 
     /**
      * @param listener invoked when finish button is pressed.
      */
     @Parameter(aliases = "onFinish")
     public abstract void setFinishListener(IActionListener listener);
 
     public abstract IActionListener getFinishListener();
     @Parameter(aliases = "onUpdate")
     public abstract void setUpdateListener(IActionListener listener);
 
     public abstract IActionListener getUpdateListener();
 
     @InjectObject("service:amplafi.flow.resultHandler")
     public abstract FlowResultHandler getFlowResultHandler();
 
     public abstract FlowTransition getFlowTransition();
 
     public abstract ListenerInvoker getListenerInvoker();
 
     public abstract String getPropertyName();
 
     public abstract void setRedirectLocation(String redirectLocation);
 
     public abstract String getRedirectLocation();
 
     @Cached(resetAfterRewind=true)
     public List<FlowTransition> getFlowTransitions() {
         List<FlowTransition> transitions = new ArrayList<FlowTransition>(getFsFlowTransitions().values());
         if ( !getHasFinish()) {
             for(Iterator<FlowTransition> iterator = transitions.iterator(); iterator.hasNext();) {
                 if (iterator.next().isCompletingFlow()) {
                     iterator.remove();
                 }
             }
         } else {
             // find the normal finish and put that first
             for(Iterator<FlowTransition> iterator = transitions.iterator(); iterator.hasNext();) {
                 FlowTransition defaultTransition = iterator.next();
                 if ( defaultTransition.getTransitionType() == TransitionType.normal) {
                     iterator.remove();
                     transitions.add(0, defaultTransition);
                     break;
                 }
             }
         }
         return transitions;
     }
     /**
      * Contained components can reference the flow border and modify the
      * listeners. On the other hand, pages can directly set the listeners as
      * parameters to this component.
      *
      * @param cycle
      * @return current FlowBorder
      */
     public static FlowBorder get(IRequestCycle cycle) {
         return (FlowBorder) cycle.getAttribute(FlowBorder.class.getName());
     }
 
     @Override
     protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) {
         cycle.setAttribute(FlowBorder.class.getName(), this);
 
         if (getRedirectLocation() != null) {
             cycle.getResponseBuilder().addInitializationScript(this,
                     "location.href='" + getRedirectLocation() + "'");
         } else {
             super.renderComponent(writer, cycle);
         }
     }
 
     @Override
     protected void cleanupAfterRender(IRequestCycle cycle) {
         cycle.removeAttribute(FlowBorder.class.getName());
         super.cleanupAfterRender(cycle);
     }
 
     @Cached(resetAfterRewind = true)
     public List<String> getComponentsToRefresh() {
         if (!isAsync()) {
             return null;
         }
         List<String> updateList = new ArrayList<String>();
         updateList.addAll(findComponentsToUpdate(getUpdateComponents()));
         if (getAdditionalUpdateComponents() != null && !getAdditionalUpdateComponents().isEmpty()) {
             updateList.addAll(getAdditionalUpdateComponents());
         }
         return updateList;
     }
 
     public void onFinish(IRequestCycle cycle) {
         if (getForm().getDelegate().getHasErrors()) {
             return;
         }
 
         invokeIfNotNull(cycle, getFinishListener(), getEndListener());
 
         FlowState currentFlowState = getAttachedFlowState();
         FlowValidationResult result = currentFlowState.getCurrentActivityFlowValidationResult();
 
         if (result.isValid()) {
             String page = currentFlowState.finishFlow();
             FlowWebUtils.activatePageIfNotNull(cycle, page, currentFlowState);
         } else {
             getFlowResultHandler().handleFlowResult(result, this);
         }
     }
 
     public void onOneAlternateFlow(IRequestCycle cycle) {
         FlowState attachedFlow = getAttachedFlowState();
         FlowTransition flowTransition = getFlowTransitions().get(0);
         String key = flowTransition.getKey();
         attachedFlow.setFinishType(key);
         onFinish(cycle);
     }
 
     public void onMultipleAlternateFlow(IRequestCycle cycle) {
         FlowState attachedFlow = getAttachedFlowState();
         FlowTransition flowTransition = getFlowTransition();
        String nextFlow = flowTransition.getNextFlow();
         /// HACK should be calling flowTransition.getFlowLauncher()
         if (flowTransition.isMorphingFlow()) {
             String page = attachedFlow.morphFlow(nextFlow, flowTransition.getInitialValues());
             FlowWebUtils.activatePageIfNotNull(cycle, page, attachedFlow);
         } else {
             attachedFlow.setFinishType(flowTransition.getKey());
             onFinish(cycle);
         }
     }
 
     public void onCancel(IRequestCycle cycle) {
         invokeIfNotNull(cycle, getCancelListener(), getEndListener());
 
         FlowState currentFlowState = getAttachedFlowState();
         String page = currentFlowState.cancelFlow();
         FlowWebUtils.activatePageIfNotNull(cycle, page, currentFlowState);
     }
 
     public void onPrevious(IRequestCycle cycle) {
         invokeIfNotNull(cycle, getPreviousListener());
         FlowState currentFlowState = getAttachedFlowState();
         String page = currentFlowState.previous().getPageName();
         FlowWebUtils.activatePageIfNotNull(cycle, page, currentFlowState);
     }
 
     /**
      * advance to next step.
      *
      * @param cycle
      */
     public void onNext(IRequestCycle cycle) {
         if (getForm().getDelegate().getHasErrors()) {
             return;
         }
 
         invokeIfNotNull(cycle, getNextListener());
 
         FlowState currentFlowState = getAttachedFlowState();
         FlowValidationResult result = currentFlowState.getCurrentActivityFlowValidationResult();
         if (result.isValid()) {
             String page = currentFlowState.next().getPageName();
             FlowWebUtils.activatePageIfNotNull(cycle, page, currentFlowState);
         } else {
             getFlowResultHandler().handleFlowResult(result, this);
         }
     }
 
     /**
      * submit form but do not advance flow.
      *
      * @param cycle
      */
     public void onUpdate(IRequestCycle cycle) {
         invokeIfNotNull(cycle, getUpdateListener());
 
         FlowState currentFlowState = getAttachedFlowState();
         FlowValidationResult result = currentFlowState.getCurrentActivityFlowValidationResult();
         if (result.isValid()) {
             String page = currentFlowState.getCurrentPage();
             FlowWebUtils.activatePageIfNotNull(cycle, page, currentFlowState);
         } else {
             getFlowResultHandler().handleFlowResult(result, this);
         }
     }
     @Cached(resetAfterRewind=true)
     public String getNextLabel() {
         // see if &raquo; is indeed better
         return getMessages().getMessage("flow.label-next-step");
     }
     @Cached(resetAfterRewind=true)
     public String getPreviousLabel() {
         // see if &laquo; is indeed better
         return getMessages().getMessage("flow.label-prev-step");
     }
 
     protected void invokeIfNotNull(IRequestCycle cycle, IActionListener... listeners) {
         for (IActionListener listener : listeners) {
             if (listener != null) {
                 getListenerInvoker().invokeListener(listener, this, cycle);
             }
         }
     }
     @Cached(resetAfterRewind=true)
     public String getUpdateLabel() {
         return processLabel(getAttachedFlowState().getUpdateText(), "flow.label-update");
     }
     @Cached(resetAfterRewind=true)
     public boolean getHasUpdate() {
         return getAttachedFlowState().isUpdatePossible();
     }
     @Cached(resetAfterRewind=true)
     public boolean getHasCancel() {
         return getAttachedFlowState().isCancelPossible();
     }
     @Cached(resetAfterRewind=true)
     public boolean getHasNext() {
         return getAttachedFlowState().hasVisibleNext();
     }
     @Cached(resetAfterRewind=true)
     public boolean getHasFinish() {
         return getAttachedFlowState().isFinishable();
     }
 
     public boolean getHasOneAlternateFlow() {
         int altFlowCount = getFlowTransitionSize();
         return getHasFinish() && altFlowCount == 1;
     }
 
     public boolean getHasMultipleAlternateFlows() {
         int altFlowCount = getFlowTransitionSize();
         return getHasFinish() && altFlowCount > 1;
     }
 
     /**
      * @return
      */
     private int getFlowTransitionSize() {
         List<FlowTransition> flowTransitions = getFlowTransitions();
         if ( isNotEmpty(flowTransitions)) {
             return flowTransitions.size();
         } else {
             return 0;
         }
     }
     @Cached(resetAfterRewind=true)
     public boolean getHasPrevious() {
         return getAttachedFlowState().hasVisiblePrevious();
     }
 
     public boolean isActivityNotCompletable() {
         return !this.getAttachedFlowState().isCurrentActivityCompletable();
     }
     @Cached(resetAfterRewind=true)
     public String getFinishText() {
         return processLabel(getAttachedFlowState().getFinishText(), "flow.label-finish");
     }
 
     public String getOneAlternateFlowText() {
         String label = getFlowTransitions().get(0).getLabel();
         return processLabel(label, null);
     }
 
 
     public String getCancelText() {
         return processLabel(getAttachedFlowState().getCancelText(), "flow.label-cancel");
     }
 
     public BeanPropertySelectionModel getFlowTransitionModel() {
         return new LocalizedBeanPropertySelectionModel(getFlowTransitions(), "label",
                 getMessages());
     }
     @Cached(resetAfterRewind=true)
     public String getFlowTitle() {
         return processLabel(getAttachedFlowState().getFlowTitle(), null);
     }
 
     public String getDefaultButtonId() {
         final IComponent comp = getComponent(getDefaultButtonName());
         if (comp instanceof FinishSelectorProvider) {
             return ((FinishSelectorProvider)comp).getButtonId();
         } else {
             return comp.getClientId();
         }
     }
 
     public String getDefaultButtonName() {
         if (getHasNext()) {
             return "next";
         } else if (getHasOneAlternateFlow()) {
             return "OneAltFlow";
         } else {
             return "MultipleAltFlow";
         }
     }
 
     @Cached(resetAfterRewind=true)
     public boolean getIncludeFlowControl() {
         return !(getHideFlowControl() || getAttachedFlowState().getPropertyAsObject(FlowConstants.FSHIDE_FLOW_CONTROL, boolean.class))
                 && getAttachedFlowState().getVisibleActivities().size() > 1;
     }
 }
