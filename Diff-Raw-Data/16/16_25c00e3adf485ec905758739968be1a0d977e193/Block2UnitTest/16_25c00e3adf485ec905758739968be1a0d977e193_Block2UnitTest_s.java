 /*
  Copyright (c) 2010, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccato.commons.lang.block;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.IOException;
 
 import net.sf.staccato.commons.defs.Executable2;
 import net.sf.staccato.commons.lang.SoftException;
 import net.sf.staccato.commons.testing.junit.jmock.JUnit4MockObjectTestCase;
 
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test for {@link Block2}
  * 
  * @author flbulgarelli
  */
 public class Block2UnitTest extends JUnit4MockObjectTestCase {
 
 	Block2<MutableInt, String> block;
 	Executable2<MutableInt, String> executable, otherExecutable;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		executable = mock(Executable2.class);
 		otherExecutable = mock(Executable2.class, "other");
 		block = new Block2<MutableInt, String>() {
 			public void exec(MutableInt arg1, String arg2) {
 				executable.exec(arg1, arg2);
 			}
 		};
 	}
 
 	/**
 	 * Test method for {@link Block2#exec(Object, Object)} when softExec is
 	 * overriden
 	 */
 	@Test(expected = SoftException.class)
 	public void testSoftExec() throws Exception {
 		block = new Block2<MutableInt, String>() {
 			protected void softExec(MutableInt arg1, String arg2) throws Exception {
 				throw new IOException();
 			}
 		};
 		block.exec(new MutableInt(5), "");
 	}
 
 	/**
 	 * Test for {@link Block2#apply(Object)}
 	 */
 	@Test
 	public void testApply() throws Exception {
 		checking(new Expectations() {
 			{
 				exactly(2).of(executable).exec(new MutableInt(5), "");
 			}
 		});
 		block.exec(new MutableInt(5), "");
 		block.apply(new MutableInt(5)).exec("");
 	}
 
 	/**
	 * Test method for {@link Block2#then(Block2)} .
 	 */
 	@Test
 	public void testThen() {
 		final MutableInt mi = new MutableInt(5);
 		checking(new Expectations() {
 			{
 				one(executable).exec(new MutableInt(5), "");
 				will(new IncrementAction(mi));
 				one(otherExecutable).exec(new MutableInt(6), "");
 			}
 		});
 		block.then(otherExecutable).exec(mi, "");
 		assertEquals(6, mi.getValue());
 	}
 
 }
