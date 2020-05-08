 package ch.opentrainingcenter.model.navigation.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import ch.opentrainingcenter.model.navigation.INavigationItem;
 import ch.opentrainingcenter.model.navigation.INavigationParent;
 import ch.opentrainingcenter.model.navigation.KalenderWoche;
 
 public class NavigationParent implements INavigationParent {
 
     List<INavigationItem> items = new ArrayList<INavigationItem>();
     private KalenderWoche kw = null;
 
     public NavigationParent() {
 
     }
 
     @Override
     public String getName() {
         final StringBuffer result = new StringBuffer("KW"); //$NON-NLS-1$
        result.append(kw != null ? kw.getKw() : "");//$NON-NLS-1$
         return result.toString();
     }
 
     @Override
     public List<INavigationItem> getChilds() {
         Collections.sort(items);
         return items;
     }
 
     @Override
     public void add(final INavigationItem item) {
         items.add(item);
         if (kw == null) {
             kw = new KalenderWoche(item.getDate());
         }
     }
 
     @Override
     public void remove(final INavigationItem item) {
         items.remove(item);
     }
 
     @Override
     public KalenderWoche getKalenderWoche() {
         return kw;
     }
 }
