 package lesson2.contener;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 22.02.12
  * Time: 0:13
  */
 
 public class Matrix {
     
     private int matrix[][];
     private int m;
     private int n;
 
     private int i;
     private int j;
 
     private final String globalPath = "/Volumes/Home/privratnik/Desktop/";
 
     public Matrix( int m, int n ){
         this.m = m;
         this.n = n;
 
         this.matrix = new int[ m ][ n ];
     }
     
     public int getM() {
         return m;
     }
 
     public void setM(int m) {
         this.m = m;
     }
 
     public int getN() {
         return n;
     }
 
     public void setN(int n) {
         this.n = n;
     }
     
     public int getIndex(int m, int n){
         return matrix[m][n];
     }
     
     public void addItem( int item, int m, int n ){
         matrix[m][n] = item;
     }
 
     public void addItem( int item ){
         if( j < n ){
             matrix[i][j] = item;
             j++;
         }
         if( j == n ){
             j = 0;
             i++;
         }
     }
 
     public Matrix add( Matrix sumMatrix ) throws Exception {
 
         Matrix result = null;
 
         if( (m == sumMatrix.m) && (n == sumMatrix.n) ){
             result = new Matrix( m, n );
 
             for( int i = 0; i < m; i++ ){
                 for( int j = 0; j < n; j++ ){
                     result.addItem( (this.matrix[i][j]) + (sumMatrix.matrix[i][j]) );
                 }
             }
 
         }else {
             throw new MatrixException();
         }
 
         return result;
     }
 
     public boolean equal( Matrix eqMatrix ){
 
         boolean flag = false;
 
         if( (m != eqMatrix.m) || (n != eqMatrix.n) ) return false;
 
         for( int i = 0; i < m; i++ ){
             for( int j = 0; j < n; j++ ){
                 flag = this.matrix[i][j] == eqMatrix.matrix[i][j];
                 if( !flag ) return false;
             }
         }
 
         return flag;
     }
 
     public void write( String filename ) throws IOException {
 
         FileWriter writer = null;
         
         try{
             writer = new FileWriter( globalPath + filename );
             
             for( int i = 0; i < m; i++ ){
                 for( int j = 0; j < n; j++ ){
                     writer.write( String.valueOf( matrix[i][j] ) + " " );
                 }
                 writer.write("\n");
             }
 
         }catch ( NullPointerException e ){
             e.printStackTrace();            
         }catch ( IOException e){
             e.printStackTrace();
         }finally {
             writer.close();
         }
 
     }
 
     public void read( int m, int n, String filename ) throws IOException {
 
         FileReader reader = null;
 
         try{
             reader = new FileReader( globalPath + filename );
             int tmp = 0;
 
            do{
                 tmp = reader.read();
 
                if( tmp != -1 ){
                    addItem((char) tmp);
                 }
 
            }while( tmp != -1 );
 
         }catch (FileNotFoundException e){
             e.printStackTrace();
         }catch ( NullPointerException e ){
             e.printStackTrace();
         }finally {
             reader.close();
         }
 
     }
 
     public void output(){
         for( int i = 0; i < m; i++ ){
             for( int j = 0; j < n; j++ ){
                 System.out.print(matrix[i][j] + "\t");
             }
             System.out.println("\n");
         }
     }
 
 }
