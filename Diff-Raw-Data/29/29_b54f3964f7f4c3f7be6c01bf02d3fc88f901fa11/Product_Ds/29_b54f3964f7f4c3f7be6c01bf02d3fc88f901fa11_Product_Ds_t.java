 /**
  * DNet eBusiness Suite
  * Copyright: 2010-2013 Nan21 Electronics SRL. All rights reserved.
  * Use is subject to license terms.
  */
 package net.nan21.dnet.module.md.presenter.impl.mm.model;
 
 import java.math.BigDecimal;
 import net.nan21.dnet.core.api.annotation.Ds;
 import net.nan21.dnet.core.api.annotation.DsField;
 import net.nan21.dnet.core.api.annotation.Param;
 import net.nan21.dnet.core.api.annotation.RefLookup;
 import net.nan21.dnet.core.api.annotation.RefLookups;
 import net.nan21.dnet.core.api.annotation.SortField;
 import net.nan21.dnet.core.presenter.model.AbstractTypeWithCodeDs;
 import net.nan21.dnet.module.bd.domain.impl.attr.AttributeSet;
 import net.nan21.dnet.module.bd.domain.impl.attr.LookupItem;
 import net.nan21.dnet.module.bd.domain.impl.uom.Uom;
 import net.nan21.dnet.module.md.domain.impl.mm.Product;
 import net.nan21.dnet.module.md.domain.impl.mm.ProductCategory;
 import net.nan21.dnet.module.md.domain.impl.mm.ProductManufacturer;
 
 @Ds(entity = Product.class, sort = {@SortField(field = Product_Ds.f_code)})
 @RefLookups({
 		@RefLookup(refId = Product_Ds.f_manufacturerId, namedQuery = ProductManufacturer.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_manufacturer)}),
 		@RefLookup(refId = Product_Ds.f_uomId, namedQuery = Uom.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_uom)}),
 		@RefLookup(refId = Product_Ds.f_dimUomId, namedQuery = Uom.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_dimUom)}),
 		@RefLookup(refId = Product_Ds.f_volumeUomId, namedQuery = Uom.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_volumeUom)}),
 		@RefLookup(refId = Product_Ds.f_weightUomId, namedQuery = Uom.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_weightUom)}),
 		@RefLookup(refId = Product_Ds.f_attributeSetId, namedQuery = AttributeSet.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_attributeSet)}),
 		@RefLookup(refId = Product_Ds.f_categoryId, namedQuery = ProductCategory.NQ_FIND_BY_CODE, params = {@Param(name = "code", field = Product_Ds.f_category)}),
		@RefLookup(refId = Product_Ds.f_materialId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.material"),
				@Param(name = "code", field = Product_Ds.f_material)}),
		@RefLookup(refId = Product_Ds.f_qualityId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.quality"),
				@Param(name = "code", field = Product_Ds.f_quality)}),
		@RefLookup(refId = Product_Ds.f_surfaceId, namedQuery = LookupItem.NQ_FIND_BY_CODE, params = {
				@Param(name = "type", value = "Product.surface"),
				@Param(name = "code", field = Product_Ds.f_surface)})})
 public class Product_Ds extends AbstractTypeWithCodeDs<Product> {
 
 	public static final String f_iconUrl = "iconUrl";
 	public static final String f_imageUrl = "imageUrl";
 	public static final String f_storable = "storable";
 	public static final String f_weight = "weight";
 	public static final String f_volume = "volume";
 	public static final String f_dimWidth = "dimWidth";
 	public static final String f_dimHeight = "dimHeight";
 	public static final String f_dimDepth = "dimDepth";
 	public static final String f_manufacturerProductNo = "manufacturerProductNo";
 	public static final String f_iconLocation = "iconLocation";
 	public static final String f_imageLocation = "imageLocation";
 	public static final String f_categoryId = "categoryId";
 	public static final String f_category = "category";
 	public static final String f_categoryName = "categoryName";
 	public static final String f_attributeSetId = "attributeSetId";
 	public static final String f_attributeSet = "attributeSet";
 	public static final String f_attributeSetName = "attributeSetName";
 	public static final String f_uomId = "uomId";
 	public static final String f_uom = "uom";
 	public static final String f_weightUomId = "weightUomId";
 	public static final String f_weightUom = "weightUom";
 	public static final String f_volumeUomId = "volumeUomId";
 	public static final String f_volumeUom = "volumeUom";
 	public static final String f_dimUomId = "dimUomId";
 	public static final String f_dimUom = "dimUom";
 	public static final String f_manufacturerId = "manufacturerId";
 	public static final String f_manufacturer = "manufacturer";
 	public static final String f_materialId = "materialId";
 	public static final String f_material = "material";
 	public static final String f_qualityId = "qualityId";
 	public static final String f_quality = "quality";
 	public static final String f_surfaceId = "surfaceId";
 	public static final String f_surface = "surface";
 
 	@DsField
 	private String iconUrl;
 
 	@DsField
 	private String imageUrl;
 
 	@DsField
 	private Boolean storable;
 
 	@DsField
 	private BigDecimal weight;
 
 	@DsField
 	private BigDecimal volume;
 
 	@DsField
 	private BigDecimal dimWidth;
 
 	@DsField
 	private BigDecimal dimHeight;
 
 	@DsField
 	private BigDecimal dimDepth;
 
 	@DsField
 	private String manufacturerProductNo;
 
 	@DsField(fetch = false)
 	private String iconLocation;
 
 	@DsField(fetch = false)
 	private String imageLocation;
 
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
 
 	@DsField(join = "left", path = "defaultUom.id")
 	private String uomId;
 
 	@DsField(join = "left", path = "defaultUom.code")
 	private String uom;
 
 	@DsField(join = "left", path = "weightUom.id")
 	private String weightUomId;
 
 	@DsField(join = "left", path = "weightUom.code")
 	private String weightUom;
 
 	@DsField(join = "left", path = "volumeUom.id")
 	private String volumeUomId;
 
 	@DsField(join = "left", path = "volumeUom.code")
 	private String volumeUom;
 
 	@DsField(join = "left", path = "dimUom.id")
 	private String dimUomId;
 
 	@DsField(join = "left", path = "dimUom.code")
 	private String dimUom;
 
 	@DsField(join = "left", path = "manufacturer.id")
 	private String manufacturerId;
 
 	@DsField(join = "left", path = "manufacturer.code")
 	private String manufacturer;
 
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
 
 	public Product_Ds() {
 		super();
 	}
 
 	public Product_Ds(Product e) {
 		super(e);
 	}
 
 	public String getIconUrl() {
 		return this.iconUrl;
 	}
 
 	public void setIconUrl(String iconUrl) {
 		this.iconUrl = iconUrl;
 	}
 
 	public String getImageUrl() {
 		return this.imageUrl;
 	}
 
 	public void setImageUrl(String imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 
 	public Boolean getStorable() {
 		return this.storable;
 	}
 
 	public void setStorable(Boolean storable) {
 		this.storable = storable;
 	}
 
 	public BigDecimal getWeight() {
 		return this.weight;
 	}
 
 	public void setWeight(BigDecimal weight) {
 		this.weight = weight;
 	}
 
 	public BigDecimal getVolume() {
 		return this.volume;
 	}
 
 	public void setVolume(BigDecimal volume) {
 		this.volume = volume;
 	}
 
 	public BigDecimal getDimWidth() {
 		return this.dimWidth;
 	}
 
 	public void setDimWidth(BigDecimal dimWidth) {
 		this.dimWidth = dimWidth;
 	}
 
 	public BigDecimal getDimHeight() {
 		return this.dimHeight;
 	}
 
 	public void setDimHeight(BigDecimal dimHeight) {
 		this.dimHeight = dimHeight;
 	}
 
 	public BigDecimal getDimDepth() {
 		return this.dimDepth;
 	}
 
 	public void setDimDepth(BigDecimal dimDepth) {
 		this.dimDepth = dimDepth;
 	}
 
 	public String getManufacturerProductNo() {
 		return this.manufacturerProductNo;
 	}
 
 	public void setManufacturerProductNo(String manufacturerProductNo) {
 		this.manufacturerProductNo = manufacturerProductNo;
 	}
 
 	public String getIconLocation() {
 		return this.iconLocation;
 	}
 
 	public void setIconLocation(String iconLocation) {
 		this.iconLocation = iconLocation;
 	}
 
 	public String getImageLocation() {
 		return this.imageLocation;
 	}
 
 	public void setImageLocation(String imageLocation) {
 		this.imageLocation = imageLocation;
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
 
 	public String getUomId() {
 		return this.uomId;
 	}
 
 	public void setUomId(String uomId) {
 		this.uomId = uomId;
 	}
 
 	public String getUom() {
 		return this.uom;
 	}
 
 	public void setUom(String uom) {
 		this.uom = uom;
 	}
 
 	public String getWeightUomId() {
 		return this.weightUomId;
 	}
 
 	public void setWeightUomId(String weightUomId) {
 		this.weightUomId = weightUomId;
 	}
 
 	public String getWeightUom() {
 		return this.weightUom;
 	}
 
 	public void setWeightUom(String weightUom) {
 		this.weightUom = weightUom;
 	}
 
 	public String getVolumeUomId() {
 		return this.volumeUomId;
 	}
 
 	public void setVolumeUomId(String volumeUomId) {
 		this.volumeUomId = volumeUomId;
 	}
 
 	public String getVolumeUom() {
 		return this.volumeUom;
 	}
 
 	public void setVolumeUom(String volumeUom) {
 		this.volumeUom = volumeUom;
 	}
 
 	public String getDimUomId() {
 		return this.dimUomId;
 	}
 
 	public void setDimUomId(String dimUomId) {
 		this.dimUomId = dimUomId;
 	}
 
 	public String getDimUom() {
 		return this.dimUom;
 	}
 
 	public void setDimUom(String dimUom) {
 		this.dimUom = dimUom;
 	}
 
 	public String getManufacturerId() {
 		return this.manufacturerId;
 	}
 
 	public void setManufacturerId(String manufacturerId) {
 		this.manufacturerId = manufacturerId;
 	}
 
 	public String getManufacturer() {
 		return this.manufacturer;
 	}
 
 	public void setManufacturer(String manufacturer) {
 		this.manufacturer = manufacturer;
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
