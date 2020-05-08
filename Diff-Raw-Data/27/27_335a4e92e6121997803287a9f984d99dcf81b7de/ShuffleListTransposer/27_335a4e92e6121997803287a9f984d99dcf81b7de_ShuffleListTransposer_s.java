 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
 * 
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
 * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
 * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
 * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.types.transpose;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.math.JavaRandomNumberSequence;
 import org.jbasics.math.RandomNumberSequence;
 import org.jbasics.pattern.factory.Factory;
 import org.jbasics.pattern.strategy.SubstitutionStrategy;
 import org.jbasics.pattern.transpose.Transposer;
 import org.jbasics.types.factories.ListFactory;
 
 public class ShuffleListTransposer<T> implements SubstitutionStrategy<List<T>, List<? extends T>>, Transposer<List<T>, List<? extends T>> {
 	public static final ShuffleListTransposer<?> IMMUTABLE = new ShuffleListTransposer<Object>(false);
 	public static final ShuffleListTransposer<?> MUTABLE = new ShuffleListTransposer<Object>(true);
 
	private final RandomNumberSequence<? extends Number> randomNumberSequence;
 
 	@SuppressWarnings("unchecked")
 	public static <T> ShuffleListTransposer<T> mutableTransposer() {
 		return (ShuffleListTransposer<T>) ShuffleListTransposer.MUTABLE;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> ShuffleListTransposer<T> immutableTransposer() {
 		return (ShuffleListTransposer<T>) ShuffleListTransposer.IMMUTABLE;
 	}
 
 	private final boolean mutable;
 	private final Factory<List<T>> listFactory;
 
 	public ShuffleListTransposer() {
 		this(null, false, ListFactory.<T> randomAccessListFactory());
 	}
 
	public ShuffleListTransposer(final RandomNumberSequence<Integer> randomNumberSequence) {
 		this(randomNumberSequence, false, ListFactory.<T> randomAccessListFactory());
 	}
 
 	public ShuffleListTransposer(final boolean mutable) {
 		this(null, mutable, ListFactory.<T> randomAccessListFactory());
 	}
 
	public ShuffleListTransposer(final RandomNumberSequence<Integer> randomNumberSequence, final boolean mutable) {
 		this(randomNumberSequence, mutable, ListFactory.<T> randomAccessListFactory());
 	}
 
 	public ShuffleListTransposer(final Factory<List<T>> listFactory) {
 		this(null, false, listFactory);
 	}
 
	public ShuffleListTransposer(final RandomNumberSequence<Integer> randomNumberSequence, final Factory<List<T>> listFactory) {
 		this(randomNumberSequence, false, listFactory);
 	}
 
 	public ShuffleListTransposer(final boolean mutable, final Factory<List<T>> listFactory) {
 		this(null, mutable, listFactory);
 	}
 
	public ShuffleListTransposer(final RandomNumberSequence<Integer> randomNumberSequence, final boolean mutable, final Factory<List<T>> listFactory) {
 		this.randomNumberSequence = randomNumberSequence == null ? new JavaRandomNumberSequence() : randomNumberSequence;
 		this.mutable = mutable;
 		this.listFactory = ContractCheck.mustNotBeNull(listFactory, "listFactory"); //$NON-NLS-1$
 	}
 
 	@Override
 	public List<T> transpose(final List<? extends T> input) {
 		if (input == null || input.isEmpty()) {
 			return this.mutable ? this.listFactory.newInstance() : Collections.<T> emptyList();
 		}
 		final List<T> result = this.listFactory.newInstance();
 		final List<T> temp = this.listFactory.newInstance();
 		temp.addAll(input);
 		for (int i = 0; i < input.size(); i++) {
 			result.add(temp.remove((int) Math.max(Math.round(this.randomNumberSequence.nextRandomNumber().doubleValue() * temp.size()),
 					temp.size() - 1)));
 		}
 		return this.mutable ? result : Collections.unmodifiableList(result);
 	}
 
 	@Override
 	public List<T> substitute(final List<? extends T> input) {
 		return transpose(input);
 	}
 
 }
