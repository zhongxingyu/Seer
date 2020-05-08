 package bem.idea.bemhtml.highlighter;
 
 import bem.idea.bemhtml.BemHtmlIcons;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 import com.intellij.openapi.fileTypes.SyntaxHighlighter;
 import com.intellij.openapi.options.colors.AttributesDescriptor;
 import com.intellij.openapi.options.colors.ColorDescriptor;
 import com.intellij.openapi.options.colors.ColorSettingsPage;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import java.util.Map;
 
 public class BemHtmlColorSettingsPage implements ColorSettingsPage {
 
     private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[] {
             new AttributesDescriptor("Entity keyword", BemHtmlSyntaxHighlighter.KEYWORD_ENTITY),
             new AttributesDescriptor("Mod keyword", BemHtmlSyntaxHighlighter.KEYWORD_MOD),
             new AttributesDescriptor("BEM value", BemHtmlSyntaxHighlighter.BEM_VALUE),
             new AttributesDescriptor("Other BEMJSON property", BemHtmlSyntaxHighlighter.JSON_PROPERTY),
             new AttributesDescriptor("Delim", BemHtmlSyntaxHighlighter.KEYWORDS_DELIM),
             new AttributesDescriptor("Colon", BemHtmlSyntaxHighlighter.KEYWORDS_COLON),
             new AttributesDescriptor("Braces", BemHtmlSyntaxHighlighter.BRACES),
             new AttributesDescriptor("Error", BemHtmlSyntaxHighlighter.BAD_CHARACTER)
     };
 
     @NotNull
     public String getDisplayName() {
         return "BEMHTML";
     }
 
     public Icon getIcon() {
         return BemHtmlIcons.FILE_TYPE;
     }
 
     @NotNull
     public AttributesDescriptor[] getAttributeDescriptors() {
         return ATTRS;
     }
 
     @NotNull
     public ColorDescriptor[] getColorDescriptors() {
         return ColorDescriptor.EMPTY_ARRAY;
     }
 
     @NotNull
     public SyntaxHighlighter getHighlighter() {
         return new BemHtmlSyntaxHighlighter();
     }
 
     @NotNull
     public String getDemoText() {
         return "block b-block, elem myElem, default {\n" +
                 "\n" +
                 "    mod pseudo 'y'+'e'+'s' {\n" +
                 "        bem: false,\n" +
                 "        js: true,\n" +
                 "        tag: 'img',\n" +
                 "        myProperty: 'text',\n" +
                 "        mix: [ this.ctx ]\n" +
                 "    }\n" +
                 "\n" +
                "    elemMod state current error {\n" +
                 "        content: {\n" +
                 "            tag: 'i',\n" +
                 "            elem: 'elem'\n" +
                 "        }\n" +
                 "    }\n" +
                 "\n" +
                 "    attrs, this.ctx.url: {\n" +
                 "        return { src: this.ctx.url };\n" +
                 "    }\n" +
                 "\n" +
                 "}";
     }
 
     public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
         return null;
     }
 }
