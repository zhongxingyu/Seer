 package client.views.solar;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.Map;
 
 import client.GameView;
 import client.everVoidClient;
 import client.graphics.FrameUpdate;
 import client.graphics.Grid.HoverMode;
 import client.graphics.GridNode;
 import client.graphics.UIShip;
 import client.graphics.geometry.AnimatedTransform.DurationMode;
 import client.graphics.geometry.Geometry;
 import client.graphics.geometry.Transform;
 
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class SolarSystemView extends GameView
 {
 	private final static float aGridScrollBorder = 0.2f;
 	private final static float aGridScrollSpeed = 1024f;
 	private final SolarSystemGrid aGrid;
 	private final Transform aGridOffset;
 	private Point aGridPoint = null;
 	private Rectangle aGridScrollRegion = new Rectangle(0, 0, everVoidClient.sScreenWidth, everVoidClient.sScreenHeight);
 	private final Vector2f aGridTranslation = new Vector2f();
 	private Transform aShipRotation;
 	private UIShip tmpShip;
 	private GridNode tmpShipGrid;
 
 	public SolarSystemView()
 	{
 		super();
 		aGrid = new SolarSystemGrid();
 		addNode(aGrid);
 		aGrid.setHandleHover(HoverMode.ON);
 		aGrid.setHoverColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
 		aGridOffset = aGrid.getNewTransform();
 		aGridOffset.setMinimumConstraint(aGridScrollRegion.width - aGrid.getTotalWidth(), aGridScrollRegion.height
 				- aGrid.getTotalHeight());
 		aGridOffset.setMaximumConstraint(2, 2);
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		aGridOffset.move(aGridTranslation.mult(f.aTpf));
 		// Hovered square
 		final Vector2f gridPosition = f.getMousePosition().add(aGridOffset.getTranslation2f().negate());
 		final Point gridPoint = aGrid.handleOver(gridPosition);
		if (gridPoint != null && !gridPoint.equals(aGridPoint))
 		{
 			tmpShip.getRotationAnimation().setTargetPoint(
 					aGrid.getCellCenter(gridPoint).subtract(tmpShipGrid.getCellCenter()));
 			aGridPoint = gridPoint;
 		}
 	}
 
 	@Override
 	public void onMouseMove(final String name, final float isPressed, final float tpf, final Vector2f position)
 	{
 		// Recompute grid scrolling speed
 		aGridTranslation.set(0, 0);
 		for (final Map.Entry<Geometry.Border, Float> e : Geometry.isInBorder(position, aGridScrollRegion,
 				SolarSystemView.aGridScrollBorder).entrySet())
 		{
 			aGridTranslation.addLocal(-e.getKey().getXDirection() * e.getValue() * SolarSystemView.aGridScrollSpeed, -e
 					.getKey().getYDirection() * e.getValue() * SolarSystemView.aGridScrollSpeed);
 		}
 	}
 
 	@Override
 	public void resolutionChanged()
 	{
 		aGridScrollRegion = new Rectangle(0, 0, everVoidClient.sScreenWidth, everVoidClient.sScreenHeight);
 	}
 
 	/**
 	 * Temporary; delete once engine is done
 	 */
 	public void sampleGame()
 	{
 		tmpShip = new UIShip();
 		tmpShipGrid = aGrid.addGridNode(tmpShip, 4, 10);
 		tmpShip.setHue(ColorRGBA.Red);
 		aShipRotation = tmpShip.getNewTransform();
 		tmpShip.getRotationAnimation().setSpeed(1.2f).setDurationMode(DurationMode.CONTINUOUS);
 	}
 }
