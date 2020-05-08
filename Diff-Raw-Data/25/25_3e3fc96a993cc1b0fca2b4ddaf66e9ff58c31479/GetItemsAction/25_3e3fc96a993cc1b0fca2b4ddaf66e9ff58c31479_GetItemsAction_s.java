 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eboy.action.user;
 
 import com.ebay.services.finding.Amount;
 import com.eboy.api.ExchangeConversion;
 import com.eboy.po.Category;
 import com.eboy.po.Item;
 import com.eboy.po.ItemTag;
 import com.eboy.po.Tag;
 import com.eboy.service.CategoryService;
 import com.eboy.service.ItemService;
 import com.eboy.service.ItemTagService;
 import com.eboy.service.TagService;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author wjl
  */
 public class GetItemsAction extends ActionSupport {
 
         private String method;
         private String value;
         private ItemService itemService;
         private CategoryService categoryService;
         private TagService tagService;
 
         @Override
         public String execute() {
                 List<String> cla = new ArrayList<String>();
                 for (int i = 0; i < 9; i++) {
                         if (i % 3 == 2) {
                                 cla.add("product_box no_margin_right");
                         } else {
                                 cla.add("product_box");
                         }
                 }
                 ActionContext context = ActionContext.getContext();
                 context.put("class", cla);
                 List<Item> items = null;
                 if (getMethod().equals("all")) {
                         items = getItemService().getItems();
                         for (int i = 0; i < items.size(); i++) {
                                 if (items.get(i).getItemQuantity() == 0) {
                                         items.remove(i);
                                 }
                         }
                 }
                 if (getMethod().equals("category")) {
                         int categoryId = Integer.parseInt(getValue());
                         Category category = getCategoryService().getCategory(categoryId);
                         Object[] is = category.getItems().toArray();
                         items = new ArrayList<Item>();
                         for (int i = 0; i < is.length; i++) {
                                 items.add((Item) is[i]);
                         }
                         for (int i = 0; i < items.size(); i++) {
                                 if (items.get(i).getItemQuantity() == 0) {
                                         items.remove(i);
                                 }
                         }
                 }
                 if (getMethod().equals("tag")) {
                         int tagId = Integer.parseInt(getValue());
                         Tag tag = tagService.getTag(tagId);
                         Object[] its = tag.getItemTags().toArray();
                         items = new ArrayList<Item>();
                         for (int i = 0; i < its.length; i++) {
                                 items.add(((ItemTag) its[i]).getItem());
                         }
                         for (int i = 0; i < items.size(); i++) {
                                 if (items.get(i).getItemQuantity() == 0) {
                                         items.remove(i);
                                 }
                         }
                 }
                 if (getMethod().equals("keyword")) {
                         String keyword = value;
                         items = itemService.getItemsOrderBySale(keyword);
                         //System.out.println(items.size());
                         for (int i = 0; i < items.size(); i++) {
                                 if (items.get(i).getItemQuantity() == 0) {
                                         items.remove(i);
                                 }
                         }
                 }
 
 
                 List<Item> newItemList = new ArrayList<Item>();
                 List<Item> rateItemList = new ArrayList<Item>();
                 List<Item> priceItemList = new ArrayList<Item>();
                 List<Item> saleItemList = new ArrayList<Item>();
                 int size;
                 if (items.size() > 9) {
                         size = 9;
                 } else {
                         size = items.size();
                 }
                 for (int i = 0; i < items.size(); i++) {
                         Item item = items.get(i);
                         Amount amount = new Amount();
                         amount.setCurrencyId(item.getItemPriceCurrency());
                         amount.setValue(item.getItemPrice());
                         Amount newAmount = ExchangeConversion.execute(amount);
                         item.setItemPrice(newAmount.getValue());
                         item.setItemPriceCurrency(newAmount.getCurrencyId());
                 }
                 newItemList = SortItem.sortByNew(items);
                 newItemList = newItemList.subList(0, size);
                 context.put("newItemList", newItemList);
                 rateItemList = SortItem.sortByRate(items);
                 rateItemList = rateItemList.subList(0, size);
                 context.put("rateItemList", rateItemList);
                 saleItemList = SortItem.sortBySale(items);
                 saleItemList = saleItemList.subList(0, size);
                 context.put("saleItemList", saleItemList);
                 priceItemList = SortItem.sortByPrice(items);
                 priceItemList = priceItemList.subList(0, size);
                 context.put("priceItemList", priceItemList);
                 return "success";
         }
 
         public ItemService getItemService() {
                 return itemService;
         }
 
         public void setItemService(ItemService itemService) {
                 this.itemService = itemService;
         }
 
         public String getMethod() {
                 return method;
         }
 
         public void setMethod(String method) {
                 this.method = method;
         }
 
         public String getValue() {
                 return value;
         }
 
         public void setValue(String value) {
                 this.value = value;
         }
 
         public CategoryService getCategoryService() {
                 return categoryService;
         }
 
         public void setCategoryService(CategoryService categoryService) {
                 this.categoryService = categoryService;
         }
 
         public TagService getTagService() {
                 return tagService;
         }
 
         public void setTagService(TagService tagService) {
                 this.tagService = tagService;
         }
 }
