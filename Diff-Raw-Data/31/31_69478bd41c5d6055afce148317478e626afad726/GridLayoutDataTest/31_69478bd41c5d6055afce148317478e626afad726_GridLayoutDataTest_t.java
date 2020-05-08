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
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests {@code GridLayoutData}.
  * 
  * @author Mark Hobson
  * @see GridLayoutData
  */
 public class GridLayoutDataTest
 {
 	// fields -----------------------------------------------------------------
 	
 	private GridLayoutData data;
 	
 	// public methods ---------------------------------------------------------
 	
 	@Before
 	public void setUp()
 	{
 		data = new GridLayoutData();
 	}
 
 	// tests ------------------------------------------------------------------
 	
 	@Test
 	public void newGridLayoutData()
 	{
 		assertEquals(HorizontalAlignment.LEFT, data.getHorizontalAlignment());
 		assertEquals(VerticalAlignment.MIDDLE, data.getVerticalAlignment());
 		assertEquals(1, data.getColumnSpan());
 	}
 	
 	@Test
 	public void setHorizontalAlignment()
 	{
 		data.setHorizontalAlignment(HorizontalAlignment.MIDDLE);
 		
 		assertEquals(HorizontalAlignment.MIDDLE, data.getHorizontalAlignment());
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void setHorizontalAlignmentWithNull()
 	{
 		data.setHorizontalAlignment(null);
 	}
 	
 	@Test
 	public void setVerticalAlignment()
 	{
 		data.setVerticalAlignment(VerticalAlignment.TOP);
 		
 		assertEquals(VerticalAlignment.TOP, data.getVerticalAlignment());
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void setVerticalAlignmentWithNull()
 	{
 		data.setVerticalAlignment(null);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void setColumnSpanWithNegative()
 	{
 		data.setColumnSpan(-1);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void setColumnSpanWithZero()
 	{
 		data.setColumnSpan(0);
 	}
 	
 	@Test
 	public void setColumnSpanWithPositive()
 	{
 		data.setColumnSpan(1);
 		
 		assertEquals(1, data.getColumnSpan());
 	}
 	
 	@Test
 	public void hashCodeWhenEqual()
 	{
 		GridLayoutData data1 = new GridLayoutData();
 		GridLayoutData data2 = new GridLayoutData();
 		
 		assertEquals(data1.hashCode(), data2.hashCode());
 	}
 	
 	@Test
 	public void equalsWithEqual()
 	{
 		GridLayoutData data1 = new GridLayoutData();
 		GridLayoutData data2 = new GridLayoutData();
 		
 		assertTrue(data1.equals(data2));
 	}
 	
 	@Test
 	public void equalsWithDifferentHorizontalAlignment()
 	{
 		GridLayoutData data1 = new GridLayoutData();
 		GridLayoutData data2 = new GridLayoutData();
 		data2.setHorizontalAlignment(HorizontalAlignment.MIDDLE);
 		
 		assertFalse(data1.equals(data2));
 	}
 	
 	@Test
 	public void equalsWithDifferentVerticalAlignment()
 	{
 		GridLayoutData data1 = new GridLayoutData();
 		GridLayoutData data2 = new GridLayoutData();
 		data2.setVerticalAlignment(VerticalAlignment.TOP);
 		
 		assertFalse(data1.equals(data2));
 	}
 	
 	@Test
 	public void equalsWithDifferentColumnSpan()
 	{
 		GridLayoutData data1 = new GridLayoutData();
 		GridLayoutData data2 = new GridLayoutData();
 		data2.setColumnSpan(2);
 		
 		assertFalse(data1.equals(data2));
 	}
 	
 	@Test
 	public void equalsWithDifferentClass()
 	{
 		assertFalse(data.equals(new Object()));
 	}
 	
 	@Test
 	public void equalsWithNull()
 	{
		// workaround Checkstyle bug 2809655
		// CHECKSTYLE:OFF
 		assertFalse(data.equals(null));
		// CHECKSTYLE:ON
 	}
 	
 	@Test
 	public void toStringTest()
 	{
 		String expected = "org.hobsoft.symmetry.ui.layout.GridLayoutData[horizontalAlignment=LEFT, "
 			+ "verticalAlignment=MIDDLE, columnSpan=1]";
 		
 		assertEquals(expected, data.toString());
 	}
 }
