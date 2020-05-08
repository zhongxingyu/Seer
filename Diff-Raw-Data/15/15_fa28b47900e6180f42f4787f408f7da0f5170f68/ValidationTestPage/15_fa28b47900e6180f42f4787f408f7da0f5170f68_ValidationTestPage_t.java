 package wicket.contrib.dojo.examples;
 
 import wicket.PageParameters;
 import wicket.contrib.dojo.markup.html.form.validation.FXFeedbackIndicator;
 import wicket.contrib.dojo.markup.html.form.validation.FXValidationAjaxHandler;
 import wicket.contrib.dojo.markup.html.form.validation.bubble.DojoBubbleValidationAjaxBehavior;
 import wicket.contrib.dojo.markup.html.form.validation.bubble.DojoErrorBubble;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.form.FormComponent;
 import wicket.markup.html.form.RequiredTextField;
 import wicket.markup.html.form.TextArea;
 import wicket.model.PropertyModel;
import wicket.validation.validator.NumberValidator;
import wicket.validation.validator.StringValidator;
 
 public class ValidationTestPage extends WebPage{
 
 	private static final long serialVersionUID = 1L;
 	private Form form;
 	//private TextField testfield;
 	private TextArea testfield;
 	private String textValue;
 	private	FormComponent tx;
 	private int integerInRangeProperty;
 	private int integerInRangeProperty2;
 	
 	private Form formBis;
 	//private TextField testfield;
 	private TextArea testfieldBis;
 	private String textValueBis;
 	private	FormComponent txBis;
 	private int integerInRangePropertyBis;
 	private int integerInRangeProperty2Bis;
 	
 	
 	
 	public ValidationTestPage(PageParameters parameters)
 	{
 		 
 			form = new Form("testform");
 			add(form);
 			//testfield = new RequiredTextField("text", new PropertyModel(ValidationTestPage.this, "textValue"));
 			testfield =  new TextArea("text",new PropertyModel(ValidationTestPage.this, "textValue"));
 			testfield.add(StringValidator.maximumLength(5));
 			testfield.add(new FXValidationAjaxHandler("onblur"));
 			form.add(testfield);
 			
 			FXFeedbackIndicator indicator = new FXFeedbackIndicator("indicator");
 	        form.add(indicator);
 
 			
 	        FormComponent tx = new RequiredTextField("integerInRangeProperty", new PropertyModel(ValidationTestPage.this, "integerInRangeProperty"),Integer.class).add(NumberValidator.range(0, 100));
 	        form.add(tx);
 	        tx.add(new FXValidationAjaxHandler("onblur"));
 			
 
 			//test multiple ajax textfields
 			FormComponent tx2 = new RequiredTextField("integerInRangeProperty2", new PropertyModel(ValidationTestPage.this, "integerInRangeProperty2"),Integer.class).add(NumberValidator.range(0, 200));
 			form.add(tx2);
 			tx2.add(new FXValidationAjaxHandler("onblur"));
 			
 			
 			/**************************************/
 			
 			
 			formBis = new Form("testformBis");
 			add(formBis);
 			
 			final DojoErrorBubble bubble = new DojoErrorBubble("bubble");
 			add(bubble);
 
 			
 			testfieldBis =  new TextArea("textBis",new PropertyModel(ValidationTestPage.this, "textValueBis"));
 			testfieldBis.setOutputMarkupId(true);
 			testfieldBis.add(StringValidator.maximumLength(5));
 			testfieldBis.add(new DojoBubbleValidationAjaxBehavior("onblur", bubble));
 			formBis.add(testfieldBis);
 			
 	        FormComponent txBis = new RequiredTextField("integerInRangePropertyBis", new PropertyModel(ValidationTestPage.this, "integerInRangePropertyBis"),Integer.class).add(NumberValidator.range(0, 100));
 	        formBis.add(txBis);
 	        txBis.setOutputMarkupId(true);
 	        txBis.add(new DojoBubbleValidationAjaxBehavior("onblur", bubble));
 			
 
 			//test multiple ajax textfields
 			FormComponent tx2Bis = new RequiredTextField("integerInRangeProperty2Bis", new PropertyModel(ValidationTestPage.this, "integerInRangeProperty2Bis"),Integer.class).add(NumberValidator.range(0, 200));
 			formBis.add(tx2Bis);
 			tx2Bis.setOutputMarkupId(true);
 			tx2Bis.add(new DojoBubbleValidationAjaxBehavior("onblur", bubble));
 
 	        
 	}
 	public String getTextValue()
 	{
 		return this.textValue;
 	}
 	public void setTextValue(String v)
 	{
 		this.textValue = v;
 	}
 	public int getIntegerInRangeProperty()
 	{
 		return this.integerInRangeProperty;
 	}
 	public int getIntegerInRangeProperty2()
 	{
 		return this.integerInRangeProperty2;
 	}
 	public void setIntegerInRangeProperty(int i)
 	{
 		this.integerInRangeProperty = i;
 	}
 	public void setIntegerInRangeProperty2(int i)
 	{
 		this.integerInRangeProperty2 = i;
 	}
 }
