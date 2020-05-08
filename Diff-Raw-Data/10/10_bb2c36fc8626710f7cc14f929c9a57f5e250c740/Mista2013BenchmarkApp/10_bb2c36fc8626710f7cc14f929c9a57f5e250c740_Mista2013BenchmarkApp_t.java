 package org.optaplanner.examples.projectscheduling.app;
 
 import org.optaplanner.examples.common.app.CommonBenchmarkApp;
 
 public class Mista2013BenchmarkApp extends CommonBenchmarkApp {
 
     public static final String BENCHMARK_CONFIG_TEMPLATE
             = "/org/optaplanner/examples/projectscheduling/benchmark/mista2013BenchmarkConfig.xml.ftl";
     public static final String STEP_LIMIT_BENCHMARK_CONFIG
             = "/org/optaplanner/examples/projectscheduling/benchmark/mista2013StepLimitBenchmarkConfig.xml";
    public static final String CONSTRUCTION_HEURISTICS_BENCHMARK_CONFIG
    = "/org/optaplanner/examples/projectscheduling/benchmark/mista2013CHBenchmarkConfig.xml.ftl";
 
     public static void main(final String[] args) {
         if (args.length > 0) {
            String argument = args[0];
            if (argument.equals("stepLimit")) {
                 new Mista2013BenchmarkApp().buildAndBenchmark(STEP_LIMIT_BENCHMARK_CONFIG);
                 return;
            } else if (argument.equals("constructionHeuristics")) {
                new Mista2013BenchmarkApp().buildFromTemplateAndBenchmark(CONSTRUCTION_HEURISTICS_BENCHMARK_CONFIG);
                return;
             }
         }
         new Mista2013BenchmarkApp().buildFromTemplateAndBenchmark(BENCHMARK_CONFIG_TEMPLATE);
     }
 
 }
