 package org.mule.galaxy.atom;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.abdera.i18n.iri.IRI;
 import org.apache.abdera.model.Content;
 import org.apache.abdera.model.Person;
 import org.apache.abdera.protocol.server.RequestContext;
 import org.apache.abdera.protocol.server.RequestContext.Scope;
 import org.apache.abdera.protocol.server.context.ResponseContextException;
 import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
 import org.mule.galaxy.Comment;
 import org.mule.galaxy.CommentManager;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.Registry;
 
 public class CommentCollectionProvider extends AbstractEntityCollectionAdapter<Comment> {
     
     private CommentManager commentManager;
     private Registry registry;
     
     public CommentCollectionProvider(CommentManager commentManager, Registry registry) {
         this.commentManager = commentManager;
         this.registry = registry;
     }
     @Override
     public String getHref(RequestContext request) {
         String href = (String) request.getAttribute(Scope.REQUEST, ArtifactResolver.COLLECTION_HREF);
         if (href == null) {
             href = request.getTargetBasePath() + "/comments";
         }
         return href;
     }
     @Override
     public Comment postEntry(String arg0, IRI arg1, String arg2, Date arg3, List<Person> arg4,
                                Content arg5, RequestContext request) throws ResponseContextException {
         throw new ResponseContextException(501);
     }
 
     @Override
     public void deleteEntry(String arg0, RequestContext request) throws ResponseContextException {
         throw new ResponseContextException(501);
     }
 
     @Override
    public String getAuthor() throws ResponseContextException {
         return "Mule Galaxy";
     }
 
     @Override
     public Object getContent(Comment c, RequestContext request) throws ResponseContextException {
         return c.getText();
     }
 
     @Override
     public Iterable<Comment> getEntries(RequestContext request) throws ResponseContextException {
         return commentManager.getRecentComments(100);
     }
 
     @Override
     public Comment getEntry(String name, RequestContext request) throws ResponseContextException {
         try {
             return commentManager.getComment(name);
         } catch (NotFoundException e) {
             throw new ResponseContextException(404);
         }
     }
 
     @Override
     public String getId(RequestContext request) {
         return "tag:galaxy.mulesource.com,2008:registry:" + registry.getUUID() + ":comments:feed";
     }
 
     @Override
     public String getId(Comment c) throws ResponseContextException {
         return "urn:galaxy:comment:" + c.getId();
     }
 
     @Override
     public String getName(Comment c) throws ResponseContextException {
         return c.getId();
     }
 
     @Override
     public String getTitle(Comment c) throws ResponseContextException {
         return "Comment on " + c.getArtifact().getPath() + " by " + c.getUser().getName();
     }
 
     public String getTitle(RequestContext request) {
         return "Mule Galaxy Comments";
     }
 
     @Override
     public Date getUpdated(Comment c) throws ResponseContextException {
         return c.getDate().getTime();
     }
 
     @Override
     public void putEntry(Comment c, String arg1, Date arg2, List<Person> arg3, String arg4,
                          Content arg5, RequestContext arg6) throws ResponseContextException {
         throw new ResponseContextException(501);
     }
 
 }
