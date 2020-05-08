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
 import edu.psu.iam.cpr.core.database.tables.PhonesTable;
 import edu.psu.iam.cpr.core.database.types.PhoneType;
 import edu.psu.iam.cpr.core.error.CprException;
 import edu.psu.iam.cpr.core.error.GeneralDatabaseException;
 import edu.psu.iam.cpr.core.util.ValidatePhone;
 
 /**
  * @author jvuccolo
  *
  */
 public class ValidatePhoneTest {
 
 	private static Database db = new Database();
 	
 	public static void openDbConnection() throws GeneralDatabaseException {
 		db.openSession(SessionFactoryUtil.getSessionFactory());
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidExtension(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidExtension1() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidExtension(null), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidExtension(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidExtension2() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidExtension(""), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidExtension(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidExtension3() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidExtension("x"), false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidExtension(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidExtension4() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidExtension("x1"), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidExtension(java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidExtension5() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidExtension("1"), true);
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber1() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber(null, null), false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber2() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("Y", null), false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber3() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "112-1111"), false);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber4() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "8141112456"), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber5() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "814 111 2456"), true);
 
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber6() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "814-111-2456"), true);
 
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber7() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "(814)-111-2456"), true);
 
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber8() {
		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "(814)-111-2456"), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber9() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("Y", "+44 20 1112 1112"), true);
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#isValidPhoneNumber(java.lang.String, java.lang.String)}.
 	 */
 	@Test
 	public final void testIsValidPhoneNumber10() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("N", "814-278-9153"), true);
 	}
 	@Test
 	public final void testIsValidPhoneNumberNotYesOrNo() {
 		AssertJUnit.assertEquals(ValidatePhone.isValidPhoneNumber("F", "814-278-9153"), false);
 	}
 
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 )}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersNoType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, null,  null, null, null, null);
 		db.closeSession();
 	}
 	
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesRequestedByNull() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "home",  "814-278-9153", "1121", null, null);
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesRequestedByBlank() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "home",  "814-278-9153", "1121", null, "");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesRequestedByTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "home", "814-278-9153", "1121", null, "asdfghjklzxcvbnmqwertyuiopasdfgh");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersInvalidType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "home",  "814-278-9153", "1121", "N", "system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersInvalidTypenull() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, null,  "814-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersInvalidTypeBlank() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "", "814-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersInvalidPhoneNumber() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "Local_phone",  "84-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
  @Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParameterInvalidIntlPhone() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "Local_phone",  "84-278-9153", "1121", "Y", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersValidPhoneBadIntl() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "local_phone", "814-278-9153", "1121", "F", "jvuccolo");
 		db.closeSession();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesBadExtension() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "home", "814-278-9153", "abcd", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesBadIntlInd() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "LOCAL_PHONE",  "814-278-9153", "1121", "F", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesNoPhoneNumber() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 0, "Local_phone",null, "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testValidateAddPhonesParametersGoodRequest() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 100000,  "permanent_phone",  "814-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersPhoneTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 100000,  "local_phone", "123456789012345678901234567890123456789012", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersExtensionTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 100000,  "permanent_phone", "814-278-9153", "123456789012345678901234567890123456789012", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateAddPhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateAddPhonesParametersBadType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateAddPhonesParameters(db, 100000,  "phone", "814-278-9153", "123456789012345678901234567890123456789012", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesGroupId() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 100000, null, null,"system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesTypeNull() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 100000, null, 1L,"system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesTypeBlank() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 100000, "", 1L,"system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesTypeInvalid() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 100000, "Legal_name", 1L,"system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesNullRequestedBy() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 0, "permanent_phone", 1L, null);
 		db.closeSession();
 		
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesBlankRequestedBy() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 0, "permanent_phone", 1L, "");
 		db.closeSession();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateArchivePhonesTooLongRequestedBy() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 0, "permanent_phone", 1L, "asdfghjklzxcvbnmqwertyuiopasdfgh");
 		db.closeSession();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateArchivePhonesParameters(int, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testValidateArchivePhonesGoodData() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateArchiveAndSetPrimaryPhonesParameters(db, 0, "permanent_phone", 1L, "system");
 		db.closeSession();
 	}	
 	
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateGetPhonesParameters(int, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateGetPhonesParametersRequestedByNull() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateGetPhonesParameters(db, 0, null,null,"Y");
 		db.closeSession();
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateGetPhonesParameters(int, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateGetPhonesRequestedByBlank() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateGetPhonesParameters(db, 0, " ",null,"Y");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateGetPhonesParameters(int, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateGetPhonesRequestedTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateGetPhonesParameters(db, 0,  "asdfghjklqwertyuiopzxcvbnmasdfg",null,"Y");
 		db.closeSession();
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateGetPhonesParameters(int, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testValidateGetPhonesParametersAll() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		PhonesTable phonesTable = ValidatePhone.validateGetPhonesParameters(db, 100000, "llg5","work_phone","n");
 		AssertJUnit.assertFalse(phonesTable.isReturnHistoryFlag());
 		AssertJUnit.assertEquals(phonesTable.getPhoneType(),PhoneType.WORK_PHONE);
 		db.closeSession();
 	}
 
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesBlankType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0,"", 1L,  null, null, null, "system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesNoType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, null, 1L,  null, null, null, "system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesBadType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "home", 1L,  null, null, null, "system");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesUpdateParametersInvalidGroupId() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "local_phone", null, "814 278 9153", null, null, "system");
 		db.closeSession();
 	}
 	
 	
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesBadPhone() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "local_phone", 1L,  "84-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testValidateUpdatePhonesParametersInvalidIntlNo() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "local_phone", 1L,  "12345842789153", "1121", "Y", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesParameters9() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "permanent_phone",  null, "84-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 		
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesInvalidType() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "home", 1L,  "814-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesInvalidExtension() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "local_phone", 1L,  "814-278-9153", "abcd", "N", "jvuccolo");
 		db.closeSession();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesNonPhoneNo() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "work_phone",1L, null, "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesBadIntlInd() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "work_phone", 1L,  "814-278-9153", "1121", "F", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test
 	public final void testValidateUpdatePhonesParameters13() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 100000, "work_phone", 1L,  "814-278-9153", "1121", "N", "jvuccolo");
 		db.closeSession();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesPhoneTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "employee_home", null,  "123456789012345678901234567890123456789012", "1234", "N", "jvuccolo");
 		openDbConnection();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesExtensionTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "permanent_phone", null,  "814-278-9153", "123456789012345678901234567890123456789012", "N", "jvuccolo");
 		openDbConnection();
 	}
 	
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesUpdateByTooLong() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "permanent_phone", 1L,  "814-278-9153", "1234", "N", "asdfghjklqwertyuiopzxcvbnmasdfg");
 		openDbConnection();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesUpdateNull() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "permanent_phone", 1L,  "814-278-9153", "1234", "N", null);
 		openDbConnection();
 	}
 	/**
 	 * Test method for {@link edu.psu.iam.cpr.core.util.ValidatePhone#validateUpdatePhonesParameters(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
 	 * @throws CprException 
 	 * @throws GeneralDatabaseException 
 	 */
 	@Test(expectedExceptions=CprException.class)
 	public final void testValidateUpdatePhonesUpdateByBlank() throws CprException, GeneralDatabaseException {
 		openDbConnection();
 		ValidatePhone.validateUpdatePhonesParameters(db, 0, "permanent_phone", 1L,"814-278-9153", "1234", "N", " ");
 		openDbConnection();
 	}
 }
