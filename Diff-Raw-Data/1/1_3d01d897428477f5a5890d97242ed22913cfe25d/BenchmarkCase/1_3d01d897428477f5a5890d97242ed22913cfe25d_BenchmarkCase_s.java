 package ru.tyurin.benchmark;
 
 
 import java.util.*;
 
 public class BenchmarkCase {
 
 	private Set<Benchmark> benchmarks;
 	private Map<Benchmark, List<BenchmarkResult>> results;
 	private Map<Benchmark, Integer> counts;
 	private String name = "";
 
 	/**
 	 * Create Benchmark Case
 	 */
 	public BenchmarkCase() {
 		benchmarks = new HashSet<Benchmark>();
 		results = new HashMap<Benchmark, List<BenchmarkResult>>();
 		counts = new HashMap<Benchmark, Integer>();
 	}
 
 	/**
 	 * Create named benchmark case
 	 *
 	 * @param name benchmark case name
 	 * @see #setName
 	 */
 	public BenchmarkCase(String name) {
 		this();
 		setName(name);
 	}
 
 	/**
 	 * Get benchmark case name
 	 *
 	 * @return case name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set benchmark case name
 	 * <p/>
 	 * If name is null, then name - empty string
 	 *
 	 * @param name case name
 	 */
 	public void setName(String name) {
 		if (name != null) {
 			this.name = name;
 		}
 	}
 
 	/**
 	 * Add benchmark in test case
 	 *
 	 * @param benchmark
 	 * @throws NullPointerException If  benchmark is null
 	 */
 	public void addBenchmark(Benchmark benchmark) {
 		addBenchmark(benchmark, 1);
 	}
 
 	/**
 	 * Add benchmark in test case.
 	 * <p/>
 	 * If benchmark already exist, it wont be added, but count will be changed
 	 *
 	 * @param benchmark
 	 * @param count     Count of starts test
 	 * @throws NullPointerException     If  benchmark is null
 	 * @throws IllegalArgumentException If count is negative
 	 */
 	public void addBenchmark(Benchmark benchmark, int count) {
 		if (benchmark == null) {
 			throw new NullPointerException("Benchmark is null");
 		}
 		if (count < 0) {
 			throw new IllegalArgumentException("Count value cannot be negative");
 		}
 		addBenchmarkTest(benchmark);
 		setCount(benchmark, count);
 	}
 
 	/**
 	 * Set count of benchmark runs
 	 *
 	 * @param benchmark
 	 * @param count     Count of starts test
 	 * @throws NullPointerException     If  benchmark is null
 	 * @throws IllegalArgumentException If count is negative or if benchmark not found
 	 */
 	public void setBenchmarkCount(Benchmark benchmark, int count) {
 		if (benchmark == null) {
 			throw new NullPointerException("Benchmark is null");
 		}
 		if (count < 0) {
 			throw new IllegalArgumentException("Count < 0");
 		}
 		if (!hasBenchmark(benchmark)) {
 			throw new IllegalArgumentException("Benchmark not found");
 		}
 		setCount(benchmark, count);
 	}
 
 	/**
 	 * Remove benchmark from case
 	 *
 	 * @param benchmark
 	 * @return True if test removed, or False if not.
 	 */
 	public boolean removeBenchmark(Benchmark benchmark) {
 		counts.remove(benchmark);
 		return benchmarks.remove(benchmark);
 	}
 
 	/**
 	 * Run test case
 	 * <p/>
 	 * Old result will be cleared
 	 */
 	public void run() {
 		results.clear();
 		for (Benchmark benchmark : benchmarks) {
 			int count = getCount(benchmark);
 			List<BenchmarkResult> res = new ArrayList<BenchmarkResult>();
 			for (int c = 0; c < count; c++) {
 				BenchmarkResult result = benchmark.run();
 				res.add(result);
 			}
 			BenchmarkResult total = getTotal(res, benchmark);
 			BenchmarkResult avg = getAVG(res, benchmark);
 			res.add(total);
 			res.add(avg);
 			results.put(benchmark, res);
 		}
 	}
 
 	/**
 	 * Get result of last test
 	 *
 	 * @return Collection of results
 	 */
 	public Collection<BenchmarkResult> getResults() {
 		Collection<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
 		for (Collection<BenchmarkResult> resultList : this.results.values()) {
 			results.addAll(resultList);
 		}
 		return results;
 	}
 
 	/**
 	 * Get result of last benchmark test
 	 *
 	 * @param benchmark
 	 * @return Collection of results
 	 */
 	public Collection<BenchmarkResult> getResults(Benchmark benchmark) {
 		return this.results.get(benchmark);
 	}
 
 	/**
 	 * Get benchmarks
 	 *
 	 * @return Collection of benchmarks
 	 */
 	public Collection<Benchmark> getBenchmarks() {
 		return benchmarks;
 	}
 
 	/**
 	 * Get summary string
 	 * <p/>
 	 * Return string with all results of last test
 	 *
 	 * @return
 	 */
 	public String getSummary() {
 		StringBuilder builder = new StringBuilder(String.format("Benchmark Case %s%n", getName()));
 		builder.append("\n");
 		for (Benchmark benchmark : benchmarks) {
 			builder.append("\t");
 			builder.append(String.format("Benchmark %s%n", benchmark.getName()));
 			for (BenchmarkResult result : results.get(benchmark)) {
 				builder.append("\t\t");
 				builder.append(String.format("[%s]%n \t\t\t Time: %d \tMemory: %d", result.getName(), result.getTime(), result.getMemory()));
 				builder.append("\n");
 			}
 		}
 		return builder.toString();
 	}
 
 
 	protected int getCount(Benchmark benchmark) {
 		return counts.get(benchmark);
 	}
 
 	protected void setCount(Benchmark benchmark, int count) {
 		counts.put(benchmark, count);
 	}
 
 	protected void addBenchmarkTest(Benchmark benchmark) {
 		if (!hasBenchmark(benchmark))
 			benchmarks.add(benchmark);
 	}
 
 	protected boolean hasBenchmark(Benchmark benchmark) {
 		return benchmarks.contains(benchmark);
 	}
 
 	private BenchmarkResult getTotal(Collection<BenchmarkResult> results, Benchmark benchmark) {
 		long time = 0;
 		long mem = 0;
 		BenchmarkResult res = new BenchmarkResult(benchmark, "total");
 		for (BenchmarkResult result : results) {
 			time += result.getTime();
 			mem += result.getMemory();
 		}
 		res.setTime(time);
 		res.setMemory(mem);
 		return res;
 	}
 
 	private BenchmarkResult getAVG(Collection<BenchmarkResult> results, Benchmark benchmark) {
 		long time = 0;
 		long mem = 0;
 		long count = results.size();
 		BenchmarkResult res = new BenchmarkResult(benchmark, "avg");
 		for (BenchmarkResult result : results) {
 			time += result.getTime();
 			mem += result.getMemory();
 		}
 		if (count > 0) {
 			res.setTime(time / count);
 			res.setMemory(mem / count);
 		}
 		return res;
 	}
 
 }
