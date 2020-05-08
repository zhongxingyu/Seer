 package es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners.DependentComboboxHandler;
 
 @SuppressWarnings("serial")
 public class DepIntermediosForm extends BasicAbstractForm {
 
     public static final String NAME = "dep_intermedios";
     public static String[] colNames = {
 "denominacion", "altura", "sistema", "volumen"
     };
     public static String[] colAlias = {
 "Denominacin", "Altura (m)", "Sistema", "Volumen (m)"
     };
 
     private JComponent codAbastecimiento;
     private DependentComboboxHandler bombeosDomainHandler;
 
     public DepIntermediosForm(FLyrVect layer) {
 	super(layer);
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
     @Override
     protected void fillSpecificValues() {
 	bombeosDomainHandler.updateComboBoxValues();
     }
 
     @Override
     public void setListeners() {
 	super.setListeners();
 	codAbastecimiento = getWidgetComponents().get("cod_abastecimiento");
 	JComboBox codBombeo = (JComboBox) getWidgetComponents().get(
 		"cod_bombeo");
 	bombeosDomainHandler = new DependentComboboxHandler(this,
 		codAbastecimiento, codBombeo);
 	((JTextField) codAbastecimiento).addFocusListener(bombeosDomainHandler);
     }
 
     @Override
     public void removeListeners() {
 	super.removeListeners();
 	((JTextField) codAbastecimiento)
		.removeFocusListener(bombeosDomainHandler);
     }
 
 }
