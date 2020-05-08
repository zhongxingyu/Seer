 /*******************************************************************************
  * Copyright (c) 2012 David Harrison, Triptech Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     David Harrison, Triptech Ltd - initial API and implementation
  ******************************************************************************/
 package com.sfs.metahive.model;
 
 import java.text.DecimalFormat;
import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OrderBy;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.TypedQuery;
 import javax.validation.constraints.NotNull;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Index;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import com.sfs.metahive.web.model.KeyValueForm;
 
 
 /**
  * The Class KeyValue.
  */
 @RooJavaBean
 @RooToString
 @RooEntity
 public class KeyValue {
 
     /** The key value type. */
     @NotNull
     @Enumerated(EnumType.STRING)
     private KeyValueType keyValueType;
 
     /** The record. */
     @ManyToOne
     @NotNull
     private Record record;
 
     /** The definition. */
     @ManyToOne
     @NotNull
     @Index(name="indexDefinition")
     private Definition definition;
 
     /** The primary record id. */
     @NotNull
     @Index(name="indexPrimaryRecordId")
     private String primaryRecordId;
 
     /** The secondary record id. */
     @Index(name="indexSecondaryRecordId")
     private String secondaryRecordId;
 
     /** The tertiary record id. */
     @Index(name="indexTertiaryRecordId")
     private String tertiaryRecordId;
 
     /** The submitted field count. */
     private int submittedFieldCount;
 
     /** The submitted fields that contributed to this key value. */
     @OrderBy("created DESC")
 
     /** The string value. */
     @Index(name="indexStringValue")
     private String stringValue;
 
     /** The double value. */
     @Index(name="indexDoubleValue")
     private Double doubleValue;
 
     /** The boolean value. */
     @Index(name="indexBooleanValue")
     @Enumerated(EnumType.STRING)
     private KeyValueBoolean booleanValue;
 
     /** The comment about the key value. */
     @Lob
     private String comment;
 
     /** The user who overrode the key value. */
     @ManyToOne
     private Person overriddenBy;
 
     /** The modified timestamp. */
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "modified", nullable = false)
     private Date modified;
 
     /** The user role. */
     @Transient
     private UserRole userRole;
 
     /** The submitted fields. */
     @Transient
     private List<SubmittedField> submittedFields = new ArrayList<SubmittedField>();
 
     /** The application context. */
     @Transient
     private ApplicationContext context;
 
 
      @PrePersist
      @PreUpdate
      protected void preCreateOrUpdate() {
     	 if (doubleValue != null) {
     		 if (doubleValue == Double.NaN
     				 || doubleValue == Double.POSITIVE_INFINITY
     				 || doubleValue == Double.NEGATIVE_INFINITY) {
     			 doubleValue = null;
     		 }
          }
          // Update the modified date
          modified = new Date();
      }
 
     /**
      * Gets the css class.
      *
      * @return the css class
      */
     public String getCssClass() {
         String value = "keyValueNumber";
 
         if (this.getDefinition() != null && this.getDefinition().getDataType() != null) {
             value = this.getDefinition().getDataType().getCssClass();
         }
         return value;
     }
 
     /**
      * Sets the value.
      *
      * @param value the new value as an object
      */
     public void setValue(final Object value) {
 
         // Reset the values
         this.doubleValue = null;
         this.stringValue = null;
         this.booleanValue = null;
 
         if (value != null && value instanceof String) {
             this.setStringValue((String) value);
         }
         if (value != null && value instanceof Double) {
             this.setDoubleValue((Double) value);
         }
         if (value != null && value instanceof KeyValueBoolean) {
             this.setBooleanValue((KeyValueBoolean) value);
         }
      }
 
 
     /**
      * Gets the value including the unit of measure.
      *
      * @return the value
      */
     public final String getValue() {
         return getValue(true);
     }
 
     /**
      * Gets the value sans the units of measure.
      *
      * @return the value sans units
      */
     public final String getValueSansUnits() {
         return getValue(false);
     }
 
 
     /**
      * Checks for no data in the key value.
      *
      * @return true, if successful
      */
     public final boolean hasNoData() {
         boolean hasNoData = true;
 
         if (this.getDefinition().getDataType() == DataType.TYPE_STRING) {
             if (this.getStringValue() != null) {
                 hasNoData = false;
             }
         }
         if (this.getDefinition().getDataType() == DataType.TYPE_BOOLEAN) {
             if (this.getBooleanValue() != null) {
                 hasNoData = false;
             }
         }
         if (this.getDefinition().getDataType() == DataType.TYPE_NUMBER
                 || this.getDefinition().getDataType() == DataType.TYPE_CURRENCY
                 || this.getDefinition().getDataType() == DataType.TYPE_PERCENTAGE) {
             if (this.getDoubleValue() != null) {
                 hasNoData = false;
             }
         }
         return hasNoData;
     }
 
     /**
      * Update relevant key values based on the supplied form data.
      * Returns a list of updated key values.
      *
      * @param id the id
      * @param kvForm the kv form
      * @param user the user
      * @return the list
      */
     public static final List<KeyValue> updateRelevantKeyValues(final Long id,
     		final KeyValueForm kvForm, final Person user) {
 
     	List<KeyValue> updatedKeyValues = new ArrayList<KeyValue>();
 
     	KeyValue keyValue = KeyValue.findKeyValue(id);
     	DataType dt = keyValue.getDefinition().getDataType();
 
     	List<KeyValue> relatedKeyValues = KeyValue.findRelatedKeyValues(keyValue);
 
     	for (KeyValue kv : relatedKeyValues) {
 
     		boolean keyValueChanged = false;
 
     		kv.setComment(kvForm.getTrimmedOverrideComment());
 
     		if (kvForm.isOverridden()) {
     			// Key value is overridden
     			kv.setKeyValueType(KeyValueType.OVERRIDDEN);
     			kv.setValue(KeyValueGenerator.parseValue(dt, kvForm.getOverrideValue()));
     			kv.setOverriddenBy(user);
 
     			keyValueChanged = true;
     		}
 
     		if (kv.getKeyValueType() == KeyValueType.OVERRIDDEN
     				&& !kvForm.isOverridden()) {
     			// Key value changed to being not overridden
     			kv.setKeyValueType(KeyValueType.CALCULATED);
     			kv.setOverriddenBy(null);
 
     			keyValueChanged = true;
     		}
     		// Save the key value
     		kv.merge();
     		kv.flush();
 
     		if (keyValueChanged) {
     			updatedKeyValues.add(kv);
     		}
     	}
         return updatedKeyValues;
     }
 
 
     /**
      * Find key values for the supplied Record.
      *
      * @param record the record
      * @return the key value
      */
     public static List<KeyValue> findKeyValues(final Record record) {
 
         List<KeyValue> keyValues = new ArrayList<KeyValue>();
 
         if (record == null) {
             throw new IllegalArgumentException("A valid record is required");
         }
 
         StringBuilder sql = new StringBuilder();
 
         sql.append("SELECT k FROM KeyValue AS k JOIN k.record r");
         sql.append(" WHERE r.id = :recordId");
 
         TypedQuery<KeyValue> q = entityManager().createQuery(
                 sql.toString(), KeyValue.class);
 
         q.setParameter("recordId", record.getId());
 
         if (q.getResultList() != null) {
             for (KeyValue keyValue : q.getResultList()) {
                 keyValues.add(keyValue);
             }
         }
         return keyValues;
     }
 
     /**
      * Find key values for the supplied Record.
      *
      * @param record the record
      * @param definitions the list of definitions to lookup
      * @return the key value
      */
     public static List<KeyValue> findKeyValues(final Record record,
             final List<Definition> definitions) {
 
         List<KeyValue> keyValues = new ArrayList<KeyValue>();
 
         if (record == null) {
             throw new IllegalArgumentException("A valid record is required");
         }
 
         if (definitions != null && definitions.size() > 0) {
 
             StringBuilder sql = new StringBuilder();
             StringBuilder where = new StringBuilder();
 
             sql.append("SELECT k FROM KeyValue AS k JOIN k.record r");
             sql.append(" LEFT JOIN k.definition d WHERE r.id = :recordId");
 
             sql.append(" AND (");
             for (Definition definition : definitions) {
                 if (where.length() > 0) {
                     where.append(" OR ");
                 }
                 where.append("d.id = ");
                 where.append(definition.getId());
             }
             sql.append(where.toString());
             sql.append(")");
 
             TypedQuery<KeyValue> q = entityManager().createQuery(
                     sql.toString(), KeyValue.class);
 
             q.setParameter("recordId", record.getId());
 
             if (q.getResultList() != null) {
                 for (KeyValue keyValue : q.getResultList()) {
                     keyValues.add(keyValue);
                 }
             }
         }
         return keyValues;
     }
 
     /**
      * Find key value based on its id values.
      *
      * @param def the definition
      * @param primaryId the primary record id
      * @param secondaryId the secondary record id
      * @param tertiaryId the tertiary record id
      * @return the key value
      */
     public static KeyValue findKeyValue(final Definition def,
             final String primaryId, final String secondaryId, final String tertiaryId) {
 
         KeyValue keyValue = null;
 
         if (def == null) {
             throw new IllegalArgumentException("A valid defintion is required");
         }
         if (StringUtils.isBlank(primaryId)) {
             throw new IllegalArgumentException("The primaryId argument is required");
         }
 
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT k FROM KeyValue AS k JOIN k.definition d");
         sql.append(" WHERE d.id = :definitionId AND");
         sql.append(" LOWER(k.primaryRecordId) = LOWER(:primaryRecordId)");
         sql.append(" AND LOWER(k.secondaryRecordId) = LOWER(:secondaryRecordId)");
         sql.append(" AND LOWER(k.tertiaryRecordId) = LOWER(:tertiaryRecordId)");
 
         TypedQuery<KeyValue> q = entityManager().createQuery(sql.toString(),
                 KeyValue.class);
         q.setParameter("definitionId", def.getId());
         q.setParameter("primaryRecordId", primaryId);
         q.setParameter("secondaryRecordId", secondaryId);
         q.setParameter("tertiaryRecordId", tertiaryId);
 
         List<KeyValue> keyValues = q.getResultList();
 
         if (keyValues != null && keyValues.size() > 0) {
             keyValue = keyValues.get(0);
         }
         return keyValue;
     }
 
     /**
      * Find the related key values based on the supplied key value.
      *
      * @param def the definition
      * @param primaryId the primary record id
      * @param secondaryId the secondary record id
      * @param tertiaryId the tertiary record id
      * @return the key value
      */
     public static List<KeyValue> findRelatedKeyValues(final KeyValue keyValue) {
 
     	List<KeyValue> relatedKeyValues = new ArrayList<KeyValue>();
 
         if (keyValue == null) {
             throw new IllegalArgumentException("A valid key value is required");
         }
 
         StringBuilder sql = new StringBuilder();
         HashMap<String, Object> variables = new HashMap<String, Object>();
 
         sql.append("SELECT k FROM KeyValue AS k LEFT JOIN k.definition d");
         sql.append(" WHERE d.id = :definition AND k.primaryRecordId = :primary");
 
         variables.put("definition", keyValue.getDefinition().getId());
         variables.put("primary", keyValue.getPrimaryRecordId());
 
         Applicability applicability = keyValue.getDefinition().getApplicability();
 
         if (applicability == Applicability.RECORD_SECONDARY) {
         	sql.append(" AND k.secondaryRecordId = :secondary");
         	variables.put("secondary", keyValue.getSecondaryRecordId());
         }
         if (applicability == Applicability.RECORD_TERTIARY) {
         	sql.append(" AND k.tertiaryRecordId = :tertiary");
         	variables.put("tertiary", keyValue.getTertiaryRecordId());
         }
 
         TypedQuery<KeyValue> q = entityManager().createQuery(
                 sql.toString(), KeyValue.class);
 
         for (String key : variables.keySet()) {
             q.setParameter(key, variables.get(key));
         }
 
         if (q.getResultList() != null) {
             for (KeyValue kv : q.getResultList()) {
             	relatedKeyValues.add(kv);
             }
         }
         System.out.println("Values: " + relatedKeyValues.size());
 
         return relatedKeyValues;
     }
 
 
     /**
      * Gets the formatted value.
      *
      * @param includeUnits the include units flag
      * @return the value
      */
     private String getValue(final boolean includeUnits) {
 
         if (this.getDefinition() == null) {
             throw new NullPointerException("A valid definition is required");
         }
         if (this.getContext() == null) {
             throw new NullPointerException("A valid application context is required");
         }
 
         // The default value is authorisation required
         String value = context.getMessage(
                 "label_com_sfs_metahive_model_keyvalue_access_restricted",
                 null, LocaleContextHolder.getLocale());
 
         if (UserRole.allowAccess(this.getUserRole(), definition.getKeyValueAccess())) {
             value = context.getMessage("label_com_sfs_metahive_model_keyvalue_no_data",
                     null, LocaleContextHolder.getLocale());
 
             if (this.getDefinition().getDataType() == DataType.TYPE_STRING) {
                 if (this.getStringValue() != null) {
                     value = this.getStringValue();
                     if (includeUnits) {
                         value += appendUnitOfMeasure();
                     }
                 }
             }
             if (this.getDefinition().getDataType() == DataType.TYPE_BOOLEAN) {
                 if (this.getBooleanValue() != null && this.getContext() != null) {
                     value = context.getMessage(this.getBooleanValue().getMessageKey(),
                             null, LocaleContextHolder.getLocale());
                 }
             }
            if (this.getDefinition().getDataType() == DataType.TYPE_NUMBER) {
                 if (this.getDoubleValue() != null) {
                     DecimalFormat df = new DecimalFormat("#.######");
                     value = df.format(this.getDoubleValue());
                     if (StringUtils.endsWithIgnoreCase(value, ".000000")) {
                         value = StringUtils.substring(value, 0, value.length() -6);
                     }
                     if (includeUnits) {
                         value += appendUnitOfMeasure();
                     }
                 }
             }
             if (this.getDefinition().getDataType() == DataType.TYPE_CURRENCY) {
                 if (this.getDoubleValue() != null) {
                     DecimalFormat df = new DecimalFormat("$###,###,###,##0.00");
                     value = df.format(this.getDoubleValue()) + appendUnitOfMeasure();
                 }
             }
            if (this.getDefinition().getDataType() == DataType.TYPE_PERCENTAGE) {
                if (this.getDoubleValue() != null) {
                    NumberFormat percentFormatter = NumberFormat.getPercentInstance(
                            LocaleContextHolder.getLocale());
                    value = percentFormatter.format(this.getDoubleValue());
                    if (includeUnits) {
                        value += appendUnitOfMeasure();
                    }
                }
            }
         }
         return value;
     }
 
     /**
      * Append unit of measure.
      *
      * @return the string
      */
     private String appendUnitOfMeasure() {
 
         String value = "";
 
         if (this.getDefinition() != null
                 && this.getDefinition().getDescription() != null) {
 
             String unitOfMeasure = this.getDefinition().getDescription()
                     .getUnitOfMeasure();
 
             if (StringUtils.isNotBlank(unitOfMeasure)) {
                 value += " " + StringUtils.trim(unitOfMeasure);
             }
         }
         return value;
     }
 
 }
