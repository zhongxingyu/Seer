 package lesson2;
 
 import lesson2.contener.Matrix;
 import lesson2.contener.Vector;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 22.02.12
  * Time: 0:17
  */
 
 public class Main1 {
     public static void main( String[] args ){
 
 /*
         System.out.println("Введите колличество строк матрицы :");
 
         Scanner scanner = new Scanner( System.in );
         int m = scanner.nextInt();
 
         System.out.println("Введите колличество столбцов матрицы :");
 
         int n = scanner.nextInt();
 
         Matrix matrix = new Matrix( m , n );
 
         System.out.println("Введите элементы матрицы :");
 
         for( int i = 0; i < m; i++ ){
             for( int j = 0; j < n; j++ ){
                 matrix.addItem( scanner.nextInt() );
             }
         }
 */
         Matrix matrix_1 = new Matrix( 2, 3 );
         matrix_1.addItem( 1 );
         matrix_1.addItem( 2 );
         matrix_1.addItem( 3 );
         matrix_1.addItem( 1 );
         matrix_1.addItem( 2 );
         matrix_1.addItem( 1 );
 
         Matrix matrix_2 = new Matrix( 2, 3 );
         matrix_2.addItem( 1 );
         matrix_2.addItem( 2 );
         matrix_2.addItem( 3 );
         matrix_2.addItem( 1 );
         matrix_2.addItem( 2 );
         matrix_2.addItem( 1 );
 
         try{
 /*
             Matrix out = matrix_1.add( matrix_2 );
             out.output();
 */
    //         matrix_1.write( "test.txt" );
 
            Matrix matrix_3 = new Matrix(2, 3);
            matrix_3.read( 2, 3, "test.txt" );
            matrix_3.output();
 
   //          System.out.print( matrix_1.equal( matrix_2 ) );
             
             Vector vector = new Vector(5);
             vector.addItem( 1 );
             vector.addItem( 2 );
             vector.addItem( 3 );
             vector.addItem( 4 );
             vector.addItem( 5 );
      //      vector.output();
 
         //   System.out.println( vector.length() );
 
         } catch ( Exception e ){
             e.printStackTrace();
         }
 
     }
 }
