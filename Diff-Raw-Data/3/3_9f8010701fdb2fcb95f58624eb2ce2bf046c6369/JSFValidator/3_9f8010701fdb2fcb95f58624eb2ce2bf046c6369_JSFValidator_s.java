 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.ui.internal.validation;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jst.jsf.common.internal.JSPUtil;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigUtils;
 import org.eclipse.jst.jsf.validation.internal.IJSFViewValidator;
 import org.eclipse.jst.jsf.validation.internal.JSFValidatorFactory;
 import org.eclipse.jst.jsf.validation.internal.ValidationPreferences;
 import org.eclipse.jst.jsp.core.internal.validation.JSPValidator;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 
 /**
  * @author cbateman
  *
  */
 public class JSFValidator extends JSPValidator implements ISourceValidator
 {
     // TODO: should the source validator be a separate class in jsp.ui?
     // problem with simple split off is that preference must also be split off
     static final boolean DEBUG;
     static
     {
         final String value = Platform
         .getDebugOption("org.eclipse.jst.jsf.ui/validation"); //$NON-NLS-1$
         DEBUG = value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
     }
 
     private IDocument    fDocument;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#connect(org.eclipse.jface.text.IDocument)
      */
     public void connect(final IDocument document)
     {
         fDocument = document;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#disconnect(org.eclipse.jface.text.IDocument)
      */
     public void disconnect(final IDocument document)
     {
         // finished
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#validate(org.eclipse.jface.text.IRegion,
      *      org.eclipse.wst.validation.internal.provisional.core.IValidationContext,
      *      org.eclipse.wst.validation.internal.provisional.core.IReporter)
      */
     public void validate(final IRegion dirtyRegion,
             final IValidationContext helper, final IReporter reporter)
     {
         if (DEBUG)
         {
             System.out.println("exec JSPSemanticsValidator.validateRegion"); //$NON-NLS-1$
         }
 
         final IFile file = getFile(helper);
 
        if (fDocument instanceof IStructuredDocument)
         {
             final IStructuredDocument sDoc = (IStructuredDocument) fDocument;
             final IStructuredDocumentRegion[] regions = sDoc
             .getStructuredDocumentRegions(dirtyRegion.getOffset(),
                     dirtyRegion.getLength());
             if (regions != null)
             {
                 final IJSFViewValidator validator = JSFValidatorFactory
                         .createDefaultXMLValidator();
                 final ValidationPreferences prefs = new ValidationPreferences(
                         JSFCorePlugin.getDefault().getPreferenceStore());
                 prefs.load();
 
                 IStructuredModel model = null;
                 try
                 {
                     model = StructuredModelManager.getModelManager().getModelForRead(
                             file);
 
                     final ValidationReporter jsfReporter = new ValidationReporter(
                             this, reporter, file, prefs, model);
                     validator.validateView(file, regions, jsfReporter);
                 }
                 catch (final CoreException e)
                 {
                     JSFCorePlugin.log("Error validating JSF", e); //$NON-NLS-1$
                 }
                 catch (final IOException e)
                 {
                     JSFCorePlugin.log("Error validating JSF", e); //$NON-NLS-1$
                 }
                 finally
                 {
                     if (null != model)
                     {
                         model.releaseFromRead();
                     }
                 }
             }
         }
     }
 
     private IFile getFile(final IValidationContext helper)
     {
         final String[] uris = helper.getURIs();
         final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
         if (uris.length > 0)
         {
             return wsRoot.getFile(new Path(uris[0]));
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jst.jsp.core.internal.validation.JSPValidator#validateFile(org.eclipse.core.resources.IFile,
      *      org.eclipse.wst.validation.internal.provisional.core.IReporter)
      */
     @Override
     protected void validateFile(final IFile file, final IReporter reporter)
     {
         if (shouldValidate(file))
         {
              final IJSFViewValidator validator = JSFValidatorFactory
                     .createDefaultXMLValidator();
             final ValidationPreferences prefs = new ValidationPreferences(
                     JSFCorePlugin.getDefault().getPreferenceStore());
             prefs.load();
     
             IStructuredModel model = null;
             try
             {
                 model = StructuredModelManager.getModelManager().getModelForRead(
                         file);
 
                 final ValidationReporter jsfReporter = new ValidationReporter(this,
                         reporter, file, prefs, model);
                 validator.validateView(file, jsfReporter);
             }
             catch (final CoreException e)
             {
                 JSFCorePlugin.log("Error validating JSF", e); //$NON-NLS-1$
             }
             catch (final IOException e)
             {
                 JSFCorePlugin.log("Error validating JSF", e); //$NON-NLS-1$
             }
             finally
             {
                 if (null != model)
                 {
                     model.releaseFromRead();
                 }
             }
         }
     }
 
     private boolean shouldValidate(final IFile file)
     {
         return (JSPUtil.isJSPContentType(file)
                 && JSFAppConfigUtils.isValidJSFProject(file.getProject()));
     }
 }
