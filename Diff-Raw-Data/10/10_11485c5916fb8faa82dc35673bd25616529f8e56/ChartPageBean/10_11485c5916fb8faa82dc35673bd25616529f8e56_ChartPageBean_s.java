 package org.strangeforest.currencywatch.web;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import javax.faces.bean.*;
 import javax.faces.context.*;
 import javax.faces.model.*;
 import javax.servlet.http.*;
 
 import org.jfree.chart.*;
 import org.jfree.chart.servlet.*;
 import org.slf4j.*;
 import org.strangeforest.currencywatch.core.*;
 import org.strangeforest.currencywatch.ui.*;
 
 import com.finsoft.util.*;
 
 import static com.finsoft.util.Algorithms.*;
 import static java.util.Arrays.*;
 
 @ManagedBean(name = "chartPage")
 @RequestScoped
 public class ChartPageBean {
 
 	@ManagedProperty("#{chartApp}")
 	private ChartAppBean chartApp;
 
 	private CurrencySymbol currency = UIUtil.DEFAULT_CURRENCY;
 	private Period period = UIUtil.DEFAULT_PERIOD;
 	private SeriesQuality quality = UIUtil.DEFAULT_QUALITY;
 	private boolean showBidAsk;
 	private boolean showMovAvg;
 	private boolean showBollBands;
	private int movAvgPeriod = UIUtil.DEFAULT_MOV_AVG_PERIOD;
 
 	private String chartFileName;
 
 	private static final String PAGE_TITLE = "Currency Chart";
 	private static final int CHART_WIDTH  = 1000;
 	private static final int CHART_HEIGHT =  600;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ChartPageBean.class);
 
 	public ChartAppBean getChartApp() {
 		return chartApp;
 	}
 
 	public void setChartApp(ChartAppBean chartApp) {
 		this.chartApp = chartApp;
 	}
 
 	public String getTitle() {
 		return chartFileName == null ? PAGE_TITLE : String.format("%1$s - %2$s", PAGE_TITLE, getCurrencyFullName());
 	}
 
 	public CurrencySymbol getCurrency() {
 		return currency;
 	}
 
 	public void setCurrency(CurrencySymbol currency) {
 		this.currency = currency;
 	}
 
 	public String getCurrencyFullName() {
 		return String.format("%1$s (%2$s)", currency, currency.description());
 	}
 
 	public Period getPeriod() {
 		return period;
 	}
 
 	public void setPeriod(Period period) {
 		this.period = period;
 	}
 
 	public SeriesQuality getQuality() {
 		return quality;
 	}
 
 	public void setQuality(SeriesQuality quality) {
 		this.quality = quality;
 	}
 
 	public boolean isShowBidAsk() {
 		return showBidAsk;
 	}
 
 	public void setShowBidAsk(boolean showBidAsk) {
 		this.showBidAsk = showBidAsk;
 	}
 
 	public boolean isShowMovAvg() {
 		return showMovAvg;
 	}
 
 	public void setShowMovAvg(boolean showMovAvg) {
 		this.showMovAvg = showMovAvg;
 	}
 
 	public boolean isShowBollBands() {
 		return showBollBands;
 	}
 
 	public void setShowBollBands(boolean showBollBands) {
 		this.showBollBands = showBollBands;
 	}
 
	public int getMovAvgPeriod() {
 		return movAvgPeriod;
 	}
 
	public void setMovAvgPeriod(int movAvgPeriod) {
		this.movAvgPeriod = movAvgPeriod;
 	}
 
 	public String getChartFileName() {
 		return chartFileName;
 	}
 
 	public int getChartWidth() {
 		return CHART_WIDTH;
 	}
 
 	public int getChartHeight() {
 		return CHART_HEIGHT;
 	}
 
 	public List<SelectItem> getCurrencies() {
 		return transform(asList(CurrencySymbol.values()), new Transformer<CurrencySymbol, SelectItem>() {
 			@Override public SelectItem transform(CurrencySymbol currency) {
 				String symbol = currency.toString();
 				return new SelectItem(symbol, symbol, currency.description());
 			}
 		});
 	}
 
 	public List<SelectItem> getPeriods() {
 		return transform(asList(Period.values()), new Transformer<Period, SelectItem>() {
 			@Override public SelectItem transform(Period period) {
 				return new SelectItem(period, period.label());
 			}
 		});
 	}
 
 	public List<SelectItem> getQualities() {
 		return transform(asList(SeriesQuality.values()), new Transformer<SeriesQuality, SelectItem>() {
 			@Override public SelectItem transform(SeriesQuality quality) {
 				return new SelectItem(quality, quality.label());
 			}
 		});
 	}
 
 	public List<SelectItem> getMovAvgPeriods() {
 		return transform(asList(UIUtil.MOV_AVG_PERIODS), new Transformer<Integer, SelectItem>() {
 			@Override public SelectItem transform(Integer movAvgPeriod) {
 				return new SelectItem(movAvgPeriod, String.valueOf(movAvgPeriod));
 			}
 		});
 	}
 
 	public String showChart() throws IOException {
 		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
 		CurrencyChart chart = new CurrencyChart();
 		chart.createSeries(currency, showBidAsk, showMovAvg, showBollBands);
 
 		int days = period.days();
 		DateRange dateRange = UIUtil.toDateRange(days);
 		chart.setDateRange(dateRange);
 
 		CurrencyRate currencyRate = new CurrencyRate(UIUtil.BASE_CURRENCY, currency.name(), chartApp.getProvider());
 		Map<Date, RateValue> rates;
 		try {
 			int step = 1 + days/quality.points();
 			currencyRate.getRates(dateRange.dates(step*10)); // Fetch outline first
 			rates = currencyRate.getRates(dateRange.dates(step));
 		}
 		catch (CurrencyRateException ex) {
 			LOGGER.error("Error getting currency data.", ex);
 			rates = currencyRate.getRates();
 		}
 
 		chart.updateBaseSeries(rates);
 		chart.updateDerivedSeries(movAvgPeriod);
 		JFreeChart jFreeChart = chart.getChart();
 		jFreeChart.setBackgroundPaint(Color.WHITE);
 		chartFileName = ServletUtilities.saveChartAsPNG(jFreeChart, CHART_WIDTH, CHART_HEIGHT, session);
 
 		return "currency-chart.xhtml";
 	}
 }
