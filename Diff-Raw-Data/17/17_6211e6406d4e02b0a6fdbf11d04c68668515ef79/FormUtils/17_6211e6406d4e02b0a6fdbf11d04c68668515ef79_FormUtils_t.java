 package org.romaframework.aspect.view.html.form.helper;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.core.feature.CoreFieldFeatures;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.ViewHelper;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.html.HtmlViewAspectHelper;
 import org.romaframework.aspect.view.html.area.HtmlViewFormArea;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.component.HtmlViewActionComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableExpandedEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponentImpl;
 import org.romaframework.aspect.view.html.component.HtmlViewContentForm;
 import org.romaframework.aspect.view.html.component.HtmlViewInvisibleContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewMenuForm;
 import org.romaframework.aspect.view.html.component.composed.list.HtmlViewCollectionComposedComponent;
 import org.romaframework.aspect.view.html.component.composed.list.HtmlViewLabelCollectionComponent;
 import org.romaframework.aspect.view.html.component.composed.tree.HtmlViewTreeComposedComponent;
 import org.romaframework.core.Roma;
 import org.romaframework.core.binding.Bindable;
 import org.romaframework.core.binding.BindingException;
 import org.romaframework.core.domain.type.TreeNode;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 import org.romaframework.core.schema.SchemaManager;
 import org.romaframework.core.schema.SchemaObject;
 
 public class FormUtils {
 
 	protected static Log	log	= LogFactory.getLog(FormUtils.class);
 
 	public static void createFieldComponent(final SchemaField field, final HtmlViewContentForm iForm) {
 
 		String featureLayout = (String) field.getFeature(ViewFieldFeatures.LAYOUT);
 		String featureRender = (String) field.getFeature(ViewFieldFeatures.RENDER);
 
 		if (field.getType() != null && field.getClassInfo() != null) {
 			if (featureRender == null || ViewConstants.RENDER_DEFAULT.equals(featureRender)) {
 				String tmpRender = (String) field.getClassInfo().getFeature(ViewFieldFeatures.RENDER);
 				if (tmpRender != null && !tmpRender.equals(ViewConstants.RENDER_DEFAULT)) {
 					featureRender = tmpRender;
 				}
 			}
 			if (featureLayout == null || ViewConstants.LAYOUT_DEFAULT.equals(featureLayout)) {
 				String tmpLayout = (String) field.getClassInfo().getFeature(ViewFieldFeatures.LAYOUT);
 				if (tmpLayout != null && !tmpLayout.equals(ViewConstants.LAYOUT_DEFAULT)) {
 					featureLayout = tmpLayout;
 				}
 			}
 		}
 
 		if ((Boolean) field.getFeature(ViewFieldFeatures.VISIBLE)) {
 
 			Class<?> fieldType = (Class<?>) field.getLanguageType();
 			SchemaClass displayWithClass = field.getFeature(ViewFieldFeatures.DISPLAY_WITH);
 			if (displayWithClass != null && !Roma.schema().getSchemaClass(Bindable.class).equals(displayWithClass)) {
 				Object value = null;
 				try {
 					Roma.context().create();
 					value = displayWithClass.newInstance();
 					if (value instanceof Bindable) {
 						Roma.context().create();
 						try {
 							((Bindable) value).setSource(iForm.getContent(), field.getName());
 						} finally {
 							Roma.context().destroy();
 						}
 					} else {
 						log.error("Using " + displayWithClass + " to wrap field " + field.getName() + ", it must implement Bindable interface");
 						return;
 					}
 				} catch (Exception e) {
 					log.error("Unable to instantiate " + displayWithClass, e);
 					return;
 				} finally {
 					Roma.context().destroy();
 				}
 
 				createFormComponentForDisplayReplacement(field, featureLayout, iForm, value);
 
 			} else if (SchemaHelper.isMultiValueObject(field)) {
 				if (featureRender != null && featureRender.equals(ViewConstants.RENDER_OBJECTEMBEDDED)) {
 					createExpandedCollectionFormComponents(field, featureLayout, iForm);
 				} else if (isExpandedCollection(field)) {
 					createCollectionComposedComponent(field, featureLayout, iForm);
 				} else {
 					createLabelCollectionComponent(field, featureLayout, iForm);
 				}
 				return;
 			} else if (TreeNode.class.isAssignableFrom(fieldType) && featureRender != null && featureRender.equals(ViewConstants.RENDER_TREE)) {
 				createTreeComposedComponent(field, featureLayout, iForm);
 			} else if (featureLayout != null && featureLayout.equals(ViewConstants.LAYOUT_EXPAND)) {
 				createExpandedFormComponent(field, featureLayout, iForm);
 				return;
 			} else if (featureRender != null && featureRender.equals(ViewConstants.RENDER_OBJECTEMBEDDED)) {
 				Object content = iForm.getContent();
 				if (content != null)
 					content = ViewHelper.getWrappedContent(content);
 				Object value = field.getValue(content);
 				if (content != null && value == null) {
 					createHiddenContentComponent(field, featureLayout, iForm);
 				} else {
 					createFormComponent(field, featureLayout, iForm, value);
 				}
 			} else if (featureRender != null && featureRender.equals(ViewConstants.RENDER_MENU)) {
 				// Manage the menu component
 				createMenuComponent(field, featureLayout, iForm);
 			} else {
 				// Other renders mode
 				createContentComponent(field, featureLayout, iForm);
 			}
 		} else {
 			if (featureRender == null || !featureRender.equals(ViewConstants.RENDER_MENU))
 				createHiddenContentComponent(field, featureLayout, iForm);
 		}
 	}
 
 	private static boolean isExpandedCollection(final SchemaField field) {
 
 		Object render = field.getFeature(ViewFieldFeatures.RENDER);
 
 		if (render == null)
 			render = HtmlViewAspectHelper.getDefaultRenderType(field);
 
 		if (ViewConstants.RENDER_SELECT.equals(render)) {
 			return false;
 		}
 		if (ViewConstants.RENDER_LIST.equals(render)) {
 			return false;
 		}
 		if (ViewConstants.RENDER_RADIO.equals(render)) {
 			return false;
 		}
 
 		// TODO add IoC management of exceptions
 		return true;
 	}
 
 	private static void createTreeComposedComponent(SchemaField field, String featureLayout, HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		final HtmlViewTreeComposedComponent newComponent = new HtmlViewTreeComposedComponent(iForm, field, iForm.getContent(), iForm.getScreenAreaObject());
 		newComponent.setContent(SchemaHelper.getFieldValue(field, iForm.getContent()));
 		if (iForm.getFieldComponent(field.getName()) == null) {
 			((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 	}
 
 	/**
 	 * Create a list component
 	 * 
 	 * @param field
 	 * @param featureLayout
 	 * @param iForm
 	 */
 	private static void createCollectionComposedComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		Object fieldContent = SchemaHelper.getFieldValue(field, iForm.getContent());
 		final HtmlViewCollectionComposedComponent newComponent = new HtmlViewCollectionComposedComponent(iForm, field, fieldContent, iForm.getScreenAreaObject());
 
 		if (iForm.getFieldComponent(field.getName()) == null) {
 			((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 	}
 
 	/**
 	 * Create a list of labels component
 	 * 
 	 * @param field
 	 * @param featureLayout
 	 * @param iForm
 	 */
 	private static void createLabelCollectionComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		final HtmlViewLabelCollectionComponent newComponent = new HtmlViewLabelCollectionComponent(iForm, field, SchemaHelper.getFieldValue(field,
 				iForm.getContent()), iForm.getScreenAreaObject());
 		if (iForm.getFieldComponent(field.getName()) == null) {
 			((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 	}
 
 	/**
 	 * Create a tree component
 	 * 
 	 * @param field
 	 * @param featureLayout
 	 * @param iForm
 	 */
 	private static void createMenuComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		final SchemaClass embeddedSchemaClass = field.getClassInfo();
 		final HtmlViewMenuForm newComponent = new HtmlViewMenuForm(iForm, new SchemaObject(embeddedSchemaClass), field, iForm.getScreenAreaObject(), null, null,
 				null);
 		newComponent.setContent(SchemaHelper.getFieldValue(field, iForm.getContent()));
 		if (areaForRendering instanceof HtmlViewFormArea) {
 			if (iForm.getFieldComponent(field.getName()) == null) {
 				((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 			}
 		} else {
 			final HtmlViewScreenArea screenArea = (HtmlViewScreenArea) areaForRendering;
 			try {
 				screenArea.bindForm(newComponent);
 			} catch (final Exception e) {
 				// TODO Handle he exception
 				log.error("Cannot retrieve the container component", e);
 			}
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 
 	}
 
 	private static void createExpandedCollectionFormComponents(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		Collection<?> elements = (Collection<?>) field.getValue(iForm.getContent());
 		if (elements != null) {
 
 			String prefix = field.getName();
 			int i = 0;
 			for (Object element : elements) {
 				SchemaField sf = new SchemaField(null, prefix + i++) {
 					// TODO WTF
 
 					private static final long	serialVersionUID	= -2831509394077925444L;
 
 					public Object							value;
 
 					@Override
 					public void setValue(Object iObject, Object iFieldValue) {
 						this.value = iFieldValue;
 					}
 
 					@Override
 					public SchemaClassDefinition getType() {
 						return getSchemaClassFromLanguageType();
 					}
 
 					@Override
 					public Object getValue(Object iObject) throws BindingException {
 						return value;
 					}
 
 					@Override
 					protected SchemaClass getSchemaClassFromLanguageType() {
 						if (value == null)
 							return null;
 						return Roma.schema().getSchemaClass(value.getClass());
 					}
 
 					@Override
 					public Object getLanguageType() {
 						return value == null ? null : value.getClass();
 					}
 
 					@Override
 					protected void setValueFinal(Object iObject, Object iValue) throws IllegalAccessException, InvocationTargetException {
 					}
 
 					@Override
 					public boolean isArray() {
 						return false;
 					}
 
 				};
 				sf.setValue(null, element);
 				sf.setFeature(ViewFieldFeatures.LABEL, "");
 				createFormComponent(sf, "form://" + field.getName(), iForm, element);
 			}
 		}
 	}
 
 	private static void createExpandedFormComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 
 		SchemaClassDefinition embeddedSchemaClassDef = field.getType();
 		final Boolean useRuntimeType = (Boolean) field.getFeature(CoreFieldFeatures.USE_RUNTIME_TYPE);
 		// Check for useRuntimeType
 		if (useRuntimeType == null || useRuntimeType.equals(Boolean.FALSE)) {
 			if (Object.class.equals(field.getLanguageType())) {
 				log.error("Cannot expand field " + field + " try to use the use runtimeType annotation.");
 			} else {
 				newExpandedForm(field, iForm, embeddedSchemaClassDef);
 			}
 
 		} else {
 
 			if (SchemaHelper.getFieldValue(field, iForm.getContent()) == null) {
 				log.warn("Cannot expand use runtime type because the content object is null, maybe it wasn't inizialized");
 			} else {
 				final SchemaManager schemaManager = Roma.schema();
 				embeddedSchemaClassDef = schemaManager.getSchemaClass(SchemaHelper.getFieldValue(field, iForm.getContent()).getClass().getSimpleName());
 				newExpandedForm(field, iForm, embeddedSchemaClassDef);
 			}
 		}
 	}
 
 	private static void newExpandedForm(final SchemaField field, final HtmlViewContentForm iForm, final SchemaClassDefinition embeddedSchemaClassDef,
 			final Object fieldValue) {
 		SchemaObject schemaObj = null;
 		if (embeddedSchemaClassDef instanceof SchemaObject) {
 			schemaObj = (SchemaObject) embeddedSchemaClassDef;
 		} else {
 			schemaObj = new SchemaObject((SchemaClass) embeddedSchemaClassDef);
 		}
 		final HtmlViewConfigurableExpandedEntityForm newComponent = new HtmlViewConfigurableExpandedEntityForm(iForm, schemaObj, field, fieldValue,
 				iForm.getScreenAreaObject(), null, null, null);
 		newComponent.setContent(fieldValue);
 		iForm.addExpandedChild(newComponent);
 		// iForm.addChild(field.getName(), null, newComponent);
 	}
 
 	private static void newExpandedForm(final SchemaField field, final HtmlViewContentForm iForm, final SchemaClassDefinition embeddedSchemaClassDef) {
 
 		Object temp = ViewHelper.getWrappedContent(iForm.getContent());
 
 		Object fieldValue = field.getValue(temp);
 		newExpandedForm(field, iForm, embeddedSchemaClassDef, fieldValue);
 	}
 
 	public static void createActionComponent(final SchemaAction action, final HtmlViewContentForm iForm) {
 		if (visible(action)) {
 			final String featureLayout = (String) action.getFeature(ViewActionFeatures.LAYOUT);
 			final HtmlViewFormArea areaForRendering = (HtmlViewFormArea) iForm.searchAreaForRendering(featureLayout, action);
 			if (areaForRendering == null) {
 				log.warn("[HtmlViewAspect]: no area found for the rendering of " + action + " in form iForm");
 				return;
 			}
 			final HtmlViewActionComponent newComponent = new HtmlViewActionComponent(iForm, iForm.getScreenAreaObject(), action, iForm.getContent());
 			areaForRendering.addComponent(newComponent);
 			iForm.addChild(action.getName(), areaForRendering, newComponent);
 		}
 	}
 
 	private static boolean visible(SchemaAction iElement) {
 		Boolean feature = iElement.getFeature(ViewActionFeatures.VISIBLE);
 		if (feature == null) {
 			return true;
 		}
 		return feature;
 	}
 
 	private static void createContentComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		final HtmlViewContentComponentImpl component = new HtmlViewContentComponentImpl(iForm, field, SchemaHelper.getFieldValue(field, iForm.getContent()),
 				iForm.getScreenAreaObject());
 		if (areaForRendering instanceof HtmlViewFormArea) {
 			if (iForm.getFieldComponent(field.getName()) == null) {
 				((HtmlViewFormArea) areaForRendering).addComponent(component);
 			}
 		} else {
 			final HtmlViewScreenArea screenArea = (HtmlViewScreenArea) areaForRendering;
 			try {
 				final SchemaClass embeddedSchemaClass = field.getClassInfo();
 				final HtmlViewConfigurableEntityForm newComponent = new HtmlViewMenuForm(iForm, new SchemaObject(embeddedSchemaClass), field,
 						iForm.getScreenAreaObject(), null, null, null);
 				newComponent.setContent(SchemaHelper.getFieldValue(field, iForm.getContent()));
 				screenArea.bindForm(newComponent);
 			} catch (final Exception e) {
 				// TODO Handle he exception
 				log.error("Cannot retrieve the container component", e);
 			}
 		}
 		iForm.addChild(field.getName(), areaForRendering, component);
 
 	}
 
 	private static void createHiddenContentComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		final HtmlViewInvisibleContentComponent component = new HtmlViewInvisibleContentComponent(iForm, field, SchemaHelper.getFieldValue(field,
 				iForm.getContent()), iForm.getScreenAreaObject());
 		if (areaForRendering instanceof HtmlViewFormArea) {
 			if (iForm.getFieldComponent(field.getName()) == null) {
 				((HtmlViewFormArea) areaForRendering).addComponent(component);
 			}
 		} else {
 			final HtmlViewScreenArea screenArea = (HtmlViewScreenArea) areaForRendering;
 			try {
 				final SchemaClass embeddedSchemaClass = field.getClassInfo();
 				final HtmlViewConfigurableEntityForm newComponent = new HtmlViewMenuForm(iForm, new SchemaObject(embeddedSchemaClass), field,
 						iForm.getScreenAreaObject(), null, null, null);
 				screenArea.bindForm(newComponent);
 			} catch (final Exception e) {
 				// TODO Handle he exception
 				log.error("Cannot retrieve the container component", e);
 			}
 		}
 		iForm.addChild(field.getName(), areaForRendering, component);
 
 	}
 
 	/**
 	 * Create a normal form component
 	 * 
 	 * @param field
 	 * @param featureLayout
 	 * @param iForm
 	 */
 	private static void createFormComponent(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm, final Object fieldValue) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		String className = ((Class<?>) field.getLanguageType()).getSimpleName();
 		if (Boolean.TRUE.equals(field.getFeature(CoreFieldFeatures.USE_RUNTIME_TYPE)) && fieldValue != null) {
 			className = fieldValue.getClass().getSimpleName();
 		}
 		final SchemaClass embeddedSchemaClass = Roma.schema().getSchemaClass(className);
 		SchemaObject newSchemaObject = Roma.getSchemaObject(fieldValue);
 		if (newSchemaObject == null) {
 			newSchemaObject = new SchemaObject(embeddedSchemaClass);
 		}
 		final HtmlViewConfigurableEntityForm newComponent = new HtmlViewConfigurableEntityForm(iForm, newSchemaObject, field, iForm.getScreenAreaObject(), null,
 				null, null);
 		newComponent.setContent(fieldValue);
 		if (areaForRendering instanceof HtmlViewFormArea) {
 			if (iForm.getFieldComponent(field.getName()) == null) {
 				((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 			}
 		} else {
 			final HtmlViewScreenArea screenArea = (HtmlViewScreenArea) areaForRendering;
 			try {
 				screenArea.bindForm(newComponent);
 			} catch (final Exception e) {
 				// TODO remove log.error("Cannot retrieve the container component", e);
 			}
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 	}
 
 	private static void createFormComponentForDisplayReplacement(final SchemaField field, final String featureLayout, final HtmlViewContentForm iForm,
 			final Object fieldValue) {
 		final AreaComponent areaForRendering = iForm.searchAreaForRendering(featureLayout, field);
 		if (areaForRendering == null) {
 			return;
 		}
 		String className = fieldValue.getClass().getSimpleName();
 		final SchemaClass embeddedSchemaClass = Roma.schema().getSchemaClass(className);
 		SchemaObject newSchemaObject = Roma.getSchemaObject(fieldValue);
 		if (newSchemaObject == null) {
 			newSchemaObject = new SchemaObject(embeddedSchemaClass);
 		}
 		final HtmlViewConfigurableEntityForm newComponent = new HtmlViewConfigurableEntityForm(iForm, newSchemaObject, field, iForm.getScreenAreaObject(), null,
 				null, null);
 		newComponent.setContent(fieldValue);
 		if (areaForRendering instanceof HtmlViewFormArea) {
 			if (iForm.getFieldComponent(field.getName()) == null) {
 				((HtmlViewFormArea) areaForRendering).addComponent(newComponent);
 			}
 		} else {
 			final HtmlViewScreenArea screenArea = (HtmlViewScreenArea) areaForRendering;
 			try {
 				screenArea.bindForm(newComponent);
 			} catch (final Exception e) {
 				// TODO remove log.error("Cannot retrieve the container component", e);
 			}
 		}
 		iForm.addChild(field.getName(), areaForRendering, newComponent);
 	}
 }
