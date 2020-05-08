 package oleksii.filonov.readers;
 
import static java.lang.System.out;

 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.ListMultimap;
 
 public class CampaignProcessor {
 
 	private ColumnReaderHelper columnReaderHelper;
 
 	public ListMultimap<String, Cell> linkBodyIdWithCampaigns(final Cell[] bodyIds, final File campaignFile,
 			final String vinColumnTitle) {
 		final String[] bodyIdsToProcess = new String[bodyIds.length];
 		for (int i = 0; i < bodyIds.length; ++i) {
 			bodyIdsToProcess[i] = bodyIds[i].getStringCellValue().trim();
 		}
 		final ListMultimap<String, Cell> result = LinkedListMultimap.create(bodyIds.length);
 		Arrays.sort(bodyIdsToProcess);
		out.println(Arrays.toString(bodyIdsToProcess));
 		try {
 			final Workbook campaignWB = WorkbookFactory.create(campaignFile);
 			final int numbersOfSheet = campaignWB.getNumberOfSheets();
 			for (int sheetIndex = 1; sheetIndex < numbersOfSheet; sheetIndex++) {
 				final Sheet vinSheet = campaignWB.getSheetAt(sheetIndex);
				System.out.println("-------" + vinSheet.getSheetName() + "----------");
 				final Iterator<Row> vinRows = vinSheet.rowIterator();
 				final int vinColumnIndex = columnReaderHelper.findCell(vinRows, vinColumnTitle).getColumnIndex();
 				while (vinRows.hasNext()) {
 					final Row vinRow = vinRows.next();
 					final Cell vinCell = vinRow.getCell(vinColumnIndex);
 					if (columnReaderHelper.isStringType(vinCell)) {
						out.println("\"" + vinCell.getStringCellValue() + "\"");
 						final int bodyIndex = Arrays.binarySearch(bodyIdsToProcess, vinCell.getStringCellValue());
 						if (bodyIndex > -1) {
 							final String foundBodyId = bodyIdsToProcess[bodyIndex];
 							result.put(foundBodyId, vinCell);
 						}
 					}
 				}
 			}
 		} catch (InvalidFormatException | IOException e) {
 			throw new ReadDataException("Exception occurred while opening campaign file", e);
 		}
 		return result;
 	}
 
 	public void setColumnReaderHelper(final ColumnReaderHelper columnReaderHelper) {
 		this.columnReaderHelper = columnReaderHelper;
 	}
 
 }
