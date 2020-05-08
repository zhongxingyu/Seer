 package utils;
 
 import models.Transaction;
 import models.TransactionTag;
 import models.TransactionType;
 import models.User;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 public class ModelHelper {
 
     private static Logger logger = Logger.getLogger(
             ModelHelper.class.getName());
 
    public static TransactionTag insertIgnoreTag(TransactionTag t) {
        if (t == null || t.name == null || t.name.trim().length() == 0) {
             return null;
         }
        TransactionTag tag = TransactionTag.find("name", t.name).first();
         if (tag != null) {
             return tag;
         } else {
             tag = new TransactionTag();
            tag.name = t.name;
             tag.save();
             return tag;
         }
     }
 
    public static TransactionType insertIgnoreType(TransactionType t) {
        if (t == null || t.name == null || t.name.trim().length() == 0) {
             return null;
         }
        TransactionType type = TransactionType.find("name", t.name).first();
         if (type != null) {
             return type;
         } else {
             type = new TransactionType();
            type.name = t.name;
             type.save();
             return type;
         }
     }
 
     public static Transaction saveOrUpdate(Transaction t, User user) {
         if (!t.dirty) {
             logger.log(Level.WARN,
                     "Not saving non-dirty transaction with _id: " + t._id);
             return t;
         }
         if (t.id != null) {
             Transaction existing = Transaction.findById(t.id);
             if (existing != null) {
                 existing._id = t._id;
                 existing.accountingDate = t.accountingDate;
                 existing.amountIn = t.amountIn;
                 existing.amountOut = t.amountOut;
                 existing.text = t.text;
                 existing.internal = t.internal;
                 existing.timestamp = t.timestamp;
                 existing.dirty = false;
                existing.tag = ModelHelper.insertIgnoreTag(t.tag);
                existing.type = ModelHelper.insertIgnoreType(t.type);
                 existing.save();
                 return existing;
             }
         }
         t.id = null;
         // Need to update relationship for all foreign fields
        t.tag = ModelHelper.insertIgnoreTag(t.tag);
        t.type = ModelHelper.insertIgnoreType(t.type);
         t.dirty = false;
         t.user = user;
         t.save();
         return t;
     }
 }
