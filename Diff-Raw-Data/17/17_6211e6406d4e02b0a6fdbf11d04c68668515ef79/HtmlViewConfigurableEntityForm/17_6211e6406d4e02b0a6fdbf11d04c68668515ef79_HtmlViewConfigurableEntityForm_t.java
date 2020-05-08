 package org.romaframework.aspect.view.html.component;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 
 import org.romaframework.aspect.session.SessionInfo;
 import org.romaframework.aspect.validation.MultiValidationException;
 import org.romaframework.aspect.validation.ValidationException;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.ViewHelper;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.form.ViewComponent;
 import org.romaframework.aspect.view.html.HtmlViewAspectHelper;
 import org.romaframework.aspect.view.html.area.HtmlViewFormArea;
 import org.romaframework.aspect.view.html.area.HtmlViewFormAreaInstance;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.area.component.ChildrenMap;
 import org.romaframework.aspect.view.html.component.composed.list.HtmlViewCollectionComposedComponent;
 import org.romaframework.aspect.view.html.form.helper.FormUtils;
 import org.romaframework.aspect.view.html.screen.HtmlViewScreen;
 import org.romaframework.aspect.view.html.transformer.freemarker.TableDriver;
 import org.romaframework.aspect.view.screen.AbstractConfigurableScreenFactory;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 import org.romaframework.core.schema.SchemaObject;
 import org.romaframework.core.schema.xmlannotations.XmlFormAreaAnnotation;
 
 public class HtmlViewConfigurableEntityForm extends HtmlViewAbstractContentComponent implements HtmlViewContentForm {
 
 	private List<HtmlViewConfigurableExpandedEntityForm>	expandedChildren	= new ArrayList<HtmlViewConfigurableExpandedEntityForm>();
 
 	private static final HashSet<Integer>									EMPTY_INDEXES			= new HashSet<Integer>();
 
 	protected HtmlViewFormArea														rootArea;
 
 	protected SchemaObject																schemaObject;
 
 	protected Integer																			rowElementIndex;
 	protected Integer																			colElementIndex;
 	protected String																			label;
 
 	protected ChildrenMap																	childrenMap				= new ChildrenMap();
 
 	public HtmlViewConfigurableEntityForm(final HtmlViewContentComponent htmlViewConfigurableEntityForm,
 			final SchemaObject iSchemaObject, final SchemaField field, final HtmlViewScreenArea iScreenArea, Integer rowIndex,
 			Integer colIndex, String label) {
 		super(htmlViewConfigurableEntityForm, field, null, iScreenArea);
 		// Create the pojo form association
 		schemaObject = iSchemaObject;
 		this.rowElementIndex = rowIndex;
 		this.label = label;
 		this.colElementIndex = colIndex;
 		configureArea(iSchemaObject, iScreenArea);
 
 	}
 
 	private void configureArea(final SchemaObject iSchemaObject, final HtmlViewScreenArea screenArea) {
 		final XmlFormAreaAnnotation iAreaTag = AbstractConfigurableScreenFactory.getScreenConfiguration(iSchemaObject.getSchemaClass());
 		rootArea = new HtmlViewFormAreaInstance(this, null, iAreaTag, screenArea);
 	}
 
 	/**
 	 * Create the components for the pojo, it create and place a component for every field and every action of pojo
 	 * 
 	 * @param iContent
 	 *          The object container in the form
 	 * @param iRootArea
 	 *          The
 	 * @param iSchemaInstance
 	 */
 	public void placeComponents() {
 		clearComponents();
 		setDirty(true);
 		clearAreas();
 		if (content == null)
 			return;
 		// Getting the iterators for the field and the action
 		final Iterator<SchemaField> fieldIterator = schemaObject.getFieldIterator();
 		final Iterator<SchemaAction> actionIterator = schemaObject.getActionIterator();
 
 		// Add the components for the fields
 		while (fieldIterator.hasNext()) {
 			final SchemaField field = fieldIterator.next();
			if (Boolean.TRUE.equals(field.getFeature(ViewFieldFeatures.VISIBLE))) {
 				FormUtils.createFieldComponent(field, this);
 			}
 		}
 		while (actionIterator.hasNext()) {
 			final SchemaAction action = actionIterator.next();
 			FormUtils.createActionComponent(action, this);
 		}
 	}
 
 	@Override
 	public void clearComponents() {
 		super.clearComponents();
 		if (rootArea != null) {
 			rootArea.clear();
 		}
 		if (expandedChildren == null) {
 			return;
 		}
 		for (HtmlViewGenericComponent component : expandedChildren) {
 			if (component.getContent() != null) {
 				ViewHelper.invokeOnDispose(component.getContent());
 				component.clearComponents();
 				if (Roma.session().getActiveSystemSession() != null)
 					HtmlViewAspectHelper.getHtmlViewSession().removeRenderableBinding((HtmlViewRenderable) component);
 				Roma.aspect(ViewAspect.class).removeObjectFormAssociation(component.getContent(), null);
 			}
 		}
 		expandedChildren.clear();
 	}
 
 	public HtmlViewFormArea getAreaForComponentPlacement() {
 		return rootArea;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ViewComponent#close()
 	 */
 	public void close() {
 		// Roma.aspect(FlowAspect.class).back();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ViewComponent#render(org.romaframework.aspect.view.form.ViewComponent)
 	 */
 	public void render(final ViewComponent formToRender) {
 		// It makes nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ContentForm#activate()
 	 */
 	public void activate() {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ContentForm#isFirstToOpenPopup(java.lang.Object)
 	 */
 	public boolean isFirstToOpenPopup(final Object object) {
 		final HtmlViewScreen screen = (HtmlViewScreen) Roma.aspect(ViewAspect.class).getScreen();
 		return screen.isFirstToOpenPopup(object);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ContentForm#passivate()
 	 */
 	public void passivate() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.form.ContentForm#validateValues()
 	 */
 	public void validateValues() throws ValidationException, MultiValidationException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @return the rootArea
 	 */
 	public HtmlViewFormArea getRootArea() {
 		return rootArea;
 	}
 
 	public ViewComponent getChildComponent(final String childName) {
 		return childrenMap.getChild(childName);
 	}
 
 	public void addChild(final String fieldName, final AreaComponent iAreaComponent, final ViewComponent iComponent) {
 		childrenMap.addChild(fieldName, iAreaComponent, (HtmlViewGenericComponent) iComponent);
 		setDirty(true);
 	}
 
 	public Collection<HtmlViewGenericComponent> getChildren() {
 		return childrenMap.getChildren();
 	}
 
 	public Collection<HtmlViewGenericComponent> getChildrenFilled() {
 		if (getContainerComponent() instanceof HtmlViewCollectionComposedComponent) {
 			List<HtmlViewGenericComponent> result = new ArrayList<HtmlViewGenericComponent>();
 			TableDriver tableDriver = ((HtmlViewCollectionComposedComponent) getContainerComponent()).getTableDriver();
 
 			for (SchemaClassElement element : tableDriver.getExpandedElements()) {
 				if (element instanceof SchemaField) {
 					result.add(findField(element.getName()));
 				}
 				if (element instanceof SchemaAction) {
 					result.add(findAction(element.getName()));
 				}
 			}
 
 			return result;
 		}
 		return getChildren();
 	}
 
 	private HtmlViewGenericComponent findField(String fieldDriver) {
 		for (HtmlViewGenericComponent child : getChildren()) {
 			if ((child.getSchemaElement() instanceof SchemaField) && (child.getSchemaElement().getName().equals(fieldDriver)))
 				return child;
 		}
 		return new HtmlViewNullComponent();
 	}
 
 	private HtmlViewGenericComponent findAction(String actionDriver) {
 		for (HtmlViewGenericComponent child : getChildren()) {
 			if ((child.getSchemaElement() instanceof SchemaAction) && (child.getSchemaElement().getName().equals(actionDriver)))
 				return child;
 		}
 		return new HtmlViewNullComponent();
 	}
 
 	@Override
 	public String getHtmlId() {
 		StringBuilder builder = new StringBuilder();
 		if (containerComponent == null) {
 			builder.append(((HtmlViewRenderable) screenArea).getHtmlId()).append(SEPARATOR).append(schemaObject.getName());
 		} else {
 			if (getSchemaElement() != null) {
 				builder.append(((HtmlViewRenderable) containerComponent).getHtmlId()).append(SEPARATOR).append(getSchemaField().getName());
 			} else {
 				builder.append(((HtmlViewRenderable) containerComponent).getHtmlId());
 			}
 		}
 
 		if (rowElementIndex != null) {
 			builder.append(SEPARATOR).append(rowElementIndex);
 		}
 
 		if (colElementIndex != null) {
 			builder.append(SEPARATOR).append(colElementIndex);
 		}
 
 		return builder.toString();
 	}
 
 	public void setMetaDataSchema(final SchemaObject schemaObject) {
 		this.schemaObject = schemaObject;
 	}
 
 	public SchemaObject getSchemaInstance() {
 		return schemaObject;
 	}
 
 	@Override
 	public void render(OutputStream out) throws IOException {
 		try {
 			HtmlViewAspectHelper.renderByJsp(this, out);
 		} catch (final ServletException e) {
 			// TODO handle exception!!!
 			e.printStackTrace();
 		} catch (final IOException e) {
 			// TODO handle exception!!!
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setScreenArea(final HtmlViewScreenArea screenArea) {
 		this.screenArea = screenArea;
 		for (final ViewComponent component : childrenMap.getChildren()) {
 			final HtmlViewGenericComponent castedComponent = (HtmlViewGenericComponent) component;
 			castedComponent.setScreenArea(screenArea);
 		}
 		rootArea.setScreenArea(screenArea);
 	}
 
 	public void clearAreas() {
 		if (childrenMap != null)
 			childrenMap.clear();
 	}
 
 	public void clearChildren() {
 		if (childrenMap != null)
 			childrenMap.clear();
 	}
 
 	@Override
 	public HtmlViewContentComponent getFieldComponent(String name) {
 		if (name.contains(Utility.PACKAGE_SEPARATOR_STRING)) {
 			Object object = SchemaHelper.getFieldObject(getContent(), name);
 			HtmlViewContentForm childComponent = (HtmlViewContentForm) Roma.aspect(ViewAspect.class).getFormByObject(object);
 			name = Utility.getResourceNamesLastSeparator(name, Utility.PACKAGE_SEPARATOR_STRING, "")[1];
 			return childComponent.getFieldComponent(name);
 		}
 		try {
 			return (HtmlViewContentComponent) childrenMap.getChild(name);
 		} catch (final ClassCastException e) {
 			log.error("The requested field " + name + " could not be a field.");
 		}
 		return null;
 	}
 
 	public void removeFieldComponent(final String fieldName) {
 		childrenMap.remove(fieldName);
 	}
 
 	@Override
 	public boolean validate() {
 		boolean result = true;
 		Object realContent = ViewHelper.getWrappedContent(content);
 
 		MultiValidationException mvex = Roma.validation().validateAndCollectExceptions(realContent);
 		final Iterator<ValidationException> exceptions = mvex.getDetailIterator();
 		if (exceptions != null) {
 			while (exceptions.hasNext()) {
 				final ValidationException exception = exceptions.next();
 				setInvalidField(exception.getObject(), exception);
 			}
 			result = false;
 		}
 
 		return result;
 	}
 
 	private void setInvalidField(Object pojo, final ValidationException vex) {
 		final String fieldName = vex.getFieldName();
 		final ViewComponent child = (ViewComponent) vex.getComponent();
 		if (child != null && child instanceof HtmlViewAbstractContentComponent) {
 			((HtmlViewAbstractContentComponent) child).setValid(false);
 			((HtmlViewAbstractContentComponent) child).setValidationMessage(vex.getLocalizedMessage());
 		}
 
 		if (pojo != null) {
 			Roma.fieldChanged(pojo, fieldName);
 		}
 	}
 
 	public void resetValidation() {
 		setValid(true);
 		final Collection<HtmlViewGenericComponent> children = getChildren();
 		if (children != null) {
 			for (final ViewComponent child : children) {
 				if (child instanceof HtmlViewRenderable) {
 					((HtmlViewRenderable) child).resetValidation();
 				}
 			}
 		}
 	}
 
 	public void setContent(final Object content, final SessionInfo session) {
 		if (this.content != null) {
 			ViewHelper.invokeOnDispose(this.content);
 			Roma.aspect(ViewAspect.class).removeObjectFormAssociation(this.content, session);
 		}
 		this.content = content;
 
 		Class<?> typeClass = content == null ? null : SchemaHelper.getClassForJavaTypes(content.getClass().getSimpleName());
 
 		if (typeClass == null || Collection.class.isAssignableFrom(typeClass))
 			placeComponents();
 
 		// Create the pojo form association
 		Roma.aspect(ViewAspect.class).createObjectFormAssociation(this.content, this, session);
 		ViewHelper.invokeOnShow(content);
 	}
 
 	public SchemaObject getSchemaObject() {
 		return schemaObject;
 	}
 
 	@Override
 	public void setContent(final Object content) {
 		setContent(content, null);
 	}
 
 	public boolean hasLabel() {
 		if (ViewConstants.RENDER_MENU.equals(this.schemaObject.getFeature(ViewFieldFeatures.RENDER))) {
 			return false;
 		}
 
 		if ("".equals(this.getSchemaField().getFeature(ViewFieldFeatures.LABEL))) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public AreaComponent searchAreaForRendering(final String featureLayout, final SchemaClassElement iField) {
 		final HtmlViewFormArea rootArea = getAreaForComponentPlacement();
 		return HtmlViewAspectHelper.searchAreaForRendering(featureLayout, iField, rootArea);
 	}
 
 	public void renderContent() {
 		// make nothing
 
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public Set<Integer> selectedIndex() {
 		return EMPTY_INDEXES;
 	}
 
 	public void addExpandedChild(HtmlViewConfigurableExpandedEntityForm iChildForm) {
 		expandedChildren.add(iChildForm);
 	}
 
 }
