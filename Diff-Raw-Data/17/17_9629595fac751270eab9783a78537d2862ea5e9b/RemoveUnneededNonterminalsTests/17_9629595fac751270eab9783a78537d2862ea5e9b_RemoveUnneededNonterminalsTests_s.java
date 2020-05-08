 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public class RemoveUnneededNonterminalsTests extends AbstractTestHelper {
 
     @Test
     public void test1() throws XmlUtilsException, SerializationException {
         test("example_8_14.xml", "example_8_14_transformed.xml");
     }
 
     @Test
     public void test2() throws XmlUtilsException, SerializationException {
        test("cue_3_2.xml", "cue_3_2_transformed.xml");
     }
     
     @Test
     public void test3() throws XmlUtilsException, SerializationException {
        test("cue_3_3.xml", "cue_3_3_transformed.xml");
     }
 
     private void test(String grammarFile, String expectedGrammarFile) throws XmlUtilsException,
             SerializationException {
         final Document grammarDocument = getXmlService().documentFromFile(grammarFile);
         final Document transformedGrammarDocument = getXmlService().documentFromFile(
                 expectedGrammarFile);
 
         final Grammar expectedGrammar = getGrammarService().grammarFrom(transformedGrammarDocument);
 
         final Document resultDocument = getWebUtilService().post(
                 getServiceUrl("kfgedit/transform/removeUnneededNonterminals"), grammarDocument);
 
         Assert.assertEquals(WebServiceResultStatus.SUCCESS,
                 getXmlService().getResultStatus(resultDocument));
 
         final Grammar actualGrammar = getGrammarService().grammarFrom(
                 getXmlService().getContent(resultDocument));
 
         Assert.assertEquals(expectedGrammar, actualGrammar);
     }
 }
