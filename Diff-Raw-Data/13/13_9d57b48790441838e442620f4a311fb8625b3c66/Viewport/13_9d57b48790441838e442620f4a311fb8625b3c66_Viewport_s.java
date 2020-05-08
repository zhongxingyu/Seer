 package ro7.engine;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 
 import ro7.engine.sprites.EmptyRectangle;
 import cs195n.Vec2f;
 
 public class Viewport {
 
 	private Vec2f position;
 	private Vec2f dimensions;
 	private GameSpace gameSpace;
 	private Vec2f scale;
 	private Vec2f gamePosition;
 
 	public Viewport(Vec2f position, Vec2f dimensions, GameSpace gameSpace) {
 		this.position = position;
 		this.dimensions = dimensions;
 		this.gameSpace = gameSpace;
 		this.scale = dimensions.pdiv(gameSpace.getDimensions());
 		this.gamePosition = new Vec2f(0.0f, 0.0f);
 	}
 
 	public Viewport(Vec2f position, Vec2f dimensions, GameSpace gameSpace,
 			Vec2f scale, Vec2f gamePosition) {
 		this.position = position;
 		this.dimensions = dimensions;
 		this.gameSpace = gameSpace;
 		this.scale = scale;
 		this.gamePosition = gamePosition;
 	}
 
 	private void doTransform(Graphics2D g) {
 		g.translate(position.x, position.y);
 		g.scale(scale.x, scale.y);
 		g.translate(-gamePosition.x, -gamePosition.y);
 	}
 
 	private void undoTransform(Graphics2D g) {
 		g.translate(gamePosition.x, gamePosition.y);
 		g.scale(1.0f / scale.x, 1.0f / scale.y);
 		g.translate(-position.x, -position.y);
 	}
 
 	public Vec2f screenToGame(Vec2f point) {
 		return point.minus(position).pdiv(scale).plus(gamePosition);
 	}
 
 	public void zoomIn(float factor) {
		Vec2f viewportDimensions = screenToGame(dimensions);
 
 		scale = scale.smult(factor);
 
 		Vec2f translateVector = viewportDimensions.smult(factor - 1).sdiv(2.0f);
 		translate(translateVector);
 	}
 
 	public void zoomOut(float factor) {
		Vec2f viewportDimensions = screenToGame(dimensions);
 
 		scale = scale.sdiv(factor);
 
 		Vec2f translateVector = viewportDimensions.smult(-(factor - 1)).sdiv(
 				2.0f);
 		translate(translateVector);
 	}
 
 	public void translate(Vec2f direction) {
 		gamePosition = gamePosition.plus(direction);
 	}
 
 	public void draw(Graphics2D g) {
 		EmptyRectangle viewport = new EmptyRectangle(position, dimensions,
 				Color.BLACK);
 		viewport.draw(g);
 
 		Shape clip = g.getClip();
 		g.setClip(viewport.getShape());
 		doTransform(g);
 
 		Vec2f min = screenToGame(position);
 		Vec2f max = screenToGame(dimensions);
 
 		gameSpace.draw(g, min, max);
 		undoTransform(g);
 		g.setClip(clip);
 	}
 
 	public Vec2f getGamePosition() {
 		return gamePosition;
 	}
 
 	public Vec2f getScale() {
 		return scale;
 	}
 
 }
