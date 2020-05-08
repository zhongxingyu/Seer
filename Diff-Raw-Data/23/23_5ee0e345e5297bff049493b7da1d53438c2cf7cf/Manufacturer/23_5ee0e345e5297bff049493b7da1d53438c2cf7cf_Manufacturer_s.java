 package com.salesmanager.core.business.catalog.product.model.manufacturer;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
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
 
 import org.hibernate.annotations.Cascade;
 
 
 import com.salesmanager.core.business.common.model.audit.AuditListener;
 import com.salesmanager.core.business.common.model.audit.AuditSection;
 import com.salesmanager.core.business.common.model.audit.Auditable;
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.business.merchant.model.MerchantStore;
 import com.salesmanager.core.constants.SchemaConstant;
 
 @Entity
 @EntityListeners(value = AuditListener.class)
 @Table(name = "MANUFACTURER", schema="SALESMANAGER")
 public class Manufacturer extends SalesManagerEntity<Long, Manufacturer> implements Auditable {
 	private static final long serialVersionUID = 80693964563570099L;
 	
 	@Id
 	@Column(name = "MANUFACTURER_ID", unique=true, nullable=false)
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "MANUFACT_SEQ_NEXT_VAL")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Long id;
 	
 	@Embedded
 	private AuditSection auditSection = new AuditSection();
 	
 	@OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL)
 	private Set<ManufacturerDescription> descriptions = new HashSet<ManufacturerDescription>();
 	
 	@Column(name = "MANUFACTURER_IMAGE")
 	private String image;
 	
	@ManyToMany(fetch=FetchType.LAZY, cascade = {CascadeType.REFRESH})
 	@JoinTable(name = "MANUFACTURER_MERCHANT", schema=SchemaConstant.SALESMANAGER_SCHEMA, joinColumns = { 
 			@JoinColumn(name = "MANUFACTURER_ID", nullable = false) }
 			, 
 			inverseJoinColumns = { @JoinColumn(name = "MERCHANT_ID", 
 					nullable = false) }
 	)
 	@Cascade({
 		org.hibernate.annotations.CascadeType.DETACH,
 		org.hibernate.annotations.CascadeType.LOCK,
 		org.hibernate.annotations.CascadeType.REFRESH,
 		org.hibernate.annotations.CascadeType.REPLICATE
 		
 	})
	private Set<MerchantStore> stores = new HashSet<MerchantStore>();
 
 	public Manufacturer() {
 	}
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Override
 	public AuditSection getAuditSection() {
 		return auditSection;
 	}
 	
 	@Override
 	public void setAuditSection(AuditSection auditSection) {
 		this.auditSection = auditSection;
 	}
 
 	public String getImage() {
 		return image;
 	}
 
 	public void setImage(String image) {
 		this.image = image;
 	}
 
 	public Set<ManufacturerDescription> getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(Set<ManufacturerDescription> descriptions) {
 		this.descriptions = descriptions;
 	}
 
	public Set<MerchantStore> getStores() {
		return stores;
 	}
 
	public void setStores(Set<MerchantStore> stores) {
		this.stores = stores;
 	}
 
 
 }
