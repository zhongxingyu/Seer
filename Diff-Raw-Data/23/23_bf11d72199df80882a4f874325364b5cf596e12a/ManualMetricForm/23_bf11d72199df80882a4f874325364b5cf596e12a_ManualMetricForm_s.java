 package de.uni_leipzig.simba.saim.gui.widget;
 
 import com.google.gwt.text.client.DoubleParser;
 import com.vaadin.data.Validator;
 import com.vaadin.data.validator.DoubleValidator;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Button.ClickEvent;
 
 import de.uni_leipzig.simba.saim.SAIMApplication;
 import de.uni_leipzig.simba.saim.core.Configuration;
 
 public class ManualMetricForm extends Form{
 	final TextField metricTextField = new TextField("Insert metric here");
 	final TextField thresholdTextField = new TextField("Acceptance threshold");
 	
 	public ManualMetricForm() {
 		HorizontalLayout perform = new HorizontalLayout();
 		perform.addComponent(metricTextField);
 		perform.addComponent(thresholdTextField);
 		setLayout(perform);
 		HorizontalLayout okbar = new HorizontalLayout();
 		okbar.setHeight("25px");
 		
 		setDefaultValues();
 		addValidators();
 		
 		this.addField("metric", metricTextField);
 		this.addField("threshold", thresholdTextField);
 		getFooter().addComponent(okbar);
 		Button okbutton = getOKButton();
 		okbar.addComponent(okbutton);
 		okbar.setComponentAlignment(okbutton, Alignment.TOP_RIGHT);
 		okbar.addComponent(new Button("Reset", this, "setDefaultValues"));
 	}
 	
 
 	private void addValidators() {
 		thresholdTextField.addValidator(new ThresHoldValidator());
 		metricTextField.addValidator(new MetricValidator());
 	}
 	
 	private Button getOKButton() {
 		Button b = new Button("OK", this, "commit");
 		b.addListener(new ClickListener() {			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				// get and store values
 				String metric = metricTextField.getValue().toString();
 				Double threshold = Double.parseDouble(thresholdTextField.getValue().toString());
 				Configuration config = Configuration.getInstance();
 				config.setMetricExpression(metric);
 				config.setAcceptanceThreshold(threshold);
 //				config.setDefaultNameSpaces();
 				// run it
				SAIMApplication appl = (SAIMApplication) getApplication();
				appl.showComponent(new ExecutionPanel());
 			}
 		});
 		return b;
 	}
 	
 	public class ThresHoldValidator extends DoubleValidator {
 		public ThresHoldValidator() {
 			this("A Threshold must be a value between 1 and 0.");
 		}		
 		public ThresHoldValidator(String errorMessage) {
 			super(errorMessage);
 		}		
 		@Override
 	    protected boolean isValidString(String value) {
 	        try {
 	            double d = Double.parseDouble(value);
 	            return d>0;
 	        } catch (Exception e) {
 	            return false;
 	        }
 	    }
 	}
 	
 	public class MetricValidator implements Validator {	
 		@Override
 		public void validate(Object value) throws InvalidValueException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public boolean isValid(Object value) {
 			return value.toString().length()>0;
 		}
 	}
 	
 	public void setDefaultValues() {
 		thresholdTextField.setValue(0.8d);
 		String metric = "trigram(src.rdfs:label, dest.rdfs:label)";	
 		//metric ="ADD(0.5*Trigram(src.rdfs:label,dest.rdfs:label), 0.5*Trigram(src.rdfs:label,dest.sider:sideEffectName))";
 		//drugbank - sider
 		//metric = "trigrams(src.sider:drugName,dest.dailymed:name)";
 		metricTextField.setValue(metric);
 	}
 }
