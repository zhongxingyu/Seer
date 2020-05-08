 package by.epamlab.elevator.ui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.geom.Dimension2D;
 import java.util.Random;
 
 import org.jsfml.system.Vector2f;
 import org.jsfml.system.Vector2i;
 
 public class BaseWidget implements DynamicWidget {
 	/* Inspired by SFML */
 	static final Vector2i DEFAULT_POSITION = new Vector2i(0, 0);
 	static final Vector2i DEFAULT_SIZE = new Vector2i(20, 20);
 	protected Vector2i position;
 	protected Vector2i size;
 	
 	public BaseWidget() {
 		position = DEFAULT_POSITION;
 		size = DEFAULT_SIZE;
 	}
 	
 	public BaseWidget(Vector2i position) {
 		this.position = position;
		size = DEFAULT_SIZE;
 	}
 	
 	public BaseWidget(Vector2i position, Vector2i size) {
 		this.position = position;
 		this.size = size;
 	}
 	
 	@Override
 	public void draw(Graphics target) {
 		target.setColor(Color.WHITE);
 		target.drawRect(position.x, position.y, size.x, size.y);
 	}
 	
 	@Override
 	public void update(float dt) {
 		//pass
 	}
 
 }
