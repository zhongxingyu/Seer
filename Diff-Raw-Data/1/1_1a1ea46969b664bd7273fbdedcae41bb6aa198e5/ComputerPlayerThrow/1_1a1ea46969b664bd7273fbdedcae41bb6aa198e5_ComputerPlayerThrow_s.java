 package org.opendarts.core.x01.service;
 
 import java.util.Arrays;
 
 import org.opendarts.core.ia.service.IComputerPlayerDartService;
 import org.opendarts.core.model.dart.DartZone;
 import org.opendarts.core.model.dart.IComputerThrow;
 import org.opendarts.core.model.dart.IDart;
 import org.opendarts.core.model.dart.IDartsThrow;
 import org.opendarts.core.model.dart.InvalidDartThrowException;
 import org.opendarts.core.model.dart.impl.ComputerThrow;
 import org.opendarts.core.model.dart.impl.ThreeDartsThrow;
 import org.opendarts.core.model.player.IComputerPlayer;
 import org.opendarts.core.service.game.IGameService;
 import org.opendarts.core.x01.OpenDartsX01Bundle;
 import org.opendarts.core.x01.model.BrokenX01DartsThrow;
 import org.opendarts.core.x01.model.GameX01;
 import org.opendarts.core.x01.model.WinningX01DartsThrow;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class ComputerPlayerThrow.
  */
 public class ComputerPlayerThrow {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(ComputerPlayerThrow.class);
 
 	/** The computer player dart service. */
 	private final IComputerPlayerDartService computerPlayerDartService;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/** The player. */
 	private final IComputerPlayer player;
 
 	/** The game. */
 	private final GameX01 game;
 
 	/** The darts throw. */
 	private IDartsThrow dartsThrow;
 
 	/** The darts. */
 	private IDart[] darts;
 
 	/** The wished. */
 	private IDart[] wished;
 
 	/**
 	 * Instantiates a new computer player throw.
 	 *
 	 * @param game the game
 	 * @param player the player
 	 */
 	public ComputerPlayerThrow(GameX01 game, IComputerPlayer player) {
 		super();
 		this.game = game;
 		this.player = player;
 		this.computerPlayerDartService = OpenDartsX01Bundle
 				.getComputerPlayerDartService();
 		this.gameService = this.game.getParentSet().getGameService();
 	}
 
 	/**
 	 * Gets the darts throw.
 	 *
 	 * @param baseScore the base score
 	 * @return the darts throw
 	 */
 	public IComputerThrow getComputerThrow(int baseScore) {
 		int score;
 		IDart dart;
 
 		this.darts = new IDart[3];
 		this.wished = new IDart[3];
 		score = baseScore;
 
 		this.dartsThrow = null;
 		try {
 			dart = this.throwDart(score, 0);
 
 			if (this.dartsThrow == null) {
 				score -= dart.getScore();
 				dart = this.throwDart(score, 1);
 
 				if (this.dartsThrow == null) {
 					this.throwDart(score, 2);
 					if (this.dartsThrow == null) {
 						this.dartsThrow = new ThreeDartsThrow(darts);
 					}
 				}
 			}
 
 		} catch (InvalidDartThrowException e) {
 			LOG.error("WTF !", e);
 		}
 		return new ComputerThrow(dartsThrow,  Arrays.asList(this.wished),Arrays.asList(this.darts)) ;
 	}
 
 	/**
 	 * Throw dart.
 	 *
 	 * @param score the score
 	 * @param index the index
 	 * @return the i dart
 	 * @throws InvalidDartThrowException the invalid dart throw exception
 	 */
 	private IDart throwDart(int score, int index)
 			throws InvalidDartThrowException {
 		IDart dart = this.getDart(score, index);
 		this.darts[index] = dart;
 		if (score == dart.getScore()) {
 			if (DartZone.DOUBLE.equals(dart.getZone())) {
 				// win
 				this.dartsThrow = new WinningX01DartsThrow(this.darts);
 			} else {
 				// broken
 				this.dartsThrow = new BrokenX01DartsThrow(this.darts);
 			}
 		} else if ((score - dart.getScore()) < 2) {
 			// broken
 			this.dartsThrow = new BrokenX01DartsThrow(this.darts);
 		}
 		return dart;
 	}
 
 	/**
 	 * Gets the dart.
 	 *
 	 * @param score the score
 	 * @param index the index
 	 * @return the first dart
 	 */
 	private IDart getDart(int score, int index) {
 		IDart wished = this.gameService.chooseBestDart(this.player, score,
 				this.darts.length - index);
 		this.wished[index] = wished;
 		IDart done = this.computerPlayerDartService.getComputerDart(
 				this.player, wished);
 		return done;
 	}
 
 }
