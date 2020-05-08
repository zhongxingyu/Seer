 package com.tjh.util;
 
 public enum State {
 
    AE("Armed Forces"),
     AL("Alabama"),
    AP("Armed Forces Pacific"),
     AK("Alaska"),
    AS("American Samoa"),
     AZ("Arizona"),
     AR("Arkansas"),
     CA("California"),
     CO("Colorado"),
     CT("Connecticut"),
     DE("Delaware"),
     DC("District of Columbia"),
    FM("Federated States of Micronesia"),
     FL("Florida"),
     GA("Georgia"),
    GU("Guam"),
     HI("Hawaii"),
     ID("Idaho"),
     IL("Illinois"),
     IN("Indiana"),
     IA("Iowa"),
     KS("Kansas"),
     KY("Kentucky"),
     LA("Louisiana"),
     ME("Maine"),
    MH("Marshall Islands"),
     MD("Maryland"),
     MA("Massachusetts"),
     MI("Michigan"),
     MN("Minnesota"),
     MS("Mississippi"),
     MO("Missouri"),
     MT("Montana"),
     NE("Nebraska"),
     NV("Nevada"),
     NH("New Hampshire"),
     NJ("New Jersey"),
     NM("New Mexico"),
     NY("New York"),
     NC("North Carolina"),
     ND("North Dakota"),
    MP("Northern Mariana Islands"),
     OH("Ohio"),
     OK("Oklahoma"),
     OR("Oregon"),
    PW("Palau"),
     PA("Pennsylvania"),
    PR("Puerto Rico"),
     RI("Rhode Island"),
     SC("South Carolina"),
     SD("South Dakota"),
     TN("Tennessee"),
     TX("Texas"),
     UT("Utah"),
     VT("Vermont"),
    VI("Virgin Islands"),
     VA("Virginia"),
     WA("Washington"),
     WV("West Virginia"),
     WI("Wisconsin"),
     WY("Wyoming");
 
     private final String name;
 
     State(final String name) { this.name = name; }
 
     /**
      * Get state by 2-letter abbreviation
      *
      * @param abbrev 2-letter postal abbreviation
      * @return the State, null if none found
      */
     public static State withAbbreviation(final String abbrev) {
         return Lists.find(values(), new Block<State, Boolean>() {
             public Boolean invoke(final State state) { return state.getAbbreviation().equalsIgnoreCase(abbrev); }
         });
     }
 
     /**
      * Get state by name
      *
      * @param name the state's full name
      * @return the State, null if none found
      */
     public static State withName(final String name) {
         return Lists.find(values(), new Block<State, Boolean>() {
             public Boolean invoke(final State state) { return state.getName().equalsIgnoreCase(name); }
         });
     }
 
     public String getName() { return name; }
 
     public String getAbbreviation() { return name(); }
 }
