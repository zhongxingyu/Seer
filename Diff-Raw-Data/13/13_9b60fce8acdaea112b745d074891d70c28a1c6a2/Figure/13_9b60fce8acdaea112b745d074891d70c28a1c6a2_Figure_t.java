 package hhu.propra_2013.gruppe_13;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 /**
  * Enthält alle Informationen über die Spielfigur
  * @author Gruppe13
  *
  */
 
 
 class Figure extends CoreGameObjects {
 	
 	private long id;
 	private static final long serialVersionUID = 1L;
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	// set these for the viewing direction of the figure
 	static final int NONE			= 0;
 	static final int UP				= 1;
 	static final int DOWN			= 2;
 	static final int LEFT			= 3;
 	static final int RIGHT			= 4;
 	static final int UPLEFT			= 5;
 	static final int UPRIGHT		= 6;
 	static final int DOWNLEFT		= 7;
 	static final int DOWNRIGHT		= 8;
 	
 	// Movement direction of the figure, this is needed by the logic in a server implementation
 	private int 	player;
 	
 	/* hp: Hitpoints, money is, well, money and chocolate is a very fun stuff to many people eat way too much of. 
 	 * Here it is our mana though. */
 	private int 	hp;
 	private int 	money;
 	private int 	chocolate;
 	
 	// Position and velocity data, and the width and height of the figure 
 	private double 	x, y, r;
 	private double 	v_x, v_y;
 	private double 	width, height;
 	
 	// maximum amount of health slots and a cooldown for enemy contact. The latter is established via the System time. 
 	private int 	maxHP;
 	private long	cooldown;
 	
 	// armor variable and items, if armor is present it is reduced instead of health
 	private int 	armor;
 	private Item 	item1, item2, item3;
 	
 	// special attack and attack type is needed in order to draw the current attack
 	private boolean	specialAttack;
 	private int 	attackType;
 	private int 	bulletType;
 	private int 	bulletCoolDownTime;
 	
 	// this variables is needed to set the direction in which the figure is looking
 	private int 	direction = CoreLogic.NONE;
 	private int 	activity;
 	
 	// set the current figures color
 	private Color	figureColor;
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	// class constructor
 	
 	/**
 	 * Konstruktor
 	 * @param initX   		X-Startposition der Figur
 	 * @param initY	 		Y-Startposition der Figur
 	 * @param initHeight	Höhe der Figur
 	 * @param initWidth 	Breite der Figur
 	 * @param player		Die Nummer des Spielers dem die Figur gehört (Multiplayer)
 	 * @param figureColor	Farbe der Figur (Multiplayer)
 	 * @param id			Um die Figur für den Server eindeutig zu machen (Multiplayer)
 	 */
 	
 	Figure(double initX, double initY, double initHeight, double initWidth, int player, Color figureColor, long id) {
 		x 		= initX;
 		y 		= initY;
 		
 		width  	= initWidth;
 		height 	= initHeight;
 		
 		v_x 	= 0.3;
 		v_y 	= 0.3;
 		r   	= Math.max(width, height) + Math.pow(Math.ceil(Math.abs(v_x)), 2)*Math.pow(Math.ceil(Math.abs(v_y)), 2);
 		
 		hp  	= 15;
 		maxHP	= 8;
 		chocolate	= 10;
 		
 		item1 	= null;
 		item2 	= null;
 		item3 	= null;
 		
 		cooldown = System.currentTimeMillis();
 		armor	 = 5;
 		
 		bulletType = AttackBullet.PLAYER_BULLET_STD;
 		bulletCoolDownTime = 500;
 		
 		this.player = player;
 		this.figureColor = figureColor;
 		this.id = id;
 	}
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	@Override
 	long getID() {
 		return id;
 	}
 	
 	int getBulletCoolDownTime() {
 		return bulletCoolDownTime;
 	}
 	
 	int getBulletType() {
 		return bulletType;
 	}
 	
 	int getArmor(){
 		return armor;
 	}
 	
 	int getGeld(){
 		return money;
 	}
 	
 	
 	int getMaxHP(){
 		return maxHP;
 	}
 	
 	@Override
 	int getHP(){
 		return hp;
 	}
 	
 	@Override
 	double getPosX() {
 		return x;
 	}
 	
 	@Override
 	double getPosY() {
 		return y;
 	}
 	
 	@Override
 	double getRad() {
 		return r;
 	}
 	
 	@Override
 	double getVX() {
 		return v_x;
 	}
 	
 	@Override
 	double getVY() {
 		return v_y;
 	}
 	
 	@Override
 	double getWidth() {
 		return width;
 	}
 
 	@Override
 	double getHeight() {
 		return height;
 	}
 	
 	int getChocolate() {
 		return chocolate;
 	}
 	
 	int getPlayer() {
 		return player;
 	}
 	
 	int getDirection() {
 		return direction;
 	}
 	
 	int getActivity() {
 		return activity;
 	}
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	void setBulletCoolDownTime (int cooldown) {
 		bulletCoolDownTime = cooldown;
 	}
 	
 	void setBulletType (int inType) {
 		bulletType = inType;
 	}
 	
 	void setArmor(int inArmor){
 		armor = inArmor;
 	}
 	
 	void setPos(double inX, double inY) {
 		x 	= inX;
 		y 	= inY;
 	}
 	
 	void setSpeed(double inVX, double inVY) {
 		v_x = inVX;
 		v_y = inVY;
 		r   	= Math.max(width, height) + Math.pow(Math.ceil(Math.abs(v_x)), 2)*Math.pow(Math.ceil(Math.abs(v_y)), 2);
 	}
 	
 	void setRad(double inR) {
 		r 	= inR;
 	}
 	
 	void setHP(int inHP) {
 		hp 	= inHP;
 	}
 	
 	void setMaxHP(int inMaxHP){
 		maxHP	= inMaxHP;
 	}
 	
 	void setGeld(int inGeld){
 		money 	= inGeld;
 	}
 	
 	void setChocolate (int inChocolate) {
 		chocolate 	= inChocolate;
 	}
 	
 	void setAttackType (int type) {
 		attackType 	= type;
 	}
 	
 	void setDirection (int input) {
 		direction 	= input;
 	}
 
 	void setItem1 (Item inItem){
 		item1 = inItem;
 	}
 	
 	void setItem2 (Item inItem){
 		item2 = inItem;
 	}
 	
 	void setItem3 (Item inItem){
 		item3 = inItem;
 	}
 	
 	void setCooldown (long cooldown) {
 		this.cooldown = cooldown;
 	}
 	
 	void setSpecialAttack (boolean specialAttack) {
 		this.specialAttack = specialAttack;
 	}
 	
 	void setActivity(int activity) {
 		this.activity = activity;
 	}
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	/**
 	 * Prüft ob der Spieler über das Resurrect-Item verfügt. Wird aufgerufen falls der Spieler tödlichen Schaden erleidet
 	 * @return	true Falls der Spieler das Item mindestens einmal besitzt, false wenn nicht
 	 * @see 	ItemResurrect
 	 */
 	
 	
 	boolean checkRes() {
 		if(item1 instanceof ItemResurrect){
 			return true;
 		}
 		else if(item2 instanceof ItemResurrect){
 			return true;
 		}
 		else if(item3 instanceof ItemResurrect){
 			return true;
 		}
 
 		else 
 			return false;
 	}
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	@Override
 	void draw(Graphics2D g, int xOffset, int yOffset, double step) {
 		/* Durch die Variable step ist eine Umrechnung auf den Screen möglich, ansonsten würde das Spiel immer unterschiedlich angezeigt werden, 
 		 * allerdings ist zu beachten, dass durch den Gebrauch von Math.round() eine Verzeichnung um einen Pixel unten oder rechts nicht Auftritt, 
 		 * wie es bei einem cast auf int passieren kann.  */
 		g.setColor(figureColor);
 		g.fillRect(xOffset+(int)Math.round((x-width/2.)*step),  yOffset+(int)Math.round((y-height/2.)*step), (int)Math.round(step*width), (int)Math.round(step*height));
 		// TODO: bilder anstelle des quadrats malen
 	}
 	
 	// this method can be called from the collision method, then later on be used
 	// TODO: Make that shit better
 	
 	/**
 	 * Fügt ein Item dem "Inventar" des Spielers im untersten freien der drei Slots hinzu
 	 * @param inItem	Das Hinzugefügte Item
 	 */
 	
 	
 	void pickUpItem(Item inItem) {
 		if		(item1 == null)	item1 = inItem;
 		else if	(item2 == null)	item2 = inItem;
 		else if	(item3 == null)	item3 = inItem;
 	}
 
 	/**
 	 * Lässt die Figur Schaden nehmen. 
 	 * Überprüft dabei erst ob die Figur vor kurzem angegriffen wurde (cooldown)
 	 * Falls nein wird entsprechend der Stärke des Angriffs die Armor (falls vorhanden)
 	 * oder die hp (falls nicht genug Armor vorhanden)
 	 * @param attackType	Typ des Schadens, für Stein-Schere-Papier
 	 * @param inStrength	Stärke des eingehenden Schadens
 	 */
 	
 	
 	// check whether there is any armor to destroy, else reduce hp
 	void takeDamage(int attackType, int inStrength) {
 		if(System.currentTimeMillis()-cooldown > 1000){
 			switch (attackType) {
 			default:
 				if (armor > 0)
 					armor -= inStrength;
 				else
 					hp -= inStrength;
 			}
 			
 			cooldown = System.currentTimeMillis();
 		}
 	}
 	
 	/*-----------------------------------------------------------------------------------------------------------------------*/
 	//TODO Dokumentier das mal jemand, aka Was zur Hölle ist das
 	
 	Figure copy() {
 		// create a new figure object
 		Figure figure = new Figure(this.x, this.y, this.height, this.width, this.player, this.figureColor, this.id);
 		
 		// set the viewing direction of the new figure
 		figure.setDirection(this.direction);
 		
 		// set hp, money and chocolate variables
 		figure.setHP(this.hp);
 		figure.setGeld(this.money);
 		figure.setChocolate(this.chocolate);
 		
 		// set speed and collision detection radius
 		figure.setSpeed(this.v_x, this.v_y);
 		figure.setRad(this.r);
 		
 		// maximum HP and cooldown timer
 		figure.setMaxHP(this.maxHP);
 		figure.setCooldown(this.cooldown);
 		
 		// transfer armor and items
 		figure.setArmor(this.armor);
 		figure.setItem1(this.item1);
 		figure.setItem2(this.item2);
 		figure.setItem3(this.item3);
 		
 		// variables for the attack types and what type of attack to draw
 		figure.setSpecialAttack (this.specialAttack);
 		figure.setAttackType(this.attackType);
 		figure.setBulletType(this.bulletType);
 		figure.setBulletCoolDownTime(this.bulletCoolDownTime);
 
 		// set the viewing direction of the figure
 		figure.setDirection(this.direction);
 		
 		// return the newly created figure
 		return figure;
 	}
 }
