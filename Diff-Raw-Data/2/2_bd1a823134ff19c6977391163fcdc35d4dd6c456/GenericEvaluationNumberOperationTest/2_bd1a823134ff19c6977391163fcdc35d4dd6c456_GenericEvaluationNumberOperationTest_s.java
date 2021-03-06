 /**
  * Copyright (c) 2009, 2011 Obeo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *     Axel Uhl (SAP AG) - Bug 342644
  */
 package org.eclipse.ocl.tests;
 
 import java.math.BigInteger;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.ocl.expressions.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.options.ParsingOptions;
 
 //FIXME we're missing oclIsNew and oclIsInState
 /**
  * This unit test focuses on the Standard library's Integer and Real operations.
  * The unlimited natural will be considered as conforming to Integer. This will
  * need revisting once bug 261008 is fixed.
  * 
  * @author Laurent Goubet (lgoubet)
  */
 @SuppressWarnings("nls")
 public abstract class GenericEvaluationNumberOperationTest<E extends EObject, PK extends E, T extends E, C extends T, CLS extends C, DT extends C, PT extends C, ET extends DT, O extends E, PM extends E, P extends E, PA extends P, PR extends P, EL, S, COA, SSA, CT>
 extends GenericEvaluationTestSuite<E, PK, T, C, CLS, DT, PT, ET, O, PM, P, PA, PR, EL, S, COA, SSA, CT> {
 
 	public void testNumberAbs() {
 		// Integer::abs()
 		assertResult(Integer.valueOf(3), "3.abs()");
 		assertResult(Integer.valueOf(3), "(-3).abs()");
 
 		assertResult(2147483647, "2147483647.abs()");
 		assertParserError("2147483648.abs()", "For input string: \"{0}\"", "2147483648");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
         assertResult(2147483648L, "2147483648.abs()");
 		assertResult(2147483649L, "(-2147483649).abs()");
 		assertResult(2147483648L, "(-2147483648).abs()");
 
 		// Real::abs()
 		assertResult(Double.valueOf(3d), "(3.0).abs()");
 		assertResult(Double.valueOf(3d), "(-3.0).abs()");
 		assertResult(Double.valueOf(3.1758d), "(3.1758).abs()");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberAbsInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i.abs()");
 		assertResultInvalid("let r : Real = invalid in r.abs()");
 	}
 
 	public void testNumberAbsNull() {
 		assertResultInvalid("let i : Integer = null in i.abs()");
 		assertResultInvalid("let r : Real = null in r.abs()");
 	}
 
 	public void testNumberDiv() {
 		assertResult(Integer.valueOf(1), "3.div(2)");
 		assertResult(Integer.valueOf(-1), "(-3).div(2)");
 		assertResult(Integer.valueOf(-1), "3.div(-2)");
 		assertResult(Integer.valueOf(1), "(-3).div(-2)");
 	}
 
 	public void testNumberDivByZero() {
 		assertResultInvalid("1.div(0)");
 	}
 
 	public void testNumberDivide() {
 		// A.2.1.3 Contrary to other operations, "Integer x Integer -> Real"
 		// Integer::/(Integer)
 		assertResult(Double.valueOf(1d), "1 / 1");
 		assertResult(Double.valueOf(-0.25d), "1 / -4");
 
 		// Integer::/(Real)
 		assertResult(Double.valueOf(1d), "1 / 1.0");
 		assertResult(Double.valueOf(-0.25d), "1 / -4.0");
 
 		// Real::/(Integer)
 		assertResult(Double.valueOf(1d), "1.0 / 1");
 		assertResult(Double.valueOf(-0.25d), "1.0 / -4");
 
 		// Real::/(Real)
 		assertResult(Double.valueOf(1d), "1.0 / 1.0");
 		assertResult(Double.valueOf(1.11d / 1.12d), "1.11 / 1.12");
 	}
 
 	public void testNumberDivideByZero() {
 		assertResultInvalid("1 / 0");
 		assertResultInvalid("1.0 / 0");
 		assertResultInvalid("1 / 0.0");
 		assertResultInvalid("1.0 / 0.0");
 	}
 
 	public void testNumberDivideInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1 / i");
 		assertResultInvalid("let i : Integer = invalid in i / 1");
 		assertResultInvalid("let r : Real = invalid in 1 / r");
 		assertResultInvalid("let r : Real = invalid in r / 1");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 / i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 / r2");
 	}
 
 	public void testNumberDivideNull() {
 		assertResultInvalid("let i : Integer = null in 1 / i");
 		assertResultInvalid("let i : Integer = null in i / 1");
 		assertResultInvalid("let r : Real = null in 1 / r");
 		assertResultInvalid("let r : Real = null in r / 1");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 / i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 / r2");
 	}
 
 	public void testNumberDivInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1.div(i)");
 		assertResultInvalid("let i : Integer = invalid in i.div(1)");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1.div(i2)");
 	}
 
 	public void testNumberDivNull() {
 		assertResultInvalid("let i : Integer = null in 1.div(i)");
 		assertResultInvalid("let i : Integer = null in i.div(1)");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1.div(i2)");
 	}
 
 	public void testNumberEqual() {
 		assertResultFalse("4 = 5");
 		assertResultFalse("1 = 4.0");
 		assertResultFalse("1.0 = 4");
 		assertResultFalse("1.0 = 4.0");
 
 		assertResultTrue("4 = 4");
 		assertResultTrue("1 = 1.0");
 		assertResultTrue("1.0 = 1");
 		assertResultTrue("1.0 = 1.0");
 	}
 
 	public void testNumberEqualInvalid() {
 		// operation invocations on invalid except for oclIsInvalid and oclIsUndefined yield invalid
 		assertResultInvalid("let i : Integer = invalid in i = 0");
 		assertResultInvalid("let i : Integer = invalid in -1 = i");
 		assertResultInvalid("let r : Real = invalid in r = 0.0");
 		assertResultInvalid("let r : Real = invalid in -1.0 = r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 = i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 = r2");
 	}
 
 	public void testNumberEqualNull() {
 		assertResultFalse("let i : Integer = null in i = 0");
 		assertResultFalse("let i : Integer = null in -1 = i");
 		assertResultFalse("let r : Real = null in r = 0.0");
 		assertResultFalse("let r : Real = null in -1.0 = r");
 
 		assertResultTrue("let i1 : Integer = null, i2 : Integer = null in i1 = i2");
 		assertResultTrue("let r1 : Real = null, r2 : Real = null in r1 = r2");
 	}
 
 	public void testNumberFloor() {
 		// Integer::floor()
 		assertResult(Integer.valueOf(3), "3.floor()");
 		assertResult(Integer.valueOf(-3), "(-3).floor()");
 
 		// Real::floor()
 		assertResult(Integer.valueOf(-2), "(-1.5).floor()");
 		assertResult(Integer.valueOf(1), "(1.01).floor()");
 		assertResult(Integer.valueOf(3), "(3.999).floor()");
 	}
 
 	public void testNumberFloorInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i.floor()");
 		assertResultInvalid("let r : Real = invalid in r.floor()");
 	}
 
 	public void testNumberFloorNull() {
 		assertResultInvalid("let i : Integer = null in i.floor()");
 		assertResultInvalid("let r : Real = null in r.floor()");
 	}
 
 	public void testNumberGreaterThan() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::greaterThan(Integer)
 		assertResultTrue("3 > 2");
 		assertResultFalse("-3 > 2");
 		assertResultTrue("3 > -2");
 		assertResultFalse("-3 > -2");
 
 		assertQueryTrue(null, "2147483648 > 2147483647");
 		assertQueryFalse(null, "2147483647 > 2147483648");
 		assertQueryFalse(null, "-2147483649 > -2147483648");
 		assertQueryTrue(null, "-2147483648 > -2147483649");
 
 		assertQueryTrue(null, "2147483648 >= 2147483647");
 		assertQueryFalse(null, "2147483647 >= 2147483648");
 		assertQueryFalse(null, "-2147483649 >= -2147483648");
 		assertQueryTrue(null, "-2147483648 >= -2147483649");
 
 		// Real::greaterThan(Real)
 		assertResultTrue("3.0 > 2.0");
 		assertResultFalse("-3.0 > 2.0");
 		assertResultTrue("3.0 > -2.0");
 		assertResultFalse("-3.0 > -2.0");
 
 		// Real::greaterThan(Integer)
 		assertResultFalse("3.0 > 3");
 		assertResultFalse("-3.0 > 3");
 		assertResultTrue("3.0 > -3");
 		assertResultFalse("-3.0 > -3");
 
 		// Integer::greaterThan(Real)
 		assertResultFalse("3 > 3.0");
 		assertResultFalse("-3 > 3.0");
 		assertResultTrue("3 > -3.0");
 		assertResultFalse("-3 > -3.0");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberGreaterThanInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i > 0");
 		assertResultInvalid("let i : Integer = invalid in 0 > i");
 		assertResultInvalid("let r : Real = invalid in r > 0");
 		assertResultInvalid("let r : Real = invalid in 0 > r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 > i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 > r2");
 	}
 
 	public void testNumberGreaterThanNull() {
 		assertResultInvalid("let i : Integer = null in i > 0");
 		assertResultInvalid("let i : Integer = null in 0 > i");
 		assertResultInvalid("let r : Real = null in r > 0");
 		assertResultInvalid("let r : Real = null in 0 > r");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 > i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 > r2");
 	}
 
 	public void testNumberGreaterThanOrEqual() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::greaterThanOrEqual(Integer)
 		assertResultTrue("3 >= 2");
 		assertResultFalse("-3 >= 2");
 		assertResultTrue("3 >= -2");
 		assertResultFalse("-3 >= -2");
 
 		assertQueryTrue(null, "2147483648 >= 2147483647");
 		assertQueryFalse(null, "2147483647 >= 2147483648");
 		assertQueryFalse(null, "-2147483649 >= -2147483648");
 		assertQueryTrue(null, "-2147483648 >= -2147483649");
 
 		// Real::greaterThanOrEqual(Real)
 		assertResultTrue("3.0 >= 2.0");
 		assertResultFalse("-3.0 >= 2.0");
 		assertResultTrue("3.0 >= -2.0");
 		assertResultFalse("-3.0 >= -2.0");
 
 		// Real::greaterThanOrEqual(Integer)
 		assertResultTrue("3.0 >= 3");
 		assertResultFalse("-3.0 >= 3");
 		assertResultTrue("3.0 >= -3");
 		assertResultTrue("-3.0 >= -3");
 
 		// Integer::greaterThanOrEqual(Real)
 		assertResultTrue("3 >= 3.0");
 		assertResultFalse("-3 >= 3.0");
 		assertResultTrue("3 >= -3.0");
 		assertResultTrue("-3 >= -3.0");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 	}
 
 	public void testNumberGreaterThanOrEqualInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i >= 0");
 		assertResultInvalid("let i : Integer = invalid in 0 >= i");
 		assertResultInvalid("let r : Real = invalid in r >= 0");
 		assertResultInvalid("let r : Real = invalid in 0 >= r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 >= i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 >= r2");
 	}
 
 	public void testNumberGreaterThanOrEqualNull() {
 		assertResultInvalid("let i : Integer = null in i >= 0");
 		assertResultInvalid("let i : Integer = null in 0 >= i");
 		assertResultInvalid("let r : Real = null in r >= 0");
 		assertResultInvalid("let r : Real = null in 0 >= r");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 >= i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 >= r2");
 	}
 
 	public void testNumberLessThan() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::lessThan(Integer)
 		assertResultFalse("3 < 2");
 		assertResultTrue("-3 < 2");
 		assertResultFalse("3 < -2");
 		assertResultTrue("-3 < -2");
 
 		assertQueryFalse(null, "2147483648 < 2147483647");
 		assertQueryTrue(null, "2147483647 < 2147483648");
 		assertQueryTrue(null, "-2147483649 < -2147483648");
 		assertQueryFalse(null, "-2147483648 < -2147483649");
 
 		// Real::lessThan(Real)
 		assertResultFalse("3.0 < 2.0");
 		assertResultTrue("-3.0 < 2.0");
 		assertResultFalse("3.0 < -2.0");
 		assertResultTrue("-3.0 < -2.0");
 
 		// Real::lessThan(Integer)
 		assertResultFalse("3.0 < 3");
 		assertResultTrue("-3.0 < 3");
 		assertResultFalse("3.0 < -3");
 		assertResultFalse("-3.0 < -3");
 
 		// Integer::lessThan(Real)
 		assertResultFalse("3 < 3.0");
 		assertResultTrue("-3 < 3.0");
 		assertResultFalse("3 < -3.0");
 		assertResultFalse("-3 < -3.0");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberLessThanInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i < 0");
 		assertResultInvalid("let i : Integer = invalid in 0 < i");
 		assertResultInvalid("let r : Real = invalid in r < 0");
 		assertResultInvalid("let r : Real = invalid in 0 < r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 < i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 < r2");
 	}
 
 	public void testNumberLessThanNull() {
 		assertResultInvalid("let i : Integer = null in i < 0");
 		assertResultInvalid("let i : Integer = null in 0 < i");
 		assertResultInvalid("let r : Real = null in r < 0");
 		assertResultInvalid("let r : Real = null in 0 < r");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 < i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 < r2");
 	}
 
 	public void testNumberLessThanOrEqual() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::lessThanOrEqual(Integer)
 		assertResultFalse("3 <= 2");
 		assertResultTrue("-3 <= 2");
 		assertResultFalse("3 <= -2");
 		assertResultTrue("-3 <= -2");
 
 		assertQueryFalse(null, "2147483648 <= 2147483647");
 		assertQueryTrue(null, "2147483647 <= 2147483648");
 		assertQueryTrue(null, "-2147483649 <= -2147483648");
 		assertQueryFalse(null, "-2147483648 <= -2147483649");
 
 		// Real::lessThanOrEqual(Real)
 		assertResultFalse("3.0 <= 2.0");
 		assertResultTrue("-3.0 <= 2.0");
 		assertResultFalse("3.0 <= -2.0");
 		assertResultTrue("-3.0 <= -2.0");
 
 		// Real::lessThanOrEqual(Integer)
 		assertResultTrue("3.0 <= 3");
 		assertResultTrue("-3.0 <= 3");
 		assertResultFalse("3.0 <= -3");
 		assertResultTrue("-3.0 <= -3");
 
 		// Integer::lessThanOrEqual(Real)
 		assertResultTrue("3 <= 3.0");
 		assertResultTrue("-3 <= 3.0");
 		assertResultFalse("3 <= -3.0");
 		assertResultTrue("-3 <= -3.0");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberLessThanOrEqualInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i <= 0");
 		assertResultInvalid("let i : Integer = invalid in 0 <= i");
 		assertResultInvalid("let r : Real = invalid in r <= 0");
 		assertResultInvalid("let r : Real = invalid in 0 <= r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 <= i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 <= r2");
 	}
 
 	public void testNumberLessThanOrEqualNull() {
 		assertResultInvalid("let i : Integer = null in i <= 0");
 		assertResultInvalid("let i : Integer = null in 0 <= i");
 		assertResultInvalid("let r : Real = null in r <= 0");
 		assertResultInvalid("let r : Real = null in 0 <= r");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 <= i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 <= r2");
 	}
 
 	public void testNumberMax() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::max(Integer)
 		assertResult(Integer.valueOf(3), "3.max(2)");
 		assertResult(Integer.valueOf(2), "(-3).max(2)");
 		assertResult(Integer.valueOf(3), "3.max(-2)");
 		assertResult(Integer.valueOf(-2), "(-3).max(-2)");
 
 		assertResult(2147483648L, "2147483648.max(2147483647)");
 		assertResult(2147483648L, "2147483647.max(2147483648)");
 		assertResult(-2147483648, "(-2147483649).max(-2147483648)");
 		assertResult(-2147483648, "(-2147483648).max(-2147483649)");
 
 		// Integer::max(Real)
 		assertResult(Double.valueOf(3d), "3.max(2.0)");
 		assertResult(Double.valueOf(2d), "(-3).max(2.0)");
 		assertResult(Double.valueOf(3d), "3.max(-2.0)");
 		assertResult(Double.valueOf(-2d), "(-3).max(-2.0)");
 
 		// Real::max(Integer)
 		assertResult(Double.valueOf(3d), "(3.0).max(2)");
 		assertResult(Double.valueOf(2d), "(-3.0).max(2)");
 		assertResult(Double.valueOf(3d), "(3.0).max(-2)");
 		assertResult(Double.valueOf(-2d), "(-3.0).max(-2)");
 
 		// Real::max(Real)
 		assertResult(Double.valueOf(3d), "(3.0).max(2.0)");
 		assertResult(Double.valueOf(2d), "(-3.0).max(2.0)");
 		assertResult(Double.valueOf(3d), "(3.0).max(-2.0)");
 		assertResult(Double.valueOf(-2d), "(-3.0).max(-2.0)");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberMaxInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1.max(i)");
 		assertResultInvalid("let i : Integer = invalid in i.max(1)");
 		assertResultInvalid("let r : Real = invalid in 1.max(r)");
 		assertResultInvalid("let r : Real = invalid in r.max(1)");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1.max(i2)");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1.max(r2)");
 	}
 
 	public void testNumberMaxNull() {
 		assertResultInvalid("let i : Integer = null in 1.max(i)");
 		assertResultInvalid("let i : Integer = null in i.max(1)");
 		assertResultInvalid("let r : Real = null in 1.max(r)");
 		assertResultInvalid("let r : Real = null in r.max(1)");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1.max(i2)");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1.max(r2)");
 	}
 
 	public void testNumberMin() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::min(Integer)
 		assertResult(Integer.valueOf(2), "3.min(2)");
 		assertResult(Integer.valueOf(-3), "(-3).min(2)");
 		assertResult(Integer.valueOf(-2), "3.min(-2)");
 		assertResult(Integer.valueOf(-3), "(-3).min(-2)");
 
 		assertResult(2147483647, "2147483648.min(2147483647)");
 		assertResult(2147483647, "2147483647.min(2147483648)");
 		assertResult(-2147483649L, "(-2147483649).min(-2147483648)");
 		assertResult(-2147483649L, "(-2147483648).min(-2147483649)");
 
 		// Integer::min(Real)
 		assertResult(Double.valueOf(2d), "3.min(2.0)");
 		assertResult(Double.valueOf(-3d), "(-3).min(2.0)");
 		assertResult(Double.valueOf(-2d), "3.min(-2.0)");
 		assertResult(Double.valueOf(-3d), "(-3).min(-2.0)");
 
 		// Real::min(Integer)
 		assertResult(Double.valueOf(2d), "(3.0).min(2)");
 		assertResult(Double.valueOf(-3d), "(-3.0).min(2)");
 		assertResult(Double.valueOf(-2d), "(3.0).min(-2)");
 		assertResult(Double.valueOf(-3d), "(-3.0).min(-2)");
 
 		// Real::min(Real)
 		assertResult(Double.valueOf(2d), "(3.0).min(2.0)");
 		assertResult(Double.valueOf(-3d), "(-3.0).min(2.0)");
 		assertResult(Double.valueOf(-2d), "(3.0).min(-2.0)");
 		assertResult(Double.valueOf(-3d), "(-3.0).min(-2.0)");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberMinInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1.min(i)");
 		assertResultInvalid("let i : Integer = invalid in i.min(1)");
 		assertResultInvalid("let r : Real = invalid in 1.min(r)");
 		assertResultInvalid("let r : Real = invalid in r.min(1)");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1.min(i2)");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1.min(r2)");
 	}
 
 	public void testNumberMinNull() {
 		assertResultInvalid("let i : Integer = null in 1.min(i)");
 		assertResultInvalid("let i : Integer = null in i.min(1)");
 		assertResultInvalid("let r : Real = null in 1.min(r)");
 		assertResultInvalid("let r : Real = null in r.min(1)");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1.min(i2)");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1.min(r2)");
 	}
 
 	public void testNumberMinus() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::-(Integer)
 		assertResult(Integer.valueOf(0), "1 - 1");
 		assertResult(Integer.valueOf(5), "1 - -4");
 
 		assertResult(BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE).intValue(), "2147483646 - -1");
 		assertResult(BigInteger.ONE.shiftLeft(31).longValue(), "2147483647 - -1");
 		assertResult(BigInteger.ONE.shiftLeft(63).subtract(BigInteger.ONE).longValue(), "9223372036854775806 - -1");
 		assertResult(BigInteger.ONE.shiftLeft(63).longValue(), "9223372036854775807 - -1");
 
 		assertResult(BigInteger.ONE.negate().shiftLeft(31).intValue(), "-2147483647 - 1");
 		assertResult(BigInteger.ONE.negate().shiftLeft(31).subtract(BigInteger.ONE).longValue(), "-2147483648 - 1");
 		assertResult(BigInteger.ONE.shiftLeft(63).negate().longValue(), "-9223372036854775807 - 1");
 
 		// Integer::-(Real)
 		assertResult(Double.valueOf(0d), "1 - 1.0");
 		assertResult(Double.valueOf(5d), "1 - -4.0");
 
 		// Real::-(Integer)
 		assertResult(Double.valueOf(0d), "1.0 - 1");
 		assertResult(Double.valueOf(5d), "1.0 - -4");
 
 		// Real::-(Real)
 		assertResult(Double.valueOf(0d), "1.0 - 1.0");
 		assertResult(Double.valueOf(-0.01d), "1.11 - 1.12");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberMinusInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1 - i");
 		assertResultInvalid("let i : Integer = invalid in i - 1");
 		assertResultInvalid("let r : Real = invalid in 1 - r");
 		assertResultInvalid("let r : Real = invalid in r - 1");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 - i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 - r2");
 	}
 
 	public void testNumberMinusNull() {
 		assertResultInvalid("let i : Integer = null in 1 - i");
 		assertResultInvalid("let i : Integer = null in i - 1");
 		assertResultInvalid("let r : Real = null in 1 - r");
 		assertResultInvalid("let r : Real = null in r - 1");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 - i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 - r2");
 	}
 
 	public void testNumberMod() {
 		assertResult(Integer.valueOf(1), "3.mod(2)");
 		assertResult(Integer.valueOf(-1), "(-3).mod(2)");
 		assertResult(Integer.valueOf(1), "3.mod(-2)");
 		assertResult(Integer.valueOf(-1), "(-3).mod(-2)");
 	}
 
 	public void testNumberModInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1.mod(i)");
 		assertResultInvalid("let i : Integer = invalid in i.mod(1)");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1.mod(i2)");
 	}
 
 	public void testNumberModNull() {
 		assertResultInvalid("let i : Integer = null in 1.mod(i)");
 		assertResultInvalid("let i : Integer = null in i.mod(1)");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1.mod(i2)");
 	}
 
 	public void testNumberNegate() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		assertResult(-1, "-1");
//		assertResult(-1.0, "-1.0", 0.0);
 
 		assertResult(-2147483647, "-2147483647");
 		assertResult(-2147483648, "-2147483648");
 		assertResult(2147483648L, "-(-2147483648)");
 		assertResult(-2147483649L, "-2147483649");
 		// invalid
 		assertResultInvalid("let i : Integer = invalid in -i");
 		assertResultInvalid("let r : Real = invalid in -r");
 		// null
 		assertResultInvalid("let i : Integer = null in -i");
 		assertResultInvalid("let r : Real = null in -r");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberNotEqual() {
 		assertResultTrue("4 <> 5");
 		assertResultTrue("1 <> 4.0");
 		assertResultTrue("1.0 <> 4");
 		assertResultTrue("1.0 <> 4.0");
 
 		assertResultFalse("4 <> 4");
 		assertResultFalse("1 <> 1.0");
 		assertResultFalse("1.0 <> 1");
 		assertResultFalse("1.0 <> 1.0");
 	}
 
 	public void testNumberNotEqualInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i <> 0");
 		assertResultInvalid("let i : Integer = invalid in -1 <> i");
 		assertResultInvalid("let r : Real = invalid in r <> 0.0");
 		assertResultInvalid("let r : Real = invalid in -1.0 <> r");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 <> i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 <> r2");
 	}
 
 	public void testNumberNotEqualNull() {
 		assertResultTrue("let i : Integer = null in i <> 0");
 		assertResultTrue("let i : Integer = null in -1 <> i");
 		assertResultTrue("let r : Real = null in r <> 0.0");
 		assertResultTrue("let r : Real = null in -1.0 <> r");
 
 		assertResultFalse("let i1 : Integer = null, i2 : Integer = null in i1 <> i2");
 		assertResultFalse("let r1 : Real = null, r2 : Real = null in r1 <> r2");
 	}
 
 	public void testNumberOclIsInvalid() {
 		assertResultFalse("3.oclIsInvalid()");
 		assertResultFalse("(3.0).oclIsInvalid()");
 	}
 
 	public void testNumberOclIsUndefined() {
 		assertResultFalse("3.oclIsUndefined()");
 		assertResultFalse("(3.0).oclIsUndefined()");
 	}
 
 	public void testNumberPlus() {
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, true);
 		// Integer::+(Integer)
 		assertResult(Integer.valueOf(2), "1 + 1");
 		assertResult(Integer.valueOf(-3), "1 + -4");
 
 		assertResult(BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE).intValue(), "2147483646 + 1");
 		assertResult(BigInteger.ONE.shiftLeft(31).longValue(), "2147483647 + 1");
 		assertResult(BigInteger.ONE.shiftLeft(63).subtract(BigInteger.ONE).longValue(), "9223372036854775806 + 1");
 		assertResult(BigInteger.ONE.shiftLeft(63).longValue(), "9223372036854775807 + 1");
 
 		assertResult(BigInteger.ONE.negate().shiftLeft(31).intValue(), "-2147483647 + -1");
 		assertResult(BigInteger.ONE.negate().shiftLeft(31).subtract(BigInteger.ONE).longValue(), "-2147483648 + -1");
 		assertResult(BigInteger.ONE.negate().shiftLeft(63).longValue(), "-9223372036854775807 + -1");
 
 		// Integer::+(Real)
 		assertResult(Double.valueOf(2d), "1 + 1.0");
 		assertResult(Double.valueOf(-3d), "1 + -4.0");
 
 		// Real::+(Integer)
 		assertResult(Double.valueOf(2d), "1.0 + 1");
 		assertResult(Double.valueOf(-3d), "1.0 + -4");
 
 		// Real::+(Real)
 		assertResult(Double.valueOf(2d), "1.0 + 1.0");
 		assertResult(Double.valueOf(2.23d), "1.11 + 1.12");
         ParsingOptions.setOption(helper.getOCL().getEnvironment(), ParsingOptions.USE_LONG_INTEGERS, false);
 	}
 
 	public void testNumberPlusInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1 + i");
 		assertResultInvalid("let i : Integer = invalid in i + 1");
 		assertResultInvalid("let r : Real = invalid in 1 + r");
 		assertResultInvalid("let r : Real = invalid in r + 1");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 + i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 + r2");
 	}
 
 	public void testNumberPlusNull() {
 		assertResultInvalid("let i : Integer = null in 1 + i");
 		assertResultInvalid("let i : Integer = null in i + 1");
 		assertResultInvalid("let r : Real = null in 1 + r");
 		assertResultInvalid("let r : Real = null in r + 1");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 + i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 + r2");
 	}
 
 	public void testNumberRound() {
 		// Integer::round()
 		assertResult(Integer.valueOf(3), "3.round()");
 		assertResult(Integer.valueOf(-3), "(-3).round()");
 
 		// Real::round()
 		assertResult(Integer.valueOf(-1), "(-1.5).round()");
 		assertResult(Integer.valueOf(1), "(1.01).round()");
 		assertResult(Integer.valueOf(4), "(3.999).round()");
 	}
 
 	public void testNumberRoundInvalid() {
 		assertResultInvalid("let i : Integer = invalid in i.round()");
 		assertResultInvalid("let r : Real = invalid in r.round()");
 	}
 
 	public void testNumberRoundNull() {
 		assertResultInvalid("let i : Integer = null in i.round()");
 		assertResultInvalid("let r : Real = null in r.round()");
 	}
 
 	public void testNumberTimes() {
 		// Integer::*(Integer)
 		assertResult(Integer.valueOf(1), "1 * 1");
 		assertResult(Integer.valueOf(-4), "1 * -4");
 
 		// Integer::*(Real)
 		assertResult(Double.valueOf(1d), "1 * 1.0");
 		assertResult(Double.valueOf(-4d), "1 * -4.0");
 
 		// Real::*(Integer)
 		assertResult(Double.valueOf(1d), "1.0 * 1");
 		assertResult(Double.valueOf(-4d), "1.0 * -4");
 
 		// Real::*(Real)
 		assertResult(Double.valueOf(1d), "1.0 * 1.0");
 		assertResult(Double.valueOf(1.11d * 1.12d), "1.11 * 1.12");
 	}
 
 	public void testNumberTimesInvalid() {
 		assertResultInvalid("let i : Integer = invalid in 1 * i");
 		assertResultInvalid("let i : Integer = invalid in i * 1");
 		assertResultInvalid("let r : Real = invalid in 1 * r");
 		assertResultInvalid("let r : Real = invalid in r * 1");
 
 		assertResultInvalid("let i1 : Integer = invalid, i2 : Integer = invalid in i1 * i2");
 		assertResultInvalid("let r1 : Real = invalid, r2 : Real = invalid in r1 * r2");
 	}
 
 	public void testNumberTimesNull() {
 		assertResultInvalid("let i : Integer = null in 1 * i");
 		assertResultInvalid("let i : Integer = null in i * 1");
 		assertResultInvalid("let r : Real = null in 1 * r");
 		assertResultInvalid("let r : Real = null in r * 1");
 
 		assertResultInvalid("let i1 : Integer = null, i2 : Integer = null in i1 * i2");
 		assertResultInvalid("let r1 : Real = null, r2 : Real = null in r1 * r2");
 	}
 
 	public void testNumberToString() {
 		assertResult("1", "1.toString()");
 		assertResult("3.0", "3.0.toString()");
 		assertResult("4.0", "(1.0+3.0).toString()");
 		assertResult("null", "null.toString()");
 		assertResult("invalid", "invalid.toString()");
 		assertResult("*", "*.toString()");
 	}
 
 	public void testNumberUnaryMinus() {
 		assertResult(Integer.valueOf(-1), "-1");
 		assertResult(Double.valueOf(-1d), "-1.0");
 	}
 
 	public void testNumberUnaryMinusInvalid() {
 		assertResultInvalid("let i : Integer = invalid in -i");
 		assertResultInvalid("let r : Real = invalid in -r");
 	}
 
 	public void testNumberUnaryMinusNull() {
 		assertResultInvalid("let i : Integer = null in -i");
 		assertResultInvalid("let r : Real = null in -r");
 	}
 
 	public void testUnlimitedAbs() {
 		assertResultInvalid("*.abs()");
 	}
 
 	public void testUnlimitedAbsInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.abs()");
 	}
 
 	public void testUnlimitedAbsNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u.abs()");
 	}
 
 	public void testUnlimitedDiv() {
 		/*
 		 * FIXME I'm expecting the UnlimitedNatural to conform to Integer, div
 		 * and mod calls should then at least parse for them even though they
 		 * return an invalid value.
 		 */
 		assertResultInvalid("1.div(*)");
 		// FIXME UnlimitedNatural.div(Integer) currently not found; problem in analyzer?
 		assertResultInvalid("*.div(1)");
 
 		assertResultInvalid("*.div(*)");
 	}
 
 	public void testUnlimitedDivide() {
 		assertResultInvalid("1 / *");
 		assertResultInvalid("* / 1");
 
 		assertResultInvalid("1.0 / *");
 		assertResultInvalid("* / 1.0");
 
 		assertResultInvalid("* / *");
 	}
 
 	public void testUnlimitedDivideByZero() {
 		assertResultInvalid("* / 0");
 		assertResultInvalid("* / 0.0");
 	}
 
 	public void testUnlimitedDivideInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in * / u");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u / *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 / u2");
 	}
 
 	public void testUnlimitedDivideNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in * / u");
 		assertResultInvalid("let u : UnlimitedNatural = null in u / *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 / u2");
 	}
 
 	public void testUnlimitedDivInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 1.div(u)");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.div(1)");
 	}
 
 	public void testUnlimitedDivNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in 1.div(u)");
 		assertResultInvalid("let u : UnlimitedNatural = null in u.div(1)");
 	}
 
 	public void testUnlimitedEqual() {
 		assertResultFalse("* = 1");
 		assertResultFalse("1 = *");
 		assertResultFalse("* = 1.0");
 		assertResultFalse("1.0 = *");
 		// FIXME UNLIMIED is currently represented as -1, so no difference can be observed
 		// assertResultFalse("* = -1");
 		// assertResultFalse("-1 = *");
 		// assertResultFalse("* = -1.0");
 		// assertResultFalse("-1.0 = *");
 
 		assertResultTrue("* = *");
 	}
 
 	public void testUnlimitedEqualInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u = *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 = u2");
 	}
 
 	public void testUnlimitedEqualNull() {
 		assertResultFalse("let u : UnlimitedNatural = null in u = *");
 
 		assertResultTrue("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 = u2");
 	}
 
 	public void testUnlimitedFloor() {
 		assertResultInvalid("*.floor()");
 	}
 
 	public void testUnlimitedFloorInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.floor()");
 	}
 
 	public void testUnlimitedFloorNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u.floor()");
 	}
 
 	public void testUnlimitedGreaterThan() {
 		assertResultFalse("1 > *");
 		assertResultTrue("* > 1");
 		assertResultFalse("* > *");
 	}
 
 	public void testUnlimitedGreaterThanInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u > 0");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 0 > u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 > u2");
 	}
 
 	public void testUnlimitedGreaterThanNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u > 0");
 		assertResultInvalid("let u : UnlimitedNatural = null in 0 > u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 > u2");
 	}
 
 	public void testUnlimitedGreaterThanOrEqual() {
 		/*
 		 * FIXME "(* = *) == true" but "(* >= *) == false" ? something's amiss
 		 * and since this behavior isn't defined in the specification, we'll
 		 * have to make an arbitrary choice. The "expected" here is Java's
 		 * behavior with Double.POSITIVE_INFINITY.
 		 */
 		assertResultFalse("1 >= *");
 		assertResultTrue("* >= 1");
 		assertResultTrue("* >= *");
 	}
 
 	public void testUnlimitedGreaterThanOrEqualInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u >= 0");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 0 >= u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 >= u2");
 	}
 
 	public void testUnlimitedGreaterThanOrEqualNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u >= 0");
 		assertResultInvalid("let u : UnlimitedNatural = null in 0 >= u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 >= u2");
 	}
 
 	public void testUnlimitedLessThan() {
 		assertResultTrue("1 < *");
 		assertResultFalse("* < 1");
 		assertResultFalse("* < *");
 	}
 
 	public void testUnlimitedLessThanInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u < 0");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 0 < u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 < u2");
 	}
 
 	public void testUnlimitedLessThanNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u < 0");
 		assertResultInvalid("let u : UnlimitedNatural = null in 0 < u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 < u2");
 	}
 
 	public void testUnlimitedLessThanOrEqual() {
 		/*
 		 * FIXME "(* = *) == true" but "(* <= *) == false" ? something's amiss
 		 * and since this behavior isn't defined in the specification, we'll
 		 * have to make an arbitrary choice. The "expected" here is Java's
 		 * behavior with Double.POSITIVE_INFINITY.
 		 */
 		assertResultTrue("1 <= *");
 		assertResultFalse("* <= 1");
 		assertResultTrue("* <= *");
 	}
 
 	public void testUnlimitedLessThanOrEqualInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u <= 0");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 0 <= u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 <= u2");
 	}
 
 	public void testUnlimitedLessThanOrEqualNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u <= 0");
 		assertResultInvalid("let u : UnlimitedNatural = null in 0 <= u");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 <= u2");
 	}
 
 	public void testUnlimitedMax() {
 		assertResultInvalid("1.max(*)");
 		assertResultInvalid("*.max(1)");
 
 		assertResultInvalid("(1.0).max(*)");
 		assertResultInvalid("*.max(1.0)");
 
 		assertResultInvalid("*.max(*)");
 	}
 
 	public void testUnlimitedMaxInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in *.max(u)");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.max(*)");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1.max(u2)");
 	}
 
 	public void testUnlimitedMaxNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in *.max(u)");
 		assertResultInvalid("let u : UnlimitedNatural = null in u.max(*)");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1.max(u2)");
 	}
 
 	public void testUnlimitedMin() {
 		assertResultInvalid("1.min(*)");
 		assertResultInvalid("*.min(1)");
 
 		assertResultInvalid("(1.0).min(*)");
 		assertResultInvalid("*.min(1.0)");
 
 		assertResultInvalid("*.min(*)");
 	}
 
 	public void testUnlimitedMinInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in *.min(u)");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.min(*)");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1.min(u2)");
 	}
 
 	public void testUnlimitedMinNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in *.min(u)");
 		assertResultInvalid("let u : UnlimitedNatural = null in u.min(*)");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1.min(u2)");
 	}
 
 	public void testUnlimitedMinus() {
 		assertResultInvalid("1 - *");
 		assertResultInvalid("* - 1");
 
 		assertResultInvalid("1.0 - *");
 		assertResultInvalid("* - 1.0");
 
 		assertResultInvalid("* - *");
 	}
 
 	public void testUnlimitedMinusInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in * - u");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u - *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 - u2");
 	}
 
 	public void testUnlimitedMinusNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in * - u");
 		assertResultInvalid("let u : UnlimitedNatural = null in u - *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 - u2");
 	}
 
 	public void testUnlimitedMod() {
 		/*
 		 * FIXME I'm expecting the UnlimitedNatural to conform to Integer, div
 		 * and mod calls should then at least parse for them even though they
 		 * return an invalid value.
 		 */
 		assertResultInvalid("1.mod(*)");
 		assertResultInvalid("*.mod(1)");
 
 		assertResultInvalid("*.mod(*)");
 	}
 
 	public void testUnlimitedModInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in 1.mod(u)");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.mod(1)");
 	}
 
 	public void testUnlimitedModNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in 1.mod(u)");
 		assertResultInvalid("let u : UnlimitedNatural = null in u.mod(1)");
 	}
 
 	public void testUnlimitedNotEqual() {
 		assertResultTrue("* <> 1");
 		assertResultTrue("1 <> *");
 		assertResultTrue("* <> 1.0");
 		assertResultTrue("1.0 <> *");
 		// FIXME currently, * is encoded as -1 and undistinguishable from it
 		// assertResultTrue("* <> -1");
 		// assertResultTrue("-1 <> *");
 		// assertResultTrue("* <> -1.0");
 		// assertResultTrue("-1.0 <> *");
 
 		assertResultFalse("* <> *");
 	}
 
 	public void testUnlimitedNotEqualInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u <> *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 <> u2");
 	}
 
 	public void testUnlimitedNotEqualNull() {
 		assertResultTrue("let u : UnlimitedNatural = null in u <> *");
 
 		assertResultFalse("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 <> u2");
 	}
 
 	public void testUnlimitedOclAsType() {
 		// As demanded by the specification, OCL 2.3 (OMG 10-11-42), Section 8.2.1,
 		// * when converted to Integer evaluates to invalid. As Integer conforms to
 		// Real, * also has to evaluate to invalid when converted to Real.
 		assertResultInvalid("*.oclAsType(Integer)");
 		assertResultInvalid("*.oclAsType(Real)");
 		assertResult(UnlimitedNaturalLiteralExp.UNLIMITED,
 			"*.oclAsType(UnlimitedNatural)");
 		assertResultInvalid("*.oclAsType(String)");
 		assertResult(UnlimitedNaturalLiteralExp.UNLIMITED,
 			"*.oclAsType(OclAny)");
 		assertResultInvalid("*.oclAsType(OclVoid)");
 		assertResultInvalid("*.oclAsType(OclInvalid)");
 	}
 
 	public void testUnlimitedOclIsInvalid() {
 		assertResultFalse("*.oclIsInvalid()");
 	}
 
 	public void testUnlimitedOclIsTypeOf() {
 		// From OCL 2.3 (11.5.1): "Note that UnlimitedNatural is a subclass
 		// of Integer and that Integer is a subclass of Real"
 		assertResultFalse("*.oclIsTypeOf(Integer)");
 		assertResultFalse("*.oclIsTypeOf(Real)");
 		assertResultTrue("*.oclIsTypeOf(UnlimitedNatural)");
 		assertResultFalse("*.oclIsTypeOf(String)");
 		assertResultFalse("*.oclIsTypeOf(OclAny)");
 		assertResultFalse("*.oclIsTypeOf(OclVoid)");
 		assertResultFalse("*.oclIsTypeOf(OclInvalid)");
 	}
 
 	public void testUnlimitedOclIsUndefined() {
 		assertResultFalse("*.oclIsUndefined()");
 	}
 
 	public void testUnlimitedPlus() {
 		assertResultInvalid("1 + *");
 		assertResultInvalid("* + 1");
 
 		assertResultInvalid("1.0 + *");
 		assertResultInvalid("* + 1.0");
 
 		assertResultInvalid("* + *");
 	}
 
 	public void testUnlimitedPlusInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in * + u");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u + *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 + u2");
 	}
 
 	public void testUnlimitedPlusNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in * + u");
 		assertResultInvalid("let u : UnlimitedNatural = null in u + *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 + u2");
 	}
 
 	public void testUnlimitedRound() {
 		assertResultInvalid("*.round()");
 	}
 
 	public void testUnlimitedRoundInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u.round()");
 	}
 
 	public void testUnlimitedRoundNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in u.round()");
 	}
 
 	public void testUnlimitedTimes() {
 		assertResultInvalid("1 * *");
 		assertResultInvalid("* * 1");
 
 		assertResultInvalid("1.0 * *");
 		assertResultInvalid("* * 1.0");
 
 		assertResultInvalid("* * *");
 	}
 
 	public void testUnlimitedTimesInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in * * u");
 		assertResultInvalid("let u : UnlimitedNatural = invalid in u * *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = invalid, u2 : UnlimitedNatural = invalid in u1 * u2");
 	}
 
 	public void testUnlimitedTimesNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in * * u");
 		assertResultInvalid("let u : UnlimitedNatural = null in u * *");
 
 		assertResultInvalid("let u1 : UnlimitedNatural = null, u2 : UnlimitedNatural = null in u1 * u2");
 	}
 
 	public void testUnlimitedUnaryMinus() {
 		/*
 		 * FIXME OCL doesn't have a negative infinity, this should then be
 		 * invalid
 		 */
 		assertResultInvalid("-*");
 	}
 
 	public void testUnlimitedUnaryMinusInvalid() {
 		assertResultInvalid("let u : UnlimitedNatural = invalid in -u");
 	}
 
 	public void testUnlimitedUnaryMinusNull() {
 		assertResultInvalid("let u : UnlimitedNatural = null in -u");
 	}
 }
