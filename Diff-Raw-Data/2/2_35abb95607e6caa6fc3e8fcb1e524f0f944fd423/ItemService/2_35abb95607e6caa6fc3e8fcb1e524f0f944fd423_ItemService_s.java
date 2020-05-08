 
 package org.easetech.easytest.codegen.example;
 
 import java.util.List;
 
 import org.easetech.easytest.codegen.example.dto.Item;
 import org.easetech.easytest.codegen.example.dto.ItemId;
 import org.easetech.easytest.codegen.example.dto.LibraryId;
 
 public interface ItemService {
 
     public List<Item> getItems(LibraryId libraryId, String searchText, String itemType);
 
     public Item findItem(LibraryId libraryId, ItemId itemId);
 
 }
