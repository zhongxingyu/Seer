 package com.euroit.militaryshop.service.impl;
 
 import com.euroit.common.exception.ItemAlreadyExistsException;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.persistence.dao.ItemDao;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopItem;
 import com.euroit.militaryshop.service.ItemService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * @author Roman Tuchin
  */
 @Service
 public class ItemServiceImpl implements ItemService {
 
     private ItemDao itemDao;
 
     @Override
     @Transactional
     public void createOrSaveItem(MilitaryShopItemDto militaryShopItemDto) {
         itemDao.createOrSave(militaryShopItemDto);
     }
 
     @Override
     @Transactional
     public void checkItemWithProperties(MilitaryShopItemDto militaryShopItemDto) throws ItemAlreadyExistsException {
         List<MilitaryShopItem> items = itemDao.getProductItemsByProperties(militaryShopItemDto.getProductId(),
                 militaryShopItemDto.getColorId(), militaryShopItemDto.getMaterialId(), militaryShopItemDto.getSizeId());
 
         if (items.size() > 0) {
             throw new ItemAlreadyExistsException();
         }
     }
 
     @Autowired
     public void setItemDao(ItemDao itemDao) {
         this.itemDao = itemDao;
     }
 }
