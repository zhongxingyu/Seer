 package com.euroit.militaryshop.service.impl;
 
 import com.euroit.eshop.dto.BaseTrolleyItemDto;
 import com.euroit.eshop.exception.ItemAlreadyExistsException;
 import com.euroit.militaryshop.dto.DictionaryEntryDto;
 import com.euroit.militaryshop.dto.ItemInTrolleyDto;
 import com.euroit.militaryshop.dto.MilitaryShopItemDto;
 import com.euroit.militaryshop.persistence.dao.ItemDao;
 import com.euroit.militaryshop.persistence.entity.MilitaryShopItem;
 import com.euroit.militaryshop.service.DictionaryEntryService;
 import com.euroit.militaryshop.service.ItemService;
 import com.google.appengine.api.datastore.Key;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Roman Tuchin
  */
 @Service
 public class ItemServiceImpl implements ItemService {
 
 	Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
 	
     private ItemDao itemDao;
     
     private DictionaryEntryService dictionaryEntryService;
 
     @Autowired
     public void setItemDao(ItemDao itemDao) {
         this.itemDao = itemDao;
     }
     
     @Autowired
     public void setDictionaryEntryService(
 			DictionaryEntryService dictionaryEntryService) {
 		this.dictionaryEntryService = dictionaryEntryService;
 	}
 
 	@Override
     @Transactional
     public void createOrSaveItem(MilitaryShopItemDto militaryShopItemDto) {
         itemDao.createOrSave(militaryShopItemDto);
     }
 
     @Override
     @Transactional
     public void checkItemWithProperties(MilitaryShopItemDto militaryShopItemDto) throws ItemAlreadyExistsException {
         Assert.state(militaryShopItemDto.getProductId() > 0);
 
         List<MilitaryShopItem> items = itemDao.getProductItemsByProperties(militaryShopItemDto.getProductId(),
                 militaryShopItemDto.getColorId(), militaryShopItemDto.getMaterialId(), militaryShopItemDto.getSizeId());
 
         if (items.size() > 0) {
             throw new ItemAlreadyExistsException();
         }
     }
 
 	@Override
 	public List<ItemInTrolleyDto> getItemsForTrolleyList(Map<BaseTrolleyItemDto, Integer> itemsMap) {
 		List<ItemInTrolleyDto> retList = new ArrayList<ItemInTrolleyDto>();
 
         if (itemsMap == null || itemsMap.isEmpty()) {
             return retList;
         }
 
 		Map<BaseTrolleyItemDto, MilitaryShopItemDto> fullItemsMap = getItemsByIds(itemsMap);
 		
 		for (Map.Entry<BaseTrolleyItemDto, MilitaryShopItemDto> itemEntry : fullItemsMap.entrySet()) {
 			
 			ItemInTrolleyDto itemDto = new ItemInTrolleyDto();
 			itemDto.setShortName(itemEntry.getValue().getShortName());
 			itemDto.setQuantity(itemsMap.get(itemEntry.getKey()));
 			
 			long colorId = itemEntry.getValue().getColorId();
 			itemDto.setColorId(colorId);			
 			DictionaryEntryDto dictDto = fetchValueFromDictionary(colorId);
 			itemDto.setColor(dictDto != null ? dictDto.getValue() : "");
 			
 			long materialId = itemEntry.getValue().getMaterialId();
 			itemDto.setMaterialId(materialId);
 			dictDto = fetchValueFromDictionary(materialId);
 			itemDto.setMaterial(dictDto != null ? dictDto.getValue() : "");
 			
 			long sizeId = itemEntry.getValue().getSizeId();
 			itemDto.setSizeId(sizeId);
 			dictDto = fetchValueFromDictionary(sizeId);
 			itemDto.setSize(dictDto != null ? dictDto.getValue() : "");
 			itemDto.setPrice(itemEntry.getKey().getPrice());
 			
 			retList.add(itemDto);
 		}
 		
 		return retList;
 	}
 
 	//TODO try to avoid using it
 	private Map<BaseTrolleyItemDto, MilitaryShopItemDto> getItemsByIds(
 			Map<BaseTrolleyItemDto, Integer> itemsMap) {
 		Map<BaseTrolleyItemDto, MilitaryShopItemDto> retMap = 
 				new LinkedHashMap<BaseTrolleyItemDto, MilitaryShopItemDto>();
 		
 		Map<BaseTrolleyItemDto, MilitaryShopItem> fullItemsMap = itemDao.getItemsByIds(itemsMap.keySet());
 		
 		for (Map.Entry<BaseTrolleyItemDto, MilitaryShopItem> entry : fullItemsMap.entrySet()) {
 			MilitaryShopItemDto dto = new MilitaryShopItemDto();
             dto.setId(entry.getValue().getKey().getId());
 
             final Key colorKey = entry.getValue().getColorKey();
             if (colorKey != null) {
                 dto.setColorId(colorKey.getId());
             }
 
             final Key materialKey = entry.getValue().getMaterialKey();
             if (materialKey != null) {
                 dto.setMaterialId(materialKey.getId());
             }
 
             final Key sizeKey = entry.getValue().getSizeKey();
             if (sizeKey != null) {
                 dto.setSizeId(sizeKey.getId());
             }
 
 			dto.setProductId(entry.getValue().getProductKey().getId());
 			dto.setShortName(entry.getValue().getShortName());
 
 			
 			retMap.put(entry.getKey(), dto);
 		}
 		
 		return retMap;
 	}
 
 	private DictionaryEntryDto fetchValueFromDictionary(
 			long dictId) {
		DictionaryEntryDto dictDto = null;
        if (dictId != 0) {
		    dictDto = dictionaryEntryService.findDictionaryEntryById(dictId);
        }
        if (dictDto == null) {
            log.warn("Dictionary entry for id={} wasn't found", dictId);
        }

 		return dictDto;
 	}
 }
