 import java.lang.invoke.MethodHandle;
 import java.lang.invoke.MethodHandles;
 import java.lang.invoke.MethodType;
 
 
 /**
  * <p>Main. </p>
  *
  * @author anavarro - Apr 3, 2013
  *
  */
 public class Main {
 
     
    public static void main(String[] args) {
         try {
             final MethodHandle methodHandle = MethodHandles.lookup().findConstructor(String.class, MethodType.methodType(void.class, String.class));
             String s = (String) methodHandle.invoke("a");
             System.out.println("methodHandle.type().returnType()=" + methodHandle.type().returnType());
             System.out.println("s=" + s);
             
             final MethodHandle methodHandle2 = MethodHandles.lookup().findStatic(Main.class, "create", MethodType.methodType(String.class));
             String s2 = (String) methodHandle2.invoke();
             System.out.println("methodHandle2.type().returnType()=" + methodHandle2.type().returnType());
            System.out.println("s=" + s2);
             
             Class.forName("java.lang.String");
             
             
            
         } catch (NoSuchMethodException e) {
             System.out.println("e=" + e);
         } catch (IllegalAccessException e) {
             System.out.println("e=" + e);
         } catch (Throwable e) {
 
             System.out.println("e=" + e);
             
         }
     }
     
     
     public static String create() {
        return "aaa";
     }
 }
