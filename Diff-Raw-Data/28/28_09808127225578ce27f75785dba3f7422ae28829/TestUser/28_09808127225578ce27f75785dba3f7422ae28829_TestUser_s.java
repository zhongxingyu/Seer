 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 package org.megatome.app.jaxb;
 
 import java.io.OutputStream;
 
 import junit.framework.TestCase;
 
 import org.megatome.app.jaxbgen.ACK;
 import org.megatome.app.jaxbgen.NACK;
 import org.megatome.app.jaxbgen.User;
 import org.megatome.app.jaxbgen.impl.ACKImpl;
 import org.megatome.app.jaxbgen.impl.NACKImpl;
 import org.megatome.app.jaxbgen.impl.UserImpl;
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.util.Helper;
 
 public class TestUser extends TestCase {
    private final String TARGET_PACKAGE = "org.megatome.app.jaxbgen";
 
    public void testUser() {
 		UserImpl user = makeFred();
 
      Errors errors = Errors.newInstance();
       
       assertTrue(user.validate(errors));
       assertTrue(errors.isEmpty());
       
       user.setEmail("a-boogie-boogie-boogie");
 
       assertFalse(user.validate(errors));
       assertFalse(errors.isEmpty());
       assertEquals(1,errors.size());
    }
 
    public void testAckNack() {
 
    }
 
 	private UserImpl makeFred() {
 		UserImpl user = new UserImpl();
 		
 		user.setFirstName("Fred");
 		user.setLastName("Flintstone");
 		user.setEmail("fred@bedrockcommunity.org");
 		return user;
 	}
 
    private ACKImpl makeACK() {
       ACKImpl ack = new ACKImpl();
       
       ack.setValue("It was a good thing.");
       
       return ack;
    }
 
    private NACKImpl makeNACK() {
       NACKImpl nack = new NACKImpl();
       
       nack.setValue("It was a bad thing.");
       
       return nack;
    }
 
    public void testMarshall() throws Exception {
       User user = makeFred();
       OutputStream os = Helper.marshall(user,TARGET_PACKAGE,getClass().getClassLoader());
       assertNotNull(os);
       assertTrue(os.toString().indexOf("Flintstone") > 0);
 
       ACK ack = makeACK();
       os = Helper.marshall(ack,TARGET_PACKAGE,getClass().getClassLoader());
       assertNotNull(os);      
       assertTrue(os.toString().indexOf("It was a good thing.") > 0);
  
       NACK nack = makeNACK();
       os = Helper.marshall(nack,TARGET_PACKAGE,getClass().getClassLoader());
       assertNotNull(os);        
       assertTrue(os.toString().indexOf("It was a bad thing.") > 0);
    }
 
    public void testUnmarshall() throws Exception  {
       User user = (User) unmarshall("org/megatome/app/jaxb/user.xml");
       assertNotNull(user);
 
       ACK ack = (ACK) unmarshall("org/megatome/app/jaxb/ack.xml");
       assertNotNull(ack);
 
       NACK nack = (NACK) unmarshall("org/megatome/app/jaxb/nack.xml");
       assertNotNull(nack);
    }
 
    private Object unmarshall(String path) throws Exception {
       return Helper.unmarshall(path,TARGET_PACKAGE,getClass().getClassLoader());
    }
 }
