 /*******************************************************************************
  * Bizcal is a component library for calendar widgets written in java using swing.
  * Copyright (C) 2007  Frederik Bertilsson 
  * Contributors:       Martin Heinemann martin.heinemann(at)tudor.lu
  * 
  * http://sourceforge.net/projects/bizcal/
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  * 
  * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
  * in the United States and other countries.]
  * 
  *******************************************************************************/
 package bizcal.swing;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.LayoutManager;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import bizcal.common.DayViewConfig;
 import bizcal.common.Event;
 import bizcal.swing.util.FrameArea;
 import bizcal.util.BizcalException;
 import bizcal.util.DateInterval;
 import bizcal.util.DateUtil;
 import bizcal.util.Interval;
 import bizcal.util.TimeOfDay;
 import bizcal.util.Tuple;
 
 public class DayView extends CalendarView {
 	public static int PIXELS_PER_HOUR = 80;
 
 	private static final int CAPTION_ROW_HEIGHT0 = 20;
 
 	public static final int PREFERRED_DAY_WIDTH = 10;
 
 	public static final Integer GRID_LEVEL = new Integer(1);
 
 	private List<List<FrameArea>> frameAreaCols = new ArrayList<List<FrameArea>>();
 
 	private List<List<Event>> eventColList = new ArrayList<List<Event>>();
 
 	private List<Date> _dateList = new ArrayList<Date>();
 
 	private Map<Tuple, JLabel> timeLines = new HashMap<Tuple, JLabel>();
 
 	private Map hourLabels = new HashMap();
 
 	private Map minuteLabels = new HashMap();
 
 	private List<JLabel> vLines = new ArrayList<JLabel>();
 
 	private List<JPanel> calBackgrounds = new ArrayList<JPanel>();
 
 	private ColumnHeaderPanel columnHeader;
 
 	private TimeLabelPanel rowHeader;
 
 	private int dayCount;
 
 	private JScrollPane scrollPane;
 
 	private JLayeredPane calPanel;
 
 	private boolean firstRefresh = true;
 
 	private DayViewConfig config;
 
 	private List<JLabel> dateFooters = new ArrayList<JLabel>();
 
 
 
 	/**
 	 * @param desc
 	 * @throws Exception
 	 */
 	public DayView(DayViewConfig desc) throws Exception  {
 		this(desc, null);
 	}
 
 
 	/**
 	 * @param desc
 	 * @param upperLeftCornerComponent component that is displayed in the upper left corner of the scrollpaine
 	 * @throws Exception
 	 */
 	public DayView(DayViewConfig desc, Component upperLeftCornerComponent) throws Exception {
 		super(desc);
 		this.config = desc;
 		calPanel = new JLayeredPane();
 		calPanel.setLayout(new Layout());
 		ThisMouseListener mouseListener = new ThisMouseListener();
 		ThisKeyListener keyListener = new ThisKeyListener();
 		calPanel.addMouseListener(mouseListener);
 		calPanel.addMouseMotionListener(mouseListener);
 		calPanel.addKeyListener(keyListener);
 		// calPanel.setPreferredSize(new
 		// Dimension(calPanel.getPreferredSize().width,
 		// calPanel.getPreferredSize().height+200));
 		scrollPane = new JScrollPane(calPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setCursor(Cursor.getDefaultCursor());
 		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
 
 		/* ------------------------------------------------------- */
 		if (upperLeftCornerComponent == null) {
 			/* ------------------------------------------------------- */
 			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, createCorner(true,
 					true));
 			/* ------------------------------------------------------- */
 		} else {
 			/* ------------------------------------------------------- */
 			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, upperLeftCornerComponent);
 			/* ------------------------------------------------------- */
 		}
 
 		scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, createCorner(true,
 				false));
 		scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCorner(
 				false, true));
 		columnHeader = new ColumnHeaderPanel(desc);
 		columnHeader.setShowExtraDateHeaders(desc.isShowExtraDateHeaders());
 		scrollPane.setColumnHeaderView(columnHeader.getComponent());
 		/* ------------------------------------------------------- */
 		// set the time label at the left side
 		rowHeader = new TimeLabelPanel(desc, new TimeOfDay(this.config.getDayStartHour(), 0),
 				new TimeOfDay(this.config.getDayEndHour(), 0));
 		/* ------------------------------------------------------- */
 		rowHeader.setFooterHeight(getFooterHeight());
 		scrollPane.setRowHeaderView(rowHeader.getComponent());
 
 		// scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(),
 		// scrollPane.getHeight()+400));
 
 //		calPanel.addComponentListener(new ComponentAdapter() {
 //			@Override
 //			public void componentResized(ComponentEvent e) {
 //				/* ====================================================== */
 //				try {
 //					// DayView.this.refresh();
 //					// DayView.this.refresh0();
 //				} catch (Exception e1) {
 //					e1.printStackTrace();
 //				}
 //				/* ====================================================== */
 //			}
 //		});
 
 	}
 
 	public void refresh0() throws Exception {
		if (calPanel == null || this.getModel() == null)
 			return;
 
 		dayCount = (int) (getModel().getInterval().getDuration() / (24 * 3600 * 1000));
 		calPanel.removeAll();
 		calPanel.setBackground(Color.WHITE);
 		rowHeader.setStartEnd(new TimeOfDay(this.config.getDayStartHour(), 0),
 				new TimeOfDay(this.config.getDayEndHour(), 0));
 		rowHeader.setFooterHeight(getFooterHeight());
 		rowHeader.getComponent().revalidate();
 
 
 		frameAreaCols.clear();
 		eventColList.clear();
 		timeLines.clear();
 		hourLabels.clear();
 		minuteLabels.clear();
 		calBackgrounds.clear();
 		vLines.clear();
 		dateFooters.clear();
 
 		addDraggingComponents(calPanel);
 
 		Font hourFont = getDesc().getFont().deriveFont((float) 12);
 		hourFont = hourFont.deriveFont(Font.BOLD);
 
 		// Steps through the time axis and adds hour labels, minute labels
 		// and timelines in different maps.
 		// key: date, value: label
 		long pos = getFirstInterval().getStartDate().getTime();
 		while (pos < getFirstInterval().getEndDate().getTime()) {
 			Date date = new Date(pos);
 
 
 			int timeSlots = this.config.getNumberOfTimeSlots();
 			// do not print more than 6 minute time slots (every 10'')
 			if (PIXELS_PER_HOUR > 120)
 				timeSlots = 6;
 
 			if (timeSlots > 10)
 				timeSlots = 10;
 			/* ------------------------------------------------------- */
 			// create a horizontal line for each time slot
 			Color color = getDesc().getLineColor();
 			Color hlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(),getDesc().getGridAlpha());
 			for (int i = 1; i <= timeSlots; i++) {
 				/* ------------------------------------------------------- */
 				JLabel line = new JLabel();
 				line.setOpaque(true);
 				line.setBackground(hlineColor);
 				calPanel.add(line, GRID_LEVEL);
 				timeLines.put(new Tuple(date, ""+(60/timeSlots)*i), line);
 				addHorizontalLine(line);
 				/* ------------------------------------------------------- */
 			}
 //			// Hour line
 //			JLabel line = new JLabel();
 //			line.setBackground(getDesc().getLineColor());
 //			line.setOpaque(true);
 //			calPanel.add(line, GRID_LEVEL);
 //			timeLines.put(new Tuple(date, "00"), line);
 //			addHorizontalLine(line);
 //			/* ------------------------------------------------------- */
 ////			// Quarter hour line
 ////			// ev l�gga denna loop efter att vi placerat ut aktiviteterna
 ////			// d� kommer den hamna l�ngst bak men �nd� synas
 ////			line = new JLabel();
 ////			line.setBackground(getDesc().getLineColor());
 ////			line.setOpaque(true);
 ////			calPanel.add(line, GRID_LEVEL);
 ////			timeLines.put(new Tuple(date, "15"), line);
 ////			addHorizontalLine(line);
 //			/* ------------------------------------------------------- */
 //			// Half hour line
 //			// ev l�gga denna loop efter att vi placerat ut aktiviteterna
 //			// d� kommer den hamna l�ngst bak men �nd� synas
 //			line = new JLabel();
 //			line.setBackground(getDesc().getLineColor());
 //			line.setOpaque(true);
 //			calPanel.add(line, GRID_LEVEL);
 //			timeLines.put(new Tuple(date, "30"), line);
 //			addHorizontalLine(line);
 
 			//
 //			// 3*Quarter hour line
 //			// ev l�gga denna loop efter att vi placerat ut aktiviteterna
 //			// d� kommer den hamna l�ngst bak men �nd� synas
 //			line = new JLabel();
 //			line.setBackground(getDesc().getLineColor());
 //			line.setOpaque(true);
 //			calPanel.add(line, GRID_LEVEL);
 //			timeLines.put(new Tuple(date, "45"), line);
 //			addHorizontalLine(line);
 
 			pos += 3600 * 1000;
 		}
 		if (config.isShowDateFooter()) {
 			JLabel line = new JLabel();
 			line.setBackground(getDesc().getLineColor());
 			line.setOpaque(true);
 			calPanel.add(line, GRID_LEVEL);
 			timeLines.put(new Tuple(new Date(pos), "00"), line);
 		}
 
 		createColumns();
 
 		Iterator i = getSelectedCalendars().iterator();
 		while (i.hasNext()) {
 			bizcal.common.Calendar cal = (bizcal.common.Calendar) i.next();
 			JPanel calBackground = new JPanel();
 			calBackground.setBackground(cal.getColor());
 			calBackgrounds.add(calBackground);
 			calPanel.add(calBackground);
 		}
 
 		columnHeader.setModel(getModel());
 		columnHeader.setPopupMenuCallback(popupMenuCallback);
 		columnHeader.refresh();
 
 		if (firstRefresh)
 			initScroll();
 		firstRefresh = false;
 
 
 
 
 		calPanel.validate();
 		calPanel.repaint();
 //		scrollPane.setPreferredSize(new DiSmension(calPanel.getPreferredSize().width, rowHeader.getComponent().getPreferredSize().height));
 		/* ------------------------------------------------------- */
 		// put the timelines in the background
 		for (JLabel l : timeLines.values()) {
 			calPanel.setComponentZOrder(l, calPanel.getComponents().length-2);
 		}
 		/* ------------------------------------------------------- */
 
 		scrollPane.validate();
 		scrollPane.repaint();
 
 
 		rowHeader.getComponent().updateUI();
 		// Hack to make to init scroll work
 		// JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
 		// scrollBar.setValue(scrollBar.getValue()-1);
 
 	}
 
 	private int getColCount() throws Exception {
 		return dayCount * getSelectedCalendars().size();
 	}
 
 	/**
 	 * Returns the first interval to show. Start day plus one.
 	 *
 	 * @return
 	 * @throws Exception
 	 */
 	private DateInterval getFirstInterval() throws Exception {
 		Date start = getInterval().getStartDate();
 //		Date end = DateUtil.getDiffDay(start, +1);
 
 		return new DateInterval(
 				DateUtil.round2Hour(start, this.config.getDayStartHour()),
 				DateUtil.round2Hour(start, this.config.getDayEndHour()));
 	}
 
 	/**
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	private void createColumns() throws Exception {
 		DateInterval interval = getFirstInterval();
 		int cols = getColCount();
 
 
 		frameAreaHash.clear();
 		List events = null;
 		DateInterval interval2 = null;
 		/* ------------------------------------------------------- */
 		// iterate over all columns
 		for (int it = 0; it < cols; it++) {
 			/* ------------------------------------------------------- */
 			int iCal = it / dayCount;
 			bizcal.common.Calendar cal = (bizcal.common.Calendar) getSelectedCalendars()
 					.get(iCal);
 			Object calId = cal.getId();
 			// obtain all events for the calendar
 			events = broker.getEvents(calId);
 			Collections.sort(events);
 
 			if (it % dayCount == 0)
 				interval2 = new DateInterval(interval);
 
 			_dateList.add(interval2.getStartDate());
 
 			Calendar startdate = DateUtil.newCalendar();
 			startdate.setTime(interval2.getStartDate());
 			/* ------------------------------------------------------- */
 			// create vertical lines
 			Color vlColor = getDesc().getLineColor();
 			int vlAlpha = getDesc().getGridAlpha()+50;
 			if (vlAlpha > 255)
 				vlAlpha = 255;
 			/* ------------------------------------------------------- */
 			Color vlAlphaColor = new Color(vlColor.getRed(), vlColor.getGreen(), vlColor.getBlue(), vlAlpha);
 			/* ------------------------------------------------------- */
 			if (it > 0) {
 				/* ------------------------------------------------------- */
 				JLabel verticalLine = new JLabel();
 				verticalLine.setOpaque(true);
 				verticalLine.setBackground(vlAlphaColor);
 //				verticalLine.setBackground(getDesc().getLineColor());
 
 				if (startdate.get(Calendar.DAY_OF_WEEK) == startdate
 						.getFirstDayOfWeek())
 					verticalLine.setBackground(getDescriptor().getLineColor2());
 				if (getSelectedCalendars().size() > 1 && it % dayCount == 0)
 					verticalLine.setBackground(getDescriptor().getLineColor3());
 				calPanel.add(verticalLine, GRID_LEVEL);
 				vLines.add(verticalLine);
 				/* ------------------------------------------------------- */
 			}
 			/* ------------------------------------------------------- */
 			List<FrameArea> frameAreas = new ArrayList<FrameArea>();
 			// l�gger till en framearea-lista f�r varje dag
 			frameAreaCols.add(frameAreas);
 			// f�r alla event f�r personen inom intervallet
 			if (calId == null)
 				continue;
 			Interval currDayInterval = getInterval(it % dayCount);
 			List<Event> colEvents = new ArrayList<Event>();
 			eventColList.add(colEvents);
 			int iEvent = 0;
 			if (events == null)
 				events = new ArrayList();
 			Iterator j = events.iterator();
 
 			while (j.hasNext()) {
 				Event event = (Event) j.next();
 				DateInterval eventInterv = new DateInterval(event.getStart(),
 						event.getEnd());
 				if (!currDayInterval.overlap(eventInterv))
 					continue;
 
 				// if there are overlapping events
 				FrameArea area = createFrameArea(calId, event);
 
 				area.setBackground(config.getPrimaryColor());
 
 				frameAreas.add(area);
 				colEvents.add(event);
 				calPanel.add(area, new Integer(event.getLevel()));
 				iEvent++;
 
 				/* ------------------------------------------------------- */
 				if (!frameAreaHash.containsKey(event))
 					frameAreaHash.put(event, area);
 				else {
 					frameAreaHash.get(event).addChild(area);
 
 				}
 
 			}
 
 			if (config.isShowDateFooter()) {
 				JLabel footer = new JLabel(broker.getDateFooter(cal.getId(),
 						interval2.getStartDate(), colEvents));
 				footer.setHorizontalAlignment(JLabel.CENTER);
 				dateFooters.add(footer);
 				calPanel.add(footer);
 			}
 
 			if (dayCount > 1)
 				interval2 = incDay(interval2);
 		}
 
 	}
 
 	// F�r in ett events start- eller slutdatum, h�jden p� f�nstret samt
 	// intervallet som positionen ska ber�knas utifr�n
 	private int getYPos(Date aDate, int dayNo) throws Exception {
 		long time = aDate.getTime();
 		return getYPos(time, dayNo);
 	}
 
 	private int getYPos(long time, int dayNo) throws Exception {
 		DateInterval interval = getInterval(dayNo);
 		time -= interval.getStartDate().getTime();
 
 		double viewPortHeight = getHeight() - getCaptionRowHeight()
 				- getFooterHeight();
 		// double timeSpan = (double) getTimeSpan();
 //		double timeSpan = 24 * 3600 * 1000;
 		double timeSpan = this.config.getHours() * 3600 * 1000;
 
 		double dblTime = time;
 		int ypos = (int) (dblTime / timeSpan * viewPortHeight);
 		ypos += getCaptionRowHeight();
 		return ypos;
 	}
 
 	/*
 	 * private long getTimeSpan() throws Exception { return
 	 * getDesc().getViewEndTime().getValue() -
 	 * getDesc().getViewStartTime().getValue(); }
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see bizcal.swing.CalendarView#getDate(int, int)
 	 */
 	protected Date getDate(int xPos, int yPos) throws Exception {
 		int colNo = getColumn(xPos);
 		int dayNo = 0;
 		if (dayCount != 0)
 			dayNo = colNo % dayCount;
 
 		DateInterval interval = getInterval(dayNo);
 		yPos -= getCaptionRowHeight();
 		double ratio = ((double) yPos) / ((double) getTimeHeight());
 		long time = (long) (interval.getDuration() * ratio);
 		time += interval.getStartDate().getTime();
 
 		return new Date(time);
 	}
 
 	private DateInterval getInterval(int dayNo) throws Exception {
 		DateInterval interval = getFirstInterval();
 		for (int i = 0; i < dayNo; i++)
 			interval = incDay(interval);
 		return interval;
 	}
 
 	private int getColumn(int xPos) throws Exception {
 		xPos -= getXOffset();
 		int width = getWidth() - getXOffset();
 		double ratio = ((double) xPos) / ((double) width);
 		return (int) (ratio * getColCount());
 	}
 
 	private Object getCalendarId(int colNo) throws Exception {
 		int pos = 0;
 		// dayCount = 1;
 		if (dayCount != 0)
 			pos = colNo / dayCount;
 		bizcal.common.Calendar cal = (bizcal.common.Calendar) getSelectedCalendars()
 				.get(pos);
 		return cal.getId();
 	}
 
 	protected int getXOffset() {
 		// return LABEL_COL_WIDTH;
 		return 0;
 	}
 
 	private int getXPos(int colno) throws Exception {
 		double x = getWidth();
 		x = x - getXOffset();
 		double ratio = ((double) colno) / ((double) getColCount());
 		return ((int) (x * ratio)) + getXOffset();
 		/*
 		 * BigDecimal xPos = new BigDecimal((x * ratio) + getXOffset()); return
 		 * xPos.setScale(0,BigDecimal.ROUND_CEILING).intValue();
 		 */
 	}
 
 	private int getWidth() {
 		return calPanel.getWidth();
 	}
 
 	private int getHeight() {
 		return calPanel.getHeight();
 	}
 
 	private int getTimeHeight() throws Exception {
 		return getHeight() - getCaptionRowHeight() - getFooterHeight();
 	}
 
 	private int getFooterHeight() {
 		if (config.isShowDateFooter())
 			return PIXELS_PER_HOUR / 2;
 		return 0;
 	}
 
 	/**
 	 *
 	 * 05.06.2007 11:31:56
 	 *
 	 *
 	 * @version <br>
 	 *          $Log: DayView.java,v $
	 *          Revision 1.29  2008/01/21 14:13:55  heine_
	 *          fixed nullpointer problem when refreshing without a model.
	 *          The refresh method just returns in case of this
	 *
	 *          Revision 1.24  2008-01-21 14:06:11  heinemann
	 *          fixed nullpointer problem when refreshing without a model.
	 *          The refresh method just returns in case of this.
 	 *
 	 *          Revision 1.23  2007-09-18 12:39:57  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.22  2007/07/09 07:30:08  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.21  2007/07/09 07:16:47  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.20  2007/06/20 12:08:08  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.19  2007/06/18 11:41:32  heinemann
 	 *          bug fixes and alpha optimations
 	 *
 	 *          Revision 1.18  2007/06/15 07:00:38  hermen
 	 *          changed translatrix keys
 	 *
 	 *          Revision 1.17  2007/06/14 13:31:25  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.16  2007/06/12 11:58:03  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.15  2007/06/11 13:23:39  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.14  2007/06/08 12:21:10  heinemann
 	 *          *** empty log message ***
 	 *
 	 *          Revision 1.13  2007/06/07 12:12:50  heinemann
 	 *          Events that lasts longer than a day and have at least one overlapping, will now have the same width for all FrameAreas in the columns
 	 * <br>
 	 *          Revision 1.12 2007/06/06 11:23:01 heinemann <br>
 	 *          *** empty log message *** <br>
 	 *
 	 */
 	private class Layout implements LayoutManager {
 		public void addLayoutComponent(String name, Component comp) {
 		}
 
 		public void removeLayoutComponent(Component comp) {
 		}
 
 		public Dimension preferredLayoutSize(Container parent) {
 			try {
 				int width = dayCount * getModel().getSelectedCalendars().size()
 						* PREFERRED_DAY_WIDTH;
 				return new Dimension(width, getPreferredHeight());
 			} catch (Exception e) {
 				throw BizcalException.create(e);
 			}
 		}
 
 		public Dimension minimumLayoutSize(Container parent) {
 			return new Dimension(50, 100);
 		}
 
 		@SuppressWarnings("unchecked")
 		public void layoutContainer(Container parent0) {
 			try {
 				DayView.this.resetVerticalLines();
 				int width = getWidth();
 				int height = getHeight();
 				DateInterval day = getFirstInterval();
 
 				int numberOfCols = getColCount();
 				if (numberOfCols == 0)
 					numberOfCols = 1;
 
 
 				for (int i = 0; i < eventColList.size(); i++) {
 					int dayNo = i % dayCount;
 					int xpos = getXPos(i);
 					int captionYOffset = getCaptionRowHeight()
 							- CAPTION_ROW_HEIGHT0;
 					int colWidth = getXPos(i + 1) - getXPos(i);
 					// Obs. tempor�r l�sning med korrigering med +2. L�gg till
 					// korrigeringen p� r�tt st�lle
 					// kan h�ra ihop synkning av tidsaxel och muslyssnare
 					int vLineTop = captionYOffset + CAPTION_ROW_HEIGHT0 + 2;
 					if (dayNo == 0 && (getSelectedCalendars().size() > 1)) {
 						vLineTop = 0;
 						day = getFirstInterval();
 					}
 
 					Calendar startinterv = Calendar.getInstance(Locale
 							.getDefault());
 					startinterv.setTime(day.getStartDate());
 
 					if (i > 0) {
 						JLabel verticalLine = (JLabel) vLines.get(i-1);
 						int vLineHeight = height - vLineTop;
 						verticalLine.setBounds(xpos, vLineTop, 1, vLineHeight);
 						// add the line position to the list
 						addVerticalLine(verticalLine);
 					}
 
 					if (config.isShowDateFooter()) {
 						JLabel dayFooter = (JLabel) dateFooters.get(i);
 						dayFooter.setBounds(xpos, getTimeHeight(), colWidth, getFooterHeight());
 					}
 
 					DateInterval currIntervall = getInterval(dayNo);
 					FrameArea prevArea = null;
 					int overlapCol = 0;
 					int overlapColCount = 0;
 					// ======================================================
 					// eventColList contains a list of ArrayLists that holds the
 					// events per day
 					// the same with the frameAreaCols
 					// =======================================================
 					List events = (List) eventColList.get(i);
 					List<FrameArea> areas = frameAreaCols.get(i);
 					/* ------------------------------------------------------- */
 					int overlapCols[] = new int[events.size()];
 					for (int j = 0; j < events.size(); j++) {
 						/* ------------------------------------------------------- */
 						FrameArea area = (FrameArea) areas.get(j);
 						Event event = (Event) events.get(j);
 						// adapt the FrameArea according the appropriate event
 						// data
 						Date startTime = event.getStart();
 						if (startTime.before(currIntervall.getStartDate()))
 							startTime = currIntervall.getStartDate();
 						/* ------------------------------------------------------- */
 						Date endTime = event.getEnd();
 						// if the events lasts longer than the current day, set
 						// 23:59 as end
 						if (endTime.after(currIntervall.getEndDate()))
 							endTime = currIntervall.getEndDate();
 						/* ------------------------------------------------------- */
 						int y1 = getYPos(startTime, dayNo);
 						if (y1 < getCaptionRowHeight())
 							y1 = getCaptionRowHeight();
 
 						int y2 = getYPos(endTime, dayNo);
 						int dy = y2 - y1;
 						int x1 = xpos;
 						area.setBounds(x1, y1, colWidth, dy);
 						/* ------------------------------------------------------- */
 						// Overlap logic
 						if (!event.isBackground()) {
 							/* ------------------------------------------------------- */
 							if (prevArea != null) {
 								Rectangle r = prevArea.getBounds();
 								int prevY2 = r.y + r.height;
 								if (prevY2 > y1) {
 									// Previous event overlap
 									overlapCol++;
 									if (prevY2 < y2) {
 										// This events finish later than
 										// previous
 										prevArea = area;
 									}
 								} else {
 									overlapCol = 0;
 									prevArea = area;
 								}
 							}  else
 								prevArea = area;
 							overlapCols[j] = overlapCol;
 							if (overlapCol > overlapColCount)
 								overlapColCount = overlapCol;
 							/* ------------------------------------------------------- */
 						} else
 							overlapCols[j] = 0;
 						}
 						// Overlap logic. Loop the events/frameareas a second
 						// time and set the xpos and widths
 						if (overlapColCount > 0) {
 							/* ------------------------------------------------------- */
 							int currWidth = colWidth;
 							for (int j = 0; j < areas.size(); j++) {
 								/* ------------------------------------------------------- */
 								Event event = (Event) events.get(j);
 								/* ------------------------------------------------------- */
 								if (event.isBackground())
 									continue;
 								/* ------------------------------------------------------- */
 								FrameArea area = (FrameArea) areas.get(j);
 								int index = overlapCols[j];
 								if (index == 0)
 									currWidth = colWidth;
 								/* ------------------------------------------------------- */
 								try {
 									/* ------------------------------------------------------- */
 									if ( overlapCols[j+1] > 0){
 										// find greates in line
 										int curr = index;
 										for (int a = j+1; a < areas.size();a++) {
 											/* ------------------------------------------------------- */
 											if (overlapCols[a] == 0)
 												break;
 											if (overlapCols[a] > curr)
 												curr = overlapCols[a];
 											/* ------------------------------------------------------- */
 										}
 										currWidth = colWidth / (curr+1);
 									}
 								} catch (Exception e) {}
 								/* ------------------------------------------------------- */
 								Rectangle r = area.getBounds();
 								area.setBounds(r.x + index*currWidth, r.y, currWidth, r.height);
 							}
 						}
 					}
 
 				// Loop the frameareas a third time
 				// and set areas that belong to an event to the same width
 				for (List<FrameArea> fAreas : frameAreaCols) {
 					/* ------------------------------------------------------- */
 					if (fAreas != null)
 						for (FrameArea fa : fAreas) {
 							/* ------------------------------------------------------- */
 							int sw = findSmallestFrameArea(fa);
 							int baseFAWidth;
 							try {
 								baseFAWidth = getBaseFrameArea(fa.getEvent()).getBounds().width;
 							} catch (Exception e) {
 								continue;
 							}
 							if (sw > baseFAWidth) {
 								sw = baseFAWidth;
 							}
 							fa.setBounds(fa.getBounds().x, fa.getBounds().y,
 									sw,
 									fa.getBounds().height);
 
 							// ensure, that the background events are really painted in the background!
 							if (fa.getEvent().isBackground())
 								calPanel.setComponentZOrder(fa, calPanel.getComponents().length-5);
 							/* ------------------------------------------------------- */
 						}
 					/* ------------------------------------------------------- */
 				}
 
 
 					// old obsolete
 // // Overlap logic. Loop the events/frameareas a second
 // // time and set the xpos and widths
 // if (overlapColCount > 0) {
 // int slotWidth = colWidth / (overlapColCount+1);
 // for (int j = 0; j < areas.size(); j++) {
 // Event event = (Event) events.get(j);
 // if (event.isBackground())
 // continue;
 // FrameArea area = (FrameArea) areas.get(j);
 // int index = overlapCols[j];
 // Rectangle r = area.getBounds();
 // area.setBounds(r.x + index*slotWidth, r.y, slotWidth, r.height);
 // }
 // }
 					if (dayCount > 1)
 						day = incDay(day);
 
 
 				Iterator i = timeLines.keySet().iterator();
 				while (i.hasNext()) {
 					Tuple key = (Tuple) i.next();
 					Date date = (Date) key.elementAt(0);
 					int minutes = Integer.parseInt((String) key.elementAt(1));
 					JLabel line = (JLabel) timeLines.get(key);
 					Date date1 = new Date(date.getTime() + minutes * 60 * 1000);
 					int y1 = getYPos(date1, 0);
 					int x1 = 0;
 					int lineheight = 1;
 					if (minutes > 0) {
 						// x1 = 25;
 						lineheight = 1;
 					}
 					line.setBounds(x1, y1, width, lineheight);
 				}
 
 				for (int iCal = 0; iCal < calBackgrounds.size(); iCal++) {
 					int x1 = getXPos(iCal * dayCount);
 					int x2 = getXPos((iCal + 1) * dayCount);
 					JPanel calBackground = (JPanel) calBackgrounds.get(iCal);
 					calBackground.setBounds(x1, getCaptionRowHeight(), x2 - x1,
 							getHeight());
 				}
 			} catch (Exception e) {
 				throw BizcalException.create(e);
 			}
 		}
 	}
 
 	/**
 	 * Finds the smalles width of a framearea and its children
 	 *
 	 * @param fa
 	 * @return
 	 */
 	private int findSmallestFrameArea(FrameArea fa) {
 		/* ================================================== */
 		if (fa.getChildren() == null || fa.getChildren().size() < 1)
 			return fa.getBounds().width;
 		else {
 			int smallest = fa.getBounds().width;
 			for (FrameArea child : fa.getChildren()) {
 				if (child.getBounds().width < smallest)
 					smallest = child.getBounds().width;
 			}
 			return smallest;
 		}
 		/* ================================================== */
 	}
 
 	protected Object getCalendarId(int x, int y) throws Exception {
 		return getCalendarId(getColumn(x));
 	}
 
 	private DayViewConfig getDesc() throws Exception {
 		DayViewConfig result = (DayViewConfig) getDescriptor();
 		if (result == null) {
 			result = new DayViewConfig();
 			setDescriptor(result);
 		}
 		return result;
 	}
 
 	public DayViewConfig getDayViewConfig() throws Exception {
 		return getDesc();
 	}
 
 	protected int getInitYPos() throws Exception {
 		double viewStart = getModel().getViewStart().getValue();
 		double ratio = viewStart / (24 * 3600 * 1000);
 		return (int) (ratio * this.config.getHours() * PIXELS_PER_HOUR);
 
 //		double viewStart = getModel().getViewStart().getValue();
 //		double ratio = viewStart / (24 * 3600 * 1000);
 //		return (int) (ratio * 24 * PIXELS_PER_HOUR);
 
 	}
 
 	private int getPreferredHeight() {
 
 		return this.config.getHours() * PIXELS_PER_HOUR + getFooterHeight();
 	}
 
 	public JComponent getComponent() {
 		return scrollPane;
 	}
 
 	public void initScroll() throws Exception {
 		scrollPane.getViewport().setViewPosition(new Point(0, getInitYPos()));
 	}
 
 	public void addListener(CalendarListener listener) {
 		super.addListener(listener);
 		columnHeader.addCalendarListener(listener);
 	}
 
 }
