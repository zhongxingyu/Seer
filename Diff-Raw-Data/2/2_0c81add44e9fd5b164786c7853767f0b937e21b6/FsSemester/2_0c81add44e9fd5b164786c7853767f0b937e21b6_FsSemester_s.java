 package no.uis.service.studinfo.data;
 
 import javax.xml.bind.annotation.XmlEnum;
 import javax.xml.bind.annotation.XmlType;
 
@XmlType(name = "YESNOType")
 @XmlEnum
 public enum FsSemester {
 
   HOST("HØST"),
   VAR("VÅR");
   
   private String val;
   
   FsSemester(String str) {
     this.val = str;
   }
   
   public static FsSemester stringToUisSemester(String str) {
     for (FsSemester sem : FsSemester.values()) {
       if (sem.val.equals(str)) {
         return sem;
       }
     }
     throw new IllegalArgumentException(str);
   }
   
   @Override
   public String toString() {
     return val;
   }
 }
