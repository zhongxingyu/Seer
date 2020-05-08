 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.wizard.converter;
 
 import org.openide.WizardDescriptor;
 import org.openide.util.NbBundle;
 
 import java.util.List;
 
 import de.cismet.commons.converter.Converter;
 
 import de.cismet.commons.gui.wizard.AbstractWizardPanel;
 
 /**
  * Basic ConverterChooser panel implementation.
  *
  * @author   mscholl
  * @version  1.0
  */
 public abstract class AbstractConverterChooseWizardPanel extends AbstractWizardPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String PROP_CONVERTER = "__prop_converter__"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private transient Converter converter;
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected DefaultConverterChooseVisualPanel createComponent() {
         return new DefaultConverterChooseVisualPanel(this);
     }
 
     @Override
     protected void read(final WizardDescriptor wizard) {
        converter = (Converter)wizard.getProperty(PROP_CONVERTER);
         ((DefaultConverterChooseVisualPanel)getComponent()).init();
         wizard.putProperty(
             WizardDescriptor.PROP_INFO_MESSAGE,
             NbBundle.getMessage(
                 AbstractConverterChooseWizardPanel.class,
                 "AbstractConverterChooseWizardPanel.read(WizardDescriptor).wizard.putProperty(String,String)")); // NOI18N
     }
 
     @Override
     protected void store(final WizardDescriptor wizard) {
         wizard.putProperty(PROP_CONVERTER, converter);
         wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
     }
 
     @Override
     public boolean isValid() {
         return converter != null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract List<Converter> getAvailableConverters();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Converter getConverter() {
         return converter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  converter  DOCUMENT ME!
      */
     public void setConverter(final Converter converter) {
         this.converter = converter;
 
         changeSupport.fireChange();
     }
 }
