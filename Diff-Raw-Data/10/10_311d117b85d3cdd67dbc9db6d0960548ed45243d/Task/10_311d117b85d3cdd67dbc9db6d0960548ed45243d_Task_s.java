 package mvc.views;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import javax.swing.JComponent;
 import mvc.model.Activity;
 
 public class Task extends JComponent
 {
     private Activity task;
     private boolean transparent = false;
     private boolean visibleInChartView;
     
     private static Color border      = Color.BLACK;
     private static Color tooEarly    = Color.GRAY;
     private static Color tooLate     = Color.GRAY;
     private static Color normal      = Color.GREEN;
     private static Color textC       = Color.BLACK;
     
     private static int tsize = 25;
     
     private static final long DAY_IN_MILLIS = (1000 * 60 * 60 * 24);
     private final DateFormat DATE_FORMAT = new SimpleDateFormat("d MMMM y", Locale.ENGLISH);
     
     public Task(boolean visibleInChartView)
     {
         this.visibleInChartView = visibleInChartView;
         this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
     }
     
     /**
      * Associates a certain activity in the Model to this component from witch
      * data will be gathered to render this object.
      * @param task 
      */
     public void setActivity(Activity task)
     {
         this.task = task;
         
         this.setSize(new Dimension(PlanningView.cellWidth * task.getDateSpan(),
                                 PlanningView.cellHeight));
         
         // Set the tool-tip text
         StringBuilder sb = new StringBuilder();
         sb.append("<html>");
         sb.append("Customer: ");
         sb.append(task.getCustomer());
         sb.append("<br/>Duration: ");
         sb.append(task.getDateSpan());
         sb.append("<br/>Start date: ");
         sb.append(DATE_FORMAT.format(task.getStartDate().getTime()));
         sb.append("<br/>End date: ");
         sb.append(DATE_FORMAT.format(task.getEndDate().getTime()));
         sb.append("<br/>Earliest start date: ");
         sb.append(DATE_FORMAT.format(task.getEarliestStartDate().getTime()));
         sb.append("<br/>Latest end date: ");
         sb.append(DATE_FORMAT.format(task.getLatestEndDate().getTime()));
         sb.append("</html>");
         this.setToolTipText(sb.toString());
     }
     
     public Activity getActivity() {
         return task;
     }
     
     /**
      * Set the border color for all instances of this custom JComponent.
      * @param c 
      */
     public static void setBorderColor(Color c)
     {
         border = c;
     }
     
     /**
      * Set the rendering background color for when the starting date is
      * previous to the earliest starting date specified in the activity.
      * @param c 
      */
     public static void setEarlyColor(Color c)
     {
         tooEarly = c;
     }
     
     /**
      * Set the rendering background color for when the end date is
      * later to the lates date specified in the activity.
      * @param c 
      */
     public static void setLateColor(Color c)
     {
         tooLate = c;
     }
     
     /**
      * Set the rendering background color for when the activity period is
      * between the earliest start and latest end.
      * @param c 
      */
     public static void setNormalColor(Color c)
     {
         normal = c;
     }
     
     public static void setTextColor(Color c)
     {
         textC = c;
     }
     
     /**
      * Makes the component to be partially invisible. When set to true, only
      * the outer border will be rendered. Useful to make a drag effect.
      * @param b 
      */
     public void setContentTransparent(boolean b)
     {
         this.transparent = b;
     }
 
     @Override
     public void paint(Graphics g)
     {
         boolean paintControl = (this.getWidth() > 0 && this.getHeight() > 0);
         
         if (paintControl) {
             if (!this.transparent) {
                 // Print with the normal color
                 g.setColor(normal);
                 g.fillRect(0, 0, getWidth(), getHeight());
                 
                 if(visibleInChartView) {
                     long dt;
                     int x;
 
                     // Check if task is to early
                     if(task.getStartDate().before(task.getEarliestStartDate())) {
 
                         dt = task.getEarliestStartDate().getTimeInMillis()
                                 - task.getStartDate().getTimeInMillis();
                         x = (int) (PlanningView.cellWidth * (dt / DAY_IN_MILLIS));
 
                         g.setColor(tooEarly);
                         g.fillRect(0, 0, x, this.getHeight());
                     }
 
                     // Check if task is to late
                     if(task.getEndDate().after(task.getLatestEndDate())) {
 
                         dt = task.getEndDate().getTimeInMillis()
                                 - task.getLatestEndDate().getTimeInMillis();
                        x = (int) (PlanningView.cellWidth * ((dt / DAY_IN_MILLIS) + 1));
 
                         g.setColor(tooLate);
                         g.fillRect(getWidth() - x, 0, x, this.getHeight());
                     }
                 }
 
                 // getting font dimensions
                 g.setColor(textC);
                 Font font = new Font("Helvetica", Font.PLAIN, tsize);
                 g.setFont(font);
                 FontMetrics fm = g.getFontMetrics();
                 Rectangle2D r = fm.getStringBounds(task.getCustomer(), g);
 
                 // adapting text size
                 // makes it bigger if it is possible
                 while (this.getWidth() > r.getWidth() && this.getHeight() > r.getHeight()) {
                     ++tsize;
                     font = new Font("Helvetica", Font.PLAIN, tsize);
                     g.setFont(font);
                     fm   = g.getFontMetrics();
                     r    = fm.getStringBounds(task.getCustomer(), g);
                 }
                 
                 // adapting text size
                 // makes it smaller if it doesn't fit in the component bounds.
                 while (this.getWidth() <= r.getWidth() || this.getHeight() <= r.getHeight()) {
                     --tsize;
                     font = new Font("Helvetica", Font.PLAIN, tsize);
                     g.setFont(font);
                     fm   = g.getFontMetrics();
                     r    = fm.getStringBounds(task.getCustomer(), g);
                 }
 
                 // drawing the text
                 int ix = (int)((this.getWidth() - r.getWidth())/2);
                 int iy = (int)((this.getHeight()/2) + (r.getHeight()/4));
 
                 g.drawString(task.getCustomer(), ix, iy);
             }
 
             // drawing the outer rectangle
             g.setColor(border);
             g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
         }
     }
 
     @Override
     public String toString()
     {
         return task.getCustomer();
     }
 }
