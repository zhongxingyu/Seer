 package wicket.contrib.scriptaculous.examples;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wicket.PageParameters;
 import wicket.contrib.scriptaculous.autocomplete.CustomLayoutAutocompleteResultsPageContribution;
 import wicket.markup.html.basic.Label;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 
 public class CustomLayoutAjaxAutocompleteExamplePageContribution extends CustomLayoutAutocompleteResultsPageContribution {
 
 	public CustomLayoutAjaxAutocompleteExamplePageContribution(PageParameters parameters) {
 		super(parameters);
 	}
 
 	protected ListView buildListView(String input) {
 		List results = new ArrayList();
 		results.add(new CustomResultObject("ryan.gif", "Ryan Sonnek", "ryan@youremail.com"));
 		results.add(new CustomResultObject("billy.gif", "Bill Gates", "bill.gates@microsoft.com"));
 		results.add(new CustomResultObject("janet.gif", "Janet Someone", "janet@thethirdwheel.com"));
		return new ListView("entries", results) {
 
 			protected void populateItem(ListItem item) {
 				CustomResultObject result = (CustomResultObject) item.getModelObject();
 
 				item.add(new Label("name", result.getName()));
 				item.add(new Label("email", result.getEmail()));
 			}
 		};
 	}
 
 	private class CustomResultObject {
 
 		private final String name;
 		private final String image;
 		private final String email;
 
 		public CustomResultObject(String image, String name, String email) {
 			this.image = image;
 			this.name = name;
 			this.email = email;
 		}
 
 		public String getEmail()
 		{
 			return email;
 		}
 
 		public String getImage()
 		{
 			return image;
 		}
 
 		public String getName()
 		{
 			return name;
 		}
 
 
 	}
 }
 
 
