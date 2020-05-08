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
 package de.cismet.belis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObjectNode;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Polygon;
 
 import org.apache.log4j.Logger;
 
 import java.rmi.RemoteException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 import de.cismet.cids.server.search.MetaObjectNodeServerSearch;
 
 import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mroncoroni
  * @version  $Revision$, $Date$
  */
 public class BelisSearchStatement extends AbstractCidsServerSearch implements MetaObjectNodeServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(BelisSearchStatement.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final boolean standort;
     private final boolean schaltstelle;
     private final boolean mauerlasche;
     private final boolean leitung;
     private final boolean abzweigdose;
     private final boolean leuchte;
     private final boolean veranlassung;
     private final boolean arbeitsauftrag;
     private Geometry geometry;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new BelisSearchStatement object.
      */
     public BelisSearchStatement() {
         this(true, false, true, true, true, true, true, true);
     }
 
     /**
      * Creates a new BelisSearchStatement object.
      *
      * @param  standort        DOCUMENT ME!
      * @param  leuchte         DOCUMENT ME!
      * @param  schaltstelle    DOCUMENT ME!
      * @param  mauerlasche     DOCUMENT ME!
      * @param  leitung         DOCUMENT ME!
      * @param  abzweigdose     DOCUMENT ME!
      * @param  veranlassung    DOCUMENT ME!
      * @param  arbeitsauftrag  DOCUMENT ME!
      */
     public BelisSearchStatement(
             final boolean standort,
             final boolean leuchte,
             final boolean schaltstelle,
             final boolean mauerlasche,
             final boolean leitung,
             final boolean abzweigdose,
             final boolean veranlassung,
             final boolean arbeitsauftrag) {
         this.standort = standort;
         this.leuchte = leuchte;
         this.schaltstelle = schaltstelle;
         this.mauerlasche = mauerlasche;
         this.leitung = leitung;
         this.abzweigdose = abzweigdose;
         this.veranlassung = veranlassung;
         this.arbeitsauftrag = arbeitsauftrag;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Geometry getGeometry() {
         return geometry;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geometry  DOCUMENT ME!
      */
     public void setGeometry(final Geometry geometry) {
         this.geometry = geometry;
     }
 
     @Override
     public Collection<MetaObjectNode> performServerSearch() {
         try {
             final MetaService ms = (MetaService)getActiveLocalServers().get("BELIS");
             final MetaClass MC_STANDORT = ms.getClassByTableName(getUser(), "tdta_standort_mast");
             final MetaClass MC_LEUCHTE = ms.getClassByTableName(getUser(), "tdta_leuchte");
             final MetaClass MC_SCHALTSTELLE = ms.getClassByTableName(getUser(), "schaltstelle");
             final MetaClass MC_LEITUNG = ms.getClassByTableName(getUser(), "leitung");
             final MetaClass MC_ABZWEIGDOSE = ms.getClassByTableName(getUser(), "abzweigdose");
             final MetaClass MC_MAUERLASCHE = ms.getClassByTableName(getUser(), "mauerlasche");
             final MetaClass MC_VERANLASSUNG = ms.getClassByTableName(getUser(), "veranlassung");
             final MetaClass MC_ARBEITSAUFTRAG = ms.getClassByTableName(getUser(), "arbeitsauftrag");
 
            if (!standort && !leuchte && !schaltstelle && !mauerlasche && !leitung && !abzweigdose) {
                 return new ArrayList<MetaObjectNode>();
             }
 
             final ArrayList<String> union = new ArrayList<String>();
             final ArrayList<String> join = new ArrayList<String>();
             final ArrayList<String> joinFilter = new ArrayList<String>();
             if (standort) {
                 union.add(
                     "SELECT "
                             + MC_STANDORT.getId()
                             + " AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Standort'::text AS searchIntoClass FROM tdta_standort_mast");
                 join.add(
                     "tdta_standort_mast ON geom_objects.searchIntoClass = 'Standort' AND tdta_standort_mast.id = geom_objects.searchIntoId");
                 joinFilter.add("tdta_standort_mast.id IS NOT null");
             }
             if (leuchte) {
                 union.add(
                     "SELECT "
                             + MC_STANDORT.getId()
                             + " AS classid, tdta_standort_mast.id AS objectid, tdta_leuchten.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Leuchte'::text AS searchIntoClass FROM tdta_leuchten LEFT JOIN tdta_standort_mast ON tdta_leuchten.fk_standort = tdta_standort_mast.id");
                 join.add(
                     "tdta_leuchten ON geom_objects.searchIntoClass = 'Leuchte' AND tdta_leuchten.id = geom_objects.searchIntoId");
                 joinFilter.add("tdta_leuchten.id IS NOT null");
             }
             if (schaltstelle) {
                 union.add(
                     "SELECT "
                             + MC_SCHALTSTELLE.getId()
                             + " AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Schaltstelle'::text AS searchIntoClass FROM schaltstelle");
                 join.add(
                     "schaltstelle ON geom_objects.searchIntoClass = 'Schaltstelle' AND schaltstelle.id = geom_objects.searchIntoId");
                 joinFilter.add("schaltstelle.id IS NOT null");
             }
             if (mauerlasche) {
                 union.add(
                     "SELECT "
                             + MC_MAUERLASCHE.getId()
                             + " AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Mauerlasche'::text AS searchIntoClass FROM mauerlasche");
                 join.add(
                     "mauerlasche ON geom_objects.searchIntoClass = 'Mauerlasche' AND mauerlasche.id = geom_objects.searchIntoId");
                 joinFilter.add("mauerlasche.id IS NOT null");
             }
             if (leitung) {
                 union.add(
                     "SELECT "
                             + MC_LEITUNG.getId()
                             + " AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Leitung'::text AS searchIntoClass FROM leitung");
                 join.add(
                     "leitung ON geom_objects.searchIntoClass = 'Leitung' AND leitung.id = geom_objects.searchIntoId");
                 joinFilter.add("leitung.id IS NOT null");
             }
             if (abzweigdose) {
                 union.add(
                     "SELECT "
                             + MC_ABZWEIGDOSE.getId()
                             + " AS classid, id AS objectid, id AS searchIntoId, fk_geom, 'Abzweigdose'::text AS searchIntoClass FROM abzweigdose");
                 join.add(
                     "abzweigdose ON geom_objects.searchIntoClass = 'Abzweigdose' AND abzweigdose.id = geom_objects.searchIntoId");
                 joinFilter.add("abzweigdose.id IS NOT null");
             }
             if (veranlassung) {
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_leuchte, tdta_leuchten, tdta_standort_mast WHERE veranlassung.ar_leuchten = jt_veranlassung_leuchte.veranlassung_reference AND tdta_leuchten.id = jt_veranlassung_leuchte.fk_leuchte AND tdta_standort_mast.id = tdta_leuchten.fk_standort");
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, leitung.fk_geom AS fk_geom           , 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_leitung, leitung WHERE veranlassung.ar_leitungen = jt_veranlassung_leitung.veranlassung_reference AND leitung.id = jt_veranlassung_leitung.fk_leitung");
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, abzweigdose.fk_geom AS fk_geom       , 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_abzweigdose, abzweigdose WHERE veranlassung.ar_leitungen = jt_veranlassung_abzweigdose.veranlassung_reference AND abzweigdose.id = jt_veranlassung_abzweigdose.fk_abzweigdose");
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_standort, tdta_standort_mast WHERE veranlassung.ar_standorte = jt_veranlassung_standort.veranlassung_reference AND tdta_standort_mast.id = jt_veranlassung_standort.fk_standort");
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, schaltstelle.fk_geom AS fk_geom      , 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_schaltstelle, schaltstelle WHERE veranlassung.ar_schaltstellen = jt_veranlassung_schaltstelle.veranlassung_reference AND schaltstelle.id = jt_veranlassung_schaltstelle.fk_schaltstelle");
                 union.add("SELECT " + MC_VERANLASSUNG.getId()
                             + " AS classid, veranlassung.id AS objectid, veranlassung.id AS searchIntoId, mauerlasche.fk_geom AS fk_geom       , 'Veranlassung'::text AS searchIntoClass FROM veranlassung, jt_veranlassung_mauerlasche, mauerlasche WHERE veranlassung.ar_mauerlaschen = jt_veranlassung_mauerlasche.veranlassung_reference AND mauerlasche.id = jt_veranlassung_mauerlasche.fk_mauerlasche");
                 join.add(
                     "veranlassung ON geom_objects.searchIntoClass = 'Veranlassung' AND veranlassung.id = geom_objects.searchIntoId");
                 joinFilter.add("veranlassung.id IS NOT null");
             }
             if (arbeitsauftrag) {
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, tdta_leuchten, tdta_standort_mast WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND tdta_leuchten.id = arbeitsprotokoll.fk_leuchte AND tdta_standort_mast.id = tdta_leuchten.fk_standort");
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, leitung.fk_geom AS fk_geom           , 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, leitung WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND leitung.id = arbeitsprotokoll.fk_leitung");
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, abzweigdose.fk_geom AS fk_geom       , 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, abzweigdose WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND abzweigdose.id = arbeitsprotokoll.fk_abzweigdose");
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, tdta_standort_mast.fk_geom AS fk_geom, 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, tdta_standort_mast WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND tdta_standort_mast.id = arbeitsprotokoll.fk_standort");
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, schaltstelle.fk_geom AS fk_geom      , 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, schaltstelle WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND schaltstelle.id = arbeitsprotokoll.fk_schaltstelle");
                 union.add("SELECT " + MC_ARBEITSAUFTRAG.getId()
                             + " AS classid, arbeitsauftrag.id AS objectid, arbeitsauftrag.id AS searchIntoId, mauerlasche.fk_geom AS fk_geom       , 'Arbeitsauftrag'::text AS searchIntoClass FROM arbeitsauftrag, arbeitsprotokoll, mauerlasche WHERE arbeitsprotokoll.fk_arbeitsauftrag = arbeitsauftrag.id AND mauerlasche.id = arbeitsprotokoll.fk_mauerlasche");
                 join.add(
                     "arbeitsauftrag ON geom_objects.searchIntoClass = 'Arbeitsauftrag' AND arbeitsauftrag.id = geom_objects.searchIntoId");
                 joinFilter.add("arbeitsauftrag.id IS NOT null");
             }
             final String implodedUnion = implodeArray(union.toArray(new String[0]), " UNION ");
             final String implodedJoin = (joinFilter.isEmpty())
                 ? "" : (" LEFT JOIN " + implodeArray(join.toArray(new String[0]), " LEFT JOIN "));
             final String implodedJoinFilter = implodeArray(joinFilter.toArray(new String[0]), " OR ");
 
             String query = "SELECT DISTINCT classid, objectid"
                         + " FROM (" + implodedUnion + ") AS geom_objects"
                         + " " + implodedJoin + ", geom"
                         + " WHERE geom.id = geom_objects.fk_geom"
                         + " AND (" + implodedJoinFilter + ")";
 
             if (geometry != null) {
                 final String geostring = PostGisGeometryFactory.getPostGisCompliantDbString(geometry);
                 if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
                     query += " AND geo_field &&\n"
                                 + "st_buffer(\n"
                                 + "GeometryFromText('" + geostring + "')\n"
                                 + ", 0.000001)\n"
                                 + "and intersects(geo_field,st_buffer(GeometryFromText('" + geostring
                                 + "'), 0.000001))";
                 } else {
                     query += " AND geo_field &&\n"
                                 + "st_buffer(\n"
                                 + "GeometryFromText('" + geostring + "')\n"
                                 + ", 0.000001)\n"
                                 + "and intersects(geo_field, GeometryFromText('" + geostring + "'))";
                 }
             }
 
             final String andQueryPart = getAndQueryPart();
             if ((andQueryPart != null) && !andQueryPart.trim().isEmpty()) {
                 query += " AND " + andQueryPart;
             }
 
             final List<MetaObjectNode> result = new ArrayList<MetaObjectNode>();
             final ArrayList<ArrayList> searchResult = ms.performCustomSearch(query);
             LOG.info(query);
             for (final ArrayList al : searchResult) {
                 final int cid = (Integer)al.get(0);
                 final int oid = (Integer)al.get(1);
                 final MetaObjectNode mon = new MetaObjectNode("BELIS", oid, cid, null);
                 result.add(mon);
             }
 
             return result;
         } catch (RemoteException ex) {
             LOG.error("Problem", ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected String getAndQueryPart() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   id     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateIdQuery(final String field, final Integer id) {
         final String query;
         if (id != null) {
             query = field + " = " + id + "";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   like   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLikeQuery(final String field, final String like) {
         final String query;
         if (like != null) {
             query = field + " like '%" + like + "%'";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   field  DOCUMENT ME!
      * @param   von    DOCUMENT ME!
      * @param   bis    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateVonBisQuery(final String field, final String von, final String bis) {
         final String query;
         if (von != null) {
             if (bis != null) {
                 query = field + " BETWEEN '" + von + "' AND '" + bis + "'";
             } else {
                 query = field + " >= '" + von + "'";
             }
         } else if (bis != null) {
             query = field + " <= '" + bis + "'";
         } else {
             query = "TRUE";
         }
         return query;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   inputArray  DOCUMENT ME!
      * @param   glueString  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String implodeArray(final String[] inputArray, final String glueString) {
         String output = "";
         if (inputArray.length > 0) {
             final StringBuilder sb = new StringBuilder();
             sb.append(inputArray[0]);
             for (int i = 1; i < inputArray.length; i++) {
                 sb.append(glueString);
                 sb.append(inputArray[i]);
             }
             output = sb.toString();
         }
         return output;
     }
 }
