 package de.metalab.namnam.model;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Currency;
 import java.util.Locale;
 
 /**
  * diese klase repraesentiert einen preis, und arbeitet intern mit cent-betraegen.
  *
  * @author fake
  * @author nati
  * @author testicle
  */
 public class Preis {
 
     private Integer cents;
     private Currency currency = Currency.getInstance(Locale.GERMANY);
 
     public Preis() {
     }
 
     /**
      * konstruktor, der den preis gleich mit einem wert initialisiert
      * @param value der wert in cent
      */
     public Preis(Integer value) {
         this.cents = value;
     }
 
     /**
      * konstruktor, der den preis gleich mit einem wert initialisiert und
      * dessen waehrung festlegt.
      *
      * @param value der wert in cent
     * @param currency die waerhung - ein bischen bogus, weil oben ja schon "cent" steht ^^ zumindest ein bisschen formatierung wird momentan benutzt.
      */
     public Preis(Integer value, Currency currency) {
         this.cents = value;
         this.currency = currency;
     }
 
     /**
      * gibt diesen preis in cent zurueck
      * @return dieser preis in cent
      */
     public Integer getCents() {
         return cents;
     }
     /**
      * setzt diesen preis in cent
      * @param cents dieser preis in cent
      */
     public void setCents(Integer cents) {
         this.cents = cents;
     }
 
     /**
      * gibt die waehrung dieses preises zurueck
      * @return die waehrung diese preises
      */
     public Currency getCurrency() {
         return currency;
     }
     /**
      * setzt die waehrung dieses preises
      * @param currency die waehrung dieses preises
      */
     public void setCurrency(Currency currency) {
         this.currency = currency;
     }
 
     /**
      * gibt eine schoen string-representation dieses preises, allerdings leider ohne
      * waehrungssymbol zurueck. es fliesst jedoch z.b. die wahl der tausender- und
      * dezimaltrennzeichen sowie die anzahl der ueblicherweise dargestellten nach-
      * kommastellen aus der waehrung in diese formatierung ein.
      *
      * @return ein schoener string. einfach ausprobieren!
      */
     @Override
     public String toString() {
         DecimalFormat nf = new DecimalFormat();
         DecimalFormatSymbols decf = new DecimalFormatSymbols(Locale.GERMAN);
         nf.setDecimalFormatSymbols(decf);
         nf.setMinimumFractionDigits(currency.getDefaultFractionDigits());
         return nf.format(cents/100.0);
     }
 
 
 }
