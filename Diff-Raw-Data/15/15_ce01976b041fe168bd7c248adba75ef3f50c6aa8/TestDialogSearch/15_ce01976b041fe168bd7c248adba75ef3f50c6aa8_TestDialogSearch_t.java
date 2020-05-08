 package de.cismet.cids.custom.wunda_blau.search;
 
 import de.cismet.cids.tools.search.clientstuff.CidsDialogSearch;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.search.CidsServerSearch;
 import Sirius.server.search.SearchOption;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 
 /**
  *
  * @author stefan
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsDialogSearch.class)
 public class TestDialogSearch implements CidsDialogSearch {
 
     public TestDialogSearch() {
         mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "ADRESSE");
         icon = new ImageIcon(mc.getIconData());
         classCol = new ArrayList<MetaClass>(1);
         classCol.add(mc);
     }
     private final MetaClass mc;
     private final ImageIcon icon;
     private Collection<MetaClass> classCol;
 
     @Override
     public ImageIcon getIcon() {
         return icon;
     }
 
   
 
     @Override
     public String getName() {
         return "Testsuche 2";
     }
 
 
     @Override
     public JDialog getDialogComponent() {
         JDialog test = new JDialog();
         test.setLayout(new BorderLayout());
         test.add(new JLabel("geht!"), BorderLayout.CENTER);
         test.pack();
         return test;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     
 }
