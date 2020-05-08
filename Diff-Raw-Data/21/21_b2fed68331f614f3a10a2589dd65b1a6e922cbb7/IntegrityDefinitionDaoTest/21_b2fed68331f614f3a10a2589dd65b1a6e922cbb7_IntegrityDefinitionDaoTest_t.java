 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 7, 2012
  */
 package edu.cmu.sv.arinc838.dao;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotEquals;
 import static org.testng.Assert.assertNull;
 
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.arinc.arinc838.IntegrityDefinition;
 
 import edu.cmu.sv.arinc838.dao.IntegrityDefinitionDao.IntegrityType;
 import edu.cmu.sv.arinc838.util.Converter;
 
 public class IntegrityDefinitionDaoTest {
 
 	private IntegrityDefinition integDef;
 	private IntegrityDefinitionDao integDao;
 
 	@BeforeMethod
 	public void setup() {
 		integDef = new IntegrityDefinition();
 		integDef.setIntegrityType(IntegrityType.CRC16.getType());
 		integDef.setIntegrityValue(Converter.hexToBytes("DEADBEEF"));
 	}
 
 	@Test
 	public void testXmlConstructor() {
 		integDao = new IntegrityDefinitionDao(integDef);
 
 		assertEquals(integDef.getIntegrityType(), integDao.getIntegrityType());
 		assertEquals(integDef.getIntegrityValue(), integDao.getIntegrityValue());
 	}
 
 	@Test
 	public void testIntegrityTypeAccessors () {
 		integDao = new IntegrityDefinitionDao();
 		long type1 = IntegrityType.CRC16.getType();
 		long newType = IntegrityType.CRC64.getType();
 		
 		integDef.setIntegrityType(type1);
 		integDao.setIntegrityType(newType);
 		assertNotEquals(integDef.getIntegrityType(), integDao.getIntegrityType());
 		
 		integDao.setIntegrityType(integDef.getIntegrityType());
 		assertEquals(integDef.getIntegrityType(), integDao.getIntegrityType());
 	}
 	
 	@Test
 	public void testSetIntegrityValue() {
 		integDao = new IntegrityDefinitionDao();
 	
 		byte[] b1 = new byte[] { 0x11 };
 		byte[] b2 = new byte[] { 0x11, 0x12 };
 
 		integDef.setIntegrityValue(b1);
 		integDao.setIntegrityValue(b2);
 		
 		assertNotEquals(integDef.getIntegrityValue(), integDao.getIntegrityValue());
 		
 		integDao.setIntegrityValue(integDef.getIntegrityValue());
 		assertEquals(integDef.getIntegrityValue(), integDao.getIntegrityValue());
 	}
 	
 	@Test
 	public void testIntegrityTypeEnum()
 	{
 		assertEquals(2, IntegrityDefinitionDao.IntegrityType.CRC16.getType());
 		assertEquals(3, IntegrityDefinitionDao.IntegrityType.CRC32.getType());
 		assertEquals(6, IntegrityDefinitionDao.IntegrityType.CRC64.getType());		
 	}
 	
 	@Test
 	public void testIntegrityTypeEnumFromLong()
 	{
 		assertEquals(IntegrityDefinitionDao.IntegrityType.CRC16, IntegrityDefinitionDao.IntegrityType.fromLong(2));
 		assertEquals(IntegrityDefinitionDao.IntegrityType.CRC32, IntegrityDefinitionDao.IntegrityType.fromLong(3));
 		assertEquals(IntegrityDefinitionDao.IntegrityType.CRC64, IntegrityDefinitionDao.IntegrityType.fromLong(6));
 		assertNull(IntegrityDefinitionDao.IntegrityType.fromLong(-1));
 	}
 	
 	@Test
 	public void testHashCode(){
		IntegrityDefinitionDao integDao = new IntegrityDefinitionDao(integDef);
 		assertEquals(integDao.hashCode(), integDao.getIntegrityValue().hashCode());
 	}
 	
 	@Test 
 	public void testHashCodeWithNoIntegrity(){
 		assertEquals(new IntegrityDefinitionDao().hashCode(), 0);
 	}
 	
 	@Test
 	public void testEquals(){
		IntegrityDefinitionDao first = new IntegrityDefinitionDao(integDef);
 		IntegrityDefinitionDao second = new IntegrityDefinitionDao(integDef);
 		
		assertEquals(first, second);
		assertEquals(second, first);
		assertNotEquals("null", first);
		
 	}
 }
