 package com.strategy.api.logic;
 
 import java.util.List;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.strategy.api.board.Board;
import com.strategy.api.field.Field;
 import com.strategy.api.logic.prediction.Prediction;
 import com.strategy.havannah.board.BoardHavannah;
 import com.strategy.havannah.logic.prediction.PredictionHavannah;
 import com.strategy.util.PrimitiveBoardProvider;
 import com.strategy.util.RowConstant;
 import com.strategy.util.StoneColor;
 import com.strategy.util.Turn;
 
 public class GtpWrapper {
 
 	private static final Splitter splitter = Splitter.on(" ")
 			.omitEmptyStrings().trimResults();
 
 	private Board board;
 	private Prediction p;
 
 	public GtpWrapper() {
 	}
 
 	public void setBoardSize(Integer size) {
 		this.board = BoardHavannah.createInstance(
 				PrimitiveBoardProvider.getBoard(size), size);
 	}
 
 	public void setMoves(String moves) {
 		if (null == board) {
 			return;
 		}
 		List<Turn> turns = parseMoves(moves);
 		if (turns.isEmpty()) {
 			p = new PredictionHavannah(board);
 		} else {
 			Turn last = Iterables.getLast(turns);
			Field field = board
					.getField(last.getCoord(), last.getCoordNumber());
			Integer lastIndex = field.getIndex();
 			p = new PredictionHavannah(board, lastIndex, turns);
 		}
 	}
 
 	public String getResponse() {
 		if (null == board || null == p) {
			return "?";
 		}
 
 		List<Turn> turnsSoFar = p.getTurnsSoFar();
 		Integer move;
 		if (turnsSoFar.isEmpty()) {
 			move = p.doCalculatedTurn(StoneColor.BLACK);
 		} else {
 			Turn last = Iterables.getLast(turnsSoFar);
 			StoneColor colorToUse = last.getColor().getOpposite();
 			move = p.doCalculatedTurn(colorToUse);
 		}
 
 		if (null != move) {
			return ""
 					+ RowConstant.parse(move, board.getBoardSize())
 					+ RowConstant
							.parseToCoordNumber(move, board.getBoardSize());
 		}
 
		return "?";
 	}
 
 	// ************************************************************************
 
 	private List<Turn> parseMoves(String moves) {
 		List<Turn> result = Lists.newArrayList();
 		Iterable<String> split = splitter.split(moves);
 		StoneColor color = StoneColor.BLACK;
 		for (String move : split) {
 			String coordLetterStr = move.substring(0, 1);
 			String coordNumberStr = move.substring(1);
 			int coordNumber = Integer.parseInt(coordNumberStr);
 
 			RowConstant coord = RowConstant.parseToConstant(coordLetterStr);
 
 			Turn turn = new Turn(coord, coordNumber, color);
 			result.add(turn);
 			color = color.getOpposite();
 		}
 		return result;
 	}
 
 }
