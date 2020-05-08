 package ycp.edu.seniordesign.util;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 
 import ycp.edu.seniordesign.model.Assignment;
 import ycp.edu.seniordesign.model.persist.Database;
 
 public class CreateAssignmentsFromExcelFile {
 	
 	public static void createAssignmentsFromExcelSheet(String filename, int courseId) throws Exception{		
 		String [][] assignmentInfo = generateAssignmentInfo(filename);
 		
 		// Determine which row in the excel sheet is which based on the headings
 		int studentNameColumn = -1;
 		int assignmentNameColumn = -1;
 		int earnedPointsColumn = -1;
 		int possiblePointsColumn = -1;
 		int dueDateColumn = -1; // FIXME: Are professor really going to have such a column in their gradebook?
 		int gradeWeightColumn = -1; // FIXME: Are professor really going to have such a column in their gradebook?
 		
 		for(int i = 0; i < 100; i++){
 			String heading = assignmentInfo[0][i];
 			if (heading.toLowerCase().equals("student name")){
 				studentNameColumn = i;
 			} else if (heading.toLowerCase().equals("assignment name")){
 				assignmentNameColumn = i;
 			} else if (heading.toLowerCase().equals("earned points")){
 				earnedPointsColumn = i;
 			} else if (heading.toLowerCase().equals("possible points")){
 				possiblePointsColumn = i;
 			} else if (heading.toLowerCase().equals("due date")){
 				dueDateColumn = i;
 			} else if (heading.toLowerCase().equals("grade weight")){
 				gradeWeightColumn = i;
 			} 
 		}
 				
 		for(int i = 1; i < 100; i++){			
 			String studentName = assignmentInfo[i][studentNameColumn];
 			
 			// Only use the row if there is a student name
 			if (!studentName.equals("")){
 				// Parse the values for the row
 				String assignmentName = assignmentInfo[i][assignmentNameColumn];
 				
 				String tempEarnedPoints = assignmentInfo[i][earnedPointsColumn];
 				int earnedPoints = Integer.parseInt(tempEarnedPoints.substring(0, tempEarnedPoints.length() - 2));
 				
 				String tempPossiblePoints = assignmentInfo[i][possiblePointsColumn];
 				int possiblePoints = Integer.parseInt(tempPossiblePoints.substring(0, tempPossiblePoints.length() - 2));
 				
 				String date = assignmentInfo[i][dueDateColumn];
 				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 				Date dueDate =  (Date) formatter.parse(date);
 			    
 			    String tempGradeWeight = assignmentInfo[i][gradeWeightColumn];
 				int gradeWeight = Integer.parseInt(tempGradeWeight.substring(0, tempGradeWeight.length() - 2));
 				
 				if (assignmentName.equals("")){
 					// No assignment name, use "Unnamed Assignment" as default
 					assignmentName = "Unnamed Assignment";
 				}				
 				
 				// Look up the id of the user associated with the student name
 				int studentId = Database.getInstance().getUserByName(studentName).getId(); // FIXME: this will not work if there are multiple students with the same name
 			
 				// Look up the id of the grade weight associated with this course and grade weight
				int gradeWeightId = Database.getInstance().addGradeWeight(gradeWeight, courseId); //FIXME: currently this adds a new grade weight for each row, this should not be the case
 				
 				// Create the assignment object
 				Assignment assignment = new Assignment(-1, courseId, studentId, assignmentName, dueDate, gradeWeightId, earnedPoints, possiblePoints); 
 				
 				// Add the assignment to the database
 				Database.getInstance().addAssignmentForCourse(assignment);
 			}
 		}
 	}
 	
 	private static String[][] generateAssignmentInfo (String filename) throws SQLException, ParseException, IOException {
 		// Create a blank two dimensional array to store the information
 		String [][]assignmentInfo = new String[100][100]; // Note: the sheet must be 100 X 100 or less
 		for (int i = 0; i < 100; i++){
 			for (int j = 0; j < 100; j++){
 				assignmentInfo[i][j] = "";
 			}
 		}
 		
 		FileInputStream fileInputStream = null;
 		try {
 			// Create a FileInputStream that will be use to read the excel file.
 			fileInputStream = new FileInputStream(filename);
 
 			// Create an excel workbook from the file system.
 			HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
 			
 			// Get the first sheet on the workbook.
 			HSSFSheet sheet = workbook.getSheetAt(0);
 			
 			// Read in all the information from the excel sheet to the array
 			int rowPos = 0;
 		    Iterator<Row> rows = sheet.rowIterator();
 		    while (rows.hasNext()) {
 		        HSSFRow row = (HSSFRow) rows.next();
 
 		        Iterator<Cell> cells = row.cellIterator();
 		        while (cells.hasNext()) {
 		            HSSFCell cell = (HSSFCell) cells.next();
 		            String value = "";
 		            
 		            switch (cell.getCellType()) {
 		                case HSSFCell.CELL_TYPE_NUMERIC:
 		                    value = BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
 		                    break;
 
 		                case HSSFCell.CELL_TYPE_STRING:
 		                    value = cell.getStringCellValue();
 		                    break;
 
 		                case HSSFCell.CELL_TYPE_BLANK:
 		                    value = "";
 		                    break;
 
 		                case HSSFCell.CELL_TYPE_FORMULA:
 		                    value = cell.getCellFormula();
 		                    break;
 		                    
 		                default:
 		                    break;
 		            }
 		            assignmentInfo[rowPos][cell.getColumnIndex()] = value;
 		        }
 		        rowPos++;
 		    }
 
 		    
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (fileInputStream != null) {
 				try {
 					fileInputStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		return assignmentInfo;
 	}
 }
