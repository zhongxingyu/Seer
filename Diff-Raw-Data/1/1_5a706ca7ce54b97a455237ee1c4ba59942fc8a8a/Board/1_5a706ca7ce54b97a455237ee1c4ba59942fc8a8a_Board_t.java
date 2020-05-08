 package com.github.abalone.elements;
 
 import com.github.abalone.controller.GameController;
 import com.github.abalone.util.Color;
 import com.github.abalone.util.Coords;
 import com.github.abalone.util.Direction;
 import com.github.abalone.controller.Move;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  *
  * @author keruspe
  */
 public class Board implements Serializable {
 
    private static Board singleton;
 
    private HashSet<Ball> balls = new HashSet<Ball>(28);
 
    private boolean filled;
 
    private void addBall(Ball ball) {
       this.balls.add(ball);
    }
 
    public Color elementAt(Coords coords) {
       Integer col = coords.getCol();
       Integer row = Math.abs(coords.getRow());
       if (col < 0 || row > 4 || row + col > 8) {
          return Color.INVALID;
       }
       Ball ball = new Ball(Color.WHITE, coords);
       if (this.balls.contains(ball)) {
          return Color.WHITE;
       }
       ball.setColor(Color.BLACK);
       if (this.balls.contains(ball)) {
          return Color.BLACK;
       }
       return Color.NONE;
    }
 
    public Ball getBallAt(Coords coords) {
       Integer col = coords.getCol();
       Integer row = Math.abs(coords.getRow());
       if (col < 0 || row > 4 || row + col > 8) {
          return null;
       }
       Ball ball = new Ball(Color.WHITE, coords);
       if (this.balls.contains(ball)) {
          return ball;
       }
       ball.setColor(Color.BLACK);
       if (this.balls.contains(ball)) {
          return ball;
       }
       return null;
    }
 
    private Ball getColorBallAt(Color color, Coords coords) {
       Integer col = coords.getCol();
       Integer row = Math.abs(coords.getRow());
       if (col < 0 || row > 4 || row + col > 8) {
          return null;
       }
       Ball ball = new Ball(color, coords);
       if (this.balls.contains(ball)) {
          return ball;
       }
       return null;
    }
 
    public Set<Ball> getLineColorBallsAt(Set<Coords> coords, Color color) {
       Set<Ball> selectedBalls = new HashSet<Ball>();
       for (Coords c : coords) {
          Ball b = this.getColorBallAt(color, c);
          if ( b == null )
             return null;
          selectedBalls.add(b);
       }
       if ( areALine(selectedBalls) ) {
          return selectedBalls;
       }
       return null;
    }
 
    public Ball getBallAt(Ball ball, Direction direction) {
       Integer row = ball.getCoords().getRow();
       Integer col = ball.getCoords().getCol();
       switch (direction) {
          case UPLEFT:
             if (--row < 0) {
                --col;
             }
             break;
          case UPRIGHT:
             if (--row > -1) {
                ++col;
             }
             break;
          case LEFT:
             --col;
             break;
          case RIGHT:
             ++col;
             break;
          case DOWNLEFT:
             if (++row > 0) {
                --col;
             }
             break;
          case DOWNRIGHT:
             if (++row < 1) {
                ++col;
             }
             break;
       }
       Coords newCoords = new Coords(row, col);
       Ball returnBall = this.getBallAt(newCoords);
       if (returnBall == null) {
          returnBall = new Ball(this.elementAt(newCoords), newCoords);
       }
       return returnBall;
    }
 
    private Board() {
       this.filled = false;
    }
 
    public Board(Board other) {
       this.filled = true;
       this.balls = new HashSet<Ball>();
       for ( Ball b : other.balls )
           this.balls.add(new Ball(b));
    }
 
    public void fill(Game p) {
       if (this.filled) {
          return;
       }
       if (p == null) {
          for (int i = 0; i <= 4; ++i) {
             this.addBall(new Ball(Color.WHITE, -4, i));
             this.addBall(new Ball(Color.BLACK, 4, i));
          }
          for (int i = 0; i <= 5; ++i) {
             this.addBall(new Ball(Color.WHITE, -3, i));
             this.addBall(new Ball(Color.BLACK, 3, i));
          }
          for (int i = 2; i <= 4; ++i) {
             this.addBall(new Ball(Color.WHITE, -2, i));
             this.addBall(new Ball(Color.BLACK, 2, i));
          }
       } else {
          this.balls = p.getBoard().balls;
       }
 
       this.filled = true;
    }
 
    public static Board getInstance() {
       if (Board.singleton == null) {
          Board.singleton = new Board();
       }
       return Board.singleton;
    }
 
    /**
     * Returns the list of ball, read-only
     * @return the ball list as a {Set<Ball>}
     */
    public Set<Ball> getBalls() {
       return Collections.unmodifiableSet(this.balls);
    }
 
    public Integer ballsCount(Color color) {
       Integer count = 0;
       for (Ball b : balls) {
          if (b.getColor() == color) {
             ++count;
          }
       }
       return count;
    }
 
    public Color dominant() {
       Integer white = ballsCount(Color.WHITE);
       Integer black = ballsCount(Color.BLACK);
       if (white > black) {
          return Color.WHITE;
       } else if (black > white) {
          return Color.BLACK;
       } else {
          return Color.NONE;
       }
    }
 
    public void apply(Move move) throws RuntimeException {
       if ( ! move.isValid() )
          throw new RuntimeException("Try to apply an invalid move");
       this.balls.removeAll(move.getInitialBalls());
       this.balls.addAll(move.getFinalBalls());
    }
 
    public void revert(Move move) {
       if ( ! move.isValid() )
          throw new RuntimeException("Try to apply an invalid move");
       this.balls.removeAll(move.getFinalBalls());
       this.balls.addAll(move.getInitialBalls());
    }
 
    public Boolean loose(Color color) {
       return (ballsCount(color) < 9);
    }
    
    public Boolean areALine(Set<Ball> coords) {
       Iterator<Ball> itc = coords.iterator();
       Coords c1, c2, c3;
       switch (coords.size()) {
          case 1:
             return Boolean.TRUE;
          case 2:
             c1 = itc.next().getCoords();
             c2 = itc.next().getCoords();
             if (c1.getRow().equals(c2.getRow())) {
                return (Math.abs(c1.getCol() - c2.getCol()) == 1);
             } else if (Math.abs(c1.getRow() - c2.getRow()) == 1) {
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
            break;
          case 3:
             Ball b1 = itc.next();
             Ball b2 = itc.next();
             Ball b3 = itc.next();
             Set<Ball> sub1 = new HashSet<Ball>();
             sub1.add(b1);
             sub1.add(b2);
             Set<Ball> sub2 = new HashSet<Ball>();
             sub2.add(b1);
             sub2.add(b3);
             Integer colModifier = 0;
             c1 = b1.getCoords();
             c2 = b2.getCoords();
             c3 = b3.getCoords();
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
 }
