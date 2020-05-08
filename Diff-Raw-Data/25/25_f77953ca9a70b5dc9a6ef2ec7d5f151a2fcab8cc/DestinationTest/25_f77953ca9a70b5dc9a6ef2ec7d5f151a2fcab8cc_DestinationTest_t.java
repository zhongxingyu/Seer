 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave;
 
 import javax.servlet.ServletContext;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class DestinationTest {
     
     Mockery context = new Mockery();
     ServletContext servletContext;
     
     @Before
     public void setup() throws Exception {
         servletContext = context.mock(ServletContext.class);
     }
 
     @Test
     public void testConstructorWithOnlyPathArgument() throws Exception {
         Destination dest = new Destination("/some/resource");
         Assert.assertNotNull(dest);
         Assert.assertNull(dest.getRedirect());
     }
     
     @Test
     public void testConstructorWithPathAndRedirectArguments() throws Exception {
         Destination dest = new Destination("/some/resource", true);
         Assert.assertNotNull(dest);
         Assert.assertNotNull(dest.getRedirect());
         Assert.assertTrue(dest.getRedirect());
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorWithInvalidPath() throws Exception {
         Destination dest = new Destination("some/resource");
         Assert.assertNotNull(dest);
     }
     
     @Test
     public void testAddingParameters() throws Exception {
         Destination dest = new Destination("/some/resource");
         dest.addParameter("dog", "woof");
         dest.addParameter("dog", "bark");
         dest.addParameter("cat", "meow");
         dest.addParameter("cat", "purr");
         
         Assert.assertNotNull(dest.getParameters());
         Assert.assertNotNull(dest.getParameters().get("dog"));
         Assert.assertNotNull(dest.getParameters().get("cat"));
         Assert.assertTrue(dest.getParameters().get("dog").contains("woof"));
         Assert.assertTrue(dest.getParameters().get("dog").contains("bark"));
         Assert.assertTrue(dest.getParameters().get("cat").contains("meow"));
         Assert.assertTrue(dest.getParameters().get("cat").contains("purr"));
     }
     
     @Test
     public void testEncode() throws Exception { 
         Destination dest = new Destination("/some/resource");
         dest.addParameter("dog", "woof");
         dest.addParameter("dog", "bark");
         dest.addParameter("cat", "meow");
         dest.addParameter("cat", "purr");
         
         context.checking(new Expectations() {{
             allowing(servletContext).getContextPath(); will(returnValue("/app"));
         }});
         
        Assert.assertEquals("/some/resource?cat=meow&cat=purr&dog=bark&dog=woof", dest.encode(servletContext));
         dest.addParameter("bird", "chirp&chirp");
        Assert.assertEquals("/some/resource?bird=chirp&amp;chirp&cat=meow&cat=purr&dog=bark&dog=woof", dest.encode(servletContext));
     }
     
 }
