 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import java.util.Timer; 
 import java.util.TimerTask;
 
 
 public class Board {
 
 	//		  	size	mines	opened
 	//	beg 	8x8 	10		54
 	//	med 	16x16 	40		216
 	//	exp 	30x16 	99		381
 
 	boolean doneAnimating = true;
 	GameDisplay game;
 	CheckList pressed;
 	CheckList bombs;
 	CheckList zeros;
 	CheckList fast;
 	Mine board[][];
 	Thread t;
 	Thread f;
 
 	int width; 
 	int height;
 
 	int flagCount;
 	int unsafeBombCount;
 	int zeroCount;
 	int fastCount;
 
 	int flagLimit;
 	int totalBombs;
 	int totalBoxes;
 
 	int startX;
 	int startY;
 
 	int tileSize = 26;
 	boolean win = false;
 	boolean lose = false;
 
 	boolean firstTurn = true;
 	boolean touchedBomb = false;
 	boolean speedTrick = false;
 
 	boolean checkBoard = false; 
 	boolean showHint = false; 
 	boolean showCheck = false;
 	boolean questionMarks = false;
 	boolean compactMode = false;
 
 	int timeCounter;
 
 	Timer timer;
 	TimerTask tt;
 
 	Font font = new Font("SANS_SERIF", Font.BOLD,10);
 	Font compactFont = new Font("SANS_SERIF", Font.BOLD,8);
 
 	public Mine[][] getBoard(){
 
 		return board;
 	}
 
 	public Board(int width, int height, GameDisplay g){
 
 		game = g;
 		this.width = width;
 		this.height = height;
 		bombs = new CheckList();
 
 		startup();
 	}
 
 	public int getWidth(){
 
 		return width;
 	}
 
 	public void setWidth(int a){
 
 		width = a;
 	}
 
 	public int getHeight(){
 
 		return height;
 	}
 
 	public void setHeight(int a){
 
 		height = a;
 	}
 
 	public int getTotalBombs(){
 
 		return totalBombs;
 	}
 
 	public void setTotalBombs(int a){
 
 		totalBombs = a;
 	}
 
 	public int getTotalBoxes(){
 
 		return totalBoxes;
 	}
 
 	public boolean getTouchedBomb(){
 
 		return touchedBomb;
 	}
 
 	public int getFlagCount(){ //Let's play access flags
 
 		return flagCount;
 	}
 
 	public boolean getWin(){
 
 		return win;
 	}
 
 	public boolean getLose(){
 
 		return lose;
 	}
 
 	public int getWindowX(){
 
 		int windowX = (tileSize+1)*getWidth()+1;
 
 		return windowX;
 	}
 
 	public int getWindowY(){
 
 		int windowY = (tileSize+1)*getHeight()+80;
 
 		return windowY;
 	}
 
 	public boolean getFirstTurn(){
 
 		return firstTurn;
 	}
 
 	public int getStartX(){
 
 		return startX;
 	}
 
 	public int getStartY(){
 
 		return startY;
 	}
 
 	public void setUp(){
 
 		flagCount = totalBombs;
 		unsafeBombCount = totalBombs;
 		flagLimit=flagCount;
 
 	}
 
 	public void check(){
 
 		checkBoard = true;
 
 	}
 
 	public boolean doneAnimating()
 	{
 		return doneAnimating;
 	}
 
 	public void setDoneAnimating(boolean a)
 	{
 		doneAnimating = a;
 	}
 
 	public boolean checkBoard(){
 		
 		boolean correct = true;		
 
 		for(int y =0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				if(board[x][y].isFlagged()&&!board[x][y].isBomb())
 					correct = false;
 			}
 		}
 
 		return correct;
 
 	}
 
 	public boolean alreadyThere(int x, int y){
 
 		return pressed.alreadyHead(x, y);
 
 	}
 
 	public void replace(int x, int y){
 
 		pressed.replaceHead(x, y);
 
 	}
 
 	public void add(int x, int y){
 
 		pressed = new CheckList();
 		pressed.enque(x, y);
 
 	}
 
 	public void press(int x, int y){
 
 		board[x][y].setPressed(true);
 	}
 
 	public void resetPressed(){
 
 		try{
 			if(pressed.getHead()!=null)
 				board[pressed.getHead().getXCord()][pressed.getHead().getYCord()].setPressed(false);
 		}
 		catch(Exception e){
 
 		}
 	}
 
 	public int getUnsafeBombCount(){
 
 		return unsafeBombCount;
 	}
 
 	public void wipeBoard(){
 
 		for(int y=0; y<height; y++ ){
 			for(int x=0; x<width; x++){
 
 				board[x][y] = new Mine (false, 0, false, false, false, false, false, false);
 
 			}
 		}
 
 	}
 
 	public void startup(){
 
 		bombs.empty();
 
 		board = new Mine[width][height];
 
 		totalBoxes = width*height;
 
 		flagCount = totalBombs;
 		unsafeBombCount = totalBombs;
 		flagLimit=flagCount;
 
 		timeCounter = 0;
 
 
 		win = false;
 		lose = false;
 
 		firstTurn = true;
 		touchedBomb = false;
 
 	}
 
 	public boolean flagged(int x, int y){
 
 		boolean flag = false;
 		if(board[x][y].isFlagged())
 			flag = true;
 
 		return flag;
 
 	}
 
 	public void open(int x, int y){ //Play can't access Mines so it is a double method
 
 		openBox(x,y);
 
 	}
 
 	public boolean bomb(int x, int y){ //Play can't access Mines so it is a double method
 
 		return board[x][y].isBomb();
 	}
 
 	public void initializeBoard(){
 
 		for(int y=0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				board[x][y] = new Mine (false, 0, false, false, false, false, false, false); 
 
 			}
 		}
 
 
 	}
 
 
 
 	public void setStartXandY(int x, int y){
 
 		startX = x;
 		startY = y;
 
 		placeBombs();
 		placeBombsSurrounding();
 
 
 		timer = new Timer();
 
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				timeCounter++;
 				game.repaint();
 			}
 
 		}, 0, 1000);
 	}
 
 
 	public void endTimer()
 	{
 		timer.cancel();
 		timer.purge();
 	}
 
 	public void placeBombs(){
 
 		//bombs = new CheckList();
 
 		int count = totalBombs;
 
 		while(count > 0){
 			int rand1 = (int) (Math.random() * width); 
 			int rand2 = (int) (Math.random() * height); //to get the y (rectangle ones)
 
 			if(!board[rand1][rand2].isBomb() && notSurroundingStart(rand1,rand2)){
 				board[rand1][rand2].setBomb(true);
 				bombs.enque(rand1,rand2);
 				count--;
 			}
 		}
 	}
 
 	public void placeBombsSurrounding(){
 
 		for(int y=0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				if(board[x][y].isBomb()){
 
 					for(int j=-1; j<2; j++){			
 						for(int i=-1; i<2; i++){
 
 							if(isValid(x+j,y+i)&& !board[x+j][y+i].isBomb())
 								board[x+j][y+i].addOneBombSurrounding();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean isValid(int x, int y){
 
 		boolean valid = false;
 
 		try{
 			if(board[x][y]!=null)
 				valid = true;
 		}
 		catch(Exception e){
 
 		}
 		return valid;
 	}
 
 	public boolean isOpen(int x,int y){
 
 		boolean open = false;
 
 		if(board[x][y].isOpened())
 			open = true;
 
 		return open;
 	}
 
 	public boolean unopenedAround(int x, int y){
 
 		boolean unopened = false;
 
 		for(int i=-1; i<2; i++){			
 			for(int j=-1; j<2; j++){
 
 				if(isValid(x+j,y+i) && !board[x+j][y+i].isOpened())
 					unopened = true;
 
 			}
 		}
 
 		return unopened;
 	}
 
 	public boolean unopenedAndUnflaggedAround(int x, int y){
 
 		boolean unopened = false;
 
 		for(int i=-1; i<2; i++){			
 			for(int j=-1; j<2; j++){
 
 				if(isValid(x+j,y+i) && !board[x+j][y+i].isOpened() && !board[x+j][y+i].isFlagged() )
 					unopened = true;
 
 			}
 		}
 
 		return unopened;
 	}
 
 	public void fastClick(int x, int y){
 
 		fast = new CheckList();
 		fastCount =0;
 
 		if(flagsSurrounding(x,y) == board[x][y].getBombsSurrounding()){
 
 			for(int i=-1; i<2; i++){			
 				for(int j=-1; j<2; j++){
 
 					if(isValid(x+j,y+i) && !board[x+j][y+i].isOpened() && !board[x+j][y+i].isFlagged()){
 
 
 						if(board[x+j][y+i].isBomb()){
 							bombs.remove(x+j,y+i);
 							lose = true;
 							endOfGame();
 						}
 						fast.enque(x+j, y+i);
 						fastCount++;
 
 					}
 				}
 			}
 		}
 		fastAnimation();
 		checkWin();
 	}
 
 	public void fastAnimation(){
 
 		f = new Thread( new Runnable(){
 			public void run(){
 
 				int acceleration =25;
 
 				while(fastCount>0){
 					try {
 						Thread.sleep(acceleration);
 					} catch(InterruptedException ex) {
 						Thread.currentThread().interrupt();
 					}
 
 					tileOpen();
 					fastCount--;
 				}
 			}
 		});
 
 		f.start();
 
 	}
 
 	public void tileOpen(){
 
 		openBox(fast.getValues()[0],fast.getValues()[1]);
 
 		fast.deque();
 
 		game.repaint();
 	}
 
 	public void finishFlagging(){
 
 		for(int y=0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				if(!board[x][y].isOpened()&&!board[x][y].flagged)
 					board[x][y].setFlagged(true);
 			}
 		}
 
 		flagCount = 0; 
 
 	}
 
 	public void openZeros(int x, int y){
 
 		int tempX = x;
 		int tempY = y;
 
 		zeros = new CheckList();
 		CheckList check = new CheckList();
 		check.enque(x,y);
 
 		FullList full = new FullList();
 		full.add(x,y);
 
 		while (check.getHead()!=null){
 
 			check.deque();
 
 			for(int i=-1; i<2; i++){			
 				for(int j=-1; j<2; j++){
 
 					if(isValid(tempX+i,tempY+j)&& 
 							!full.alreadyInFullList(tempX+i, tempY+j)&&
 							!board[tempX+i][tempY+j].isOpened()&&
 							!board[tempX+i][tempY+j].isFlagged()){
 
 						zeros.enque(tempX+i,tempY+j);
 						zeroCount++;
 						;
 
 						full.add(tempX+i, tempY+j); //add coordinates to not repeat
 
 						if((board[tempX+i][tempY+j].getBombsSurrounding()==0)){ 
 
 							check.enque(tempX+i,tempY+j);
 
 						}
 					}
 				}
 
 			}
 			if(check.getHead()!=null){
 
 				tempX = check.getValues()[0];
 				tempY = check.getValues()[1];
 			}
 		}
 	}
 
 	public void tileAnimation(){
 
 		t = new Thread( new Runnable(){
 			public void run(){
 
 				doneAnimating = false;
 
 				int acceleration = 2;
 
 				while(zeroCount>0){
 
 					try {
 						Thread.sleep(acceleration);
 					} catch(InterruptedException ex) {
 						Thread.currentThread().interrupt();
 					}
 
 					tileTurn();
 
 					zeroCount--;
 				}
 				doneAnimating = true;
 			}
 		});
 
 		t.start();
 	}
 
 	public void tileTurn(){
 		try{
 			board[zeros.getValues()[0]][zeros.getValues()[1]].setOpened(true);
 
 			zeros.deque();
 		}
 		catch(Exception e){
 
 		}
 		game.repaint();
 	}
 
 	public void markFlagged(int x, int y){
 
 		if(!board[x][y].opened){
 			if(board[x][y].isFlagged()){
 
 				removeFlag(x,y);
 				if(questionMarks)
 					questionMark(x,y);
 				else
 					removeQuestionMark(x,y);
 
 			}
 
 			else if(board[x][y].isQuestionMarked()&& questionMarks){
 
 				removeQuestionMark(x,y);
 
 			}
 
 			else{ //flagCount>0 && 
 
 				board[x][y].setFlagged(true);
 
 				flagCount--;
 			}
 		}
 
 	}
 
 	public boolean notSurroundingStart(int x, int y){
 
 		boolean notSurrounded = true;
 
 		for(int i=-1; i<2; i++){
 			for(int j=-1; j<2; j++){
 
 				if(x+j==startX && y+i==startY){
 
 					notSurrounded = false;
 					break;
 				}
 
 			}
 			if(!notSurrounded)
 				break;
 		}
 
 		return notSurrounded;
 	}
 
 	public void removeFlag(int x, int y){
 
 		if(flagCount<flagLimit){
 
 			board[x][y].setFlagged(false);
 
 			flagCount++;
 
 
 		}
 	}
 
 	public void setQuestionMarks(boolean a){
 
 		questionMarks = a;
 	}
 
 	public boolean questionMarksEnabled(){
 
 		return questionMarks;
 
 	}
 
 	public void questionMark(int x, int y){
 
 		board[x][y].setQuestionMarked(true);
 
 	}
 
 	public void removeQuestionMark(int x, int y){
 
 		board[x][y].setQuestionMarked(false);
 	}
 
 	public boolean compactModeEnabled(){
 
 		return compactMode;
 
 	}
 
 	public void setCompactMode(boolean a){
 
 		compactMode = a;
 	}
 
 	public void setTileSize(int a){
 
 		tileSize = a;
 	}
 
 	public int flagsSurrounding(int x, int y){
 
 		int flags = 0;
 
 		for(int i=-1; i<2; i++){			
 			for(int j=-1; j<2; j++){
 
 				if(isValid(x+j,y+i) && !board[x+j][y+i].isOpened()){
 
 					if(board[x+j][y+i].isFlagged())
 						flags++;
 				}
 			}
 		}
 		return flags;
 	}
 
 
 	public int unopenedSurrounding(int x, int y){
 
 		int unopened = 0;
 
 		for(int i=-1; i<2; i++){			
 			for(int j=-1; j<2; j++){
 
 				if(isValid(x+j,y+i) && !board[x+j][y+i].isOpened()){
 
 					if(!board[x+j][y+i].isOpened())
 						unopened++;
 				}
 			}
 		}
 		return unopened;
 	}
 
 
 	public int[] getHint(){
 
 		int xHint =-1; 
 		int yHint =-1;
 
 		for(int y=0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				Mine b = board[x][y];
 
				if(b.isOpened() && b.getBombsSurrounding()>0)
				{
 					if (flagsSurrounding(x,y)== b.getBombsSurrounding()&&unopenedAndUnflaggedAround(x, y)
							|| ((unopenedSurrounding(x,y) == board[x][y].getBombsSurrounding())
									&& flagsSurrounding(x,y) != board[x][y].getBombsSurrounding())){
 						xHint = x;
 						yHint = y;
 					}
 				}
 			}
 		}
 
 		int[] hint = {xHint, yHint};
 		return hint;
 
 	}
 
 	public void hint(){
 
 		showHint = true;
 
 	}
 	
 	public void removeHint(){
 
 		showHint = false;
 	}
 	
 	public void setCheck(boolean a)
 	{
 		showCheck = a;
 	}
 
 	public void openBomb(){
 
 		if(board[bombs.getValues()[0]][bombs.getValues()[1]].isBomb())
 			board[bombs.getValues()[0]][bombs.getValues()[1]].setOpened(true);
 
 		//		else
 		//			board[bombs.getValues()[0]][bombs.getValues()[1]].setWrong(true);
 
 		bombs.deque();
 
 
 	}
 
 	public boolean isEmpty(){
 
 		boolean empty = false;
 
 		try{
 			if(pressed.getHead()==null)
 				empty = true;
 		}
 		catch(Exception e){
 
 		}
 
 		return empty;
 	}
 
 
 	public void openBox(int x, int y){
 
 		if(t!=null){
 
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 
 				e.printStackTrace();
 			}
 		}
 		if(board[x][y].isFlagged())
 			removeFlag(x,y);
 
 		else if(firstTurn){
 
 			setStartXandY(x,y);
 
 			firstTurn = false;
 			openBox(x,y);
 
 		}
 		else{
 
 			if(!board[x][y].isOpened()){
 
 				board[x][y].setOpened(true);
 
 				if(board[x][y].isBomb()){
 
 					touchedBomb = true;
 					lose = true;
 
 					bombs.remove(x,y); //so it won't print twice
 					bombs.enque(x, y); //first to print
 
 					endOfGame();
 
 
 				}
 
 				else if(board[x][y].getBombsSurrounding()==0){
 					openZeros(x, y);
 					tileAnimation();
 
 				}
 				else;
 			}
 		}
 		checkWin();
 	}
 
 	void checkWin(){
 
 		if(getOpenedBoxCount() == totalBoxes - totalBombs){
 			win = true;
 			if(flagCount >0){
 				finishFlagging();
 			}
 
 		}
 
 	}
 
 	public int getOpenedBoxCount(){
 
 		int opened = 0;
 
 		for(int y=0; y<height; y++){
 			for(int x=0; x<width; x++){
 
 				if(board[x][y].isOpened())
 					opened++;
 			}
 		}
 
 		return opened;
 	}
 
 	public void endOfGame(){
 
 		for(int i=0; i<height; i++){
 			for(int k=0; k<width; k++){
 
 
 
 				if(board[k][i].isFlagged()&&board[k][i].isBomb()){
 					bombs.remove(k,i);
 					//					unsafeBombCount--;
 				}
 
 				else if(board[k][i].isFlagged()&&!board[k][i].isBomb()){
 					board[k][i].setWrong(true);
 				}
 
 			}
 		}
 	}
 
 	public boolean gameOver(){
 
 		boolean gameOver = false;
 
 		if(lose || win)
 			gameOver = true;
 
 		return gameOver;
 	}
 
 	public String gameOverMessage(){
 
 		String gameOver;
 
 		if(win)
 		{
 			if(!compactMode)
 				gameOver = "Game Over! You win! Time: "+timeCounter; //timer*1000;
 			else
 				gameOver = "Game over you win! Time: "+timeCounter; //timer*1000;
 		}
 		else
 		{
 			if(!compactMode)
 				gameOver = "Game Over! You Lose! "+timeCounter;
 			else
 				gameOver = "Game over you lose! Time: "+timeCounter; //timer*1000;
 		}
 
 		return gameOver;
 	}
 
 
 	public void paintBoard(Graphics g){
 
 		if(speedTrick)
 			g.drawString("", 1, 1);
 		else
 			speedTrick = true;
 
 		int[] xArray = new int[3];
 		int[] yArray = new int[3];
 
 		int ySpacing = 0; 
 
 		for(int y=0; y<height; y++){
 
 			int xSpacing = 0;
 
 			for(int x=0; x<width; x++){
 
 				if(!board[x][y].isOpened()&&!board[x][y].isWrong()){
 					int[] xLightTri ={xSpacing+1,xSpacing+1,xSpacing+(tileSize+2)};
 					int[] yLightTri ={ySpacing+1,ySpacing+(tileSize+2),ySpacing+1};
 
 					int[] xDarkTri ={xSpacing+(tileSize+2),xSpacing+(tileSize+2),xSpacing+1};
 					int[] yDarkTri ={ySpacing+(tileSize+2),ySpacing+1,ySpacing+(tileSize+2)};
 
 					g.setColor(Color.LIGHT_GRAY);
 					g.fillPolygon(xLightTri, yLightTri, 3);
 
 					g.setColor(Color.BLACK);
 					g.fillPolygon(xDarkTri, yDarkTri, 3);
 
 					g.setColor(Color.GRAY);
 					g.fillRect(xSpacing+1+3, ySpacing+1+3, (tileSize+1)-6, (tileSize+1)-6);	
 
 				}
 
 				xSpacing+=(tileSize+1);
 			}
 			ySpacing+=(tileSize+1);
 		}
 
 		Color color1 = new Color(140,140,140,128);
 		Color color2 = new Color(39,39,39,128);
 		Color color3 = new Color(200,200,200,127);;
 
 		Graphics2D g2d = (Graphics2D)g;
 		GradientPaint gp = new GradientPaint(0, 0, color1, getWindowX(), getWindowY()-78, color2);
 		g2d.setPaint(gp);
 		g.fillRect( 0, 0, getWindowX(), getWindowY()-78);	
 		//g2d.fillRect(xSpacing+1, ySpacing+1, (tileSize), (tileSize));
 
 		ySpacing =0;
 
 		for(int y=0; y<height; y++){
 
 			int xSpacing = 0;
 
 			for(int x=0; x<width; x++){
 
 				g.setColor(board[x][y].determineColor());
 
 				if(!board[x][y].isOpened()){
 
 					if(board[x][y].isWrong()){ //draws X					
 						g.setColor(color1);
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 						g.setColor(Color.GRAY);
 						g.drawRect(xSpacing+1, ySpacing+1, (tileSize), (tileSize));
 						g.setColor(Color.WHITE);
 						g.setFont(font);
 						if(compactMode)
 							g.drawString("X", xSpacing+8, ySpacing+17);
 						else
 							g.drawString("X", xSpacing+11, ySpacing+19);
 					}
 
 					else if(board[x][y].isFlagged()){
 
 						if(compactMode){
 							xArray[0] = xSpacing+8; //top left
 							yArray[0] = ySpacing+6;
 
 							xArray[1] = xSpacing+17; //top mid
 							yArray[1] = ySpacing+9;
 
 							xArray[2] = xSpacing+7; //bottom right
 							yArray[2] = ySpacing+13;
 						}
 
 						else{
 							xArray[0] = xSpacing+10; //top left
 							yArray[0] = ySpacing+8;
 
 							xArray[1] = xSpacing+23; //top mid
 							yArray[1] = ySpacing+13;
 
 							xArray[2] = xSpacing+10; //bottom right
 							yArray[2] = ySpacing+16;
 						}
 
 						g.setColor(Color.RED);
 						g.fillPolygon(xArray,yArray, 3);
 						g.setColor(color3);
 						g.fillPolygon(xArray,yArray, 3);
 						g.setColor(Color.BLACK);
 						if(compactMode)
 						{
 							g.drawLine(xSpacing+7, ySpacing+6, xSpacing+7, ySpacing+17);
 							g.drawLine(xSpacing+8, ySpacing+6, xSpacing+8, ySpacing+17);
 						}
 						else{
 							g.drawLine(xSpacing+9, ySpacing+8, xSpacing+9, ySpacing+22);
 							g.drawLine(xSpacing+10, ySpacing+8, xSpacing+10, ySpacing+22);
 
 						}
 					}
 
 					else if(board[x][y].beingPressed()){
 						g.setColor(Color.GRAY);
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 					}
 
 					else if(board[x][y].isQuestionMarked()){
 						g.setColor(Color.WHITE);
 						g.setFont(font);
 						if(compactMode)
 							g.drawString("?", xSpacing+8, ySpacing+16);
 						else
 							g.drawString("?", xSpacing+11, ySpacing+19);
 					}
 
 				}
 				else{
 
 					if(board[x][y].isBomb()){
 
 						g.setColor(color1);
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 
 						g.setColor(Color.BLACK);
 
 						if(compactMode){
 							g.fillOval(xSpacing+9-2, ySpacing+9-2, 10, 10);
 							
 							g.drawLine(xSpacing+8-2, ySpacing+8-2, xSpacing+21-2, ySpacing+21-2); //top left/bottom right
 							g.drawLine(xSpacing+8-2, ySpacing+8-2-1, xSpacing+21-2, ySpacing+21-2-1); //top left/bottom right
 							g.drawLine(xSpacing+8-2, ySpacing+8-2+1, xSpacing+21-2, ySpacing+21-2+1); //top left/bottom right
 
 							g.drawLine(xSpacing+5-2, ySpacing+14-2, xSpacing+23-2, ySpacing+14-2);//mid 
 							g.drawLine(xSpacing+5-2, ySpacing+14-1, xSpacing+23-2, ySpacing+14-1);//mid 
 
 							
 							g.drawLine(xSpacing+21-2, ySpacing+8-2, xSpacing+8-2, ySpacing+21-2);//top right/bottom left
 							g.drawLine(xSpacing+21-2, ySpacing+8-2-1, xSpacing+8-2, ySpacing+21-2-1);//top right/bottom left
 							g.drawLine(xSpacing+21-2, ySpacing+8-2+1, xSpacing+8-2, ySpacing+21-2+1);//top right/bottom left
 							
 							g.drawLine(xSpacing+14-2, ySpacing+5-2, xSpacing+14-2, ySpacing+23-2);//top/down
 							g.drawLine(xSpacing+14-1, ySpacing+5-2, xSpacing+14-1, ySpacing+23-2);//top/down
 
 						}
 						else{
 							
 							g.fillOval(xSpacing+9, ySpacing+9, 11, 11);
 							
 							g.drawLine(xSpacing+7, ySpacing+7, xSpacing+22, ySpacing+22); //top left/bottom right
 							g.drawLine(xSpacing+7, ySpacing+7-1, xSpacing+22, ySpacing+22-1); //top left/bottom right
 							g.drawLine(xSpacing+7, ySpacing+7+1, xSpacing+22, ySpacing+22+1); //top left/bottom right
 
 
 							g.drawLine(xSpacing+5-2, ySpacing+14, xSpacing+23+2, ySpacing+14);//mid 
 							g.drawLine(xSpacing+5-2, ySpacing+15, xSpacing+23+2, ySpacing+15);//mid 
 
 							g.drawLine(xSpacing+22, ySpacing+7, xSpacing+7, ySpacing+22);//top right/bottom left
 							g.drawLine(xSpacing+22, ySpacing+7-1, xSpacing+7, ySpacing+22-1);//top right/bottom left
 							g.drawLine(xSpacing+22, ySpacing+7+1, xSpacing+7, ySpacing+22+1);//top right/bottom left
 							
 							g.drawLine(xSpacing+14, ySpacing+5-2, xSpacing+14, ySpacing+23+2);//top/down
 							g.drawLine(xSpacing+15, ySpacing+5-2, xSpacing+15, ySpacing+23+2);//top/down
 
 						}
 						g.setColor(Color.GRAY);
 						g.drawRect(xSpacing+1, ySpacing+1, (tileSize), (tileSize));
 					}
 					else if(board[x][y].getBombsSurrounding()==0)
 					{
 						g.setColor(color1);
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 						g.setColor(Color.GRAY);
 						g.drawRect(xSpacing+1, ySpacing+1, (tileSize), (tileSize));
 					}
 
 					else{
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 						g.setColor(color3);
 						g.fillRect(xSpacing+1, ySpacing+1, (tileSize+1), (tileSize+1));
 						g.setColor(Color.WHITE);
 						g.setFont(font);
 						if(compactMode)
 							g.drawString(""+board[x][y].getBombsSurrounding(), xSpacing+8, ySpacing+16);
 						else
 							g.drawString(""+board[x][y].getBombsSurrounding(), xSpacing+10, ySpacing+19);
 						g.setColor(Color.GRAY);
 						g.drawRect(xSpacing+1, ySpacing+1, (tileSize), (tileSize));
 
 					}
 
 				}
 
 				xSpacing += (tileSize+1);
 			}
 			ySpacing+= (tileSize+1);
 		}
 
 		g.setColor(Color.GRAY);
 		g.drawRect(0, getWindowY()-79, getWindowX(), 22);
 		g.setColor(Color.WHITE);
 		g.fillRect(0, getWindowY()-78, getWindowX(), 21);
 		g.setColor(Color.BLACK);
 		g.setFont(new Font("Arial", Font.BOLD,13)); // like the little mermaid
 		g.drawString("Flags: "+getFlagCount(), 25, getWindowY()-63);
 		g.drawString("Time: "+timeCounter, getWindowX()-75, getWindowY()-63);
 
 		if(showCheck){
 			
 			if(checkBoard())
 				g.setColor(new Color(0, 255, 0,80));
 			else
 				g.setColor(new Color(255, 0, 0,80));
 			
 			g.fillRect(0, 0, getWindowX(), getWindowY()-78);
 			
 		}
 
 		if(showHint){
 			Color color = new Color(255,255,0,255);
 			g.setColor(color);
 			g2d.setStroke(new BasicStroke(3));
 			if(!compactMode)
 				g.drawRect(getHint()[0]*(tileSize+1)+2, getHint()[1]*(tileSize+1)+2, 25, 25);
 			else
		
 				g.drawRect(getHint()[0]*(tileSize+1)+2, getHint()[1]*(tileSize+1)+2, 20, 20);
 		}
 
 		if(gameOver()){
 			g.setColor(Color.WHITE);
 			g.fillRect(0, getWindowY()-78, getWindowX(), 21);
 			g.setColor(Color.BLACK);
 			g.drawString(gameOverMessage(), 2, getWindowY()-63);
 		}
 		//g.setColor(Color.);
 		//getwindowy -36
 	}
 }
 
 
