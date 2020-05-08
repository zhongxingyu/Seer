 package cz.cuni.mff.d3s.adapt.bookstore.monitor;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Set;
 
 import cz.cuni.mff.d3s.adapt.bookstore.agent.data.EventLogger;
 import cz.cuni.mff.d3s.adapt.bookstore.agent.data.LoggedEventListener;
 
 import de.erichseifert.gral.data.DataTable;
 import de.erichseifert.gral.io.plots.DrawableWriter;
 import de.erichseifert.gral.io.plots.DrawableWriterFactory;
 import de.erichseifert.gral.plots.XYPlot;
 import de.erichseifert.gral.plots.axes.AxisRenderer;
 import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
 import de.erichseifert.gral.plots.lines.LineRenderer;
 import de.erichseifert.gral.util.Insets2D;
 
 public class Graphs {
 	
 	private static class CountedData {
		private final int WIDTH = 80;
		
		private DataTable data;
 		private long lastTime = 0;
 		private long valueAtLastTime = 0;
 		private Long startValue = null;
 		
		@SuppressWarnings("unchecked")
 		public CountedData(Long start) {
 			startValue = start;
			data = new DataTable(Long.class, Long.class);
 			valueAtLastTime = 0;
			for (long i = -WIDTH; i < 0; i++) {
				data.add(i, (long) 0);
			}
 		}
 		
 		public synchronized void addConst(long time, int theConst) {
 			if (time < lastTime) {
 				return;
 			}
 			if (time != lastTime) {
 				storeLastTime();
 				if (startValue != null) {
 					valueAtLastTime = startValue;
 				}
 			}
 			lastTime = time;
 			valueAtLastTime += theConst;
 		}
 		
 		public synchronized DataTable getData() {
 			return new DataTable(data);
 		}
 		
 		private void storeLastTime() {
 			if (((startValue != null) && (valueAtLastTime == startValue.longValue()))
 					&& (lastTime == 0)) {
 				return;
 			}
 			
 			data.add(lastTime, valueAtLastTime);
			data.remove(0);
 		}
 	}
 	
 	private static class UniversalListener implements LoggedEventListener {
 		private CountedData data;
 		private int constant;
 		
 		public UniversalListener(CountedData data, int constant) {
 			this.data = data;
 			this.constant = constant;
 		}
 		
 		@Override
 		public void action(long timeMillis) {
 			data.addConst(timeMillis / 1000, constant);
 		}
 	}
 	
 	private static class UpdateAllDataListener implements LoggedEventListener {
 		private Set<CountedData> allData = new HashSet<>();
 		
 		public UpdateAllDataListener(Set<CountedData> all) {
 			allData = all;
 		}
 		
 		@Override
 		public void action(long timeMillis) {
 			for (CountedData data : allData) {
 				data.addConst(timeMillis / 1000, 0);
 			}
 		}
 		
 	}
 	
 	private CountedData violations;
 	private CountedData clients;
 
 	public Graphs() {
 		violations = new CountedData(new Long(0));
 		clients = new CountedData(null);
 		
 		EventLogger.addListener("violation", new UniversalListener(violations, 1));
 		EventLogger.addListener("enter", new UniversalListener(clients, 1));
 		EventLogger.addListener("leave", new UniversalListener(clients, -1));
 		
 		Set<CountedData> all = new HashSet<>();
 		all.add(violations);
 		all.add(clients);
 		UpdateAllDataListener prettyGraphs = new UpdateAllDataListener(all);
 		String events[] = {"violation", "enter", "leave", "image"};
 		for (String event : events) {
 			EventLogger.addListener(event, prettyGraphs);
 		}
 	}
 	
 	public InputStream generate(String id, int width, int height) {
 		EventLogger.imageRequested();
 		if (id.equals("violations")) {
 			return generatePlot(violations.getData());
 		} else if (id.equals("all")) {
 			DataTable violationsData = violations.getData();
 			DataTable clientsData = clients.getData();
 			
 			XYPlot plot = new XYPlot(violationsData);
 			
 			setLinesOnly(plot, violationsData, Color.RED);
 			
 			return plotToPngStream(plot, width, height);
 		}
 		
 		return null;
 	}
 	
 	private InputStream plotToPngStream(XYPlot plot, int width, int height) {
 		plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, 20.0);
 		
 		plot.setInsets(new Insets2D.Double(20, 60, 60, 40));
 		
 		DrawableWriterFactory factory = DrawableWriterFactory.getInstance();
 		DrawableWriter writer = factory.get("image/png");
 		
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		try {
 			writer.write(plot, out, width, height);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		ByteArrayInputStream result = new ByteArrayInputStream(out.toByteArray());
 		
 		return result;
 	}
 	
 	public InputStream generatePlot(DataTable data) {
 		XYPlot plot = new XYPlot(data);
 		setLinesOnly(plot, data, Color.BLACK);
 		return plotToPngStream(plot, 1024, 768);
 	}
 	
 	private void setLinesOnly(XYPlot plot, DataTable data, Color color) {
 		LineRenderer lines = new DefaultLineRenderer2D();
 		lines.setSetting(DefaultLineRenderer2D.COLOR, color);
 		
 		plot.setLineRenderer(data, lines);
 		
 		plot.setPointRenderer(data, null);
 	}
 }
