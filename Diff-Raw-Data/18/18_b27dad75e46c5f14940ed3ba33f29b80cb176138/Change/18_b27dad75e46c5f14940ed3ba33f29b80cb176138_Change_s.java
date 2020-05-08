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
 
 import org.smurn.pokerutils.Table;
 
 /**
  * Describes one or more modifications of a table.
  * <h3>Granularity</h3>
  * <p>
  * When designing implementations of this interface there is always a choice of
  * granularity. One can design for many small changes or one big change.
  * </p><p>
  * A change should never 'jump' over an action of a player. If the player makes
  * a desision then there should not be a change that performs modifications
  * that happend before and after that decision.
  * </p><p>
  * If there is a potential waiting period between two modifications, they should
  * be separated into individiual changes.
  * </p>
  * <h3>Information content</h3>
  * <p>
  * Each change must at least contain all the information required to apply it.
  * Otherwise the {@link #apply(org.smurn.pokerutils.Table)} method cannot
  * be implemented.
  * </p><p>
  * Furthermore it should contain the information required to describe the
  * 'relevant' aspects of the change. Even if this information could be derived
  * from the input table. An example is {@link GetUpChange} which includes
  * the player that left, even if that could easily received from the table.
  * But for log messages knowing player who left is very relevant. Without it
 * the log function would require the table in addition to the change. The 
  * goal is to have self-contained changes.
  * </p>
  */
 public interface Change {
 
     /**
      * Applies this change to a table.
      * @param table Table to apply this change to. Must not be {@code null}.
      * @return Table with the change applied. Is never {@code null}.
      * @throws IncompatibleTableException If this change cannot be applied
      * to the given table.
      */
     Table apply(Table table);
 
 }
