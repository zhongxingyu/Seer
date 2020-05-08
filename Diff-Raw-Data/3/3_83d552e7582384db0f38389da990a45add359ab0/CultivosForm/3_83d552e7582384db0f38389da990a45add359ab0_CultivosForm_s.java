 package es.udc.cartolab.gvsig.pmf.forms;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 
 @SuppressWarnings("serial")
 public class CultivosForm extends AbstractSubForm {
 
     public static final String NAME = "cultivos";
    public static String[] colNames = { "cultivo_tipo", "cultivo_area",
	    "cultivo_vol_prod", "cultivo_vol_con" };
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
