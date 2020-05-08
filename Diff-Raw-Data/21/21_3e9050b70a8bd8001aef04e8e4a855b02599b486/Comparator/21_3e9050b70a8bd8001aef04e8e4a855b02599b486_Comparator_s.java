 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.annotations;
 
 public enum Comparator {
     EQUAL,
     NOT_EQUAL,
     EQUAL_NULLABLE,
     NOT_EQUAL_NULLABLE,
     SMALLER,
     LARGER,
     SMALL_AS,
     LARGER_AS,
     IN,
     NOT_IN,
     LIKE,
     NOT_LIKE,
     LIKE_INSENSITIVE,
     NOT_LIKE_INSENSITIVE,
     START_WITH,
     NOT_START_WITH,
     END_WITH,
     NOT_END_WITH,
     START_WITH_INSENSITIVE,
     NOT_START_WITH_INSENSITIVE,
     END_WITH_INSENSITIVE,
     NOT_END_WITH_INSENSITIVE,
     CONTAINS,
     NOT_CONTAINS,
     CONTAINS_INSENSITIVE,
     NOT_CONTAINS_INSENSITIVE;
 }
