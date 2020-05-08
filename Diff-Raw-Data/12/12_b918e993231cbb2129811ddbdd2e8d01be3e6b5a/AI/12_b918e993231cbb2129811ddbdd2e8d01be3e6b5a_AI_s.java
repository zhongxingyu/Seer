 package com.github.abalone.ai;
 
 import com.github.abalone.controller.GameController;
 import com.github.abalone.elements.Ball;
 import com.github.abalone.elements.Game;
 import com.github.abalone.util.Color;
 import com.github.abalone.util.Coords;
 import com.github.abalone.util.Direction;
 import com.github.abalone.util.Move;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author keruspe
  */
 public class AI {
 
    private static AI instance;
    private Game game;
    private Color selfColor;
 
    private AI(Game game, Color selfColor) {
       this.game = game;
       this.selfColor = selfColor;
    }
 
    public static void init(Game game, Color AIColor) {
       if (AI.instance == null)
          AI.instance = new AI(game, AIColor);
       AI.instance.getBestMove(Color.WHITE);
    }
 
    public static AI getInstance() {
       return AI.instance;
    }
 
    public Move getBestMove(Color current) {
       Move bestMove = new Move();
       Set<Ball> balls = this.game.getBoard().getBalls();
       for (Ball b : balls) {
          if (b.getColor() != current) {
             continue;
          }
          Set<Coords> coords = new HashSet<Coords>();
          coords.add(b.getCoords());
          Set<Direction> directions = GameController.getInstance().validDirections(coords);
          if (!directions.isEmpty()) {
             Set<Ball> ballsToMove = new HashSet<Ball>();
             ballsToMove.add(b);
             bestMove.setInitialState(ballsToMove);
             bestMove.setDirection(directions.iterator().next());
             break;
          }
       }
       if (current == selfColor) {
          Set<Coords> coords = new HashSet<Coords>();
          for (Ball b : bestMove.getInitialBalls()) {
             if (b.getColor() == current) {
                coords.add(b.getCoords());
             }
          }
         GameController.getInstance().move(coords, bestMove.getDirection(), Boolean.TRUE);
       }
       return bestMove;
    }
 
    public Color getColor() {
       return selfColor;
    }
 
 }
