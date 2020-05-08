 /**
  *    OpenTrainingCenter
  *
  *    Copyright (C) 2014 Sascha Iseli sascha.iseli(at)gmx.ch
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.opentrainingcenter.charts.bar;
 
 import java.awt.Color;
 import java.text.DecimalFormat;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.labels.CategoryItemLabelGenerator;
 import org.jfree.chart.labels.ItemLabelAnchor;
 import org.jfree.chart.labels.ItemLabelPosition;
 import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
 import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.AbstractRenderer;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.Month;
 import org.jfree.data.time.RegularTimePeriod;
 import org.jfree.data.time.Week;
 import org.jfree.data.time.Year;
 import org.jfree.experimental.chart.swt.ChartComposite;
 import org.jfree.ui.TextAnchor;
 
 import ch.opentrainingcenter.charts.bar.internal.ChartDataSupport;
 import ch.opentrainingcenter.charts.bar.internal.ChartDataWrapper;
 import ch.opentrainingcenter.charts.bar.internal.OTCBarPainter;
 import ch.opentrainingcenter.charts.ng.SimpleTrainingChart;
 import ch.opentrainingcenter.charts.single.ChartSerieType;
 import ch.opentrainingcenter.core.PreferenceConstants;
 import ch.opentrainingcenter.core.helper.ColorFromPreferenceHelper;
 import ch.opentrainingcenter.model.training.ISimpleTraining;
 
 public class OTCCategoryChartViewer {
 
     private static final String DECIMAL = "0.000"; //$NON-NLS-1$
 
     private static final Logger LOGGER = Logger.getLogger(OTCCategoryChartViewer.class);
 
     static final int ALPHA = 255;
 
     private ChartComposite chartComposite;
 
     private JFreeChart chart;
 
     private final IPreferenceStore store;
 
     private Composite parent;
 
     private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
 
     private BarRenderer renderer;
 
     public OTCCategoryChartViewer(final IPreferenceStore store) {
         this.store = store;
     }
 
     public void setParent(final Composite parent) {
         this.parent = parent;
     }
 
     public void createPartControl(final ChartSerieType chartSerieType, final SimpleTrainingChart chartType, final List<ISimpleTraining> dataNow,
             final List<ISimpleTraining> dataPast) {
 
         init(dataNow, dataPast, chartSerieType);
 
         createChart(chartSerieType, chartType);
 
         chartComposite = new ChartComposite(parent, SWT.NONE, chart, true);
         final GridData gd = new GridData(SWT.FILL);
         gd.grabExcessHorizontalSpace = true;
         gd.grabExcessVerticalSpace = true;
         gd.horizontalAlignment = SWT.FILL;
         gd.verticalAlignment = SWT.FILL;
         gd.heightHint = 700;
         chartComposite.setLayoutData(gd);
         chartComposite.forceRedraw();
     }
 
     void init(final List<ISimpleTraining> dataNow, final List<ISimpleTraining> dataPast, final ChartSerieType type) {
         dataset.clear();
         updateData(dataPast, dataNow, type, SimpleTrainingChart.DISTANZ, false);
     }
 
     JFreeChart createChart(final ChartSerieType type, final SimpleTrainingChart chartType) {
         chart = ChartFactory.createBarChart(chartType.getTitle(), chartType.getxAchse(), chartType.getyAchse(), dataset, PlotOrientation.VERTICAL, false, true,
                 false);
         chart.setAntiAlias(true);
         chart.setBorderVisible(false);
         chart.setAntiAlias(true);
         chart.setBorderVisible(true);
         chart.setTextAntiAlias(true);
         chart.setBackgroundPaint(Color.white);
         chart.setBorderPaint(Color.white);
 
         updateRenderer(type, chartType, false);
         //
         final CategoryPlot plot = chart.getCategoryPlot();
         plot.setBackgroundPaint(Color.white);
         plot.setDomainGridlinePaint(Color.lightGray);
         plot.setRangeGridlinePaint(Color.lightGray);
         return chart;
     }
 
     /**
      * Aktualisiert die Daten
      * 
      * @param compareLast
      *            mit letztem Jahr vergleichen
      */
     public void updateData(final List<ISimpleTraining> dataPast, final List<ISimpleTraining> dataNow, final ChartSerieType type,
             final SimpleTrainingChart chartType, final boolean compareLast) {
 
         updateAxis(chartType);
         dataset.clear();
 
         final ChartDataSupport support = new ChartDataSupport(type);
         final List<ChartDataWrapper> now = support.convertAndSort(dataNow);
         final List<ChartDataWrapper> past = support.createPastData(dataPast, now);
 
         if (isComparable(type, compareLast)) {
            addValues(past, "LetztesJahr", chartType); //$NON-NLS-1$
         }
        addValues(now, "DiesesJahr", chartType); //$NON-NLS-1$
     }
 
     private void addValues(final List<ChartDataWrapper> now, final String rowName, final SimpleTrainingChart stc) {
         for (final ChartDataWrapper t : now) {
             dataset.addValue(t.getValue(stc), rowName, t.getCategory());
         }
     }
 
     void updateAxis(final SimpleTrainingChart chartType) {
         if (chart != null) {
             // update axis & title
             chart.setTitle(chartType.getTitle());
             ((CategoryPlot) chart.getPlot()).getRangeAxis().setLabel(chartType.getyAchse());
         }
     }
 
     public void updateRenderer(final ChartSerieType type, final SimpleTrainingChart chartType, final boolean compareLast) {
         renderer = new BarRenderer();
         setSeriesPaint(renderer, chartType, type, compareLast);
 
         final OTCBarPainter painter = new OTCBarPainter();
         renderer.setBarPainter(painter);
         renderer.setItemMargin(0.0);
 
         final String labelPattern = "{2} km (" + type.getLabel() + "{1})"; //$NON-NLS-1$//$NON-NLS-2$
         renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(labelPattern, new DecimalFormat(DECIMAL)));
 
         final ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER, -Math.PI / 2.0);
         renderer.setBasePositiveItemLabelPosition(position);
 
         final CategoryItemLabelGenerator cl = new StandardCategoryItemLabelGenerator(labelPattern, new DecimalFormat(DECIMAL));
         renderer.setBaseItemLabelGenerator(cl);
         renderer.setBaseItemLabelsVisible(true);
         if (chart != null) {
             chart.getCategoryPlot().setRenderer(renderer);
         }
     }
 
     private void setSeriesPaint(final AbstractRenderer renderer, final SimpleTrainingChart chartType, final ChartSerieType type, final boolean compareLast) {
         final String firstColor;
         final String secondColor;
         if (SimpleTrainingChart.DISTANZ.equals(chartType)) {
             if (isComparable(type, compareLast)) {
                 firstColor = PreferenceConstants.CHART_DISTANCE_COLOR_PAST;
                 secondColor = PreferenceConstants.CHART_DISTANCE_COLOR;
             } else {
                 firstColor = PreferenceConstants.CHART_DISTANCE_COLOR;
                 secondColor = PreferenceConstants.CHART_DISTANCE_COLOR;
             }
         } else {
             if (isComparable(type, compareLast)) {
                 firstColor = PreferenceConstants.CHART_HEART_COLOR_PAST;
                 secondColor = PreferenceConstants.CHART_HEART_COLOR;
             } else {
                 firstColor = PreferenceConstants.CHART_HEART_COLOR;
                 secondColor = PreferenceConstants.CHART_HEART_COLOR;
             }
         }
         renderer.setSeriesPaint(0, ColorFromPreferenceHelper.getColor(store, firstColor, ALPHA));
         renderer.setSeriesPaint(1, ColorFromPreferenceHelper.getColor(store, secondColor, ALPHA));
     }
 
     private boolean isComparable(final ChartSerieType type, final boolean compareLast) {
         return compareLast && !ChartSerieType.DAY.equals(type);
     }
 
     Class<? extends RegularTimePeriod> getSeriesType(final ChartSerieType chartType) {
         final Class<? extends RegularTimePeriod> thisClazz;
         switch (chartType) {
         case DAY:
             thisClazz = Day.class;
             break;
         case WEEK:
             thisClazz = Week.class;
             break;
         case MONTH:
             thisClazz = Month.class;
             break;
         case YEAR:
             thisClazz = Year.class;
             break;
         default:
             thisClazz = Day.class;
         }
         return thisClazz;
     }
 
     public void forceRedraw() {
         LOGGER.info("Force Redraw Chart"); //$NON-NLS-1$
         chartComposite.forceRedraw();
     }
 
     /**
      * nur fuer Testzwecke
      */
     void setChart(final JFreeChart mockedChart) {
         chart = mockedChart;
     }
 
     /**
      * nur fuer Testzwecke
      */
     DefaultCategoryDataset getDataset() {
         return dataset;
     }
 
     public BarRenderer getRenderer() {
         return renderer;
     }
 
 }
