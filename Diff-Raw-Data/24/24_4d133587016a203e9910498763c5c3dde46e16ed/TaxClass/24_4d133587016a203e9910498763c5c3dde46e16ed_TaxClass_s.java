 package com.salesmanager.core.business.tax.model.taxclass;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
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
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 
 import com.salesmanager.core.business.catalog.product.model.Product;
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.business.merchant.model.MerchantStore;
 import com.salesmanager.core.business.tax.model.taxrate.TaxRate;
 import com.salesmanager.core.constants.SchemaConstant;
 
 @Entity
 @Table(name = "TAX_CLASS", schema = SchemaConstant.SALESMANAGER_SCHEMA)
 public class TaxClass extends SalesManagerEntity<Long, TaxClass> {
 	private static final long serialVersionUID = -325750148480212355L;
 	
 	@Id
 	@Column(name = "TAX_CLASS_ID", unique=true, nullable=false)
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TX_CLASS_SEQ_NEXT_VAL")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Long id;
 	
 	@Column(name="TAX_CLASS_CODE", length=10)
 	private String code;
 	
 	@Column(name = "TAX_CLASS_TITLE" , nullable=false , length=32 )
 	private String title;
 	
 
 	
 	@SuppressWarnings("unused")
 	@OneToMany(mappedBy = "taxClass", targetEntity = Product.class)
 	private List<Product> products = new ArrayList<Product>();
 	
	//TODO appropriate mapping
	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
 	@JoinTable(name = "MERCHANT_TAXCLASS", schema=SchemaConstant.SALESMANAGER_SCHEMA, joinColumns = { 
 			@JoinColumn(name = "TAX_CLASS_ID", nullable = false) }, 
 			inverseJoinColumns = { @JoinColumn(name = "MERCHANT_ID", 
 					nullable = false) })
	private Set<MerchantStore> stores = new HashSet<MerchantStore>();
 	
 	@OneToMany(mappedBy = "taxClass", cascade = CascadeType.ALL)
 	private List<TaxClassDescription> descriptions = new ArrayList<TaxClassDescription>();
 	
 	@OneToMany(mappedBy = "taxClass")
 	private List<TaxRate> taxRates = new ArrayList<TaxRate>();
 	
 	public TaxClass() {
 		super();
 	}
 	
 	@Override
 	public Long getId() {
 		return this.id;
 	}
 
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public List<TaxClassDescription> getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(List<TaxClassDescription> descriptions) {
 		this.descriptions = descriptions;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public List<TaxRate> getTaxRates() {
 		return taxRates;
 	}
 
 	public void setTaxRates(List<TaxRate> taxRates) {
 		this.taxRates = taxRates;
 	}
 
	public Set<MerchantStore> getStores() {
		return stores;
 	}
 
	public void setStores(Set<MerchantStore> stores) {
		this.stores = stores;
 	}
 	
 }
