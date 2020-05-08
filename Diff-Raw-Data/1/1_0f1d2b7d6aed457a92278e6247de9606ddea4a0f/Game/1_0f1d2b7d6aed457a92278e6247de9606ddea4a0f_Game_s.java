 package com.github.abalone.elements;
 
 import com.github.abalone.util.Move;
 import com.github.abalone.util.Color;
 import java.util.ArrayList;
 
 /**
  *
  * @author joe
  */
 public class Game {
 
    private Board board;
 
    private Color turn;
 
    private Integer timeLeft;
 
    private Integer turnsLeft;
 
    private ArrayList<Move> history;
 
    public Game(Color turn, Integer time, Integer turns) {
       this.board = Board.getInstance();
       this.turn = turn;
       this.timeLeft = time;
       this.turnsLeft = turns;
    }
 
    public ArrayList<Move> getHistory() {
       return history;
    }
 
    public void addToHistory(Move move) {
       this.history.add(move);
    }
 
    public Integer getTurnsLeft() {
       return turnsLeft;
    }
 
    public Integer getTimeLeft() {
       return timeLeft;
    }
 
    public Board getBoard() {
       return this.board;
    }
 
    public Color getTurn() {
       return this.turn;
    }
 
    public Color getTurnAndGoNext() {
       Color current = this.turn;
       if (!this.turnsLeft.equals(-1)) {
          --this.turnsLeft;
       }
       if (this.turnsLeft.equals(0)) {
          this.turn = Color.NONE;
       } else {
          this.turn = (this.turn.equals(Color.BLACK)) ? Color.WHITE : Color.BLACK;
       }
       return current;
    }
 }
