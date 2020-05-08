 package org.eclipse.jst.jsf.facelet.ui.internal.validation;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jst.jsf.ui.internal.validation.ValidationMessageFactory;
 import org.eclipse.jst.jsf.validation.internal.IJSFViewValidator;
 import org.eclipse.jst.jsf.validation.internal.ValidationPreferences;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidator;
 
 /*package*/class ValidationReporter implements
         IJSFViewValidator.IValidationReporter
 {
     private final IValidator            _validator;
     private final IReporter             _reporter;
     private final IFile                 _file;
    private final ValidationPreferences _prefs;
 
     public ValidationReporter(final IValidator validator,
             final IReporter reporter, final IFile file,
             final ValidationPreferences prefs)
     {
         _validator = validator;
         _reporter = reporter;
         _file = file;
        _prefs = prefs;
     }
 
     public void report(final Diagnostic problem, final int start,
             final int length)
     {
        final IMessage message = ValidationMessageFactory.createFromDiagnostic(
                problem, start, length, _file, _prefs);
 
         if ((message.getSeverity() & IMessage.ALL_MESSAGES) != 0)
         {
             _reporter.addMessage(_validator, message);
         }
     }
 
     public void report(IMessage message)
     {
         if ((message.getSeverity() & IMessage.ALL_MESSAGES) != 0)
         {
             _reporter.addMessage(_validator, message);
         }
     }
 }
