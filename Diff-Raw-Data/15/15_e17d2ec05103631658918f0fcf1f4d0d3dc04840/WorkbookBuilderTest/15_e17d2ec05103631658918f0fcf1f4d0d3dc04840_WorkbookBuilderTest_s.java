 package oleksii.filonov.writers;
 
 import static oleksii.filonov.TestConstants.LINKED_RESULT_PATH;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import oleksii.filonov.readers.CampaignProcessor;
 import oleksii.filonov.readers.ColumnReaderHelper;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.Hyperlink;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.ListMultimap;
 
 @RunWith(MockitoJUnitRunner.class)
 public class WorkbookBuilderTest {
 
 	private static final String BODY_ID_FOUND = "bodyIdFound";
 	private static final String PATH_TO_CAMPAIGN_FILE = "pathToCampaignFile";
 	private static final int FOUND_BODY_COLUMN_INDEX = 7;
 	private static final int FIRST_LINK_COLUMN_INDEX = 8;
 	private static final int SECOND_LINK_COLUMN_INDEX = 9;
 	private static final String ANOTHER_BODY_ID_FOUND = "anotherFoundBodyId";
 	private static final int FOUND_ROW_INDEX = 7;
 	private static final int SHIFT_VALUE = 1;
 	private static final String CLIENT_SHEET_NAME = "clientSheetName";
 	private static final int BODY_ID_COLUMN_INDEX = 1;
 	private static final int VIN_LIST_1_ROW_INDEX = 1;
 	private static final int VIN_LIST_2_ROW_INDEX = 2;
 	private static final int VIN_LIST_3_ROW_INDEX = 3;
 	private static final int VIN_LIST_4_ROW_INDEX = 4;
 	private static final String LINK_1_BODY_ID_SHEET_NAME = "link1bodyIdSheetName";
 	private static final String LINK_2_BODY_ID_SHEET_NAME = "link2bodyIdSheetName";
 	private static final String LINK_3_BODY_ID_SHEET_NAME = "link3bodyIdSheetName";
 	private static final String LINK_4_BODY_ID_SHEET_NAME = "link4bodyIdSheetName";
 	private static final int EXCEL_ROW_OFFSET = 1;
 	private static final String VIN_LIST_1_DESC = "This is the 1 vin list description";
 	private static final String VIN_LIST_2_DESC = "This is the 2 vin list description";
 	private static final String VIN_LIST_ANOTHER_FOUND_BODY_DESC = "This is the 3 vin list description";
 	private static final String VIN_LIST_4_DESC = "This is the 4 vin list description";
 
 	private ColumnReaderHelper columnReaderHelper;
 	private CampaignProcessor campaignProcessor;
 
 	private WorkbookBuilder excelBuilder;
 	@Mock
 	private Workbook clientWorkbook;
 	@Mock
 	private CreationHelper creationHelper;
 	@Mock
 	private CellStyle foundCellStyle;
 	@Mock
 	private CellStyle linkCellStyle;
 	@Mock
 	private Cell foundBodyIdCell;
 	@Mock
 	private Cell notFoundBodyIdCell;
 	@Mock
 	private Row foundRow;
 	@Mock
 	private Cell firstLinkToVinCell;
 	@Mock
 	private Hyperlink foundBodyFirstHyperLink;
 	@Mock
 	private Cell secondLinkToVin;
 	@Mock
 	private Hyperlink foundBodySecondHyperLink;
 	@Mock
 	private Hyperlink anotherFoundBodyHyperLink;
 	@Mock
 	private Cell foundAnotherBodyIdCell;
 	@Mock
 	private Row anotherFoundRow;
 	@Mock
 	private Cell linkToAnotherVinCell;
 	@Mock
 	private Sheet clientSheet;
 	@Mock
 	private Row rowForHyperLink2;
 	@Mock
 	private Row rowForHyperLink4;
 	@Mock
 	private Hyperlink foundBodyFourthHyperLink;
 	@Mock
 	private Cell fourthLinkToVin;
 	@Mock
 	private Cell foundBodyIdOnVinList1Cell;
 	@Mock
 	private Cell foundBodyIdOnVinList2Cell;
 	@Mock
 	private Cell foundBodyIdOnVinList3Cell;
 	@Mock
 	private Cell foundBodyIdOnVinList4Cell;
 	@Mock
 	private Sheet link1bodyIdSheet;
 	@Mock
 	private Sheet link2bodyIdSheet;
 	@Mock
 	private Sheet link3bodyIdSheet;
 	@Mock
 	private Sheet link4bodyIdSheet;
 	@Mock
 	private Cell firstVinDescriptionCell;
 	@Mock
 	private Cell secondVinDescriptionCell;
 	@Mock
 	private Cell fourthVinDescriptionCell;
 	@Mock
 	private Cell anotherFoundBodyVinDescriptionCell;
 
 	@Before
 	public void setUp() throws IOException, InvalidFormatException {
 		excelBuilder = new WorkbookBuilder();
 		excelBuilder.setPathToCampaignFile(PATH_TO_CAMPAIGN_FILE);
 		columnReaderHelper = new ColumnReaderHelper();
 		campaignProcessor = new CampaignProcessor();
 		campaignProcessor.setColumnReaderHelper(columnReaderHelper);
 		populateMocks();
 	}
 
 	private void populateMocks() throws IOException, InvalidFormatException {
 		when(clientWorkbook.getCreationHelper()).thenReturn(creationHelper);
 		when(clientWorkbook.createCellStyle()).thenReturn(foundCellStyle).thenReturn(linkCellStyle);
 		when(clientWorkbook.getSheetAt(0)).thenReturn(clientSheet);
 		when(clientSheet.getSheetName()).thenReturn(CLIENT_SHEET_NAME);
 		excelBuilder.useWorkbook(clientWorkbook);
 		mockFoundCellWithThreeLinksBehaviour();
 		mockAnotherFoundCellBehaviour();
		when(creationHelper.createHyperlink(Hyperlink.LINK_FILE)).thenReturn(foundBodyFirstHyperLink)
 				.thenReturn(foundBodySecondHyperLink).thenReturn(foundBodyFourthHyperLink)
 				.thenReturn(anotherFoundBodyHyperLink);
 		mockFoundBodyIdCellsOnVinLists();
 		when(notFoundBodyIdCell.getStringCellValue()).thenReturn("bodyIdNotFound");
 	}
 
 	private void mockFoundBodyIdCellsOnVinLists() {
 		mockBodyIdCell(foundBodyIdOnVinList1Cell, VIN_LIST_1_ROW_INDEX, link1bodyIdSheet);
 		when(link1bodyIdSheet.getSheetName()).thenReturn(LINK_1_BODY_ID_SHEET_NAME);
 		mockBodyIdCell(foundBodyIdOnVinList2Cell, VIN_LIST_2_ROW_INDEX, link2bodyIdSheet);
 		when(link2bodyIdSheet.getSheetName()).thenReturn(LINK_2_BODY_ID_SHEET_NAME);
 		mockBodyIdCell(foundBodyIdOnVinList3Cell, VIN_LIST_3_ROW_INDEX, link3bodyIdSheet);
 		when(link3bodyIdSheet.getSheetName()).thenReturn(LINK_3_BODY_ID_SHEET_NAME);
 		mockBodyIdCell(foundBodyIdOnVinList4Cell, VIN_LIST_4_ROW_INDEX, link4bodyIdSheet);
 		when(link4bodyIdSheet.getSheetName()).thenReturn(LINK_4_BODY_ID_SHEET_NAME);
 	}
 
 	private void mockBodyIdCell(final Cell bodyIdCell, final int bodyIdRowIndex, final Sheet bodyIdSheet) {
 		when(bodyIdCell.getSheet()).thenReturn(bodyIdSheet);
 		when(bodyIdCell.getColumnIndex()).thenReturn(BODY_ID_COLUMN_INDEX);
 		when(bodyIdCell.getRowIndex()).thenReturn(bodyIdRowIndex);
 	}
 
 	private void mockFoundCellWithThreeLinksBehaviour() {
 		when(foundBodyIdCell.getStringCellValue()).thenReturn(BODY_ID_FOUND);
 		when(foundBodyIdCell.getRow()).thenReturn(foundRow);
 		when(foundBodyIdCell.getColumnIndex()).thenReturn(FOUND_BODY_COLUMN_INDEX);
 		when(foundRow.getSheet()).thenReturn(clientSheet);
 		when(foundRow.getRowNum()).thenReturn(FOUND_ROW_INDEX);
 		when(foundRow.createCell(FIRST_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(firstLinkToVinCell);
 		when(foundRow.createCell(SECOND_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(firstVinDescriptionCell);
 		when(clientSheet.createRow(FOUND_ROW_INDEX + 1)).thenReturn(rowForHyperLink2);
 		when(clientSheet.createRow(FOUND_ROW_INDEX + 2)).thenReturn(rowForHyperLink4);
 		when(rowForHyperLink2.createCell(FIRST_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(secondLinkToVin);
 		when(rowForHyperLink2.createCell(SECOND_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(
 				secondVinDescriptionCell);
 		when(rowForHyperLink4.createCell(FIRST_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(fourthLinkToVin);
 		when(rowForHyperLink4.createCell(SECOND_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(
 				fourthVinDescriptionCell);
 	}
 
 	private void mockAnotherFoundCellBehaviour() {
 		when(foundAnotherBodyIdCell.getStringCellValue()).thenReturn(ANOTHER_BODY_ID_FOUND);
 		when(foundAnotherBodyIdCell.getRow()).thenReturn(anotherFoundRow);
 		when(foundAnotherBodyIdCell.getColumnIndex()).thenReturn(FOUND_BODY_COLUMN_INDEX);
 		when(anotherFoundRow.getSheet()).thenReturn(clientSheet);
 		when(anotherFoundRow.createCell(FIRST_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(
 				linkToAnotherVinCell);
 		when(anotherFoundRow.createCell(SECOND_LINK_COLUMN_INDEX, Cell.CELL_TYPE_STRING)).thenReturn(
 				anotherFoundBodyVinDescriptionCell);
 	}
 
 	@Test
 	public void formResultFileWithLinks() throws IOException, InvalidFormatException {
 		final Cell[] bodyIds = new Cell[] { foundBodyIdCell, notFoundBodyIdCell, foundAnotherBodyIdCell };
 		final ListMultimap<String, Cell> bodyIdLinks = LinkedListMultimap.create();
 		bodyIdLinks.put(BODY_ID_FOUND, foundBodyIdOnVinList1Cell);
 		bodyIdLinks.put(BODY_ID_FOUND, foundBodyIdOnVinList2Cell);
 		bodyIdLinks.put(ANOTHER_BODY_ID_FOUND, foundBodyIdOnVinList3Cell);
 		bodyIdLinks.put(BODY_ID_FOUND, foundBodyIdOnVinList4Cell);
 		final Map<String, String> bodyIdDescriptionMap = new HashMap<>();
 		bodyIdDescriptionMap.put(LINK_1_BODY_ID_SHEET_NAME, VIN_LIST_1_DESC);
 		bodyIdDescriptionMap.put(LINK_2_BODY_ID_SHEET_NAME, VIN_LIST_2_DESC);
 		bodyIdDescriptionMap.put(LINK_3_BODY_ID_SHEET_NAME, VIN_LIST_ANOTHER_FOUND_BODY_DESC);
 		bodyIdDescriptionMap.put(LINK_4_BODY_ID_SHEET_NAME, VIN_LIST_4_DESC);
 		excelBuilder.setVinListDescriptionMap(bodyIdDescriptionMap);
 		excelBuilder.assignTasks(bodyIds, bodyIdLinks);
 		excelBuilder.saveToFile(LINKED_RESULT_PATH.toFile());
 		verifyCreatedDocument();
 	}
 
 	private void verifyCreatedDocument() throws IOException {
 		verify(firstLinkToVinCell).setCellValue(LINK_1_BODY_ID_SHEET_NAME);
 		verify(foundBodyFirstHyperLink).setAddress(
 				PATH_TO_CAMPAIGN_FILE + "#'" + LINK_1_BODY_ID_SHEET_NAME + "'!B"
 						+ (VIN_LIST_1_ROW_INDEX + EXCEL_ROW_OFFSET));
 		verify(firstVinDescriptionCell).setCellValue(VIN_LIST_1_DESC);
 		verify(clientSheet).shiftRows(eq(FOUND_ROW_INDEX + 1), anyInt(), eq(SHIFT_VALUE));
 		verify(secondLinkToVin).setCellValue(LINK_2_BODY_ID_SHEET_NAME);
 		verify(foundBodySecondHyperLink).setAddress(
 				PATH_TO_CAMPAIGN_FILE + "#'" + LINK_2_BODY_ID_SHEET_NAME + "'!B"
 						+ (VIN_LIST_2_ROW_INDEX + EXCEL_ROW_OFFSET));
 		verify(secondVinDescriptionCell).setCellValue(VIN_LIST_2_DESC);
 		verify(clientSheet).shiftRows(eq(FOUND_ROW_INDEX + 2), anyInt(), eq(SHIFT_VALUE));
 		verify(fourthLinkToVin).setCellValue(LINK_4_BODY_ID_SHEET_NAME);
 		verify(foundBodyFourthHyperLink).setAddress(
 				PATH_TO_CAMPAIGN_FILE + "#'" + LINK_4_BODY_ID_SHEET_NAME + "'!B"
 						+ (VIN_LIST_4_ROW_INDEX + EXCEL_ROW_OFFSET));
 		verify(fourthVinDescriptionCell).setCellValue(VIN_LIST_4_DESC);
 		verify(notFoundBodyIdCell, never()).getRow();
 		verify(linkToAnotherVinCell).setCellValue(LINK_3_BODY_ID_SHEET_NAME);
 		verify(anotherFoundBodyHyperLink).setAddress(
 				PATH_TO_CAMPAIGN_FILE + "#'" + LINK_3_BODY_ID_SHEET_NAME + "'!B"
 						+ (VIN_LIST_3_ROW_INDEX + EXCEL_ROW_OFFSET));
 		verify(anotherFoundBodyVinDescriptionCell).setCellValue(VIN_LIST_ANOTHER_FOUND_BODY_DESC);
 		verify(clientWorkbook).write(any(FileOutputStream.class));
 	}
 
 }
