 package net.cyklotron.cms.modules.actions.files;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Permission;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.DirectoryResource;
 import net.cyklotron.cms.files.FileAlreadyExistsException;
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * Create the directory action.
  * 
  * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: CreateDirectory.java,v 1.4 2005-03-08 10:51:58 pablo Exp $
  */
 public class CreateDirectory
     extends BaseFilesAction
 {
     
     
     public CreateDirectory(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, FilesService filesService)
     {
         super(logger, structureService, cmsDataFactory, filesService);
         
     }
     /**
      * Performs the action.
      */
     public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         String name = parameters.get("name","");
         if(name.equals(""))
         {
             templatingContext.put("result","directory_name_empty");
             return;
         }
 		if(!filesService.isValid(name))
 		{
 			templatingContext.put("result","invalid_directory_name");
 			return;
 		}
 
        String description = parameters.get("directory_description","");
         long dirId = parameters.getLong("dir_id", -1);
         if(dirId == -1)
         {
             templatingContext.put("result","parameter_not_found");
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
             DirectoryResource directory = filesService.
                 createDirectory(coralSession, name, (DirectoryResource)parent);
             directory.setDescription(description);
             directory.update();
         }
         catch(FileAlreadyExistsException e)
         {
             templatingContext.put("result","already_exists");
             return;
         }
         catch(EntityDoesNotExistException e)
         {
             logger.error("ARLException: ",e);
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             return;
         }
         catch(FilesException e)
         {
             logger.error("FilesException: ",e);
             templatingContext.put("result","exception");
             templatingContext.put("trace",new StackTrace(e));
             return;
         }
         templatingContext.put("result","created_successfully");
        mvcContext.setView(parameters.get("target_view", mvcContext.getView()));
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
