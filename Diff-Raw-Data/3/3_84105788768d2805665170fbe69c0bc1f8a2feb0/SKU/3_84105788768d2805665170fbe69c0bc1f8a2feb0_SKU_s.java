 package org.openxava.demoapp.model.md;
 
 import java.math.BigDecimal;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.openxava.annotations.DescriptionsList;
 import org.openxava.annotations.Hidden;
 import org.openxava.annotations.ReferenceView;
 import org.openxava.annotations.Required;
 import org.openxava.annotations.Stereotype;
 import org.openxava.annotations.Tab;
 import org.openxava.annotations.View;
 import org.openxava.demoapp.model.purchase.RequirementFormDetail;
 import org.openxava.ex.model.base.BaseMasterDataModel;
 
 @Entity
 @Table(name="MD_SKU")
 //BP: Can use a non-persistence in @Tab and @View
@Tab(baseCondition = "enabled=true", properties="code, name, vendor.name, uom.displayName, price, descr")
 @View(name="V-SKU-code-name", members="code; nameWithUom")
 public class SKU extends BaseMasterDataModel{
 	@ManyToOne(fetch=FetchType.LAZY, optional=false)
 	@ReferenceView("V-UOM-code-name")	//Code and name
 	private UOM uom;
 	
 	@ManyToOne(fetch=FetchType.LAZY, optional=false)
 	//BP: use @DescriptionsList to present a combobox to select
 	@DescriptionsList(descriptionProperties="code, name")
 	@Required
 	private Vendor vendor;
 
 	@Stereotype("MONEY")
 	private BigDecimal price;
 	
 	//BP: Use image field
 	@Stereotype("PHOTO")
 	private byte [] photo;
 	
 	public UOM getUom() {
 		return uom;
 	}
 
 	public void setUom(UOM uom) {
 		this.uom = uom;
 	}
 
 	public BigDecimal getPrice() {
 		return price;
 	}
 
 	public void setPrice(BigDecimal price) {
 		this.price = price;
 	}
 
 	public byte[] getPhoto() {
 		return photo;
 	}
 
 	public void setPhoto(byte[] photo) {
 		this.photo = photo;
 	}
 
 	public Vendor getVendor() {
 		return vendor;
 	}
 
 	public void setVendor(Vendor vendor) {
 		this.vendor = vendor;
 	}
 
 	/** BP: Let @ReferenceView({@link RequirementFormDetail#sku}) display uom.name property (can't display uom.name directly) */ 
 	@Transient @Hidden
 	public String getNameWithUom(){
 		return this.getName() + " ("+this.getUom().getName()+")";
 	}
 }
