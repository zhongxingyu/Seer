 package com.strategy.havannah.logic.evaluation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import net.sf.javabdd.BDD;
 import net.sf.javabdd.BDDFactory;
 
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.strategy.api.board.Board;
 import com.strategy.api.field.Field;
 import com.strategy.api.logic.Position;
 import com.strategy.api.logic.evaluation.Evaluation;
 import com.strategy.api.logic.situation.Situation;
 import com.strategy.util.Debug;
 import com.strategy.util.StoneColor;
 import com.strategy.util.operation.Logging;
 import com.strategy.util.predicates.EmptyPositionFilter;
 import com.strategy.util.predicates.ValidPositionFilter;
 
 /**
  * @author Ralph Dürig
  */
 public class EvaluationHavannah implements Evaluation {
 
 	private double[] rating;
 	private double avg;
 	private int best;
 	private final Board board;
 	private final BDD win;
 	private final BDD winBlack;
 	private final StoneColor color;
 	private Logging logRestrictWhite;
 	private Logging logRestrictBlack;
 	private Logging logId;
 	private Logging logSat;
 
 	public static Evaluation create(Situation sit, Situation sitBlack,
 			StoneColor color) {
 		return new EvaluationHavannah(sit.getBoard(),
 				sit.getWinningCondition(), sitBlack.getWinningCondition(),
 				color);
 	}
 
 	public static Evaluation create(Board board, BDD win, BDD winBlack,
 			StoneColor color) {
 		return new EvaluationHavannah(board, win, winBlack, color);
 	}
 
 	private EvaluationHavannah(Board board, BDD win, BDD winBlack,
 			StoneColor color) {
 		this.board = board;
 		this.win = win;
 		this.winBlack = winBlack;
 		this.color = color;
 		avg = 0d;
 		best = 0;
 		logRestrictWhite = Logging.create("evaluation: " + StoneColor.WHITE
 				+ " - restrict");
 		logRestrictBlack = Logging.create("evaluation: " + StoneColor.BLACK
 				+ " - restrict");
 		logId = Logging.create("evaluation: " + color + " - copy win");
 		logSat = Logging.create("evaluation: " + color + " - sat count");
 
 		String caption = "prediction for " + color;
 		Debug initlog = Debug.create(caption);
 		init();
 		initlog.log();
 	}
 
 	@Override
 	public double getAverageRating() {
 		return avg;
 	}
 
 	@Override
 	public int getBestIndex() {
 		return best;
 	}
 
 	@Override
 	public double[] getRating() {
 		return rating;
 	}
 
 	@Override
 	public void log() {
 		logRestrictWhite.log();
 		logRestrictBlack.log();
 		logId.log();
 		logSat.log();
 	}
 
 	@Override
 	public StoneColor getColor() {
 		return color;
 	}
 
 	// ************************************************************************
 
 	private void init() {
 		Collection<Position> valid = Collections2.filter(board.getPositions(),
 				new ValidPositionFilter(board));
 		Collection<Position> validEmpty = Collections2.filter(valid,
 				new EmptyPositionFilter(board));
		ArrayList<Position> filtered = Lists.newArrayList(validEmpty);
 		BDDFactory fac = win.getFactory();
 		rating = new double[board.getRows() * board.getColumns()];
 		double sum = 0d;
 		double maxValue = -1d;
 		double minValue = 1d;
 
		int[] varset = new int[valid.size()];
 		int count = 0;
		for (Position p : valid) {
 			varset[count++] = board.getField(p.getRow(), p.getCol()).getIndex();
 		}
 		BDD v = win.getFactory().makeSet(varset);
 
 		for (Position pos : filtered) {
 			Field field = board.getField(pos.getRow(), pos.getCol());
 			BDD bddWin = logId.id(win);
 			BDD bddWinBlack = logId.id(winBlack);
 			if (StoneColor.WHITE.equals(color)) {
 				bddWin = logRestrictWhite.restrictLog(bddWin,
 						fac.ithVar(field.getIndex()));
 				bddWinBlack = logRestrictBlack.restrictLog(bddWinBlack,
 						fac.ithVar(field.getIndex()));
 			} else {
 				bddWin = logRestrictWhite.restrictLog(bddWin,
 						fac.nithVar(field.getIndex()));
 				bddWinBlack = logRestrictBlack.restrictLog(bddWinBlack,
 						fac.nithVar(field.getIndex()));
 			}
 			// double valuation = (logSat.satCountLog(bddWin) - logSat
 			// .satCountLog(bddWinBlack)) / Math.pow(2, fac.varNum());
 			// double valuation = Valuation.compute(bddWin, bddWinBlack,
 			// filtered.size());
 			double valuation = (bddWin.satCount(v) - bddWinBlack.satCount(v))
					/ Math.pow(2, valid.size());
 			rating[field.getIndex()] = valuation;
 			sum += valuation;
 			if (StoneColor.WHITE.equals(color)) {
 				if (valuation > maxValue) {
 					best = field.getIndex();
 					maxValue = valuation;
 				}
 			} else {
 				if (valuation < minValue) {
 					best = field.getIndex();
 					minValue = valuation;
 				}
 			}
 			bddWin.free();
 			bddWinBlack.free();
 		}
 
 		avg = sum / filtered.size();
 	}
 
 	private BDD getVarset() {
 		Collection<Position> allPos = board.getPositions();
 		Collection<Position> filtered = Collections2.filter(allPos,
 				new ValidPositionFilter(board));
 		int[] varset = new int[filtered.size()];
 		int count = 0;
 		for (Position p : filtered) {
 			varset[count++] = board.getField(p.getRow(), p.getCol()).getIndex();
 		}
 		return win.getFactory().makeSet(varset);
 	}
 
 }
