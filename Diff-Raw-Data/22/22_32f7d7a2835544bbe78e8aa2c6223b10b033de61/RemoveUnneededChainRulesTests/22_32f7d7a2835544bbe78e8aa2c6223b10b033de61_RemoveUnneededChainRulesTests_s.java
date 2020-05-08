 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 public class RemoveUnneededChainRulesTests extends AbstractTestHelper {
 
    @Before
    public void setUp() {
        startServer();
        attachService("/kfgedit/transform/removeUnneededChainRules", RemoveUnneededChainRules.class);
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
    }

     @Test
     public void test1() throws XmlUtilsException, SerializationException {
         final Document grammarDocument = getXmlService().documentFromFile("example_8_14.xml");
         final Document transformedGrammarDocument = getXmlService().documentFromFile(
                 "example_8_14_transformed.xml");
 
         final Grammar expectedGrammar = getGrammarService().grammarFrom(transformedGrammarDocument);
 
         final Document resultDocument = getWebUtilService().post(
                 getServiceUrl("kfgedit/transform/removeUnneededChainRules"), grammarDocument);
 
         Assert.assertEquals(WebServiceResultStatus.SUCCESS,
                 getXmlService().getResultStatus(resultDocument));
 
         final Grammar actualGrammar = getGrammarService().grammarFrom(
                 getXmlService().getContent(resultDocument));
 
         Assert.assertEquals(expectedGrammar, actualGrammar);
     }
     
 }
