 /**
  * The MIT License (MIT)
  * 
  * Copyright (c) 2013 Twuni
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package org.twuni.twoson;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class JSONParserTest {
 
	private static final byte [] TEST_OBJECT = "{\"a\":\"test\",\"b\":123,\"c\":[1,2,3],\"d\":{\"da\":1,\"db\":\"test\"},\"e\":\"with \\\"quote\\\" in it\"}".getBytes();
 
 	@Test
 	public void testParser() throws IOException {
 
 		JSONParser parser = new JSONParser( new ByteArrayInputStream( TEST_OBJECT ), new JSONEventListener() {
 
 			@Override
 			public void onBeginArray() {
 				System.out.println( "#onBeginArray" );
 			}
 
 			@Override
 			public void onBeginObject() {
 				System.out.println( "#onBeginObject" );
 			}
 
 			@Override
 			public void onBoolean( boolean value ) {
 				System.out.println( "#onBoolean value:" + Boolean.toString( value ) );
 			}
 
 			@Override
 			public void onEndArray() {
 				System.out.println( "#onEndArray" );
 			}
 
 			@Override
 			public void onEndObject() {
 				System.out.println( "#onEndObject" );
 			}
 
 			@Override
 			public void onFloat( float value ) {
 				System.out.println( "#onFloat value:" + Float.toString( value ) );
 			}
 
 			@Override
 			public void onInteger( int value ) {
 				System.out.println( "#onInteger value:" + Integer.toString( value ) );
 			}
 
 			@Override
 			public void onNull() {
 				System.out.println( "#onNull" );
 			}
 
 			@Override
 			public void onObjectKey( char [] value ) {
 				System.out.println( "#onObjectKey key:" + new String( value ) );
 			}
 
 			@Override
 			public void onString( char [] value ) {
 				System.out.println( "#onString value:" + new String( value ) );
 			}
 
 		} );
 
 		parser.read();
 
 	}
 
 }
