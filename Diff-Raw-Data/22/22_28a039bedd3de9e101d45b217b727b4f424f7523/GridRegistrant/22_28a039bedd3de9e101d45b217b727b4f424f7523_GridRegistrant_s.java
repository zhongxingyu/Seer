 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * The GridRegistrant class represents a player registered with the system. 
  * This class simply maps into the persistent layer to give the system
  * a means to track users, how many games they have won, and how many
  * played.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.grid.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 import mx.ecosur.multigame.model.interfaces.Registrant;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 @NamedQueries( {
    @NamedQuery(name = "Registrant.GetByName",
         query = "select DISTINCT gr from GridRegistrant as gr where gr.name = :name")
 })
 @Entity
 public class GridRegistrant implements Registrant, Cloneable {
 
         private static final long serialVersionUID = 5230114393058543176L;
 
         /*
          * Primary key
          */
         private int id;
 
         /*
          * The player's name
          */
         private String name;
         
         /* 
          * The player's password
          */
         private String password;
 
         /*
          * The number of games this player has played
          */
         private int gamecount;
 
         /*
          * The number of games this player has won
          */
         private int wins;
 
         /*
          * Last time this player logged in to play
          */
         private long lastRegistration;
         
         public enum PlayerType {
                 ADMINISTRATOR, PLAYER
         }
 
         public GridRegistrant() {
                 super();
         }
         
         public GridRegistrant (String name) {
                 super();
                 this.name = name;
         }
 
         /**
          * @return the id
          */
         @Id
         @GeneratedValue
         public int getId() {
                 return id;
         }
 
         /**
          * @param id
          *            the id to set
          */
         public void setId(int id) {
                 this.id = id;
         }
 
         /**
          * @return the name
          */
         public String getName() {
                 return name;
         }
 
         /**
          * @param name
          *            the name to set
          */
         public void setName(String name) {
                 this.name = name;
         }
 
         /**
          * @return the gamecount
          */
         public int getGamecount() {
                 return gamecount;
         }
 
         /**
          * @param gamecount
          *            the gamecount to set
          */
         public void setGamecount(int gamecount) {
                 this.gamecount = gamecount;
         }
 
         /**
          * @return the wins
          */
         public int getWins() {
                 return wins;
         }
 
         /**
          * @param wins
          *            the wins to set
          */
         public void setWins(int wins) {
                 this.wins = wins;
         }
 
         /**
          * @return the lastRegistration
          */
         public long getLastRegistration() {
                 return lastRegistration;
         }
 
         /**
          * @param lastRegistration
          *            the lastRegistration to set
          */
         public void setLastRegistration(long lastRegistration) {
                 this.lastRegistration = lastRegistration;
         }
 
         public String getPassword() {
                 return password;
         }
 
         public void setPassword(String password) {
                 this.password = password;
         }
 
         public String toString() {
                 return "id = " + id + ", name = " + name + ", gamecount = " + gamecount
                                 + ", lastRegistration = " + lastRegistration + ", wins = "
                                 + wins;
         }
     
     @Override
     public boolean equals(Object obj) {
         boolean ret = obj instanceof GridRegistrant;
         if (ret) {
             GridRegistrant comp = (GridRegistrant) obj;
             ret = (comp.getId() == this.getId());
         } else
             ret = super.equals(obj);
         return ret;
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(id).append(name).append(lastRegistration).append(gamecount).append(wins).toHashCode();
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         super.clone();
         GridRegistrant ret = new GridRegistrant();
         ret.id = this.id;
         ret.gamecount = this.gamecount;
         ret.lastRegistration = this.lastRegistration;
         ret.name = this.name;
         ret.password = this.password;
         ret.wins = this.wins;
         return ret;
     }
 }
