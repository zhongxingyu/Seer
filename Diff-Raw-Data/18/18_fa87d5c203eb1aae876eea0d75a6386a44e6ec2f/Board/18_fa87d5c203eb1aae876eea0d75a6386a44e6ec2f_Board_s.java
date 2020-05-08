 package backend;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Board {
     /* Array con las direcciones en el orden óptimo precalculadas, para usar
        en la solución aproximada a la hora de buscar una solución inicial */
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
 
     /* Constante T de probabilidad para el algoritmo aproximádo
        hill-climber estocástico */
     private final static int T = 1;
     private List<Direction> path;
 	private Cell matrix[][];
 	private ArrayList<Dot> dots = new ArrayList<Dot>();
     private long calls = 0;
     private int paintedCells = 0;
 
     public Board(Cell[][] matrix, ArrayList<Dot> dots) {
         this.matrix = matrix;
         this.dots = dots;
     }
 
 	public Cell[][] getIntBoard(){
 		return matrix;
 	}
 
     public long getCalls(){
         return calls;
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
      * @return una instancia de Board con la variable de instancia matrix
      *         conteniendo la solución si es que había una, null en caso
      *         contrario
      */
 	public Board solve(Listener listener){
         Dot initialDot = dots.get(0);
         Board solution = new Board(null, dots);
         solve(initialDot.getColor(), null, initialDot.getStart(), 0, solution,listener);
         return solution.matrix == null ? null : solution;
     }
 
     /**
      *
      * @param color
      * @param prevPos
      * @param currentPos
      * @param index
      * @param solution
      * @param listener
      * @return true si la solución cubre el tablero completo, false en caso contrario
      */
     private boolean solve(int color, Position prevPos, Position currentPos, int index, Board solution,Listener listener){
         calls++;
         if(matrix.length <= currentPos.row || currentPos.row < 0
            || matrix[0].length <= currentPos.col || currentPos.col < 0) return false;
 
         int currentPosColor = this.matrix[currentPos.row][currentPos.col].color; //color original de la celda
         if(color == currentPosColor){
             if(!currentPos.equals(dots.get(index).getStart())){
                 if(currentPos.equals(dots.get(index).getEnd())){
                     if(dots.size() == index+1){
                         saveSolution(solution);
                         if(solution.unPaintedCells() == 0) return true;
                     }else{
                         Dot nextDot = dots.get(index+1);
                         solve(nextDot.getColor(), null, nextDot.getStart(), index+1, solution,listener);
                     }
                 }
                 return false;
             }
             if(prevPos != null){
                return false;
             }
         }else if(currentPosColor!= -1) return false;
 
         this.matrix[currentPos.row][currentPos.col].color = color;
         Position nextPos;
 
         /*secci�n para imprimir con intervalos de a 100ms*/
         if(listener!=null) listener.printToScreen();
 
         for(Direction d : Direction.values()){
             if( !(nextPos = currentPos.getPosition(d)).equals(prevPos) ){
                 if(solve(color, currentPos, nextPos, index, solution,listener)) return true;
             }
         }
         this.matrix[currentPos.row][currentPos.col].color = currentPosColor;
         return false;
     }
 
     private void saveSolution(Board solution){
         if( (solution.matrix==null)
                 || (solution.paintedCells() < this.paintedCells()) ){
            this.copyMatrix(solution);
         }
     }
 
     /**
      * @param board
      * @return a Board with it's matrix initialized with a copy of this Board's matrix
      */
     private void copyMatrix(Board board){
         int row, col;
         board.matrix = new Cell[matrix.length][matrix[0].length];
         for(row = 0; row < matrix.length; row++){
             for(col = 0; col < matrix[0].length; col++){
                 Cell c = this.matrix[row][col];
                 board.matrix[row][col] = new Cell(c.color,c.nextPathDir);
             }
         }
     }
 
     private int paintedCells(){
         if(this.paintedCells != 0) return this.paintedCells;
 
         int cellsPainted = 0;
         for(int row = 0; row < matrix.length; row++){
             for(int col = 0; col < matrix[0].length; col++){
                 if(matrix[row][col].color != -1)           /* -1 era la marca para espacio no? */
                     cellsPainted++;
             }
         }
         return cellsPainted;
     }
 
     public int unPaintedCells(){
     	return colsSize()*rowsSize()-paintedCells();
     }
     
     /*Algoritmo basado en Hill Climbing */
     public Board solveAprox(Listener l,Chronometer chronometer){

        int dotIndex = 0;
         Board copy = new Board(null, dots);
         Board solution = new Board(null, dots);
         copyMatrix(copy);
        while(chronometer.isThereTimeRemaining() && dotIndex < dots.size()){
            Dot nextDot = dots.get(dotIndex);
            boolean ans=findInitialSolution(nextDot.getColor(), null,nextDot.getStart(), 0, copy, solution,l);
             if(solution == null) return null; 
             
             /*improve sol*/
            dotIndex++;
         }
         return solution;
     }
     public void improveSolution(Board solution, Listener l){
         for(Dot dot: dots){
             for(Direction dir : Direction.values()){
                 Position nextPos = dot.getStart().getPosition(dir);
                 Position currentPos = dot.getStart();
                 if(nextPos.row < matrix.length && nextPos.row >= 0 &&
                         nextPos.col < matrix[0].length && nextPos.col >= 0 &&
                         matrix[nextPos.row][nextPos.col].color == dot.getColor()){
                     if(dir.equals(Direction.DOWN) || dir.equals(Direction.UP)){
                         tryCycleCols(dot.getColor(), currentPos.row, currentPos.col, solution);
                     }else{
                         tryCycleFils(dot.getColor(), currentPos.row, currentPos.col, solution);
                     }
                     improveSolution(solution, nextPos, dir, dot.getColor(), dot, null);
                     break;
                 }
             }
         }
     }
 
     private void improveSolution(Board solution, Position pos, Direction prevPathDir, int color, Dot dot, Listener l){
         int posColor = matrix[pos.row][pos.col].color;
         if(pos.equals(dot.getEnd()) || posColor != color) return;
         Position nextPos;
         nextPos = pos.getPosition(prevPathDir);
         if(!check(nextPos.row, nextPos.col, color)){
             if(prevPathDir.equals(Direction.UP) || prevPathDir.equals(Direction.DOWN)){
                 nextPos = pos.getPosition(Direction.LEFT);
                 nextPos = check(nextPos.row, nextPos.col, color) ? nextPos : pos.getPosition(Direction.RIGHT);
             }else{
                 nextPos = pos.getPosition(Direction.UP);
                 nextPos = check(nextPos.row, nextPos.col, color) ? nextPos : pos.getPosition(Direction.DOWN);
             }
         }
         switch (prevPathDir){
             case UP:
             case DOWN:
                 tryCycleCols(color, nextPos.row, nextPos.col, solution);
                 break;
             case LEFT:
             case RIGHT:
                 tryCycleFils(color, nextPos.row, nextPos.col, solution);
         }
     }
 
     private void tryBestSolution(Board solution, Listener l){
 		for(Dot dot:dots){
 			System.out.println("color"+dot.getColor());
 		    tryCycle(dot,solution,l);
 		}
 		System.out.println("luego de trycle");
 		for(int h=0;h<solution.getIntBoard().length;h++){
 			for(int k=0;k<solution.getIntBoard()[0].length;k++){
 				System.out.print(solution.getIntBoard()[h][k]);
 			}
 			System.out.println();
 		}
 	}
 
 	private void tryCycle(Dot dot,Board board,Listener l) {
 		Position pos = dot.getStart();
 		int color = dot.getColor();
 		int row=dot.getStart().row;
 		int col =dot.getStart().col;
 
         /* si el camino sigue verticalmente... */
 		if ( (row+1) < matrix.length && matrix[row+1][col].color == color || (row-1) >= 0 && matrix[row-1][col].color == color){
 			if(tryCycleCols(color, row, col, board)){
 				tryCycleFils(color, row, col, board);
 			}
 		}else if(((col-1)>=0&& matrix[row][col-1].color==color)||((col+1)<matrix[0].length && matrix[row][col+1].color==color)){
 			if(tryCycleFils(color,row,col,board)){
 				tryCycleCols(color,row,col,board);
 			}
 			System.out.println("entro en ciclo filas");
 		}
 	}
 	private boolean check(int x, int y, int color) {
 		return x>=0&&x<matrix.length && y>=0 && y<matrix[0].length && matrix[x][y].color == color;
 	}
 
 	private boolean tryCycleFils(int color, int row, int col, Board board) {
 		int r = row+1;
 		int c = ( (col-1) >= 0 && matrix[row][col-1].color == color)? col-1 : col+1;
         if(r < matrix.length && matrix[r][col].color == 1 && matrix[r][c].color == -1 ){
 			matrix[r][col].color=color;
 			matrix[r][c].color=color;
             return true;
 		}
 		
 
         r = row-1;
 		if(r >= 0 && matrix[r][col].color == -1 && matrix[r][col].color == -1){
             System.out.println("valor de la fila i col c");
             System.out.println("fila"+r+"col"+c);
             matrix[r][c].color=color;
             matrix[r][col].color=color;
              return true;
 
 		}
 
 		for(int h=0;h<board.getIntBoard().length;h++){/*cableo una impresion de la matrix para probar que se cambiaron*/
 		    for(int k=0;k<board.getIntBoard()[0].length;k++){
 				System.out.print(board.getIntBoard()[h][k]);
 			}
 			System.out.println();                                                                    
 		}
 		System.out.println("valor del flag en ciclo fils");
         return false;
     }
 
 	private boolean tryCycleCols(int color, int fila, int col,Board board) /*trata de ciclar por columna*/
 	{
 		System.out.println("llega a tryCycle");
 		int f=((fila-1)>=0&& board.getIntBoard()[fila-1][col].color==color)?fila-1:fila+1;
 		int i=col+1;
 		if( i < board.matrix[0].length && board.matrix[fila][i].color == -1 && board.matrix[f][i].color == -1){
 			board.getIntBoard()[fila][i].color=color;
 			board.getIntBoard()[f][i].color=color;
             return true;
 		}else{
 		    i=col-1;
 		    if(i<=0 && board.getIntBoard()[fila][i].color==-1 && board.getIntBoard()[f][i].color==-1){
                 board.getIntBoard()[fila][i].color=color;
 				board.getIntBoard()[f][i].color=color;
 			    return true;
 			}
 		}
 		System.out.println("valor del flag en ciclo cols");
 	    for(int h=0;h<board.getIntBoard().length;h++)/*cableo una impresion de la matrix para probar que se cambiaron*/
 	    {
 		for(int k=0;k<board.getIntBoard()[0].length;k++){
 			System.out.print(matrix[h][k].color);
 		}
 		System.out.println();
 	    }
 
         return false;
 		}
 			/*for(;nfila<=matrix.length&&fila<matrix.length &&col<matrix[0].length&&ncol<matrix[0].length;){{
 					if((matrix[fila][col]==-1||matrix[fila][col]==color)&&(matrix[fila][col]==-1||matrix[fila][col]==color)){
 						matrix[fila][col]=color;
 						matrix[nfila][ncol]=color;
 						col+=incrcol;
 						System.out.println("llegue trycycle");
 						
 					}
 					return;
 				}*/
 
     private boolean findInitialSolution(int color, Position prevPos, Position currentPos, int index, Board boardCopy, Board solution,Listener l){
         Cell[][] cpMatrix = boardCopy.matrix;
         if(cpMatrix.length <= currentPos.row || currentPos.row < 0
            || cpMatrix[0].length <= currentPos.col || currentPos.col < 0) return false;
 
         int currentPosColor = cpMatrix[currentPos.row][currentPos.col].color;
 
         if(color == currentPosColor){
             if(!currentPos.equals(dots.get(index).getStart())){
                 if(currentPos.equals(dots.get(index).getEnd())){
                     if(dots.size() == index+1){
                         saveSolution(solution);
                         System.out.println("entro en el save solution"); // TODO borrar, es para debugger nomás
                         return true;
                     }else{
                         Dot nextDot = dots.get(index+1);
                         findInitialSolution(nextDot.getColor(), null, nextDot.getStart(), index+1, boardCopy, solution,l);
                     }
                 }
                 return false;
             }
             if(prevPos != null){
                 return false;
             }
         }else if(currentPosColor != -1) return false;
         cpMatrix[currentPos.row][currentPos.col].color = color;
 
         /*secci�n para imprimir con intervalos de a 100ms*/
         if(l!=null)	l.printToScreen();
 
         Direction[] dir = getOptimalDirArray(currentPos, dots.get(index).getEnd());
         Position nextPos;
         for(int i = 0; i < 4; i++){
             if( !(nextPos = currentPos.getPosition(dir[i])).equals(prevPos)){
                 boardCopy.dots.get(index).getPath().add(dir[i]);
                 if(findInitialSolution(color,currentPos,nextPos,index,boardCopy,solution,l)) return true;
                 boardCopy.dots.get(index).getPath().remove(dir[i]);
             }
         }
         cpMatrix[currentPos.row][currentPos.col].color = currentPosColor;
         return false;
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
 
 	public void addDot(Dot dot) {
 		dots.add(dot);
 	}
 
     public static void main(String args[]){
         if(args.length < 2){
             System.out.println("Arguments are lacking");
             return;
         }
         Board board;
         try{
             board = (new Parser()).parseLevel(args[0].toString());
         }catch(Exception e){
             e.getMessage();
             return;
         }
 
         if(args[1].equals("exact")){
             if(args.length > 2 && args[2].equals("progress")){
                 board.solve(null /*reemplazar por listener*/);
             }else{
                 board.solve(null);
             }
             return;
         }else if(args[1].equals("approx")){
             if(args.length > 2){
                 if(args.length > 3 && args[2].equals("progress")){
                     board.solveAprox(null, null /*reemplazar por valores!*/);
                     return;
                 }
                 board.solveAprox(null, null /*reemplazar por valores!*/);
                 return;
             }
             board.solveAprox(null, null);
         }else{
             throw new IllegalArgumentException();
         }
     }
 }
