 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package android.telephony.cts;
 
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.provider.Contacts;
 import android.provider.Contacts.People;
 import android.telephony.PhoneNumberUtils;
 import android.telephony.TelephonyManager;
 import android.test.AndroidTestCase;
 import android.text.Editable;
 import android.text.SpannableStringBuilder;
 
 import java.util.Locale;
 
 @TestTargetClass(PhoneNumberUtils.class)
 public class PhoneNumberUtilsTest extends AndroidTestCase {
     // mPhoneNumber ~ "+17005550020", length == 7.
     private byte[] mPhoneNumber = { (byte) 0x91, (byte) 0x71, (byte) 0x00, (byte) 0x55,
             (byte) 0x05, (byte) 0x20, (byte) 0xF0 };
 
     @TestTargets({
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "extractNetworkPortion",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "extractPostDialPortion",
         args = {String.class}
       )
     })
     public void testExtractMethods() {
 
         // Test extractNetworkPortion
         assertNull(PhoneNumberUtils.extractNetworkPortion(null));
         assertEquals("+17005554141", PhoneNumberUtils.extractNetworkPortion("+17005554141"));
         assertEquals("+17005554141*#N",
                 PhoneNumberUtils.extractNetworkPortion("+1 (700).555-4141*#N"));
         assertEquals("170055541", PhoneNumberUtils.extractNetworkPortion("1 (700).555-41,1234"));
         assertEquals("**21**17005554141#",
                 PhoneNumberUtils.extractNetworkPortion("**21**+17005554141#"));
 
         // Test extractPostDialPortion
         assertNull(PhoneNumberUtils.extractPostDialPortion(null));
         assertEquals("", PhoneNumberUtils.extractPostDialPortion("+17005554141"));
         assertEquals(",1234", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN,1234"));
         assertEquals(";1234", PhoneNumberUtils.extractPostDialPortion("+1 (700).555-41NN;1234"));
         assertEquals(";1234,;N", PhoneNumberUtils
                 .extractPostDialPortion("+1 (700).555-41NN;1-2.34 ,;N"));
     }
 
     @TestTargets({
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "calledPartyBCDToString",
         args = {byte[].class, int.class, int.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "toCallerIDMinMatch",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "networkPortionToCalledPartyBCD",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "calledPartyBCDFragmentToString",
         args = {byte[].class, int.class, int.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "networkPortionToCalledPartyBCDWithLength",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "numberToCalledPartyBCD",
         args = {String.class}
       )
     })
     public void testCallMethods() {
         // Test calledPartyBCDToString
         assertEquals("+17005550020", PhoneNumberUtils.calledPartyBCDToString(mPhoneNumber, 0, 7));
 
         // Test toCallerIDMinMatch
         assertNull(PhoneNumberUtils.toCallerIDMinMatch(null));
         assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("17005554141"));
         assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141"));
         assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141,1234"));
         assertEquals("1414555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-4141;1234"));
         assertEquals("NN14555", PhoneNumberUtils.toCallerIDMinMatch("1-700-555-41NN"));
         assertEquals("", PhoneNumberUtils.toCallerIDMinMatch(""));
         assertEquals("0032", PhoneNumberUtils.toCallerIDMinMatch("2300"));
         assertEquals("0032+", PhoneNumberUtils.toCallerIDMinMatch("+2300"));
         assertEquals("#130#*", PhoneNumberUtils.toCallerIDMinMatch("*#031#"));
 
         // Test networkPortionToCalledPartyBCD, calledPartyBCDToString
         byte[] bRet = PhoneNumberUtils.networkPortionToCalledPartyBCD("+17005550020");
         assertEquals(mPhoneNumber.length, bRet.length);
         for (int i = 0; i < mPhoneNumber.length; i++) {
             assertEquals(mPhoneNumber[i], bRet[i]);
         }
         bRet = PhoneNumberUtils.networkPortionToCalledPartyBCD("7005550020");
         assertEquals("7005550020", PhoneNumberUtils.calledPartyBCDToString(bRet, 0, bRet.length));
 
         // Test calledPartyBCDFragmentToString
         assertEquals("1917005550020", PhoneNumberUtils.calledPartyBCDFragmentToString(mPhoneNumber,
                 0, 7));
 
         // Test networkPortionToCalledPartyBCDWithLength
         bRet = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength("+17005550020");
         assertEquals(mPhoneNumber.length + 1, bRet.length);
         for (int i = 0; i < mPhoneNumber.length; i++) {
             assertEquals(mPhoneNumber[i], bRet[i + 1]);
         }
 
         // Test numberToCalledPartyBCD
         bRet = PhoneNumberUtils.numberToCalledPartyBCD("+17005550020");
         assertEquals(mPhoneNumber.length, bRet.length);
         for (int i = 0; i < mPhoneNumber.length; i++) {
             assertEquals(mPhoneNumber[i], bRet[i]);
         }
     }
 
     /**
      * Tests which must be successful both in compareLoosely() and in compareStrictly().
      */
     private void testCompareCommon(final boolean strict) {
         assertTrue(PhoneNumberUtils.compare(null, null, strict));
         assertFalse(PhoneNumberUtils.compare(null, "", strict));
         assertFalse(PhoneNumberUtils.compare("", null, strict));
         assertFalse(PhoneNumberUtils.compare("", "", strict));
 
         assertTrue(PhoneNumberUtils.compare("911", "911", strict));
         assertFalse(PhoneNumberUtils.compare("911", "18005550911", strict));
 
         assertTrue(PhoneNumberUtils.compare("+17005554141", "+17005554141", strict));
         assertTrue(PhoneNumberUtils.compare("+17005554141", "+1 (700).555-4141", strict));
         assertTrue(PhoneNumberUtils.compare("+17005554141", "7005554141", strict));
         assertFalse(PhoneNumberUtils.compare("+1 999 7005554141", "+1 7005554141", strict));
         assertTrue(PhoneNumberUtils.compare("011 1 7005554141", "7005554141", strict));
         assertFalse(PhoneNumberUtils.compare("011 11 7005554141", "+17005554141", strict));
         assertTrue(PhoneNumberUtils.compare("+44 207 792 3490", "0 207 792 3490", strict));
         assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "00 207 792 3490", strict));
 
         // MMI header should be ignored
         assertFalse(PhoneNumberUtils.compare("+17005554141", "**31#17005554141", strict));
         assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "+44 (0) 207 792 3490", strict));
         assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "010 44 207 792 3490", strict));
         assertFalse(PhoneNumberUtils.compare("+44 207 792 3490", "0011 44 207 792 3490", strict));
         assertTrue(PhoneNumberUtils.compare("+444 207 792 3490", "0 207 792 3490", strict));
 
         // make sure SMS short code comparison for numbers less than 7 digits work correctly.
         // For example, "404-04" and "40404" should match because the dialable portion for both
         // numbers are the same.
         assertTrue(PhoneNumberUtils.compare("404-04", "40404", strict));
     }
 
     @TestTargetNew(
       level = TestLevel.COMPLETE,
       method = "compare",
       args = {String.class, String.class}
     )
     public void testCompareLoosely() {
         testCompareCommon(false);
 
         assertTrue(PhoneNumberUtils.compareLoosely("17005554141", "5554141"));
         assertTrue(PhoneNumberUtils.compareLoosely("+17005554141", "**31#+17005554141"));
         assertFalse(PhoneNumberUtils.compareLoosely("+7(095)9100766", "8(095)9100766"));
     }
 
     @TestTargetNew(
       level = TestLevel.COMPLETE,
       method = "compare",
       args = {String.class, String.class}
     )
     public void testCompareStrictly() {
         testCompareCommon(true);
 
         // This must be true, since
         // - +7 is russian country calling code
         // - 8 is russian trunk prefix, which should be omitted when being compared to
         //   the number with country calling code.
         // - so, this comparation becomes same as comparation between
         //   "(095)9100766" v.s."(095)9100766", which is definitely true.
         assertTrue(PhoneNumberUtils.compareStrictly("+7(095)9100766", "8(095)9100766"));
 
         // Test broken caller ID seen on call from Thailand to the US.
         assertTrue(PhoneNumberUtils.compareStrictly("+66811234567", "166811234567"));
 
         // This is not related to Thailand case. NAMP "1" + region code "661".
         assertTrue(PhoneNumberUtils.compareStrictly("16610001234", "6610001234"));
 
         assertFalse(PhoneNumberUtils.compareStrictly("080-1234-5678", "+819012345678"));
         assertTrue(PhoneNumberUtils.compareStrictly("650-000-3456", "16500003456"));
 
         // Phone numbers with Ascii characters, which are common in some countries
         assertFalse(PhoneNumberUtils.compareStrictly("abcd", "bcde"));
         assertTrue(PhoneNumberUtils.compareStrictly("1-800-flowers", "800-flowers"));
         assertFalse(PhoneNumberUtils.compareStrictly("1-800-flowers", "1-800-abcdefg"));
     }
 
     @TestTargets({
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getStrippedReversed",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "getFormatTypeForLocale",
         args = {Locale.class}
       ),
       @TestTargetNew(
         level = TestLevel.PARTIAL,
         notes = "partial, null branch is ok, non-null has a insert fail",
         method = "getNumberFromIntent",
         args = {Intent.class, Context.class}
       )
     })
     public void testGetMethods() throws RemoteException {
         // Test getStrippedReversed
         assertNull(PhoneNumberUtils.getStrippedReversed(null));
         assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141"));
         assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141,1234"));
         assertEquals("14145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-4141;1234"));
         assertEquals("NN145550071", PhoneNumberUtils.getStrippedReversed("1-700-555-41NN"));
         assertEquals("", PhoneNumberUtils.getStrippedReversed(""));
         assertEquals("#130#*+", PhoneNumberUtils.getStrippedReversed("+*#031#"));
 
         // Test getFormatTypeForLocale
         int formatType = PhoneNumberUtils.getFormatTypeForLocale(Locale.CHINA);
         assertEquals(PhoneNumberUtils.FORMAT_UNKNOWN, formatType);
         formatType = PhoneNumberUtils.getFormatTypeForLocale(Locale.US);
         assertEquals(PhoneNumberUtils.FORMAT_NANP, formatType);
         formatType = PhoneNumberUtils.getFormatTypeForLocale(Locale.JAPAN);
         assertEquals(PhoneNumberUtils.FORMAT_JAPAN, formatType);
 
         // Test getNumberFromIntent, query nothing, return null.
         Intent intent = new Intent();
         intent.setData(Contacts.People.CONTENT_URI);
         Context context = getContext();
         assertNull(PhoneNumberUtils.getNumberFromIntent(intent, context));
 
         intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+18005555555"));
         assertEquals("+18005555555", PhoneNumberUtils.getNumberFromIntent(intent, getContext()));
 
         intent = new Intent(Intent.ACTION_DIAL, Uri.parse("voicemail:"));
         TelephonyManager tm =
                 (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
         assertEquals(tm.getVoiceMailNumber(),
                 PhoneNumberUtils.getNumberFromIntent(intent, getContext()));
 
         ContentResolver cr = getContext().getContentResolver();
         Uri personRecord = null;
         Uri phoneRecord = null;
         try {
             // insert a contact with phone number
             ContentValues values = new ContentValues();
             values.put(People.NAME, "CTS test contact");
             personRecord = cr.insert(People.CONTENT_URI, values);
             Uri phoneUri = Uri.withAppendedPath(personRecord, People.Phones.CONTENT_DIRECTORY);
             values.clear();
             values.put(People.Phones.TYPE, People.Phones.TYPE_HOME);
             values.put(People.Phones.NUMBER, "+18005552871");
             phoneRecord = cr.insert(phoneUri, values);
 
             intent = new Intent(Intent.ACTION_DIAL, phoneRecord);
             assertEquals("+18005552871",
                     PhoneNumberUtils.getNumberFromIntent(intent, getContext()));
         } finally {
             if (personRecord != null) {
                 cr.delete(personRecord, null, null);
             }
             if (phoneRecord != null) {
                 cr.delete(phoneRecord, null, null);
             }
         }
     }
 
     @TestTargets({
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "formatNanpNumber",
         args = {Editable.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "convertKeypadLettersToDigits",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "stringFromStringAndTOA",
         args = {String.class, int.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "formatJapaneseNumber",
         args = {Editable.class}
        ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "formatNumber",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "formatNumber",
         args = {Editable.class, int.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "stripSeparators",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "toaFromString",
         args = {String.class}
       )
     })
     public void testFormatMethods() {
         // Test formatNanpNumber
         SpannableStringBuilder builderNumber = new SpannableStringBuilder();
         builderNumber.append("8005551212");
         PhoneNumberUtils.formatNanpNumber(builderNumber);
         assertEquals("800-555-1212", builderNumber.toString());
         builderNumber.clear();
         builderNumber.append("800555121");
         PhoneNumberUtils.formatNanpNumber(builderNumber);
         assertEquals("800-555-121", builderNumber.toString());
         builderNumber.clear();
         builderNumber.append("555-1212");
         PhoneNumberUtils.formatNanpNumber(builderNumber);
         assertEquals("555-1212", builderNumber.toString());
         builderNumber.clear();
         builderNumber.append("180055512");
         PhoneNumberUtils.formatNanpNumber(builderNumber);
         assertEquals("1-800-555-12", builderNumber.toString());
         builderNumber.clear();
         builderNumber.append("+180055512");
         PhoneNumberUtils.formatNanpNumber(builderNumber);
         assertEquals("+1-800-555-12", builderNumber.toString());
 
         // Test convertKeypadLettersToDigits
         assertEquals("1-800-4664-411", PhoneNumberUtils
                 .convertKeypadLettersToDigits("1-800-GOOG-411"));
         assertEquals("1-800-466-4411", PhoneNumberUtils
                 .convertKeypadLettersToDigits("1-800-466-4411"));
         assertEquals("222-333-444-555-666-7777-888-9999", PhoneNumberUtils
                 .convertKeypadLettersToDigits("ABC-DEF-GHI-JKL-MNO-PQRS-TUV-WXYZ"));
         assertEquals("222-333-444-555-666-7777-888-9999", PhoneNumberUtils
                 .convertKeypadLettersToDigits("abc-def-ghi-jkl-mno-pqrs-tuv-wxyz"));
         assertEquals("(800) 222-3334", PhoneNumberUtils
                 .convertKeypadLettersToDigits("(800) ABC-DEFG"));
 
         // Test stringFromStringAndTOA
         assertNull(PhoneNumberUtils.stringFromStringAndTOA(null, 1));
         assertEquals("+888888888", PhoneNumberUtils.stringFromStringAndTOA("888888888",
                 PhoneNumberUtils.TOA_International));
 
         // Test formatJapaneseNumber
         Editable jpEditNumber = Editable.Factory.getInstance().newEditable("0377777777");
         PhoneNumberUtils.formatJapaneseNumber(jpEditNumber);
         assertEquals("03-7777-7777", jpEditNumber.toString());
         jpEditNumber = Editable.Factory.getInstance().newEditable("09077777777");
         PhoneNumberUtils.formatJapaneseNumber(jpEditNumber);
         assertEquals("090-7777-7777", jpEditNumber.toString());
         jpEditNumber = Editable.Factory.getInstance().newEditable("0120777777");
         PhoneNumberUtils.formatJapaneseNumber(jpEditNumber);
         assertEquals("0120-777-777", jpEditNumber.toString());
         jpEditNumber = Editable.Factory.getInstance().newEditable("+81377777777");
         PhoneNumberUtils.formatJapaneseNumber(jpEditNumber);
         assertEquals("+81-3-7777-7777", jpEditNumber.toString());
         jpEditNumber = Editable.Factory.getInstance().newEditable("+819077777777");
         PhoneNumberUtils.formatJapaneseNumber(jpEditNumber);
         assertEquals("+81-90-7777-7777", jpEditNumber.toString());
 
         // Test formatNumber(String). Only numbers begin with +1 or +81 can be formatted.
         assertEquals("+1-888-888-888", PhoneNumberUtils.formatNumber("+1888888888"));
         // Test formatNumber(Editable, int)
         Editable editNumber = Editable.Factory.getInstance().newEditable("0377777777");
         PhoneNumberUtils.formatNumber(editNumber, PhoneNumberUtils.FORMAT_UNKNOWN);
         assertEquals("0377777777", editNumber.toString());
         editNumber = Editable.Factory.getInstance().newEditable("+177777777");
         PhoneNumberUtils.formatNumber(editNumber, PhoneNumberUtils.FORMAT_UNKNOWN);
         assertEquals("+1-777-777-77", editNumber.toString());
         editNumber = Editable.Factory.getInstance().newEditable("+8177777777");
         PhoneNumberUtils.formatNumber(editNumber, PhoneNumberUtils.FORMAT_UNKNOWN);
         assertEquals("+81-77-777-777", editNumber.toString());
 
         // Test stripSeparators
         assertEquals("+188888888", PhoneNumberUtils.stripSeparators("+188-888-888"));
 
         // Test toaFromString
         assertEquals(PhoneNumberUtils.TOA_International, PhoneNumberUtils
                 .toaFromString("+88888888"));
         assertEquals(PhoneNumberUtils.TOA_Unknown, PhoneNumberUtils.toaFromString("88888888"));
     }
 
     @TestTargets({
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "is12Key",
         args = {char.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isDialable",
         args = {char.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isEmergencyNumber",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isGlobalPhoneNumber",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isISODigit",
         args = {char.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isReallyDialable",
         args = {char.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isStartsPostDial",
         args = {char.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isWellFormedSmsAddress",
         args = {String.class}
       ),
       @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "isNonSeparator",
         args = {char.class}
       )
     })
     public void testJudgeMethods() {
         // Test is12Key, isDialable, isISODigit, isReallyDialable, isStartsPostDial
         for (char c = '0'; c <= '9'; c++) {
             assertTrue(PhoneNumberUtils.is12Key(c));
             assertTrue(PhoneNumberUtils.isDialable(c));
             assertTrue(PhoneNumberUtils.isISODigit(c));
             assertTrue(PhoneNumberUtils.isNonSeparator(c));
             assertTrue(PhoneNumberUtils.isReallyDialable(c));
         }
         char c = '*';
         assertTrue(PhoneNumberUtils.is12Key(c));
         assertTrue(PhoneNumberUtils.isDialable(c));
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertTrue(PhoneNumberUtils.isReallyDialable(c));
         c = '#';
         assertTrue(PhoneNumberUtils.is12Key(c));
         assertTrue(PhoneNumberUtils.isDialable(c));
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertTrue(PhoneNumberUtils.isReallyDialable(c));
         c = '$';
         assertFalse(PhoneNumberUtils.is12Key(c));
         assertFalse(PhoneNumberUtils.isDialable(c));
         assertFalse(PhoneNumberUtils.isDialable(c));
         c = '+';
         assertTrue(PhoneNumberUtils.isDialable(c));
         assertFalse(PhoneNumberUtils.isISODigit(c));
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertTrue(PhoneNumberUtils.isReallyDialable(c));
         c = PhoneNumberUtils.WILD;
         assertTrue(PhoneNumberUtils.isDialable(c));
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertFalse(PhoneNumberUtils.isReallyDialable(c));
         c = PhoneNumberUtils.WAIT;
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertTrue(PhoneNumberUtils.isStartsPostDial(c));
         c = PhoneNumberUtils.PAUSE;
         assertTrue(PhoneNumberUtils.isNonSeparator(c));
         assertTrue(PhoneNumberUtils.isStartsPostDial(c));
         c = '8';
         assertFalse(PhoneNumberUtils.isStartsPostDial(c));
 
         // Test isEmergencyNumber, now only know US emergency number
         if (Locale.getDefault().getCountry().equals("US")) {
             assertTrue(PhoneNumberUtils.isEmergencyNumber("911"));
            assertTrue(PhoneNumberUtils.isEmergencyNumber("112"));
             assertFalse(PhoneNumberUtils.isEmergencyNumber("119"));
         }
 
         // Test isGlobalPhoneNumber
         assertTrue(PhoneNumberUtils.isGlobalPhoneNumber("+17005554141"));
         assertFalse(PhoneNumberUtils.isGlobalPhoneNumber("android"));
 
         // Test isWellFormedSmsAddress
         assertTrue(PhoneNumberUtils.isWellFormedSmsAddress("+17005554141"));
         assertFalse(PhoneNumberUtils.isWellFormedSmsAddress("android"));
     }
 }
