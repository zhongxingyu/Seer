 package asciiWorld.entities;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import asciiWorld.Camera;
 import asciiWorld.Convert;
 import asciiWorld.Direction;
 import asciiWorld.IHasPosition;
 import asciiWorld.IHasRangeOfVision;
 import asciiWorld.animations.FadingTextAnimation;
 import asciiWorld.animations.IAnimation;
 import asciiWorld.chunks.Chunk;
 import asciiWorld.collections.MethodIterator;
 import asciiWorld.lighting.ConvexHull;
 import asciiWorld.lighting.IConvexHull;
 import asciiWorld.lighting.Light;
 import asciiWorld.math.MathHelper;
 import asciiWorld.math.Vector3f;
 import asciiWorld.tiles.Tile;
 
 public class Entity implements IHasPosition, IHasRangeOfVision, IConvexHull {
 	
 	public static final float MOVEMENT_STEP = 8.0f;
 	
 	private static final int DEFAULT_AGILITY = 1;
 	private static final int DEFAULT_STRENGTH = 2;
 	private static final int DEFAULT_PERCEPTION = 10;
 	private static final float DEFAULT_WEIGHT = 1;
 	private static final int DEFAULT_MAX_HEALTH = 10;
 	
 	private static final float MODIFIER_AGILITY = 15.0f;
 	private static final float SPEED_TIME_DIVISOR = 250f; // 20.0f;
 	private static final int HEALTH_REGEN_UPDATE_MS = 1000;
 	private static final float USE_COOLDOWN_FACTOR = 15000.0f;
 	
 	private int _totalTimeAlive;
 	
 	private Chunk _chunk;
 	
 	private Tile _tile;
 	private List<IAnimation> _animations;
 	
 	private Direction _direction;
 	private Vector3f _position;
 	private Vector3f _moveFromPosition;
 	private Vector3f _moveToPosition;
 	private float _movementWeight;
 	
 	private String _name;
 	
 	private int _agility;
 	private int _perception;
 	private int _strength;
 	private float _weight;
 	
 	private int _health;
 	private int _maxHealth;
 	private int _healthRegenRate;
 	private int _lastHealthRegenTime;
 	
 	/**
 	 * The container that contains this entity.
 	 */
 	private InventoryContainer _container;
 	
 	/**
 	 * The inventory contained by this entity.
 	 */
 	private InventoryContainer _inventory;
 	
 	/**
 	 * This is the item that this entity is currently using.
 	 */
 	private Entity _activeItem;
 	
 	private boolean _ableToUseSomething;
 	private boolean _somethingIsBeingUsed;
 	private int _lastUseTime;
 	
 	private List<EntityComponent> _components;
 	
 	/*
 	public static Entity load(String path) throws Exception {
 		return fromXml((Element)new SAXBuilder().build(new File(path)).getRootElement());
 	}
 	*/
 	
 	/*
 	public static Entity fromXml(Element elem) throws Exception {
 		Entity newEntity = new Entity();
 		newEntity.setName(elem.getAttributeValue("name"));
 		newEntity.setTile(TileFactory.get().getResource(elem.getAttributeValue("tile")));
 		
 		loadComponents(newEntity, elem.getChild("Components"));
 		loadInventory(newEntity, elem.getChild("Inventory"));
 		loadProperties(newEntity, elem.getChild("Properties"));
 		
 		return newEntity;
 	}
 	*/
 	
 	/*
 	private static void loadComponents(Entity newEntity, Element componentsElem) throws Exception {
 		if (componentsElem != null) {
 			List<Element> componentElems = componentsElem.getChildren("Component");
 			for (Element componentElem : componentElems) {
 				newEntity.getComponents().add(new EntityComponentTemplate(componentElem).createInstance(newEntity));
 			}
 		}
 	}
 	*/
 	
 	/*
 	private static void loadInventory(Entity newEntity, Element inventoryElem) throws Exception {
 		if (inventoryElem != null) {
 			List<Element> itemElems = inventoryElem.getChildren("Item");
 			for (Element itemElem : itemElems) {
 				String itemName = itemElem.getAttributeValue("name");
 				newEntity.getInventory().add(EntityFactory.get().getResource(itemName));
 			}
 		}
 	}
 	*/
 	
 	/*
 	private static void loadProperties(Entity newEntity, Element propertiesElem) throws Exception {
 		if (propertiesElem != null) {
 			List<Element> propertyElems = propertiesElem.getChildren("Property");
 			for (Element propertyElem : propertyElems) {
 				String propertyName = propertyElem.getAttributeValue("name");
 				String propertyValue = propertyElem.getAttributeValue("value");
 				newEntity.setProperty(propertyName, propertyValue);
 			}
 		}
 	}
 	*/
 	
 	public Entity() {
 		_totalTimeAlive = 0;
 		
 		_chunk = null;
 		_tile = null;
 		_animations = new ArrayList<IAnimation>();
 		_position = new Vector3f();
 		_moveFromPosition = new Vector3f();
 		_moveToPosition = new Vector3f();
 		_movementWeight = 0.0f;
 		_direction = Direction.South;
 		
 		setName("");
 		
 		setMaxHealth(DEFAULT_MAX_HEALTH);
 		setHealth(getMaxHealth());
 		setHealthRegenRate(0); // no health regeneration by default
 		_lastHealthRegenTime = 0;
 		
 		setAgility(DEFAULT_AGILITY);
 		setStrength(DEFAULT_STRENGTH);
 		setPerception(DEFAULT_PERCEPTION);
 		setWeight(DEFAULT_WEIGHT);
 		
 		setContainer(null);
 		try {
 			_inventory = new InventoryContainer(this);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Unable to initialize the inventory container.");
 		}
 		setActiveItem(null);
 
 		setComponents(new ArrayList<EntityComponent>());
 		
 		_ableToUseSomething = true;
 		_somethingIsBeingUsed = false;
 		_lastUseTime = 0;
 	}
 	
 	public void setProperty(String propertyName, Object propertyValue) throws Exception {
 		Method method = MethodIterator.getMethods(getClass()).withName(String.format("set%s", propertyName)).first();
 		
 		if (method != null) {
 			Class<?>[] parameterTypes = method.getParameterTypes();
 			if (parameterTypes.length != 1) {
 				throw new Exception("The property setter must have 1, and only 1, parameter.");
 			} else {
 				method.invoke(this, Convert.changeType(propertyValue, parameterTypes[0]));
 				return;
 			}
 		} else {
 			throw new Exception(String.format("I do not understand this property name: %s", propertyName));
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 	
 	public InventoryContainer getContainer() {
 		return _container;
 	}
 	
 	public void setContainer(InventoryContainer value) {
 		_container = value;
 	}
 	
 	public InventoryContainer getInventory() {
 		return _inventory;
 	}
 	
 	public List<EntityComponent> getComponents() {
 		return _components;
 	}
 	
 	public void setComponents(List<EntityComponent> value) {
 		_components = value;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <TComponent extends EntityComponent> TComponent findComponent(Class<TComponent> type) {
 		for (EntityComponent component : getComponents()) {
 			if (type.isAssignableFrom(component.getClass())) {
 				return (TComponent)component;
 			}
 		}
 		return null;
 	}
 	
 	public Chunk getChunk() {
 		return _chunk;
 	}
 	
 	public void setChunk(Chunk chunk) {
 		if ((_chunk != null) && (_chunk != chunk)) {
 			Chunk removedFromChunk = _chunk;
 			for (EntityComponent component : getComponents()) {
 				component.beforeRemovedFromChunk(removedFromChunk);
 			}
 			
 			if (_chunk.containsEntity(this)) {
 				_chunk.removeEntity(this);
 			}
 			_chunk = null;
 			
 			for (EntityComponent component : getComponents()) {
 				component.afterRemovedFromChunk(removedFromChunk);
 			}
 		}
 		
 		if ((_chunk == null) && (chunk != null)) {
 			for (EntityComponent component : getComponents()) {
 				component.beforeAddedToChunk(chunk);
 			}
 			
 			_chunk = chunk;
 			if (!_chunk.containsEntity(this)) {
 				_chunk.addEntity(this);
 			}
 
 			for (EntityComponent component : getComponents()) {
 				component.afterAddedToChunk(chunk);
 			}
 		}
 	}
 	
 	public Tile getTile() {
 		return _tile;
 	}
 	
 	public void setTile(Tile value) {
 		_tile = value;
 	}
 	
 	public void addAnimation(IAnimation value) {
 		_animations.add(value);
 	}
 	
 	public double getDistanceFromEntity(Entity otherEntity) {
 		return getDistanceFromPoint(otherEntity.getOccupiedChunkPoint());
 	}
 	
 	public double getDistanceFromPoint(Vector2f chunkPoint) {
 		return Math.abs(chunkPoint.distance(getOccupiedChunkPoint()));
 	}
 	
 	public Vector3f getPosition() {
 		return _position;
 	}
 	
 	public Vector2f getCenterPosition() {
 		return new Vector2f(_position.x, _position.y).add(_tile.getCenter());
 	}
 	
 	public Vector2f getOccupiedChunkPoint() {
 		return Camera.translatePositionToPoint(_moveToPosition);
 	}
 	
 	public int getLayer() {
 		return (int)getPosition().z;
 	}
 	
 	public Boolean isMoving() {
 		return !_moveFromPosition.equals(_moveToPosition);
 	}
 	
 	public Direction getDirection() {
 		return _direction;
 	}
 	
 	public String getName() {
 		return _name;
 	}
 	
 	public void setName(String value) {
 		_name = value;
 	}
 	
 	public int getAgility() {
 		return _agility;
 	}
 	
 	public void setAgility(Integer value) {
 		_agility = value;
 	}
 	
 	public int getStrength() {
 		return _strength;
 	}
 	
 	public void setStrength(Integer value) {
 		_strength = value;
 	}
 	
 	public int getAttackStrength() {
 		return getStrength() / 15;
 	}
 	
 	public int getAttackSpeed() {
 		int attackSpeed = getStrength() * getAgility();
 		if (hasActiveItem()) {
 			attackSpeed += getActiveItem().getAgility();
 		}
 		return attackSpeed;
 	}
 	
 	public int getPerception() {
 		return _perception;
 	}
 	
 	public void setPerception(Integer value) {
 		_perception = value;
 	}
 	
 	/**
 	 * 
 	 * @return The weight of this entity by itself, i.e. without counting it's inventory.
 	 */
 	public float getBaseWeight() {
 		return _weight;
 	}
 	
 	/**
 	 * 
 	 * @return The weight of this entity, including it's inventory.
 	 */
 	public float getTotalWeight() {
 		float weight = getBaseWeight();
 		for (Entity item : getInventory()) {
 			weight += item.getTotalWeight();
 		}
 		return weight;
 	}
 	
 	public void setWeight(float value) {
 		_weight = value;
 	}
 	
 	public float getBaseMovementSpeed() {
 		return (float)getAgility() / MODIFIER_AGILITY;
 	}
 	
 	public float getMovementSpeed() {
 		float movementSpeed = getBaseMovementSpeed();
 		Chunk myChunk = getChunk();
 		
 		if (myChunk != null) {
 			Vector2f chunkPoint = getOccupiedChunkPoint();
 			Entity entity = myChunk.getEntityAt(chunkPoint, getLayer() - 1);
 			if (entity != null) {
 				movementSpeed *= entity.getTile().getFriction();
 			}
 		}
 		
 		return movementSpeed;
 	}
 	
 	public float getBaseRangeOfVision() {
 		return getPerception() * 3;
 	}
 	
 	public float getRangeOfVision() {
 		return getBaseRangeOfVision();
 	}
 	
 	public boolean isAlive() {
 		return getHealth() > 0;
 	}
 	
 	public int getMaxHealth() {
 		return _maxHealth;
 	}
 	
 	public void setMaxHealth(Integer value) {
 		_maxHealth = value;
 	}
 	
 	public int getHealth() {
 		return _health;
 	}
 	
 	private void setHealth(int value) {
 		_health = value;
 	}
 	
 	/**
 	 * Measured in health points per second.
 	 * @return A negative value here may imply poisoning.
 	 */
 	public int getHealthRegenRate() {
 		return _healthRegenRate;
 	}
 	
 	/**
 	 * Measured in health points per second.
 	 * @param A negative value here may imply poisoning.
 	 */
 	public void setHealthRegenRate(Integer value) {
 		_healthRegenRate = value;
 	}
 	
 	public void takeDamage(Entity damagedByEntity, int amount) {
 		int originalHealth = _health;
 		
 		setHealth(getHealth() - amount);
 		if (getHealth() < 0) {
 			setHealth(0);
 		} else if (getHealth() > getMaxHealth()) {
 			setHealth(getMaxHealth());
 		}
 		
 		if (originalHealth != _health) {
 			addAnimation(FadingTextAnimation.createDamageNotification(this, amount));
 		}
 	}
 	
 	public void restoreHealth(Entity healedByEntity, int amount) {
 		int originalHealth = _health;
 		
 		setHealth(getHealth() + amount);
 		if (getHealth() < 0) {
 			setHealth(0);
 		} else if (getHealth() > getMaxHealth()) {
 			setHealth(getMaxHealth());
 		}
 		
 		if (originalHealth != _health) {
 			addAnimation(FadingTextAnimation.createRestoreNotification(this, amount));
 		}
 	}
 	
 	public Entity getActiveItem() {
 		if (!getInventory().contains(_activeItem)) {
 			_activeItem = null;
 		}
 		return _activeItem;
 	}
 	
 	/**
 	 * The item will be added to this entity's inventory if it isn't already there.
 	 * 
 	 * @param item
 	 */
 	public void setActiveItem(Entity item) {
 		if (item != null) {
 			if (!getInventory().contains(item)) {
 				try {
 					getInventory().add(item);
 				} catch (Exception e) {
 					e.printStackTrace();
 					System.err.println("Unable to set the active item.");
 				}
 			}
 		}
 		_activeItem = item;
 	}
 	
 	public Boolean hasActiveItem() {
 		return getActiveItem() != null;
 	}
 	
 	public void useActiveItem(Vector3f targetChunkPoint) {
 		if (!_ableToUseSomething) {
 			return;
 		}
 		
 		if (hasActiveItem()) {
 			_somethingIsBeingUsed = true;
 			getActiveItem().use(targetChunkPoint);
 		}
 	}
 	
 	public void moveTo(Vector2f chunkPoint, float layer) {
 		_position = Camera.translatePointToPosition(chunkPoint, layer);
 		_moveFromPosition = _position.clone();
 		_moveToPosition = _position.clone();
 	}
 	
 	public void moveTo(Vector3f chunkPoint) {
 		moveTo(chunkPoint.toVector2f(), chunkPoint.z);
 	}
 	
 	public void move(Direction direction) {
 		if (direction == null) {
 			return;
 		}
 		
 		if (!isMoving()) {
 			Chunk cachedChunk = getChunk();
 			
 			_direction = direction;
 			
 			Vector3f pendingMoveToPosition = _moveToPosition.clone().add(_direction.toVector3f().multiply(MOVEMENT_STEP));
 			
 			// Check for a collision:
 			if (cachedChunk != null) {
 				Vector2f pendingChunkPoint = Camera.translatePositionToPoint(pendingMoveToPosition);
 				
 				Entity entity = getChunk().getEntityAt(pendingChunkPoint, getLayer());
 				if (entity != null) {
 					entity.collidedWith(this);
 					pendingMoveToPosition = _moveToPosition;
 				}
 			}
 			
 			_moveToPosition = pendingMoveToPosition;
 		}
 	}
 	
 	public void use(Vector3f targetChunkPoint) {
 		Entity owner = getContainer().getOwner();
 		for (EntityComponent component : getComponents()) {
 			component.use(owner, targetChunkPoint);
 		}
 	}
 	
 	public void collidedWith(Entity collidedWithEntity) {
 		for (EntityComponent component : getComponents()) {
 			component.collided(collidedWithEntity);
 		}
 	}
 	
 	public void touch() {
 		if (getChunk() == null) {
 			return;
 		}
 		
 		Vector2f chunkPoint = getOccupiedChunkPoint();
 		Vector2f directionVector = getDirection().toVector2f();
 		chunkPoint.x += directionVector.x;
 		chunkPoint.y += directionVector.y;
 		
 		Entity entity = getChunk().getEntityAt(chunkPoint, getLayer());
 		if (entity != null) {
 			entity.touched(this);
 		}
 	}
 	
 	public void touched(Entity touchedByEntity) {
 		for (EntityComponent component : getComponents()) {
 			component.touched(touchedByEntity);
 		}
 	}
 	
 	public void die() {
 		Vector2f chunkPoint = getOccupiedChunkPoint();
 		Chunk cachedChunk = getChunk();
 		_chunk.removeEntity(this);
 		
 		// Drop inventory into the chunk.
 		while (_inventory.getItemCount() > 0) {
 			Entity item = _inventory.getItemAt(0);
 			try {
 				_inventory.remove(item);
 			} catch (Exception e) {
 				System.err.println("Unable to clear the inventory.");
 				e.printStackTrace();
 				break;
 			}
 			item.moveTo(cachedChunk.findSpawnPoint(chunkPoint, getLayer()));
 			cachedChunk.addEntity(item);
 		}
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int deltaTime) {
 		_totalTimeAlive += deltaTime;
 		
 		if (getContainer() == null) {
 			if (isMoving()) {
 				_position = MathHelper.smoothStep(_moveFromPosition, _moveToPosition, _movementWeight);
 				_movementWeight += (deltaTime / SPEED_TIME_DIVISOR) * getMovementSpeed();
 				if (_movementWeight > 1.0f) {
 					_position = _moveToPosition.clone();
 					_moveFromPosition = _moveToPosition.clone();
 					_movementWeight = 0.0f;
 				}
 			}
 		}
 		
 		_tile.update(deltaTime);
 		
 		List<IAnimation> deadAnimations = new ArrayList<IAnimation>();
 		for (IAnimation animation : _animations) {
 			animation.update(deltaTime);
 			if (!animation.isAlive()) {
 				deadAnimations.add(animation);
 			}
 		}
 		while (!deadAnimations.isEmpty()) {
 			_animations.remove(deadAnimations.remove(0));
 		}
 		
 		for (EntityComponent component : getComponents()) {
 			component.update(container, game, deltaTime);
 		}
 		
 		updateUseVariables();
 		
 		updateHealthRegeneration(deltaTime);
 	}
 	
 	private void updateUseVariables() {
 		if (_somethingIsBeingUsed)
 		{
 			_ableToUseSomething = false;
 			_somethingIsBeingUsed = false;
 			_lastUseTime = _totalTimeAlive;
 		}
 		if (!_ableToUseSomething)
 		{
 			if ((_totalTimeAlive - _lastUseTime) >= (USE_COOLDOWN_FACTOR / getAttackSpeed()))
 			{
 				_ableToUseSomething = true;
 			}
 		}
 	}
 	
 	private void updateHealthRegeneration(int deltaTime) {
 		if (getHealthRegenRate() != 0) {
 			if ((_totalTimeAlive - _lastHealthRegenTime) >= HEALTH_REGEN_UPDATE_MS) {
 				restoreHealth(this, getHealthRegenRate());
 				_lastHealthRegenTime = _totalTimeAlive;
 			}
 		}
 	}
 	
 	public void render(Graphics g) {
 		/*if (isBlockedInAllDirections()) {
 			return;
 		}*/
 		
 		_tile.render(g, _position.x, _position.y);
 		
 		for (IAnimation animation : _animations) {
 			animation.render(g);
 		}
 	}
 
 	@Override
 	public void drawShadowGeometry(Light light) {
 		Vector2f lightPoint = Camera.translatePositionToPoint(light.getPosition());
 		ConvexHull.drawShadowGeometry(getPointsForShadow(lightPoint), light);
 	}
 	
 	private Vector2f[] getPointsForShadow(Vector2f lightPoint) {
 		float degAngle = (float)Math.atan2(getOccupiedChunkPoint().y - lightPoint.y, getOccupiedChunkPoint().x - lightPoint.x) * 360 / (float)Math.PI;
 		while (degAngle < 0) {
 			degAngle += 360;
 		}
 		degAngle = degAngle % 360;
 		
 		boolean eastWest = false;
 		if (((45 > degAngle) && (degAngle > 315)) ||
 			((135 < degAngle) && (degAngle < 225))) {
 			//System.err.println(String.format("false: %d", (int)degAngle));
 			eastWest = true;
 		} else {
 			//System.err.println(String.format("true: %d", (int)degAngle));
 		}
 		
 		Vector3f position = getPosition();
 		Vector2f size = getTile().getTileSet().getSize();
 		
 		float minX = position.x;
 		float minY = position.y;
 		float maxX = position.x + size.x;
 		float maxY = position.y + size.y;
 
 		int layer = getLayer();
 
 		if (eastWest) {
 			Vector2f chunkPoint = getOccupiedChunkPoint();
 			chunkPoint.x--;
 			while (chunkPoint.x >= 0) {
 				Entity occupyingEntity = getChunk().getEntityAt(chunkPoint, layer);
 				if (occupyingEntity == null) {
 					break;
 				}
 				if (!occupyingEntity.getName().equals(getName())) {
 					break;
 				}
 				if (occupyingEntity.getName().equals("Player")) { // players don't cast shadows
 					break;
 				}
 				/*if (chunkPoint.equals(lightPoint)) {
 					break;
 				}*/
 				minX -= size.x;
 				chunkPoint.x--;
 			}
 	
 			chunkPoint = getOccupiedChunkPoint();
 			chunkPoint.x++;
 			while (chunkPoint.x < Chunk.COLUMNS) {
 				Entity occupyingEntity = getChunk().getEntityAt(chunkPoint, layer);
 				if (occupyingEntity == null) {
 					break;
 				}
 				if (!occupyingEntity.getName().equals(getName())) {
 					break;
 				}
 				if (occupyingEntity.getName().equals("Player")) { // players don't cast shadows
 					break;
 				}
 				maxX += size.x;
 				chunkPoint.x++;
 			}
 		} else {
 			Vector2f chunkPoint = getOccupiedChunkPoint();
 			chunkPoint.y--;
 			while (chunkPoint.y >= 0) {
 				Entity occupyingEntity = getChunk().getEntityAt(chunkPoint, layer);
 				if (occupyingEntity == null) {
 					break;
 				}
 				if (!occupyingEntity.getName().equals(getName())) {
 					break;
 				}
 				if (occupyingEntity.getName().equals("Player")) { // players don't cast shadows
 					break;
 				}
 				minY -= size.y;
 				chunkPoint.y--;
 			}
 	
 			chunkPoint = getOccupiedChunkPoint();
 			chunkPoint.y++;
 			while (chunkPoint.y < Chunk.ROWS) {
 				Entity occupyingEntity = getChunk().getEntityAt(chunkPoint, layer);
 				if (occupyingEntity == null) {
 					break;
 				}
 				if (occupyingEntity.getName().equals("Player")) { // players don't cast shadows
 					break;
 				}
 				maxY += size.y;
 				chunkPoint.y++;
 			}
 		}
 		
 		return new Vector2f[] {
 			new Vector2f(minX, minY),
 			new Vector2f(maxX, minY),
 			new Vector2f(maxX, maxY),
 			new Vector2f(minX, maxY)
 		};
 	}
 	
 	/*
 	private boolean isBlockedInAllDirections() {
 		return
 				isChunkOccupiedInDirection(Direction.North, Chunk.LAYER_OBJECT) &&
 				isChunkOccupiedInDirection(Direction.South, Chunk.LAYER_OBJECT) &&
 				isChunkOccupiedInDirection(Direction.East, Chunk.LAYER_OBJECT) &&
 				isChunkOccupiedInDirection(Direction.West, Chunk.LAYER_OBJECT);
 	}
 	
 	private boolean isChunkOccupiedInDirection(Direction dir) {
 		return isChunkOccupiedInDirection(dir, getLayer());
 	}
 	
 	private boolean isChunkOccupiedInDirection(Direction dir, int layer) {
 		Vector2f chunkPoint = getOccupiedChunkPoint().copy().add(dir.toVector2f());
 		return getChunk().isSpaceOccupied(chunkPoint, layer);
 	}
 	*/
 }
