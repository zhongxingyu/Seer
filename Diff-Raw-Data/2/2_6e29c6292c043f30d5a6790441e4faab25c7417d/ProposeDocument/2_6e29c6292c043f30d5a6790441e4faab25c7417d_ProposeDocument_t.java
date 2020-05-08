 package net.cyklotron.cms.modules.actions.structure;
 
 import static net.cyklotron.cms.structure.internal.ProposedDocumentData.getAttachmentName;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.AuthenticationException;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.datatypes.ResourceList;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.html.HTMLService;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.captcha.CaptchaService;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.structure.NavigationNodeAlreadyExistException;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureException;
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
 
 public class ProposeDocument
     extends BaseAddEditNodeAction
 {
     private CategoryService categoryService;
 
     private final FileUpload uploadService;
 
     private final FilesService filesService;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private final RelatedService relatedService;
 
     private final HTMLService htmlService;
     
     private final CaptchaService captchaService;
     
     private final UserManager userManager;
 
     public ProposeDocument(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService, CategoryService categoryService,
         FileUpload uploadService, FilesService filesService,
         CoralSessionFactory coralSessionFactory, RelatedService relatedService,
         HTMLService htmlService, CaptchaService captchaService, UserManager userManager)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.categoryService = categoryService;
         this.uploadService = uploadService;
         this.filesService = filesService;
         this.coralSessionFactory = coralSessionFactory;
         this.relatedService = relatedService;
         this.htmlService = htmlService;
         this.captchaService = captchaService;
         this.userManager = userManager;
     }
 
     /**
      * Performs the action.
      */
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         // basic setup
 
         Subject subject = coralSession.getUserSubject();
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
         DocumentNodeResource node = null;
         boolean valid = true;
         CategoryResource[] parentCategories = null;
         
         try
         {
             // get parameters
             ProposedDocumentData data = new ProposedDocumentData(screenConfig,logger);
             data.fromParameters(parameters, coralSession);
 
             // check required parameters
             if(!data.isValid(htmlService))
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
         
             // file upload - checking
             if(valid && !data.isFileUploadValid(coralSession, uploadService))
             {
                 valid = false;
                 templatingContext.put("result", data.getValidationFailure());
             }
             
             if(valid && screenConfig.getBoolean("add_captcha", false))
             {
                 if(!captchaService.checkCaptcha(httpContext, (RequestParameters)parameters))
                 {
                     templatingContext.put("result", "invalid_captcha_verification");
                     valid = false;                    
                 }
             }
 
             // find parent node
             NavigationNodeResource parent = null;
             if(valid)
             {
                 long parentId = screenConfig.getLong("parent_id", -1L);
                 if(parentId != -1L)
                 {
                     try
                     {
                         parent = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession,
                             parentId);
                     }
                     catch(EntityDoesNotExistException e)
                     {
                         templatingContext.put("result", "parent_misconfigured");
                         valid = false;
                     }
                 }
                 else
                 {
                     // when no parent is selected in component config, add new node as child of the node where proposal form resides
                     parent = cmsData.getNode();
                 }
             }
                 
             if(valid)
             {    
                 parentCategories = categoryService.getCategories(coralSession, parent, false);
                 CoralSession rootSession = coralSessionFactory.getRootSession();
 
                 try
                 {
                     Subject rootSubject = rootSession.getSecurity().getSubject(Subject.ROOT);
                     if(data.isCalendarTree() && data.getValidityStart() != null)
                     {
                         parent = structureService.getParent(rootSession, parent, data
                             .getValidityStart(), StructureService.DAILY_CALENDAR_TREE_STRUCTURE,
                             rootSubject);
                     }
                 }
                 finally
                 {
                     rootSession.close();
                 }
 
                 try
                 {
                     // add navigation node
                     node = structureService.addDocumentNode(coralSession, data.getName(), data.getTitle(),
                         parent, subject);
                 }
                 catch(NavigationNodeAlreadyExistException e)
                 {
                     templatingContext.put("result", "navi_name_repeated");
                     valid = false;
                 }
             }
 
             if(valid)
             {
                 data.toNode(node);
                 node.setSequence(getMaxSequence(coralSession, parent));
                 assignCategories(data, coralSession, node, parentCategories);
                 uploadAndAttachFiles(node, data, coralSession);        
                 setState(coralSession, subject, node);
                 setOwner(node, context);
                 structureService.updateNode(coralSession, node, data.getName(), true, subject);
                 
                 data.logProposal(logger, node);
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             logger.error("StructureException: ", e);
             templatingContext.put("trace", new StackTrace(e));
             valid = false;
         }
         if(valid)
         {
             templatingContext.put("result", "added_successfully");
         }
         else
         {
             parameters.set("state", "AddDocument");
         }
     }
 
     private void uploadAndAttachFiles(DocumentNodeResource node, ProposedDocumentData data,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             if(data.isAttachmentsEnabled())
             {
                 ResourceList<FileResource> attachments = new ResourceList<FileResource>(coralSessionFactory);
                 DirectoryResource dir = data.getAttachmenDirectory(coralSession);
                 for (int i = 0; i < data.getAttachmentsMaxCount(); i++)
                 {
                     UploadContainer file = data.getAttachmentContainer(i, uploadService);
                     if(file != null)
                     {
                         String description = data.getAttachmentDescription(i);
                         FileResource f = filesService.createFile(coralSession,
                             getAttachmentName(file.getFileName()), file.getInputStream(), file
                                 .getMimeType(), null, dir);
                         f.setDescription(description);
                         f.update();
                         attachments.add(f);
                     }
                 }
                 relatedService.setRelatedTo(coralSession, node, attachments.toArray(new Resource[attachments.size()])); 
                 node.setRelatedResourcesSequence(attachments);
                 node.update();
             }
         }
         catch(Exception e)
         {
             throw new ProcessingException("problem while processing attachments", e);
         }
     }
 
     private void setState(CoralSession coralSession, Subject subject, DocumentNodeResource node)
         throws StructureException
     {
         // set the state to taken if user has redactor role
         Role role = findRedactor(coralSession, node);
         if(role != null && subject.hasRole(role))
         {
             structureService.enterState(coralSession, node, "taken", subject);
         }
         else
         {
             structureService.enterState(coralSession, node, "new", subject);
         }
     }
 
     private void assignCategories(ProposedDocumentData data, CoralSession coralSession,
         DocumentNodeResource node, CategoryResource[] parentCategories)
         throws EntityDoesNotExistException
     {
         if(data.isInheritCategories() || data.getSelectedCategories().size() > 0)
         {
             Relation refs = categoryService.getResourcesRelation(coralSession);
             RelationModification diff = new RelationModification();
             Permission classifyPermission = coralSession.getSecurity().getUniquePermission(
                 "cms.category.classify");
             if(data.isInheritCategories() && parentCategories != null)
             {                
                 for (CategoryResource category : parentCategories)
                 {
                     if(coralSession.getUserSubject().hasPermission(category, classifyPermission))
                     {
                         diff.add(category, node);
                     }
                 }
             }
             for (CategoryResource categoryResource : data.getSelectedCategories())
             {
                 if(coralSession.getUserSubject().hasPermission(categoryResource, classifyPermission))
                 {
                     diff.add(categoryResource, node);
                 }
             }
             coralSession.getRelationManager().updateRelation(refs, diff);
         }
     }
 
     private void setOwner(NavigationNodeResource node,
         Context context)
     throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
         
         String ownerLogin = screenConfig.get("owner_login", "");
         CoralSession rootSession = coralSessionFactory.getRootSession();        
         try
         {
            if(ownerLogin.length() > 0)
             {
                 String dn = userManager.getUserByLogin(ownerLogin).getName();
                 Subject owner = rootSession.getSecurity().getSubject(dn);
                 Permission modifyOwnPermission = rootSession.getSecurity().getUniquePermission(
                     "cms.structure.modify_own");
                 if(owner != null && owner.hasPermission(node, modifyOwnPermission))
                 {
                     structureService.enterState(rootSession, node, "taken", owner);
                     rootSession.getStore().setOwner(node, owner);
                 }
             }
         }
         catch(EntityDoesNotExistException e)
         {
             throw new ProcessingException("Subject " + ownerLogin
                 + "  not found. Repair application configuration.", e);
         }
         catch(AuthenticationException e)
         {
             throw new ProcessingException("Subject " + ownerLogin
                 + "  not found. Repair application configuration.", e);
         }
         catch(StructureException e)
         {
             throw new ProcessingException("Error while entering state.", e);
         }
         finally
         {
             rootSession.close();
         }
     }
     
     private Role findRedactor(CoralSession coralSession, NavigationNodeResource node)
     {
         while(node != null)
         {
             Role role = node.getRedactor();
             if(role != null)
             {
                 return role;
             }
             Resource parent = node.getParent();
             node = parent instanceof NavigationNodeResource ? (NavigationNodeResource)parent : null;
         }
         return null;
     }
 
     private int getMaxSequence(CoralSession coralSession, NavigationNodeResource parent)
     {
         // get greatest sequence number to put new node on top of
         // sequence-sorted list
         int sequence = 0;
         Resource[] children = coralSession.getStore().getResource(parent);
         for (int i = 0; i < children.length; i++)
         {
             Resource child = children[i];
             if(child instanceof NavigationNodeResource)
             {
                 int childSeq = ((NavigationNodeResource)child).getSequence(0);
                 sequence = sequence < childSeq ? childSeq : sequence;
             }
         }
         return sequence;
     }
 
     @Override
     protected String getViewName()
     {
         return "";
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         try
         {
             Permission permission = coralSession.getSecurity().getUniquePermission(
                 "cms.structure.submit");
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig();
             long parentId = screenConfig.getLong("parent_id", -1L);
             Resource parent = parentId != -1L ? NavigationNodeResourceImpl
                 .getNavigationNodeResource(coralSession, parentId) : cmsData.getNode();
             return coralSession.getUserSubject().hasPermission(parent, permission);
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
 }
