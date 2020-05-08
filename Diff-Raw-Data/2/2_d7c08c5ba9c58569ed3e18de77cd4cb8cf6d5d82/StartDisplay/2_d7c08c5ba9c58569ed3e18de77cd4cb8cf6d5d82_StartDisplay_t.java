 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 
 public class StartDisplay {
 	
 	long lastFrame;
 	int fps;
 	long lastFPS;
 	int width;
 	int hight;
 	int mx;
 	int my;
 	boolean lMouse;
 	Board board = new Board();
 	MouseStuff m = new MouseStuff(this);
 	int z =0;
 	boolean selectedPieceForPawnReplacment = true;
 	int pawnPlaceForReplacment;
 	
 	public void start(int width, int hight, String title){
 		try {
 			Display.setDisplayMode(new DisplayMode(Display.getDesktopDisplayMode().getWidth() -15,Display.getDesktopDisplayMode().getWidth() -10));
 			Display.create();
 			Display.setTitle(title);
 			Display.setResizable(true);
 			
 			Mouse.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 		initGL(Display.getDesktopDisplayMode().getWidth()-15, Display.getDesktopDisplayMode().getHeight()-10); 
 		if(width < Display.getWidth() && hight < Display.getHeight()){
 			try {
 				Display.setDisplayMode(new DisplayMode(width,hight));
 			} catch (LWJGLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	
 		while (!Display.isCloseRequested()) {
 			if(!selectedPieceForPawnReplacment){
 				displayUpdate();
 				userUpdate();
 				renderPawnSelection();
 				checkPawnSelection();
 				Display.update();
 			}else{
 				renderGLBoard();
 				displayUpdate();
 				userUpdate();
 				renderGLEffect();
 				chessUpdate();
 				renderGLPieces();
 				Display.update();
 			}
 		}
 		Mouse.destroy();
 		Display.destroy();
 		
 	}
 	
 	
     private void renderGLEffect() {
 		if(((m.SelectedTile/10) + (m.SelectedTile-(m.SelectedTile/10)*10))%2 == 0){
 			GL11.glColor3f(0.9f, 0.9f, 0.2f);
 		}else{
 			GL11.glColor3f(0.7f, 0.7f, 0.0f);
 		}
 		new RenderTileEffect(width,hight,m.SelectedTile);
 		if(m.SelectedTile > -0.5){
 			if(board.pieceAt[m.SelectedTile/10][m.SelectedTile - (m.SelectedTile/10)*10].piece != Piece.Empty){
 				new RenderMoves(width,hight,m.SelectedTile,board);
 			}
 				
 		}
 		for(int i = 0; i <8; i++){
 			for(int j = 0;j<8;j++){
 				if(board.pieceAt[i][j].piece == Piece.KING && board.pieceAt[i][j].white == board.whiteTurn){
 					if(board.isPieceInDanger( i*10 + j, board)){
 						new RenderTileEffect(width, hight, (i*10 + j), 0.5f, 0.1f, 0.1f);
 					}
 				}
 			}
 		}
 		
 	}
 
 
 	private void userUpdate() {
 		m.setSelectedTile();
 		
 		
 	}
 
 	private void renderGLPieces() {
 		for(int i = 0; i<8;i++){
 			for(int j =0; j<8; j++){
 				if(board.pieceAt[i][j].piece != Piece.Empty){
 					if(board.pieceAt[i][j].white){
 						GL11.glColor3f(1f, 1f, 1f);
 					}else{
 						GL11.glColor3f(0f, 0f, 0f);
 					}
 					if(board.pieceAt[i][j].piece == Piece.PAWN){
 						new RenderPawn(width, hight, i, j);
 					}
 					if(board.pieceAt[i][j].piece == Piece.ROOK){
 						new RenderRook(width,hight,i,j);
 					}
 					if(board.pieceAt[i][j].piece == Piece.BISHOP){
 						new RenderBishop(width,hight,i,j);
 					}
 					if(board.pieceAt[i][j].piece == Piece.KNIGHT){
 						new RenderKnight(width,hight,i,j);
 					}
 					if(board.pieceAt[i][j].piece == Piece.QUEEN){
 						new RenderQueen(width,hight,i,j);
 					}
 					if(board.pieceAt[i][j].piece == Piece.KING){
 						new RenderKing(width,hight,i,j);
 					}
 				}
 			}
 		}
 		
 		
 	}
 
 	private void displayUpdate() {
 		width = (Display.getWidth()/8+1)*8;
 		hight = (Display.getHeight()/8+1)*8;
 		mx = Mouse.getX();
 		my = Mouse.getY();
 		lMouse = Mouse.isButtonDown(0);
 		//System.out.println("" +mx +" "+my +" " +lMouse);
 	}
 
 	private void chessUpdate() {
 		if(m.SelectedTile > -0.5f && m.LastSelectedTile > -0.5f){	
 			if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].white == board.whiteTurn){
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.PAWN){
 					CalcMovesPawn mp = new CalcMovesPawn(m.LastSelectedTile,board);
 					int[] a = mp.GetMoves();
 					for(int i=0;i<6;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 							int w;
 							if(board.pieceAt[m.SelectedTile/10][m.SelectedTile - (m.SelectedTile/10)*10].white){
 								w = 7;
 							}else{
 								w = 0;
 							}
 							if(m.SelectedTile - (m.SelectedTile/10)*10 == w){
 								pawnPlaceForReplacment =m.SelectedTile;
 								selectedPieceForPawnReplacment = false;
 								/*while(!Display.isCloseRequested() && !selectedPieceForPawnReplacment){
 									displayUpdate();
 									userUpdate();
 									renderPawnSelection();
 									checkPawnSelection();
 									Display.update();
 								}*/
 							}
 						}
 					}
 					for(int i=7; i<12;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 							if(m.SelectedTile - (m.SelectedTile/10)*10 == 5){
 								board.pieceAt[m.SelectedTile/10][(m.SelectedTile-(m.SelectedTile/10)*10)-1].piece  = Piece.Empty;
 							}else if(m.SelectedTile - (m.SelectedTile/10)*10 == 2){
								board.pieceAt[m.SelectedTile/10][(m.SelectedTile-(m.SelectedTile/10)*10)+1].piece  = Piece.Empty;
 							}else{
 								System.out.println("error when trying to preform al-pesant!");
 							}
 						}
 					}
 				}
 				
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.BISHOP){
 					CalcMovesBishop mb = new CalcMovesBishop(m.LastSelectedTile,board);
 					int[] a = mb.GetMoves();
 					for(int i=0;i<a.length;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 						}
 					}
 				}
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.ROOK){
 					CalcMovesRook mr = new CalcMovesRook(m.LastSelectedTile,board);
 					int[] a = mr.GetMoves();
 					for(int i=0;i<a.length;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 							if(m.SelectedTile == 00){
 								board.canCastleWL = false;
 							}
 							if(m.SelectedTile == 70){
 								board.canCastleWR = false;
 							}
 							if(m.SelectedTile == 07){
 								board.canCastleBL = false;
 							}
 							if(m.SelectedTile == 77){
 								board.canCastleBR = false;
 							}
 						}
 					}
 				}
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.QUEEN){
 					CalcMovesQueen mq = new CalcMovesQueen(m.LastSelectedTile,board);
 					int[] a = mq.GetMoves();
 					for(int i=0;i<a.length;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 						}
 					}
 				}
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.KING){
 					CalcMovesKing mk = new CalcMovesKing(m.LastSelectedTile,board);
 					int[] a = mk.GetMoves();
 					for(int i=0;i<45;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 							if(m.SelectedTile == 40){
 								board.canCastleWL =false;
 								board.canCastleWR = false;
 							}
 							if(m.SelectedTile == 47){
 								board.canCastleBL = false;
 								board.canCastleBR = false;
 							}
 						}
 					}
 					if(a[48] == 27 && m.SelectedTile == 27){
 						performMove(47,27,board);
 						performMove(07,37,board);
 						board.whiteTurn = !board.whiteTurn;
 					}
 					if(a[48] == 20 && m.SelectedTile == 20){
 						performMove(40,20,board);
 						performMove(00,30,board);
 						board.whiteTurn = !board.whiteTurn;
 					}
 					if(a[49] == 67 && m.SelectedTile == 67){
 						performMove(47,67,board);
 						performMove(77,57,board);
 						board.whiteTurn = !board.whiteTurn;
 					}
 					if(a[49] == 60 && m.SelectedTile == 60){
 						performMove(40,60,board);
 						performMove(70,50,board);
 						board.whiteTurn = !board.whiteTurn;
 					}
 				}
 				if(board.pieceAt[m.LastSelectedTile/10][m.LastSelectedTile - (m.LastSelectedTile/10)*10].piece == Piece.KNIGHT){
 					CalcMovesKnight mkn = new CalcMovesKnight(m.LastSelectedTile,board);
 					int[] a = mkn.GetMoves();
 					for(int i=0;i<a.length;i++){
 						if(a[i] == m.SelectedTile){
 							performMove(m.LastSelectedTile,m.SelectedTile,board);
 						}
 					}
 				}
 			}
 			}
 		}
 		
 
 	private void checkPawnSelection() {
 		if(m.SelectedTile == 22){
 			board.pieceAt[pawnPlaceForReplacment/10][pawnPlaceForReplacment-(pawnPlaceForReplacment/10)*10].piece = Piece.BISHOP;
 			selectedPieceForPawnReplacment =true;
 		}
 		if(m.SelectedTile == 25){
 			board.pieceAt[pawnPlaceForReplacment/10][pawnPlaceForReplacment-(pawnPlaceForReplacment/10)*10].piece = Piece.ROOK;
 			selectedPieceForPawnReplacment =true;
 		}
 		if(m.SelectedTile == 52){
 			board.pieceAt[pawnPlaceForReplacment/10][pawnPlaceForReplacment-(pawnPlaceForReplacment/10)*10].piece = Piece.KNIGHT;
 			selectedPieceForPawnReplacment =true;
 		}
 		if(m.SelectedTile == 55){
 			board.pieceAt[pawnPlaceForReplacment/10][pawnPlaceForReplacment-(pawnPlaceForReplacment/10)*10].piece = Piece.QUEEN;
 			selectedPieceForPawnReplacment =true;
 		}
 		
 	}
 
 
 	private void renderPawnSelection() {
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		
 		new RenderTileEffect(width, hight, 25, 0.4f, 0.4f, 0.4f);
 		new RenderTileEffect(width, hight, 22, 0.4f, 0.4f, 0.4f);
 		new RenderTileEffect(width, hight, 52, 0.4f, 0.4f, 0.4f);
 		new RenderTileEffect(width, hight, 55, 0.4f, 0.4f, 0.4f);
 		
 		if(!board.whiteTurn){
 			GL11.glColor3f(1f, 1f, 1f);
 		}else{
 			GL11.glColor3f(0f, 0f, 0f);
 		}
 		new RenderQueen(width, hight, 5, 5);
 		new RenderRook(width, hight, 2, 5);
 		new RenderKnight(width, hight, 5, 2);
 		new RenderBishop(width, hight, 2, 2);
 		
 	}
 
 
 	private void performMove(int lastSelectedTile, int selectedTile,Board board) {
 		board.pieceAt[selectedTile/10][selectedTile - (selectedTile/10)*10].piece = board.pieceAt[lastSelectedTile/10][lastSelectedTile - (lastSelectedTile/10)*10].piece;
 		board.pieceAt[selectedTile/10][selectedTile - (selectedTile/10)*10].white = board.pieceAt[lastSelectedTile/10][lastSelectedTile - (lastSelectedTile/10)*10].white;
 		board.pieceAt[lastSelectedTile/10][lastSelectedTile - (lastSelectedTile/10)*10].piece = Piece.Empty;
 		board.lastMoveFrom = lastSelectedTile;
 		board.lastMoveTo = selectedTile;
 		board.whiteTurn = !board.whiteTurn;
 	}
 
 
 	private void renderGLBoard() {
 				new RenderBoard(width,hight);
 				/*for(int i = 0; i <8; i++){
 					for(int j = 0;j<8;j++){
 						if(board.isTileInDangerTo(board.whiteTurn, i*10 + j)){
 							new RenderTileEffect(width, hight, (i*10 + j), 0.5f, 0.1f, 0.1f);
 						}
 					}
 				}*/
 		}
 		
 	
 
 
 	private void initGL(int width, int hight) {
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, width, 0, hight, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		//GL11.glEnable(GL11.GL_BLEND);
 		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	}
 
 }
