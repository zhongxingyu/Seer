 package fi.haju.haju3d.client.util;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 
 import com.google.common.collect.Maps;
 
 public class ProfileInterceptor implements MethodInterceptor {
 
   private static class MethodProfile {
     public MethodProfile(String method, long time) {
       this.method = method;
       this.time = time;
     }
     public String method;
     public long calls;
     public long time;
     
     @Override
     public String toString() {
       return method + "\t" + (time / calls) + "\t" + calls + "\t" + time;
     }
   }
       
   private static final long SAVE_INTERVAL = 30000; 
   
   private long lastSaveTime = System.currentTimeMillis();
   
   private Map<String,MethodProfile> profiles = Maps.newHashMap();
 
   public Object invoke(final MethodInvocation invocation) throws Throwable {
     String method = invocation.getMethod().getName();
     long start = System.currentTimeMillis();
     Object result = invocation.proceed();
     long end = System.currentTimeMillis();
     if(profiles.containsKey(method)) {
       MethodProfile prof = profiles.get(method);
       prof.calls++;
       prof.time += end - start;
     } else {
       profiles.put(method, new MethodProfile(method, end - start));
     }
     if(System.currentTimeMillis() > lastSaveTime + SAVE_INTERVAL) {
       saveProfiles();
       lastSaveTime = System.currentTimeMillis();
     }
     return result;
   }
 
   private void saveProfiles() {
     try {
       FileWriter writer = new FileWriter("profiling.txt");
       for(MethodProfile profile : profiles.values()) {
         writer.write(profile.toString() + "\n");
       }
       writer.close();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
 }
