 package org.pentaho.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Color;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.pentaho.chart.model.AreaPlot;
 import org.pentaho.chart.model.BarPlot;
 import org.pentaho.chart.model.ChartModel;
 import org.pentaho.chart.model.DialPlot;
 import org.pentaho.chart.model.LinePlot;
 import org.pentaho.chart.model.Palette;
 import org.pentaho.chart.model.PiePlot;
 import org.pentaho.chart.model.StyledText;
 import org.pentaho.chart.model.Axis.LabelOrientation;
 import org.pentaho.chart.model.BarPlot.BarPlotFlavor;
 import org.pentaho.chart.model.ChartTitle.TitleLocation;
 import org.pentaho.chart.model.CssStyle.FontStyle;
 import org.pentaho.chart.model.CssStyle.FontWeight;
 import org.pentaho.chart.model.DialPlot.DialRange;
 import org.pentaho.chart.model.DialPlot.Scale;
 import org.pentaho.chart.model.LinePlot.LinePlotFlavor;
 import org.pentaho.chart.model.Plot.Orientation;
 import org.pentaho.chart.model.Theme.ChartTheme;
 import org.pentaho.chart.model.util.ChartSerializer;
 import org.pentaho.chart.model.util.ChartSerializer.ChartSerializationFormat;
 import org.pentaho.platform.plugin.action.chartbeans.ChartDataDefinition;
 
 public class SerializationTest {
 
   @Before
   public void init(){}
   
   @Test
   public void testDataDefinition() {
     ChartDataDefinition chartDataDefinition = new ChartDataDefinition();
     chartDataDefinition.setCategoryColumn("category");
     chartDataDefinition.setDomainColumn("domain");
     chartDataDefinition.setQuery("query");
     chartDataDefinition.setRangeColumn("range");
    chartDataDefinition.setConvertNullsToZero(true);
     chartDataDefinition.setScalingFactor(2);
     
     String result = ChartSerializer.serializeDataDefinition(chartDataDefinition, ChartSerializationFormat.XML);    
     System.out.println(result);
     ChartDataDefinition chartDataDefinition2 = ChartSerializer.deSerializeDataDefinition(result, ChartSerializationFormat.XML);   
     assertEquals(chartDataDefinition, chartDataDefinition2);
     
     result = ChartSerializer.serializeDataDefinition(chartDataDefinition, ChartSerializationFormat.JSON);    
     chartDataDefinition2 = ChartSerializer.deSerializeDataDefinition(result, ChartSerializationFormat.JSON);   
     assertEquals(chartDataDefinition, chartDataDefinition2);
   }
   
   @Test
   public void testDialPlot() {
     ChartModel chartModel = new ChartModel();
     chartModel.setTheme(ChartTheme.THEME4);
     chartModel.setChartEngine(ChartModel.CHART_ENGINE_JFREE);
     chartModel.setBackground(0x343434);
     chartModel.setBorderColor(0x987654);
     chartModel.setBorderVisible(true);
     chartModel.getTitle().setText("Chart Title");
     chartModel.getTitle().setColor(0x123456);
     chartModel.getTitle().setFont("monospace", 20, FontStyle.OBLIQUE, FontWeight.BOLD);
     chartModel.getLegend().setVisible(true);
     chartModel.getLegend().setBorderColor(0x654321);
     chartModel.getLegend().setBorderVisible(true);
     chartModel.getLegend().setBorderWidth(2);
     chartModel.getLegend().setFont("verdana", 18, FontStyle.ITALIC, FontWeight.BOLD);
     StyledText subtitle = new StyledText("subtitle", "monospace", FontStyle.ITALIC, FontWeight.BOLD, 12);
     subtitle.setColor(0x00FF00);
     subtitle.setBackgroundColor(0x0000FF);
     chartModel.getSubtitles().add(subtitle);;
     chartModel.getSubtitles().add(new StyledText("subtitle 2"));;
     
     
     DialPlot dialPlot = new DialPlot();
     dialPlot.setBackground(0x765890);
     dialPlot.setOpacity(0.75f);
     dialPlot.getScale().addRange(new DialRange(0, 100, Color.RED.getRGB()));
     dialPlot.getScale().addRange(new DialRange(100, 200, Color.GREEN.getRGB()));
     dialPlot.getAnnotation().setText("annotation");
     dialPlot.getAnnotation().setFont("verdana", 10, FontStyle.ITALIC, FontWeight.BOLD);
     
     chartModel.setPlot(dialPlot);
     
     
     String result = ChartSerializer.serialize(chartModel, ChartSerializationFormat.XML);
     
     System.out.println(result);
     
     ChartModel chartModel2 = ChartSerializer.deSerialize(result, ChartSerializationFormat.XML);
     assertEquals(chartModel2.getTheme(), ChartTheme.THEME4);
     assertEquals(chartModel2.getBackground(), new Integer(0x343434));
     assertTrue(chartModel2.getBorderVisible());
     assertEquals(chartModel2.getBorderColor(), new Integer(0x987654));
     assertEquals(chartModel2.getBorderWidth(), new Integer(1));
     assertEquals(chartModel2.getTitle().getText(), "Chart Title");
     assertEquals(chartModel2.getTitle().getColor(), 0x123456);
     assertEquals(chartModel2.getTitle().getFontFamily(), "monospace");
     assertEquals(chartModel2.getTitle().getFontSize(), new Integer(20));
     assertEquals(chartModel2.getTitle().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(chartModel2.getTitle().getFontWeight(), FontWeight.BOLD);
     assertTrue(chartModel2.getLegend().getVisible());
     assertTrue(chartModel2.getLegend().getBorderVisible());
     assertEquals(chartModel2.getLegend().getBorderWidth(), 2);
     assertEquals(chartModel2.getLegend().getFontFamily(), "verdana");
     assertEquals(chartModel2.getLegend().getFontSize(), new Integer(18));
     assertEquals(chartModel2.getLegend().getFontStyle(), FontStyle.ITALIC);
     assertEquals(chartModel2.getLegend().getFontWeight(), FontWeight.BOLD);
     assertEquals(chartModel2.getSubtitles().size(), 2);
     subtitle = chartModel2.getSubtitles().get(0);
     assertEquals(subtitle.getText(), "subtitle");
     assertEquals(subtitle.getFontFamily(), "monospace");
     assertEquals(subtitle.getFontSize(), new Integer(12));
     assertEquals(subtitle.getFontStyle(), FontStyle.ITALIC);
     assertEquals(subtitle.getFontWeight(), FontWeight.BOLD);
     assertEquals(subtitle.getColor(), 0x00FF00);
     assertEquals(subtitle.getBackgroundColor(), 0x0000FF);
     
     assertEquals(chartModel2.getSubtitles().get(1).getText(), "subtitle 2");
     
     assertTrue(chartModel2.getPlot() instanceof DialPlot);    
     dialPlot = (DialPlot)chartModel2.getPlot();
     assertEquals(dialPlot.getBackground(), new Integer(0x765890));
     assertEquals(dialPlot.getOpacity(), new Float(0.75));
     assertEquals(dialPlot.getAnnotation().getText(), "annotation");
     assertEquals(dialPlot.getAnnotation().getFontFamily(), "verdana");
     assertEquals(dialPlot.getAnnotation().getFontSize(), new Integer(10));
     assertEquals(dialPlot.getAnnotation().getFontStyle(), FontStyle.ITALIC);
     assertEquals(dialPlot.getAnnotation().getFontWeight(), FontWeight.BOLD);
     Scale scale = dialPlot.getScale();
     assertEquals(scale.size(), 2);
     for (DialRange dialRange : scale) {
       if (dialRange.getMinValue().equals(new Integer(0))) {
         assertEquals(dialRange.getMaxValue(), 100);
         assertEquals(dialRange.getColor(), (0xFFFFFF & Color.RED.getRGB()));
       } else if (dialRange.getMinValue().equals(new Integer(100))) {
         assertEquals(dialRange.getMaxValue(), 200);
         assertEquals(dialRange.getColor(), (0xFFFFFF & Color.GREEN.getRGB()));
       }
     }
   }
   
   @Test
   public void testBarPlot(){
     
     ChartModel chartModel = new ChartModel();
     chartModel.setTheme(ChartTheme.THEME4);
     chartModel.setBackground(0x343434);
     chartModel.setBorderColor(0x987654);
     chartModel.setBorderVisible(true);
     chartModel.getTitle().setText("Chart Title");
     chartModel.getTitle().setColor(0x123456);
     chartModel.getTitle().setFont("monospace", 20, FontStyle.OBLIQUE, FontWeight.BOLD);
     chartModel.getLegend().setVisible(true);
     chartModel.getLegend().setBorderColor(0x654321);
     chartModel.getLegend().setBorderVisible(true);
     chartModel.getLegend().setBorderWidth(2);
     chartModel.getLegend().setFont("verdana", 18, FontStyle.ITALIC, FontWeight.BOLD);
     
     BarPlot barPlot = new BarPlot();
     barPlot.setBackground(0x765890);
     barPlot.setFlavor(BarPlotFlavor.THREED);
     barPlot.setOpacity(0.75f);
     barPlot.setOrientation(Orientation.HORIZONTAL);
     barPlot.setPalette(new Palette(0x001111, 0x222222, 0x333333));
     barPlot.getXAxis().setLabelOrientation(LabelOrientation.VERTICAL);
     barPlot.getXAxis().getLegend().setText("xAxis");
     barPlot.getXAxis().getLegend().setColor(0x192837);
     barPlot.getXAxis().getLegend().setFont("san-serif", 10, FontStyle.NORMAL, FontWeight.NORMAL);
     barPlot.getYAxis().getLegend().setText("yAxis");
     barPlot.getYAxis().getLegend().setColor(0x192837);
     barPlot.getYAxis().getLegend().setFont("san-serif", 12, FontStyle.OBLIQUE, FontWeight.BOLD);
     
     chartModel.setPlot(barPlot);
     
     
     String result = ChartSerializer.serialize(chartModel, ChartSerializationFormat.XML);
     
     System.out.println(result);
     
     ChartModel chartModel2 = ChartSerializer.deSerialize(result, ChartSerializationFormat.XML);
     assertEquals(chartModel2.getTheme(), ChartTheme.THEME4);
     assertEquals(chartModel2.getBackground(), new Integer(0x343434));
     assertTrue(chartModel2.getBorderVisible());
     assertEquals(chartModel2.getBorderColor(), new Integer(0x987654));
     assertEquals(chartModel2.getBorderWidth(), new Integer(1));
     assertEquals(chartModel2.getTitle().getText(), "Chart Title");
     assertEquals(chartModel2.getTitle().getColor(), 0x123456);
     assertEquals(chartModel2.getTitle().getFontFamily(), "monospace");
     assertEquals(chartModel2.getTitle().getFontSize(), new Integer(20));
     assertEquals(chartModel2.getTitle().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(chartModel2.getTitle().getFontWeight(), FontWeight.BOLD);
     assertTrue(chartModel2.getLegend().getVisible());
     assertTrue(chartModel2.getLegend().getBorderVisible());
     assertEquals(chartModel2.getLegend().getBorderWidth(), 2);
     assertEquals(chartModel2.getLegend().getFontFamily(), "verdana");
     assertEquals(chartModel2.getLegend().getFontSize(), new Integer(18));
     assertEquals(chartModel2.getLegend().getFontStyle(), FontStyle.ITALIC);
     assertEquals(chartModel2.getLegend().getFontWeight(), FontWeight.BOLD);
     
     assertTrue(chartModel2.getPlot() instanceof BarPlot);    
     barPlot = (BarPlot)chartModel2.getPlot();
     assertEquals(barPlot.getBackground(), new Integer(0x765890));
     assertEquals(barPlot.getFlavor(), BarPlotFlavor.THREED);
     assertEquals(barPlot.getOpacity(), new Float(0.75));
     assertEquals(barPlot.getOrientation(), Orientation.HORIZONTAL);
     Palette palette = chartModel2.getPlot().getPalette();
     assertEquals(palette.size(), 3);
     assertEquals(palette.get(0), 0x001111);
     assertEquals(palette.get(1), 0x222222);
     assertEquals(palette.get(2), 0x333333);
     assertEquals(barPlot.getXAxis().getLegend().getText(), "xAxis");
     assertEquals(barPlot.getXAxis().getLegend().getColor(), 0x192837);
     assertEquals(barPlot.getXAxis().getLegend().getFontFamily(), "san-serif");
     assertEquals(barPlot.getXAxis().getLegend().getFontSize(), 10);
     assertEquals(barPlot.getXAxis().getLegend().getFontStyle(), FontStyle.NORMAL);
     assertEquals(barPlot.getXAxis().getLegend().getFontWeight(), FontWeight.NORMAL);
     assertEquals(barPlot.getYAxis().getLegend().getText(), "yAxis");
     assertEquals(barPlot.getYAxis().getLegend().getColor(), 0x192837);
     assertEquals(barPlot.getYAxis().getLegend().getFontFamily(), "san-serif");
     assertEquals(barPlot.getYAxis().getLegend().getFontSize(), 12);
     assertEquals(barPlot.getYAxis().getLegend().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(barPlot.getYAxis().getLegend().getFontWeight(), FontWeight.BOLD);    
   }
   
   @Test
   public void testLinePlot(){
     
     ChartModel chartModel = new ChartModel();
     chartModel.setTheme(ChartTheme.THEME4);
     chartModel.setBackground(0x343434);
     chartModel.setBorderColor(0x987654);
     chartModel.setBorderVisible(true);
     chartModel.getTitle().setText("Chart Title");
     chartModel.getTitle().setLocation(TitleLocation.BOTTOM);
     chartModel.getTitle().setColor(0x123456);
     chartModel.getTitle().setFont("monospace", 20, FontStyle.OBLIQUE, FontWeight.BOLD);
     chartModel.getLegend().setVisible(true);
     chartModel.getLegend().setBorderColor(0x654321);
     chartModel.getLegend().setBorderVisible(true);
     chartModel.getLegend().setBorderWidth(2);
     chartModel.getLegend().setFont("verdana", 18, FontStyle.ITALIC, FontWeight.BOLD);
     
     LinePlot linePlot = new LinePlot();
     linePlot.setBackground(0x765890);
     linePlot.setFlavor(LinePlotFlavor.THREED);
     linePlot.setOpacity(0.75f);
     linePlot.setPalette(new Palette(0x001111, 0x222222, 0x333333));
     linePlot.getXAxis().setLabelOrientation(LabelOrientation.VERTICAL);
     linePlot.getYAxis().setLabelOrientation(LabelOrientation.VERTICAL);
     
     chartModel.setPlot(linePlot);
     
     String result = ChartSerializer.serialize(chartModel, ChartSerializationFormat.XML);
     
     System.out.println(result);
     
     ChartModel chartModel2 = ChartSerializer.deSerialize(result, ChartSerializationFormat.XML);
     assertEquals(chartModel2.getTheme(), ChartTheme.THEME4);
     assertEquals(chartModel2.getBackground(), new Integer(0x343434));
     assertTrue(chartModel2.getBorderVisible());
     assertEquals(chartModel2.getBorderColor(), new Integer(0x987654));
     assertEquals(chartModel2.getBorderWidth(), new Integer(1));
     assertEquals(chartModel2.getTitle().getText(), "Chart Title");
     assertEquals(chartModel2.getTitle().getLocation(), TitleLocation.BOTTOM);
     assertEquals(chartModel2.getTitle().getColor(), 0x123456);
     assertEquals(chartModel2.getTitle().getFontFamily(), "monospace");
     assertEquals(chartModel2.getTitle().getFontSize(), new Integer(20));
     assertEquals(chartModel2.getTitle().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(chartModel2.getTitle().getFontWeight(), FontWeight.BOLD);
     assertTrue(chartModel2.getLegend().getVisible());
     assertTrue(chartModel2.getLegend().getBorderVisible());
     assertEquals(chartModel2.getLegend().getBorderWidth(), 2);
     assertEquals(chartModel2.getLegend().getFontFamily(), "verdana");
     assertEquals(chartModel2.getLegend().getFontSize(), new Integer(18));
     assertEquals(chartModel2.getLegend().getFontStyle(), FontStyle.ITALIC);
     assertEquals(chartModel2.getLegend().getFontWeight(), FontWeight.BOLD);
     
     assertTrue(chartModel2.getPlot() instanceof LinePlot);    
     linePlot = (LinePlot)chartModel2.getPlot();
     assertEquals(linePlot.getBackground(), new Integer(0x765890));
     assertEquals(linePlot.getFlavor(), LinePlotFlavor.THREED);
     assertEquals(linePlot.getOpacity(), new Float(0.75));
     Palette palette = chartModel2.getPlot().getPalette();
     assertEquals(palette.size(), 3);
     assertEquals(palette.get(0), 0x001111);
     assertEquals(palette.get(1), 0x222222);
     assertEquals(palette.get(2), 0x333333);
     assertEquals(linePlot.getXAxis().getLabelOrientation(), LabelOrientation.VERTICAL);
     assertEquals(linePlot.getYAxis().getLabelOrientation(), LabelOrientation.VERTICAL);
   }
   
   @Test
   public void testAreaPlot(){
     
     ChartModel chartModel = new ChartModel();
     chartModel.setTheme(ChartTheme.THEME4);
     chartModel.setBackground(0x343434);
     chartModel.setBorderColor(0x987654);
     chartModel.setBorderVisible(true);
     chartModel.getTitle().setText("Chart Title");
     chartModel.getTitle().setColor(0x123456);
     chartModel.getTitle().setFont("monospace", 20, FontStyle.OBLIQUE, FontWeight.BOLD);
     chartModel.getLegend().setVisible(true);
     chartModel.getLegend().setBorderColor(0x654321);
     chartModel.getLegend().setBorderVisible(true);
     chartModel.getLegend().setBorderWidth(2);
     chartModel.getLegend().setFont("verdana", 18, FontStyle.ITALIC, FontWeight.BOLD);
     
     AreaPlot areaPlot = new AreaPlot();
     areaPlot.setBackground(0x765890);
     areaPlot.setOpacity(0.75f);
     areaPlot.setPalette(new Palette(0x001111, 0x222222, 0x333333));
     areaPlot.getXAxis().getLegend().setText("xAxis");
     areaPlot.getXAxis().getLegend().setColor(0x192837);
     areaPlot.getXAxis().getLegend().setFont("san-serif", 10, FontStyle.NORMAL, FontWeight.NORMAL);
     areaPlot.getYAxis().getLegend().setText("yAxis");
     areaPlot.getYAxis().getLegend().setColor(0x192837);
     areaPlot.getYAxis().getLegend().setFont("san-serif", 12, FontStyle.OBLIQUE, FontWeight.BOLD);
     
     chartModel.setPlot(areaPlot);
     
     
     String result = ChartSerializer.serialize(chartModel, ChartSerializationFormat.XML);
     
     System.out.println(result);
     
     ChartModel chartModel2 = ChartSerializer.deSerialize(result, ChartSerializationFormat.XML);
     assertEquals(chartModel2.getTheme(), ChartTheme.THEME4);
     assertEquals(chartModel2.getBackground(), new Integer(0x343434));
     assertTrue(chartModel2.getBorderVisible());
     assertEquals(chartModel2.getBorderColor(), new Integer(0x987654));
     assertEquals(chartModel2.getBorderWidth(), new Integer(1));
     assertEquals(chartModel2.getTitle().getText(), "Chart Title");
     assertEquals(chartModel2.getTitle().getColor(), 0x123456);
     assertEquals(chartModel2.getTitle().getFontFamily(), "monospace");
     assertEquals(chartModel2.getTitle().getFontSize(), new Integer(20));
     assertEquals(chartModel2.getTitle().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(chartModel2.getTitle().getFontWeight(), FontWeight.BOLD);
     assertTrue(chartModel2.getLegend().getVisible());
     assertTrue(chartModel2.getLegend().getBorderVisible());
     assertEquals(chartModel2.getLegend().getBorderWidth(), 2);
     assertEquals(chartModel2.getLegend().getFontFamily(), "verdana");
     assertEquals(chartModel2.getLegend().getFontSize(), new Integer(18));
     assertEquals(chartModel2.getLegend().getFontStyle(), FontStyle.ITALIC);
     assertEquals(chartModel2.getLegend().getFontWeight(), FontWeight.BOLD);
     
     assertTrue(chartModel2.getPlot() instanceof AreaPlot);    
     areaPlot = (AreaPlot)chartModel2.getPlot();
     assertEquals(areaPlot.getBackground(), new Integer(0x765890));
     assertEquals(areaPlot.getOpacity(), new Float(0.75));
     Palette palette = chartModel2.getPlot().getPalette();
     assertEquals(palette.size(), 3);
     assertEquals(palette.get(0), 0x001111);
     assertEquals(palette.get(1), 0x222222);
     assertEquals(palette.get(2), 0x333333);
     assertEquals(areaPlot.getXAxis().getLegend().getText(), "xAxis");
     assertEquals(areaPlot.getXAxis().getLegend().getColor(), 0x192837);
     assertEquals(areaPlot.getXAxis().getLegend().getFontFamily(), "san-serif");
     assertEquals(areaPlot.getXAxis().getLegend().getFontSize(), 10);
     assertEquals(areaPlot.getXAxis().getLegend().getFontStyle(), FontStyle.NORMAL);
     assertEquals(areaPlot.getXAxis().getLegend().getFontWeight(), FontWeight.NORMAL);
     assertEquals(areaPlot.getYAxis().getLegend().getText(), "yAxis");
     assertEquals(areaPlot.getYAxis().getLegend().getColor(), 0x192837);
     assertEquals(areaPlot.getYAxis().getLegend().getFontFamily(), "san-serif");
     assertEquals(areaPlot.getYAxis().getLegend().getFontSize(), 12);
     assertEquals(areaPlot.getYAxis().getLegend().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(areaPlot.getYAxis().getLegend().getFontWeight(), FontWeight.BOLD);    
   }
   
   @Test
   public void testPiePlot(){
     
     ChartModel chartModel = new ChartModel();
     chartModel.setTheme(ChartTheme.THEME4);
     chartModel.setBackground(0x343434);
     chartModel.setBorderColor(0x987654);
     chartModel.setBorderVisible(true);
     chartModel.getTitle().setText("Chart Title");
     chartModel.getTitle().setColor(0x123456);
     chartModel.getTitle().setFont("monospace", 20, FontStyle.OBLIQUE, FontWeight.BOLD);
     chartModel.getLegend().setVisible(true);
     chartModel.getLegend().setBorderColor(0x654321);
     chartModel.getLegend().setBorderVisible(true);
     chartModel.getLegend().setBorderWidth(2);
     chartModel.getLegend().setFont("verdana", 18, FontStyle.ITALIC, FontWeight.BOLD);
     
     PiePlot piePlot = new PiePlot();
     piePlot.setBackground(0x765890);
     piePlot.setStartAngle(65);
     piePlot.setOpacity(0.75f);
     piePlot.setPalette(new Palette(0x001111, 0x222222, 0x333333));
     piePlot.setAnimate(true);
     piePlot.getLabels().setFont("monospace", 8, FontStyle.OBLIQUE, FontWeight.BOLD);
     
     chartModel.setPlot(piePlot);
     
     String result = ChartSerializer.serialize(chartModel, ChartSerializationFormat.XML);
     
     System.out.println(result);
     
     ChartModel chartModel2 = ChartSerializer.deSerialize(result, ChartSerializationFormat.XML);
     assertEquals(chartModel2.getTheme(), ChartTheme.THEME4);
     assertEquals(chartModel2.getBackground(), new Integer(0x343434));
     assertTrue(chartModel2.getBorderVisible());
     assertEquals(chartModel2.getBorderColor(), new Integer(0x987654));
     assertEquals(chartModel2.getBorderWidth(), new Integer(1));
     assertEquals(chartModel2.getTitle().getText(), "Chart Title");
     assertEquals(chartModel2.getTitle().getColor(), 0x123456);
     assertEquals(chartModel2.getTitle().getFontFamily(), "monospace");
     assertEquals(chartModel2.getTitle().getFontSize(), new Integer(20));
     assertEquals(chartModel2.getTitle().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(chartModel2.getTitle().getFontWeight(), FontWeight.BOLD);
     assertTrue(chartModel2.getLegend().getVisible());
     assertTrue(chartModel2.getLegend().getBorderVisible());
     assertEquals(chartModel2.getLegend().getBorderWidth(), 2);
     assertEquals(chartModel2.getLegend().getFontFamily(), "verdana");
     assertEquals(chartModel2.getLegend().getFontSize(), new Integer(18));
     assertEquals(chartModel2.getLegend().getFontStyle(), FontStyle.ITALIC);
     assertEquals(chartModel2.getLegend().getFontWeight(), FontWeight.BOLD);
     
     assertTrue(chartModel2.getPlot() instanceof PiePlot);    
     piePlot = (PiePlot)chartModel2.getPlot();
     assertEquals(piePlot.getBackground(), new Integer(0x765890));
     assertEquals(piePlot.getOpacity(), new Float(0.75));
     Palette palette = chartModel2.getPlot().getPalette();
     assertEquals(palette.size(), 3);
     assertEquals(palette.get(0), 0x001111);
     assertEquals(palette.get(1), 0x222222);
     assertEquals(palette.get(2), 0x333333);
     assertEquals(piePlot.getStartAngle(), 65);
     assertTrue(piePlot.getAnimate());
     assertTrue(piePlot.getLabels().getVisible());
     assertEquals(piePlot.getLabels().getFontFamily(), "monospace");
     assertEquals(piePlot.getLabels().getFontSize(), new Integer(8));
     assertEquals(piePlot.getLabels().getFontStyle(), FontStyle.OBLIQUE);
     assertEquals(piePlot.getLabels().getFontWeight(), FontWeight.BOLD);
   }
 }
