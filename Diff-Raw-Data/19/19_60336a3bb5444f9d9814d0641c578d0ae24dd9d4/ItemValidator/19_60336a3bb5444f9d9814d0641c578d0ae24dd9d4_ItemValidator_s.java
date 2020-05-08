 package org.store.validation;
 
 import java.util.Map;
 import java.util.HashMap;
 import org.store.models.Item;
 
 
 public class ItemValidator {
 
     private Map<String, String> errorMessages = new HashMap<String, String>();
 
     public boolean hasErrors(){
 	return !errorMessages.isEmpty();
     }
 
     public void validate(Item item) {
 	if (item == null) {
 	    errorMessages.put("item", "Item is absent!");
 	    return;
 	}
 	if (item.getName() == null || item.getName().isEmpty()) {
 	    errorMessages.put("item.name", "name is required");
 	} else {
	    if (item.getName().length() <= 255) {
 		errorMessages.put("item.name", "name must be less than 255 signs");
 	    }
 	}
 	if (item.getPrice() == null ) {
 	    errorMessages.put("item.price", "price is required");
 	} else {
 	    if (item.getPrice().doubleValue() < 0) {
 		errorMessages.put("item.price", "price must be greater or equal zero");
 	    }
 	}
     }
 
     public Map<String, String> getErrorMessages() {
 	return errorMessages;
     }
 }
  
