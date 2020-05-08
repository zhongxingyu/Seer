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
  * @author <a href="http://www.demay-fr.net/blog">Vincent Demay</a>
  *FIXME : authorize i18n
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
 		pattern = "dd/MM/yyyy";
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
		if (value != null && !("".equals(value[1]))){
			tag.put("date", value[1]);
			tag.put("value", value[1]);
		}else if(value == null && getValue() != null){
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
 		if (value != null && !("".equals(value[1]))){
 			try
 			{
 				return formatter.parse(value[1]);
 			}
 			catch (ParseException e)
 			{
 				throw new ConversionException(e);
 			}
 		}
 		return null;
 	}
 }
