 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2006, 2007 Ola Bini <ola@ologix.com>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 package org.jruby.ext.openssl;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.crypto.spec.IvParameterSpec;
 
 import org.jruby.Ruby;
 import org.jruby.RubyClass;
 import org.jruby.RubyModule;
 import org.jruby.RubyNumeric;
 import org.jruby.RubyObject;
 import org.jruby.RubyString;
import org.jruby.common.IRubyWarnings;
 import org.jruby.exceptions.RaiseException;
 import org.jruby.runtime.Block;
 import org.jruby.runtime.CallbackFactory;
 import org.jruby.runtime.ObjectAllocator;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.util.ByteList;
 
 /**
  * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a>
  */
 public class Cipher extends RubyObject {
 
     // set to enable debug output
     private static final boolean DEBUG = false;
     private static ObjectAllocator CIPHER_ALLOCATOR = new ObjectAllocator() {
         public IRubyObject allocate(Ruby runtime, RubyClass klass) {
             return new Cipher(runtime, klass);
         }
     };
     
     public static void createCipher(Ruby runtime, RubyModule ossl) {
         RubyModule mCipher = ossl.defineModuleUnder("Cipher");
         RubyClass cCipher = mCipher.defineClassUnder("Cipher",runtime.getObject(), CIPHER_ALLOCATOR);
 
         RubyClass openSSLError = ossl.getClass("OpenSSLError");
         mCipher.defineClassUnder("CipherError",openSSLError,openSSLError.getAllocator());
 
         CallbackFactory ciphercb = runtime.callbackFactory(Cipher.class);
 
         mCipher.getMetaClass().defineFastMethod("ciphers",ciphercb.getFastSingletonMethod("ciphers"));
         cCipher.defineMethod("initialize",ciphercb.getMethod("initialize",IRubyObject.class));
         cCipher.defineFastMethod("initialize_copy",ciphercb.getFastMethod("initialize_copy",IRubyObject.class));
         cCipher.defineFastMethod("name",ciphercb.getFastMethod("name"));
         cCipher.defineFastMethod("key_len",ciphercb.getFastMethod("key_len"));
         cCipher.defineFastMethod("key_len=",ciphercb.getFastMethod("set_key_len",IRubyObject.class));
         cCipher.defineFastMethod("iv_len",ciphercb.getFastMethod("iv_len"));
         cCipher.defineFastMethod("block_size",ciphercb.getFastMethod("block_size"));
         cCipher.defineFastMethod("encrypt",ciphercb.getFastOptMethod("encrypt"));
         cCipher.defineFastMethod("decrypt",ciphercb.getFastOptMethod("decrypt"));
         cCipher.defineFastMethod("key=",ciphercb.getFastMethod("set_key",IRubyObject.class));
         cCipher.defineFastMethod("iv=",ciphercb.getFastMethod("set_iv",IRubyObject.class));
         cCipher.defineFastMethod("reset",ciphercb.getFastMethod("reset"));
         cCipher.defineFastMethod("pkcs5_keyivgen",ciphercb.getFastOptMethod("pkcs5_keyivgen"));
         cCipher.defineFastMethod("update",ciphercb.getFastMethod("update",IRubyObject.class));
         cCipher.defineFastMethod("<<",ciphercb.getFastMethod("update_deprecated",IRubyObject.class));
         cCipher.defineFastMethod("final",ciphercb.getFastMethod("_final"));
         cCipher.defineFastMethod("padding=",ciphercb.getFastMethod("set_padding",IRubyObject.class));
     }
 
     private static final Set BLOCK_MODES = new HashSet();
     static {
         BLOCK_MODES.add("CBC");
         BLOCK_MODES.add("CFB");
         BLOCK_MODES.add("CFB1");
         BLOCK_MODES.add("CFB8");
         BLOCK_MODES.add("ECB");
         BLOCK_MODES.add("OFB");
     }
 
     private static String[] rubyToJavaCipher(String inName, String padding) {
         String[] split = inName.split("-");
         String cryptoBase = split[0];
         String cryptoVersion = null;
         String cryptoMode = null;
         String realName = null;
 
         String padding_type;
         if (padding == null || padding.equalsIgnoreCase("PKCS5Padding")) {
             padding_type = "PKCS5Padding";
         } else if (padding.equals("0") || padding.equalsIgnoreCase("NoPadding")) {
             padding_type = "NoPadding";
         } else if (padding.equalsIgnoreCase("ISO10126Padding")) {
             padding_type = "ISO10126Padding";
         } else {
             padding_type = "PKCS5Padding";
         }
 
         if("bf".equalsIgnoreCase(cryptoBase)) {
             cryptoBase = "Blowfish";
         }
 
         if(split.length == 3) {
             cryptoVersion = split[1];
             cryptoMode = split[2];
         } else {
             if(split.length == 2) {
                 cryptoMode = split[1];
             } else {
                 cryptoMode = "ECB";
             }
         }
 
         if(cryptoBase.equalsIgnoreCase("DES") && "EDE3".equalsIgnoreCase(cryptoVersion)) {
             realName = "DESede";
         } else {
             realName = cryptoBase;
         }
 
         if(!BLOCK_MODES.contains(cryptoMode.toUpperCase())) {
             cryptoVersion = cryptoMode;
             cryptoMode = "CBC";
         }
 
         realName = realName + "/" + cryptoMode + "/" + padding_type;
 
         return new String[]{cryptoBase,cryptoVersion,cryptoMode,realName,padding_type};
     }
 
     private static boolean tryCipher(final String rubyName) {
         return ((Boolean) (OpenSSLReal.getWithBCProvider(new Callable() {
             public Object call() {
                 try {
                     javax.crypto.Cipher.getInstance(rubyToJavaCipher(rubyName, null)[3], OpenSSLReal.PROVIDER);
                     return Boolean.TRUE;
                 } catch (Exception e) {
                     return Boolean.FALSE;
                 }
 
             }
         }))).booleanValue();
     }
 
     public static IRubyObject ciphers(IRubyObject recv) {
         List ciphers = new ArrayList();
         String[] other = {"AES128","AES192","AES256","BLOWFISH", "RC2-40-CBC", "RC2-64-CBC","RC4","RC4-40", "CAST","CAST-CBC"};
         String[] bases = {"AES-128","AES-192","AES-256","BF", "DES", "DES-EDE","DES-EDE3", "RC2","CAST5"};
         String[] suffixes = {"","-CBC","-CFB","-CFB1","-CFB8","-ECB","-OFB"};
         for(int i=0,j=bases.length;i<j;i++) {
             for(int k=0,l=suffixes.length;k<l;k++) {
                 String val = bases[i]+suffixes[k];
                 if(tryCipher(val)) {
                     ciphers.add(recv.getRuntime().newString(val));
                     ciphers.add(recv.getRuntime().newString((val).toLowerCase()));
                 }
             }
         }
         for(int i=0,j=other.length;i<j;i++) {
             if(tryCipher(other[i])) {
                 ciphers.add(recv.getRuntime().newString(other[i]));
                 ciphers.add(recv.getRuntime().newString(other[i].toLowerCase()));
             }
         }
         return recv.getRuntime().newArray(ciphers);
     }
 
     private RubyClass ciphErr;
     public Cipher(Ruby runtime, RubyClass type) {
         super(runtime,type);
         ciphErr = (RubyClass)(((RubyModule)(getRuntime().getModule("OpenSSL").getConstant("Cipher"))).getConstant("CipherError"));
     }
 
     private javax.crypto.Cipher ciph;
     private String name;
     private String cryptoBase;
     private String cryptoVersion;
     private String cryptoMode;
     private String padding_type;
     private String realName;
     private int keyLen = -1;
     private int ivLen = -1;
     private boolean encryptMode = true;
     //private IRubyObject[] modeParams;
     private boolean ciphInited = false;
     private byte[] key;
     private byte[] iv;
     private String padding;
     
     private void dumpVars() {
         System.out.println("***** Cipher instance vars ****");
         System.out.println("name = " + name);
         System.out.println("cryptoBase = " + cryptoBase);
         System.out.println("cryptoVersion = " + cryptoVersion);
         System.out.println("cryptoMode = " + cryptoMode);
         System.out.println("padding_type = " + padding_type);
         System.out.println("realName = " + realName);
         System.out.println("keyLen = " + keyLen);
         System.out.println("ivLen = " + ivLen);
         System.out.println("ciph block size = " + ciph.getBlockSize());
         System.out.println("encryptMode = " + encryptMode);
         System.out.println("ciphInited = " + ciphInited);
         System.out.println("key.length = " + (key == null ? 0 : key.length));
         System.out.println("iv.length = " + (iv == null ? 0 : iv.length));
         System.out.println("padding = " + padding);
         System.out.println("*******************************");
     }
 
     public IRubyObject initialize(IRubyObject str, Block unusedBlock) {
         name = str.toString();
         String[] values = rubyToJavaCipher(name, padding);
         cryptoBase = values[0];
         cryptoVersion = values[1];
         cryptoMode = values[2];
         realName = values[3];
         padding_type = values[4];
         ciph = getCipher();
 
         if(hasLen() && null != cryptoVersion) {
             try {
                 keyLen = Integer.parseInt(cryptoVersion) / 8;
             } catch(NumberFormatException e) {
                 keyLen = -1;
             }
         }
         if(keyLen == -1) {
             if("DES".equalsIgnoreCase(cryptoBase)) {
                 ivLen = 8;
                 if("EDE3".equalsIgnoreCase(cryptoVersion)) {
                     keyLen = 24;
                 } else {
                     keyLen = 8;
                 }
             } else {
                 keyLen = 16;
             }
         }
 
         if(ivLen == -1) {
             if("AES".equalsIgnoreCase(cryptoBase)) {
                 ivLen = 16;
             } else {
                 ivLen = 8;
             }
         }
 
         return this;
     }
 
     public IRubyObject initialize_copy(IRubyObject obj) {
         if(this == obj) {
             return this;
         }
 
         checkFrozen();
 
         cryptoBase = ((Cipher)obj).cryptoBase;
         cryptoVersion = ((Cipher)obj).cryptoVersion;
         cryptoMode = ((Cipher)obj).cryptoMode;
         padding_type = ((Cipher)obj).padding_type;
         realName = ((Cipher)obj).realName;
         name = ((Cipher)obj).name;
         keyLen = ((Cipher)obj).keyLen;
         ivLen = ((Cipher)obj).ivLen;
         encryptMode = ((Cipher)obj).encryptMode;
         ciphInited = false;
         if(((Cipher)obj).key != null) {
             key = new byte[((Cipher)obj).key.length];
             System.arraycopy(((Cipher)obj).key,0,key,0,key.length);
         } else {
             key = null;
         }
         if(((Cipher)obj).iv != null) {
             iv = new byte[((Cipher)obj).iv.length];
             System.arraycopy(((Cipher)obj).iv,0,iv,0,iv.length);
         } else {
             iv = null;
         }
         padding = ((Cipher)obj).padding;
 
         ciph = getCipher();
 
         return this;
     }
 
     public IRubyObject name() {
         return getRuntime().newString(name);
     }
 
     public IRubyObject key_len() {
         return getRuntime().newFixnum(keyLen);
     }
 
     public IRubyObject iv_len() {
         return getRuntime().newFixnum(ivLen);
     }
 
     public IRubyObject set_key_len(IRubyObject len) {
         this.keyLen = RubyNumeric.fix2int(len);
         return len;
     }
 
     public IRubyObject set_key(IRubyObject key) {
         byte[] keyBytes;
         try {
             keyBytes = key.convertToString().getBytes();
         } catch(Exception e) {
             e.printStackTrace();
             throw new RaiseException(getRuntime(), ciphErr, null, true);
         }
         if(keyBytes.length < keyLen) {
             throw new RaiseException(getRuntime(), ciphErr, "key length to short", true);
         }

        if(keyBytes.length > keyLen) {
            byte[] keys = new byte[keyLen];
            System.arraycopy(keyBytes, 0, keys, 0, keyLen);
            keyBytes = keys;
        }

         this.key = keyBytes;
         return key;
     }
 
     public IRubyObject set_iv(IRubyObject iv) {
         byte[] ivBytes;
         try {
             ivBytes = iv.convertToString().getBytes();
         } catch(Exception e) {
             e.printStackTrace();
             throw new RaiseException(getRuntime(), ciphErr, null, true);
         }
         if(ivBytes.length < ivLen) {
             throw new RaiseException(getRuntime(), ciphErr, "iv length to short", true);
         }
         this.iv = ivBytes;
         return iv;
     }
 
     public IRubyObject block_size() {
         return getRuntime().newFixnum(ciph.getBlockSize());
     }
 
     public IRubyObject encrypt(IRubyObject[] args) {
         //TODO: implement backwards compat
         org.jruby.runtime.Arity.checkArgumentCount(getRuntime(),args,0,2);
         encryptMode = true;
         //modeParams = args;
         ciphInited = false;
         return this;
     }
 
     public IRubyObject decrypt(IRubyObject[] args) {
         //TODO: implement backwards compat
         org.jruby.runtime.Arity.checkArgumentCount(getRuntime(),args,0,2);
         encryptMode = false;
         //modeParams = args;
         ciphInited = false;
         return this;
     }
 
     public IRubyObject reset() {
         doInitialize();
         return this;
     }
 
     private javax.crypto.Cipher getCipher() {
         return (javax.crypto.Cipher) OpenSSLReal.getWithBCProvider(new Callable() {
             public Object call() {
                 try {
                     return javax.crypto.Cipher.getInstance(realName, "BC");
                 } catch (NoSuchAlgorithmException e) {
                     throw getRuntime().newLoadError("unsupported cipher algorithm (" + realName + ")");
                 } catch (NoSuchProviderException e) {
                     throw getRuntime().newLoadError("unsupported cipher algorithm (" + realName + ")");
                 } catch (javax.crypto.NoSuchPaddingException e) {
                     throw getRuntime().newLoadError("unsupported cipher padding (" + realName + ")");
                 }
             }
         });
     }
 
     private boolean hasLen() {
         return hasLen(this.cryptoBase);
     }
 
     private static boolean hasLen(String cryptoBase) {
         return "AES".equalsIgnoreCase(cryptoBase) || "RC2".equalsIgnoreCase(cryptoBase) || "RC4".equalsIgnoreCase(cryptoBase);
     }
 
     public IRubyObject pkcs5_keyivgen(IRubyObject[] args) {
         org.jruby.runtime.Arity.checkArgumentCount(getRuntime(),args,1,4);
         byte[] pass = args[0].convertToString().getBytes();
         byte[] salt = null;
         int iter = 2048;
         IRubyObject vdigest = getRuntime().getNil();
         MessageDigest digest = null;
         if(args.length>1) {
             if(!args[1].isNil()) {
                 salt = args[1].convertToString().getBytes();;
             }
             if(args.length>2) {
                 if(!args[2].isNil()) {
                     iter = RubyNumeric.fix2int(args[2]);
                 }
                 if(args.length>3) {
                     vdigest = args[3];
                 }
             }
         }
         if (null != salt) {
             if (salt.length != 8) {
                 throw new RaiseException(getRuntime(), ciphErr, "salt must be an 8-octet string", true);
             }
         }
 
         final String algorithm = vdigest.isNil() ? "MD5" : ((Digest) vdigest).getAlgorithm();
 
         digest = (MessageDigest) OpenSSLReal.getWithBCProvider(new Callable() {
             public Object call() {
                 try {
                     return MessageDigest.getInstance(algorithm, "BC");
                 } catch (Exception e) {
                     throw new RaiseException(getRuntime(), ciphErr, e.getMessage(), true);
                 }
             }
         });
 
         OpenSSLImpl.KeyAndIv result = OpenSSLImpl.EVP_BytesToKey(keyLen, ivLen, digest, salt, pass, iter);
         this.key = result.getKey();
         this.iv = result.getIv();
 
         doInitialize();
 
         return getRuntime().getNil();
     }
 
     private void doInitialize() {
 
         if (DEBUG) System.out.println("*** doInitialize");
         if (DEBUG) dumpVars();
 
         ciphInited = true;
         try {
             assert key.length * 8 == keyLen : "Key wrong length";
             assert iv.length * 8 == ivLen : "IV wrong length";
             if(!"ECB".equalsIgnoreCase(cryptoMode) && this.iv != null) {
                 this.ciph.init(encryptMode ? javax.crypto.Cipher.ENCRYPT_MODE : javax.crypto.Cipher.DECRYPT_MODE, new SimpleSecretKey(this.key), new IvParameterSpec(this.iv));
             } else {
                 this.ciph.init(encryptMode ? javax.crypto.Cipher.ENCRYPT_MODE : javax.crypto.Cipher.DECRYPT_MODE, new SimpleSecretKey(this.key));
             }
         } catch(Exception e) {
             e.printStackTrace();
             throw new RaiseException(getRuntime(), ciphErr, null, true);
         }
     }
 
     public IRubyObject update(IRubyObject data) {
         if (DEBUG) System.out.println("*** update ["+data+"]");
 
         //TODO: implement correctly
         byte[] val = data.convertToString().getBytes();
         if(val.length == 0) {
             throw getRuntime().newArgumentError("data must not be empty");
         }
 
         if(!ciphInited) {
             doInitialize();
         }
 
         byte[] str = new byte[0];
         try {
             byte[] out = ciph.update(val);
             if(out != null) {
                 str = out;
             }
         } catch(Exception e) {
             e.printStackTrace();
             throw new RaiseException(getRuntime(), ciphErr, null, true);
         }
 
         return RubyString.newString(getRuntime(), new ByteList(str,false));
     }
 
     public IRubyObject update_deprecated(IRubyObject data) {
        getRuntime().getWarnings().warn(IRubyWarnings.ID.DEPRECATED_METHOD, "" + this.getMetaClass().getRealClass().getName() + "#<< is deprecated; use " + this.getMetaClass().getRealClass().getName() + "#update instead");
         return update(data);
     }
 
     public IRubyObject _final() {
         if(!ciphInited) {
             doInitialize();
         }
 
         //TODO: implement correctly
         ByteList str = new ByteList(ByteList.NULL_ARRAY);
         try {
             byte[] out = ciph.doFinal();
             if(out != null) {
                 str = new ByteList(out,false);
             }
         } catch(Exception e) {
             throw new RaiseException(getRuntime(), ciphErr, e.getMessage(), true);
         }
 
         return getRuntime().newString(str);
     }
 
     public IRubyObject set_padding(IRubyObject padding) {
         this.padding = padding.toString();
         initialize(RubyString.newString(getRuntime(), name), Block.NULL_BLOCK);
         return padding;
     }
 
     String getAlgorithm() {
         return this.ciph.getAlgorithm();
     }
 }
