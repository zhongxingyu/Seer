 package net.xelnaga.radiate.status.serializer;
 
 import hudson.scm.ChangeLogSet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ChangeJsonSerializer extends BaseJsonSerializer {
 
     public String toJson(Iterable<ChangeLogSet.Entry> changes) {
 
         List<String> elements = new ArrayList<String>();
         for (ChangeLogSet.Entry change : changes) {
             String element = toJson(change);
             elements.add(element);
         }
 
         return makeArray(elements);
     }
 
     public String toJson(ChangeLogSet.Entry change) {
 
        String message = change.getMsg();
         return makeQuotedAndEscaped(message);
     }
 }
