 package classviewer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import classviewer.model.CourseModel;
 import classviewer.model.CourseModelListener;
 import classviewer.model.CourseRec;
 import classviewer.model.OffRec;
 import classviewer.model.Status;
 
 /**
  * Clickable calendar of offerings.
  * 
  * @author TK
  */
 public class CalendarFrame extends NamedInternalFrame implements
 		CourseModelListener {
 
 	private DateFormat format = new SimpleDateFormat("MM/dd/yy");
 
 	/** Length of a week in milliseconds */
 	private static final long WEEK_MS = 7 * 24 * 3600 * 1000l;
 
 	/** Pixels per week, width */
 	private static final int WEEK_PX = 70;
 
 	/** Height of a line */
 	private static final int LINE_HGT = 30;
 
 	/** How many weeks back from now to show */
 	private int lookBackWeeks = 10;
 
 	/** Calendar for various computations */
 	private Calendar cal = Calendar.getInstance();
 	/** Start of this week */
 	private Date today;
 	/** Week count */
 	private int maxWeek = 0;
 	/** Row count */
 	private int rowCount = 0;
 
 	/** All block records */
 	private ArrayList<Block> allBlocks = new ArrayList<Block>();
 	private ArrayList<Block> curBlocks = new ArrayList<Block>();
 
 	/** All filters */
 	private ArrayList<BlockFilter> filters = new ArrayList<BlockFilter>();
 
 	private ArrayList<GraphicSelectionListener> selectionListeners = new ArrayList<GraphicSelectionListener>();
 
 	private HeaderPanel headerPanel;
 	private DrawingPanel drawingPanel;
 
 	private Settings settings;
 
 	public CalendarFrame(CourseModel model, Settings settings) {
 		super("Calendar", model);
 		this.settings = settings;
 		model.addListener(this);
 		this.setLayout(new BorderLayout());
 
 		Dimension dim = new Dimension(400, 200);
 		this.setMinimumSize(dim);
 		this.setSize(dim);
 
 		try {
 			this.lookBackWeeks = Integer.parseInt(settings
 					.getString(Settings.LOOK_BACK_WEEKS));
 		} catch (Exception e) {
 			System.err.println("Cannot parse " + Settings.LOOK_BACK_WEEKS
 					+ ": " + e);
 			this.lookBackWeeks = 10;
 		}
 
 		JPanel buttons = new JPanel();
 		this.add(buttons, BorderLayout.SOUTH);
 
 		for (Status stat : Status.getAll()) {
 			BlockFilter filter = new BlockFilter(stat);
 			filters.add(filter);
 			buttons.add(new FilterCheckBox(filter));
 		}
 
 		this.headerPanel = new HeaderPanel();
 		this.add(headerPanel, BorderLayout.NORTH);
 		this.drawingPanel = new DrawingPanel();
 		JScrollPane scroller = new JScrollPane(drawingPanel);
 		this.add(scroller, BorderLayout.CENTER);
 		scroller.getHorizontalScrollBar().addAdjustmentListener(
 				new AdjustmentListener() {
 					@Override
 					public void adjustmentValueChanged(AdjustmentEvent e) {
 						headerPanel.setShift(e.getValue());
 						headerPanel.repaint();
 					}
 				});
 
 		today = toWeekStart(new Date());
 		this.drawingPanel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				processClick(event.getX(), event.getY());
 			}
 		});
 		refreshModel();
 	}
 
 	public void refreshModel() {
 		allBlocks.clear();
 		curBlocks.clear();
 
 		// Create ALL blocks from the model and set boundaries on week counts
 		loadBlocks(courseModel);
 		// Prune and pack. This will set row count
 		packBlocks();
 		// Now update panel size for the row count
 		drawingPanel.updateSize();
 	}
 
 	protected Block getBlockAt(int x, int y) {
 		// Pick row and column
		int row = y / LINE_HGT - 1; // skip the header
 		int col = x / WEEK_PX;
 
 		if (row < 0)
 			return null; // the header
 
 		// Find a block in that location, if any
 		for (Block b : curBlocks)
 			if (b.row == row && b.start <= col && b.start + b.len > col)
 				return b;
 		return null;
 	}
 
 	protected void processClick(int x, int y) {
 		Block block = getBlockAt(x, y);
 		if (block == null)
 			return;
 
 		for (GraphicSelectionListener lnr : selectionListeners)
 			lnr.offeringClicked(block.offering);
 	}
 
 	/** Get all blocks from the model. This should be called once */
 	private void loadBlocks(CourseModel model) {
 		maxWeek = 0;
 		for (CourseRec cr : model.getFilteredCourses())
 			for (OffRec or : cr.getOfferings()) {
 				Date start = or.getStart();
 				if (start == null)
 					continue;
 
 				// Start in weeks
 				int stw = (int) ((toWeekStart(start).getTime() - today
 						.getTime()) / WEEK_MS + lookBackWeeks);
 				int len = or.getDuration() + or.getSpread() - 1;
 				if (stw + len < 1)
 					continue;
 
 				// Create a block
 				Block block = new Block(stw, len, or);
 				allBlocks.add(block);
 				maxWeek = Math.max(maxWeek, stw + len);
 			}
 		// Sort blocks by status
 		Collections.sort(allBlocks, new Comparator<Block>() {
 			@Override
 			public int compare(Block b1, Block b2) {
 				return b1.offering.getStatus().getCalendarOrder()
 						- b2.offering.getStatus().getCalendarOrder();
 			}
 		});
 	}
 
 	/** Prune the set of blocks by filters and pack what's left into rows */
 	private void packBlocks() {
 		// Prune the set of blocks
 		curBlocks = new ArrayList<Block>(allBlocks);
 		for (BlockFilter f : filters)
 			f.filter(curBlocks);
 
 		// Last occupied week in row
 		ArrayList<Integer> lastTaken = new ArrayList<Integer>();
 
 		// Pack remaining blocks into rows
 		for (Block block : curBlocks) {
 			block.row = -1;
 
 			// Find a row for it
 			for (int i = 0; i < lastTaken.size(); i++) {
 				if (lastTaken.get(i) < block.start) {
 					block.row = i;
 					lastTaken.set(i, block.start + block.len);
 					break;
 				}
 			}
 			if (block.row < 0) {
 				// Add new row
 				block.row = lastTaken.size();
 				lastTaken.add(block.start + block.len);
 			}
 		}
 
 		rowCount = lastTaken.size();
 	}
 
 	/** Get the closest Monday to this date. Time part is zeroed */
 	private Date toWeekStart(Date date) {
 		cal.setTime(date);
 		// Want a Monday
 		while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
 			cal.add(Calendar.DAY_OF_WEEK, -1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		return cal.getTime();
 	}
 
 	private class HeaderPanel extends JPanel {
 		private int shift = 0;
 
 		public void paint(Graphics g) {
 			super.paint(g);
 			// Draw header block
 			g.setColor(Color.DARK_GRAY);
 			cal.setTime(today);
 			cal.add(Calendar.WEEK_OF_YEAR, -lookBackWeeks - 1);
 			Date day = cal.getTime();
 			for (int week = 0; week < maxWeek; week++) {
 				if (Math.abs(day.getTime() - today.getTime()) < 24 * 3600000) {
 					g.setColor(settings.getColor("TodayBg"));
 					g.fillRect((week - 1) * WEEK_PX + 1 - shift, 0,
 							WEEK_PX - 1, LINE_HGT);
 					g.setColor(Color.DARK_GRAY);
 				}
 				g.drawLine(week * WEEK_PX - shift, 0, week * WEEK_PX - shift,
 						LINE_HGT);
 				g.drawString(format.format(day), (week - 1) * WEEK_PX + 5
 						- shift, 20);
 				cal.add(Calendar.WEEK_OF_YEAR, 1);
 				day = cal.getTime();
 			}
 		}
 
 		protected void setShift(int shift) {
 			this.shift = shift;
 		}
 
 		protected void updateSize(int width) {
 			Dimension dim = new Dimension(width, LINE_HGT);
 			this.setMinimumSize(dim);
 			this.setPreferredSize(dim);
 		}
 	}
 
 	private class DrawingPanel extends JPanel {
 		public void paint(Graphics g) {
 			super.paint(g);
 			final int h = rowCount * LINE_HGT;
 
 			// Draw header block
 			g.setColor(Color.DARK_GRAY);
 			cal.setTime(today);
 			cal.add(Calendar.WEEK_OF_YEAR, -lookBackWeeks - 1);
 			Date day = cal.getTime();
 			for (int week = 0; week < maxWeek; week++) {
 				if (Math.abs(day.getTime() - today.getTime()) < 24 * 3600000) {
 					g.setColor(settings.getColor("TodayBg"));
 					g.fillRect((week - 1) * WEEK_PX + 1, 0, WEEK_PX - 1, h);
 					g.setColor(Color.DARK_GRAY);
 				}
 				g.drawLine(week * WEEK_PX, 0, week * WEEK_PX, h);
 				cal.add(Calendar.WEEK_OF_YEAR, 1);
 				day = cal.getTime();
 			}
 
 			// Draw the records
 			for (Block b : curBlocks) {
 				int x = b.start * WEEK_PX;
 				int y = b.row * LINE_HGT;
 				Status status = b.offering.getStatus();
 				g.setColor(settings.getColor(status.toString() + "CalBg"));
 				g.fillRect(x + 2, y + 2, b.len * WEEK_PX - 4, LINE_HGT - 4);
 				if (b.start < 0)
 					x = 0;
 				g.setColor(settings.getColor(status.toString() + "CalFg"));
 				g.drawString(b.getLabel(), x + 5, y + LINE_HGT - 10);
 			}
 		}
 
 		protected void updateSize() {
 			int width = maxWeek * WEEK_PX;
 			int height = (rowCount + 1) * LINE_HGT;
 			Dimension dim = new Dimension(width, height);
 			this.setMinimumSize(dim);
 			this.setPreferredSize(dim);
 			headerPanel.updateSize(width);
 			if (getParent() != null)
 				getParent().doLayout();
 		}
 	}
 
 	@Override
 	public void courseStatusChanged(CourseRec course) {
 		packBlocks();
 		drawingPanel.updateSize();
 		repaint();
 	}
 
 	@Override
 	public void modelUpdated() {
 		refreshModel();
 	}
 
 	@Override
 	public void filtersUpdated() {
 		refreshModel();
 	}
 
 	public void addFilter(BlockFilter filter) {
 		filters.add(filter);
 	}
 
 	public void addSelectionListener(GraphicSelectionListener listener) {
 		this.selectionListeners.add(listener);
 	}
 
 	private class FilterCheckBox extends JCheckBox implements ActionListener {
 		BlockFilter filter;
 
 		FilterCheckBox(BlockFilter filter) {
 			super(filter.getStatus().getName(), true);
 			this.filter = filter;
 			this.addActionListener(this);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			filter.setEnabled(!isSelected());
 			// Recompute and redraw
 			courseStatusChanged(null);
 		}
 	}
 
 }
