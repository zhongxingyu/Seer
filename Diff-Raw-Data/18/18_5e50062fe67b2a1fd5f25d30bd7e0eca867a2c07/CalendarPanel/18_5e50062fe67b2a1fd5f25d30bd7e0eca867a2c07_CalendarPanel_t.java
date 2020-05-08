 package no.ntnu.fp.gui;
 
 import com.u2d.calendar.CellResChoice;
 import com.u2d.calendar.DateTimeBounds;
 import com.u2d.type.atom.TimeEO;
 import com.u2d.type.atom.TimeSpan;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import no.ntnu.fp.gui.listeners.CalendarPanelActionListener;
 import no.ntnu.fp.model.Event;
 import no.ntnu.fp.model.ServiceWrapper.EventsRunner;
 import no.ntnu.fp.model.calendar.Utils;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo
  * SWT/Swing GUI Builder, which is free for non-commercial
  * use. If Jigloo is being used commercially (ie, by a corporation,
  * company or business for any purpose whatever) then you
  * should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details.
  * Use of Jigloo implies acceptance of these licensing terms.
  * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
  * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
  * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 public class CalendarPanel extends JPanel implements ComponentListener
 {
 
     private static final String[] WEEKS =
     {
         "Mandag", "Tysdag", "Onsdag", "Torsdag", "Fredag", "Laurdag", "Sundag"
     };
     private static DateFormat COLHEADER_FORMATTER = new SimpleDateFormat("EEE, MMM dd");
     private DateTimeBounds bounds;
     private final TimeSpan daySpan = new TimeSpan();
     private final TimeSpan weekSpan = new TimeSpan();
     private WeekTableModel model;
     private List<Event> events;
     private static int COLUMN_WIDTH = 100;
     private static int FIRST_COLUMN_WIDTH = 65;
     private static int WEEKEND_COLUMN_WIDTH = 85;
     private static int ROW_HEIGHT = 65;
     private final List<CalendarPanelActionListener> actionListeners = Collections.synchronizedList(new LinkedList<CalendarPanelActionListener>());
     private JTable table;
     private JScrollPane scrollPane;
     private BaseCalendarView container;
     private int height;
 
     public final void addCalendarPanelActionListener(CalendarPanelActionListener listener)
     {
         if (listener != null && !actionListeners.contains(listener))
         {
             actionListeners.add(listener);
         }
     }
 
     public final void removeCalendarPanelActionListener(CalendarPanelActionListener listener)
     {
         if (listener != null && actionListeners.contains(listener))
         {
             actionListeners.remove(listener);
         }
     }
 
     private void onEventClicked(Event event)
     {
         for (int i = 0, l = actionListeners.size(); i < l; i++)
         {
             actionListeners.get(i).eventClicked(this, event);
         }
     }
 
     private void onTimeClicked(TimeSpan timeSpan)
     {
         for (int i = 0, l = actionListeners.size(); i < l; i++)
         {
             actionListeners.get(i).timeClicked(this, timeSpan);
         }
     }
 
     public JScrollPane getScrollPane() {
         return scrollPane;
     }
 
     /**
      * @wbp.parser.constructor
      */
     public CalendarPanel(BaseCalendarView container) {
         this(container, 410);
     }
 
     public CalendarPanel(BaseCalendarView container, int height)
     {
         super();
         this.height = height;
         this.container = container;
         bounds = new DateTimeBounds();
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         bounds.dayStartTime(cal.getTime());
         bounds.dayInterval(Calendar.HOUR, 24);
         initGUI();
     }
 
     public void setEvents(List<Event> events)
     {
         this.events = events;
         repaint();
         table.repaint();
     }
 
     private void initGUI()
     {
         events = null;
         container.getService().getEvents(new EventsRunner() {
 
             public void run(List<Event> events)
             {
                 if(CalendarPanel.this.events == null)
                     setEvents(events);
             }
         });
         adjustSpan();
         buildTable();
         addEventListeners();
         scrollPane = new JScrollPane(table);
         SwingUtilities.invokeLater(new Runnable() {
             public void run()
             {
                 scrollPane.getVerticalScrollBar().setValue(500);
             }
         });
         
         //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         add(scrollPane);
         //setSize(new Dimension(735, 400));
     }
 
     private void addEventListeners()
     {
         addComponentListener(this);
     }
 
     public void setWeek(Calendar calendar)
     {
         bounds.position(calendar.getTime());
         //bounds.weekStartTime(calendar.getTime());
         adjustSpan();
         System.out.print("Cal: ");
         System.out.println(calendar.getTime());
 
         System.out.print("getWeek(): ");
         System.out.println(getWeek().getTime());
 
         System.out.print("weekSpan: ");
         System.out.println(weekSpan);
         SwingUtilities.invokeLater(new Runnable()
         {
 
             public void run()
             {
                 for (int i = 1; i < model.getColumnCount(); i++)
                 {
                     table.getColumn("" + i).setHeaderValue(model.getColumnName(i));
                 }
 
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(bounds.position().dateValue());
                 int column = cal.get(Calendar.DAY_OF_WEEK);
                 if (table.getSelectedColumn() != column)
                 {
                     table.setColumnSelectionInterval(column, column);
                 }
 
                 repaint();
                 table.repaint();
             }
         });
     }
 
     public Calendar getWeek()
     {
         return Utils.getCalendar(bounds.position().dateValue());
     }
 
     private void adjustSpan()
     {
         Calendar startOfWeek = Calendar.getInstance();
         Date time = bounds.position().dateValue();
         startOfWeek.setTime(time);
 
         Calendar weekStartTimeCalendar = bounds.weekStartTime().calendarValue();
 
         startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 
         if (startOfWeek.getTime().after(time))
         {
             startOfWeek.set(Calendar.DAY_OF_WEEK, -7);
         }
 
         startOfWeek.set(Calendar.HOUR_OF_DAY, weekStartTimeCalendar.get(Calendar.HOUR_OF_DAY));
         startOfWeek.set(Calendar.MINUTE, weekStartTimeCalendar.get(Calendar.MINUTE));
         startOfWeek.set(Calendar.SECOND, weekStartTimeCalendar.get(Calendar.SECOND));
 
         System.out.println("beginning of week is " + startOfWeek.getTime());
 
         TimeEO startHr = new TimeEO(startOfWeek.getTimeInMillis());
 
         Calendar dayStartTimeCalendar = bounds.dayStartTime().calendarValue();
         startHr.set(Calendar.HOUR_OF_DAY, dayStartTimeCalendar.get(Calendar.HOUR_OF_DAY));
 
         weekSpan.setValue(new TimeSpan(startOfWeek.getTime(), bounds.weekInterval()));
         daySpan.setValue(new TimeSpan(startHr.dateValue(), bounds.dayInterval()));
     }
 
     private Calendar getDateTimeForCellCoordinates(int rowidx, int colidx)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(daySpan.startDate());
         cal.add(Calendar.DATE, colidx - 1);
         // (colidx of 1 is sunday, first day of week, add nothing)
         cal.add(Calendar.MINUTE, rowidx * (int) cellRes().timeInterval().getMilis() / (1000 * 60));
         return cal;
     }
 
     private Event getEventForTime(Calendar cal)
     {
         if(events == null)
             return null;
         for (Event e : events)
         {
             if (e.getDate().get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                && e.getDate().get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR))
             {
                 boolean startsBefore = e.getStartTime().get(Calendar.HOUR_OF_DAY) <= cal.get(Calendar.HOUR_OF_DAY);
                 boolean endsAfter = e.getEndTime().get(Calendar.HOUR_OF_DAY) > cal.get(Calendar.HOUR_OF_DAY);
                 if (startsBefore && endsAfter)
                 {
                     if (e.getStartTime().get(Calendar.HOUR_OF_DAY) == cal.get(Calendar.HOUR_OF_DAY))
                     {
                         e.setFirst(true);
                     }
                     else
                     {
                         e.setFirst(false);
                     }
                     return e;
                 }
             }
         }
         return null;
     }
 
     protected void buildTable()
     {
         model = new WeekTableModel();
         table = new JTable();
         int twidth = 0;
 
         table.setAutoCreateColumnsFromModel(false);
         table.setModel(model);
         //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
         table.setRowHeight(ROW_HEIGHT);
 
 
         TableColumn column = new TableColumn(0, FIRST_COLUMN_WIDTH,
                                              new RowHeaderCellRenderer(), null);
 
         column.setMinWidth(FIRST_COLUMN_WIDTH);
         column.setMaxWidth(FIRST_COLUMN_WIDTH);
         twidth += FIRST_COLUMN_WIDTH;
         column.setIdentifier("times");
 
         table.addColumn(column);
 
         DefaultTableCellRenderer renderer = null;
         for (int i = 1; i < model.getColumnCount(); i++)
         {
             int width = (i > 5)
                 ? WEEKEND_COLUMN_WIDTH : COLUMN_WIDTH;
             renderer = new CalendarCellRenderer();
             column = new TableColumn(i, width, renderer, null);
             column.setMinWidth(width);
             column.setIdentifier("" + i);
             table.addColumn(column);
             twidth += width;
         }
 
         table.setGridColor(Color.lightGray);
         table.setShowGrid(true);
         table.setRowSelectionAllowed(false);
         table.setColumnSelectionAllowed(true);
         table.getTableHeader().setReorderingAllowed(false);
         table.setSelectionBackground(Color.cyan);
         table.setPreferredScrollableViewportSize(new Dimension(960, height));
         //table.setPreferredSize(new Dimension(965, 1600));
         //table.setSize(new Dimension(twidth, 1560));
         //table.setMinimumSize(new Dimension(735, 1560));
         table.addMouseListener(new MouseListener() {
 
             public void mouseClicked(MouseEvent e)
             {
                 int x = e.getX();
                 int y = e.getY();
                 int row = 0;
                 int col = 0;
 
                 boolean hit = false;
                 while(!hit) {
                     Rectangle rect = table.getCellRect(row, col, true);
                     if(rect.contains(x, y))
                     {
                         hit = true;
                     }
                     else
                     {
                         if(rect.getMaxX() < x)
                         {
                             col++;
                         }
                         if(rect.getMaxY() < y)
                         {
                             row++;
                         }
                         if(table.getColumnCount() == col)
                             return;
                         if(table.getRowCount() == row)
                             return;
                     }
                 }
 
                 Calendar cal = getDateTimeForCellCoordinates(row, col);
                 Event event = getEventForTime(cal);
                 if(event != null)
                 {
                     onEventClicked(event);
                 }
 
                 TimeSpan ts = new TimeSpan(cal.getTime(), 1000 * 3600);
                 onTimeClicked(ts);
             }
 
             public void mousePressed(MouseEvent e)
             {
                 //throw new UnsupportedOperationException("Not supported yet.");
             }
 
             public void mouseReleased(MouseEvent e)
             {
                 //throw new UnsupportedOperationException("Not supported yet.");
             }
 
             public void mouseEntered(MouseEvent e)
             {
                 //throw new UnsupportedOperationException("Not supported yet.");
             }
 
             public void mouseExited(MouseEvent e)
             {
                 //throw new UnsupportedOperationException("Not supported yet.");
             }
         });
     }
 
     protected CellResChoice cellRes()
     {
         return CellResChoice.ONE_HOUR;
     }
 
     public void componentResized(ComponentEvent ce)
     {
         //throw new UnsupportedOperationException("Not supported yet.");
         //table.setPreferredSize(table.getSize());
         //System.out.println(table.getSize());
         //scrollPane.setBounds(getBounds());
         //scrollPane.revalidate();
     }
 
     public void componentMoved(ComponentEvent ce)
     {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void componentShown(ComponentEvent ce)
     {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void componentHidden(ComponentEvent ce)
     {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private class WeekTableModel extends AbstractTableModel
     {
 
         private int numCellsInDay;
         private TimeEO[] times;
 
         public WeekTableModel()
         {
             updateCellRes();
         }
 
         public void updateCellRes()
         {
             numCellsInDay = daySpan.numIntervals(cellRes().timeInterval());
             times = new TimeEO[numCellsInDay];
 
             int i = 0;
             for (Iterator itr = daySpan.iterator(cellRes().timeInterval());
                 itr.hasNext();)
             {
                 times[i++] = (TimeEO) itr.next();
             }
             fireTableStructureChanged();
         }
 
         public int getRowCount()
         {
             return numCellsInDay;
         }
 
         public int getColumnCount()
         {
             return WEEKS.length + 1;
         }
 
         public String getColumnName(int column)
         {
             if (column == 0)
             {
                 return " ";
             }
             TimeSpan span = weekSpan.add(Calendar.DATE, column - 1);
             return COLHEADER_FORMATTER.format(span.startDate());
         }
 
         public boolean isCellEditable(int nRow, int nCol)
         {
             return false;
         }
 
         public Object getValueAt(int nRow, int nCol)
         {
             if (nCol > 0)
             {
                 return null; //TODO: Make better!
             }
             return times[nRow].toString();
         }
 
         public String toString()
         {
             return "Week View";
         }
     }
 
     private class RowHeaderCellRenderer implements TableCellRenderer
     {
 
         JLabel cell;
 
         public RowHeaderCellRenderer()
         {
             cell = new JLabel();
             cell.setOpaque(true);
             Color color = UIManager.getColor("TableHeader.background");
             cell.setBackground(color);
 
             Font font = UIManager.getFont("TableHeader.font");
             cell.setFont(font.deriveFont(10.0f));
             cell.setVerticalAlignment(SwingConstants.TOP);
             cell.setHorizontalAlignment(SwingConstants.TRAILING);
             cell.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
         }
 
         public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus, int row, int column)
         {
             cell.setText(value.toString());
             return cell;
         }
     }
 
     private class CalendarCellRenderer extends DefaultTableCellRenderer
     {
 
         JTextArea cell;
 
         public CalendarCellRenderer()
         {
             //setLineWrap(true);
             cell = new JTextArea();
             cell.setOpaque(true);
             Color color = table.getBackground();
             cell.setBackground(color);
 
             Font font = table.getFont();
             cell.setFont(font);
             cell.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
         }
 
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column)
         {
             Boolean start = false;
             Calendar cal = getDateTimeForCellCoordinates(row, column);
             Event event = getEventForTime(cal);
             if (event != null)
             {
                 cell.setBackground(Color.yellow);
                 start = event.getFirst();
                 if (start)
                 {
                     cell.setText(event.getDescription());
                 }
                 else
                 {
                     cell.setText("");
                 }
             }
             else
             {
                 cell.setBackground(table.getBackground());
                 cell.setText("");
             }
 //            return this;
             cell.setSize(new Dimension(table.getColumn("" + column).getWidth(), 10000));
             return cell;
         }
     }
 }
