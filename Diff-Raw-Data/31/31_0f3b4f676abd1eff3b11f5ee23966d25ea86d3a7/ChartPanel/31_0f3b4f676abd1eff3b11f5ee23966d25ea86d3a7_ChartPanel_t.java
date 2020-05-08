 package org.scriptbox.ui.client;
 
 import java.util.Date;
 
 import org.scriptbox.metrics.model.Metric;
 import org.scriptbox.ui.shared.timed.TimeBasedChartUpdateBinding;
 import org.scriptbox.ui.shared.timed.TimeBasedLoader;
 import org.scriptbox.ui.shared.tree.MetricQueryDto;
 import org.scriptbox.ui.shared.tree.MetricRangeDto;
 import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
 import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
 import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.editor.client.Editor.Path;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sencha.gxt.chart.client.chart.Chart;
 import com.sencha.gxt.chart.client.chart.Chart.Position;
 import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
 import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
 import com.sencha.gxt.chart.client.chart.series.LineSeries;
 import com.sencha.gxt.chart.client.chart.series.Primitives;
 import com.sencha.gxt.chart.client.draw.RGB;
 import com.sencha.gxt.chart.client.draw.sprite.Sprite;
 import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.data.client.loader.RpcProxy;
 import com.sencha.gxt.data.shared.LabelProvider;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.PropertyAccess;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 
 public class ChartPanel extends ContentPanel {
 
 	public interface MetricPropertyAccess extends PropertyAccess<Metric> {
 		@Path("date")
 		ModelKeyProvider<Metric> nameKey();
 		ValueProvider<Metric, Date> date();
		ValueProvider<Metric, Float> value();
 	}
 
 	private static final MetricPropertyAccess metricAccess = GWT.create(MetricPropertyAccess.class);
 	private static final DateTimeFormat f = DateTimeFormat.getFormat("HH:mm:ss");
 
 	// private Timer update;
 
 	private Chart<Metric> chart;
 	private NumericAxis<Metric> valueAxis;
 	private TimeAxis<Metric> timeAxis;
 	private	LineSeries<Metric> series;
 	private MetricTreeGWTInterfaceAsync service;
 	private	TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader;
 	private ListStore<Metric> store;
 	private TimeBasedChartUpdateBinding<MetricQueryDto, Metric, MetricRangeDto> binding;
 	
 
 	public void load( MetricTreeNodeDto node ) {
 		MetricQueryDto query = new MetricQueryDto();
 		query.setNode( node );
 		query.setStart( new Date() );
 		query.setEnd( new Date() );
 		loader.load( query );
 	}
 	
 	public ChartPanel() {
 		service = GWT.create(MetricTreeGWTInterface.class);
 		
 		chart = new Chart<Metric>(600, 400);
 		chart.setDefaultInsets(20);
 
 		RpcProxy<MetricQueryDto, MetricRangeDto> proxy = new RpcProxy<MetricQueryDto, MetricRangeDto>() {
 			@Override
 			public void load(MetricQueryDto loadConfig, AsyncCallback<MetricRangeDto> callback) {
 				service.getMetrics(loadConfig, callback);
 			}
 		};
 
 		loader = new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
 		store = new ListStore<Metric>(metricAccess.nameKey());
 		chart.setStore(store);
 
 		valueAxis = new NumericAxis<Metric>();
 		valueAxis.setPosition(Position.LEFT);
 		valueAxis.addField(metricAccess.value());
 		TextSprite title = new TextSprite("Value");
 		title.setFontSize(18);
 		valueAxis.setTitleConfig(title);
 		valueAxis.setDisplayGrid(true);
 		valueAxis.setMinimum(0);
 		valueAxis.setMaximum(100);
 		
 		chart.addAxis(valueAxis);
 
 		timeAxis = new TimeAxis<Metric>();
 		timeAxis.setField(metricAccess.date());
 		timeAxis.setStartDate(new Date());
 		timeAxis.setEndDate(new Date());
 		timeAxis.setLabelProvider(new LabelProvider<Date>() {
 			@Override
 			public String getLabel(Date item) {
 				return f.format(item);
 			}
 		});
 		chart.addAxis(timeAxis);
 
 		series = new LineSeries<Metric>();
 		series.setYAxisPosition(Position.LEFT);
 		series.setYField(metricAccess.value());
 		series.setStroke(new RGB(148, 174, 10));
 		series.setShowMarkers(true);
 		series.setMarkerIndex(1);
 		Sprite marker = Primitives.circle(0, 0, 6);
 		marker.setFill(new RGB(148, 174, 10));
 		series.setMarkerConfig(marker);
 		chart.addSeries(series);
 
 		binding = new TimeBasedChartUpdateBinding<MetricQueryDto, Metric, MetricRangeDto>(chart, timeAxis, store);
 		
 		getElement().getStyle().setMargin(10, Unit.PX);
 		setCollapsible(true);
 		setHeadingText("Chart");
 		setPixelSize(620, 500);
 		setBodyBorder(true);
 
 		VerticalLayoutContainer layout = new VerticalLayoutContainer();
 		add(layout);
 
 		chart.setLayoutData(new VerticalLayoutData(1, 1));
 		layout.add(chart);
 	}
 }
