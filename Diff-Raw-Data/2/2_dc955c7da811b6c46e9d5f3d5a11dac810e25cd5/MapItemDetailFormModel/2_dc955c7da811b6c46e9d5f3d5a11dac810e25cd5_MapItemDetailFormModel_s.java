 package com.scurab.web.drifmaps.client.formmodel;
 
 import static com.pietschy.gwt.pectin.client.form.validation.ValidationPlugin.getValidationManager;
 import static com.pietschy.gwt.pectin.client.form.validation.ValidationPlugin.validateField;
 
 import com.google.gwt.core.client.GWT;
 import com.pietschy.gwt.pectin.client.bean.BeanModelProvider;
 import com.pietschy.gwt.pectin.client.form.FieldModel;
 import com.pietschy.gwt.pectin.client.form.FormModel;
 import com.pietschy.gwt.pectin.client.form.ListFieldModel;
 import com.pietschy.gwt.pectin.client.form.validation.validator.NotEmptyValidator;
 import com.pietschy.gwt.pectin.client.form.validation.validator.NotNullValidator;
 import com.scurab.web.drifmaps.client.validation.NotZeroValidation;
 import com.scurab.web.drifmaps.shared.datamodel.Detail;
 import com.scurab.web.drifmaps.shared.datamodel.MapItem;
 
 public class MapItemDetailFormModel extends FormModel
 {
 	public static abstract class DataProvider extends BeanModelProvider<MapItem>
 	{
 	}
 
 	private DataProvider itemProvider = GWT.create(DataProvider.class);
 
 	protected final FieldModel<String> type;
 
 	protected final FieldModel<String> name;
 	protected final FieldModel<String> city;
 	protected final FieldModel<String> street;
 	protected final FieldModel<String> country;
 	protected final FieldModel<String> web;
 	protected final FieldModel<String> streetViewLink;
 	protected final FieldModel<String> author;
 	protected final FieldModel<String> contact;
 	protected final ListFieldModel<Detail> details;
 	private final FieldModel<Double> x;
 	private final FieldModel<Double> y;
 
 	public MapItemDetailFormModel()
 	{
 		itemProvider.setAutoCommit(true);
 		
 		name = fieldOfType(String.class).boundTo(itemProvider, "name");
 		city = fieldOfType(String.class).boundTo(itemProvider, "city");
 		street = fieldOfType(String.class).boundTo(itemProvider, "street");
 		country = fieldOfType(String.class).boundTo(itemProvider, "country");
 		web = fieldOfType(String.class).boundTo(itemProvider, "web");
 		streetViewLink = fieldOfType(String.class).boundTo(itemProvider, "streetViewLink");
 		author = fieldOfType(String.class).boundTo(itemProvider, "author");
 		type = fieldOfType(String.class).boundTo(itemProvider, "type");
 		details = listOfType(Detail.class).boundTo(itemProvider, "details");
 		x = fieldOfType(Double.class).boundTo(itemProvider, "x");
 		y = fieldOfType(Double.class).boundTo(itemProvider, "y");
 		contact = fieldOfType(String.class).boundTo(itemProvider, "contact");
 		initValidation();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void initValidation()
 	{
 		validateField(name).using(new NotEmptyValidator("!"));
 		validateField(x).using(new NotZeroValidation("!"));
 		validateField(y).using(new NotZeroValidation("!"));
		validateField(type).using(new NotNullValidator("!"));
 		validateField(street).using(new NotNullValidator("!"));
 	}
 	
 	public boolean validate()
 	{
 		if(getValue() == null)
 			return true;
 		return getValidationManager(this).validate();
 	}
 	
 	public void setValue(MapItem item)
 	{
 		itemProvider.setValue(item);
 	}
 	
 	public MapItem getValue()
 	{
 		return itemProvider.getValue();
 	}
 
 	public FieldModel<String> getType()
 	{
 		return type;
 	}
 
 	public FieldModel<String> getName()
 	{
 		return name;
 	}
 
 	public FieldModel<String> getCity()
 	{
 		return city;
 	}
 
 	public FieldModel<String> getStreet()
 	{
 		return street;
 	}
 
 	public FieldModel<String> getWeb()
 	{
 		return web;
 	}
 
 	public FieldModel<String> getAuthor()
 	{
 		return author;
 	}
 
 	public ListFieldModel<Detail> getDetails()
 	{
 		return details;
 	}
 
 	public FieldModel<Double> getX()
 	{
 		return x;
 	}
 
 	public FieldModel<Double> getY()
 	{
 		return y;
 	}
 
 	public FieldModel<String> getCountry()
 	{
 		return country;
 	}
 
 	public FieldModel<String> getStreetViewLink()
 	{
 		return streetViewLink;
 	}
 
 	public FieldModel<String> getContact()
 	{
 		return contact;
 	}
 }
