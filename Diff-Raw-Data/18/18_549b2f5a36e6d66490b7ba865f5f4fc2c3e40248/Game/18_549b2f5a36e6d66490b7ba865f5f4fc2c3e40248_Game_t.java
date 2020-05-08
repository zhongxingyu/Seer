 package com.imie.morpion.model;
 
 import com.imie.morpion.controller.ViewListener;
 import com.imie.morpion.exception.GameFinishedException;
 import com.imie.morpion.exception.NonEmptySquareException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Marc-Antoine Perennou<Marc-Antoine@Perennou.com>
  */
 
 public class Game {
 
    public List<ViewListener> listeners;
 
    public int score1;
    public int score2;
 
   private SquareState[][] squares;
   private GameState state;
 
    public Game() {
       this.listeners = new ArrayList<ViewListener>();
 
       this.score1 = 0;
       this.score2 = 0;
       this.state = GameState.P1_TURN;
 
       squares = new SquareState[3][3];
       for (int i = 0; i < 9; i++) {
          squares[i / 3][i % 3] = SquareState.EMPTY;
       }
    }
 
   public SquareState[][] getSquares() {
      return this.squares;
   }

    public void subscribe(ViewListener listener) {
       this.listeners.add(listener);
    }
 
    public void notifAll() {
       for (ViewListener listener : this.listeners) {
          listener.onViewChanged();
       }
    }
 
    public void play(int x, int y) {
       if (squares[x][y].equals(SquareState.EMPTY)) {
          switch (state) {
             case P1_TURN:
                squares[x][y] = SquareState.P1;
                break;
             case P2_TURN:
                squares[x][y] = SquareState.P2;
                break;
             default:
                throw new GameFinishedException();
          }
       } else {
          throw new NonEmptySquareException();
       }
 
       calculateState();
      this.notifAll();
    }
 
    private void calculateState() {
       for (int i = 0; i < 3; i++) {
          /* Horizontal check */
          if (squares[0][i] == SquareState.P1 && squares[1][i] == SquareState.P1 && squares[2][i] == SquareState.P1) {
             state = GameState.P1_WIN;
             return;
          } else if (squares[0][i] == SquareState.P2 && squares[1][i] == SquareState.P2 && squares[2][i] == SquareState.P2) {
             state = GameState.P2_WIN;
             return;
          }
          /* Vertical check */
          else if (squares[i][0] == SquareState.P1 && squares[i][1] == SquareState.P1 && squares[i][2] == SquareState.P1) {
             state = GameState.P1_WIN;
             return;
          } else if (squares[i][0] == SquareState.P2 && squares[i][1] == SquareState.P2 && squares[i][2] == SquareState.P2) {
             state = GameState.P2_WIN;
             return;
          }
       }
 
       /* First diagonal check */
       if (squares[0][0] == SquareState.P1 && squares[1][1] == SquareState.P1 && squares[2][2] == SquareState.P1) {
          state = GameState.P1_WIN;
          return;
       } else if (squares[0][0] == SquareState.P2 && squares[1][1] == SquareState.P2 && squares[2][2] == SquareState.P2) {
          state = GameState.P2_WIN;
          return;
       }
 
       /* Second diagonal check */
       if (squares[2][0] == SquareState.P1 && squares[1][1] == SquareState.P1 && squares[0][2] == SquareState.P1) {
          state = GameState.P1_WIN;
          return;
       } else if (squares[2][0] == SquareState.P2 && squares[1][1] == SquareState.P2 && squares[0][2] == SquareState.P2) {
          state = GameState.P2_WIN;
          return;
       }
 
       /* Tie check */
       boolean all = true;
       for (int i = 0; i < 9; i++) {
          int x = i / 3;
          int y = i % 3;
          if (squares[i / 3][i % 3].equals(SquareState.EMPTY)) {
             all = false;
             break;
          }
       }
 
       if (all) {
          state = GameState.TIE;
       } else if (state.equals(GameState.P1_TURN)) {
          state = GameState.P2_TURN;
       } else {
          state = GameState.P1_TURN;
       }
    }
 }
