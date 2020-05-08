 package com.github.abalone.controller;
 
 import com.github.abalone.ai.AI;
 import com.github.abalone.config.Config;
 import com.github.abalone.elements.Ball;
 import com.github.abalone.elements.Board;
 import com.github.abalone.elements.Game;
 import com.github.abalone.util.Typelignepl;
 import com.github.abalone.util.Color;
 import com.github.abalone.util.Coords;
 import com.github.abalone.util.Direction;
 import com.github.abalone.util.Move;
 import com.github.abalone.view.Window;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author keruspe
  * @author sardemff7
  */
 public class GameController {
 
    private static GameController singleton;
 
    private Window window;
 
    private Game game;
 
    private Move currentBestMove;
 
    private GameController() {
    }
 
    public static GameController getInstance() {
       if (GameController.singleton == null) {
          GameController.singleton = new GameController();
       }
       return GameController.singleton;
    }
 
    /// Launch a new game
    public void launch() {
       Board.getInstance().fill(null);
       this.game = new Game(Color.WHITE, -1, -1);
       AI.init(this.game, ((Boolean) Config.get("AI")) ? Color.BLACK : Color.NONE);
       this.window.updateBoard();
    }
 
    /// Save the game
    public void save() {
       FileOutputStream fos = null;
       ObjectOutputStream oos = null;
       try {
          File f = new File("abalone.save");
          fos = new FileOutputStream(f);
          oos = new ObjectOutputStream(fos);
          oos.writeObject(this.game);
       } catch (Exception ex) {
          Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
       } finally {
          try {
             fos.close();
             oos.close();
          } catch (IOException ex) {
             Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
    }
 
    /// Load the saved game
    public void load() {
       FileInputStream fis = null;
       ObjectInputStream ois = null;
       try {
          File f = new File("abalone.save");
          fis = new FileInputStream(f);
          ois = new ObjectInputStream(fis);
          Game loadedGame = (Game) ois.readObject();
          this.game = new Game(loadedGame.getTurn(), loadedGame.getTimeLeft(), loadedGame.getTurnsLeft());
          this.game.setHistory(loadedGame.getHistory());
          this.game.setBoard(Board.getInstance());
          this.game.getBoard().fill(loadedGame);
          AI.init(this.game, Color.BLACK);
          this.window.updateBoard();
       } catch (Exception ex) {
          Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
       } finally {
          try {
             fis.close();
             ois.close();
          } catch (IOException ex) {
             Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
       this.window.updateBoard();
    }
 
    /// Quit the game
    public void quit() {
       System.exit(0);
    }
 
    private Color opponent(Color self) {
       Color opponent = Color.NONE;
       if (self == Color.BLACK) {
          opponent = Color.WHITE;
       } else if (self == Color.WHITE) {
          opponent = Color.BLACK;
       }
       return opponent;
    }
 
    //renvoi la bille la plus proche de la bille adverse ou de la case vide
    private Ball closest(Set<Ball> selectedBalls, Direction to) {
       Coords closest = null;
       switch (to) {
          case DOWNLEFT:
             closest = new Coords(-10, 10);
             for (Ball b : selectedBalls) {
                if ((closest.getRow() < b.getCoords().getRow())
                        || (closest.getRow().equals(b.getCoords().getRow())
                        && closest.getCol() > b.getCoords().getCol())) {
                   closest = new Coords(b.getCoords());
                }
             }
             break;
          case DOWNRIGHT:
             closest = new Coords(-10, -10);
             for (Ball b : selectedBalls) {
                if ((closest.getRow() < b.getCoords().getRow())
                        || (closest.getRow().equals(b.getCoords().getRow())
                        && closest.getCol() < b.getCoords().getCol())) {
                   closest = new Coords(b.getCoords());
                }
             }
             break;
          case UPLEFT:
             closest = new Coords(10, 10);
             for (Ball b : selectedBalls) {
                if ((closest.getRow() > b.getCoords().getRow())
                        || (closest.getRow().equals(b.getCoords().getRow())
                        && closest.getCol() > b.getCoords().getCol())) {
                   closest = new Coords(b.getCoords());
                }
             }
             break;
          case UPRIGHT:
             closest = new Coords(10, -10);
             for (Ball b : selectedBalls) {
                if ((closest.getRow() > b.getCoords().getRow())
                        || (closest.getRow().equals(b.getCoords().getRow())
                        && closest.getCol() < b.getCoords().getCol())) {
                   closest = new Coords(b.getCoords());
                }
             }
             break;
          case LEFT:
             closest = new Coords(10, 10);
             for (Ball b : selectedBalls) {
                if (closest.getCol() > b.getCoords().getCol()) {
                   closest = new Coords(b.getCoords());
                }
             }
             break;
          case RIGHT:
             closest = new Coords(10, -10);
             for (Ball b : selectedBalls) {
                if (closest.getCol() < b.getCoords().getCol()) {
                   closest = new Coords(b.getCoords());
                }
             }
       }
       return this.game.getBoard().getBallAt(closest);
 
    }
 
    private Set<Ball> validMove2(Set<Ball> selectedBalls, Direction direction, Color selfColor) {
       Iterator<Ball> itb = selectedBalls.iterator();
       Set<Ball> result = new HashSet<Ball>();
       Ball b1;
       Ball b2;
       Ball b3;
       switch (selectedBalls.size()) {
          case 1:
             b1 = itb.next();
             if (this.game.getBoard().getBallAt(b1, direction).getColor() == Color.NONE) {
                result.add(b1);
             }
             break;
          case 2:
             b1 = itb.next();
             b2 = itb.next();
             if (Typelignepl.lesDirectionPerpendiculaire(b1.getCoords().LignePl(b2.getCoords())).contains(direction)) {
                Color nextColor1 = this.game.getBoard().getBallAt(b1, direction).getColor();
                Color nextColor2 = this.game.getBoard().getBallAt(b2, direction).getColor();
                if ((nextColor1 == Color.NONE) && (nextColor2 == Color.NONE)) {
                   result.add(b1);
                   result.add(b2);
                }
             } else {
                Ball closest = closest(selectedBalls, direction);
                Ball next = this.game.getBoard().getBallAt(closest, direction);
                if (next.getColor() == Color.NONE) {
                   result.add(b1);
                   result.add(b2);
                } else if (next.getColor() == this.opponent(selfColor)) {
                   Color nextColor = this.game.getBoard().getBallAt(next, direction).getColor();
                   if (nextColor == Color.NONE || nextColor == Color.INVALID) {
                      result.add(b1);
                      result.add(b2);
                      result.add(next);
                   }
                }
             }
             break;
          case 3:
             b1 = itb.next();
             b2 = itb.next();
             b3 = itb.next();
             Typelignepl linepl = b1.getCoords().LignePl(b2.getCoords());
             if (linepl == Typelignepl.NONADJACENT) {
                linepl = b1.getCoords().LignePl(b3.getCoords());
             }
             if (Typelignepl.lesDirectionPerpendiculaire(linepl).contains(direction)) {
                Color nextColor1 = this.game.getBoard().getBallAt(b1, direction).getColor();
                Color nextColor2 = this.game.getBoard().getBallAt(b2, direction).getColor();
                Color nextColor3 = this.game.getBoard().getBallAt(b3, direction).getColor();
                if (nextColor1 == Color.NONE && nextColor2 == Color.NONE && nextColor3 == Color.NONE) {
                   result.add(b1);
                   result.add(b2);
                   result.add(b3);
                }
             } else {
                Ball closest = closest(selectedBalls, direction);
                Ball next1 = this.game.getBoard().getBallAt(closest, direction);
                if (next1.getColor() == Color.NONE) {
                   result.add(b1);
                   result.add(b2);
                   result.add(b3);
                } else if (next1.getColor() == this.opponent(selfColor)) {
                   Ball next2 = this.game.getBoard().getBallAt(next1, direction);
                   Color nextColor2 = next2.getColor();
                   if (nextColor2 == Color.NONE || nextColor2 == Color.INVALID) {
                      result.add(b1);
                      result.add(b2);
                      result.add(b3);
                      result.add(next1);
                   } else {
                      Color nextColor3 = this.game.getBoard().getBallAt(next2, direction).getColor();
                      if (nextColor3 == Color.NONE || nextColor3 == Color.INVALID) {
                         result.add(b1);
                         result.add(b2);
                         result.add(b3);
                         result.add(next1);
                         result.add(next2);
                      }
                   }
                }
             }
             break;
       }
       return result;
    }
 
    private Set<Ball> validMove(Set<Coords> selectedBallsCoords, Direction direction, Color current) {
       if (!areALine(selectedBallsCoords)) {
          return new HashSet<Ball>();
       }
       Set<Ball> selectedBalls = new HashSet<Ball>();
       for (Coords c : selectedBallsCoords) {
          Ball b = this.game.getBoard().getBallAt(c);
          if (b.getColor() != current) {
             return new HashSet<Ball>();
          }
          selectedBalls.add(b);
       }
       return validMove2(selectedBalls, direction, current);
    }
 
    private Boolean validMove(Set<Coords> selectedBallsCoords, Direction direction) {
       Set<Ball> balls = validMove(selectedBallsCoords, direction, this.game.getTurn());
       return (!balls.isEmpty());
    }
 
    public Set<Direction> validDirections(Set<Coords> selectedBallsCoords) {
       Set<Direction> answer = new HashSet<Direction>();
       if (validMove(selectedBallsCoords, Direction.UPLEFT)) {
          answer.add(Direction.UPLEFT);
       }
       if (validMove(selectedBallsCoords, Direction.UPRIGHT)) {
          answer.add(Direction.UPRIGHT);
       }
       if (validMove(selectedBallsCoords, Direction.LEFT)) {
          answer.add(Direction.LEFT);
       }
       if (validMove(selectedBallsCoords, Direction.RIGHT)) {
          answer.add(Direction.RIGHT);
       }
       if (validMove(selectedBallsCoords, Direction.DOWNLEFT)) {
          answer.add(Direction.DOWNLEFT);
       }
       if (validMove(selectedBallsCoords, Direction.DOWNRIGHT)) {
          answer.add(Direction.DOWNRIGHT);
       }
       return answer;
    }
 
    public Boolean move(Set<Coords> selectedBallsCoords, Direction direction) {
       Color current = this.game.getTurn();
       if (current == Color.NONE) {
          return Boolean.FALSE;
       }
       Set<Ball> ballsTomove = validMove(selectedBallsCoords, direction, current);
       if (!ballsTomove.isEmpty()) {
          Move move = new Move(ballsTomove);
          move.setFinalState(this.game.getBoard().move(ballsTomove, direction));
          this.game.addToHistory(move);
          this.window.updateBoard();
          Move bestMove = AI.getInstance().getBestMove(this.game.getNextTurn());
          if (!bestMove.isAIMove()) {
             this.currentBestMove = bestMove;
          }
       }
       return Boolean.TRUE;
    }
 
    public Move getCurrentBestMove() {
       return currentBestMove;
    }
 
    private void doGoBack() {
       int lastIndex = this.game.getHistory().size() - 1;
       if (lastIndex != -1) {
          this.game.getPreviousTurn();
          Move move = this.game.getHistory().get(lastIndex);
          this.game.getHistory().remove(move);
          this.game.getBoard().revert(move);
          this.window.updateBoard();
       }
    }
 
    public void goBack() {
       if (this.game == null) {
          return;
       }
       doGoBack();
      if ((Boolean) Config.get("AI")) {
          doGoBack();
       }
    }
 
    public Boolean areALine(Set<Coords> coords) {
       Iterator<Coords> itc = coords.iterator();
       Coords c1, c2, c3;
       switch (coords.size()) {
          case 1:
             return Boolean.TRUE;
          case 2:
             c1 = itc.next();
             c2 = itc.next();
             if (c1.getRow().equals(c2.getRow())) {
                return (Math.abs(c1.getCol() - c2.getCol()) == 1);
             } else if (Math.abs(c1.getRow() - c2.getRow()) != 1) {
                return Boolean.FALSE;
             } else {
                Integer diff;
                if (c1.getRow() < c2.getRow()) {
                   diff = c2.getCol() - c1.getCol();
                } else {
                   diff = c1.getCol() - c2.getCol();
                }
                if (c1.getRow() < 0) {
                   return (diff == 0 || diff == 1);
                } else {
                   return (diff == 0 || diff == -1);
                }
             }
          case 3:
             c1 = itc.next();
             c2 = itc.next();
             c3 = itc.next();
             Set<Coords> sub1 = new HashSet<Coords>();
             sub1.add(c1);
             sub1.add(c2);
             Set<Coords> sub2 = new HashSet<Coords>();
             sub2.add(c1);
             sub2.add(c3);
             Integer colModifier = 0;
             if (areALine(sub1)) {
                if (!c3.getRow().equals(0)) {
                   if ((c1.getRow().equals(0)
                           && !c2.getRow().equals(0)
                           && (c2.getRow() == -c3.getRow()))
                           || (c2.getRow().equals(0)
                           && (c1.getRow() == -c3.getRow()))) {
                      colModifier = -1;
                   }
                }
                return ((c3.getRow().equals(2 * c2.getRow() - c1.getRow()) && c3.getCol().equals(2 * c2.getCol() - c1.getCol() + colModifier))
                        || (c3.getRow().equals(2 * c1.getRow() - c2.getRow()) && c3.getCol().equals(2 * c1.getCol() - c2.getCol() + colModifier)));
             } else if (areALine(sub2)) {
                if (!c2.getRow().equals(0)) {
                   if ((c1.getRow().equals(0)
                           && !c3.getRow().equals(0)
                           && (c2.getRow() == -c3.getRow()))
                           || (c3.getRow().equals(0)
                           && (c2.getRow() == -c1.getRow()))) {
                      colModifier = -1;
                   }
                }
                return ((c2.getRow().equals(2 * c3.getRow() - c1.getRow()) && c2.getCol().equals(2 * c3.getCol() - c1.getCol() + colModifier))
                        || (c2.getRow().equals(2 * c1.getRow() - c3.getRow()) && c2.getCol().equals(2 * c1.getCol() - c3.getCol() + colModifier)));
             }
       }
       return Boolean.FALSE;
    }
 
    public void setWindow(Window window) {
       this.window = window;
    }
 }
