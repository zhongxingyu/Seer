 /**
  * 
  */
 package edu.psu.iam.cpr.core.tests;
 
 /**
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import org.testng.annotations.Test;
 import org.testng.AssertJUnit;
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.SessionFactoryUtil;
 import edu.psu.iam.cpr.core.database.beans.IdentifierType;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.GeneralDatabaseException;
 import edu.psu.iam.cpr.core.util.Validate;
 
 /**
  * @author jvuccolo
  *
  */
 public class ValidateTest {
 
 	private static Database db = new Database();
 	
 	public static void openDbConnection() throws GeneralDatabaseException {
 		db.openSession(SessionFactoryUtil.getSessionFactory());
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidYesNo(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidYesNo1() {
 		String retValue = Validate.isValidYesNo(null);
 		AssertJUnit.assertEquals(retValue, "N");
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidYesNo(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidYesNo2() {
 		String retValue = Validate.isValidYesNo("X");
 		AssertJUnit.assertEquals(retValue, null);
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidYesNo(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidYesNo3() {
 		String retValue = Validate.isValidYesNo("y");
 		AssertJUnit.assertEquals(retValue, "Y");
 	}
 	
 	@Test
 	public final void testIsValidYesNo4() {
 		String retValue = Validate.isValidYesNo("yes");
		AssertJUnit.assertEquals(retValue, null);
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidPsuId(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPsuId1() {
 		boolean retValue = Validate.isValidPsuId(null);
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidPsuId(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPsuId2() {
 		boolean retValue = Validate.isValidPsuId("");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidPsuId(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPsuId3() {
 		boolean retValue = Validate.isValidPsuId("9");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidPsuId(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPsuId4() {
 		boolean retValue = Validate.isValidPsuId("91234567890");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidPsuId(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPsuId5() {
 		boolean retValue = Validate.isValidPsuId("912345678");
 		AssertJUnit.assertEquals(retValue, true);
 	}
 
 //	/**
 //	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidIdentifierType(java.lang.String)}.
 //	 */
 //	@Test
 //	public final void testIsValidIdentifierType1() {
 //		assertNull(Validate.isValidIdentifierType(db, null));
 //	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidIdentifierType(java.lang.String)}.
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testIsValidIdentifierType2() throws GeneralDatabaseException {
 		openDbConnection();
 		IdentifierType t = Validate.isValidIdentifierType(db, "person_id");
 		db.closeSession();
 		AssertJUnit.assertNotNull(t);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidIdentifierType(java.lang.String)}.
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testIsValidIdentifierType3() throws GeneralDatabaseException {
 		openDbConnection();
 		IdentifierType t = Validate.isValidIdentifierType(db, "psu_id");
 		db.closeSession();
 		AssertJUnit.assertNotNull(t);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.Validate#isValidIdentifierType(java.lang.String)}.
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testIsValidIdentifierType4() throws GeneralDatabaseException {
 		openDbConnection();
 		IdentifierType t = Validate.isValidIdentifierType(db, "userid");
 		db.closeSession();
 		AssertJUnit.assertNotNull(t);
 	}
 	
 	@Test
 	public final void testIsValidDate1() {
 		boolean retValue = Validate.isValidDate(null);
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidDate2() {
 		boolean retValue = Validate.isValidDate("");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidDate3() {
 		boolean retValue = Validate.isValidDate("m/d/y");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidDate4() {
 		boolean retValue = Validate.isValidDate("1/1/1");
 		AssertJUnit.assertEquals(retValue, true);
 	}
 	@Test
 	public final void testIsValidDate5() {
 		boolean retValue = Validate.isValidDate("12/33/2010");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidDate6() {
 		boolean retValue = Validate.isValidDate("12/31/2010");
 		AssertJUnit.assertEquals(retValue, true);
 	}
 	@Test
 	public final void testIsValidPartialDate1() {
 		boolean retValue = Validate.isValidPartialDate(null);
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidPartialDate2() {
 		boolean retValue = Validate.isValidPartialDate("");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidPartialDate3() {
 		boolean retValue = Validate.isValidPartialDate("1");
 		AssertJUnit.assertEquals(retValue, false);
 	}
 	@Test
 	public final void testIsValidPartialDate4() {
 		boolean retValue = Validate.isValidPartialDate("1/1");
 		AssertJUnit.assertEquals(retValue, true);
 	}
 	
 	@Test
 	public final void testIsValidIdentifier1() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "PERSON_ID", "1"));
 		db.closeSession();
 	}
 	
 	@Test
 	public final void testIsValidIdentifier2() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "USERID", "adbcd"));
 		db.closeSession();
 	}
 	
 	@Test
 	public final void testIsValidIdentifier3() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "SSN", "abcd"));
 		db.closeSession();
 	}
 	
 	@Test
 	public final void testIsValidIdentifier4() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "PSU_ID", "abcd"));
 		db.closeSession();
 	}
 
 	@Test(expectedExceptions=Exception.class)
 	public final void testIsValidIdentifier7() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "PSU_ID", "11111111111111111111111111111111111111111111111111111111111111111111111"));
 		db.closeSession();
 	}
 
 	@Test
 	public final void testIsValidIdentifier5() throws GeneralDatabaseException, CprException {
 		openDbConnection();
 		AssertJUnit.assertTrue(Validate.isIdentifierLengthValid(db, "ID_CARD", "abcd"));
 		db.closeSession();
 	}
 	
 	@Test(expectedExceptions=Exception.class)
 	public final void testDoIdentifierCheck2() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		Validate.doIdentifierLengthCheck(db, "abcd1111111111111111111111111111111111111111111111111111111111", "Psu Id", "PSU_ID", "PSU_ID");
 		db.closeSession();
 	}
 
 	@Test
 	public final void testDoIdentifierCheck1() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		Validate.doIdentifierLengthCheck(db, "abcd", "Psu Id", "PSU_ID", "PSU_ID");
 		db.closeSession();
 	}
 	
 
 }
