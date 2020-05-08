 package com.anasoft.os.daofusion.test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.test.annotation.ExpectedException;
 
 import com.anasoft.os.daofusion.AbstractHibernateEntityDao;
 import com.anasoft.os.daofusion.PersistentEntityDao;
 import com.anasoft.os.daofusion.criteria.FilterCriterion;
 import com.anasoft.os.daofusion.criteria.NestedPropertyCriteria;
 import com.anasoft.os.daofusion.criteria.PersistentEntityCriteria;
 import com.anasoft.os.daofusion.criteria.PropertyFilterCriterionProvider;
 import com.anasoft.os.daofusion.criteria.SortCriterion;
 import com.anasoft.os.daofusion.entity.MutablePersistentEntity;
 import com.anasoft.os.daofusion.entity.Persistable;
 import com.anasoft.os.daofusion.test.example.entity.Customer;
 import com.anasoft.os.daofusion.test.example.entity.Order;
 import com.anasoft.os.daofusion.test.example.entity.OrderItem;
 import com.anasoft.os.daofusion.test.example.entity.PromotedStockItem;
 import com.anasoft.os.daofusion.test.example.entity.StockItem;
 import com.anasoft.os.daofusion.test.example.enums.StockItemCategory;
 
 /**
  * Core integration test for {@link AbstractHibernateEntityDao}.
  * 
  * @see BaseHibernateCoreIntegrationTest
  * 
  * @author vojtech.szocs
  */
 public abstract class AbstractHibernateEntityDaoTest extends BaseHibernateCoreIntegrationTest {
 
     /**
      * @return Sample {@link PromotedStockItem} transient instance.
      */
     private PromotedStockItem getSampleStockItemTransient() {
         final StockItemCategory foodCategory = stockItemCategoryDao.get(STOCK_ITEM_CATEGORY_FOOD, StockItemCategory.class);
         
         final PromotedStockItem stockItemTransient = new PromotedStockItem();
         stockItemTransient.setName("Milk");
         stockItemTransient.setDescription("Moo moo!");
         stockItemTransient.setPrice(5);
         stockItemTransient.setCategory(foodCategory);
         stockItemTransient.setPromotionPrice(4);
         
         assertThat(stockItemTransient.getId(), nullValue());
         
         return stockItemTransient;
     }
     
     /**
      * @param instance {@link PromotedStockItem} instance to modify.
      */
     private void modifySampleStockItem(PromotedStockItem instance) {
         final StockItemCategory computerCategory = stockItemCategoryDao.get(STOCK_ITEM_CATEGORY_COMPUTERS, StockItemCategory.class);
         
         instance.setName("MacBook");
         instance.setDescription("Portable computer.");
         instance.setPrice(100);
         instance.setCategory(computerCategory);
         instance.setPromotionPrice(99);
     }
     
     /**
      * @return Sample {@link PromotedStockItem} transient instance
      * which violates database constraints.
      */
     private PromotedStockItem getSampleConstraintViolatingStockItemTransient() {
         final PromotedStockItem stockItemTransient = getSampleStockItemTransient();
         stockItemTransient.setName(null);
         
         return stockItemTransient;
     }
     
     /**
      * Verifies that properties of both {@link PromotedStockItem}
      * instances match (excluding the <tt>id</tt> field).
      * 
      * @param instanceOne First {@link PromotedStockItem} instance.
      * @param instanceTwo Second {@link PromotedStockItem} instance.
      */
     private void verifyStockItemPropertiesMatch(PromotedStockItem instanceOne, PromotedStockItem instanceTwo) {
         assertThat(instanceOne.getName(), equalTo(instanceTwo.getName()));
         assertThat(instanceOne.getDescription(), equalTo(instanceTwo.getDescription()));
         assertThat(instanceOne.getPrice(), equalTo(instanceTwo.getPrice()));
         assertThat(instanceOne.getCategory(), equalTo(instanceTwo.getCategory()));
         assertThat(instanceOne.getPromotionPrice(), equalTo(instanceTwo.getPromotionPrice()));
     }
     
     /**
      * Verifies the presence of {@link PromotedStockItem}
      * instances within the <tt>stockItemList</tt>, based
      * on their <tt>id</tt> values (instance order within
      * the <tt>stockItemList</tt> does not matter).
      * 
      * @param stockItemList List of {@link PromotedStockItem}
      * instances.
      * @param instanceIds Instance <tt>id</tt>
      * values, sorted in an ascending numerical order.
      * @param referenceInstances Reference
      * {@link PromotedStockItem} instances matching
      * the <tt>id</tt> values (can be <tt>null</tt>).
      */
     private void verifyStockItemsInList(List<PromotedStockItem> stockItemList, Long[] instanceIds, PromotedStockItem[] referenceInstances) {
     	for (PromotedStockItem stockItem : stockItemList) {
     		final int instanceIdIndex = Arrays.binarySearch(instanceIds, stockItem.getId());
     		assertThat(instanceIdIndex >= 0, equalTo(true));
     		
     		final Long referenceId = instanceIds[instanceIdIndex];
     		assertThat(stockItem.getId(), equalTo(referenceId));
     		
     		final PromotedStockItem referenceStockItem = referenceInstances != null ? referenceInstances[instanceIdIndex] : null;
     		if (referenceStockItem != null) {
     			verifyStockItemPropertiesMatch(stockItem, referenceStockItem);
     		}
     	}
     }
     
     /**
      * Verifies the presence of {@link MutablePersistentEntity}
      * instances within the <tt>entityList</tt>, based
      * on their <tt>id</tt> values.
      * 
      * @param entityList List of {@link MutablePersistentEntity}
      * instances.
      * @param instanceIds Instance <tt>id</tt>
      * values, sorted in an ascending numerical order.
      */
     private void verifyMutablePersistentEntitiesInList(List<? extends MutablePersistentEntity> entityList, Long[] instanceIds) {
     	for (MutablePersistentEntity entity : entityList) {
     		final int instanceIdIndex = Arrays.binarySearch(instanceIds, entity.getId());
     		assertThat(instanceIdIndex >= 0, equalTo(true));
     	}
     }
     
     /**
      * @return Modified version of sample {@link PromotedStockItem}
      * transient instance.
      */
     private PromotedStockItem getModifiedSampleStockItemTransient() {
         final PromotedStockItem stockItemTransientModified = getSampleStockItemTransient();
         modifySampleStockItem(stockItemTransientModified);
         
         return stockItemTransientModified;
     }
     
     /**
      * @return {@link PersistentEntityCriteria} for {@link Customer}
      * entity which provides an empty result set.
      */
     private PersistentEntityCriteria getEmptyResultSetCustomerCriteria() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
     	
     	entityCriteria.add(new FilterCriterion("email", "nobody@non-existing-provider.net", false, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, directValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         return entityCriteria;
     }
     
     /**
      * Verify the test data shared by all test case methods.
      */
     @Before
     public void verifyTestData() {
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(0));
         
         stockItemDao.getHibernateSession().clear();
     }
     
     /**
      * Test for {@link PersistentEntityDao#getEntityClass()}.
      */
     @Test
     public void testGetEntityClass() {
         assertThat(stockItemDao.getEntityClass(), equalTo(StockItem.class));
     }
     
     /**
      * Test for {@link PersistentEntityDao#saveOrUpdate(Persistable)}:
      * persisting a transient instance.
      */
     @Test
     public void testSaveOrUpdate_persistingTransientInstance() {
         final PromotedStockItem stockItemTransient = getSampleStockItemTransient();
         final PromotedStockItem stockItemPersistent = stockItemDao.saveOrUpdate(stockItemTransient);
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(true));
         assertThat(stockItemPersistent.getId(), notNullValue());
         
         verifyStockItemPropertiesMatch(stockItemTransient, stockItemPersistent);
     }
     
     /**
      * Test for {@link PersistentEntityDao#saveOrUpdate(Persistable)}:
      * updating a detached instance.
      */
     @Test
     public void testSaveOrUpdate_updatingDetachedInstance() {
         final PromotedStockItem stockItemDetached = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(true));
         assertThat(stockItemDetached.getId(), notNullValue());
         
         stockItemDao.getHibernateSession().evict(stockItemDetached);
         
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(false));
         
         modifySampleStockItem(stockItemDetached);
         
         final PromotedStockItem stockItemPersistent = stockItemDao.saveOrUpdate(stockItemDetached);
         
         assertThat(stockItemPersistent.getId(), notNullValue());
         
         verifyStockItemPropertiesMatch(stockItemDetached, stockItemPersistent);
     }
     
     /**
      * Test for {@link PersistentEntityDao#saveOrUpdate(Persistable)}:
      * trying to persist a transient instance which violates database
      * constraints.
      */
     @Test
     @ExpectedException(HibernateException.class)
     public void testSaveOrUpdate_persistingConstraintViolatingTransientInstance() {
         final PromotedStockItem stockItemTransientViolating = getSampleConstraintViolatingStockItemTransient();
         
         stockItemDao.saveOrUpdate(stockItemTransientViolating);
     }
     
     /**
      * Test for {@link PersistentEntityDao#delete(Persistable)}:
      * deleting a persistent instance.
      */
     @Test
     public void testDeleteByInstance_deletingPersistentInstance() {
         final PromotedStockItem stockItemPersistent = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(true));
         
         stockItemDao.delete(stockItemPersistent);
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(0));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(false));
         
         assertThat(stockItemDao.get(stockItemPersistent.getId(), PromotedStockItem.class), nullValue());
     }
     
     /**
      * Test for {@link PersistentEntityDao#delete(Serializable, Class)}:
      * deleting a persistent instance.
      */
     @Test
     public void testDeleteById_deletingPersistentInstance() {
         final PromotedStockItem stockItemPersistent = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(true));
         
         stockItemDao.delete(stockItemPersistent.getId(), PromotedStockItem.class);
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(0));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(false));
         
         assertThat(stockItemDao.get(stockItemPersistent.getId(), PromotedStockItem.class), nullValue());
     }
     
     /**
      * Test for {@link PersistentEntityDao#deleteAll(Class)}:
      * deleting multiple persistent instances.
      */
     @Test
     public void testDeleteAll_deletingPersistentInstances() {
         final PromotedStockItem stockItemPersistentOne = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         final PromotedStockItem stockItemPersistentTwo = stockItemDao.saveOrUpdate(getModifiedSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(2));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentOne), equalTo(true));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentTwo), equalTo(true));
         
         final int instanceDeleteCount = stockItemDao.deleteAll(PromotedStockItem.class);
         
         assertThat(instanceDeleteCount, equalTo(2));
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(0));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentOne), equalTo(false));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentTwo), equalTo(false));
         
         assertThat(stockItemDao.get(stockItemPersistentOne.getId(), PromotedStockItem.class), nullValue());
         assertThat(stockItemDao.get(stockItemPersistentTwo.getId(), PromotedStockItem.class), nullValue());
     }
     
     /**
      * Test for {@link PersistentEntityDao#refresh(Persistable)}:
      * refreshing a persistent instance.
      */
     @Test
     public void testRefresh_refreshingPersistentInstance() {
         final PromotedStockItem stockItemPersistent = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(true));
         
         PromotedStockItem stockItemPersistentModified = stockItemDao.get(stockItemPersistent.getId(), PromotedStockItem.class);
         modifySampleStockItem(stockItemPersistentModified);
         stockItemPersistentModified = stockItemDao.saveOrUpdate(stockItemPersistentModified);
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentModified), equalTo(true));
         
         stockItemDao.refresh(stockItemPersistent);
         
         assertThat(stockItemPersistent.getId(), equalTo(stockItemPersistentModified.getId()));
         
         verifyStockItemPropertiesMatch(stockItemPersistent, stockItemPersistentModified);
     }
     
     /**
      * Test for {@link PersistentEntityDao#refresh(Persistable)}:
      * refreshing a detached instance.
      */
     @Test
     public void testRefresh_refreshingDetachedInstance() {
         final PromotedStockItem stockItemDetached = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(true));
         
         stockItemDao.getHibernateSession().evict(stockItemDetached);
         
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(false));
         
         PromotedStockItem stockItemPersistentModified = stockItemDao.get(stockItemDetached.getId(), PromotedStockItem.class);
         modifySampleStockItem(stockItemPersistentModified);
         stockItemPersistentModified = stockItemDao.saveOrUpdate(stockItemPersistentModified);
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentModified), equalTo(true));
         
         stockItemDao.getHibernateSession().evict(stockItemPersistentModified);
         
         stockItemDao.refresh(stockItemDetached);
         
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(true));
         assertThat(stockItemDetached.getId(), equalTo(stockItemPersistentModified.getId()));
         
         verifyStockItemPropertiesMatch(stockItemDetached, stockItemPersistentModified);
     }
     
     /**
      * Test for {@link PersistentEntityDao#get(Serializable, Class)}:
      * retrieving a persistent instance.
      */
     @Test
     public void testGet_retrievingPersistentInstance() {
         final PromotedStockItem stockItemDetached = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(1));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(true));
         
         stockItemDao.getHibernateSession().evict(stockItemDetached);
         
         assertThat(stockItemDao.getHibernateSession().contains(stockItemDetached), equalTo(false));
         
         final PromotedStockItem stockItemPersistent = stockItemDao.get(stockItemDetached.getId(), PromotedStockItem.class);
         
         assertThat(stockItemPersistent, notNullValue());
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistent), equalTo(true));
         assertThat(stockItemPersistent.getId(), equalTo(stockItemDetached.getId()));
         
         verifyStockItemPropertiesMatch(stockItemPersistent, stockItemDetached);
     }
     
     /**
      * Test for {@link PersistentEntityDao#get(Serializable, Class)}:
      * trying to retrieve a non-existing persistent instance.
      */
     @Test
     public void testGet_retrievingNonExistingPersistentInstance() {
         final PromotedStockItem stockItemPersistentNonExisting = stockItemDao.get(1L, PromotedStockItem.class);
         
         assertThat(stockItemPersistentNonExisting, nullValue());
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(0));
     }
     
     /**
      * Test for {@link PersistentEntityDao#getAll(Class)}:
      * retrieving multiple persistent instances.
      */
     @Test
     public void testGetAll_retrievingPersistentInstances() {
         final PromotedStockItem stockItemPersistentOne = stockItemDao.saveOrUpdate(getSampleStockItemTransient());
         final PromotedStockItem stockItemPersistentTwo = stockItemDao.saveOrUpdate(getModifiedSampleStockItemTransient());
         
         assertThat(stockItemDao.count(new NestedPropertyCriteria(), PromotedStockItem.class), equalTo(2));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentOne), equalTo(true));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentTwo), equalTo(true));
         
         stockItemDao.getHibernateSession().evict(stockItemPersistentOne);
         stockItemDao.getHibernateSession().evict(stockItemPersistentTwo);
         
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentOne), equalTo(false));
         assertThat(stockItemDao.getHibernateSession().contains(stockItemPersistentTwo), equalTo(false));
         
         final List<PromotedStockItem> stockItemList = stockItemDao.getAll(PromotedStockItem.class);
         
         assertThat(stockItemList.size(), equalTo(2));
         
         assertThat(stockItemList.contains(stockItemPersistentOne), equalTo(true));
         assertThat(stockItemList.contains(stockItemPersistentTwo), equalTo(true));
         
         verifyStockItemsInList(stockItemList,
         		new Long[] { stockItemPersistentOne.getId(), stockItemPersistentTwo.getId() },
         		new PromotedStockItem[] { stockItemPersistentOne, stockItemPersistentTwo });
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
     * retrieving persistent entities using empty {@link CompositeCriteria}.
      */
     @Test
     public void testQuery_emptyCompositeCriteria() {
         final List<Customer> customerList = customerDao.query(new NestedPropertyCriteria(), Customer.class);
         
         assertThat(customerList.size(), equalTo(TOTAL_CUSTOMER_COUNT));
         
         verifyMutablePersistentEntitiesInList(customerList, new Long[] { customerOneId, customerTwoId });
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using {@link FilterCriterion}.
      * 
      * <p>
      * 
      * Filtering by direct persistent entity properties (excluding associated objects).
      */
     @Test
     public void testQuery_filterCriteria_directProperties() {
         final Customer filterObject = new Customer();
         filterObject.setFirstName(CUSTOMER_TWO_FIRST_NAME);
         filterObject.setLastName(CUSTOMER_TWO_LAST_NAME_PREFIX + "%");
         filterObject.setEmail("%" + CUSTOMER_EMAIL_SUFFIX);
         
         final Integer orderCount = ORDER_PER_CUSTOMER_COUNT;
         
         final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.setFilterObject(filterObject);
         
         entityCriteria.add(new FilterCriterion("firstName", "firstName", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("lastName", "lastName", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.like(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("email", "email", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.like(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("orders", orderCount, false, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.sizeEq(targetPropertyName, (Integer) directValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         final List<Customer> customerList = customerDao.query(entityCriteria, Customer.class);
         
         assertThat(customerList.size(), equalTo(1));
         
         final Customer singleResult = customerList.get(0);
         
         assertThat(singleResult.getId(), notNullValue());
         assertThat(singleResult.getId(), equalTo(customerTwoId));
         
         assertThat(singleResult.getFirstName(), equalTo(filterObject.getFirstName()));
         assertThat(singleResult.getLastName().startsWith(CUSTOMER_TWO_LAST_NAME_PREFIX), equalTo(true));
         assertThat(singleResult.getEmail().endsWith(CUSTOMER_EMAIL_SUFFIX), equalTo(true));
         assertThat(singleResult.getUnmodifiableOrderList().size(), equalTo(ORDER_PER_CUSTOMER_COUNT));
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using {@link FilterCriterion}.
      * 
      * <p>
      * 
      * Filtering by nested persistent entity properties (including associated objects).
      */
     @Test
     public void testQuery_filterCriteria_nestedProperties() {
         final Customer filterCustomer = new Customer();
         filterCustomer.setFirstName(CUSTOMER_ONE_FIRST_NAME);
         filterCustomer.setEmail("%" + CUSTOMER_EMAIL_SUFFIX);
         
         final Order filterOrder = new Order();
         filterOrder.setShippingAddress(orderOneShippingAddress);
         filterOrder.setComplete(false);
         filterCustomer.addOrder(filterOrder);
         
         final StockItem filterStockItem = new StockItem();
         filterStockItem.setPrice(0);
         
         final StockItemCategory filterStockItemCategory = new StockItemCategory();
         filterStockItemCategory.setName(STOCK_ITEM_CATEGORY_FOOD);
         filterStockItem.setCategory(filterStockItemCategory);
         
         final OrderItem filterObject = new OrderItem();
         filterObject.setStockItem(filterStockItem);
         filterOrder.addOrderItem(filterObject);
         
         final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.setFilterObject(filterObject);
         
         entityCriteria.add(new FilterCriterion("order.shippingAddress", "order.shippingAddress", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("order.complete", "order.complete", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("order.customer.firstName", "order.customer.firstName", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("order.customer.email", "order.customer.email", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.like(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("stockItem.price", "stockItem.price", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.gt(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new FilterCriterion("stockItem.category.name", "stockItem.category.name", true, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, filterObjectValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         final List<OrderItem> orderItemList = orderItemDao.query(entityCriteria, OrderItem.class);
         
         assertThat(orderItemList.size(), equalTo(1));
         
         final OrderItem singleResult = orderItemList.get(0);
         
         assertThat(singleResult.getId(), notNullValue());
         
         assertThat(singleResult.getOrder().getShippingAddress(), equalTo(filterObject.getOrder().getShippingAddress()));
         assertThat(singleResult.getOrder().getComplete(), equalTo(filterObject.getOrder().getComplete()));
         assertThat(singleResult.getOrder().getCustomer().getFirstName(), equalTo(filterObject.getOrder().getCustomer().getFirstName()));
         assertThat(singleResult.getOrder().getCustomer().getEmail().endsWith(CUSTOMER_EMAIL_SUFFIX), equalTo(true));
         assertThat(singleResult.getStockItem().getPrice() > filterObject.getStockItem().getPrice(), equalTo(true));
         assertThat(singleResult.getStockItem().getCategory().getName(), equalTo(filterObject.getStockItem().getCategory().getName()));
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using {@link SortCriterion}.
      * 
      * <p>
      * 
      * Sorting by direct persistent entity properties (excluding associated objects).
      */
     @Test
     public void testQuery_sortCriteria_directProperties() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.add(new SortCriterion("firstName", false, false));
         entityCriteria.add(new SortCriterion("email", true, true));
         
         final List<Customer> customerList = customerDao.query(entityCriteria, Customer.class);
         
         assertThat(customerList.size(), equalTo(TOTAL_CUSTOMER_COUNT));
         
         verifyMutablePersistentEntitiesInList(customerList, new Long[] { customerOneId, customerTwoId });
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using {@link SortCriterion}.
      * 
      * <p>
      * 
      * Sorting by nested persistent entity properties (including associated objects).
      */
     @Test
     public void testQuery_sortCriteria_nestedProperties() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.add(new SortCriterion("order.customer.email", true, true));
         entityCriteria.add(new SortCriterion("stockItem.category.name", true, false));
         entityCriteria.add(new SortCriterion("stockItem.price", false));
         
         final List<OrderItem> orderItemList = orderItemDao.query(entityCriteria, OrderItem.class);
         
         assertThat(orderItemList.size(), equalTo(TOTAL_ORDER_ITEM_COUNT));
         
         for (int i = 0; i < TOTAL_ORDER_ITEM_COUNT; i++) {
             final OrderItem result = orderItemList.get(i);
             
             assertThat(result.getId(), notNullValue());
             
             final String customerEmailPrefix = i < (TOTAL_ORDER_ITEM_COUNT / 2) ? CUSTOMER_ONE_EMAIL_PREFIX : CUSTOMER_TWO_EMAIL_PREFIX;
             assertThat(result.getOrder().getCustomer().getEmail().startsWith(customerEmailPrefix), equalTo(true));
             
             final int resultModuloPosition = i % (TOTAL_ORDER_ITEM_COUNT / 2);
             
             String categoryName;
             if (resultModuloPosition < 2) {
                 categoryName = STOCK_ITEM_CATEGORY_COMPUTERS;
             } else {
                 categoryName = STOCK_ITEM_CATEGORY_FOOD;
             }
             assertThat(result.getStockItem().getCategory().getName(), equalTo(categoryName));
             
             Integer stockItemPrice;
             if (resultModuloPosition == 0 || resultModuloPosition == 3) {
                 stockItemPrice = 3 - resultModuloPosition;
             } else {
                 stockItemPrice = resultModuloPosition;
             }
             assertThat(result.getStockItem().getPrice(), equalTo(stockItemPrice));
         }
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using the implicit paging functionality.
      * 
      * <p>
      * 
      * Requesting all elements.
      */
     @Test
     public void testQuery_pagingCriteria_allElements() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         final List<Customer> customerList = customerDao.query(entityCriteria, Customer.class);
         
         assertThat(customerList.size(), equalTo(TOTAL_CUSTOMER_COUNT));
         
         verifyMutablePersistentEntitiesInList(customerList, new Long[] { customerOneId, customerTwoId });
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using the implicit paging functionality.
      * 
      * <p>
      * 
      * Requesting second element.
      */
     @Test
     public void testQuery_pagingCriteria_secondElement() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.setFirstResult(1);
         entityCriteria.setMaxResults(1);
         
         final List<Customer> customerList = customerDao.query(entityCriteria, Customer.class);
         
         assertThat(customerList.size(), equalTo(1));
         
         verifyMutablePersistentEntitiesInList(customerList, new Long[] { customerTwoId });
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using the implicit paging functionality.
      * 
      * <p>
      * 
      * Trying to retrieve entities by exceeding the <tt>firstResult</tt> parameter.
      */
     @Test
     public void testQuery_pagingCriteria_firstResultOutOfBounds() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.setFirstResult(10);
         
         final List<Customer> customerList = customerDao.query(entityCriteria, Customer.class);
         
         assertThat(customerList.size(), equalTo(0));
     }
     
     /**
      * Test for {@link PersistentEntityDao#query(PersistentEntityCriteria, Class)}:
      * retrieving persistent entities using a composition of {@link FilterCriterion}
      * and {@link SortCriterion} instances along with the implicit paging functionality.
     * 
     * <p>
     * 
     * Combining {@link FilterCriteria}, {@link SortCriteria} and {@link PagingCriteria}.
      */
     @Test
     public void testQuery_compositeCriteria_filterSortPaging() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
     	
     	entityCriteria.add(new FilterCriterion("stockItem.category.name", STOCK_ITEM_CATEGORY_COMPUTERS, false, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, directValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         entityCriteria.add(new SortCriterion("stockItem.price", false));
         
         entityCriteria.setFirstResult(1);
         entityCriteria.setMaxResults(2);
         
         final List<OrderItem> orderItemList = orderItemDao.query(entityCriteria, OrderItem.class);
         
         assertThat(orderItemList.size(), equalTo(2));
         
         assertThat(orderItemList.get(0).getStockItem().getCategory().getName(), equalTo(STOCK_ITEM_CATEGORY_COMPUTERS));
         assertThat(orderItemList.get(1).getStockItem().getCategory().getName(), equalTo(STOCK_ITEM_CATEGORY_COMPUTERS));
         
         assertThat(orderItemList.get(0).getStockItem().getPrice() > orderItemList.get(1).getStockItem().getPrice(), equalTo(true));
     }
     
     /**
      * Test for {@link PersistentEntityDao#uniqueResult(PersistentEntityCriteria, boolean, Class)}:
      * trying to retrieve unique result from an empty result set.
      */
     @Test
     public void testUniqueResult_emptyResultSet() {
         final PersistentEntityCriteria entityCriteria = getEmptyResultSetCustomerCriteria();
         
         final Customer uniqueCustomer = customerDao.uniqueResult(entityCriteria, false, Customer.class);
         
         assertThat(uniqueCustomer, nullValue());
     }
     
     /**
      * Test for {@link PersistentEntityDao#uniqueResult(PersistentEntityCriteria, boolean, Class)}:
      * retrieving unique result from a result set with single instance.
      */
     @Test
     public void testUniqueResult_singleInstanceResultSet() {
     	final NestedPropertyCriteria entityCriteria = new NestedPropertyCriteria();
         entityCriteria.add(new FilterCriterion("email", customerOneEmail, false, new PropertyFilterCriterionProvider() {
             public Criterion getCriterion(String targetPropertyName, Object[] filterObjectValues, Object[] directValues) {
                 return Restrictions.eq(targetPropertyName, directValues[0]);
             }
             public boolean enabled(Object[] filterObjectValues, Object[] directValues) {
                 return true;
             }
         }));
         
         final Customer uniqueCustomer = customerDao.uniqueResult(entityCriteria, false, Customer.class);
         
         assertThat(uniqueCustomer, notNullValue());
         assertThat(uniqueCustomer.getId(), equalTo(customerOneId));
     }
     
     /**
      * Test for {@link PersistentEntityDao#uniqueResult(PersistentEntityCriteria, boolean, Class)}:
      * retrieving unique result from a result set with multiple instances.
      * 
      * <p>
      * 
      * Setting <tt>returnNullOnMultipleResults</tt> to <tt>false</tt>.
      */
     @Test
     public void testUniqueResult_multiInstanceResultSet_dontEnforceSingleResult() {
         final Customer uniqueCustomer = customerDao.uniqueResult(new NestedPropertyCriteria(), false, Customer.class);
         
         assertThat(uniqueCustomer, notNullValue());
         assertThat(uniqueCustomer.getId(), equalTo(customerOneId));
     }
     
     /**
      * Test for {@link PersistentEntityDao#uniqueResult(PersistentEntityCriteria, boolean, Class)}:
      * retrieving unique result from a result set with multiple instances.
      * 
      * <p>
      * 
      * Setting <tt>returnNullOnMultipleResults</tt> to <tt>true</tt>.
      */
     @Test
     public void testUniqueResult_multiInstanceResultSet_enforceSingleResult() {
         final Customer uniqueCustomer = customerDao.uniqueResult(new NestedPropertyCriteria(), true, Customer.class);
         
         assertThat(uniqueCustomer, nullValue());
     }
     
     /**
      * Test for {@link PersistentEntityDao#count(PersistentEntityCriteria, Class)}:
      * performing instance count on an empty result set.
      */
     @Test
     public void testCount_emptyResultSet() {
         final PersistentEntityCriteria entityCriteria = getEmptyResultSetCustomerCriteria();
         
         final int count = customerDao.count(entityCriteria, Customer.class);
         
         assertThat(count, equalTo(0));
     }
     
     /**
      * Test for {@link PersistentEntityDao#count(PersistentEntityCriteria, Class)}:
      * performing instance count on a result set with multiple instances.
      */
     @Test
     public void testCount_multiInstanceResultSet() {
         final int count = customerDao.count(new NestedPropertyCriteria(), Customer.class);
         
         assertThat(count, equalTo(2));
     }
     
 }
