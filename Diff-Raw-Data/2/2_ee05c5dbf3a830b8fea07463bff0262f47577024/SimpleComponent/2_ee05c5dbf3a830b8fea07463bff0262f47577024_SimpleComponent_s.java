 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.components.shared;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.event.IEventSource;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.peralta.mycellar.domain.shared.repository.FilterEnum;
 import fr.peralta.mycellar.domain.shared.repository.SearchForm;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.feedback.FormComponentFeedbackBorder;
 import fr.peralta.mycellar.interfaces.client.web.renderers.RendererServiceFacade;
 import fr.peralta.mycellar.interfaces.client.web.shared.LoggingHelper;
 
 /**
  * @author speralta
  */
 public abstract class SimpleComponent<O, C extends Component> extends CompoundPropertyPanel<O> {
 
     private static final long serialVersionUID = 201107281247L;
     private static final Logger logger = LoggerFactory.getLogger(SimpleComponent.class);
 
     private static final String CONTAINER_COMPONENT_ID = "container";
     private static final String SELECTOR_COMPONENT_ID = "selector";
     private static final String VALUE_COMPONENT_ID = "value";
 
     @SpringBean
     private RendererServiceFacade rendererServiceFacade;
 
     private final FormComponentFeedbackBorder container;
     private final ValueComponent valueComponent;
     private C selectorComponent;
 
     private final IModel<SearchForm> searchFormModel;
     private boolean valued = false;
 
     /**
      * @param id
      * @param label
      * @param searchFormModel
      */
     public SimpleComponent(String id, IModel<String> label, IModel<SearchForm> searchFormModel) {
         super(id);
         this.searchFormModel = searchFormModel;
         setOutputMarkupId(true);
         setRequired(true);
         container = createBorder(CONTAINER_COMPONENT_ID, id, label);
         container.add(valueComponent = new ValueComponent(VALUE_COMPONENT_ID, id));
         add(container);
     }
 
     /**
      * @param allowed
      */
     public SimpleComponent<O, C> setCancelAllowed(boolean allowed) {
         valueComponent.setCancelAllowed(allowed);
         return this;
     }
 
     /**
      * @param value
      */
     public void setValue(O value) {
         if (value != null) {
             markAsValued(value);
         } else {
             markAsNonValued();
         }
     }
 
     /**
      * @param id
      * @param forId
      * @param label
      * @return
      */
     protected FormComponentFeedbackBorder createBorder(String id, String forId, IModel<String> label) {
         return new FormComponentFeedbackBorder(id, label, forId, true, getFilteredIdsForFeedback());
     }
 
     protected String[] getFilteredIdsForFeedback() {
         return null;
     }
 
     public boolean isContainerVisibleInHierarchy() {
         return container.isVisibleInHierarchy();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onInitialize() {
         super.onInitialize();
         container.add(selectorComponent = createSelectorComponent(SELECTOR_COMPONENT_ID));
         if (isValuedAtStart()) {
             valued = true;
             FilterEnum filterToReplace = getFilterToReplace();
             if ((filterToReplace != null) && (getSearchFormModel() != null)
                     && (getSearchFormModel().getObject() != null)) {
                 getSearchFormModel().getObject().replaceSet(filterToReplace, getModelObject());
             }
         }
     }
 
     /**
      * @return the filter to replace at start (could be null if no filter to
      *         replace)
      */
     protected abstract FilterEnum getFilterToReplace();
 
     /**
      * @param id
      * @return
      */
     protected abstract C createSelectorComponent(String id);
 
     /**
      * @return
      */
     protected boolean isReadyToSelect() {
         return true;
     }
 
     /**
      * @param object
      * @return
      */
     protected String getValueLabelFor(O object) {
         return rendererServiceFacade.render(object);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected final void onConfigure() {
         super.onConfigure();
         container.setVisibilityAllowed(isReadyToSelect());
         internalOnConfigure();
         valueComponent.setVisibilityAllowed(valued);
         if (valued) {
             valueComponent.setDefaultModel(new Model<String>(getValueLabelFor(getModelObject())));
         } else {
             valueComponent.setDefaultModel(new Model<String>());
         }
     }
 
     /**
      * 
      */
     protected void internalOnConfigure() {
         selectorComponent.setVisibilityAllowed(!valued);
     }
 
     /**
      * @param object
      *            not null
      * @return true if the object is created or selected
      */
     public final boolean isValued() {
         return valued;
     }
 
     protected final void markAsNonValued() {
         valued = false;
         setModelObject(createDefaultObject());
     }
 
     protected final void markAsValued(O object) {
         valued = true;
         setModelObject(object);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void convertInput() {
         setConvertedInput(getModelObject());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean checkRequired() {
         if (isRequired()) {
             return isValued();
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         if (isContainerVisibleInHierarchy()) {
             super.validate();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onModelChanged() {
         send(getParent(), Broadcast.BUBBLE, Action.MODEL_CHANGED);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final void onEvent(IEvent<?> event) {
         LoggingHelper.logEventReceived(logger, event);
         if (event.getPayload() instanceof Action) {
             Action action = (Action) event.getPayload();
             switch (action) {
             case SELECT:
                 onSelect(event);
                 break;
             case CANCEL:
                 onCancel(event);
                 break;
             case ADD:
                 onAdd(event);
                 break;
             case SAVE:
                 onSave(event);
                 break;
             case MODEL_CHANGED:
                 onModelChanged(event);
                 break;
             case DELETE:
                 onDelete(event);
             default:
                 throw new IllegalStateException("Unknown " + ActionEnum.class.getSimpleName()
                         + " value " + action + ".");
             }
         } else {
             onOtherEvent(event);
         }
         LoggingHelper.logEventProcessed(logger, event);
     }
 
     protected void onOtherEvent(IEvent<?> event) {
 
     }
 
     protected void onSelect(IEvent<?> event) {
         markAsValued(getModelObjectFromEvent(event.getSource()));
         AjaxTool.ajaxReRender(this);
         event.stop();
     }
 
     protected void onCancel(IEvent<?> event) {
         markAsNonValued();
         AjaxTool.ajaxReRender(this);
         event.stop();
     }
 
     protected void onModelChanged(IEvent<?> event) {
     }
 
     protected void onSave(IEvent<?> event) {
     }
 
     protected void onAdd(IEvent<?> event) {
     }
 
     protected void onDelete(IEvent<?> event) {
     }
 
     /**
      * @param source
      * @return
      */
     protected abstract O getModelObjectFromEvent(IEventSource source);
 
     /**
      * @return the container
      */
     protected final FormComponentFeedbackBorder getContainer() {
         return container;
     }
 
     /**
      * @return the valueComponent
      */
     protected final ValueComponent getValueComponent() {
         return valueComponent;
     }
 
     /**
      * @return the selectorComponent
      */
     protected final C getSelectorComponent() {
         return selectorComponent;
     }
 
     /**
      * @return the searchFormModel
      */
     protected final IModel<SearchForm> getSearchFormModel() {
         return searchFormModel;
     }
 
     protected final RendererServiceFacade getRendererServiceFacade() {
         return rendererServiceFacade;
     }
 
 }
