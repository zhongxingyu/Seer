 package es.udc.cartolab.gvsig.fonsagua.forms.comunidades;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners.DependentComboboxesHandler;
 
 @SuppressWarnings("serial")
 public class ValoracionSistemaForm extends AbstractSubForm {
 
     public static final String NAME = "valoracion_sistema";
     public static String[] colNames = { "cod_comunidad", "cod_abastecimiento",
 	    "sist_cobros", "nivel_serv" };
     public static String[] colAlias = { "ID Comunidad", "Cd. Abastecimiento",
 	    "Cobros", "Servicio" };
 
     private JComponent codComunidad;
    private DependentComboboxesHandler abastecimientosDomainHandler;
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
     @Override
     protected void fillSpecificValues() {
 	abastecimientosDomainHandler.updateComboBoxValues();
     }
 
     @Override
     public void setListeners() {
 	super.setListeners();
 	codComunidad = getWidgets().get("cod_comunidad");
 	JComboBox codAbastecimiento = (JComboBox) getWidgets().get(
 		"cod_abastecimiento");
	abastecimientosDomainHandler = new DependentComboboxesHandler(this,
 		codComunidad, codAbastecimiento);
 	((JTextField) codComunidad)
		.addActionListener(abastecimientosDomainHandler);
     }
 
     @Override
     public void removeListeners() {
 	super.removeListeners();
 	((JTextField) codComunidad)
		.addActionListener(abastecimientosDomainHandler);
     }
 }
