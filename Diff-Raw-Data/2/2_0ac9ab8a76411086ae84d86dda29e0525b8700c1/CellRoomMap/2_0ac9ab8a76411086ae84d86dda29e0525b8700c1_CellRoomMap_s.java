 /**
  * 
  */
 package org.mklab.taskit.server.roommap.cell;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * 部屋の情報を保持するクラスです。
  * 
  * @author Yuhi Ishikura
  */
 public class CellRoomMap {
 
   private Cell[][] cells;
   private Map<String, Cell> userIdToCell = new HashMap<String, Cell>();
 
   /**
    * CSVファイルからmapfileを読み込みます。
    * 
    * @param file ファイル
    * @return マップ
    * @throws IOException 読み込めなかった場合
    */
   public static CellRoomMap load(File file) throws IOException {
     Reader reader = new InputStreamReader(new FileInputStream(file));
     try {
       return load(reader);
     } catch (IOException ex) {
       throw ex;
     } finally {
       reader.close();
     }
   }
 
   /**
    * CSVのマップファイルを読み込みます。
    * 
    * @param reader リーダー
    * @return マップ
    * @throws IOException 読み込めなかった場合
    */
   public static CellRoomMap load(Reader reader) throws IOException {
     List<List<Cell>> cellList = new ArrayList<List<Cell>>();
     int maximumColumnCount = 0;
 
     final BufferedReader br = new BufferedReader(reader);
     String line;
     while ((line = br.readLine()) != null) {
       line = line.replaceAll(",,", ", ,"); //$NON-NLS-1$ //$NON-NLS-2$
       final String[] s = line.split(","); //$NON-NLS-1$
       final List<Cell> cellsX = new ArrayList<Cell>(s.length);
      for (int i = 0; i < cellsX.size(); i++) {
         String cell = s[i].trim();
         if (cell.length() == 0) {
           cellsX.add(Cell.EMPTY_CELL);
         } else {
           cellsX.add(new Cell(cell));
         }
       }
       if (cellsX.size() > maximumColumnCount) maximumColumnCount = cellsX.size();
       cellList.add(cellsX);
     }
 
     final Cell[][] cells = new Cell[cellList.size()][maximumColumnCount];
     int i = 0;
     for (List<Cell> row : cellList) {
       while (row.size() < maximumColumnCount) {
         row.add(Cell.EMPTY_CELL);
       }
       cells[i++] = row.toArray(new Cell[row.size()]);
     }
     return new CellRoomMap(cells);
   }
 
   private CellRoomMap(Cell[][] cells) {
     this.cells = cells;
     for (Cell[] row : cells) {
       for (Cell cell : row) {
         this.userIdToCell.put(cell.getUserId(), cell);
       }
     }
   }
 
   /**
    * (x,y)のセルを取得します。
    * 
    * @param x x座標
    * @param y y座標
    * @return セル
    */
   public Cell getCell(int x, int y) {
     return this.cells[y][x];
   }
 
   /**
    * 与えられたユーザーのセルを取得します。
    * 
    * @param userId ユーザーID
    * @return セル。存在しなければnull
    */
   public Cell getCellFor(String userId) {
     return this.userIdToCell.get(userId);
   }
 
   /**
    * 横方向のセルの数を取得します。
    * 
    * @return 横方向のセルの数
    */
   public int getCellCountX() {
     return this.cells[0].length;
   }
 
   /**
    * 横方向のセルの数を取得します。
    * 
    * @return 横方向のセルの数
    */
   public int getCellCountY() {
     return this.cells.length;
   }
 
 }
