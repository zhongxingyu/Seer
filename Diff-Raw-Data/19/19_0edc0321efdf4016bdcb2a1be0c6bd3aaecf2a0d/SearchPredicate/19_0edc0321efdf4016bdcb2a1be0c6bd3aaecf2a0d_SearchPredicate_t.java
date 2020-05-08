 package org.mule.galaxy.web.rpc;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class SearchPredicate implements IsSerializable {
     public static final int HAS_VALUE           = 0;
     public static final int LIKE                = 1;
     public static final int DOES_NOT_HAVE_VALUE = 2;
     
     private String property;
     private int matchType;
     private String value;
     
     public SearchPredicate() {
     }
     
     public SearchPredicate(String property, int matchType, String value) {
         this.property   = property;
         this.matchType  = matchType;
        this.value      = value;
     }
     
     public String getProperty() {
         return property;
     }
     
     public void setProperty(String property) {
         this.property = property;
     }
     
     public int getMatchType() {
         return matchType;
     }
     
     public void setMatchType(int matchType) {
         this.matchType = matchType;
     }
     
     public String getValue() {
         return value;
     }
     
     public void setValue(String value) {
         this.value = value;
     }
 }
