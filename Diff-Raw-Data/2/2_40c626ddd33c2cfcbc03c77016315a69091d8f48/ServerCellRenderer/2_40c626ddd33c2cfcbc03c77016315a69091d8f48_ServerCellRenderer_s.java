 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.LinearGradientPaint;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.painter.MattePainter;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.ui.RectangleInsets;
 
 import be.ac.ua.comp.scarletnebula.core.Server;
 import be.ac.ua.comp.scarletnebula.misc.Utils;
 
 class ServerCellRenderer implements ListCellRenderer
 {
 	private static final long serialVersionUID = 1L;
 	public static HashMap<Server, JPanel> panelMapping = new HashMap<Server, JPanel>();
 
 	ServerCellRenderer()
 	{
 	}
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus)
 	{
 		// Dirty hack: the last item in the serverlist is always a fake server
 		// that when double clicked produces an "add new server" wizard.
 		if (value == null)
 			return getNewServerServer(list, index, isSelected);
 
 		Server server = (Server) value;
 
 		JPanel p = createServerPanel(list, index, isSelected);
 		final Color foreground = getForegroundColor(list, index, isSelected);
 
 		final JLabel label = getServernameComponent(server, foreground);
 		final JLabel tags = getTagComponent(server, foreground);
 		final ChartPanel chartPanel = getChartPanelComponent();
 
 		p.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.insets = new Insets(5, 5, 1, 5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 
 		p.add(label, c);
 		c.insets = new Insets(0, 5, 5, 5);
 		c.gridy = 1;
 		p.add(tags, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1.0;
 		c.gridy = 2;
 		p.add(chartPanel, c);
 
 		return p;
 
 	}
 
 	private ChartPanel getChartPanelComponent()
 	{
 		TimeSeries total = new TimeSeries("Total Memory", Millisecond.class);
 		TimeSeries free = new TimeSeries("Free Memory", Millisecond.class);
 		TimeSeriesCollection dataset = new TimeSeriesCollection();
 		dataset.addSeries(total);
 		dataset.addSeries(free);
 		DateAxis domain = new DateAxis("Time");
 		domain.setVisible(false);
 		NumberAxis range = new NumberAxis("Memory");
 		range.setVisible(false);
 		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
 		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
 		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
 		renderer.setSeriesPaint(0, Color.red);
 		renderer.setSeriesPaint(1, Color.green);
 		renderer.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT,
 				BasicStroke.JOIN_BEVEL));
 		XYPlot plot = new XYPlot(dataset, domain, range, renderer);
 		plot.setBackgroundPaint(Color.lightGray);
 		plot.setDomainGridlinePaint(Color.white);
 		plot.setRangeGridlinePaint(Color.white);
 		plot.setInsets(new RectangleInsets(0, 0, 0, 0));
 		domain.setAutoRange(true);
 		domain.setLowerMargin(0.0);
 		domain.setUpperMargin(0.0);
 		domain.setTickLabelsVisible(true);
 		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 		JFreeChart chart = new JFreeChart(null, new Font("SansSerif",
 				Font.BOLD, 24), plot, false);
 		chart.setBorderVisible(false);
 		chart.setBackgroundPaint(Color.white);
 		ChartPanel chartPanel = new ChartPanel(chart);
 		chartPanel.setBorder(BorderFactory
 				.createBevelBorder(BevelBorder.LOWERED));
 		return chartPanel;
 	}
 
 	private JLabel getServernameComponent(Server server, final Color foreground)
 	{
 		JLabel label = new JLabel(server.getFriendlyName(),
 				getServerIcon(server), SwingConstants.LEFT);
 		label.setOpaque(false);
 
 		label.setForeground(foreground);
 		return label;
 	}
 
 	private JLabel getTagComponent(Server server, final Color foreground)
 	{
 		JLabel tags = new JLabel();
 
 		Font tagFont = new Font(tags.getFont().getName(), Font.PLAIN, 11);
 		tags.setFont(tagFont);
 		tags.setText(Utils.implode(new ArrayList<String>(server.getTags()),
 				", "));
 		tags.setForeground(foreground);
 		return tags;
 	}
 
 	Color getBackgroundColor(JList list, int index, boolean isSelected)
 	{
 		Color background;
 
 		// check if this cell represents the current DnD drop location
 		JList.DropLocation dropLocation = list.getDropLocation();
 		if (dropLocation != null && !dropLocation.isInsert()
 				&& dropLocation.getIndex() == index)
 		{
 			background = Color.RED;
 		}
 		else if (isSelected)
 		{
 			background = UIManager.getColor("Tree.selectionBackground");
 		}
 		else
 		{
 			background = Color.WHITE;
 		}
 
 		return background;
 	}
 
 	Color getForegroundColor(JList list, int index, boolean isSelected)
 	{
 		Color foreground;
 
 		// check if this cell represents the current DnD drop location
 		JList.DropLocation dropLocation = list.getDropLocation();
 		if (dropLocation != null && !dropLocation.isInsert()
 				&& dropLocation.getIndex() == index)
 		{
 			foreground = Color.WHITE;
 		}
 		else if (isSelected)
 		{
 			foreground = UIManager.getColor("Tree.selectionForeground");
 		}
 		else
 		{
 			foreground = Color.BLACK;
 		}
 		return foreground;
 	}
 
 	private JPanel createServerPanel(JList list, int index, boolean isSelected)
 	{
 		JXPanel p = new JXPanel();
 		p.setLayout(new GridBagLayout());
 
 		Color background = Colors.alpha(
 				getBackgroundColor(list, index, isSelected), 0.4f);
 
 		p.setBackground(background);
 		Color color1 = Colors.White.color(0.5f);
 		Color color2 = Colors.Gray.color(0.95f);
 		// Color color2 = Colors.Red.color(0.2f);
 
 		LinearGradientPaint gradientPaint = new LinearGradientPaint(0.0f, 0.0f,
 				250, 500, new float[] { 0.0f, 1.0f }, new Color[] { color1,
 						color2 });
 		MattePainter mattePainter = new MattePainter(gradientPaint, true);
 		p.setBackgroundPainter(mattePainter);
 
 		if (isSelected)
 		{
 			p.setBorder(BorderFactory.createCompoundBorder(
 					BorderFactory.createLineBorder(
 							UIManager.getColor("List.background"), 2),
 					BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
 		}
 		else
 		{
 			p.setBorder(BorderFactory.createCompoundBorder(
 					BorderFactory.createLineBorder(
 							UIManager.getColor("List.background"), 2),
 					BorderFactory.createEtchedBorder()));
 
 		}
 
 		return p;
 	}
 
 	private Component getNewServerServer(JList list, int index,
 			boolean isSelected)
 	{
 		JPanel p = createServerPanel(list, index, isSelected);
 		JLabel label = new JLabel("Start a new server", new ImageIcon(
 				getClass().getResource("/images/add.png")), JLabel.LEFT);
 		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 16));
 		// Border for better horizontal alignment
 		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
 		p.add(label);
 
 		return p;
 	}
 
 	/**
 	 * Returns the icon that represents this server's status.
 	 * 
 	 * @param server
	 * @return
 	 */
 	public ImageIcon getServerIcon(Server server)
 	{
 		String filename = null;
 
 		switch (server.getStatus())
 		{
 			case PAUSED:
 				filename = "/images/paused.png";
 				break;
 			case PENDING:
 				filename = "/images/pending.png";
 				break;
 			case RUNNING: // This needs to be made load-dependent...
 				filename = "/images/running_ok.png";
 				break;
 			case REBOOTING:
 				filename = "/images/restarting.png";
 				break;
 			case STOPPING:
 				filename = "/images/stopping.png";
 				break;
 			case TERMINATED:
 				filename = "/images/terminated.png";
 				break;
 
 		}
 
 		ImageIcon icon = new ImageIcon(getClass().getResource(filename));
 		return icon;
 	}
 
 	public JXPanel onRollOver(JXPanel input)
 	{
 		Color color1 = Colors.White.color(0.5f);
 		Color color2 = Colors.Black.color(0.8f);
 		// Color color2 = Colors.Red.color(0.2f);
 
 		LinearGradientPaint gradientPaint = new LinearGradientPaint(0.0f, 0.0f,
 				250, 500, new float[] { 0.0f, 1.0f }, new Color[] { color1,
 						color2 });
 		MattePainter mattePainter = new MattePainter(gradientPaint, true);
 		input.setBackgroundPainter(mattePainter);
 		return input;
 	}
 }
