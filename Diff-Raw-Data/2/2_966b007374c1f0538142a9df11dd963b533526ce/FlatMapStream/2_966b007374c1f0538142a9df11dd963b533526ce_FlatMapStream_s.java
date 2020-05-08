 package net.sf.staccato.commons.collections.stream.impl.internal;
 
 import java.util.Iterator;
 
 import net.sf.staccato.commons.collections.iterable.internal.AbstractUnmodifiableIterator;
 import net.sf.staccato.commons.collections.stream.AbstractStream;
 import net.sf.staccato.commons.collections.stream.Stream;
 import net.sf.staccato.commons.lang.Applicable;
 
 /**
  * @author flbulgarelli
  * 
  * @param <B>
  * @param <I>
  */
 public final class FlatMapStream<A, B> extends AbstractStream<B> {
 	private final Stream<A> stream;
 	private final Applicable<? super A, ? extends Iterable<? extends B>> function;
 
 	/**
 	 * Creates a new {@link FlatMapStream}
 	 */
 	public FlatMapStream(Stream<A> stream,
 		Applicable<? super A, ? extends Iterable<? extends B>> function) {
 		this.stream = stream;
 		this.function = function;
 	}
 
 	public Iterator<B> iterator() {
 
 		final Iterator<A> iter = stream.iterator();
 		return new AbstractUnmodifiableIterator<B>() {
 			private Iterator<? extends B> subIter;
 
 			public boolean hasNext() {
 				if (subIter != null && subIter.hasNext())
 					return true;
				if (iter.hasNext()) {
 					subIter = function.apply(iter.next()).iterator();
 					if (subIter.hasNext())
 						return true;
 				}
 				return false;
 			}
 
 			public B next() {
 				return subIter.next();
 			}
 		};
 	}
 }
