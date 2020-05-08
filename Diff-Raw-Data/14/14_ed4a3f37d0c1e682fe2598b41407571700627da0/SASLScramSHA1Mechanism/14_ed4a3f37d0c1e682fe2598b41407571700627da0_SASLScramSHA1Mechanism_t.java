 /**
  * Copyright 2011 Glenn Maynard
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack.sasl;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.UUID;
 
 import javax.crypto.Mac;
 
 import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.util.Base64;
 import org.jivesoftware.smack.util.StringPrep;
 
 /**
  * Implementation of the SASL SCRAM-SHA-1 mechanism.
  *
  * http://tools.ietf.org/html/rfc5802
  */
 public class SASLScramSHA1Mechanism extends SASLMechanismType {
     static public class Factory extends SASLMechanismType.Factory {
         public Factory() { super("SCRAM-SHA-1"); }
         public SASLMechanismType create() { return new SASLScramSHA1Mechanism(); }
     }
 
     private final String gs2_header = "n,,";
 
     private CallbackHandler callbackHandler;
     private String clientNonce;
     private String clientFirstMessageBare;
     private byte[] serverSignature;
 
     public SASLScramSHA1Mechanism() { super("SCRAM-SHA-1"); }
 
     /** Perform Hi(str, salt, i). */
     private static byte[] calculateSaltedPassword(byte[] str, byte[] salt, int iterCount) throws XMPPException {
         Mac hmac = SASLHelpers.createMac(str);
 
         // Do U1.
         hmac.update(salt);
         hmac.update("\00\00\00\01".getBytes());
         byte[] result = hmac.doFinal();
 
         /* Do U2 ... Ui. */
         byte[] previous = null;
         for(int i = 1; i < iterCount; ++i) {
             hmac.update(previous != null? previous: result);
             previous = hmac.doFinal();
             result = xorBytes(result, previous);
         }
 
         return result;
     }
 
    private static byte[] stringToBytesUTF8(String data) {
        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

     private static String byteToStringAscii(byte[] data) {
         try {
             return new String(data, "ASCII");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     static class Tokenizer
     {
         String originalString;
         String[] parts;
         int index;
 
         public Tokenizer(String s) {
             originalString = s;
             parts = s.split(",");
             index = 0;
         }
         public String getNext() throws XMPPException {
             if(index >= parts.length)
                 throw new XMPPException("Unexpected end of challenge string: " + originalString);
 
             return parts[index++];
         }
     }
 
     public byte[] authenticate(String username, String host, CallbackHandler cbh) throws XMPPException {
         callbackHandler = cbh;
         clientNonce = UUID.randomUUID().toString();
 
         // Before sending the username to the server, the client SHOULD
         // prepare the username using the "SASLprep" profile [RFC4013] of
         // the "stringprep" algorithm [RFC3454] treating it as a query
         // string.         
         try {
             username = StringPrep.prepAsQueryString(username);
         } catch(StringPrep.StringPrepError e) {
             throw new XMPPException("Invalid username", e);
         }
 
         String escapedUsername = username.replace("=", "=3D").replace(",", "=2C");
 
         clientFirstMessageBare = "n=" + escapedUsername + ",r=" + clientNonce;
 
         String firstMessage = gs2_header + clientFirstMessageBare;
        return stringToBytesUTF8(firstMessage);
     }
 
     static private byte[] xorBytes(byte[] lhs, byte[] rhs) {
         if(lhs.length != rhs.length)
             throw new RuntimeException("Mismatched buffer sizes: " + lhs.length + ", " + rhs.length);
             
         byte[] result = new byte[lhs.length];
         for(int i = 0; i < lhs.length; ++i)
             result[i] = (byte) (lhs[i] ^ rhs[i]);
         return result;
     }
     
     public byte[] challengeReceived(byte[] challenge) throws XMPPException {
         String serverFirstMessage = byteToStringAscii(challenge);
 
         Tokenizer t = new Tokenizer(serverFirstMessage);
         String serverNonce = t.getNext();
         String salt = t.getNext();
         String iterCountValue = t.getNext();
 
         // No mandatory extensions are supported.
         if(serverNonce.startsWith("m"))
             throw new XMPPException("Unexpected SCRAM extension: " + serverFirstMessage);
 
         // Strip "r=" off of the nonce.
         if(!serverNonce.startsWith("r="))
             throw new XMPPException("Unexpected SCRAM string: " + serverFirstMessage);
         serverNonce = serverNonce.substring(2);
         if(!serverNonce.startsWith(clientNonce))
             throw new XMPPException("Server nonce didn't begin with client nonce: " + serverFirstMessage);
 
         // Strip "s=" off of the salt, and decode it.
         if(!salt.startsWith("s="))
             throw new XMPPException("Unexpected SCRAM string: " + serverFirstMessage);
         salt = salt.substring(2);
         byte[] decodedSalt = SASLHelpers.decodeBase64(salt);
 
         // Strip "i=" off of the iteration-count, and parse it.
         if(!iterCountValue.startsWith("i="))
             throw new XMPPException("Unexpected SCRAM string: " + serverFirstMessage);
         iterCountValue = iterCountValue.substring(2);
 
         int iterCount;
         try {
             iterCount = Integer.parseInt(iterCountValue);
         } catch(NumberFormatException e) {
             throw new XMPPException("Couldn't parse iteration count in challenge: " + serverFirstMessage);
         }
 
         if(iterCount > 65536)
             throw new XMPPException("Unreasonably large SCRAM iteration count received");
 
         // Once we've successfully decoded the challenge, prompt the user for the password.
         String password = SASLHelpers.requestPassword(callbackHandler);
 
         // Normalize(str): Apply the SASLprep profile [RFC4013] of the "stringprep" algorithm
         // [...] "str" is treated as a "stored strings"
         try {
             password = StringPrep.prepAsStoredString(password);
         } catch(StringPrep.StringPrepError e) {
             throw new XMPPException("Invalid password", e);
         }
         
         // channel-binding + nonce:
         String clientFinalMessageWithoutProof = 
             "c=" + Base64.encodeBytes(gs2_header.getBytes()) + ",r=" + serverNonce;
 
         /* Perform the steps in rfc5802 sec3. */
        byte[] saltedPassword = calculateSaltedPassword(stringToBytesUTF8(password), decodedSalt, iterCount);
         byte[] clientKey = SASLHelpers.computeHMACSHA1(saltedPassword, "Client Key".getBytes());
         byte[] storedKey = SASLHelpers.computeSHA1(clientKey);
 
         String authMessage =
             clientFirstMessageBare + "," +
             serverFirstMessage + "," +
             clientFinalMessageWithoutProof;
 
         // ClientSignature:
         byte[] clientSignature = SASLHelpers.computeHMACSHA1(storedKey, authMessage.toString().getBytes());
         byte[] clientProof = xorBytes(clientKey, clientSignature);
         byte[] serverKey = SASLHelpers.computeHMACSHA1(saltedPassword, "Server Key".getBytes());
         serverSignature = SASLHelpers.computeHMACSHA1(serverKey, authMessage.toString().getBytes());
 
         String finalMessageWithProof = clientFinalMessageWithoutProof + ",p=" + Base64.encodeBytes(clientProof);
         return finalMessageWithProof.getBytes();
     }
 
     public void successReceived(byte[] finalMessage) throws XMPPException {
         String serverFinalMessage = byteToStringAscii(finalMessage);
 
         Tokenizer t = new Tokenizer(serverFinalMessage);
         String response = t.getNext();
 
         // We shouldn't receive errors here; they should be reported with <failure>.
         if(!response.startsWith("v="))
             throw new XMPPException("Unexpected SCRAM response: " + serverFinalMessage);
         response = response.substring(2);
 
         byte[] receivedServerSignature = SASLHelpers.decodeBase64(response);
         if(!Arrays.equals(serverSignature, receivedServerSignature))
             throw new XMPPException("Received invalid SCRAM server verifier: " + serverFinalMessage);
     }
 }
