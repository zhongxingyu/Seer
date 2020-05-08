 /**
  * package-info.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * Provides some basic implementations of the evolutionary algorithms interface
  * defined in the <code>jmona</code> package.
  * 
  * Among other classes, this package provides:
  * <ul>
  * <li>{@link jmona.impl.AbstractEvolutionContext}, a base class for
  * {@link jmona.EvolutionContext} implementations</li>
 * <li>{@link jmona.impl.DefaultCompletionCriteria}, a default
  * {@link jmona.CompletionCondition} implementation which checks for maximum
  * fitness and for maximum number of generations</li>
  * <li>{@link jmona.impl.DefaultPopulation}, a <code>Vector</code>-based default
  * implementation of a {@link jmona.Population}</li>
  * <li>{@link jmona.impl.PeriodicPostProcessor}, a base class for
  * {@link jmona.PostProcessor} implementations which execute periodically.</li>
  * </ul>
  * 
  * @author jfinkels
  */
 package jmona.impl;
 
