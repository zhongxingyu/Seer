 package org.sukrupa.student;
 
 public enum StudentClass {
    DAY_CARE("Day Care", "NURSERY"),
    NURSERY("Nursery", "LKG"),
     LKG("LKG", "UKG"),
     UKG("UKG", "ONE_STD"),
     ONE_STD("1 Std", "TWO_STD"),
     TWO_STD("2 Std", "THREE_STD"),
     THREE_STD("3 Std", "FOUR_STD"),
     FOUR_STD("4 Std", "FIVE_STD"),
     FIVE_STD("5 Std", "SIX_STD"),
     SIX_STD("6 Std", "SEVEN_STD"),
     SEVEN_STD("7 Std", "EIGHT_STD"),
     EIGHT_STD("8 Std", "NINE_STD"),
     NINE_STD("9 Std", "TEN_STD"),
     TEN_STD("10 Std", "TEN_STD");
 
     private String nextClass;
     private String displayName;
 
     StudentClass(String displayName, String nextClass) {
         this.nextClass = nextClass;
         this.displayName = displayName;
     }
 
 
     public StudentClass next() {
         return StudentClass.valueOf(nextClass);
     }
 
     public String displayName() {
         return displayName;
     }
 
     public static StudentClass fromDisplayName(String displayName) {
 
         StudentClass displayNameClass = null;
         for(StudentClass studentClass : StudentClass.values()) {
             if(studentClass.displayName.equals(displayName)) {
                 displayNameClass = studentClass;
             }
         }
         return displayNameClass;
     }
 
 }
