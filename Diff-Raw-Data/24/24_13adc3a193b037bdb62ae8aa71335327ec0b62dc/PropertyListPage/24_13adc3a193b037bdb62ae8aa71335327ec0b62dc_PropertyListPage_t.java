 package ar.edu.itba.paw.grupo1.web.PropertyList;
 
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.repository.PropertyRepository;
 import ar.edu.itba.paw.grupo1.web.AddProperty.AddPropertyPage;
 import ar.edu.itba.paw.grupo1.web.Base.BasePage;
 import ar.edu.itba.paw.grupo1.web.EditProperty.EditPropertyPage;
 import ar.edu.itba.paw.grupo1.web.PropertyDetail.PropertyDetailPage;
 
 @SuppressWarnings("serial")
 public class PropertyListPage extends BasePage {
 
 	private List<Property> propertyList;
 	@SpringBean
 	private PropertyRepository properties;
 
 	public PropertyListPage() {
 
 		propertyList = properties.getProperties(getSignedInUser());
 		
 		add(new BookmarkablePageLink<Void>("newProperty", AddPropertyPage.class));
 		add(this, new Label("noProperties"), propertyList == null || propertyList.isEmpty());
 
 		add(new ListView<Property>("properties", new PropertyModel<List<Property>>(this, "propertyList")) {
 			
 			@Override
 			protected void populateItem(ListItem<Property> item) {
				Property property = item.getModelObject();
				final int id = property.getId();
				
 				PageParameters pars = new PageParameters();
				pars.add("id", id);
 				Link<Void> detailLink = new BookmarkablePageLink<Void>("detail", PropertyDetailPage.class, pars);
 				detailLink.add(new Label("id", property.getId().toString()));
 				item.add(detailLink);
 
 				item.add(new Label("description", property.getDescription()));
 				item.add(new BookmarkablePageLink<Void>("edit", EditPropertyPage.class, pars));
 				Link<Void> publishLink = new Link<Void>("publish") {
 
 					@Override
 					public void onClick() {
						properties.get(id).publish();
						setResponsePage(PropertyListPage.class);
 					}
 				};
 				add(item, publishLink, !property.isPublished());
 				
 				Link<Void> unpublishLink = new Link<Void>("unpublish") {
 
 					@Override
 					public void onClick() {
						properties.get(id).unpublish();
						setResponsePage(PropertyListPage.class);
 					}
 				};
 				add(item, unpublishLink, property.isPublished());
 				
 				Link<Void> reserveLink = new Link<Void>("reserve") {
 
 					@Override
 					public void onClick() {
						properties.get(id).reserve();
						setResponsePage(PropertyListPage.class);
 					}
 				};
 				add(item, reserveLink, !property.isReserved());
 				
 				Link<Void> unreservelink = new Link<Void>("unreserve") {
 
 					@Override
 					public void onClick() {
						properties.get(id).unreserve();
						setResponsePage(PropertyListPage.class);
 					}
 				};
 				add(item, unreservelink, property.isReserved());			
 			}
 
 			private void add(ListItem<Property> item, Component c, boolean condition) {
 				c.setVisible(condition);
 				item.add(c);
 			}
 		});
 
 	}
 }
