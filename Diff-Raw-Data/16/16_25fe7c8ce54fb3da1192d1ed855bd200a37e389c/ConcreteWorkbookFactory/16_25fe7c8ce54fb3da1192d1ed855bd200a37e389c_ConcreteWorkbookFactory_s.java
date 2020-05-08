 package controller;
 
 import model.WorkBook;
 
public class ConcreteWorkbookFactory implements IWorkbookFactory {
	
	public ConcreteWorkbookFactory () {
		
	}
 
	public WorkBook createWorkbook () {
 		return new WorkBook();
 	}
 }
