 /*******************************************************************************
  * Copyright 2012 Apigee Corporation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.usergrid.security.crypto.command;
 
 import static org.junit.Assert.*;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.UUID;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.shiro.crypto.hash.Md5Hash;
 import org.junit.Test;
 import org.usergrid.persistence.CredentialsInfo;
 import org.usergrid.persistence.entities.User;
 
 /**
  * @author tnine
  *
  */
 public class Sha1HashCommandTest {
   @Test
   public void hashAndAuthCorrect() throws UnsupportedEncodingException, NoSuchAlgorithmException {
     
     String test = "I'm a  test password";
     
     byte[] hashed = digest(test.getBytes("UTF-8"));
     
     Sha1HashCommand command = new  Sha1HashCommand();
     
     CredentialsInfo info = new CredentialsInfo();
     
     
     byte[] results = command.hash(test.getBytes("UTF-8"), info, null, null);
     
     assertArrayEquals(hashed, results);
     
     byte[] authed = command.auth(test.getBytes("UTF-8"), info, null, null);
     
     assertArrayEquals(results, authed);
     
   }
   
   private byte[] digest(byte[] input) throws NoSuchAlgorithmException{
     MessageDigest md = MessageDigest.getInstance("SHA-1"); 
     return md.digest(input);
   }
 }
