 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package iniconfigurationmanager.items;
 
 import iniconfigurationmanager.schema.ConfigItemData;
 import iniconfigurationmanager.schema.ConfigItemSchema;
 
 /**
  *
  * @author KlonK
  */
 public class SignedOptionSchema extends ConfigItemSchema {
 
     @Override
    protected ConfigItemData getItemData() {
        return new SignedOptionData();
     }
 
 }
