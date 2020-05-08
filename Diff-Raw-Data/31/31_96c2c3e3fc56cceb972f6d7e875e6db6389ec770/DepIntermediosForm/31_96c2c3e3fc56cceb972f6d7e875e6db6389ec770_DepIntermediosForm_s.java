 package es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 
 @SuppressWarnings("serial")
 public class DepIntermediosForm extends BasicAbstractForm {
 
     public static final String NAME = "dep_intermedios";
     public static String[] colNames = {
 "denominacion", "altura", "sistema", "volumen"
     };
     public static String[] colAlias = {
 "Denominacin", "Altura (m)", "Sistema", "Volumen (m)"
     };
 
     public DepIntermediosForm(FLyrVect layer) {
 	super(layer);
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
     @Override
     protected void fillSpecificValues() {
     }
 
 }
