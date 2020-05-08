 package editor;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import java.util.ArrayList;
 
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.Color;
 import de.matthiasmann.twl.utils.TintAnimator;
 import de.matthiasmann.twl.utils.TintAnimator.TimeSource;
 
 public class Block<E extends Number> extends Button{
 	private Coordinate<E> position;
 	private Color base_color;
 	private ArrayList<ActionListener> action_listeners;
 	//TODO: Textures?
 	public Block(){
 		action_listeners = new ArrayList<ActionListener>();
 		this.addCallback(new Callback(this));
 	}
 	public Coordinate<E> getCoordinate(){return position.clone();}
 	public Color getColor(){return new Color(base_color.toARGB());}
 	
 	public void setPosition(Coordinate<E> position){
 		this.position = position;
 	}
 	
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
	  action_listeners.add(listener);
 	}
 	
 	private class Callback implements Runnable{
 		Block<E> owner;
 		public Callback(Block<E> owner){
 			this.owner = owner;
 		}
 		@Override
 		public void run() {
 			owner.fireActionEvent();
 		}
 	}
 }
