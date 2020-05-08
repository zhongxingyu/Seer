 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.magento.api.catalog;
 
 import com.magento.api.AssociativeEntity;
 import com.magento.api.CatalogCategoryEntityCreate;
 import com.magento.api.CatalogInventoryStockItemUpdateEntity;
 import com.magento.api.CatalogProductAttributeMediaCreateEntity;
 import com.magento.api.CatalogProductCreateEntity;
 import com.magento.api.CatalogProductImageFileEntity;
 import com.magento.api.CatalogProductLinkEntity;
 import com.magento.api.CatalogProductRequestAttributes;
 import com.magento.api.CatalogProductTierPriceEntity;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.UnhandledException;
 import org.apache.commons.lang.Validate;
 import org.mule.module.magento.api.AbstractMagentoClient;
 import org.mule.module.magento.api.AxisPortProvider;
 import org.mule.module.magento.api.catalog.model.MediaMimeType;
 import org.mule.module.magento.api.catalog.model.ProductIdentifier;
 import org.mule.module.magento.api.util.MagentoObject;
 import org.mule.module.magento.filters.FiltersParser;
 import org.mule.util.Base64;
 
 import javax.validation.constraints.NotNull;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import static org.mule.module.magento.api.util.MagentoObject.fromMap;
 import static org.mule.module.magento.api.util.MagentoObject.removeNullValues;
 
 
 public class AxisMagentoCatalogClient extends AbstractMagentoClient
     implements MagentoCatalogClient<Object, Object[], RemoteException>
 {
 
     public AxisMagentoCatalogClient(AxisPortProvider provider)
     {
         super(provider);
     }
 
     /*Category*/
     
     /**
      * Lists product of a given category. See  catalog-category-assignedProducts SOAP method.   
      *  
      * @param categoryId
      * @return the listing of category products
      */
     public Object[] listCategoryProducts(int categoryId) throws RemoteException
     {
         return getPort().catalogCategoryAssignedProducts(getSessionId(), categoryId);
     }
 
     /**
      * Assign product to category. See catalog-category-assignProduct SOAP method
      *  
      * @param categoryId
      * @param productId
      * @param position
      */
     public void addCategoryProduct(int categoryId,
                                    @NotNull ProductIdentifier productId,
                                    String position) throws RemoteException
     {
         getPort().catalogCategoryAssignProduct(getSessionId(), categoryId, productId.getIdentifierAsString(), position,
             productId.getIdentifierType());
     }
 
     /**
      * Creates a new category. See catalog-category-create SOAP method.
      *  
      * @param parentId
      * @param attributes
      * @param storeView
      * @return the new category id
      */
     public int createCategory(int parentId, @NotNull Map<String, Object> attributes, String storeView)
         throws RemoteException
     {
         Validate.notNull(attributes);
         return getPort().catalogCategoryCreate(getSessionId(), parentId,
             fromMap(CatalogCategoryEntityCreate.class, attributes), storeView);
     }
     
     public int getCatalogCurrentStoreView() throws RemoteException
     {
         return getPort().catalogCategoryCurrentStore(getSessionId(), null);
     }
     
     public void updateCatalogCurrentStoreView(String storeViewIdOrCode) throws RemoteException
     {
         Validate.notNull(storeViewIdOrCode);
         getPort().catalogCategoryCurrentStore(getSessionId(), storeViewIdOrCode);
     }
 
     /**
      * Deletes a category. See  catalog-category-delete SOAP method
      *  
      * @param categoryId
     * @return a boolean value indicating whether the category was deleted or not
      */
    public boolean deleteCategory(int categoryId) throws RemoteException
     {
        return getPort().catalogCategoryDelete(getSessionId(), categoryId);
     }
 
     /**
      * Answers category attributes. See catalog-category-info  SOAP method.  
      * 
      * @param categoryId
      * @param storeView
      * @param attributeNames
      * @return the category attributes
      */
     public Object getCategory(int categoryId, String storeView, List<String> attributeNames) throws RemoteException
     {
         return getPort().catalogCategoryInfo(getSessionId(), categoryId, storeView, toArray(attributeNames, String.class));
     }
     
     public Object[] listCategoryLevels(String website, String storeView, String parentCategory)
         throws RemoteException
     {
         return getPort().catalogCategoryLevel(getSessionId(), website, storeView, parentCategory);
     }
 
     /**
      * Move category in tree. See  catalog-category-move SOAP method. 
      *  
      * @param categoryId
      * @param parentId
      * @param afterId
      * 
      */
     public void moveCategory(int categoryId, int parentId, String afterId) throws RemoteException
     {
         getPort().catalogCategoryMove(getSessionId(), categoryId, parentId, afterId);
     }
 
     /**
      * Remove a product assignment. See catalog-category-removeProduct SOAP method. 
      *   
      * @param categoryId
      * @param productId
      * @return
      * 
      */
     public void deleteCategoryProduct(int categoryId, @NotNull ProductIdentifier productId)
         throws RemoteException
     {
         getPort().catalogCategoryRemoveProduct(getSessionId(), categoryId, productId.getIdentifierAsString(),
             productId.getIdentifierType());
     }
 
     /**
      * 
      * Retrieve hierarchical tree. See  catalog-category-tree SOAP method. 
      * @param parentId
      * @param storeView
      * @return
      * 
      */
     public Object getCategoryTree(String parentId, String storeView) throws RemoteException
     {
         return getPort().catalogCategoryTree(getSessionId(), parentId, storeView);
     }
 
     /**
      * Updates a category. See catalog-category-update SOAP method
      * 
      * @param categoryId
      * @param attributes
      * @param storeView
      * @return
      */
     public void updateCategory(int categoryId, @NotNull Map<String, Object> attributes, String storeView)
         throws RemoteException
     {
         Validate.notNull(attributes);
         getPort().catalogCategoryUpdate(getSessionId(), categoryId,
             fromMap(CatalogCategoryEntityCreate.class, attributes), storeView);
     }
 
     /**
      * 
      * @param categoryId
      * @param productId
      * @param position
      * @return
      * 
      */
     public void updateCategoryProduct(int categoryId,
                                       @NotNull ProductIdentifier productId,
                                       String position) throws RemoteException
     {
         getPort().catalogCategoryUpdateProduct(getSessionId(), categoryId, productId.getIdentifierAsString(), position,
             productId.getIdentifierType());
     }
 
     public Object[] listInventoryStockItems(List<String> products) throws RemoteException
     {
         return getPort().catalogInventoryStockItemList(getSessionId(), toArray(products, String.class));
     }
 
     /**
      * 
      * @param productId
      * @param attributes
      * @return
      * 
      */
     public void updateInventoryStockItem(@NotNull ProductIdentifier productId, @NotNull Map<String, Object> attributes)
         throws RemoteException
     {
         Validate.notNull(attributes);
         getPort().catalogInventoryStockItemUpdate(getSessionId(), productId.getIdentifierAsString(),
             fromMap(CatalogInventoryStockItemUpdateEntity.class, attributes));
     }
  
     
     /*Product*/
     
     /**
     * Creates a new product
     * 
     * @param type the new product's type
     * @param set the new product's set
     * @param sku the new product's sku
     * @param attributes the standard attributes of the new product
     * @param additionalAttributes the non standard attributes of the new product
     * @return the new product's id
     */
     public int createProduct(@NotNull String type,
                              @NotNull int set,
                              @NotNull String sku,
                              Map<String, Object> attributes,
                              Map<String, Object> additionalAttributes
                              , String storeView)
         throws RemoteException
     {
         Validate.notNull(type);
         Validate.notNull(set);
         Validate.notNull(sku);
         
         AssociativeEntity[] additional_attributes;
         CatalogProductCreateEntity productData = fromMap(CatalogProductCreateEntity.class, attributes);
 
         removeNullValues(additionalAttributes);
         if(additionalAttributes!=null && !additionalAttributes.isEmpty()){
             additional_attributes= fromMap(additionalAttributes);
             productData.setAdditional_attributes(additional_attributes);
         }
 
         return getPort().catalogProductCreate(getSessionId(), type, String.valueOf(set), sku,
             productData, storeView);
     }
 
 
     /**
      * Deletes a product.
      *  See catalog-product-delete SOAP method. 
      * 
      * @param productId the product identifier
      */
     public void deleteProduct(ProductIdentifier productId) throws RemoteException
     {
         getPort().catalogProductDelete(getSessionId(), 
             productId.getIdentifierAsString(),
             productId.getIdentifierType());
     }
 
     /**
      * Answers a product special price. 
      * 
      * See catalog-product-getSpecialPrice SOAP method.
      * @param product
      * @param storeView
      * @param productId.getIdentifierType()
      * @return
      * 
      */
     public Object getProductSpecialPrice(@NotNull ProductIdentifier productId, String storeView)
         throws RemoteException
     {
         return getPort().catalogProductGetSpecialPrice(getSessionId(), productId.getIdentifierAsString(), storeView,
             productId.getIdentifierType());
     }
 
     /**
      * Answers a product's attributes. See catalog-product-info SOAP method. 
      *
      * @param product
      * @param storeView
      * @param attributes
      * @param productId.getIdentifierType()
      * @return the product attributes
      */
     public Object getProduct(@NotNull ProductIdentifier productId,
                              String storeView,
                              List<String> attributeNames,
                              List<String> additionalAttributeNames) throws RemoteException
     {
         Validate.notNull(productId);
         Validate.isTrue(CollectionUtils.isNotEmpty(attributeNames)
                         || CollectionUtils.isNotEmpty(additionalAttributeNames));
         
         CatalogProductRequestAttributes request = 
             new CatalogProductRequestAttributes(
                 toArray(nullToEmpty(attributeNames), String.class), 
                 toArray(nullToEmpty(additionalAttributeNames), String.class));
         
         return getPort().catalogProductInfo(getSessionId(), productId.getIdentifierAsString(), storeView,
             request, productId.getIdentifierType());
     }
     
 
     /**
      * Retrieve products list by filters
      * See catalog-product-list SOAP method. 
      * @param filters an optional filtering expression
      * @param storeViewIdOrCode an optional storeView
      * @return the list of product attributes that match the given optional filtering expression
      */
     public Object[] listProducts(String filters, String storeView) throws RemoteException
     {
         return getPort().catalogProductList(getSessionId(), FiltersParser.parse(filters), storeView);
     }
 
     /**
      * Sets a product special price. See catalog-product-setSpecialPrice  SOAP method
      * 
      * @param product
      * @param specialPrice
      * @param fromDate
      * @param toDate
      * @param storeView
      * @param productId.getIdentifierType()
      * @return
      */
     public void updateProductSpecialPrice(@NotNull ProductIdentifier productId,
                                           @NotNull String specialPrice,
                                          String fromDate,
                                          String toDate,
                                          String storeView) throws RemoteException
     {
         Validate.notNull(specialPrice);
         Validate.notNull(productId);
         getPort().catalogProductSetSpecialPrice(getSessionId(), productId.getIdentifierAsString(), specialPrice, fromDate,
             toDate, storeView, productId.getIdentifierType());
     }
     
     /**
      * Updates a product. At least one of attributes or additionalAttributes 
      * must be non null and non empty. See catalog-category-updateProduct SOAP method
      * 
      * @param attributes the map of standard product attributes to update
      * @param additionalAttributes the map of non standard product attributes to update
      * @param storeViewIdOrCode optional store view
      */
     public void updateProduct(@NotNull ProductIdentifier productId,
                               Map<String, Object> attributes,
                               Map<String, Object> additionalAttributes,
                               String storeView) throws RemoteException
     {
         removeNullValues(attributes);
         removeNullValues(additionalAttributes);
 
         Validate.notNull(productId);
         Validate.isTrue( (attributes != null && !attributes.isEmpty() ) || 
                         (additionalAttributes!=null && !additionalAttributes.isEmpty()) );
 
         AssociativeEntity[] additional_attributes;
         CatalogProductCreateEntity productData = fromMap(CatalogProductCreateEntity.class, attributes);
 
         if(additionalAttributes!=null && !additionalAttributes.isEmpty()){
           additional_attributes= fromMap(additionalAttributes);
           productData.setAdditional_attributes(additional_attributes);
         }
         
         getPort().catalogProductUpdate(getSessionId(), productId.getIdentifierAsString(),
             productData, storeView, productId.getIdentifierType());
     }
     
     
     /*
      * Product Images
      */
     public String createProductAttributeMedia(@NotNull ProductIdentifier productId,
                                               Map<String, Object> attributes,
                                               @NotNull InputStream content,
                                               @NotNull MediaMimeType mimeType,
                                               @NotNull String baseFileName,
                                               String storeView) throws RemoteException
     {
         Validate.notNull(productId);
         Validate.notNull(mimeType);
         
         CatalogProductImageFileEntity file = new CatalogProductImageFileEntity(encodeStream(content), mimeType.toString(), baseFileName);
         CatalogProductAttributeMediaCreateEntity request = 
             fromMap(CatalogProductAttributeMediaCreateEntity.class, nullToEmpty(attributes));
         request.setFile(file);
         
         return getPort().catalogProductAttributeMediaCreate(getSessionId(), productId.getIdentifierAsString(),
             request, storeView,
             productId.getIdentifierType());
     }
 
    
     private String encodeStream(InputStream content)
     {
         try
         {
            return Base64.encodeBytes(IOUtils.toByteArray(content));
         }
         catch (IOException e)
         {
             throw new UnhandledException("Could not encode the stream", e);
         }
         finally
         {
             IOUtils.closeQuietly(content);
         }
     }
 
     public Object getProductAttributeMedia(@NotNull ProductIdentifier productId,
                                            @NotNull String file,
                                            String storeView
                                            ) throws RemoteException
     {
         return getPort().catalogProductAttributeMediaInfo(getSessionId(), productId.getIdentifierAsString(), file, storeView,
             productId.getIdentifierType());
     }
 
     public Object[] listProductAttributeMedia(@NotNull ProductIdentifier productId, String storeViewIdOrCode )
         throws RemoteException
     {
         return getPort().catalogProductAttributeMediaList(getSessionId(), productId.getIdentifierAsString(), storeViewIdOrCode,
             productId.getIdentifierType());
     }
 
     public void deleteProductAttributeMedia(@NotNull ProductIdentifier productId, @NotNull  String file)
         throws RemoteException
     {
         Validate.notNull(productId);
         Validate.notNull(file);
         getPort().catalogProductAttributeMediaRemove(getSessionId(), productId.getIdentifierAsString(), file,
             productId.getIdentifierType());
     }
 
     public Object[] listProductAttributeMediaTypes(int setId) throws RemoteException
     {
         return getPort().catalogProductAttributeMediaTypes(getSessionId(), String.valueOf(setId));
     }
 
     public void updateProductAttributeMedia(@NotNull ProductIdentifier productId,
                                             String fileName,
                                             @NotNull Map<String, Object> attributes,
                                             String storeView) throws RemoteException
     {
         Validate.notNull(attributes);
         getPort().catalogProductAttributeMediaUpdate(getSessionId(), productId.getIdentifierAsString(), fileName,
             fromMap(CatalogProductAttributeMediaCreateEntity.class, attributes), storeView,
             productId.getIdentifierType());
     }
 
     /*
      * Category Attributes
      */
 
     public Object[] listCategoryAttributes() throws RemoteException
     {
         return getPort().catalogCategoryAttributeList(getSessionId());
     }
 
     public Object[] listCategoryAttributeOptions(@NotNull String attributeId, String storeView)
         throws RemoteException
     {
         Validate.notNull(attributeId);
         return getPort().catalogCategoryAttributeOptions(getSessionId(), attributeId, storeView);
     }
 
     /* Product Attribute */
 
     public Object[] listProductAttributes(int setId) throws RemoteException
     {
         return getPort().catalogProductAttributeList(getSessionId(), setId);
     }
 
     @NotNull
     public Object[] listProductAttributeOptions(@NotNull String attributeId, String storeView)
         throws RemoteException
     {
         Validate.notNull(attributeId);
         return getPort().catalogProductAttributeOptions(getSessionId(), attributeId, storeView);
     }
 
     @NotNull
     public Object[] listProductAttributeSets() throws RemoteException
     {
         return getPort().catalogProductAttributeSetList(getSessionId());
     }
 
     /* Product Type */
 
     @NotNull
     public Object[] listProductTypes() throws RemoteException
     {
         return getPort().catalogProductTypeList(getSessionId());
     }
 
     /* Product Tier Price */
 
     @NotNull
     public Object[] listProductAttributeTierPrices(@NotNull ProductIdentifier productId
                                                    )
         throws RemoteException
     {
         Validate.notNull(productId);
         
         return getPort().catalogProductAttributeTierPriceInfo(getSessionId(), productId.getIdentifierAsString(), productId.getIdentifierType());
     }
 
     public void updateProductAttributeTierPrice(@NotNull ProductIdentifier productId,
                                                  @NotNull Map<String, Object> attributes)
         throws RemoteException
     {
         Validate.notNull(productId);
         Validate.notNull(attributes);
         
         getPort().catalogProductAttributeTierPriceUpdate(getSessionId(), 
             productId.getIdentifierAsString(),
             new CatalogProductTierPriceEntity[]{
                 MagentoObject.fromMap(CatalogProductTierPriceEntity.class, attributes)},
             productId.getIdentifierType());
     }
 
     /* h. Product Link (related, cross sells, up sells, grouped) */
 
     public void addProductLink(@NotNull String type,
                                     @NotNull ProductIdentifier productId,
                                     @NotNull String linkedProductIdOrSku,
                                     Map<String, Object> attributes) throws RemoteException
     {
         Validate.notNull(type);
         Validate.notNull(productId);
         Validate.notNull(linkedProductIdOrSku);
         getPort().catalogProductLinkAssign(getSessionId(), type, productId.getIdentifierAsString(), linkedProductIdOrSku,
             fromMap(CatalogProductLinkEntity.class, attributes), productId.getIdentifierType());
     }
 
     public Object[] listProductLinkAttributes(String type) throws RemoteException
     {
         return getPort().catalogProductLinkAttributes(getSessionId(), type);
     }
 
     public Object[] listProductLink(@NotNull String type,
                                     @NotNull ProductIdentifier productId) throws RemoteException
     {
         return getPort().catalogProductLinkList(getSessionId(), type, productId.getIdentifierAsString(), productId.getIdentifierType());
     }
 
     public void deleteProductLink(@NotNull String type,
                                     @NotNull ProductIdentifier productId,
                                     @NotNull String linkedProductIdOrSku) throws RemoteException
     {
         getPort().catalogProductLinkRemove(getSessionId(), type, productId.getIdentifierAsString(), linkedProductIdOrSku,
             productId.getIdentifierType());
     }
 
     public List<String> listProductLinkTypes() throws RemoteException
     {
         return Arrays.asList(getPort().catalogProductLinkTypes(getSessionId()));
     }
 
     public void updateProductLink(@NotNull String type,
                                     @NotNull ProductIdentifier productId,
                                     @NotNull String linkedProduct,
                                     @NotNull Map<String, Object> attributes) throws RemoteException
     {
         Validate.notNull(attributes);
         Validate.notNull(type);
         Validate.notNull(linkedProduct);
 
         getPort().catalogProductLinkUpdate(getSessionId(), type, productId.getIdentifierAsString(), linkedProduct,
             fromMap(CatalogProductLinkEntity.class, attributes), productId.getIdentifierType());
     }
 }
