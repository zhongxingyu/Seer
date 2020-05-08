 /*
  * Copyright (c) 2006 Zauber  -- All rights reserved
  */
 package ar.com.zauber.commons.gis.street.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.Validate;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataRetrievalFailureException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.ResultSetExtractor;
 
 import ar.com.zauber.commons.dao.Paging;
 import ar.com.zauber.commons.gis.Result;
 import ar.com.zauber.commons.gis.street.Options;
 import ar.com.zauber.commons.gis.street.StreetsDAO;
 import ar.com.zauber.commons.gis.street.impl.parser.AddressParser;
 import ar.com.zauber.commons.gis.street.model.results.GeocodeResult;
 import ar.com.zauber.commons.gis.street.model.results.IntersectionResult;
 import ar.com.zauber.commons.gis.street.model.results.StreetResult;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKTReader;
 
 
 /**
  * SQL implementation for {@link StreetsDAO}.
  * 
  * @author Juan F. Codagnone
  * @since Mar 21, 2006
  */
 public class SQLStreetsDAO implements StreetsDAO {
     /** jdbc template */
     private final JdbcTemplate template;
     /** options */
     private final List<Options> optionsList;
     /** der */
     private final WKTReader wktReader = new WKTReader();
     
     private final AddressParser parser;
     
     static private final List<Options> DEFAULT_OPTIONS = new ArrayList<Options>();
     static {
         DEFAULT_OPTIONS.add(Options.IGNORE_COMMON_WORDS);
         DEFAULT_OPTIONS.add(Options.REMOVE_EXTRA_SPACES);
         DEFAULT_OPTIONS.add(Options.REMOVE_U_DIERESIS);
         DEFAULT_OPTIONS.add(Options.REMOVE_U_DIERESIS);
         DEFAULT_OPTIONS.add(Options.AVENUE_WORD_MOVE);
     }
     
     /**
      * Creates the SQLStreetsDAO.
      *
      * @param template jdbc template
      */
     public SQLStreetsDAO(final JdbcTemplate template) {
     	this(template, DEFAULT_OPTIONS, null);
     }
     
     /**
      * Creates the SQLStreetsDAO.
      *
      * @param template jdbc template
      */
     public SQLStreetsDAO(final JdbcTemplate template, final AddressParser parser) {
         this(template, DEFAULT_OPTIONS, parser);
     }
     
     /**
      * @param template jdbc template
      * @param optionsList la lista de opciones. Notar que las opciones se aplican en el orden
      * en que se reciben.
      */
     public SQLStreetsDAO(final JdbcTemplate template, List<Options> optionsList, 
             final AddressParser parser) {
     	Validate.notNull(template);
     	Validate.notNull(optionsList);
     	
     	this.parser = parser;
     	this.template = template;
     	this.optionsList = Collections.unmodifiableList(optionsList);
     }
     
     /** @see StreetsDAO#getIntersection(String, String) */
     public final Collection<IntersectionResult> getIntersection(
             final String street1Param, final String street2Param) {
         final Collection<IntersectionResult> ret = 
              new ArrayList<IntersectionResult>();
 
         // Si busco calles con longitud <= 2 se muere la base.
         if((street1Param != null && street1Param.length() <= 2) ||
                 (street2Param != null && street2Param.length() <= 2)) {
             return ret;
         }
 
         final String wildcard;
         // EJ. busco CORDOBA esquina CORDOBA AV.
         if(street1Param.startsWith(street2Param) 
                 || street2Param.startsWith(street1Param)) {
            wildcard = ""; 
         } else {
            wildcard = "%";
         }
         
         final String sql = "select DISTINCT AsText(intersection(c1.the_geom, "
             + "c2.the_geom)) AS the_geom, c1.nomoficial AS c1, "
             + "c2.nomoficial AS c2  from streets "
             + "AS c1, streets As c2 WHERE c1.nomoficial ILIKE ? ESCAPE '+' "
             + "AND  c2.nomoficial ILIKE ? ESCAPE '+' "
             + "AND c1.ciudad = c2.ciudad AND c1.ciudad = 'buenos aires' "
             + "and not IsEmpty(AsText(intersection(c1.the_geom, c2.the_geom))) "
             + "LIMIT 10";
 
         String street1Filtered = executeFilters(street1Param);
         String street2Filtered = executeFilters(street2Param);
         
         
         template.query(sql , new Object[] {
                 wildcard + escapeForLike(street1Filtered, '+') + wildcard,
                 wildcard + escapeForLike(street2Filtered, '+') + wildcard},
             new ResultSetExtractor() {
                 /** @see ResultSetExtractor#extractData(java.sql.ResultSet) */
                 public Object extractData(final ResultSet rs) 
                 throws SQLException, DataAccessException {
                     while(rs.next()) {
                         try {
                             final Geometry geom  = 
                                 wktReader.read(rs.getString("the_geom"));
                             if(!geom.isEmpty()) {
                                 if(geom instanceof Point) {
                                     /* a veces, si buscamos la interseccion
                                      * de una calle con si misma, 
                                      * da una linea. hay que ignorarlo.
                                      */
                                     ret.add(new IntersectionResult(
                                             rs.getString("c1"), 
                                             rs.getString("c2"),"AR", 
                                             (Point)geom));
                                 }
                                 
                             }
                             
                         } catch(final ParseException e) {
                             throw new DataRetrievalFailureException(
                                 "parsing feature geom");
                         }
                     }
                     return null;
                 }
             });
             
         
         return ret;
     }
 
     /**
      * Escapes a character from a string intended to be used in a like clause.
      * 
      * @param text string to escape
      * @param escapeChar escape character 
      * @return the escaped string
      */
     private String escapeForLike(final String text, 
             final Character escapeChar) {
         String escape = escapeChar.toString();
         return text.replace("%", escape.concat("%")).replace("_", 
                 escape.concat("_"));
     }
 
     /** @see StreetsDAO#geocode(String, int) */
     public final Collection<GeocodeResult> geocode(final String street, 
             final int altura) {
         return geocode_(street, altura, null);
     }
     
     /** @see StreetsDAO#geocode(String, int, int) */
     public final Collection<GeocodeResult> geocode(final String street, 
             final int altura, final int id) {
         return geocode_(street, altura, id);
     }
     
     
     /** @see StreetsDAO#geocode(String, int) */
     public final Collection<GeocodeResult> geocode_(final String streetParam, 
             final int altura, Integer id) {
         Validate.notEmpty(streetParam);
         
         String streetFiltered = executeFilters(streetParam);
         
         final Collection<Object> args = new ArrayList<Object>();
         args.add(streetFiltered);
         args.add(altura);
         if(id != null) {
             args.add(id.intValue());
         }
         
         final Collection<GeocodeResult> ret = new ArrayList<GeocodeResult>();
         final String q = "select * from geocode_ba(?, ?)" + 
                         (id == null ? "" : " where id=?");
         template.query(q, args.toArray(), new ResultSetExtractor() {
                     public final Object extractData(final ResultSet rs) 
                        throws SQLException, DataAccessException {
                         
                         while(rs.next()) {
                             try {
                                 ret.add(new GeocodeResult(
                                         rs.getInt("id"),
                                         rs.getString("nomoficial"),
                                         rs.getInt("altura"),
                                         "AR",
                                         (Point)wktReader.read(rs.getString
                                                   ("astext"))
                                         ));
                             } catch(ParseException e) {
                                 throw new DataRetrievalFailureException(
                                     "parsing feature geom"); 
                             } 
                         }
                         
                         return null;
                     }
         });
         
         return ret;
     }
 
     /**  @see StreetsDAO#suggestStreets(String, Paging) */
     public List<String> suggestStreets(final String beggining, 
             final Paging paging) {
         final List<String> usernames = new ArrayList<String>();
         final String q = "%" + escapeForLike(beggining, '+') + "%";
         final List<Object> args = new ArrayList<Object>(4);
         args.add(q);
         if(paging != null) {
             args.add(paging.getResultsPerPage());
             args.add(paging.getFirstResult());
         }
         
         template.query("select distinct nomoficial from streets WHERE "
                 + "nomoficial ILIKE ? ESCAPE '+' AND ciudad = 'buenos aires' "
                 + "ORDER BY nomoficial"
                 + (paging == null ? "" : " LIMIT ? OFFSET ? "),
                 args.toArray(),
                 new ResultSetExtractor() {
                     public Object extractData(final ResultSet rset) 
                         throws SQLException, DataAccessException {
                         while(rset.next()) {
                             usernames.add(rset.getString("nomoficial"));
                         }
                         return null;
                     }
                 });
         
         return usernames;
     }
     
     /**
      * Ejecuta los filters en el orden de la lista {@link #optionsList}.
      * @param street el texto a filtrar.
      * @return el texto filtrado
      */
     private String executeFilters(String street) {
     	for (Iterator<Options> optionsIter = this.optionsList.iterator(); optionsIter.hasNext();) {
 			Options options = optionsIter.next();
 			street = options.filter(street);
 		}
     	return street.trim();
     }
 
     /** @see StreetsDAO#getIntersectionsFor(String) */
     public List<String> getIntersectionsFor(String fullStreetName) {
         Validate.notNull(fullStreetName);
 
         final List<String> ret = new ArrayList<String>();
         template.query("select distinct nomoficial from geocode_calles_que_cortan(?) order by nomoficial",
                 new Object[]{fullStreetName}, new ResultSetExtractor() {
                     public Object extractData(final ResultSet rs)
                             throws SQLException, DataAccessException {
                         while (rs.next()) {
                             final String s = rs.getString(1);
                             if(s != null) {
                                 ret.add(s);
                             }
                         }
                         return null;
                     }
         });
         return ret;
     }
 
     /** @see StreetsDAO#guessStreetName(java.util.List, java.lang.String) */
     public final List<GuessStreetResult> guessStreetName(List<String> streets,
             String unnomalizedStreetName) {
         final List <String> unknownStreetTokens = tokenizeCalle(unnomalizedStreetName);
         final List<GuessStreetResult> ret = new ArrayList<GuessStreetResult>();
         
         for(final String street : streets) {
             final List <String> tokens = tokenizeCalle(street);
             int hits = 0;
             for (final String token : tokens) {
                 for (final String unknownToken : unknownStreetTokens) {
                     if(unknownToken.equals(token)) {
                         hits++;
                     }
                 }
             }
             ret.add(new GuessStreetResult(street, hits));
         }
         
         Collections.sort(ret);
         return ret;
     }
     
     
     private final List<String> tokenizeCalle(final String streets) {
         final List<String> ret = new ArrayList<String>();
         
         final String s = streets.replace(',', ' ')
                                 .replace(';', ' ')
                                 .replace('.', ' ');
         
         StringTokenizer t = new StringTokenizer(s, " ", false);
         while(t.hasMoreTokens()) {
             final String token = t.nextToken();
             if(!token.equals("de")) {
                 ret.add(token);
             }
         }
         
         return ret;
     }
 
     /** @see ar.com.zauber.commons.gis.street.StreetsDAO#fullNameStreetExist(java.lang.String) */
     public boolean fullNameStreetExist(final String name) {
         int i = template.queryForInt("select count(nomoficial) from streets where nomoficial =  upper(?)",
                 new Object[]{name});
         return i != 0;
     }
 
     /** @see StreetsDAO#getSinonimos(String) */
     public List<String> getSinonimos(final String fullStreetName) {
         final String s = "select distinct nomoficial from streets where nomanter ILIKE ?";
         final List<String> ret = new ArrayList<String>();
         
         template.query(s, new Object[] {fullStreetName}, new ResultSetExtractor() {
             public Object extractData(final ResultSet rs) throws SQLException,
                     DataAccessException {
                 while(rs.next()) {
                     final String s = rs.getString(1);
                     if(s != null) {
                         ret.add(s);
                     }
                 }
                 
                 return null;
             }
         });
         
         return ret;
     }
 
     /** @see ar.com.zauber.commons.gis.street.StreetsDAO#suggestAddresses(java.lang.String)
      */
     public final List<Result> suggestAddresses(final String text) {
         return parser.parse(text, this);
     }
     
     public final List<Result> getStreets(final String text) {
         final List<Result> results = new ArrayList<Result>();
         
        final String q = "%" + escapeForLike(text, '+') + "%";
         final List<Object> args = new ArrayList<Object>(4);
         args.add(q);
         template.query("select * from geocode_street(?)",
                 args.toArray(),
                 new ResultSetExtractor() {
                     public Object extractData(final ResultSet rset) 
                         throws SQLException, DataAccessException {
                         while(rset.next()) {
                             try {
                                 results.add(
                                         new StreetResult(
                                                 rset.getString("nomoficial"),
                                                 (Point)wktReader.read(rset.getString
                                                         ("middle")),
                                                 "Buenos Aires", 
                                                 "AR"));
                             } catch(ParseException e) {
                                 throw new DataRetrievalFailureException(
                                 "parsing feature geom"); 
                             } 
                         }
                         return null;
                     }
                 });
         
         return results;
     }
 }
