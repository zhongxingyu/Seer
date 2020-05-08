 package uk.ac.shef.dcs.oak.musicview.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import uk.ac.shef.dcs.oak.musicview.Controller;
 import uk.ac.shef.dcs.oak.musicview.Event;
 import uk.ac.shef.dcs.oak.musicview.Model;
 import uk.ac.shef.dcs.oak.musicview.ModelListener;
 
 /**
  * The action line shows the score as a series of actions
  * 
  * @author sat
  * 
  */
 public class ActionLine extends JPanel implements ModelListener
 {
    /** Flag for painting the bar lines */
    private static final boolean BAR_LINES = true;
 
    /** Margins for plotting */
    private static final int MARGIN = 5;

    /** Size of the circle */
    private static final int MAX_CIRCLE_SIZE = 40;
    /** Size of the circle */
    private static final int MIN_CIRCLE_SIZE = 10;
 
    /** The voice we're considering */
    private final double chosenVoice;
 
    /** The list of events that should be plotted */
    private Model model;
 
    /**
     * Constructor
     * 
     * @param cont
     *           The main controller
     * @param voice
     *           The voice that this line will display
     */
    public ActionLine(final Controller cont, final double voice)
    {
       cont.addListener(this);
       chosenVoice = voice;
    }
 
    @Override
    public final void newModelLoaded(final Model mod)
    {
       model = mod;
       repaint();
    }
 
    @Override
    public final void paint(final Graphics g)
    {
       super.paint(g);
 
       if (model != null)
       {
          // Do some housekeeping
         double pixelPerSecond = this.getWidth() / model.getTotalLength();
 
          // Plot the events
          for (Event ev : model.getEvents())
             if (ev.getPitch() == chosenVoice)
             {
                double percVelocity = model.getVelocityPerc(ev);
                int circleSize = (int) (percVelocity * (MAX_CIRCLE_SIZE - MIN_CIRCLE_SIZE))
                      + MIN_CIRCLE_SIZE;
 
                // Draw a circle at the relevant point and the relevant pitch
                g.setColor(Color.RED);
               int pixCent = (int) ((ev.getOnset() - model.getOffset()) * pixelPerSecond);
                int pixHeight = this.getHeight() / 2 - circleSize / 2;
                g.drawOval(pixCent - circleSize / 2, pixHeight, circleSize, circleSize);
 
                // Draw the Desired onset in black
                g.setColor(Color.black);
               int actPixCent = (int) ((model.getScoreTime(ev) - model.getOffset()) * pixelPerSecond);
                g.drawOval(actPixCent - circleSize / 2, pixHeight + 2, circleSize, circleSize);
             }
 
          // Draw the bar lines if needed
          if (BAR_LINES)
          {
             g.setColor(Color.lightGray);
             for (double barTime : model.getBarTimes())
             {
               int pixPos = (int) ((barTime - model.getOffset()) * pixelPerSecond);
                g.drawLine(pixPos, 0, pixPos, this.getHeight());
             }
          }
       }
    }
 }
