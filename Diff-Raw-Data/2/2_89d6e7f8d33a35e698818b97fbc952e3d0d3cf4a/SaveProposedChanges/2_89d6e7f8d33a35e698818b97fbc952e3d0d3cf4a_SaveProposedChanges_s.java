 package net.cyklotron.cms.modules.actions.structure;
 
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.doc;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.dom4jToText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.elm;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.selectFirstText;
 import static net.cyklotron.cms.documents.DocumentMetadataHelper.textToDom4j;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.html.HTMLException;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.modules.views.documents.BaseSkinableDocumentScreen;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  * Propose new navigation node in document tree.
  * 
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @author <a href="mailo:mover@caltha.pl">Michal Mach</a>
  * @version $Id: ProposeDocument.java,v 1.22 2008-11-05 23:21:37 rafal Exp $
  */
 
 public class SaveProposedChanges
     extends BaseAddEditNodeAction
 {
     private CategoryService categoryService;
     
     private RelatedService relatedService;
 
     public SaveProposedChanges(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, CategoryService categoryService,
         FileUpload uploadService, FilesService filesService,
         CoralSessionFactory coralSessionFactory, RelatedService relatedService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.categoryService = categoryService;
         this.relatedService = relatedService;
     }
 
     /**
      * Performs the action.
      */
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         long docId = parameters.getLong("doc_id", -1L);
 
         try
         {
             if(docId != -1)
             {
                 DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                     coralSession, docId);
 
                 CmsData cmsData = cmsDataFactory.getCmsData(context);
                 ProposedDocumentData proposedData = new ProposedDocumentData();
                 ProposedDocumentData data = new ProposedDocumentData();
                 proposedData.fromProposal(node, coralSession);
                 Parameters screenConfig = cmsData.getEmbeddedScreenConfig(proposedData.getOrigin());
                 proposedData.setConfiguration(screenConfig);
                 
                 if(parameters.get("title", "").equals("accept"))
                 {
                     node.setTitle(proposedData.getTitle());
                 }
                 else if(parameters.get("title", "").equals("reject"))
                 {
                     proposedData.setTitle(node.getTitle());
                 }
                 if(parameters.get("abstract", "").equals("accept"))
                 {
                     node.setAbstract(proposedData.getAbstract());
                 }
                 else if(parameters.get("abstract", "").equals("reject"))
                 {
                     proposedData.setDocAbstract(node.getAbstract());
                 }
                 if(parameters.get("description", "").equals("accept"))
                 {
                     node.setDescription(proposedData.getDescription());
                 }
                 else if(parameters.get("description", "").equals("reject"))
                 {
                     proposedData.setDescription(node.getDescription());
                 }
                 if(parameters.get("content", "").equals("accept"))
                 {
                     node.setContent(proposedData.getContent());
                 }
                 else if(parameters.get("content", "").equals("reject"))
                 {
                     proposedData.setContent(node.getContent());
                 }
                 if(parameters.get("eventPlace", "").equals("accept"))
                 {
                     node.setEventPlace(proposedData.getEventPlace());
                 }
                 else if(parameters.get("eventPlace", "").equals("reject"))
                 {
                     proposedData.setEventPlace(node.getEventPlace());
                 }
                 if(parameters.get("eventStart", "").equals("accept"))
                 {
                     node.setEventStart(proposedData.getEventStart());
                 }
                 else if(parameters.get("eventStart", "").equals("reject"))
                 {
                     proposedData.setEventStart(node.getEventStart());
                 }
                 if(parameters.get("eventEnd", "").equals("accept"))
                 {
                     node.setEventEnd(proposedData.getEventEnd());
                 }
                 else if(parameters.get("eventEnd", "").equals("reject"))
                 {
                     proposedData.setEventEnd(node.getEventEnd());
                 }
                 if(parameters.get("validityStart", "").equals("accept"))
                 {
                     node.setValidityStart(proposedData.getValidityStart());
                 }
                 else if(parameters.get("validityStart", "").equals("reject"))
                 {
                     proposedData.setValidityStart(node.getValidityStart());
                 }
                 if(parameters.get("validityEnd", "").equals("accept"))
                 {
                     node.setValidityEnd(proposedData.getValidityEnd());
                 }
                 else if(parameters.get("validityEnd", "").equals("reject"))
                 {
                     proposedData.setValidityEnd(node.getValidityEnd());
                 }
 
                 Document metaDom = textToDom4j(node.getMeta());
 
                 String organizedBy = selectFirstText(metaDom, "/meta/organisation/name");
                 String organizedAddress = selectFirstText(metaDom, "/meta/organisation/address");
                 String organizedPhone = selectFirstText(metaDom, "/meta/organisation/tel");
                 String organizedFax = selectFirstText(metaDom, "/meta/organisation/fax");
                 String organizedEmail = selectFirstText(metaDom, "/meta/organisation/e-mail");
                 String organizedWww = selectFirstText(metaDom, "/meta/organisation/url");
                 String sourceName = selectFirstText(metaDom, "/meta/sources/source/name");
                 String sourceUrl = selectFirstText(metaDom, "/meta/sources/source/url");
                 String proposerCredentials = selectFirstText(metaDom, "/meta/authors/author/name");
                 String proposerEmail = selectFirstText(metaDom, "/meta/authors/author/e-mail");
                     
                 if(parameters.get("organizedBy", "").equals("accept"))
                 {
                     organizedBy = proposedData.getOrganizedBy();
                 }
                 else if(parameters.get("organizedBy", "").equals("reject"))
                 {
                     proposedData.setOrganizedBy(organizedBy);
                 }
                 if(parameters.get("organizedAddress", "").equals("accept"))
                 {
                     organizedAddress = proposedData.getOrganizedAddress();
                 }
                 else if(parameters.get("organizedAddress", "").equals("reject"))
                 {
                     proposedData.setOrganizedAddress(organizedAddress);
                 }
                 if(parameters.get("organizedPhone", "").equals("accept"))
                 {
                     organizedPhone = proposedData.getOrganizedPhone();
                 }
                 else if(parameters.get("organizedPhone", "").equals("reject"))
                 {
                     proposedData.setOrganizedPhone(organizedPhone);
                 }
                 if(parameters.get("organizedFax", "").equals("accept"))
                 {
                     organizedFax = proposedData.getOrganizedFax();
                 }
                 else if(parameters.get("organizedFax", "").equals("reject"))
                 {
                     proposedData.setOrganizedFax(organizedFax);
                 }
                 if(parameters.get("organizedEmail", "").equals("accept"))
                 {
                     organizedEmail = proposedData.getOrganizedEmail();
                 }
                 else if(parameters.get("organizedEmail", "").equals("reject"))
                 {
                     proposedData.setOrganizedEmail(organizedEmail);
                 }
                 if(parameters.get("organizedWww", "").equals("accept"))
                 {
                     organizedWww = proposedData.getOrganizedWww();
                 }
                 else if(parameters.get("organizedWww", "").equals("reject"))
                 {
                     proposedData.setOrganizedWww(organizedWww);
                 }
                 if(parameters.get("sourceName", "").equals("accept"))
                 {
                     sourceName = proposedData.getSourceName();
                 }
                 else if(parameters.get("sourceName", "").equals("reject"))
                 {
                     proposedData.setSourceName(sourceName);
                 }
                 if(parameters.get("sourceUrl", "").equals("accept"))
                 {
                     sourceUrl = proposedData.getSourceUrl();
                 }
                 else if(parameters.get("sourceUrl", "").equals("reject"))
                 {
                     proposedData.setSourceUrl(sourceUrl);
                 }
                 if(parameters.get("proposerCredentials", "").equals("accept"))
                 {
                     proposerCredentials = proposedData.getProposerCredentials();
                 }
                 else if(parameters.get("proposerCredentials", "").equals("reject"))
                 {
                     proposedData.setProposerCredentials(proposerCredentials);
                 }
                 if(parameters.get("proposerEmail", "").equals("accept"))
                 {
                     proposerEmail = proposedData.getProposerEmail();
                 }
                 else if(parameters.get("proposerEmail", "").equals("reject"))
                 {
                     proposedData.setProposerEmail(proposerEmail);
                 }
 
                 Element metaElm = elm("meta", elm("authors", elm("author", elm("name",
                     proposerCredentials), elm("e-mail", proposerEmail))), elm("sources", elm(
                     "source", elm("name", sourceName), elm("url", sourceUrl))), elm("editor"), elm(
                     "organisation", elm("name", organizedBy), elm("address", organizedAddress),
                     elm("tel", organizedPhone), elm("fax", organizedFax), elm("e-mail",
                         organizedEmail), elm("url", organizedWww), elm("id", "0")));
 
                 Document doc = doc(metaElm);
                 node.setMeta(dom4jToText(doc));
 
                 if(parameters.get("docCategories", "").equals("accept"))
                 {                       
                     Relation relation = categoryService.getResourcesRelation(coralSession);
                     RelationModification modification = new RelationModification();
                     
                     // take document node categories  
                     Set<CategoryResource> publishedDocCategories = new HashSet<CategoryResource>(
                         Arrays.asList(categoryService.getCategories(coralSession, node, false)));
                     // take proposed document categories
                     Set<CategoryResource> proposedDocCategories = proposedData.getSelectedCategories();
                     
                     // take component available root categories id 
                     long root_category_1 = screenConfig.getLong("category_id_1", -1);  
                     long root_category_2 = screenConfig.getLong("category_id_2", -1);
                     int categoryDepth = screenConfig.getInt("category_depth", 1);
                     List<CategoryResource> allAvailableCategories = new ArrayList<CategoryResource>();
                     BaseSkinableDocumentScreen.getCategoryList(root_category_1, categoryDepth,
                         true, coralSession, allAvailableCategories);
                     BaseSkinableDocumentScreen.getCategoryList(root_category_2, categoryDepth,
                         true, coralSession, allAvailableCategories);
                     
                     List<Resource> toRemove = new ArrayList<Resource>(allAvailableCategories);
                     List<Resource> toAdd = new ArrayList<Resource>(proposedDocCategories);
 
                     // remove proposed categories from available categories
                     toRemove.removeAll(proposedDocCategories);       
                     
                     // remove from proposed categories document node categories
                     toAdd.removeAll(publishedDocCategories);
                     
                     modification.add(toAdd, node);
                     modification.remove(toRemove, node);
 
                     // update categories
                     coralSession.getRelationManager().updateRelation(relation, modification);
                     
                 }
                 else if(parameters.get("docCategories", "").equals("reject"))
                 {
                     proposedData.setSelectedCategories(new HashSet<CategoryResource>(Arrays
                         .asList(categoryService.getCategories(coralSession, node, false))));
                 }
                 if(parameters.get("docAttachments", "").equals("accept"))
                 {
                    Relation relation = categoryService.getResourcesRelation(coralSession);
                     RelationModification modification = new RelationModification();
                     
                     List<Resource> publishedDocAttachments = new ArrayList<Resource>(Arrays
                         .asList(relatedService.getRelatedTo(coralSession, node, 
                             node.getRelatedResourcesSequence(), null)));
                     
                     List<Resource> proposedDocAttachments = proposedData.getAttachments();                   
                    
                     List<Resource> toRemove = new ArrayList<Resource>(publishedDocAttachments);
                     List<Resource> toAdd = new ArrayList<Resource>(proposedDocAttachments);
                     
                     toRemove.removeAll(proposedDocAttachments);
                     toAdd.removeAll(publishedDocAttachments);
                     
                     modification.add(node, toAdd);
                     modification.remove(node, toRemove);
 
                     coralSession.getRelationManager().updateRelation(relation, modification);
                 }
                 else if(parameters.get("docAttachments", "").equals("reject"))
                 {
                     proposedData.setAttachments(new ArrayList<Resource>(Arrays
                         .asList(relatedService.getRelatedTo(coralSession, node, node
                             .getRelatedResourcesSequence(), null))));
                 }
                 if(parameters.getBoolean("save_doc_proposal", false))
                 {
                     proposedData.toProposal(node);
                 }
                 else
                 {
                     node.setProposedContent(null);
                 }
                 node.update();
             }
 
         }
         catch(EntityDoesNotExistException e)
         {
             logger.error("excception", e);
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
         catch(ValueRequiredException e)
         {
             logger.error("excception", e);
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
         catch(HTMLException e)
         {
             logger.error("excception", e);
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
         }
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         try
         {
             Parameters requestParameters = context.getAttribute(RequestParameters.class);
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             Subject userSubject = coralSession.getUserSubject();
 
             long id = requestParameters.getLong("doc_id", -1);
             Resource node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession, id);
             Permission modifyPermission = coralSession.getSecurity().getUniquePermission(
                 "cms.structure.modify");
             Permission modifyOwnPermission = coralSession.getSecurity().getUniquePermission(
                 "cms.structure.modify_own");
             if(userSubject.hasPermission(node, modifyPermission))
             {
                 return true;
             }
             if(node.getOwner().equals(userSubject)
                 && userSubject.hasPermission(node, modifyOwnPermission))
             {
                 return true;
             }
             return false;
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured during access rights checking ", e);
         }
     }
 
     /**
      * @{inheritDoc
      */
     @Override
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return false;
     }
 
     @Override
     protected String getViewName()
     {
         return null;
     }
 }
