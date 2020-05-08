 package es.udc.cartolab.gvsig.fonsagua.alternativas.ui;
 
 import java.awt.event.ActionEvent;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.iver.andami.PluginServices;
 
 import es.udc.cartolab.gvsig.fonsagua.OpenAlternativeExtension;
 import es.udc.cartolab.gvsig.fonsagua.utils.DatabaseDirectAccessQueries;
 import es.udc.cartolab.gvsig.fonsagua.utils.FilteredDialog;
 
 @SuppressWarnings("serial")
 public class OpenAlternativeDialog extends FilteredDialog {
 
     public OpenAlternativeDialog(
 	    Map<String, Map<String, Map<String, List<String>>>> divsCodes,
 	    Map<String, String> departNames, Map<String, String> municNames,
 	    Map<String, String> cantonNames) {
 	super(divsCodes, departNames, municNames, cantonNames);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
 	try {
 	    if (e.getSource() == okButton) {
 		String alternCod = elementCombo.getSelectedItem().toString();
 		OpenAlternativeExtension.openAlternative(alternCod);
 		OpenAlternativeExtension
 			.setValidAlternative(DatabaseDirectAccessQueries
 				.isValidAlternative(alternCod));
 	    }
 	} catch (Exception e1) {
 	    OpenAlternativeExtension.setValidAlternative(false);
 	    e1.printStackTrace();
 	}
 	PluginServices.getMDIManager().closeWindow(this);
 
     }
 
     @Override
     protected Logger getLogger() {
 	return Logger.getLogger("AlternativesExtension");
     }
 
     @Override
     protected String getElementLabel() {
 	return "Cod_alternativa";
     }
 
     @Override
     protected String getDialogTitle() {
	return "open_alternative";
     }
 
 }
