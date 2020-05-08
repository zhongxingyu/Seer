 package gui.reports;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import model.reports.IPrintObject;


 public class TestPrintObject implements IPrintObject {
     private ArrayList<TestPrintObjectHeader> headers;
     private ArrayList<TestPrintObjectTextBlock> textBlocks;
     private ArrayList<TestPrintObjectTable> tables;
 
     public TestPrintObject(){
         headers = new ArrayList<TestPrintObjectHeader>();
         textBlocks = new ArrayList<TestPrintObjectTextBlock>();
         tables = new ArrayList<TestPrintObjectTable>();
     }
 
     public IPrintObjectHeader getHeader(int position) {
         return headers.get(position);
     }
 
     public void addHeader(TestPrintObjectHeader header){
         headers.add(header);
     }
 
     public ArrayList<IPrintObjectHeader> getHeaders() {
         return new ArrayList<IPrintObjectHeader>(headers);
     }
 
     public IPrintObjectTextBlock getTextBlock(int position) {
         return textBlocks.get(position);
     }
 
     public void addTextBlock(TestPrintObjectTextBlock textBlock){
         textBlocks.add(textBlock);
     }
 
     public ArrayList<IPrintObjectTextBlock> getTextBlocks() {
         return new ArrayList<IPrintObjectTextBlock>(textBlocks);
     }
 
     public IPrintObjectTable getTable(int position) {
         return tables.get(position);
     }
 
     public void addTable(TestPrintObjectTable table){
         tables.add(table);
     }
 
     public ArrayList<IPrintObjectTable> getTables() {
         return new ArrayList<IPrintObjectTable>(tables);
     }
 
     public class TestPrintObjectHeader implements IPrintObjectHeader {
         String contents;
 
         public TestPrintObjectHeader(String header){
             contents = header;
         }
 
         @Override
         public String toString(){
             return contents;
         }
     }
 
     public class TestPrintObjectTextBlock implements IPrintObjectTextBlock {
         String contents;
 
         public TestPrintObjectTextBlock(String textBlock){
             contents = textBlock;
         }
 
         @Override
         public String toString(){
             return contents;
         }
     }
 
     public class TestPrintObjectTable implements IPrintObjectTable {
         HashMap<Integer,HashMap<Integer,String>> contents;
 
         public TestPrintObjectTable(){
             contents = new HashMap<Integer, HashMap<Integer, String>>();
         }
 
         public void setCell(int x, int y, String cell){
             if(contents.containsKey(x))
                 contents.get(x).put(y,cell);
             else{
                 contents.put(x, new HashMap<Integer, String>());
                 contents.get(x).put(y,cell);
             }
         }
 
         @Override
         public String getCell(int x, int y) {
             return contents.get(x).get(y);
         }

		@Override
		public void addRow(String[] row) {
			// TODO Auto-generated method stub
			
		}
     }
 }
