 package com.madalla.webapp.components.image.album;
 
 import static com.madalla.webapp.scripts.scriptaculous.Scriptaculous.PROTOTYPE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.CROSSFADE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.FAST_INIT;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.emalan.cms.bo.image.AlbumData;
 import org.emalan.cms.bo.image.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import com.madalla.webapp.scripts.JavascriptResources;
 import com.madalla.webapp.scripts.utility.ScriptUtils;
 
 public class AlbumImagesPanel extends Panel{
 	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(AlbumImagesPanel.class);
 	
 	private final boolean navigation;
 
 	public AlbumImagesPanel(String id, final AlbumData album, final IModel<List<ImageData>> imagesModel, final boolean navigation) {
 		super(id);
 		this.navigation = navigation;
 		
 		final WebMarkupContainer imageListContainer  = new WebMarkupContainer("images");
         imageListContainer.setOutputMarkupId(true);
         add(imageListContainer);
 
         imageListContainer.add(new ListView<ImageData>("image-list", imagesModel) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<ImageData> item) {
 				final ImageData imageData = item.getModelObject();
 				item.add(createImage(album, imageData));
 			}
 
 			@Override
 			protected void onComponentTag(ComponentTag tag) {
 				super.onComponentTag(tag);
 			}
 
 			@Override
 			public void renderHead(HtmlHeaderContainer container) {
 				int interval = album.getInterval();
 				int width = album.getWidth();
 				int height = album.getHeight();
 				String params = "{interval:" + interval + ", height:" + height + ", width: " + width + "}";
 
 				if (navigation) {
 					container.getHeaderResponse().renderOnLoadJavaScript(
 							"new Banner($('" + imageListContainer.getMarkupId() + "')," + params + ")");
 				} else {
 					container.getHeaderResponse().renderOnLoadJavaScript(
 							"new Crossfade($('" + imageListContainer.getMarkupId() + "')," + params + ")");
 				}
 				super.renderHead(container);
 			}
 
 		});
 		
 		
 	}
 	
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		super.renderHead(response);
 
 		response.renderJavaScriptReference(PROTOTYPE);
 		response.renderJavaScriptReference(JavascriptResources.ANIMATOR);
 		response.renderJavaScriptReference(FAST_INIT);
 
 		if (navigation) {
 			response.renderJavaScriptReference(CROSSFADE);
 			response.renderJavaScriptReference(ScriptUtils.BANNER);
 			response.renderCSSReference(ScriptUtils.BANNER_CSS);
 		} else {
 			response.renderJavaScriptReference(CROSSFADE);
 			response.renderCSSReference(ScriptUtils.CROSSFADE_CSS);
 		}
 		
 	}
 	
 	private WebMarkupContainer createImageWrapper(final AlbumData album) {
 		return new WebMarkupContainer("imageWrapper") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onComponentTag(ComponentTag tag) {
 				super.onComponentTag(tag);
 				tag.put("style", "height: " + (album.getHeight() + 15) + "px; width: " + (album.getWidth() + 15)
 						+ "px;");
 				tag.put("class", "imageWrapper");
 			}
 		};
 	}
 
 	private Component createImage(final AlbumData album, final ImageData imageData) {
 		WebMarkupContainer imageWrapper = createImageWrapper(album);
 
 		Image image = new Image("id", imageData.getImageFull()) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onComponentTag(ComponentTag tag) {
 				super.onComponentTag(tag);
 				tag.put("height", album.getHeight());
 				tag.put("width", album.getWidth());
 				tag.put("title", imageData.getTitle());
 			}
 
 		};
 
 		if (StringUtils.isNotEmpty(imageData.getUrl())) {
 			image.add(new AjaxEventBehavior("onclick") {
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void onEvent(AjaxRequestTarget target) {
 					getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(imageData.getUrl()));
 				}
 			});
 		}
 		imageWrapper.add(image);
 
 		return imageWrapper;
 	}
 
 }
