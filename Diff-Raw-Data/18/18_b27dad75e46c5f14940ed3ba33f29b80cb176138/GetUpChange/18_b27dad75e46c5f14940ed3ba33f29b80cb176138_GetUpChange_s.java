 /*
  * Copyright 2011 stefan.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.smurn.pokerutils.automata;
 
 import net.jcip.annotations.Immutable;
 import org.apache.commons.lang.NullArgumentException;
 import org.smurn.pokerutils.Player;
 import org.smurn.pokerutils.Seat;
 import org.smurn.pokerutils.Table;
 
 /**
  * Change occuring if a player is leaving a table.
  */
 @Immutable
 public final class GetUpChange implements Change {
 
     /** Player that is getting up. */
     private final Player player;
     /** Seat from which the player is leaving. */
     private final int seatNr;
     /** Number of chips the player takes with him. */
     private final int stake;
 
     /**
      * Creates an instance.
      * @param player Player that is leaving. Must not be {@code null}.
      * @param seatNr Seat number from which the player is getting up. Must be
      * non-negative.
      * @param stake Number of chips the player takes with him. Must be
      * non-negative.
      */
     public GetUpChange(final Player player, final int seatNr,
             final int stake) {
         if (player == null) {
             throw new NullArgumentException("player");
         }
         if (seatNr < 0) {
             throw new IllegalArgumentException("seatNr must be non-negative.");
         }
         if (stake < 0) {
             throw new IllegalArgumentException("stake must be non-negative.");
         }
         this.player = player;
         this.seatNr = seatNr;
         this.stake = stake;
     }
 
     /**
      * Applies this change to a table.
      * This sets the seat's player field to {@code null} and the stake field
      * to 0.
      * @param table Table to apply this change to. Must not be {@code null}.
      * @return Table with the change applied. Is never {@code null}.
      * @throws IncompatibleTableException If the following conditions are not
      * met by the given table:
      * <ul>
      * <li>The table must have a seat with the seat number of this change.</li>
      * <li>The player in the seat matches the one of this change.</li>
      * <li>The stake of the seat is equal to the stake defined in this change.
      * </li>
      * <li>The seat has no hole or public cards.</li>
      * </ul>
      */
     @Override
     public Table apply(final Table table) {
         if (!table.isSealed()) {
             throw new IllegalArgumentException("Table is not sealed.");
         }
         if (getSeatNr() >= table.getSeatCount()) {
             throw new IncompatibleTableException("The table does not have a "
                     + "seat " + getSeatNr() + ".");
         }
         Seat seat = table.getSeat(this.seatNr);
         if (seat.getPlayer() != this.player) {
             throw new IncompatibleTableException("The player " + this.player
                     + " is not sitting on seat " + this.seatNr + ".");
         }
         if (seat.getStake() != this.stake) {
             throw new IncompatibleTableException("The seat contains "
                     + seat.getStake() + " chips, cannot leave with "
                     + this.stake + ".");
         }
         if (!seat.getHoleCards().isEmpty()
                 || !seat.getVisibleCards().isEmpty()) {
             throw new IncompatibleTableException("On the seat " + this.seatNr
                     + " are cards.");
         }
         Table after = new Table(table);
 
         Seat afterSeat = after.getSeat(this.seatNr);
         afterSeat.setPlayer(null);
         afterSeat.setStake(0);
 
         after.seal();
         return after;
     }
 
     /**
      * Gets the player that is leaving.
      * @return The player that is leaving. Is never {@code null}.
      */
     public Player getPlayer() {
         return player;
     }
 
     /**
      * Gets the seat number from which the player is leaving.
      * @return The seat number on which the player is leaving. Is
      * non-negative.
      */
     public int getSeatNr() {
         return seatNr;
     }
 
     /**
      * Gets the chips the player takes from to the table.
      * @return The chips the player takes from the table. Is non-negative.
      */
     public int getStake() {
         return stake;
     }
 }
