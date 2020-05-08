 package org.aldeon.model;
 
 import org.aldeon.crypt.Key;
 import org.aldeon.crypt.KeyGen;
 
 public class Identity implements User {
 
     private Key publicKey;
     private Key privateKey;
     private String name;
 
     private Identity() { }
 
     public static Identity create(final String name, KeyGen keyGen) {
 
         KeyGen.KeyPair pair = keyGen.generate();
         return create(name, pair.publicKey, pair.privateKey);
     }
 
     public static Identity create(final String name, final Key publicKey, final Key privateKey) {
 
         if(publicKey.getType() == Key.Type.PUBLIC && privateKey.getType() == Key.Type.PRIVATE) {
             Identity identity = new Identity();
 
             identity.publicKey = publicKey;
             identity.privateKey = privateKey;
             identity.name = name;
 
             return identity;
         } else {
             throw new IllegalArgumentException("Invalid key types");
         }
     }
 
    Key getPrivateKey() {
         return privateKey;
     }
 
     @Override
     public Key getPublicKey() {
         return publicKey;
     }
 
     @Override
     public String getName() {
         return name;
     }
 }
