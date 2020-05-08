 package ru.pondohva.tictactoe;
 
 public class Field {
 
     private static final char DEFAULT_VALUE = ' ';
 
     private static final int DEFAULT_FIELD_SIZE = 3;
 
     public int field_size;
 
     private char[][] field;
 
     @SuppressWarnings("unused")
     public Field() {
         this(DEFAULT_FIELD_SIZE);
     }
 
     @SuppressWarnings("all")
     public Field(Field copyField) {
         this.field_size = copyField.field_size;
         this.field = new char[this.field_size][this.field_size];
        System.arraycopy(copyField.field, 0, this.field, 0, this.field_size);
         // Arrays.copyOf and System.arraycopy not working for this
//        for (int i = 0; i < this.field_size; i++) {
//            for (int j = 0; j < this.field_size; j++) {
//                this.field[i][j] = copyField.field[i][j];
//            }
//        }
     }
 
     public Field(int newFieldSize) {
         this.field_size = newFieldSize;
         this.field = new char[this.field_size][this.field_size];
         initField();
     }
 
     public char[][] getField() {
         return this.field;
     }
 
     public void setVal(int row, int column, char val) {
         this.field[row][column] = val;
     }
 
     private void initField() {
         for (int i = 0; i < field_size; i++) {
             for (int j = 0; j < field_size; j++) {
                 field[i][j] = DEFAULT_VALUE;
             }
         }
     }
 
 
     public void showField() {
         for (int i = 0; i < field_size; i++) {
             for (int j = 0; j < field_size; j++ ) {
                 System.out.print("[ " + field[i][j] + " ]");
             }
             System.out.println();
         }
     }
 }
