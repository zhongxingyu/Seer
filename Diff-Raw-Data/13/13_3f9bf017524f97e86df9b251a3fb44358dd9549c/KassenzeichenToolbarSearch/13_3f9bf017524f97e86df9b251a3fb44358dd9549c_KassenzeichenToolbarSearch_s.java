 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.verdis.search;
 
import Sirius.server.search.CidsServerSearch;
 
 import javax.swing.ImageIcon;
 
 import de.cismet.cids.tools.search.clientstuff.CidsToolbarSearch;
 
 import de.cismet.verdis.server.search.KassenzeichenSearchStatement;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsToolbarSearch.class)
 public class KassenzeichenToolbarSearch implements CidsToolbarSearch {
 
     //~ Instance fields --------------------------------------------------------
 
     ImageIcon icoKassenzeichen = new javax.swing.ImageIcon(getClass().getResource(
                 "/de/cismet/verdis/res/images/titlebars/kassenzeichen.png"));
     String input;
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void setSearchParameter(final String toolbarSearchString) {
         input = toolbarSearchString;
     }
 
     @Override
     public ImageIcon getIcon() {
         return icoKassenzeichen;
     }
 
     @Override
     public String getName() {
         return "Kassenzeichensuche";
     }
 
     @Override
    public CidsServerSearch getServerSearch() {
         return new KassenzeichenSearchStatement(input);
     }
 }
