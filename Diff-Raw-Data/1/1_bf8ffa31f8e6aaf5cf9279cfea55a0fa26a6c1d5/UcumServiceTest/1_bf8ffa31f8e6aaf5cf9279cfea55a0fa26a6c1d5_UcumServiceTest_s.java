 /*******************************************************************************
  * Copyright (c) 2010, 2013 Werner Keil.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Werner Keil - initial API and implementation
  *******************************************************************************/
 package org.eclipse.uomo.ucum.tests;
 
 import static org.junit.Assert.*;
 import static org.eclipse.uomo.core.impl.OutputHelper.*;
 
 import java.math.BigDecimal;
 import java.util.Set;
 
 import org.eclipse.uomo.core.UOMoException;
 import org.eclipse.uomo.ucum.UcumService;
 import org.eclipse.uomo.ucum.expression.Symbol;
 import org.eclipse.uomo.ucum.expression.Term;
 import org.eclipse.uomo.ucum.impl.UcumEssenceService;
 import org.eclipse.uomo.ucum.parsers.ExpressionParser;
 import org.eclipse.uomo.util.Parser;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.unitsofmeasurement.unit.Unit;
 
 import com.ibm.icu.text.DecimalFormat;
 import com.ibm.icu.text.NumberFormat;
 
 
 /**
  * @author Werner Keil
  * @version 1.2, 2013-04-28
  */
@Ignore
 public class UcumServiceTest {
 	private UcumService ucumService;
 	
 	@Before
 	public void init() {
 		if (ucumService == null) {
 			ucumService = new UcumEssenceService(getClass().getClassLoader().getResourceAsStream("ucum-essence.xml"));
 		}
 	}
 	
 	@Test
 	public void testConversion() {
 		Number mult = ucumService.convert(new BigDecimal(1000d), "l", "m3");
 		assertNotNull(mult);		
 		NumberFormat fmt = new DecimalFormat("#,##0.000");
 		assertEquals(fmt.format(BigDecimal.ONE), fmt.format(mult));
 	}
 
 	@Test
 	public void testProperties() {
 		Set<String> props = ucumService.getProperties();
 		if (isConsoleOutput()) {
 			for (String prop : props) {
 				println(prop);
 			}
 		}
 		assertTrue(props.size() == 92);
 	}
 	
 	
 	@Test
 	public void testParse() {
 		Parser<String, Term> p = new ExpressionParser(ucumService.getModel());
 		try {
 			Term t =  p.parse("m/s2");
 			assertNotNull(t);
 			assertEquals("DIVISION", t.getOp().toString());
 			Symbol s = (Symbol)t.getComp();
 			Unit<?> u = s.getUnit();
 			assertEquals("m", u.getSymbol());
 		} catch (UOMoException e) {
 			println(e.getLocalizedMessage());
 			fail(e.getLocalizedMessage());
 		}
 	}
 	
 	@Test
 	public void testParseParseMult1() {
 		Parser<String, Term> p = new ExpressionParser(ucumService.getModel());
 		try {
 			Term t =  p.parse("m.s2");
 			assertNotNull(t);
 			assertEquals("MULTIPLICATION", t.getOp().toString());
 			Symbol s = (Symbol)t.getComp();
 			Unit<?> u = s.getUnit();
 			assertEquals("m", u.getSymbol());
 		} catch (UOMoException e) {
 			println(e.getLocalizedMessage());
 			fail(e.getLocalizedMessage());
 		}
 	}
 	
 	/**
 	 * @see <a
      *  href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=371433">Bugzilla: 371433</a>
 	 */
 	@Test
 	public void testParseParseParenthes1() {
 		Parser<String, Term> p = new ExpressionParser(ucumService.getModel());
 		try {
 			Term t =  p.parse("kg/m/s");
 			assertNotNull(t);
 			assertEquals("DIVISION", t.getOp().toString());
 			Symbol s = (Symbol)t.getComp();
 			Unit<?> u = s.getUnit();
 			assertEquals("g", u.getSymbol());
 			assertEquals("k", s.getPrefix().getSymbol());
 		} catch (UOMoException e) {
 			println(e.getLocalizedMessage());
 			fail(e.getLocalizedMessage());
 		}
 	}
 }
