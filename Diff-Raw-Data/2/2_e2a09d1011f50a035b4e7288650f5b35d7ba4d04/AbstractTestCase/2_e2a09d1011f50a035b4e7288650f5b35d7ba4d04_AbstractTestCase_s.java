 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.duplications.benchmark.perf;
 
 import static org.hamcrest.Matchers.greaterThanOrEqualTo;
 
 import java.io.File;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.duplications.benchmark.Benchmark;
 import org.sonar.duplications.benchmark.BenchmarkResult;
 import org.sonar.duplications.benchmark.BenchmarksDiff;
 import org.sonar.duplications.benchmark.MemoryUtils;
 import org.sonar.duplications.benchmark.NewCpdBenchmark;
 import org.sonar.duplications.benchmark.OldCpdBenchmark;
 import org.sonar.duplications.benchmark.ThreadedNewCpdBenchmark;
 
 public class AbstractTestCase {
 
   protected static int BLOCK_SIZE = 13;
   protected static int WARMUP_ROUNDS = 3;
   protected static int BENCHMARK_ROUNDS = 10;
 
   protected static List<File> files;
   protected static BenchmarksDiff results = new BenchmarksDiff();
 
   protected BenchmarkResult run(Benchmark benchmark) {
    return benchmark.runBenchmark(WARMUP_ROUNDS, BENCHMARK_ROUNDS);
   }
 
   @Before
   public void setUp() {
     MemoryUtils.cleanup();
   }
 
   @After
   public void tearDown() {
     MemoryUtils.cleanup();
   }
 
   @Test
   public void oldCpd() {
     OldCpdBenchmark oldCpd = new OldCpdBenchmark(files);
     results.setReference(run(oldCpd));
     System.out.println("Old CPD matches: " + oldCpd.getCount());
   }
 
   @Test
   public void newCpd() {
     results.add(run(new NewCpdBenchmark(files, 13)));
   }
 
   @Test
   public void newCpdWithTwoThreads() {
     int availableProcessors = Runtime.getRuntime().availableProcessors();
     Assume.assumeThat(availableProcessors, greaterThanOrEqualTo(2));
     results.add(run(new ThreadedNewCpdBenchmark(files, BLOCK_SIZE, 2)));
   }
 
   @Test
   public void newCpdWithFourThreads() {
     int availableProcessors = Runtime.getRuntime().availableProcessors();
     Assume.assumeThat(availableProcessors, greaterThanOrEqualTo(4));
     results.add(run(new ThreadedNewCpdBenchmark(files, BLOCK_SIZE, 4)));
   }
 
   @AfterClass
   public static void after() {
     results.print();
   }
 
 }
