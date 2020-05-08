 package net.cyklotron.cms.modules.actions.documents;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import pl.caltha.forms.FormsException;
 import pl.caltha.forms.FormsService;
 import pl.caltha.forms.Instance;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentService;
 import net.cyklotron.cms.modules.actions.structure.BaseStructureAction;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  *
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: BaseDocumentAction.java,v 1.3 2005-03-23 14:22:41 zwierzem Exp $
  */
 public abstract class BaseDocumentAction
     extends BaseStructureAction
 {
     /** Form-tool service. */
     protected  FormsService formService;
 
     /** Document service. */
     protected  DocumentService documentService;
 
     
     public BaseDocumentAction(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, StyleService styleService,
         FormsService formsService, DocumentService documentService)
     {
         super(logger, structureService, cmsDataFactory, styleService);
         this.formService = formsService;
         this.documentService = documentService;
     }
 
     protected Instance getInstance(HttpContext httpContext)
         throws ProcessingException
     {
         try
         {
             return formService.getInstance(DocumentService.FORM_NAME, httpContext);
         }
         catch(FormsException e)
         {
             throw new ProcessingException("Cannot get a form instance", e);
         }
     }
 
     public DocumentNodeResource getDocument(Context context)
         throws ProcessingException
     {
         NavigationNodeResource node = getNode(context);
         if(node instanceof DocumentNodeResource)
         {
             return (DocumentNodeResource)node;
         }
         else
         {
             throw new ProcessingException("Current node is not a document node");
         }
     }
 
     public void restoreView(HttpContext httpContext, MVCContext mvcContext, Parameters parameters)
         throws ProcessingException
     {
        String viewName = "structure.NaviInfo";
         // WARN: ugly hacking
         if(httpContext.getSessionAttribute("document_edit_return_view") != null)
         {
             viewName = (String)httpContext.getSessionAttribute("document_edit_return_view");
         }
         viewName = parameters.get("target_view",viewName);
         mvcContext.setView(viewName);
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         return getCmsData(context).getNode().canModify(context, coralSession.getUserSubject());
     }
 }
