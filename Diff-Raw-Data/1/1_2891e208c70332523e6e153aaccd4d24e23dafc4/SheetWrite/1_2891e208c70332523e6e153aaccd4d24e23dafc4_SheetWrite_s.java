 package org.freshwaterlife.fishlink.xlwrap;
 
 import org.freshwaterlife.fishlink.FishLinkException;
 import at.jku.xlwrap.spreadsheet.Sheet;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.freshwaterlife.fishlink.FishLinkConstants;
 import org.freshwaterlife.fishlink.FishLinkUtils;
 import org.freshwaterlife.fishlink.ZeroNullType;
 
 /**
  *
  * @author Christian
  */
 public class SheetWrite extends AbstractSheet{
 
     private int sheetNumber;
 
     private String dataPath;
     private String pid;
 
     private NameChecker masterNameChecker;
 
     private HashMap<String,String> idColumns;
     private HashMap<String,String> categoryUris;
     private ArrayList<String> allColumns;
 
     SheetWrite (NameChecker nameChecker, Sheet annotatedSheet, int sheetNumber, String url, String pid)
             throws FishLinkException{
         super(annotatedSheet);
         this.pid = pid;
         this.sheetNumber = sheetNumber;
         this.dataPath = url;
         this.masterNameChecker = nameChecker;
         idColumns = new HashMap<String,String>();
         categoryUris = new HashMap<String,String>();
         allColumns = new ArrayList<String>();
     }
 
     String getSheetInfo(){
         return sheet.getSheetInfo();
     }
 
     private String template(){
         return sheet.getName() + "template";
     }
 
     void writeMapping(BufferedWriter writer) throws FishLinkException{
         try {
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
             writer.write("				xl:restriction \"B" + firstData + ":" + lastDataColumn + firstData + "\" ;");
             writer.newLine();
             writer.write("				xl:steps \"1\" ;");
             writer.newLine();
             writer.write("			] ;");
             writer.newLine();
             writer.write("		]");
             writer.newLine();
             writer.write("	] ;");
             writer.newLine();
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write mapping", ex);
         }
     }
 
     private void writeUri(BufferedWriter writer, String category, String field, String idType,
             String dataColumn, ZeroNullType zeroVsNulls) throws FishLinkException {
         if(category.equalsIgnoreCase(FishLinkConstants.OBSERVATION_LABEL)) {
             writeObservationUril(writer, field, idType, dataColumn, zeroVsNulls);
         } else if (idType == null || idType.isEmpty()  || idType.equalsIgnoreCase("n/a") ||
                 idType.equalsIgnoreCase(FishLinkConstants.AUTOMATIC_LABEL)){
             writeUri(writer, category, field, dataColumn, zeroVsNulls);
         } else {
             //There is an Id reference to another dataColumn.
             if (idType.equalsIgnoreCase("row")){
                 throw new FishLinkException("IDType row not longer supported. Found in sheet " + sheet.getSheetInfo());
             } else if (idType.equalsIgnoreCase(FishLinkConstants.ALL_LABEL)){
                 throw new FishLinkException("Unexpected IDType " + FishLinkConstants.ALL_LABEL);
             } else {
                 String idCategory = this.getCellValue(idType, categoryRow);
                 String idColumn = idColumns.get(idCategory);
                 writeUri(writer, idCategory, field, idColumn, zeroVsNulls);
             }
         }
     }
     
     private void writeUri(BufferedWriter writer, String category, String field, String dataColumn, 
             ZeroNullType dataZeroVsNulls) throws FishLinkException {
         String uri = categoryUris.get(category);
         if (field.toLowerCase().equals(FishLinkConstants.ID_LABEL)){
             try {
                 writer.write("[ xl:uri \"ID_URI('" + uri + "', " + dataColumn + firstData + ",'"
                         + dataZeroVsNulls + "')\"^^xl:Expr ] ");
             } catch (IOException ex) {
                 throw new FishLinkException("Unable to write uri", ex);
             }
             return;
         }
         writeUriOther(writer, uri, category, dataColumn, dataZeroVsNulls);
     }
 
     private void writeObservationUril(BufferedWriter writer, String field, String idColumn, 
             String dataColumn, ZeroNullType dataZeroNull) throws FishLinkException {
         String uri = categoryUris.get(FishLinkConstants.OBSERVATION_LABEL);
         if(field.equalsIgnoreCase(FishLinkConstants.VALUE_LABEL)) {
             try {
                 writer.write("[ xl:uri \"CELL_URI('" + uri + "', " + dataColumn + firstData + ",'"
                         + dataZeroNull + "')\"^^xl:Expr ] ");
             } catch (IOException ex) {
                 throw new FishLinkException("Unable to write uri", ex);
             }                
             return;
         }
         if (idColumn == null || idColumn.isEmpty()){
             throw new FishLinkException(sheet.getSheetInfo() + " Data Column " + dataColumn + " with category " +
                     FishLinkConstants.OBSERVATION_LABEL + " and field " + field + " needs an id Type");
         }
         String idNullZeroString = getCellValue (idColumn, idTypeRow);
         ZeroNullType idZeroNull = ZeroNullType.parse(idNullZeroString);
         try {
             writer.write("[ xl:uri \"CELL_URI('" + uri + "', " + idColumn + firstData + ", '" + idZeroNull + "' ,"
                 + dataColumn + firstData + ",'" + dataZeroNull + "')\"^^xl:Expr ] ");
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write uri", ex);
         }
     }
 
     private void writeUriOther(BufferedWriter writer, String uri, String category, 
             String dataColumn, ZeroNullType dataZeroVsNulls) throws FishLinkException {
         //"row" is added for automatic ones where no id column found.
         String idColumn = idColumns.get(category);
         try {
             if (idColumn.equalsIgnoreCase("row")){
                 writer.write("[ xl:uri \"ROW_URI('" + uri + "', " + dataColumn + firstData + ",'" +
                         dataZeroVsNulls + "')\"^^xl:Expr ] ");
             } else {
                 String zeroNullString = getCellValue (idColumn, ZeroNullRow);
                 ZeroNullType idZeroNull = ZeroNullType.parse(zeroNullString);
                 writer.write("[ xl:uri \"ID_URI('" + uri + "', " + idColumn + firstData + ",'" + idZeroNull + "'," + 
                         dataColumn + firstData + ",'" + dataZeroVsNulls + "')\"^^xl:Expr ] ");
             }
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write uri", ex);
         }
     }
 
     private void writeVocab (BufferedWriter writer, String vocab) throws FishLinkException {
         try {
             if (vocab.startsWith("is") || vocab.startsWith("has")){
                 writer.write("	vocab:" + vocab + "\t");
             } else {
                 writer.write("	vocab:has" + vocab + "\t");
             }
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write vocab", ex);
         }            
     }
 
     private void writeRdfType (BufferedWriter writer, String type) throws FishLinkException {
         try {
            writer.write ("	rdf:type");
            writeType(writer, type);
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write vocab", ex);
         }            
     }
 
     private void writeType (BufferedWriter writer, String type) throws FishLinkException {
         try {
            writer.write (" type:" + type);
            writer.write (" ;");
            writer.newLine();
         } catch (IOException ex) {
             throw new FishLinkException("Unable to write vocab", ex);
         }            
     }
 
     private void writeConstant (BufferedWriter writer, String category, String column, int row) 
             throws FishLinkException{
         String field = getCellValue ("A", row);
         if (field == null){
             return;
         }
         String value = getCellValue (column, row);
         if (value == null){
             return;
         }
         if (value.equalsIgnoreCase("n/a")){
             return;
         }
         if (FishLinkConstants.isRdfTypeField(field)){
             masterNameChecker.checkSubType(sheet.getSheetInfo(), category, value);
             writeRdfType(writer, value);
         } else {
             masterNameChecker.checkConstant(sheet.getSheetInfo(), field, value);
             writeVocab (writer, field);
             if (!value.startsWith("'")){
                 value = "'" + value ;
             }
             if (!value.endsWith("'")){
                 value = value + "'";
             }
             try {
                 writer.write("[ xl:uri \"'" + FishLinkConstants.RDF_BASE_URL + "constant/' & URLENCODE(" + value + ")\"^^xl:Expr ] ;");
                 writer.newLine();
             } catch (IOException ex) {
                 throw new FishLinkException("Unable to write constant", ex);
             }            
         }
     }
 
     private ZeroNullType getZeroVsNull (String column) throws FishLinkException {
         if (ZeroNullRow >= 0){
             String ignoreZeroString = getCellValue (column, ZeroNullRow);
             if (ignoreZeroString == null){
                 return ZeroNullType.KEEP;
             } else if (ignoreZeroString.isEmpty()){
                 return ZeroNullType.KEEP;
             } return ZeroNullType.parse(ignoreZeroString);
         } else{
             return ZeroNullType.KEEP;
         }
     }
 
     private String refersToCategory(String subjectCategory, String field) throws FishLinkException{
        if ( masterNameChecker.isCategory(field)) {
            System.out.println("£££");
            return field;
        }
        if (FishLinkConstants.refersToSameCategery(field)){
            return subjectCategory;
        }
        return FishLinkConstants.refersToCategory(field);
     }
 
     private void writeData(BufferedWriter writer, String subjectCategory, String column, ZeroNullType zeroNull)
             throws FishLinkException {
         String field = getCellValue (column, fieldRow);
         writeVocab(writer, field);
         String category = refersToCategory(subjectCategory, field);
         System.out.println(field + " = " + category);
         try {
             if (category  == null) {
                 switch (zeroNull){
                     case KEEP:
                         writer.write("\"" + column + firstData + "\"^^xl:Expr ;");
                         break;
                     case ZEROS_AS_NULLS:   
                         writer.write("\"ZERO_AS_NULL(" + column + firstData + ")\"^^xl:Expr ;");
                         break;
                     case NULLS_AS_ZERO:   
                         writer.write("\"NULL_AS_ZERO(" + column + firstData + ")\"^^xl:Expr ;");
                         break;
                     default:
                         throw new FishLinkException("Unexpected ZeroNullType " + zeroNull);
                 }
             } else {
                 String uri = getUri(column, category);
                 writer.write("[ xl:uri \"ID_URI('" + uri + "'," + column + firstData + ", '" + zeroNull + 
                         "')\"^^xl:Expr ];");
             }
             writer.newLine();
             }  catch (IOException ex) {
                 throw new FishLinkException("Unable to write data", ex);
             }            
     }
 
     private void writeAutoRelated(BufferedWriter writer, String category, String column, ZeroNullType dataZeroVsNulls)
             throws FishLinkException {
         String related = FishLinkConstants.autoRelatedCategory(category);
         if (related == null){
             return;
         }
         String uri = categoryUris.get(related);
         if (uri == null){
             return;
         }
         writeVocab(writer, related);
         writeUriOther(writer, uri, category, column, dataZeroVsNulls);
         try {
             writer.write(";");
             writer.newLine();
         }  catch (IOException ex) {
             throw new FishLinkException("Unable to write ", ex);
         }            
     }
 
     private void writeAllRelated(BufferedWriter writer, String category, ZeroNullType zeroVsNulls)
             throws FishLinkException {
         if (!category.equalsIgnoreCase(FishLinkConstants.OBSERVATION_LABEL)){
             return;
         }
         String uri = categoryUris.get(category);
         for (String column : allColumns){
             String idNullZeroString = getCellValue (column, ZeroNullRow);
             ZeroNullType idZeroNull = ZeroNullType.parse(idNullZeroString);
             writeData(writer, category, column, idZeroNull);
         }
     }
 
     private boolean writeTemplateColumn(BufferedWriter writer, String column)
             throws FishLinkException{
         String category = getCellValue (column, categoryRow);
         //ystem.out.println(category + " " + metaColumn + " " + categoryRow);
         String field = getCellValue (column, fieldRow);
         String idType = getCellValue (column, idTypeRow);
         String external = getExternal(column);
         if (category == null || category.toLowerCase().equals("undefined")) {
             FishLinkUtils.report("Skippig column " + column + " as no Category provided");
         }
         if (field == null){
             FishLinkUtils.report("Skippig column " + column + " as no Feild provided");
             return false;
         }
         masterNameChecker.checkName(sheet.getSheetInfo(), category, field);
         if (field.equalsIgnoreCase("id") && !external.isEmpty()){
             FishLinkUtils.report("Skipping column " + column + " as it is an external id");
             return false;
         }
         if (idType != null && idType.equals(FishLinkConstants.ALL_LABEL)){
             FishLinkUtils.report("Skipping column " + column + " as it is an all column.");
             return false;
         }
        
         ZeroNullType zeroNull =getZeroVsNull(column);
         writeUri(writer, category, field, idType, column, zeroNull);
         try {
             writer.write (" a ");
             writeType (writer, category);
         }  catch (IOException ex) {
             throw new FishLinkException("Unable to write a type", ex);
         }            
         writeData(writer, category, column, zeroNull);
         writeAutoRelated(writer, category, column, zeroNull);
         writeAllRelated(writer, category, zeroNull);
         for (int row = firstConstant; row <= lastConstant; row++){
             writeConstant(writer,  category, column, row);
         }
 
         try {
             if (external.isEmpty()){
                writeRdfType (writer, pid + "_" + sheet.getName() + "_" + category);
              }
 
             writer.write(".");
             writer.newLine();
             writer.newLine();
         }  catch (IOException ex) {
             throw new FishLinkException("Unable to other type ", ex);
         }            
         return true;
     }
 
     private String getExternal(String metaColumn) throws FishLinkException {
         if (externalSheetRow < 1){
             return "";
         }
         String externalFeild = getCellValue (metaColumn, externalSheetRow);
         if (externalFeild == null){
             return "";
         }
         return externalFeild;
    }
 
    private String getCatgerogyUri(String category){
        return  FishLinkConstants.RDF_BASE_URL + "resource/" + category + "_" + pid + "_" + sheet.getName() + "/";
    }
 
    private String getUri(String metaColumn , String category) throws FishLinkException {
         String externalField = getExternal(metaColumn);
         if (externalField.isEmpty()){
             return getCatgerogyUri(category);
         }
         String externalPid;
         String externalSheet;
         if (externalField.startsWith("[")){
             externalPid = externalField.substring(1, externalField.indexOf(']'));
             externalSheet = externalField.substring( externalField.indexOf(']')+1);
         } else {
             externalPid = pid;
             externalSheet = externalField;
         }
         return  FishLinkConstants.RDF_BASE_URL + "resource/" + category + "_" + pid + "_" + externalSheet + "/";
     }
 
    private void findId(String category, String column) throws FishLinkException{
         String field = getCellValue (column, fieldRow);
         String id = idColumns.get(category);
         if (field.equalsIgnoreCase("id")){
             if (id == null || id.equalsIgnoreCase("row")){
                 System.out.println (column + " " +category + "    " + column);
                 idColumns.put(category, column);
                 categoryUris.put(category, getUri(column, category));
             } else {
                 throw new FishLinkException("Found two different id columns of type " + category);
             }
         } else {
             if (id == null){
                 System.out.println (column + " " + category + "    row");
                 idColumns.put(category, "row");
             }//else leave the column or "row" already there.
             if (categoryUris.get(category) == null){
                 categoryUris.put(category, getCatgerogyUri(category));
             }
         }
     }
 
    private void findAll(String category, String metaColumn) throws FishLinkException{
         String idColumn = getCellValue (metaColumn, idTypeRow);
         if (idColumn == null || !idColumn.equalsIgnoreCase("all")){
             return;
         }
         if (category.equalsIgnoreCase(FishLinkConstants.OBSERVATION_LABEL)){
             String field = getCellValue (metaColumn, fieldRow);
             if (field == null || field.isEmpty()){
                 throw new FishLinkException ("All id.Value Column " + metaColumn + " missing a field value");
             }
             allColumns.add(metaColumn);
         } else {
             throw new FishLinkException ("All id.Value Column only supported for Categeroy " +
                     FishLinkConstants.OBSERVATION_LABEL);
         }
     }
 
    private void findIds() throws FishLinkException{
         int maxColumn = FishLinkUtils.alphaToIndex(lastDataColumn);
         for (int i = 1; i < maxColumn; i++){
             String metaColumn = FishLinkUtils.indexToAlpha(i);
             String category = getCellValue (metaColumn, categoryRow);
             if (category == null || category.isEmpty()){
                 //do nothing
             } else {
                 findId(category, metaColumn);
                 findAll(category, metaColumn);
             }
         }
      }
 
     void writeTemplate(BufferedWriter writer) throws FishLinkException{
         FishLinkUtils.report("Writing template for "+sheet.getSheetInfo());
         findIds();
         try {
             writer.write(":");
             writer.write(template());
             writer.write(" {");
             writer.newLine();
         }  catch (IOException ex) {
             throw new FishLinkException("Unable to write template ", ex);
         }            
         int maxColumn = FishLinkUtils.alphaToIndex(lastDataColumn);
         boolean foundColumn = false;
         //Start at 1 to ignore label column;
         for (int i = 1; i < maxColumn; i++){
             String column = FishLinkUtils.indexToAlpha(i);
             if (writeTemplateColumn(writer, column)){
                 foundColumn = true;
             }
         }
         if (!foundColumn){
             throw new FishLinkException("No mappable columns found in sheet " +  sheet.getSheetInfo());
         }
         try {
             writer.write("}");
             writer.newLine();
         }  catch (IOException ex) {
             throw new FishLinkException("Unable to write template end ", ex);
         }            
     }
 
 }
 
 
