 package org.optaplanner.examples.projectscheduling.swingui;
 
 import java.awt.Dimension;
 import java.util.Date;
 
 import javax.swing.BoxLayout;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.data.category.IntervalCategoryDataset;
 import org.jfree.data.gantt.Task;
 import org.jfree.data.gantt.TaskSeries;
 import org.jfree.data.gantt.TaskSeriesCollection;
 import org.jfree.data.time.SimpleTimePeriod;
 import org.joda.time.DateTime;
 import org.optaplanner.core.impl.solution.Solution;
 import org.optaplanner.examples.common.swingui.SolutionPanel;
 import org.optaplanner.examples.common.swingui.TangoColorFactory;
 import org.optaplanner.examples.projectscheduling.domain.Allocation;
 import org.optaplanner.examples.projectscheduling.domain.Job;
 import org.optaplanner.examples.projectscheduling.domain.Project;
 import org.optaplanner.examples.projectscheduling.domain.ProjectSchedule;
 import org.optaplanner.examples.projectscheduling.solver.solution.Mista2013SolutionCloner;
 
 public class GanttPanel extends SolutionPanel {
 
     private static final long serialVersionUID = -1962577861486577643L;
 
     private static final Mista2013SolutionCloner CLONER = new Mista2013SolutionCloner();
 
     public static IntervalCategoryDataset createDataset(final ProjectSchedule input) {
         final ProjectSchedule solution = GanttPanel.CLONER.cloneSolution(input);
         final TaskSeriesCollection collection = new TaskSeriesCollection();
         for (final Project p : solution.getProblem().getProjects()) {
             final TaskSeries series = new TaskSeries("P" + p.getId());
             for (final Job j : p.getJobs()) {
                 if (j.isSink() || j.isSource()) {
                     continue;
                 }
                 final Allocation a = solution.getAllocation(j);
                 if (!a.isInitialized()) {
                     continue;
                 }
                 series.add(new Task("J" + j.getId(), new SimpleTimePeriod(GanttPanel.date(a.getStartDate()),
                        GanttPanel.date(a.getDueDate()))));
             }
             collection.add(series);
         }
         return collection;
     }
 
     private static Date date(final int day) {
         final DateTime dt = new DateTime().plusDays(day);
         return dt.toDate();
     }
 
     public GanttPanel() {
         final BoxLayout bl = new BoxLayout(this, BoxLayout.Y_AXIS);
         this.setLayout(bl);
     }
 
     private static final TangoColorFactory TANGO = new TangoColorFactory();
 
     private JFreeChart createChart(final IntervalCategoryDataset dataset) {
         final JFreeChart c = ChartFactory.createGanttChart(null, null, null, dataset, true, false, false);
         final CategoryPlot plot = (CategoryPlot) c.getPlot();
         for (int i = 0; i < dataset.getRowCount(); i++) {
             plot.getRenderer().setSeriesPaint(i, GanttPanel.TANGO.pickColor(i));
         }
         return c;
     }
 
     @Override
     public void resetPanel(@SuppressWarnings("rawtypes") final Solution solution) {
         final IntervalCategoryDataset dataset = GanttPanel.createDataset((ProjectSchedule) solution);
         final JFreeChart chart = this.createChart(dataset);
 
         // add the chart to a panel...
         final ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new Dimension(1280, 720));
 
         // add panel to the component
         this.removeAll();
         this.add(chartPanel);
     }
 
 }
