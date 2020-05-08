 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.lmb97.util;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import net.sourceforge.stripes.format.Formatter;
 import org.lmb97.data.Seasons;
 
 /**
  *
  * @author javier
  */
 public class SeasonFormatter implements Formatter<Seasons>{
     
     @Override
     public void setFormatType(String formatType) {
     }
 
     @Override
     public void setFormatPattern(String formatPattern) {
     }
 
     @Override
     public void setLocale(Locale locale) {
     }
 
     @Override
     public void init() {
     }
 
     @Override
     public String format(Seasons season) {
         String season_output;
         String spell_output;
         SimpleDateFormat dateFormatter;
         dateFormatter=new SimpleDateFormat("yyyy");
         season_output=dateFormatter.format(season.getYear());
         if(season.getSpell())
             spell_output="Invierno";
         else
             spell_output="Verano";
        return season_output+": "+spell_output;
     }
 }
