 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.webapp.api;
 
 import com.google.common.collect.Lists;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.List;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.hamcrest.Description;
 import org.hamcrest.Factory;
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 import org.hamcrest.core.IsEqual;
 import org.json.simple.JSONArray;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.rules.ExpectedException;
 
 /**
  * Tests for {@link Converter}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ConverterTest {
 
     @Rule
     //CHECKSTYLE:OFF
     public final ExpectedException thrown = ExpectedException.none();
     //CHECKSTYLE:ON
 
     @Test
     public void fromJsonToXml_Empty() throws JSONException {
         assertThat(Converter.xml(new JSONObject(), "UTF-8"), is(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + "<!DOCTYPE data SYSTEM \"http://lafayette.weltraumschaf.de/api/dtd/data.dtd\">\r\n"
                 + "<data version=\"1.0\">\r\n"
                 + "</data>\r\n")));
     }
 
     @Test
     public void fromJsonToXml_simple() throws JSONException {
         final JSONObject json = new JSONObject();
         json.put("foo", "bar");
         json.put("number1", 42);
         json.put("number2", 3.14);
         json.put("boolean", true);
         assertThat(Converter.xml(json, "UTF-8"), is(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + "<!DOCTYPE data SYSTEM \"http://lafayette.weltraumschaf.de/api/dtd/data.dtd\">\r\n"
                 + "<data version=\"1.0\">\r\n"
                 + "    <map>\r\n"
                 + "        <key>number2</key>\r\n"
                 + "        <float>3.14</float>\r\n"
                 + "        <key>number1</key>\r\n"
                 + "        <integer>42</integer>\r\n"
                 + "        <key>boolean</key>\r\n"
                 + "        <true/>\r\n"
                 + "        <key>foo</key>\r\n"
                 + "        <string>bar</string>\r\n"
                 + "    </map>\r\n"
                 + "</data>\r\n")));
     }
 
     @Test
     public void fromJsonToXml_complex() throws JSONException {
         final JSONObject json = createComplexObjectWithAllTypes();
 
         assertThat(Converter.xml(json, "UTF-8"), is(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + "<!DOCTYPE data SYSTEM \"http://lafayette.weltraumschaf.de/api/dtd/data.dtd\">\r\n"
                 + "<data version=\"1.0\">\r\n"
                 + "    <map>\r\n"
                 + "        <key>number1</key>\r\n"
                 + "        <integer>42</integer>\r\n"
                 + "        <key>arr</key>\r\n"
                 + "        <array>\r\n"
                 + "            <string>foo</string>\r\n"
                 + "            <string>bar</string>\r\n"
                 + "            <string>baz</string>\r\n"
                 + "        </array>\r\n"
                 + "        <key>obj</key>\r\n"
                 + "        <map>\r\n"
                 + "            <key>number2</key>\r\n"
                 + "            <float>3.14</float>\r\n"
                 + "            <key>boolean</key>\r\n"
                 + "            <true/>\r\n"
                 + "        </map>\r\n"
                 + "        <key>foo</key>\r\n"
                 + "        <string>bar</string>\r\n"
                 + "    </map>\r\n"
                 + "</data>\r\n")));
     }
 
     @Test
     public void fromJsonToMessagePack_null() throws JSONException, IOException {
         thrown.expect(NullPointerException.class);
         Converter.mpack(null);
     }
 
     @Test
     public void fromJsonToMessagePack_empty() throws JSONException, IOException {
         assertThat(Converter.mpack(new JSONObject()), is(equalTo(new byte[]{
             (byte) 0x80, // map w/ 0 elements
         })));
     }
 
     @Test
     public void fromJsonToMessagePack_simple() throws JSONException, IOException {
         final JSONObject json = new JSONObject();
         json.put("foo", "bar");
         json.put("number1", 42);
         json.put("number2", 3.14);
         json.put("boolean", true);
 
         assertThat(Converter.mpack(json), is(ByteIsEqual.equalTo(new byte[]{
            (byte) 0x84, // Fix map w/ 4 elements
            (byte) 0xa7, (byte) 0x6e, (byte) 0x75, (byte) 0x6d, (byte) 0x62, (byte) 0x65, (byte) 0x72, (byte) 0x32, // "number2"
            (byte) 0xcb, (byte) 0x40, (byte) 0x09, (byte) 0x1e, (byte) 0xb8, (byte) 0x51, (byte) 0xeb, (byte) 0x85, (byte) 0x1f, // 3.14
            (byte) 0xa7, (byte) 0x6e, (byte) 0x75, (byte) 0x6d, (byte) 0x62, (byte) 0x65, (byte) 0x72, (byte) 0x31, // "number1"
            (byte) 0x2a, // 42
            (byte) 0xa7, (byte) 0x62, (byte) 0x6f, (byte) 0x6f, (byte) 0x6c, (byte) 0x65, (byte) 0x61, (byte) 0x6e, // boolean
            (byte) 0xc3, // true
            (byte) 0xa3, (byte) 0x66, (byte) 0x6f, (byte) 0x6f, // "foo"
            (byte) 0xa3, (byte) 0x62, (byte) 0x61, (byte) 0x72, // "bar"
        })));
     }
 
     @Test
     @Ignore
     public void fromJsonToMessagePack_complex() throws JSONException, IOException {
         final JSONObject json = createComplexObjectWithAllTypes();
 
         assertThat(Converter.mpack(json), is(ByteIsEqual.equalTo(new byte[]{
             (byte) 0x84, (byte) 0xa3, (byte) 0x66, (byte) 0x6f, (byte) 0x6f, (byte) 0xa3, (byte) 0x62,
             (byte) 0x61, (byte) 0x72, (byte) 0xa7, (byte) 0x6e, (byte) 0x75, (byte) 0x6d, (byte) 0x62, (byte) 0x65,
             (byte) 0x72, (byte) 0x31, (byte) 0x2a, (byte) 0xa3, (byte) 0x6f, (byte) 0x62, (byte) 0x6a, (byte) 0x91,
             (byte) 0x82, (byte) 0xa7, (byte) 0x6e, (byte) 0x75, (byte) 0x6d, (byte) 0x62, (byte) 0x65, (byte) 0x72,
             (byte) 0x32, (byte) 0xcb, (byte) 0x40, (byte) 0x09, (byte) 0x1e, (byte) 0xb8, (byte) 0x51, (byte) 0xeb,
             (byte) 0x85, (byte) 0x1f, (byte) 0xa7, (byte) 0x62, (byte) 0x6f, (byte) 0x6f, (byte) 0x6c, (byte) 0x65,
             (byte) 0x61, (byte) 0x6e, (byte) 0xc3, (byte) 0xa3, (byte) 0x61, (byte) 0x72, (byte) 0x72, (byte) 0x91,
             (byte) 0x93, (byte) 0xa3, (byte) 0x66, (byte) 0x6f, (byte) 0x6f, (byte) 0xa3, (byte) 0x62, (byte) 0x61,
             (byte) 0x72, (byte) 0xa3, (byte) 0x62, (byte) 0x61, (byte) 0x7a,})));
     }
 
     @SuppressWarnings("unchecked") // Because JSONArray extends ArrayList as raw type.
     private JSONObject createComplexObjectWithAllTypes() throws JSONException {
         final JSONObject json = new JSONObject();
         json.put("foo", "bar");
         json.put("number1", 42);
         final JSONObject obj1 = new JSONObject();
         obj1.put("number2", 3.14);
         obj1.put("boolean", true);
         json.put("obj", obj1);
         final JSONArray arr = new JSONArray();
         arr.add("foo");
         arr.add("bar");
         arr.add("baz");
         json.put("arr", arr);
         return json;
     }
 
     /**
      * Matcher for byte (arrays) which will be formatted as unsigned hex in descriptions.
      *
      * @param <T> type of matched object
      */
     private static class ByteIsEqual<T> extends IsEqual<T> {
         private static final int UNSIGNED_MASK = 0xFF;
         private static final String HEX_PREFIX = "0x";
 
         /**
          * Needed because member in parent is private.
          */
         private final T expectedValue;
 
         /**
          * Dedicated constructor.
          *
          * @param equalArg may be {@code null}
          */
         public ByteIsEqual(final T equalArg) {
             super(equalArg);
             expectedValue = equalArg;
         }
 
         @Override
         public void describeTo(final Description description) {
             if (isByteArray(expectedValue)) {
                 description.appendValue(formatBytes((byte[]) expectedValue));
             } else if (expectedValue instanceof Byte) {
                 description.appendValue(formatByte((Byte) expectedValue));
             } else {
                 super.describeTo(description);
             }
         }
 
         @Override
         public void describeMismatch(final Object item, final Description description) {
             if (isByteArray(item)) {
                 description.appendText("was ");
                 description.appendValue(formatBytes((byte[]) item));
             } else if (item instanceof Byte) {
                 super.describeMismatch(formatByte((Byte) item), description);
             } else {
                 super.describeMismatch(item, description);
             }
         }
 
         private static List<String> formatBytes(final byte[] bytes) {
             final List<String> formatted = Lists.newArrayList();
 
             for (final byte b : bytes) {
                 formatted.add(formatByte(b));
             }
 
             return formatted;
         }
 
         private static String formatByte(final byte b) {
            String hexString = Integer.toHexString(formatSignedByteToSignedInt(b));

            if (hexString.length() == 1) {
                hexString = "0" + hexString;
            }

            return HEX_PREFIX + hexString;
         }
 
         private static boolean isByte(final Object o) {
             return o instanceof Byte;
         }
 
         private static boolean isByteArray(final Object o) {
             return o instanceof byte[] || o instanceof Byte[];
         }
 
         private static int formatSignedByteToSignedInt(final byte b) {
             return b & UNSIGNED_MASK;
         }
 
         @Factory
         public static <T> Matcher<T> equalTo(final T operand) {
             return new ByteIsEqual<T>(operand);
         }
     }
 }
