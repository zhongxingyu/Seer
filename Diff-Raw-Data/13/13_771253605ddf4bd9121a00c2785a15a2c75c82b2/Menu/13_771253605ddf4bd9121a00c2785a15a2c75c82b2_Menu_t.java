 package javasudoku;
 
 import java.util.List;
 
 /**
  *
  * Clase que controla la interfaz de usuario y comunica con Juego
  */
 public class Menu {
     Leer leer = new Leer();
     Sudoku sudoku = new Sudoku();
     Juego juego = new Juego();
     Graficos graficos = new Graficos();
     
     /**
      * Constructor de Menu
      */
     public void Inicio()
     {
         int opcion = 0;
         System.out.println("Bienvenido a JavaSudoku v.1");
         System.out.println("Para comenzar ingrese una opción");
         
         do
         {
             System.out.println("1: Nuevo Juego");
             System.out.println("2: Salir");
             opcion = leer.LeerDatosInt();
             
             if(opcion==0)            
                 System.out.println("Error al recibir la instrucción, reintente");            
             else if(opcion==1)            
                 NuevoJuego();            
         }
         while(opcion==0);
     }
 
     /**
      * Inicializa UI y carga nuevo juego
      */
     public void NuevoJuego()
     {
         int dificultad = 0;
         do
         {
             System.out.println("Ingrese nivel de dificultad");
             System.out.println("1: Fácil");
             System.out.println("2: Normal");
             System.out.println("3: Difícil");
             System.out.println("4: Salir");
             
             dificultad = leer.LeerDatosInt();
             
             if(dificultad==0)            
                 System.out.println("Error al recibir la opción, reintente");
             
         }
         while(dificultad==0 || dificultad>4);
         
         //Se crea instancia de juego
         //Se inicializa matriz de juego
         sudoku = juego.GeneraNuevoJuego(dificultad);
         ImprimeMatrizActual();
                        
         int opcion = 0;
         do
         {
             System.out.println("Ingrese una opción para jugar");
             System.out.println("1: Modificar celda");
             System.out.println("2: Borrar celda");
             System.out.println("3: Listar jugadas");
             System.out.println("4: Ver solución");
             System.out.println("5: Reiniciar");
             System.out.println("6: Salir");
             
             opcion = leer.LeerDatosInt();
             switch (opcion) {
                 case 1:
                     ModificarCelda();
                     opcion=0;
                     break;
                 case 2:
                     BorrarCelda();
                     opcion=0;
                     break;
                 case 3:
                     ListarJugadas();
                     opcion=0;
                     break;
                 case 4:
                     VerSolucion();
                     opcion=0;
                     break;
                 case 5:
                     Reiniciar();
                     opcion=0;
                     break;
                 case 0:
                     System.out.println("Error al recibir la opción, reintente");
                     break;
             }
         }
         while(opcion==0);
     }
     
     /**
      * Permite capturar datos de usuario para modificar una celda
      */
     public void ModificarCelda()
     {
         int fila = 0;
         int columna = 0;
         int valor = 0;
         do
         {
             System.out.println("Ingrese número de fila");
             fila = leer.LeerDatosInt();
             
             if(fila==0)
                 System.out.println("Error al recibir la opción, reintente");
             else if(fila>9){
                 System.out.println("El número a ingresar debe estar entre 1 y 9");
                 fila=0;
             }            
         }
         while(fila==0);    
         
         do
         {
             System.out.println("Ingrese número de columna");
             columna = leer.LeerDatosInt();
             
             if(columna==0)
                 System.out.println("Error al recibir la opción, reintente");
             else if(columna>9){
                 System.out.println("El número a ingresar debe estar entre 1 y 9");
                 columna=0;
             }            
         }
         while(columna==0);            
         
         do
         {
             System.out.println("Ingrese valor a modificar");
             valor = leer.LeerDatosInt();
             
             if(valor==0)
                 System.out.println("Error al recibir la opción, reintente");
             else if(valor>9){
                 System.out.println("El número a ingresar debe estar entre 1 y 9");
                 valor=0;
             }
         }
         while(valor==0);
         
         //Llamada a juego
         sudoku = juego.ModificarPosicion(sudoku, fila, columna, valor);
         if(juego.ValidaSolucionJuegoActual(sudoku))
             System.out.println("Nivel completado, felicitaciones!");
         
         ImprimeMatrizActual();
     }
     
     /**
      * Lista las jugadas realizadas por el usuario
      */
     public void ListarJugadas()
     {
        List<Movimientos> lista = juego.ListarMovimientos(sudoku);
         //Muestra jugadas secuencialmente
         for (int i = 0; i < lista.size(); i++) {
             System.out.print("Tipo jugada: ");
             System.out.print(lista.get(i).getTipo());
             System.out.print(" fila: " + lista.get(i).getFila());
             System.out.print(" columna: " + lista.get(i).getColumna());
             System.out.print(" valor: " + lista.get(i).getValor());
             System.out.print(" fecha y hora : " + lista.get(i).getFechaHora());
             System.out.println("");
         }
     }
     
     /**
      * Elimina el dato de una celda
      */
     public void BorrarCelda()
     {
         int fila = 0;
         int columna = 0;
         do
         {
             System.out.println("Ingrese número de fila");
             fila = leer.LeerDatosInt();
             
             if(fila==0)
                 System.out.println("Error al recibir la opción, reintente");
         }
         while(fila==0);    
         
         do
         {
             System.out.println("Ingrese número de columna");
             columna = leer.LeerDatosInt();
             
             if(columna==0)
                 System.out.println("Error al recibir la opción, reintente");
         }
         while(columna==0);
         
         //Se elimina valor de acuerdo a los datos capturados
         sudoku = juego.BorrarPosicion(sudoku, fila, columna);        
         ImprimeMatrizActual();
     }
     
     /**
      * Despliega la solución del juego
      */
     public void VerSolucion()
     {
         graficos.ImprimeSudoku(sudoku.getMatriz_solucion());
     }
     
     /**
      * Reinicia el juego volviendo a la generación inicial del sudoku
      */
     public void Reiniciar()
     {    
         sudoku = juego.ReiniciarJuego(sudoku);
         graficos.ImprimeSudoku(sudoku.getMatriz_actual());
     }
     
     /**
      * Imprime la matriz actual del juego
      */
     public void ImprimeMatrizActual()
     {   
         graficos.ImprimeSudoku(sudoku.getMatriz_actual());
     }
 }
