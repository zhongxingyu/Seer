 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.utils.collect;
 
 import se.toxbee.sleepfighter.utils.model.IdProvider;
 
 /**
 * {@link IdFallbackOrdering} further simplifies the creation of {@link FieldOrdering}s by<br/>
  * falling back to {@link IdProvider#getId()} when {@link #compare(IdProvider, IdProvider)} ties.
  *
  * @param <T> The type being compared extending {@link IdProvider}.
  * @param <C> A {@link Comparable} that is the result of {@link #fieldToComparable(Object)}.
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Dec 14, 2013
  */
 public abstract class IdFallbackOrdering<T extends IdProvider, C extends Comparable<? super C>> extends FieldOrdering<T, C> {
 	@Override
 	public int compare( T lhs, T rhs ) {
 		int r = super.compare( lhs, rhs );
 		return r == 0 ? lhs.getId() - rhs.getId() : r;
 	}
 }
