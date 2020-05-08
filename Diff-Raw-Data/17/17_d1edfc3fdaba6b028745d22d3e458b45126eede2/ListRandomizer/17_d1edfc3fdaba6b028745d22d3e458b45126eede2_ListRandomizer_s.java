 package com.narmnevis.range.random;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.narmnevis.range.Randomizer;
 import com.narmnevis.range.RangeContext;
 
 public class ListRandomizer implements Randomizer {
 
 	private final AtomicInteger index = new AtomicInteger(-1);
	protected List<Double> randoms;
 	private Double sum;
 
 	public ListRandomizer(List<Double> randoms) {
 		this.randoms = randoms;
 	}
 
 	protected ListRandomizer() {
 	}
 
 	@Override
 	public Double next(RangeContext context) {
 		init(context);
 		if (index.incrementAndGet() == randoms.size()) {
 			index.getAndSet(0);
 		}
 		return randoms.get(index.get());
 	}
 
 	@Override
 	public List<Double> values(RangeContext context) {
 		init(context);
 		return Collections.unmodifiableList(randoms);
 	}
 
 	@Override
 	public Double sum(RangeContext context) {
 		if (sum != null) {
 			return sum;
 		}
 		for (Double d : randoms) {
 			sum += d;
 		}
 		return sum;
 	}
 
 	/**
 	 * @param context
 	 */
 	protected void init(RangeContext context) {
 	}
 
 	/**
 	 * @param randoms
 	 */
 	protected void setRandoms(List<Double> randoms) {
 		this.randoms = randoms;
 	}
 
 }
