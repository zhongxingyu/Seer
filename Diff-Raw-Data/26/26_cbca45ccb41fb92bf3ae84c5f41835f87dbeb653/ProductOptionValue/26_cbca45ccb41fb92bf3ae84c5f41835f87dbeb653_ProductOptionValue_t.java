 package com.salesmanager.core.business.catalog.product.model.attribute;
 
 import java.util.HashSet;
 import java.util.Set;
 
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
 
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.business.merchant.model.MerchantStore;
 import com.salesmanager.core.constants.SchemaConstant;
 
 
 @Entity
 @Table(name="PRODUCT_OPTION_VALUE", schema=SchemaConstant.SALESMANAGER_SCHEMA)
 public class ProductOptionValue extends SalesManagerEntity<Long, ProductOptionValue> {
 	private static final long serialVersionUID = 3736085877929910891L;
 
 	@Id
 	@Column(name="PRODUCT_OPTION_VALUE_ID")
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPT_VAL_SEQ_NEXT_VAL")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Long id;
 	
 	@Column(name="PRODUCT_OPT_VAL_SORT_ORD")
 	private Integer productOptionValueSortOrder;
 	
 	@Column(name="PRODUCT_OPT_VAL_IMAGE")
 	private String productOptionValueImage;
 	
 	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productOptionValue")
 	private Set<ProductOptionValueDescription> descriptions = new HashSet<ProductOptionValueDescription>();
 	
/*	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
 	@JoinTable(name = "MERCHANT_PRD_OPT_VAL", schema=SchemaConstant.SALESMANAGER_SCHEMA, joinColumns = { 
 			@JoinColumn(name = "PRODUCT_OPTION_VALUE_ID", nullable = false) }, 
 			inverseJoinColumns = { @JoinColumn(name = "MERCHANT_ID", 
 					nullable = false) })
	private Set<MerchantStore> stores = new HashSet<MerchantStore>();*/
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MERCHANT_ID", nullable=false)
	private MerchantStore merchantSore;
 	
 	public ProductOptionValue() {
 	}
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Integer getProductOptionValueSortOrder() {
 		return productOptionValueSortOrder;
 	}
 
 	public void setProductOptionValueSortOrder(Integer productOptionValueSortOrder) {
 		this.productOptionValueSortOrder = productOptionValueSortOrder;
 	}
 
 	public String getProductOptionValueImage() {
 		return productOptionValueImage;
 	}
 
 	public void setProductOptionValueImage(String productOptionValueImage) {
 		this.productOptionValueImage = productOptionValueImage;
 	}
 
 	public Set<ProductOptionValueDescription> getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(Set<ProductOptionValueDescription> descriptions) {
 		this.descriptions = descriptions;
 	}
 
	public MerchantStore getMerchantSore() {
		return merchantSore;
 	}
 
	public void setMerchantSore(MerchantStore merchantSore) {
		this.merchantSore = merchantSore;
 	}
 
 
 


 }
