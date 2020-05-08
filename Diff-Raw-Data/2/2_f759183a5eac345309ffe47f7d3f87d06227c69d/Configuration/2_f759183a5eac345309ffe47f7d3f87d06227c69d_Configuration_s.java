 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.configuration;
 
 import java.util.Locale;
 
 import cz.cuni.mff.peckam.java.origamist.utils.PropertyChangeSource;
 
 /**
  * A configuration of the program.
  * 
  * Properties that fire PropertyChangeEvent when they are changed:
  * locale
  * diagramLocale
  * 
  * @author Martin Pecka
  */
 public class Configuration extends PropertyChangeSource
 {
 
     /**
      * General locale of the program.
      */
     protected Locale locale        = Locale.getDefault();
 
     /**
      * The preferred locale for diagrams. If null, means that it is the same as
      * locale.
      */
     protected Locale diagramLocale = null;
 
     /**
      * @return the locale
      */
     public Locale getLocale()
     {
         return locale;
     }
 
     /**
      * @param locale the locale to set
      */
     public void setLocale(Locale locale)
     {
         Locale oldLocale = this.locale;
         this.locale = locale;
        firePropertyChange("locale", locale, oldLocale);
     }
 
     /**
      * @return the diagramLocale
      */
     public Locale getDiagramLocale()
     {
         return diagramLocale == null ? locale : diagramLocale;
     }
 
     /**
      * @param diagramLocale the diagramLocale to set
      */
     public void setDiagramLocale(Locale diagramLocale)
     {
         Locale oldLocale = this.diagramLocale;
         this.diagramLocale = diagramLocale;
         firePropertyChange("diagramLocale", oldLocale, diagramLocale);
     }
 
 }
