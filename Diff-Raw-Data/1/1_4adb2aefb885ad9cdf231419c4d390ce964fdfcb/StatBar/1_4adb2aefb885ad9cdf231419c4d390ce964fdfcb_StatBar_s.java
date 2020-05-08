 package yuuki.sprite;
 
 import java.awt.Color;
 
 import javax.swing.JLayeredPane;
 
 import yuuki.animation.Animation;
import yuuki.animation.MotionTween;
 import yuuki.animation.SizeTween;
 import yuuki.animation.engine.Animator;
 import yuuki.entity.VariableStat;
 import yuuki.sprite.Rectangle;
 import yuuki.sprite.Sprite;
 
 /**
  * Displays a VariableStat in a bar.
  */
 @SuppressWarnings("serial")
 public class StatBar extends Sprite {
 	
 	/**
 	 * The color of the unfilled portion of all StatBar instances.
 	 */
 	public static final Color BACKGROUND_COLOR = new Color(79, 76, 0);
 	
 	/**
 	 * The color of the border of all StatBar instances.
 	 */
 	public static final Color BORDER_COLOR = new Color(0, 0, 0);
 	
 	/**
 	 * The level of the fighter who owns this StatBar's VariableStat.
 	 */
 	private int level;
 	
 	/**
 	 * The VariableStat that this StatBar is showing.
 	 */
 	private VariableStat stat;
 	
 	/**
 	 * The currently-displayed value of this StatBar.
 	 */
 	private int value;
 	
 	/**
 	 * The filled portion of this StatBar.
 	 */
 	private Rectangle foreground;
 	
 	/**
 	 * The background portion of this StatBar.
 	 */
 	private Rectangle background;
 	
 	/**
 	 * Creates a new StatBar with the given dimensions and whose filled portion
 	 * is the given color.
 	 * 
 	 * @param animator The handler for this StatBar's animations.
 	 * @param width The total width of the StatBar in pixels.
 	 * @param height The total height of the StatBar in pixels.
 	 * @param barColor The color of the filled portion of the StatBar.
 	 */
 	public StatBar(Animator animator, int width, int height, Color barColor) {
 		super(animator, width, height);
 		this.stat = null;
 		this.value = 0;
 		this.level = 1;
 		background = new Rectangle(animator, getWidth(), getHeight());
 		foreground = new Rectangle(animator, getWidth(), getHeight());
 		background.setBorderColor(BORDER_COLOR);
 		background.setFillColor(BACKGROUND_COLOR);
 		foreground.setBorderColor(null);
 		foreground.setFillColor(barColor);
 		JLayeredPane pane = new JLayeredPane();
 		pane.add(background, new Integer(0));
 		pane.add(foreground, new Integer(1));
 		pane.setBounds(0, 0, getWidth(), getHeight());
 		add(pane);
 	}
 	
 	/**
 	 * Gets the percent of this StatBar that is currently filled.
 	 * 
 	 * @return The percent filled.
 	 */
 	public double getPercent() {
 		return (double) stat.getCurrent() / stat.getMax(level);
 	}
 	
 	/**
 	 * Checks whether this StatBar is displaying the value of the given
 	 * VariableStat.
 	 * 
 	 * @param stat The stat to check.
 	 * 
 	 * @return Whether the given VariableStat and this StatBar's VariableStat
 	 * reference the same instance.
 	 */
 	public boolean isWatching(Object stat) {
 		return (this.stat == stat);
 	}
 	
 	/**
 	 * Sets the level of the fighter that owns this StatBar's VariableStat.
 	 * 
 	 * @param level The level.
 	 */
 	public void setLevel(int level) {
 		this.level = level;
 	}
 	
 	/**
 	 * Sets this StatBar to be associated with a VariableStat. Subsequent calls
 	 * to update() will use this VaraibleStat to set values.
 	 * 
 	 * @param stat The VariableStat to associate with this StatBar.
 	 */
 	public void setStat(VariableStat stat) {
 		this.stat = stat;
 		this.value = stat.getCurrent();
 	}
 	
 	/**
 	 * Updates this StatBar to show the value of its associated VariableStat.
 	 * If it is a new value, this StatBar is repainted.
 	 */
 	public void update() {
 		if (stat.getCurrent() != this.value) {
 			int targetWidth = (int) Math.round(getWidth() * getPercent());
 			int dw = targetWidth - foreground.getWidth();
 			Animation updateAnimation;
 			updateAnimation = new SizeTween(foreground, 1000, dw, 0);
 			try {
 				Animator.animateAndWait(animator, updateAnimation);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void advance(int fps) {}
 	
 }
