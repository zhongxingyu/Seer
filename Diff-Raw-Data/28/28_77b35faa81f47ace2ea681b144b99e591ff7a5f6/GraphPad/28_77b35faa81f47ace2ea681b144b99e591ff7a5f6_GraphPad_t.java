 package org.eclipse.iee.sample.graph.pad;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.utils.runtime.console.ConsoleMessageEvent;
 import org.eclipse.iee.editor.core.utils.runtime.console.ConsoleMessager;
 import org.eclipse.iee.editor.core.utils.runtime.console.IConsoleMessageListener;
 import org.eclipse.iee.editor.core.utils.runtime.file.FileMessageEvent;
 import org.eclipse.iee.editor.core.utils.runtime.file.FileMessager;
 import org.eclipse.iee.editor.core.utils.runtime.file.IFileMessageListener;
 import org.eclipse.iee.sample.graph.pad.model.GraphElement;
 import org.eclipse.iee.sample.graph.pad.model.GraphModel;
 import org.eclipse.iee.translator.antlr.translator.JavaTranslator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.Range;
 import org.jfree.data.general.DatasetChangeEvent;
 import org.jfree.data.xy.AbstractXYDataset;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.ui.RectangleInsets;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class GraphPad extends Pad implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private GraphModel model;
 
 	private boolean fIsAdvancedMode;
 
 	private transient GraphModelPresenter graphModelPresenter;
 	private transient XYPlot plot;
 	private transient XYDataset dataset;
 	private Map<Integer, double[][]> results;
 	
 	public GraphPad() {
 		this(new GraphModel());
 	}
 	public GraphPad(GraphModel model) {
 		fIsAdvancedMode = false;
 		this.model = model;
 	}
 
 	private IFileMessageListener fFileMessageListener = new IFileMessageListener() {
 
 		@Override
 		public void messageReceived(FileMessageEvent e) {
 			updateResult(e.getMessage());
 		}
 
 		@Override
 		public String getRequesterID() {
 			return getContainerID();
 		}
 
 	};
 
 	
 
 	
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new FillLayout());
 		GraphComposite graphComposite = new GraphComposite(parent, SWT.NONE);
 
 		JFreeChart chart = createChart();
 		graphComposite.getGraphComposite().setChart(chart);
 
 		initModelView(graphComposite, model);
 
 		FileMessager.getInstance().addFileMessageListener(fFileMessageListener, 
 				getContainer().getContainerManager().getStoragePath());
 	}
 
 	public void initModelView(GraphComposite parent, GraphModel model) {
 		graphModelPresenter = new GraphModelPresenter(this, parent, model);
 	}
 
 	protected GraphPad(String containerID) {
 		super();
 		model = new GraphModel();
 	}
 
 	@Override
 	public Pad copy() {
 		GraphPad newPad = new GraphPad();
 		newPad.results = this.results;
 		newPad.model = this.model.clone();
 		newPad.fIsAdvancedMode = this.fIsAdvancedMode;
 		return newPad;
 	}
 
 	// Save&Load operations, use it for serialization
 
 	public void save() {
 		if (graphModelPresenter != null) {
 			graphModelPresenter.save();
 			if (plot.getDomainAxis().isAutoRange()) {
 				model.setMinX(null);
				model.setMaxX(null);
 			} else {
 				Range range = plot.getDomainAxis().getRange();
 				model.setMinX(range.getLowerBound());
				model.setMaxX(range.getUpperBound());
			}
			if (plot.getRangeAxis().isAutoRange()) {
				model.setMinY(null);
				model.setMaxY(null);
			} else {
				Range range = plot.getRangeAxis().getRange();
				model.setMinY(range.getLowerBound());
				model.setMaxY(range.getUpperBound());
 			}
 			
			
 			processInput(model);
 		}
 	}
 
 	@Override
 	public void unsave() {
 	}
 
 	/**
 	 * Creates the demo chart.
 	 * 
 	 * @return The chart.
 	 */
 	public JFreeChart createChart() {
 
 		XYDataset dataset = createDataset("Series 1");
 
 		JFreeChart chart = ChartFactory.createXYLineChart(null, null,
 				null, dataset, PlotOrientation.HORIZONTAL, false, false, false);
 		
 		chart.setBackgroundPaint(Color.white);
 		chart.setBorderVisible(true);
 		chart.setBorderPaint(Color.BLACK);
 		plot = (XYPlot) chart.getPlot();
 		plot.setOrientation(PlotOrientation.VERTICAL);
 		plot.setBackgroundPaint(Color.lightGray);
 		plot.setDomainGridlinePaint(Color.white);
 		plot.setRangeGridlinePaint(Color.white);
 		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
 		plot.getRangeAxis().setFixedDimension(15.0);
 		XYItemRenderer renderer = plot.getRenderer();
 		renderer.setSeriesPaint(0, Color.black);
 
		if (model.getMaxX() != null && model.getMinX() != null) {
			plot.getDomainAxis().setRange(model.getMinX(), model.getMaxX());
		} else {
			plot.getDomainAxis().setAutoRange(true);
		}
		
		if (model.getMaxY() != null && model.getMinY() != null) {
			plot.getRangeAxis().setRange(model.getMinY(), model.getMaxY());
		} else {
			plot.getRangeAxis().setAutoRange(true);
		}
		
 		return chart;
 	}
 
 	/**
 	 * Creates a sample dataset.
 	 * 
 	 * @param name
 	 *            the dataset name.
 	 * 
 	 * @return The dataset.
 	 */
 	public XYDataset createDataset(String name) {
 		dataset = new AbstractXYDataset() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public Number getY(int arg0, int arg1) {
 				return getResult(arg0)[arg1][1];
 			}
 
 			@Override
 			public Number getX(int arg0, int arg1) {
 				return getResult(arg0)[arg1][0];
 			}
 
 			@Override
 			public int getItemCount(int arg0) {
 				return getResult(arg0).length;
 			}
 
 			@Override
 			public Comparable getSeriesKey(int arg0) {
 				return Integer.valueOf(arg0);
 			}
 
 			@Override
 			public int getSeriesCount() {
 				return model.getElements().size();
 			}
 			
 			private double[][] getResult(int number) {
 				double[][] ds = getResults().get(Integer.valueOf(number));
 				if (ds == null) {
 					return new double[0][];
 				}
 				return ds;
 			}
 			
 		};
 
 		return dataset;
 	}
 
 	public void processInput(GraphModel model) {
 
 		List<GraphElement> elements = model.getElements();
 		List<String> variables = model.getVariables();
 		StringBuilder generatedText = new StringBuilder();
 
 		Range range = plot.getDomainAxis().getRange();
 		double minX = model.getMinX() != null ? model.getMinX() : range.getLowerBound();
 		double maxX = model.getMaxX() != null ? model.getMaxX() : range.getUpperBound();
 		
 		generatedText.append("{");
 		generatedText.append("StringBuilder result").append(" = new StringBuilder();");
 		if (variables.size() > 0) {
 			for (int i = 0; i < elements.size(); i++) {
 				GraphElement graphElement = elements.get(i);
 				String variable;
 				if (i < variables.size()) {
 					variable = variables.get(i);
 				} else {
 					variable = variables.get(variables.size() - 1);
 				}
 				if (variable != null && variable.length() > 0) {
 					String function = graphElement.getfFunction();
 					if (function != null && function.trim().length() > 0) {
 						String translateElement =  JavaTranslator.translate(function,
 								getContainer().getContainerManager().getCompilationUnit(),
 								getContainer().getPosition().getOffset(), getContainerID(),
 								getContainer().getContainerManager().getStoragePath(),
 								FileMessager.getInstance().getRuntimeDirectoryName());
 						generatedText.append("StringBuilder sb").append(i).append(" = new StringBuilder();");
 						generatedText.append("sb").append(i).append(".append(\"{\");");
 						generatedText.append("for (double ").append(variable).append(" = ")
 						.append(minX).append("; ").append(variable).append(" < ")
 						.append(maxX)
 						.append("; ").append(variable).append(" += Math.abs((")
 						.append(minX).append(" - ")
 						.append(maxX).append(") / ")
 						.append(graphElement.getfDomainCardinality())
 						.append(")) {");
 						generatedText.append("double __grpVal = ").append(translateElement)
 						.append(";");
 						generatedText.append("sb").append(i).append(".append(\"{\").append(").append(variable).append(").append(\",\").append(__grpVal).append(\"},\");");
 						generatedText.append("}");
 						generatedText.append("sb").append(i).append(".append(\"}\");");
 						generatedText.append("result.append(\"").append(i).append(": \" + ").append("sb").append(i).append(".toString()").append(");");
 						generatedText.append("result.append(\"\\n\");");
 					}
 				}
 			}
 		}
 		generatedText.append("File file = new File(\"").append(getContainer().getContainerManager().getStoragePath() + "/" + FileMessager.getInstance().getRuntimeDirectoryName() + "/" + getContainerID()).append("\");");
 		generatedText.append("try {");
 		generatedText.append("FileUtils.writeStringToFile(file, result.toString());");
 		generatedText.append("}");
 		generatedText.append("catch(IOException e) {");
 		generatedText.append("e.printStackTrace();");
 		generatedText.append("}");
 		generatedText.append("}");
 
 		Gson gson = new GsonBuilder().create();
 		getContainer().setValue(gson.toJson(model));
 		getContainer().setTextContent(generatedText.toString());
 	}
 
 	public void updateResult(String result) {
 		if (result.isEmpty()) {
 			getResults().clear();
 			return;
 		}
 		String[] items = result.split("\n");
 		for (String string : items) {
 			String[] splited = string.split("\\:");
 			Integer number = Integer.valueOf(splited[0].trim());
 			try {
 				getResults().put(number, parseArray(splited[1].trim()));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				plot.datasetChanged(new DatasetChangeEvent(this, dataset));
 			}
 		});
 	}
 	
 	public Map<Integer, double[][]> getResults() {
 		if (results == null) {
 			results = new HashMap<Integer, double[][]>();
 		}
 		return results;
 	}
 	
 	public double[][] parseArray(String arrayStr) throws IOException {
 		StringReader reader = new StringReader(arrayStr);
 		return parseArray(reader);
 	}
 	
 	public double[][] parseArray(Reader reader) throws IOException {
 		List<double[]> parsed = new ArrayList<double[]>();
 		readChar(reader, '{', true);
 		double[] readPair;
 		while ((readPair = readPair(reader)) != null) {
 			parsed.add(readPair);
 			readChar(reader, ',', false);
 		}
 		readChar(reader, '}', true);
 		double[][] result = new double[parsed.size()][];
 		for (int i = 0; i < parsed.size(); i++) {
 			result[i] = parsed.get(i);
 		}
 		return result;
 	}
 	
 	public double[] readPair(Reader reader) throws IOException {
 		double result[]; 
 		if (readChar(reader, '{', false)) {
 			result = new double[2];
 			result[0] = readDouble(reader);
 			readChar(reader, ',', true);
 			result[1] = readDouble(reader);
 			readChar(reader, '}', true);
 		} else {
 			result = null;
 		}
 		return result;
 	}
 	
 	public double readDouble(Reader reader) throws IOException {
 		
 		StringBuilder str = new StringBuilder();
 		while (true) {
 			reader.mark(1);
 			int i = reader.read();
 			if (i == -1) {
 				break;
 			} 
 			char c = (char) i;
 			if (Character.isDigit(c) || c == '.' || c =='-' || c == 'e' || c == 'E') {
 				str.append(c);
 			} else {
 				reader.reset();
 				break;
 			}
 		}
 		return Double.parseDouble(str.toString());
 	}
 	
 	public boolean readChar(Reader reader, char character, boolean mandatory) throws IOException {
 		while (true) {
 			reader.mark(1);
 			int i = reader.read();
 			if (i == -1) {
 				break;
 			} 
 			char c = (char) i;
 			if (character == c) {
 				return true;
 			} else if (Character.isSpaceChar(c)) {
 				continue;
 			} else if (mandatory) {
 				throw new IllegalArgumentException("Illegal symbol " + c);
 			} else {
 				reader.reset();
 				return false;
 			}
 		}
 		throw new IllegalArgumentException("Stream unexpectedly closed");
 	}
 
 	@Override
 	public void onContainerAttached() {
 	}
 
 	@Override
 	public void activate() {
 	}
 
 	@Override
 	public void updateData(Map<String, String> params, String value) {
 	}
 	
 	@Override
 	public String getType() {
 		return "Graph";
 	}
 
 	@Override
 	public String getTex() {
 		return "";
 	}
 }
