 package project.phase2.structs;
 
 public class StringMatchTuple {
 
     public String string;
     public String fileName = "";
     public int line = -1, startIndex = -1, endIndex = -1;
 
     public StringMatchTuple() {
         //default
     }
 
     public StringMatchTuple(String s) {
         string = s;
     }
 
     public StringMatchTuple(StringMatchTuple s) {
         set(s);
     }
 
     public void set(StringMatchTuple s) {
         string = s.string;
         fileName = s.fileName;
         line = s.line;
         startIndex = s.startIndex;
         endIndex = s.endIndex;
     }
 
     public boolean found() {
         return line != -1;
     }
 
     public String toString() {
         // "cba"<"file1.txt", 40, 50>}
         return "\"" + string + "\" <\"" + fileName + "\", " + line + ", " + startIndex + ", " + endIndex + ">";
     }
 
     @Override
     public boolean equals(Object o) {
 
         if (this == o)
             return true;
 
         if (!(o instanceof StringMatchTuple)) {
             return false;
         }
 
         StringMatchTuple t = (StringMatchTuple) o;
 
        return this.string.equals(t.string);
     }
 }
