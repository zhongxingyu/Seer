 package backend;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Board {
     /* Array con las direcciones en el orden óptimo precalculadas, para usar
        en la solución aproximada a la hora de buscar una solución inicial
        Ver método privado getOptimalDirArray*/
     private final static Direction[][] optimalDir = new Direction[][]{
     /* 0 */ {Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT},
     /* 1 */ {Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN},
     /* 2 */ {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT},
     /* 3 */ {Direction.LEFT, Direction.UP, Direction.DOWN, Direction.RIGHT},
     /* 4 */ {Direction.RIGHT, Direction.UP, Direction.DOWN, Direction.LEFT},
     /* 5 */ {Direction.DOWN, Direction.LEFT, Direction.UP, Direction.RIGHT},
     /* 6 */ {Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP},
     /* 7 */ {Direction.DOWN, Direction.RIGHT, Direction.UP, Direction.LEFT}
     };
 
     /* Constante T de probabilidad para el algoritmo aproximado
        hill-climber estocástico */
     private final static double T = 0.1;
  
 	private Cell matrix[][];
 	private ArrayList<Dot> dots = new ArrayList<Dot>();
     private long calls = 0;
     private int paintedCells;
 
     public Board(Cell[][] matrix, ArrayList<Dot> dots) {
         this.matrix = matrix;
         this.dots = dots;
         if(dots != null){
             paintedCells = dots.size()*2;
         }else{
             paintedCells = 0;
         }
     }
 
     public long getCalls(){
         return calls;
     }
 
 	public Cell[][] getMatrix(){
 		return matrix;
 	}
 
     public Cell at(Position pos){
         if(pos.row < 0 || pos.row >= matrix.length || pos.col < 0 || pos.col >= matrix[0].length)
             return null;
         return this.matrix[pos.row][pos.col];
     }
 
     public boolean isOrigin(int row,int col,int color){
 		for(Dot d:dots){
 			if(d.getColor() == color){
 				return d.getStart().col == col && d.getStart().row == row;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isEnd(int row,int col,int color){
 		for(Dot d : dots){
 			if(d.getColor() == color){
 				return d.getEnd().col == col && d.getEnd().row == row;
 			}
 		}
 		return false;
 	}
 
 	public int colsSize(){
 		return matrix != null ? matrix[0].length : 0;
 	}
 	
 	public int rowsSize(){
 		return matrix != null ? matrix.length : 0;
 	}
 	
     /**
      * Busca una solución exacta mediante backtracking. Al menos que encuentre
      * una solución que cubra el tablero entero y retorne antes de tiempo,
      * seguirá probando tableros y devolverá la mejor solución encontrada
      *
      * @return una instancia de Board con la variable de instancia matrix
      *         conteniendo la solución si es que había una, null en caso
      *         contrario
      */
 	public Board solve(Listener listener){
         Dot initialDot = dots.get(0);
         Board solution = new Board(null, dots);
         this.paintedCells = 0;
         solve(initialDot.getColor(), null, initialDot.getStart(), 0, solution,listener);
         return solution.matrix == null ? null : solution;
     }
 
     /**
      * @return true si la solución cubre el tablero completo, false en caso contrario
      */
     private boolean solve(int color, Position prevPos, Position currentPos, int index, Board solution,Listener listener){
         calls++; // TODO recordar sacar calls antes de la entrega final!
         if(matrix.length <= currentPos.row || currentPos.row < 0
            || matrix[0].length <= currentPos.col || currentPos.col < 0) return false;
 
         int currentPosColor = this.matrix[currentPos.row][currentPos.col].color;
         if(color == currentPosColor){
             if(!currentPos.equals(dots.get(index).getStart())){
                 if(currentPos.equals(dots.get(index).getEnd())){
                     this.paintedCells++;
                     if(dots.size() == index+1){
                         saveSolution(this, solution);
                         if(solution.unPaintedCells() == 0) return true;
                     }else{
                         Dot nextDot = dots.get(index+1);
                        if(solve(nextDot.getColor(), null, nextDot.getStart(), index+1, solution,listener))
                            return true;
                     }
                     this.paintedCells--;
                 }
                 return false;
             }
             if(prevPos != null){
                return false;
             }
         }else if(currentPosColor!= -1) return false;
 
         this.matrix[currentPos.row][currentPos.col].color = color;
         this.paintedCells++;
         Position nextPos;
 
         /*secci�n para imprimir con intervalos de a 100ms*/
         if(listener!=null) listener.printToScreen();
 
         for(Direction d : this.optimalDir[0]){
             if( !(nextPos = currentPos.getPosition(d)).equals(prevPos) ){
                 if(solve(color, currentPos, nextPos, index, solution,listener)) return true;
             }
         }
         this.matrix[currentPos.row][currentPos.col].color = currentPosColor;
         this.paintedCells--;
         return false;
     }
 
     private static void saveSolution(Board board, Board solution){
         if( (solution.matrix==null)
                 || (solution.paintedCells < board.paintedCells) ){
             solution.cloneMatrix(board);
         }
     }
 
     private void cloneMatrix(Board board){
         int row, col;
         Cell c;
         this.matrix = new Cell[board.matrix.length][board.matrix[0].length];
         for(row = 0; row < board.matrix.length; row++){
             for(col = 0; col < board.matrix[0].length; col++){
                 c = board.matrix[row][col];
                 this.matrix[row][col] = new Cell(c.color,c.nextPathDir);
             }
         }
         this.paintedCells = board.paintedCells;
     }
 
     public int getPaintedells(){
         return this.paintedCells;
     }
 
     public int unPaintedCells(){
     	return (rowsSize()*colsSize()) - paintedCells;
     }
     
     /* Algoritmo basado en Hill Climbing  ,
      * empieza por un color y evalua sus vecinos,si la solucion es mejor la acepta y sino sigue buscando otra mejor
      * hasta que tenga tiempo
      */
     public Board solveAprox(Listener l,Chronometer chronometer){
         Board solution = new Board(null, dots);
         Board bestSolution = null;
         chronometer.start();
         while(chronometer.thereIsTimeRemaining() && solution.unPaintedCells() != 0){
             solution = findInitialSolution(l,chronometer);
             if(solution == null) return null;
             if(solution.unPaintedCells() == 0) return solution;
             improveSolution(solution, l);
             System.out.println(chronometer.checkCurrentTime()); // TODO BORRAR !
             if(bestSolution == null ||  bestSolution.paintedCells < solution.paintedCells){
                 bestSolution = solution;
             }
             Collections.shuffle(dots); // randomizar orden de colores para escapar al máximo local
         }
         return bestSolution;
     }
 
     public Board findInitialSolution(Listener l, Chronometer chronometer){
         Board initialBoardCopy = new Board(null, dots);
         initialBoardCopy.cloneMatrix(this);
         Board solution = new Board(null, dots);
         Dot initialDot = dots.get(0);
         if(l!=null) l.changeBoard(initialBoardCopy);
         initialBoardCopy.paintedCells = 0;
         initialBoardCopy.findInitialSolution(initialDot.getColor(), null, initialDot.getStart(), 0, solution, l,chronometer);
         if(l!=null) l.changeBoard(initialBoardCopy);
         return solution.matrix == null ? null : solution;
     }
 
     private boolean findInitialSolution(int color, Position prevPos, Position currentPos, int index, Board solution,Listener l,Chronometer chronometer){
         if(matrix.length <= currentPos.row || currentPos.row < 0
                 ||matrix[0].length <= currentPos.col || currentPos.col < 0||!chronometer.thereIsTimeRemaining()) return false;
 
         int currentPosColor = matrix[currentPos.row][currentPos.col].color;
         if(color == currentPosColor){
             if(!currentPos.equals(dots.get(index).getStart())){
                 if(currentPos.equals(dots.get(index).getEnd())){
                     this.paintedCells++;
                     if(dots.size() == index+1){
                         saveSolution(this, solution);
 
                         return true;
                     }else{
                         Dot nextDot = dots.get(index+1);
                        if(findInitialSolution(nextDot.getColor(), null, nextDot.getStart(), index+1, solution,l,chronometer))
                            return true;
                     }
                     this.paintedCells--;
                 }
                 return false;
             }
             if(prevPos != null){
                 return false;
             }
         }else if(currentPosColor != -1) return false;
         matrix[currentPos.row][currentPos.col].color = color;
         this.paintedCells++;
 
         if(l != null) l.printToScreen();
 
         Direction[] dir = getOptimalDirArray(currentPos, dots.get(index).getEnd());
         Direction prevDir;
         Position nextPos;
         for(int i = 0; i < 4; i++){
             if( !(nextPos = currentPos.getPosition(dir[i])).equals(prevPos)){
                 prevDir = this.at(currentPos).nextPathDir;
                 this.at(currentPos).nextPathDir = dir[i];
                 if(findInitialSolution(color,currentPos,nextPos,index, solution,l,chronometer)) return true;
                 this.at(currentPos).nextPathDir = prevDir;
             }
         }
         matrix[currentPos.row][currentPos.col].color = currentPosColor;
         this.paintedCells--;
         return false;
     }
 
     public void improveSolution(Board solution, Listener l){
         Difference difference =null;
         int previousPaintedCells;
         do{
             previousPaintedCells = solution.paintedCells;
             for(Dot dot: dots){
                 Position currentPos = dot.getStart();
                 Cell auxCell = solution.matrix[currentPos.row][currentPos.col];
                 Position aux1, aux2;
                 Direction currentDir = auxCell.nextPathDir;
                 while(currentDir != null){
                     switch(currentDir){
                         case UP:    if(thereIsSpaceAtCellPair(solution.at(aux1 = currentPos.getPosition(Direction.LEFT)),
                                     solution.at(aux2 = currentPos.getPosition(Direction.UPPERLEFT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.LEFT,Direction.UP,Direction.RIGHT,dot.getColor());
                                     }else if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.RIGHT)),
                                     solution.at(aux2 =currentPos.getPosition(Direction.UPPERRIGHT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.RIGHT,Direction.UP,Direction.LEFT,dot.getColor());
                                     }
                                     break;
                         case DOWN:  if(thereIsSpaceAtCellPair(solution.at(aux1=currentPos.getPosition(Direction.LEFT)),
                                     solution.at(aux2 =currentPos.getPosition(Direction.LOWERLEFT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.LEFT,Direction.DOWN,Direction.RIGHT,dot.getColor());
                                     }else if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.RIGHT)),
                                     solution.at(aux2=currentPos.getPosition(Direction.LOWERRIGHT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.RIGHT,Direction.DOWN,Direction.LEFT,dot.getColor());
                                     }
                                     break;
                         case LEFT:  if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.UP)),
                                     solution.at(aux2=currentPos.getPosition(Direction.UPPERLEFT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.UP,Direction.LEFT,Direction.DOWN,dot.getColor());
                                     } else if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.DOWN)),
                                     solution.at(aux2 =currentPos.getPosition(Direction.LOWERLEFT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.DOWN,Direction.LEFT,Direction.UP,dot.getColor());
                                     }
                                     break;
                         case RIGHT: if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.UP)),
                                     solution.at(aux2=currentPos.getPosition(Direction.UPPERRIGHT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.UP,Direction.RIGHT,Direction.DOWN,dot.getColor());
                                     }else if(thereIsSpaceAtCellPair(solution.at(aux1 =currentPos.getPosition(Direction.DOWN)),
                                     solution.at(aux2 =currentPos.getPosition(Direction.LOWERRIGHT)))){
                                         difference = new Difference(currentPos,aux1,aux2,Direction.DOWN,Direction.RIGHT,Direction.UP,dot.getColor());
                                     }
                                     break;
                     }
                     currentPos = currentPos.getPosition(solution.at(currentPos).nextPathDir);
                     currentDir = solution.at(currentPos).nextPathDir;
                     if(difference != null && Math.random() < 1/(1+ Math.pow(Math.E, ((double)(solution.paintedCells - 2))/T))){
                         break;
                     }
                 }
                 if(difference != null){
                     solution.applyDifferences(difference);
                     difference = null;
                 }
             }
         }while(previousPaintedCells < solution.paintedCells);
        // System.out.println(previousPaintedCells);
     }
 
     private boolean thereIsSpaceAtCellPair(Cell c1, Cell c2){
         if(c1 == null || c2 == null) return false;
         return c1.color == -1 && c2.color == -1;
     }
 
     /**
      * Siendo C el "current point", hay 8 posibles posiciones diferentes
      * en las que el destino puede encontrarse, numeradas desde el extremo
      * izquierdo superior al derecho inferior.
      * --------------------
      * | >>     =>     <> |
      * |                  |
      * | >=     C      <= |
      * |                  |
      * | ><     =<     << |
      * --------------------
      * @param from Posición en la que se encuentra el algoritmo
      * @param to Posición a la que busca llegar el algoritmo
      * @return Arreglo de direcciones óptimas que corresponda
      */
     private Direction[] getOptimalDirArray(Position from, Position to){
         int horizontal = from.col - to.col;
         int vertical = from.row - to.row;
         if(horizontal > 0)
             return (vertical > 0) ? optimalDir[0] : (vertical < 0) ? optimalDir[5] : optimalDir[3];
         if(horizontal == 0)
             return (vertical > 0) ? optimalDir[1]: optimalDir[6];
         return (vertical > 0) ? optimalDir[2] : vertical == 0 ? optimalDir[4] : optimalDir[7];
     }
 
     public int countFreecels(){
         int ans=0;
         for(int i=0;i<matrix.length;i++){
             for(int j=0;j<matrix[0].length;j++){
                 if(matrix[i][j].getColor()==-1)
                     ans++;
             }
         }
         return ans;
     }
 
     public String toString(){
         int aux;
         String ans = "";
         for(int row = 0; row < matrix.length; row++){
             for(int col = 0; col < matrix[0].length; col++){
                 if((aux = matrix[row][col].color) != -1)
                     ans+=aux;
                 else
                 	ans+=" ";
                     
             }ans+="\n";
         }
         return ans.toString();
     }
 
     private void applyDifferences(Difference difference){
     	this.at(difference.pos1).color= difference.color;
     	this.at(difference.pos2).color= difference.color;
     	this.at(difference.origin).nextPathDir = difference.d0;
     	this.at(difference.pos1).nextPathDir= difference.d1;
     	this.at(difference.pos2).nextPathDir= difference.d2;
     	this.paintedCells += 2;
     }
 
     /**
      * Clase que almacena un cambio en el tablero. Al ser costosa la copia de
      * tableros para representar cada vecino de un estado del algoritmo
      * hill-climbing, se almacena el cambio en una instancia de Difference y luego
      * se lo aplica mediante el método applyDifferences
      */
     private class Difference {
     	int color;
     	Position origin, pos1, pos2;
     	Direction d0, d1, d2;
 
     	Difference(Position origin, Position c1, Position c2, Direction d0, Direction d1, Direction d2, int color){
     		this.origin=origin;
     		this.d0=d0;
     		this.pos1=c1;
     		this.d1=d1;
     		this.pos2=c2;
     		this.d2=d2;
     		this.color=color;
     	}
     }
 }
