 package webelement;
 
 import helper.WebDriverHelper;
 
 import java.util.*;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 public class HtmlTable {
     // DTD states for table (w3.org)
     // <!ELEMENT table (caption?, (col*|colgroup*), thead?, tfoot?, (tbody+|tr+))>
 
     private WebElement caption;
     private List<WebElement> colGroups;
 
     private WebElement table;
     private WebElement tableHeader;
     private List<WebElement> tableBodies;
     private WebElement tableFooter;
     private List<WebElement> allTableRows;
     private WebDriverHelper driverHelper = WebDriverHelper.getInstance();
 
     public HtmlTable(WebElement webElement) {
         this.table = webElement;
         initialiseTableValues();
     }
 
     public HtmlTable(By by) {
         this.table = driverHelper.findElement(by);
         initialiseTableValues();
     }
 
     private void initialiseTableValues() {
         if(driverHelper.doesWebElementExist(this.table, By.cssSelector("thead"))) {
             this.tableHeader = this.table.findElement(By.cssSelector("thead"));
         }
         if(driverHelper.doesWebElementExist(this.table, By.cssSelector("tfoot"))) {
             this.tableFooter = this.table.findElement(By.cssSelector("tfoot"));
         }
         if(driverHelper.doesWebElementExist(this.table, By.cssSelector("caption"))) {
             this.caption = this.table.findElement(By.cssSelector("caption"));
         }
         if(driverHelper.doesWebElementExist(this.table, By.cssSelector("colgroup"))) {
             this.colGroups = this.table.findElements(By.cssSelector("colgroup"));
         }
         this.tableBodies = this.table.findElements(By.cssSelector("tbody"));
         this.allTableRows = this.table.findElements(By.cssSelector("tr"));
     }
 
     public WebElement getTableHeader() {
         return this.tableHeader;
     }
 
     public List<WebElement> getTableHeaderColumns() {
         return this.tableHeader.findElements(By.cssSelector("th"));
     }
 
     public List<WebElement> getAllTableBodies() {
         return this.tableBodies;
     }
 
     public WebElement getTableBody(int index) {
         return this.tableBodies.get(index);
     }
 
     public List<WebElement> getTableBodyRows(int index) {
         return this.tableBodies.get(index).findElements(By.cssSelector("tr"));
     }
 
     public List<WebElement> getAllTableBodyRows() {
         List<WebElement> tableBodyRows = new ArrayList<WebElement>();
         for(WebElement tableBody : tableBodies) {
             tableBodyRows.addAll(tableBody.findElements(By.cssSelector("tr")));
         }
         return tableBodyRows;
     }
 
     public List<List<WebElement>> getTableBodyColumns(int index) {
         List<List<WebElement>> tableColumns = new ArrayList<List<WebElement>>();
         List<WebElement> tableRows = getTableBodyRows(index);
         return getTableColumns(tableColumns, tableRows);
     }
 
     public List<WebElement> getAllTableRows() {
         return this.allTableRows;
     }
 
     public List<List<WebElement>> getAllTableBodyColumns() {
         List<List<WebElement>> allTableColumns = new ArrayList<List<WebElement>>();
         List<WebElement> tableRows = getAllTableRows();
         return getTableColumns(allTableColumns, tableRows);
     }
 
     private List<List<WebElement>> getTableColumns(List<List<WebElement>> tableColumns, List<WebElement> tableRows) {
         for(WebElement tableRow : tableRows) {
             List<WebElement> columns = tableRow.findElements(By.cssSelector("td"));
             tableColumns.add(columns);
         }
         return tableColumns;
     }
 
     public WebElement getTableFooter() {
         return this.tableFooter;
     }
 
     public List<WebElement> getTableFooterColumns() {
        return this.tableFooter.findElements(By.cssSelector("td"));
     }
 
     public WebElement getCaption() {
         return caption;
     }
 
     public WebElement getTable() {
         return table;
     }
 
     public List<WebElement> getTableBodies() {
         return tableBodies;
     }
 
     public List<WebElement> getColGroups() {
         return colGroups;
     }
 
     public List<Map<String, WebElement>> getTableBodyColumnWebElementValues() {
         List<List<WebElement>> allColumns = getAllTableBodyColumns();
         List<WebElement> tableHeaders = null;
         if(tableHeader != null) {
             tableHeaders = getTableHeaderColumns();
         }
         return getColumnWebElementValues(allColumns, tableHeaders);
     }
 
     public List<Map<String, String>> getTableBodyColumnTextValues() {
         List<List<WebElement>> allColumns = getAllTableBodyColumns();
         List<WebElement> tableHeaders = null;
         if(tableHeader != null) {
              tableHeaders = getTableHeaderColumns();
         }
         return getColumnTextValues(allColumns, tableHeaders);
     }
 
     private List<Map<String, WebElement>> getColumnWebElementValues(List<List<WebElement>> allColumns, List<WebElement> tableHeaders) {
         List<Map<String, WebElement>> webElementValues = new ArrayList<Map<String, WebElement>>();
         for(List<WebElement> columns : allColumns) {
             Map<String, WebElement> columnElement = new LinkedHashMap<String, WebElement>();
             putColumnElement(tableHeaders, columns, columnElement);
             webElementValues.add(columnElement);
         }
         return webElementValues;
     }
 
     private List<Map<String, String>> getColumnTextValues(List<List<WebElement>> allColumns, List<WebElement> tableHeaders) {
         List<Map<String, String>> tableTextValues = new ArrayList<Map<String, String>>();
         for(List<WebElement> columns : allColumns) {
             Map<String, String> columnData = new HashMap<String, String>();
             putColumnData(tableHeaders, columns, columnData);
             tableTextValues.add(columnData);
         }
         return tableTextValues;
     }
 
     private void putColumnData(List<WebElement> tableHeaders, List<WebElement> columns, Map<String, String> columnData) {
         for(int i = 0; i < columns.size(); i++) {
             if(tableHeaders != null) {
                 columnData.put(tableHeaders.get(i).getText(), columns.get(i).getText());
             } else {
                 columnData.put("Header " + (i+1), columns.get(i).getText());
             }
         }
     }
 
     private void putColumnElement(List<WebElement> tableHeaders, List<WebElement> columns, Map<String, WebElement> columnData) {
         for(int i = 0; i < columns.size(); i++) {
             if(tableHeaders != null) {
                 columnData.put(tableHeaders.get(i).getText(), columns.get(i));
             } else {
                 columnData.put("Header " + (i+1), columns.get(i));
             }
         }
     }
 }
