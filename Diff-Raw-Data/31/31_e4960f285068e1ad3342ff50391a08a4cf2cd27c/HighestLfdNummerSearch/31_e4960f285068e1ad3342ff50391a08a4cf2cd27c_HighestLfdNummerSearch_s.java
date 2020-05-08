 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.belis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.Date;
 import java.util.List;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class HighestLfdNummerSearch extends AbstractCidsServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(HighestLfdNummerSearch.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Collection performServerSearch() {
         final List<Integer> numbers = new ArrayList<Integer>();
 
        final String query = "SELECT MAX(s.laufendeNummer) "
                    + "FROM Standort s "
                    + "WHERE s.strassenschluessel.pk like :strassenschluessel "
                    + "AND s.kennziffer.kennziffer = :kennziffer;";
 
         final MetaService metaService = (MetaService)getActiveLocalServers().get("BELIS");
 
         try {
             for (final ArrayList fields : metaService.performCustomSearch(query)) {
                 numbers.add((Integer)fields.get(0));
             }
         } catch (Exception ex) {
             LOG.error("problem fortfuehrung item search", ex);
         }
 
         return numbers;
     }
 }
