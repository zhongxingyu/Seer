 package wicket.contrib.dojo.markup.html.form;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import wicket.behavior.AttributeAppender;
 import wicket.contrib.dojo.toggle.DojoToggle;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.form.FormComponent;
 import wicket.markup.html.form.TextField;
 import wicket.model.IModel;
 import wicket.model.Model;
 import wicket.util.convert.ConversionException;
 
 /**
  * <p>
  * A DatePicker to select a Date in a popup (not navigator but Js)
  * You can add effect to this popup adding a {@link DojoToggle} with the setToggle method.<br/>
  * This component should be associated to an <b>input</b> in the markup
  * </p>
  * <p>
  * <b><u>Sample</u></b>
  * <pre>
  * [...]
  * DojoDatePicker datePicker = new DojoDatePicker("date", new Model(date));
  * datePicker.setToggle(new DojoWipeToggle(200));
  * form.add(datePicker);
  * </pre>
  * </p>
  * 
  * @author <a href="http://www.demay-fr.net/blog">Vincent Demay</a>
  * 
  * FIXME : authorize i18n and pattern setting
  */
 public class DojoDatePicker extends TextField{
 	
 	private SimpleDateFormat formatter;
 	private String pattern;
 
 	/**
 	 * @param parent
 	 * @param id
 	 * @param model
 	 * @param pattern
 	 */
 	public DojoDatePicker(String id, IModel model/*, String pattern*/)
 	{
 		super(id, model);
 		add(new DojoDatePickerHandler());
 		this.setOutputMarkupId(true);
 		//setDatePattern(pattern);
 		pattern = "MM/dd/yyyy";
 		formatter = new SimpleDateFormat(pattern);
 	}
 	
 	
 	public DojoDatePicker(String id){
 		this(id, null);
 	}
 	
 	/**
 	 * Set the date pattern
 	 * @param pattern date pattern example %d/%m/%y
 	 */
 	/*public void setDatePattern(String pattern){
 		this.pattern = pattern;
 		formatter = new SimpleDateFormat(getSimpleDatePattern());
 	}*/
 	
 	/*private String getSimpleDatePattern(){
 		return pattern.replace("%d", "dd").replace("%Y", "yyyy").replace("%m", "MM");
 	}*/
 
 	protected void onComponentTag(ComponentTag tag)
 	{
 		super.onComponentTag(tag);
 		String[] value = getInputAsArray();
 		if (isDojoValue(value)){							//value returned by Dojo - Classic
 			tag.put("date", value[1]);
 			tag.put("value", value[1]);
 		}else if(getValue() != null && ( value == null || value.length < 2)){	//value returned when js is inactive - TestCase
 			tag.put("date", getValue());
 			tag.put("value", getValue());
 		}else{
 			tag.put("date", "");
 			tag.put("value", "");
 		}
 		tag.put("dojoType", "dropdowndatepicker");
 		tag.put("dateFormat", "%m/%d/%Y");
 		tag.put("inputName", this.getId());
 	}
 	
 	/**
 	 * return true if it is a Dojo Request field or false otherwise (in test for exemple)
 	 * @param value request argument giving the value
 	 * @return true if it is a Dojo Request field or false otherwise (in test for exemple)
 	 */
 	private boolean isDojoValue(String[] value){
 		return (value != null && value.length > 1 && !("".equals(value[1])) );
 	}
 
 	/**
 	 * Set the date picker effect
 	 * @param toggle
 	 */
 	public void setToggle(DojoToggle toggle){
 		this.add(new AttributeAppender("containerToggle", new Model(toggle.getToggle()),""));
 		this.add(new AttributeAppender("containerToggleDuration", new Model(toggle.getDuration() + ""),""));
 	}
 
 	/**
 	 * @see FormComponent#getModelValue()
 	 */
 	public final String getModelValue()
 	{
 		if (getModelObject() != null){
 			return formatter.format((Date)getModelObject());
 		}
 		return null;
 	}
 
 
 	protected Object convertValue(String[] value) throws ConversionException
 	{
 		String usableValue;
 		
 		if (isDojoValue(value)){							//value returned by Dojo - Classic 
 			usableValue = value[1];
		}else if(getValue() != null){		//value returned when js is inactive - TestCase
 			usableValue = getValue();
 		}else{
 			return null;									//No value
 		}
 		
 		try
 		{
 			return formatter.parse(usableValue);
 		}
 		catch (ParseException e)
 		{
 			throw new ConversionException(e);
 		}
 		
 	}
 }
