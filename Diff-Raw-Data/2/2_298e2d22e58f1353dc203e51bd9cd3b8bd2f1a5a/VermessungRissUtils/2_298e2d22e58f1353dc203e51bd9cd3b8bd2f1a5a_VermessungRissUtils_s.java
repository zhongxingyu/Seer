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
 package de.cismet.cids.custom.objecteditors.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Gilles Baatz
  * @version  $Revision$, $Date$
  */
 public class VermessungRissUtils {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(VermessungRissUtils.class);
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * The parameter <code>vermessung</code> is a 'vermessung_flurstuecksvermessung'-CidsBean, whose property
      * 'tmp_lp_orig' is a Flurstueck-CidsBean or an Alkis_landparce-CidsBean. The goal of this method is to replace this
      * Flurstuck with a FlurstueckKicker (vermessung_flurstueck_kicker-CidsBean). At the end of the method the property
      * 'flurstueck' will be such a FlurstueckKicker, whereas the property 'tmp_lp_orig' will be null.<br/>
      * This will not be the case if the property 'tmp_lp_orig' is not a a Flurstueck-CidsBean or an
      * Alkis_landparce-CidsBean, or if an Exception was thrown.
      *
      * @param   vermessung  a 'vermessung_flurstuecksvermessung'-CidsBean
      *
      * @throws  ConnectionException  DOCUMENT ME!
      * @throws  Exception            DOCUMENT ME!
      */
     public static void setFluerstueckKickerInVermessung(final CidsBean vermessung) throws ConnectionException,
         Exception {
         final Object tmp = vermessung.getProperty("tmp_lp_orig");
         if (tmp instanceof CidsBean) {
             // For each object find the corresponding kicker
 
             String gemarkung = null;
             String flur = null;
             String zaehler = null;
             String nenner = null;
 
             final CidsBean tmpbean = (CidsBean)tmp;
             CidsBean kicker = null;
 
             // Alternative for flurstueck
             if (tmpbean.getMetaObject().getMetaClass().getTableName().equalsIgnoreCase("flurstueck")) {
                 gemarkung = String.valueOf(tmpbean.getProperty("gemarkungs_nr.gemarkungsnummer"));
                 flur = String.valueOf(tmpbean.getProperty("flur"));
                 zaehler = String.valueOf(tmpbean.getProperty("fstnr_z"));
                 nenner = String.valueOf(tmpbean.getProperty("fstnr_n"));
                 if (nenner == null) {
                     zaehler = "0";
                 }
             } // Alternative for ALKIS_landparcel
             else if (tmpbean.getMetaObject().getMetaClass().getTableName().equalsIgnoreCase(
                             "ALKIS_landparcel")) {
                 gemarkung = ((String)tmpbean.getProperty("alkis_id")).substring(2, 6);
                 flur = (String)tmpbean.getProperty("flur");
                 zaehler = new Integer((String)tmpbean.getProperty("fstck_zaehler")).toString();
                nenner = (String)tmpbean.getProperty("fstck_nenner");
                 if (nenner == null) {
                     nenner = "0";
                 }
             }
 
             // get the kicker
             final MetaClass kickerClass = ClassCacheMultiple.getMetaClass(
                     "WUNDA_BLAU",
                     "vermessung_flurstueck_kicker");
             final StringBuffer kickerQuery = new StringBuffer("select ").append(kickerClass.getId())
                         .append(", ")
                         .append(kickerClass.getPrimaryKey())
                         .append(" from ")
                         .append(kickerClass.getTableName())
                         .append(" where gemarkung=")
                         .append(gemarkung)
                         .append(" and flur='")
                         .append(flur)
                         .append("'")
                         .append(" and zaehler='")
                         .append(zaehler)
                         .append("'")
                         .append(" and nenner='")
                         .append(nenner)
                         .append("'");
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SQL: kickerQuery:" + kickerQuery.toString());
             }
             final MetaObject[] kickers = SessionManager.getProxy().getMetaObjectByQuery(kickerQuery.toString(), 0);
             if (kickers.length > 0) {
                 kicker = kickers[0].getBean();
             }
             // if there is no kicker
             // create a new kicker
             if (kicker == null) {
                 kicker = CidsBean.createNewCidsBeanFromTableName("WUNDA_BLAU", "vermessung_flurstueck_kicker");
 
                 // retrieve the vermessung_gemarkung by id
                 final MetaClass vermessungGemarkungClass = ClassCacheMultiple.getMetaClass(
                         "WUNDA_BLAU",
                         "vermessung_gemarkung");
                 final MetaObject gemarkungObject = SessionManager.getProxy()
                             .getMetaObject(new Integer(gemarkung),
                                 vermessungGemarkungClass.getId(),
                                 "WUNDA_BLAU");
                 final CidsBean vGemarkungBean = gemarkungObject.getBean();
 
                 kicker.setProperty("gemarkung", vGemarkungBean);
                 kicker.setProperty("flur", StringUtils.leftPad(flur, 3, "0"));
                 kicker.setProperty("zaehler", zaehler);
                 kicker.setProperty("nenner", nenner);
 
                 // Check for a real flurstueck that matches
                 final MetaClass flurstueckClass = ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "flurstueck");
                 final StringBuffer fQuery = new StringBuffer("select ").append(flurstueckClass.getId())
                             .append(", ")
                             .append(flurstueckClass.getPrimaryKey())
                             .append(" from ")
                             .append(flurstueckClass.getTableName())
                             .append(" where gemarkungs_nr=")
                             .append(gemarkung)
                             .append(" and flur='")
                             .append(flur)
                             .append("'")
                             .append(" and fstnr_z='")
                             .append(zaehler)
                             .append("'")
                             .append(" and fstnr_n='")
                             .append(nenner)
                             .append("'");
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("SQL: flurstueckQuery:" + fQuery.toString());
                 }
                 final MetaObject[] matchedLandparcels = SessionManager.getProxy()
                             .getMetaObjectByQuery(fQuery.toString(), 0);
                 if (matchedLandparcels.length > 0) {
                     kicker.setProperty("flurstueck", matchedLandparcels[0].getBean());
                 }
             }
             // set it to the "flurstueck" property
             vermessung.setProperty("flurstueck", kicker);
 
             // delete the "tmp_lp_orig" property
             vermessung.setProperty("tmp_lp_orig", null);
         }
     }
 }
