 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public final class RemoveEpsilonRulesTests extends AbstractTestHelper {
 
     @Test
     public void test1() throws XmlUtilsException, SerializationException {
         test("example_4_9.xml", "example_4_9_epsilon_free.xml");
     }
 
     @Test
     public void test2() throws XmlUtilsException, SerializationException {
         test("task_4_2.xml", "task_4_2_epsilon_free.xml");
     }
     
     @Test
     public void test3() throws XmlUtilsException, SerializationException {
         test("cue_3_3.xml", "cue_3_3_epsilon_free.xml");
     }
 
     private void test(String grammarFile, String expectedGrammarFile) throws XmlUtilsException,
             SerializationException {
         final Document grammarDocument = getXmlService().documentFromFile(grammarFile);
         final Document epsilonFreeGrammarDocument = getXmlService().documentFromFile(
                 expectedGrammarFile);
 
         final Grammar expectedGrammar = getGrammarService().grammarFrom(epsilonFreeGrammarDocument);
 
         final Document resultDocument = getWebUtilService().post(
                 getServiceUrl("kfgedit/transform/removeEpsilonRules"), grammarDocument);
 
         Assert.assertEquals(WebServiceResultStatus.SUCCESS,
                 getXmlService().getResultStatus(resultDocument));
 
         final Grammar actualGrammar = getGrammarService().grammarFrom(
                 getXmlService().getContent(resultDocument));
 
         Assert.assertEquals(expectedGrammar, actualGrammar);
     }
 }
