 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 
 
 public class Utils {
 
 	static class Var {
 		public String name = "";
 		public String value = "";
 	}
 
 	static class Ref<T> {
 		T value;
 		Ref(T initial) { value = initial; }
 	}
 	
 	static Iterable<String> tokenizeString(final String str, String delim) {
 		final Set<Character> delimChars = new HashSet<Character>(collFromIter(iterableString(delim)));
 		return new Iterable<String>() {
 			public Iterator<String> iterator() {
 				return new Iterator<String>() {
 					int pos = 0;
 					public boolean hasNext() { return pos < str.length(); }
 					public String next() {
 						String ret = "";
 						for(; pos < str.length(); ++pos) {
 							char c = str.charAt(pos);
 							if(delimChars.contains(c)) {
 								if(ret.isEmpty()) {
 									ret += c;
 									pos++;
 								}
 								return ret;
 							}
 							ret += c;
 						}
 						return ret;
 					}
 					public void remove() { throw new UnsupportedOperationException(); }
 				};
 			}
 		};
 	}
 	
 	static Iterable<String> tokenizeString(String str) {
 		return tokenizeString(str, " \t\n\r\f");
 	}
 	
 	static <T> T randomChoiceFrom(List<T> list, Random r) {
 		int i = r.nextInt(list.size());
 		return list.get(i);
 	}
 	
 	static interface Size {
 	    int getWidth();
 	    int getHeight();
 	}
 		
 	static abstract class LightCollection<E> implements Collection<E> {
 		public boolean isEmpty() { return !iterator().hasNext(); }
 		public int size() {
 			int c = 0;
 			for(Iterator<E> i = iterator(); i.hasNext(); ++c) i.next();
 			return c;
 		}
 		public Object[] toArray() { return toArray(new Object[] {}); }
 		public <T> T[] toArray(T[] array) { 
 		    int size = size();
 		    if (array.length < size) { 
 		        array = newArray(classOf(array), size);
 		    } else if (array.length > size) {
 		        array[size] = null;
 		    }
 
 		    int i = 0;
 		    for (E e : this) {
 		        array[i] = classOf(array).cast(e);
 		        i++;
 		    }
 		    return array;
 		} 
 		public boolean add(E e) { throw new UnsupportedOperationException(); }
 		public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
 		public void clear() { throw new UnsupportedOperationException(); }
 		public boolean contains(Object o) {
 			for(Iterator<E> i = iterator(); i.hasNext(); )
 				if(i.next().equals(o)) return true;
 			return false;
 		}
 		public boolean containsAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		public boolean remove(Object o) { throw new UnsupportedOperationException(); }
 		public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
 		@Override public String toString() { return "[" + concat(this, ", ") + "]"; }
 	}
 	
 	@SuppressWarnings("unchecked") static <T> Class<? extends T> classOf(T obj) {
 		return (Class<? extends T>) obj.getClass();
 	}
 
 	@SuppressWarnings("unchecked") static <T> Class<? extends T> classOf(T[] array) {
 		return (Class<? extends T>) array.getClass().getComponentType();
 	}
 
 	@SuppressWarnings("unchecked") static <T> T[] newArray(Class<T> clazz, int size) {
 		return (T[]) Array.newInstance(clazz, size);
 	}	
 	
 	static <E> LightCollection<E> collFromIter(final Iterable<E> iter) {
 		return new LightCollection<E>() {
 			public Iterator<E> iterator() {
 				return iter.iterator();
 			}
 		};
 	}
 	
 	static abstract class LightSet<E> extends LightCollection<E> implements Set<E> {}
 	
 	static <E> Collection<E> extendedCollectionView(final Collection<E> base, final Collection<E> extension) {
 		return new Utils.LightCollection<E>() {
 			@Override public boolean add(E e) { return extension.add(e); }
 			@Override public boolean addAll(Collection<? extends E> c) { return extension.addAll(c); }
 
 			public boolean isEmpty() { return base.isEmpty() && extension.isEmpty(); }
 			public int size() { return base.size() + extension.size(); }
 
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					Iterator<E> baseIterator = base.iterator();
 					Iterator<E> extendedIterator = extension.iterator();
 					boolean lastElementWasFromBase = true;
 					
 					public boolean hasNext() {
 						if(baseIterator.hasNext()) return true;
 						return extendedIterator.hasNext();
 					}
 
 					public E next() {
 						if(baseIterator.hasNext()) { lastElementWasFromBase = true; return baseIterator.next(); }
 						if(extendedIterator.hasNext()) { lastElementWasFromBase = false; return extendedIterator.next(); }
 						throw new NoSuchElementException();
 					}
 
 					public void remove() {
 						if(lastElementWasFromBase) baseIterator.remove();
 						else extendedIterator.remove();
 					}
 				};
 			}
 			
 			// Note that we could implement some more of Collection<E>. I just did not because I didn't needed it for now.
 		};
 	}
 	
 	static <E> Collection<E> concatCollectionView(final Iterable<? extends Iterable<E>> colls) {
 		return new LightCollection<E>() {
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					Iterator<? extends Iterable<E>> curSetsIter = colls.iterator();
 					Iterable<E> curColl = null;
 					Iterator<E> curIterator = null;
 					
 					{
 						forward();
 					}
 					
 					void forward() {
 						while(curIterator == null || !curIterator.hasNext()) {
 							if(curSetsIter.hasNext()) {
 								curColl = curSetsIter.next();
 								curIterator = curColl.iterator();
 							}
 							else
 								break;
 						}
 					}
 					
 					public boolean hasNext() {
 						return curIterator != null && curIterator.hasNext();
 					}
 					public E next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						E result = curIterator.next();
 						forward();
 						return result;
 					}
 					public void remove() {
 						if(curIterator == null) throw new IllegalStateException();
 						curIterator.remove();
 					}
 				};
 			}
 		};
 	}
 	
 	static <E> Iterable<E> iterableFromArray(final E[] arr) {
 		return new Iterable<E>() {
 			public Iterator<E> iterator() {
 				return new Iterator<E>() {
 					int i = 0;					
 					public boolean hasNext() { return i < arr.length; }
 					public E next() { if(!hasNext()) throw new NoSuchElementException(); return arr[i++]; } 
 					public void remove() { throw new UnsupportedOperationException(); }					
 				};
 			}
 		};
 	}
 
 	static <E> List<E> listFromArgs() {
 		return new LinkedList<E>();
 	}
 
 	static <E> List<E> listFromArgs(E arg1) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		return list;
 	}
 
 	static <E> List<E> listFromArgs(E arg1, E arg2) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		list.add(arg2);
 		return list;
 	}
 
 	static <E> List<E> listFromArgs(E arg1, E arg2, E arg3) {
 		List<E> list = new LinkedList<E>();
 		list.add(arg1);
 		list.add(arg2);
 		list.add(arg3);
 		return list;
 	}
 	
 	static <E> Collection<E> concatCollectionView(Iterable<E> c1, Iterable<E> c2) {		
 		return concatCollectionView(listFromArgs(c1, c2));
 	}
 	
	static <E> Set<E> mergedSetView(final Iterable<? extends Iterable<E>> sets) {
 		return new LightSet<E>() {
 			public Iterator<E> iterator() {
 				// NOTE: Because of unique, this requires O(N) memory.
 				// It is possible to implement this with O(1) memory requirement;
 				// however, this is a bit complicated/annoying and this is enough for now.
 				return unique(concatCollectionView(sets)).iterator();
 			}
 		};
 	}
 	
 	static interface Callback<T> {
 		void run(T obj);
 	}
 	
 	static <T> Callback<T> runnableToCallback(final Runnable r) {
 		return new Callback<T>() {
 			public void run(T obj) { r.run(); }
 		};
 	}
 	
 	static interface Predicate<T> {
 		boolean apply(T obj);
 	}
 	
 	static <T> Iterable<T> filter(final Iterable<T> i, final Predicate<? super T> pred) {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Iterator<T> last = i.iterator(), curr = i.iterator();
 					int lastDistToCurr = 0;
 					T nextObj = null;
 					
 					public boolean hasNext() { return advance(); }
 					public void remove() { last.remove(); }
 					
 					private boolean advance() {
 						if(nextObj != null) return true;
 						while(curr.hasNext()) {
 							lastDistToCurr++;
 							nextObj = curr.next();
 							if(pred.apply(nextObj))
 								return true;
 						}
 						nextObj = null;
 						return false;
 					}
 					
 					public T next() {
 						if(advance()) {
 							T obj = nextObj;
 							nextObj = null;
 							while(lastDistToCurr > 0) { last.next(); --lastDistToCurr; }
 							return obj;
 						}
 						throw new NoSuchElementException();
 					}
 				};
 			}
 		};
 	}
 	
 	static <X, Y extends X> Iterable<Y> filterType(final Iterable<X> i, final Class<Y> clazz) {
 		Iterable<X> iter = filter(i, new Predicate<X>() {
 			public boolean apply(X obj) {
 				return clazz.isInstance(obj);
 			}
 		});
 		return map(iter, new Function<X,Y>() {
 			public Y eval(X obj) {
 				return clazz.cast(obj);
 			}
 		});
 	}
 	
 	static <T> Set<T> filter(final Set<T> i, final Predicate<? super T> pred) {
 		return new LightSet<T>() {
 			public Iterator<T> iterator() {
 				return filter((Iterable<T>) i, pred).iterator();
 			}
 		};
 	}
 	
 	static <T> Iterable<T> filterNotIn(final Iterable<T> i, final Set<T> set) {
 		Predicate<T> filterNotInPred = new Predicate<T>() {
 			public boolean apply(T obj) { return !set.contains(obj); }						
 		};
 		return filter(i, filterNotInPred);
 	}
 	
 	static <T> Set<T> substractedSet(final Set<T> base, final Set<T> substract) {
 		return new LightSet<T>() {
 			public Iterator<T> iterator() {
 				return filterNotIn(base, substract).iterator();
 			}
 		};
 	}
 	
 	static <T> Iterable<T> unique(final Iterable<T> i) {
 		return new Iterable<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Set<T> objectsSoFar = new HashSet<T>();
 					Iterator<T> filteredIter = filterNotIn(i, objectsSoFar).iterator();
 					
 					public boolean hasNext() { return filteredIter.hasNext(); }
 					public T next() {
 						T obj = filteredIter.next();
 						objectsSoFar.add(obj);
 						return obj;
 					}
 					public void remove() { filteredIter.remove(); }
 				};
 			}
 		};
 	}
 	
 	static <T> Collection<T> cuttedFromRight(final Iterable<T> coll, final Predicate<T> stopCondition) {
 		return new LightCollection<T>() {
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					Iterator<T> iter = coll.iterator();
 					Iterator<T> nextIter = coll.iterator();					
 					T nextObj = null;
 					boolean stop = false;					
 					{
 						advanceNextIter();
 					}
 					
 					void advanceNextIter() {
 						if(nextIter.hasNext()) {
 							nextObj = nextIter.next();
 							if(stopCondition.apply(nextObj))
 								stop = true;
 						}
 						else {
 							if(iter.hasNext()) throw new AssertionError("something is wrong");
 							nextObj = null;
 						}
 					}
 					
 					public boolean hasNext() { return iter.hasNext() && !stop; }
 					public T next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						T objToReturn = nextObj;
 						if(iter.next() != objToReturn) throw new AssertionError("something is wrong");
 						advanceNextIter();
 						return objToReturn;
 					}
 					public void remove() { iter.remove(); }
 				};
 			}			
 		};
 	}
 	
 	static <T> List<T> list(Iterable<T> i) {
 		List<T> l = new LinkedList<T>();
 		for(T o : i) l.add(o);
 		return l;
 	}
 	
 	static class Pair <T1,T2> {
 		T1 first;
 		T2 second;
 		public Pair(T1 first, T2 second) {
 			this.first = first;
 			this.second = second;
 		}
 	}
 	
 	static <T> Iterable<Pair<T,T>> allPairs(Iterable<T> coll) { return allPairs(coll, coll, true, true); }
 	static <T> Iterable<Pair<T,T>> allPairs(Iterable<T> coll1, Iterable<T> coll2) { return allPairs(coll1, coll2, false, false); }
 	static <T> Iterable<Pair<T,T>> allPairs(final Iterable<T> coll1, final Iterable<T> coll2, final boolean removeIdentityAndSwappedPairs, final boolean useDeepEqualCheck) {
 		return new Iterable<Pair<T,T>>() {
 			public Iterator<Pair<T,T>> iterator() {
 				return new Iterator<Pair<T,T>>() {
 					Iterator<T> i1 = coll1.iterator();
 					T obj1 = null;
 					Iterator<T> i2 = null;
 					T obj2 = null;
 					{
 						advance();
 					}
 					
 					void advance() {
 						if(obj1 != null && i2.hasNext()) {
 							obj2 = i2.next();
 							return;
 						}
 						
 						if(!i1.hasNext()) {
 							obj1 = null;
 							obj2 = null;
 							return;
 						}
 						
 						obj1 = i1.next();
 						i2 = coll2.iterator();
 						if(removeIdentityAndSwappedPairs) {
 							while(i2.hasNext()) {
 								if(useDeepEqualCheck && i2.next().equals(obj1)) break;
 								if(!useDeepEqualCheck && i2.next() == obj1) break;
 							}
 						}
 						if(i2.hasNext())
 							obj2 = i2.next();
 						else
 							obj2 = null;
 					}
 					
 					public boolean hasNext() { return obj1 != null && obj2 != null; }
 					public Pair<T,T> next() {
 						if(!hasNext()) throw new NoSuchElementException();
 						Pair<T,T> pair = new Pair<T,T>(obj1, obj2);
 						advance();
 						return pair;
 					}
 					public void remove() { throw new UnsupportedOperationException(); }
 				};
 			}
 		};
 	}	
 		
 	static <T1,T2> Iterable<Pair<T1,T2>> zip(final Iterable<T1> i1, final Iterable<T2> i2) {
 		return new Iterable<Pair<T1,T2>>() {
 			public Iterator<Pair<T1, T2>> iterator() {
 				return new Iterator<Pair<T1,T2>>() {
 					Iterator<T1> it1 = i1.iterator();
 					Iterator<T2> it2 = i2.iterator();
 					
 					public boolean hasNext() { return it1.hasNext() && it2.hasNext(); }
 					public Pair<T1, T2> next() { return new Pair<T1,T2>(it1.next(), it2.next()); }
 					public void remove() { it1.remove(); it2.remove(); }
 				};
 			}
 		};
 	}
 	
     static <T> Set<T> minSet(Iterable<? extends T> it, Comparator<? super T> comp) {
     	Iterator<? extends T> i = it.iterator();
     	Set<T> candidates = new HashSet<T>();
 
     	while (i.hasNext()) {
     		T next = i.next();
     		if(candidates.isEmpty())
     			candidates.add(next);
     		else {
     			int c = comp.compare(next, candidates.iterator().next());
     			if(c < 0) {
     				candidates.clear();
     				candidates.add(next);
     			}
     			else if(c == 0)
     				candidates.add(next);
     		}
     	}
     	
     	return candidates;
     }
 
     static <T> int indexInArray(T[] a, T obj) {
     	for(int i = 0; i < a.length; ++i)
     		if(a[i] == obj) return i;
     	return -1;
     }
     
 	static interface Function<X,Y> {
 		Y eval(X obj);
 	}
 
 	static <X> Function<X,X> identifyFunc() {
 		return new Function<X,X>() {
 			public X eval(X obj) { return obj; }
 		};
 	}	
 	
 	static <Y, X extends Y> Function<X,Y> identifyFunc2() {
 		return new Function<X,Y>() {
 			public Y eval(X obj) { return obj; }
 		};
 	}
 	
 	static interface CopyableIterator<X> extends Iterator<X>, Cloneable {}
 	
     static <X,Y> Iterable<Y> map(final Iterable<? extends X> coll, final Function<X,Y> func) {
 		return new Iterable<Y>() {
 			public Iterator<Y> iterator() {
 				return new Iterator<Y>() {
 					Iterator<? extends X> baseIter = coll.iterator();					
 					public boolean hasNext() { return baseIter.hasNext(); }
 					public Y next() { return func.eval(baseIter.next()); }
 					public void remove() { baseIter.remove(); }
 				};
 			}
 		};
     }
 
 	static <X,Y> Collection<Y> map(final Collection<? extends X> coll, final Function<X,Y> func) {
     	return new LightCollection<Y>() {
 			public boolean isEmpty() { return coll.isEmpty(); }
 			public int size() { return coll.size(); }
 			public Iterator<Y> iterator() { return map((Iterable<? extends X>) coll, func).iterator(); }
     	};
     }
     
     static <X> String concat(Iterable<X> coll, String seperator) {
     	String s = ""; Iterator<X> i = coll.iterator();
     	if(i.hasNext()) s += i.next().toString();
     	while(i.hasNext()) {
     		s += seperator;
     		s += i.next().toString();
     	}
     	return s;
     }
 
     static <T> Iterable<T> iterableReverseList(final List<T> l) {
     	return new Iterable<T>() {
     		public Iterator<T> iterator() {
     			return new Iterator<T>() {
     				ListIterator<T> listIter = l.listIterator(l.size());    				
 					public boolean hasNext() { return listIter.hasPrevious(); }
 					public T next() { return listIter.previous(); }
 					public void remove() { listIter.remove(); }    				
 				};
     		}
 		};
     }
     
     static <T extends Comparable<T>> Comparator<Collection<T>> orderOnCollection() {
     	return new Comparator<Collection<T>>() {
     		public int compare(Collection<T> o1, Collection<T> o2) {
     	        Iterator<T> i1 = o1.iterator();
     	        Iterator<T> i2 = o2.iterator();
     	        while(i1.hasNext() && i2.hasNext()) {
     	            int c = i1.next().compareTo(i2.next());
     	            if(c != 0) return c;
     	        }
     	        if(i1.hasNext())
     	            return 1;
     	        else if(i2.hasNext())
     	            return -1;
     	        return 0;
     	    }
 		};
     }
     
 	static class StringIterator implements Iterator<Character> {
 		String str; int pos = 0;
 		StringIterator(String str) { this.str = str; }
 		public boolean hasNext() { return pos < str.length(); }
 		public Character next() { return str.charAt(pos++); }
 		public void remove() { throw new AssertionError("removing in string iterator not supported"); }
 	}
     
 	static class StringReverseIterator implements Iterator<Character> {
 		String str; int pos;
 		StringReverseIterator(String str) { this.str = str; pos = str.length() - 1; }
 		public boolean hasNext() { return pos >= 0; }
 		public Character next() { return str.charAt(pos--); }
 		public void remove() { throw new AssertionError("removing in string iterator not supported"); }
 	}
 
 	static Iterable<Character> iterableString(final String s) {
 		return new Iterable<Character>() {
 			public Iterator<Character> iterator() { return new StringIterator(s); }
 		};
 	}
 
 	static Iterable<Character> iterableStringReversed(final String s) {
 		return new Iterable<Character>() {
 			public Iterator<Character> iterator() { return new StringReverseIterator(s); }
 		};
 	}
 
 	static <T> int equalLen(Iterable<? extends T> c1, Iterable<? extends T> c2) {
 		Iterator<? extends T> i1 = c1.iterator();
 		Iterator<? extends T> i2 = c2.iterator();
 		int len = 0;
 		while(i1.hasNext() && i2.hasNext()) {
 			if(!i1.next().equals(i2.next())) break;
 			len++;
 		}
 		return len;
 	}
 	
 	static int equalStartLen(String s1, String s2) { return equalLen(iterableString(s1), iterableString(s2)); }
 	static int equalEndLen(String s1, String s2) { return equalLen(iterableStringReversed(s1), iterableStringReversed(s2)); }
 	
     static <T> T castOrNull(Object obj, Class<T> clazz) {
         if(clazz.isInstance(obj))
             return clazz.cast(obj);
         return null;
     }
 	
     static int countStackFrames(String functionName) {
     	StackTraceElement[] stack = new Exception().getStackTrace();
     	int count = 0;
     	for(StackTraceElement frame : stack) {
     		if(frame.getMethodName().contains(functionName))
     			count++;
     	}
     	return count;
     }
     
     static String multiplyString(String s, int fac) {
     	String r = "";
     	for(int i = 0; i < fac; ++i) r += s;
     	return r;
     }
     
     static void debugUtilsParsingOpTree(String s) {
 		OperatorTree.debugOperatorTreeDump = true;
 		OperatorTree ot = OTParser.parse(s); 
 		String debugStr = ot.toString();
 		OperatorTree.debugOperatorTreeDump = false;
 		String normalStr = ot.toString();
 		String simplifiedStr = ot.simplify().toString();
 		System.out.println("parsed " + s + " -> " + debugStr + " -> " + normalStr + " -> " + simplifiedStr);
 	}
 
 	static void debugUtilsParsingOpTree() {
 		debugUtilsParsingOpTree("a * b = c");
 		debugUtilsParsingOpTree("a + b * d");
 		debugUtilsParsingOpTree("1 + 2 - 3 - 4 + 5");
 		debugUtilsParsingOpTree("(1 + 2) + (3 + 4) - 5");
 		debugUtilsParsingOpTree("(1 + 2) - (3) - 4");
 		debugUtilsParsingOpTree("(1 + 2) + (3 + 4) (5 + 6)");
 		debugUtilsParsingOpTree("1 + 2 (3)");
 		debugUtilsParsingOpTree("1 + 2 3 / 4 * 5");
 		debugUtilsParsingOpTree("1 = (2 + 3)");
 		debugUtilsParsingOpTree("a + b = c + d");
 		debugUtilsParsingOpTree("(a + b) = c + d");
 		debugUtilsParsingOpTree("a = (b * c) + d");
 		debugUtilsParsingOpTree("a + (b * )");
 		debugUtilsParsingOpTree("a * -b");
 		debugUtilsParsingOpTree("a * --b");
 		debugUtilsParsingOpTree("a * -+-b");
 		debugUtilsParsingOpTree("1 + -2 + 3");
 		debugUtilsParsingOpTree("1 + -(2 + 3) + 4");
 		debugUtilsParsingOpTree("a - (b - c)");
 		debugUtilsParsingOpTree("a + +b");
 	}
     
     
 }
