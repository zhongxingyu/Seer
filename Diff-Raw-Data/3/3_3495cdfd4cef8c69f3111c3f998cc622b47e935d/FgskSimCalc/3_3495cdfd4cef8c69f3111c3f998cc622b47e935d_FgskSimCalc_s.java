 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.wrrl_db_mv.fgsksimulation;
 
 import org.apache.log4j.Logger;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 /**
  * DOCUMENT ME!
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class FgskSimCalc {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static Logger LOG = Logger.getLogger(FgskSimCalc.class);
     public static int THRESHHOLD = 90;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new FgskSimCalc object.
      */
     private FgskSimCalc() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static FgskSimCalc getInstance() {
         return LazyInitializer.INSTANCE;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   wBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int calcFgskSum(final CidsBean wBean) {
         int sum = 0;
 
         sum += toInt((Integer)wBean.getProperty("anzahl_laengsbaenken_mvs"));
         sum += toInt((Integer)wBean.getProperty("substratdiversitaet"));
         sum += toInt((Integer)wBean.getProperty("kruemmungserosion"));
         sum += toInt((Integer)wBean.getProperty("flaechennutzung"));
         sum += toInt((Integer)wBean.getProperty("laufkruemmung"));
         sum += toInt((Integer)wBean.getProperty("stroemungsdiversitaet"));
         sum += toInt((Integer)wBean.getProperty("anzahl_querbaenke_mvs"));
         sum += toInt((Integer)wBean.getProperty("anzahl_besonderer_uferstrukturen"));
         sum += toInt((Integer)wBean.getProperty("gewaesserrandstreifen"));
         sum += toInt((Integer)wBean.getProperty("anzahl_besonderer_laufstrukturen"));
         sum += toInt((Integer)wBean.getProperty("sonstige_umfeldstrukturen"));
         sum += toInt((Integer)wBean.getProperty("breitenvarianz"));
         sum += toInt((Integer)wBean.getProperty("anzahl_besonderer_sohlstrukturen"));
         sum += toInt((Integer)wBean.getProperty("sohltiefe_obere_profilbreite"));
         sum += toInt((Integer)wBean.getProperty("tiefenvarianz"));
         sum += toInt((Integer)wBean.getProperty("uferbewuchs"));
         sum += toInt((Integer)wBean.getProperty("sohlverbau"));
         sum += toInt((Integer)wBean.getProperty("uferverbau"));
         sum += toInt((Integer)wBean.getProperty("breitenerosion"));
         sum += toInt((Integer)wBean.getProperty("profiltyp"));
 
         return sum;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   wBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int calcBioSum(final CidsBean wBean) {
         int sum = 0;
 
         sum += toInt((Integer)wBean.getProperty("fische"));
         sum += toInt((Integer)wBean.getProperty("makrozoobenthos"));
         sum += toInt((Integer)wBean.getProperty("makrophyten"));
 
         return sum;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   kaBean  DOCUMENT ME!
      * @param   rule    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isRuleFulfilled(final CidsBean kaBean, final CidsBean rule) {
         final String evalCode = (String)rule.getProperty("tester");
         try {
             if (evalCode != null) {
                 final String json = kaBean.toJSONString(false);
                 final ScriptEngineManager manager = new ScriptEngineManager();
                 final ScriptEngine engine = manager.getEngineByName("js");
                 String code = "var ka= " + json + ";";
                 code += "\n " + evalCode;
 
                 final Object result = engine.eval(code);
 
                 if (result instanceof Boolean) {
                     return ((Boolean)result).booleanValue();
                 } else {
                     LOG.warn("Test does not end with a boolean. Code: " + code);
                     return false;
                 }
             } else {
                 return true;
             }
         } catch (Exception ex) {
             LOG.error("Error while check the following rule: " + evalCode, ex);
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   i  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int toInt(final Integer i) {
         if (i == null) {
             return 0;
         } else {
             return i.intValue();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   kaBean     DOCUMENT ME!
      * @param   simMaBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public double calcCosts(final CidsBean kaBean, final CidsBean simMaBean) throws Exception {
         try {
             final ScriptEngineManager manager = new ScriptEngineManager();
             final ScriptEngine engine = manager.getEngineByName("js");
             String costFormula = (String)simMaBean.getProperty("kosten");
             String calculationRule = (String)simMaBean.getProperty("kostenformel");
 
             if (calculationRule == null) {
                 return 0.0;
             }
 
             if (costFormula != null) {
                 costFormula = replaceVariables(costFormula, kaBean);
                 final Object costs = engine.eval(costFormula);
 
                 if (costs == null) {
                     LOG.warn("Costs are null");
                 }
 
                 calculationRule = calculationRule.replaceAll("KOSTEN", String.valueOf(costs));
             }
 
             calculationRule = replaceVariables(calculationRule, kaBean);
 
             final Object costs = engine.eval(calculationRule);
 
             if (costs instanceof Number) {
                 return ((Number)costs).doubleValue();
             } else {
                 final String message = "illegal cost settings: " + calculationRule; // NOI18N
                 LOG.error(message);
                 throw new IllegalStateException(message);
             }
         } catch (final Exception e) {
             LOG.error("Error while calculating costs", e);
             throw e;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   formula  DOCUMENT ME!
      * @param   kaBean   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String replaceVariables(final String formula, final CidsBean kaBean) {
         String breadth = (String)kaBean.getProperty("gewaesserbreite_id.name");
         final Double bedBreadth = (Double)kaBean.getProperty("sohlenbreite");
         final Integer wbType = (Integer)kaBean.getProperty("gewaessertyp_id.value");
         final Double sohlsubstrKuenst = (Double)kaBean.getProperty("sohlensubstrat_kue");
 
         if (breadth != null) {
             breadth = "\"" + breadth + "\"";
         }
 
         String newFormula = formula.replaceAll("LAENGE", String.valueOf(getKaLength(kaBean)));
         newFormula = newFormula.replaceAll("BREITE", String.valueOf(breadth));
         newFormula = newFormula.replaceAll("TYP", String.valueOf(wbType));
         newFormula = newFormula.replaceAll("SUBSTRAT", String.valueOf(sohlsubstrKuenst));
        newFormula = newFormula.replaceAll("SOHLBREITE", String.valueOf(bedBreadth));
 
         return newFormula;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   kaBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     private double getKaLength(final CidsBean kaBean) {
         try {
             final Double toValue = (Double)kaBean.getProperty("linie.bis.wert");
             final Double fromValue = (Double)kaBean.getProperty("linie.von.wert");
 
             return Math.abs(toValue - fromValue);
         } catch (final Exception e) {
             final String message = "illegal station settings in kartierabschnitt"; // NOI18N
             LOG.error(message, e);
             throw new IllegalStateException(message, e);
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class LazyInitializer {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final transient FgskSimCalc INSTANCE = new FgskSimCalc();
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LazyInitializer object.
          */
         private LazyInitializer() {
         }
     }
 }
