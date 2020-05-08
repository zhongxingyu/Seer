 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawb.common.ui.plot.function;
 
 import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
 
 import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
 //import uk.ac.diamond.scisoft.analysis.fitting.functions.CubicSpline;
 //import uk.ac.diamond.scisoft.analysis.fitting.functions.GaussianND;
 //import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
 //import uk.ac.diamond.scisoft.analysis.fitting.functions.Step;
 
 /**
  * These are the functions editable with the edit table and as such 
  * are not *all* those available from the FunctionFactory 
  * 
  * This class now uses FunctionFactory, although this does not actually
  * make much difference as it was fine as it was. It does mean that 
  * package private constructors could be used for functions and 
 * package private classes to make functions truely swappable at some
  * point in the future.
  */
 public enum FunctionType {
 
 	BOX("Box"),
 	CUBIC("Cubic"),
 //	CUBIC_SPLINE(CubicSpline.class),
 	FERMI("Fermi"),
 	FERMIGAUSS("Fermi * Gaussian"),
 	GAUSSIAN("Gaussian"),
 //	GAUSSIAN_ND(GaussianND.class),
 	LORENTZIAN("Lorentzian"),
 //	OFFSET(Offset.class),
 	PEARSON_VII("PearsonVII"),
 	POLYNOMIAL("Polynomial"),
 	PSEUDO_VOIGT("PseudoVoigt"),
 	QUADRATIC("Quadratic"),
 //	STEP(Step.class),
 	STRAIGHT_LINE("Linear");
 	
 	private String functionName;
 
 	FunctionType(String functionName) {
 		this.functionName = functionName;
 	}
 	
 	public int getIndex() {
 		final FunctionType[] ops = FunctionType.values();
 		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
 		return -1;
 	}
 
 	public static String[] getTypes() {
 		final FunctionType[] ops = FunctionType.values();
 		final String[] names = new String[ops.length];
 		for (int i = 0; i < ops.length; i++) {
 			names[i] = ops[i].getName();
 		}
 		return names;
 	}
 
 	public String getName() {
 		return functionName;
 	}
 
 	public static FunctionType getType(int index) {
 		final FunctionType[] ops = FunctionType.values();
 		return ops[index];
 	}
 
 	public IFunction getFunction() throws Exception {
 		return FunctionFactory.getFunction(functionName);
 	}
 
 	public static int getIndex(Class<? extends IFunction> class1) {
 		
 		try {
 			String name = FunctionFactory.getName(class1);
 			final FunctionType[] ops = FunctionType.values();
 			for (FunctionType functionType : ops) {
 				if (functionType.functionName == name) return functionType.getIndex();
 			}
 		} catch (Exception e) {
 			return -1;
 		}
 		return -1;
 	}
 
 	public static IFunction createNew(int selectionIndex) throws Exception {
 		final FunctionType function = getType(selectionIndex);
 		return FunctionFactory.getFunction(function.functionName);
 	}
 
 	public static IFunction createNew(FunctionType function) throws Exception {
 		return FunctionFactory.getFunction(function.functionName);
 	}
 }
