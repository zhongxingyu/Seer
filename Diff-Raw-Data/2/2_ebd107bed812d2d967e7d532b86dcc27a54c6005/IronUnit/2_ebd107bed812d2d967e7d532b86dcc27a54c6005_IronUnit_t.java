 package fr.frozen.iron.common.entities;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 
 import fr.frozen.game.GameObject;
 import fr.frozen.game.ISprite;
 import fr.frozen.game.ISpriteManager;
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.skills.MeleeAttack;
 import fr.frozen.iron.common.skills.RangedAttack;
 import fr.frozen.iron.common.skills.Skill;
 import fr.frozen.iron.common.weapon.Weapon;
 import fr.frozen.iron.common.weapon.WeaponManager;
 import fr.frozen.iron.util.IronConfig;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronGL;
 import fr.frozen.iron.util.IronUtil;
 import fr.frozen.util.XMLParser;
 import fr.frozen.util.pathfinding.Mover;
 
 public class IronUnit extends GameObject implements Mover {
 	/*serializes as :
 	 * id - type - ownerId - float x - float y
 	 */
 	public static final int ACTION_MOVE = 0;
 	public static final int ACTION_SKILL = 1;
 	
 	public static final int TYPE_FOOTSOLDIER = 0;
 	public static final int TYPE_ARCHER = 1;
 	public static final int TYPE_CAVALRY = 2;
 	public static final int TYPE_HEALER = 3;
 	public static final int TYPE_MAGE = 4;
 	public static final int TYPE_SPECIAL = 5;
 	
 	public static String[] typeNames = {"footsoldier","archer","cavalry","healer","mage", "special"};
 	
 	protected List<int []> movementCache;
 	protected int [] movementCachePos;
 	
 	protected IronWorld world;
 	
 	protected int id;
 	protected int type;
 	protected int ownerId;
 	
 	protected int hp;
 	protected int maxHp;
 	
 	protected Weapon meleeWeapon;
 	protected Weapon rangedWeapon;
 	
 	protected int movement;
 	protected int maxMovement;
 	
 	protected Vector2f wantedPos = null;
 	
 	protected boolean selected = false;
 	protected boolean played = true;
 	
 	protected List<Skill> skills;
 
 	protected ISprite teamColorSprite = null;
 	protected ISprite weaponSprite = null;
 	
 	public IronUnit(IronWorld world, int id, int type, int ownerId, float x, float y) {
 		super(null, x, y);
 		this.id = id;
 		this.type = type;
 		this.ownerId = ownerId;
 		this.world = world;
 		skills = new ArrayList<Skill>();
 		addInitialSkills();
 		
 		movementCachePos = new int[2];
 		movementCachePos[0] = movementCachePos[1] = -1;
 		movementCache = new ArrayList<int[]>();
 		
 		if (world != null) getStats();
 	}
 	
 	public static IronUnit getUnit(char letter, IronWorld world, int entId, int clientId, int x, int y) {
 		switch (letter) {
 		case 'F' :
 			return getUnit(TYPE_FOOTSOLDIER, world, entId, clientId, x, y);
 		case 'A' :
 			return getUnit(TYPE_ARCHER, world, entId, clientId, x, y);
 		case 'H' :
 			return getUnit(TYPE_HEALER, world, entId, clientId, x, y);
 		case 'C' :
 			return getUnit(TYPE_CAVALRY, world, entId, clientId, x, y);
 		case 'M' :
 			return getUnit(TYPE_MAGE, world, entId, clientId, x, y);
 		case 'S' :
 			return getUnit(TYPE_SPECIAL, world, entId, clientId, x, y);
 		}
 		return null;
 	}
 	
 	public static IronUnit getUnit(int type, IronWorld world, int entId, int clientId, int x, int y) {
 		switch (type) {
 		case TYPE_FOOTSOLDIER :
 			return new FootSoldier(world, entId, clientId, x, y);
 			
 		case TYPE_ARCHER :
 			return new Archer(world, entId, clientId, x, y);
 			
 		case TYPE_HEALER :
 			return new Healer(world, entId, clientId, x, y);
 			
 		case TYPE_CAVALRY :
 			return new Cavalry(world, entId, clientId, x, y);
 			
 		case TYPE_MAGE :
 			return new Mage(world, entId, clientId, x, y);
 			
 		case TYPE_SPECIAL :
 			return new Special(world, entId, clientId, x, y);
 		}
 		return null;
 	}
 
 	//TODO : implement in subclasses
 	protected void addInitialSkills() {
 		synchronized (skills) {
 			
 			
 		}
 		/*skills.add(MeleeAttack.getInstance());
 		skills.add(RangedAttack.getInstance());*/
 	}
 	
 	public void onEndTurn() {
 		played = true;
 	}
 	
 	public int getHp() {
 		return hp;
 	}
 	
 	public int getMaxHp() {
 		return maxHp;
 	}
 
 	public void setMaxHp(int val) {
 		maxHp = val;
 	}
 	
 	public void setHp(int val) {
 		hp = Math.max(0, Math.min(val, maxHp));
 		//System.out.println("hp = "+hp);
 		if (hp <= 0) {
 			onDeath();
 		}
 	}
 	
 	public void onDeath() {
 		world.getMap().getTile((int)getX(), (int)getY()).setUnitOnTile(null);
 		world.addGameObject(this, "corpse");
 	}
 	
 	public void setCorpseSprite() {
 		setSprite(_spriteManager.getSprite("corpse_"+getRaceStr()));
 		_sprite.setAlpha(.8f);
 	}
 	
 	public void onStartTurn() {
 		played = isDead();
 		movement = maxMovement;
 	}
 	
 	public List<Skill> getSkills() {
 		synchronized (skills) {
 			return skills;
 		}
 	}
 	
 	public void addSkill(Skill s) {
 		synchronized (skills) {
 			skills.add(s);
 		}
 	}
 	
 	public void removeSkill(Skill skill) {
 		synchronized (skills) {
 			skills.remove(skill);
 		}
 	}
 	
 	public void move(int x, int y, int cost) {
 		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(null);
 		_pos.set(x, y);
 		world.getMap().getTile((int)_pos.getX(), (int)_pos.getY()).setUnitOnTile(this);
 		movement = Math.max(0, movement - cost);
 		
 		if (movement == 0) {
 			//TODO check if a unit cant do something after it moved
 			played = true;
 		}
 	}
 	
 	public void findSprite() {
 		String spriteName = typeNames[type]+"_"+getRaceStr();
 		setSprite(ISpriteManager.getInstance().getSprite(spriteName));
 		
 		String teamColorSpriteName = typeNames[type]+"_"+getRaceStr()+"_color";
 		if (ISpriteManager.getInstance().isSpriteLoaded(teamColorSpriteName)) {
 			teamColorSprite = ISpriteManager.getInstance().getSprite(teamColorSpriteName);
 		} else {
 			teamColorSprite = ISpriteManager.getInstance().getSprite(typeNames[type]+"_color");
 		}
 		
 		if (teamColorSprite != null) {
 			teamColorSprite.setColor(world.getContext().getPlayerInfo(ownerId).getColor());
 		} 
 		
 		/*if (weapon != null) {
 			//weaponSprite = ISpriteManager.getInstance().getSprite("weapon_"+weapon.getName());
 		} else {
 			System.out.println("WEAPON NULL, NOT GETTING ANY SPRITE");
 		}*/
 	}
 	
 	public int getMovement() {
 		return movement;
 	}
 	
 	protected String getRaceStr() {
 		return IronUtil.getRaceStr(world.getContext().getPlayerInfo(ownerId).getRace());
 	}
 	
 	public void getStats() {
 		//TODO throw exception if problem ?!
 		XMLParser ic = IronConfig.getIronXMLParser();
 		String racestr = getRaceStr();
 		String base = "unitstats/"+typeNames[type]+"/"+racestr;
 		
 		if (racestr == null) {
 			System.err.println("PROBLEM WHILE GETTING UNIT STATS");
 			return;
 		}
 		
 		hp = maxHp = Integer.parseInt(ic.getAttributeValue(base, "maxhp"));
 		movement = maxMovement = IronConst.MOVE_COST_DEFAULT * Integer.parseInt(ic.getAttributeValue(base, "movement"));
 		
 		if (meleeWeapon == null) {
 			String weaponName = ic.getAttributeValue(base, "meleeweapon");
 			if (weaponName != null && !weaponName.equals(""))
 				meleeWeapon = WeaponManager.getInstance().getWeapon(weaponName);
 		}
 		
 		if (rangedWeapon == null) {
 			String weaponName = ic.getAttributeValue(base, "rangedweapon");
 			if (weaponName != null && !weaponName.equals("")) {
 				rangedWeapon = WeaponManager.getInstance().getWeapon(weaponName);
 			}
 		}
 	}
 
 	public boolean hasPlayed() {
 		return played;
 	}
 	
 	public boolean isDead() {
 		return hp <= 0;
 	}
 	
 	public void setPlayed(boolean val) {
 		played = val;
 	}
 	
 	
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	public void setSelected(boolean val) {
 		selected = val;
 	}
 	
 	public IronWorld getWorld() {
 		return world;
 	}
 	
 	public int getOwnerId() {
 		return ownerId;
 	}
 	
 	public void setOwnerId(int val) {
 		ownerId = val;
 	}
 	
 	public void setWorld(IronWorld world) {
 		this.world = world;
 		if (world != null) getStats();
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	public Weapon getMeleeWeapon() {
 		return meleeWeapon;
 	}
 	
 	public Weapon getRangedWeapon() {
 		return rangedWeapon;
 	}
 
 	public void setMeleeWeapon(Weapon w) {
 		meleeWeapon = w;
 		setWeaponAux(w, MeleeAttack.getInstance());
 	}
 	
 	protected void setWeaponAux(Weapon w, Skill skill) {
 		if (w != null){
 			if (!skills.contains(skill)) {
 				addSkill(skill);
 			} else {
 				System.out.println("already contains skill "+skill);
 			}
 		} else {
 			if (skills.contains(skill)) {
 				removeSkill(skill);
 			}
 		}
 	}
 	
 	public void setRangedWeapon(Weapon w) {
 		rangedWeapon = w;
 		setWeaponAux(w, RangedAttack.getInstance());
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 	
 	public byte [] serialize() throws IOException {
 		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
 		byteArray.reset();
 		
 		byteArray.write(IronUtil.intToByteArray(id));
 		byteArray.write(IronUtil.intToByteArray(type));
 		byteArray.write(IronUtil.intToByteArray(ownerId));
 		
 		byteArray.write(IronUtil.intToByteArray((int)_pos.x));//supposed to be float, but we will use as int
 		byteArray.write(IronUtil.intToByteArray((int)_pos.y));
 		
 		int meleeWeaponId = -1;
 		if (meleeWeapon != null) {
 			meleeWeaponId = meleeWeapon.getId();
 		}
 		byteArray.write(IronUtil.intToByteArray(meleeWeaponId));
 		
 		int rangedWeaponId = -1;
 		if (rangedWeapon != null) {
 			rangedWeaponId = rangedWeapon.getId();
 		}
 		byteArray.write(IronUtil.intToByteArray(rangedWeaponId));
 		
 		return byteArray.toByteArray();
 	}
 	
 	@Override
 	public void render(float deltaTime) {
 		if (_sprite != null) {
 			_sprite.draw(_pos.x * IronConst.TILE_WIDTH, _pos.y * IronConst.TILE_HEIGHT);
 			
 			if (teamColorSprite != null && !isDead()) {
 				teamColorSprite.draw(_pos.x * IronConst.TILE_WIDTH, _pos.y * IronConst.TILE_HEIGHT);
 			}
 			
 			/*if (weapon != null && weaponSprite != null) {
 				if (weapon.isDisplayIdle()) {
 					weaponSprite.draw(_pos.x * IronConfig.TILE_WIDTH, _pos.y * IronConfig.TILE_HEIGHT);
 				}
 			}*/
 		} else {
 			findSprite();
 		}
 	}
 	
 	protected void drawHealthBar(int x, int y) {
 		IronGL.drawRect(x, y,IronConst.TILE_WIDTH, 9,
 				0.1f, 0.1f, 0.1f, 1f);
 		
 		IronGL.drawRect(x+1, y+1,IronConst.TILE_WIDTH - 2, 7,
 				0.5f, 0.5f, 0.5f, 1f);
 		
 		IronGL.drawRect(x+2, y+2,IronConst.TILE_WIDTH - 4, 5,
 				0.1f, 0.1f, 0.1f, 1f);
 		
 		
 		IronGL.drawRect(x+3, y+3, IronConst.TILE_WIDTH - 6, 3,
 				0f, 0f, 0f, 1f);
 		
 		float percentHealthLeft = (float)hp / maxHp;
 		
 		float healthWidth = percentHealthLeft * (IronConst.TILE_WIDTH - 6);
 		IronGL.drawRect(x+3, y+3, healthWidth, 3,
 				1 - percentHealthLeft, percentHealthLeft, 0f, 1f);
 	}
 	
 	public void renderStatusBars(float deltaTime) {
 		if (isDead() || _sprite == null) return;
 		
 		int y = (int)(getY() * IronConst.TILE_HEIGHT - (_sprite.getHeight() - IronConst.TILE_HEIGHT));
 		int x = (int)(getX() * IronConst.TILE_WIDTH);
 		y -= 11;
		if (y < 0) y = 0;
 		drawHealthBar(x,y);
 	}
 	
 	public void renderTileGfx(float deltaTime) {
 		if (!hasPlayed() 
 			&& world.getContext().getClientId() >= 0
 			&& world.getContext().getPlayerInfo(world.getContext().getClientId()).isTurnToPlay()) {
 			
 			IronGL.drawRect((int)getX() * IronConst.TILE_WIDTH,
 					(int)getY() * IronConst.TILE_HEIGHT,
 					IronConst.TILE_WIDTH,
 					IronConst.TILE_HEIGHT,
 					1f, 0.8f, 0f, 0.3f);
 			
 			if (isSelected()) {
 				IronGL.drawRect((int)_pos.x * IronConst.TILE_WIDTH,
 								(int)_pos.y * IronConst.TILE_HEIGHT,
 								IronConst.TILE_WIDTH,
 								IronConst.TILE_HEIGHT,
 								1f, 0f, 0f, 0.5f);
 			}//end if isSelected()
 		}
 	}
 	
 
 	public void renderMoveableTiles() {
 		if (!hasPlayed() && isSelected()
 				&& world.getContext().getClientId() >= 0
 				&& world.getContext().getPlayerInfo(world.getContext().getClientId()).isTurnToPlay()) {
 		
 			//if (_pos.getX() != movementCachePos[0] || _pos.getY() != movementCachePos[1]) {
 				//cache needs to be computed
 				int xstart, ystart, xend, yend;
 				movementCache.clear();
 				
 				xstart = Math.max(0, (int)_pos.getX() - getMovement() / IronConst.MOVE_COST_DEFAULT);
 				ystart = Math.max(0, (int)_pos.getY() - getMovement() / IronConst.MOVE_COST_DEFAULT);
 				
 				xend = Math.min(IronConst.MAP_WIDTH - 1, (int)_pos.getX() + getMovement() / IronConst.MOVE_COST_DEFAULT);
 				yend = Math.min(IronConst.MAP_HEIGHT - 1, (int)_pos.getY() + getMovement() / IronConst.MOVE_COST_DEFAULT);
 				
 				for (int i = xstart; i <= xend; i++) {
 					for (int j = ystart; j <= yend; j++) {
 						if (i == _pos.getX() && j == _pos.getY()) continue;
 						
 						if (world.getPath(getId(), i, j) != null) {
 							int []pos = new int[2];
 							pos[0] = i;
 							pos[1] = j;
 							movementCache.add(pos);
 						}
 					}
 				}//end for i
 				
 				movementCachePos[0] = (int)_pos.getX();
 				movementCachePos[1] = (int)_pos.getY();
 			//}//cache computed
 			
 			for (int [] p : movementCache) {
 				IronGL.drawRect(p[0] * IronConst.TILE_WIDTH,
 						p[1] * IronConst.TILE_HEIGHT,
 						IronConst.TILE_WIDTH,
 						IronConst.TILE_HEIGHT,
 						0.3f, 1f, 0f, 0.2f);
 			}
 		}
 	}
 	
 	public String toString() {
 		String str = super.toString();
 		str += " id = "+id+" type = "+type+" ownerId = "+ownerId+"\n";
 		str += "can perform skills : ";
 		for (Skill skill : skills) {
 			str += skill.getSkillName()+" ";
 		}
 		return str;
 	}
 }
