 /*******************************************************************************
  * Copyright (c) 2012 Andreas Engelbredt Dalsgaard <andreas.dalsgaard@gmail.com>.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Andreas Engelbredt Dalsgaard <andreas.dalsgaard@gmail.com>
  ******************************************************************************/
 package analyser;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import com.ibm.wala.analysis.pointers.BasicHeapGraph;
 import com.ibm.wala.analysis.pointers.HeapGraph;
 import com.ibm.wala.analysis.reflection.InstanceKeyWithNode;
 import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.ipa.callgraph.AnalysisCache;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
 import com.ibm.wala.ipa.callgraph.AnalysisScope;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.CallGraph;
 import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
 import com.ibm.wala.ipa.callgraph.Context;
 import com.ibm.wala.ipa.callgraph.ContextSelector;
 import com.ibm.wala.ipa.callgraph.Entrypoint;
 import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
 import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
 import com.ibm.wala.ipa.callgraph.impl.Util;
 import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.AbstractLocalPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.AbstractTypeInNode;
 import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNodeFactory;
 import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
 import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
 import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
 import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
 import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
 import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
 import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.ExceptionReturnValueKey;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
 import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
 import com.ibm.wala.ipa.cha.ClassHierarchy;
 import com.ibm.wala.ipa.cha.IClassHierarchy;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.io.CommandLine;
 
 
 public class ScjMemoryScopeAnalysis {
 
 	public static void main(String[] args) throws WalaException {
 		String primordial, main;
 		Properties p = CommandLine.parse(args);
 		int problemCounter = 0;
 		
 		if (p.getProperty("application") == null ) 		
 			util.print_usage();
  
 		if (p.getProperty("main") != null)
 			main = p.getProperty("main");
 		else
 			main = "Main";
 		
 		if (p.getProperty("primordial") != null)
 			primordial = p.getProperty("primordial");
 		else
 			primordial = null;
 			
 		try {
 			Set<Problem> problems = buildPointsTo(p.getProperty("application"), main, primordial);
 			
 			Iterator<Problem> pItr = problems.iterator();			
 			System.out.print("Problems:\n");
 			
 			while ( pItr.hasNext() )
 			{	
 				String strNext = pItr.next().toString();								
 				if (!strNext.isEmpty()) {
 					System.out.print(strNext+"\n");
 					problemCounter++;
 				}
 			}
 			
 			System.out.print("Nr. of problems: "+problemCounter+"\n");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Set<Problem> buildPointsTo(String application, String main_class, String primordial) throws WalaException, IllegalArgumentException, CancelException, IOException, InvalidClassFileException {	
 		
 	    AnalysisScope scope = MyAnalysisScopeReader.makeJavaBinaryAnalysisScope(application, primordial, null);
 	    ClassHierarchy cha = ClassHierarchy.make(scope);
 	    Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha, "L"+main_class);	    
 	    AnalysisOptions options = new AnalysisOptions(scope, entrypoints);   
 	    options.setReflectionOptions(ReflectionOptions.NONE);
 	    
 	    ContextSelector scjContextSelector = new ScjContextSelector(cha);	    
 		com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = ZeroXCFABuilder(options, new AnalysisCache(), cha, scope, scjContextSelector);	    
 	    CallGraph cg = builder.makeCallGraph(options,null);
 	    PointerAnalysis pointerAnalysis = builder.getPointerAnalysis(); 	   
 	    BasicHeapGraph bhg = new BasicHeapGraph(pointerAnalysis, cg); 
 	    HashSet<Problem> problems = new HashSet<Problem>();    
 	    
 	    runAnalysis(bhg.getPointerAnalysis(), (HeapGraph)bhg, problems);	
 	    
 		return problems; 
   }	
 	
 	
 	public static void runAnalysis(PointerAnalysis pa, HeapGraph hg, HashSet<Problem> problems)
 	{
 		
 	    Iterator<InstanceKey> ikItr = pa.getInstanceKeys().iterator();	
 		int i = 0;
 		
 	    while( ikItr.hasNext() )
 	    {	    	
 	    	InstanceKey ik = ikItr.next();
     		Iterator<Object> pkIter = hg.getPredNodes(ik);
     		
     		if (getScjContext(ik) == null) {
 	    		i++;
 	    	} else 
 	    	{    		
 	    		
 	    		
 	    		while (pkIter.hasNext())
 	    		{
 	    			Object pk = pkIter.next();	    		
 	    			
 	    			if (!getScjContext(ik).less(getScjContext(pk)))
 	    				report_problems(problems, ik, pk);	    			
     			}
     		}	    	
 	    }
 	    
	    if (i > 10)
 	    	util.error("Unexpected number: "+i+" of InstanceKey objects with context == Null");
 	}
 
 	
 	private static void report_problems(HashSet<Problem> problems,
 			InstanceKey ik, Object pk) {
 		
 		if (ik instanceof InstanceKeyWithNode) {
 			InstanceKeyWithNode ikn = (InstanceKeyWithNode) ik;
 			if (pk instanceof InstanceFieldKey){
 				problems.add(new ProblemField(ikn, (InstanceFieldKey)pk));
 			} else if (pk instanceof StaticFieldKey) {
 				problems.add(new ProblemStaticField(ikn, (StaticFieldKey)pk));
 			} else if (pk instanceof ExceptionReturnValueKey) {	//XXX: We currently do not handle exceptions
 				util.warnException();
 			} else if (pk instanceof AbstractLocalPointerKey) {
 				problems.add(new ProblemPkIk(ikn, (AbstractLocalPointerKey)pk));
 			} else{		
 				if (pk instanceof FilteredPointerKey)
 				{
 					problems.add(new ProblemUnknown("Filtered mismatch is node: "+ pk.getClass() + "scopes: "+getScjContext(pk)+ " result: " + getScjContext(ik)+
 						"\n   instance of class: "+ikn.getNode().getMethod().getDeclaringClass() + " in method: " + ikn.getNode().getMethod().getName()+ "\n"));
 				} else {
 					problems.add(new ProblemUnknown("Unknown mismatch is node: "+ pk.getClass() + "scopes: "+getScjContext(pk)+ " result: " + getScjContext(ik)+
 							"\n   instance of class: "+ikn.getNode().getMethod().getDeclaringClass() + " in method: " + ikn.getNode().getMethod().getName()+ "\n"));
 				}
 			}
 		} else {	    					
 			problems.add(new ProblemUnknown("Unknown mismatch is: "+ pk.getClass() + "scopes: "+getScjContext(pk)+ " result: " + getScjContext(ik)+"\n"));	    				
 		}		
 	}
 
 	private static ScjContext getScjContext(Object o) 
 	{
 		  Context context = null;
 		  
 		  if(o instanceof AbstractTypeInNode) { // In case o is an instancekey this is  
 			  context = ((AbstractTypeInNode) o).getNode().getContext();			  
 		  } else if(o instanceof AbstractLocalPointerKey) {
 			  context = ((AbstractLocalPointerKey) o).getNode().getContext();			  
 		  } else if (o instanceof AbstractFieldPointerKey) {			  
 			  context = getScjContext(((AbstractFieldPointerKey) o).getInstanceKey());		  		  
 		  } else if (o instanceof StaticFieldKey) {             
 			  context = new ScjContext(null, "Ljavax/realtime/ImmortalMemory", ScjScopeType.IMMORTAL); 
 		  }
 		  
 		  if (context instanceof ScjContext)
 			  return (ScjContext) context;
 		  
 		  return null;		  
 	}
 	
 	private static CallGraphBuilder ZeroXCFABuilder(AnalysisOptions options,
 			AnalysisCache cache, ClassHierarchy cha, AnalysisScope scope,
 			ContextSelector customSelector) {
 
 		if (options == null) {
 			throw new IllegalArgumentException("options is null");
 		}
 
 		Util.addDefaultSelectors(options, cha);
 		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(),
 				cha);		
 			
 		return new ZeroXCFABuilder(cha, options, cache, customSelector, 
 			 null, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY					
 						);
 
 	}
 
 }
 
 
 
 
