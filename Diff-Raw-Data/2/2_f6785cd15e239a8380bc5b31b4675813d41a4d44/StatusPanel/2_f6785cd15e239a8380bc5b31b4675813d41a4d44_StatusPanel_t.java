 package javachallenge.graphics.components;
 
 import javachallenge.graphics.util.ColorMaker;
 import javachallenge.graphics.util.HTMLMaker;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: LGM
  * Date: 2/18/13
  * Time: 12:20 AM
  */
 public class StatusPanel extends Panel
 {
 	public static int GAP_SIZE=10;
 	protected ArrayList<Label> labels=new ArrayList<Label>();
 	protected Label time, score;
 	public StatusPanel(Color color)
 	{
 		super(color);
 		setLayout(null);
 		time=new Label(new HTMLMaker("time: 0", ColorMaker.white, 10).toString());
 		time.setBounds(10, 0, 200, 50);
 		add(time);
 	//	addLabel(score = new Label("score: 0"));
 	}
 	public void updatePosition()
 	{
 		//for (Label label:labels)
 		//	label.setBounds(GAP_SIZE,label.getY(),getWidth()-2*GAP_SIZE,30);
 	}
 	
 	public void setTime(int a) {
 		time.setText("time: " + a);
 	}
 	
 	public void setScore(int a) {
		//score.setText("score: " + a);
 	}
 }
