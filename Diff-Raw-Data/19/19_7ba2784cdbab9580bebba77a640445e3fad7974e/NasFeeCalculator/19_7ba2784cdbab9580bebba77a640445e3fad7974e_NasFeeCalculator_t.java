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
 package de.cismet.cids.custom.nas;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.List;
 
import de.cismet.cids.custom.objectrenderer.utils.billing.ProductGroupAmount;
import de.cismet.cids.custom.utils.nas.NasProductTemplate;
 import de.cismet.cids.custom.wunda_blau.search.actions.NasZaehlObjekteSearch;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsMeasurementPointSearchStatement;
 import de.cismet.cids.custom.wunda_blau.search.server.CidsMeasurementPointSearchStatement.Pointtype;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public abstract class NasFeeCalculator {
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   amount  template DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     /**
      * DOCUMENT ME!
      *
      * @param   amount  g DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static double getFeeForPoints(final int amount) {
         return calculateFeeWithDiscount(amount, 0.2d);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   amount  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static double getFeeForFlurstuecke(final int amount) {
         return calculateFeeWithDiscount(amount, 1.98d);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   amount  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static double getFeeForGebaeude(final int amount) {
         return calculateFeeWithDiscount(amount, 1.8d);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   amount  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static double getFeeForEigentuemer(final int amount) {
         return calculateFeeWithDiscount(amount, 0.9d);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   g  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     public static int getPointAmount(final Geometry g) throws ConnectionException {
         final ArrayList<Pointtype> pointtypes = new ArrayList<Pointtype>();
         pointtypes.add(Pointtype.AUFNAHMEPUNKTE);
         pointtypes.add(Pointtype.SONSTIGE_VERMESSUNGSPUNKTE);
         pointtypes.add(Pointtype.GRENZPUNKTE);
         pointtypes.add(Pointtype.BESONDERE_GEBAEUDEPUNKTE);
         pointtypes.add(Pointtype.BESONDERE_BAUWERKSPUNKTE);
         pointtypes.add(Pointtype.BESONDERE_TOPOGRAPHISCHE_PUNKTE);
         final CidsMeasurementPointSearchStatement search = new CidsMeasurementPointSearchStatement(
                 null,
                 pointtypes,
                 null,
                 g);
         final Collection searchResult;
         searchResult = SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), search);
         return searchResult.size();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   g  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     public static int getFlurstueckAmount(final Geometry g) throws ConnectionException {
         final NasZaehlObjekteSearch flurstueckSearch = new NasZaehlObjekteSearch(
                 g,
                 NasZaehlObjekteSearch.NasSearchType.FLURSTUECKE);
 
         final ArrayList<Integer> c = (ArrayList<Integer>)SessionManager.getProxy()
                     .customServerSearch(SessionManager.getSession().getUser(), flurstueckSearch);
         return c.get(0);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   g  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     public static int getGebaeudeAmount(final Geometry g) throws ConnectionException {
         final NasZaehlObjekteSearch gebaeudeSearch = new NasZaehlObjekteSearch(
                 g,
                 NasZaehlObjekteSearch.NasSearchType.GEBAEUDE);
 
         final ArrayList<Integer> c = (ArrayList<Integer>)SessionManager.getProxy()
                     .customServerSearch(SessionManager.getSession().getUser(), gebaeudeSearch);
         return c.get(0);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   amount     DOCUMENT ME!
      * @param   basePrice  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static double calculateFeeWithDiscount(int amount, final double basePrice) {
         double fee = 0;
         if (amount > 1000000) {
             final int tmpPoints = amount - 1000000;
             fee += tmpPoints * basePrice * 0.0625;
             amount = 1000000;
        }
        if (amount > 100000) {
             final int tmpPpoints = amount - 100000;
             fee += tmpPpoints * basePrice * 0.125;
             amount = 100000;
        }
        if (amount > 10000) {
             final int tmpPoints = amount - 10000;
             fee += tmpPoints * basePrice * 0.25d;
             amount = 10000;
        }
        if (amount > 1000) {
             final int tmpPoints = amount - 1000;
             fee += tmpPoints * basePrice * 0.5d;
             amount = 1000;
         }
         fee += amount * 0.2d;
         return fee;
     }
 }
