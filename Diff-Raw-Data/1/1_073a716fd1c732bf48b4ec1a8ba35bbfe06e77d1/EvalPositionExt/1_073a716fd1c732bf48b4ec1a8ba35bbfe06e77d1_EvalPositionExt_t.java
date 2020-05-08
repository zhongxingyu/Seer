 package chess.uci.controlExtension;
 
 import chess.eval.Evaluator;
 import chess.eval.ScoreEncoder;
 import chess.uci.Position;
 import chess.uci.UCIEngine;
 
 /** extension for calling the eval function on target board state*/
 public final class EvalPositionExt implements ControlExtension {
 	@Override
 	public void execute(String command, Position pos, UCIEngine engine) {
 		Evaluator e = engine.getEval();
		e.initialize(pos.s);
 		int scoreEncoding = e.eval(pos.sideToMove, pos.s);
 		int score = ScoreEncoder.getScore(scoreEncoding);
 		System.out.println("score = "+score);
 	}
 }
