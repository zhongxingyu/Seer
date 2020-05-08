 /*
  * Copyright (c) Open Source Strategies, Inc.
  *
  * Opentaps is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Opentaps is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.opentaps.dataimport;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import javolution.util.FastList;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.apache.log4j.Logger;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 import org.hibernate.search.util.LoggerFactory;
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilValidate;
 import org.opentaps.base.entities.DataImportCategory;
 import org.opentaps.base.entities.DataImportCustomer;
 import org.opentaps.base.entities.DataImportGeo;
 import org.opentaps.base.entities.DataImportGlAccount;
 import org.opentaps.base.entities.DataImportInventory;
 import org.opentaps.base.entities.DataImportParty;
 import org.opentaps.base.entities.DataImportPresupuestoEgreso;
 import org.opentaps.base.entities.DataImportPresupuestoIngreso;
 import org.opentaps.base.entities.DataImportProduct;
 import org.opentaps.base.entities.DataImportProject;
 import org.opentaps.base.entities.DataImportSupplier;
 import org.opentaps.base.entities.DataImportTag;
 import org.opentaps.base.entities.DataImportContableGuide;
 import org.opentaps.base.entities.DataimportCatalogoConceptos;
 import org.opentaps.base.entities.DataimportCatalogoSubconceptos;
 import org.opentaps.base.entities.DataImportMatrizEgr;
 import org.opentaps.base.entities.DataImportMatrizIng;
 import org.opentaps.base.entities.DataImportIngresoDiario;
 import org.opentaps.base.entities.DataImportEgresoDiario;
 import org.opentaps.base.entities.DataImportOperacionDiaria;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.domain.party.PartyRepositoryInterface;
 import org.opentaps.foundation.entity.EntityInterface;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 import com.ibm.icu.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * Common services and helper methods related to Excel files uploading and
  * management.
  */
 public final class ExcelImportServices extends DomainService {
 
 	private static final String MODULE = ExcelImportServices.class.getName();
 	private static final String EXCEL_CGUIDE_TAB = "ContableGuide";
 	private static final String EXCEL_CGUIDE_TAB_CON = "ContableGuideCon";
 	private static final String EXCEL_CGUIDE_TAB_SCON = "ContableGuideSCon";
 	private static final String EXCEL_PRODUCT_TAB = "Products";
 	private static final String EXCEL_SUPPLIERS_TAB = "Suppliers";
 	private static final String EXCEL_CUSTOMERS_TAB = "Customers";
 	private static final String EXCEL_INVENTORY_TAB = "Inventory";
 	private static final String EXCEL_GL_ACCOUNTS_TAB = "GL Accounts";
 	private static final String EXCEL_TAG_TAB = "Tag";
 	private static final String EXCEL_PARTY_TAB = "Party";
 	private static final String EXCEL_PROJECT_TAB = "Project";
 	private static final String EXCEL_GEO_TAB = "GEO";
 	private static final String EXCEL_CATEGORY_TAB = "Category";
 	private static final String EXCEL_PRESUPUESTO_INGRESO_TAB = "Presupuesto Ingreso";
 	private static final String EXCEL_PRESUPUESTO_EGRESO_TAB = "Presupuesto Egreso";
 	private static final String EXCEL_EGRESO_DIARIO_TAB = "OD Egreso";
 	private static final String EXCEL_INGRESO_DIARIO_TAB = "OD Ingreso";
 	private static final String EXCEL_OPERACION_DIARIA_TAB = "ODiaria";
 
 	private static final String EXCEL_MATRIZ_CONVERSION_EGR_TAB = "Matriz_Conversion_Egresos";
 	private static final String EXCEL_MATRIZ_CONVERSION_ING_TAB = "Matriz_Conversion_Ingresos";
 
 	private static final List<String> EXCEL_TABS = Arrays.asList(
 			EXCEL_PRODUCT_TAB, EXCEL_SUPPLIERS_TAB, EXCEL_CUSTOMERS_TAB,
 			EXCEL_INVENTORY_TAB, EXCEL_GL_ACCOUNTS_TAB, EXCEL_TAG_TAB,
 			EXCEL_PARTY_TAB, EXCEL_PROJECT_TAB, EXCEL_GEO_TAB,
 			EXCEL_CATEGORY_TAB, EXCEL_PRESUPUESTO_INGRESO_TAB,
 			EXCEL_PRESUPUESTO_EGRESO_TAB, EXCEL_CGUIDE_TAB,
 			EXCEL_MATRIZ_CONVERSION_EGR_TAB, EXCEL_MATRIZ_CONVERSION_ING_TAB,
 			EXCEL_EGRESO_DIARIO_TAB, EXCEL_INGRESO_DIARIO_TAB,
 			EXCEL_OPERACION_DIARIA_TAB, EXCEL_CGUIDE_TAB_CON,
 			EXCEL_CGUIDE_TAB_SCON);
 
 	private static Logger logger = Logger.getLogger(ExcelImportServices.class);
 	private String uploadedFileName;
 
 	private Session session;
 	/**
 	 * Default constructor.
 	 */
 	public ExcelImportServices() {
 		super();
 	}
 
 	/**
 	 * Creates a new <code>ExcelImportServices</code> instance.
 	 * 
 	 * @param infrastructure
 	 *            an <code>Infrastructure</code> value
 	 * @param user
 	 *            an <code>User</code> value
 	 * @param locale
 	 *            a <code>Locale</code> value
 	 * @exception ServiceException
 	 *                if an error occurs
 	 */
 	public ExcelImportServices(Infrastructure infrastructure, User user,
 			Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/**
 	 * Gets the specified Excel File in the given directory.
 	 * 
 	 * @param path
 	 *            the path <code>String</code> of the directory to look files
 	 *            into
 	 * @param fileName
 	 *            the name of the file to find in the path
 	 * @return the File found
 	 */
 	public File getUploadedExcelFile(String path, String fileName) {
 		String name = path;
 		if (File.separatorChar == name.charAt(name.length() - 1)) {
 			name += File.separatorChar;
 		}
 		name += fileName;
 
 		if (UtilValidate.isNotEmpty(name)) {
 			File file = new File(name);
 			if (file.canRead()) {
 				return file;
 			} else {
 				Debug.logWarning("File not found or can't be read " + name,
 						MODULE);
 				return null;
 			}
 		} else {
 			Debug.logWarning("No path specified, doing nothing", MODULE);
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the specified Excel File in the default directory.
 	 * 
 	 * @param fileName
 	 *            the name of the file to find in the path
 	 * @return the File found
 	 */
 	public File getUploadedExcelFile(String fileName) {
 		return getUploadedExcelFile(CommonImportServices.getUploadPath(),
 				fileName);
 	}
 
 	/**
 	 * Helper method to check if an Excel row is empty.
 	 * 
 	 * @param row
 	 *            a <code>HSSFRow</code> value
 	 * @return a <code>boolean</code> value
 	 */
 	public boolean isNotEmpty(HSSFRow row) {
 		if (row == null) {
 			return false;
 		}
 		String s = row.toString();
 		if (s == null) {
 			return false;
 		}
 		return !"".equals(s.trim());
 	}
 
 	/**
 	 * Helper method to read a String cell and auto trim it.
 	 * 
 	 * @param row
 	 *            a <code>HSSFRow</code> value
 	 * @param index
 	 *            the column index <code>int</code> value which is then casted
 	 *            to a short
 	 * @return a <code>String</code> value
 	 */
 	public String readStringCell(HSSFRow row, int index) {
 		HSSFCell cell = row.getCell(index);
 
 		if (cell == null) {
 			return null;
 		}
 
 		// check if cell contains a number
 		BigDecimal bd = null;
 		try {
 			double d = cell.getNumericCellValue();
 
 			bd = BigDecimal.valueOf(d);
 
 		} catch (Exception e) {
 			// do nothing
 		}
 
 		String s = null;
 		if (bd == null) {
 			s = cell.toString().trim();
 
 		} else {
 			// if cell contains number parse it as long
 			s = Long.toString(bd.longValue());
 
 		}
 
 		return s;
 	}
 
 	public String readStringCellPoint(HSSFRow row, int index) {
 		HSSFCell cell = row.getCell(index);
 		if (cell == null) {
 			return null;
 		}
 
 		// check if cell contains a number
 		BigDecimal bd = null;
 		try {
 			double d = cell.getNumericCellValue();
 			bd = BigDecimal.valueOf(d);
 		} catch (Exception e) {
 			// do nothing
 		}
 
 		String s = null;
 		if (bd == null) {
 			s = cell.toString().trim();
 		} else {
 			// if cell contains number parse it as long
 			String remainder = bd.remainder(new BigDecimal(1)).toString();
 			if (remainder.equalsIgnoreCase("0")
 					|| remainder.equalsIgnoreCase("0.0")) {
 				s = Long.toString(bd.longValue());
 				if (s.equalsIgnoreCase("0"))
 					s = null;
 			} else {
 				s = bd.toString();
 			}
 		}
 
 		return s;
 	}
 
 	/**
 	 * Helper method to read a Long cell and auto trim it.
 	 * 
 	 * @param row
 	 *            a <code>HSSFRow</code> value
 	 * @param index
 	 *            the column index <code>int</code> value which is then casted
 	 *            to a short
 	 * @return a <code>Long</code> value
 	 */
 	public Long readLongCell(HSSFRow row, int index) {
 		HSSFCell cell = row.getCell(index);
 		if (cell == null) {
 			return null;
 		}
 
 		BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
 		if (bd == null) {
 			return null;
 		}
 		return bd.longValue();
 	}
 
 	/**
 	 * Helper method to read a BigDecimal cell and auto trim it.
 	 * 
 	 * @param row
 	 *            a <code>HSSFRow</code> value
 	 * @param index
 	 *            the column index <code>int</code> value which is then casted
 	 *            to a short
 	 * @return a <code>BigDecimal</code> value
 	 */
 	public BigDecimal readBigDecimalCell(HSSFRow row, int index) {
 		HSSFCell cell = row.getCell(index);
 		if (cell == null) {
 			return null;
 		}
 
 		return BigDecimal.valueOf(cell.getNumericCellValue());
 	}
 
 	/**
 	 * Takes each row of an Excel sheet and put it into DataImportProduct.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportProduct entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportProducts(
 			HSSFSheet sheet) throws RepositoryException {
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		List<DataImportProduct> products = FastList.newInstance();
 
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read productId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("productId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Products tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				// Se modifico datos de entrada
 				DataImportProduct product = new DataImportProduct();
 				product.setProductId(id);
 				int rowCount = 2;
 				product.setProductName(readStringCell(row, 1));
 				product.setInternalName(readStringCell(row, 1));
 				product.setProductTypeId(readStringCell(row, rowCount++));
 				product.setDescription(readStringCell(row, rowCount++));
 				product.setCategory(readStringCell(row, rowCount++));
 				product.setGlAccountId(readStringCellPoint(row, rowCount++));
 				product.setOrganization(readStringCell(row, rowCount++));
 				products.add(product);
 			}
 		}
 		return products;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportSupplier.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportSupplier entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportSuppliers(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportSupplier> suppliers = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read supplierId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("supplierId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Suppliers tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportSupplier supplier = new DataImportSupplier();
 				supplier.setSupplierId(id);
 				supplier.setSupplierName(readStringCell(row, 1));
 				supplier.setAddress1(readStringCell(row, 2));
 				supplier.setAddress2(readStringCell(row, 3));
 				supplier.setCity(readStringCell(row, 4));
 				supplier.setStateProvinceGeoId(readStringCell(row, 5));
 				supplier.setPostalCode(readStringCell(row, 6));
 				supplier.setCountryGeoId(readStringCell(row, 7));
 				supplier.setPrimaryPhoneCountryCode(readStringCell(row, 8));
 				supplier.setPrimaryPhoneAreaCode(readStringCell(row, 9));
 				supplier.setPrimaryPhoneNumber(readStringCell(row, 10));
 				supplier.setNetPaymentDays(readLongCell(row, 11));
 				supplier.setIsIncorporated(readStringCell(row, 12));
 				supplier.setFederalTaxId(readStringCell(row, 13));
 				supplier.setRequires1099(readStringCell(row, 14));
 				suppliers.add(supplier);
 			}
 		}
 
 		return suppliers;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportCustomer.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportCustomer entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportCustomers(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportCustomer> customers = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read customerId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("customerId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Customers tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportCustomer customer = new DataImportCustomer();
 				customer.setCustomerId(id);
 				int rowCount = 1; // keep track of the row
 				customer.setCompanyName(this.readStringCell(row, rowCount++));
 				customer.setFirstName(this.readStringCell(row, rowCount++));
 				customer.setLastName(this.readStringCell(row, rowCount++));
 				customer.setAttnName(this.readStringCell(row, rowCount++));
 				customer.setAddress1(this.readStringCell(row, rowCount++));
 				customer.setAddress2(this.readStringCell(row, rowCount++));
 				customer.setCity(this.readStringCell(row, rowCount++));
 				customer.setStateProvinceGeoId(this.readStringCell(row,
 						rowCount++));
 				customer.setPostalCode(this.readStringCell(row, rowCount++));
 				customer.setPostalCodeExt(this.readStringCell(row, rowCount++));
 				customer.setCountryGeoId(this.readStringCell(row, rowCount++));
 				customer.setPrimaryPhoneCountryCode(this.readStringCell(row,
 						rowCount++));
 				customer.setPrimaryPhoneAreaCode(this.readStringCell(row,
 						rowCount++));
 				customer.setPrimaryPhoneNumber(this.readStringCell(row,
 						rowCount++));
 				customer.setPrimaryPhoneExtension(this.readStringCell(row,
 						rowCount++));
 				customer.setSecondaryPhoneCountryCode(this.readStringCell(row,
 						rowCount++));
 				customer.setSecondaryPhoneAreaCode(this.readStringCell(row,
 						rowCount++));
 				customer.setSecondaryPhoneNumber(this.readStringCell(row,
 						rowCount++));
 				customer.setSecondaryPhoneExtension(this.readStringCell(row,
 						rowCount++));
 				customer.setFaxCountryCode(this.readStringCell(row, rowCount++));
 				customer.setFaxAreaCode(this.readStringCell(row, rowCount++));
 				customer.setFaxNumber(this.readStringCell(row, rowCount++));
 				customer.setDidCountryCode(this.readStringCell(row, rowCount++));
 				customer.setDidAreaCode(this.readStringCell(row, rowCount++));
 				customer.setDidNumber(this.readStringCell(row, rowCount++));
 				customer.setDidExtension(this.readStringCell(row, rowCount++));
 				customer.setEmailAddress(this.readStringCell(row, rowCount++));
 				customer.setWebAddress(this.readStringCell(row, rowCount++));
 				customer.setDiscount(this.readBigDecimalCell(row, rowCount++));
 				customer.setPartyClassificationTypeId(this.readStringCell(row,
 						rowCount++));
 				customer.setCreditCardNumber(this.readStringCell(row,
 						rowCount++));
 				customer.setCreditCardExpDate(this.readStringCell(row,
 						rowCount++));
 				customer.setOutstandingBalance(this.readBigDecimalCell(row,
 						rowCount++));
 				customer.setCreditLimit(this
 						.readBigDecimalCell(row, rowCount++));
 				customer.setCurrencyUomId(this.readStringCell(row, rowCount++));
 				customer.setDisableShipping(this
 						.readStringCell(row, rowCount++));
 				customer.setNetPaymentDays(this.readLongCell(row, rowCount++));
 				customer.setShipToCompanyName(this.readStringCell(row,
 						rowCount++));
 				customer.setShipToFirstName(this
 						.readStringCell(row, rowCount++));
 				customer.setShipToLastName(this.readStringCell(row, rowCount++));
 				customer.setShipToAttnName(this.readStringCell(row, rowCount++));
 				customer.setShipToAddress1(this.readStringCell(row, rowCount++));
 				customer.setShipToAddress2(this.readStringCell(row, rowCount++));
 				customer.setShipToCity(this.readStringCell(row, rowCount++));
 				customer.setShipToStateProvinceGeoId(this.readStringCell(row,
 						rowCount++));
 				customer.setShipToPostalCode(this.readStringCell(row,
 						rowCount++));
 				customer.setShipToPostalCodeExt(this.readStringCell(row,
 						rowCount++));
 				customer.setShipToStateProvGeoName(this.readStringCell(row,
 						rowCount++));
 				customer.setShipToCountryGeoId(this.readStringCell(row,
 						rowCount++));
 				customer.setNote(this.readStringCell(row, rowCount++));
 				customers.add(customer);
 			}
 		}
 
 		return customers;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportInventory.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportInventory entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportInventory(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportInventory> inventory = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read itemId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("itemId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Inventory tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportInventory inventoryItem = new DataImportInventory();
 				inventoryItem.setItemId(id);
 				inventoryItem.setProductId(this.readStringCell(row, 1));
 				inventoryItem.setFacilityId(this.readStringCell(row, 2));
 				inventoryItem.setAvailableToPromise(this.readBigDecimalCell(
 						row, 3));
 				inventoryItem.setOnHand(this.readBigDecimalCell(row, 4));
 				inventoryItem.setMinimumStock(this.readBigDecimalCell(row, 5));
 				inventoryItem.setReorderQuantity(this
 						.readBigDecimalCell(row, 6));
 				inventoryItem.setDaysToShip(this.readBigDecimalCell(row, 7));
 				inventoryItem
 						.setInventoryValue(this.readBigDecimalCell(row, 8));
 				inventory.add(inventoryItem);
 			}
 		}
 
 		return inventory;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportGlAccount.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportGlAccount entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportGlAccounts(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportGlAccount> glAccounts = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read glAccountrId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCellPoint(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("glAccountId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from GL Accounts tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportGlAccount glAccount = new DataImportGlAccount();
 				glAccount.setGlAccountId(id);
 				glAccount
 						.setParentGlAccountId(this.readStringCellPoint(row, 1));
 				glAccount.setClassification(this.readStringCell(row, 2));
 				glAccount.setAccountName(this.readStringCell(row, 3));
 				glAccount.setCodificacion(this.readStringCell(row, 4));
 				glAccount.setNaturaleza(this.readStringCell(row, 5));
 				glAccount.setTipoCuenta(this.readStringCell(row, 6));
 				glAccount.setSaldoinicial(this.readBigDecimalCell(row, 7));
 				glAccount.setType(this.readStringCell(row, 8));
 				glAccount.setMajorGlAccount(this.readStringCell(row, 9));
 				glAccount.setCatalog(this.readStringCell(row, 10));
 				glAccount.setNode(this.readStringCell(row, 11));
 				glAccounts.add(glAccount);
 			}
 		}
 
 		return glAccounts;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportTag.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportTag entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportTag(
 			HSSFSheet sheet) throws RepositoryException {
 
 		LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 				.getLedgerDomain().getLedgerRepository();
 
 		List<DataImportTag> tags = FastList.newInstance();
 		// List<DataImportCustomer> customers = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		try {
 			for (int j = 1; j <= sheetLastRowNumber; j++) {
 				HSSFRow row = sheet.getRow(j);
 				if (isNotEmpty(row)) {
 					// row index starts at 0 here but is actually 1 in Excel
 					int rowNum = row.getRowNum() + 1;
 					// read catalogoId from first column "sheet column index
 					// starts from 0"
 					String id = readStringCellPoint(row, 1);
 
 					if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 							|| id.equalsIgnoreCase("ID")) {
 						Debug.logWarning(
 								"Row number "
 										+ rowNum
 										+ " not imported from Catalogos tab: invalid ID value ["
 										+ id + "].", MODULE);
 						continue;
 					}
 
 					String numConsecutivo = ledger_repo
 							.getNextSeqId("DataImportTag");
 					logger.debug("Numero Consecutivo tag " + numConsecutivo);
 					DataImportTag tag = new DataImportTag();
 					tag.setType(this.readStringCell(row, 0));
 					tag.setSequenceNum(readStringCellPoint(row, 1));
 					tag.setId(numConsecutivo);
 					tag.setName(this.readStringCell(row, 2));
 					tag.setDescription(this.readStringCell(row, 3));
 					tag.setParentId(this.readStringCellPoint(row, 4));
 					tag.setNivel(this.readStringCell(row, 5));
 					tag.setFechaInicio(getFecha(row.getCell(6)));
 					tag.setFechaFin(getFecha(row.getCell(7)));
 					tag.setNode(this.readStringCell(row, 8));
 					tags.add(tag);
 
 				}
 			}
 
 			return tags;
 		} catch (Exception e) {
 			logger.debug("Error: " + e);
 			return null;
 		}
 	}
 
 	private Date getFecha(HSSFCell celda) {
 
 		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd-MM-yyyy");
 		Date fecha = null;
 
 		try {
 			switch (celda.getCellType()) {
 			case HSSFCell.CELL_TYPE_BLANK:
 				Calendar c1 = GregorianCalendar.getInstance();
 				fecha = new java.sql.Date(c1.getTime().getTime());
 				break;
 
 			default:
 
 				String cadFecha = celda.toString().trim();
 				fecha = new java.sql.Date(formatoDelTexto.parse(cadFecha)
 						.getTime());
 				break;
 			}
 		} catch (Exception e) {
 			logger.debug("No se pudo hacer el parser de la fecha: " + e);
 		}
 
 		return fecha;
 
 	}
 
 	protected Collection<? extends EntityInterface> createDataImportParty(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportParty> listparty = FastList.newInstance();
 		// List<DataImportCustomer> customers = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read catalogoId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 2);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("catalogoId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Catalogos tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportParty party = new DataImportParty();
 				int rowCount = 1;
 				party.setGroupName(this.readStringCell(row, 0));
 				party.setGroupNameLocal(this.readStringCell(row, rowCount++));
 				party.setExternalId(readStringCellPoint(row, rowCount++));
 				party.setParentExternalId(this.readStringCellPoint(row,
 						rowCount++));
 				party.setNivel(this.readStringCell(row, rowCount++));
 				party.setRol(this.readStringCell(row, rowCount++));
 				party.setRfc(this.readStringCell(row, rowCount++));
 				party.setMoneda(this.readStringCell(row, rowCount++));
 				party.setNode(this.readStringCell(row, rowCount++));
 				listparty.add(party);
 			}
 		}
 
 		return listparty;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportProject.
 	 * Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportProject entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportProject(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportProject> projects = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read Tipo from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("Project Name")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Project tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportProject project = new DataImportProject();
 				project.setWorkEffortName(id);
 				int rowCount = 1; // keep track of the row
 				project.setWorkEffortTypeId(this
 						.readStringCell(row, rowCount++));
 				project.setDescription(this.readStringCell(row, rowCount++));
 				project.setGroupName(this.readStringCell(row, rowCount++));
 				project.setWorkEffortParentId(this.readStringCell(row,
 						rowCount++));
 				project.setNivelId(this.readStringCell(row, rowCount++));
 				project.setExternalId(this.readStringCell(row, rowCount++));
 				project.setNode(this.readStringCell(row, rowCount++));
 				
 				projects.add(project);
 			}
 		}
 
 		return projects;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportGeo. Author:
 	 * Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportGeo entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportGeo(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportGeo> geos = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read geoId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("geoId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Catalogos tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportGeo geo = new DataImportGeo();
 				geo.setGeoId(id);
 				int rowCount = 1; // keep track of the row
 				geo.setGeoTypeId(this.readStringCell(row, rowCount++));
 				geo.setGeoName(this.readStringCell(row, rowCount++));
 				geo.setGeoCode(this.readStringCell(row, rowCount++));
 				geo.setAbbreviation(this.readStringCell(row, rowCount++));
 				geo.setNode(this.readStringCell(row, rowCount++));
 				geos.add(geo);
 			}
 		}
 
 		return geos;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into DataImportCategory.
 	 * Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportCategory entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportCategory(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportCategory> categories = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read productCategoryId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("productCategoryId")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Catalogos tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportCategory category = new DataImportCategory();
 				category.setProductCategoryId(id);
 				int rowCount = 1; // keep track of the row
 				category.setProductCategoryTypeId(this.readStringCell(row,
 						rowCount++));
 				category.setCode(this.readStringCell(row, rowCount++));
 				category.setCategoryName(this.readStringCell(row, rowCount++));
 				category.setPrimaryParentCategoryId(this.readStringCell(row,
 						rowCount++));
 				category.setNode(this.readStringCell(row, rowCount++));
 				categories.add(category);
 			}
 		}
 
 		return categories;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportPresupuestoIngreso. Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportIngresoDiario entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportIngresoDiario(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportIngresoDiario> ingresos = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				String tipo = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(tipo) || tipo.indexOf(" ") > -1
 						|| tipo.equalsIgnoreCase("Tipo Documento")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Ingreso Diario tab: invalid ID value ["
 									+ tipo + "].", MODULE);
 					continue;
 				}
 
 				DataImportIngresoDiario ingreso = new DataImportIngresoDiario();
 				ingreso.setIdTipoDoc(tipo);
 				int rowCount = 1; // keep track of the row
 				ingreso.setFechaRegistro(getFecha(row.getCell(rowCount++)));
 				ingreso.setFechaContable(getFecha(row.getCell(rowCount++)));
 				ingreso.setMonto(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setOrganizationPartyId(this.readStringCell(row,
 						rowCount++));
 				ingreso.setRefDoc(this.readStringCell(row, rowCount++));
 				ingreso.setSecuencia(this.readStringCell(row, rowCount++));
 				ingreso.setUsuario(this.readStringCell(row, rowCount++));
 				Debug.log("Usuario " + ingreso.getUsuario());
 				ingreso.setLote(this.readStringCell(row, rowCount++));
 				ingreso.setIdTipoCatalogo(this.readStringCell(row, rowCount++));
 				ingreso.setIdPago(this.readStringCell(row, rowCount++));
 				ingreso.setIdProducto(this.readStringCell(row, rowCount++));
 				ingreso.setCiclo(this.readStringCell(row, rowCount++));
 				ingreso.setUr(this.readStringCell(row, rowCount++));
 				ingreso.setUo(this.readStringCell(row, rowCount++));
 				ingreso.setUe(this.readStringCell(row, rowCount++));
 				ingreso.setRub(this.readStringCell(row, rowCount++));
 				ingreso.setTip(this.readStringCell(row, rowCount++));
 				ingreso.setCla(this.readStringCell(row, rowCount++));
 				ingreso.setCon(this.readStringCell(row, rowCount++));
 				ingreso.setN5(this.readStringCell(row, rowCount++));
 				ingreso.setF(this.readStringCell(row, rowCount++));
 				ingreso.setSf(this.readStringCell(row, rowCount++));
 				ingreso.setSfe(this.readStringCell(row, rowCount++));
 				ingreso.setEf(this.readStringCell(row, rowCount++));
 				ingreso.setReg(this.readStringCell(row, rowCount++));
 				ingreso.setMun(this.readStringCell(row, rowCount++));
 				ingreso.setLoc(this.readStringCell(row, rowCount++));
 				ingreso.setClavePres(this.readStringCell(row, rowCount++));
 				ingresos.add(ingreso);
 			}
 		}
 
 		return ingresos;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportPresupuestoEgreso. Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportEgresoDiario entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportEgresoDiario(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportEgresoDiario> egresos = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				String tipo = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(tipo) || tipo.indexOf(" ") > -1
 						|| tipo.equalsIgnoreCase("Tipo Documento")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Ingreso Diario tab: invalid ID value ["
 									+ tipo + "].", MODULE);
 					continue;
 				}
 
 				DataImportEgresoDiario egreso = new DataImportEgresoDiario();
 				egreso.setIdTipoDoc(tipo);
 				int rowCount = 1; // keep track of the row
 				egreso.setFechaRegistro(getFecha(row.getCell(rowCount++)));
 				egreso.setFechaContable(getFecha(row.getCell(rowCount++)));
 				egreso.setMonto(this.readBigDecimalCell(row, rowCount++));
 				egreso.setOrganizationPartyId(this.readStringCell(row,
 						rowCount++));
 				egreso.setRefDoc(this.readStringCell(row, rowCount++));
 				egreso.setSecuencia(this.readStringCell(row, rowCount++));
 				egreso.setUsuario(this.readStringCell(row, rowCount++));
 				egreso.setLote(this.readStringCell(row, rowCount++));
 				egreso.setIdTipoCatalogo(this.readStringCell(row, rowCount++));
 				egreso.setIdPago(this.readStringCell(row, rowCount++));
 				egreso.setIdProducto(this.readStringCell(row, rowCount++));
 				egreso.setCiclo(this.readStringCell(row, rowCount++));
 				egreso.setUr(this.readStringCell(row, rowCount++));
 				egreso.setUo(this.readStringCell(row, rowCount++));
 				egreso.setUe(this.readStringCell(row, rowCount++));
 				egreso.setFin(this.readStringCell(row, rowCount++));
 				egreso.setFun(this.readStringCell(row, rowCount++));
 				egreso.setSubf(this.readStringCell(row, rowCount++));
 				egreso.setEje(this.readStringCell(row, rowCount++));
 				egreso.setPp(this.readStringCell(row, rowCount++));
 				egreso.setSpp(this.readStringCell(row, rowCount++));
 				egreso.setAct(this.readStringCell(row, rowCount++));
 				egreso.setTg(this.readStringCell(row, rowCount++));
 				egreso.setCap(this.readStringCell(row, rowCount++));
 				egreso.setCon(this.readStringCell(row, rowCount++));
 				egreso.setPg(this.readStringCell(row, rowCount++));
 				egreso.setPe(this.readStringCell(row, rowCount++));
 				egreso.setF(this.readStringCell(row, rowCount++));
 				egreso.setSf(this.readStringCell(row, rowCount++));
 				egreso.setSfe(this.readStringCell(row, rowCount++));
 				egreso.setEf(this.readStringCell(row, rowCount++));
 				egreso.setReg(this.readStringCell(row, rowCount++));
 				egreso.setMun(this.readStringCell(row, rowCount++));
 				egreso.setLoc(this.readStringCell(row, rowCount++));
 				egreso.setSec(this.readStringCell(row, rowCount++));
 				egreso.setSubsec(this.readStringCell(row, rowCount++));
 				egreso.setArea(this.readStringCell(row, rowCount++));
 				egreso.setClavePres(this.readStringCell(row, rowCount++));
 				egresos.add(egreso);
 			}
 		}
 
 		return egresos;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportPresupuestoIngreso. Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportOperacionDiaria entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportOperacionDiaria(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportOperacionDiaria> operacionesDiarias = FastList
 				.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				String tipo = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(tipo) || tipo.indexOf(" ") > -1
 						|| tipo.equalsIgnoreCase("Tipo Documento")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Ingreso Diario tab: invalid ID value ["
 									+ tipo + "].", MODULE);
 					continue;
 				}
 
 				DataImportOperacionDiaria operacionDiaria = new DataImportOperacionDiaria();
 				operacionDiaria.setIdTipoDoc(tipo);
 				int rowCount = 1; // keep track of the row
 				operacionDiaria.setFechaRegistro(getFecha(row
 						.getCell(rowCount++)));
 				operacionDiaria.setFechaContable(getFecha(row
 						.getCell(rowCount++)));
 				operacionDiaria.setMonto(this.readBigDecimalCell(row,
 						rowCount++));
 				operacionDiaria.setOrganizationPartyId(this.readStringCell(row,
 						rowCount++));
 				operacionDiaria.setOrganizacionEjecutora(this.readStringCell(
 						row, rowCount++));
 				operacionDiaria.setRefDoc(this.readStringCell(row, rowCount++));
 				operacionDiaria.setSecuencia(this.readStringCell(row,
 						rowCount++));
 				operacionDiaria
 						.setUsuario(this.readStringCell(row, rowCount++));
 				operacionDiaria.setLote(this.readStringCell(row, rowCount++));
 				operacionDiaria.setConcepto(this
 						.readStringCell(row, rowCount++));
 				operacionDiaria.setSubconcepto(this.readStringCell(row,
 						rowCount++));
 				operacionDiaria.setIdTipoCatalogoC(this.readStringCell(row,
 						rowCount++));
 				operacionDiaria.setIdC(this.readStringCell(row, rowCount++));
 				operacionDiaria.setIdTipoCatalogoD(this.readStringCell(row,
 						rowCount++));
 				operacionDiaria.setIdD(this.readStringCell(row, rowCount++));
 				operacionesDiarias.add(operacionDiaria);
 			}
 		}
 
 		return operacionesDiarias;
 	}
 
 	protected Collection<? extends EntityInterface> createDataImportContableGuide(
 			HSSFSheet sheet) throws RepositoryException {
 		List<DataImportContableGuide> glContableGuide = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read glAccountrId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("id")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from GL Accounts tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataImportContableGuide glContableGuides = new DataImportContableGuide();
 				glContableGuides.setC_guide_id(id);
 				glContableGuides.setId_concepto(this.readStringCell(row, 1));
 				glContableGuides.setId_subconcepto(this.readStringCell(row, 2));
 				glContableGuides.setRp_cargo(this.readStringCell(row, 3));
 				glContableGuides.setRp_abono(this.readStringCell(row, 4));
 				glContableGuides.setRc_cargo(this.readStringCell(row, 5));
 				glContableGuides.setRc_abono(this.readStringCell(row, 6));
 				glContableGuide.add(glContableGuides);
 
 			}
 		}
 
 		return glContableGuide;
 	}
 
 	protected Collection<? extends EntityInterface> createDataImportConceptos(
 			HSSFSheet sheet) throws RepositoryException {
 		List<DataimportCatalogoConceptos> glConcepto = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read glAccountrId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("id")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from GL Accounts tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataimportCatalogoConceptos glConceptos = new DataimportCatalogoConceptos();
 				glConceptos.setId_concepto(id);
 				glConceptos.setDesc_concept(this.readStringCell(row, 1));
 				glConcepto.add(glConceptos);
 
 			}
 		}
 
 		return glConcepto;
 	}
 
 	protected Collection<? extends EntityInterface> createDataImportSubConceptos(
 			HSSFSheet sheet) throws RepositoryException {
 		List<DataimportCatalogoSubconceptos> glsubconcepto = FastList
 				.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read glAccountrId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("id")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from GL Accounts tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				DataimportCatalogoSubconceptos glsubconceptos = new DataimportCatalogoSubconceptos();
 				glsubconceptos.setId_subconcepto(id);
 				glsubconceptos.setDesc_subconcept(this.readStringCell(row, 1));
 				glsubconcepto.add(glsubconceptos);
 
 			}
 		}
 
 		return glsubconcepto;
 	}
 
 	protected Collection<? extends EntityInterface> createDataImportMatrizConversionEngresos(
 			HSSFSheet sheet) throws RepositoryException {
 		Debug.logInfo("OMAR - Entra a createDataImportEgresos", MODULE);
 		List<DataImportMatrizEgr> matrizConversionEgr = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			Debug.logInfo("OMAR - Entra a for Egresos", MODULE);
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				Debug.logInfo("OMAR - No esta vacia la fila", MODULE);
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read idRegistroEgr from first column "sheet column index
 				// starts from 0"
 				String id = readStringCellPoint(row, 0);
 
 				Debug.logInfo("OMAR - Id: " + id, MODULE);
 				// Debug.logInfo("OMAR - IdIndex: " + id.indexOf(" "), MODULE);
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("idRegistroEgr")) {
 					Debug.logInfo(
 							"Row number "
 									+ rowNum
 									+ " not imported from DataImport Matriz Egresos tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				Debug.logInfo("OMAR - Va a hacer la intancia", MODULE);
 				DataImportMatrizEgr matrizEgresos = new DataImportMatrizEgr();
 				matrizEgresos.setIdRegistroEgr(id);
 				matrizEgresos.setMatrizId(this.readStringCell(row, 1));
 				matrizEgresos.setCog(this.readStringCellPoint(row, 2));
 				matrizEgresos.setNombreCog(this.readStringCell(row, 3));
 				matrizEgresos.setTipoGasto(this.readStringCellPoint(row, 4));
 				matrizEgresos.setCaracteristicas(this.readStringCell(row, 5));
 				matrizEgresos.setMedioPago(this.readStringCell(row, 6));
 				matrizEgresos.setCargo(this.readStringCell(row, 7));
 				matrizEgresos.setCuentaCargo(this.readStringCell(row, 8));
 				matrizEgresos.setAbono(this.readStringCell(row, 9));
 				matrizEgresos.setCuentaAbono(this.readStringCell(row, 10));
 				matrizEgresos.setImportStatusId("DATAIMP_IMPORTED");
 				matrizConversionEgr.add(matrizEgresos);
 
 				// DataImportMatrizEgr me= new DataImportMatrizEgr();
 
 			}
 		}
 
 		return matrizConversionEgr;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportMatrizConversionIngresos.
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportMatrizConversionIngresos
 	 *         entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportMatrizConversionIngresos(
 			HSSFSheet sheet) throws RepositoryException {
 		Debug.logInfo("OMAR - Entra a createDataImportIngresos", MODULE);
 		List<DataImportMatrizIng> matrizConversionIng = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			Debug.logInfo("OMAR - Entra a for Ingresos", MODULE);
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				Debug.logInfo("OMAR - No esta vacia la fila", MODULE);
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				// read glAccountrId from first column "sheet column index
 				// starts from 0"
 				String id = readStringCellPoint(row, 0);
 
 				Debug.logInfo("OMAR - Id: " + id, MODULE);
 				// Debug.logInfo("OMAR - IdIndex: " + id.indexOf(" "), MODULE);
 				if (UtilValidate.isEmpty(id) || id.indexOf(" ") > -1
 						|| id.equalsIgnoreCase("idRegistroIng")) {
 					Debug.logInfo(
 							"Row number "
 									+ rowNum
 									+ " not imported from DataImport Matriz Ingresos tab: invalid ID value ["
 									+ id + "].", MODULE);
 					continue;
 				}
 
 				Debug.logInfo("OMAR - Va a hacer la intancia", MODULE);
 				DataImportMatrizIng matrizIngresos = new DataImportMatrizIng();
 				matrizIngresos.setIdRegistroIng(id);
 				matrizIngresos.setMatrizId(this.readStringCell(row, 1));
 				matrizIngresos.setCri(this.readStringCellPoint(row, 2));
 				matrizIngresos.setNombreCri(this.readStringCell(row, 3));
 				matrizIngresos.setCaracteristicas(this.readStringCell(row, 4));
 				matrizIngresos.setMedioPago(this.readStringCell(row, 5));
 				matrizIngresos.setCargo(this.readStringCell(row, 6));
 				matrizIngresos.setCuentaCargo(this.readStringCell(row, 7));
 				matrizIngresos.setAbono(this.readStringCell(row, 8));
 				matrizIngresos.setCuentaAbono(this.readStringCell(row, 9));
 				matrizIngresos.setImportStatusId("DATAIMP_IMPORTED");
 				matrizConversionIng.add(matrizIngresos);
 
 				// DataImportMatrizEgr me= new DataImportMatrizEgr();
 
 			}
 		}
 
 		return matrizConversionIng;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportPresupuestoIngreso. Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportPresupuestoIngreso
 	 *         entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportPresupuestoIngreso(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportPresupuestoIngreso> ingresos = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				String ciclo = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(ciclo) || ciclo.indexOf(" ") > -1
 						|| ciclo.equalsIgnoreCase("CICLO")
 						|| ciclo.equalsIgnoreCase("AO")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Presupuesto Ingreso tab: invalid ID value ["
 									+ ciclo + "].", MODULE);
 					continue;
 				}
 
 				DataImportPresupuestoIngreso ingreso = new DataImportPresupuestoIngreso();
 				ingreso.setCiclo(ciclo);
 				int rowCount = 1; // keep track of the row
 				ingreso.setUr(this.readStringCell(row, rowCount++));
 				ingreso.setUo(this.readStringCell(row, rowCount++));
 				ingreso.setUe(this.readStringCell(row, rowCount++));
 				ingreso.setRub(this.readStringCell(row, rowCount++));
 				ingreso.setTip(this.readStringCell(row, rowCount++));
 				ingreso.setCla(this.readStringCell(row, rowCount++));
 				ingreso.setCon(this.readStringCell(row, rowCount++));
 				ingreso.setN5(this.readStringCell(row, rowCount++));
 				ingreso.setF(this.readStringCell(row, rowCount++));
 				ingreso.setSf(this.readStringCell(row, rowCount++));
 				ingreso.setSfe(this.readStringCell(row, rowCount++));
 				ingreso.setEf(this.readStringCell(row, rowCount++));
 				ingreso.setReg(this.readStringCell(row, rowCount++));
 				ingreso.setMun(this.readStringCell(row, rowCount++));
 				ingreso.setLoc(this.readStringCell(row, rowCount++));
 				ingreso.setClavePres(this.readStringCell(row, rowCount++));
 				ingreso.setUsuario(this.readStringCell(row, rowCount++));
 				ingreso.setEnero(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setFebrero(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setMarzo(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setAbril(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setMayo(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setJunio(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setJulio(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setAgosto(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setSeptiembre(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setOctubre(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setNoviembre(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setDiciembre(this.readBigDecimalCell(row, rowCount++));
 				ingreso.setAgrupador(this.readStringCell(row, rowCount++));
 				ingresos.add(ingreso);
 			}
 		}
 
 		return ingresos;
 	}
 
 	/**
 	 * Take each row of an Excel sheet and put it into
 	 * DataImportPresupuestoEgreso. Author: Jesus Rodrigo Ruiz Merlin
 	 * 
 	 * @param sheet
 	 *            the Excel sheet
 	 * @return a <code>Collection</code> of DataImportPresupuestoEgreso entities
 	 * @throws RepositoryException
 	 *             if an error occurs
 	 */
 	protected Collection<? extends EntityInterface> createDataImportPresupuestoEgreso(
 			HSSFSheet sheet) throws RepositoryException {
 
 		List<DataImportPresupuestoEgreso> egresos = FastList.newInstance();
 		int sheetLastRowNumber = sheet.getLastRowNum();
 		for (int j = 1; j <= sheetLastRowNumber; j++) {
 			HSSFRow row = sheet.getRow(j);
 			if (isNotEmpty(row)) {
 				// row index starts at 0 here but is actually 1 in Excel
 				int rowNum = row.getRowNum() + 1;
 				String ciclo = readStringCell(row, 0);
 
 				if (UtilValidate.isEmpty(ciclo) || ciclo.indexOf(" ") > -1
 						|| ciclo.equalsIgnoreCase("CICLO")
 						|| ciclo.equalsIgnoreCase("AO")) {
 					Debug.logWarning(
 							"Row number "
 									+ rowNum
 									+ " not imported from Presupuesto Egreso tab: invalid ID value ["
 									+ ciclo + "].", MODULE);
 					continue;
 				}
 
 				DataImportPresupuestoEgreso egreso = new DataImportPresupuestoEgreso();
 				egreso.setCiclo(ciclo);
 				int rowCount = 1; // keep track of the row
 				egreso.setUr(this.readStringCell(row, rowCount++));
 				egreso.setUo(this.readStringCell(row, rowCount++));
 				egreso.setUe(this.readStringCell(row, rowCount++));
 				egreso.setFin(this.readStringCell(row, rowCount++));
 				egreso.setFun(this.readStringCell(row, rowCount++));
 				egreso.setSubf(this.readStringCell(row, rowCount++));
 				egreso.setEje(this.readStringCell(row, rowCount++));
 				egreso.setPp(this.readStringCell(row, rowCount++));
 				egreso.setSpp(this.readStringCell(row, rowCount++));
 				egreso.setAct(this.readStringCell(row, rowCount++));
 				egreso.setTg(this.readStringCell(row, rowCount++));
 				egreso.setCap(this.readStringCell(row, rowCount++));
 				egreso.setCon(this.readStringCell(row, rowCount++));
 				egreso.setPg(this.readStringCell(row, rowCount++));
 				egreso.setPe(this.readStringCell(row, rowCount++));
 				egreso.setF(this.readStringCell(row, rowCount++));
 				egreso.setSf(this.readStringCell(row, rowCount++));
 				egreso.setSfe(this.readStringCell(row, rowCount++));
 				egreso.setEf(this.readStringCell(row, rowCount++));
 				egreso.setReg(this.readStringCell(row, rowCount++));
 				egreso.setMun(this.readStringCell(row, rowCount++));
 				egreso.setLoc(this.readStringCell(row, rowCount++));
 				egreso.setSec(this.readStringCell(row, rowCount++));
 				egreso.setSubsec(this.readStringCell(row, rowCount++));
 				egreso.setArea(this.readStringCell(row, rowCount++));
 				egreso.setClavePres(this.readStringCell(row, rowCount++));
 				egreso.setUsuario(this.readStringCell(row, rowCount++));
 				egreso.setEnero(this.readBigDecimalCell(row, rowCount++));
 				egreso.setFebrero(this.readBigDecimalCell(row, rowCount++));
 				egreso.setMarzo(this.readBigDecimalCell(row, rowCount++));
 				egreso.setAbril(this.readBigDecimalCell(row, rowCount++));
 				egreso.setMayo(this.readBigDecimalCell(row, rowCount++));
 				egreso.setJunio(this.readBigDecimalCell(row, rowCount++));
 				egreso.setJulio(this.readBigDecimalCell(row, rowCount++));
 				egreso.setAgosto(this.readBigDecimalCell(row, rowCount++));
 				egreso.setSeptiembre(this.readBigDecimalCell(row, rowCount++));
 				egreso.setOctubre(this.readBigDecimalCell(row, rowCount++));
 				egreso.setNoviembre(this.readBigDecimalCell(row, rowCount++));
 				egreso.setDiciembre(this.readBigDecimalCell(row, rowCount++));
 				egreso.setAgrupador(this.readStringCell(row, rowCount++));
 				egresos.add(egreso);
 			}
 		}
 
 		return egresos;
 	}
 
 	/**
 	 * Uploads an Excel file in the correct directory.
 	 * 
 	 * @exception ServiceException
 	 *                if an error occurs
 	 * @throws InfrastructureException 
 	 */
 	public void parseFileForDataImport() throws ServiceException, InfrastructureException {
 
 		// Get the uploaded file
 		File file = getUploadedExcelFile(getUploadedFileName());
 
 		// set it up as an Excel workbook
 		POIFSFileSystem fs = null;
 		HSSFWorkbook wb = null;
 		try {
 			// this will auto close the FileInputStream when the constructor
 			// completes
 			fs = new POIFSFileSystem(new FileInputStream(file));
 			wb = new HSSFWorkbook(fs);
 		} catch (IOException e) {
 			throw new ServiceException(
 					"Unable to read or create workbook from file ["
 							+ getUploadedFileName() + "] " + e.getMessage());
 		}
 
 		// loop through the tabs and import them one by one
 		try {
 
 			// a collection of all the records from all the excel spreadsheet
 			// tabs
 			FastList<EntityInterface> entitiesToCreate = FastList.newInstance();
 
 			for (String excelTab : EXCEL_TABS) {
 				HSSFSheet sheet = wb.getSheet(excelTab);
 				if (sheet == null) {
 					Debug.logWarning("Did not find a sheet named " + excelTab
 							+ " in " + file.getName()
 							+ ".  Will not be importing anything.", MODULE);
 				} else {
 					if (EXCEL_PRODUCT_TAB.equals(excelTab)) {
 						deleteEntities("DataImportProduct");
 						entitiesToCreate
 								.addAll(createDataImportProducts(sheet));
 					} else if (EXCEL_SUPPLIERS_TAB.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportSuppliers(sheet));
 					} else if (EXCEL_CUSTOMERS_TAB.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportCustomers(sheet));
 					} else if (EXCEL_INVENTORY_TAB.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportInventory(sheet));
 					} else if (EXCEL_GL_ACCOUNTS_TAB.equals(excelTab)) {
 						deleteEntities("DataImportGlAccount");
 						entitiesToCreate
 								.addAll(createDataImportGlAccounts(sheet));
 					} else if (EXCEL_TAG_TAB.equals(excelTab)) {
 						deleteEntities("DataImportTag");
 						entitiesToCreate.addAll(createDataImportTag(sheet));
 					} else if (EXCEL_PARTY_TAB.equals(excelTab)) {
 						deleteEntities("DataImportParty");
 						entitiesToCreate.addAll(createDataImportParty(sheet));
 					} else if (EXCEL_PROJECT_TAB.equals(excelTab)) {
 						deleteEntities("DataImportProject");
 						entitiesToCreate.addAll(createDataImportProject(sheet));
 					} else if (EXCEL_GEO_TAB.equals(excelTab)) {
						deleteEntities("DataImportGeo");
 						entitiesToCreate.addAll(createDataImportGeo(sheet));
 					} else if (EXCEL_CATEGORY_TAB.equals(excelTab)) {
 						deleteEntities("DataImportCategory");
 						entitiesToCreate
 								.addAll(createDataImportCategory(sheet));
 					} else if (EXCEL_PRESUPUESTO_INGRESO_TAB.equals(excelTab)) {
						deleteEntities("DataImportPresupuestoIngreso");
 						entitiesToCreate
 								.addAll(createDataImportPresupuestoIngreso(sheet));
 					} else if (EXCEL_PRESUPUESTO_EGRESO_TAB.equals(excelTab)) {
						deleteEntities("DataImportPresupuestoEgreso");
 						entitiesToCreate
 								.addAll(createDataImportPresupuestoEgreso(sheet));
 					} else if (EXCEL_CGUIDE_TAB.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportContableGuide(sheet));
 					} else if (EXCEL_MATRIZ_CONVERSION_EGR_TAB.equals(excelTab)) {
 						Debug.logInfo("OMAR - Entra a tab Matriz Egresos",
 								MODULE);
 						entitiesToCreate
 								.addAll(createDataImportMatrizConversionEngresos(sheet));
 					} else if (EXCEL_MATRIZ_CONVERSION_ING_TAB.equals(excelTab)) {
 						Debug.logInfo("OMAR - Entra a tab Matriz Ingresos",
 								MODULE);
 						entitiesToCreate
 								.addAll(createDataImportMatrizConversionIngresos(sheet));
 						// etc ...
 					} else if (EXCEL_EGRESO_DIARIO_TAB.equals(excelTab)) {
						deleteEntities("DataImportEgresoDiario");
 						entitiesToCreate
 								.addAll(createDataImportEgresoDiario(sheet));
 					} else if (EXCEL_CGUIDE_TAB_CON.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportConceptos(sheet));
 					} else if (EXCEL_CGUIDE_TAB_SCON.equals(excelTab)) {
 						entitiesToCreate
 								.addAll(createDataImportSubConceptos(sheet));
 					} else if (EXCEL_INGRESO_DIARIO_TAB.equals(excelTab)) {
						deleteEntities("DataImportIngresoDiario");
 						entitiesToCreate
 								.addAll(createDataImportIngresoDiario(sheet));
 					} else if (EXCEL_OPERACION_DIARIA_TAB.equals(excelTab)) {
						deleteEntities("DataImportOperacionDiaria");
 						entitiesToCreate
 								.addAll(createDataImportOperacionDiaria(sheet));
 					}
 
 					// etc ...
 				}
 			}
 
 			// create and store values from all the sheets in the workbook in
 			// database using the PartyRepositoryInterface
 			// note we're just using the most basic repository method, so any
 			// repository could do here
 			PartyRepositoryInterface partyRepo = this.getDomainsDirectory()
 					.getPartyDomain().getPartyRepository();
 			partyRepo.createOrUpdate(entitiesToCreate);
 
 		} catch (RepositoryException e) {
 			throw new ServiceException(e);
 		}
 
 		// remove the uploaded file now
 		if (!file.delete()) {
 			Debug.logWarning("Could not delete the file : " + file.getName(),
 					MODULE);
 		}
 	}
 	
 	/*Delete Entities 
 	 * */
 private void deleteEntities(String entity) throws InfrastructureException {
 		
 		this.session = this.getInfrastructure().getSession();		
 		Transaction tx = null;
 		
 		try {
 				
 			tx = session.beginTransaction();		
 			//Query query = session.createQuery("delete from DataImportGlAccount");
 			Query query = session.createQuery("delete from " + entity);
 			int rowCount = query.executeUpdate();
 	        logger.debug("Rows affected: " + rowCount + " ," + entity);
 			tx.commit();	
 			
 		} catch (Exception e) {
 			Debug.log("Error al borrar registros " + e); 
 			if (tx != null)  
 		        tx.rollback();  
 		}
 	}
 
 
 	public void setUploadedFileName(String uploadedFileName) {
 		this.uploadedFileName = uploadedFileName;
 	}
 
 	public String getUploadedFileName() {
 		return uploadedFileName;
 	}
 
 }
