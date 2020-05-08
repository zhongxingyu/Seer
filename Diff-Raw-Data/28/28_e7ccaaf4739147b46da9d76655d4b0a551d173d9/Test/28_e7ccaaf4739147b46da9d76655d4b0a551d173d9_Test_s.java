 
 
 import sim.monitor.Builder;
 import sim.monitor.Monitor;
 import sim.monitor.timing.TimeUnit;
 
 /**
  * 
  */
 
 /**
  * @author valer
  *
  */
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws InterruptedException {
 
 		Thread.sleep(2000);
 
 		/*
 		 * Monitor monitor =
 		 * MonitorBuilder.useCategory("sim.monitoring:type=Testing")
 		 * .setPickoutInterval(TimeUnit, int) .addDefaultStatistic(Sum)
 		 * //.addDefaultRateStatistic(Sum, TimeUnit, int) . .build(name,
 		 * description);
 		 */
 
 		/*
 		Builder.Monitor("Counter")
 			.description("This is a counter")
 			.tags()
 				.add("Backend")
 				.add("Ordering")
 			.transformers()
 				.addDelta()
 				.add(Class<? extends Transformer>)
 			.statistics()
 				.addCount()
 				.addAverage("Average")
 					.description("The average")
 				.add(Class)
 			.build();
 
 		T instance = Builder.tag("sadsad").tag("sasda");
 
 		c.hit();
 		 */
 
 		Monitor counter = Builder.Monitor("Counter test")
 				.description("counter desc").tags().add("testing").filters()
 				.rates().addCount().build();
 
 		counter.hit();
 		Thread.sleep(20);
 		counter.hit();
 
 		Monitor mlv = Builder.Monitor("Value Long Test")
 				.description("long value test descr").tags().add("testing")
 				.filters()
				// .addDelta()
 				.rates().addAverage()
 				.add(TimeUnit.Second, 1).description("Rate 1 sec.")
 				.publishAggregate().build();
 		//mlv.addRateStatistic(Type.sum, TimeUnit.Second, 1, "sum rate", "desc sum rate");
 		// mlv.setRateStatistic(Type.sum, TimeUnit.Second, 1);
 		//mlv.countRate(TimeUnit.Second, 1);
 		//mlv.min();
 		//mlv.max();
 		mlv.hit(new Long(2));
 		// Thread.sleep(1000);
 		mlv.hit(new Long(8));
 		Thread.sleep(1001);
 		mlv.hit(new Long(30));
 		// Thread.sleep(1000);
 		mlv.hit(new Long(5));
 
 		Thread.sleep(1000);
 
 		/*
 		MonitorCounter counter = new MonitorCounter(domain, "Counter test", "counter desc");
 		counter.addRate(new TimePeriod(TimeUnit.Second, 5), "Rate test", "rate description");
 		counter.hit();
 		try {
 			Thread.sleep(20);
 		} catch (InterruptedException e) {}
 		counter.hit();
 
 		MonitorDoubleValue mdv = new MonitorDoubleValue(domain, "Double Value Testing", "description");
 		mdv.hit(2.45);
 
 		MonitorLongValue mlv = new MonitorLongValue(domain, "Long Value Testing", "description long value testing");
 		mlv.hit(13);
 
 		MonitorDoubleDelta mdd = new MonitorDoubleDelta(domain, "Double Delta Testing", "descr. double delta testing");
 		mdd.hit(3.5);
 		mdd.hit(4.7);
 		 */
 	}
 
 }
