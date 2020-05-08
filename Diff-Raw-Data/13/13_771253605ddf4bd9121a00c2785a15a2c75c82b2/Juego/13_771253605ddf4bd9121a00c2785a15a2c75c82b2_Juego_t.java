 package javasudoku;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 /**
  * 
  * Estructura que maneja una secuencia utilizada al momento de generar los números del Sudoku
  */
 class Secuencia {
     int valor;
     boolean activo;
     public Secuencia(int _valor, boolean _activo)
     {
         this.valor = _valor;
         this.activo = _activo;
     }
 }
 
 /**
  * Clase con los métodos de generación de partidas de Sudoku 
  */
 public class Juego {
     /**
      * Mantiene la secuencia de los números utilizados durante la generación
      */
     private List<Secuencia> listaSecuencia = new ArrayList<Secuencia>();
     
     /**
      * Constructor de la clase
      */
     public Juego()
     {
         
     }
     
     /**
      * Modifica la secuencia a su estado original
      */
     private void ReiniciarSecuencia()
     {
         listaSecuencia.clear();
         listaSecuencia.add(new Secuencia(1, false));
         listaSecuencia.add(new Secuencia(2, false));
         listaSecuencia.add(new Secuencia(3, false));
         listaSecuencia.add(new Secuencia(4, false));
         listaSecuencia.add(new Secuencia(5, false));
         listaSecuencia.add(new Secuencia(6, false));
         listaSecuencia.add(new Secuencia(7, false));
         listaSecuencia.add(new Secuencia(8, false));
         listaSecuencia.add(new Secuencia(9, false));
     }
     
     /**
      * Modifica estado de la secuencia para un número i 
      * @param i El número de la secuencia para modificar su estado
      */
     private void InsertaSecuencia(int i)
     {        
         listaSecuencia.set(i-1, new Secuencia(i, true));
     }
     
     /**
      * Revisa que se hayan utilizado todos los números de la secuencia
      * @return Verdadero si se utilizaron todos los números de la secuencia
      */
     private boolean ValidaSecuencia()
     {
         boolean result = true;
         for(int i=0;i<9;i++)
         {
             if(!listaSecuencia.get(i).activo)
             {    
                 result = false;
                 break;
             }
             
         }        
         return result;
     }
             
     /**
      * Valida para un cuadrante recibido por parámetro, exista el número a insertar
      * @param matriz La matriz de los datos
      * @param cuadrante El número del cuadrante del juego
      * @param num El número a evaluar
      * @return Falso si el número ya existe en el cuadrante
      */
     private boolean ValidaCuadrante(int[][] matriz, int cuadrante, int num)
     {
         boolean resp = true;
         switch(cuadrante)
         {
             case 1: 
             {
                 for(int i = 0; i < 3; i++){
                     for(int j = 0; j < 3; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 2: 
             {
                 for(int i = 0; i < 3; i++){
                     for(int j = 3; j < 6; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 3: 
             {
                 for(int i = 0; i < 3; i++){
                     for(int j = 6; j < 9; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }                
             case 4: 
             {
                 for(int i = 3; i < 6; i++){
                     for(int j = 0; j < 3; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 5: 
             {
                 for(int i = 3; i < 6; i++){
                     for(int j = 3; j < 6; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 6: 
             {
                 for(int i = 3; i < 6; i++){
                     for(int j = 6; j < 9; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 7: 
             {
                 for(int i = 6; i < 9; i++){
                     for(int j = 0; j < 3; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 8: 
             {
                 for(int i = 6; i < 9; i++){
                     for(int j = 3; j < 6; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }
             case 9: 
             {
                 for(int i = 6; i < 9; i++){
                     for(int j = 6; j < 9; j++){
                         if(num==matriz[i][j])
                            resp = false; 
                     }
                 }
                 break;
             }                                        
         }
         return resp;
     }       
     
     /**
      * Devuelve el cuadrante a partir de una fila y una columna
      * @param fila La fila a evaluar
      * @param columna La columna a evaluar
      * @return El número del cuadrante
      */
     private int ObtieneCuadrante(int fila, int columna)    
     {
        int cuadrante = 0;
        if(fila>-1 && fila<3)
        {
            if(columna>-1 && columna<3)
            {
                cuadrante = 1;
            }
            else if(columna>2 && columna <6)    
            {
                 cuadrante = 2;
            }
            else if(columna>5 && columna <9)
            {
                 cuadrante = 3;
            }           
        }
        else if(fila>2 && fila <6)    
        {
            if(columna>-1 && columna<3)
            {
                cuadrante = 4;
            }
            else if(columna>2 && columna <6)    
            {
                 cuadrante = 5;
            }
            else if(columna>5 && columna <9)
            {
                 cuadrante = 6;
            }                      
        }
        else if(fila>5 && fila <9)
        {
            if(columna>-1 && columna<3)
            {
                cuadrante = 7;
            }
            else if(columna>2 && columna <6)    
            {
                 cuadrante = 8;
            }
            else if(columna>5 && columna <9)
            {
                 cuadrante = 9;
            }                      
        }
        return cuadrante;
     }
     
     /**
      * Genera el número validando que no exista en la fila, columna y cuadrante
      * correspondiente
      * @param matriz La matriz a evaluar
      * @param fila Posición actual de la fila
      * @param columna Posición actual de la columna
      * @return Número mayor a cero si el procesamiento estuvo libre de errores
      * . Cero si se encontró un error en la generación del número
      */
     private int GeneraNumero(int[][] matriz, int fila, int columna) 
     {
         int resp = 0;
         boolean flag = false;
         //Genera lista para validar los numeros utilizados en el ciclo
         ReiniciarSecuencia();
         do
         {                        
             boolean validaFila = true;
             boolean validaColumna = true;
             boolean validaCuadrante = true;
             resp = (int)(Math.random() * (10 - 1) + 1);
 
             //Chequea que el numero no se repita en los cuadrantes 3 x 3
             if(!ValidaCuadrante(matriz, ObtieneCuadrante(fila, columna),resp))
                 validaCuadrante = false;
             else
             {
                 //Chequea que el numero no se repita en la fila
                 for(int i = 0;i < 9;i++)
                 {
                     if(matriz[i][columna]==resp)
                     {
                         validaColumna = false;
 
                     }
                     if(matriz[fila][i]==resp)
                     {
                         validaFila = false;
                     }
                 }                
             }    
                        
             if(validaFila && validaColumna && validaCuadrante)
                 flag = true;
             
             InsertaSecuencia(resp);
             
             //Si se han insertado todos los valores posibles se devuelve 0            
             if(ValidaSecuencia())
             {
                 flag = true;
                 resp = 0;
             }
         }
         while (!flag);        
         return resp;
     }
     
     /**
      * Genera la matriz inicial con la solución del juego
      * @return Devuelve matriz con la solución
      */
     private int[][] GeneraMatrizInicial() 
     {
         int[][] matriz = new int[9][9];
         boolean flag = false;
 
         //Recorre matriz por cuadrante
         for(int i = 0; i < 9;i++)
         {
             int cont = 0;
             for(int j = 0; j < 9;j++)
             {                
                 matriz[i][j] = GeneraNumero(matriz,i,j);                 
                 if(matriz[i][j]==0)
                 {
                     //Backtracking: Si algo sale mal, se genera la fila completa de nuevo
                     for(int a = 0;a< 9;a++)
                         matriz[i][a] = 0;
                     //Se devuelve el puntero la posicion inicial - 1
                     j=-1;
                     //Sumamos en el contador si eliminando la fila actual no resuelve el problema
                     cont++;
                     
                     //Backtracking fila n - 1 
                     if(cont>9 && i>1)
                     {
                         //Se eliminan los datos de la fila n-1
                         for(int a = 0;a< 9;a++)
                             matriz[i-1][a] = 0;                        
                         
                         //Se devuelve puntero a la fila n-1
                         i=i-1;
                         //Se resetea contador
                         cont=0;
                     }
                 }                
             }
         }
         return matriz;
     }
     
     /**
      * Elimina elementos de la matriz de acuerdo a la dificultad indicada
      * @param matriz Matriz de juego
      * @param dificultad Dato de dificultad
      * @return Matriz con elementos 'eliminados'
      */
     private int[][] BorraElementosPorDificultad(int[][] matriz, int dificultad)
     {
         int[][] resp = new int[9][9];
         int borrar=0;
         int x = 0;
         int y = 0;
         //Fácil
         if(dificultad==1)
         {
             borrar = 81 - 38;
         }
         //Intermedia
         else if(dificultad==2)
         {
             borrar = 81 - 30;
         }
         //Difícil
         else if(dificultad==3)
         {
             borrar = 81 - 27;
         }
         
         for (int i = 0; i < matriz.length; i++) {
             for (int j = 0; j < matriz[0].length; j++) {
                 resp[i][j] = matriz[i][j];
             }
         }
         
         
         do
         {
             x = (int)(Math.random() * (10 - 1));
             y = (int)(Math.random() * (10 - 1));
             
             if(resp[x][y]>0)
             {
                 borrar = borrar - 1;
                 resp[x][y]=0;
             }
         }
         while(borrar>0);
         
         return resp;
     }
 
     /**
      * Devuelve una instancia de Sudoku con las matrices inicializadas
      * @param dificultad Nivel de dificultad
      * @return Instancia de Sudoku con las matrices inicializadas
      */
     public Sudoku GeneraNuevoJuego(int dificultad) 
     {                
         Sudoku sudoku = new Sudoku();
         int[][] solucion = new int[9][9];
         sudoku.setMatriz_solucion(GeneraMatrizInicial());
         solucion = sudoku.getMatriz_solucion();
         sudoku.setMatriz_inicial(BorraElementosPorDificultad(solucion,
                 dificultad));
        sudoku.setMatriz_actual(sudoku.getMatriz_inicial());        
         return sudoku;
     }
     
     /**
      * Permite volver al juego original inicializando nuevamente la matriz de la partida
      * @param sudoku 
      */
     public Sudoku ReiniciarJuego(Sudoku sudoku)
     {
        sudoku.setListadoMovimientos(new ArrayList<Movimientos>());
         sudoku.setMatriz_actual(sudoku.getMatriz_inicial());
         return(sudoku);
     }
     
     /**
      * Borra el dato en la posición indicada 
      * @param sudoku Instancia actual del juego
      * @param fila número de fila ingresado
      * @param columna número de columna 
      * @return Instancia de Sudoku actualizada
      */
     public Sudoku BorrarPosicion(Sudoku sudoku, int fila, int columna)
     {
         Sudoku response = null;
         int[][] resp = null;
         try
         {
             resp = sudoku.getMatriz_actual();
             resp[fila-1][columna-1] = 0;
             sudoku.setMatriz_actual(resp);
             
             //Se agrega al listado de movimientos
             List<Movimientos> movimientos = sudoku.getListadoMovimientos();
             movimientos.add(new Movimientos("Eliminación", fila-1, columna-1, 0, new Date()));
             
             sudoku.setListadoMovimientos(movimientos);
             response = sudoku;
         }
         catch(Exception e)
         {
         }
         return response;
     }
     
     /**
      * Modifica una posición en el juego actual
      * @param sudoku Instancia de la clase Sudoku
      * @param fila Fila donde se quiere realizar la modificación
      * @param columna Columna donde se quiere realizar la modificación
      * @param valor Valor a modificar
      * @return Instancia de Sudoku actualizada
      */
     public Sudoku ModificarPosicion(Sudoku sudoku, int fila, int columna,int valor)
     {
         Sudoku resp = null;
         int[][] matrizActual = null;
         try
         {   
             matrizActual = sudoku.getMatriz_actual();
             matrizActual[fila-1][columna-1] = valor;
             sudoku.setMatriz_actual(matrizActual);
             //Se agrega al listado de movimientos
             List<Movimientos> movimientos = sudoku.getListadoMovimientos();
             movimientos.add(new Movimientos("Modificación", fila-1, columna-1, valor, new Date()));
             
             sudoku.setListadoMovimientos(movimientos);
             
             resp = sudoku;
         }
         catch(Exception e)
         {
         }
         return resp;
     }
     
     /**
      * Lista los movimientos en la instancia actual de Sudoku
      * @param sudoku Instancia actual de Sudoku
      * @return Lista de movimientos efectuados
      */
     public List<Movimientos> ListarMovimientos(Sudoku sudoku)
     {
         return sudoku.getListadoMovimientos();
     }
     
     /**
      * Devuelve la matriz de la solución
      * @param sudoku Instancia actual de Sudoku
      * @return Matriz de la solución del juego
      */
     public int[][] ObtieneSolucion(Sudoku sudoku)
     {
         return sudoku.getMatriz_solucion();
     }
     
     /**
      * Valida que la matriz de juego corresponda celda por celda con la solución
      * @param sudoku Estructura del juego sudoku
      * @return Devuelve verdadero si el juego está completamente solucionado.
      */
     public boolean ValidaSolucionJuegoActual(Sudoku sudoku)
     {
         boolean resp = true;
         int[][] solucion = sudoku.getMatriz_solucion();
         int[][] actual = sudoku.getMatriz_actual();
         for (int i = 0; i < solucion.length; i++) {
             for (int j = 0; j < solucion[0].length; j++) {
                 if(solucion[i][j]!=actual[i][j])
                     return false;
             }
         }
         
         return resp;
     }
            
 }
