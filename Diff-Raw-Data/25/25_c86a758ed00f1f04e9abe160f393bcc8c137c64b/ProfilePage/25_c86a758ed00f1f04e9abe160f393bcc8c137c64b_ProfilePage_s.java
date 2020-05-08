 package nl.topicus.lan.dashboard.pages.personal;
 
import nl.topicus.cobra.types.personalia.Geslacht;
 import nl.topicus.lan.dashboard.DashboardSession;
 import nl.topicus.lan.dashboard.dao.providers.ProfileProvider;
 import nl.topicus.lan.dashboard.entities.person.Profile;
 import nl.topicus.lan.dashboard.models.ELModelFactory;
 import nl.topicus.lan.dashboard.pages.AbstractSecureBasePage;
 
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 
 public class ProfilePage extends AbstractSecureBasePage
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private IModel<Profile> profileModel;
 
 	public ProfilePage()
 	{
 		super();
 		profileModel = initializeModel();
 		initializeForm();
 	}
 
 	/**
 	 * Initialize form
 	 */
 	private void initializeForm()
 	{
 		Form<Profile> form =
 			new Form<Profile>("form", new CompoundPropertyModel<Profile>(profileModel))
 			{
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void onSubmit()
 				{
 					ProfileProvider provider = new ProfileProvider();
 					provider.begin();
 					profileModel.getObject().saveOrUpdate();
 					provider.commit();
 				}
 			};
 
 		form.add(new TextField<String>("nickname"));
 
 		// Personal
 		form.add(new TextField<String>("name"));
 		form.add(new TextField<String>("surname"));
 		form.add(new TextField<String>("lastname"));
		form.add(new DropDownChoice<Geslacht>("gender"));
 		form.add(new TextField<Integer>("tableNumber"));
 
 		add(form);
 	}
 
 	/**
 	 * Returns new profile, or current profile as model.
 	 */
 	private IModel<Profile> initializeModel()
 	{
 		IModel<Profile> model = null;
 		if (DashboardSession.get().getAccount().getObject().getProfile() != null)
 		{
 			model =
 				ELModelFactory.getModel(DashboardSession.get().getAccount().getObject()
 					.getProfile());
 		}
 		else
 		{
 			Profile newProfile = new Profile();
 			newProfile.setAccount(DashboardSession.get().getAccount().getObject());
 			model = ELModelFactory.getModel(newProfile);
 		}
 		return model;
 	}
 }
