 package org.eclipselabs.recommenders.test.codesearch.rcp.dslQL2;
 
 import java.util.Map;
 
 import org.eclipse.recommenders.internal.codesearch.rcp.views.CodeSnippetQLEditorWrapper;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.QL2StandaloneSetup;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.VariableExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.VariableUsage;
 import org.eclipselabs.recommenders.test.codesearch.QLTestBase;
 import org.junit.Test;
 
 public class ExampleTest extends QLTestBase {
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         with(QL2StandaloneSetup.class);
     }
 
     /**
      * Test example queries that are being shown in UI. This is just a
      * syntactical test.
      * 
      * @throws Exception
      */
     @Test
     public void testExamples() throws Exception {
         setUp();
 
         CodeSnippetQLEditorWrapper w = new CodeSnippetQLEditorWrapper();
 
         for (String exampleQuery : w.getExampleQueriesInternal()) {
            IParseResult result = parse(exampleQuery);

            assertFalse(result.hasSyntaxErrors());
 
             Map<String, VariableUsage> map = new VariableExtractor().getVars(result.getRootASTElement());
         }
     }
 }
