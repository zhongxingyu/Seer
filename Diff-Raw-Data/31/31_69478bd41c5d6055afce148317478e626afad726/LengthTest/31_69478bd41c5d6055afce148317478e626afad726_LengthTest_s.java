 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.ui.layout;
 
 import org.hobsoft.symmetry.ui.layout.Length.Unit;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests {@code Length}.
  * 
  * @author Mark Hobson
  * @see Length
  */
 public class LengthTest
 {
 	// tests ------------------------------------------------------------------
 	
 	@Test
 	public void pixelsWithPositive()
 	{
 		Length length = Length.pixels(1);
 		
 		assertLength(1, Unit.PIXELS, length);
 	}
 	
 	@Test
 	public void pixelsWithZero()
 	{
 		Length length = Length.pixels(0);
 		
 		assertLength(0, Unit.PIXELS, length);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void pixelsWithNegative()
 	{
 		Length.pixels(-1);
 	}
 	
 	@Test
 	public void percentageWithPositive()
 	{
 		Length length = Length.percentage(1);
 		
 		assertLength(1, Unit.PERCENTAGE, length);
 	}
 	
 	@Test
 	public void percentageWithZero()
 	{
 		Length length = Length.percentage(0);
 		
 		assertLength(0, Unit.PERCENTAGE, length);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void percentageWithNegative()
 	{
 		Length.percentage(-1);
 	}
 	
 	@Test
 	public void flexWithPositive()
 	{
 		Length length = Length.flex(1);
 		
 		assertLength(1, Unit.FLEX, length);
 	}
 	
 	@Test
 	public void flexWithZero()
 	{
 		Length length = Length.flex(0);
 		
 		assertLength(0, Unit.FLEX, length);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void flexWithNegative()
 	{
 		Length.flex(-1);
 	}
 	
 	@Test
 	public void hashCodeWhenEqual()
 	{
 		Length length1 = Length.pixels(1);
 		Length length2 = Length.pixels(1);
 		
 		assertEquals(length1.hashCode(), length2.hashCode());
 	}
 	
 	@Test
 	public void equalsWithEqual()
 	{
 		Length length1 = Length.pixels(1);
 		Length length2 = Length.pixels(1);
 		
 		assertTrue(length1.equals(length2));
 	}
 	
 	@Test
 	public void equalsWithDifferentValue()
 	{
 		Length length1 = Length.pixels(1);
 		Length length2 = Length.pixels(2);
 		
 		assertFalse(length1.equals(length2));
 	}
 	
 	@Test
 	public void equalsWithDifferentUnit()
 	{
 		Length length1 = Length.pixels(1);
 		Length length2 = Length.flex(1);
 		
 		assertFalse(length1.equals(length2));
 	}
 	
 	@Test
 	public void equalsWithDifferentClass()
 	{
 		Length length = Length.pixels(1);
 		
 		assertFalse(length.equals(new Object()));
 	}
 	
 	@Test
 	public void equalsWithNull()
 	{
 		Length length = Length.pixels(1);
 		
 		assertFalse(length.equals(null));
 	}
 	
 	@Test
 	public void toStringWhenPixels()
 	{
 		assertEquals("1px", Length.pixels(1).toString());
 	}
 	
 	@Test
 	public void toStringWhenPercentage()
 	{
 		assertEquals("1%", Length.percentage(1).toString());
 	}
 	
 	@Test
 	public void toStringWhenFlex()
 	{
 		assertEquals("1*", Length.flex(1).toString());
 	}
 	
 	// private methods --------------------------------------------------------
 	
 	private static void assertLength(int expectedValue, Unit expectedUnit, Length actual)
 	{
 		assertEquals("value", expectedValue, actual.getValue());
 		assertEquals("unit", expectedUnit, actual.getUnit());
 	}
 }
