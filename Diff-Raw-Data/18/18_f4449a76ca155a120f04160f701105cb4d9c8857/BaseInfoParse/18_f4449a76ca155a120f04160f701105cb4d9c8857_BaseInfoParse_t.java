     package zhenghui.jvm.parse;
 
     import zhenghui.jvm.Util;
 
     import java.util.ArrayList;
     import java.util.List;
 
     import static zhenghui.jvm.CommonConstant.*;
 
     /**
      * Created by IntelliJ IDEA.
      * User: zhenghui
      * Date: 13-1-14
      * Time: 4:45
      * һЩϢĽ
      */
     public class BaseInfoParse {
 
         /**
          * classʮֽ
          */
         private String code;
 
         private AttributeParse attributeParse;
 
         public BaseInfoParse(String code) {
             this.code = code;
             attributeParse = new AttributeParse(code);
         }
 
         /**
          * Ϣ,,ħͰ汾
          *
          * @return
          */
         public String[] parseBaseInfo() {
             String[] strs = new String[3];
             //ǰ4ֽħ
             Type magic_num = new Type(code.substring(0 * TWO, 4 * TWO));
             strs[0] = "magic num : " + magic_num.getValue();
             //minnor 汾
             Type minor_version = new Type(code.substring(4 * TWO, 6 * TWO));
             strs[1] = "minor version : " + minor_version.getValue();
             //major汾
             Type major_version = new Type(code.substring(6 * TWO, 8 * TWO));
             strs[2] = "major version : " + major_version.getValue();
             return strs;
         }
 
         /**
          * access_flags
          *
          * @param hand
          * @return
          */
         public ParseResult parseAccessFlags(int hand) {
             ParseResult result = new ParseResult();
             int access_flag_end = hand + 2 * TWO;
             Type access_flag_type = new Type(code.substring(hand, access_flag_end));
             int acccess_flag = access_flag_type.getDecimalInteger();
             String[] strs = new String[1];
             strs[0] = "flags: " + getAccessFlags(acccess_flag);
             //ʱڳغ,ռֽ.
             result.setHandle(access_flag_end);
             result.setStrs(strs);
             return result;
         }
 
         /**
          *  this_class super_class,interfaces
          *
          * @param hand
          * @return
          */
         public ParseResult parseClassInfo(int hand) {
             ParseResult result = new ParseResult();
             //this_class ռֽ
             int this_class_end = hand + 2 * TWO;
             Type this_class = new Type(code.substring(hand, this_class_end));
             //super_class ռֽ
             int super_class_end = this_class_end + 2 * TWO;
             Type super_class = new Type(code.substring(this_class_end, super_class_end));
             //ӿڼռֽ
             int interface_count_end = super_class_end + 2 * TWO;
             Type interfaces_couont = new Type(code.substring(super_class_end, interface_count_end));
             //ӿ
             Type[] interfaces = null;
             //ǰλ
             int current = interface_count_end;
             //ÿӿռֽ
             if (interfaces_couont.getDecimalInteger() > 0) {
                 interfaces = new Type[interfaces_couont.getDecimalInteger()];
                 for (int i = 0; i < interfaces_couont.getDecimalInteger(); i++) {
                     int end = current + 2 * TWO;
                     interfaces[i] = new Type(code.substring(current, end));
                     current = end;
                 }
             }
             //this_class  super_classдһ. .interfacesдһ
             String[] strs = new String[2];
             strs[0] = "this_class  index:#" + this_class.getDecimalInteger() + BLANK + "super_class index:#" + super_class.getDecimalInteger();
             String interface_msg = "interface_count:" + interfaces_couont.getDecimalInteger() + BLANK;
             if (interfaces != null) {
                 for (Type inter : interfaces) {
                     interface_msg += "#" + inter.getDecimalInteger() + ",";
                 }
                 interface_msg = interface_msg.substring(0, interface_msg.length() - 1);
             }
             strs[1] = interface_msg;
             result.setStrs(strs);
             result.setHandle(current);
             return result;
         }
 
         /**
          * parse field info
          *
          * @param hand
          * @return
          */
         public ParseResult parseFieldInfo(int hand) {
             ParseResult result = new ParseResult();
             //field count ռֽ
             int field_count_end = hand + 2 * TWO;
             Type field_count = new Type(code.substring(hand, field_count_end));
             String[] strs = null;
             int current = field_count_end;
             if (field_count.getDecimalInteger() > 0) {
                 //һԵַΪ
                 strs = new String[field_count.getDecimalInteger() * 2];
                 for (int i = 0; i < field_count.getDecimalInteger(); i++) {
                     //access_flag ռֽ
                     int access_flag_end = current + 2 * TWO;
                     Type access_flag = new Type(code.substring(current, access_flag_end));
                     //name_index ռֽ
                     int name_index_end = access_flag_end + 2 * TWO;
                     Type name_index = new Type(code.substring(access_flag_end, name_index_end));
                     //descriptor_index ռֽ
                     int descriptor_index_end = name_index_end + 2 * TWO;
                     Type descriptor_index = new Type(code.substring(name_index_end, descriptor_index_end));
                     //access_flag name_index descriptor_indexռֽ,ƶ6ֽ
                     current = current + (2 + 2 + 2) * TWO;
                     //濪ʼֶε
                     ParseResult attributeResult = attributeParse.parseFieldAttribute(current);
                     strs[2 * i] = "field  access_flag:" + getAccessFlags(access_flag.getDecimalInteger()) + ",name:"
                             + ConstantPoolParse.constantPoolMap.get(name_index.getDecimalInteger()) + ",descriptor:"
                             + ConstantPoolParse.constantPoolMap.get(descriptor_index.getDecimalInteger());
                     strs[(2 * i) + 1] = " atrributes: " + attributeResult.getStrs().get(0);
                     current = attributeResult.getHandle();
                 }
             }
             if (strs == null || strs.length <= 0) {
                 strs = new String[1];
                 strs[0] = "this class has no field";
             }
             result.setStrs(strs);
             result.setHandle(current);
             return result;
         }
 
         /**
          * parse method info
          *
          * @param hand
          * @return
          */
         public ParseResult parseMethodInfo(int hand) {
             ParseResult result = new ParseResult();
             int method_count_end = hand + 2 * TWO;
             Type method_count = new Type(code.substring(hand, method_count_end));
             int current = method_count_end;
             if (method_count.getDecimalInteger() > 0) {
                 for (int i = 0; i < method_count.getDecimalInteger(); i++) {
                     //access_flag ռֽ
                     int access_flag_end = current + 2 * TWO;
                     Type access_flag = new Type(code.substring(current,access_flag_end));
                     result.addStr("flags:" + getAccessFlags(access_flag.getDecimalInteger()));;
                     int name_index_end = access_flag_end + 2 * TWO;
                     Type name_index = new Type(code.substring(access_flag_end,name_index_end));
                     result.addStr("name:" + ConstantPoolParse.constantPoolMap.get(name_index.getDecimalInteger()));
                     int descriptor_index_end = name_index_end + 2* TWO;
                     Type descriptor_index = new Type(code.substring(name_index_end,descriptor_index_end));
                     result.addStr("descriptor:"+ConstantPoolParse.constantPoolMap.get(descriptor_index.getDecimalInteger()));
                     //access_flag name_index descriptor_indexռֽ,ƶ6ֽ
                     current = current + (2 + 2 + 2) * TWO;
                     ParseResult pr = attributeParse.parseMethodAttribute(current);
                     result.addStrs(pr.getStrs());
                     result.addStr("---------------------");
                     current = pr.getHandle();
                 }
             } else {
                 result.addStr("this class has no method");
             }
             result.setHandle(current);
             return result;
         }
 
         /**
          * class
          *
          * @param hand
          * @return
          */
         public ParseResult parseAttribute(int hand){
             return attributeParse.parseAttribute(hand);
         }
 
         /**
          * Ӧλ.class,field  method
          *
          * @param access_flag
          * @return
          */
         private String getAccessFlags(int access_flag) {
             String flags = "";
             if (Util.hasTag(access_flag, ACC_PUBLIC)) {
                 flags += " ACC_PUBLIC,";
             }
             if (Util.hasTag(access_flag, ACC_PRIVATE)) {
                 flags += " ACC_PRIVATE,";
             }
             if (Util.hasTag(access_flag, ACC_PROTECTED)) {
                 flags += " ACC_PROTECTED,";
             }
             if (Util.hasTag(access_flag, ACC_STATIC)) {
                 flags += " ACC_STATIC,";
             }
             if (Util.hasTag(access_flag, ACC_FINAL)) {
                 flags += " ACC_FINAL,";
             }
             if (Util.hasTag(access_flag, ACC_SUPER)) {
                 flags += " ACC_SUPER,";
             }
             if (Util.hasTag(access_flag, ACC_SYNCHRONIZED)) {
                 flags += " ACC_SYNCHRONIZED,";
             }
             if (Util.hasTag(access_flag, ACC_VOLATILE)) {
                 flags += " ACC_VOLATILE,";
             }
             if (Util.hasTag(access_flag, ACC_BRIDGE)) {
                 flags += " ACC_BRIDGE,";
             }
             if (Util.hasTag(access_flag, ACC_TRANSIENT)) {
                 flags += " ACC_TRANSIENT,";
             }
             if (Util.hasTag(access_flag, ACC_VARARGS)) {
                 flags += " ACC_VARARGS,";
             }
             if (Util.hasTag(access_flag, ACC_NATIVE)) {
                 flags += " ACC_NATIVE,";
             }
             if (Util.hasTag(access_flag, ACC_INTERFACE)) {
                 flags += " ACC_INTERFACE,";
             }
             if (Util.hasTag(access_flag, ACC_ABSTRACT)) {
                 flags += " ACC_ABSTRACT,";
             }
             if (Util.hasTag(access_flag, ACC_STRICT)) {
                 flags += " ACC_STRICT,";
             }
             if (Util.hasTag(access_flag, ACC_SYNTHETIC)) {
                 flags += " ACC_SYNTHETIC,";
             }
             if (Util.hasTag(access_flag, ACC_ANNOTATION)) {
                 flags += " ACC_ANNOTATION,";
             }
             if (Util.hasTag(access_flag, ACC_ENUM)) {
                 flags += " ACC_ENUM,";
             }
             if (Util.hasTag(access_flag, ACC_MODULE)) {
                 flags += " ACC_MODULE,";
             }
            if(flags.length() == 0){
                return "";
            }
             return flags.substring(0, flags.length() - 1);
         }
 
     }
