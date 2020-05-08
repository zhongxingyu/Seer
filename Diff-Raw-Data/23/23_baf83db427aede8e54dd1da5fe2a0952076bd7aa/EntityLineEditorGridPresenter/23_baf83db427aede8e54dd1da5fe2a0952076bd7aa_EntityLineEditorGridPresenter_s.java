 package org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.grid;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageComponent;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFactory;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.builder.VaadinPageDataBuilder;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.ext.mvp.IConfigurablePresenter;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.ext.mvp.lineeditor.section.ILineEditorSectionContentPresenter;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.editor.multilevel.MultiLevelEditorEventBus;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.grid.view.EntityLineEditorGridView;
 import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.mvp.lineeditor.section.grid.view.IEntityLineEditorGridView;
 import org.flowframe.kernel.common.mdm.domain.BaseEntity;
 import org.flowframe.kernel.common.mdm.domain.metamodel.EntityType;
 import org.flowframe.kernel.common.mdm.domain.metamodel.EntityTypeAttribute;
 import org.flowframe.kernel.common.mdm.domain.metamodel.PluralAttribute;
 import org.flowframe.kernel.jpa.container.services.IDAOProvider;
 import org.flowframe.kernel.jpa.container.services.IEntityContainerProvider;
 import org.flowframe.kernel.metamodel.dao.services.IEntityTypeDAOService;
 import org.flowframe.ui.component.domain.table.GridComponent;
import org.flowframe.ui.services.contribution.IMainApplication;
 import org.flowframe.ui.services.factory.IComponentFactory;
 import org.flowframe.ui.vaadin.editors.entity.vaadin.ext.table.EntityEditorGrid.IEditListener;
 import org.flowframe.ui.vaadin.editors.entity.vaadin.ext.table.EntityEditorGrid.ISelectListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vaadin.mvp.eventbus.EventBusManager;
 import org.vaadin.mvp.presenter.BasePresenter;
 import org.vaadin.mvp.presenter.annotation.Presenter;
 
 import com.vaadin.Application;
 import com.vaadin.addon.jpacontainer.JPAContainerItem;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.ui.Component;
 
 @Presenter(view = EntityLineEditorGridView.class)
 public class EntityLineEditorGridPresenter extends BasePresenter<IEntityLineEditorGridView, EntityLineEditorGridEventBus> implements
 		IEditListener, ILineEditorSectionContentPresenter, IConfigurablePresenter, ISelectListener {
 	protected Logger logger = LoggerFactory.getLogger(this.getClass());
 	private boolean initialized = false;
 	private BeanItemContainer<?> entityContainer;
 	private IEntityContainerProvider mainApplication;
 	private GridComponent tableComponent;
 	private IEntityTypeDAOService entityTypeDao;
 	private Class<?> entityClass;
 	private PluralAttribute gridAttribute;
 	private IPageFactory factory;
 	private Object bean;
 	private Item selectedItem;
 	private EventBusManager sectionEventBusManager;
 	private IDAOProvider daoProvider;
 	private VaadinPageDataBuilder pageDataBuilder;
 
 	private void initialize() {
 		String[] visibleFieldNames = this.tableComponent.getDataSource().getVisibleFieldNames().toArray(new String[0]);
 
 		this.getView().init();
 		this.getView().setContainerDataSource(this.entityContainer);
 		this.getView().setVisibleColumns(visibleFieldNames);
 		this.getView().addEditListener(this);
 		this.getView().addSelectListener(this);
 
 		// -- Done
 		this.setInitialized(true);
 	}
 
 	@Override
 	public void bind() {
 		// initialize();
 	}
 
 	@Override
 	public void onSetItemDataSource(Item item, Container... container) throws Exception {
 		if (item instanceof BeanItem) {
 			this.bean = ((BeanItem<?>) item).getBean();
 			updateQueryFilter(provideGridAttribute(this.entityClass, this.bean));
 			if (!isInitialized()) {
 				initialize();
 			}
 
 			if (this.tableComponent.getRecordEditor() != null) {
 				this.sectionEventBusManager.fireAnonymousEvent("enableCreate");
 				this.sectionEventBusManager.fireAnonymousEvent("enablePrint");
 			}
 		} else {
 			this.getView().setContainerDataSource(null);
 		}
 	}
 
 	private PluralAttribute provideGridAttribute(Class<?> propertyType, Object bean) throws Exception {
 		if (this.gridAttribute == null) {
 			EntityType beanEntityType = this.entityTypeDao.provide(bean.getClass()), attributeType = null;
 			Set<EntityTypeAttribute> beanAttributes = beanEntityType.getAllDeclaredAttributes();
 			for (EntityTypeAttribute attribute : beanAttributes) {
 				if (attribute.getAttribute() instanceof PluralAttribute) {
 					attributeType = attribute.getAttribute().getEntityType();
 					if (attributeType != null) {
 						if (propertyType.isAssignableFrom(attributeType.getJavaType())) {
 							this.gridAttribute = (PluralAttribute) attribute.getAttribute();
 							return this.gridAttribute;
 						}
 					}
 				}
 			}
 		}
 		return this.gridAttribute;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void updateQueryFilter(PluralAttribute attribute) throws Exception {
 		if (attribute != null) {
 			this.entityContainer.removeAllItems();
 			Method[] methods = this.bean.getClass().getMethods();
 			for (Method method : methods) {
 				if (method.getName().toLowerCase().contains("get")
 						&& method.getName().toLowerCase().contains(attribute.getName().toLowerCase())) {
 					if (Collection.class.isAssignableFrom(method.getReturnType())) {
 						Collection<?> result = (Collection<?>) method.invoke(this.bean);
 						for (Object resultItem : result) {
 							if (this.entityClass.isAssignableFrom(resultItem.getClass())) {
 								((BeanItemContainer) this.entityContainer).addBean(resultItem);
 							}
 						}
 						return;
 					}
 				}
 			}
 		} else {
 			throw new Exception("The attribute required of this table does not exist in " + this.bean.getClass().getName());
 		}
 	}
 
 	public boolean isInitialized() {
 		return initialized;
 	}
 
 	public void setInitialized(boolean initialized) {
 		this.initialized = initialized;
 	}
 
 	@Override
 	public void onConfigure(Map<String, Object> params) {
 		this.tableComponent = (GridComponent) params.get(IComponentFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL);
 		this.mainApplication = (IEntityContainerProvider) params.get(IPageComponent.ENTITY_CONTAINER_PROVIDER);
 		this.factory = (IPageFactory) params.get(IComponentFactory.VAADIN_COMPONENT_FACTORY);
 		this.daoProvider = (IDAOProvider) params.get(IPageComponent.DAO_PROVIDER);
 		this.entityTypeDao = (IEntityTypeDAOService) params.get(IPageComponent.ENTITY_TYPE_DAO_SERVICE);
 
 		try {
 			this.entityClass = this.tableComponent.getDataSource().getEntityType().getJavaType();
 			this.entityContainer = (BeanItemContainer<?>) mainApplication.createBeanContainer(this.entityClass);
 			Set<String> nestedFieldNames = this.tableComponent.getDataSource().getNestedFieldNames();
 			for (String nestedFieldName : nestedFieldNames) {
 				this.entityContainer.addNestedContainerProperty(nestedFieldName);
 			}
 		} catch (UnsupportedOperationException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		this.pageDataBuilder = new VaadinPageDataBuilder();
 	}
 
 	public void onDelete(Item item) throws Exception {
 		this.getView().deleteItem(item);
 	}
 
 	@Override
 	public void onEdit(Item item) {
 		MultiLevelEditorEventBus ownerEditorEventBus = this.factory.getPresenterFactory().getEventBusManager()
 				.getEventBus(MultiLevelEditorEventBus.class);
 		if (ownerEditorEventBus != null) {
 			ownerEditorEventBus.renderEditor(this.tableComponent.getRecordEditor(), item, this.entityContainer);
 		} else {
 			throw new UnsupportedOperationException("Cannot edit an sub-entity with outside a multi-level editor.");
 		}
 	}
 	
 	/**
 	 * This event is fired when the reporting button is clicked.
 	 * 
 	 * @param item
 	 */
 	public void onReport(Item item) {
 		assert (item instanceof JPAContainerItem || item instanceof BeanItem) : "The item must be a JPAContainerItem or BeanItem";
 		MultiLevelEditorEventBus ownerEditorEventBus = this.factory.getPresenterFactory().getEventBusManager()
 				.getEventBus(MultiLevelEditorEventBus.class);
 		if (ownerEditorEventBus != null) {
 			Object entity = (item instanceof JPAContainerItem) ? ((JPAContainerItem<?>) item).getEntity() : ((BeanItem<?>) item).getBean();
 			assert (entity instanceof BaseEntity) : "The item's bean must be of type BaseEntity";
 			String url = getReportUrlForEntity((BaseEntity) entity);
 			assert (url != null) : "The URL could not be generated for this item";
 			ownerEditorEventBus.viewDocument(url, "Label (" + ((BaseEntity) entity).getName() + ")");
 		} else {
 			throw new UnsupportedOperationException("Cannot edit an sub-entity with outside a multi-level editor.");
 		}
 	}
 
 	@Override
 	public void onSelect(Item item) {
 		this.selectedItem = item;
 		if (this.tableComponent.getRecordEditor() != null) {
 			this.sectionEventBusManager.fireAnonymousEvent("enableEdit");
 			this.sectionEventBusManager.fireAnonymousEvent("enableDelete");
 		}
 	}
 
 	public void onCreate() throws Exception {
 		if (this.tableComponent.getRecordEditor() != null) {
 			Object newInstance = this.pageDataBuilder.saveInstance(this.entityClass.newInstance(), this.daoProvider, this.bean);
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			Item item = ((BeanItemContainer) this.entityContainer).addBean(newInstance);
 			MultiLevelEditorEventBus eventBus = this.factory.getPresenterFactory().getEventBusManager()
 					.getEventBus(MultiLevelEditorEventBus.class);
 			eventBus.renderEditor(this.tableComponent.getRecordEditor(), item, this.entityContainer);
 		} else {
 			throw new UnsupportedOperationException("Cannot view a report outside a multi-level editor.");
 		}
 	}
 
 	public void onEdit() {
 		if (this.selectedItem != null) {
 			onEdit(this.selectedItem);
 		}
 	}
 	
 	public void onReport() {
 		if (this.selectedItem != null) {
 			onReport(this.selectedItem);
 		}
 	}
 
 	public void onDelete() throws Exception {
 		if (this.selectedItem != null) {
 			onDelete(this.selectedItem);
 			this.sectionEventBusManager.fireAnonymousEvent("disableEdit");
 			this.sectionEventBusManager.fireAnonymousEvent("disableDelete");
 		}
 	}
 
 	public void onPrint() {
 		// TODO implement printing
 		throw new UnsupportedOperationException("Printing has not yet been implemented.");
 	}
 
 	@Override
 	public void subscribe(EventBusManager eventBusManager) {
 		this.sectionEventBusManager = eventBusManager;
 		this.sectionEventBusManager.register(EntityLineEditorGridEventBus.class, this);
 
 		// Ensure that the header is setup correctly
 		if (this.tableComponent.getRecordEditor() == null) {
 			this.sectionEventBusManager.fireAnonymousEvent("disableCreate");
 			this.sectionEventBusManager.fireAnonymousEvent("disableEdit");
 		}
 	}
 	
 	/**
 	 * Gets the full, browser compatible url of the report for the provided entity.
 	 * FIXME THIS CURRENTLY ONLY WORKS FOR STOCK ITEMS.
 	 * 
 	 * @param entity to get the report url for
 	 * @return report url for entity
 	 */
 	private String getReportUrlForEntity(BaseEntity entity) {
 		Application app = ((Component) this.getView()).getApplication();
 		if (app instanceof IMainApplication) {
 //			String baseUrl = ((IMainApplication) app).getReportingUrl();
 //			if (entity instanceof StockItem) {
 //				return this.daoProvider.provideByDAOClass(IStockItemDAOService.class).getStockItemLabelUrl((StockItem) entity, baseUrl);
 //			}
 		}
 		
 		return null;
 	}
 }
