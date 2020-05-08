 package org.wings.plaf;
 
 import java.util.Iterator;
 
 import org.wings.SComponent;
 
 public interface Update {
 
     public static final int FINE_GRAINED_UPDATE = 0;
     public static final int FULL_REPLACE_UPDATE = 1;
 
     public SComponent getComponent();
 
     public int getProperty();
 
     public int getPriority();
 
     public Handler getHandler();
 
     public interface Handler {
 
         public String getName();
 
        public void addParameter(Object param);

         public Iterator getParameters();
 
     }
 
 }
