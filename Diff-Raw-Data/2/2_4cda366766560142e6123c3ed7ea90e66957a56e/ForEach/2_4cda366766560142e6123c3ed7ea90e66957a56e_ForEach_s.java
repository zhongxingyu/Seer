 package jdk8.performance;
 
 import com.google.caliper.Benchmark;
 import com.google.caliper.Param;
 import com.google.caliper.config.InvalidConfigurationException;
 import com.google.caliper.runner.CaliperMain;
 import com.google.caliper.runner.InvalidBenchmarkException;
 import com.google.caliper.util.InvalidCommandException;
 import com.google.common.collect.ObjectArrays;
 import java.io.PrintWriter;
 import java.security.SecureRandom;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.function.IntFunction;
 import static java.util.stream.Collectors.toList;
 import java.util.stream.IntStream;
 
 public class ForEach {
 
     public static class ForEachBenchmark extends Benchmark {
 
         @Param
         int size;
 
         private List<Integer> list;
 
         @Override
         protected void setUp() throws Exception {
             list = IntStream.range(0, size).parallel().boxed().collect(toList());
         }
 
         public void timeForEachClassic(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicInteger atomicInteger = new AtomicInteger(0);
                 for (Integer integer : list) {
                     atomicInteger.accumulateAndGet(integer, Integer::sum);
                 }
             }
         }
 
         public void timeForEachStream(int reps) {
             for (int i = 0; i < reps; i++) {
 
                 AtomicInteger atomicInteger = new AtomicInteger(0);
                 list.stream().forEach((integer) -> {
                     atomicInteger.accumulateAndGet(integer, Integer::sum);
                 });
             }
         }
 
         public void timeForEachArrayList(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicInteger atomicInteger = new AtomicInteger(0);
                 list.forEach((integer) -> {
                     atomicInteger.accumulateAndGet(integer, Integer::sum);
                 });
             }
         }
 
         public void timeForEachParallelStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicInteger atomicInteger = new AtomicInteger(0);
                 list.parallelStream().forEach((integer) -> {
                     atomicInteger.accumulateAndGet(integer, Integer::sum);
                 });
             }
         }
     }
 
     public static class ConditionalForEachBenchmark extends Benchmark {
 
         @Param
         int size;
 
         private List<Integer> list;
 
         @Override
         protected void setUp() throws Exception {
             list = IntStream.range(0, size).parallel().boxed().collect(toList());
         }
 
         public void timeForEachClassic(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                 for (Integer integer : list) {
                     final boolean par = integer % 2 == 0;
                     if (!(par && atomicBoolean.get())) {
                         atomicBoolean.set(!atomicBoolean.get());
                     }
                 }
             }
         }
 
         public void timeForEachStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                 list.stream().forEach((integer) -> {
                     final boolean par = integer % 2 == 0;
                     if (!(par && atomicBoolean.get())) {
                         atomicBoolean.set(!atomicBoolean.get());
                     }
                 });
             }
         }
 
         public void timeForEachArrayList(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                 list.forEach((integer) -> {
                     final boolean par = integer % 2 == 0;
                     if (!(par && atomicBoolean.get())) {
                         atomicBoolean.set(!atomicBoolean.get());
                     }
                 });
             }
         }
 
         public void timeForEachParallelStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                 list.parallelStream().forEach((integer) -> {
                     final boolean par = integer % 2 == 0;
                     if (!(par && atomicBoolean.get())) {
                         atomicBoolean.set(!atomicBoolean.get());
                     }
                 });
             }
         }
     }
 
     public static class SecureRandomForEachBenchmark extends Benchmark {
 
         @Param
         int size;
 
         private List<Integer> list;
 
         @Override
         protected void setUp() throws Exception {
             list = IntStream.range(0, size).parallel().boxed().collect(toList());
         }
 
         public void timeForEachClassic(int reps) {
             for (int i = 0; i < reps; i++) {
                 for (Integer integer : list) {
                     SecureRandom secureRandom = new SecureRandom(new byte[integer]);
                 }
             }
         }
 
         public void timeForEachStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.stream().forEach((integer) -> {
                     SecureRandom secureRandom = new SecureRandom(new byte[integer]);
                 });
             }
         }
 
         public void timeForEachArrayList(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.forEach((integer) -> {
                     SecureRandom secureRandom = new SecureRandom(new byte[integer]);
                 });
             }
         }
 
         public void timeForEachParallelStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.parallelStream().forEach((integer) -> {
                     SecureRandom secureRandom = new SecureRandom(new byte[integer]);
                 });
             }
         }
     }
 
     public static class FatorialForEachBenchmark extends Benchmark {
 
         @Param
         int size;
 
         private List<Integer> list;
 
         private final IntFunction<Integer> factorial = i -> {
            return i == 0 ? 1 : i * factorial.apply(i - 1);
         };
 
         @Override
         protected void setUp() throws Exception {
             list = IntStream.range(0, size).parallel().boxed().collect(toList());
         }
 
         public void timeForEachClassic(int reps) {
             for (int i = 0; i < reps; i++) {
                 for (Integer integer : list) {
                     factorial.apply(integer);
                 }
             }
         }
 
         public void timeForEachStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.stream().forEach((integer) -> {
                     factorial.apply(integer);
                 });
             }
         }
 
         public void timeForEachArrayList(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.forEach((integer) -> {
                     factorial.apply(integer);
                 });
             }
         }
 
         public void timeForEachParallelStream(int reps) {
             for (int i = 0; i < reps; i++) {
                 list.parallelStream().forEach((integer) -> {
                     factorial.apply(integer);
                 });
             }
         }
     }
 
     public static void main(String[] args) throws InvalidCommandException, InvalidBenchmarkException, InvalidConfigurationException {
         runForEach();
         runFatorialForEach();
     }
 
     private static void runForEach() throws InvalidCommandException, InvalidBenchmarkException, InvalidConfigurationException {
         PrintWriter stdout = new PrintWriter(System.out, true);
         PrintWriter stderr = new PrintWriter(System.err, true);
         CaliperMain.exitlessMain(
                 ObjectArrays.concat(
                 new String[]{
                     "-Cinstrument.micro.options.warmup=30s",
                     "-Dsize=10,100,1000",
                     "--instrument", "micro",
                     "--time-limit", "60s"},
                 ForEachBenchmark.class.getName()),
                 stdout, stderr);
     }
 
     private static void runFatorialForEach() throws InvalidCommandException, InvalidBenchmarkException, InvalidConfigurationException {
         PrintWriter stdout = new PrintWriter(System.out, true);
         PrintWriter stderr = new PrintWriter(System.err, true);
         CaliperMain.exitlessMain(
                 ObjectArrays.concat(
                 new String[]{
                     "-Cinstrument.micro.options.warmup=30s",
                     "-Dsize=2000",
                     "--instrument", "micro",
                     "--time-limit", "60s"},
                 FatorialForEachBenchmark.class.getName()),
                 stdout, stderr);
     }
 }
