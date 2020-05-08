 package no.eirikb.h5z1.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.graphics.shapes.ShapeBuilder;
 import no.eirikb.h5z1.client.resources.Resources;
 import no.eirikb.h5z1.client.visualbody.VisualPlayer;
 import no.eirikb.h5z1.client.visualbody.VisualStatic;
 import no.eirikb.h5z1.visual.VisualBody;
 import no.eirikb.h5z1.visual.VisualImage;
 
 import org.jbox2d.collision.CircleShape;
 import org.jbox2d.collision.PolygonShape;
 import org.jbox2d.collision.Shape;
 import org.jbox2d.collision.ShapeType;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.common.XForm;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.World;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.dom.client.Style.Cursor;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 public class GameMap extends FocusPanel {
 	private MapBuilder mapBuilder;
 	private int viewportWidth;
 	private int viewportHeight;
 	private float transX;
 	private float transY;
 	private float scaleFactor = 20.0f;
 	private float yFlip = -1.0f; // flip y coordinate
 	private AbsolutePanel container;
 	private AbsolutePanel background1;
 	private AbsolutePanel background2;
 	private AbsolutePanel background3;
 	private AbsolutePanel foreground;
 	private Surface surface;
 	private SimplePanel mouseBlock;
 
 	public GameMap(World world, MapBuilder mapBuilder, int width, int height,
 			int viewportWidth, int viewportHeight) {
 		super();
 		setSize(viewportWidth + "px", viewportHeight + "px");
 		this.mapBuilder = mapBuilder;
 		this.viewportWidth = viewportWidth;
 		this.viewportHeight = viewportHeight;
 
 		getElement().getStyle().setCursor(Cursor.CROSSHAIR);
 
 		container = new AbsolutePanel();
 		container.setSize(width + "px", height + "px");
 
 		background1 = new AbsolutePanel();
 		background1.setSize((width / 2) + "px", height + "px");
 		container.add(background1, 0, 0);
 		background2 = new AbsolutePanel();
 		background2.setSize(width + "px", height + "px");
 		container.add(background2, 0, 0);
 		background3 = new AbsolutePanel();
 		background3.setSize(width + "px", height + "px");
 		container.add(background3, 0, 0);
 		surface = new Surface(viewportWidth, viewportHeight);
 		container.add(surface);
 		foreground = new AbsolutePanel();
 		foreground.setSize(width + "px", height + "px");
 		container.add(foreground, 0, 0);
 		mouseBlock = new SimplePanel();
 		mouseBlock.setSize(viewportWidth + "px", viewportHeight + "px");
 		container.add(mouseBlock, 0, 0);
 		add(container);
 
 		getElement().getStyle().setOverflow(Overflow.HIDDEN);
 
 		setCamera(-2, 2, 20);
 
 		for (Body b = world.getBodyList(); b != null; b = b.m_next) {
 			if (b instanceof VisualStatic) {
 				XForm xf = b.getXForm();
 				VisualStatic visualStatic = (VisualStatic) b;
 				int minX = Integer.MAX_VALUE;
 				int maxX = 0;
 				int minY = Integer.MAX_VALUE;
 				int maxY = 0;
 				for (Shape s = b.m_shapeList; s != null; s = s.m_next) {
 					PolygonShape poly = (PolygonShape) s;
 					int vertexCount = poly.getVertexCount();
 					Vec2[] localVertices = poly.getVertices();
 					for (int i = 0; i < vertexCount; ++i) {
 						Vec2 v1 = worldToScreen(XForm.mul(xf, localVertices[i]));
 						if (v1.x < minX) {
 							minX = (int) v1.x;
 						}
 						if (v1.x > maxX) {
 							maxX = (int) v1.x;
 						}
 						if (v1.y < minY) {
 							minY = (int) v1.y;
 						}
 						if (v1.y > maxY) {
 							maxY = (int) v1.y;
 						}
 					}
 				}
 				Image image = new Image(visualStatic.getImage());
 				if (visualStatic.getRepeat()) {
 					int w = maxX - minX;
 					for (int i = 0; i < w / image.getWidth(); i++) {
 						background3.add(image, minX + (i * image.getWidth()),
 								minY);
 						image = new Image(visualStatic.getImage());
 					}
 				}
 			}
 		}
 
 		Image image = new Image(Resources.INSTANCE.bg2());
 		for (int i = 0; i < (width / image.getWidth()) + 1; i++) {
 			background1.add(image, i * image.getWidth(), 0);
 			image = new Image(Resources.INSTANCE.bg2());
 		}
 	}
 
 	private final float map(float value, float istart, float istop,
 			float ostart, float ostop) {
 		return ostart + (ostop - ostart)
 				* ((value - istart) / (istop - istart));
 	}
 
 	public void setCamera(float x, float y, float scale) {
 		transX = map(x, 0.0f, -1.0f, viewportWidth * .5f, viewportWidth * .5f
 				+ scale);
 		transY = map(y, 0.0f, yFlip * 1.0f, viewportHeight * .5f,
 				viewportHeight * .5f + scale);
 		scaleFactor = scale;
 	}
 
 	public Vec2 worldToScreen(Vec2 world) {
 		float x = map(world.x, 0f, 1f, transX, transX + scaleFactor);
 		float y = map(world.y, 0f, 1f, transY, transY + scaleFactor);
 		if (yFlip == -1.0f)
 			y = map(y, 0f, viewportHeight, viewportHeight, 0f);
 		return new Vec2(x, y);
 	}
 
 	public Vec2 worldToScreen(float x, float y) {
 		return worldToScreen(new Vec2(x, y));
 	}
 
 	public Vec2 screenToWorld(Vec2 screen) {
 		float x = map(screen.x, transX, transX + scaleFactor, 0f, 1f);
 		float y = 0;
 		if (yFlip == -1.0f)
 			y = map(y, viewportHeight, 0f, 0f, viewportHeight);
 		y = map(y, transY, transY + scaleFactor, 0f, 1f);
 		return new Vec2(x, y);
 	}
 
 	public Vec2 screenToWorld(float x, float y) {
 		return screenToWorld(new Vec2(x, y));
 	}
 
 	public void draw(long box2DTime, int fps) {
 		long canvasTime = System.currentTimeMillis();
 		int newX = (int) transX - (viewportWidth / 2) - 40;
 		container.setWidgetPosition(background1, (int) newX / 2, 0);
 		container.setWidgetPosition(background2, newX, 0);
 		container.setWidgetPosition(background3, newX, 0);
 		container.setWidgetPosition(surface, 0, 0);
 		container.setWidgetPosition(foreground, newX, 0);
 		container.setWidgetPosition(mouseBlock, 0, 0);
 		surface.clear();
 		for (Body body : mapBuilder.getVisualBodies()) {
 			VisualBody visualBody = (VisualBody) body;
 			for (VisualImage image : visualBody.getImages()) {
 				Vec2 v = worldToScreen(body.getPosition());
 				visualBody.setVisualX((int) v.x);
 				visualBody.setVisualY((int) v.y);
 				ImageElement ie = image.getImage();
 				if (ie != null) {
 					if (body.getAngle() != 0) {
 						surface.translate(v.x, v.y);
 						surface.rotate(-body.getAngle());
 						surface.drawImage(ie, 0 + image.getOffX(),
 								0 + image.getOffY());
 						surface.rotate(body.getAngle());
 						surface.translate(-v.x, -v.y);
 					} else {
 						surface.drawImage(ie, v.x + image.getOffX(), v.y
 								+ image.getOffY());
 					}
 				}
 			}
 		}
 
 		VisualPlayer me = mapBuilder.getMe();
 		ImageElement flame = me.getFlame();
 		if (flame != null) {
 			Vec2 v = worldToScreen(me.getPosition());
 			double cosin[] = me.getCosin();
 			float x = v.x + (float) cosin[0] * 15;
			float y = v.y + (float) cosin[1] * 15 - 10;
 
			double a = cosin[0] > 0 ? cosin[1] - 0.3 : (-cosin[1] + Math.PI);
 
 			surface.translate(x, y);
 			surface.rotate(a);
 			surface.drawImage(flame, 0, 0);
 			surface.rotate(-a);
 			surface.translate(-x, -y);
 		}
 
 		for (Body body : mapBuilder.getBodies()) {
 			XForm xf = body.getXForm();
 			for (Shape s = body.m_shapeList; s != null; s = s.m_next) {
 				surface.setStrokeStyle(new Color(0, 0, 0));
 				if (body.m_invMass == 0.0f) {
 					surface.setStrokeStyle(new Color(100, 100, 100));
 				} else if (body.isSleeping()) {
 					surface.setStrokeStyle(new Color(200, 200, 200));
 				} else {
 					surface.setStrokeStyle(new Color(30, 30, 30));
 				}
 				if (s.getType() == ShapeType.CIRCLE_SHAPE) {
 					CircleShape circle = (CircleShape) s;
 					Vec2 center = XForm.mul(xf, circle.getLocalPosition());
 					float radius = circle.getRadius() * scaleFactor;
 					center = worldToScreen(center);
 					surface.strokeShape(new ShapeBuilder().drawCircle(center.x,
 							center.y, radius).build());
 				} else if (s.getType() == ShapeType.POLYGON_SHAPE) {
 					PolygonShape poly = (PolygonShape) s;
 					int vertexCount = poly.getVertexCount();
 					Vec2[] localVertices = poly.getVertices();
 					boolean inside = false;
 					ShapeBuilder shapeBuilder = new ShapeBuilder();
 					for (int i = 0; i < vertexCount; ++i) {
 						int ind = (i + 1 < vertexCount) ? i + 1
 								: (i + 1 - vertexCount);
 						Vec2 v1 = worldToScreen(XForm.mul(xf, localVertices[i]));
 						Vec2 v2 = worldToScreen(XForm.mul(xf,
 								localVertices[ind]));
 						shapeBuilder.drawLineSegment(v1.x, v1.y, v2.x, v2.y);
 						if (v1.x >= 0 && v1.x < viewportWidth && v1.y >= 0
 								&& v1.y < viewportHeight) {
 							inside = true;
 						}
 					}
 					if (inside) {
 						surface.strokeShape(shapeBuilder.build());
 					}
 				}
 			}
 		}
 		surface.setStrokeStyle(new Color(255, 255, 255));
 		surface.strokeText("FPS: " + fps, 0, 10);
 		surface.strokeText("Box2D: " + box2DTime, 0, 20);
 		canvasTime = System.currentTimeMillis() - canvasTime;
 		surface.strokeText("Canvas: " + canvasTime, 0, 30);
 	}
 }
