 /*
  * Licensed to csti consulting 
  * You may obtain a copy of the License at
  *
  * http://www.csticonsulting.com
  * Copyright (c) 2006-Aug 24, 2010 Consultation CS-TI inc. 
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.salesmanager.core.business.tax.model.taxrate;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
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
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 
 import com.salesmanager.core.business.common.model.audit.AuditListener;
 import com.salesmanager.core.business.common.model.audit.AuditSection;
 import com.salesmanager.core.business.common.model.audit.Auditable;
 import com.salesmanager.core.business.generic.model.SalesManagerEntity;
 import com.salesmanager.core.business.merchant.model.MerchantStore;
 import com.salesmanager.core.business.reference.geozone.model.GeoZone;
 import com.salesmanager.core.business.tax.model.taxclass.TaxClass;
 import com.salesmanager.core.constants.SchemaConstant;
 
 @Entity
 @EntityListeners(value = AuditListener.class)
 @Table(name = "TAX_RATE" , schema = SchemaConstant.SALESMANAGER_SCHEMA )
 public class TaxRate  extends SalesManagerEntity<Long, TaxRate> implements Auditable {
 	private static final long serialVersionUID = 3356827741612925066L;
 	
 	@Id
 	@Column(name = "TAX_RATE_ID")
 	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TAX_RATE_ID_NEXT_VALUE")
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
 	private Long id;
 	
 	@Embedded
 	private AuditSection auditSection = new AuditSection();
 	
 	@Column(name = "TAX_PRIORITY")
 	private Integer taxPriority;
 	
 	@Column(name = "TAX_RATE" , nullable= false , precision=7, scale=4)
 	private BigDecimal taxRate;
 	
 	@Column(name = "PIGGYBACK")
 	private boolean piggyback;
 	
 	@ManyToOne
 	@JoinColumn(name = "TAX_CLASS_ID" , nullable=false)
 	private TaxClass taxClass;
 	

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="MERCHANT_ID", nullable=false)
	private MerchantStore merchantSore;
 	
 	@OneToMany(mappedBy = "taxRate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 	private List<TaxRateDescription> descriptions = new ArrayList<TaxRateDescription>();
 	
 	@ManyToOne(targetEntity = GeoZone.class)
 	@JoinColumn(name = "GEOZONE_ID")
 	private GeoZone geoZone;
 	
 	public TaxRate() {
 	}
 
 	public Long getId() {
 		return id;
 	}
 
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
 
 	public Integer getTaxPriority() {
 		return taxPriority;
 	}
 
 	public void setTaxPriority(Integer taxPriority) {
 		this.taxPriority = taxPriority;
 	}
 
 	public BigDecimal getTaxRate() {
 		return taxRate;
 	}
 
 	public void setTaxRate(BigDecimal taxRate) {
 		this.taxRate = taxRate;
 	}
 
 	public boolean isPiggyback() {
 		return piggyback;
 	}
 
 	public void setPiggyback(boolean piggyback) {
 		this.piggyback = piggyback;
 	}
 
 	public TaxClass getTaxClass() {
 		return taxClass;
 	}
 
 	public void setTaxClass(TaxClass taxClass) {
 		this.taxClass = taxClass;
 	}
 
 
 
 	public List<TaxRateDescription> getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(List<TaxRateDescription> descriptions) {
 		this.descriptions = descriptions;
 	}
 
 	public GeoZone getGeoZone() {
 		return geoZone;
 	}
 
 	public void setGeoZone(GeoZone geoZone) {
 		this.geoZone = geoZone;
 	}

	public MerchantStore getMerchantSore() {
		return merchantSore;
	}

	public void setMerchantSore(MerchantStore merchantSore) {
		this.merchantSore = merchantSore;
	}
 }
