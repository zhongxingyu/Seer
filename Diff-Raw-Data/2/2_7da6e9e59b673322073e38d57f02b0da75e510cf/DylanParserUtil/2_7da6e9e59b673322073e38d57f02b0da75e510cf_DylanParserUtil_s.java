 package org.dylanfoundry.deft.filetypes.dylan;
 
 import com.intellij.lang.PsiBuilder;
 import org.dylanfoundry.deft.filetypes.dylan.parser.DylanParser;
 import org.dylanfoundry.deft.parser.GeneratedParserUtilBase;
 
 import static com.intellij.lang.PsiBuilder.Marker;
 import static org.dylanfoundry.deft.filetypes.dylan.psi.DylanTypes.*;
 
 public class DylanParserUtil extends GeneratedParserUtilBase {
 
   public static boolean clauseOption(PsiBuilder builder, int level) {
     String tokenText = builder.getTokenText();
     final String lowercaseTokenText = tokenText.toLowerCase();
     if ("import:".equals(lowercaseTokenText)) {
       return DylanParser.import_option(builder, level);
     } else if ("exclude:".equals(lowercaseTokenText)) {
       return DylanParser.exclude_option(builder, level);
     } else if ("prefix:".equals(lowercaseTokenText)) {
       return DylanParser.prefix_option(builder, level);
     } else if ("rename:".equals(lowercaseTokenText)) {
       return DylanParser.rename_option(builder, level);
     } else if ("export:".equals(lowercaseTokenText)) {
      return DylanParser.export_clause(builder, level);
     }
     return false;
   }
 
   public static boolean keywordWithValues(PsiBuilder builder, int level, String... values) {
     if (!nextTokenIs(builder, KEYWORD)) return false;
     boolean result = false;
     String tokenText = builder.getTokenText();
     if (tokenText == null) return false;
     if (builder.getTokenType() != KEYWORD) return false;
 
     for (String value : values) {
       if (value.equals(tokenText.toLowerCase()))
         result = true;
     }
     if (result) consumeToken(builder, KEYWORD);
     return result;
   }
 
   public static boolean keywordWithValue(PsiBuilder builder, int level, String value) {
     return keywordWithValues(builder, level, value);
   }
 
   public static boolean unreservedNameWithValues(PsiBuilder builder, int level, String... values) {
     //if (!nextTokenIs(builder, UNRESERVED_NAME)) return false;
     boolean result = false;
     String tokenText = builder.getTokenText();
     if (tokenText == null) return false;
     for (String value : values) {
       if (value.equals(tokenText.toLowerCase())) {
         result = true;
       }
     }
     if (result) consumeToken(builder, NONDEFINING_NONEXPRESSION_WORD);
     return result;
   }
 }
