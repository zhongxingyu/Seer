 package com.salesmanager.core.business.merchant.model;
 
 import java.util.ArrayList;
 import java.util.Date;
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
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import com.salesmanager.core.business.catalog.product.model.Product;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOption;
 import com.salesmanager.core.business.catalog.product.model.attribute.ProductOptionValue;
 import com.salesmanager.core.business.catalog.product.model.manufacturer.Manufacturer;
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.business.reference.country.model.Country;
 import com.salesmanager.core.business.reference.currency.model.Currency;
 import com.salesmanager.core.business.reference.language.model.Language;
 import com.salesmanager.core.business.reference.zone.model.Zone;
 import com.salesmanager.core.business.tax.model.taxclass.TaxClass;
 import com.salesmanager.core.business.tax.model.taxrate.TaxRate;
 import com.salesmanager.core.constants.SchemaConstant;
 import com.salesmanager.core.utils.CloneUtils;
 
 @Entity
 @Table(name = "MERCHANT_STORE", schema=SchemaConstant.SALESMANAGER_SCHEMA)
 public class MerchantStore extends SalesManagerEntity<Integer, MerchantStore> {
 	private static final long serialVersionUID = 7671103335743647655L;
 	
 	@Id
 	@Column(name = "MERCHANT_ID", unique=true, nullable=false)
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "LANG_SEQ_NEXT_VAL")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Integer id;
 
 	@Column(name = "STORE_NAME", nullable=false, unique=true, length=100)
 	private String storename;
 
 	@Column(name = "STORE_PHONE", length=50)
 	private String storephone;
 
 	@Column(name = "STORE_ADDRESS")
 	private String storeaddress;
 
 	@Column(name = "STORE_CITY", length=100)
 	private String storecity;
 
 	@Column(name = "STORE_POSTAL_CODE", length=15)
 	private String storepostalcode;
 
 	@OneToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name="COUNTRY_ID", nullable=false, updatable=true)
 	private Country country;
 
 	@OneToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name="ZONE_ID", nullable=true, updatable=true)
 	private Zone zone;
 
 	@Column(name = "STORE_STATE_PROV", length=100)
 	private String storestateprovince;
 	
 	@Column(name = "WEIGHTUNITCODE", length=5)
 	private String weightunitcode;
 
 	@Column(name = "SEIZEUNITCODE", length=5)
 	private String seizeunitcode;
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "IN_BUSINESS_SINCE")
 	private Date inBusinessSince;
 	
 	@OneToOne
 	@JoinColumn(name="LANGUAGE_ID", nullable=false)
 	private Language defaultLanguage;
 
 	@OneToOne
 	@JoinColumn(name="STORE_BRANDING_ID", nullable=true, updatable=true)
 	private StoreBranding branding;
 
 	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
 	@JoinTable(name = "MERCHANT_LANGUAGE", joinColumns = { 
 			@JoinColumn(name = "MERCHANT_ID", nullable = false, updatable = false) }, 
 			inverseJoinColumns = { @JoinColumn(name = "LANGUAGE_ID", 
 					nullable = false, updatable = false) })
 	private List<Language> languages = new ArrayList<Language>();
 	
 
 	
 	public MerchantStore() {
 	}
 	
 	@Column(name = "USE_CACHE")
 	private boolean useCache = false;
 
 	public boolean isUseCache() {
 		return useCache;
 	}
 
 	public void setUseCache(boolean useCache) {
 		this.useCache = useCache;
 	}
 	
 	@Override
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Override
 	public Integer getId() {
 		return this.id;
 	}
 	
 	public String getStorename() {
 		return storename;
 	}
 
 	public void setStorename(String storename) {
 		this.storename = storename;
 	}
 
 	public String getStorephone() {
 		return storephone;
 	}
 
 	public void setStorephone(String storephone) {
 		this.storephone = storephone;
 	}
 
 	public String getStoreaddress() {
 		return storeaddress;
 	}
 
 	public void setStoreaddress(String storeaddress) {
 		this.storeaddress = storeaddress;
 	}
 
 	public String getStorecity() {
 		return storecity;
 	}
 
 	public void setStorecity(String storecity) {
 		this.storecity = storecity;
 	}
 
 	public String getStorepostalcode() {
 		return storepostalcode;
 	}
 
 	public void setStorepostalcode(String storepostalcode) {
 		this.storepostalcode = storepostalcode;
 	}
 
 	public Country getCountry() {
 		return country;
 	}
 
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 
 	public Zone getZone() {
 		return zone;
 	}
 
 	public void setZone(Zone zone) {
 		this.zone = zone;
 	}
 
 	public String getStorestateprovince() {
 		return storestateprovince;
 	}
 
 	public void setStorestateprovince(String storestateprovince) {
 		this.storestateprovince = storestateprovince;
 	}
 
 	public Currency getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(Currency currency) {
 		this.currency = currency;
 	}
 
 	public String getWeightunitcode() {
 		return weightunitcode;
 	}
 
 	public void setWeightunitcode(String weightunitcode) {
 		this.weightunitcode = weightunitcode;
 	}
 
 	public String getSeizeunitcode() {
 		return seizeunitcode;
 	}
 
 	public void setSeizeunitcode(String seizeunitcode) {
 		this.seizeunitcode = seizeunitcode;
 	}
 
 	public StoreBranding getBranding() {
 		return branding;
 	}
 
 	public void setBranding(StoreBranding branding) {
 		this.branding = branding;
 	}
 
 	public Date getInBusinessSince() {
 		return CloneUtils.clone(inBusinessSince);
 	}
 
 	public void setInBusinessSince(Date inBusinessSince) {
 		this.inBusinessSince = CloneUtils.clone(inBusinessSince);
 	}
 
 	public Language getDefaultLanguage() {
 		return defaultLanguage;
 	}
 
 	public void setDefaultLanguage(Language defaultLanguage) {
 		this.defaultLanguage = defaultLanguage;
 	}
 
 
 
 	public List<Language> getLanguages() {
 		return languages;
 	}
 
 	public void setLanguages(List<Language> languages) {
 		this.languages = languages;
 	}
 	
 	//mappings
 	@SuppressWarnings("unused")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "stores")
 	private Set<Manufacturer> manufacturers = new HashSet<Manufacturer>();
 	
 	@SuppressWarnings("unused")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "stores")
 	private Set<TaxClass> taxClasses = new HashSet<TaxClass>();
 	
 	@SuppressWarnings("unused")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "stores")
 	private Set<ProductOption> productOptions = new HashSet<ProductOption>();
 	
 	@SuppressWarnings("unused")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "stores")
 	private Set<ProductOptionValue> productOptionValues = new HashSet<ProductOptionValue>();
 	
 	@SuppressWarnings("unused")
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "stores")
 	private Set<Product> products = new HashSet<Product>();
 	
 	//TODO Currency is system not specific to a merchant
	@ManyToOne(targetEntity = Currency.class)
 	@JoinColumn(name = "CURRENCY_ID", nullable=false)
 	private Currency currency;
 	
 	//TODO ManyToMany
 	@SuppressWarnings("unused")
	@OneToMany(mappedBy = "merchant")
 	private List<TaxRate> taxRates = new ArrayList<TaxRate>();
 
 
 }
