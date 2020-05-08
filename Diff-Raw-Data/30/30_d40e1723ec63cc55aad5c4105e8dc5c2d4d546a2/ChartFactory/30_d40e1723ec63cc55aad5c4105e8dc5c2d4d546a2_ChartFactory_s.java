 package org.motechproject.carereporting.web.chart;
 
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.motechproject.carereporting.domain.ReportEntity;
 import org.motechproject.carereporting.domain.types.ReportType;
 import org.motechproject.carereporting.exception.CareNoValuesException;
 import org.motechproject.carereporting.service.ReportService;
 import org.motechproject.carereporting.web.chart.builder.AxisBuilder;
 import org.motechproject.carereporting.web.chart.builder.BarsBuilder;
 import org.motechproject.carereporting.web.chart.builder.ChartBuilder;
 import org.motechproject.carereporting.web.chart.builder.GridBuilder;
 import org.motechproject.carereporting.web.chart.builder.LegendBuilder;
 import org.motechproject.carereporting.web.chart.builder.MouseBuilder;
 import org.motechproject.carereporting.web.chart.builder.PieBuilder;
 import org.motechproject.carereporting.web.chart.builder.SelectionBuilder;
 import org.motechproject.carereporting.web.chart.builder.SerieBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 
 @Component
 public final class ChartFactory {
 
     @Autowired
     private ReportService reportService;
 
     private static final int CREATE_LINE_CHART_VARIABLE = 4;
     private static final double BAR_WIDTH = .5;
     private static final int EXPLOSION_NUMBER = 6;
     private static final int SELECTION_FPS = 30;
 
     public Chart createLineChart(IndicatorEntity indicator, List<IndicatorValueEntity> values) {
         if (values.size() == 0) {
             throw new CareNoValuesException();
         }
         
         return createTemplateChart(indicator.getName())
                 .xAxis(new AxisBuilder()
                         .minorTickFreq(CREATE_LINE_CHART_VARIABLE)
                         .mode(AxisBuilder.Mode.TIME)
                         .timeformat("%m/%d/%y"))
                 .grid(new GridBuilder()
                         .minorVerticalLines(true))
                 .selection(new SelectionBuilder()
                         .fps(SELECTION_FPS)
                         .mode(SelectionBuilder.Mode.X))
                 .serie(createSerieForIndicatorValues(values))
                 .build();
     }
 
     private Serie createSerieForIndicatorValues(List<IndicatorValueEntity> values) {
         SerieBuilder serieBuilder = new SerieBuilder();
 
         for (IndicatorValueEntity value: values) {
             serieBuilder.point(BigDecimal.valueOf(value.getDate().getTime()), value.getValue());
         }
 
         return serieBuilder.build();
     }
 
     public Chart createBarChart(IndicatorEntity indicator, List<IndicatorValueEntity> values) {
             BigDecimal latestIndicatorValue = values.size() != 0
                     ? values.get(values.size() - 1).getValue()
                     : BigDecimal.ZERO;
 
         ReportEntity reportEntity = reportService.getReportByTypeAndIndicatorId(
                 ReportType.BarChart, indicator.getId());
         String labelX = (reportEntity.getLabelX() == null) ? "" : reportEntity.getLabelX();
         String labelY = (reportEntity.getLabelY() == null) ? "" : reportEntity.getLabelY();
 
         return createTemplateChart(indicator.getName())
                 .bars(new BarsBuilder()
                         .show(true)
                         .horizontal(true)
                         .shadowSize(0)
                         .barWidth(BAR_WIDTH))
                 .xAxis(new AxisBuilder()
                         .min(0)
                         .autoScaleMargin(1))
                 .serie(new SerieBuilder()
                         .label(labelX)
                         .point(latestIndicatorValue, BigDecimal.ONE))
                 .serie(new SerieBuilder()
                         .label(labelY)
                         .point(BigDecimal.ONE.subtract(latestIndicatorValue), 1 + BAR_WIDTH))
                 .build();
     }
 
     public Chart createPieChart(IndicatorEntity indicator, List<IndicatorValueEntity> values) {
         if (values.size() == 0) {
             throw new CareNoValuesException();
         }
 
         BigDecimal indicatorNumeratorsCombined = BigDecimal.ZERO;
         BigDecimal indicatorDenominatorsCombined = BigDecimal.ZERO;
         for (IndicatorValueEntity value : values) {
             indicatorNumeratorsCombined = indicatorNumeratorsCombined.add(value.getNumerator());
             indicatorDenominatorsCombined = indicatorDenominatorsCombined.add(value.getDenominator());
         }
         BigDecimal chartValue = indicatorNumeratorsCombined.divide(indicatorDenominatorsCombined, 4, RoundingMode.HALF_UP);
 
         ReportEntity reportEntity = reportService.getReportByTypeAndIndicatorId(
                 ReportType.PieChart, indicator.getId());
         String labelX = (reportEntity.getLabelX() == null) ? "" : reportEntity.getLabelX();
         String labelY = (reportEntity.getLabelY() == null) ? "" : reportEntity.getLabelY();
 
         return createTemplateChart(indicator.getName())
                 .grid(new GridBuilder()
                         .verticalLines(false)
                         .horizontalLines(false))
                 .xAxis(new AxisBuilder()
                         .showLabels(false))
                 .yAxis(new AxisBuilder()
                         .showLabels(false))
                 .pie(new PieBuilder()
                         .show(true)
                         .explode(EXPLOSION_NUMBER))
                 .serie(new SerieBuilder()
                         .label(labelX)
                         .point(BigDecimal.ZERO, chartValue))
                 .serie(new SerieBuilder()
                         .label(labelY)
                         .point(BigDecimal.ZERO, BigDecimal.ONE.subtract(chartValue)))
                 .build();
     }
 
     private ChartBuilder createTemplateChart(String title) {
         return new ChartBuilder()
                 .title(title)
                 .htmlText(false)
                 .legend(new LegendBuilder()
                         .position(LegendBuilder.Position.BOTTOM_LEFT)
                         .backgroundColor("#D2E8FF"))
                 .mouse(new MouseBuilder()
                         .track(true)
                        .relative(true));
     }
 }
