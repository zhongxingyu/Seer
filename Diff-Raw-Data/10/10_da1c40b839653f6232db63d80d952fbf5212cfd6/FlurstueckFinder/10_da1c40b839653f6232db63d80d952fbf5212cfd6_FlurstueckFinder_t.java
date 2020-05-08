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
 package de.cismet.cids.custom.objectrenderer.utils;
 
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.LightweightMetaObject;
 import Sirius.server.middleware.types.MetaObject;
 
 import java.util.Arrays;
 import java.util.Comparator;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class FlurstueckFinder {
 
     //~ Static fields/initializers ---------------------------------------------
 
    public static final String FLURSTUECK_TABLE_NAME = "alb_flurstueck_kicker";
     public static final String GEMARKUNG_TABLE_NAME = "gemarkung";
     public static final String GEMARKUNG_ID = "gemarkungsnummer";
     public static final String GEMARKUNG_NAME = "name";
    public static final String FLURSTUECK_GEMARKUNG = "gemarkung";
     public static final String FLURSTUECK_FLUR = "flur";
    public static final String FLURSTUECK_ZAEHLER = "zaehler";
    public static final String FLURSTUECK_NENNER = "nenner";
     public static final String SEP = " - ";
     //
     private static final String STMNT_LANDPARCELS = "select id," + FLURSTUECK_GEMARKUNG + "," + FLURSTUECK_FLUR + ","
                 + FLURSTUECK_ZAEHLER + "," + FLURSTUECK_NENNER + " from " + FLURSTUECK_TABLE_NAME + " order by "
                 + FLURSTUECK_GEMARKUNG + ", " + FLURSTUECK_FLUR + ", " + FLURSTUECK_ZAEHLER + ", " + FLURSTUECK_NENNER;
     private static final Comparator<MetaObject> ZAEHLER_NENNER_COMPARATOR = new Comparator<MetaObject>() {
 
             @Override
             public final int compare(final MetaObject o1, final MetaObject o2) {
                 // DANGER!
                 final LightweightMetaObject lwmo1 = (LightweightMetaObject)o1;
                 final LightweightMetaObject lwmo2 = (LightweightMetaObject)o2;
                 int res = AlphanumComparator.getInstance()
                             .compare(lwmo1.getLWAttribute(FLURSTUECK_NENNER), lwmo2.getLWAttribute(FLURSTUECK_NENNER));
                 if (res == 0) {
                     res = AlphanumComparator.getInstance()
                                 .compare(lwmo1.getLWAttribute(FLURSTUECK_ZAEHLER),
                                         lwmo2.getLWAttribute(FLURSTUECK_ZAEHLER));
                 }
                 return res;
             }
         };
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWLandparcels() {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery(
                 FLURSTUECK_TABLE_NAME,
                 STMNT_LANDPARCELS,
                 new String[] { FLURSTUECK_GEMARKUNG, FLURSTUECK_FLUR, FLURSTUECK_ZAEHLER, FLURSTUECK_NENNER },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         final StringBuilder result = new StringBuilder(30);
                         result.append(getAttribute(FLURSTUECK_GEMARKUNG)).append("-");
                         result.append(getAttribute(FLURSTUECK_FLUR)).append("-");
                         result.append(getAttribute(FLURSTUECK_ZAEHLER));
                         final Object nenner = getAttribute(FLURSTUECK_NENNER);
                         if (nenner != null) {
                             result.append("/").append(nenner);
                         }
                         return result.toString();
                     }
                 });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWGemarkungen() {
         final MetaObject[] moa = ObjectRendererUtils.getLightweightMetaObjectsForQuery(
                 FLURSTUECK_TABLE_NAME,
                 "select min(id) as id, "
                         + FLURSTUECK_GEMARKUNG
                         + ", min("
                         + GEMARKUNG_NAME
                         + ") as "
                         + GEMARKUNG_NAME
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " join "
                         + GEMARKUNG_TABLE_NAME
                         + " on "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + GEMARKUNG_ID
                         + " group by "
                         + FLURSTUECK_GEMARKUNG
                         + " order by "
                         + FLURSTUECK_GEMARKUNG,
                 new String[] { "id", FLURSTUECK_GEMARKUNG, GEMARKUNG_NAME },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         return String.valueOf(getAttribute(FLURSTUECK_GEMARKUNG));
                     }
                 });
         return moa;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gemarkungsnummer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWFlure(final String gemarkungsnummer) {
         final MetaObject[] result = ObjectRendererUtils.getLightweightMetaObjectsForQuery(
 
                 FLURSTUECK_TABLE_NAME,
                 "select min(id) as id, "
                         + FLURSTUECK_FLUR
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " where "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + gemarkungsnummer
                         + " group by "
                         + FLURSTUECK_FLUR
                         + " order by "
                         + FLURSTUECK_FLUR,
                 new String[] { "id", FLURSTUECK_FLUR },
                 new AbstractAttributeRepresentationFormater() {
 
 //                FLURSTUECK_TABLE_NAME, "select min(id) as id, " + FLURSTUECK_FLUR + " from " + FLURSTUECK_TABLE_NAME + " where " + FLURSTUECK_GEMARKUNG + " = " + gemarkungsnummer + " group by " + FLURSTUECK_FLUR, new String[]{"id", FLURSTUECK_FLUR}, new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         return String.valueOf(getAttribute(FLURSTUECK_FLUR));
                     }
                 });
 //        Arrays.sort(result, AlphanumComparator.getInstance());
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gemarkungsnummer  DOCUMENT ME!
      * @param   flur              DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWFurstuecksZaehlerNenner(final String gemarkungsnummer, final String flur) {
         final MetaObject[] result = ObjectRendererUtils.getLightweightMetaObjectsForQuery(
 
                 // FLURSTUECK_TABLE_NAME, "select min(id) as id, " + FLURSTUECK_ZAEHLER + ", " + FLURSTUECK_NENNER + "
                 // from " + FLURSTUECK_TABLE_NAME + " where " + FLURSTUECK_GEMARKUNG + " = " + gemarkungsnummer + " and
                 // " + FLURSTUECK_FLUR + " = '" + flur + "' group by " + FLURSTUECK_ZAEHLER + ", " + FLURSTUECK_NENNER +
                 // " order by " + FLURSTUECK_ZAEHLER + ", " + FLURSTUECK_NENNER
                 FLURSTUECK_TABLE_NAME,
                 "select min(id) as id, "
                         + FLURSTUECK_ZAEHLER
                         + ", "
                         + FLURSTUECK_NENNER
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " where "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + gemarkungsnummer
                         + " and "
                         + FLURSTUECK_FLUR
                         + " = '"
                         + flur
                         + "' group by "
                         + FLURSTUECK_ZAEHLER
                         + ", "
                         + FLURSTUECK_NENNER,
                 new String[] { "id", FLURSTUECK_ZAEHLER, FLURSTUECK_NENNER },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         final Object nenner = getAttribute(FLURSTUECK_NENNER);
                         final StringBuilder result = new StringBuilder();
                         result.append(getAttribute(FLURSTUECK_ZAEHLER));
                         if (nenner != null) {
                             result.append("/").append(nenner);
                         }
                         return result.toString();
                     }
                 });
         Arrays.sort(result, ZAEHLER_NENNER_COMPARATOR);
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gemarkungsnummer  DOCUMENT ME!
      * @param   flur              DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWFurstuecksZaehler(final String gemarkungsnummer, final String flur) {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery(
 
                 // FLURSTUECK_TABLE_NAME, "select min(id) as id, " + FLURSTUECK_ZAEHLER + " from " +
                 // FLURSTUECK_TABLE_NAME + " where " + FLURSTUECK_GEMARKUNG + " = " + gemarkungsnummer + " and " +
                 // FLURSTUECK_FLUR + " = '" + flur + "' group by " + FLURSTUECK_ZAEHLER + " order by " +
                 // FLURSTUECK_ZAEHLER
                 FLURSTUECK_TABLE_NAME,
                 "select min(id) as id, "
                         + FLURSTUECK_ZAEHLER
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " where "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + gemarkungsnummer
                         + " and "
                         + FLURSTUECK_FLUR
                         + " = '"
                         + flur
                         + "' group by "
                         + FLURSTUECK_ZAEHLER,
                 new String[] { "id", FLURSTUECK_ZAEHLER },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         return String.valueOf(getAttribute(FLURSTUECK_ZAEHLER));
                     }
                 });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gemarkungsnummer  DOCUMENT ME!
      * @param   flur              DOCUMENT ME!
      * @param   zaehler           DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWFurstuecksNenner(final String gemarkungsnummer,
             final String flur,
             final String zaehler) {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery(
                 FLURSTUECK_TABLE_NAME,
                 "select id, "
                         + FLURSTUECK_NENNER
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " where "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + gemarkungsnummer
                         + " and "
                         + FLURSTUECK_FLUR
                         + " = '"
                         + flur
                         + "' and "
                         + FLURSTUECK_ZAEHLER
                         + " = '"
                         + zaehler
                         + "' order by "
                         + FLURSTUECK_NENNER,
                 new String[] { "id", FLURSTUECK_NENNER },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         return String.valueOf(getAttribute(FLURSTUECK_NENNER));
                     }
                 });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   gemarkungsnummer  DOCUMENT ME!
      * @param   flur              DOCUMENT ME!
      * @param   zaehler           DOCUMENT ME!
      * @param   nenner            DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MetaObject[] getLWLandparcel(final String gemarkungsnummer,
             final String flur,
             final String zaehler,
             final String nenner) {
         return ObjectRendererUtils.getLightweightMetaObjectsForQuery(
                 FLURSTUECK_TABLE_NAME,
                 "select id, "
                         + FLURSTUECK_GEMARKUNG
                         + ","
                         + FLURSTUECK_FLUR
                         + ","
                         + FLURSTUECK_ZAEHLER
                         + ","
                         + FLURSTUECK_NENNER
                         + " from "
                         + FLURSTUECK_TABLE_NAME
                         + " where "
                         + FLURSTUECK_GEMARKUNG
                         + " = "
                         + gemarkungsnummer
                         + " and "
                         + FLURSTUECK_FLUR
                         + " = '"
                         + flur
                         + "' and "
                         + FLURSTUECK_ZAEHLER
                         + " = '"
                         + zaehler
                         + "' and "
                         + FLURSTUECK_NENNER
                         + " = '"
                         + nenner
                         + "'",
                 new String[] { "id", FLURSTUECK_GEMARKUNG, FLURSTUECK_FLUR, FLURSTUECK_ZAEHLER, FLURSTUECK_NENNER },
                 new AbstractAttributeRepresentationFormater() {
 
                     @Override
                     public String getRepresentation() {
                         final StringBuilder result = new StringBuilder(30);
                         result.append(getAttribute(FLURSTUECK_GEMARKUNG)).append("-");
                         result.append(getAttribute(FLURSTUECK_FLUR)).append("-");
                         result.append(getAttribute(FLURSTUECK_ZAEHLER));
                         final Object nenner = getAttribute(FLURSTUECK_NENNER);
                         if (nenner != null) {
                             result.append("/").append(nenner);
                         }
                         return result.toString();
                     }
                 });
     }
 }
