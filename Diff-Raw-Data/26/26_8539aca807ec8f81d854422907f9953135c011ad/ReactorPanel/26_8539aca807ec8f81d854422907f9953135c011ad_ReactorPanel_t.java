 //////////////////////////////////////////////////////////////////////////////////
 // Class: 	ReactorPanel
 //
 // Purpose: This class is the parent class of the two classes that implement
 //			the visual representation of the batch reactor and the cstr reactor.
 //			It contains most of the logic for visually representing a reactor,
 //			including methods for performing individual reaction steps and
 //			individual animation steps.
 //
 //////////////////////////////////////////////////////////////////////////////////
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.Timer;
 import javax.swing.JLabel;
 
 public class ReactorPanel extends JPanel 
 {
 	private static final long serialVersionUID = 1L;
 	private ArrayList<FFDot> dots;
 	private int totalNumberOfDots;
 	private int currentNumberOfDots;
 	private JTextArea txtConcentrationLog;
 	private int dotDiameter;
 	private int upperCornerX;
 	private Color dotColor;
 	private Color backgroundColor;
 	private Timer animationTimer;
 	private Timer reactionTimer;
 	private FFReactor reactor;
 	private JLabel concentrationLabel;
 	private JLabel percentageLabel;
 	
 	//Constructor sets the position, numbers of dots, dot/background color, and timestep
 	//of both timers
 	public ReactorPanel(int numDots, int diameter, int upperCornerX, int rDelta, int aDelta)
 	{
 		this.setUpperCornerX(upperCornerX);
 		dotDiameter = diameter;
 		totalNumberOfDots = numDots;
 		currentNumberOfDots = numDots;
 		dots = new ArrayList<FFDot>();
 		dotColor = Color.RED;
 		backgroundColor = Color.WHITE;
 		setBackground(backgroundColor);
 		setAnimationTimer(new Timer(aDelta, new AnimationTimerListener()));
 		setReactionTimer(new Timer(rDelta, new ReactionTimerListener()));
 		getAnimationTimer().setRepeats(true);
 		getReactionTimer().setRepeats(true);
 	}
 	
 	//Adds dots up to the necessary amount into the array
 	public void makeDots()
 	{
 		for(int i = 0; i < currentNumberOfDots; i++)
 			dots.add(new FFDot(this.getBounds(), dotDiameter));
 	}
 	
 	//Removes all dots from the array
 	public void clearDots()
 	{
 		dots = new ArrayList<FFDot>();
 	}
 	
 	//Repaints all dots in their updated positions
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(dotColor);
 		makeDots();
 		for(int i = 0; i < currentNumberOfDots; i++)
 		{
 			Ellipse2D.Double dot = dots.get(i).getEllipse();
 			g2.fill(dot);
 		}
 	}
 
 	//Performs one animation step.  By clearing dots and
 	//then remaking them, any changes in concentration that
 	//occurred during the last reaction step are taken into
 	//account visually
 	class AnimationTimerListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent evt) 
 		{
 			clearDots();
 			makeDots();
 			repaint();
 		}
 	}
 	
 	//Performs one reaction step
 	class ReactionTimerListener implements ActionListener 
 	{
 		DecimalFormat df1 =  new DecimalFormat("#.####");
 		DecimalFormat df2 =  new DecimalFormat("#.##");
 		public void actionPerformed(ActionEvent evt) 
 		{
 			//Updates the dynamic labels to reflect new concentration and %
			if (concentrationLabel != null) concentrationLabel.setText(df1.format(reactor.getCurrentConcentration()));
			if (percentageLabel != null) percentageLabel.setText(df2.format(reactor.getPercentageOfUpperLimit() * 100) + "%");
 			reactor.setCurrentTime(reactor.getCurrentTime() + 1); //increment time by 1
 			if (getTextLogArea() != null)
 			{
 				getTextLogArea().append("Concentration at time "
 				+ (int) reactor.getCurrentTime() + " is "					//append current
 				+ df1.format(reactor.getCurrentConcentration()) + "\n"); 	//concentration info
 			}
 			clearDots();
 			//manually reset the current number of dots to reflect any changes
 			setCurrentNumberOfDots((int) (reactor.getPercentageOfConcentrationLeft() * getTotalNumberOfDots()));
 			makeDots();
 			repaint();
 		}
 	}
 	
 	//Accessor and mutator methods for instance variables
 	public void setLogTextArea(JTextArea log)
 	{
 		txtConcentrationLog = log;
 	}
 	
 	public JTextArea getTextLogArea()
 	{
 		return txtConcentrationLog;
 	}
 	
 	public void setTotalNumberOfDots(int index)
 	{
 		totalNumberOfDots = index;
 	}
 	
 	public void setCurrentNumberOfDots(int index)
 	{
 		currentNumberOfDots = index;
 	}
 	
 	public int getCurrentNumberOfDots()
 	{
 		return currentNumberOfDots;
 	}
 	
 	public int getTotalNumberOfDots()
 	{
 		return totalNumberOfDots;
 	}
 	
 	
 	public int getLastDot()
 	{
 		return totalNumberOfDots;
 	}
 	
 	public void setDotColor(Color RGB){
 		dotColor = RGB;
 	}
 	
 	public void setBgColor(Color RGB){
 		backgroundColor = RGB;
 		setBackground(backgroundColor);
 	}
 	
 	public void setDotSize(int sz){
 		dotDiameter = sz;
 	}
 	
 	public void setAnimationTimer(Timer t)
 	{
 		animationTimer = t;
 	}
 	
 	public Timer getAnimationTimer()
 	{
 		return animationTimer;
 	}
 	
 	public void setReactionTimer(Timer t)
 	{
 		reactionTimer = t;
 	}
 	
 	public Timer getReactionTimer()
 	{
 		return reactionTimer;
 	}
 	
 	public void startReaction()
 	{
 		reactionTimer.start();
 	}
 	
 	public void stopReaction()
 	{
 		reactionTimer.stop();
 	}
 
 	public void startAnimation()
 	{
 		animationTimer.start();
 	}
 	
 	public void stopAnimation()
 	{
 		animationTimer.stop();
 	}
 	
 	public int getUpperCornerX() 
 	{
 		return upperCornerX;
 	}
 	
 	public void setAnimationTimer(int delay)
 	{
 		animationTimer.setDelay(delay);
 	}
 
 	public void setReactionTimer(int delay)
 	{
 		reactionTimer.setDelay(delay);
 	}
 	
 	public void setUpperCornerX(int upperCornerX) 
 	{
 		this.upperCornerX = upperCornerX;
 	}
 	
 	public void setReactionConstant(double rate)
 	{
 		reactor.setReactionConstant(rate);
 	}
 	
 	public void setCurrentTime(double time)
 	{
 		reactor.setCurrentTime(time);
 	}
 	
 	public FFReactor getReactor() 
 	{
 		return reactor;
 	}
 
 	public void setReactor(FFReactor newReactor) 
 	{
 		reactor = null;
 		reactor = newReactor;
 	}
 	
 	public JLabel getConcentrationLabel() {
 		return concentrationLabel;
 	}
 
 	public void setConcentrationLabel(JLabel concentrationLabel) {
 		this.concentrationLabel = concentrationLabel;
 	}
 
 	public JLabel getPercentageLabel() {
 		return percentageLabel;
 	}
 
 	public void setPercentageLabel(JLabel percentageLabel) {
 		this.percentageLabel = percentageLabel;
 	}
 }
