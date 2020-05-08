 /*
  Copyright (c) 2011, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.lang.internal;
 
import java.io.Serializable;
 import java.util.Comparator;
 
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 
 /**
  * @author flbulgarelli
  * 
  */
public class NaturalComparator<A extends Comparable<A>> implements Comparator<A>, Serializable {

	private static final long serialVersionUID = 2687644878633769309L;
 
 	public int compare(A o1, A o2) {
 		return o1.compareTo(o2);
 	}
 
 	/**
 	 * Answers a {@link NaturalComparator}
 	 * 
 	 * @param <A>
 	 * @return a natural comparator
 	 */
 	@NonNull
 	@Constant
 	public static <A extends Comparable<A>> Comparator<A> natural() {
 		return new NaturalComparator();
 	}
 
 }
