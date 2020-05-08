 package de.cismet.cids.custom.wunda_blau.search;
 
 import de.cismet.cids.tools.search.clientstuff.CidsWindowSearch;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.search.CidsServerSearch;
 import Sirius.server.search.SearchOption;
 import de.cismet.cids.custom.objectrenderer.utils.CidsBeanSupport;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author stefan
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsWindowSearch.class)
 public class TestWindowSearch implements CidsWindowSearch {
 
     public TestWindowSearch() {
         mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, "FLURSTUECK");
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
         return "Testsuche 3";
     }
 
     @Override
     public JComponent getSearchWindowComponent() {
         JPanel test = new JPanel();
         test.setLayout(new BorderLayout());
         test.add(new JLabel("geht!"), BorderLayout.CENTER);
         return test;
     }
 
     @Override
     public CidsServerSearch getServerSearch() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     
 }
