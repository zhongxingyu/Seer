 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.domain;
 
 import java.util.EnumSet;
 import java.util.Set;
 
 /**
  * Enum representing the status of a {@link Reservation}.
 *
 *  /---------\      /--------\      /---------\      /---------\      /-------\      /---------\
 * | Requested | -> | Reserved | -> | Scheduled | -> | Preparing | -> | Running | -> | Succeeded |
 *  \---------/      \--------/      \---------/      \---------/      \-------/      \---------/
 *      |    |                         ^    |                |            |
 *      |     \----(auto provision)---/     |                |            |
 *      |                                   v                 \           v
 *      |                             /---------\              \        /------\
 *      | (request failed)           | Cancelled |              \----->| Failed |
 *      v                             \---------/                       \------/
 *    /------------\
 *   | Not Accepted |
 *    \------------/
  */
 public enum ReservationStatus {
 
   REQUESTED, RESERVED, SCHEDULED, PREPARING, RUNNING, SUCCEEDED, CANCELLED, FAILED, NOT_ACCEPTED;
 
   /**
    * All states which are allowed to transition to an other state. All otherf
    * states will automatically be regarded as endStates.
    */
   public static final Set<ReservationStatus> TRANSITION_STATES = EnumSet.of(REQUESTED, RESERVED, SCHEDULED, RUNNING,
       PREPARING);
 
   /**
    * All states which are considered as error states.
    */
   public static final Set<ReservationStatus> ERROR_STATES = EnumSet.of(FAILED, NOT_ACCEPTED);
 
   public static final ReservationStatus[] TRANSITION_STATES_AS_ARRAY = TRANSITION_STATES
       .toArray(new ReservationStatus[TRANSITION_STATES.size()]);
 
   /**
    * @return true if the reservationStatus is an endState, meaning no further
    *         state transitions are allowed. Returns false otherwise.
    */
   public boolean isEndState() {
     return !isTransitionState();
   }
 
   /**
    * @return true if the reservationStatus is an transitionState, meaning
    *         further state transitions are allowed. Returns false otherwise.
    */
   public boolean isTransitionState() {
     return TRANSITION_STATES.contains(this);
   }
 
   /**
    * @return true if a Reservation is allowed to be delete, only based on its
    *         state.
   *
    */
   public boolean isDeleteAllowed() {
     return isTransitionState();
   }
 }
