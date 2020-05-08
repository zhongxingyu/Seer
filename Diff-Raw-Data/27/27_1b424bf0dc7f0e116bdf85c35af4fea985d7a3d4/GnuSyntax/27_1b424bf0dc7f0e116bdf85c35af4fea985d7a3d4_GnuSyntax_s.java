 package org.testinfected.cli.gnu;
 
 import org.testinfected.cli.args.Syntax;
 import org.testinfected.cli.args.Option;
 import org.testinfected.cli.util.Strings;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class GnuSyntax implements Syntax
 {
     private static final Pattern LONG_FORM_PATTERN = Pattern.compile("--([^\\s]+)(?:\\s(.+))?");
     private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("-([^\\s]+)(?:\\s+(.+))?");
     private static final int IDENTIFIER_POSITION = 1;
     private static final int ARGUMENT_PATTERN_POSITION = 2;
 
     public Option defineOption(String name, String... schema) {
         Option option = new Option(name);
 
        SchemaValidator schemaValidator = new SchemaValidator();
         for (String schemaElement : schema) {
             Matcher longFormMatcher = LONG_FORM_PATTERN.matcher(schemaElement);
             if (longFormMatcher.matches()) {
                 String longForm = getOptionIdentifier(longFormMatcher);
                 option.setLongForm(schemaValidator.validateLongForm(longForm));
 
                 String argumentPattern = getArgumentPattern(longFormMatcher);
                 option.setArgumentPattern(schemaValidator.validateArgumentPattern(argumentPattern));
 
                 continue;
             }
 
             Matcher shortFormMatcher = SHORT_FORM_PATTERN.matcher(schemaElement);
             if (shortFormMatcher.matches()) {
                 String shortForm = getOptionIdentifier(shortFormMatcher);
                 option.setShortForm(schemaValidator.validateShortForm(shortForm));
 
                 String argumentPattern = getArgumentPattern(shortFormMatcher);
                 option.setArgumentPattern(schemaValidator.validateArgumentPattern(argumentPattern));
 
                 continue;
             }
 
             option.setDescription(schemaValidator.validateDescription(schemaElement));
         }
         return option;
     }
 
     private String getArgumentPattern(Matcher matcher) {
         return matcher.group(ARGUMENT_PATTERN_POSITION);
     }
 
     private String getOptionIdentifier(Matcher matcher) {
         return matcher.group(IDENTIFIER_POSITION);
     }
 
     private static class SchemaValidator
     {
         private boolean shortFormHasBeenGiven;
         private boolean longFormHasBeenGiven;
         private boolean argumentPatternHasBeenGiven;
         private boolean descriptionHasBeenGiven;
 
         public String validateShortForm(String shortForm) {
            if (shortFormHasBeenGiven) throw new IllegalArgumentException("Short form given twice");
             this.shortFormHasBeenGiven = true;
             return shortForm;
         }
 
         public String validateLongForm(String longForm) {
            if (longFormHasBeenGiven) throw new IllegalArgumentException("Long form given twice");
             this.longFormHasBeenGiven = true;
             return longForm;
         }
 
         public String validateArgumentPattern(String argumentPattern) {
             if (Strings.blank(argumentPattern)) return null;
            if (argumentPatternHasBeenGiven) throw new IllegalArgumentException("Argument pattern given twice");
             this.argumentPatternHasBeenGiven = true;
             return argumentPattern;
         }
 
         public String validateDescription(String description) {
            if (descriptionHasBeenGiven) throw new IllegalArgumentException("Description given twice");
             descriptionHasBeenGiven = true;
             return description;
         }
     }
 }
