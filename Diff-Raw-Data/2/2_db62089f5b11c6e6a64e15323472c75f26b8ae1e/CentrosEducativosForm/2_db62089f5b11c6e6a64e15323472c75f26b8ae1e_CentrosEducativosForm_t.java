 package es.udc.cartolab.gvsig.pmf.forms;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 
 @SuppressWarnings("serial")
 public class CentrosEducativosForm extends BasicAbstractForm {
 
     public static final String NAME = "centros_educativos";
     public static final String PKFIELD = "cod_cedu";
    public static String[] colNames = { "cod_cedu", "nom_cedu", "tipo_cedu" };
     public static String[] colAlias = { "Cdigo", "Nombre", "Tipo" };
 
     public CentrosEducativosForm(FLyrVect layer) {
 	super(layer);
     }
 
     @Override
     protected String getPrimaryKeyValue() {
 	return getFormController().getValue(PKFIELD);
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 }
