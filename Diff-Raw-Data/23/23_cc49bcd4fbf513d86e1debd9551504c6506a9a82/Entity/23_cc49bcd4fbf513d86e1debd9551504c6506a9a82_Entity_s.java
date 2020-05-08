 package game.entities;
 
 import game.entities.item.EquipItem;
 import game.entities.item.EtcItem;
 import game.entities.item.UsableItem;
 import game.entities.superentities.Monster;
 import game.structure.GameObject;
 import game.util.Util;
 
 import org.lwjgl.util.Dimension;
 import org.lwjgl.util.Point;
 import org.newdawn.slick.opengl.Texture;
 
 /**
  * An entity is any game object that can be in a map such as the player, NPCs,
  * monsters, objects, etc.
  */
 public abstract class Entity extends GameObject
 {
 	
 	//TODO abstract method parse implemented by concrete entities and called from this class' constructor
 
 	private Dimension renderSize = new Dimension(1, 1);
 	private Point renderOffset = new Point();
 	private boolean strong = false;
 	private Point position;
 	private Texture texture;
 	private boolean invisible = false;
 
 	public Entity(int id)
 	{
 		super(id);
 	}
 
 	public static Entity createEntity(int id)
 	{
 		// static constructor
 		EntityType type = EntityType.getType(id);
 
 		switch (type)
 		{
 		case Tile:
 			return new Tile(id);
 		case Portal:
 			return new Portal(id);
 		case EquipItem:
 			return new EquipItem(id);
 		case UseItem:
 			return new UsableItem(id);
 		case EtcItem:
 			return new EtcItem(id);
 		case Monster:
 			return new Monster(id);
 		case NPC:
 			return new NPC(id);
 		case Object:
 			return new Object(id);
 		}
 
 		throw new IllegalArgumentException("The ID does not match an entity type.");
 
 	}
 
 	public void render()
 	{
 		if (!isInvisible())
 			Util.renderEntity(getTexture(), Util.pointArithmetic(-1, position(), getMap().getOffSet()), getRenderOffset(),
 					getRenderSize());
 	}
 
 	public void UIRender()
 	{
 	}
 
 	public void update()
 	{
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>render</b> <br>
 	 * <p>
 	 * <tt>public void render(int x, int y)</tt>
 	 * </p>
 	 * Renders the texture of the entity at position <i>x, y</i> <br>
 	 * <br>
 	 */
 	public void render(int x, int y) //TODO make it a static method with an id parameter to render its texture (to be called by the inventory)
 	{
 		Util.renderEntity(getTexture(), new Point(0, 0), new Point(x, y), renderSize);
 	}
 
 	public Point position()
 	{
 		return position;
 	}
 
 	public int getX()
 	{
 		return position().getX();
 	}
 
 	public int getY()
 	{
 		return position().getY();
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>setPosition</b> <br>
 	 * <p>
 	 * <tt>public void setPosition(Point pos)</tt>
 	 * </p>
 	 * Removes <i>this</i> entity from the map and adds it to the map in
 	 * position <i>pos</i>. <br>
 	 * <br>
 	 */
 	public void setPosition(Point pos)
 	{
 		if (position() != null)
 		{
 			getMap().get(position()).remove(this);
 		}
 		getMap().get(pos).add(this);
 		position = new Point(pos); // save a reference to the position
 	}
 
 	public void setPosition(int x, int y)
 	{
 		setPosition(new Point(x, y));
 	}
 
 	public void setX(int x)
 	{
 		setPosition(x, getY());
 	}
 
 	public void setY(int y)
 	{
 		setPosition(getX(), y);
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>getPositionInGrid</b> <br>
 	 * <p>
 	 * <tt>public Point getPositionInGrid()</tt>
 	 * </p>
 	 * Returns the position of the entity relative to the camera. <br>
 	 * <br>
 	 */
 	public Point getPositionInGrid()
 	{
 		return new Point(getX() - getMap().getOffSet().getX(), getY() - getMap().getOffSet().getY());
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>setStrong</b> <br>
 	 * <p>
 	 * <tt>protected void setStrong()</tt>
 	 * </p>
 	 * Sets the entity to be strong. Entities are not strong by default. A slot
 	 * cannot have more than one strong entity. <br>
 	 * <br>
 	 * 
 	 * @see #setStrong()
 	 */
 	protected void setStrong()
 	{
 		strong = true;
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>setStrong</b> <br>
 	 * <p>
 	 * <tt>public boolean isStrong()</tt>
 	 * </p>
 	 * Returns whether the entity is strong or not. Entities are not strong by
 	 * default. A slot cannot have more than one strong entity.
 	 * <p>
 	 * 
 	 * @see #setStrong()
 	 */
 	public boolean isStrong()
 	{
 		return strong;
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>renderSize</b> <br>
 	 * <p>
 	 * <tt>public Dimension renderSize()</tt>
 	 * </p>
 	 * Returns the size in tiles that the texture will be rendered. <br>
 	 * <br>
 	 * 
 	 * @see com.game.structure.Slot#SIZE
 	 */
 	public Dimension getRenderSize()
 	{
 		return new Dimension(renderSize);
 	}
 
 	public void setRenderSize(int width, int height)
 	{
 		renderSize.setSize(width, height);
 	}
 
 	public Texture getTexture()
 	{
		return texture;
 	}
 
 	public void setTexture(Texture t)
 	{
 		texture = t; // reference to the texture held by TextureManager
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>getRenderOffSet</b> <br>
 	 * <p>
 	 * <tt>public Point getRenderOffSet()</tt>
 	 * </p>
 	 * Returns the offset in pixels of the texture rendered. <br>
 	 * <br>
 	 * 
 	 * @see #setRenderOffset(int x, int y)
 	 */
 	public Point getRenderOffset()
 	{
 		return new Point(renderOffset); // new point for immutability
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>setRenderOffSet</b> <br>
 	 * <p>
 	 * <tt>public void setRenderOffset(int x, int y)</tt>
 	 * </p>
 	 * Sets an offset in its render position in pixels.
 	 * <br>
 	 */
 	public void setRenderOffset(int x, int y)
 	{
 		renderOffset.setLocation(x, y);
 	}
 
 	public void move(Point move)
 	{
 		setPosition(getX() + move.getX(), getY() + move.getY());
 	}
 
 	/**
 	 * 
 	 * <br>
 	 * <b>modifyPos</b> <br>
 	 * <p>
 	 * <tt>public void modifyPos(Point pos)</tt>
 	 * </p>
 	 * Changes the position reference of the entity without changing its
 	 * position in the map. <br>
 	 * <br>
 	 */
 	public void modifyPos(Point pos)
 	{
 		position = pos;
 	}
 
 	public boolean isInvisible()
 	{
 		return invisible;
 	}
 
 	public void setInvisible(boolean inv)
 	{
 		invisible = inv;
 	}
 }
