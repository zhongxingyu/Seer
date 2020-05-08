 package notify;
 
 import java.util.Deque;
 import java.util.LinkedList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 public class Notification {
 	
 	private static final Deque<Notification> notifications = new LinkedList<Notification>();
 	
 	public static void addNotification(String str){
 		notifications.addFirst(new Notification(str));
 	}
 	
 	public static void update(GameContainer gc){
 		for(Notification n : notifications){
 			n.update();
 		}
 		while(!notifications.isEmpty() && !notifications.peekLast().isVisible()){
 			notifications.pollLast();
 		}
 	}
 	
 	private static final int NOTIFICATION_RENDER_OFFSET = 10;
 	private static final int NOTIFICATION_SPACING = 10;
 	private static final Color NOTIFICATION_COLOUR = Color.white;
 	
 	public static void render(GameContainer gc, Graphics g){
 		g.setColor(NOTIFICATION_COLOUR);
 		int y = NOTIFICATION_RENDER_OFFSET;
 		int dy = g.getFont().getLineHeight() + NOTIFICATION_SPACING;
 		for(Notification n : notifications){
 			n.render(g, NOTIFICATION_RENDER_OFFSET, y);
 			y += dy;
 		}
 	}
 	
 	public static int getNotificationCount(){
 		return notifications.size();
 	}
 	
	private static final float OPACITY_DELTA = -0.004f;
 	
 	private final String str;
 	
 	private float opacity = 1.0f;
 	
 	protected Notification(String str){
 		this.str = str;
 	}
 	
 	public boolean isVisible() {
 		return opacity > 0f;
 	}
 	
 	public void update() {
 		opacity += OPACITY_DELTA;
 	}
 	
 	public void render(Graphics g, int x, int y) {
 		Color c = g.getColor();
 		c.a = opacity;
 		g.setColor(c);
 		g.drawString(str, x, y);
 	}
 	
 }
