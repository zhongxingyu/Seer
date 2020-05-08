 package org.iplantc.core.uicommons.client.validators;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.iplantc.core.resources.client.messages.I18N;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorError;
 import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
 import com.sencha.gxt.widget.core.client.form.validator.AbstractValidator;
 
 public class CmdLineArgCharacterValidator extends AbstractValidator<String> {
 
     private final String restrictedChars;
 
    protected CmdLineArgCharacterValidator(String restrictedChars) {
         this.restrictedChars = restrictedChars;
     }
 
     public CmdLineArgCharacterValidator() {
         this(I18N.V_CONSTANTS.restrictedCmdLineArgChars());
     }
 
     public CmdLineArgCharacterValidator(boolean excludeReturnChar) {
         this(I18N.V_CONSTANTS.restrictedCmdLineArgCharsExclNewline());
     }
 
     @Override
     public List<EditorError> validate(Editor<String> editor, String value) {
         if (value == null) {
             return Collections.emptyList();
         }
         // We have an error
         char[] restrictedCharsArr = (restrictedChars + "=").toCharArray(); //$NON-NLS-1$
         StringBuilder restrictedFound = new StringBuilder();
 
         for (char restricted : restrictedCharsArr) {
             for (char next : value.toCharArray()) {
                 if (next == restricted) {
                     restrictedFound.append(restricted);
                     break;
                 }
             }
         }
 
         if (restrictedFound.length() > 0) {
             String errorMsg = I18N.VALIDATION.unsupportedChars(restrictedChars) + " " + I18N.VALIDATION.invalidChars(restrictedFound.toString());
             return createError(new DefaultEditorError(editor, errorMsg, value));
         }
 
         return Collections.emptyList();
     }
 
 }
