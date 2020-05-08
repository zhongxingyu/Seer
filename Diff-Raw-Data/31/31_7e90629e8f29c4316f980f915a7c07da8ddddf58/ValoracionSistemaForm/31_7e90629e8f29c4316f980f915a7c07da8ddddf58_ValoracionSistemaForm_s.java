 package es.udc.cartolab.gvsig.fonsagua.forms.comunidades;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 
 @SuppressWarnings("serial")
 public class ValoracionSistemaForm extends AbstractSubForm {
 
     public static final String NAME = "valoracion_sistema";
     public static String[] colNames = {
 "cod_comunidad", "cod_abastecimiento", "sist_cobros", "nivel_serv"
     };
     public static String[] colAlias = {
 "ID Comunidad", "Cdigo Abastecimiento", "Cobros", "Servicio"
     };
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
     @Override
     protected void fillSpecificValues() {
     }
 }
