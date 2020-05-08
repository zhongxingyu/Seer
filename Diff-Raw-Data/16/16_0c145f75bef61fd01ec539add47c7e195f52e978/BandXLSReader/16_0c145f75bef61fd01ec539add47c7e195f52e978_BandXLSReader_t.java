 package pb.tps.reader;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 
 import pb.tps.model.Book;
 import pb.tps.model.ItemLink;
 import pb.tps.model.Links;
 
 import com.google.common.base.Strings;
 
 public class BandXLSReader extends Links<ItemLink> implements Runnable {
 
 	private boolean isAll = true;
 	private int defPageId = 0;
 	private Book book;
 
 	private static final int ALIAS_COL = 0;
 	private static final int NAME_COL = 1;
 	private static final int PAGE_COL = 2;
 
 	public BandXLSReader(String fileName, int defPageId) {
 		this.book = new Book(fileName);
 		this.defPageId = defPageId;
 	}
 
 	/**
 	 * Creates a multi map with sorted keys and sorted values for each key
 	 * 
 	 * @return
 	 */
 	@Override
 	public void run() {
 		Sheet sheet = book.getSheet(0);
 		int rowIndex = 0;
 
 		for (Row row : sheet) {
 			if (rowIndex == 0) {
 				rowIndex++;
 				continue;
 			}
 
 			if (row.getCell(NAME_COL) == null) {
 				break;
 			}
 
 			String sort = "";
 			if (row.getCell(ALIAS_COL) != null) {
 				sort = row.getCell(ALIAS_COL).toString();
 				int dot = sort.indexOf('.');
 				if (dot != -1) {
 					sort = sort.substring(0, sort.indexOf('.'));
 				}
 			}
 
 			int pageId = 0;
 			String value = row.getCell(PAGE_COL).toString();
 			if (!StringUtils.isBlank(value)) {
 				Double page = -1d;
 				try {
 					page = row.getCell(PAGE_COL).getNumericCellValue();
 				} catch (Exception e) {
 					System.out.println("ROW " + rowIndex + " v " + value);
 					e.printStackTrace();
 				}
 				pageId = page.intValue();
 			}
 
			System.out.println(rowIndex + " " + row.getCell(NAME_COL));
 			String band = row.getCell(NAME_COL).getStringCellValue();
 			boolean skip = Strings.isNullOrEmpty(band);
 			if (!skip) {
 				if (isAll || pageId > 0) {
 					if (pageId == 0) {
 						pageId = defPageId;
 					}
 					this.add(new ItemLink(sort, band, pageId));
 				} else {
 					ItemLink bl = new ItemLink(sort, band, 0);
 					bl.setIsEmpty(true);
 					this.add(bl);
 				}
 			}
 			rowIndex++;
 		}
 	}
 }
