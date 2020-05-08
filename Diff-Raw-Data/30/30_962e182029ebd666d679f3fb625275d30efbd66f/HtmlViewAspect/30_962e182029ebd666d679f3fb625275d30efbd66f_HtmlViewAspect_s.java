 /*
  * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.romaframework.aspect.view.html;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.core.feature.CoreFieldFeatures;
 import org.romaframework.aspect.flow.FlowAspect;
 import org.romaframework.aspect.session.SessionAspect;
 import org.romaframework.aspect.session.SessionInfo;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.ViewAspectAbstract;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.ViewException;
 import org.romaframework.aspect.view.ViewHelper;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.command.ViewCommand;
 import org.romaframework.aspect.view.command.impl.DownloadReaderViewCommand;
 import org.romaframework.aspect.view.command.impl.DownloadStreamViewCommand;
 import org.romaframework.aspect.view.command.impl.OpenWindowViewCommand;
 import org.romaframework.aspect.view.command.impl.RedirectViewCommand;
 import org.romaframework.aspect.view.command.impl.RefreshViewCommand;
 import org.romaframework.aspect.view.command.impl.ReportingDownloadViewCommand;
 import org.romaframework.aspect.view.command.impl.ShowViewCommand;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewClassFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.form.ContentForm;
 import org.romaframework.aspect.view.form.FormViewer;
 import org.romaframework.aspect.view.form.ViewComponent;
 import org.romaframework.aspect.view.html.area.HtmlViewFormAreaInstance;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenAreaInstance;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenPopupAreaInstance;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewContentForm;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewInvisibleContentComponent;
 import org.romaframework.aspect.view.html.form.helper.FormUtils;
 import org.romaframework.aspect.view.html.screen.HtmlViewScreen;
 import org.romaframework.aspect.view.screen.Screen;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.binding.Bindable;
 import org.romaframework.core.domain.type.TreeNodeMap;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.FieldRefreshListener;
 import org.romaframework.core.flow.SchemaFieldListener;
 import org.romaframework.core.schema.Feature;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassResolver;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaFeaturesChangeListener;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 import org.romaframework.core.schema.SchemaObject;
 import org.romaframework.web.session.HttpAbstractSessionAspect;
 
 /**
  * The implementation of the metaframework view aspect interface
  * 
  * @author Giordano Maestro (giordano.maestro--at--assetdata.it)
  * 
  */
 public class HtmlViewAspect extends ViewAspectAbstract implements SchemaFeaturesChangeListener, FieldRefreshListener, SchemaFieldListener {
 
 	private static Log					log	= LogFactory.getLog(HtmlViewAspect.class);
 	private Map<String, String>	typeRenders;
 	private Set<String>					formRenders;
 
 	public HtmlViewAspect() {
 		Controller.getInstance().registerListener(FieldRefreshListener.class, this);
 		Controller.getInstance().registerListener(SchemaFieldListener.class, this);
 		Controller.getInstance().registerListener(SchemaFeaturesChangeListener.class, this);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspectAbstract#startup()
 	 */
 	@Override
 	public void startup() {
 		super.startup();
 
 		Roma.component(SchemaClassResolver.class).addPackage(Utility.getRomaAspectPackage(aspectName()) + ".html");
 		Roma.component(SchemaClassResolver.class).addPackage(Utility.getRomaAspectPackage(aspectName()) + ".html.domain");
 		Roma.component(SchemaClassResolver.class).addPackage("org.romaframework.web.session.domain.view");
 		Roma.component(SchemaClassResolver.class).addPackage("org.romaframework.aspect.view.html.domain");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspect#createForm(org.romaframework.core.schema.SchemaObject,
 	 * org.romaframework.core.schema.SchemaField, org.romaframework.aspect.view.form.ViewComponent)
 	 */
 	public ContentForm createForm(final SchemaObject schemaClass, final SchemaField schemaField, final ViewComponent parent) {
 		return new HtmlViewConfigurableEntityForm(null, schemaClass, schemaField, null, null, null, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspect#releaseForm(org.romaframework.aspect.view.form.ContentForm)
 	 */
 	public void releaseForm(final ContentForm formInstance) {
 		if (formInstance == null) {
 			return;
 		}
 
 		try {
 			// REMOVE THE ASSOCIATION FROM THE USER OBJECT AND THE FORM
 			((HtmlViewContentForm) formInstance).clearComponents();
 			removeObjectFormAssociation(formInstance.getContent(), null);
 		} catch (final Exception e) {
 			log.error("[FormPool.releaseForm] Error", e);
 		}
 	}
 
 	@Override
 	public void removeObjectFormAssociation(Object iUserObject, SessionInfo iSession) {
 		if (iSession == null)
 			if (iSession == null)
 				iSession = Roma.session().getActiveSessionInfo();
 
 		// REMOVE OBJECT-FORM ASSOCIATION
 		Map<Object, ViewComponent> userForms = objectsForms.get(iSession);
 		if (userForms != null) {
 			if (log.isDebugEnabled())
 				log.debug("[ViewAspectAbstract.removeObjectFormAssociation] Flushing form: " + iUserObject);
 			ViewComponent comp = userForms.remove(iUserObject);
 			HtmlViewAspectHelper.getHtmlViewSession().removeRenderableBinding((HtmlViewRenderable) comp);
 
 		}
 
 	}
 
 	@Override
 	public void show(Object content, String position, Screen screen, SessionInfo session, SchemaObject schema) throws ViewException {
 		super.show(content, position, screen, session, schema);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspect#showForm(org.romaframework.aspect.view.form.ContentForm, java.lang.String,
 	 * org.romaframework.aspect.view.screen.Screen)
 	 */
 	public String showForm(final ContentForm form, String where, final Screen desktop) {
 
 		if (where != null && (where.startsWith(HtmlViewScreen.SCREEN_POPUP) || where.startsWith(HtmlViewScreen.POPUP))) {
 			HtmlViewScreenAreaInstance area = (HtmlViewScreenAreaInstance) desktop.getArea(HtmlViewScreen.POPUPS);
 			HtmlViewScreenPopupAreaInstance popupArea = new HtmlViewScreenPopupAreaInstance(area, "popup");
 			area.addChild((TreeNodeMap) popupArea);
 			area.setDirty(true);
 			popupArea.bindForm((HtmlViewContentForm) form);
 			if (where.startsWith(HtmlViewScreen.SCREEN_DOUBLE_DOTS))
 				where = where.substring(HtmlViewScreen.SCREEN_DOUBLE_DOTS.length());
 			if (where.contains(":"))
 				where = where.substring(0, where.indexOf(":"));
 			form.setScreenArea(where);
 			return where;
 		}
 
 		if (where.startsWith("form:")) {
 			HtmlViewScreenArea screenArea = (HtmlViewScreenArea) desktop.getArea("body");
 			HtmlViewConfigurableEntityForm parentComponent = (HtmlViewConfigurableEntityForm) screenArea.getForm();
 
 			AreaComponent area = parentComponent.searchAreaForRendering(where, null);
 
 			((HtmlViewFormAreaInstance) area).addComponent((HtmlViewGenericComponent) form);
 			form.setScreenArea("body");
 			parentComponent.setDirty(true);
 			((HtmlViewFormAreaInstance) area).setDirty(true);
 			return area.getName();
 
 		} else {
 			HtmlViewScreenAreaInstance area = (HtmlViewScreenAreaInstance) desktop.getArea(where);
 			if (area == null) {
 				area = (HtmlViewScreenAreaInstance) ((HtmlViewScreen) desktop).getDefaultArea();
 				Roma.view().getScreen().setActiveArea(area.getName());
 			}
 			area.bindForm((HtmlViewContentForm) form);
 			form.setScreenArea(where);
 			return area.getName();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspectAbstract#onSessionDestroying(org.romaframework.aspect.session.SessionInfo)
 	 */
 	@Override
 	public void onSessionDestroying(final SessionInfo iSession) {
 		super.onSessionDestroying(iSession);
 		HtmlViewAspectHelper.destroyHtmlViewSession();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.schema.SchemaFeaturesChangeListener#signalChangeAction(java.lang.Object, java.lang.String,
 	 * java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
 	 */
 	public <T> void signalChangeAction(Object userObject, String actionName, final Feature<T> featureName, final T oldValue, final T featureValue) {
 
 		userObject = SchemaHelper.getFieldObject(userObject, actionName);
 		final HtmlViewContentForm form = (HtmlViewContentForm) getFormByObject(userObject);
 
 		// There id no form for the component
 		if (form == null) {
 			log.warn("The form for the object " + userObject + " doesn't exist");
 			return;
 		}
 
 		if (oldValue == null && featureValue == null) {
 			return;
 		}
 
 		if (oldValue != null && oldValue.equals(featureValue) || featureValue != null && featureValue.equals(oldValue)) {
 			return;
 		}
 
 		if (featureName.equals(ViewActionFeatures.VISIBLE) || featureName.equals(ViewActionFeatures.RENDER)) {
 			int pos = actionName.lastIndexOf(Utility.PACKAGE_SEPARATOR_STRING);
 			if (pos != -1) {
 				actionName = actionName.substring(pos + 1);
 			}
 			SchemaAction action = form.getSchemaObject().getAction(actionName);
 			if (action != null) {
 				if (featureValue instanceof Boolean && (Boolean) featureValue) {
 					FormUtils.createActionComponent(action, form);
 				} else {
 					form.removeFieldComponent(actionName);
 					form.setDirty(true);
 				}
 
 			}
 		}
 
 		if (featureName.equals(ViewActionFeatures.ENABLED)) {
 			((HtmlViewConfigurableEntityForm) form).setDirty(true);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.schema.SchemaFeaturesChangeListener#signalChangeClass(java.lang.Object, java.lang.String,
 	 * java.lang.String, java.lang.Object, java.lang.Object)
 	 */
 	public <T> void signalChangeClass(final Object userObject, final Feature<T> featureName, final T oldValue, final T featureValue) {
 		final HtmlViewContentForm form = (HtmlViewContentForm) getFormByObject(userObject);
 
 		// There id no form for the component
 		if (form == null) {
 			log.warn("The form for the object " + userObject + " doesn't exist");
 			return;
 		}
 
 		if (oldValue == null && featureValue == null) {
 			return;
 		}
 
 		if (oldValue != null && oldValue.equals(featureValue) || featureValue != null && featureValue.equals(oldValue)) {
 			return;
 		}
 
 		if (featureName.equals(ViewClassFeatures.RENDER)) {
 			form.placeComponents();
 		}
 
 		if (featureName.equals(ViewClassFeatures.STYLE)) {
 			((HtmlViewConfigurableEntityForm) form).setDirty(true);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.schema.SchemaFeaturesChangeListener#signalChangeField(java.lang.Object, java.lang.String,
 	 * java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
 	 */
 	public <T> void signalChangeField(Object userObject, String fieldName, final Feature<T> featureName, final T oldValue, final T featureValue) {
 
 		userObject = SchemaHelper.getFieldObject(userObject, fieldName);
 		final HtmlViewContentForm form = (HtmlViewContentForm) getFormByObject(userObject);
 
 		// There id no form for the component
 		if (form == null) {
 			log.warn("The form for the object " + userObject + " doesn't exist");
 			return;
 		}
 
 		if (oldValue == null && featureValue == null) {
 			return;
 		}
 
 		if (featureName.equals(ViewFieldFeatures.DEPENDS)) {
 			changeFieldDepends(fieldName, form, featureValue);
 			return;
 		} else if (featureName.equals(ViewFieldFeatures.DEPENDS_ON)) {
 			changeFieldDependsOn(fieldName, form, oldValue, featureValue);
 			return;
 		}
 
 		if (oldValue != null && oldValue.equals(featureValue) || featureValue != null && featureValue.equals(oldValue)) {
 			return;
 		}
 
 		int pos = fieldName.lastIndexOf(Utility.PACKAGE_SEPARATOR_STRING);
 		if (pos != -1) {
 			fieldName = fieldName.substring(pos + 1);
 		}
 		if (featureName.equals(ViewFieldFeatures.VISIBLE) || featureName.equals(ViewFieldFeatures.RENDER)) {
 			SchemaField field = form.getSchemaObject().getField(fieldName);
 			FormUtils.createFieldComponent(field, form);
 		}
 
		HtmlViewRenderable comp = form.getFieldComponent(fieldName);

 		if (featureName.equals(ViewFieldFeatures.ENABLED)) {

 			form.setDirty(true);
 		}
 
 	}
 
 	// Methods from interface UserObjectEventListener //
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.flow.UserObjectEventListener#onAfterFieldRead(java.lang.Object,
 	 * org.romaframework.core.schema.SchemaField, java.lang.Object)
 	 */
 	public Object onAfterFieldRead(final Object content, final SchemaField field, final Object currentValue) {
 		return currentValue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.flow.UserObjectEventListener#onAfterFieldWrite(java.lang.Object,
 	 * org.romaframework.core.schema.SchemaField, java.lang.Object)
 	 */
 	public Object onAfterFieldWrite(final Object content, final SchemaField field, final Object currentValue) {
 		String[] dependencies = field.getFeature(ViewFieldFeatures.DEPENDS);
 		if (dependencies != null) {
 			Roma.fieldChanged(content, dependencies);
 		}
 		return currentValue;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.flow.UserObjectEventListener#onBeforeFieldRead(java.lang.Object,
 	 * org.romaframework.core.schema.SchemaField, java.lang.Object)
 	 */
 	public Object onBeforeFieldRead(final Object content, final SchemaField field, final Object currentValue) {
 		return IGNORED;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.flow.UserObjectEventListener#onBeforeFieldWrite(java.lang.Object,
 	 * org.romaframework.core.schema.SchemaField, java.lang.Object)
 	 */
 	public Object onBeforeFieldWrite(final Object content, final SchemaField field, final Object currentValue) {
 		return IGNORED;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.core.flow.UserObjectEventListener#onFieldRefresh(org.romaframework.aspect.session.SessionInfo,
 	 * java.lang.Object, org.romaframework.core.schema.SchemaField)
 	 */
 	public void onFieldRefresh(final SessionInfo iSession, final Object iContent, final SchemaField iField) {
 		final HtmlViewContentForm form = (HtmlViewContentForm) getFormByObject(iSession, iContent);
 
 		if (!iField.getFeature(ViewFieldFeatures.VISIBLE))
 			return;
 
 		if (form == null) {
 			// FORM NOT YET CREATED: JUST RETURN
 			return;
 		}
 
 		final HtmlViewContentComponent componentToUpdate = form.getFieldComponent(iField.getName());
 
 		if (componentToUpdate != null) {
 			// TODO TEST IT!!!
 			if (Boolean.TRUE.equals(iField.getFeature(CoreFieldFeatures.USE_RUNTIME_TYPE))) {
 				FormUtils.createFieldComponent(iField, form);
 				return;
 			}
 
 			Object displayWith = iField.getFeature(ViewFieldFeatures.DISPLAY_WITH);
 			if (displayWith != null && !Roma.schema().getSchemaClass(Bindable.class).equals(displayWith)) {
 				Object content = componentToUpdate.getContent();
 				if (content != null) {
 					((Bindable) content).setSource(iContent, iField.getName());
 				}
 				return;
 			}
 			// manage appear/disappear of null ObjectEmbedded
 			if (ViewConstants.RENDER_OBJECTEMBEDDED.equals(iField.getFeature(ViewFieldFeatures.RENDER))) {
 				Object content = componentToUpdate.getContent();
 
 				if (componentToUpdate instanceof HtmlViewInvisibleContentComponent || content == null) {
 					form.removeFieldComponent(iField.getName());
 					FormUtils.createFieldComponent(iField, form);
 					return;
 				}
 			}
 
 			final Object value = SchemaHelper.getFieldValue(form.getSchemaObject(), iField.getName(), iContent);
 			if (componentToUpdate instanceof ContentForm) {
 				ViewHelper.invokeOnShow(value);
 			}
 			((HtmlViewAbstractComponent) componentToUpdate).setDirty(true);
 
 			componentToUpdate.setContent(value);
 			// FIX FOR FIELD REFRESH OF EXPANDED COMPONENTS
 		} else if (ViewConstants.LAYOUT_EXPAND.equals(iField.getFeature(ViewFieldFeatures.POSITION))) {
 			boolean first = true;
 			for (String fieldName : iField.getClassInfo().getFields().keySet()) {
 				SchemaField iSubField = iField.getClassInfo().getFields().get(fieldName);
 				if (((Boolean) iSubField.getFeature(ViewFieldFeatures.VISIBLE))) {
 					HtmlViewContentComponent expandedComponentToUpdate = form.getFieldComponent(fieldName);
 					if (first) {
 						expandedComponentToUpdate.getContainerComponent().setContent(SchemaHelper.getFieldValue(form.getSchemaObject(), iField.getName(), iContent));
 						first = false;
 					}
 					if (expandedComponentToUpdate != null) {
 						// TODO TEST IT!!!
 						if (Boolean.TRUE.equals(iSubField.getFeature(CoreFieldFeatures.USE_RUNTIME_TYPE))) {
 							FormUtils.createFieldComponent(iSubField, form);
 							return;
 						}
 
 						// manage appear/disappear of null ObjectEmbedded
 						if (ViewConstants.RENDER_OBJECTEMBEDDED.equals(iSubField.getFeature(ViewFieldFeatures.RENDER))) {
 							Object content = expandedComponentToUpdate.getContent();
 							if (expandedComponentToUpdate instanceof HtmlViewInvisibleContentComponent || content == null) {
 								form.removeFieldComponent(fieldName);
 								FormUtils.createFieldComponent(iSubField, form);
 								return;
 							}
 						}
 
 						final Object value = SchemaHelper.getFieldValue(form.getSchemaObject(), iField.getName() + "." + fieldName, iContent);
 						if (componentToUpdate instanceof ContentForm) {
 							ViewHelper.invokeOnShow(value);
 						}
 						((HtmlViewAbstractComponent) expandedComponentToUpdate).setDirty(true);
 
 						expandedComponentToUpdate.setContent(value);
 					}
 				}
 			}
 		}
 
 	}
 
 	public void cleanDirtyComponents() {
 		cleanDirtyComponents(getScreen());
 	}
 
 	public void cleanDirtyComponents(Screen iScreen) {
 		HtmlViewScreen screen = (HtmlViewScreen) iScreen;
 		if (screen != null) {
 			HtmlViewScreenAreaInstance area = screen.getRootArea();
 			cleanDirtyArea(area);
 		}
 	}
 
 	private void changeFieldDepends(String fieldName, final HtmlViewContentForm form, final Object featureValue) {
 		SchemaField field = form.getSchemaObject().getField(fieldName);
 		Set<String> dependencies = new HashSet<String>();
 		String[] depends = (String[]) featureValue;
 		for (String dependency : depends) {
 			dependencies.add(dependency);
 		}
 		field.setFeature(ViewFieldFeatures.DEPENDS, dependencies.toArray(new String[] {}));
 		updateFieldDependencies(form.getSchemaObject().getSchemaClass());
 	}
 
 	private void changeFieldDependsOn(String fieldName, final HtmlViewContentForm form, final Object oldValue, final Object featureValue) {
 		SchemaField field = form.getSchemaObject().getField(fieldName);
 		if (oldValue != null) {
 			String[] currentDependsOn = (String[]) oldValue;
 			for (String dependsFieldName : currentDependsOn) {
 				SchemaField dependsField = form.getSchemaObject().getField(dependsFieldName);
 				Set<String> values = new HashSet<String>(Arrays.asList(dependsField.getFeature(ViewFieldFeatures.DEPENDS)));
 				values.remove(fieldName);
 			}
 		}
 		List<String> dependsOn = Arrays.asList((String[]) featureValue);
 		for (String dependsFieldName : dependsOn) {
 			SchemaField dependsField = form.getSchemaObject().getField(dependsFieldName);
 			Set<String> values = new HashSet<String>(Arrays.asList(dependsField.getFeature(ViewFieldFeatures.DEPENDS)));
 			values.add(fieldName);
 		}
 		field.setFeature(ViewFieldFeatures.DEPENDS_ON, dependsOn.toArray(new String[] {}));
 	}
 
 	private void cleanDirtyArea(HtmlViewScreenAreaInstance area) {
 		area.setDirty(false);
 		if (area.getComponents() != null)
 			for (Object child : area.getComponents()) {
 				if (child instanceof HtmlViewScreenAreaInstance) {
 					HtmlViewScreenAreaInstance childArea = (HtmlViewScreenAreaInstance) child;
 					cleanDirtyArea(childArea);
 				}
 			}
 		if (area.getComponentInArea() != null)
 			cleanDirtyForm((HtmlViewConfigurableEntityForm) area.getComponentInArea());
 	}
 
 	private void cleanDirtyForm(HtmlViewConfigurableEntityForm component) {
 		component.setDirty(false);
 		if (component.getChildren() != null)
 			for (HtmlViewGenericComponent child : component.getChildren()) {
 				if (child instanceof HtmlViewAbstractComponent)
 					cleanDirtyComponent((HtmlViewAbstractComponent) child);
 				else if (child instanceof HtmlViewConfigurableEntityForm)
 					cleanDirtyForm((HtmlViewConfigurableEntityForm) child);
 			}
 	}
 
 	private void cleanDirtyComponent(HtmlViewAbstractComponent component) {
 		component.setDirty(false);
 		if (component.getChildren() != null)
 			for (HtmlViewGenericComponent child : component.getChildren()) {
 				if (child instanceof HtmlViewAbstractComponent)
 					cleanDirtyComponent((HtmlViewAbstractComponent) child);
 				else if (child instanceof HtmlViewConfigurableEntityForm)
 					cleanDirtyForm((HtmlViewConfigurableEntityForm) child);
 			}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.ViewAspect#pushCommand(org.romaframework.aspect.view.command.ViewCommand)
 	 */
 	public void pushCommand(final ViewCommand command) {
 		if (command instanceof RedirectViewCommand) {
 			pushRedirectView((RedirectViewCommand) command);
 		} else if (command instanceof ShowViewCommand) {
 			ShowViewCommand cmd = (ShowViewCommand) command;
 			pushShowView(cmd);
 		} else if (command instanceof OpenWindowViewCommand) {
 			OpenWindowViewCommand cmd = (OpenWindowViewCommand) command;
 			pushOpenWindow(cmd);
 		} else if (command instanceof RefreshViewCommand) {
 			RefreshViewCommand cmd = (RefreshViewCommand) command;
 			pushRefreshView(cmd);
 		} else if (command instanceof DownloadStreamViewCommand) {
 			DownloadStreamViewCommand cmd = (DownloadStreamViewCommand) command;
 			pushDownloadStream(cmd);
 		} else if (command instanceof DownloadReaderViewCommand) {
 			DownloadReaderViewCommand cmd = (DownloadReaderViewCommand) command;
 			pushDownloadReader(cmd);
 		} else if (command instanceof ReportingDownloadViewCommand) {
 			ReportingDownloadViewCommand cmd = (ReportingDownloadViewCommand) command;
 			pushDownloadReporting(cmd);
 		}
 
 	}
 
 	private void pushRefreshView(RefreshViewCommand cmd) {
 		// TODO test it!!!!
 		((HtmlViewAspect) Roma.aspect(ViewAspect.class)).releaseForm((ContentForm) cmd.getForm());
 	}
 
 	private void pushShowView(ShowViewCommand cmd) {
 		Roma.aspect(FlowAspect.class).forward(cmd.getForm().getContent(), cmd.getWhere(), FormViewer.getInstance().getScreen(cmd.getSession()), cmd.getSession());
 	}
 
 	private void pushDownloadReader(DownloadReaderViewCommand command) {
 
 		Roma.component(SessionAspect.class).setProperty(DownloadReaderViewCommand.class.getSimpleName(), command);
 	}
 
 	private void pushDownloadStream(DownloadStreamViewCommand command) {
 		Roma.component(SessionAspect.class).setProperty(DownloadStreamViewCommand.class.getSimpleName(), command);
 	}
 
 	private void pushOpenWindow(OpenWindowViewCommand command) {
 		Roma.component(SessionAspect.class).setProperty(OpenWindowViewCommand.class.getSimpleName(), command);
 	}
 
 	private void pushRedirectView(final RedirectViewCommand command) {
 		Roma.component(SessionAspect.class).setProperty(RedirectViewCommand.class.getSimpleName(), command);
 	}
 
 	private void pushDownloadReporting(final ReportingDownloadViewCommand command) {
 		Roma.component(SessionAspect.class).setProperty(ReportingDownloadViewCommand.class.getSimpleName(), command);
 	}
 
 	public void configEvent(SchemaEvent event) {
 	}
 
 	public Object getUnderlyingComponent() {
 		return null;
 	}
 
 	public String getContextPath() {
 		return ((HttpServletRequest) Roma.context().component(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR)).getContextPath();
 	}
 
 	public Map<String, String> getTypeRenders() {
 		return typeRenders;
 	}
 
 	public void setTypeRenders(Map<String, String> typeRenders) {
 		this.typeRenders = typeRenders;
 	}
 
 	public String getRender(String typeName) {
 		if (typeRenders != null) {
 			typeRenders.get(typeName);
 		}
 		return null;
 	}
 
 	public Set<String> getFormRenders() {
 		return formRenders;
 	}
 
 	public void setFormRenders(Set<String> formRenders) {
 		this.formRenders = formRenders;
 	}
 
 }
