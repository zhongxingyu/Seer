 package controller;
 
 import controller.SufficeController;
import view.WorkbookEditor;
 //This class is for initialization of the application.
 //It creates a new workbook (using the builder), and starts everything up.
 
 public class SufficeBootstrapper {
 
 	public static void main(String[] args) {
 		
 		WorkbookEditor we = new WorkbookEditor ();
 		// Are we still using WorkbookEditor, or is it under
 		// a different name (SufficeTable)?
 		
 		SufficeController.newWorkbook();
 		
 		// pass a Workbook to the WorkbookEditor somehow
 	}
 
 }
