 import java.io.FileOutputStream;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import org.apache.solr.common.util.JavaBinCodec;
 
 public class MakeData {
     public static void main(String[] args) {
         try {
             new JavaBinCodec().marshal(new HashMap<String, Object>(){{
                 put("array", new String[]{"foo", "bar", "baz", "qux"});
                 put("byte", (byte)127);
                 put("byte_array", new byte[]{-128, 0, 127});
                 put("byte_neg", (byte)-128);
                 put("date", new Date(613_180_800_000L));
                 put("double", 1.797_693_134_862_31e308);
                 put("iterator", Arrays.asList(new String[]{"qux", "baz", "bar", "foo"}).iterator());
                 put("false", false);
                 put("float", 3.402_823_466_385_29e+38f);
                 put("shifted_sint", 2_147_483_647);
                put("long", 9_223_372_036_854_775_807L);
                put("long_neg", -9_223_372_036_854_775_808L);
                 put("null", null);
                 put("pangram", "The quick brown fox jumped over the lazy dog");
                 put("short", (short)32_767);
                 put("short_neg", (short)-32_768);
                 put("snowman", "☃");
                 put("true", true);
             }}, new FileOutputStream("data"));
         }
         catch (Exception e){}
     }
 }
