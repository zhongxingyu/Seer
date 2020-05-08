 package net.cyklotron.cms.modules.actions.structure;
 
 import static net.cyklotron.cms.structure.internal.ProposedDocumentData.getAttachmentName;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 public class UpdateProposedDocument
     extends BaseStructureAction
 {
     private final FileUpload fileUpload;
 
     private final FilesService filesService;
 
     private final RelatedService relatedService;
 
     public UpdateProposedDocument(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, FileUpload fileUpload,
         FilesService filesService, RelatedService relatedService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.fileUpload = fileUpload;
         this.filesService = filesService;
         this.relatedService = relatedService;
     }
 
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         boolean valid = true;
         try
         {
             long docId = parameters.getLong("doc_id");
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
 
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             ProposedDocumentData data = new ProposedDocumentData(screenConfig);
             data.fromParameters(parameters, coralSession);
 
             // check required parameters
             if(!data.isValid())
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
 
             // file upload - checking
             if(valid && !data.isFileUploadValid(coralSession, fileUpload))
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
 
             if(valid && data.isAttachmentsEnabled())
             {
                 DirectoryResource dir = data.getAttachmenDirectory(coralSession);
                 for(int index = data.getAttachments().size(); index < data
                     .getAttachmentsMaxCount(); index++)
                 {
                     String description = data.getAttachmentDescription(index);
                     UploadContainer container = data.getAttachmentContainer(index, fileUpload);
                     if(container != null)
                     {
                         FileResource file = filesService.createFile(coralSession,
                             getAttachmentName(container.getFileName()), container.getInputStream(),
                             container.getMimeType(), null, dir);
                         file.setDescription(description);
                         file.update();
                         data.addAttachment(file);
                     }
                 }
 
                 Set<Resource> publishedAttachments = new HashSet<Resource>(Arrays
                     .asList(relatedService.getRelatedTo(coralSession, node, node
                         .getRelatedResourcesSequence(), null)));
                 for(long id : parameters.getLongs("remove_attachment"))
                 {
                     FileResource file = data.removeAttachment(id, coralSession);
                     if(!publishedAttachments.contains(file))
                     {
                         filesService.deleteFile(coralSession, file);
                     }
                 }
             }
             
             if(valid)
             {
                 data.toProposal(node);
                 node.update();
             }
         }
         catch(Exception e)
         {
             logger.error("excception", e);
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e));
             valid = false;
         }
         if(valid)
         {
             parameters.set("state", "Result");
            templatingContext.put("result", "added_successfully");
         }
         else
         {
             parameters.set("state", "EditDocument");
         }
     }
     
     @Override
     public boolean requiresAuthenticatedUser(Context context)
         throws Exception
     {
         return true;
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws Exception
     {
         Parameters requestParameters = context.getAttribute(RequestParameters.class);
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         Subject userSubject = coralSession.getUserSubject();
 
         long id = requestParameters.getLong("doc_id", -1);
         Resource node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,
             id);
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
 }
