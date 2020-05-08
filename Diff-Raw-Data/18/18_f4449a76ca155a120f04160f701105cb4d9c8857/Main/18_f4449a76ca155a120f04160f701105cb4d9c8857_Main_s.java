 package zhenghui.jvm;
 
 import zhenghui.jvm.parse.BaseInfoParse;
 import zhenghui.jvm.parse.ConstantPoolParse;
 import zhenghui.jvm.parse.ParseResult;
 
 /**
  *
  * User: zhenghui
  * Date: 13-1-10
  * Time: 2:19
  *
  */
 public class Main {
 
     public static void main(String[] args) throws Exception {
         ClassLoader cl = new ClassLoader();
        String code = cl.loadClass("d:\\Test.class");
         //Ϣ
         BaseInfoParse baseInfoParse = new BaseInfoParse(code);
         for(String str : baseInfoParse.parseBaseInfo()){
             System.out.println(str);
         }
         System.out.println("------------constant pool start-------------");
         //
         ConstantPoolParse constantPoolParse = new ConstantPoolParse(code);
         ParseResult result = constantPoolParse.pareContantPool();
         for(String str : result.getStrs()){
             System.out.println(str);
         }
         System.out.println("-----------access_flag--------");
         //ʱ
         result = baseInfoParse.parseAccessFlags(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
         System.out.println("----------this_class superc_class interfaces--------");
         result = baseInfoParse.parseClassInfo(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
         System.out.println("-------field info -----");
         result = baseInfoParse.parseFieldInfo(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
         System.out.println("------method info----------");
         result = baseInfoParse.parseMethodInfo(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
         System.out.println("------attributes info----------");
         result = baseInfoParse.parseAttribute(result.getHandle());
         for(String str : result.getStrs()){
             System.out.println(str);
         }
     }
 }
