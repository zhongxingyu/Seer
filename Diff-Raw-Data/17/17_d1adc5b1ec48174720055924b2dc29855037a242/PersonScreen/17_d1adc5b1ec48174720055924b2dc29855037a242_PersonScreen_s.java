 package com.fbs.web.vaadin.ui.user.party;
 
 import java.util.List;
 
 import com.fbs.dmr.universal.model.party.Person;
 import com.fbs.web.vaadin.application.MyVaadinApplication;
 import com.fbs.web.vaadin.i18n.ApplicationMessages;
 import com.vaadin.data.Item;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 
 public class PersonScreen extends PartyScreen<Person>
 {
 	private static final long serialVersionUID = 1L;
 	private static final String BDATE = "birthDate";
 	private static final String COMMENT = "comment";
 	private static final String FIRST_NAME = "firstName";
 	private static final String GENDER = "gender";
 	private static final String ID = "id";
 	private static final String MIDDLE_NAME = "middleName";
 	private static final String LAST_NAME = "lastName";
 	private static final String TITLE = "title";
 	private static final String[] VISIBLE_COLUMNS = new String[]
 		{ ID, FIRST_NAME, LAST_NAME };
 	private static final String[] VISIBLE_FIELDS = new String[]
 		{ ID, GENDER, TITLE, FIRST_NAME, LAST_NAME, MIDDLE_NAME, BDATE, COMMENT};
 
 	public PersonScreen(MyVaadinApplication app)
 	{
 		super(app, Person.class, VISIBLE_COLUMNS, VISIBLE_FIELDS);
 	}
 
 	@Override
 	protected Person createBeanInstance()
 	{
 		return new Person();
 	}
 
 	@Override
 	protected List<Person> getAllBeans() throws Exception
 	{
 		List<Person> result = this.services.getCrudServicePerson().findAll();
 
 		return result;
 	}
 
 	@Override
 	protected Person createBean(Person t) throws Exception
 	{
		super.createBean(t);

 		this.services.getCrudServicePerson().create(t);
 
 		return t;
 	}
 
 	@Override
 	protected void updateBean(Person t) throws Exception
 	{
 		super.updateBean(t);
 
 		this.services.getCrudServicePerson().update(t);
 	}
 
 	@Override
 	protected Person readBean(Person t) throws Exception
 	{
 		Person result;
 
 		result = this.services.getCrudServicePerson().read(t.getId());
 
 		return result;
 	}
 
 	@Override
 	protected void deleteBean(Person t) throws Exception
 	{
 		this.services.getCrudServicePerson().delete(t.getId());
 	}
 
 	@Override
 	protected String getColumnName(String propertyId)
 	{
 		if (propertyId.equals(ID))
 			return this.app.getMessage(ApplicationMessages.PersonId);
 
 		if (propertyId.equals(FIRST_NAME))
 			return this.app.getMessage(ApplicationMessages.PersonFirstName);
 
 		if (propertyId.equals(LAST_NAME))
 			return this.app.getMessage(ApplicationMessages.PersonLastName);
 
 		return propertyId;
 	}
 
 	@Override
 	protected FormFieldFactory getFormFieldFactory()
 	{
 		return new PersonFormFieldFactory(this.app);
 	}
 
 	private static class PersonFormFieldFactory implements FormFieldFactory
 	{
 		private static final long serialVersionUID = 1L;
 		private MyVaadinApplication app;
 
 		public PersonFormFieldFactory(MyVaadinApplication app)
 		{
 			this.app = app;
 		}
 
 		@Override
 		public Field createField(Item item, Object propertyId, Component uiContext)
 		{
 			// Identify the fields by their Property ID.
 			Field result = null;
 
 			String pid = (String) propertyId;
 
 			if (ID.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.PersonId));
 
 				result.setReadOnly(true);
 			}
 			else if (FIRST_NAME.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.PersonFirstName));
 			}
 			else if (LAST_NAME.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.PersonLastName));
 			}
 			else if (MIDDLE_NAME.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.PersonMiddleName));
 			}
 			else if (GENDER.equals(pid))
 			{
 				Select select;
 				String caption;
 
 				caption = this.app.getMessage(ApplicationMessages.PersonGender);
 				
 				select = new Select(caption);
 				select.addItem("");
 				select.addItem(this.app.getMessage(ApplicationMessages.PersonGenderFemaleSalutation));
 				select.addItem(this.app.getMessage(ApplicationMessages.PersonGenderMaleSalutation));
 				
 
 				result = select;
 			}
 			else if (BDATE.equals(pid))
 			{
 				DateField date;
 				
 				
 				date = new DateField(this.app.getMessage(ApplicationMessages.PersonBirthDate));
 				date.setResolution(DateField.RESOLUTION_DAY);
 
 				result = date;
 			}
 			else if (TITLE.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.PersonTitleAcademic));
 			}
 			else if (COMMENT.equals(pid))
 			{
 				TextArea area;
 				
 				area = new TextArea(this.app.getMessage(ApplicationMessages.CommonComment));
 				area.setColumns(40);
 				area.setRows(5);
 				
 				result = area;
 			}
 
 			return result;
 		}
 	}
 }
