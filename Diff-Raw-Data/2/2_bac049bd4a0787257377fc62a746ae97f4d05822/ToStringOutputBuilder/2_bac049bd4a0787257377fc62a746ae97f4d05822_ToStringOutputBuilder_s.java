 package org.plugtree.drools.shell.outputbuilders;
 
 import java.util.Collection;
 
 /**
  * creation date: 2/20/11
  */
 public class ToStringOutputBuilder implements OutputBuilder<Collection<Object>>{
     @Override
     public String getOutput(Collection<Object> objects) {
        if(objects.isEmpty())
             return "0 results found";
 
         StringBuilder builder = new StringBuilder();
         for(Object object : objects){
             builder.append(object.toString());
             builder.append(CR);
         }
         builder.deleteCharAt(builder.lastIndexOf(CR));
         return builder.toString();
     }
 }
