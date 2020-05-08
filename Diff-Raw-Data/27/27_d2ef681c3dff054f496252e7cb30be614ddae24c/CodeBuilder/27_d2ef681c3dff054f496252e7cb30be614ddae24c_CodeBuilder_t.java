 package jp.long_long_float.cuick.cppStructure;
 
 
 public class CodeBuilder {
     private StringBuilder sb = new StringBuilder();
     
    public final static String SEPARATOR = System.getProperty("line.separator");
    
     public CodeBuilder add(String str, boolean withIndent) {
         sb.append((withIndent ? CodeContext.getInstance().getTabSpace() : "") + str);
         return this;
     }
     
     public CodeBuilder add(String str) {
         add(str, true);
         return this;
     }
     
     public CodeBuilder addLine(String line, boolean withIndent) {
        add(line + SEPARATOR, withIndent);
         return this;
     }
     
     public CodeBuilder addLine(String line) {
         addLine(line, true);
         return this;
     }
     
     public String toString() {
         return sb.toString();
     }
     
     public interface BlockCallback {
         public void call(CodeBuilder cb);
     }
     
     public void block(BlockCallback callback, boolean semicolon) {
         beginBlock();
         callback.call(this);
         endBlock(semicolon);
     }
     
     public void beginBlock() {
         addLine("{", false);
         CodeContext.getInstance().indent();
     }
     
     public void endBlock(boolean semicolon) {
         CodeContext.getInstance().unindent();
         addLine("}" + (semicolon ? ";" : ""));
     }
 }
