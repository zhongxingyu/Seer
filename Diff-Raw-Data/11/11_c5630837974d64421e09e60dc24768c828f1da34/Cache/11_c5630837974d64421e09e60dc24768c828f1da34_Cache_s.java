 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 //TODO: ARRAYLIST KIIRTASA (csak pointernel maradhat) +KOMPAKTÁLÁSKOR CACHE INVALIDÁLÁSA + ALGORITMUSOK
 
 package memsim_java;
 import cache.algorithm.*;
 /**
  *
  * @author zalatnaicsongor
  */
 public class Cache {
     private int rowSize;
     private int numRows;
     private int associativity;
     private static Cache instance;
 
     private int lineLength;
     private int tagLength;
     private int displacementLength;
 
     private CacheWriteStrategy writeStrategy;
     private CacheRowDiscardStrategy rowDiscardStrategy;
 
     private CacheLine[] lines;
 
    public CacheRow getMinUsageCountCacheRow() {
        CacheRow retval = null;
    }

 
     public void destroyAll() {
         for (CacheLine cr: lines) {
             cr.destroyAll();
         }
     }
 
     public void destroyByAddress(int address) {
         int line = this.genLine(address);
         int tag = this.genTag(address);
         try {
             CacheLine cLine = this.getLine(line);
             CacheRow cRow = cLine.getRowByTag(tag);
             cRow.discard();
         } catch (CacheRowNotFoundException e) {
             System.out.println("Nem is volt bent a cache-ben, nem kell kidobni");
         }
     }
 
     public CacheLine getLine(int line) {
         return lines[line];
     }
 
     public CacheRowDiscardStrategy getRowDiscardStrategy() {
         return rowDiscardStrategy;
     }
 
     public void setRowDiscardStrategy(CacheRowDiscardStrategy rowDiscardStrategy) {
         this.rowDiscardStrategy = rowDiscardStrategy;
     }
 
     public CacheWriteStrategy getWriteStrategy() {
         return writeStrategy;
     }
 
     public void setWriteStrategy(CacheWriteStrategy writeStrategy) {
         this.writeStrategy = writeStrategy;
     }
 
     public int getAssociativity() {
         return associativity;
     }
 
     public int getNumRows() {
         return numRows;
     }
 
     public int getRowSize() {
         return rowSize;
     }
 
     public int getSize() {
         return numRows * rowSize * associativity;
     }
 
     public int genLine(int address) {
         int remainLength = Memory.ADDRESSLENGTH - this.tagLength - this.lineLength;
         int mask = (int)(Math.pow(2, this.lineLength) - 1) << remainLength;
         return ((mask & address) >>> remainLength);
     }
 
     public int genTag(int address) {
         int remainLength = Memory.ADDRESSLENGTH - this.tagLength;
         int mask = (int)(Math.pow(2, this.tagLength) - 1) << remainLength;
         return ((mask & address) >>> remainLength);
     }
 
     public int genDisplacement(int address) {
         int mask = (int)(Math.pow(2, this.displacementLength) - 1);
         return (mask & address);
     }
 
     public int genAddress(int tag, int line, int displacement) {
         int retval = 0;
         retval = retval | displacement;
         retval |= line << this.displacementLength;
         retval |= tag << (this.displacementLength + this.lineLength);
         return retval;
     }
 
     public int readByte(int address) {
         int line = Cache.getInstance().genLine(address);
         int tag = Cache.getInstance().genTag(address);
         int displacement = Cache.getInstance().genDisplacement(address);
 
         CacheRow row = null;
 
         try {
             row = Cache.getInstance().getLine(line).getRowByTag(tag);
         } catch (CacheRowNotFoundException e) {
             System.out.println(e);
             row = Cache.getInstance().getLine(line).createRow(tag);
         }
         return row.readByte(displacement);
     }
     public void writeByte(int address, int data) {
         this.getWriteStrategy().writeByte(address, data);
     }
 
     public static int logKetto(int a) {
         int r = 0;
         while (true) {
             a = a >>> 1;
             if (a<=0) {
                 break;
             }
             r++;
         }
         return r;
     }
 
     protected Cache(int rowSize, int numRows, int associativity) throws Exception {
         if ((rowSize & (rowSize - 1)) != 0 || rowSize <= 0) {
             throw new Exception("rowSize nem kettohatvany, vagy 0, vagy negativ");
         }
         if ((numRows & (numRows - 1)) != 0 || numRows <= 0) {
             throw new Exception("numRows nem kettohatvany, vagy 0, vagy negativ");
         }
         if ((associativity & (associativity - 1)) != 0 || associativity <= 0) {
             throw new Exception("associativity nem kettohatvany, vagy 0, vagy negativ");
         }
         this.rowSize = rowSize;
         this.numRows = numRows;
         this.associativity = associativity;
         this.lineLength = Cache.logKetto(numRows);
         this.displacementLength = Cache.logKetto(rowSize);
         this.tagLength = Memory.ADDRESSLENGTH - this.lineLength - this.displacementLength;
         if (this.tagLength <= 0) {
             throw new Exception("itt valami rossz történt");
         }
         this.lines = new CacheLine[numRows];
         for (int i = 0; i < numRows; i++) {
             this.lines[i] = new CacheLine(i, associativity);
         }
     }
 
     public static void create(int rowSize, int numRows, int associativity) throws Exception {
         try {
             Cache.instance = new Cache(rowSize, numRows, associativity);
         } catch (Exception e) {
             throw new Exception(e);
         }
     }
     public static Cache getInstance() {
         return Cache.instance;
     }
 
 }
