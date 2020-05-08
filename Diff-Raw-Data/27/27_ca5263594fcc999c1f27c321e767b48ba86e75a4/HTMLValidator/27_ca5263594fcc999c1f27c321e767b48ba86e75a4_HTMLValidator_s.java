 package org.eclipse.jst.jsf.facelet.ui.internal.validation;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.core.runtime.content.IContentTypeManager;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.Namespace;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IDOMContextResolver;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.internal.view.model.ITagRegistry;
 import org.eclipse.jst.jsf.facelet.core.internal.util.ViewUtil;
 import org.eclipse.jst.jsf.facelet.ui.internal.FaceletUiPlugin;
 import org.eclipse.jst.jsf.validation.internal.IJSFViewValidator;
 import org.eclipse.jst.jsf.validation.internal.JSFValidatorFactory;
 import org.eclipse.jst.jsf.validation.internal.ValidationPreferences;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * The Facelet HTML file validator.
  * 
  * @author cbateman
  *
  */
public class HTMLValidator implements IValidatorJob
 {
     public ISchedulingRule getSchedulingRule(final IValidationContext helper)
     {
         // no rule...
         return null;
     }
 
     public IStatus validateInJob(final IValidationContext helper,
             final IReporter reporter) throws ValidationException
     {
         IStatus status = Status.OK_STATUS;
         try
         {
             validate(helper, reporter);
         }
         catch (final ValidationException e)
         {
             status = new Status(IStatus.ERROR, FaceletUiPlugin.PLUGIN_ID,
                     IStatus.ERROR, e.getLocalizedMessage(), e);
         }
         return status;
 
     }
 
     public void cleanup(final IReporter reporter)
     {
         // do nothing
     }
 
     public void validate(final IValidationContext helper,
             final IReporter reporter) throws ValidationException
     {
         final String[] uris = helper.getURIs();
         final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
         if (uris.length > 0)
         {
             IFile currentFile = null;
 
             for (int i = 0; i < uris.length && !reporter.isCancelled(); i++)
             {
                 currentFile = wsRoot.getFile(new Path(uris[i]));
                 if (currentFile != null && currentFile.exists())
                 {
                     if (shouldValidate(currentFile))
                     {
                         final int percent = (i * 100) / uris.length + 1;
                         final IMessage message = new LocalizedMessage(
                                 IMessage.LOW_SEVERITY, percent + "% " + uris[i]);
                         reporter.displaySubtask(this, message);
 
                         validateFile(currentFile, reporter);
                     }
                 }
             }
         }
 
     }
 
     private void validateFile(final IFile file, final IReporter reporter)
     {
         final IJSFViewValidator validator = JSFValidatorFactory
                 .createDefaultXMLValidator();
         final ValidationPreferences prefs = new ValidationPreferences(
                 JSFCorePlugin.getDefault().getPreferenceStore());
         prefs.load();
 
         final ValidationReporter jsfReporter = new ValidationReporter(this,
                 reporter, file, prefs);
         validator.validateView(file, jsfReporter);
         // TODO: break off into composite strategies
         validateFaceletHtml(file, jsfReporter);
     }
 
     private void validateFaceletHtml(final IFile file,
             final ValidationReporter reporter)
     {
         IStructuredModel model = null;
         try
         {
             model = StructuredModelManager.getModelManager().getModelForRead(
                     file);
 
             final IStructuredDocument structuredDoc = model
                     .getStructuredDocument();
 
             validateDocument(structuredDoc, reporter, file.getProject());
         }
         catch (final CoreException e)
         {
             JSFCorePlugin.log("Error validating JSF", e);
         }
         catch (final IOException e)
         {
             JSFCorePlugin.log("Error validating JSF", e);
         }
         finally
         {
             if (null != model)
             {
                 model.releaseFromRead();
             }
         }
     }
 
     private void validateDocument(IStructuredDocument structuredDoc,
             final ValidationReporter reporter, IProject project)
     {
         validateRoot(structuredDoc, reporter, project);
     }
 
     private void validateRoot(IStructuredDocument structuredDoc,
             ValidationReporter reporter, IProject project)
     {
         final IStructuredDocumentContext context = IStructuredDocumentContextFactory.INSTANCE
                 .getContext(structuredDoc, -1);
         final IDOMContextResolver resolver = IStructuredDocumentContextResolverFactory.INSTANCE
                 .getDOMContextResolver(context);
         final Document document = resolver.getDOMDocument();
         Element rootElement = document.getDocumentElement();
 
         if ("html".equals(rootElement.getNodeName()))
         {
             final Set<Attr> declaredNamespaces = ViewUtil
                     .getDeclaredNamespaces(rootElement.getAttributes());
             final ITagRegistry tagRegistry = ViewUtil
                     .getHtmlTagRegistry(project);
             final Collection<? extends Namespace> namespaces = tagRegistry
                     .getAllTagLibraries();
             for (final Attr attr : declaredNamespaces)
             {
                 // only validate prefix declarations
                 if (attr.getPrefix() != null && attr instanceof IDOMAttr)
                 {
                     final String declaredUri = attr.getValue();
                     String findUri = null;
                     SEARCH_NAMESPACES: for (final Namespace ns : namespaces)
                     {
                         if (ns.getNSUri().equals(declaredUri))
                         {
                             findUri = ns.getNSUri();
                             break SEARCH_NAMESPACES;
                         }
                     }
 
                     if (findUri == null)
                     {
                         // XXX: Add resource locator call to find composite components,
                         // per the glassfish changes.
                         // TODO: need factory
                         final Diagnostic diag = new BasicDiagnostic(
                                 Diagnostic.WARNING, "", -1,
                                 "Can't find facelet tag library for uri "
                                         + declaredUri, null);
                         final IDOMAttr domAttr = (IDOMAttr) attr;
                         reporter.report(diag, domAttr.getValueRegionStartOffset(), domAttr
                                 .getValue().length());
                     }
                 }
             }
         }
     }
     
     private boolean shouldValidate(final IFile model)
     {
         final IContentTypeManager manager = Platform.getContentTypeManager();
         final IContentType contentType = manager
                 .getContentType("org.eclipse.wst.html.core.htmlsource");
         return (contentType.isAssociatedWith(model.getName()))
                 && ViewUtil.isFaceletVDLFile(model);
     }
 }
