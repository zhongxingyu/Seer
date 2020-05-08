 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
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
 package eu.larkc.iris.functional;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cascading.tuple.TupleEntry;
 import cascading.tuple.TupleEntryIterator;
 import eu.larkc.iris.CascadingTest;
 import eu.larkc.iris.evaluation.EvaluationContext;
 import eu.larkc.iris.imports.Importer;
 import eu.larkc.iris.rules.compiler.CascadingRuleCompiler;
 import eu.larkc.iris.rules.compiler.FlowAssembly;
 import eu.larkc.iris.rules.compiler.IDistributedCompiledRule;
 
 /**
  * 
  * @history Nov 19, 2010, vroman, creation
  * @author vroman
  */
 public class ConstantFilterTest extends CascadingTest {
 
 	private static final Logger logger = LoggerFactory.getLogger(ConstantFilterTest.class);
 
 	public ConstantFilterTest(String name) {
		super(name, false);
 	}
 	
 	@Override
 	protected  void createFacts() throws IOException {
 		defaultConfiguration.project = "test";
 		if (enableCluster) {
 			new Importer().importFromFile(defaultConfiguration, defaultConfiguration.project, this.getClass().getResource("/facts/default.nt").getPath(), "import");
 		} else {
 			new Importer().processNTriple(defaultConfiguration, this.getClass().getResource("/facts/default.nt").getPath(), defaultConfiguration.project, "import");
 		}		
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.evaluation.distributed.ProgramEvaluationTest#getRulesFile()
 	 */
 	@Override
 	protected String getRulesFile() {
 		return "/rules/constant_filter.xml";
 	}
 
 	public void testEvaluation() throws Exception {
 		CascadingRuleCompiler crc = new CascadingRuleCompiler(defaultConfiguration);
 		IDistributedCompiledRule dcr = crc.compile(rules.get(0));
 		dcr.evaluate(new EvaluationContext(1, 1));
 		FlowAssembly fa = dcr.getFlowAssembly();
 		
 		TupleEntryIterator tei = fa.openSink();
 		int size = 0;
 		while (tei.hasNext()) {
 			TupleEntry te = tei.next();
 			logger.info(te.getTuple().toString());
 			size++;
 		}
 		assertEquals(1, size);
 	}
 
 }
