 package controller;
import model.ModelDiagram;
import fileexport.OutputInterface;
 import fileexport.OutputWriter;
 import fileexport.ReportGenerator;
 import fileimport.ReaderFactory;
 
 
 public class GUIController {
 	DataController dc;
 	ReportGenerator rg = new ReportGenerator();
 	OutputWriter ow = new OutputWriter();
 	ReaderFactory rf = new ReaderFactory();
 	
 	public GUIController() {
 		
 	}
 	
 	public void generateReport() {
 		rg.generateReport();
 	}
 	
 	public ModelDiagram read(String fp) {
 		return rf.read(fp);
 	}
 	
	public void write(String fp, String form) {
		ow.write(fp, form);
 	}
 }
