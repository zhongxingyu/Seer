 package misc;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import communication.Messages;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Scanner;
 
 import static communication.Messages.ProcessIdentifier;
 
 public class MiscTool {
 
     public static final Integer BUFFER_SIZE = 4096;
 
     private static Logger logger = Logger.getLogger(MiscTool.class);
 
     public static void callStaticMethod(Class className, String methodName) {
         Method method = null;
         try {
             method = className.getDeclaredMethod(methodName);
             method.setAccessible(true);
             method.invoke(null);    //static method
             method.setAccessible(false);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
     }
 
     public static String inputFileName(Scanner in) {
         System.out.print("Input the filename: ");
         return in.nextLine();
     }
 
     public static String inputSDFSRoot(Scanner in) {
         System.out.print("Input the root: ");
         return in.nextLine();
     }
 
     public static Integer inputPortNumber(Scanner in) {
         String str;
         int ret;
         while(true) {
             try {
                 System.out.print("Input the port: ");
                 str = in.nextLine();
                 ret = Integer.parseInt(str);
                 break;
             } catch (NumberFormatException ex) {
                 System.out.println("Number format error");
             }
         }
         return ret;
     }
 
     public static String inputAddress(Scanner in) {
         String str;
         while(true) {
             System.out.print("Input the address:");
             str = in.nextLine();
             if(MiscTool.isIPAddress(str)) {
                 break;
             }
         }
         return str;
     }
 
     public static boolean isIPAddress(String str) {
         String[] res = str.split(":");
         if(res.length!=2) {
             return false;
         }
 
         String ip;
         Integer port;
         try{
             port = Integer.parseInt(res[1]);
         } catch(NumberFormatException ex) {
             return false;
         }
 
         if(port<=0 || port >=65536) {
             return false;
         }
 
         ip = res[0];
 
         String regex;
 
         regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                     + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                     + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                     + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$" ;
         return ip.matches(regex);
     }
 
     public static void readFromInputStreamToOutputStream(InputStream is, OutputStream os) throws IOException {
         byte [] buffer = new byte[8096*4];
         int c;
         while((c = is.read(buffer)) != -1 ) {
             os.write(buffer, 0, c);
         }
         is.close();
         os.close();
     }
 
     public static void main(String[] args) throws InvalidProtocolBufferException {
         ProcessIdentifier identifier = ProcessIdentifier.newBuilder()
                 .setId("1").setIP("127.0.0.1").setPort(1234).build();
         Messages.JoinMessage joinMessage = Messages.JoinMessage.newBuilder().setJoinedMachine(identifier).build();
         Messages.Message m1 = Messages.Message.newBuilder().
                 setType(Messages.MessageType.Join).setJoinMessage(joinMessage).build();
 
         Messages.Message m2 = Messages.Message.parseFrom(m1.toByteArray());
         System.out.println(m2.toString());
     }
 
     public static boolean isTheSameIdentifier(ProcessIdentifier p1, ProcessIdentifier p2) {
         return p1.getId().equals(p2.getId());
     }
 }
