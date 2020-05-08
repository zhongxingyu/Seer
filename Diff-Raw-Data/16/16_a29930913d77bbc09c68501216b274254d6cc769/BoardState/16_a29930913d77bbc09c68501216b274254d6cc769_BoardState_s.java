 /* -*- c-basic-offset: 2; -*- */
 
 import java.util.*;
 import java.io.*;
 
 /**
  * Data object to hold the state of the board for A* search.
  */
 public class BoardState {
 
   // ---- Set by Board ----
   public Point position;
   public Robot.Move move;
   public int deltaId;	// to somehow store history of the board for fast
                         // backtracking
 
   // ---- Set by AStar ----
   public BoardState parentState;
   public double score;

 }
