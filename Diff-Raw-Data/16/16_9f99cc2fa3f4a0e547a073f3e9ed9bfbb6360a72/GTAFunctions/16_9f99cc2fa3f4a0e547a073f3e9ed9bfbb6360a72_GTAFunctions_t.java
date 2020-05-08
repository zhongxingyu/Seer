 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 
 public class GTAFunctions {
 	public static Map<String, String> skuSubF;
 	public static Map<Integer, TreeMap<String, Integer>> weeklyMapsSF;
 	public static Map<Integer, TreeMap<String, Double>> weeklyMapsSku;
 	static int wks = 0;
 	static Row wkRow0;
 	
 	public static void countAndCheck(File inputPath) {
 		ArrayList<File> folderFiles = new ArrayList<File>();
 		boolean isFirst = true;
 		boolean isLast = false;
 		
 		for (File f : inputPath.listFiles()){
 			folderFiles.add(f);
 		}
 		
 		GTAGUI.generalMessage("Found " + (folderFiles.size()-1) + " files in folder " + inputPath.getName());
 		for (File f2 : folderFiles) {
 			if (!(f2.getName().contains("DS_Store"))){
 				GTAGUI.generalMessage(f2.getName() + ", Last Modified on " + (new Date(f2.lastModified())).toString());
 				if(folderFiles.indexOf(f2) == folderFiles.size()-1 ) {
 					isLast = true;
 				}
 				calculateMix(f2, isFirst, isLast);
 				isFirst = false;
 				
 			}
 		}
 
 	}
 	
 	public static void calculateMix(File f2, boolean isFirst, boolean isLast){
 		Workbook wb = new HSSFWorkbook();
 		Sheet sheet1 = null, sheet0 = null;
 		FileInputStream readStr = null;
 		String name = f2.getName();
 		String subName = name.substring(name.indexOf(".")+1, name.lastIndexOf("."));
 		try {
 			readStr = new FileInputStream(f2);
 			wb = new HSSFWorkbook(readStr);
 			sheet0 = wb.getSheet("Product");
 			sheet1 = wb.getSheet("PPN");
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		wkRow0 = sheet0.getRow(7);
 		
 		//how many weeks in this quarter, check only once
 		if(isFirst) {
 			wks = calcWeeks(wkRow0);
 			GTAGUI.generalMessage("Weeks in this quarter: " + wks);
 		}
 		
 		
 		// list subfamilies, list this for each file
 		ArrayList<String> subFamilies = new ArrayList<String>();
 		for (Row r : sheet0) {
 			try {
 				if (r.getRowNum() > 6 && 
 					!(r.getCell(1).getStringCellValue().contains("Total")) &&
 					!(r.getCell(1).getCellType()==Cell.CELL_TYPE_BLANK) && 
 					!(sheet0.getRow(r.getRowNum()+1).getCell(2).getCellType()==Cell.CELL_TYPE_STRING)) {
 				subFamilies.add(r.getCell(1).getStringCellValue());
 				}
 			} catch (NullPointerException e) {
 				//do nothing
 			} catch (IllegalStateException e) {
 				//do nothing
 			}
 		}
 	//	GTAGUI.generalMessage("SubFamilies: " + subFamilies); //testing only, delete later
 		
 		//create map Sku to SubFamily, also for each file
 		skuSubF = new HashMap<String, String>();
 		skuSubF = createSkuSubF(sheet1);
 	//	GTAGUI.generalMessage("Map SKU to SubFamily" +skuSubF);//testing only, delete later
 		
 		//create forecast/wk/subfamily, also for each file, but first file builds header and last file creates the .xls file
 		weeklyMapsSF = new TreeMap<Integer, TreeMap<String, Integer>>();
 		weeklyMapsSF = createSubFPerWeek( wks, sheet0);
 		GTAGUI.generalMessage("Map qty by SubFamily" +weeklyMapsSF);//testing only, delete later
 		TemplateBuilder.FrcstByAccntBySubF(subName,weeklyMapsSF, isFirst, isLast);
 		
 		//create mix/wk/sku, also for each file
 		weeklyMapsSku = new TreeMap<Integer, TreeMap<String, Double>>();
 		weeklyMapsSku = createMixPerWeek(wks, sheet1, wkRow0);
 		GTAGUI.generalMessage("Total Mix per Sku per week" + weeklyMapsSku);//testing only, delete later
 		TemplateBuilder.createMixTemplate(subName, weeklyMapsSku, isFirst, isLast);
 		
 		//create forecast judged and create the .xls
 		TemplateBuilder.createTemplate(subName, weeklyMapsSku, isFirst, isLast);
 		
 	}
 	
 	//Pure calculations start here
 	/**
 	 * 
 	 * @param wkRow0
 	 * @return
 	 */
 	public static int calcWeeks (Row wkRow0) {
 		int nwks = 0;
 		for (Cell c : wkRow0) {
 			try {
 				if (c.getStringCellValue().equalsIgnoreCase("ST")) {
 					nwks = (int) wkRow0.getCell((c.getColumnIndex() - 1)).getNumericCellValue();
 					break;
 				}
 			} catch (NullPointerException e) {
 				//e.printStackTrace();
 			} catch (IllegalStateException e) {
 				//do nothing
 			}
 		}
 		return nwks;
 	}
 	
 	/**
 	 * 
 	 * @param wks
 	 * @param sheet0
 	 * @return
 	 */
 	public static Map<Integer, TreeMap<String, Integer>> createSubFPerWeek(int wks, Sheet sheet0 ){
 		Row wkRow00 = sheet0.getRow(7);
 		Map<Integer, TreeMap<String, Integer>> weeklyMapsSF = new TreeMap<Integer, TreeMap<String, Integer>>();
 		int columnIndexSF = 0;
 		for (int w = 1; w < wks + 1; w++) {
 			Map<String, Integer> subFPerWk = new TreeMap<String, Integer>();//create a new map for each wk
 			//find the column index of this wk
 			for (Cell d : wkRow00) {
 				try {
 					if ((new Double(d.getNumericCellValue())).intValue() == w) {
 						columnIndexSF = d.getColumnIndex();
 						break;
 					}
 				} catch (NullPointerException e) {
 				//	e.printStackTrace();
 				} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 				}
 			}
 			//build subFamily Per Week map
 			for (Row rw : sheet0) {
 				try {
 					if (rw.getRowNum() > 6 && 
 							!(rw.getCell(1).getCellType()==Cell.CELL_TYPE_BLANK) &&
 							!(rw.getCell(1).getStringCellValue().contains("Total")) && 
 							!(sheet0.getRow(rw.getRowNum()+1).getCell(2).getCellType()==Cell.CELL_TYPE_STRING)) {
 						subFPerWk.put(rw.getCell(1).getStringCellValue(), (new Double(rw.getCell(columnIndexSF).getNumericCellValue())).intValue());
 					}
 				} catch (NullPointerException e) {
 				//	e.printStackTrace();
 				} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 				}
 				
 			}
 			weeklyMapsSF.put(w, (TreeMap<String, Integer>) subFPerWk);
 		}
 		return weeklyMapsSF;
 	}
 	
 	/**
 	 * 
 	 * @param wks
 	 * @param sheet1
 	 * @param wkRow0
 	 * @return
 	 */
 	public static Map<Integer, TreeMap<String, Double>> createMixPerWeek(int wks, Sheet sheet1, Row wkRow0 ){
 		Row wkRow1 = sheet1.getRow(7);
 		Map<Integer, TreeMap<String, Double>> weeklyMapsSku = new TreeMap<Integer, TreeMap<String, Double>>();
 		int columnIndexSku = 0;
 		int columnIndexSF =0;
 		for (int w = 1; w < wks + 1; w++) {
 			Map<String, Double> skuPerWk = new TreeMap<String, Double>();//create a new map for each wk
 			//find the column index of this wk, for the SKU tab
 			for (Cell d : wkRow1) {
 				try {
 					if ((new Double(d.getNumericCellValue())).intValue() == w) {
 						columnIndexSku = d.getColumnIndex();
 						break;
 					}
 				} catch (NullPointerException e) {
 				//	e.printStackTrace();
 				} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 				}
 			}
 			//find the column index of this wk, for the SubFamily tab
 			for (Cell d2 : wkRow0) {
 				try {
 					if ((new Double(d2.getNumericCellValue())).intValue() == w) {
 						columnIndexSF = d2.getColumnIndex();
 						break;
 					}
 				} catch (NullPointerException e) {
 				//	e.printStackTrace();
 				} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 				}
 			}
 			//build Sku mix Per Week map
 			for (Row rw : sheet1) {
 				try {
 					if (rw.getRowNum() > 6 && 
 							!(rw.getCell(3).getCellType()==Cell.CELL_TYPE_BLANK) &&
 							rw.getCell(3).getStringCellValue().contains("PPM"))  {
						//for some reason we are not catching the /0, here's the workaround
						if((weeklyMapsSF.get(columnIndexSF - 1).get(skuSubF.get(rw.getCell(3).getStringCellValue()))) == 0) {
							skuPerWk.put(rw.getCell(3).getStringCellValue(),0.0);
						} else {
							skuPerWk.put(rw.getCell(3).getStringCellValue(), (new Double(rw.getCell(columnIndexSku).getNumericCellValue()))/(weeklyMapsSF.get(columnIndexSF - 1).get(skuSubF.get(rw.getCell(3).getStringCellValue()))) );
						}
 					}
 				} catch (NullPointerException e) {
 				//	e.printStackTrace();
 				} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 				} catch (ArithmeticException e) {
				//  e.printStackTrace();
 					skuPerWk.put(rw.getCell(3).getStringCellValue(),0.0);
 				}
 				
 			}
 			weeklyMapsSku.put(w, (TreeMap<String, Double>) skuPerWk);
 		}
 		return weeklyMapsSku;
 	}
 	
 	/**
 	 * 
 	 * @param sheet1
 	 * @return
 	 */
 	public static Map<String, String> createSkuSubF(Sheet sheet1) {
 		Map<String, String> skuSubF = new HashMap<String, String>();
 		ArrayList<String> tempArray = new ArrayList<String>();
 		int test = 0, test1 = 0, test2 = 0, test3 = 0;
 		
 		for (Row r : sheet1) {
 			test++;
 			try {
 				if( r.getRowNum() > 6 &&
 					(r.getCell(2).getCellType()==Cell.CELL_TYPE_BLANK && !(sheet1.getRow(r.getRowNum()+1).getCell(3).getCellType()==Cell.CELL_TYPE_BLANK) ||
 					(!(r.getCell(2).getCellType()==Cell.CELL_TYPE_BLANK) && !(sheet1.getRow(r.getRowNum()+1).getCell(3).getCellType()==Cell.CELL_TYPE_BLANK)) ) &&
 					r.getCell(3).getStringCellValue().contains("PPM")) {
 						tempArray.add(r.getCell(3).getStringCellValue());
 						test1++;
 				} else if(!(r.getCell(2).getCellType()==Cell.CELL_TYPE_BLANK) &&
 						sheet1.getRow(r.getRowNum()+1).getCell(3).getCellType()==Cell.CELL_TYPE_BLANK &&
 						 !(r.getCell(2).getStringCellValue().contains("Total"))) {
 					test2++;
 					for (String tmp : tempArray) {
 						skuSubF.put(tmp, r.getCell(2).getStringCellValue());
 						test3++;
 					}
 					tempArray = new ArrayList<String>();//create a new list object, instead of clearing the previous one
 				}
 			} catch (NullPointerException e) {
 				//	e.printStackTrace();
 			} catch (IllegalStateException e) {
 				//	e.printStackTrace();
 			}
 		}
 		GTAGUI.generalMessage("test: " + test + ", test1: " + test1 + ", test2: " + test2 + ", test3: " + test3);
 		return skuSubF;
 	}
 }
