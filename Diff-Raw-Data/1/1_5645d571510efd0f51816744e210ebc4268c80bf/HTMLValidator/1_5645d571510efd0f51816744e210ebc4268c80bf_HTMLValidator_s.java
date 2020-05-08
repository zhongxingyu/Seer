 package org.eclipse.jst.jsf.facelet.ui.internal.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.jst.jsf.designtime.internal.BasicExtensionFactory.ExtensionData;
 import org.eclipse.jst.jsf.facelet.ui.internal.FaceletUiPlugin;
 import org.eclipse.wst.validation.AbstractValidator;
 import org.eclipse.wst.validation.ValidationResult;
 import org.eclipse.wst.validation.ValidationState;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.eclipse.wst.validation.internal.provisional.core.IValidator;
 
 /**
  * The Facelet HTML file validator.
  * 
  * @author cbateman
  * 
  */
 public class HTMLValidator extends AbstractValidator implements IValidator {
     /**
      * @param helper
      * @return no rule, null
      */
     public ISchedulingRule getSchedulingRule(final IValidationContext helper) {
         // no rule...
         return null;
     }
 
     public void cleanup(final IReporter reporter) {
         // do nothing
     }
 
     @Override
     public ValidationResult validate(IResource resource, int kind, ValidationState state, IProgressMonitor monitor) {
         final ValidationResult vr = new ValidationResult();
         if (resource == null || !(resource instanceof IFile)) {
             return vr;
         }
         IFile currentFile = (IFile) resource;
         List<AbstractFaceletValidationStrategy> validationStrategies = getStrategies(currentFile.getProject());
         for (AbstractFaceletValidationStrategy strategy : validationStrategies) {
             if (strategy.shouldValidate(currentFile)) {
                 strategy.validateFile(currentFile, vr.getReporter(monitor), this);
             }
         }
         return vr;
     }
 
     public void validate(final IValidationContext helper, final IReporter reporter) throws ValidationException {
         final String[] uris = helper.getURIs();
         final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
         if (uris.length > 0) {
             IFile currentFile = null;
 
             for (int i = 0; i < uris.length && !reporter.isCancelled(); i++) {
                 currentFile = wsRoot.getFile(new Path(uris[i]));
                 if (currentFile != null && currentFile.exists()) {
                     List<AbstractFaceletValidationStrategy> validationStrategies = getStrategies(currentFile
                             .getProject());
                     for (AbstractFaceletValidationStrategy strategy : validationStrategies) {
                         if (strategy.shouldValidate(currentFile)) {
                             final int percent = (i * 100) / uris.length + 1;
                             final IMessage message = new LocalizedMessage(IMessage.LOW_SEVERITY, percent + "% "
                                     + uris[i]);
                             reporter.displaySubtask(this, message);
 
                             strategy.validateFile(currentFile, reporter, this);
                         }
                     }
                 }
             }
         }
     }
 
     private List<AbstractFaceletValidationStrategy> getStrategies(final IProject project) {
         Map<String, ExtensionData<AbstractFaceletValidationStrategy>> possibleStrategies = FaceletUiPlugin.getDefault()
                 .getValidationStrategy();
         List<AbstractFaceletValidationStrategy> strategies = new ArrayList<AbstractFaceletValidationStrategy>(
                 possibleStrategies.size());
         int maxPriority = 0;
         for (final Map.Entry<String, ExtensionData<AbstractFaceletValidationStrategy>> entry : possibleStrategies
                 .entrySet()) {
             AbstractFaceletValidationStrategy instance = entry.getValue().getInstance(project);
             if (instance != null) {
 
                 int priority = instance.getPriority();
                 if (priority > maxPriority) {
                     strategies.clear();
                     maxPriority = priority;
                     strategies.add(instance);
                 }
 
                 if (priority == maxPriority) {
                     strategies.add(instance);
                 }
             }
         }
         return strategies;
     }
 
 }
