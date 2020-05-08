 /**
  * DNet eBusiness Suite
  * Copyright: 2010-2013 Nan21 Electronics SRL. All rights reserved.
  * Use is subject to license terms.
  */
 package net.nan21.dnet.module.md.presenter.impl.mm.model;
 
 import net.nan21.dnet.core.api.annotation.Ds;
 import net.nan21.dnet.core.api.annotation.DsField;
 import net.nan21.dnet.core.api.annotation.Param;
 import net.nan21.dnet.core.api.annotation.RefLookup;
 import net.nan21.dnet.core.api.annotation.RefLookups;
 import net.nan21.dnet.core.api.annotation.SortField;
 import net.nan21.dnet.core.presenter.model.AbstractTypeWithCodeDs;
 import net.nan21.dnet.module.bd.domain.impl.attr.AttributeSet;
 import net.nan21.dnet.module.bd.domain.impl.attr.LookupItem;
 import net.nan21.dnet.module.md.domain.impl.mm.ProductCategory;
 
 @Ds(entity = ProductCategory.class, sort = {@SortField(field = ProductCategory_Ds.f_code)})
 @RefLookups({
 		@RefLookup(refId = ProductCategory_Ds.f_categoryId, namedQuery = ProductCategory.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = ProductCategory_Ds.f_category)}),
 		@RefLookup(refId = ProductCategory_Ds.f_attributeSetId, namedQuery = AttributeSet.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = ProductCategory_Ds.f_attributeSet)}),
		@RefLookup(refId = ProductCategory_Ds.f_materialId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.material"),
				@Param(name = "code", field = ProductCategory_Ds.f_material)}),
		@RefLookup(refId = ProductCategory_Ds.f_qualityId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.quality"),
				@Param(name = "code", field = ProductCategory_Ds.f_quality)}),
		@RefLookup(refId = ProductCategory_Ds.f_surfaceId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.surface"),
				@Param(name = "code", field = ProductCategory_Ds.f_surface)})})
 public class ProductCategory_Ds extends AbstractTypeWithCodeDs<ProductCategory> {
 
 	public static final String f_acceptProd = "acceptProd";
 	public static final String f_acceptCateg = "acceptCateg";
 	public static final String f_iconUrl = "iconUrl";
 	public static final String f_iconLocation = "iconLocation";
 	public static final String f_categoryId = "categoryId";
 	public static final String f_category = "category";
 	public static final String f_categoryName = "categoryName";
 	public static final String f_attributeSetId = "attributeSetId";
 	public static final String f_attributeSet = "attributeSet";
 	public static final String f_attributeSetName = "attributeSetName";
 	public static final String f_materialId = "materialId";
 	public static final String f_material = "material";
 	public static final String f_qualityId = "qualityId";
 	public static final String f_quality = "quality";
 	public static final String f_surfaceId = "surfaceId";
 	public static final String f_surface = "surface";
 
 	@DsField
 	private Boolean acceptProd;
 
 	@DsField
 	private Boolean acceptCateg;
 
 	@DsField
 	private String iconUrl;
 
 	@DsField(fetch = false)
 	private String iconLocation;
 
 	@DsField(join = "left", path = "category.id")
 	private String categoryId;
 
 	@DsField(join = "left", path = "category.code")
 	private String category;
 
 	@DsField(join = "left", path = "category.name")
 	private String categoryName;
 
 	@DsField(join = "left", path = "attributeSet.id")
 	private String attributeSetId;
 
 	@DsField(join = "left", path = "attributeSet.code")
 	private String attributeSet;
 
 	@DsField(join = "left", path = "attributeSet.name")
 	private String attributeSetName;
 
 	@DsField(join = "left", path = "material.id")
 	private String materialId;
 
 	@DsField(join = "left", path = "material.code")
 	private String material;
 
 	@DsField(join = "left", path = "quality.id")
 	private String qualityId;
 
 	@DsField(join = "left", path = "quality.code")
 	private String quality;
 
 	@DsField(join = "left", path = "surface.id")
 	private String surfaceId;
 
 	@DsField(join = "left", path = "surface.code")
 	private String surface;
 
 	public ProductCategory_Ds() {
 		super();
 	}
 
 	public ProductCategory_Ds(ProductCategory e) {
 		super(e);
 	}
 
 	public Boolean getAcceptProd() {
 		return this.acceptProd;
 	}
 
 	public void setAcceptProd(Boolean acceptProd) {
 		this.acceptProd = acceptProd;
 	}
 
 	public Boolean getAcceptCateg() {
 		return this.acceptCateg;
 	}
 
 	public void setAcceptCateg(Boolean acceptCateg) {
 		this.acceptCateg = acceptCateg;
 	}
 
 	public String getIconUrl() {
 		return this.iconUrl;
 	}
 
 	public void setIconUrl(String iconUrl) {
 		this.iconUrl = iconUrl;
 	}
 
 	public String getIconLocation() {
 		return this.iconLocation;
 	}
 
 	public void setIconLocation(String iconLocation) {
 		this.iconLocation = iconLocation;
 	}
 
 	public String getCategoryId() {
 		return this.categoryId;
 	}
 
 	public void setCategoryId(String categoryId) {
 		this.categoryId = categoryId;
 	}
 
 	public String getCategory() {
 		return this.category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public String getCategoryName() {
 		return this.categoryName;
 	}
 
 	public void setCategoryName(String categoryName) {
 		this.categoryName = categoryName;
 	}
 
 	public String getAttributeSetId() {
 		return this.attributeSetId;
 	}
 
 	public void setAttributeSetId(String attributeSetId) {
 		this.attributeSetId = attributeSetId;
 	}
 
 	public String getAttributeSet() {
 		return this.attributeSet;
 	}
 
 	public void setAttributeSet(String attributeSet) {
 		this.attributeSet = attributeSet;
 	}
 
 	public String getAttributeSetName() {
 		return this.attributeSetName;
 	}
 
 	public void setAttributeSetName(String attributeSetName) {
 		this.attributeSetName = attributeSetName;
 	}
 
 	public String getMaterialId() {
 		return this.materialId;
 	}
 
 	public void setMaterialId(String materialId) {
 		this.materialId = materialId;
 	}
 
 	public String getMaterial() {
 		return this.material;
 	}
 
 	public void setMaterial(String material) {
 		this.material = material;
 	}
 
 	public String getQualityId() {
 		return this.qualityId;
 	}
 
 	public void setQualityId(String qualityId) {
 		this.qualityId = qualityId;
 	}
 
 	public String getQuality() {
 		return this.quality;
 	}
 
 	public void setQuality(String quality) {
 		this.quality = quality;
 	}
 
 	public String getSurfaceId() {
 		return this.surfaceId;
 	}
 
 	public void setSurfaceId(String surfaceId) {
 		this.surfaceId = surfaceId;
 	}
 
 	public String getSurface() {
 		return this.surface;
 	}
 
 	public void setSurface(String surface) {
 		this.surface = surface;
 	}
 }
