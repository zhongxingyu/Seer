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
 import Sirius.server.search.CidsServerSearch;
 import com.vividsolutions.jts.geom.Point;
 import de.cismet.cismap.commons.CrsTransformer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class AlkisLandparcelSearch extends CidsServerSearch {
 
     //~ Instance fields --------------------------------------------------------
 
     private final Point pointGeometry;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new KassenzeichenSearchStatement object.
      *
      * @param  searchString  DOCUMENT ME!
      */
     public AlkisLandparcelSearch(final Point pointGeometry) {
         this.pointGeometry = pointGeometry;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Collection performServerSearch() {
         try {
             final String sql = "SELECT alkis_landparcel.id "
                     + "FROM "
                     + "   alkis_landparcel, "
                     + "   geom "
                     + "WHERE "
                     + "   geom.id = alkis_landparcel.geometrie AND "
                     + "   ST_Within(GeomFromText('" + pointGeometry.toText() + "', " + pointGeometry.getSRID() + "), geom.geo_field)";
 
            getLog().fatal(sql);
             final MetaService metaService = (MetaService)getActiveLoaclServers().get("WUNDA_BLAU");
             final ArrayList<ArrayList> result = metaService.performCustomSearch(sql);
 
             final ArrayList<Integer> ids = new ArrayList<Integer>();
             for (final ArrayList fields : result) {
                 ids.add((Integer)fields.get(0));
             }
             return ids;
         } catch (Exception e) {
             getLog().fatal("problem during landparcel search", e);
             return null;
         }
     }
 }
