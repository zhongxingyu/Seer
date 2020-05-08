 package es.udc.cartolab.gvsig.pmf.forms;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 
 @SuppressWarnings("serial")
 public class CultivosForm extends AbstractSubForm {
 
     public static final String NAME = "cultivos";
    public static String[] colNames = { "tipo", "area", "vol_prod", "vol_con" };
     public static String[] colAlias = { "Tipo", "Area", "Producido",
 	    "Consumido" };
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
     @Override
     protected void fillSpecificValues() {
     }
 }
