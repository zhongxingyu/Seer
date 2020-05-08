 package com.euroit.militaryshop.persistence.dao.impl;
 
 import com.euroit.eshop.dto.BaseTrolleyItemDto;
 import com.euroit.eshop.persistence.dao.impl.BaseEntityManagerSupport;
 import com.euroit.eshop.util.SeoNameGenerator;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.persistence.dao.DictionaryEntryDao;
 import com.euroit.militaryshop.persistence.dao.ItemDao;
 import com.euroit.militaryshop.persistence.entity.DictionaryEntry;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopItem;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopProduct;
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterOperator;

 import org.apache.commons.lang.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.support.SortDefinition;
 import org.springframework.stereotype.Repository;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import java.util.*;
 
 /**
  * @author Roman Tuchin
  */
 @Repository
 public class ItemDaoImpl extends BaseEntityManagerSupport implements ItemDao {
 
     private static final Logger LOG = LoggerFactory.getLogger(ItemDaoImpl.class);
     
     public static final int IMAGE_NAME_LENGTH = 6;
     
     private DictionaryEntryDao dictionaryEntryDao;
 
     private SeoNameGenerator seoNameGenerator;
 
     @Override
     public long createOrSave(MilitaryShopItemDto itemDto) {
 
         MilitaryShopProduct product = em.find(MilitaryShopProduct.class, itemDto.getProductId());
         Assert.notNull(product);
 
         MilitaryShopItem item = null;
 
         if (itemDto.getId() != 0) {
             Key itemKey = new KeyFactory.Builder(MilitaryShopItem.class.getSimpleName(),
                     itemDto.getId()).getKey();
 
             item = em.find(MilitaryShopItem.class, itemKey);
         }
 
         if (item == null) {
             item = new MilitaryShopItem();
             
             if (!StringUtils.hasText(itemDto.getBigImageName())) {
                 String bigImageName = String.format("%s.jpg", RandomStringUtils.randomAlphanumeric(IMAGE_NAME_LENGTH));
                 itemDto.setBigImageName(bigImageName);
             }
 
             if (!StringUtils.hasText(itemDto.getSmallImageName())) {
 				String smallImageName = String.format("%s.jpg", RandomStringUtils.randomAlphanumeric(IMAGE_NAME_LENGTH));
 				itemDto.setSmallImageName(smallImageName);
             }
         }
 
         item.setSmallImageName(itemDto.getSmallImageName());
         item.setBigImageName(itemDto.getBigImageName());
         //item.setShortName(itemDto.getShortName());
         item.setInternalCatalogCode(itemDto.getInternalCatalogCode());
 
         String color = null;
         
         if (itemDto.getColorId() != 0) {
             item.setColorKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     itemDto.getColorId()));
             DictionaryEntry colorDictEntry = 
                     dictionaryEntryDao.findDictionaryEntryById(itemDto.getColorId());
             color = colorDictEntry != null ? colorDictEntry.getValue() : null;
             item.setColor(color);
             itemDto.setColor(color);
         } else {
             item.setColorKey(null);
             item.setColor(null);
         }
 
         if (itemDto.getMaterialId() != 0) {
             item.setMaterialKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     itemDto.getMaterialId()));
             DictionaryEntry materialDictEntry = 
                     dictionaryEntryDao.findDictionaryEntryById(itemDto.getMaterialId());
             item.setMaterial(materialDictEntry != null ? materialDictEntry.getValue() : null);
             itemDto.setMaterial(materialDictEntry != null ? materialDictEntry.getValue() : null);
         } else {
             item.setMaterialKey(null);
             item.setMaterial(null);
         }
 
         if (itemDto.getSizeId() != 0) {
             item.setSizeKey(KeyFactory.createKey(DictionaryEntry.class.getSimpleName(),
                     itemDto.getSizeId()));
             DictionaryEntry sizeDictEntry = 
                     dictionaryEntryDao.findDictionaryEntryById(itemDto.getSizeId());
             item.setSize(sizeDictEntry != null ? sizeDictEntry.getValue() : null);
             itemDto.setSize(sizeDictEntry != null ? sizeDictEntry.getValue() : null);
         } else {
             item.setSizeKey(null);
             item.setSize(null);
         }
 
         item.setProductKey(KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(),
                 itemDto.getProductId()));
         
         item = em.merge(item);
         
         updateProductShortNameRelatedFields(product, item);
         
         return item.getKey().getId();
     }
 
     /**
      * Generate and update seo name in the item and product, which uses the item as default 
      * @param product product entity object
      * @param item item entity object
      */
     public void updateProductShortNameRelatedFields(MilitaryShopProduct product, MilitaryShopItem item) {
         final String generatedItemSeoName = seoNameGenerator.generateItemSeoName(product.getShortName(), item.getColor());
         item.setSeoName(generatedItemSeoName);
         item.setShortName(dictionaryEntryDao.generateItemShortName(product.getFullName(), 
                 item.getColorKey(), item.getSizeKey(), item.getMaterialKey()));
         //TODO generate here internal code
         
         em.merge(item);
         
         if (item.getKey() == null || product.getDefaultItemKey() == null) {
             return;
         }
         
         if (product.getDefaultItemKey().equals(item.getKey()) && !generatedItemSeoName.equals(product.getDefaultSeoName())) {
             product.setDefaultSeoName(generatedItemSeoName);
             em.merge(product);
         }
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<MilitaryShopItem> getProductItemsByProperties(long productId, long colorId, long materialId, long sizeId) {
         StringBuilder sb = new StringBuilder("select from MilitaryShopItem i where i.productKey = :productKey and ");
         sb.append("i.colorKey = :colorKey and i.materialKey = :materialKey and i.sizeKey = :sizeKey");
 
         Query query = em.createQuery(sb.toString());
 
         query.setParameter(PROP_PRODUCT_KEY, KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
 
         if (colorId != 0) {
             query.setParameter(PROP_COLOR_KEY, KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), colorId));
         } else {
             query.setParameter(PROP_COLOR_KEY, null);
         }
 
         if (materialId != 0) {
             query.setParameter("materialKey", KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), materialId));
         } else {
             query.setParameter("materialKey", null);
         }
 
         if (sizeId != 0) {
             query.setParameter("sizeKey", KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), sizeId));
         } else {
             query.setParameter("sizeKey", null);
         }
 
         return query.getResultList();
     }
 
     @Override
     public long countItemsForProduct(long productId) {
         return countItemsForProduct2(productId, null, null, null);
     }
 
     /**
      * Low-level datastore-API count method
      */
     @Override
     public long countItemsForProduct2(long productId, List<Key> colorKeyFilterList,
                                      List<Key> materialKeyFilterList, List<Key> sizeKeyFilterList) {
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
         com.google.appengine.api.datastore.Query onlyKeysQuery =
                 new com.google.appengine.api.datastore.Query(MilitaryShopItem.class.getSimpleName()).setKeysOnly();
         
         List<Filter> subFilters = new ArrayList<>();
         
         
         com.google.appengine.api.datastore.Query.Filter productIdFilter =
                 new com.google.appengine.api.datastore.Query.FilterPredicate(PROP_PRODUCT_KEY,
                         com.google.appengine.api.datastore.Query.FilterOperator.EQUAL, 
                         KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
         subFilters.add(productIdFilter);
 
         //filter by colorKey keys
         if (colorKeyFilterList != null && !colorKeyFilterList.isEmpty()) {
             com.google.appengine.api.datastore.Query.Filter colorKeyFilter =
                     new com.google.appengine.api.datastore.Query.FilterPredicate(PROP_COLOR_KEY,
                             com.google.appengine.api.datastore.Query.FilterOperator.IN, colorKeyFilterList);
             subFilters.add(colorKeyFilter);
         }
 
         //filter by materialKey keys
         if (materialKeyFilterList != null && !materialKeyFilterList.isEmpty()) {
             com.google.appengine.api.datastore.Query.Filter materialKeyFilter =
                     new com.google.appengine.api.datastore.Query.FilterPredicate("materialKey",
                             com.google.appengine.api.datastore.Query.FilterOperator.IN, materialKeyFilterList);
             subFilters.add(materialKeyFilter);
         }
 
         //filter by sizeKey keys
         if (sizeKeyFilterList != null && !sizeKeyFilterList.isEmpty()) {
             com.google.appengine.api.datastore.Query.Filter sizeKeyFilter =
                     new com.google.appengine.api.datastore.Query.FilterPredicate("sizeKey",
                             com.google.appengine.api.datastore.Query.FilterOperator.IN, sizeKeyFilterList);
             subFilters.add(sizeKeyFilter);
         }
         
         if (subFilters.size() > 1) {
             onlyKeysQuery.setFilter(
                     new com.google.appengine.api.datastore.Query.CompositeFilter(CompositeFilterOperator.AND, subFilters));
         } else {
             onlyKeysQuery.setFilter(subFilters.get(0));
         }
 
         PreparedQuery pq = datastore.prepare(onlyKeysQuery);
        return pq.countEntities(FetchOptions.Builder.withDefaults());
     }
 
     @Override
     public List<MilitaryShopItem> getItemsForProduct(long productId, List<Key> colorKeyFilterList,
                                                      List<Key> materialKeyFilterList, List<Key> sizeKeyFilterList,
                                                      SortDefinition sortDefinition,
                                                      int startPosition, int maxResult) {
         CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
         CriteriaQuery<MilitaryShopItem> criteriaQuery = criteriaBuilder.createQuery(MilitaryShopItem.class);
         final Root<MilitaryShopItem> queryRoot = criteriaQuery.from(MilitaryShopItem.class);
         
         criteriaQuery.select(queryRoot);
 
         final TypedQuery<MilitaryShopItem> typedQuery = prepareTypedQuery(criteriaBuilder, criteriaQuery, queryRoot,
                 productId, colorKeyFilterList, materialKeyFilterList, sizeKeyFilterList, sortDefinition);
 
         return typedQuery
                 .setFirstResult(startPosition).setMaxResults(maxResult).getResultList();
     }
 
     private <T> TypedQuery<T> prepareTypedQuery(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery,
                                                 Root<MilitaryShopItem> queryRoot,
                                             long productId, List<Key> colorKeyFilterList, List<Key> materialKeyFilterList, 
                                             List<Key> sizeKeyFilterList, SortDefinition sortDefinition) {
 
         ParameterExpression<Key> productKeyParam = criteriaBuilder.parameter(Key.class);
 
         List<Predicate> restrictions = new ArrayList<>();
 
         final Predicate productKeyPredicate = criteriaBuilder.equal(queryRoot.get(PROP_PRODUCT_KEY), productKeyParam);
         restrictions.add(productKeyPredicate);
 
         ParameterExpression<List> colorKeyParams = null;
         //filter by colorKey keys
         if (colorKeyFilterList != null && !colorKeyFilterList.isEmpty()) {
             colorKeyParams = criteriaBuilder.parameter(List.class);
             restrictions.add(queryRoot.get(PROP_COLOR_KEY).in(colorKeyParams));
         }
 
         ParameterExpression<List> materialKeyParams = null;
         //filter by materialKey keys
         if (materialKeyFilterList != null && !materialKeyFilterList.isEmpty()) {
             materialKeyParams = criteriaBuilder.parameter(List.class);
             restrictions.add(queryRoot.get("materialKey").in(materialKeyParams));
         }
 
         ParameterExpression<List> sizeKeyParams = null;
         //filter by sizeKey keys
         if (sizeKeyFilterList != null && !sizeKeyFilterList.isEmpty()) {
             sizeKeyParams = criteriaBuilder.parameter(List.class);
             restrictions.add(queryRoot.get("sizeKey").in(sizeKeyParams));
         }
 
         Expression<Boolean> whereRestriction = criteriaBuilder.and(restrictions.toArray(new Predicate[]{}));
         criteriaQuery.where(whereRestriction);
         
         if (sortDefinition != null && StringUtils.hasText(sortDefinition.getProperty())) {
             Order order;
             if (sortDefinition.isAscending()) {
                 order = criteriaBuilder.asc(queryRoot.get(sortDefinition.getProperty()));
             } else {
                 order = criteriaBuilder.desc(queryRoot.get(sortDefinition.getProperty()));
             }
             criteriaQuery.orderBy(order);
         }
 
         final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery)
                 .setParameter(productKeyParam, KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
         if (colorKeyParams != null) {
             typedQuery.setParameter(colorKeyParams, colorKeyFilterList);
         }
 
         if (materialKeyParams != null) {
             typedQuery.setParameter(materialKeyParams, materialKeyFilterList);
         }
 
         if (sizeKeyParams != null) {
             typedQuery.setParameter(sizeKeyParams, sizeKeyFilterList);
         }
 
         return typedQuery;
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<MilitaryShopItem> getItemsForProduct(long productId) {
         Query query = em.createQuery("select from MilitaryShopItem i where i.productKey = :productKey");
         query.setParameter(PROP_PRODUCT_KEY, KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
 
         return query.getResultList();
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public List<MilitaryShopItem> getItemsForProductAndColor(long productId, long colorId) {
         Query query = em.createQuery("select from MilitaryShopItem i where i.productKey = :productKey and i.colorKey = :colorKey");
         query.setParameter(PROP_PRODUCT_KEY, KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
         query.setParameter(PROP_COLOR_KEY, KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), colorId));
 
         return query.getResultList();
     }
     
     @Override
     public List<MilitaryShopItem> getItemsForProductByColorAndMaterial(long productId, long colorId, Long materialId) {
         CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
         CriteriaQuery<MilitaryShopItem> criteriaQuery = criteriaBuilder.createQuery(MilitaryShopItem.class);
         final Root<MilitaryShopItem> queryRoot = criteriaQuery.from(MilitaryShopItem.class);
         
         List<Predicate> restrictions = new ArrayList<>();
 
         ParameterExpression<Key> productKeyParam = criteriaBuilder.parameter(Key.class);
         
         final Predicate productKeyPredicate = criteriaBuilder.equal(queryRoot.get(PROP_PRODUCT_KEY), productKeyParam);
         restrictions.add(productKeyPredicate);
         
         ParameterExpression<Key> colorKeyParam = criteriaBuilder.parameter(Key.class);
         
         final Predicate colorKeyPredicate = criteriaBuilder.equal(queryRoot.get(PROP_COLOR_KEY), colorKeyParam);
         restrictions.add(colorKeyPredicate);
         
         ParameterExpression<Key> materialKeyParam = null;
         
         if (materialId != null) {
             materialKeyParam = criteriaBuilder.parameter(Key.class);
             
             final Predicate materialKeyPredicate = criteriaBuilder.equal(queryRoot.get("materialKey"), materialKeyParam);
             restrictions.add(materialKeyPredicate);
         }
         
         Expression<Boolean> whereRestriction = criteriaBuilder.and(restrictions.toArray(new Predicate[]{}));
         criteriaQuery.where(whereRestriction);
         
         TypedQuery<MilitaryShopItem> query = em.createQuery(criteriaQuery);
         
         query.setParameter(productKeyParam, KeyFactory.createKey(MilitaryShopProduct.class.getSimpleName(), productId));
         query.setParameter(colorKeyParam, KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), colorId));
         
         if (materialId != null && materialKeyParam != null) {
             query.setParameter(materialKeyParam, KeyFactory.createKey(DictionaryEntry.class.getSimpleName(), materialId));
         }
         
         return query.getResultList();
     }
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map<BaseTrolleyItemDto, MilitaryShopItem> getItemsByIds(Set<BaseTrolleyItemDto> baseItemSet) {
 		Query query = em.createQuery("select from MilitaryShopItem i where i.key in (:itemsKeyList)");
 		List<Key> itemKeys = new ArrayList<Key>();
 		Map<BaseTrolleyItemDto, MilitaryShopItem> retMap = 
 				new LinkedHashMap<BaseTrolleyItemDto, MilitaryShopItem>();
 		
 		for (BaseTrolleyItemDto baseItem : baseItemSet) {
 			Key itemKey = new KeyFactory.Builder(MilitaryShopItem.class.getSimpleName(),
 							baseItem.getItemId()).getKey();
 			itemKeys.add(itemKey);
 			retMap.put(baseItem, null);
 		}
 		
 		query.setParameter("itemsKeyList", itemKeys);
 		
 		List<MilitaryShopItem> itemsList = query.getResultList();
 		
 		for (MilitaryShopItem item : itemsList) {
 			BaseTrolleyItemDto baseItem = new BaseTrolleyItemDto();
 			baseItem.setItemId(item.getKey().getId());
 			baseItem.setProductId(item.getProductKey().getId());
 			
 			retMap.put(baseItem, item);
 		}
 		
 		return retMap;
 	}
 
     @Override
     public MilitaryShopItem findItemByKey(Key itemKey) {
         return em.find(MilitaryShopItem.class, itemKey);
     }
     
     @Override
     public MilitaryShopItem findItemBySeoName(String itemSeoName) {
         Query query = em.createQuery("select from MilitaryShopItem i where i.seoName = :itemSeoName");
         query.setParameter("itemSeoName", itemSeoName);
         
         return (MilitaryShopItem)query.setMaxResults(1).getSingleResult();
     }
 
     public void setDictionaryEntryDao(DictionaryEntryDao dictionaryEntryDao) {
         this.dictionaryEntryDao = dictionaryEntryDao;
     }
 
     public void setSeoNameGenerator(SeoNameGenerator seoNameGenerator) {
         this.seoNameGenerator = seoNameGenerator;
     }
 
     @Override
     public List<Entity> getAllPossibleColorsForProduct(Key productKey) {
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
         com.google.appengine.api.datastore.Query query =
                 new com.google.appengine.api.datastore.Query(MilitaryShopItem.class.getSimpleName());
         query.addProjection(new PropertyProjection(PROP_COLOR_KEY, Key.class));
         query.addProjection(new PropertyProjection(PROP_COLOR, String.class));
         query.addProjection(new PropertyProjection(PROP_SEO_NAME, String.class));
         query.setDistinct(true);
         com.google.appengine.api.datastore.Query.Filter productKeyFilter 
             = new com.google.appengine.api.datastore.Query.FilterPredicate(PROP_PRODUCT_KEY, FilterOperator.EQUAL, productKey);
         query.setFilter(productKeyFilter);
         
         PreparedQuery preparedQuery = datastore.prepare(query);
         return preparedQuery.asList(FetchOptions.Builder.withDefaults());
     }
 
     @Override
     public List<Entity> getAllPossibleMaterialsForProductAndColor(Key productKey, Key colorKey) {
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
         com.google.appengine.api.datastore.Query query =
                 new com.google.appengine.api.datastore.Query(MilitaryShopItem.class.getSimpleName());
         query.addProjection(new PropertyProjection(PROP_MATERIAL_KEY, Key.class));
         query.addProjection(new PropertyProjection(PROP_MATERIAL, String.class));
         query.setDistinct(true);
         
         com.google.appengine.api.datastore.Query.Filter productKeyFilter 
             = new com.google.appengine.api.datastore.Query.FilterPredicate(PROP_PRODUCT_KEY, FilterOperator.EQUAL, productKey);
         
         com.google.appengine.api.datastore.Query.Filter colorKeyFilter 
             = new com.google.appengine.api.datastore.Query.FilterPredicate(PROP_COLOR_KEY, FilterOperator.EQUAL, colorKey);
         
         Collection<Filter> subFilters = new HashSet<>();
         subFilters.add(productKeyFilter);
         subFilters.add(colorKeyFilter);
         
         Filter compositeFilter = new com.google.appengine.api.datastore.Query.CompositeFilter(CompositeFilterOperator.AND,
                 subFilters);
         query.setFilter(compositeFilter);
         
         PreparedQuery preparedQuery = datastore.prepare(query);
         return preparedQuery.asList(FetchOptions.Builder.withDefaults());
     }
 }
