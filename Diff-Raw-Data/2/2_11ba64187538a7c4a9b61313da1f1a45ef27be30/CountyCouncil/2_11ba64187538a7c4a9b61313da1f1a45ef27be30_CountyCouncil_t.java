 package com.github.croesch.partimana.types;
 
 import com.github.croesch.annotate.NotNull;
 import com.github.croesch.partimana.i18n.Text;
 
 /**
  * Represents the available county councils.
  * 
  * @author croesch
  * @since Date: Jun 16, 2011
  */
 public enum CountyCouncil {
 
   /** unknown county council */
   UNKNOWN (Text.UNKNOWN),
 
   /** county of alzey */
   COUNTY_ALZEY (Text.COUNTY_ALZEY),
 
   /** county of bad kreuznach */
   COUNTY_BAD_KREUZNACH (Text.COUNTY_BAD_KREUZNACH),
 
   /** county of bad duerkheim */
  COUNTY_BAD_DUERKHEIM (Text.COUNTY_BAD_DUERKHEIM),
 
   /** city of frankenthal */
   CITY_FRANKENTHAL (Text.CITY_FRANKENTHAL),
 
   /** county of germersheim */
   COUNTY_GERMERSHEIM (Text.COUNTY_GERMERSHEIM),
 
   /** city of kaiserslautern */
   CITY_KAISERSLAUTERN (Text.CITY_KAISERSLAUTERN),
 
   /** county of kaiserslautern */
   COUNTY_KAISERSLAUTERN (Text.COUNTY_KAISERSLAUTERN),
 
   /** county of kirchheimbolanden */
   COUNTY_KIRCHHEIMBOLANDEN (Text.COUNTY_KIRCHHEIMBOLANDEN),
 
   /** county of kusel */
   COUNTY_KUSEL (Text.COUNTY_KUSEL),
 
   /** city of landau */
   CITY_LANDAU (Text.CITY_LANDAU),
 
   /** city of ludwigshafen */
   CITY_LUDWIGSHAFEN (Text.CITY_LUDWIGSHAFEN),
 
   /** county of rhein-pfalz */
   COUNTY_RHEIN_PFALZ (Text.COUNTY_RHEIN_PFALZ),
 
   /** city of neustadt */
   CITY_NEUSTADT (Text.CITY_NEUSTADT),
 
   /** city of pirmasens */
   CITY_PIRMASENS (Text.CITY_PIRMASENS),
 
   /** city of speyer */
   CITY_SPEYER (Text.CITY_SPEYER),
 
   /** county of suedliche weinstrasse */
   COUNTY_SUEDLICHE_WEINSTRASSE (Text.COUNTY_SUEDLICHE_WEINSTRASSE),
 
   /** county of */
   COUNTY_SUEDWESTPFALZ (Text.COUNTY_SUEDWESTPFALZ),
 
   /** city of zweibruecken */
   CITY_ZWEIBRUECKEN (Text.CITY_ZWEIBRUECKEN),
 
   /** another county council */
   OTHER (Text.OTHER);
 
   /** the i18n representation of this county council */
   @NotNull
   private final String s;
 
   /**
    * Constructs a {@link CountyCouncil} with the given i18n representation of the specific county council.
    * 
    * @author croesch
    * @since Date: Jun 21, 2011
    * @param t the {@link Text} that represents this council.
    */
   private CountyCouncil(final Text t) {
     this.s = t.text(); //FIXME null check!
   }
 
   @Override
   @NotNull
   public String toString() {
     return this.s;
   }
 }
