 package es.icarto.gvsig.sixhiara.forms;
 
 import java.io.InputStream;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.jeta.forms.components.panel.FormPanel;
 import com.jeta.forms.gui.common.FormException;
 
 import es.icarto.gvsig.navtableforms.AbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.handler.AlphanumericTableHandler;
 
 @SuppressWarnings("serial")
 public class FountainsForm extends AbstractForm {
 
     public static final String LAYERNAME = "Fontes";
     public static final String PKFIELD = "cod_fonte";
    public static final String ABEILLE = "ui/fountains.xml";
    public static final String METADATA = "metadata/fountains.xml";
 
     public FountainsForm(FLyrVect layer) {
 	super(layer);
 	addTableHandler(new AlphanumericTableHandler(
 		FountainsAnalyticalSubForm.TABLENAME, getWidgetComponents(),
 		PKFIELD, FountainsAnalyticalSubForm.colNames,
 		FountainsAnalyticalSubForm.colAlias));
 	addTableHandler(new AlphanumericTableHandler(
 		FountainsFlowrateGaugingSubForm.TABLENAME,
 		getWidgetComponents(), PKFIELD,
 		FountainsFlowrateGaugingSubForm.colNames,
 		FountainsFlowrateGaugingSubForm.colAlias));
     }
 
     @Override
     public String getXMLPath() {
 	return this.getClass().getClassLoader().getResource(METADATA).getPath();
     }
 
     @Override
     public FormPanel getFormBody() {
 	if (formBody == null) {
 	    InputStream stream = getClass().getClassLoader()
 		    .getResourceAsStream(ABEILLE);
 	    try {
 		formBody = new FormPanel(stream);
 	    } catch (FormException e) {
 		e.printStackTrace();
 	    }
 	}
 	return formBody;
     }
 
     @Override
     protected void fillSpecificValues() {
 	super.fillSpecificValues();
     }
 
     @Override
     protected String getPrimaryKeyValue() {
 	return getFormController().getValue(PKFIELD);
     }
 }
