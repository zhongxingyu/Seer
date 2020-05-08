 package seg.project.checkers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JOptionPane;
 
 
 public class CheckerBoard  {
 	private CheckerSquare[][] grid; 
 	private ArrayList<CheckerSquare> redPieces;
 	private ArrayList<CheckerSquare> blackPieces;
 	private CheckerSquare selectedPiece;
 	private boolean pieceJumped;
 	public CheckerBoard(){
 		redPieces = new ArrayList<CheckerSquare>(12);
 		blackPieces = new ArrayList<CheckerSquare>(12);
 		grid = new CheckerSquare[8][8];
 		pieceJumped=false;
 		for(int i =0; i < 8;i++){
 			for(int u = 0;u<8;u++){
 				if(i%2==0){
 					if(u%2!=0)
 						if(i>=5){
 							grid[i][u]= new CheckerSquare(this,i,u,false);
 							redPieces.add(grid[i][u]);
 						}
 						else if(i <3){
 							grid[i][u] = new CheckerSquare(this,i,u,true);
 							blackPieces.add(grid[i][u]);
 							
 						}
 					}
 				
 				else{
 					if(u%2 == 0){
 						if(i>=5){
 							grid[i][u]= new CheckerSquare(this,i,u,false);
 							redPieces.add(grid[i][u]);
 						}else if(i <3){
 							grid[i][u] = new CheckerSquare(this,i,u,true);
 							blackPieces.add(grid[i][u]);
 						}
 					}
 				}
 			}
 		}
 	}
 	public boolean validateMove(int oldX, int oldY, int newX, int newY) {
 		boolean black = !CheckerGame.getInstance().isBlack();
 		if(isValidNonjump(oldX, oldY, newX, newY)){
 			if(canJump(black))
 				return false;
 			CheckerSquare square = grid[oldX][oldY];
 			if(square == null) 
 				return false;
 			if((black && !square.isBlack()) || (!black && square.isBlack()))
 				return false;
 			
 			grid[newX][newY] = square;
 			square.setxPos(newX);
 			square.setyPos(newY);
 			square.setPieceSelected(false);
 			this.crownKing(newX, newY);
 			grid[oldX][oldY]=null;
 			return true;
 		}
 		if(isValidjump(oldX,oldY,newX, newY)){
 			CheckerSquare square = grid[oldX][oldY];
 			if(square == null) 
 				return false;
 			if((black && !square.isBlack()) || (!black && square.isBlack()))
 				return false;
 			int dX = sign(newX-oldX);
 			int dY = sign(newY-oldY);
 			CheckerSquare toRem = grid[oldX + dX][oldY+dY];
 			if(square.isBlack())
 				redPieces.remove(toRem);
 			else
 				blackPieces.remove(toRem);
 			grid[oldX + dX][oldY+dY] = null;//Jump piece
 			grid[oldX + 2*dX][oldY+2*dY] = square; // move square
 			square.setxPos(oldX + 2*dX);
 			square.setyPos(oldY+2*dY);
 			square.setPieceSelected(false);
 			this.crownKing(oldX + 2*dX, oldY+2*dY);
 			grid[oldX][oldY]=null;
 			return true;
 		}
 		return false;
 		}
 
 	
 	public void performMove(int x, int y){
 		if(!CheckerGame.getInstance().isTurn()){
 			notify("It is not your turn, please wait");
 			return;
 		}
 		int oldX = 0;
 		int oldY = 0;
 		if(selectedPiece != null)
 		{
 			oldX =selectedPiece.getxPos();
 			oldY = selectedPiece.getyPos();
 		}
 		boolean black = CheckerGame.getInstance().isBlack();
 		if(selectedPiece == null || (selectedPiece != null &&oldX == x &&oldY == y)){
 			CheckerSquare square = grid[x][y];
 			if(square == null){ 
 				notify("There is no piece at location X-" +x + " Y-" + y);
 				return;
 			}
 			if((black && !square.isBlack()) || (!black && square.isBlack())){
 				notify("That is not your piece at location X-" +x + " Y-" + y);
 				return;
 			}
 			square.setPieceSelected(!square.isPieceSelected());
 			if(selectedPiece==null)
 				selectedPiece  = square;
 			else{
 				selectedPiece = null;
 				if(pieceJumped)
 				{
 					pieceJumped = false;
					CheckerGame.getInstance().sendCommand("Done");
 				}
 			}
 			notify(null);
 			return;
 		}
 		else if(isValidNonjump(oldX, oldY, x, y)){
 			if(canJump(black))
 			{
 				notify("You must make a jump, since it is possible.");
 				return;
 			}
 			CheckerSquare square = grid[oldX][oldY];
 			if(square == null){
 				notify("Invalid selected piece at X-"+oldX+ " Y-" +oldY);
 				return;
 			}
 			if((black && !square.isBlack()) || (!black && square.isBlack())){
 				notify("It is not your piece selected at X-"+oldX+ " Y-" +oldY);
 				return;
 			}
 			grid[x][y] = square;
 			int ox = square.getxPos(), oy = square.getyPos();
 			square.setxPos(x);
 			square.setyPos(y);
 			square.setPieceSelected(false);
 			selectedPiece = null;
 			this.crownKing(x, y);
 			grid[oldX][oldY]=null;
 			CheckerGame.getInstance().sendCommand("move:"+ox+":"+oy+":"+x+":"+y);
 			CheckerGame.getInstance().sendCommand("done");
 			notify("You made move from X-"+ox+" Y-"+oy+" to X-"+x+" Y-"+y  );
 			return;
 		}
 		else if(isValidjump(oldX,oldY,x , y)){
 			CheckerSquare square = grid[oldX][oldY];
 			if(square == null){
 				notify("Invalid selected piece at X-"+oldX+ " Y-" +oldY);
 				return;
 			}
 			if((black && !square.isBlack()) || (!black && square.isBlack())){
 				notify("It is not your piece selected at X-"+oldX+ " Y-" +oldY);
 				return;
 			}
 			int dX = sign(x-oldX);
 			int dY = sign(y-oldY);
 			int ox =oldX, oy =oldY;
 			CheckerSquare toRem = grid[oldX + dX][oldY+dY];
 			if(square.isBlack())
 				redPieces.remove(toRem);
 			else
 				blackPieces.remove(toRem);
 			grid[oldX + dX][oldY+dY] = null;//Jump piece
 			grid[oldX + 2*dX][oldY+2*dY] = square; // move square
 			square.setxPos(oldX + 2*dX);
 			square.setyPos(oldY+2*dY);
 			this.crownKing(oldX + 2*dX, oldY+2*dY);
 			grid[oldX][oldY]=null;
 			if(canJump(square)){
 				pieceJumped = true;
 				notify("You made jump from X-"+ox+" Y-"+oy+" to X-"+x+" Y-"+y+ " it is still your turn");
 				CheckerGame.getInstance().sendCommand("move:"+ox+":"+oy+":"+x+":"+y);
 				return;
 			}
 			square.setPieceSelected(false);
 			selectedPiece = null;
 			CheckerGame.getInstance().sendCommand("move:"+ox+":"+oy+":"+x+":"+y);
 			CheckerGame.getInstance().sendCommand("done");
 			notify("You made jump from X-"+ox+" Y-"+oy+" to X-"+x+" Y-"+y  );
 			if(win(black))
 			{
 				JOptionPane.showMessageDialog(null,"You won!");
 				System.exit(0);
 			}
 			return;
 		}
 		notify("Invalid move at X-"+x+" Y-"+y);
 		return;
 	}
 	public boolean canJump(boolean black) {
 		boolean can = false;
 		if(black){
 			Iterator<CheckerSquare> iter = blackPieces.iterator();
 			while(iter.hasNext() && !can){
 				can = canJump(iter.next());
 			}
 		}
 		else{
 			Iterator<CheckerSquare> iter = redPieces.iterator();
 			while(iter.hasNext() && !can){
 				can = canJump(iter.next());
 			}
 		}
 		return can;
 	}
 	public boolean canJump(CheckerSquare piece) {
 		if(this.isValidjump(piece.getxPos(), piece.getyPos(), piece.getxPos()+1, piece.getyPos()+1))
 			return true;
 		else if(this.isValidjump(piece.getxPos(), piece.getyPos(), piece.getxPos()+1, piece.getyPos()-1))
 			return true;
 		else if(this.isValidjump(piece.getxPos(), piece.getyPos(), piece.getxPos()-1, piece.getyPos()+1))
 			return true;
 		else if(this.isValidjump(piece.getxPos(), piece.getyPos(), piece.getxPos()-1, piece.getyPos()-1))
 			return true;
 		return false;
 	}
 	
 	public ArrayList<CheckerSquare> getBlackPieces() {
 		return blackPieces;
 	}
 	public ArrayList<CheckerSquare> getRedPieces() {
 		return redPieces;
 	}
 	public CheckerSquare[][] getGrid() {
 		// TODO Auto-generated method stub
 		return grid;
 	}
 	public boolean win(boolean black){
 		if(blackPieces.isEmpty() && !black)
 			return true;
 		else if(redPieces.isEmpty() && black)
 			return true;
 		return false;
 			
 	}
 	
 	private boolean isValidjump(int oldX, int oldY, int newX, int newY) {
 		int xPos = newX - oldX;
 		int yPos = newY - oldY;
 		try{
 		CheckerSquare square = grid[oldX][oldY];
 		if(square == null)
 			return false;
 		
 		int absX = Math.abs(xPos);
 		int absY = Math.abs(yPos);
 		
 		if (absX != absY) return false;
         if (absX > 2) return false;
         if (absY > 2) return false;
        
         
         CheckerSquare jumpSquare = grid[oldX+sign(xPos)][oldY+sign(yPos)];
         
         CheckerSquare landSquare = grid[oldX+2*sign(xPos)][oldY+2*sign(yPos)];
         
         if(jumpSquare == null)return false;
         if(square.isBlack() == jumpSquare.isBlack())return false;
  
         boolean king = square.isKing();
         boolean invalidRedMove = ((!grid[oldX][oldY].isBlack()) && xPos > 0);
         boolean invalidBlackMove = grid[oldX][oldY].isBlack() && xPos < 0;
         if(!king && (invalidRedMove || invalidBlackMove) )
 				return false;
         
         if(landSquare != null)return false;
         return (true);
 		}
 		catch( java.lang.ArrayIndexOutOfBoundsException e){
 			return false;
 		}
 	}
 	
 	private int sign(int value){
 		if (value > 0)
             return 1;
         if (value < 0)
             return -1;
         return value;
 	}
 	
 	private void crownKing(int newX, int newY){
 		if ((newX == 7) && grid[newX][newY].isBlack()) {
 			grid[newX][newY].setKing(true);
 			return;
 		}
 
 		if ((newX == 0) && !grid[newX][newY].isBlack()) {
 			grid[newX][newY].setKing(true);
 			return;
 		}
 	}
 	private void notify(String mes){
 		if(mes != null){
 			CheckerGame.getInstance().addText(mes);
 		}
 		CheckerGame.getInstance().notifyObservers(null);	
 	}
 	private boolean isValidNonjump(int oldX, int oldY, int newX, int newY) {
 		int xPos = newX - oldX;
 		int yPos = newY - oldY;
 		try{
 		// Return false if the move is not a move to an adjacent row and column,
 		if (Math.abs(xPos) != 1)
 			return false;
 		if (Math.abs(yPos) != 1)
 			return false;
 		
 		if(grid[oldX][oldY] == null)
 			return false;
 		if(grid[newX][newY] != null)
 			return false;
 		// Return true if this is a King
 		if (grid[oldX][oldY].isKing())
 			return true;
 
 		// The piece is not a king. Return value of the piece moves forward
 		return ((!grid[oldX][oldY].isBlack()) && xPos < 0)
 				|| (grid[oldX][oldY].isBlack() && xPos > 0);
 		}
 		catch( java.lang.ArrayIndexOutOfBoundsException e){
 			return false;
 		}
 	}
 }
