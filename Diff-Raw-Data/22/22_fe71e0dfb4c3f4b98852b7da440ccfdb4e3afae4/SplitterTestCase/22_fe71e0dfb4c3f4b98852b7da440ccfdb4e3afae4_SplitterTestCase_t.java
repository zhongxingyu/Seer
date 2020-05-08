 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.core;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import junit.framework.TestCase;
 
 import org.dada.core.Splitter.StatelessStrategy;
 
 public class SplitterTestCase extends TestCase {
 
 	public void testKeyOnImmutableAttribute() {
 		View<Datum<Integer>> view = new View<Datum<Integer>>() {
 			
 			@Override
 			public void update(Collection<Update<Datum<Integer>>> insertions,
 					Collection<Update<Datum<Integer>>> alterations,
 					Collection<Update<Datum<Integer>>> deletions) {
 			}
 		};
 		
 		final Collection<View<Datum<Integer>>> views =Collections.singleton(view);
 		
 		StatelessStrategy<Integer, Datum<Integer>> idStrategy = new StatelessStrategy<Integer, Datum<Integer>>() {
 
 			@Override
 			public boolean getMutable() {
 				return false;
 			}
 
 			@Override
			public Collection<Integer> getKeys(Datum<Integer> value) {
				return Collections.singleton(value.getId());
 			}
 
 			@Override
 			public Collection<View<Datum<Integer>>> getViews(Integer key) {
 				return views;
 			}
 		};
 
 		View<Datum<Integer>> Splitter = new Splitter<Integer, Datum<Integer>>(idStrategy);
 
 		Collection<Update<Datum<Integer>>> nil = Collections.emptyList();
 
 		DatumImpl<Integer> v0 = new DatumImpl<Integer>(0, 0) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> insertion = new Update<Datum<Integer>>(null, v0);
 		Collection<Update<Datum<Integer>>> insertions = Collections.singleton(insertion);
 		Splitter.update(insertions, nil, nil);
 
 		DatumImpl<Integer> v1 = new DatumImpl<Integer>(0, 1) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> update = new Update<Datum<Integer>>(v0, v1);
 		Collection<Update<Datum<Integer>>> updates = Collections.singleton(update);
 		Splitter.update(nil, updates, nil);
 
 		DatumImpl<Integer> v2 = new DatumImpl<Integer>(0, 2) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> deletion = new Update<Datum<Integer>>(v1, v2);
 		Collection<Update<Datum<Integer>>> deletions = Collections.singleton(deletion);
 		Splitter.update(nil, nil, deletions);
 	}
 
 	public void testKeyOnmutableAttribute() {
 		View<Datum<Integer>> view = new View<Datum<Integer>>() {
 			
 			@Override
 			public void update(Collection<Update<Datum<Integer>>> insertions,
 					Collection<Update<Datum<Integer>>> alterations,
 					Collection<Update<Datum<Integer>>> deletions) {
 			}
 		};
 		
 		final Collection<View<Datum<Integer>>> views =Collections.singleton(view);
 		
 		StatelessStrategy<Integer, Datum<Integer>> idStrategy = new StatelessStrategy<Integer, Datum<Integer>>() {
 
 			@Override
 			public boolean getMutable() {
 				return true;
 			}
 
 			@Override
			public Collection<Integer> getKeys(Datum<Integer> value) {
				return Collections.singleton(value.getVersion());
 			}
 
 			@Override
 			public Collection<View<Datum<Integer>>> getViews(Integer key) {
 				return views;
 			}
 		};
 
 		View<Datum<Integer>> Splitter = new Splitter<Integer,  Datum<Integer>>(idStrategy);
 
 		Collection<Update<Datum<Integer>>> nil = Collections.emptyList();
 
 		DatumImpl<Integer> v0 = new DatumImpl<Integer>(0, 0) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> insertion = new Update<Datum<Integer>>(null, v0);
 		Collection<Update<Datum<Integer>>> insertions = Collections.singleton(insertion);
 		Splitter.update(insertions, nil, nil);
 
 		DatumImpl<Integer> v1 = new DatumImpl<Integer>(0, 1) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> update = new Update<Datum<Integer>>(v0, v1);
 		Collection<Update<Datum<Integer>>> updates = Collections.singleton(update);
 		Splitter.update(nil, updates, nil);
 		
 		{
 		DatumImpl<Integer> v11 = new DatumImpl<Integer>(1, 1) {
 			@Override
 			public int compareTo(Datum<Integer> o) {
 				throw new UnsupportedOperationException("NYI");
 			}
 		};
 		Update<Datum<Integer>> update1 = new Update<Datum<Integer>>(v0, v11);
 		Collection<Update<Datum<Integer>>> updates1 = Collections.singleton(update1);
 		Splitter.update(nil, updates1, nil);
 		}
 	}
 }
