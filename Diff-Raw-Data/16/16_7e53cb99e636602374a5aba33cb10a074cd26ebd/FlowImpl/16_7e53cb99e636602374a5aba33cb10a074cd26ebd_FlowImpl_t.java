 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow.impl;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import org.amplafi.flow.Flow;
 import org.amplafi.flow.FlowActivity;
 import org.amplafi.flow.FlowActivityImplementor;
 import org.amplafi.flow.FlowGroup;
 import org.amplafi.flow.FlowImplementor;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowPropertyDefinition;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.FlowTransition;
 import org.amplafi.flow.FlowUtils;
 import org.amplafi.flow.flowproperty.CancelTextFlowPropertyValueProvider;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImpl;
 import org.amplafi.flow.flowproperty.MessageFlowPropertyValueProvider;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import static org.amplafi.flow.FlowConstants.*;
 import static org.amplafi.flow.flowproperty.PropertyScope.*;
 import static org.amplafi.flow.flowproperty.PropertyUsage.*;
 import static org.apache.commons.lang.StringUtils.*;
 
 
 /**
  * defines a definition of a flow or a specific flow.
  *
  * <p>
  * Flows consist of FlowActivities. FlowActivities can be shared across
  * instances of Flows.
  * <p>
  * FlowActivities can create new objects. However it is the responsibility of
  * the Flow to determine if the object created by FlowActivities should be
  * actually persisted. (i.e. committed to the database.)
  * <p>
  * Flows are also responsible for connecting relationships. A FlowActivity may
  * create a relationship object but it will not be aware of the endpoints of
  * that relationship (a FlowActivity is not aware of the Flow nor its
  * history/state.)
  * <p>
  * Flows should not keep references to objects that FlowActivities create or
  * retrieve. The FlowActivity is responsible for that. This is important if a
  * FlowActivity is shared amongst Flow instances.
  * </p>
  */
 public class FlowImpl extends BaseFlowPropertyProvider<FlowImplementor> implements Serializable, Cloneable, FlowImplementor, Iterable<FlowActivityImplementor> {
 
     private static final long serialVersionUID = -985306244948511836L;
 
     private FlowGroup primaryFlowGroup;
     private List<FlowActivityImplementor> activities;
 
     @Deprecated // use FlowPropertyDefinition
     private String flowTitle;
     @Deprecated // use FlowPropertyDefinition
     private String continueFlowTitle;
     @Deprecated // use FlowPropertyDefinition
     private String linkTitle;
     @Deprecated // use FlowPropertyDefinition
     private String pageName;
     @Deprecated // use FlowPropertyDefinition
     private String defaultAfterPage;
 
     @Deprecated // use FlowPropertyDefinition
     private String mouseoverEntryPointText;
 
     @Deprecated // use FlowPropertyDefinition
     private String flowDescriptionText;
 
     private transient FlowState flowState;
 
     @Deprecated // use FlowPropertyDefinition
     private boolean activatable;
 
     /**
      * False means {@link FlowState}s don't need to be current. True means that if a FlowState of this
      * Flow type is no longer the current flow (after being the current flow), the FlowState
      * is dropped.
      */
     @Deprecated // use FlowPropertyDefinition
     private boolean notCurrentAllowed;
 
     /**
      * Used to restore an existing definition or create an new definitions from XML
      */
     public FlowImpl() {
         // see #2179 #2192
         this.addPropertyDefinitions(
             new FlowPropertyDefinitionImpl(FSTITLE_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
             new FlowPropertyDefinitionImpl(FSNO_CANCEL, boolean.class).initAccess(flowLocal, use),
             new FlowPropertyDefinitionImpl(FSFINISH_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
             new FlowPropertyDefinitionImpl(FSRETURN_TO_TEXT).initAccess(flowLocal, use).initFlowPropertyValueProvider( MessageFlowPropertyValueProvider.INSTANCE ),
             // io -- for now because need to communicate the next page to be displayed
             // TODO think about PropertyScope/PropertyUsage
             new FlowPropertyDefinitionImpl(FSPAGE_NAME).initPropertyUsage(io),
             // TODO think about PropertyScope/PropertyUsage
             new FlowPropertyDefinitionImpl(FSAFTER_PAGE).initPropertyUsage(io),
             new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_PAGE).initAccess(flowLocal, internalState),
             new FlowPropertyDefinitionImpl(FSDEFAULT_AFTER_CANCEL_PAGE).initAccess(flowLocal, internalState),
             new FlowPropertyDefinitionImpl(FSHIDE_FLOW_CONTROL, boolean.class).initPropertyScope(flowLocal),
             new FlowPropertyDefinitionImpl(FSACTIVATABLE, boolean.class).initAccess(flowLocal, consume),
             new FlowPropertyDefinitionImpl(FSIMMEDIATE_SAVE, boolean.class).initAccess(flowLocal, internalState),
 
             new FlowPropertyDefinitionImpl(FSAPI_CALL, boolean.class).initAccess(flowLocal, io),
             new FlowPropertyDefinitionImpl(FSAUTO_COMPLETE, boolean.class).initAccess(flowLocal, internalState),
             new FlowPropertyDefinitionImpl(FSALT_FINISHED).initAccess(flowLocal, use),
             new FlowPropertyDefinitionImpl(FSREDIRECT_URL, URI.class).initPropertyUsage(io),
             new FlowPropertyDefinitionImpl(FSREFERRING_URL, URI.class).initPropertyUsage(use),
             new FlowPropertyDefinitionImpl(FSCONTINUE_WITH_FLOW).initPropertyUsage(io),
             new FlowPropertyDefinitionImpl(FSFLOW_TRANSITIONS, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
             new FlowPropertyDefinitionImpl(FSFLOW_TRANSITION, FlowTransition.class).initAccess(flowLocal, initialize),
 
             new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW).initPropertyUsage(io),
             new FlowPropertyDefinitionImpl(FSRETURN_TO_FLOW_TYPE).initPropertyUsage(io),
             new FlowPropertyDefinitionImpl(FSSUGGESTED_NEXT_FLOW_TYPE, FlowTransition.class, Map.class).initAutoCreate().initAccess(flowLocal, use),
             // TODO think about PropertyScope/PropertyUsage
             new FlowPropertyDefinitionImpl(FSNEXT_FLOW).initPropertyUsage(io)
 
         );
         CancelTextFlowPropertyValueProvider.INSTANCE.defineFlowPropertyDefinitions(this);
     }
 
     /**
      * creates a instance Flow from a definition.
      * @param definition
      */
     public FlowImpl(FlowImplementor definition) {
         super(definition);
         this.setFlowPropertyProviderName(definition.getFlowPropertyProviderName());
     }
     /**
      * Used to create a definition for testing.
      *
      * @param flowPropertyProviderName
      */
     public FlowImpl(String flowPropertyProviderName) {
         this();
         this.setFlowPropertyProviderName(flowPropertyProviderName);
     }
 
     public FlowImpl(String flowTypeName, FlowActivityImplementor... flowActivities) {
         this(flowTypeName);
         for(FlowActivityImplementor flowActivity : flowActivities) {
             addActivity(flowActivity);
         }
     }
 
     @Override
     public FlowImplementor createInstance() {
         FlowImpl inst = new FlowImpl(this);
         inst.activities = new ArrayList<FlowActivityImplementor>();
 
         if ( CollectionUtils.isNotEmpty(this.activities)) {
             for(FlowActivityImplementor activity: this.activities) {
                 FlowActivityImplementor fa = activity.createInstance();
                 if ( isActivatable() ) {
                     fa.setActivatable(true);
                 }
                 inst.addActivity(fa);
             }
             // need to always be able to start!
             inst.activities.get(0).setActivatable(true);
         }
         return inst;
     }
 
     @Override
     public void setActivities(List<FlowActivityImplementor> activities) {
         this.activities = null;
         for(FlowActivityImplementor activity: activities) {
             this.addActivity(activity);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<FlowActivityImplementor> getActivities() {
         return activities;
     }
 
     /**
      * @see java.lang.Iterable#iterator()
      */
     @Override
     public ListIterator<FlowActivityImplementor> iterator() {
         return this.activities.listIterator();
     }
 
     /**
      * @see org.amplafi.flow.Flow#getActivity(int)
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T extends FlowActivity> T getActivity(int activityIndex) {
         if ( activityIndex < 0 || activityIndex >= activities.size()) {
             // this may be case if done with the flow or we haven't started it yet.
             return null;
         }
         return (T) activities.get(activityIndex);
     }
 
     @Override
     public void addActivity(FlowActivityImplementor activity) {
         if ( activities == null ) {
             activities = new ArrayList<FlowActivityImplementor>();
         } else {
             for(FlowActivityImplementor existing: activities) {
                 if (existing.isFlowPropertyProviderNameSet() && activity.isFlowPropertyProviderNameSet() && StringUtils.equalsIgnoreCase(existing.getFlowPropertyProviderName(), activity.getFlowPropertyProviderName())) {
                     throw new IllegalArgumentException(this.getFlowPropertyProviderName()+": A FlowActivity with the same name has already been added to this flow. existing="+existing+" new="+activity);
                 }
             }
         }
         activity.setFlow(this);
         activities.add(activity);
         if ( !isInstance()) {
             activity.processDefinitions();
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T extends FlowActivity> List<T> getVisibleActivities() {
         List<T> list = new ArrayList<T>();
         for(FlowActivity flowActivity: this.activities) {
             if (!flowActivity.isInvisible() ) {
                 list.add((T)flowActivity);
             }
         }
         return list;
     }
 
     protected FlowManagement getFlowManagement() {
         return this.getFlowState() == null ? null : this.getFlowState().getFlowManagement();
     }
 
     /**
      * TODO -- copied from FlowActivityImpl -- not certain this is good idea.
      * Need somewhat to find statically defined properties - not enough to always be looking at the flowState.
      */
     protected <T> FlowPropertyDefinition getFlowPropertyDefinitionWithCreate(String key, Class<T> expected, T sampleValue) {
         FlowPropertyDefinition flowPropertyDefinition = getFlowPropertyDefinition(key);
         if (flowPropertyDefinition == null) {
            flowPropertyDefinition = getFlowManagement().createFlowPropertyDefinition((FlowImplementor)getFlow(), key, expected, sampleValue);
         }
         return flowPropertyDefinition;
     }
 
     @Override
     public String getFlowPropertyProviderName() {
         if ( super.getFlowPropertyProviderName() == null && isInstance()) {
             return getDefinition().getFlowPropertyProviderName();
         } else {
             return super.getFlowPropertyProviderName();
         }
     }
 
     @Override
     public String getFlowTitle() {
         if ( flowTitle == null && isInstance()) {
             return getDefinition().getFlowTitle();
         } else {
             return this.flowTitle;
         }
     }
 
     @Override
     public void setFlowTitle(String flowTitle) {
         this.flowTitle = flowTitle;
     }
 
     @Override
     public String getContinueFlowTitle() {
         if ( continueFlowTitle == null && isInstance()) {
             return getDefinition().getContinueFlowTitle();
         } else {
             return this.continueFlowTitle;
         }
     }
 
     @Override
     public void setContinueFlowTitle(String continueFlowTitle) {
         this.continueFlowTitle = continueFlowTitle;
     }
 
     @Override
     public void setLinkTitle(String linkTitle) {
         this.linkTitle = linkTitle;
     }
 
     @Override
     public String getLinkTitle() {
         if ( linkTitle != null ) {
             return this.linkTitle;
         } else if ( isInstance()) {
             return getDefinition().getLinkTitle();
         } else {
             return "message:" + "flow." + FlowUtils.INSTANCE.toLowerCase(this.getFlowPropertyProviderName())+".link-title";
         }
     }
 
     @Override
     public String getMouseoverEntryPointText() {
         if ( mouseoverEntryPointText == null && isInstance()) {
             return getDefinition().getMouseoverEntryPointText();
         } else {
             return this.mouseoverEntryPointText;
         }
     }
 
     @Override
     public void setMouseoverEntryPointText(String mouseoverEntryPointText) {
         this.mouseoverEntryPointText = mouseoverEntryPointText;
     }
 
     /**
      * @see org.amplafi.flow.Flow#getFlowDescriptionText()
      */
     @Override
     public String getFlowDescriptionText() {
         if ( flowDescriptionText == null && isInstance()) {
             return getDefinition().getFlowDescriptionText();
         } else {
             return this.flowDescriptionText;
         }
     }
 
     @Override
     public void setFlowDescriptionText(String flowDescriptionText) {
         this.flowDescriptionText = flowDescriptionText;
     }
 
     @Override
     public void setPageName(String pageName) {
         this.pageName = pageName;
     }
 
     /**
      * @see org.amplafi.flow.Flow#getPageName()
      */
     @Override
     public String getPageName() {
         return isInstance()&& pageName == null? getDefinition().getPageName() : pageName;
     }
 
     @Override
     public void setDefaultAfterPage(String defaultAfterPage) {
         this.defaultAfterPage = defaultAfterPage;
     }
 
     @Override
     public String getDefaultAfterPage() {
         return isInstance() && defaultAfterPage ==null? getDefinition().getDefaultAfterPage():defaultAfterPage;
     }
 
     @Override
     public void refresh() {
         int activityIndex = flowState.getCurrentActivityIndex();
         FlowActivityImplementor flowActivity = getActivity(activityIndex);
         if ( flowActivity != null ) {
             flowActivity.refresh();
         }
     }
 
     @Override
     public void setFlowState(FlowState state) {
         this.flowState = state;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <FS extends FlowState> FS getFlowState() {
         return (FS) this.flowState;
     }
 
     /**
      * @see org.amplafi.flow.Flow#indexOf(org.amplafi.flow.FlowActivity)
      */
     @Override
     public int indexOf(FlowActivity activity) {
         return this.activities.indexOf(activity);
     }
 
     /**
      * @see org.amplafi.flow.Flow#setActivatable(boolean)
      */
     @Override
     public void setActivatable(boolean activatable) {
         this.activatable = activatable;
     }
 
     /**
      * @see org.amplafi.flow.Flow#isActivatable()
      */
     @Override
     public boolean isActivatable() {
         return activatable;
     }
 
     /**
      * @see org.amplafi.flow.Flow#setNotCurrentAllowed(boolean)
      */
     @Override
     public void setNotCurrentAllowed(boolean notCurrentAllowed) {
         this.notCurrentAllowed = notCurrentAllowed;
     }
 
     /**
      * @see org.amplafi.flow.Flow#isNotCurrentAllowed()
      */
     public boolean isNotCurrentAllowed() {
         return notCurrentAllowed;
     }
 
     @Override
     public String toString() {
         return getFlowPropertyProviderName()+ (isInstance()?"(instance)":"")+" activities=["+join(this.activities, ", ")+"]";
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinition(java.lang.String)
      */
     @Override
     public FlowImplementor getFlowDefinition(String flowTypeName) {
         if ( isFlowDefined(flowTypeName)) {
             return this;
         } else {
             return null;
         }
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#getFlowDefinitions()
      */
     @Override
     public Map<String, FlowImplementor> getFlowDefinitions() {
         Map<String, FlowImplementor> map = new HashMap<String, FlowImplementor>();
         map.put(this.getFlowPropertyProviderName(), this);
         return map;
     }
 
     /**
      * @see org.amplafi.flow.definitions.DefinitionSource#isFlowDefined(java.lang.String)
      */
     @Override
     public boolean isFlowDefined(String flowTypeName) {
         return this.getFlowPropertyProviderName().equals(flowTypeName);
     }
 
     /**
      * @param primaryFlowGroup the primaryFlowGroup to set
      */
     public void setPrimaryFlowGroup(FlowGroup primaryFlowGroup) {
         this.primaryFlowGroup = primaryFlowGroup;
     }
 
     /**
      * @return the primaryFlowGroup
      */
     public FlowGroup getPrimaryFlowGroup() {
         return primaryFlowGroup;
     }
 
     /**
      * @see org.amplafi.flow.FlowProvider#getFlow()
      */
     @SuppressWarnings("unchecked")
     @Override
     public <F extends Flow> F getFlow() {
         return (F) this;
     }
 }
