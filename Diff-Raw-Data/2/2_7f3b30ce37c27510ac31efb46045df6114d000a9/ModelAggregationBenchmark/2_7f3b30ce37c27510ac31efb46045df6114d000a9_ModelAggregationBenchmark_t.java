 /*******************************************************************************
  * Copyright (c) 2013 CWI
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
  *******************************************************************************/
 package org.eclipse.imp.pdb.values.benchmarks;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.io.binary.BinaryReader;
 import org.junit.Test;
 
 import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
 import com.carrotsearch.junitbenchmarks.Clock;
 
 @BenchmarkOptions(clock = Clock.NANO_TIME)
 public class ModelAggregationBenchmark extends AbstractJUnitBenchmark {
 	
 	public ModelAggregationBenchmark(IValueFactory valueFactory) throws Exception {
 		super(valueFactory);
 		setUpStaticValueFactorySpecificTestData();
 	}
 	
 	private static IValue[] constructorValues;
 	
 	private static String[] relationNames = new String[] { 
 			"methodBodies",
 			"classes",
 			"methodDecls",
 			"packages",
 			"fieldDecls",
 			"implements",
 			"methods",
 			"declaredFields",
 			"calls",
 			"variables",
 			"declaredMethods",
 			"types",
 			"modifiers",
 //			"declaredTopTypes" // ISet
 	};
 	
 	private static ISet[] unionRelations;
 
 //	@Before
 //	public void setUp() throws Exception {
 //		super.setUp();
 //	}		
 	
 	public void setUpStaticValueFactorySpecificTestData() throws Exception {
 		String resourcePrefixRelativeToClass = "model-aggregation";
 		List<String> resources = new ArrayList<>();
 		
 		try (
				InputStream inputStream = ModelAggregationBenchmark.class.getResourceAsStream(resourcePrefixRelativeToClass + "/" + "index.txt");
 				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 			) {
 
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				resources.add(resourcePrefixRelativeToClass + "/" + line);
 			}
 			
 		}
 		constructorValues = (IValue[]) readValuesFromFiles(this.getClass(), resources);
 
 				
 		// TODO: load from serialized files instead of computing.
 		unionRelations = unionRelations();
 	}
 	
 	private IValue[] readValuesFromFiles(Class<?> clazz, List<String> resources) throws Exception {
 		IValue[] values = new IValue[resources.size()];
 				
 		for (int i = 0; i < resources.size(); i++) {
 			try (InputStream inputStream = clazz.getResourceAsStream(resources.get(i))) {
 				
 				BinaryReader binaryReader = new BinaryReader(valueFactory, typeStore, inputStream);
 				values[i] = binaryReader.deserialize();
 			}
 		}
 		
 		return values;
 	}
 	
 	public ISet[] unionRelations() throws Exception {
 		
 		// initialize
 		ISet[] relations = new ISet[relationNames.length];
 		for (int i = 0; i < relations.length; i++) {
 			relations[i] = valueFactory.set();
 		}
 
 		// compute / accumulate
 		for (IValue value : constructorValues) {
 			IConstructor constructor = (IConstructor) value;
 
 			for (int i = 0; i < relations.length; i++) {
 				String relationName = relationNames[i];
 				ISet one = relations[i];
 				ISet two = (ISet) constructor.asAnnotatable().getAnnotation(relationName);
 				
 				relations[i] = one.union(two); 
 			}		
 		}
 				
 		return relations;
 	}
 
 	@Test
 	public void timeUnionRelations() throws Exception {
 		unionRelations();
 	}	
 	
 	public ISet[] subtractRelations() throws Exception {
 		
 		// initialize
 		ISet[] relations = unionRelations;
 			
 		// compute / accumulate		
 		for (IValue value : constructorValues) {
 			IConstructor constructor = (IConstructor) value;
 
 			for (int i = 0; i < relations.length; i++) {
 				String relationName = relationNames[i];
 				ISet one = relations[i];
 				ISet two = (ISet) constructor.asAnnotatable().getAnnotation(relationName);
 				
 				relations[i] = one.subtract(two); 
 			}		
 		}
 				
 		return relations;
 	}
 	
 	@Test
 	public void timeSubtractRelations() throws Exception {
 		subtractRelations();
 	}	
 
 	public ISet[] intersectRelations() throws Exception {
 		
 		// initialize
 		ISet[] relations = unionRelations;
 				
 		// compute / accumulate
 		for (IValue value : constructorValues) {
 			IConstructor constructor = (IConstructor) value;
 
 			for (int i = 0; i < relations.length; i++) {
 				String relationName = relationNames[i];
 				ISet one = relations[i];
 				ISet two = (ISet) constructor.asAnnotatable().getAnnotation(relationName);
 				
 				relations[i] = one.intersect(two); 
 			}		
 		}
 				
 		return relations;
 	}
 	
 	@Test
 	public void timeIntersectRelations() throws Exception {
 		intersectRelations();
 	}	
 	
 }
