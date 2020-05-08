 package sliceWars.logic.gameStates;
 
 import sliceWars.logic.Board;
 import sliceWars.logic.PlayOutcome;
 import sliceWars.logic.Player;
 import sliceWars.logic.gameStates.GameStateContextImpl.Phase;
 
 public class FirstDiceDistribution implements GameState {
 
 	private final Board _board;
 	private final int _totalDiceToAdd;
 	private int _turnsLeft;
 	private DiceDistribution _distributeDiePhase;
 	private Player _currentPlaying;
 
 	public FirstDiceDistribution(final Player currentPlaying,final Board board) {
 		_currentPlaying = currentPlaying;
 		_board = board;
 		int playersCount = currentPlaying.getPlayersCount();
		_totalDiceToAdd = board.getCellCount()/playersCount;
 		_turnsLeft = _totalDiceToAdd;
 		_distributeDiePhase = new DiceDistribution(currentPlaying, board, 1);
 	}
 
 	@Override
 	public PlayOutcome play(final int x,final int y,final GameStateContext gameStateContext){
 		_distributeDiePhase.play(x, y, gameStateContext);
 		if(_currentPlaying.isLastPlayer()) _turnsLeft--;
 		if(_turnsLeft == 0){
 			gameStateContext.setState(new FirstAttacks(_currentPlaying.next(), _board));
 			return new PlayOutcome(0);			
 		}
 			
 		gameStateContext.setState(this);
 		Player nextPlayer = _currentPlaying.next();
 		_currentPlaying = nextPlayer;
 		_distributeDiePhase = new DiceDistribution(nextPlayer, _board, 1);
 		return new PlayOutcome(_turnsLeft);
 	}
 	
 	@Override
 	public String getPhaseName() {
 		return "First round, add dice";
 	}
 	
 	@Override
 	public Player getWhoIsPlaying() {
 		return _currentPlaying;
 	}
 
 	@Override
 	public boolean canPass() {
 		return false;
 	}
 
 	@Override
 	public PlayOutcome pass(final GameStateContext gameStateContext){
 		return null;
 	}
 
 	@Override
 	public Phase getPhase(){
 		return Phase.FIRST_DICE_DISTRIBUTION;
 	}
 
 	public int getDiceToAdd() {
 		return _turnsLeft;
 	}
 }
