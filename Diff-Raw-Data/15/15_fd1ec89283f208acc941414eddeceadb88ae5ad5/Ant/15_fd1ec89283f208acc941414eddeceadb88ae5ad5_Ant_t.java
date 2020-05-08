 package com.bradleybuda;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class Ant {
     private static final Set<Ant> LIVING = new HashSet<Ant>();
     private static int nextAntId = 0;
     
     private final int id;
     private Square square;
     private Square nextSquare;
     private Goal goal;
     
     public Ant(Square square) {
         this.id = nextAntId;
         nextAntId++;
         this.square = this.nextSquare = square;
         square.setAnt(this);
         square.setNextAnt(this);
         LIVING.add(this);
     }
     
    public static void advanceAllTurns() {
         for (final Ant a : LIVING)
             a.advanceTurn();
     }
     
     public boolean isAlive() {
         return LIVING.contains(this);
     }
     
     public void die() {
         LIVING.remove(this);
         square = nextSquare = null;
     }
     
     // Entering this method, all squares should have ant == null
     public void advanceTurn() {
         square = nextSquare;
         square.setAnt(this);
         square.setNextAnt(this);        
     }
     
     public void orderTo(final Square adjacent) {
         nextSquare = adjacent;
         nextSquare.setNextAnt(this);
         
         AI.order(square, square.directionTo(adjacent));
     }
     
     @Override
     public String toString() {
         return String.format("<Ant %d at %s>", id, square);
     }
     
     @Override
     public int hashCode() {
         HashCodeBuilder b = new HashCodeBuilder();
         b.append(square);
         return b.hashCode();
     }
     
     @Override
     public boolean equals(Object obj) {
         EqualsBuilder b = new EqualsBuilder();
         b.append(this.square, ((Ant)obj).square);
         return b.isEquals();
     }
 }
