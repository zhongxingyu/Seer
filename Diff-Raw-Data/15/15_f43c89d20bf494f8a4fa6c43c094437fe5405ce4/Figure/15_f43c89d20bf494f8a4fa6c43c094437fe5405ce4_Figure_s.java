 package hhu.propra_2013.gruppe_13;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Timer;
 
 
 class Figure extends CoreGameObjects {
 	/*-----------------------------------------------------------------------------------------------*/
 	// Hitpoints, position, collision radius and constructor
 	private int 	hp;
 	private int 	money;
 	private int 	volt;
 	
 	private double 	x, y, r;
 	private double 	v_x, v_y;
 	private double 	width, height;
 	
 	private int 	maxHP;
 	private long	cooldown;
 	
 	private int 	armor;
 	private Item 	item1, item2, item3;
 	
 	private boolean	specialAttack;
 	private int 	attackType;
 	
 	// class constructor
 	Figure(double initX, double initY, double initHeight, double initWidth) {
 		x = initX;
 		y = initY;
 		
 		width  = initWidth;
 		height = initHeight;
 		
 		v_x = 0.3;
 		v_y = 0.3;
 		r   = Math.max(width, height) + Math.pow(Math.ceil(Math.abs(v_x)), 2)*Math.pow(Math.ceil(Math.abs(v_y)), 2);
 		hp  = 1;
		maxHP	= 2;
 		
 		item1 = null;
 		item2 = null;
 		item3 = null;
 		
 		cooldown = System.currentTimeMillis();
 	}
 	
 	
 	/*-----------------------------------------------------------------------------------------------*/
 	// Getter and Setter methods for above variables
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
 	
 	boolean checkRes() {
 		if(item1 instanceof ItemResurrect  || item2 instanceof ItemResurrect || item3 instanceof ItemResurrect)
 			return true;
 		else 
 			return false;
 	}
 	
 	int getVolt() {
 		return volt;
 	}
 	
 	/*-----------------------------------------------------------------------------------------------*/
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
 	
 	void setVolt (int inVolt) {
 		volt 	= inVolt;
 	}
 	
 	void setAttackType (int type) {
 		attackType = type;
 	}
 	/*-----------------------------------------------------------------------------------------------*/
 	@Override
 	void draw(Graphics2D g, int xOffset, int yOffset, double step) {
 		/* Durch die Variable step ist eine Umrechnung auf den Screen möglich, ansonsten würde das Spiel immer unterschiedlich angezeigt werden, 
 		 * allerdings ist zu beachten, dass durch den Gebrauch von Math.round() eine Verzeichnung um einen Pixel unten oder rechts nicht Auftritt, 
 		 * wie es bei einem cast auf int passieren kann.  */
 		g.setColor(Color.BLUE);
 		g.fillRect(xOffset+(int)Math.round((x-width/2.)*step),  yOffset+(int)Math.round((y-height/2.)*step), (int)Math.round(step*width), (int)Math.round(step*height));
 		// TODO: bilder anstelle des quadrats malen
 
 	}
 	
 	@Override
 	void attack() {
 		
 	}
 	//this method can be called from the collision method, then later on be used
 	void pickUpItem(Item inItem){
 		if(item1 == null)		item1 = inItem;
 		else if(item2 == null)	item2 = inItem;
 		else if(item3 == null)	item3 = inItem;
 	}
 
 	void takeDamage(int type, int inStrength) {
 		if(System.currentTimeMillis()-cooldown > 1000){
 			hp = hp - inStrength;
 			cooldown = System.currentTimeMillis();
 		}
 	}
 }
