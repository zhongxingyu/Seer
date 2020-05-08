 package org.ocha.hdx.importer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.NoResultException;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.ocha.hdx.model.validation.ValidationStatus;
 import org.ocha.hdx.persistence.entity.curateddata.Entity;
 import org.ocha.hdx.persistence.entity.curateddata.EntityType;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorImportConfig;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorValue;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.service.CuratedDataService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Importer for the WFP.
  */
 public class WfpImporter implements HDXImporter {
 
 	Logger logger = LoggerFactory.getLogger(WfpImporter.class);
 
 	private final CuratedDataService curatedDataService;
 
 	private final Source source;
 	private final EntityType country;
 	private final EntityType subnational;
 	private final IndicatorType poor;
 	private final IndicatorType borderline;
 	private final IndicatorType acceptable;
 	private final Periodicity periodicity = Periodicity.YEAR;
 
 	public WfpImporter(final CuratedDataService curatedDataService) {
 		this.curatedDataService = curatedDataService;
 
 		// Set the source as WFP
 		source = curatedDataService.getSourceByCode("WFP");
 
 		// Set the entity types
 		country = curatedDataService.getEntityTypeByCode("country");
 		subnational = curatedDataService.getEntityTypeByCode("subnational");
 
 		// Set the indicator types
 		poor = curatedDataService.getIndicatorTypeByCode("PVF040");
 		borderline = curatedDataService.getIndicatorTypeByCode("PVF050");
 		acceptable = curatedDataService.getIndicatorTypeByCode("WFP_ACCEPTABLE");
 
 	}
 
 	private static class Reporter {
 		List<String> messages = new ArrayList<>();
 
 		void addEntry(final String message) {
 			messages.add(message);
 		}
 	}
 
 	@Override
 	public PreparedData prepareDataForImport(final File file) {
 		final Reporter report = new Reporter();
 		final List<PreparedIndicator> preparedIndicators = new ArrayList<>();
 		boolean success = true;
 
 		try {
 			// Read the XLSX file
 			final InputStream inp = new FileInputStream(file);
 
 			// Get the workbook
 			final Workbook wb = WorkbookFactory.create(inp);
 
 			// Get the first sheet
 			final Sheet sheet = wb.getSheetAt(0);
 
 			// Iterate the rows (first row is headers)
 			final Iterator<Row> rowIterator = sheet.rowIterator();
 			rowIterator.next();
 
 			while (rowIterator.hasNext()) {
 				final Row row = rowIterator.next();
 				final PreparedIndicator poorIndicator = new PreparedIndicator();
 				final PreparedIndicator borderlineIndicator = new PreparedIndicator();
 				final PreparedIndicator acceptableIndicator = new PreparedIndicator();
 
 				preparedIndicators.add(poorIndicator);
 				preparedIndicators.add(borderlineIndicator);
 				// preparedIndicators.add(acceptableIndicator); --> Acceptable is not needed, cf. #240
 
 				// Entities
 				Entity countryEntity = null;
 				String countryCode = null;
 				Entity adm1Entity = null;
 				String adm1Code = null;
 				Entity adm2Entity = null;
 				String adm2Code = null;
 
 				// Cell 0 contains country code
 				final Cell countryCodeCell = row.getCell(0);
 				countryCode = countryCodeCell.getStringCellValue();
 				if (3 != countryCode.length()) {
 					final String message = String.format("Row %d contains invalid country code (expected XXX) <%s>", row.getRowNum(), countryCode);
 					logger.debug(message);
 					report.addEntry(message);
 				}
 
 				// Check the country exists
 				try {
 					countryEntity = curatedDataService.getEntityByCodeAndType(countryCode, country.getCode());
 					// If not (unlikely), create it
 				} catch (final NoResultException e) {
 					// Cell 1 contains country name
 					final Cell countryNameCell = row.getCell(1);
 					final String countryName = countryNameCell.getStringCellValue();
 					countryEntity = curatedDataService.createEntity(countryCode, countryName, country.getCode(), null);
 				}
 
 				// Cell 2 contains adm1 (maybe null)
 				final Cell adm1Cell = row.getCell(2);
 				if ((null != adm1Cell.getStringCellValue()) && !"".equals(adm1Cell.getStringCellValue())) {
 					adm1Code = countryCode + "_" + convertToAscii(adm1Cell.getStringCellValue());
 
 					// Check the adm1 exists
 					try {
 						adm1Entity = curatedDataService.getEntityByCodeAndType(adm1Code, subnational.getCode());
 					} catch (final NoResultException e) {
 						// If not, create it
 						final String adm1Name = adm1Cell.getStringCellValue();
 						adm1Entity = curatedDataService.createEntity(adm1Code, adm1Name, subnational.getCode(), countryEntity.getId());
 					}
 				}
 
 				// Cell 3 contains adm2 (maybe null)
 				final Cell adm2Cell = row.getCell(3);
 				if ((null != adm2Cell.getStringCellValue()) && !"".equals(adm2Cell.getStringCellValue())) {
 					adm2Code = countryCode + "_" + adm1Code + "_" + convertToAscii(adm2Cell.getStringCellValue());
 
 					// Check the adm2 exists
 					try {
 						adm2Entity = curatedDataService.getEntityByCodeAndType(adm2Code, subnational.getCode());
 					} catch (final NoResultException e) {
 						// If not, create it
 						final String adm2Name = adm2Cell.getStringCellValue();
 						adm2Entity = curatedDataService.createEntity(adm2Code, adm2Name, subnational.getCode(), adm1Entity.getId());
 					}
 				}
 
 				// Cell 4 contains year
 				Integer year = null;
 				final Cell yearCell = row.getCell(4);
 				final String yearAsString = "" + (Cell.CELL_TYPE_NUMERIC == yearCell.getCellType() ? yearCell.getNumericCellValue() : yearCell.getStringCellValue());
 				final String message = String.format("Row %d contains invalid year (expected yyyy or yyyy-yyyy) <%s>", row.getRowNum(), yearAsString);
 				switch (yearAsString.length()) {
 				case 6:
 					try {
 						year = Integer.valueOf(yearAsString.substring(0, 4));
 					} catch (final Exception e) {
 						logger.debug(message);
 						report.addEntry(message);
 					}
 					break;
 				case 9:
 					try {
 						year = Integer.valueOf(yearAsString.substring(0, 4));
 					} catch (final Exception e) {
 						logger.debug(message);
 						report.addEntry(message);
 					}
 					break;
 
 				default:
 					logger.debug(message);
 					report.addEntry(message);
 					break;
 				}
 
 				// Cell 5 contains "poor" data
 				Double poorValue = null;
 				final Cell poorCell = row.getCell(5);
 				final String poorAsString = "" + poorCell.getNumericCellValue();
 				try {
 					poorValue = Double.valueOf(poorAsString);
 				} catch (final Exception e) {
 					final String msg = String.format("Row %d contains invalid 'poor' data (number) <%s>", row.getRowNum(), poorAsString);
 					logger.debug(msg);
 					report.addEntry(msg);
 				}
 
 				// Cell 6 contains "borderline" data
 				Double borderlineValue = null;
 				final Cell borderlineCell = row.getCell(6);
 				final String borderlineAsString = "" + borderlineCell.getNumericCellValue();
 				try {
 					borderlineValue = Double.valueOf(borderlineAsString);
 				} catch (final Exception e) {
 					final String msg = String.format("Row %d contains invalid 'borderline' data (number) <%s>", row.getRowNum(), borderlineAsString);
 					logger.debug(msg);
 					report.addEntry(msg);
 				}
 
 				// Cell 7 contains "acceptable" data
 				Double acceptableValue = null;
 				final Cell acceptableCell = row.getCell(7);
 				final String acceptableAsString = "" + acceptableCell.getNumericCellValue();
 				try {
 					acceptableValue = Double.valueOf(acceptableAsString);
 				} catch (final Exception e) {
 					final String msg = String.format("Row %d contains invalid 'acceptable' data (number) <%s>", row.getRowNum(), acceptableAsString);
 					logger.debug(msg);
 					report.addEntry(msg);
 				}
 
 				final Calendar start = new GregorianCalendar(year, 0, 1);
 				final Calendar end = new GregorianCalendar(1 + year, 0, 1);
 
 				// Poor
 				poorIndicator.setSourceCode(source.getCode());
 				poorIndicator.setEntityCode(adm2Code == null ? (adm1Code == null ? countryCode : adm1Code) : adm2Code);
 				poorIndicator.setEntityTypeCode(((adm1Code != null) || (adm2Code != null)) ? subnational.getCode() : country.getCode());
 				poorIndicator.setStart(start.getTime());
 				poorIndicator.setEnd(end.getTime());
 				poorIndicator.setPeriodicity(periodicity);
				poorIndicator.setIndicatorImportConfig(new IndicatorImportConfig(poorAsString, ValidationStatus.SUCCESS));
 				poorIndicator.setSourceLink(source.getOrgLink()); // TODO Check
 
 				poorIndicator.setIndicatorTypeCode(poor.getCode());
 				poorIndicator.setValue(new IndicatorValue(poorValue));
 
 				// Borderline
 				borderlineIndicator.setSourceCode(source.getCode());
 				borderlineIndicator.setEntityCode(adm2Code == null ? (adm1Code == null ? countryCode : adm1Code) : adm2Code);
 				borderlineIndicator.setEntityTypeCode(((adm1Code != null) || (adm2Code != null)) ? subnational.getCode() : country.getCode());
 				borderlineIndicator.setStart(start.getTime());
 				borderlineIndicator.setEnd(end.getTime());
 				borderlineIndicator.setPeriodicity(periodicity);
				borderlineIndicator.setIndicatorImportConfig(new IndicatorImportConfig(borderlineAsString, ValidationStatus.SUCCESS));
 				borderlineIndicator.setSourceLink(source.getOrgLink()); // TODO Check
 
 				borderlineIndicator.setIndicatorTypeCode(borderline.getCode());
 				borderlineIndicator.setValue(new IndicatorValue(borderlineValue));
 
 				// Acceptable
 				acceptableIndicator.setSourceCode(source.getCode());
 				acceptableIndicator.setEntityCode(adm2Code == null ? (adm1Code == null ? countryCode : adm1Code) : adm2Code);
 				acceptableIndicator.setEntityTypeCode(((adm1Code != null) || (adm2Code != null)) ? subnational.getCode() : country.getCode());
 				acceptableIndicator.setStart(start.getTime());
 				acceptableIndicator.setEnd(end.getTime());
 				acceptableIndicator.setPeriodicity(periodicity);
				acceptableIndicator.setIndicatorImportConfig(new IndicatorImportConfig(acceptableAsString, ValidationStatus.SUCCESS));
 				acceptableIndicator.setSourceLink(source.getOrgLink()); // TODO Check
 
 				acceptableIndicator.setIndicatorTypeCode(acceptable.getCode());
 				acceptableIndicator.setValue(new IndicatorValue(acceptableValue));
 			}
 		} catch (final Exception e) {
 			final String message = String.format("Error caused by an exception <%s>", e.getMessage());
 			logger.debug(message);
 			report.addEntry(message);
 		}
 
 		success = 0 == report.messages.size();
 
 		final PreparedData result = new PreparedData(success, preparedIndicators);
 		return result;
 	}
 
 	private static String convertToAscii(final String string) {
 		final String resultString = string.replaceAll("[^a-zA-Z0-9]", "");
 		return resultString.toUpperCase();
 	}
 }
