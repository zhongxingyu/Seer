 package com.eemi.ext4j.explorer.client.modules.charts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.eemi.ext4j.client.chart.ChartTooltipRenderer;
 import com.eemi.ext4j.client.chart.HighLighter;
import com.eemi.ext4j.client.chart.Legend;
 import com.eemi.ext4j.client.chart.Segment;
 import com.eemi.ext4j.client.chart.laf.ChartTooltip;
 import com.eemi.ext4j.client.chart.laf.Label;
 import com.eemi.ext4j.client.chart.series.AbstractSerie;
 import com.eemi.ext4j.client.chart.series.PieSerie;
 import com.eemi.ext4j.client.chart.theme.Theme;
 import com.eemi.ext4j.client.core.config.Dock;
import com.eemi.ext4j.client.core.config.Position;
 import com.eemi.ext4j.client.data.BaseModel;
 import com.eemi.ext4j.client.data.Store;
 import com.eemi.ext4j.client.data.handlers.EachCallBack;
 import com.eemi.ext4j.client.events.button.ClickEvent;
 import com.eemi.ext4j.client.events.button.ClickHandler;
 import com.eemi.ext4j.client.layout.Layout;
 import com.eemi.ext4j.client.ui.Button;
 import com.eemi.ext4j.client.ui.Chart;
 import com.eemi.ext4j.client.ui.ToolBar;
 import com.eemi.ext4j.client.ui.ToolTip;
 import com.eemi.ext4j.explorer.client.data.DataUtil;
 import com.eemi.ext4j.explorer.client.modules.base.BaseDemoModule;
 import com.eemi.ext4j.explorer.client.modules.charts.resources.ChartsModuleResources;
 import com.eemi.ext4j.webdesktop.client.core.DesktopModuleConfig;
 import com.eemi.ext4j.webdesktop.client.ui.modules.DesktopModuleWindow;
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class PieChartModule extends BaseDemoModule {
 
     public static final PieChartModule INSTANCE = new PieChartModule();
 
     private static final String TITLE = "Pie Chart";
     private Store store;
 
     private PieChartModule() {
 
     }
 
     @Override
     public DesktopModuleConfig createModuleConfig() {
         DesktopModuleConfig moduleConfig = super.createModuleConfig();
         moduleConfig.setModuleTitle(TITLE);
         moduleConfig.setModuleDescription("Ext4j Pie Chart demo");
         moduleConfig.setWallPaperIcon(ChartsModuleResources.ICONS.pie128());
         moduleConfig.setToolbarIcon(ChartsModuleResources.ICONS.pie24());
         moduleConfig.setMenuItemIcon(ChartsModuleResources.ICONS.pie16());
         moduleConfig.setInitialWindowXPosition(500);
         moduleConfig.setInitialWindowYPosition(300);
         return moduleConfig;
     }
 
     @Override
     public DesktopModuleWindow createModuleWindow() {
         DesktopModuleWindow win = super.createModuleWindow();
         win.setLayout(Layout.FIT);
         win.setBodyPadding(20);
 
         final Chart chart = createChart();
         win.add(chart);
 
         ToolBar tb = new ToolBar();
         tb.setDocked(Dock.TOP);
         Button reloadButton = new Button("Reload");
         reloadButton.setCls("x-btn-default-small");
         reloadButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 store.loadData(DataUtil.generateData(12, 20));
             }
         });
         tb.add(reloadButton);
 
         Button donutButton = new Button("Donut", new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 Button button = event.getSource();
                 if (button.isPressed()) {
                     chart.getSeries().get(0).setDonut(35);
                 } else {
                     chart.getSeries().get(0).setDonut(false);
                 }
                 chart.refresh();
 
             }
         });
         donutButton.setEnableToggle(true);
         donutButton.setCls("x-btn-default-small");
         tb.add(donutButton);
 
         win.addDocked(tb);
 
         return win;
     }
 
     private Chart createChart() {
 
         final List<AbstractSerie> series = new ArrayList<AbstractSerie>();
 
         PieSerie serie = new PieSerie();
         serie.setField("data1");
         serie.setShowInLegend(true);
 
         ChartTooltip toolTip = new ChartTooltip();
         toolTip.setTrackMouse(true);
         toolTip.setWidth(140);
         toolTip.setHeight(28);
         toolTip.setRenderer(new ChartTooltipRenderer() {
             private int total;
 
             @Override
             public void onRender(ToolTip tip, BaseModel record, JavaScriptObject item) {
                 total = 0;
                 store.each(new EachCallBack() {
                     @Override
                     public void onEach(BaseModel record) {
                         total += (int) record.getAsInteger("data1");
                     }
                 });
                 tip.setTitle(record.getAsString("name") + " : " + Math.round(record.getAsNumber("data1") / total * 100)
                                 + " %");
             }
         });
         serie.setTips(toolTip);
 
         HighLighter highlither = new HighLighter();
         highlither.setSegment(new Segment(20));
         serie.setHighLighter(highlither);
 
         Label label = new Label();
         label.setField("name");
         label.setDisplay("rotate");
         label.setContrast(true);
         label.setFont("18px Arial");
         serie.setLabel(label);
         series.add(serie);
 
         store = new Store(ChartData.generateData(12));
 
         final Chart chart = Chart.create(store, series);
         chart.setStyle("background:#fff");
        chart.setLegend(new Legend(Position.RIGHT));
         chart.setAnimate(true);
         chart.setShadow(true);
         chart.setInsetPadding(60);
         chart.setTheme(Theme.BASE_GRADIENTS);
         return chart;
     }
 }
