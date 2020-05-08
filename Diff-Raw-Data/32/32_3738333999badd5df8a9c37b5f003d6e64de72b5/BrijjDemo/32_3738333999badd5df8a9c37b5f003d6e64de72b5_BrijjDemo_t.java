 package net.r0kit.brijj.demo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.r0kit.brijj.BrijjServlet;
 import net.r0kit.brijj.RemoteRequestProxy;
 import org.apache.catalina.Context;
 import org.apache.catalina.LifecycleException;
 import org.apache.catalina.Wrapper;
 import org.apache.catalina.servlets.DefaultServlet;
 import org.apache.catalina.startup.Tomcat;
 
 public class BrijjDemo {
   
   public static void main(String[] args) 
       throws IOException, ServletException, LifecycleException, InterruptedException {
 
       final Tomcat tomcat = new Tomcat();
       tomcat.setPort(8260);
              
       RemoteRequestProxy.register(Demo.class, UploadDownload.class, TestTypes.class);
       
       Context c2 = tomcat.addContext("/demo", BrijjDemo.class.getResource(".").getFile());
       Wrapper w2 = Tomcat.addServlet(c2,"default", new DefaultServlet());
       c2.addServletMapping("/*", "default");
       w2.addInitParameter("listings", "true");
 
       Context ctx = tomcat.addContext("/", new File("..").getAbsolutePath());
       Tomcat.addServlet(ctx, "brijj", new BrijjServlet());
       ctx.setAllowCasualMultipartParsing(true);
       ctx.addServletMapping("/*", "brijj");
 
       tomcat.enableNaming();
       tomcat.start();
       tomcat.getServer().await();
   }
   
 
   public static class Demo extends RemoteRequestProxy {
     public Demo(HttpServletRequest r, HttpServletResponse s) {
       super(r, s);
     }
     
    public void logError(String mth, Exception ex) {
      System.out.println(mth+": "+ex);
     }
     
     @Documentation(text="Supply the name of a person who will be greeted when the button is pressed")
     public String sayHello(@Eg(value="Joe") String name) {
       return "<span style=\"color: rgb(204,153,204);\">Hello, <b style=\"background: rgb(153,204,153); color: rgb(104,13,104); padding: 6px; \">" + name+"</b></span>";
     }
     public String[] getInsert() {
       return new String[] { "<p><strong style=color:red;>BriJJ is bridging!</strong></p>", "1.3.1" };
     }
     public String getValue(String k) {
       return System.getProperty(k);
     }
     public Set<Object> getKeys() {
       return System.getProperties().keySet();
     }
     @SuppressWarnings("unused") private String hidden() {
       return "This should not appear in the list of available methods";
     }
     protected String alsoHidden() {
       return "This should also not appear in the list of available methods";
     }
     public static String likewiseHidden() {
       return "Static methods like this one should not appear in the list of available methods";
     }
     public Map<String,String> getMap(String s) {
       HashMap<String, String> res = new HashMap<String, String>();
       Set<Object> so = System.getProperties().keySet();
       for(Object k : so) {
         String kk = (String)k;
         if ( kk.toLowerCase().contains(s.toLowerCase())) {
           res.put(kk, (String)System.getProperty(kk));
         }
       }
       return res;
     }
     @Documentation(text="This just prefixes a constant string to the supplied value")
     public String getError(@Eg(value="bogus") String s) throws IOException {
       throw new IOException("invalid argument: " + s);
     }
   }
   public static class TestTypes extends RemoteRequestProxy {
     public TestTypes(HttpServletRequest q, HttpServletResponse p) {
       super(q, p);
     }
     public int getInt() {
       return 1000000000;
     }
    public void logError(String mth, Exception ex) {
      System.out.println(mth+": "+ex);
     }
     public double getDouble() {
       return 1000;
     }
     public int[] getIntArray() {
       return new int[] { 3, 6, 9, 101, 4324324 };
     }
     public double[] getDoubleArray() {
       return new double[] { 3, 6, 9, 101, 434343 };
     }
     public int sumArray(int[] a) {
       int b = 0;
       for (int c : a)
         b += c;
       return b;
     }
     public double productArray(double[] a) {
       double b = 1;
       for (double c : a)
         b *= c;
       return b;
     }
     public double crossProduct(int[] a, double[] b) {
       double res = 0;
       if (a.length != b.length) throw new IllegalArgumentException();
       for (int i = 0; i < a.length; i++)
         res += a[i] * b[i];
       return res;
     }
     public double productReals(double[] ds) {
       double res = 1;
       for (double c : ds)
         res *= c;
       return res;
     }
   }
 }
