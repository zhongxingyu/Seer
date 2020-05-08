 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 thorsten
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.verdis.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 import com.vividsolutions.jts.geom.Geometry;
 import de.cismet.verdis.CidsAppBackend;
 import de.cismet.verdis.constants.KassenzeichenPropertyConstants;
 import de.cismet.verdis.constants.VerdisMetaClassConstants;
 import java.util.*;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class KassenzeichenGeomSearch extends GeomServerSearch {
 
     @Override
     public Collection performServerSearch() {
         final Geometry searchGeometry = getGeometry();
         
         if (searchGeometry != null) {
                 final String sqlKassenzeichenGeom = "SELECT " +
                     "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer " + 
                     "FROM " +
                     "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, " +
                     "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom " +
                     "WHERE " +                        
                     "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " IS NOT NULL AND " +
                     "    geom.id = kassenzeichen.geometrie AND " +
                    "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', " + searchGeometry.getSRID() + "), geom.geo_field) AND " +            
                    "    GeometryType(geom.geo_field) = 'POINT' " +            
                     "    ORDER BY kassenzeichennumer ASC;";
 
                 final String sqlFlaechenGeom = "SELECT " +
                     "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer " + 
                     "FROM " +
                     "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, " +
                     "    flaechen AS flaechen, " +
                     "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS flaeche, " +
                     "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS flaecheninfo, " +
                     "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom " +
                     "WHERE " +                        
                     "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " IS NOT NULL AND " +
                     "    flaechen.kassenzeichen_reference = kassenzeichen.id AND " +
                     "    flaechen.flaeche = flaeche.id AND " +
                     "    flaeche.flaecheninfo = flaecheninfo.id AND " +
                     "    geom.id = flaecheninfo.geometrie AND " +
                     "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', " + searchGeometry.getSRID() + "), geom.geo_field) " +            
                     "    ORDER BY kassenzeichennumer ASC;";
                 
                 final String sqlFrontenGeom = "SELECT " +
                     "    DISTINCT " + VerdisMetaClassConstants.MC_KASSENZEICHEN + "." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kassenzeichennumer " + 
                     "FROM " +
                     "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen, " +
                     "    fronten AS fronten, " +
                     "    " + VerdisMetaClassConstants.MC_FRONTINFO + " AS frontinfo, " +
                     "    " + VerdisMetaClassConstants.MC_GEOM + " AS geom " +
                     "WHERE " +                        
                     "    kassenzeichen." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " IS NOT NULL AND " +
                     "    fronten.kassenzeichen_reference = kassenzeichen.id AND " +
                     "    fronten.frontinfo = frontinfo.id AND " +
                     "    geom.id = frontinfo.geometrie AND  " +
                     "    ST_Intersects(GeomFromText('" + searchGeometry.toText() + "', " + searchGeometry.getSRID() + "), geom.geo_field) " +            
                     "    ORDER BY kassenzeichennumer ASC;";
                 
                 final MetaService metaService = (MetaService) getActiveLoaclServers().get(CidsAppBackend.DOMAIN);
                 
                 // ids der kassenzeichen sammeln
                 final Set<Integer> idSet = new HashSet<Integer>();
                 
                 getLog().debug(sqlKassenzeichenGeom);
                 try {
                     for (final ArrayList fields : metaService.performCustomSearch(sqlKassenzeichenGeom)) {
                         idSet.add((Integer) fields.get(0));
                     }
                 } catch (Exception ex) {
                     getLog().error("problem during kassenzeichen geom search", ex);
                 }
                 
                 getLog().debug(sqlFlaechenGeom);
                 try {
                     for (final ArrayList fields : metaService.performCustomSearch(sqlFlaechenGeom)) {
                         idSet.add((Integer) fields.get(0));
                     }
                 } catch (Exception ex) {
                     getLog().error("problem during flaechen geom search", ex);
                 }
                 
                 getLog().debug(sqlFrontenGeom);
                 try {
                     for (final ArrayList fields : metaService.performCustomSearch(sqlFrontenGeom)) {
                         idSet.add((Integer) fields.get(0));
                     }
                 } catch (Exception ex) {
                     getLog().error("problem during fronten geom search", ex);
                 }
                 
                 // ids der Kassenzeichen sortieren
                 final List<Integer> sortedIdList = Arrays.asList(idSet.toArray(new Integer[0]));
                 Collections.sort(sortedIdList);
                                 
                 //
                 return sortedIdList;
         } else {
             getLog().info("searchGeometry is null, geom search is not possible");
         }
         return null;
     }
 }
