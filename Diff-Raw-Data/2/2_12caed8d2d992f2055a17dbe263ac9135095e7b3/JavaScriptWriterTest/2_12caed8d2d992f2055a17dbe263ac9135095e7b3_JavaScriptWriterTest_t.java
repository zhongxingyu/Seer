 package fi.vincit.jmobster.processor.languages.javascript.writer;/*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
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
 
 import fi.vincit.jmobster.util.itemprocessor.ItemStatuses;
 import fi.vincit.jmobster.util.writer.DataWriter;
 import fi.vincit.jmobster.util.writer.StringBufferWriter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.*;
 
 public class JavaScriptWriterTest {
     private DataWriter mw;
     private JavaScriptWriter writer;
 
     @Before
     public void initTest() {
         mw = new StringBufferWriter();
         writer = new JavaScriptWriter(mw);
     }
 
     @After
     public void tearDownTest() {
         try { writer.close(); } catch (Exception e) {}
     }
 
     @Test
     public void testIsOpen() {
         assertTrue(writer.isOpen());
     }
 
     @Test
     public void testIsOpenAfterClose() {
         writer.close();
         assertFalse( writer.isOpen() );
     }
 
     @Test
     public void testToString() {
         writer.startAnonFunction("arg1", "arg2", "arg3").endFunction( ItemStatuses.last() );
         mw.close();
         final String result = writer.toString();
 
         assertEquals( "function(arg1, arg2, arg3) {\n}\n", result );
     }
 
     @Test
     public void testWrite() {
         writer.write("test");
         mw.close();
         final String result = writer.toString();
 
         assertEquals( "test", result );
     }
 
     @Test
     public void testWriteLine() {
         writer.writeLine("Line");
         mw.close();
         final String result = writer.toString();
 
         assertEquals( "Line\n", result );
     }
 
     @Test
     public void testWriteChar() {
         writer.write('c');
         mw.close();
         final String result = writer.toString();
 
         assertEquals( "c", result );
     }
 
     @Test
     public void testAnonFunction() {
         writer.startAnonFunction("arg1", "arg2", "arg3").endFunction( ItemStatuses.last() );
         mw.close();
 
         assertEquals("function(arg1, arg2, arg3) {\n}\n", mw.toString());
     }
 
     @Test
     public void testNamedFunction() {
         writer.startNamedFunction("func", "arg1", "arg2", "arg3").endFunction( ItemStatuses.last() );
         mw.close();
 
         assertEquals("function func(arg1, arg2, arg3) {\n}\n", mw.toString());
     }
 
     @Test
     public void testFunctionWithContent() {
         writer.startAnonFunction("arg1", "arg2", "arg3");
         writer.writeLine("return this;").endFunction( ItemStatuses.last() );
         mw.close();
 
         assertEquals("function(arg1, arg2, arg3) {\n    return this;\n}\n", mw.toString());
     }
 
     @Test
     public void testKeyValuesFunction() {
         writer.writeKeyValue("key1", "1", ItemStatuses.notLast());
         writer.writeKeyValue("key2", "2", ItemStatuses.notLast());
         writer.writeKeyValue("key3", "3", ItemStatuses.last());
         mw.close();
 
         assertEquals("key1: 1,\nkey2: 2,\nkey3: 3\n", mw.toString());
     }
 
     @Test
     public void testStartBlock() {
         writer.startBlock();
         mw.close();
 
         assertEquals("{\n", mw.toString());
     }
 
     @Test
     public void testEndBlock() {
         writer.endBlock( ItemStatuses.notLast());
         mw.close();
 
         assertEquals("},\n", mw.toString());
     }
 
     @Test
     public void testEndBlockAsLast() {
         writer.endBlock( ItemStatuses.last());
         mw.close();
 
         assertEquals("}\n", mw.toString());
     }
 
     @Test
     public void testArray() {
         writer.writeArray( ItemStatuses.notLast(), 1, 2, 3);
         mw.close();
 
         assertEquals("[1, 2, 3],\n", mw.toString());
     }
 
     @Test
     public void testArrayAsLast() {
         writer.writeArray( ItemStatuses.last(), 1, 2, 3);
         mw.close();
 
         assertEquals("[1, 2, 3]\n", mw.toString());
     }
 
     @Test(expected = RuntimeException.class)
     public void testUnclosedAnonFunction() {
         writer.startAnonFunction();
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testUnclosedNamedFunction() {
         writer.startNamedFunction("func");
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testTooManyFunctionsClosedNonStarted() {
         writer.endFunction( ItemStatuses.last() );
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testTooManyFunctionsClosed() {
         writer.startAnonFunction();
         writer.endFunction( ItemStatuses.last() );
         writer.endFunction( ItemStatuses.last() );
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testUnclosedBlock() {
         writer.startBlock();
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testTooManyBlockClosedNonStarted() {
         writer.endBlock( ItemStatuses.last());
         writer.close();
     }
     @Test(expected = RuntimeException.class)
     public void testTooManyBlockClosed() {
         writer.startBlock();
         writer.endBlock( ItemStatuses.notLast() );
         writer.endBlock( ItemStatuses.last());
         writer.close();
     }
 
     @Test
     public void testNoSanityChecksAnonFunctions() {
         writer.setLenientMode( true );
         writer.startAnonFunction();
         writer.close();
     }
 
     @Test
     public void testNoSanityChecksNameFunctions() {
         writer.setLenientMode( true );
         writer.startNamedFunction("func");
         writer.close();
     }
 
     @Test
     public void testNoSanityChecksBlocks() {
         writer.setLenientMode( true );
         writer.startBlock();
         writer.close();
     }
 
     @Test
     public void testNoSanityFunctionCall() {
         writer.setLenientMode( true );
         writer.startFunctionCall("foo");
         writer.close();
     }
 
     @Test
     public void testNoSanityFunctionCallBlock() {
         writer.setLenientMode( true );
         writer.startFunctionCallBlock("foo");
         writer.close();
     }
 
     @Test
     public void testSetIndentation() {
         writer.setIndentation(3);
         writer.startBlock();
         writer.endBlock( ItemStatuses.last());
         mw.close();
 
         assertEquals("{\n}\n", mw.toString());
     }
 
     @Test
     public void testSetIndentationChar() {
         writer.setIndentationChar('\t', 2);
         writer.startBlock();
         writer.write("test");
         writer.endBlock( ItemStatuses.last());
         mw.close();
 
         assertEquals("{\n\t\ttest\n}\n", mw.toString());
     }
 
     @Test
      public void testSetLineSeparator() {
         writer.setLineSeparator("l");
         writer.startBlock();
         writer.endBlock( ItemStatuses.last());
         mw.close();
 
         assertEquals("{l}l", mw.toString());
     }
 
     @Test
     public void testSetSpace() {
         writer.setSpace("_SPACE_");
         writer.startNamedFunction("func", "arg1");
         writer.endFunction( ItemStatuses.last() );
         mw.close();
 
         assertEquals("function_SPACE_func(arg1)_SPACE_{\n}\n", mw.toString());
     }
 
     @Test
     public void testWriteVariable() {
         writer.writeVariable("foo", "bar");
         mw.close();
 
         assertThat("var foo = \"bar\";\n", is(mw.toString()));
     }
 
 
     @Test
     public void testWriteVariableType_String() {
         writer.writeVariable("foo", "bar", JavaScriptWriter.VariableType.STRING);
         mw.close();
 
         assertThat("var foo = \"bar\";\n", is(mw.toString()));
     }
 
     @Test
     public void testWriteVariableType_Other() {
         writer.writeVariable("foo", "bar", JavaScriptWriter.VariableType.OTHER);
         mw.close();
 
         assertThat("var foo = bar;\n", is(mw.toString()));
     }
 
     @Test
     public void testWriteVariableType_Block() {
         writer.writeVariable("foo", "bar", JavaScriptWriter.VariableType.BLOCK);
         mw.close();
 
         assertThat("var foo = {\n", is(mw.toString()));
     }
 
     @Test(expected = RuntimeException.class)
     public void testWriteVariable_BlockLenientFails() {
         writer.writeVariable("foo", "bar", JavaScriptWriter.VariableType.BLOCK);
         writer.close();
     }
 
     @Test
     public void testWriteVariableType_BlockLenient() {
         writer.writeVariable("foo", "bar", JavaScriptWriter.VariableType.BLOCK);
         writer.endBlock(ItemStatuses.last());
         writer.close();
 
         assertThat("var foo = {\n}\n", is(mw.toString()));
     }
 
     @Test
     public void testWriteComment() {
         writer.writeComment("Foo Bar");
         mw.close();
 
         assertThat("/*\n * Foo Bar\n */\n", is(mw.toString()));
     }
 
     @Test
     public void testWriteComment_JSONmode() {
         writer.setJSONmode(true);
         writer.writeComment("Foo Bar");
         mw.close();
 
         assertThat("", is(mw.toString()));
     }
 
     @Test
     public void testEndStatement() {
         writer.endStatement();
         mw.close();
 
         assertThat(";\n", is(mw.toString()));
     }
 
     @Test
     public void testStartFunctionCall() {
         writer.startFunctionCall("foo");
         mw.close();
 
         assertThat("foo(", is(mw.toString()));
     }
 
     @Test
     public void testEndFunctionCall() {
         writer.endFunctionCall();
         mw.close();
 
         assertThat(")", is(mw.toString()));
     }
 
     @Test
     public void testStartFunctionCallBlock() {
         writer.startFunctionCallBlock("foo");
         mw.close();
 
         assertThat("foo({\n", is(mw.toString()));
     }
 
     @Test
     public void testEndFunctionCallBlock_NotFirstNorLast() {
         writer.endFunctionCallBlock(ItemStatuses.notFirstNorLast());
         mw.close();
 
         assertThat("}),\n", is(mw.toString()));
     }
 
     @Test
     public void testEndFunctionCallBlock_First() {
         writer.endFunctionCallBlock(ItemStatuses.first());
         mw.close();
 
         assertThat("}),\n", is(mw.toString()));
     }
 
     @Test
     public void testEndFunctionCallBlock_Last() {
         writer.endFunctionCallBlock(ItemStatuses.last());
         mw.close();
 
         assertThat("})\n", is(mw.toString()));
     }
 
     @Test
     public void testEndFunctionCallBlock_FirstAndLast() {
         writer.endFunctionCallBlock(ItemStatuses.last());
         mw.close();
 
         assertThat("})\n", is(mw.toString()));
     }
 
     @Test
     public void testCompleteFunctionCall() {
         writer.startFunctionCall("foo").endFunctionCall();
         writer.close();
         assertThat("foo()", is(mw.toString()));
     }
 
     @Test(expected = RuntimeException.class)
     public void testUnclosedFunctionCall() {
         writer.startFunctionCall("foo");
         writer.close();
     }
 
     @Test(expected = RuntimeException.class)
     public void testTooManyOpenedFunctionCalls() {
         writer.startFunctionCall("foo");
         writer.startFunctionCall("foo");
         writer.endFunctionCall();
         writer.close();
     }
 
     @Test(expected = RuntimeException.class)
     public void testTooManyClosedFunctionCalls() {
         writer.startFunctionCall("foo");
         writer.endFunctionCall();
         writer.endFunctionCall();
         writer.close();
     }
 
 
     @Test
     public void testCompleteFunctionBlockCall() {
         writer.startFunctionCallBlock("foo")
                 .endFunctionCallBlock(ItemStatuses.last());
         writer.close();
         assertThat("foo({\n})\n", is(mw.toString()));
     }
 
     @Test(expected = RuntimeException.class)
     public void testUnclosedFunctionCallBlock() {
         writer.startFunctionCallBlock("foo");
         writer.close();
     }
 
     @Test(expected = RuntimeException.class)
     public void testTooManyOpenedFunctionCallBlocks() {
         writer.startFunctionCallBlock("foo");
         writer.startFunctionCallBlock("foo");
         writer.endFunctionCallBlock(ItemStatuses.notFirstNorLast());
         writer.close();
     }
 
     @Test(expected = RuntimeException.class)
     public void testTooManyClosedFunctionCallBlocks() {
         writer.startFunctionCallBlock("foo");
         writer.endFunctionCallBlock(ItemStatuses.notFirstNorLast());
         writer.endFunctionCallBlock(ItemStatuses.notFirstNorLast());
         writer.close();
     }
 
     @Test
    public void testClosingAnonFunction() {
         writer.startAnonFunction();
         writer.endFunction(ItemStatuses.last());
         writer.close();
 
         assertThat("function() {\n}\n", is(mw.toString()));
     }
 }
