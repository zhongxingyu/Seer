 package edu.uwm.cs552;
 
 import java.awt.Color;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Queue;
 
 public class Player extends Observable {
   public Player(RailGame railGame, int i, Color color, double startup) {
     pNum = i;
     game = railGame;
     pColor = color;
     start = startup;
     turns = 10;
   }
 
   public int getNum() {
     return pNum;
   }
 
   public double getCash() {
     return start; // TODO : don't know what this variable is for sure
   }
 
   public HexCoordinate getTrain() {
     return train;
   }
 
   public void setTrain(HexCoordinate coordinate) {
     train = coordinate;
   }
 
   public void addProposedTrack(HexEdge edge) {
     proposedTrack.add(edge);
   }
 
   public HexEdge getNextProposedTrack() {
     if (proposedTrack.isEmpty())
       return null;
 
     setChanged();
     return proposedTrack.remove();
   }
 
   public void addTrainIntent(HexCoordinate coordinate) {
     trainIntent.add(coordinate);
   }
 
   public HexCoordinate getNextTrainIntent() {
     if (trainIntent.isEmpty())
       return null;
 
     setChanged();
     return trainIntent.remove();
   }
 
   public int getTurns() {
     return turns;
   }
 
   public void setTurns(int turns) {
     this.turns = turns;
 
     setChanged();
     notifyObservers();
   }
 
   private int pNum, turns;
   private RailGame game;
   private Color pColor;
   private double start;
 
   private HexCoordinate train;
   private Queue<HexEdge> proposedTrack = new LinkedList<HexEdge>();
   private Queue<HexCoordinate> trainIntent = new LinkedList<HexCoordinate>();
 }
