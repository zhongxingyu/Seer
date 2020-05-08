 package ch10.ex10_04;
 
 class Transform {
   public static String str2JavaString(String source) {
     String transformedStr = "";
    int index = 0;
    //for (int i = 0; i < source.length(); ++i) {
     while (index < source.length()) {
       char ch = source.charAt(index);
       
       switch (ch) {
       case '\n':
         transformedStr += "\\n";
         break;
       case '\t':
         transformedStr += "\\t";
         break;
       case '\b':
         transformedStr += "\\b";
         break;
       case '\r':
         transformedStr += "\\r";
         break;
       case '\f':
         transformedStr += "\\f";
         break;
       case '\\':
         transformedStr += "\\\\";
         break;
       case '\'':
         transformedStr += "\\\'";
         break;
       case '\"':
         transformedStr += "\\\"";
         break;
       default:
         transformedStr += ch;
         break;
       }
       
       index++;
     }
     
     return transformedStr;
   }
   
   public static void main(String[] args) {
     System.out.println(str2JavaString("hoge\""));
   }
 }
