 package com.bastengao.struts2.freeroute;
 
import com.google.common.collect.Sets;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.regex.Pattern;
 
 /**
  * @author bastengao
  * @date 12-12-22 22:50
  */
 public class DefaultRouteMappingHandlerTest {
     @Test
    public void testPatternEquals() {
         Pattern a = Pattern.compile("abc");
         Pattern b = Pattern.compile("abc");
 
         Assert.assertNotEquals(a, b);
     }
 
    @Test
    public void testPatternEquals2() {
        Pattern a = Pattern.compile("abc");
        Pattern b = Pattern.compile("abc");

        Assert.assertEquals(2, Sets.newHashSet(a, b).size());

        Assert.assertEquals(1, Sets.newHashSet("abc", "abc").size());
    }

 }
