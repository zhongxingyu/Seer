 package com.euroit.militaryshop.dto;

 import com.euroit.eshop.dto.ShopItemDto;
 import com.euroit.militaryshop.web.validation.constraint.ItemUnique;
 
import java.io.Serializable;

 /**
  * @author Roman Tuchin
  */
 @ItemUnique(message = "{adminPage.item.error.item.already.exists}")
 public class MilitaryShopItemDto extends ShopItemDto {
 	
 	private long colorId;
     private long materialId;
     private long sizeId;
 
     public long getColorId() {
         return colorId;
     }
 
     public void setColorId(long colorId) {
         this.colorId = colorId;
     }
 
     public long getMaterialId() {
         return materialId;
     }
 
     public void setMaterialId(long materialId) {
         this.materialId = materialId;
     }
 
     public long getSizeId() {
         return sizeId;
     }
 
     public void setSizeId(long sizeId) {
         this.sizeId = sizeId;
     }
 }
