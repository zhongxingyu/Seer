 package org.nees.rpi.vis.model;
 
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class TestDVDataChannel
 {
 	private DVDataChannel units;
 	private DVDataChannel unitless;
 	
 	@Before public void setUp()
 	{
 		units = new DVDataChannel("Units", "mb", new float[] { 0.0f, 10.0f, 100.0f});
 		unitless = new DVDataChannel("Unitless", new float[] { 0.0f, 10.0f, 100.0f});
 	}
 	
 	@Test
 	public void testGetData()
 	{
 		assertTrue(Arrays.equals(new float[] { 0.0f, 10.0f, 100.0f }, units.data()));
 		assertTrue(Arrays.equals(new float[] { 0.0f, 10.0f, 100.0f }, unitless.data()));
 	}
 	
 	@Test
 	public void testGetName()
 	{
 		assertEquals("Units", units.name());
 		assertEquals("Unitless", unitless.name());
 	}
 	
 	@Test
 	public void testGetUnit()
 	{
 		assertEquals("mb", units.unit().get());
 		assertTrue(unitless.unit().isEmpty());
 	}
 	
 	@Test
 	public void testHasUnit()
 	{
 		assertTrue(units.hasUnit());
 		assertFalse(unitless.hasUnit());
 	}
 	
 	@Test
 	public void testGetParent()
 	{
 		DVDataChannel dc = new DVDataChannel("Test", new float[] {});
 		DVDataFile df = new DVDataFile(null, null, null);
		dc = new DVDataChannel(dc, df);
 		assertTrue(units.parent().isEmpty());
 		assertTrue(dc.parent().isDefined());
 		assertEquals(df, dc.parent().get());
 	}
 }
