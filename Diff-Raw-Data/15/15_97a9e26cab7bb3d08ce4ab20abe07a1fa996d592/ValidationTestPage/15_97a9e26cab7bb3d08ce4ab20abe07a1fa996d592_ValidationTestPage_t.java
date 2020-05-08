 package wicket.contrib.dojo.examples;
 
 import wicket.PageParameters;
 import wicket.contrib.markup.html.form.validation.FXFeedbackIndicator;
 import wicket.contrib.markup.html.form.validation.FXValidationAjaxHandler;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.form.FormComponent;
 import wicket.markup.html.form.TextArea;
import wicket.markup.html.form.TextField;
 import wicket.markup.html.panel.FeedbackPanel;
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
 	
 	
 	
 	public ValidationTestPage(PageParameters parameters)
 	{
 		 
 			form = new Form(this, "testform");
 			//testfield = new RequiredTextField("text", new PropertyModel(ValidationTestPage.this, "textValue"));
 			testfield =  new TextArea(form, "text",new PropertyModel(ValidationTestPage.this, "textValue"));
 			testfield.add(StringValidator.maximumLength(5));
 			testfield.add(new FXValidationAjaxHandler("onblur"));
 			
 			
 			FXFeedbackIndicator indicator = new FXFeedbackIndicator(form, "indicator");
 	        indicator.setIndicatorFor(testfield);
 
 			
	        FormComponent tx = new TextField(form, "integerInRangeProperty", new PropertyModel(ValidationTestPage.this, "integerInRangeProperty"), Integer.class);
            tx.setRequired(true);
	        tx.add(NumberValidator.range(0, 100));
 	        tx.add(new FXValidationAjaxHandler("onblur"));
 			
 			
 
 			//test multiple ajax textfields
			FormComponent tx2 = new TextField(form, "integerInRangeProperty2", new PropertyModel(ValidationTestPage.this, "integerInRangeProperty2"),Integer.class);
            tx2.setRequired(true);
			tx2.add(NumberValidator.range(0, 200));
 			tx2.add(new FXValidationAjaxHandler("onblur"));
 			
 			
 
 			
 			/*TextArea tarea = new TextArea("tarea", new PropertyModel(ValidationTestPage.this, "textValue"));
 	           tarea.add(LengthValidator.max(10));
 	                     //add the component first before you add the handler
 	          
 	           tarea.add(new FXValidationAjaxHandler("onblur", 251, 156, 71)); 
 	        form.add(tarea);*/
 	        
 	        final FeedbackPanel feedback = new FeedbackPanel(this, "feedback");
 	        
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
