 package zhenghui.jvm.parse;
 
 import static zhenghui.jvm.CommonConstant.TWO;
 
 /**
  * Created by IntelliJ IDEA.
  * User: zhenghui
  * Date: 13-1-14
  * Time: 4:45
  * һЩϢĽ
  */
 public class BaseInfoParse {
 
     public static final int ACC_PUBLIC        = 0x0001; // class, inner, field, method
     public static final int ACC_FINAL         = 0x0010; // class, inner, field, method
     public static final int ACC_SUPER         = 0x0020; // class
     public static final int ACC_INTERFACE     = 0x0200; // class, inner
     public static final int ACC_ABSTRACT      = 0x0400; // class, inner,        method
     public static final int ACC_SYNTHETIC     = 0x1000; // class, inner, field, method
     public static final int ACC_ANNOTATION    = 0x2000; // class, inner
     public static final int ACC_ENUM          = 0x4000; // class, inner, field
     public static final int ACC_MODULE        = 0x8000; // class, inner, field, method
 
     /**
      * classʮֽ
      */
     private String code;
 
     public BaseInfoParse(String code) {
         this.code = code;
     }
 
     /**
      * Ϣ,,ħͰ汾
      *
      * @return
      */
     public String[] parseBaseInfo() {
         String[] strs = new String[4];
         //todo Ҫ
         strs[0] = "source file : xxx";
         //ǰ4ֽħ
         Type magic_num = new Type(code.substring(0 * TWO, 4 * TWO));
         strs[1] = "magic num : " + magic_num.getValue();
         //minnor 汾
         Type minor_version = new Type(code.substring(4 * TWO, 6 * TWO));
         strs[2] = "minor version : " + minor_version.getValue();
         //major汾
         Type major_version = new Type(code.substring(6 * TWO, 8 * TWO));
         strs[3] = "major version : " + major_version.getValue();
         return strs;
     }
 
     public ParseResult parseAccessFlags(int hand){
         ParseResult result = new ParseResult();
         int access_flag_end = hand + 2 * TWO;
         Type access_flag_type = new Type(code.substring(hand,access_flag_end));
         int acccess_flag = access_flag_type.getDecimalInteger();
         String[] strs = new String[1];
         strs[0] = "flags: " + getAccessFlags(acccess_flag);
         //ʱڳغ,ռֽ.
         result.setHandle(access_flag_end);
         result.setStrs(strs);
         return result;
     }
 
     private String getAccessFlags(int access_flag){
         String flags = "";
         if(hasTag(access_flag,ACC_PUBLIC)){
             flags += " ACC_PUBLIC,";
         }
         if(hasTag(access_flag,ACC_FINAL)){
             flags += " ACC_FINAL,";
         }
         if(hasTag(access_flag,ACC_SUPER)){
             flags += " ACC_SUPER,";
         }
         if(hasTag(access_flag,ACC_INTERFACE)){
             flags += " ACC_INTERFACE,";
         }
         if(hasTag(access_flag,ACC_ABSTRACT)){
             flags += " ACC_ABSTRACT,";
         }
         if(hasTag(access_flag,ACC_SYNTHETIC)){
             flags += " ACC_SYNTHETIC,";
         }
         if(hasTag(access_flag,ACC_ANNOTATION)){
             flags += " ACC_ANNOTATION,";
         }
         if(hasTag(access_flag,ACC_ENUM)){
             flags += " ACC_ENUM,";
         }
         if(hasTag(access_flag,ACC_MODULE)){
             flags += " ACC_MODULE,";
         }
         return flags.substring(0,flags.length() -1);
     }
 
     /**
      * flagǷжӦtag
      * @param flag
      * @param tag
      * @return
      */
     private boolean hasTag(int flag,int tag){
        return (flag * tag) == tag;
     }
 }
