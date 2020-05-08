 package com.imie.morpion.model;
 
 import com.imie.morpion.controller.EndGameListener;
 import com.imie.morpion.view.GameListener;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Marc-Antoine Perennou<Marc-Antoine@Perennou.com>
  */
 
 public class Game {
 
    public List<GameListener> listeners;
    public List<EndGameListener> endListeners;
 
    private SquareState me;
 
    private int scoreMe;
    private int scoreOther;
 
    private SquareState[][] squares;
    private GameState state;
 
    private Map<String, SquareState> players;
 
    public Game(SquareState me) {
       this.listeners = new ArrayList<>();
       this.endListeners = new ArrayList<>();
       this.players = new HashMap<>();
 
       this.me = me;
 
       this.scoreMe = 0;
       this.scoreOther = 0;
 
       squares = new SquareState[3][3];
       reset();
    }
 
    public void reset() {
       this.state = GameState.P1_TURN;
       for (int i = 0; i < 9; i++) {
          squares[i / 3][i % 3] = SquareState.EMPTY;
       }
       this.onStateUpdate();
       this.onEndGame();
    }
 
    public void addGameListener(GameListener listener) {
       this.listeners.add(listener);
    }
 
    public void addEndGameListener(EndGameListener listener) {
       this.endListeners.add(listener);
    }
 
    public void onEndGame() {
       for (EndGameListener l : this.endListeners) {
          l.onGameEnd();
       }
    }
 
    public void onSquaresUpdate() {
       for (GameListener l : this.listeners) {
          l.onSquaresUpdate(this.squares);
       }
    }
 
    public void onStateUpdate() {
       for (GameListener l : this.listeners) {
          l.onStateUpdate(this.scoreMe, this.scoreOther);
       }
    }
 
    public void join(String id, SquareState player) {
       this.players.put(id, player);
    }
 
   public void play(Play play) {
       if (squares[play.x][play.y] != SquareState.EMPTY)
         return;
 
       SquareState player = this.players.get(play.player);
       if ((state == GameState.P1_TURN && player == SquareState.P1)
             || (state == GameState.P2_TURN && player == SquareState.P2)) {
          squares[play.x][play.y] = player;
       }
 
       calculateState();
 
       switch (state) {
          case P1_WIN:
             if (me == SquareState.P1)
                scoreMe++;
             else
                scoreOther++;
             reset();
             break;
          case P2_WIN:
             if (me == SquareState.P2)
                scoreMe++;
             else
                scoreOther++;
             reset();
             break;
          case TIE:
             reset();
             break;
       }
 
       this.onSquaresUpdate();
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
