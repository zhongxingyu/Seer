 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.verdis.server.search;
 
 import Sirius.server.middleware.interfaces.domainserver.MetaService;
 
 import org.apache.log4j.Logger;
 
 import java.rmi.RemoteException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import de.cismet.cids.server.search.AbstractCidsServerSearch;
 
 import de.cismet.verdis.commons.constants.FrontPropertyConstants;
 import de.cismet.verdis.commons.constants.FrontenPropertyConstants;
 import de.cismet.verdis.commons.constants.FrontinfoPropertyConstants;
 import de.cismet.verdis.commons.constants.KassenzeichenPropertyConstants;
 import de.cismet.verdis.commons.constants.VerdisConstants;
 import de.cismet.verdis.commons.constants.VerdisMetaClassConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 
 public class FrontenCrossReferencesServerSearch extends AbstractCidsServerSearch {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(FrontenCrossReferencesServerSearch.class);
 
     //~ Instance fields --------------------------------------------------------
 
     String searchQuery = "";
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new FlaechenCrossReferencesServerSearch object.
      *
      * @param  kzNummer  DOCUMENT ME!
      */
     public FrontenCrossReferencesServerSearch(final int kzNummer) {
         searchQuery = "SELECT "
                     + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz1, "
                     + "    front1." + FrontPropertyConstants.PROP__ID + " AS fid, "
                     + "    front1." + FrontPropertyConstants.PROP__NUMMER + " AS f1, "
                     + "    kassenzeichen2." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " AS kz2, "
                     + "    front2." + FrontPropertyConstants.PROP__NUMMER + " AS f2 "
                     + "FROM "
                     + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen1, "
                     + "    " + VerdisMetaClassConstants.MC_KASSENZEICHEN + " AS kassenzeichen2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHEN + " AS fronten1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHEN + " AS fronten2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS front1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHE + " AS front2, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS frontinfo1, "
                    + "    " + VerdisMetaClassConstants.MC_FLAECHENINFO + " AS frontinfo2 "
                     + "WHERE "
                     + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__ID + " = fronten1."
                     + FrontenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                     + "    kassenzeichen2." + KassenzeichenPropertyConstants.PROP__ID + " = fronten2."
                     + FrontenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                     + "    fronten1." + FrontenPropertyConstants.PROP__FRONT + " = front1."
                     + FrontPropertyConstants.PROP__ID + " AND "
                     + "    fronten2." + FrontenPropertyConstants.PROP__FRONT + " = front2."
                     + FrontPropertyConstants.PROP__ID + " AND "
                     + "    front1." + FrontPropertyConstants.PROP__FRONTINFO + " = frontinfo1."
                     + FrontinfoPropertyConstants.PROP__ID + " AND "
                     + "    front2." + FrontPropertyConstants.PROP__FRONTINFO + " = frontinfo2."
                     + FrontinfoPropertyConstants.PROP__ID + " AND "
                     + "    frontinfo2." + FrontinfoPropertyConstants.PROP__ID + " = frontinfo1."
                     + FrontinfoPropertyConstants.PROP__ID + " AND "
                     + "    NOT fronten2." + FrontenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " = fronten1."
                     + FrontenPropertyConstants.PROP__KASSENZEICHEN_REFERENCE + " AND "
                     + "    kassenzeichen1." + KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER + " = "
                     + kzNummer;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Collection performServerSearch() {
         final MetaService ms = (MetaService)getActiveLocalServers().get(VerdisConstants.DOMAIN);
         if (ms != null) {
             try {
                 final ArrayList<ArrayList> lists = ms.performCustomSearch(searchQuery);
                 return lists;
             } catch (RemoteException ex) {
                 LOG.fatal("error while performing custom server search", ex);
             }
         }
         return null;
     }
 
     @Override
     public String toString() {
         return searchQuery;
     }
 }
