 package org.cloudlet.book.server;
 
 import org.cloudlet.core.server.Content;
 import org.cloudlet.core.server.CoreUtil;
 import org.cloudlet.core.server.Folder;
 import org.cloudlet.core.server.Repository;
 import org.cloudlet.core.server.Tag;
 
 import java.net.URISyntaxException;
 
 import javax.persistence.Entity;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 @XmlRootElement
 @XmlType
 @Entity(name = Books.TYPE_NAME)
 @Path(BookStore.BOOKS)
 @Produces("text/html;qs=5")
 public class Books extends Folder<Book> {
 
   public static final String TYPE_NAME = CoreUtil.PREFIX + "Books";
 
   @QueryParam("recommended")
   protected boolean featured;
 
   @QueryParam("promoted")
   protected boolean promoted;
 
   @QueryParam(Book.TAG)
   protected String tag;
 
   @Override
   public void addJoin(StringBuilder sql) {
     super.addJoin(sql);
     if (tag != null) {
       sql.append(" join e.tags t");
     }
   }
 
   @Override
   @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Book doCreate(Book book) {
     return super.doCreate(book);
   }
 
   @GET
   @Path(Content.EDIT)
   public Books editBook() {
     featured = true;
     doLoad();
     return this;
   }
 
   @GET
   @Path("/r")
   public Response getDefaultRank() {
     java.net.URI location = null;
     try {
       location = new java.net.URI("r/hot");
     } catch (URISyntaxException e) {
       e.printStackTrace();
     }
     return Response.status(Status.FOUND).location(location).build();
   }
 
   @GET
   @Path("/t")
   public Response getDefaultTag() {
     java.net.URI location = null;
     try {
       location = new java.net.URI("t/文学");
     } catch (URISyntaxException e) {
       e.printStackTrace();
     }
     return Response.status(Status.FOUND).location(location).build();
   }
 
   @Override
   public Class<Book> getEntryType() {
     return Book.class;
   }
 
   @Path("r/{rank}")
   public SalesBooks getRankedBooks(@PathParam("rank") String path) {
     BookRank rank = BookRank.getByPath(path);
     if (rank != null) {
       SalesBooks query = newChild(path, rank.type);
       query.setRank(rank);
       return query;
     }
     return null;
   }
 
   @Path("t/{tag}")
   public TaggedBooks getTaggedBooks(@PathParam("tag") String tagValue) {
     StringBuilder sql = new StringBuilder("from " + Tag.TYPE_NAME);
     sql.append(" t where t.value=:value and t.targetType=:targetType and t.parent=:parent");
     TypedQuery<Tag> query = em().createQuery(sql.toString(), Tag.class);
     query.setParameter("value", tagValue);
     query.setParameter("targetType", Book.TYPE_NAME);
     Repository repo = (Repository) getRoot();
     query.setParameter("parent", repo.getTags());
     Tag tag = query.getSingleResult();
     TaggedBooks result = newChild("t/" + tagValue, TaggedBooks.class);
     result.setTag(tag);
     return result;
   }
 
   @Override
   public String getType() {
     return TYPE_NAME;
   }
 
   public boolean isFeatured() {
     return featured;
   }
 
   public boolean isPromoted() {
     return promoted;
   }
 
   @Override
   public Book newEntry() {
     Book result = new Book();
     result.setParent(this);
     return result;
   }
 
   @Override
   public void prepareQuery(StringBuilder sql) {
     if (featured) {
       sql.append(" and e.featured=true");
     }
     if (promoted) {
       sql.append(" and e.promoted=true");
     }
     if (tag != null) {
       sql.append(" and t.id=:tag");
     }
   }
 
   public void setFeatured(boolean featured) {
     this.featured = featured;
   }
 
   @Override
   public void setParams(TypedQuery query) {
     super.setParams(query);
     if (tag != null) {
       query.setParameter("tag", tag);
     }
   }
 
   public void setPromoted(boolean promoted) {
     this.promoted = promoted;
   }
 
   @Override
   protected Book createFrom(MultivaluedMap<String, String> params) {
     return newEntry();
   }
 
 }
