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
 
 import net.sf.staccato.commons.defs.Executable3;
 import net.sf.staccato.commons.lang.SoftException;
 import net.sf.staccato.commons.testing.junit.jmock.JUnit4MockObjectTestCase;
 
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author flbulgarelli
  * 
  */
 public class Block3UnitTest extends JUnit4MockObjectTestCase {
 
 	Block3<MutableInt, String, Character> block;
 	Executable3<MutableInt, String, Character> executable, otherExecutable;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		executable = mock(Executable3.class);
 		otherExecutable = mock(Executable3.class, "other");
 		block = new Block3<MutableInt, String, Character>() {
 			public void exec(MutableInt arg1, String arg2, Character arg3) {
 				executable.exec(arg1, arg2, arg3);
 			}
 		};
 	}
 
 	/**
 	 * Test method for {@link Block3#exec(Object, Object, Object)} when softExec
 	 * is overriden
 	 */
 	@Test(expected = SoftException.class)
 	public void testSoftExec() throws Exception {
 		block = new Block3<MutableInt, String, Character>() {
 			protected void softExec(MutableInt arg1, String arg2, Character arg3) throws Exception {
 				throw new IOException();
 			}
 		};
 		block.exec(new MutableInt(5), "", 'a');
 	}
 
 	/**
 	 * Test for {@link Block3#apply(Object)} and
 	 * {@link Block3#apply(Object, Object)}
 	 */
 	@Test
 	public void testApply() throws Exception {
 		checking(new Expectations() {
 			{
 				exactly(3).of(executable).exec(new MutableInt(5), "", 'a');
 			}
 		});
 		block.exec(new MutableInt(5), "", 'a');
 		block.apply(new MutableInt(5)).exec("", 'a');
 		block.apply(new MutableInt(5), "").exec('a');
 	}
 
 	/**
	 * Test method for {@link Block3#then(Block3)} .
 	 */
 	@Test
 	public void testThen() {
 		final MutableInt mi = new MutableInt(5);
 		checking(new Expectations() {
 			{
 				one(executable).exec(new MutableInt(5), "", 'a');
 				will(new IncrementAction(mi));
 				one(otherExecutable).exec(new MutableInt(6), "", 'a');
 			}
 		});
 		block.then(otherExecutable).exec(mi, "", 'a');
 		assertEquals(6, mi.getValue());
 	}
 
 }
