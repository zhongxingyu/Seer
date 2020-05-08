 package cz.opendata.linked.geocoder.krovak;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.TextField;
 
 import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
 import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
 
 /**
  * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
  *
  */
 public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 7003725620084616056L;
 	private GridLayout mainLayout;
     private TextField interval;
     private TextField tfNumOfRecords;
 
 	public ExtractorDialog() {
 		super(ExtractorConfig.class);
         buildMainLayout();
         setCompositionRoot(mainLayout);
 
     }  
 	
     private GridLayout buildMainLayout() {
         // common part: create layout
         mainLayout = new GridLayout(1, 2);
         mainLayout.setImmediate(false);
         mainLayout.setWidth("100%");
         mainLayout.setHeight("100%");
         mainLayout.setMargin(false);
         //mainLayout.setSpacing(true);
 
         // top-level component properties
         setWidth("100%");
         setHeight("100%");
 
         interval = new TextField();
         interval.setCaption("Interval between downloads:");
         mainLayout.addComponent(interval);
 
         tfNumOfRecords = new TextField();
         tfNumOfRecords.setCaption("Number of records in one block:");
         mainLayout.addComponent(tfNumOfRecords);
 
         return mainLayout;
     }	
      
 	@Override
 	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
 		interval.setValue(Integer.toString(conf.interval));
 		tfNumOfRecords.setValue(Integer.toString(conf.numofrecords));

 	}
 
 	@Override
 	public ExtractorConfig getConfiguration() throws ConfigException {
 		ExtractorConfig conf = new ExtractorConfig();
 		conf.interval = Integer.parseInt(interval.getValue());
 		conf.numofrecords = Integer.parseInt(tfNumOfRecords.getValue());
 		return conf;
 	}
 	
 }
