 package org.glom.web.client.place;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.glom.web.server.Utils;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.Field.GlomFieldType;
 import org.junit.Test;
 
 import com.googlecode.gwt.test.GwtModule;
 import com.googlecode.gwt.test.GwtTest;
 
 @GwtModule("org.glom.web.OnlineGlom")
 public class GwtTestDetailsPlace extends GwtTest {
 
 	public GwtTestDetailsPlace() {
 	}
 
 	@Test
 	public void testGetPlaceNoParameters() {
 		checkTokenWithoutParameters("");
 		checkTokenWithoutParameters("something");
 		checkTokenWithoutParameters("list:a=1");
 		checkTokenWithoutParameters("value1=123");
 	}
 
 	@Test
 	public void testGetPlaceParametersTextPrimaryKey() {
 		// Create a DetailsPlace, testing getPlace():
 		final String documentId = "somedocument";
 		final String tableName = "sometable";
 		final String primaryKeyValue = "123";
 		DetailsPlace place = getDetailsPlaceFromToken("document=" + documentId + "&table=" + tableName + "&value="
 				+ primaryKeyValue);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 
 		// Recreate it, testing getToken(),
 		// checking that the same parameters are read back:
 		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
 		final String token = tokenizer.getToken(place);
 		place = getDetailsPlaceFromToken(token);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 	}
 	
 	@Test
 	public void testGetPlaceParametersNumericPrimaryKey() {
 		// Create a DetailsPlace, testing getPlace():
 		final String documentId = "somedocument";
 		final String tableName = "sometable";
 		final double primaryKeyValue = 123;
 		final String primaryKeyValueText = "123";
 		DetailsPlace place = getDetailsPlaceFromToken("document=" + documentId + "&table=" + tableName + "&value="
 				+ primaryKeyValueText);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 
 		// Recreate it, testing getToken(),
 		// checking that the same parameters are read back:
 		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
 		final String token = tokenizer.getToken(place);
 		place = getDetailsPlaceFromToken(token);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 	}
 
 	@Test
 	public void testGetPlaceParametersBooleanPrimaryKey() {
 		// Create a DetailsPlace, testing getPlace():
 		final String documentId = "somedocument";
 		final String tableName = "sometable";
 		final boolean primaryKeyValue = false;
 		final String primaryKeyValueText = "false";
 		DetailsPlace place = getDetailsPlaceFromToken("document=" + documentId + "&table=" + tableName + "&value="
 				+ primaryKeyValueText);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 
 		// Recreate it, testing getToken(),
 		// checking that the same parameters are read back:
 		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
 		final String token = tokenizer.getToken(place);
 		place = getDetailsPlaceFromToken(token);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 	}
 
 	@Test
 	public void testGetPlaceParametersDatePrimaryKey() {
 		// Create a DetailsPlace, testing getPlace():
 		final String documentId = "somedocument";
 		final String tableName = "sometable";
 		
 		//TODO: Is there no easier (and non-deprecated) way to create
		//a Date instance?
 		Calendar cal = new GregorianCalendar();
 		cal.setTime(new Date());
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.set(Calendar.YEAR, 1973);
 		cal.set(Calendar.MONTH, 4); /* The month is 0 indexed. */
 		cal.set(Calendar.DAY_OF_MONTH, 11);
 		final Date primaryKeyValue = cal.getTime();
 		//final Date primaryKeyValue = new Date(1973, 4, 11); /* The month is 0-indexed. */
 		
 		final String primaryKeyValueText = "1973-05-11";
 		DetailsPlace place = getDetailsPlaceFromToken("document=" + documentId + "&table=" + tableName + "&value="
 				+ primaryKeyValueText);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 
 		// Recreate it, testing getToken(),
 		// checking that the same parameters are read back:
 		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
 		final String token = tokenizer.getToken(place);
 		place = getDetailsPlaceFromToken(token);
 		checkParameters(place, documentId, tableName, primaryKeyValue);
 	}
 
 	/**
 	 * @param place
 	 * @param documentId
 	 * @param tableName
 	 * @param primaryKeyValue
 	 */
 	private void checkParameters(final DetailsPlace place, final String documentID, final String tableName, double primaryKeyValue) {
 		checkParametersBasic(place, documentID, tableName);
 		
 		//Check the value as a number:
 		final TypedDataItem dataItem = place.getPrimaryKeyValue();
 		Utils.transformUnknownToActualType(dataItem, GlomFieldType.TYPE_NUMERIC);
 		assertEquals(primaryKeyValue, dataItem.getNumber(), 0.1);
 	}
 
 	private void checkParameters(final DetailsPlace place, final String documentID, final String tableName,
 			final String primaryKeyValue) {
 		checkParametersBasic(place, documentID, tableName);
 
 		//Check the value as a string:
 		final TypedDataItem dataItem = place.getPrimaryKeyValue();
 		Utils.transformUnknownToActualType(dataItem, GlomFieldType.TYPE_TEXT);
 		assertEquals(primaryKeyValue, dataItem.getText());
 	}
 	
 	private void checkParameters(final DetailsPlace place, final String documentID, final String tableName,
 			final Date primaryKeyValue) {
 		checkParametersBasic(place, documentID, tableName);
 
 		//Check the value as a string:
 		final TypedDataItem dataItem = place.getPrimaryKeyValue();
 		Utils.transformUnknownToActualType(dataItem, GlomFieldType.TYPE_DATE);
 		assertEquals(primaryKeyValue, dataItem.getDate());
 	}
 	
 	private void checkParameters(final DetailsPlace place, final String documentID, final String tableName,
 			final boolean primaryKeyValue) {
 		checkParametersBasic(place, documentID, tableName);
 
 		//Check the value as a string:
 		final TypedDataItem dataItem = place.getPrimaryKeyValue();
 		Utils.transformUnknownToActualType(dataItem, GlomFieldType.TYPE_BOOLEAN);
 		assertEquals(primaryKeyValue, dataItem.getBoolean());
 	}
 
 	/**
 	 * @param place
 	 * @param documentID
 	 * @param tableName
 	 */
 	private void checkParametersBasic(final DetailsPlace place, final String documentID, final String tableName) {
 		assertNotNull(place);
 
 		assertEquals(documentID, place.getDocumentID());
 		assertEquals(tableName, place.getTableName());
 	}
 
 	private DetailsPlace getDetailsPlaceFromToken(final String token) {
 		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
 		final DetailsPlace place = tokenizer.getPlace(token);
 		assertNotNull(place);
 		return place;
 	}
 
 	private void checkTokenWithoutParameters(final String token) {
 		final DetailsPlace place = getDetailsPlaceFromToken(token);
 
 		assertNotNull(place.getDocumentID());
 		assertTrue(place.getDocumentID().isEmpty());
 
 		assertNotNull(place.getTableName());
 		assertTrue(place.getTableName().isEmpty());
 
 		assertNotNull(place.getPrimaryKeyValue());
 		assertTrue(place.getPrimaryKeyValue().isEmpty());
 		assertEquals(null, place.getPrimaryKeyValue().getUnknown());
 		assertEquals(0.0, place.getPrimaryKeyValue().getNumber(), 0.0); //TODO: Handle other types.
 		assertEquals(null, place.getPrimaryKeyValue().getText());
 	}
 
 }
