 package models;
 
 import java.io.FileInputStream;
 
 import lib.Logger;
 import lib.OSProperties;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 
 import controllers.File_System_Controller;
 
 
 public class Language_Model
 {
 	private OSProperties osp = new OSProperties();
 	private String languagesFile = "language.xls";
 	private String language = ""; // User's default system language
 	private POIFSFileSystem fs;
 	private HSSFWorkbook wb;
 	private HSSFSheet sheet;
 
 	public Language_Model()
 	{
 		try
 		{
 			try
 			{
 				fs = new POIFSFileSystem(this.getClass().getResourceAsStream(osp.getSeparator() + "installation" + osp.getSeparator() + languagesFile));
 			} catch(java.io.FileNotFoundException f1) {
 				try
 				{
 					fs = new POIFSFileSystem(new FileInputStream("src" + osp.getSeparator() + "installation" + osp.getSeparator() + languagesFile));
 				} catch(java.io.FileNotFoundException f2) {
 					try
 					{
 						fs = new POIFSFileSystem(new FileInputStream(new File_System_Controller().getModel().findFile(languagesFile)));
 					} catch(java.io.FileNotFoundException f3) {
 						try
 						{
 							fs = new POIFSFileSystem(new FileInputStream("src" + osp.getSeparator() + "installation" + osp.getSeparator() + languagesFile));
 						} catch(java.io.FileNotFoundException f4) {
 							Logger.write("Language file could not be found!", Logger.Level.ERROR);
 							System.exit(1);
 						}
 					}
 				}
			} catch(java.lang.NullPointerException np) {
				try
				{
					fs = new POIFSFileSystem(new FileInputStream("src" + osp.getSeparator() + "installation" + osp.getSeparator() + languagesFile));
				} catch(java.io.FileNotFoundException f2) {
					try
					{
						fs = new POIFSFileSystem(new FileInputStream(new File_System_Controller().getModel().findFile(languagesFile)));
					} catch(java.io.FileNotFoundException f3) {
						try
						{
							fs = new POIFSFileSystem(new FileInputStream("src" + osp.getSeparator() + "installation" + osp.getSeparator() + languagesFile));
						} catch(java.io.FileNotFoundException f4) {
							Logger.write("Language file could not be found!", Logger.Level.ERROR);
							System.exit(1);
						}
					}
				}
 			}
 			wb = new HSSFWorkbook(fs);
 			sheet = wb.getSheetAt(0);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		language = new OSProperties().getLanguage();
 		if (!findLanguage(language))
 		{
 			language = "en";
 			Logger.write("Language not supported! Defaulting to English.", Logger.Level.INFO);
 		}
 	}
 
 	/**
 	 * <p>Gets the value held in the specified row.
 	 * 
 	 * @param r The row to get the value from
  	 * @return A String representation of the value held in the specified row
 	 */
 	public String getValue(int r)
 	{
 		HSSFRow row = sheet.getRow(r);
 		HSSFCell cell;
 		int column = getLanguageColumn(language);
 
 		if (row != null)
 		{
 			cell = row.getCell(column);
 			if (cell != null)
 				return cell.getRichStringCellValue().getString();
 			else // Default to English if DMS doesn't have a translation for an item
 			{
 				cell = row.getCell(1);
 				return cell.getRichStringCellValue().getString();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * <p>Finds all supported languages for DMS and chooses the
 	 * correct language for the user if their default language is
 	 * supported. Otherwise, defaults to English.
 	 */
 	private boolean findLanguage(String l)
 	{
 		// Find all supported languages for DMS
 		HSSFRow row = sheet.getRow(1);
 		HSSFCell cell;
 
 		if (row != null)
 		{
 			for (int c = 1; c < row.getPhysicalNumberOfCells(); c++)
 			{
 				cell = row.getCell(c);
 
 				if (cell != null)
 				{
 					String lang = cell.getRichStringCellValue().getString();
 					lang = lang.substring(0, 2); // Language in format ("en (English)")
 					if (lang.equals(l))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * <p>Returns the column number for the specified language.
 	 * 
 	 * @param l The language id
 	 * @return an int representation of the column containing the specified language's translations
 	 */
 	private int getLanguageColumn(String l)
 	{
 		// Find all supported languages for DMS
 		HSSFRow row = sheet.getRow(1);
 		HSSFCell cell;
 
 		if (row != null)
 		{
 			for (int c = 1; c < row.getPhysicalNumberOfCells(); c++)
 			{
 				cell = row.getCell(c);
 
 				if (cell != null)
 				{
 					String lang = cell.getRichStringCellValue().getString();
 					lang = lang.substring(0, 2); // Language in format ("en (English)")
 					if (lang.equals(language))
 						return c;
 				}
 			}
 		}
 		return 1; // Default to English
 	}
 }
