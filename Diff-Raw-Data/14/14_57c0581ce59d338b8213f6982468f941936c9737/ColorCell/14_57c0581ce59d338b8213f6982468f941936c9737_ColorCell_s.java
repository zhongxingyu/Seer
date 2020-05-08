 package editor.window;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import java.util.ArrayList;
 
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.Color;
 import de.matthiasmann.twl.utils.TintAnimator;
 import de.matthiasmann.twl.utils.TintAnimator.TimeSource;
 
 public class ColorCell extends Button{
 	private Color base_color;
 	private ArrayList<ActionListener> action_listeners;
 	
 	public ColorCell(){
 		this.addCallback(new Callback(this));
 		action_listeners = new ArrayList<ActionListener>();
 	}
 
 	public ColorCell(Color base_color){
 		this.addCallback(new Callback(this));
 		action_listeners = new ArrayList<ActionListener>();
 		setColor(base_color);
 	}
 	
 	public Color getColor(){return new Color(base_color.toARGB());}
 	
 	public void setColor(Color color){
 		this.base_color = color;
 		super.setTintAnimator(new TintAnimator(new TimeSource() {
 			@Override public void resetTime() {}
 			@Override public int getTime() {return 0;}
 		}));
 		this.getTintAnimator().setColor(base_color);
 	}
 	
 	public void fireActionEvent(){
 		for(ActionListener l : action_listeners){
 			l.actionPerformed(new ActionEvent(this));
 		}
 	}
 	
 	public void addActionListener(ActionListener listener){
 	}
 	
 	private class Callback implements Runnable{
 		ColorCell owner;
 		public Callback(ColorCell owner){
 			this.owner = owner;
 		}
 		@Override
 		public void run() {
 			owner.fireActionEvent();
 		}
 	}
 }
