 /**
  * 
  */
 package org.eclipse.dltk.javascript.core.tests.contentassist;
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.internal.javascript.typeinference.HostCollection;
 
 /**
  * @author jcompagner
  * 
  */
 public class CompletionStringParsing extends TestCase {
 
 	public void testCompletionWithArrayDotResolving() throws Exception {
 
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx.yyy", true));
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx.yyy()", true));
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx().yyy()", true));
 		assertEquals("forms.xxx.[].yyy.[]", HostCollection
 				.parseCompletionString("forms.xxx[].yyy[]", true));
 		assertEquals("forms.[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx].yyy", true));
 		assertEquals("forms.[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx()].yyy", true));
 		assertEquals("forms.[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx(1)].yyy", true));
 		assertEquals("xxx", HostCollection.parseCompletionString("forms[xxx",
 				true));
 		assertEquals("", HostCollection.parseCompletionString("forms[xxx(",
 				true));
 		assertEquals("a", HostCollection.parseCompletionString("forms[xxx(a",
 				true));
 		assertEquals("xxx", HostCollection.parseCompletionString(
 				"forms[xxx(1)", true));
 		assertEquals("xxx.yyy", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy]", true));
 
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy]]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[xxx(1)).yyy]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[[xxx(1).yyy]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[xxx((1).yyy]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[[xxx((1).yyy]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				"forms[xxx((1).yyy]]", true));
 		assertEquals("forms.[]", HostCollection.parseCompletionString(
 				")forms[xxx(1)).yyy]", true));
 
 	}
 	
 	public void testCompletionWithoutArrayDotResolving() throws Exception {
 
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx.yyy", false));
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx.yyy()", false));
 		assertEquals("forms.xxx.yyy", HostCollection.parseCompletionString(
 				"forms.xxx().yyy()", false));
 		assertEquals("forms.xxx[].yyy[]", HostCollection
 				.parseCompletionString("forms.xxx[].yyy[]", false));
 		assertEquals("forms[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx].yyy", false));
 		assertEquals("forms[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx()].yyy", false));
 		assertEquals("forms[].yyy", HostCollection.parseCompletionString(
 				"forms[xxx(1)].yyy", false));
 		assertEquals("xxx", HostCollection.parseCompletionString("forms[xxx",
 				false));
 		assertEquals("", HostCollection.parseCompletionString("forms[xxx(",
 				false));
 		assertEquals("a", HostCollection.parseCompletionString("forms[xxx(a",
 				false));
 		assertEquals("xxx", HostCollection.parseCompletionString(
 				"forms[xxx(1)", false));
 		assertEquals("xxx.yyy", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy", false));
 		assertEquals("forms[]", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy]", false));
 
 		assertEquals("forms[]", HostCollection.parseCompletionString(
 				"forms[xxx(1).yyy]]", false));
 		assertEquals("forms[]", HostCollection.parseCompletionString(
 				"forms[xxx(1)).yyy]", false));
 		assertEquals("forms[]", HostCollection.parseCompletionString(
 				")forms[xxx(1)).yyy]", false));
 
 	}
 	
 	public void testDoubleParsing() throws Exception
 	{
 		assertEquals("forms.xxx.[].yyy.[]", HostCollection
 				.parseCompletionString("forms.xxx.[].yyy.[]", true));
 
 	}
 	
 	public void testWithLineEnding() throws Exception
 	{
 		assertEquals("forms.xxx.[].yyy.[]", HostCollection
 				.parseCompletionString("someCode\nforms.xxx[xxx].yyy[yyy]", true));
 		assertEquals("", HostCollection
 				.parseCompletionString("someCode\nforms.xxx[xxx].yyy[yyy]\n", true));
 
 	}
 	
 	public void testWithWhiteSpace() throws Exception
 	{
 		assertEquals("forms.xxx.[].yyy.[]", HostCollection
 				.parseCompletionString("someCode forms.xxx[xxx].yyy[yyy]", true));
 		assertEquals("", HostCollection
 				.parseCompletionString("someCode forms.xxx[xxx].yyy[yyy] ", true));
 
 	}
 	
	
	public void testWithMultiplyParams() throws Exception
	{
		assertEquals("forms.xxx.[].yyy.[]", HostCollection
				.parseCompletionString("someCode forms.xxx[xxx,yyy].yyy[xxx,yyy]", true));
		assertEquals("forms.xxx.[].yyy.[]", HostCollection
				.parseCompletionString("someCode forms.xxx[xxx , yyy].yyy[xxx , yyy, yyy]", true));
		assertEquals("a", HostCollection
				.parseCompletionString("someCode forms.xxx[xxx,yyy].yyy[a", true));
		assertEquals("a", HostCollection
				.parseCompletionString("someCode forms.xxx[xxx , yyy].yyy[xxx,a", true));
		assertEquals("a", HostCollection
				.parseCompletionString("someCode forms.xxx[xxx , yyy].yyy[xxx , a", true));
	}
	
 	public void testWithString() throws Exception
 	{
 		assertEquals("forms.xxx.[].yyy.[]", HostCollection
 				.parseCompletionString("forms.xxx['xxx'].yyy[\"yyy\"]", true));
		assertEquals("forms.xxx.[].yyy.[]", HostCollection
				.parseCompletionString("forms.xxx['xxx yyy'].yyy[\"xxx yyy\"]", true));
		assertEquals("forms.xxx.[].yyy.[]", HostCollection
				.parseCompletionString("forms.xxx['xxx yyy'].yyy[\"xxx 'yyy'\"]", true));
		assertEquals("\"xxx 'yyy'\"", HostCollection
				.parseCompletionString("\"xxx 'yyy'\"", true));
		assertEquals("'xxx \"yyy\"'", HostCollection
				.parseCompletionString("'xxx \"yyy\"'", true));
 	}
 }
