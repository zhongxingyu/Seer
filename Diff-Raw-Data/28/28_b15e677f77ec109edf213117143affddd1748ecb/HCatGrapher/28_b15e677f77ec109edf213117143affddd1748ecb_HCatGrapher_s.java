 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hcatalog.hcatmix;
 
 import com.googlecode.charts4j.*;
 import static com.googlecode.charts4j.Color.*;
 
 import org.perf4j.GroupedTimingStatistics;
 import org.perf4j.TimingStatistics;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: malakar
  */
 public class HCatGrapher {
 
 
     public static String createChart(GroupedTimingStatistics timedStats) {
         List<Double> minsList = new ArrayList<Double>();
         List<Double> avgList = new ArrayList<Double>();
         List<Double> maxList = new ArrayList<Double>();
 
         List<String> labels = new ArrayList<String>();
         Double max = 0.0;
         String fileName = "";
         for (Map.Entry<String, TimingStatistics> statEntry : timedStats.getStatisticsByTag().entrySet()) {
             labels.add(LoadStoreStopWatch.getTypeFromTag(statEntry.getKey()));
             fileName = LoadStoreStopWatch.getFileNameFromTag(statEntry.getKey());
             TimingStatistics stat = statEntry.getValue();
             minsList.add(Double.valueOf(stat.getMin()));
             avgList.add(stat.getMean());
             maxList.add(Double.valueOf(stat.getMax()));
             if(stat.getMax() > max) {
                 max = (double) stat.getMax();
             }

         }
 
         // EXAMPLE CODE START
         // Defining data series.
         Data mins = DataUtil.scaleWithinRange(0, max, minsList);
         Data avgs = DataUtil.scaleWithinRange(0, max, avgList);
         Data maxes = DataUtil.scaleWithinRange(0, max, maxList);
         BarChartPlot red = Plots.newBarChartPlot(maxes, RED, "Max");
         BarChartPlot green = Plots.newBarChartPlot(avgs, GREEN, "Avg");
         BarChartPlot yellow = Plots.newBarChartPlot(mins, YELLOW, "Min");
         BarChart chart = GCharts.newBarChart(red, green,  yellow);
 
         // Defining axis info and styles
         AxisStyle axisStyle = AxisStyle.newAxisStyle(BLACK, 13, AxisTextAlignment.CENTER);
         AxisLabels typeAxisLabels = AxisLabelsFactory.newAxisLabels(labels);
         typeAxisLabels.setAxisStyle(axisStyle);
         AxisLabels timeAxis = AxisLabelsFactory.newAxisLabels("Time in seconds");
         timeAxis.setAxisStyle(axisStyle);
        AxisLabels timeScale = AxisLabelsFactory.newNumericRangeAxisLabels(0, 500);
         timeScale.setAxisStyle(axisStyle);
 
 
         // Adding axis info to chart.
         chart.addXAxisLabels(timeScale);
         chart.addXAxisLabels(timeAxis);
         chart.addYAxisLabels(typeAxisLabels);
         chart.addTopAxisLabels(timeScale);
         chart.setHorizontal(true);
         chart.setSize(650, 450);
 //        chart.setSpaceBetweenGroupsOfBars(30);
 
         chart.setTitle(fileName, BLACK, 16);
         //51 is the max number of medals.
         chart.setGrid((50.0/500)*20, 600, 3, 2);
         chart.setBackgroundFill(Fills.newSolidFill(LIGHTGREY));
 //        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("E37600"), 100);
 //        fill.addColorAndOffset(Color.newColor("DC4800"), 0);
 //        chart.setAreaFill(fill);
         return chart.toURLString();
     }
 }
