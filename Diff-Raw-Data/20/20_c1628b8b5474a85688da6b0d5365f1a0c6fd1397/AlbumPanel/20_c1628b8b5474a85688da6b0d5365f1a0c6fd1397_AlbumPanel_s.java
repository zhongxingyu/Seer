 package com.madalla.webapp.components.image.album;
 
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
 import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.emalan.cms.bo.image.AlbumData;
 import org.emalan.cms.bo.image.ImageData;
 
 import com.madalla.webapp.admin.pages.AdminPageLink;
 import com.madalla.webapp.admin.pages.AlbumAdminPage;
 import com.madalla.webapp.css.Css;
 
 public class AlbumPanel extends Panel {
 	private static final long serialVersionUID = 1L;
 
     public AlbumPanel(String id, AlbumData album, IModel<List<ImageData>> imagesModel) {
     	this(id, album, imagesModel, false);
     }
 
 	public AlbumPanel(String id, final AlbumData album, final IModel<List<ImageData>> imagesModel, final boolean navigation) {
 		super(id);
 		
       

         //link to album configure page
         Component adminPageLink;
         final PageParameters pageParameters = new PageParameters();
         pageParameters.set(0, album.getName());
         add(adminPageLink = new AdminPageLink("adminLink", AlbumAdminPage.class, pageParameters));
         MetaDataRoleAuthorizationStrategy.authorize(adminPageLink, ENABLE, "ADMIN");
 
         final AjaxLazyLoadPanel imageListContainer  = new AjaxLazyLoadPanel("imagesPanel"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public Component getLazyLoadComponent(String markupId) {
 				return new AlbumImagesPanel(markupId, album, imagesModel, false);
 			}
 
 			@Override
 			public Component getLoadingComponent(String markupId) {
 				MarkupContainer fragment = new Fragment(markupId, "loading", AlbumPanel.this) ;
 				
 				MarkupContainer container;
 				fragment.add(container = new WebMarkupContainer("container"){
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					protected void onComponentTag(ComponentTag tag) {
 						super.onComponentTag(tag);
 						tag.put("style", "height: " + (album.getHeight() + 15) + "px; width: " + (album.getWidth() + 15)
 								+ "px;");
 					}
 				});
 				container.add(new Label("image", "<img alt=\"Loading...\" class=\"bannerImage\"" + 
 						" src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR, null) + "\"/>"){
 					
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					protected void onComponentTag(ComponentTag tag) {
 						super.onComponentTag(tag);
 						tag.put("style", "height: " + album.getHeight() + "px; width: " + album.getWidth()
 								+ "px;");
 					}
 					
 				}.setEscapeModelStrings(false));
 				
 				return fragment;
 			}
         	
 			
         };
         imageListContainer.setOutputMarkupId(true);
         add(imageListContainer);
 
 
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		 response.renderCSSReference(Css.CSS_IMAGE);
 	}
 
 }
