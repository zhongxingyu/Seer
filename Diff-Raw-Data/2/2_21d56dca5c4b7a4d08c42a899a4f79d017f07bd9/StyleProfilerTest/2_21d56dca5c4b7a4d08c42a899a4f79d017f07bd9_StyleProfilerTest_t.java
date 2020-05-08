 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.wedlum.styleprofile.util.web.JsonUtils;
 
 
 public class StyleProfilerTest {
 
     @Test
     public void testResolve() throws Exception {
         Profile profile = new Profile();
         profile.addSession("singleColorSession1", Arrays.asList("1a.png","2a.png"));
         profile.addSession("singleColorSession2", Arrays.asList("1b.png"));
 
         StyleProfiler subject = new StyleProfiler(profile);
         Map<String, String> resolved  = subject.resolveAll();
 
        Assert.assertEquals("{\"miniPalette1\":\"1a_A.png\",\"miniPalette2\":\"2a_A.png\",\"miniPalette3\":\"1b_A.png\"}", JsonUtils.toJson(resolved));
     }
 }
