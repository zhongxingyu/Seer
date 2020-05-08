 package com.pivotal.hamster.appmaster.common;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.pivotal.hamster.common.HostExprParser;
 
 public class HostExprParserTest {
   void verifyParseResult(String result, String[] actual) {
     Set<String> set = new HashSet<String>();
     for (String host : actual) {
       set.add(host);
     }
     for (String slice : result.split(",")) {
       slice = slice.trim();
       if (slice.isEmpty()) {
         continue;
       }
       if (set.contains(slice)) {
         set.remove(slice);
       } else {
         Assert.fail("don't contain slice : " + slice);
       }
     }
     if (!set.isEmpty()) {
       Assert.fail("not all word contained in result");
     }
   }
   
   void failIfParseFinished(String expr) {
     try {
       HostExprParser.parse(expr);
     } catch (Exception e) {
       return;
     }
     Assert.fail("parse should failed, expr:" + expr);
   }
   
   @Test
   public void testHostExprParser() throws IOException {
     String result;
     
     result = HostExprParser.parse("host1.[9-11].com");
     verifyParseResult(result, new String[] {"host1.9.com", "host1.10.com", "host1.11.com"});
     
     result = HostExprParser.parse("host1.[09-11].com");
     verifyParseResult(result, new String[] {"host1.09.com", "host1.10.com", "host1.11.com"});
    
     result = HostExprParser.parse("host.[1-2,a,b].com");
     verifyParseResult(result, new String[] {"host.1.com", "host.2.com", "host.a.com", "host.b.com"});
     
     result = HostExprParser.parse("host.[1-2].[a,b].com");
     verifyParseResult(result, new String[] {"host.1.a.com", "host.2.a.com", "host.1.b.com", "host.2.b.com"});
     
     result = HostExprParser.parse("host1.[009-011].com");
     verifyParseResult(result, new String[] {"host1.009.com", "host1.010.com", "host1.011.com"});
   }
   
   @Test
   public void testHostExprFail() throws IOException {
     failIfParseFinished("].xxx.com.[1-2]");
     failIfParseFinished("www.][.com");
     failIfParseFinished("www.[1-2,[3-4]]");
     failIfParseFinished("www.[2-1].com");
     failIfParseFinished("www.[01-2].com");
     failIfParseFinished("www.[01-002].com");
   }
 }
