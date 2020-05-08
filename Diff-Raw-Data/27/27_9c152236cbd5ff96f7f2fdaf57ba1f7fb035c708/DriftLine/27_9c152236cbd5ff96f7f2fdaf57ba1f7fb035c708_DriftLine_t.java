 package uk.ac.shef.dcs.oak.musicview.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.swing.JPanel;
 
 import uk.ac.shef.dcs.oak.musicview.Controller;
 import uk.ac.shef.dcs.oak.musicview.Event;
 import uk.ac.shef.dcs.oak.musicview.Model;
 import uk.ac.shef.dcs.oak.musicview.ModelListener;
 
 /**
  * The drift line shows the overall drift from the norm
  * 
  * @author sat
  * 
  */
 public class DriftLine extends JPanel implements ModelListener
 {
    /** The voice displayed by this line */
    private double chosenVoice = -1;
 
    /** Basis model */
    private Model model;
 
    /**
     * Constructor
     * 
     * @param cont
     *           THe main controller
     * @param voice
     *           The voice to display
     */
    public DriftLine(final Controller cont, final double voice)
    {
       cont.addListener(this);
       chosenVoice = voice;
    }
 
    /**
     * Method to determine if a given event is valid for the display
     * 
     * @param ev
     *           The event to test
     * @return true if the event is valid, false otherwise
     */
    protected boolean isValidEvent(final Event ev)
    {
       return ev.getPitch() == chosenVoice;
    }
 
    @Override
    public final void newModelLoaded(final Model mod)
    {
       this.model = mod;
       repaint();
    }
 
    @Override
    public final void paint(final Graphics g)
    {
       super.paint(g);
 
       if (model != null)
       {
          double pixelsPerSecond = this.getWidth() / model.getTotalLength();
 
          Map<Double, Double> drifts = new TreeMap<Double, Double>();
          double maxDrift = 0;
          for (Event ev : model.getEvents())
             if (isValidEvent(ev))
             {
                System.out.println(ev.getOnset() + " and " + model.getScoreTime(ev));
                double drift = ev.getOnset() - model.getScoreTime(ev);
                drifts.put(model.getScoreTime(ev) - model.getOffset(), drift);
                maxDrift = Math.max(maxDrift, Math.abs(drift));
             }
 
          int currY = this.getHeight() / 2;
          int currX = -1;
          // Paint the line
          for (Entry<Double, Double> entry : drifts.entrySet())
          {
             int pixY = this.getHeight() / 2
                   - (int) ((entry.getValue() / maxDrift) * this.getHeight() / 2);
            int pixX = ActionLine.LEFT_MARGIN + (int) (pixelsPerSecond * entry.getKey());
             if (currX > 0)
                g.drawLine(currX, currY, pixX, pixY);
             currX = pixX;
             currY = pixY;
          }
 
          // Draw the baseline
          g.setColor(Color.lightGray);
          g.drawLine(0, this.getHeight() / 2, getWidth(), getHeight() / 2);
       }
    }
 }
