 package pl.project13.protoscala.utils;
 
 import com.google.common.base.Joiner;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Date: 4/9/11
  *
  * @author Konrad Malawski
  */
 public class SourceStringBuilder {
   private Logger log = Logger.getLogger(getClass().getSimpleName());
 
   private StringBuilder stringBuilder = new StringBuilder();
 
   /**
    * Describes how many spaces should be used in the next line to have an consistent indentation.
    * <ul>
    * <li>Is incremented by all append calls</li>
    * <li>Is reset by all newLine calls.</li>
    * </ul>
    */
   private Integer indentation = 0;
 
   // keywords etc
   private String K_PACKAGE    = "package ";
   private String K_CASE_CLASS = "case class ";
 
   public SourceStringBuilder newLine() {
     stringBuilder.append("\n");
     return this;
   }
 
   public SourceStringBuilder appendComment(Object str) {
     stringBuilder.append("// ").append(str);
     return this;
   }
 
   // delegates ----------------------------------------------------------------
 
   public SourceStringBuilder append(Object obj) {
     stringBuilder.append(obj);
     return this;
   }
 
   public SourceStringBuilder append(String str) {
     stringBuilder.append(str);
     return this;
   }
 
   public SourceStringBuilder append(StringBuffer sb) {
     stringBuilder.append(sb);
     return this;
   }
 
   public SourceStringBuilder append(CharSequence s) {
     stringBuilder.append(s);
     return this;
   }
 
   public SourceStringBuilder append(CharSequence s, int start, int end) {
     indentation += end - start; //todo test me
     stringBuilder.append(s, start, end);
     return this;
   }
 
   public SourceStringBuilder append(char[] str) {
     indentation += str.length; //todo test me
     stringBuilder.append(str);
     return this;
   }
 
   public SourceStringBuilder append(char[] str, int offset, int len) {
     indentation += len; //todo test me
     stringBuilder.append(str, offset, len);
     return this;
   }
 
   public SourceStringBuilder append(boolean b) {
     indentation += b ? "true".length() : "false".length();
     stringBuilder.append(b);
     return this;
   }
 
   public SourceStringBuilder append(char c) {
     indentation++;
     stringBuilder.append(c);
     return this;
   }
 
   public SourceStringBuilder append(int i) {
     indentation += String.valueOf(i).length();
     stringBuilder.append(i);
     return this;
   }
 
   public SourceStringBuilder append(long lng) {
     indentation += String.valueOf(lng).length();
     stringBuilder.append(lng);
     return this;
   }
 
   public SourceStringBuilder append(float f) {
     indentation += String.valueOf(f).length();
     stringBuilder.append(f);
     return this;
   }
 
   public SourceStringBuilder append(double d) {
     indentation += String.valueOf(d).length();
     stringBuilder.append(d);
     return this;
   }
 
 //  public SourceStringBuilder delete(int start, int end) {
 //    stringBuilder.delete(start, end);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder deleteCharAt(int index) {
 //    stringBuilder.deleteCharAt(index);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder replace(int start, int end, String str) {
 //    stringBuilder.replace(start, end, str);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int index, char[] str, int offset, int len) {
 //    stringBuilder.insert(index, str, offset, len);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, Object obj) {
 //    stringBuilder.insert(offset, obj);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, String str) {
 //    stringBuilder.insert(offset, str);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, char[] str) {
 //    stringBuilder.insert(offset, str);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int dstOffset, CharSequence s) {
 //    stringBuilder.insert(dstOffset, s);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
 //    stringBuilder.insert(dstOffset, s, start, end);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, boolean b) {
 //    stringBuilder.insert(offset, b);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, char c) {
 //    stringBuilder.insert(offset, c);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, int i) {
 //    stringBuilder.insert(offset, i);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, long l) {
 //    stringBuilder.insert(offset, l);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, float f) {
 //    stringBuilder.insert(offset, f);
 //    return this;
 //  }
 //
 //  public SourceStringBuilder insert(int offset, double d) {
 //    stringBuilder.insert(offset, d);
 //    return this;
 //  }
 //
 //  public int indexOf(String str) {
 //    return stringBuilder.indexOf(str);
 //  }
 //
 //  public int indexOf(String str, int fromIndex) {
 //    return stringBuilder.indexOf(str, fromIndex);
 //  }
 //
 //  public int lastIndexOf(String str) {
 //    return stringBuilder.lastIndexOf(str);
 //  }
 //
 //  public int lastIndexOf(String str, int fromIndex) {
 //    return stringBuilder.lastIndexOf(str, fromIndex);
 //  }
 
   @Override
   public String toString() {
     return stringBuilder.toString();
   }
 
   public int length() {
     return stringBuilder.length();
   }
 
   public int capacity() {
     return stringBuilder.capacity();
   }
 
   public void ensureCapacity(int minimumCapacity) {
     stringBuilder.ensureCapacity(minimumCapacity);
   }
 
   // functionality dedicated methods
 
   public SourceStringBuilder declarePackage(String javaPackage) {
     append(K_PACKAGE).append(javaPackage);
     appendNewLines(2);
     return this;
   }
 
   public SourceStringBuilder declareCaseClass(String className, List<String> params) {
    append(K_CASE_CLASS).append(className).append("(");
 
     String preparedParams = Joiner.on("," + indentationSpaces() + "\n").join(params);
     append(preparedParams);
     newLine();
 
     insertionPoint(className + "-params"); //todo externalize
     append(")");
 
     return this;
   }
 
   private String indentationSpaces() {
     StringBuilder sb = new StringBuilder(indentation);
     // todo this sucks!!!!
     for (int i = 0; i < indentation; i++) {
       sb.append(" ");
     }
     return sb.toString();
   }
 
   private SourceStringBuilder insertionPoint(String insertionPointName) {
     log.info("New insertion point: " + insertionPointName);
 
     return append("// @@protoc_insertion_point(").append(insertionPointName).append(")").newLine();
   }
 
   private SourceStringBuilder appendNewLines(Integer numberOfNewLines) {
     for (int i = 0; i < numberOfNewLines; i++) {
       newLine();
     }
     return this;
   }
 
   public SourceStringBuilder importThe(String dependency) {
     return append("import ").append(dependency).newLine();
   }
 }
