 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.crypto.bc;
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.test.*;
 import us.exultant.ahs.crypto.*;
 import java.nio.*;
 import java.util.*;
 
 public class AesCtrPkcs7Sha1Test extends TestCase {
 	public static void main(String... $args) {
 		new AesCtrPkcs7Sha1Test().run();
 	}
 	
 	public AesCtrPkcs7Sha1Test() {
 		super(new Logger(Logger.LEVEL_DEBUG), true);
 	}
 	
 	public AesCtrPkcs7Sha1Test(Logger $log, boolean $enableConfirmation) {
 		super($log, $enableConfirmation);
 	}
 	
 	public List<Unit> getUnits() {
 		return Arr.asList(
 				new TestAnything(),
 				new TestInvalidIv(),
 				new TestInvalidKey(),
 				new TestInvalidMacKey(),
 				new TestCiphertextConsistency(),
 				new TestKeyReuse()
 		);
 	}
 	
 	private static final byte[]	$c1	= Strings.toBytes("oh god I'm covered in bees");
 	private static final Ks		$ks1	= new Ks.Basic(new byte[] { 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F });
 	private static final Ks		$ks2	= new Ks.Basic(new byte[] { 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F });
 	private static final Ks		$ks11	= new Ks.Basic(new byte[] { 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F });	// same as ks1 but not peq
 	private static final Ks		$ksws	= new Ks.Basic(new byte[] { 0x33, 0x29, 0x58, 0x76 });										// weird size
 	private static final Kc		$kc1	= new Kc(new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF });
 	private static final Kc		$kc0	= new Kc(new byte[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 });
 	private static final Kc		$kcws	= new Kc(new byte[] { 0x0, 0x0, 0x0 });											// weird size
 	private static final byte[]	$c10m;
 	static {
 		Random $r = new java.security.SecureRandom();
 		ByteBuffer $t = ByteBuffer.allocate(1024 * 1024 * 10);	// 10MB
 		while ($t.hasRemaining())
 			$t.putInt($r.nextInt());
 		$c10m = $t.array();
 	}
 	
 	private class TestAnything extends TestCase.Unit { 
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph = $sys.encrypt(
 					$ks1,
 					$kc1,
 					$ks2,
 					$c1
 			);
 			return null;
 		}
 	}
 	
 	private class TestInvalidIv extends TestCase.Unit {
 		@SuppressWarnings("unchecked")
 		public Class<ArrayIndexOutOfBoundsException> expectExceptionType(){
 			return ArrayIndexOutOfBoundsException.class;
 		}
 		
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph = $sys.encrypt(
 					$ks1,
 					$kcws,
 					$ks2,
 					$c1
 			);
 			return null;
 		}
 	}
 	
 	private class TestInvalidKey extends TestCase.Unit {
 		@SuppressWarnings("unchecked")
 		public Class<IllegalArgumentException> expectExceptionType(){
 			return IllegalArgumentException.class;
 		}
 		
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph = $sys.encrypt(
 					$ksws,
 					$kc1,
 					$ks2,
 					$c1
 			);
 			return null;
 		}
 	}
 	
 	private class TestInvalidMacKey extends TestCase.Unit {
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph = $sys.encrypt(
 					$ks1,
 					$kc1,
 					$ksws,
 					$c1
 			);
 			// It's pretty hard to come up with something that actually qualifies as an invalid MAC key.
 			// Having a MAC key longer than needed results in hashing and then trimming of the hash; having a MAC key shorter than needed results in padding with zeros.
 			// For SHA1, the appropriate HMAC key length is 64 bytes. 
 			return null;
 		}
 	}
 
 	private class TestCiphertextConsistency extends TestCase.Unit {
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph = $sys.encrypt(
 					$ks1,
 					$kc1,
 					$ks2,
 					$c1
 			);
 			assertEquals(
 					$cph.getIv().getBytes(),
 					$kc1.getBytes()
 			);
 			assertEquals(
 					$cph.getBody(),
 					Strings.fromHex("F33CF5161F48DA46249E4276081DCE0A5BF00A95F96284F753E524FCAFA9BDFE")
 			);
 			assertEquals(
 					$cph.getMac(),
 					Strings.fromHex("A2E025C02A21392EA17ADC2C0FE12CDC66F5C3C9")
 			);
 			return null;
 		}
 	}
 
 	private class TestKeyReuse extends TestCase.Unit {
 		public Object call() {
			AesCtrPkcs7Sha1 $sys = new AesCtrPkcs7Sha1();
 			CiphertextSymmetric $cph1 = $sys.encrypt(
 					$ks1,
 					$kc1,
 					$ks2,
 					$c1
 			);
 			CiphertextSymmetric $cph2 = $sys.encrypt(
 					$ks1,
 					$kc1,
 					$ks2,
 					$c1
 			);
 			assertEquals(
 					$cph1.getBody(),
 					$cph2.getBody()
 			);
 			return null;
 		}
 	}
 }
