 package org.optaplanner.examples.projectscheduling.app;
 
 import org.optaplanner.examples.common.app.CommonBenchmarkApp;
 
 public class Mista2013BenchmarkApp extends CommonBenchmarkApp {
 
     public static final String BENCHMARK_CONFIG_TEMPLATE
             = "/org/optaplanner/examples/projectscheduling/benchmark/mista2013BenchmarkConfig.xml.ftl";
     public static final String STEP_LIMIT_BENCHMARK_CONFIG
             = "/org/optaplanner/examples/projectscheduling/benchmark/mista2013StepLimitBenchmarkConfig.xml";
 
     public static void main(final String[] args) {
         if (args.length > 0) {
            if (args[0].equals("stepLimit")) {
                 new Mista2013BenchmarkApp().buildAndBenchmark(STEP_LIMIT_BENCHMARK_CONFIG);
                 return;
             }
         }
         new Mista2013BenchmarkApp().buildFromTemplateAndBenchmark(BENCHMARK_CONFIG_TEMPLATE);
     }
 
 }
