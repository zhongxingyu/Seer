 package teach.testrig;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class TypeHelper {
    
    public static int[] convIntegerToint(Integer[] o) {
       int len;
       int[] ret = new int[len = o.length];
       for (int i = 0; i < len; i++) {
          ret[i] = o[i];
       }
       return ret;
    }
    
    public static String toString(Object o) {
       if (o instanceof Object[]) {
          Object[] arr = (Object[])o;
          
          int len;
          if ((len = arr.length) == 0) {
             return "{ }";
          }
          StringBuilder sb = new StringBuilder("{");
          len--;
          for (int i = 0; i < len; i++) {
             sb.append(toString(arr[i])).append(", ");
          }
          
          return sb.append(toString(arr[len])).append("}").toString();
       }else if (o == null){
          return "null";
      }else {
          return o.toString();
       }
    }
    
    public static boolean test(Object o1, Object o2) {
       do {
          if (o1 == null) {
             if (o2 != null) {
                break;
             }
          }else if (o1 instanceof Object[] && o2 instanceof Object[]){
             if (!Arrays.deepEquals((Object[])o1, (Object[])o2)) {
                break;
             }
          }else if (!o1.equals(o2)) {
             break;
          }
          return true;
       }while (false);
       return false;
    }
    
    private static void ttest(Object o1, Object o2) {
       if (!test(o1, o2)) {
          throw new RuntimeException(o1 + " != " + o2);
       }
    }
    
    public static void main(String...asdf) throws Exception{
       ttest(scan("[String", "\\null"), null);
       ttest(scan("[String", "\\{}"), new String[]{});
       ttest(scan("[String", "[aa,\\null]"), new String[]{"aa",null});
       
       ttest(scan("[[I", "[[1,2],\\{},\\null]"), new Integer[][]{new Integer[]{1,2}, new Integer[]{}, null});
       ttest(scan("[[I", "[\\{}]"), new Integer[][]{new Integer[]{}});
       ttest(scan("[[I", "[[1,2]]"), new Integer[][]{new Integer[]{1,2}});
       ttest(scan("[[I", "[[1],\\null]"), new Integer[][]{new Integer[]{1}, null});
       ttest(scan("[String", "[llll,aaaa]"), new String[]{"llll","aaaa"});
       
       ttest(scan("[I", "[2,6,4,8]"), new Integer[]{2,6,4,8});
       ttest(scan("[[I", "[[1,2],[3,4]]"), new Integer[][]{new Integer[]{1,2}, new Integer[]{3,4}});
       ttest(scan("[I", "\\null"), null);
       
       System.out.println("Tests for TypeHelper succeeded!");
    }
    
    public static Object scan(String type, String line) {
       Object ret = scanHelper(type, line);
       
       if (ret instanceof Class<?>) {
          Class<?> clazz = ((Class<?>)ret).getComponentType();
          if (clazz == null) {
             clazz = (Class<?>)ret;
          }
          return Array.newInstance(clazz,0);
       }
       return ret;
    }
    
    private static Object scanHelper(String type, String line) {
       //Used for null tests / empty lists
       if (line.startsWith("\\")) {
          line = line.substring(1);
          switch (line) {
             case "null":
                return null;
             case "{}":
                switch(type) {
                   case "boolean":
                   case "Z":
                      return Boolean.class;
                   case "byte":
                   case "B":
                      return Byte.class;
                   case "char":
                   case "C":
                      return Character.class;
                   case "double":
                   case "D":
                      return Double.class;
                   case "float":
                   case "F":
                      return Float.class;
                   case "int":
                   case "I":
                      return Integer.class;
                   case "long":
                   case "J":
                      return Long.class;
                   case "short":
                   case "S":
                      return Short.class;
                   case "String":
                      return String.class;
                   default:
                      return Array.newInstance((Class<?>)scanHelper(type.substring(1), "\\{}"), 0).getClass();
                }
             default:
                throw new RuntimeException("Unknown escape sequence \"" + line + "\"");
          }
       }
       
       switch(type) {
          case "boolean":
          case "Z":
             return Boolean.parseBoolean(line);
          case "byte":
          case "B":
             return Byte.parseByte(line);
          case "char":
          case "C":
             return line.charAt(0);
          case "double":
          case "D":
             return Double.parseDouble(line);
          case "float":
          case "F":
             return Float.parseFloat(line);
          case "int":
          case "I":
             return Integer.parseInt(line);
          case "long":
          case "J":
             return Long.parseLong(line);
          case "short":
          case "S":
             return Short.parseShort(line);
          case "String":
             return line;
          default:
             if (!type.startsWith("[")) {
                throw new RuntimeException("Bad test case file!");
             }
             
             ArrayList<Object> list = new ArrayList<>();
             
             int pos = 1;
             int next;
             char[] cs = line.toCharArray();
             char delim;
             do {
                delim = cs[next = getNextComma(cs, pos)];
                
                list.add(scan(type.substring(1), line.substring(pos, next)));
                
                pos = next+1;
             }while (delim != ']');
             
             return list.toArray((Object[])Array.newInstance(list.get(0).getClass(),0));
             
       }
    }
    
    private static int getNextComma(char[] cs, int pos) {
       int braces = 0;
       while (true) {
          switch (cs[pos]) {
             case '[':
                braces++;
                break;
             case ']':
                if (--braces < 0) {
                   return pos;
                }
                break;
             case ',':
                if (braces == 0) {
                   return pos;
                }
                break;
          }
          pos++;
       }
    }
 }
 
 /*
 boolean       Z
 byte       B
 char       C
 class    Lclassname;
 double       D
 float       F
 int       I
 long       J
 short       S
 
  */
