 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.item.db;
 
 import com.sapienter.jbilling.common.CommonConstants;
 import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
 import com.sapienter.jbilling.server.metafields.MetaContent;
 import com.sapienter.jbilling.server.metafields.MetaFieldHelper;
 import com.sapienter.jbilling.server.metafields.db.EntityType;
 import com.sapienter.jbilling.server.metafields.db.MetaFieldValue;
 import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.pricing.PriceModelBL;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.csv.Exportable;
 import com.sapienter.jbilling.server.util.db.AbstractDescription;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.MapKey;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 @Entity
 @TableGenerator(
         name = "item_GEN",
         table = "jbilling_seqs",
         pkColumnName = "name",
         valueColumnName = "next_id",
         pkColumnValue = "item",
         allocationSize = 100
 )
 @Table(name = "item")
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class ItemDTO extends AbstractDescription implements MetaContent, Exportable {
 
     private int id;
     private CompanyDTO entity;
     private String internalNumber;
     private String glCode;
     private SortedMap<Date, PriceModelDTO> defaultPrices = new TreeMap<Date, PriceModelDTO>();
     private BigDecimal percentage;
     private Set<ItemTypeDTO> excludedTypes = new HashSet<ItemTypeDTO>();
     private Integer deleted;
     private Integer hasDecimals;
     private Set<OrderLineDTO> orderLineDTOs = new HashSet<OrderLineDTO>(0);
     private Set<ItemTypeDTO> itemTypes = new HashSet<ItemTypeDTO>(0);
     private Set<InvoiceLineDTO> invoiceLines = new HashSet<InvoiceLineDTO>(0);
     private Set<PlanDTO> plans = new HashSet<PlanDTO>(0);
     private List<MetaFieldValue> metaFields = new LinkedList<MetaFieldValue>();
 
     private int versionNum;
 
     // transient
     private Integer[] types = null;
     private Integer[] excludedTypeIds = null;
     private Collection<String> strTypes = null; // for rules 'contains' operator
     private String promoCode = null;
     private Integer currencyId = null;
     private BigDecimal price = null;
     private Integer orderLineTypeId = null;
 
 
     public ItemDTO() {
     }
 
     public ItemDTO(int id) {
         this.id = id;
     }
 
     public ItemDTO(int id, String internalNumber, String glCode,BigDecimal percentage,
                    Integer hasDecimals, Integer deleted, CompanyDTO entity) {
         this.id = id;
         this.internalNumber = internalNumber;
         this.glCode = glCode;
         this.percentage = percentage;
         this.hasDecimals = hasDecimals;
         this.deleted = deleted;
         this.entity = entity;
     }
 
     public ItemDTO(int id, Integer deleted, Integer hasDecimals) {
         this.id = id;
         this.deleted = deleted;
         this.hasDecimals = hasDecimals;
     }
 
     public ItemDTO(int id, CompanyDTO entity, String internalNumber, String glCode, BigDecimal percentage,
                    Integer deleted, Integer hasDecimals, Set<OrderLineDTO> orderLineDTOs, Set<ItemTypeDTO> itemTypes,
                    Set<InvoiceLineDTO> invoiceLines) {
         this.id = id;
         this.entity = entity;
         this.internalNumber = internalNumber;
         this.glCode = glCode;
         this.percentage = percentage;
         this.deleted = deleted;
         this.hasDecimals = hasDecimals;
         this.orderLineDTOs = orderLineDTOs;
         this.itemTypes = itemTypes;
         this.invoiceLines = invoiceLines;
     }
 
     // ItemDTOEx
     public ItemDTO(int id, String number, String glCode, CompanyDTO entity, String description, Integer deleted,
                    Integer currencyId, BigDecimal price, BigDecimal percentage, Integer orderLineTypeId,
                    Integer hasDecimals) {
 
         this(id, number, glCode, percentage, hasDecimals, deleted, entity);
         setDescription(description);
         setCurrencyId(currencyId);
         setOrderLineTypeId(orderLineTypeId);
     }
 
 
     @Transient
     protected String getTable() {
         return Constants.TABLE_ITEM;
     }
 
     @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "item_GEN")
     @Column(name = "id", unique = true, nullable = false)
     public int getId() {
         return this.id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "entity_id")
     public CompanyDTO getEntity() {
         return this.entity;
     }
 
     public void setEntity(CompanyDTO entity) {
         this.entity = entity;
     }
 
     @Column(name = "internal_number", length = 50)
     public String getInternalNumber() {
         return this.internalNumber;
     }
 
     public void setInternalNumber(String internalNumber) {
         this.internalNumber = internalNumber;
     }
 
     @Column (name = "gl_code", length = 50)
     public String getGlCode() {
 		return glCode;
 	}
 
 	public void setGlCode(String glCode) {
 		this.glCode = glCode;
 	}
 
     @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     @MapKey(columns = @Column(name = "start_date", nullable = true))
     @JoinTable(name = "item_price_timeline",
                joinColumns = {@JoinColumn(name = "item_id", updatable = false)},
                inverseJoinColumns = {@JoinColumn(name = "price_model_id", updatable = false)}
     )
     @Sort(type = SortType.NATURAL)
     @Fetch(FetchMode.SELECT)
     public SortedMap<Date, PriceModelDTO> getDefaultPrices() {
         return defaultPrices;
     }
 
     public void setDefaultPrices(SortedMap<Date, PriceModelDTO> defaultPrices) {
         this.defaultPrices = defaultPrices;
     }
 
     /**
      * Adds a new price to the default pricing list. If no date is given, then the
      * price it is assumed to be the start of a new time-line and the date will be
      * forced to 01-Jan-1970 (epoch).
      *
      * @param date date for the given price
      * @param price price
      */
     public void addDefaultPrice(Date date, PriceModelDTO price) {
         getDefaultPrices().put(date != null ? date : CommonConstants.EPOCH_DATE, price);
     }
 
     @Transient
     public PriceModelDTO getPrice(Date today) {
         return PriceModelBL.getPriceForDate(defaultPrices, today);
     }
 
     @Column(name = "percentage")
     public BigDecimal getPercentage() {
         return this.percentage;
     }
 
     public void setPercentage(BigDecimal percentage) {
         this.percentage = percentage;
     }
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinTable(name = "item_type_exclude_map",
                joinColumns = {@JoinColumn(name = "item_id", updatable = false)},
                inverseJoinColumns = {@JoinColumn(name = "type_id", updatable = false)}
     )
     public Set<ItemTypeDTO> getExcludedTypes() {
         return excludedTypes;
     }
 
     public void setExcludedTypes(Set<ItemTypeDTO> excludedTypes) {
         this.excludedTypes = excludedTypes;
     }
 
     @Column(name = "deleted", nullable = false)
     public Integer getDeleted() {
         return this.deleted;
     }
 
     public void setDeleted(Integer deleted) {
         this.deleted = deleted;
     }
 
     @Column(name = "has_decimals", nullable = false)
     public Integer getHasDecimals() {
         return this.hasDecimals;
     }
 
     public void setHasDecimals(Integer hasDecimals) {
         this.hasDecimals = hasDecimals;
     }
 
 
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
     public Set<OrderLineDTO> getOrderLines() {
         return this.orderLineDTOs;
     }
 
     public void setOrderLines(Set<OrderLineDTO> orderLineDTOs) {
         this.orderLineDTOs = orderLineDTOs;
     }
 
     @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinTable(name = "item_type_map",
                joinColumns = {@JoinColumn(name = "item_id", updatable = false)},
                inverseJoinColumns = {@JoinColumn(name = "type_id", updatable = false)}
     )
     public Set<ItemTypeDTO> getItemTypes() {
         return this.itemTypes;
     }
 
     public void setItemTypes(Set<ItemTypeDTO> itemTypes) {
         this.itemTypes = itemTypes;
     }
 
     /**
      * Strips the given prefix off of item categories and returns the resulting code. This method allows categories to
      * be used to hold identifiers and other meta-data.
      * <p/>
      * Example: item = ItemDTO{ type : ["JB_123"] } item.getCategoryCode("JB") -> "123"
      *
      * @param prefix prefix of the category code to retrieve
      * @return code minus the given prefix
      */
     public String getCategoryCode(String prefix) {
         for (ItemTypeDTO type : getItemTypes())
             if (type.getDescription().startsWith(prefix))
                 return type.getDescription().replaceAll(prefix, "");
         return null;
     }
 
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
     public Set<InvoiceLineDTO> getInvoiceLines() {
         return this.invoiceLines;
     }
 
     public void setInvoiceLines(Set<InvoiceLineDTO> invoiceLines) {
         this.invoiceLines = invoiceLines;
     }
 
     /**
      * List of all plans that use this item as the "plan subscription" item.
      *
      * @return plans
      */
     @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "item")
     public Set<PlanDTO> getPlans() {
         return plans;
     }
 
     public void setPlans(Set<PlanDTO> plans) {
         this.plans = plans;
     }
 
     @Version
     @Column(name = "OPTLOCK")
     public int getVersionNum() {
         return versionNum;
     }
 
     public void setVersionNum(int versionNum) {
         this.versionNum = versionNum;
     }
 
     @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
     @JoinTable(
             name = "item_meta_field_map",
             joinColumns = @JoinColumn(name = "item_id"),
             inverseJoinColumns = @JoinColumn(name = "meta_field_value_id")
     )
     @Sort(type = SortType.COMPARATOR, comparator = MetaFieldHelper.MetaFieldValuesOrderComparator.class)
     public List<MetaFieldValue> getMetaFields() {
         return metaFields;
     }
 
     @Transient
     public void setMetaFields(List<MetaFieldValue> fields) {
         this.metaFields = fields;
     }
 
     @Transient
     public MetaFieldValue getMetaField(String name) {
         return MetaFieldHelper.getMetaField(this, name);
     }
 
     @Transient
     public MetaFieldValue getMetaField(Integer metaFieldNameId) {
         return MetaFieldHelper.getMetaField(this, metaFieldNameId);
     }
 
     @Transient
     public void setMetaField(MetaFieldValue field) {
         MetaFieldHelper.setMetaField(this, field);
     }
 
     @Transient
     public void setMetaField(Integer entitId, String name, Object value) throws IllegalArgumentException {
         MetaFieldHelper.setMetaField(entitId, this, name, value);
     }
 
     @Transient
     public void updateMetaFieldsWithValidation(Integer entitId, MetaContent dto) {
         MetaFieldHelper.updateMetaFieldsWithValidation(entitId, this, dto);
     }
 
     @Transient
     public EntityType getCustomizedEntityType() {
        return EntityType.ITEM;
     }
 
     @Transient
     public String getNumber() {
         return getInternalNumber();
     }
 
     @Transient
     public void setNumber(String number) {
         setInternalNumber(number);
     }
 
     /*
         Transient fields
      */
 
     @Transient
     public Integer[] getTypes() {
         if (this.types == null && itemTypes != null) {
             Integer[] types = new Integer[itemTypes.size()];
             int i = 0;
             for (ItemTypeDTO type : itemTypes) {
                 types[i++] = type.getId();
             }
             setTypes(types);
         }
         return types;
     }
 
     @Transient
     public void setTypes(Integer[] types) {
         this.types = types;
 
         strTypes = new ArrayList<String>(types.length);
         for (Integer i : types) {
             strTypes.add(i.toString());
         }
     }
 
     public boolean hasType(Integer typeId) {
         return Arrays.asList(getTypes()).contains(typeId);
     }
 
     @Transient
     public Integer[] getExcludedTypeIds() {
         if (this.excludedTypeIds == null && excludedTypes != null) {
             Integer[] types = new Integer[excludedTypes.size()];
             int i = 0;
             for (ItemTypeDTO type : excludedTypes) {
                 types[i++] = type.getId();
             }
             setExcludedTypeIds(types);
         }
         return excludedTypeIds;
     }
 
     @Transient
     public void setExcludedTypeIds(Integer[] types) {
         this.excludedTypeIds = types;
     }
 
     public boolean hasExcludedType(Integer typeId) {
         return Arrays.asList(getExcludedTypeIds()).contains(typeId);
     }
 
 
     /**
      * Rules 'contains' operator only works on a collections of strings
      * @return collection of ItemTypeDTO ID's as strings.
      */
     @Transient
     public Collection<String> getStrTypes() {
         if (strTypes == null && itemTypes != null) {
             strTypes = new ArrayList<String>(itemTypes.size());
             for (ItemTypeDTO type : itemTypes)
                 strTypes.add(String.valueOf(type.getId()));
         }
 
         return strTypes;
     }
 
     @Transient
     public String getPromoCode() {
         return promoCode;
     }
 
 
     @Transient
     public void setPromoCode(String string) {
         promoCode = string;
     }
 
     @Transient
     public Integer getEntityId() {
         return getEntity().getId();
     }
 
     @Transient
     public Integer getOrderLineTypeId() {
         return orderLineTypeId;
     }
 
     @Transient
     public void setOrderLineTypeId(Integer typeId) {
         orderLineTypeId = typeId;
     }
 
     @Transient
     public Integer getCurrencyId() {
         return currencyId;
     }
 
     @Transient
     public void setCurrencyId(Integer currencyId) {
         this.currencyId = currencyId;
     }
 
     @Transient
     public BigDecimal getPrice() {
         return price;
     }
 
     @Transient
     public void setPrice(BigDecimal price) {
         this.price = price;
     }
 
     @Override
     public String toString() {
         return "ItemDTO: id=" + getId();
     }
 
     @Transient
     public String[] getFieldNames() {
         return new String[] {
                 "id",
                 "productCode",
                 "itemTypes",
                 "hasDecimals",
                 "percentage",
                 "priceStrategy",
                 "currency",
                 "rate",
                 "attributes"
         };
     }
 
     @Transient
     public Object[][] getFieldValues() {
         StringBuilder itemTypes = new StringBuilder();
         for (ItemTypeDTO type : this.itemTypes) {
             itemTypes.append(type.getDescription()).append(" ");
         }
 
         PriceModelDTO currentPrice = getPrice(new Date());
 
         return new Object[][] {
             {
                 id,
                 internalNumber,
                 itemTypes.toString(),
                 hasDecimals,
                 percentage,
                 (currentPrice != null ? currentPrice.getType().name() : null),
                 (currentPrice != null && currentPrice.getCurrency() != null ? currentPrice.getCurrency().getDescription()
                                                                             : null),
 
                 (currentPrice != null ? currentPrice.getRate() : null),
                 (currentPrice != null ? currentPrice.getAttributes() : null),
             }
         };
     }
 
 }
 
 
