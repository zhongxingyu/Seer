 package ohtu.refero.bibtex;
 
 public class BibTeXGenerator {
     
     private StringBuffer buffer;
     
     public BibTeXGenerator() {
         buffer = new StringBuffer();
     }
     
     public void writeObjectStart() {   
         buffer.append("{");
     }
     
     public void writeObjectEnd() {
         buffer.append("}");
     }
     
    public void writeNewline() {
        buffer.append("\n");
     }
     
     public void writeSeparator() {
         buffer.append(",");
     }
     
     public void writeObjectFieldStart(String name) {
         
         buffer.append("@")
               .append(name);
         
         writeObjectStart();
     }
     
     public void writeObject(Object object) {
         buffer.append(object);
     }
     
     public void writeObjectField(String name, Object object) {
         
         writeIndentation();
         buffer.append(name)
               .append(" ")
               .append("=")
               .append(" ");
         
         writeObjectStart();
         writeObject(object);
         writeObjectEnd();
     }
     
    public void writeIndentation() {
        buffer.append("  ");
    }
    
     @Override
     public String toString() {
         return buffer.toString();
     }
 }
