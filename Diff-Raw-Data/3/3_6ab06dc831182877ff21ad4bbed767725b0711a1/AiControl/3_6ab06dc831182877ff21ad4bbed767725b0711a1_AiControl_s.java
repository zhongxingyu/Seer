 package externalPlayer;
 
 import gameLogic.Game;
 import gameLogic.TurnChangeListener;
 import gameLogic.board.Board;
 import gameLogic.board.InvalidPlayException;
 import gameLogic.board.InvalidPlayStringException;
 import gameLogic.board.PlaySequence;
 import gameLogic.board.PlaySequenceValidator;
 import gameLogic.board.ValidPlaySequence;
 import utils.GameUtils;
 import utils.Printer;
 
 public class AiControl implements TurnChangeListener {
 
 	private final boolean isTopPlayer;
 	private final PathAI player;
 
 	public AiControl(final PathAI player,	final boolean isTopPlayer) {
 		this(player,isTopPlayer,false);
 	}
 	
 	public AiControl(final PathAI player,final boolean isTopPlayer, final boolean stopPlayingOnGameEnd) {
 		this.player = player;
 		this.isTopPlayer = isTopPlayer;
 	}	
 	
 	private void aiError(final Exception e,final Game game) {
		Printer.error("Ai error (Ai was turned off): "+e);
		game.clearTurnListeners();
 		unLockGame(game);
 	}
 
 	private void unLockGame(final Game game) {
 		if(isTopPlayer)
 			game.setTopLocked(false);
 		else
 			game.setBottomLocked(false);
 	}
 
 	private void lockGame(final Game game) {
 		if(isTopPlayer)
 			game.setTopLocked(true);
 		else
 			game.setBottomLocked(true);
 	}
 
 
 	private boolean isBrainTurn(final Game game) {
 		return (game.isTopPlayerTurn() && isTopPlayer) || (game.isBottomPlayerTurn() && !isTopPlayer) && !game.isGameEnded();
 	}
 
 	private void play(final Game game) {
 		if(isBrainTurn(game)){
 			lockGame(game);
 			final boolean isTopPlayerTurn = game.isTopPlayerTurn();
 			final Board board;
 			if(isTopPlayerTurn){
 				board = GameUtils.newBoardSwitchedSides(game.getBoard());
 			}else{
 				board = game.getBoard();
 			}
 			
 			final String boardString = GameUtils.printBoard(game.getBoard());
 			Printer.debug("Quering External player play for board:\n"+boardString);
 			final String aiPlay = player.play(GameUtils.printBoard(board));				
 			final PlaySequence playSequence;
 			try {
 				playSequence = new PlaySequence(aiPlay);
 			} catch (InvalidPlayStringException e) {
 				aiError(e, game);
 				return;
 			}
 			final PlaySequence playSequenceForGame = isTopPlayerTurn ? GameUtils.invertPlay(playSequence) : playSequence;
 			Printer.debug("External player played: "+playSequenceForGame);
 			
 			final ValidPlaySequence validPlaySequence;
 			PlaySequenceValidator playSequenceValidator = new PlaySequenceValidator(game);
 			try {
 				validPlaySequence = playSequenceValidator.validatePlays(playSequenceForGame,isTopPlayerTurn);
 				playSequenceValidator.play(validPlaySequence);
 			}catch (InvalidPlayException e) {
 				aiError(e, game);
 				return;
 			}
 			unLockGame(game);
 		}
 	}
 
 	@Override
 	public void changedTurn(final Game game){
 		new Thread(new Runnable(){
 			@Override
 			public void run() {
 				play(game);
 			}
 		}).start();
 	}
 
 	public void stopPlaying(final Game game) {
 		game.removeTurnListener(this);
 	}
 
 }
