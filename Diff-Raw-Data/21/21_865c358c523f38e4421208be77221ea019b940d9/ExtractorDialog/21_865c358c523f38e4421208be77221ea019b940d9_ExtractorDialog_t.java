 package cz.opendata.linked.geocoder.nominatim;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.TwinColSelect;
 
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
     private TextField limit;
     private TextField hoursToCheck;
     private TextField tfCountry;
     private CheckBox chkStructured;
     private CheckBox chkStripNumFromLocality;
     private TwinColSelect tcsProperties;
     private String properties[] = {"s:streetAddress", "s:addressRegion", "s:addressLocality", "s:postalCode"};
     
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
 
         chkStructured = new CheckBox("Structured query (experimental):");
         chkStructured.setDescription("When selected, Nominatim will be queried by structured queries.");
         chkStructured.setWidth("100%");
         
         mainLayout.addComponent(chkStructured);
 
         chkStripNumFromLocality = new CheckBox("Strip number form Locality:");
         chkStripNumFromLocality.setDescription("In structured query, replace Praha 6 => Praha, etc.");
         chkStripNumFromLocality.setWidth("100%");
         
         mainLayout.addComponent(chkStripNumFromLocality);
 
         tfCountry = new TextField();
         tfCountry.setCaption("Country");
         mainLayout.addComponent(tfCountry);
 
         interval = new TextField();
         interval.setCaption("Interval between downloads:");
         mainLayout.addComponent(interval);
 
         limit = new TextField();
         limit.setCaption("Maximum amount of downloads:");
         mainLayout.addComponent(limit);
 
         hoursToCheck = new TextField();
         hoursToCheck.setCaption("Maximum amount interval:");
         mainLayout.addComponent(hoursToCheck);
 
         tcsProperties = new TwinColSelect("Select properties to use");
         tcsProperties.setLeftColumnCaption("Supported properties");
         tcsProperties.setRightColumnCaption("Selected properties");
         for (int p = 0; p < properties.length; p++) tcsProperties.addItem(properties[p]);
         tcsProperties.setRows(properties.length);
         mainLayout.addComponent(tcsProperties);
         
         return mainLayout;
     }	
      
 	@Override
 	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
 		interval.setValue(Integer.toString(conf.interval));
 		hoursToCheck.setValue(Integer.toString(conf.hoursToCheck));
 		limit.setValue(Integer.toString(conf.limit));
 		tfCountry.setValue(conf.country);
 		chkStructured.setValue(conf.structured);
 		chkStripNumFromLocality.setValue(conf.stripNumFromLocality);
 
 		LinkedList<String> values = new LinkedList<String>();
 		if (conf.useStreet) values.add(properties[0]);
 		if (conf.useRegion) values.add(properties[1]);
 		if (conf.useLocality) values.add(properties[2]);
 		if (conf.usePostalCode) values.add(properties[3]);
 		tcsProperties.setValue(values);
 	}
 
 	@Override
 	public ExtractorConfig getConfiguration() throws ConfigException {
 		ExtractorConfig conf = new ExtractorConfig();
 		conf.interval = Integer.parseInt(interval.getValue());
 		conf.hoursToCheck = Integer.parseInt(hoursToCheck.getValue());
 		conf.country = tfCountry.getValue();
 		conf.limit = Integer.parseInt(limit.getValue());
 		conf.structured = chkStructured.getValue();
 		conf.stripNumFromLocality = chkStripNumFromLocality.getValue();
 		
 		Collection<String> values = (Collection<String>)tcsProperties.getValue();
		conf.useStreet = values.contains(properties[0]); 
		conf.useRegion = values.contains(properties[1]);
		conf.useLocality = values.contains(properties[2]);
		conf.usePostalCode = values.contains(properties[3]);
 		
 		return conf;
 	}
 	
 }
