 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol.eventhandler;
 
 /**
  * Event for a specific pillar.
  */
 public class PillarOperationEvent extends AbstractOperationEvent<String> {
     /** @see #getState() */
     private final String pillarID;
 
     /**
      * Constructor with exception information
      * @param type The event type
      * @param info Free text description of the event
      * @param pillarID The ID of the pillar this event relates to
      */
     public PillarOperationEvent(OperationEventType type, String info, String pillarID) {
         super(type, info);
         this.pillarID = pillarID;
     }
 
     /**
      * Returns the ID of the pillar this event relates to.
      */
     @Override
     public String getState() {
         return pillarID;
     }
 }
