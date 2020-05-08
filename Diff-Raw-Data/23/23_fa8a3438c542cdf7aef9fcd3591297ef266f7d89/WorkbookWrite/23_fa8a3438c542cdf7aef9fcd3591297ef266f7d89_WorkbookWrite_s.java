 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.co.brenn.xlwrap;
 
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.exec.XLWrapMaterializer;
 import at.jku.xlwrap.map.MappingParser;
 import at.jku.xlwrap.map.XLWrapMapping;
 import at.jku.xlwrap.spreadsheet.Cell;
 import at.jku.xlwrap.spreadsheet.Sheet;
 import at.jku.xlwrap.spreadsheet.Workbook;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import com.hp.hpl.jena.rdf.model.Model;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import uk.co.brenn.metadata.MetaDataCreator;
 
 /**
  *
  * @author Christian
  */
 public class WorkbookWrite {
 
     private static String MAP_FILE_ROOT = "output/mappings/";
 
     private static String RDF_FILE_ROOT = "output/rdf/";
 
     private static String RDF_BASE_URL = "http://rdf.fba.org.uk/";
 
     private String doi;
 
     SheetWrite[] sheetWrites;
 
     private static String dataRoot;
 
     private static String metaRoot;
 
     public static void setRoots(String metaRoot, String dataRoot){
         WorkbookWrite.dataRoot = dataRoot;
         WorkbookWrite.metaRoot = metaRoot;
     }
 
     //, String mapFileName, String rdfFileName
     public WorkbookWrite (String metaFileName) throws XLWrapException, XLWrapEOFException, XLWrapMapException{
         Workbook workbook = MetaDataCreator.getExecutionContext().getWorkbook(metaRoot + metaFileName);
         Sheet metaData = workbook.getSheet("MetaData");
         Cell cell;
         try{
             cell = metaData.getCell(1, 0);
         } catch (NullPointerException e) {
             throw new XLWrapMapException("Workbook: " + metaFileName + " does not have a \"MetaData\" sheet.");
         }
         String dataFileName = cell.getText();
         Workbook dataWorkbook = MetaDataCreator.getExecutionContext().getWorkbook(dataRoot + dataFileName);
 
         cell = metaData.getCell(1, 1);
         doi = cell.getText();
 
         String[] sheetNames = workbook.getSheetNames();
         sheetWrites = new SheetWrite[sheetNames.length - 2];
         int j = 0;
         for (int i = 0; i< sheetNames.length; i++ ){
             if (sheetNames[i].equals("MetaData") || sheetNames[i].equals("Lists")) {
                 //do nothing
             } else {
                 sheetWrites[j] = new SheetWrite(workbook, dataRoot + dataFileName, doi, sheetNames[i]);
                 MasterReader masterReader = new MasterReader ();
                 masterReader.check(sheetWrites[j]);
                 j++;
             }
         }
     }
 
     private void writePrefix (BufferedWriter writer) throws IOException{
         writer.write("@prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .");
         writer.newLine();
         writer.write("@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
         writer.newLine();
         writer.write("@prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .");
         writer.newLine();
         writer.write("@prefix owl:    <http://www.w3.org/2002/07/owl#> .");
         writer.newLine();
         writer.write("@prefix foaf:	<http://xmlns.com/foaf/0.1/> .");
         writer.newLine();
         writer.write("@prefix ex:	<" + RDF_BASE_URL + "resource/> .");
         writer.newLine();
         writer.write("@prefix vocab:	<" + RDF_BASE_URL + "vocab/resource/> .");
         writer.newLine();
         writer.write("@prefix dc:     <http://purl.org/dc/elements/1.1/> .");
         writer.newLine();
         writer.write("@prefix xl:	<http://purl.org/NET/xlwrap#> .");
         writer.newLine();
         writer.write("@prefix scv:	<http://purl.org/NET/scovo#> .");
         writer.newLine();
         writer.write("@prefix :       <http://myApplication/configuration#> .");
         writer.newLine();
         writer.newLine();
     }
 
      public void writeMap() throws IOException, XLWrapMapException, XLWrapException, XLWrapEOFException{
         System.out.println("write map");
        File mapFile = new File(MAP_FILE_ROOT + doi + ".trig");
         BufferedWriter mapWriter = new BufferedWriter(new FileWriter(mapFile));
         writePrefix(mapWriter);
 
         mapWriter.write("# mapping");
         mapWriter.newLine();
         mapWriter.write("{ [] a xl:Mapping ;");
         mapWriter.newLine();
         for (int i = 0; i < sheetWrites.length; i++){
             sheetWrites[i].writeMapping(mapWriter);
         }
         mapWriter.write("}");
         mapWriter.newLine();
         mapWriter.newLine();
 
         for (int i = 0; i < sheetWrites.length; i++){
             sheetWrites[i].writeTemplate(mapWriter);
          }
         mapWriter.close();
         System.out.println("Done writing map file");
     }
 
    public void runMap() throws XLWrapException, IOException{
         System.out.println("Running map");
         XLWrapMapping map = MappingParser.parse(MAP_FILE_ROOT + doi + ".trig");
 
         XLWrapMaterializer mat = new XLWrapMaterializer();
         Model m = mat.generateModel(map);
         m.setNsPrefix("ex", RDF_BASE_URL);
 
        File out = new File (RDF_FILE_ROOT + doi + ".rdf");
         FileWriter writer = new FileWriter(out);
                 //"RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3"
         //m.write(writer, "RDF/XML", RDF_BASE_URL);
         m.write(writer, "RDF/XML");
         System.out.println("Done writing rdf file to "+ out.getAbsolutePath());
     }
 
 }
