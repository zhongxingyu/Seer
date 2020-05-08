 package com.fbs.web.vaadin.ui.user.party;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.fbs.dmr.universal.model.party.Organization;
 import com.fbs.web.vaadin.application.MyVaadinApplication;
 import com.fbs.web.vaadin.i18n.ApplicationMessages;
 import com.vaadin.data.Item;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.TextField;
 
 
 public class OrganizationScreen extends PartyScreen<Organization>
 {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(OrganizationScreen.class.getName());
 	private static final String ID = "id";
 	private static final String NAME = "name";
 
 	private static final String[] VISIBLE_COLUMNS = new String[] { ID, NAME };
 	private static final String[] VISIBLE_FIELDS = new String[] { ID, NAME };
 
 
 	public OrganizationScreen(MyVaadinApplication app)
 	{
 		super(app, Organization.class, VISIBLE_COLUMNS, VISIBLE_FIELDS);
 	}
 
 
 	@Override
 	protected Organization createBeanInstance()
 	{
 		return new Organization();
 	}
 
 
 	@Override
 	protected List<Organization> getAllBeans() throws Exception
 	{
 		List<Organization> organizations;
 
 		organizations = this.services.getCrudServiceOrganization().findAll();
 
 		return organizations;
 	}
 
 
 	@Override
 	protected Organization createBean(Organization t) throws Exception
	{		
 		this.services.getCrudServiceOrganization().create(t);
		
		super.createBean(t);
 
 		return t;
 	}
 
 
 	@Override
 	protected void updateBean(Organization t) throws Exception
 	{		
 		logger.info(">updateBean");
 		super.updateBean(t);
 		
 		this.services.getCrudServiceOrganization().update(t);
 		logger.info("<updateBean");
 	}
 
 
 	@Override
 	protected Organization readBean(Organization t) throws Exception
 	{
 		Organization org;
 
 		org = this.services.getCrudServiceOrganization().read(t.getId());
 
 		return org;
 	}
 
 
 	@Override
 	protected void deleteBean(Organization t) throws Exception
 	{
 		this.services.getCrudServiceOrganization().delete(t.getId());
 	}
 
 
 	@Override
 	protected String getColumnName(String propertyId)
 	{
 		if (propertyId.equals(ID))
 			return this.app.getMessage(ApplicationMessages.OrgId);
 
 		if (propertyId.equals(NAME))
 			return this.app.getMessage(ApplicationMessages.OrgName);
 
 		return propertyId;
 	}
 
 
 	@Override
 	protected FormFieldFactory getFormFieldFactory()
 	{
 		return new OrganizationFormFieldFactory(this.app);
 	}
 
 	private static class OrganizationFormFieldFactory implements FormFieldFactory
 	{
 		private static final long serialVersionUID = 1L;
 		private MyVaadinApplication app;
 
 
 		public OrganizationFormFieldFactory(MyVaadinApplication app)
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
 				result = new TextField(this.app.getMessage(ApplicationMessages.OrgId));
 
 				result.setReadOnly(true);
 			}
 			else if (NAME.equals(pid))
 			{
 				result = new TextField(this.app.getMessage(ApplicationMessages.OrgName));
 			}
 			
 
 			return result;
 		}
 	}
 }
