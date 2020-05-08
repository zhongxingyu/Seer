 package controller;
import fileexport.OutputFormat;
 import fileexport.OutputWriter;
 import fileexport.ReportGenerator;
 import fileimport.ReaderFactory;
import fileimport.XMLReader;
import model.ModelDiagram;
 
 
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
 	
	public void write(String fp, OutputFormat of) {
		ow.write(fp, of);
 	}
 }
