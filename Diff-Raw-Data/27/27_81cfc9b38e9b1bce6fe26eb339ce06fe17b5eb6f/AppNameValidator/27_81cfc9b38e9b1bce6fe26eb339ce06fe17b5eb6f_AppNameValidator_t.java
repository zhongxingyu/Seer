 package org.iplantc.core.uicommons.client.validators;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.iplantc.core.resources.client.messages.I18N;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorError;
 import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
 import com.sencha.gxt.widget.core.client.form.validator.AbstractValidator;
 
 public class AppNameValidator extends AbstractValidator<String> {
 
     @Override
     public List<EditorError> validate(Editor<String> editor, String value) {
         if (value == null) {
             return Collections.emptyList();
         }
        
        char[] restrictedChars = (I18N.V_CONSTANTS.restrictedAppNameChars()).toCharArray(); //$NON-NLS-1$
        StringBuilder restrictedFound = new StringBuilder();
 
         // check for spaces at the beginning and at the end of the file name
         if (value.startsWith(" ") || value.endsWith(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
            return createError(new DefaultEditorError(editor, I18N.VALIDATION.analysisNameValidationMsg(new String(restrictedChars)), value));
         }
 
       
 
         for (char restricted : restrictedChars) {
             for (char next : value.toCharArray()) {
                 if (next == restricted) {
                     restrictedFound.append(restricted);
                     break;
                 }
             }
         }
 
         if (restrictedFound.length() > 0) {
            String errorMsg = I18N.VALIDATION.analysisNameValidationMsg(new String(restrictedChars)) + ". " //$NON-NLS-1$
                    + I18N.VALIDATION.invalidChars(restrictedFound.toString());
 
             return createError(new DefaultEditorError(editor, errorMsg, value));
         }
 
         return Collections.emptyList();
     
     }
 
 }
