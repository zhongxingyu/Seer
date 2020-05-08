 package com.eemi.ext4j.explorer.client.modules.charts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.eemi.ext4j.client.chart.axis.AbstractAxis;
 import com.eemi.ext4j.client.chart.axis.CategoryAxis;
 import com.eemi.ext4j.client.chart.axis.NumericAxis;
 import com.eemi.ext4j.client.chart.laf.GridConfig;
 import com.eemi.ext4j.client.chart.laf.Label;
 import com.eemi.ext4j.client.chart.laf.Style;
 import com.eemi.ext4j.client.chart.series.AbstractSerie;
 import com.eemi.ext4j.client.chart.series.AreaSerie;
 import com.eemi.ext4j.client.core.config.Dock;
 import com.eemi.ext4j.client.core.config.Position;
 import com.eemi.ext4j.client.core.config.SpriteConfig;
 import com.eemi.ext4j.client.data.Store;
 import com.eemi.ext4j.client.events.button.ClickEvent;
 import com.eemi.ext4j.client.events.button.ClickHandler;
 import com.eemi.ext4j.client.layout.Layout;
 import com.eemi.ext4j.client.ui.Button;
 import com.eemi.ext4j.client.ui.Chart;
 import com.eemi.ext4j.client.ui.ToolBar;
 import com.eemi.ext4j.explorer.client.modules.base.BaseDemoModule;
 import com.eemi.ext4j.explorer.client.modules.charts.resources.ChartsModuleResources;
 import com.eemi.ext4j.webdesktop.client.core.DesktopModuleConfig;
 import com.eemi.ext4j.webdesktop.client.ui.modules.DesktopModuleWindow;
 
 public class AreaChartModule extends BaseDemoModule {
 
     public static final AreaChartModule INSTANCE = new AreaChartModule();
 
     private static final String TITLE = "Area Chart";
     private Store store;
 
     private AreaChartModule() {
 
     }
 
     @Override
     public DesktopModuleConfig createModuleConfig() {
         DesktopModuleConfig moduleConfig = super.createModuleConfig();
         moduleConfig.setModuleTitle(TITLE);
         moduleConfig.setModuleDescription("Ext4j Area Chart demo");
         moduleConfig.setWallPaperIcon(ChartsModuleResources.ICONS.getIcon128());
         moduleConfig.setToolbarIcon(ChartsModuleResources.ICONS.getIcon24());
         moduleConfig.setMenuItemIcon(ChartsModuleResources.ICONS.getIcon16());
         moduleConfig.setInitialWindowXPosition(300);
         moduleConfig.setInitialWindowYPosition(300);
         return moduleConfig;
     }
 
     @Override
     public DesktopModuleWindow createModuleWindow() {
         DesktopModuleWindow win = super.createModuleWindow();
         win.setLayout(Layout.FIT);
         win.setBodyPadding(20);
 
         ToolBar tb = new ToolBar();
         tb.setDocked(Dock.TOP);
         Button reloadButton = new Button("Reload");
         reloadButton.setCls("x-btn-default-small");
         reloadButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 store.loadData(ChartData.generateData(12));
             }
         });
         tb.add(reloadButton);
         win.addDocked(tb);
 
         Chart chart = createChart();
         win.add(chart);
 
         return win;
     }
 
     private Chart createChart() {
 
         store = new Store(ChartData.generateData(12));
 
         final List<AbstractSerie> series = new ArrayList<AbstractSerie>();
 
         AreaSerie serie = new AreaSerie();
         serie.setHighLight(false);
         serie.setXField("name");
         serie.setYField("data1", "data2", "data3", "data4", "data5", "data6", "data7");
 
         Style style = new Style();
         style.setOpacity(0.93);
         serie.setStyle(style);
         series.add(serie);
 
         final List<AbstractAxis> axis = new ArrayList<AbstractAxis>();
 
         NumericAxis numericAxis = new NumericAxis();
         numericAxis.setPosition(Position.LEFT);
         numericAxis.setTitle("Number of Hits");
         numericAxis.setFields("data1", "data2", "data3", "data4", "data5", "data6", "data7");
 
         SpriteConfig odd = new SpriteConfig();
         odd.setOpacity(1);
         odd.setFill("#ddd");
         odd.setStroke("#bbb");
         odd.setStrokeWidth(1);
 
         numericAxis.setGrid(new GridConfig(odd));
         numericAxis.setMinimum(0);
         numericAxis.setAdjustMaximumByMajorUnit(false);
         axis.add(numericAxis);
 
         CategoryAxis categoryAxis = new CategoryAxis();
         categoryAxis.setPosition(Position.BOTTOM);
         categoryAxis.setFields("name");
         categoryAxis.setTitle("Month of the year");
         categoryAxis.setGrid(true);
 
         Label label = new Label();
         label.setRotate(315);
         categoryAxis.setLabel(label);
         axis.add(categoryAxis);
 
        final Chart chart = new Chart() {
            @Override
            protected void onConfigCreated() {
                this.setStore(store);
                // this.setSeries(series);
                this.setAxes(axis);
                // this.setLegend(new Legend(Position.BOTTOM));
            }
        };
         chart.setShadow(true);
         chart.setAnimate(true);
 
         return chart;
     }
 
 }
