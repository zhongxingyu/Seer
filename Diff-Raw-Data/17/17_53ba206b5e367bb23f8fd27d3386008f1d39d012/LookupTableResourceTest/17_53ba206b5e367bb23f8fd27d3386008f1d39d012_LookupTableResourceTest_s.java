 package com.mulesoft.lookuptable.rest.resource;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.mulesfot.lookuptable.persistence.dao.LookUpTableDao;
 import com.mulesoft.lookuptable.rest.exceptions.CustomWebApplicationException;
 import com.mulesoft.lookuptable.rest.response.LookupManagerResponse;
 
 /**
  * Validates that the responses from {@link LookupTableResources} are correct by
  * mocking the responses the {@link LookUpTableDao} provides. In this way we
  * validate that in the event of an error we get a 500 http status code and if
  * everithing is OK we get a 200 http status code.
  * 
  * @author damiansima
  * 
  */
 public class LookupTableResourceTest {
 	private static final String FAKE_CUSTOMER = "fakecustomer";
 	private static final String FAKE_TABLE_NAME = "faketable";
 
 	private static final List<Field> SIMPLE_KEY_LIST = new ArrayList<Field>();
 	private static final List<Field> COMPOSITE_KEY_LIST = new ArrayList<Field>();
 	
 	private static final List<Field> SIMPLE_VALUE_LIST = new ArrayList<Field>();
 	private static final List<Field> COMPOSITE_VALUE_LIST = new ArrayList<Field>();
 
 	private static final List<Field> EMPTY_SIMPLE_VALUE_LIST = new ArrayList<Field>();
 	private static final List<Field> EMPTY_COMPOSITE_VALUE_LIST = new ArrayList<Field>();
 	
 	private LookUpTableDao mockDao;
 	
 	private static final Gson GSON = new Gson();
 
 	@Before
 	public void setUp() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
 		this.mockDao = EasyMock.createMock(LookUpTableDao.class);
 
 		java.lang.reflect.Field servicesField = LookUpTableDao.class.getDeclaredField("INSTANCE");
 		servicesField.setAccessible(true);
 		servicesField.set(LookUpTableDao.getInstance(), mockDao);

 		
 		SIMPLE_KEY_LIST.add(new Field("key_name_0", "key_value_0"));
 		COMPOSITE_KEY_LIST.add(new Field("key_name_0", "key_value_0"));
 		COMPOSITE_KEY_LIST.add(new Field("key_name_1", "key_value_1"));
 
 		SIMPLE_VALUE_LIST.add(new Field("value_name_0", "value_value_0"));
 		COMPOSITE_VALUE_LIST.add(new Field("value_name_0", "value_value_0"));
 		COMPOSITE_VALUE_LIST.add(new Field("value_name_1", "value_value_1"));
 		
 		EMPTY_SIMPLE_VALUE_LIST.add(new Field("value_name_0", ""));
 		EMPTY_COMPOSITE_VALUE_LIST.add(new Field("value_name_0", ""));
 		EMPTY_COMPOSITE_VALUE_LIST.add(new Field("value_name_1", ""));
 	}
 
 	@Test
 	public void createDataOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.createLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST , SIMPLE_VALUE_LIST)).andReturn(true);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		String response = resource.createData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 	
 	@Test(expected = CustomWebApplicationException.class)
 	public void createDataErrorResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.createLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST, SIMPLE_VALUE_LIST)).andReturn(false);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		String response = resource.createData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(500, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 	
 	@Test(expected=CustomWebApplicationException.class)
 	public void createDataMalFormedKeyTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		String jSONSimpleKeyList = GSON.toJson(new Field("key_name_0", "key_value_0"));
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		resource.createData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 	}
 	
 	@Test(expected=CustomWebApplicationException.class)
 	public void createDataMalFormedValueTest() {
 		LookupTableResource resource = new LookupTableResource();
 		
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(new Field("value_name_0", "value_value_0"));
 		
 		resource.createData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 	}
 
 	@Test
 	public void listDataNoKeyOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.getLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME)).andReturn("");
 
 		EasyMock.replay(mockDao);
 
 		String response = resource.listData(FAKE_CUSTOMER, FAKE_TABLE_NAME, null);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test
 	public void listDataWithKeyOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.getLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST)).andReturn("");
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String response = resource.listData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 	
 	@Test(expected=CustomWebApplicationException.class)
 	public void listDataMalFormedKeyeTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		String jSONSimpleKeyList = GSON.toJson(new Field("key_name_0", "key_value_0"));
 		resource.listData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList);
 	}
 
 	@Test
 	public void updateDataOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.updateLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST , SIMPLE_VALUE_LIST)).andReturn(true);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		String response = resource.updateData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test(expected = CustomWebApplicationException.class)
 	public void updateDataErrorResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.updateLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST , SIMPLE_VALUE_LIST)).andReturn(false);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		resource.updateData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test(expected=CustomWebApplicationException.class)
 	public void updateDataMalFormedKeyTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		String jSONSimpleKeyList = GSON.toJson(new Field("key_name_0", "key_value_0"));
 		String jSONEmptySimpleValueList = GSON.toJson(SIMPLE_VALUE_LIST);
 		
 		resource.updateData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 	}
 	
 	@Test(expected=CustomWebApplicationException.class)
 	public void updateDataMalFormedValueTest() {
 		LookupTableResource resource = new LookupTableResource();
 		
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String jSONEmptySimpleValueList = GSON.toJson(new Field("value_name_0", "value_value_0"));
 		
 		resource.updateData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList, jSONEmptySimpleValueList);
 	}
 
 	@Test
 	public void deleteDataNoKeyOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.deleteLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME)).andReturn(true);
 
 		EasyMock.replay(mockDao);
 
 		String response = resource.deleteData(FAKE_CUSTOMER, FAKE_TABLE_NAME, null);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test
 	public void deleteDataWhitKeyOkResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.deleteLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST)).andReturn(true);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		String response = resource.deleteData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList);
 
 		LookupManagerResponse actualResponse = new Gson().fromJson(response, LookupManagerResponse.class);
 		Assert.assertEquals(200, actualResponse.getHttpStatus());
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test(expected = CustomWebApplicationException.class)
 	public void deleteDataNoKeyErrorResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.deleteLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME)).andReturn(false);
 
 		EasyMock.replay(mockDao);
 
 		resource.deleteData(FAKE_CUSTOMER, FAKE_TABLE_NAME, null);
 
 		EasyMock.verify(mockDao);
 	}
 
 	@Test(expected = CustomWebApplicationException.class)
 	public void deleteDataWhitKeyErrorResponseTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		EasyMock.expect(mockDao.deleteLookupTableRecords(FAKE_CUSTOMER, FAKE_TABLE_NAME, SIMPLE_KEY_LIST)).andReturn(false);
 
 		EasyMock.replay(mockDao);
 
 		String jSONSimpleKeyList = GSON.toJson(SIMPLE_KEY_LIST);
 		resource.deleteData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList);
 
 		EasyMock.verify(mockDao);
 	}
 	
 	@Test(expected = CustomWebApplicationException.class)
 	public void deleteDataMalFormedKeyTest() {
 		LookupTableResource resource = new LookupTableResource();
 
 		String jSONSimpleKeyList = GSON.toJson(new Field("key_name_0", "key_value_0"));
 		
 		resource.deleteData(FAKE_CUSTOMER, FAKE_TABLE_NAME, jSONSimpleKeyList);
 	}
 	
 
 }
