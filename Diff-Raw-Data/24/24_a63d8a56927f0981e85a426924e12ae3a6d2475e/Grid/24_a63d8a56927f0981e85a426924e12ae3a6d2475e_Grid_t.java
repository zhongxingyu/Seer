 package com.bogdan.sokoban.ai.model;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Bogdan
  */
 public class Grid {
 
     private int rowCount, columnCount;
     private Square[][] matrix;
 
     public Grid(File inputFile) {
         try {
             readGridFromFile(inputFile);
         } catch (FileNotFoundException exception) {
             System.err.println("Input file not found!");
         } catch (IOException exception) {
             System.err.println("Error reading input file!");
         }
     }
 
     private void readGridFromFile(File inputFile) throws IOException {
         BufferedReader bufferedReader =
                 new BufferedReader(new FileReader(inputFile));
         setCounts(bufferedReader);
         populateMatrix(bufferedReader);
     }
 
     private void setCounts(BufferedReader bufferedReader) throws IOException {
         String line = bufferedReader.readLine();
 
         String[] tokens = line.split(" ");
         if (tokens != null && tokens.length == 2) {
             rowCount = Integer.parseInt(tokens[0]);
             columnCount = Integer.parseInt(tokens[1]);
         }
 
         matrix = new Square[rowCount][columnCount];
     }
 
     private void populateMatrix(BufferedReader bufferedReader) throws IOException {
         String line;
         int index = 0;
         while((line = bufferedReader.readLine()) != null) {
             String[] tokens = line.split(" ");
             for (int i=0; i<tokens.length; i++) {
                 SquareType nodeType =
                         SquareType.getNodeTypeByCode(Integer.parseInt(tokens[i]));
                 Square node = new Square(nodeType, index, i);
                 matrix[index][i] = node;
             }
             index++;
         }
     }
 
     public Square get(int x, int y) {
         if (x < 0 || x >= rowCount || y < 0 || y >= columnCount) {
             return null;
         }
         return matrix[x][y];
     }
 
     public Square getSquareAt(Position position) {
         return get(position.getX(), position.getY());
     }
 
     public int getNeighborCount(Square square) {
         return getNeighbors(square).size();
     }
 
     public List<Square> getNeighbors(Square square) {
         List<Square> neighbors = new ArrayList<Square>();
         Position position = square.getPosition();
 
         Position northPosition = position.incrementX();
         if (isValidPosition(northPosition)) {
             neighbors.add(getSquareAt(northPosition));
         }
 
         Position westPosition = position.incrementY();
         if (isValidPosition(westPosition)) {
             neighbors.add(getSquareAt(westPosition));
         }
 
         Position southPosition = position.decrementX();
         if (isValidPosition(southPosition)) {
             neighbors.add(getSquareAt(southPosition));
         }
 
         Position eastPosition = position.decrementY();
         if (isValidPosition(eastPosition)) {
             neighbors.add(getSquareAt(eastPosition));
         }
 
         return neighbors;
     }
 
     private boolean isValidPosition(Position position) {
        return isValidXAxisPosition(position) &&
                isValidYAxisPosition(position);
    }
    
    private boolean isValidXAxisPosition(Position position) {
        return position.getX() >= 0 &&
                position.getX() < rowCount;
    }

    private boolean isValidYAxisPosition(Position position) {
        return position.getY() >= 0 &&
                position.getY() < columnCount;
     }
 
     public List<Square> getAllSquaresByType(SquareType type) {
         List<Square> results = new ArrayList<Square>();
         for (int i=0; i<rowCount; i++) {
             for (int j=0; j<columnCount; j++) {
                 if (type == matrix[i][j].getType()) {
                     results.add(matrix[i][j]);
                 }
             }
         }
         return results;
     }
 
     public int getRowCount() {
         return rowCount;
     }
 
     public int getColumnCount() {
         return columnCount;
     }
 
 }
