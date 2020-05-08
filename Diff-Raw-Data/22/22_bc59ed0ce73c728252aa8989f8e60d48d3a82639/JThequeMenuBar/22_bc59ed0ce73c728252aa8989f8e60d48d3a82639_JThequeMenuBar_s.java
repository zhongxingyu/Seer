 package org.jtheque.views.impl.components.menu;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.jtheque.features.able.FeatureListener;
 import org.jtheque.features.able.IFeature;
 import org.jtheque.features.able.IFeatureService;
 import org.jtheque.i18n.able.ILanguageService;
 import org.jtheque.i18n.able.Internationalizable;
 import org.jtheque.i18n.able.InternationalizableContainer;
import org.jtheque.resources.able.IResourceService;
 import org.jtheque.utils.ui.SwingUtils;
 
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * A menu bar for the main view.
  *
  * @author Baptiste Wicht
  */
 public final class JThequeMenuBar extends JMenuBar implements FeatureListener, InternationalizableContainer {
     private final Comparator<IFeature> featureComparator = new ByPositionComparator();
 
    private final IResourceService resourceService;
     private final ILanguageService languageService;
     private final IFeatureService featureService;
 
     private final Collection<Internationalizable> internationalizables = new ArrayList<Internationalizable>(25);
 
     /**
      * Construct a JThequeMenuBar.
      *
     * @param resourceService The resource service.
      * @param languageService The language service.
      * @param featureService  The feature service.
      */
    public JThequeMenuBar(IResourceService resourceService, ILanguageService languageService, IFeatureService featureService) {
         super();
 
        this.resourceService = resourceService;
         this.languageService = languageService;
         this.featureService = featureService;
 
         languageService.addInternationalizable(this);
     }
 
     /**
      * A comparator to sort the Features by position.
      *
      * @author Baptiste Wicht
      */
     private static final class ByPositionComparator implements Comparator<IFeature> {
         @Override
         public int compare(IFeature feature, IFeature other) {
             return feature.getPosition().compareTo(other.getPosition());
         }
     }
 
     /**
      * Build the menu.
      */
     public void buildMenu() {
         List<IFeature> features = new ArrayList<IFeature>(featureService.getFeatures());
 
         Collections.sort(features, featureComparator);
 
         for (IFeature feature : features) {
             addFeature(feature);
         }
 
         featureService.addFeatureListener(this);
     }
 
 
     /**
      * Add a feature.
      *
      * @param feature The feature to add.
      */
     private void addFeature(IFeature feature) {
         JThequeMenu menu = new JThequeMenu(feature.getTitleKey());
 
         addInternationalizable(menu);
 
         List<IFeature> subFeatures = new ArrayList<IFeature>(feature.getSubFeatures());
 
         Collections.sort(subFeatures, featureComparator);
 
         for (IFeature subFeature : subFeatures) {
             addFeature(menu, subFeature);
         }
 
         menu.refreshText(languageService);
 
         add(menu);
     }
 
     /**
      * Add a feature.
      *
      * @param menu    The JMenu.
      * @param feature The feature.
      */
     private void addFeature(JMenu menu, IFeature feature) {
         switch (feature.getType()) {
             case ACTION:
                 addMenuItem(menu, feature);
 
                 break;
             case SEPARATED_ACTION:
                 addSeparator(menu);
                 addMenuItem(menu, feature);
 
                 break;
             case SEPARATED_ACTIONS:
                 addSeparator(menu);
                 addActions(menu, feature);
 
                 break;
             case ACTIONS:
                 addActions(menu, feature);
 
                 break;
         }
     }
 
     /**
      * Add a menu item to the menu.
      *
      * @param menu    The menu to add the item to.
      * @param feature The feature to add to the menu.
      */
     private void addMenuItem(JMenu menu, IFeature feature) {
         feature.getAction().refreshText(languageService);
 
         menu.add(new JThequeMenuItem(feature.getAction()));
     }
 
     /**
      * Add actions to the menu.
      *
      * @param menu    The menu to add the actions to.
      * @param feature The feature to add to the menu.
      */
     private void addActions(JMenu menu, IFeature feature) {
         JThequeMenu subMenu = new JThequeMenu(feature.getTitleKey());
 
         addInternationalizable(subMenu);
 
         if (feature.getIcon() != null) {
            subMenu.setIcon(resourceService.getIcon(feature.getIcon()));
         }
 
         for (IFeature subFeature : feature.getSubFeatures()) {
             addFeature(subMenu, subFeature);
         }
 
         subMenu.refreshText(languageService);
 
         menu.add(subMenu);
     }
 
     /**
      * Add a separator to the menu.
      *
      * @param menu The menu.
      */
     private static void addSeparator(JMenu menu) {
         if (menu.getItemCount() > 0) {
             menu.addSeparator();
         }
     }
 
     @Override
     public void featureAdded(IFeature feature) {
        addFeature(feature);
 
         SwingUtils.refresh(this);
     }
 
     @Override
     public void featureRemoved(IFeature feature) {
         for (int i = 0; i < getMenuCount(); i++) {
             JMenu menu = getMenu(i);
 
             if (isCorrespondingMenu(feature, menu)) {
                 remove(menu);
 
                 break;
             }
         }
     }
 
     @Override
     public void subFeatureAdded(IFeature feature, IFeature subFeature) {
         for (int i = 0; i < getMenuCount(); i++) {
             JMenu menu = getMenu(i);
 
             if (isCorrespondingMenu(feature, menu)) {
                 menu.removeAll();
 
                 List<IFeature> subFeatures = new ArrayList<IFeature>(feature.getSubFeatures());
 
                 Collections.sort(subFeatures, featureComparator);
 
                 for (IFeature sub : subFeatures) {
                     addFeature(menu, sub);
                 }
 
                 break;
             }
         }
     }
 
     /**
      * Indicate if the menu correspond to the feature
      *
      * @param feature The feature to test.
      * @param menu    The menu to test.
      * @return true if the menu is corresponding to the feature else false.
      */
     private boolean isCorrespondingMenu(IFeature feature, AbstractButton menu) {
         return menu != null && menu.getText().equals(languageService.getMessage(feature.getTitleKey()));
     }
 
     @Override
     public void subFeatureRemoved(IFeature feature, IFeature subFeature) {
         for (int i = 0; i < getMenuCount(); i++) {
             JMenu menu = getMenu(i);
 
             if (isCorrespondingMenu(feature, menu)) {
                 removeMenu(subFeature, menu);
 
                 break;
             }
         }
     }
 
     /**
      * Remove the feature of the event from the menu.
      *
      * @param subFeature The sub feature.
      * @param menu       The menu to the remove the menu from.
      */
     private void removeMenu(IFeature subFeature, JMenu menu) {
         String subtitle = getSubtitle(subFeature);
 
         for (int z = 0; z < menu.getItemCount(); z++) {
             JMenuItem item = menu.getItem(z);
 
             //Perhaps, it's a separator
             if (item != null) {
                 if (item.getAction() != null && item.getAction().getValue(Action.NAME).equals(subtitle)) {
                     menu.remove(item);
                 } else if (item.getText() != null && item.getText().equals(subtitle)) {
                     menu.remove(item);
                 }
 
                 clearMenu(menu);
             }
         }
     }
 
     /**
      * Clear the menu.
      *
      * @param menu The menu to clear.
      */
     private void clearMenu(JMenu menu) {
         if (menu.getItemCount() <= 0) {
             remove(menu);
         } else if (menu.getItem(0) == null) {
             menu.remove(0);
         }
     }
 
     /**
      * Return the title of the given sub feature.
      *
      * @param subFeature The sub feature to get the title from.
      * @return The title of the given sub feature.
      */
     private String getSubtitle(IFeature subFeature) {
         if (subFeature.getAction() == null) {
             return languageService.getMessage(subFeature.getTitleKey());
         } else {
             return (String) subFeature.getAction().getValue(Action.NAME);
         }
     }
 
     @Override
     public void addInternationalizable(Internationalizable internationalizable) {
         internationalizables.add(internationalizable);
     }
 
     @Override
     public void refreshText(ILanguageService languageService) {
         for (Internationalizable internationalizable : internationalizables) {
             internationalizable.refreshText(languageService);
         }
 
         SwingUtils.refresh(this);
     }
 }
