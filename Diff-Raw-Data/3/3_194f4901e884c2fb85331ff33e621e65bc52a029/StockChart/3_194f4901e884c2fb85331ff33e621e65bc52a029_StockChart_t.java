 /*
  *   Copyright 2011 Hauser Olsson GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Package: ch.agent.t2.demo
  * Type: StockChart
  * Version: 1.0.0
  */
 package ch.agent.t2.demo;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.batik.svggen.SVGGeneratorContext;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.SegmentedTimeline;
 import org.jfree.chart.axis.Timeline;
 import org.jfree.chart.encoders.ImageEncoder;
 import org.jfree.chart.encoders.ImageEncoderFactory;
 import org.jfree.chart.plot.CombinedDomainXYPlot;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.title.TextTitle;
 import org.jfree.data.time.RegularTimePeriod;
 import org.jfree.data.time.TimePeriodAnchor;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYDataset;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 
 import ch.agent.t2.time.Adjustment;
 import ch.agent.t2.time.Range;
 import ch.agent.t2.time.TimeDomain;
 import ch.agent.t2.time.TimeDomainManager;
 import ch.agent.t2.time.TimeIndex;
 import ch.agent.t2.timeseries.Observation;
 import ch.agent.t2.timeseries.TimeAddressable;
 import ch.agent.t2.timeseries.TimeSeriesFactory;
 import ch.agent.t2.timeutil.JavaDateUtil;
 
 /**
  * StockChart draws a chart of stock prices and trading volume for the share
 * of a company. The demo uses the JFreeChart library for creating the chart.
  * 
  * @author Jean-Paul Vetterli
  * @version 1.0.0
  */
 public class StockChart {
 
 	/**
 	 * The main program takes the following parameters: 
 	 * <blockquote>
 	 * <pre>
 	 * title=<em>text</em>
 	 * data=<em>name of data file</em>
 	 * splits=<em>name of file with stock splits</em>
 	 * out=<em>name of image file</em>
 	 * timedomain=<em>time domain label</em>
 	 * date1=<em>date in yyyy-mm-dd format</em>
 	 * date2=<em>date in yyyy-mm-dd format</em>
 	 * width=<em>positive number</em>
 	 * height=<em>positive number</em>
 	 * fields=<em>sequence of field offsets for the data file</em>
 	 * </pre>
 	 * </blockquote>
 	 * If is necessary to quote arguments with embedded blanks. White space will
 	 * be trimmed from names and values.
 	 * <p>
 	 * Note. Stock splits are not suppported yet.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			StockChart chart = new StockChart();
 			chart.make(args);
 		} catch (Throwable e) {
 			e.printStackTrace(System.err);
 		}
 	}
 		
 	/**
 	 * Construct a StockChart object. Set up the map for runtime arguments and
 	 * initialize it with valid names and default values.
 	 */
 	public StockChart() throws Exception {
 
 	}
 	
 	/**
 	 * Make the chart. Acceptable command line parameter are
 	 * explicitly defined. This makes the source code an
 	 * easy source of documentation and more up-to-date than
 	 * comments. ;-)
 	 *
 	 * @param args the array of command line parameters
 	 * @throws Exception
 	 */
 	public void make(String[] args) throws Exception {
 
 		/* eliminate time zone and DST effects */
 		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
 		
 		/* define parameters and some default values */
 		CommandParameters param = new CommandParameters(
 			"title=", 
 			"out=", 
 			"data=", 
 			"splits=", 
 			"timedomain=workweek",
 			"date1=", /* format: yyyy-mm-dd */
 			"date2=", 
 			"width=500",
 			"height=240",
 		    "fields=0,4,5");
 
 		/* no comments in the few steps ahead */
 		param.set(true, args);
 		
 		String timeDomainLabel = param.get("timedomain", true);
 		
 		DataSet data = getData(param.get("data", true), 
 				timeDomainLabel, 
 				param.get("date1", false), param.get("date2", false));
 		
 		// TODO: adjust for splits (coming soon, as they say)
 		
 		JFreeChart chart = makeChart(param.get("title", false), data, 
 				timeDomainLabel.equals("workweek"));
 		saveChart(chart, param.get("out", true), 
 				param.getInteger("width"), param.getInteger("height"));
 		
 		// say something
 		System.out.println(param.get("out", true));
 	}
 	
 	/**
 	 * Get the data from a spreadsheet and put into a {@link DataSet}.
 	 * The range of the data to extract from the file can be reduced with
 	 * date1 and date2. If either of them is empty or if date2 is smaller than
 	 * date1, all the data will be extracted.
 	 * 
 	 * @param timeDomainLabel a non-empty {@link TimeDomain} label
 	 * @param date1 the first date of the range 
 	 * @param date2 the last date of the range 
 	 * @param fileName the non-empty name of the data file
 	 * @return a {@link DataSet}
 	 * @throws Exception
 	 */
 	public DataSet getData(String fileName, String timeDomainLabel, String date1, String date2) throws Exception {
 		
 		TimeDomain domain = TimeDomainManager.getFactory().get(timeDomainLabel);
 		
 		Class<? extends RegularTimePeriod> jfcTimeClass = null;
 
 		/* determine JFreeChart type of time from the time domain */
 		switch (domain.getResolution()) {
 		case YEAR:
 			jfcTimeClass = org.jfree.data.time.Year.class;
 			break;
 		case MONTH:
 			jfcTimeClass = org.jfree.data.time.Month.class;
 			break;
 		case DAY:
 			jfcTimeClass = org.jfree.data.time.Day.class;
 			break;
 		case HOUR:
 			jfcTimeClass = org.jfree.data.time.Hour.class;
 			break;
 		case MIN:
 			jfcTimeClass = org.jfree.data.time.Minute.class;
 			break;
 		case SEC:
 			jfcTimeClass = org.jfree.data.time.Second.class;
 			break;
 		case MSEC:
 			jfcTimeClass = org.jfree.data.time.Millisecond.class;
 			break;
 		case USEC:
 			throw new Exception("microseconds not supported in JFreeChart");
 		default:
 			throw new RuntimeException("bug: " + domain.getResolution());
 		}
 		
 		/* get the data from the spreadsheet into time series */
 		CSVFile file = new CSVFile("\\s*,\\s*", 0, 4, 5);
 		
 		DataSet data = new DataSet(domain, jfcTimeClass, date1, date2);
 		file.scan(fileName, data);
 		
 		return data;
 	}
 	
 	/**
 	 * Make a chart with the data.
 	 * 
 	 * @param title the chart title, possibly empty
 	 * @param data a non-null {@link DataSet}
 	 * @param workdaysOnly if true, use a {@link Timeline} to eliminate weekends
 	 * @return a {@link JFreeChart} chart object
 	 * @throws Exception
 	 */
 	public JFreeChart makeChart(String title, DataSet data, boolean workdaysOnly) throws Exception {
 		
 		/* determine the stroke width from the "density" of the data */ 
 		Range dataRange = data.getPrices().getRange().union(data.getVolumes().getRange());
 		float strokeWidth = Math.max(1f, 3f -0.4f * (dataRange.getSize() / 100));
 
 //		int count = data.getPrices().asIndexable().fill(Double.NaN, 0);
 //		System.out.println("range size: " + dataRange.getSize() + " value count: " + (dataRange.getSize() - count));
 		
 		/* use number axis for dates, with special formatter */
     	DateAxis dateAxis = new DateAxis();
     	dateAxis.setDateFormatOverride(new CustomDateFormat("M/d/y"));
     	if (workdaysOnly)
     		dateAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
 
 		/* combined plot with prices in top area and volumes in bottom area share date axis */
     	CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dateAxis);
 
     	/* use a number axis on the left side (default) for the prices */
     	NumberAxis axis = new NumberAxis();
     	axis.setAutoRangeIncludesZero(false);
     	
     	/* plot prices as a line */
 		XYPlot pricePlot = new XYPlot(null, null, axis, null);
 		XYLineAndShapeRenderer priceRenderer = new XYLineAndShapeRenderer();
 		priceRenderer.setDrawSeriesLineAsPath(true);
 		priceRenderer.setSeriesStroke(0, new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
 		priceRenderer.setBaseShapesVisible(false);
 		pricePlot.setRenderer(0, priceRenderer);
 		pricePlot.setDataset(data.getPricesDataset());
 
 		/* use a number axis on the right for the volumes with a special formatter for millions */
 		axis = new NumberAxis();
 		axis.setAutoRangeIncludesZero(false);
 		axis.setNumberFormatOverride(new NumberFormatForMillions());
 		
 		/* plot volumes a bars  */
 		XYPlot volPlot = new XYPlot(null, null, axis, null);
 		volPlot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
 		XYBarRenderer volRenderer = new XYBarRenderer();
 		volPlot.setRenderer(0, volRenderer);
 		volPlot.setDataset(data.getVolumesDataset());
 		
     	/* add the plots, with 3x more weight for the price plot */
 		plot.add(pricePlot, 3);
 		plot.add(volPlot, 1);
 		
 		/* make the chart, remove the legend, set the title */
 		JFreeChart chart = new JFreeChart(plot);
 		chart.removeLegend();
     	chart.setBackgroundPaint(Color.white);
 		chart.setTitle(new TextTitle(title));
 		
 		return chart;
 	}
 	
 	/**
 	 * Save the chart in a file. Currently the image types supported are
 	 * PNG and SVG. They are selected from the file extension.
 	 * 
 	 * @param chart a non-null {@link JFreeChart}
 	 * @param fileName a non-null file name
 	 * @param width a positive number
 	 * @param height a positive number
 	 * @throws Exception
 	 */
 	public void saveChart(JFreeChart chart, String fileName, int width, int height) throws Exception {
 		if (width <= 0 || height <= 0)
 			throw new IllegalArgumentException("width or height not positive");
        	if (fileName.toUpperCase().endsWith(".PNG")) {
        		saveChartAsPNG(chart, fileName, width, height);
        	} else if (fileName.toUpperCase().endsWith(".SVG"))
        		saveChartAsSVG(chart, fileName, width, height);
        	else
 			throw new Exception(String.format("Unknown file type %s; only .png and .svg are supported.", fileName));
 	}
 
 	private void saveChartAsPNG(JFreeChart chart, String fileName, int width, int height) throws Exception {
 		OutputStream out = null;
 		try {
 			out = new BufferedOutputStream(new FileOutputStream(fileName));
 			BufferedImage bufferedImage = chart.createBufferedImage(width, height, null);
 			ImageEncoder imageEncoder = ImageEncoderFactory.newInstance("png");
 			imageEncoder.encode(bufferedImage, out);
 		} catch (Exception e) {
 			throw new Exception(
 					String.format("Failed to save chart into file \"%s\"", fileName), e);
 		} finally {
 			if (out != null)
 				out.close();
 		}
     }
 	
 	private void saveChartAsSVG(JFreeChart chart, String fileName, int width, int height) throws Exception {
 		Writer out = null;
 		try {
 			out = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
 		    String svgNS = "http://www.w3.org/2000/svg";
 			DOMImplementation di = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
 			Document document = di.createDocument(svgNS, "svg", null);
 			
 			SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
 			ctx.setEmbeddedFontsOn(true);
 			SVGGraphics2D svgGenerator = new CustomSVGGraphics2D(ctx, true, 100, true);
 		    svgGenerator.setSVGCanvasSize(new Dimension(width, height));
             chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height));
 		    boolean useCSS = true;
 		    svgGenerator.stream(out, useCSS);
 		    svgGenerator.dispose();
 		} catch (Exception e) {
 			throw new Exception(String.format("Failed to save chart in file %s", fileName));
 		} finally {
 			if (out != null)
 				out.close();
 		}
     }
 	
 	
 	/* ======================================================================= */
 	
 	/**
 	 * NumberFormatForMillions formats large numbers so that they take
 	 * less space in tick labels.
 	 */
 	@SuppressWarnings("serial")
 	public class NumberFormatForMillions extends DecimalFormat {
 		@Override
 		public StringBuffer format(double number, StringBuffer result,
 				FieldPosition fieldPosition) {
 			if (number > 1000000) {
 				super.format(number / 1000000, result, fieldPosition);
 				result.append("M");
 				return result;
 			} else
 				return super.format(number, result, fieldPosition);
 		}
 		
 	}
 
 	/* ======================================================================= */
 	
 	/**
 	 * CustomDateFormat avoids repeating identical dates for tick labels.
 	 */
 	@SuppressWarnings("serial")
 	public class CustomDateFormat extends SimpleDateFormat {
 
 		private String previous;
 		
 		public CustomDateFormat(String pattern) {
 			super(pattern);
 		}
 
 		@Override
 		public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
 			StringBuffer result = super.format(date, toAppendTo, pos);
 			if (previous == null || !result.toString().equals(previous)) {
 				previous = result.toString();
 			} else
 				result.setLength(0);
 			return result;
 		}
 		
 	}
 	
 	/* ======================================================================= */
 	
 	/**
 	 * DataSet stores data as Time2 {@link TimeAddressable} series and returns
 	 * it either directly or converted to jFreeChart {@link TimeSeries}.
 	 * <p>
 	 * It implements {@link LineVisitor} and so can be passed to
 	 * {@link CSVFile#scan}.
 	 */
 	public class DataSet implements LineVisitor {
 
 		private TimeDomain domain;
 		private Range wantedRange;
 		private TimeAddressable<Double> prices;
 		private TimeAddressable<Double> volumes;
 		private Constructor<? extends RegularTimePeriod> ccc; //Class jFreeChartTimePeriodClass;
 		private boolean interpolate;
 		
 		/**
 		 * Construct a Dataset. To get all data, specify empty dates.
 		 * 
 		 * @param domain non-null time domain
 		 * @param date1 non-null date giving the start of the range of interest
 		 * @param date2 non-null date giving the end of the range of interest
 		 * @throws Exception
 		 */
 		public DataSet(TimeDomain domain, Class<? extends RegularTimePeriod> jFreeChartTimePeriodClass, String date1, String date2) throws Exception {
 			this.domain = domain;
 			wantedRange = new Range(domain);
 			if (date1 != null && date1.length() > 0 && date2 != null && date2.length() > 0)	
 				wantedRange = new Range(domain, date1, date2, Adjustment.DOWN);
 			if (wantedRange.isEmpty())
 				wantedRange = null;
 			prices = TimeSeriesFactory.make(domain, Double.class);
 			volumes = TimeSeriesFactory.make(domain, Double.class);
 			ccc = jFreeChartTimePeriodClass.getConstructor(Date.class);
 			interpolate = false;
 		}
 
 		@Override
 		public boolean visit(String date, String price, String volume) throws Exception {
 			TimeIndex t = domain.time(date);
 			if (wantedRange != null) {
 				if (t.compareTo(wantedRange.getFirst()) < 0)
 					return true;
 				if (t.compareTo(wantedRange.getLast()) > 0)
 					return false;
 			}
 			prices.put(t, parse(price));
 			volumes.put(t, parse(volume));
 			return false;
 		}
 		
 		public TimeAddressable<Double> getPrices() {
 			return prices;
 		}
 		
 		public TimeAddressable<Double> getVolumes() {
 			return volumes;
 		}
 		
 		public XYDataset getPricesDataset() throws Exception {
 			return getDataset("price", getPricesAsJFCTimeSeries());
 		}
 		
 		public TimeSeries getPricesAsJFCTimeSeries() throws Exception {
 			return convertToJFCTimeSeries("price", getPrices());
 		}
 		
 		public TimeSeries getVolumesAsJFCTimeSeries() throws Exception {
 			return convertToJFCTimeSeries("volume", getVolumes());
 		}
 		
 		public XYDataset getVolumesDataset() throws Exception {
 			return getDataset("volume", getVolumesAsJFCTimeSeries());
 		}
 		
 		private TimeSeries convertToJFCTimeSeries(String name, TimeAddressable<Double> time2TimeSeries) throws Exception {
 			 TimeSeries timeSeries = new TimeSeries(name);
 			for (Observation<Double> obs : time2TimeSeries) {
 				Double value = obs.getValue();
 				if (time2TimeSeries.isMissing(value)) {
 					if (interpolate)
 						continue;
 				}
 				Date date = JavaDateUtil.toJavaDate(obs.getTime());
 				RegularTimePeriod period = asJFCRegularTimePeriod(date);
 				timeSeries.add(period, value);
 			}
 			return timeSeries;
 		}
 		
 		private RegularTimePeriod asJFCRegularTimePeriod(Date date) throws Exception {
 			try {
 				return (RegularTimePeriod) ccc.newInstance(date);
 			} catch (InvocationTargetException e) {
 				throw new Exception(
 						String.format("Construction of RegularTimePeriod object from %s failed", date.toString()), e);
 			}
 		}
 		
 		private XYDataset getDataset(String key, TimeSeries series) throws Exception {
 			TimeSeriesCollection dataset = new TimeSeriesCollection();
 			dataset.setXPosition(TimePeriodAnchor.START); 
 	        dataset.addSeries(series);
 			return dataset;
 		}
 		
 		private Double parse(String number) throws Exception {
 			try {
 				return Double.valueOf(number);
 			} catch (NumberFormatException e) {
 				throw new Exception(e.getMessage());
 			}
 		}
 	}
 	
 	/* ======================================================================= */
 	
 	public class CommandParameters {
 		
 		/**
 		 * Map for parameters. Parameters are managed as name-value pairs. An empty
 		 * value means the parameter has no value. There are no entries with a null
 		 * value.
 		 */
 		private Map<String, String> argMap;
 		
 		/**
 		 * Construct a CommandParameters object. Definitions are passed as an
 		 * array of strings with name and value separated by an equal sign.
 		 * 
 		 * @param definitions
 		 *            non-null definitions
 		 * @throws Exception
 		 */
 		public CommandParameters(String ... definitions) throws Exception {
 			argMap = new HashMap<String, String>();
 			this.set(false, definitions);
 		}
 
 		/**
 		 * Sets a series of parameters as name-value pairs.
 		 * This method can be used to initialize the parameter map and set default values.
 		 * Name-value pairs must be represented using the syntax
 		 * <blockquote>
 		 * <pre>
 		 * name=value
 		 * </pre>
 		 * </blockquote>
 		 * White space is trimmed from the name and from the value. The value can
 		 * contain equal signs. 
 		 * 
 		 * @param mustExist if true, parameters must have been defined already
 		 * @param args an array of strings representing name-value pairs
 		 * @throws Exception
 		 */
 		protected void set(boolean mustExist, String ... args) throws Exception {
 			for (String arg : args) {
 				String[] nv = arg.split("=", 2);
 				if (nv.length == 2) {
 					String name = nv[0].trim();
 					String value = nv[1].trim();
 					if (mustExist)
 						get(name, false);
 					argMap.put(name, value);
 				} else
 					throw new Exception(
 							String.format("Argument \"%s\" not in name=value format", arg));
 			}
 		}
 		
 		/**
 		 * Return the value of the parameter with the given name. Throws an
 		 * exception if there is no parameter with this name.
 		 * 
 		 * @param name
 		 *            a non-null parameter name
 		 * @return the non-null value corresponding to name
 		 * @param nonEmpty if true, the value may not be empty
 		 * @throws Exception
 		 */
 		protected String get(String name, boolean nonEmpty) throws Exception {
 			String value = argMap.get(name);
 			if (value == null)
 				throw new Exception(
 						String.format("Parameter \"%s\" undefined", name));
 			if (nonEmpty && value.length() == 0)
 				throw new Exception(
 						String.format("Value required for parameter \"%s\"", name));
 			return value;
 		}
 
 		protected int getInteger(String name)  throws Exception {
 			try {
 				return Integer.valueOf(get(name, true));
 			} catch (Throwable e) {
 				throw new Exception(
 						String.format("Failed to get value of parameter \"%s\" as an interger", name));
 			}
 		}
 		
 	}
 	
 	/* ======================================================================= */
 	
 	public interface LineVisitor {
 		boolean visit(String date, String price, String volume) throws Exception;
 	}
 	
 	
 	/**
 	 * CSVFile hides the details of spreadsheets saved as text. Such
 	 * spreadsheets are simple text files where the following structure is
 	 * assumed:
 	 * <ul>
 	 * <li>there is a single field separator,
 	 * <li>all lines have the same number of fields,
 	 * <li>the first line contains headings, and each heading is unique
 	 * </ul>
 	 */
 	public class CSVFile {
 		
 		private Pattern fieldSeparator;
 		private int dateField;
 		private int priceField;
 		private int volumeField;
 		
 		/**
 		 * Construct a CSVFile.
 		 * 
 		 * @param fieldSeparator a pattern defining the field separator
 		 * @param dateField index of the date field (first field = 0)
 		 * @param priceField index of the price field 
 		 * @param volumeField index of the volume field 
 		 * @throws Exception
 		 */
 		public CSVFile(String fieldSeparator, int dateField, int priceField, int volumeField) throws Exception {
 			try {
 				this.fieldSeparator = Pattern.compile(fieldSeparator);
 			} catch (PatternSyntaxException e) {
 				throw new Exception(String.format("Error in pattern \"%s\"",
 						fieldSeparator), e);
 			}
 			this.dateField = dateField;
 			this.priceField = priceField;
 			this.volumeField = volumeField;
 		}
 		
 		/**
 		 * Scan an input file.
 		 * 
 		 * @param resource
 		 *            a string identifying a resource or a file
 		 * @throws Exception
 		 */
 		public void scan(String resource, LineVisitor visitor) throws Exception {
 			int lineNr = 0;
 			try {
 				InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
 				if (inputStream == null)
 					inputStream = new FileInputStream(resource);
 				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
 				while (true) {
 					String line = r.readLine();
 					if (line == null)
 						break;
 					lineNr++;
 					if (lineNr > 1) {
 						String[] fields = fieldSeparator.split(line);
 						visitor.visit(fields[dateField], fields[priceField], fields[volumeField]);
 					}
 				}
 				r.close();
 			} catch (Exception e) {
 				if (lineNr > 0)
 					throw new Exception(String.format("Error occurred while reading line %d of file \"%s\"",
 							lineNr, resource), e);
 				else
 					throw new Exception(String.format("Error occurred while accessing resource \"%s\"",
 									resource), e);
 			}
 		}
 	}
 	
 	/* ======================================================================= */
 
 	/**
 	 * CustomSVGGraphics2D is a extension of the Apache Batik
 	 * {@link SVGGraphics2D} driver.
 	 */
 	public class CustomSVGGraphics2D extends SVGGraphics2D {
 
 		private int geomPercentage = 0;
 		private boolean preserveAspectRatio;
 		
 		protected CustomSVGGraphics2D(SVGGeneratorContext generatorCtx, boolean textAsShapes, int geomPercentage, boolean preserveAspectRatio) {
 			super(generatorCtx, textAsShapes);
 			this.geomPercentage = geomPercentage;
 			this.preserveAspectRatio = preserveAspectRatio;
 		}
 
 		@Override
 		public Element getRoot(Element svgRoot) {
 	        svgRoot = domTreeManager.getRoot(svgRoot);
 	        if (svgCanvasSize != null){
 	        	// -make the image scalable in various viewers
 	        	// -preserve the aspect ratio
 	        	if (geomPercentage > 0) {
 	        		svgRoot.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, geomPercentage + "%");
 	        		svgRoot.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, geomPercentage + "%");
 	        	} else {
 	        		svgRoot.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, svgCanvasSize.width + "");
 	        		svgRoot.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, svgCanvasSize.height + "");
 	        	}
 	            svgRoot.setAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE, 
 	            		"0 0 " + svgCanvasSize.width + " " + svgCanvasSize.height);
 	            svgRoot.setAttributeNS(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, 
 	            		preserveAspectRatio ? SVG_MEET_VALUE : SVG_NONE_VALUE);
 	        }
         	return svgRoot;
 		}
 	}
 	
 
 }
