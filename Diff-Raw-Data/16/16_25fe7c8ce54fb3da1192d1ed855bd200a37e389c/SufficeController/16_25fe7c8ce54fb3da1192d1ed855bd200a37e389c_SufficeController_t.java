 package controller;
 
 import java.io.*;
 
 import model.WorkBook;
 
 public class SufficeController {
 	
 	// Return a new WorkBook
 	public static WorkBook newWorkbook() {
		return ConcreteWorkbookFactory.createWorkbook();
 	}
 	
 	// Save the WorkBook using serialization
 	public static void save(WorkBook w, OutputStream o) {
         WorkbookSerializer.save(w, o);
 	}
 	
 	
 	// Retrieve the saved info, then casting it as a workbook. 
 	public static WorkBook load(InputStream input) {
         return WorkbookSerializer.load(input);
 	}
 }
