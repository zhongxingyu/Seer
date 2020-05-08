 
 package pnlp;
 
 import java.util.*;
 import pnlp.MalariaUnderFiveReport.*;
 import pnlp.MalariaOverFiveReport.*;
 import pnlp.MalariaPregnantWomenReport.*;
 import pnlp.MalariaStockOutsReport.*;
 
 /**
  * Holder for MalariaReport Parts.
  * @author rgaudin
  */
 public class MalariaReport {
 
     private Vector _errors = new Vector();
 
     public MalariaUnderFiveReport under_five;
     public MalariaOverFiveReport over_five;
     public MalariaPregnantWomenReport pregnant_women;
     public MalariaStockOutsReport stock_outs;
     public String username;
     public String password;
     public int month;
     public int year;
 
     public MalariaReport() {
         // load all parts
         under_five = new MalariaUnderFiveReport();
         under_five.loadFromStore();
 
         over_five = new MalariaOverFiveReport();
         over_five.loadFromStore();
 
         pregnant_women = new MalariaPregnantWomenReport();
         pregnant_women.loadFromStore();
 
         stock_outs = new MalariaStockOutsReport();
         stock_outs.loadFromStore();
     }
     
 
     /*
      * generates a string to be used in SMS
      * @return long <code>String</true> describing all data
      * formatted for SMS sending.
      */
     public String toSMSFormat() {
         String sep = " ";
        return "palu" + username + sep + password + sep + month + sep + year + sep +
                under_five.toSMSFormat() + sep + over_five.toSMSFormat() + sep +
                pregnant_women.toSMSFormat() + sep + stock_outs.toSMSFormat();
     }
 
     /*
      * checks whether data is legit
      * checks all logic algorithms and fills the <code>_errors</code> variable.
      * @return <code>true</true> if all data is valid
      * <code>false</code> otherwise.
      */
     public boolean dataIsValid(boolean only_reports) {
 
         _errors = new Vector();
 
         // verify that all parts are present
         if (!under_five.dataIsValid()) {
             _errors.addElement("Les données des moins de 5ans sont incompletes ou erronées");
         }
 
         if (!over_five.dataIsValid()) {
             _errors.addElement("Les données des 5ans et plus sont incompletes ou erronées");
         }
 
         if (!pregnant_women.dataIsValid()) {
             _errors.addElement("Les données des femmes enceintes sont incompletes ou erronées");
         }
 
         if (!stock_outs.dataIsValid()) {
             _errors.addElement("Les données de ruptures de stock sont incompletes ou erronées");
         }
 
         if (!only_reports) {
 
             // verify that meta data is present
            if (!(username.length() >= 6)) {
                 _errors.addElement("L'identifiant semble incorrect.");
             }
 
            if (!(password.length() >= 6)) {
                 _errors.addElement("Le mot de passe semble incorrect.");
             }
 
             if (month < 1 || month > 12) {
                 _errors.addElement("Le mois du rapport est incorrect.");
             }
 
             if (year < 2011 || year > 2020) {
                 _errors.addElement("L'année du rapport est incorrecte.");
             }
         }
 
         if (_errors.size() == 0) {
             return true;
         }
         return false;
     }
 
     /*
      * Overload <code>dataIsValid</code> for default behavior (all checks)
      */
     public boolean dataIsValid() {
         return dataIsValid(false);
     }
 
     /*
      * List all errors on data
      * List all errors detected by <code>isValidData</code>.
      * @return Array of <code>String</true> containing error messages
      * If <code>isValidData</code> has never been called, array is empty
      */
     public String[] errors() {
         String[] all_errors = new String[_errors.size()];
         int i = 0;
         for(Enumeration en = _errors.elements(); en.hasMoreElements();) {
             all_errors[i] = (String) en.nextElement();
             i++;
         }
         return all_errors;
     }
 
     /*
      * Current error message
      * @return <code>String</true> containing the current error message
      * It's the first one in the list.
      */
     public String errorMessage() {
         return (String) _errors.firstElement();
     }
 }
