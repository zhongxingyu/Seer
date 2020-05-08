 package edu.berkeley.nlp.classification;
 
 import java.io.IOException;
 import java.util.List;
 
 import chesspresso.game.Game;
 import chesspresso.move.IllegalMoveException;
 import chesspresso.pgn.PGNReader;
 import chesspresso.pgn.PGNSyntaxError;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.TypeLiteral;
 
 import edu.berkeley.nlp.chess.PositionFeatureExtractor;
 import edu.berkeley.nlp.chess.GameSlicer;
 import edu.berkeley.nlp.chess.Games;
 import edu.berkeley.nlp.chess.PositionWithMoves;
 import edu.berkeley.nlp.classification.Featurizers.ConcatenatingPositionWithMove;
 
 public class FeatureExtractorTest {
 	public static void main(String[] args) throws PGNSyntaxError, IOException,
 			IllegalMoveException {
 		PGNReader reader = new PGNReader(
 				"themes/backrankWeakness/backrankWeakness.pgn-fixed");
 		
 		Game game = reader.parseGame();
 		
 		Injector injector = Guice.createInjector(new AbstractModule() {
 			@Override
 			protected void configure() {
 				bind(new TypeLiteral<Featurizer<List<PositionWithMoves>>>() {})
 					.to(Featurizers.ConcatenatingPositionWithMove.class);
 				bind(new TypeLiteral<Featurizer<PositionWithMoves>>() {})
 					.to(PositionFeatureExtractor.class);
 			}			
 		});
 		
 		Featurizer<List<PositionWithMoves>> featurizer =
 			injector.getInstance(Key.get(new TypeLiteral<Featurizer<List<PositionWithMoves>>>() {}));
 		
 		while (game != null) {
 			GameSlicer slicer = new GameSlicer(Games.flatten(game), 3);
 			for (List<PositionWithMoves> slice : slicer) {
 				double[] features = featurizer.getFeaturesFor(slice);
				for (double d : features)
					System.out.printf("%f ", d);
				System.out.println();
 			}
 			game = reader.parseGame();
 			System.out.println("Done with one game");
 		}
 		System.out.println("Finished!");
 	}
 }
