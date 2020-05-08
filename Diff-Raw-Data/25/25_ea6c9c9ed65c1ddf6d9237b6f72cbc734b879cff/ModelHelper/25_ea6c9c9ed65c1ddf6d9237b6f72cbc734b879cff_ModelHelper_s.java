 package utils;
 
 import models.TransactionTag;
 import models.TransactionType;
 
 public class ModelHelper {
 
     public static TransactionTag getOrSaveTag(String name) {
         TransactionTag tag = TransactionTag.find("name", name).first();
         if (tag != null) {
             return tag;
         } else {
             tag = new TransactionTag();
             tag.name = name;
             tag.save();
             return tag;
         }
     }
 
     public static TransactionType getOrAddType(String name) {
         TransactionType type = TransactionType.find("name", name).first();
         if (type != null) {
             return type;
         } else {
             type = new TransactionType();
             type.name = name;
             type.save();
             return type;
         }
     }
 }
