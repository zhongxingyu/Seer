 package oleksii.filonov.writer;
 
 import com.google.common.collect.ListMultimap;
 import oleksii.filonov.reader.CampaignProcessor;
 import oleksii.filonov.reader.ColumnExcelReader;
 import oleksii.filonov.reader.ColumnReaderHelper;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.*;
 import org.hamcrest.CoreMatchers;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.Iterator;
 
 import static oleksii.filonov.TestConstants.*;
 import static org.junit.Assert.assertThat;
 
 public class WorkbookBuilderIntegrationTest {
 
 	private static final String BODY_ID_MARKER = "Номер кузова";
 	private static final String VIN_MARKER = "VIN";
 
     private ColumnReaderHelper columnReaderHelper;
 
     private ColumnExcelReader columnExcelReader;
 
 	private CampaignProcessor campaignProcessor;
 
 	@Before
 	public void setUp() throws InvalidFormatException, IOException {
         if(!Files.exists(TARGET_RESOURCE)) {
             Files.createDirectory(TARGET_RESOURCE);
         }
 		columnReaderHelper = new ColumnReaderHelper();
 		campaignProcessor = new CampaignProcessor();
 		campaignProcessor.setColumnReaderHelper(columnReaderHelper);
         columnExcelReader = new ColumnExcelReader();
         columnExcelReader.setColumnReaderHelper(columnReaderHelper);
     }
 
 	@Test
 	public void formLinkedDocument() throws IOException, InvalidFormatException {
         final Workbook clientWB = WorkbookFactory.create(CLIENT_FILE);
         final Sheet clientSheet = clientWB.getSheetAt(0);
         final Cell[] bodyIds = columnExcelReader.getColumnValues(clientSheet, BODY_ID_MARKER);
         DataBuilder excelBuilder = new WorkbookBuilder();
         excelBuilder.useWorkbook(clientWB);
 		final ListMultimap<String, String> linkedBodyIdWithCampaigns = campaignProcessor.linkBodyIdWithCampaigns(
 				bodyIds, CAMPAIGN_FILE, VIN_MARKER);
 		excelBuilder.assignTasks(bodyIds, linkedBodyIdWithCampaigns);
 		excelBuilder.saveToFile(LINKED_RESULT_PATH.toFile());
        final Workbook workbookForVerification = WorkbookFactory.create(CLIENT_FILE);
         final Sheet verifyClientSheet = workbookForVerification.getSheetAt(0);
         final Iterator<Row> clientIterator = verifyClientSheet.rowIterator();
         //check for cell type
        assertThat(columnReaderHelper.findColumnIndex(clientIterator, "'10C150'!B213"), CoreMatchers.equalTo(6));
     }
 
 }
