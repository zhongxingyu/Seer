 package org.jboss.pressgang.ccms.contentspec.client.validator;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import com.beust.jcommander.IParameterValidator;
 import com.beust.jcommander.ParameterException;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 
 public class OverrideValidator implements IParameterValidator {
     private static final List<String> validNames = Arrays.asList(CSConstants.AUTHOR_GROUP_OVERRIDE, CSConstants.REVISION_HISTORY_OVERRIDE,
             CSConstants.FEEDBACK_OVERRIDE, CSConstants.REVNUMBER_OVERRIDE, CSConstants.PUBSNUMBER_OVERRIDE, CSConstants.BRAND_OVERRIDE,
             CSConstants.BRAND_ALT_OVERRIDE, CSConstants.POM_OVERRIDE);
 
     @Override
     public void validate(final String name, final String value) throws ParameterException {
         if (value.matches("^.*=.*$")) {
             final String[] vars = value.split("=", 2);
             final String varName = vars[0];
             final String varValue = vars[1];
 
             if (validNames.contains(varName)) {
                 if (varName.equals(CSConstants.AUTHOR_GROUP_OVERRIDE) || varName.equals(CSConstants.REVISION_HISTORY_OVERRIDE) ||
                         varName.equals(CSConstants.FEEDBACK_OVERRIDE) || varName.equals(CSConstants.POM_OVERRIDE)) {
                     final File file = new File(ClientUtilities.fixFilePath(varValue));
                     if (!(file.exists() && file.isFile())) {
                        throw new ParameterException("The \"" + varName + "\" override references a file that could not be found.");
                     }
                 } else if (varName.equals(CSConstants.REVNUMBER_OVERRIDE)) {
                     if (!varValue.matches("^(" + ProcessorConstants.VERSION_EPOCH_VALIDATE_REGEX + ")-[0-9]+$")) {
                         throw new ParameterException("The \"" + varName + "\" override is not a valid revision history number.");
                     }
                 } else if (varName.equals(CSConstants.PUBSNUMBER_OVERRIDE)) {
                     try {
                         Integer.parseInt(varValue);
                     } catch (Exception e) {
                         throw new ParameterException("The \"" + varName + "\" override is not a valid pubsnumber.");
                     }
                 }
             } else {
                 throw new ParameterException("The \"" + varName + "\" override is not a valid override parameter");
             }
         } else {
             throw new ParameterException("Invalid override parameter");
         }
     }
 
 }
