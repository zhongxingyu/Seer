 /*
  * Copyright (c) Qatar Computing Research Institute, 2013.
  * Licensed under the MIT license <http://www.opensource.org/licenses/MIT>.
  */
 
 package qa.qcri.qnoise.inject;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Stopwatch;
 import com.google.common.collect.Sets;
 import org.javatuples.Pair;
 import qa.qcri.qnoise.DataProfile;
 import qa.qcri.qnoise.DataType;
 import qa.qcri.qnoise.NoiseReport;
 import qa.qcri.qnoise.NoiseSpec;
 import qa.qcri.qnoise.model.ModelBase;
 import qa.qcri.qnoise.model.ModelFactory;
 import qa.qcri.qnoise.util.Tracer;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 public class OutlierInjector extends InjectorBase {
     private Tracer tracer = Tracer.getTracer(this.getClass());
 
     @Override
     public InjectorBase inject(
         NoiseSpec spec,
         DataProfile profile,
         NoiseReport report
     ) {
         Stopwatch stopwatch = new Stopwatch().start();
         List<String> selectedColumns = spec.filteredColumns;
         Preconditions.checkArgument(
             selectedColumns.size() == 1,
             "Outlier currently only supports one column (univariable)."
         );
 
         String selectedColumn = selectedColumns.get(0);
         DataType type = profile.getType(selectedColumn);
         int columnIndex = profile.getColumnIndex(selectedColumn);
 
         Preconditions.checkArgument(
             type == DataType.Numerical,
             "Outlier only applies to numerical data."
         );
 
         double perc = spec.percentage;
         int len = (int)Math.floor(perc * profile.getLength());
 
         Double std = profile.getStandardDeviationOn(selectedColumn);
         Double mean = profile.getMean(selectedColumn);
 
         Double lmax = mean + 2 * std; // 95 % in the bell
         Double rmax = mean + 5 * std; // (extra 3 * std)
 
         ModelBase randomModal = ModelFactory.createRandomModel();
         HashSet<Pair<Integer, Integer>> log = Sets.newHashSet();
         int index = randomModal.nextIndexWithoutReplacement(0, profile.getLength(), true);
         for (int i = 0; i < len; i ++) {
             // TODO: should we ignore the point already in the outlier region
             // how to deal with small deviation data?
             double u = Math.random() * (rmax - lmax) + lmax;
            double v = new Random().nextGaussian() + u;
             tracer.verbose(String.format("[%d, %d] <- %f", index, columnIndex, v));
             report.logChange(
                 index, columnIndex, profile.getCell(index, columnIndex), Double.toString(v)
             );
             log.add(new Pair<>(index, columnIndex));
             String[] row = profile.getData().get(index);
             row[columnIndex] = Double.toString(v);
             index = randomModal.nextIndexWithoutReplacement(0, profile.getLength(), false);
             while (log.contains(new Pair<>(index, columnIndex))) {
                 index = randomModal.nextIndexWithoutReplacement(0, profile.getLength(), false);
             }
         }
 
         long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
         report.addMetric(NoiseReport.Metric.InjectionTime, elapsedTime);
         stopwatch.stop();
         return this;
     }
 }
