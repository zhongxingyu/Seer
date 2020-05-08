 package io.ous.concurrentunit.execution;
 
 import io.ous.concurrentunit.execution.ScheduledExecutionStrategy.FixedMode;
 import io.ous.concurrentunit.testutils.CartesianParameters;
 import io.ous.concurrentunit.testutils.Stopwatch;
 import io.ous.concurrentunit.testutils.Timed;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.mockito.internal.matchers.LessOrEqual;
 
 @RunWith(Parameterized.class)
 public class ScheduledExecutionStrategyUnitTest {
	private static final long ERROR_BAR_PER_TASK = 5;
 	private final int executionCount;
 	private long delay;
 	private long period;
 	private FixedMode mode;
 	
 	private ScheduledExecutionStrategy strategy;
 	
 	@Parameters(name="{index}: Count={0}, Delay={1}, Period={2}, Mode={3}")
 	public static Collection<Object[]> getParameters() {
 		Integer[] sizes = new Integer[] {0,1,15,5};
 		Long[] delays = new Long[] {0l,50l,130l};
 		Long[] periods = new Long[]{30l,100l};
 		FixedMode[] modes = FixedMode.values();
 		return new CartesianParameters().add(sizes).add(delays).add(periods).add(modes).toParameters();
 	}
 	
 	public ScheduledExecutionStrategyUnitTest(int count, long delay, long period, FixedMode mode) {
 		executionCount = count;
 		this.delay = delay;
 		this.period = period;
 		this.mode = mode;
 	}
 	
 	@Before
 	public void init() {
 		strategy = new ScheduledExecutionStrategy(executionCount, delay, period, mode);
 	}
 	
 	@Test
 	public void test() throws Exception {
 		System.out.println(strategy);
 		Assert.assertEquals(delay, strategy.getDelay());
 		Assert.assertEquals(executionCount, strategy.getCount());
 		Assert.assertEquals(period, strategy.getPeriod());
 		Assert.assertEquals(mode, strategy.getMode());
 		
 		ExecutorService executor = Executors.newCachedThreadPool();// Mockito.mock(ExecutorService.class);
 //		Callable<?> call = Mockito.mock(Callable.class);
 		final AtomicInteger counter = new AtomicInteger(-1);
 		final Stopwatch watch = new Stopwatch();
 		Future<List<Future<Integer>>> run = strategy.run(executor, new Callable<Integer>() {
 			public Integer call() throws Exception {
 				watch.mark();
 				return counter.incrementAndGet();
 			}
 		});
 		List<Future<Integer>> list = run.get();
 		watch.end();
 		
 		
 		List<Timed> laps = watch.getLaps();
 		for(int i =0; i < list.size(); ++i) {
 			Assert.assertEquals(Integer.valueOf(i), list.get(i).get()); 
 			long expectedMark = strategy.getDelay()+(i*strategy.getPeriod());
 			Assert.assertThat(laps.get(i).getEnd()-watch.getStart(), new LessOrEqual<Long>(expectedMark+getErrorBar(i)));
 		}
 		
 		long elapsed = watch.getElapsed();
 		long expectedElapsed = strategy.getDelay()+(strategy.getCount()*strategy.getPeriod());
 		Assert.assertThat(elapsed, new LessOrEqual<Long>(expectedElapsed+getErrorBar()));
 	}
 	long getErrorBar() {
 		return getErrorBar(executionCount);
 	}
 	long getErrorBar(int tasks) {
 		return ERROR_BAR_PER_TASK*(tasks+1);
 	}
 }
