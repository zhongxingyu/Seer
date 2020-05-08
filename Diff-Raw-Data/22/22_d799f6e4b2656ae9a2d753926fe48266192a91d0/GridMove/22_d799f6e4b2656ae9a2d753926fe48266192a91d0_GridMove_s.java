 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * A move is an object which contains a GridRegistrant, a destination cell, and 
  * optionally, an originating cell. Moves are processed by the SharedBoard
  * and when successful, are integrated into a specific game's GameGrid.  
  * Success and integration are determined by each game's rule set.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.grid.entity;
 
 import javax.persistence.*;
 
 import mx.ecosur.multigame.enums.MoveStatus;
 import mx.ecosur.multigame.grid.comparator.MoveComparator;
 import mx.ecosur.multigame.model.interfaces.Cell;
 import mx.ecosur.multigame.model.interfaces.GamePlayer;
 import mx.ecosur.multigame.model.interfaces.Move;
 
 @Entity
 public class GridMove implements Move, Cloneable, Comparable {
 
     private static final long serialVersionUID = 8017901476308051472L;
    private int id, orderId;
     protected GridPlayer player;
     protected GridCell current;
     protected GridCell destination;
 
     private MoveStatus status;
 
     public GridMove() {
         super();
         this.status = MoveStatus.UNVERIFIED;
         this.current = null;
         this.destination = null;
         this.player = null;
     }
 
     public GridMove(GridPlayer player, GridCell destination) {
         this.player = player;
         this.current = null;
         this.destination = destination;
         this.status = MoveStatus.UNVERIFIED;
     }
 
     public GridMove(GridPlayer player, GridCell current, GridCell destination) {
         this.player = player;
         this.current = current;
         this.destination = destination;
         this.status = MoveStatus.UNVERIFIED;
     }
 
     @Id
     @GeneratedValue
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

     @OneToOne  (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
     public GridCell getDestinationCell() {
         return this.destination;
     }
 
     public void setDestinationCell(GridCell destination) {
         this.destination = destination;
     }
 
     @OneToOne  (cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
     public GridCell getCurrentCell() {
         return this.current;
     }
 
     public void setCurrentCell(GridCell current) {
         this.current = current;
     }
 
     @OneToOne  (cascade={CascadeType.ALL},fetch=FetchType.EAGER)
     public GridPlayer getPlayer() {
         return this.player;
     }
 
     public void setPlayer(GridPlayer player) {
         this.player = player;
     }
 
     @Transient
     public GamePlayer getPlayerModel () {
         return player;
     }
 
     @Transient
     public void setPlayerModel (GamePlayer player) {
         this.player = (GridPlayer) player;
     }
 
     /**
      * @return the status
      */
     @Enumerated(EnumType.STRING)
     public MoveStatus getStatus() {
         return status;
     }
 
     /**
      * @param status
      *            the status to set
      */
     public void setStatus(MoveStatus status) {
         this.status = status;
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.entity.interfaces.Move#setCurrent(mx.ecosur.multigame.entity.interfaces.Cell)
      */
     public void setCurrentCell(Cell cellImpl) {
             setCurrentCell((GridCell) cellImpl);
     }
 
     /* (non-Javadoc)
      * @see mx.ecosur.multigame.entity.interfaces.Move#setDestination(mx.ecosur.multigame.entity.interfaces.Cell)
      */
     public void setDestinationCell(Cell cellImpl) {
             setDestinationCell((GridCell) cellImpl);
     }
 
     public String toString() {
             return "Registrant: " + player + "\nCurrent: " + current
                             + "\nDestination: " + destination + "\nStatus: " + status;
     }
 
     @Override
         protected Object clone () throws CloneNotSupportedException {
         return super.clone();
     }
 
     @Override
     public int compareTo(Object o) {
         MoveComparator m = new MoveComparator();
         return m.compare(this, (GridMove) o);
     }
 }
