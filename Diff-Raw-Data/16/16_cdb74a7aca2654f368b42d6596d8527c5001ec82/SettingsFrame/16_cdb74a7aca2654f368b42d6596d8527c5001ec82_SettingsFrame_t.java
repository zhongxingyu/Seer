 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.viewer;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Vector;
import java.util.prefs.BackingStoreException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.origamist.JLocalizedButton;
 import javax.swing.origamist.JLocalizedLabel;
 
import org.apache.log4j.Logger;

 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.cuni.mff.peckam.java.origamist.common.LangString;
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.files.CategoriesContainer;
 import cz.cuni.mff.peckam.java.origamist.files.Category;
 import cz.cuni.mff.peckam.java.origamist.files.File;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 
 /**
  * The window with the settings of the viewer.
  * 
  * @author Martin Pecka
  */
 public class SettingsFrame extends JFrame
 {
     /** */
     private static final long        serialVersionUID = -4330246919376017211L;
 
     /** Combobox for selecting the language of the application. */
     protected final JComboBox        appLocaleComboBox;
 
     /** Combobox for selecting the default language of displayed origamis. */
     protected final JComboBox        diagramLocaleComboBox;
 
     /** Button for applying the changes. */
     protected final JLocalizedButton okBtn;
 
     /** Button for cancelling the changes. */
     protected final JLocalizedButton cancelBtn;
 
     public SettingsFrame()
     {
         PropertyChangeListener l = new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 setTitle(ResourceBundle.getBundle("application", (Locale) evt.getNewValue())
                         .getString("settings.title"));
             }
         };
         ServiceLocator.get(ConfigurationManager.class).get().addPropertyChangeListener("locale", l);
         l.propertyChange(new PropertyChangeEvent(this, "locale", null, ServiceLocator.get(ConfigurationManager.class)
                 .get().getLocale()));
 
         appLocaleComboBox = new JComboBox(moveSuggestedLocalesToTop(getLocales(), getAppSuggestedLocales()));
         appLocaleComboBox.setRenderer(new LocaleListCellRenderer());
 
         diagramLocaleComboBox = new JComboBox(moveSuggestedLocalesToTop(getLocales(), getDiagramSuggestedLocales()));
         diagramLocaleComboBox.setRenderer(new LocaleListCellRenderer());
 
         okBtn = new JLocalizedButton.OKButton();
         okBtn.setAction(new OKAction());
 
         cancelBtn = new JLocalizedButton.CancelButton();
         cancelBtn.setAction(new CancelAction());
 
         buildLayout();
 
         pack();
     }
 
     /**
      * Adds the components to this frame.
      */
     protected void buildLayout()
     {
         setLayout(new FormLayout("$dm,left:pref,$lcgap,pref:grow,right:pref,$dm", "$dm,pref,$ugap,pref,$ugap,pref,$dm"));
         CellConstraints cc = new CellConstraints();
 
         add(new JLocalizedLabel("application", "appLocale"), cc.xy(2, 2));
         add(appLocaleComboBox, cc.xyw(4, 2, 2));
 
         add(new JLocalizedLabel("application", "diagramLocale"), cc.xy(2, 4));
         add(diagramLocaleComboBox, cc.xyw(4, 4, 2));
 
         add(okBtn, cc.xy(5, 6));
         add(cancelBtn, cc.xy(2, 6));
     }
 
     /**
      * @return All available locales, sorted alphabetically. If a locale exists in both no-country and country versions,
      *         only the country versions are preserved. If a locale is available only without a country, it is also
      *         preserved.
      */
     protected Vector<Locale> getLocales()
     {
         Locale[] locales = Locale.getAvailableLocales();
         Arrays.sort(locales, new LocaleComparator());
 
         Vector<Locale> newLocales = new Vector<Locale>(locales.length);
 
         for (int i = 0; i < locales.length; i++) {
             Locale l = locales[i];
             if (l.toString().length() > 2) {
                 newLocales.add(l);
             } else {
                 if (i < locales.length - 1) {
                     if (!l.getLanguage().equals(locales[i + 1].getLanguage()))
                         newLocales.add(l);
                 } else {
                     newLocales.add(l);
                 }
             }
         }
 
         return newLocales;
     }
 
     /**
      * Removes all locales that are in <code>suggested</code> from <code>locales</code> and inserts them at the
      * beginning of the vector.
      * 
      * @param locales The vector to do the operation on.
      * @param suggested The locales to be moved to the top.
      * @return The same instance that was given in <code>locales</code>.
      */
     protected Vector<Locale> moveSuggestedLocalesToTop(Vector<Locale> locales, LinkedHashSet<Locale> suggested)
     {
         locales.removeAll(suggested);
         locales.addAll(0, suggested);
         return locales;
     }
 
     /**
      * @return The locales suggested for the application.
      */
     protected LinkedHashSet<Locale> getAppSuggestedLocales()
     {
         LinkedHashSet<Locale> result = new LinkedHashSet<Locale>();
 
         result.add(ServiceLocator.get(ConfigurationManager.class).get().getLocale());
         result.add(ServiceLocator.get(ConfigurationManager.class).get().getDiagramLocale());
         result.add(Locale.getDefault());
 
         return result;
     }
 
     /**
      * @return The locales suggested for the displayed origamis.
      */
     protected LinkedHashSet<Locale> getDiagramSuggestedLocales()
     {
         LinkedHashSet<Locale> result = new LinkedHashSet<Locale>();
 
         result.add(ServiceLocator.get(ConfigurationManager.class).get().getDiagramLocale());
         result.add(ServiceLocator.get(ConfigurationManager.class).get().getLocale());
         result.add(Locale.getDefault());
 
         Iterator<? extends CategoriesContainer> ic = ServiceLocator.get(OrigamiViewer.class).filesToDisplay
                 .recursiveCategoryIterator(false);
         while (ic.hasNext()) {
             Category cat = (Category) ic.next();
             for (LangString ls : cat.getName())
                 result.add(ls.getLang());
         }
 
         Iterator<File> itf = ServiceLocator.get(OrigamiViewer.class).filesToDisplay.recursiveFileIterator();
         while (itf.hasNext()) {
             File f = itf.next();
             for (LangString ls : f.getName())
                 result.add(ls.getLang());
             for (LangString ls : f.getShortdesc())
                 result.add(ls.getLang());
             if (f.isOrigamiLoaded()) {
                 try {
                     Origami o = f.getOrigami();
                     for (LangString ls : o.getDescription())
                         result.add(ls.getLang());
                     for (LangString ls : o.getName())
                         result.add(ls.getLang());
                     for (LangString ls : o.getShortdesc())
                         result.add(ls.getLang());
                 } catch (UnsupportedDataFormatException e) {
                     assert false : "Origami loaded but getOrigami() threw exception.";
                 } catch (IOException e) {
                     assert false : "Origami loaded but getOrigami() threw exception.";
                 }
             }
         }
 
         return result;
     }
 
     /**
      * A comparator of locales based on their textual representation.
      * 
      * @author Martin Pecka
      */
     class LocaleComparator implements Comparator<Locale>
     {
         @Override
         public int compare(Locale o1, Locale o2)
         {
             if (o1 == null) {
                 if (o2 == null)
                     return 0;
                 else
                     return -1;
             } else {
                 if (o2 == null) {
                     return 1;
                 } else {
                     return o1.toString().compareToIgnoreCase(o2.toString());
                 }
             }
         }
     }
 
     /**
      * Action to apply changes made in this dialog and close it.
      * 
      * @author Martin Pecka
      */
     class OKAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -2245759319491457479L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             ServiceLocator.get(ConfigurationManager.class).get()
                     .setLocale((Locale) appLocaleComboBox.getSelectedItem());
             ServiceLocator.get(ConfigurationManager.class).get()
                     .setDiagramLocale((Locale) diagramLocaleComboBox.getSelectedItem());
            try {
                ServiceLocator.get(ConfigurationManager.class).persist();
            } catch (BackingStoreException e1) {
                Logger.getLogger("application").warn("Couldn't save configuration", e1);
            }
             setVisible(false);
         }
 
     }
 
     /**
      * Action to cancel changes made in this dialog and close it.
      * 
      * @author Martin Pecka
      */
     class CancelAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = 487038857956049431L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             setVisible(false);
         }
 
     }
 }
