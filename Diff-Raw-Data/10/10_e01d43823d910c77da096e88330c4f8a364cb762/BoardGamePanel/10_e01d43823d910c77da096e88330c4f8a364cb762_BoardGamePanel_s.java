 package gui.gameEntities;
 
 import externalPlayer.AiControl;
 import gameEngine.JGamePanel;
 import gameLogic.Game;
 import gui.GameLayoutDefinitions;
 import gui.gameEntities.piecesBoard.BoardPlayInputDecoder;
 import gui.gameEntities.piecesBoard.PiecesBoard;
 import ai.AIPlayer;
 
 public class BoardGamePanel extends JGamePanel{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final Avatar avatarBottom;
 	private final Avatar avatarTop;
 	private final Game game;
 	private final NextStageButton nextStageButton;
 	private final RestartButton restartButton;
 	private final PiecesBoard piecesBoard;
 
 	public BoardGamePanel(final boolean isTopAi, final boolean isBottomAi) {
 		super(GameLayoutDefinitions.background);
 		game = new Game();
 
 		piecesBoard = new PiecesBoard(game.getBoard(),this,GameLayoutDefinitions.boardPosition);
 		addGameElement(piecesBoard);
 		final boolean processForTop = true;
 		final boolean processForBottom = true;
 		final BoardPlayInputDecoder processMouseEventForBoard = new BoardPlayInputDecoder(processForTop,processForBottom,game,piecesBoard);
 		addMouseListener(processMouseEventForBoard);
 		if(isTopAi){
 			game.addTurnListener(new AiControl(new AIPlayer(), true));
 		}
 		if(isBottomAi){
 			game.addTurnListener(new AiControl(new AIPlayer(), false));
 		}
 		nextStageButton = new NextStageButton(processMouseEventForBoard);
 		nextStageButton.setLocation(GameLayoutDefinitions.buttomNextStagePosition);
 		restartButton = new RestartButton(processMouseEventForBoard);
 		restartButton.setLocation(GameLayoutDefinitions.buttomRestartPosition);
 
 		final ErrorMessage errorMessageShower = new ErrorMessage(this);
 		addGameElement(errorMessageShower);
 		processMouseEventForBoard.addErrorListener(errorMessageShower);
 

 		final boolean isTopPlayer = processForTop;
 		avatarTop = new Avatar(GameLayoutDefinitions.avatarTopPosition,isTopAi,  isTopPlayer);
 		avatarBottom = new Avatar(GameLayoutDefinitions.avatarBottomPosition,isBottomAi, !isTopPlayer);
 
 		addGameElement(avatarTop);
 		addGameElement(avatarBottom);
 		add(nextStageButton);
 		add(restartButton);
 	}
 }
