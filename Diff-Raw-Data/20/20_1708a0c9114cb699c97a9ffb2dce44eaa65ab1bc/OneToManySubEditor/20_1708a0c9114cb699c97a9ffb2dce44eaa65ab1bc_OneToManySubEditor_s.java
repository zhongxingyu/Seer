 package w.wexpense.vaadin.view;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import w.wexpense.model.DBable;
 import w.wexpense.persistence.PersistenceUtils;
 import w.wexpense.vaadin.WexJPAContainerFactory;
 import w.wexpense.vaadin.fieldfactory.RelationalFieldFactory;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.Action;
 import com.vaadin.ui.TableFieldFactory;
 
 public class OneToManySubEditor<C extends DBable, P extends DBable> extends AbstractView<C> {
 
 	private static final long serialVersionUID = 1698018766277514987L;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(OneToManySubEditor.class);
 
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	@Autowired
 	protected WexJPAContainerFactory jpaContainerFactory;
 	
 	private Class<P> parentType;
 	private String parentPropertyId;
 	protected P parentEntity;
 	
 	private String childPropertyId;
 	private BeanItemContainer<C> childContainer;
 	private TableFieldFactory fieldFactory;
 	
 	private boolean editable = true;
 	
 	private Set<C> toDelete = new HashSet<C>();
 	
 	public OneToManySubEditor(Class<C> childType, String parentPropertyId) {
 		super(childType);
 		this.parentPropertyId = parentPropertyId;
 		
 		super.entitySelectedActions = new Action[] { addAction, removeAction };
 		super.noEntitySelectedActions = new Action[] { addAction };
 	}
 
 	public void setInstance(P parentEntity, JPAContainer<P> parentJpaContainer) {
 		this.parentEntity = parentEntity;
 		this.parentType = parentJpaContainer.getEntityClass(); 						
 		this.childPropertyId = PersistenceUtils.getMappedByProperty(parentType, parentPropertyId);
 
 		JPAContainer<C> childJpaContainer = jpaContainerFactory.getJPAContainerFrom(parentJpaContainer.getEntityProvider(), this.entityClass);			
 		this.fieldFactory = getTableFieldFactory(childJpaContainer, jpaContainerFactory);
 		
 //		if (parentEntity.isNew()) {
 //			BeanContainer<String, C> bContainer = new BeanContainer<String, C>(this.entityClass);
 //			bContainer.setBeanIdProperty("uid");
 //			
 //			childContainer = bContainer;
 //		} else {
 //			Filter filter = new Compare.Equal(this.childPropertyId, parentEntity);
 //			childJpaContainer.addContainerFilter(filter);
 //
 //			childContainer = childJpaContainer;
 //		}
 		
 		buildTable();
 		
 		Collection<C> children = (Collection<C>) new BeanItem<P>(parentEntity).getItemProperty(parentPropertyId).getValue();
		childContainer.addAll(children);
 		
 		addComponent(table);
 	}
 
 //	protected void buildLayout() {
 //		CssLayout vl = new CssLayout();
 //		
 //		// build the table
 //		buildTable();
 //		vl.addComponent(table);
 //	
 //		addComponent(vl);
 //	}
 	
 	protected TableFieldFactory getTableFieldFactory(JPAContainer<C> childJpaContainer, WexJPAContainerFactory jpaContainerFactory) {
 		return new RelationalFieldFactory<C>(this.propertyConfiguror, childJpaContainer, jpaContainerFactory);
 	}
 	
 	@Override
 	protected void initTable() {
 		childContainer = new BeanItemContainer<C>(entityClass);
 		table = new WexTable(childContainer, propertyConfiguror);
 	}
 	
 	@Override
 	protected void buildTable() {
 		super.buildTable();
 		table.setPageLength(5);			
 		table.setEditable(editable);
 		table.setTableFieldFactory(this.fieldFactory);		
 	}
 
 	@Override
 	public void addEntity() {
 		try {
 			C newInstance = entityClass.newInstance();
 			BeanItem<C> beanItem = new BeanItem<C>(newInstance);
 			beanItem.getItemProperty(this.childPropertyId).setValue(parentEntity);
 			childContainer.addBean(newInstance);
 			
 //			Object o=null;
 //			if (childContainer instanceof JPAContainer) {
 //				o = ((JPAContainer) childContainer).addEntity(newInstance);
 //			} else {
 //				o = ((BeanContainer) childContainer).addItem(((DBable) newInstance).getUid(), newInstance);
 //			}
 
 		} catch (Exception e) {
 			LOGGER.warn("Could not instantiate detail instance " + this.entityClass.getName(), e);
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void removeEntity(Object target) {
		Item item = childContainer.getItem(table.getValue());
 		item.getItemProperty(this.childPropertyId).setValue(null);
		childContainer.removeItem(table.getValue());
 	}
 	
 	@Override
 	public void deleteEntity(Object target) {
 		Item item = childContainer.getItem(table.getValue());
 		C c = (C) table.getValue();
 		if (!c.isNew()) {
 			toDelete.add(c);
 		}
 		childContainer.removeItem(table.getValue());
 //		Item item = childContainer.getItem(table.getValue());
 //		item.getItemProperty(this.childPropertyId).setValue(null);
 //		if (childContainer instanceof JPAContainer) {
 //			((JPAContainer) childContainer).removeItem(table.getValue());
 //		} else {
 //			((BeanContainer) childContainer).removeItem(table.getValue());
 //		}
 	}
 
 //	public void insert(P parent, EntityManager em) {
 //		BeanContainer<String, C> bContainer = (BeanContainer<String, C>) childContainer;			
 //		Collection<String> itemIds = bContainer.getItemIds();
 //		
 //		LOGGER.debug("Inserting {} children", itemIds.size());
 //		
 //		for (Object object : itemIds) {
 //			BeanItem<C> childBean =  bContainer.getItem(object);
 //			childBean.getItemProperty(this.childPropertyId).setValue(parent);
 //			em.persist(childBean.getBean());				 
 //		}
 //	}
 //	
 //	public void update(P parent, EntityManager em) {
 //		JPAContainer<C> jpaContainer = (JPAContainer<C>) childContainer;
 //		jpaContainer.commit();
 //	}
 
 	public void save(P parent, EntityManager em) {
 		LOGGER.debug("Saving {} children of ", parent);
 		
 		for (Object object : childContainer.getItemIds()) {
 			BeanItem<C> childBean =  childContainer.getItem(object);
 			childBean.getItemProperty(this.childPropertyId).setValue(parent);
 			C child = childBean.getBean();
 			if (child.isNew()) {
 				em.persist(child);				 
 			} else {
 				em.merge(child);
 			}
 		}
 		for(C child: toDelete) {
 			em.remove(em.merge(child));
 		}
 	}
 	
 	public boolean isEditable() {
 		return editable;
 	}
 
 	public void setEditable(boolean editable) {
 		this.editable = editable;
 	}
 
 	public String getChildPropertyId() {
 		return childPropertyId;
 	}
 
 	public Container getChildContainer() {
 		return childContainer;
 	}
 }
