 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.co.brenn.xlwrap;
 
 import at.jku.xlwrap.common.Utils;
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.exec.ExecutionContext;
 import at.jku.xlwrap.map.expr.val.XLExprValue;
 import at.jku.xlwrap.spreadsheet.Cell;
 import at.jku.xlwrap.spreadsheet.Sheet;
 import at.jku.xlwrap.spreadsheet.Workbook;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import java.io.BufferedWriter;
 import java.io.IOException;
 
 /**
  *
  * @author Christian
  */
 public class SheetWrite {
 
     private static String RDF_BASE_URL = "http://rdf.fba.org.uk/";
 
     private ExecutionContext context;
     private Sheet metaSheet;
     private String sheetInURI;
     private int sheetNumber;
 
     private String dataPath;
     private String doi;
 
     //Excell columns and Rows used here are Excell based and not 0 based as XLWrap uses internally
     private String LAST_DATA_COLUMN = "X";
     private int CATEGORY_ROW = 1;
     private int FIELD_ROW = 2;
     private int ID_TYPE_ROW = 3;
     private int firstLink;
     private int lastLink;
     private int firstConstant;
     private int lastConstant;
     private int firstData;
 
     //, String mapFileName, String rdfFileName
     public SheetWrite (ExecutionContext context, Workbook metaWorkbook, String dataURL, String doi, String sheetName)
             throws XLWrapException, XLWrapEOFException, XLWrapMapException{
         this.doi = doi;
         dataPath = dataURL;
         Workbook workbook = context.getWorkbook(dataPath);
         String[] sheetNames = workbook.getSheetNames();
         for (int i = 0; i< sheetNames.length; i++ ){
             if (sheetNames[i].equalsIgnoreCase(sheetName)){
                 sheetNumber = i;
             }
         }
         Sheet dataSheet = workbook.getSheet(sheetName);
         sheetInURI = dataSheet.getName() + "/";
 
         System.out.println(sheetName);
         metaSheet = metaWorkbook.getSheet(sheetName);
         System.out.println(metaSheet.getName());
         String columnA;
         int row = 4;
         columnA = getCellValue("A",row);
         System.out.println(columnA);
         if (columnA.toLowerCase().contains("links")){
             firstLink = row + 1;
         } else {
             throw new XLWrapMapException ("Ignoring sheet " + sheetName + " did not find \"links\" seperator in expected place");
         }
         do{
             row++;
             columnA = getCellValue("A",row);
         } while (!columnA.toLowerCase().contains("constant"));
         lastLink = row -1;
         firstConstant = row + 1;
         do{
             row++;
             columnA = getCellValue("A",row);
         } while (columnA !=null && !columnA.isEmpty());
         lastConstant = row - 1;
         do{
             row++;
             columnA = getCellValue("A",row);
         } while (columnA == null || !columnA.toLowerCase().contains("datastart"));
         firstData = 2;
     }
 
     /*
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
   */
     private String template(){
         return metaSheet.getName() + "template";
 //        return "template";
     }
 
     protected void writeMapping(BufferedWriter writer) throws IOException, XLWrapMapException{
         writer.write("	xl:offline \"false\"^^xsd:boolean ;");
         writer.newLine();
         writer.write("	xl:template [");
         writer.newLine();
         writer.write("		xl:fileName \"" + dataPath + "\" ;");
         writer.newLine();
         writer.write("		xl:sheetNumber \""+ sheetNumber +"\" ;");
         writer.newLine();
         writer.write("		xl:templateGraph :");
         writer.write(template());
         writer.write(" ;");
         writer.newLine();
         writer.write("		xl:transform [");
         writer.newLine();
         writer.write("			a rdf:Seq ;");
         writer.newLine();
         writer.write("			rdf:_1 [");
         writer.newLine();
         writer.write("				a xl:RowShift ;");
         writer.newLine();
        writer.write("				xl:restriction \"B" + firstData + ":" + LAST_DATA_COLUMN + firstData + "\" ;");
         writer.newLine();
         writer.write("				xl:steps \"1\" ;");
         writer.newLine();
         writer.write("			] ;");
         writer.newLine();
         writer.write("		]");
         writer.newLine();
         writer.write("	] ;");
         writer.newLine();
     }
 
     private String getCellValue (String column, int row) throws XLWrapException, XLWrapEOFException{
         //String sheetName = null; //null is first (0) sheet.
         int col = Utils.alphaToIndex(column);
         int actualRow = row - 1;
         //CellRange cellRange = new CellRange("File:" +xlsPath, sheetName, col, actualRow);
         //Cell cell = context.getCell(cellRange);
         Cell cell = metaSheet.getCell(col, actualRow);
         XLExprValue<?> value = Utils.getXLExprValue(cell);
         if (value == null){
             return null;
         }
         //remove the quotes that get added and we don't want here.
         return value.toString().replace("\"","");
     }
 
     private void writeURI(BufferedWriter writer, String type, String feild, String idType, String dataColumn) throws IOException, XLWrapMapException {
        System.out.println(feild + " " + idType + " " + dataColumn);
         if (feild.toLowerCase().equals("id")){
             writer.write("[ xl:uri \"ID_URI('" + RDF_BASE_URL + type + "/" + doi + "/" + sheetInURI + "', " +
                     dataColumn + firstData + ")\"^^xl:Expr ] ");
             return;
         } 
         if(feild.toLowerCase().equals("value")) {
             writer.write("[ xl:uri \"CELL_URI('" + RDF_BASE_URL + type + "/" + doi + "/" + sheetInURI + "', " +
                 dataColumn + firstData + ")\"^^xl:Expr ] ");
             return;
         } 
         if ((idType == null) || (idType.isEmpty())){
             throw new XLWrapMapException("Column " + dataColumn + " in Sheet " + metaSheet.getName() +
                     " has an unexpected emtpy IDType");
         }
         if (idType.equalsIgnoreCase("n/a")) {
             throw new XLWrapMapException("Column " + dataColumn + " in Sheet " + metaSheet.getName() +
                     " has an unexpected n/a IDType");
         }
         if (idType == null){
             System.out.println("Skippig column " + dataColumn + " as no idType provided");
         } else if (idType.equalsIgnoreCase("ROW")){
             writer.write("[ xl:uri \"ROW_URI('" + RDF_BASE_URL + type + "/" + doi + "/" + sheetInURI + "row', " +
                     dataColumn + firstData + ")\"^^xl:Expr ] ");
         } else {
             writer.write("[ xl:uri \"OTHER_ID_URI('" + RDF_BASE_URL + type + "/" + doi + "/" + sheetInURI + "', " +
                 idType + firstData + "," + dataColumn + firstData + ")\"^^xl:Expr ] ");
         }
     }
 
     private void writeLink (BufferedWriter writer, String metaColumn, String dataColumn, int row)
             throws XLWrapException, XLWrapEOFException, IOException, XLWrapMapException{
         String feild = getCellValue ("A", row);
         if (feild == null){
             return;
         }
         String link = getCellValue (metaColumn, row);
         System.out.println(metaColumn + "\t" + feild + "\t" + link);
         if (link == null){
             System.out.println("Skippig column " + metaColumn + " " + feild + "row as it is blank");
             return;
         }
         if (link.equalsIgnoreCase("n/a")){
             return;
         }
         writer.write("	vocab:has" + feild + "\t");
         writeURI(writer, feild, feild, link, dataColumn);
         writer.write(" ;");
         writer.newLine();
     }
 
     private void writeConstant (BufferedWriter writer, String metaColumn, int row)
             throws XLWrapException, XLWrapEOFException, IOException{
         String feild = getCellValue ("A", row);
         if (feild == null){
             return;
         }
         String value = getCellValue (metaColumn, row);
         System.out.println(metaColumn + "\t" + feild + "\t" + value);
         if (value == null){
             System.out.println("Skippig column " + metaColumn + " " + feild + "row as it is blank");
             return;
         }
         if (value.equalsIgnoreCase("n/a")){
             return;
         }
         writer.write("	vocab:has" + feild + "\t");
         writer.write("[ xl:uri \"'" + RDF_BASE_URL + "resource/' & URLENCODE('" + value + "')\"^^xl:Expr ] ;");
         //writer.write ("\"" + value + "\" ;");
         writer.newLine();
     }
 
     private void writeTemplateColumn(BufferedWriter writer, String metaColumn, String dataColumn)
             throws IOException, XLWrapException, XLWrapEOFException, XLWrapMapException{
         String category = getCellValue (metaColumn, CATEGORY_ROW);
         if (category == null){
             System.out.println("Skippig column " + metaColumn + " as no Category provided");
             return;
         }
         String field = getCellValue (metaColumn, FIELD_ROW);
         if (field == null){
             System.out.println("Skippig column " + metaColumn + " as no Feild provided");
             return;
         }
         String idType = getCellValue (metaColumn, ID_TYPE_ROW);
        writeURI(writer, category, field, idType, metaColumn);
         writer.write (" a ex:");
         writer.write (category);
         writer.write (" ;");
         writer.newLine();
         writer.write("	vocab:has" + field + "\t\"" + dataColumn + firstData + "\"^^xl:Expr ;");
         writer.newLine();
         for (int row = firstLink; row <= lastLink; row++){
             writeLink(writer, metaColumn, dataColumn, row);
         }
         for (int row = firstConstant; row <= lastConstant; row++){
             writeConstant(writer, metaColumn, row);
         }
 
         writer.write("	rdf:type [ xl:uri \"'" + RDF_BASE_URL + "resource/" + doi + category+ "'\"^^xl:Expr ] ;");
         writer.newLine();
 
         writer.write(".");
         writer.newLine();
         writer.newLine();
     }
 
     protected void writeTemplate(BufferedWriter writer) throws IOException, XLWrapException, XLWrapEOFException, XLWrapMapException{
         writer.write(":");
         writer.write(template());
         writer.write(" {");
         writer.newLine();
         for (char meta = 'B'; meta < 'Y'; meta++){
             int charValue = Character.valueOf(meta);
            String data = String.valueOf( (char) (charValue + 1));
             System.out.println(meta + "  " + data);
             writeTemplateColumn(writer, "" + meta, data);
         }
         writer.write("}");
         writer.newLine();
     }
 
   /*  public void writeMap(String mapFileName) throws IOException, XLWrapMapException, XLWrapException, XLWrapEOFException{
         File mapFile = new File(MAP_FILE_ROOT + mapFileName);
         BufferedWriter mapWriter = new BufferedWriter(new FileWriter(mapFile));
         writePrefix(mapWriter);
         writeMapping(mapWriter);
         writeTemplate(mapWriter);
         mapWriter.close();
         System.out.println("Done writing map file");
     }
 
     public void runMap(String mapFileName, String rdfFileName) throws XLWrapException, IOException{
         XLWrapMapping map = MappingParser.parse(MAP_FILE_ROOT + mapFileName);
 
         XLWrapMaterializer mat = new XLWrapMaterializer();
         Model m = mat.generateModel(map);
         m.setNsPrefix("ex", RDF_BASE_URL);
 
         File out = new File (RDF_FILE_ROOT + rdfFileName);
         FileWriter writer = new FileWriter(out);
                 //"RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3"
         //m.write(writer, "RDF/XML", RDF_BASE_URL);
         m.write(writer, "RDF/XML");
         System.out.println("Done writing rdf file to "+ out.getAbsolutePath());
     }
 */
 }
