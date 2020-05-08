 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Boyd Hofman
  * Joëll Portier
  * Michiel Westerbeek
  * Tim Waalewijn
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.ica.ap.purify.module.java.unusedmethod;
 
 import java.util.ArrayList;
 
 import nl.han.ica.ap.purify.App;
 import nl.han.ica.ap.purify.language.java.callgraph.CallGraph;
 import nl.han.ica.ap.purify.language.java.callgraph.MethodNode;
 import nl.han.ica.ap.purify.modles.IDetector;
 import nl.han.ica.ap.purify.modles.SourceFile;
 
 /**
  * Detects when a method is not called by anything in the parsed program.
  * 
  * @author Tim
  */
 public class UnusedMethodDetector implements IDetector {	
 	/** An ArrayList containing MethodNodes of the uncalled methods */
 	private CallGraph graph;
 	
 	/**
 	 * Set the used call graph.
 	 * 
 	 * @param graph Call graph to use.
 	 */
 	public void setGraph(CallGraph graph) {
 		this.graph = graph;
 	}
 	
 	@Override
 	public void analyze(SourceFile file) {
 	}
 
 	/**
 	 * Start detecting if there are uncalled methods.
 	 */
 	@Override
 	public void detect() {
 		if (graph == null) {
			App.getCallGraph();
 		}
 		
 		graph.checkIfTruelyCalled();
 		getUnCalledMethods(graph.getAllMethods());
 	}
 	
 	/**
 	 * Returns all MethodNodes that were uncalled.
 	 * 
 	 * @param methods A list with all MethodNodes in the graph.
 	 */
 	private void getUnCalledMethods(ArrayList<MethodNode> methods) {
 		for(MethodNode m : methods) {
 			// If getMemberContxt() returns null the is no method 
 			// (virtual method).
 			if(!m.called && m.getMemberContxt() != null) {	
 				
 				UnusedMethodIssue issue = new UnusedMethodIssue(
 						m.getMemberContxt(), m.getSourceFile());
 				
 				m.getSourceFile().addIssue(issue);
 			}
 		}
 	}
 }
