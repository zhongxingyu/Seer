 package com.technophobia.substeps.syntax;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 
 import com.technophobia.eclipse.preference.PreferenceLookupFactory;
 import com.technophobia.substeps.model.FeatureFile;
 import com.technophobia.substeps.model.ParentStep;
 import com.technophobia.substeps.model.Syntax;
 import com.technophobia.substeps.runner.TagManager;
 import com.technophobia.substeps.runner.TestParameters;
 import com.technophobia.substeps.runner.syntax.SyntaxErrorReporter;
 import com.technophobia.substeps.runner.syntax.validation.StepValidator;
 import com.technophobia.substeps.runner.syntax.validation.SyntaxAwareStepValidator;
 
 public class ProblemValidatingProjectToSyntaxTransformer extends ProjectToSyntaxTransformer {
 
     private final PreferenceLookupFactory<IProject> preferenceLookupFactory;
 
 
     ProblemValidatingProjectToSyntaxTransformer(final PreferenceLookupFactory<IProject> preferenceLookupFactory) {
         // package scope constructor to encourage use of the
         // CachingProjectToSyntaxTransformer
         super();
         this.preferenceLookupFactory = preferenceLookupFactory;
     }
 
 
     @Override
     protected SyntaxErrorReporter syntaxErrorReporterFor(final IProject project) {
         return new MarkerSyntaxErrorReporter(project, preferenceLookupFactory.preferencesFor(project));
     }
 
 
     @Override
     protected Syntax buildSyntaxFor(final File projectFile, final List<Class<?>> stepClasses,
             final ClassLoader classLoader, final SyntaxErrorReporter syntaxErrorReporter) {
         final Syntax syntax = super.buildSyntaxFor(projectFile, stepClasses, classLoader, syntaxErrorReporter);
 
         // while we're here, lets validate the feature and substep files for
         // missing steps
         reportMissingSteps(projectFile, syntax, syntaxErrorReporter);
 
         // we know we're using a DeferredReportingSyntaxErrorReporter, so finish
         // it off
         ((DeferredReportingSyntaxErrorReporter) syntaxErrorReporter).applyChanges();
 
         return syntax;
     }
 
 
     private void reportMissingSteps(final File projectFile, final Syntax syntax,
             final SyntaxErrorReporter syntaxErrorReporter) {
         final StepValidator validator = new SyntaxAwareStepValidator(syntax);
         reportMissingScenarioSteps(projectFile, syntax, validator, syntaxErrorReporter);
         reportMissingSubsteps(syntax.getSubStepsMap().values(), validator, syntaxErrorReporter);
     }
 
 
     private void reportMissingScenarioSteps(final File projectFile, final Syntax syntax, final StepValidator validator,
             final SyntaxErrorReporter syntaxErrorReporter) {
         final TestParameters testParameters = new TestParameters(new TagManager(""), syntax,
                 projectFile.getAbsolutePath());
        testParameters.init();
 
         for (final FeatureFile featureFile : testParameters.getFeatureFileList()) {
             validator.validateFeatureFile(featureFile, syntaxErrorReporter);
         }
     }
 
 
     private void reportMissingSubsteps(final Collection<ParentStep> substeps, final StepValidator validator,
             final SyntaxErrorReporter syntaxErrorReporter) {
         for (final ParentStep substep : substeps) {
             validator.validateSubstep(substep, syntaxErrorReporter);
         }
     }
 }
