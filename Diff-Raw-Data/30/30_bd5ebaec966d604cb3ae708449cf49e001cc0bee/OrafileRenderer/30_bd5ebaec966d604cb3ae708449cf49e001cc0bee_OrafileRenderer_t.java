 package org.codeswarm.orafile;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Iterator;
import java.util.regex.Pattern;
 
 public class OrafileRenderer {
 
     private enum Parens {
 
         Yes, No;
 
         boolean yes() {
             return this == Yes;
         }
     }
 
     public String renderFile(OrafileDict dict) {
         StringWriter writer = new StringWriter();
         try {
             renderFile(dict, writer);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return writer.toString();
     }
 
     public void renderFile(OrafileDict dict, Writer writer) throws IOException {
         Iterator<OrafileDef> defs = dict.list.iterator();
         while (defs.hasNext()) {
 
             OrafileDef def = defs.next();
 
             renderDef(writer, def, Parens.No, "");
 
             if (defs.hasNext()) {
                 writer.append("\n");
             }
         }
     }
 
     void renderDef(Writer writer, OrafileDef def, Parens parens, String indent) throws IOException {
 
         String nextIndent = indent + "  ";
 
         OrafileVal val = def.getVal();
 
         if (val instanceof OrafileString) {
 
             String stringVal = ((OrafileString) val).string;
 
             writer.append(indent);
             if (parens.yes()) writer.append("(");
             writer.append(def.getName()).append(" = ");
            renderString(writer, stringVal);
             if (parens.yes()) writer.append(")\n");
 
         } else if (val instanceof OrafileStringList) {
 
             OrafileStringList stringListVal = (OrafileStringList) val;
 
             writer.append(indent);
             if (parens.yes()) writer.append("(");
             writer.append(def.getName()).append(" = (\n");
             Iterator<String> stringVals = stringListVal.list.iterator();
             while (stringVals.hasNext()) {
 
                 String stringVal = stringVals.next();
 
                 writer.append(nextIndent);
                renderString(writer, stringVal);
                 if (stringVals.hasNext()) writer.append(",");
                 writer.append("\n");
             }
             writer.append(indent).append(")");
             if (parens.yes()) writer.append(")\n");
 
         } else {
 
             OrafileDict dict = (OrafileDict) val;
 
             writer.append(indent);
             if (parens.yes()) writer.append("(");
             writer.append(def.getName()).append(" =\n");
             for (OrafileDef nextDef : dict.list) {
                 renderDef(writer, nextDef, Parens.Yes, nextIndent);
             }
 
             if (parens.yes()) writer.append(indent).append(")\n");
         }
     }

    static final Pattern SAFE_STRING =
        Pattern.compile("^[A-Za-z0-9\\Q<>/.:;-_$+*&!%?@\\E]+$");

    void renderString(Writer writer, String string) throws IOException {

        if (SAFE_STRING.matcher(string).matches()) {
            writer.append(string);
        } else {
            String escaped = string
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\"", "\\\\\"");
            writer.append("\"").append(escaped).append("\"");
        }
    }
 }
