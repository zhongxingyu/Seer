 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.xml.bind.annotation.XmlTransient;
 
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 
 /**
  * A dimension with units specified.
  * 
  * @author Martin Pecka
  */
 @XmlTransient
 public class UnitDimension extends cz.cuni.mff.peckam.java.origamist.model.jaxb.UnitDimension
 {
     /** The localization resource bundle. */
     private static ResourceBundle messages;
 
     static {
         PropertyChangeListener l = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 messages = ResourceBundle.getBundle("application", (Locale) evt.getNewValue());
             }
         };
         ServiceLocator.get(ConfigurationManager.class).get().addPropertyChangeListener("locale", l);
        l.propertyChange(new PropertyChangeEvent(new Object(), "locale", null, ServiceLocator
                .get(ConfigurationManager.class).get().getLocale()));
     }
 
     /**
      * Convert this dimension to a dimension with the given unit
      * 
      * @param newUnit The unit of the requested dimension
      * @return A corresponding dimension with the given unit
      */
     public UnitDimension convertTo(Unit newUnit)
     {
         UnitDimension res = new UnitDimension();
         res.width = UnitHelper.convertTo(getUnit(), newUnit, width);
         res.height = UnitHelper.convertTo(getUnit(), newUnit, height);
         res.setUnit(newUnit);
         return res;
     }
 
     /**
      * Return the bigger dimension.
      * 
      * @return The bigger dimension.
      */
     public double getMax()
     {
         return Math.max(width, height);
     }
 
     @Override
     public String toString()
     {
         String format = messages.getString("unitDimension.format");
         try {
             format = messages.getString("unitDimension.format." + getUnit().toString());
         } catch (MissingResourceException e) {}
 
         return MessageFormat.format(format, UnitHelper.formatUnit(getUnit(), getWidth()),
                 UnitHelper.formatUnit(getUnit(), getHeight()));
     }
 }
