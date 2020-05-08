 package net.mydebug.corners.drive;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import android.graphics.Paint;
 
 import com.badlogic.androidgames.framework.Game;
 import com.badlogic.androidgames.framework.Graphics;
 import com.badlogic.androidgames.framework.Pixmap;
 import com.badlogic.androidgames.framework.Graphics.PixmapFormat;
 
 import net.mydebug.corners.MainMenuScreen;
 import net.mydebug.corners.drive.figures.Ai;
 import net.mydebug.corners.drive.figures.Figure;
 import net.mydebug.corners.drive.figures.FigureData;
 import net.mydebug.corners.drive.figures.MoveLine;
 import net.mydebug.corners.drive.figures.Position;
 
 public abstract class ChessGame {
 	private   Pixmap    chessBoardImage;
 	protected Game      game;
 	protected Timer     timer;
     protected Statistic statistic;
     protected Settings  settings;
 
 	protected List<Figure>   figures         = new ArrayList<Figure>();
 	protected List<Position> tips            = new ArrayList<Position>();
 	protected List<MoveLine> tipsLines 		 = new ArrayList<MoveLine>();
 	protected List<MoveLine> aiLines 		 = new ArrayList<MoveLine>();
     protected Ai AiModel;
     protected History history;
 	private float[] pixelToPositionX = new float[9];
 	private float[] pixelToPositionY = new float[9];
 	private final float RAW_BODY_SIZE     = 840;
 	private final float RAW_BODY_PADDING  = 20;
 	private final float RAW_BODY_FIELD    = 100;
 	public final static int CHESSBOARD_FIELDS_COUNT = 8;
 	private int whoseTurn = Figure.WHITE;
 
 	public static final int ONE_PLAYER = 0;
     public static final int TWO_PLAYERS = 1;
 
     protected int playerColor  = Figure.WHITE;
 	protected int aiColor      = Figure.BLACK;
 
     protected boolean gameOver = false;
 
 	public Position aiTurnFrom = null;
 	public Position aiTurnTo   = null;
 	
 	private int boardWidth;
 	private int boardHeight;
 	private float paddingX = 0;
 	private float paddingY = 0;
 	private float coefficient;
     
 	private float fieldWidth;
 	private float fieldHeight;
 	private float figureWidth;	
 	private float figureHeight;	
 	private float figurePadding;	
 	
 	protected int gameMode  = ONE_PLAYER;
     protected int gameLevel = 0;
 	
 	protected int[][] figuresOnBoard = new int[CHESSBOARD_FIELDS_COUNT][CHESSBOARD_FIELDS_COUNT];
 	protected int activeFigure = -1;
 
     private float gameTime = 0;
     private boolean boardTips = true;
 	
 	public ChessGame(Game game, boolean isNew) {
         this.game = game;
        newGame( isNew );
    }

    public void newGame( boolean isNew ) {
         gameOver   = false;
         gameTime   = 0;
         aiTurnFrom = null;
         aiTurnTo   = null;
         aiLines    = new ArrayList<MoveLine>();
         tipsLines  = new ArrayList<MoveLine>();
         tips       = new ArrayList<Position>();
         settings   = new Settings ( game.getActivity().getBaseContext().getFilesDir().toString() );
         history    = new History  ( game.getActivity() , isNew );
         statistic  = new Statistic( game.getActivity() );
 
         gameMode  = settings.getGameMode();
         boardTips = settings.getShowTips();
         gameLevel = settings.getGameLevel();
 
         if( AiModel != null ) {
             AiModel.resetAi();
         }
 
         if( settings.getPlayerColor() == Figure.BLACK )    {
             aiColor     = Figure.WHITE ;
             playerColor = Figure.BLACK;
         }
         else  {
             aiColor     = Figure.BLACK;
             playerColor = Figure.WHITE;
         }
         chessBoardImage = game.getGraphics().newPixmap("chessboard.png", PixmapFormat.RGB565 );
 
         activeFigure = -1;
         boardWidth   = game.getGraphics().getWidth();
         paddingY     = ( game.getGraphics().getHeight() - boardWidth ) / 2;
         coefficient  = boardWidth / RAW_BODY_SIZE ;
         fieldWidth   = RAW_BODY_FIELD * coefficient;
 
         float startPointX  = RAW_BODY_PADDING * coefficient;
         float startPointY  = RAW_BODY_PADDING * coefficient + paddingY;
 
         for(int i = 0 ; i < pixelToPositionX.length ; i++) {
             pixelToPositionX[i] = startPointX;
             startPointX += fieldWidth;
             pixelToPositionY[i] = startPointY;
             startPointY += fieldWidth;
         }
 
         fieldHeight = fieldWidth;
         boardHeight = boardWidth;
         figureWidth = figureHeight = fieldWidth * 0.8f;
         figurePadding = fieldWidth * 0.1f;
 
         // Если игра новая - инициализируем поле заново, иначе загружаем прошлую игру
         if( isNew ) {
             initializeFigures();
             setFiguresOnBoard();
             history.clear();
             // Запоминаем изначальное расположение фигур, если пользователь ходит первым
             // если первым ходит AI - запоминаем после хода
             if( playerColor == Figure.WHITE || gameMode == TWO_PLAYERS )
                 history.save(this);
         } else {
             this.loadGameFromHistory();
             if( isGameOver() != -1 ) {
                 gameOver();
             }
         }
 
         // Запускаем таймер времени игры
         timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 gameTime += 1;
             }
         }, 0, 1000);
         draw();
     }
 	
     /** Обрабатываем нажатие на экран
      * @param x - позиция по горизонтали
      * @param y - позиция по вертикали
      */
     public void touch( int x , int y ) {
     	// Если нажали на шахматную доску
     	if( y > paddingY && y < game.getGraphics().getHeight() - paddingY ) {
             if( ! gameOver ) {
                 // устанавливаем значение поля выходящее за пределы, потом будем проверять,
                 // если поле от 0 до 7 - значит кликнули на клетку
                 int fieldX = 99;
                 int fieldY = 99;
                 int i;
                 int fieldsCnt = pixelToPositionX.length - 1 ;
                 for( i = 0 ; i < fieldsCnt ; i++ ) {
                     if( pixelToPositionX[i] < x && pixelToPositionX[i+1] > x) {
                         fieldX = i;
                         break;
                     }
                 }
                 for( i = 0 ; i < fieldsCnt ; i++ ) {
                     if( pixelToPositionY[i] < y && pixelToPositionY[i+1] > y) {
                         fieldY = i;
                         break;
                     }
                 }
                 if( fieldX < CHESSBOARD_FIELDS_COUNT && fieldY < CHESSBOARD_FIELDS_COUNT ) {
                     press( fieldX , fieldY );
                     setFiguresOnBoard();
                     draw();
                     if( isGameOver() != -1 ) {
                         gameOver();
                     }
 
                 }
             } else {
                newGame(true);
                if( playerColor == Figure.BLACK && AiModel != null ) {
                    AiModel.move();
                }
             }
         //если по вертикали мы попали в зону нижнего меню
     	} else if( y > game.getGraphics().getHeight() - 32 ) {
     		// Если нажали в левый нижний угол (иконка "ход назад")
     		if( x < 32 && ! gameOver ) {
     			this.loadPrevFromHistory();
     		// Если нажали в нижний правый угол (иконка выйти в меню)
     		} else if( x > game.getGraphics().getWidth() - 32) {
                 this.clearTimer();
                 game.setScreen( new MainMenuScreen( game ) );
     		}
     	}
  
 
 
     }
     
     public void gameOver() {
         gameOver = true ;
         int winner = isGameOver();
         Pixmap pixmap = null;
         if( winner == Figure.WHITE ) {
 			pixmap = game.getGraphics().newPixmap( "gameOverWhiteWins.png" , Graphics.PixmapFormat.ARGB8888 );
 		} else if( winner == Figure.BLACK ){
             pixmap = game.getGraphics().newPixmap( "gameOverBlackWins.png" , Graphics.PixmapFormat.ARGB8888 );
         }
         game.getGraphics().drawRect(0, 0, game.getGraphics().getWidth(), game.getGraphics().getHeight() - 32, 0x99cccccc);
         game.getGraphics().drawPixmap( pixmap , game.getGraphics().getWidth() / 2 - 115 , game.getGraphics().getHeight() / 2 - 65 );
         drawBottomMenu();
         this.drawInfo();
         drawGameTime();
         if( gameMode == ChessGame.ONE_PLAYER && winner == playerColor )
             statistic.add( history.getTurnId() , (int) this.getGameTime() , this.getGameLevel() , this.getPlayerColor() );
         this.clearTimer();
 		history.clear();
     }
     
     
     public void loadPrevFromHistory() {
 		ArrayList<FigureData> figureDatas = history.back( );
         if( figureDatas != null ) {
             if( AiModel != null ) {
                 AiModel.resetAi();
             }
             clearTips();
     		setFiguresByFiguresData( figureDatas );
     		setFiguresOnBoard();
     		this.whoseTurn = history.lastWhosTurn();
             this.gameTime   = history.lastGameTime();
             if( playerColor == Figure.BLACK && whoseTurn == Figure.WHITE && gameMode == ONE_PLAYER && history.getTurnId() > 0 ) {
                 AiModel.clearMoveHistory();
                 AiModel.move();
             }
         }
         aiLines = new ArrayList<MoveLine>();
 		draw();	
     }
 
     public void clearTips() {
     	tipsLines  = new ArrayList<MoveLine>();
     	tips       = new ArrayList<Position>();
     	aiTurnFrom = null;
     	aiTurnTo   = null;
     }
     
 	public void loadGameFromHistory() {
 		ArrayList<FigureData> figureDatas = history.loadLastTurn( );
 		if( figureDatas != null ) {
             setFiguresByFiguresData( figureDatas );
 			setFiguresOnBoard();
     		this.whoseTurn = history.lastWhosTurn();
             this.gameTime   = history.lastGameTime();
         }
 	}
     
     public int getTurn() {
     	return whoseTurn;
     }
     
     public void nextTurn() {
     	if( whoseTurn == Figure.WHITE ) {
     		whoseTurn = Figure.BLACK;
     	} else {
     		whoseTurn = Figure.WHITE;
     	}
     }
     
     protected int getFigureIndexByField( int x , int y ) {
 		return figuresOnBoard[x][y];
     }
     
     private int[][] getFiguresOnBoard() {
     	return figuresOnBoard;
     }
     
     public void move( int figureIndex , Position position) {
         // Для графа отправляем точки откуда\куда ход
         if( whoseTurn != playerColor && gameMode == ONE_PLAYER || gameMode == TWO_PLAYERS ) {
             int x1 = figures.get( figureIndex ).getX();
             int y1 = figures.get( figureIndex ).getY();
             int x2 = position.x;
             int y2 = position.y;
             Graph graph = new Graph( tipsLines );
             aiLines     = graph.getShortestRoute( x1 , y1 , x2 , y2 );
         }
         //Передвигаем фигуру
         figures.get( figureIndex ).setPosition( position.x, position.y);
 		setFiguresOnBoard();
 		tips      = new ArrayList<Position>();
 		tipsLines = new ArrayList<MoveLine>();
 		activeFigure = -1;
 		nextTurn();
 		if( this.gameMode == ONE_PLAYER ) {
     		if( whoseTurn != playerColor ) {
     			AiModel.move();
             } else {
                 history.save( this );
     		}
 		} else {
             history.save( this );
     	}
     }
 
     public void setFigures( List<Figure> figures ) {
     	this.figures = figures;
     }
     
     protected void setFiguresOnBoard() {
         int i,j;
     	for( i = 0 ; i < CHESSBOARD_FIELDS_COUNT ; i++ ) {
     		for( j = 0 ; j < CHESSBOARD_FIELDS_COUNT ; j++ ) {
     			figuresOnBoard[i][j] = -1;
     		}
     	}
     	for( i = 0 ; i < figures.size() ; i++ ) {
     		figuresOnBoard[figures.get(i).getX()][figures.get(i).getY()] = i;
     	}
     }
     
     public void setAiTurnShowField( Position p1 , Position p2 ) {
     	aiTurnFrom = p1;
     	aiTurnTo   = p2;
     }
 
     public void draw() {
         Pixmap chessBoardImage = game.getGraphics().newPixmap("950890_72529114.jpg", Graphics.PixmapFormat.ARGB8888 );
         game.getGraphics().drawPixmap(chessBoardImage, 0, 0, game.getGraphics().getWidth(), game.getGraphics().getHeight());
         chessBoardImage.dispose();
         this.drawBoard();
     	this.drawFigures();
     	this.drawTips();
         this.drawAiTurns();
     	this.drawBottomMenu();
         this.drawInfo();
         this.drawGameTime();
         this.drawAiLines();
     }
     
     
     private int getXPixel( int i ) {
     	return (int)pixelToPositionX[i] + 1;
     }
     
     private int getYPixel( int i ) {
     	return (int)pixelToPositionY[i] + 1;
     }
     
     private void drawGrid() {
     	for( int i = 0 ; i < pixelToPositionX.length ; i++ ) {
     		game.getGraphics().drawLine( (int)pixelToPositionX[0],(int) pixelToPositionY[i], (int)pixelToPositionX[ pixelToPositionY.length - 1  ], (int)pixelToPositionY[ i ], 0xcc0000ff );
     		game.getGraphics().drawLine( (int)pixelToPositionX[i],(int) pixelToPositionY[0], (int)pixelToPositionX[i], (int)pixelToPositionY[pixelToPositionY.length - 1], 0xcc0000ff );
     	}
     }
 
 
 	private void drawTips() {
 		if( boardTips ) {
 			// Подсвечиваем поля возможного хода
 
             for (Position tip : tips) {
                 highlightField(tip, 0xcc00cc00);
                 highlightFieldBorder(tip, 0xff00ff00);
             }
 			// Рисуем линии, которые подсказывают траэкторию возможного хода
             for (MoveLine tipsLine : tipsLines) {
                 game.getGraphics().drawLine((int) (getXPixel(tipsLine.position1.x) + fieldHeight / 2), (int) (getYPixel(tipsLine.position1.y) + fieldHeight / 2), (int) (getXPixel(tipsLine.position2.x) + fieldHeight / 2), (int) (getYPixel(tipsLine.position2.y) + fieldHeight / 2), 0xff00ff00, 2);
             }
 
 		}
 	}
 
     private void drawAiLines() {
 			// Рисуем линии, которые подсказывают траэкторию хода противника
             for (MoveLine line : aiLines) {
                 game.getGraphics().drawLine((int) (getXPixel(line.position1.x) + fieldHeight / 2), (int) (getYPixel(line.position1.y) + fieldHeight / 2), (int) (getXPixel(line.position2.x) + fieldHeight / 2), (int) (getYPixel(line.position2.y) + fieldHeight / 2), 0x66ff0000, 2);
             }
 	}
     
 	private void highlightFieldBorder( Position position , int color ) {
 		int x1 = getXPixel( position.x )  ;
 		int x2 = (int)( getXPixel( position.x ) + fieldHeight )  ;
 		int y1 = getYPixel( position.y ) ;
 		int y2 = (int)( getYPixel( position.y ) + fieldHeight ) ;
 		game.getGraphics().drawLine( x1 , y1 , x1 , y2 , color );
 		game.getGraphics().drawLine( x2 , y1 , x2 , y2 , color );
 		game.getGraphics().drawLine( x1 , y1 , x2 , y1 , color );
 		game.getGraphics().drawLine( x1 , y2 , x2 , y2 , color );
 	}
 
 
 	private void highlightField( Position position , int color ) {
 		game.getGraphics().drawRect( getXPixel( position.x )  , getYPixel( position.y ) , (int) fieldHeight,(int) fieldHeight, color );
 	}
 	
     private void drawBoard() {
     	game.getGraphics().drawPixmap(chessBoardImage, paddingX, paddingY, boardWidth, boardHeight);
     }
     
     private void drawFigures() {
     	for( int i = 0 ; i < figures.size() ; i++ ) {
     		Figure figure = figures.get(i);
     		Pixmap pixmap;
     		if( i == activeFigure && getTurn() == figures.get(activeFigure).getColor() ) {
 				highlightField( figure.getPosition() ,0x33cccccc);
 				highlightFieldBorder( figure.getPosition() ,0xffcccccc);
 				pixmap = game.getGraphics().newPixmap( figure.getActiveImage() , PixmapFormat.ARGB8888 );
     		} else {
         		pixmap = game.getGraphics().newPixmap( figure.getImage() , PixmapFormat.ARGB8888 );
     		}
     		game.getGraphics().drawPixmap( pixmap  , getXPixel(figure.getX()) + figurePadding , getYPixel(figure.getY()) + figurePadding , figureWidth , figureHeight );
             pixmap.dispose();
         }
     }
     
     private void drawAiTurns() {
     	if( aiTurnFrom != null ) {
     		highlightField( aiTurnFrom ,0x11cccccc);
     		highlightFieldBorder( aiTurnFrom ,0xffcccccc);    		
     	}
 
 		if( aiTurnTo != null ) {
 			highlightField( aiTurnTo ,0x33ee0000);
 			highlightFieldBorder( aiTurnTo ,0xffff0000);    		
 		}
 
     }
 
     public void drawGameTime() {
         if( timer == null ) return;
         int minutes = (int) gameTime / 60 ;
         int seconds = (int) gameTime % 60 ;
         int hours   = (int) gameTime / 3600;
 
         Pixmap pixmap = game.getGraphics().newPixmap( "numbers.jpg" , PixmapFormat.RGB565 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 20 , 0 , 20 * ( seconds % 10 ) , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 40 , 0 , 20 * ( seconds / 10 ) , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 60 , 0 , 200 , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 80 , 0 , 20 * ( minutes % 10 ) , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 100 , 0 , 20 * ( minutes / 10 ) , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 120 , 0 , 200 , 0 ,
         21  , 30 );
         game.getGraphics().drawPixmap( pixmap, game.getGraphics().getWidth() - 140 , 0 , 20 * ( hours % 10 ) , 0 ,
                 21  , 30 );
         pixmap.dispose();
     }
     
     public int getBoardLength() {
     	return CHESSBOARD_FIELDS_COUNT;
     }
     
     protected void drawInfo() {
         int turnId = history.getTurnId();
         if( gameMode == ChessGame.TWO_PLAYERS ) {
             turnId = turnId / 2 ;
         }
         Pixmap pixmap;
         pixmap = game.getGraphics().newPixmap( "movesCount.png" , PixmapFormat.RGB565 );
         game.getGraphics().drawPixmap( pixmap  , 32 , game.getGraphics().getHeight() - 32 );
         game.getGraphics().drawText( String.valueOf( turnId )  , 162 , game.getGraphics().getHeight() - 10 , 20 , 0xff000000 , Paint.Align.LEFT );
         pixmap = game.getGraphics().newPixmap( "move.png" , PixmapFormat.RGB565 );
         game.getGraphics().drawPixmap( pixmap  , 0 , 0 );
 		if( whoseTurn == 0 )
 			pixmap = game.getGraphics().newPixmap( "checkerBlack1.png" , PixmapFormat.RGB565 );
 		else
 			pixmap = game.getGraphics().newPixmap( "checkerWhite1.png" , PixmapFormat.RGB565 );
 		game.getGraphics().drawPixmap( pixmap  , 60 , 0 , 30 , 30 );
         pixmap.dispose();
     }
     
     protected void drawBottomMenu() {
         Pixmap pixmap;
 
         pixmap = game.getGraphics().newPixmap( "menu.jpg" , PixmapFormat.RGB565 );
         game.getGraphics().drawPixmap( pixmap  , 0 , game.getGraphics().getHeight() - 35 , boardWidth , 35 );
 
         game.getGraphics().drawPixmap( pixmap  , 0 , 0 , boardWidth , 30 );
         pixmap.dispose();
         if( history.getTurnId() > 0 && ! gameOver ) {
         	pixmap = game.getGraphics().newPixmap( "go-back-icon-32.png" , PixmapFormat.RGB565 );
     		game.getGraphics().drawPixmap( pixmap  , 0 , game.getGraphics().getHeight() - 32 );
             pixmap.dispose();
         }
     	
 
     	pixmap = game.getGraphics().newPixmap( "Close-2-icon-32.png" , PixmapFormat.RGB565 );
 		game.getGraphics().drawPixmap( pixmap  , game.getGraphics().getWidth() - 32 , game.getGraphics().getHeight() - 32 );
         pixmap.dispose();
 
     }
     
     public List<Figure> getFigures() {
     	return this.figures;
     }
 
     /**
      * Проверяем пустое ли поле
      * @param position - искомое поле
      * @return  int -1 - за предлелами поля, 0 - занято, 1 - пусто
      */
 	public int checkFieldIsEmpty(Position position ) {
 		if( position.x < 0 || position.x > ChessGame.CHESSBOARD_FIELDS_COUNT - 1 || position.y < 0 || position.y > ChessGame.CHESSBOARD_FIELDS_COUNT - 1 )
 			return -1;
 		if( figuresOnBoard[position.x ][position.y] == -1 ) 
 			return 1;
 		return 0;
 	}
 
 	public void setTips(List<Position> aviableMoves) {
 		this.tips = aviableMoves;
 	} 
 
 	public void setTipsLines(List<MoveLine> aviableDirectionsLines) {
 		this.tipsLines = aviableDirectionsLines;
 	}
 
     public float getGameTime() {
         return gameTime;
     }
 
     public void setGameTime( float time ) {
         gameTime = time ;
     }
 
     public void clearTimer() {
         if( timer != null ) {
             timer.cancel();
             timer = null;
         }
     }
     
     public int getGameLevel() {
         return this.gameLevel;
     }
 
     public int getPlayerColor() {
         return this.playerColor;
     }
 
     public Settings getSettings() {
         return settings;
     }
 
     public void save() {
         history.save( this );
     }
 
     public void load() {
         history.back();
     }
 	
     protected abstract void initializeFigures();
     protected abstract void buildTips( int figureIndex , int x , int y );
     protected abstract void press( int x , int y );
     protected abstract void setFiguresByFiguresData( ArrayList<FigureData> figureData );
     // return color wins figures or -1 when game not over
     protected abstract int isGameOver();
 
 
     
 
 
 
 }
