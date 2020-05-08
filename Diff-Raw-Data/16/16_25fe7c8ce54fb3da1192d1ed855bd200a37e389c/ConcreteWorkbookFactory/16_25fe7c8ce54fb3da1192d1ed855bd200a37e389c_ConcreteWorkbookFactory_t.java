 package controller;
 
 import model.WorkBook;
 
public class ConcreteWorkbookFactory{
 
	public static WorkBook createWorkbook() {
 		return new WorkBook();
 	}
 }
