 package edu.rutgers.MOST.data;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.table.DefaultTableModel;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import edu.rutgers.MOST.config.LocalConfig;
 import edu.rutgers.MOST.presentation.GraphicalInterfaceConstants;
 
 public class TextMetabolitesModelReader {
 
 	private static DefaultTableModel metabolitesTableModel;
 	
 	public static DefaultTableModel getMetabolitesTableModel() {
 		return metabolitesTableModel;
 	}
 
 	public static void setMetabolitesTableModel(
 			DefaultTableModel metabolitesTableModel) {
 		TextMetabolitesModelReader.metabolitesTableModel = metabolitesTableModel;
 	}
 
 	public static Map<Object, String> metaboliteIdNameMap = new HashMap<Object, String>();
 	
 	public ArrayList<String> columnNamesFromFile(File file, int row) {
 		ArrayList<String> columnNamesFromFile = new ArrayList<String>();
 
 		String[] dataArray = null;
 
 		//use fileReader to read first line to get headers
 		BufferedReader CSVFile;
 		try {
 			CSVFile = new BufferedReader(new FileReader(file));
 			String dataRow = CSVFile.readLine();
 			dataArray = dataRow.split(",");				
 
 			//add all column names to list			
 			for (int h = 0; h < dataArray.length; h++) { 
 
 				//remove quotes if exist
 				if (dataArray[h].startsWith("\"")) {
 					//removes " " and null strings
 					if (dataArray[h].compareTo("\" \"") != 0 && dataArray[h].trim().length() > 0) {
 						columnNamesFromFile.add(dataArray[h].substring(1, dataArray[h].length() - 1));
 					}					
 				} else {
 					if (dataArray[h].trim().length() > 0) {
 						columnNamesFromFile.add(dataArray[h]);
 					}					
 				}			
 			} 	
 
 			if (row > 0) {
 				for (int i = 0; i < row; i++) {
 					dataRow = CSVFile.readLine();
 					dataArray = dataRow.split(",");				
 
 					columnNamesFromFile.clear();
 					//add all column names to list			
 					for (int h = 0; h < dataArray.length; h++) { 
 						//remove quotes if exist
 						if (dataArray[h].startsWith("\"")) {
 							//removes " " and null strings
 							if (dataArray[h].compareTo("\" \"") != 0 && dataArray[h].trim().length() > 0) {
 								columnNamesFromFile.add(dataArray[h].substring(1, dataArray[h].length() - 1));
 							}					
 						} else {
 							if (dataArray[h].trim().length() > 0) {
 								columnNamesFromFile.add(dataArray[h]);
 							}					
 						}			
 					} 
 				}				
 			}
 
 			CSVFile.close();
 
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();							
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 		
 		return columnNamesFromFile;
 	}
 
 	public Integer numberOfLines(File file) {
 		int count = 0;
 		CSVReader reader;
 		try {
 			reader = new CSVReader(new FileReader(file), ',');
 			String [] dataArray;
 			try {
 				while ((dataArray = reader.readNext()) != null) {
 					count++; 	
 				}
 				reader.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return count;		
 	}
 
 	public void load(File file){
 		DefaultTableModel metabTableModel = new DefaultTableModel();
 		for (int m = 0; m < GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES.length; m++) {
 			metabTableModel.addColumn(GraphicalInterfaceConstants.METABOLITES_COLUMN_NAMES[m]);
 		}
 		for (int n = 0; n < LocalConfig.getInstance().getMetabolitesMetaColumnNames().size(); n++) {
 			metabTableModel.addColumn(LocalConfig.getInstance().getMetabolitesMetaColumnNames().get(n));
 		}
 		LocalConfig.getInstance().getMetaboliteUsedMap().clear();
 		LocalConfig.getInstance().getSuspiciousMetabolites().clear();
 		LocalConfig.getInstance().getMetaboliteNameIdMap().clear();
 
 		//if first row of file is not column names, starts reading after row that contains names
 		int correction = LocalConfig.getInstance().getMetabolitesNextRowCorrection();
 		int row = 1;
 		
 		CSVReader reader;
 		
 		ArrayList<Integer> blankMetabIds = new ArrayList<Integer>();
 		
 		try {
 			reader = new CSVReader(new FileReader(file), ',');
 			String [] dataArray;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			reader = new CSVReader(new FileReader(file), ',');
 			int numLines = numberOfLines(file);
 			
 			//sets maximum metabolite id for use in adding metabolites to metaboliteIdNameMap
 			//when reactions contain metabolites not present in file being read
 			LocalConfig.getInstance().setMaxMetabolite(numLines - 1 - correction);
 			LocalConfig.getInstance().setMaxMetaboliteId(numLines - 1 - correction);
 			
 			int id = 0;
 			for (int i = 0; i < numLines; i++) {
 				String [] dataArray = reader.readNext();
 				for (int s = 0; s < dataArray.length; s++) {
 					if (dataArray[s].length() > 0 && dataArray[s].substring(0,1).matches("\"")) {
 						dataArray[s] = dataArray[s].substring(1, (dataArray[s].length() - 1));
 					}
 				}
 				if (i >= (row + correction)) {
 					Vector <String> metabRow = new Vector<String>();
 					metabRow.add(Integer.toString(id));
 					String compartment = "";
 					String boundary = "";
 					
 	                String metaboliteAbbreviation = dataArray[LocalConfig.getInstance().getMetaboliteAbbreviationColumnIndex()];
 	                
 					if (metaboliteAbbreviation == null || metaboliteAbbreviation.trim().length() == 0) {
 						blankMetabIds.add(id);		
 					} else {
 						if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(metaboliteAbbreviation)) {
 							metaboliteAbbreviation = metaboliteAbbreviation + duplicateSuffix(metaboliteAbbreviation);
 						}
 						LocalConfig.getInstance().getMetaboliteNameIdMap().put(metaboliteAbbreviation, id);
 					}
					System.out.println(LocalConfig.getInstance().getMetaboliteNameIdMap());
 					metabRow.add(metaboliteAbbreviation);
 					
 					metabRow.add(dataArray[LocalConfig.getInstance().getMetaboliteNameColumnIndex()]);
 					metaboliteIdNameMap.put(new Integer(id), dataArray[LocalConfig.getInstance().getMetaboliteNameColumnIndex()]);
 					//metaboliteName = dataArray[LocalConfig.getInstance().getMetaboliteNameColumnIndex()];
 					//chargeString = dataArray[LocalConfig.getInstance().getChargeColumnIndex()];	
 					metabRow.add(dataArray[LocalConfig.getInstance().getChargeColumnIndex()]);	
 					if (LocalConfig.getInstance().getCompartmentColumnIndex() > -1) {
 						compartment = dataArray[LocalConfig.getInstance().getCompartmentColumnIndex()];	
 					}
 					metabRow.add(compartment);
 					if (LocalConfig.getInstance().getBoundaryColumnIndex() > -1) {									
 						if (dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("false") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("FALSE") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("0") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("0.0") == 0) {
 							boundary = GraphicalInterfaceConstants.BOOLEAN_VALUES[0]; 
 						} else if (dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("true") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("TRUE") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("1") == 0 || dataArray[LocalConfig.getInstance().getBoundaryColumnIndex()].compareTo("1.0") == 0) {
 							boundary = GraphicalInterfaceConstants.BOOLEAN_VALUES[1];
 						}
 					} else {
 						boundary = GraphicalInterfaceConstants.BOOLEAN_VALUES[0];
 					}					
 					metabRow.add(boundary);
 					
 					if (LocalConfig.getInstance().getMetabolitesMetaColumnIndexList().size() > 0) {
 						for (int m = 0; m < LocalConfig.getInstance().getMetabolitesMetaColumnIndexList().size(); m++) {
 							metabRow.add(dataArray[LocalConfig.getInstance().getMetabolitesMetaColumnIndexList().get(m)]);
 						}
 					}
 					metabTableModel.addRow(metabRow);
 					id += 1;
 				}				
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		LocalConfig.getInstance().setMetaboliteIdNameMap(metaboliteIdNameMap);
 		LocalConfig.getInstance().setBlankMetabIds(blankMetabIds);					
 		LocalConfig.getInstance().hasMetabolitesFile = true;
 		setMetabolitesTableModel(metabTableModel);
 		//System.out.println("Done");		
 	}
 	
 	public String duplicateSuffix(String value) {
 		String duplicateSuffix = GraphicalInterfaceConstants.DUPLICATE_SUFFIX;
 		if (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(value + duplicateSuffix)) {
 			int duplicateCount = Integer.valueOf(duplicateSuffix.substring(1, duplicateSuffix.length() - 1));
 			while (LocalConfig.getInstance().getMetaboliteNameIdMap().containsKey(value + duplicateSuffix.replace("1", Integer.toString(duplicateCount + 1)))) {
 				duplicateCount += 1;
 			}
 			duplicateSuffix = duplicateSuffix.replace("1", Integer.toString(duplicateCount + 1));
 		}
 		return duplicateSuffix;
 	}
 }
 
 
