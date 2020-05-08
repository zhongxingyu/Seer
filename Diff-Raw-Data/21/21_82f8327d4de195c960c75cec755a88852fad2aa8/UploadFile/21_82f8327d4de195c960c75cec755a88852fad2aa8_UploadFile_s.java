 package net.cyklotron.cms.modules.actions.files;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.upload.FileUpload;
 import org.objectledge.upload.UploadContainer;
 import org.objectledge.upload.UploadLimitExceededException;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileAlreadyExistsException;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Upload file action.
  * 
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: UploadFile.java,v 1.7 2007-12-27 16:01:20 rafal Exp $
  */
 public class UploadFile
     extends BaseFilesAction
 {
     private FileUpload uploadService;
 
     
     
     public UploadFile(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, FilesService filesService,
         FileUpload fileUpload)
     {
         super(logger, structureService, cmsDataFactory, filesService);
         uploadService = fileUpload;
     }
 
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         boolean unpackZip = parameters.getBoolean("unpack", false);
        String description = parameters.get("description","");
         long dirId = parameters.getLong("dir_id", -1);
         if(dirId == -1)
         {
             templatingContext.put("result","parameter_not_found");
             return;
         }
         UploadContainer item;
         try
         {
             item = uploadService.getContainer("item1");
         }
         catch(UploadLimitExceededException e)
         {
             templatingContext.put("result", "file_size_exceeded");
             return;
         }
         if(item == null)
         {
             templatingContext.put("result","not_uploaded_correctly");
             return;
         }
 
         Subject subject = coralSession.getUserSubject();
         try
         {
             Resource parent = coralSession.getStore().getResource(dirId);
             if(!(parent instanceof DirectoryResource))
             {
                 templatingContext.put("result","invalid_directory");
                 return;
             }
 			if(!filesService.isValid(item.getFileName()))
 			{
 				templatingContext.put("result","invalid_file_name");
 				return;
 			}
             String encoding = parameters.get("encoding",httpContext.getEncoding());
             if(unpackZip && item.getFileName().endsWith(".zip"))
             {
                 filesService.unpackZipFile(coralSession, item.getInputStream(), encoding, (DirectoryResource)parent);
             }
             else
             {
                 FileResource file = filesService.createFile(coralSession, item.getFileName(), item.getInputStream(),
                                                            item.getMimeType(), encoding ,(DirectoryResource)parent);
                 file.setDescription(description);
                 I18nContext i18nContext = I18nContext.getI18nContext(context);
                 file.setLocale(parameters.get("locale",i18nContext.getLocale().toString()));
                 file.update();
                 templatingContext.put("file", file);
             }
         }
         catch(FileAlreadyExistsException e)
         {
             templatingContext.put("result","already_exists");
             return;
         }
         catch(Exception e)
         {
             logger.error("ARLException: ",e);
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             return;
         }
         templatingContext.put("result","uploaded_successfully");
     }
 
     public boolean checkAccessRights(Context context)
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         try
         {
             long dirId = parameters.getLong("dir_id", -1);
             if(dirId == -1)
             {
                 return true;
             }
             Resource parent = coralSession.getStore().getResource(dirId);
             Permission permission = coralSession.getSecurity().getUniquePermission("cms.files.write");
             return coralSession.getUserSubject().hasPermission(parent, permission);
         }
         catch(Exception e)
         {
             logger.error("Subject has no rights write in the directory", e);
             return false;
         }
     }
 
 }
 
