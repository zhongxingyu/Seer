 package com.b88m.essence.confluence;
 
 import static com.atlassian.plugin.util.collect.CollectionUtil.transform;
 import static java.lang.Long.valueOf;
 import static javax.ws.rs.core.Response.ok;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
import com.atlassian.confluence.core.ContentEntityObject;
 import com.atlassian.confluence.labels.Label;
 import com.atlassian.confluence.labels.LabelManager;
 import com.atlassian.confluence.pages.Page;
 import com.atlassian.confluence.pages.PageManager;
 import com.atlassian.plugin.util.collect.Function;
 
 /**
  * The following APIs are defined :
  * 
  * favourites: return the list of favourites of the current user
  * 
  * document/{id}: return the document
  * 
  * spaces: return the list of favourite spaces of the user
  * 
  * space/{name}: return the content of the space
  */
 @Path("/data")
 public class EssenceResource {
 
 	// private final Log log = Logger.getInstance(EssenceResource.class);
 
 	private final LabelManager labelManager;
 	private final PageManager pageManager;
 	private final Mapper defaultMapper = new Mapper();
 	private final Mapper skippContent = new Mapper(true);
 
 	public EssenceResource(LabelManager labelManager, PageManager pageManager) {
 		this.labelManager = labelManager;
 		this.pageManager = pageManager;
 	}
 
 	@GET
 	@Produces( { MediaType.APPLICATION_JSON })
 	@Path("favourites")
 	public Response getFavourites() {
 		return ok(transform(findMyFavourites().iterator(), skippContent))
 				.build();
 	}
 
 	@GET
 	@Produces( { MediaType.APPLICATION_JSON })
 	@Path("document/{id}")
 	public Response getDocument(final @PathParam("id") String id) {
 		return ok(defaultMapper.get(getPage(valueOf(id)))).build();
 	}
 
 	@SuppressWarnings("unchecked")
	public List<ContentEntityObject> findMyFavourites() {
 		return labelManager.getCurrentContentForLabel(getLabel("my:favourite"));
 	}
 
	private ContentEntityObject getPage(Long id) {
		return (ContentEntityObject) pageManager.getById(id);
 	}
 
 	private Label getLabel(String label) {
 		return labelManager.getLabel(label);
 	}
 
	private static final class Mapper implements
			Function<ContentEntityObject, Document> {
 
 		private final boolean skippContent;
 
 		public Mapper() {
 			this.skippContent = false;
 		}
 
 		public Mapper(boolean skippContent) {
 			this.skippContent = skippContent;
 		}
 
 		@Override
		public Document get(ContentEntityObject from) {
			Document document = null;
			if (from instanceof Page) {
				// TODO space key was replaced with null cause LazyLoading
				// exception do to servlet forward
				document = new Document(from.getId(), null, from.getTitle(),
						from.getLastModificationDate(), from
								.getLastModifierName(), skippContent ? null
								: from.getContent());
			}
			return document;
 		}
 	}
 }
