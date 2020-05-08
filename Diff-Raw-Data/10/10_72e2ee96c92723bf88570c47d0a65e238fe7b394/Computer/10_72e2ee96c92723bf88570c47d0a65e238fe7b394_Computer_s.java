 package connectfour.model;
 
 import connectfour.controller.GameController;
 import connectfour.util.observer.IObserverWithArguments;
 
 public class Computer extends PlayerAbstract {
 
 	private int doNextColumn = 3;
 	private final int deepSearch = 6;
 	private boolean firstMove = true;
 
 	@Override
 	public void surrender() {
 	}
 
 	@Override
 	public int dropCoin(final int column) {
 		setMove(column);
 		return getMove();
 	}
 
 	public Computer(final IObserverWithArguments observer) {
 		super();
 		this.addObserver(observer);
 	}
 
 	@Override
 	public int getMove() {
 		/*
 		 * Random r = new Random(); int random = r.nextInt() % 7; return random
 		 * < 0 ? -random : random;
 		 */
 		if (firstMove && getGameField().isEmpty()) {
 			firstMove = false;
 			return doNextColumn;
 		}
 		maxWert(6);
 		return doNextColumn;
 
 	}
 
 	@Override
 	public void setMove(final int column) {
 
 	}
 
 	private int maxWert(final int restTiefe) {
 		int ermittelt = -Integer.MAX_VALUE;
 		int zugWert;
 		for (int i = 0; i < GameField.DEFAULT_COLUMNS; i++) {
 			GameField previousState = saveState();
 			if (getGameField().getPlayerOnTurn() != this) {
 				getGameField().changePlayerTurn();
 			}
 
 			if (getGameField().dropCoin(i) >= GameField.DEFAULT_ROWS) {
 				setGameField(previousState);
 				continue;
 			}
 
 			if (restTiefe <= 1) {
 				zugWert = getGameField().evaluateScore();
 			} else {
 				zugWert = minWert(restTiefe - 1);
 			}
 			GameField newState = saveState();
 			setGameField(previousState);
 			if (newState.getWinner() != null) {
 				zugWert = +1000000;
 			}
 			if (zugWert > ermittelt) {
 				ermittelt = zugWert;
 				if (restTiefe >= deepSearch
 						|| (newState.getWinner() == this && restTiefe >= deepSearch)) {
 					doNextColumn = i;
 				}
 			}
 
 		}
 		return ermittelt;
 	}
 
 	private int minWert(final int restTiefe) {
 		int ermittelt = Integer.MAX_VALUE;
 		int zugWert;
 		for (int i = 0; i < GameField.DEFAULT_COLUMNS; i++) {
 			GameField previousState = saveState();
 			if (getGameField().getPlayerOnTurn() == this) {
 				getGameField().changePlayerTurn();
 			}
 			if (getGameField().dropCoin(i) >= GameField.DEFAULT_ROWS) {
 				setGameField(previousState);
 				continue;
 
 			}
 			GameField newState = saveState();
 			if (restTiefe <= 1) {
 				zugWert = getGameField().evaluateScore();
 			} else {
 				zugWert = maxWert(restTiefe - 1);
 			}
 
 			if (newState.getWinner() != null) {
 				zugWert = -1000000;
 			}
 			setGameField(previousState);
 			if (zugWert < ermittelt) {
 				ermittelt = zugWert;
 			}
 		}
 		return ermittelt;
 	}
 
 	private GameField saveState() {
 		GameField state = null;
 		try {
 			state = getGameField().clone();
 		} catch (CloneNotSupportedException e1) {
 		}
 		return state;
 
 	}
 
 	@Override
 	public void update() {
 		if (GameController.getInstance().getGameField()
 				.getPlayerOnTurn() == this) {
 			int columnToDrop = this.getMove();
 			this.notifyObservers(columnToDrop);
 		}
 	}
 }
