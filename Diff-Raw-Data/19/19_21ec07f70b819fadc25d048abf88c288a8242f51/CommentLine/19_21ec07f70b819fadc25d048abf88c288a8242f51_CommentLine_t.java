 package net.caprazzi.giddone.model;
 
 import net.caprazzi.giddone.parsing.SourceFile;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
 
 public class CommentLine {
     private final SourceFile source;
     private final int lineNumber;
     private final String line;
 
     public CommentLine(SourceFile source, int lineNumber, String line) {
         this.source = source;
         this.lineNumber = lineNumber;
         this.line = line;
     }
 
     public SourceFile getSource() {
         return source;
     }
 
     public int getLineNumber() {
         return lineNumber;
     }
 
     public String getLine() {
         return line;
     }
 
     public String getComment() {
         String prefix = source.getLanguage().getCommentStrategy().singleCommentStart();
         int index = line.indexOf(prefix) + prefix.length();
         String result = line.substring(index).trim();
         return result;
     }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append(source)
            .append(lineNumber)
            .append(line)
            .toString();
    }
 }
