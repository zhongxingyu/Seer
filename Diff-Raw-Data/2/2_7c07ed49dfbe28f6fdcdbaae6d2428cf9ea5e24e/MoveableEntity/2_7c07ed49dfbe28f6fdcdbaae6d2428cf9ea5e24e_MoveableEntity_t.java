 package com.jpii.navalbattle.game.entity;
 
 import com.jpii.navalbattle.game.NavalManager;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.EntityManager;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 
 public class MoveableEntity extends Entity {
 	private static final long serialVersionUID = 1L;
 	protected int maxMovement;
 	protected int moved;
 	protected int maxHealth = 100;
 	protected int currentHealth = 100;
 	protected int primaryRange;
 	protected int secondaryRange;
 	private boolean showMove = false;
 	private boolean showPrimary = false;
 	private boolean showSecondary = false;
 	private boolean usedGuns = false;
 	private boolean usedMissiles = false;
 	public boolean gunsAttackOption = false;
 	public boolean missileAttackOption = false;
 	public boolean planeAttackOption = false;
 	public byte missileCount = 0;
 	public byte percentEvade;
 	public byte rangeLimit = 3;
 
 	/**
 	 * @param em
 	 * @param loc
 	 * @param id
 	 * @param orientation
 	 * @param teams
 	 */
 	public MoveableEntity(EntityManager em, Location loc,
 			GridedEntityTileOrientation id, byte orientation, int teams) {
 		super(em, loc, id, orientation, teams);
 		handle = 1;
 	}
 	
 	public boolean isMovableTileBeingShown() {
 		return showMove;
 	}
 	
 	public boolean isPrimaryTileBeingShown() {
 		return showPrimary;
 	}
 	
 	public boolean isSecondaryTileBeingShown() {
 		return showSecondary;
 	}
 	
 	public void toggleMovable() {
 		byte good = (byte)0x2f;
 		byte bad = (byte)0x001;
 		if(showMove){
 			showMove = false;
 			good = bad = 0;
 		}
 		else{
 			showMove = true;
 		}
 			
 			
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int x = 0; x < (getMovementLeft() * 2) + 1; x++) {
 				for (int y = 0; y < (getMovementLeft() * 2) + 1; y++) {
 					int r = getRLR(y);
 					int c = getCLR(x);
 					if (r >= 0 && c >= 0) {
 						if(isPossibleMoveChoiceLR(x,y)){
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		else {
 			for (int x = 0; x < (getMovementLeft() * 2) + 1; x++) {
 				for (int y = 0; y < (getMovementLeft() * 2) + 1; y++) {
 					int c = (x + getLocation().getCol()) - (((getMovementLeft() * 2) + 1)/2);
 					int r = (y + getLocation().getRow()) - (getMovementLeft());
 					if (r >= 0 && c >= 0) {
 						if (isPossibleMoveChoiceTB(x,y)) {
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		getManager().getWorld().forceRender();
 	}
 	
 	public void togglePrimaryRange(){
 		byte good = (byte)0x2f;
 		byte bad = (byte)0x001;
 		if(showPrimary){
 			showPrimary = false;
 			good = bad = 0;
 		}
 		else{
 			showPrimary = true;
 		}
 		
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int x = 0; x < (getPrimaryRange() * 2) + 1; x++) {
 				for (int y = 0; y < (getPrimaryRange() * 2) + 1; y++) {
 					int r = getPrimaryAttackR(y);
 					int c = getPrimaryAttackC(x);
 					if (r >= 0 && c >= 0) {
 						if(isPossibleAttackChoice(c,r)){
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		else {
 			for (int x = 0; x < (getPrimaryRange() * 2) + 1; x++) {
 				for (int y = 0; y < (getPrimaryRange() * 2) + 1; y++) {
 					int c = (x + getLocation().getCol()) - (((getPrimaryRange() * 2) + 1)/2);
 					int r = (y + getLocation().getRow()) - (getPrimaryRange());
 					if (r >= 0 && c >= 0) {
 						if (isPossibleAttackChoice(c,r)) {
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		getManager().getWorld().forceRender();
 	}
 	
 	public void toggleSecondaryRange(){
 		byte good = (byte)0x2f;
 		byte bad = (byte)0x001;
 		if(showSecondary){
 			showSecondary = false;
 			good = bad = 0;
 		}
 		else{
 			showSecondary = true;
 		}
 		
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int x = 0; x < (getSecondaryRange() * 2) + 1; x++) {
 				for (int y = 0; y < (getSecondaryRange() * 2) + 1; y++) {
 					int r = getSecondaryAttackR(y);
 					int c = getSecondaryAttackC(x);
 					if (r >= 0 && c >= 0) {
 						if(isPossibleAttackChoice(c,r)){
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		else {
 			for (int x = 0; x < (getSecondaryRange() * 2) + 1; x++) {
 				for (int y = 0; y < (getSecondaryRange() * 2) + 1; y++) {
 					int c = (x + getLocation().getCol()) - (((getSecondaryRange() * 2) + 1)/2);
 					int r = (y + getLocation().getRow()) - (getSecondaryRange());
 					if (r >= 0 && c >= 0) {
 						if (isPossibleAttackChoice(c,r)) {
 							getManager().setTileOverlay(r,c,good);
 						}
 						else {
 							getManager().setTileOverlay(r,c,bad);
 						}
 					}
 				}
 			}
 		}
 		getManager().getWorld().forceRender();
 	}
 	
 	public int getRLR(int y)
 	{
 		return (y + getLocation().getRow()) - (((getMovementLeft() * 2) + 1)/2);
 	}
 	
 	public int getCLR(int x)
 	{
 		return (x + getLocation().getCol()) - (getMovementLeft());
 	}
 	
 	public int getPrimaryAttackR(int y)
 	{
 		return (y + getLocation().getRow()) - (((getPrimaryRange() * 2) + 1)/2);
 	}
 	
 	public int getPrimaryAttackC(int x)
 	{
 		return (x + getLocation().getCol()) - (getPrimaryRange());
 	}
 	
 	public int getSecondaryAttackR(int y)
 	{
 		return (y + getLocation().getRow()) - (((getSecondaryRange() * 2) + 1)/2);
 	}
 	
 	public int getSecondaryAttackC(int x)
 	{
 		return (x + getLocation().getCol()) - (getSecondaryRange());
 	}
 	
 	public Tile<?> getTileLR(int x, int y)
 	{
 		Tile<?> temps = getManager().getTile(getRLR(y),getCLR(x));
 		return temps;
 	}
 	
 	public boolean isPossibleMoveChoiceLR(int x, int y)
 	{
 		boolean horizontal = true;
 		boolean vertical = true;
 		for(int p = 0 ; p < getWidth(); p++){
 			if( ((getTileLR(x+p,y)!=null) && !getTileLR(x+p,y).getEntity().equals(this)) || (getManager().getTilePercentLand(getRLR(y),getCLR(x+p)) > Game.Settings.waterThresholdBarrier)){
 				horizontal = false;
 			}
 		}
 		
 		for(int q = 0 ; q < getWidth(); q++){
 			if( ((getTileLR(x,y-q)!=null) && !getTileLR(x,y-q).getEntity().equals(this)) || (getManager().getTilePercentLand(getRLR(y-q),getCLR(x)) > Game.Settings.waterThresholdBarrier)){
 				vertical = false;
 			}
 		}
 			
 		return (horizontal == true || vertical == true);
 	}
 	
 	public int getRTB(int y)
 	{
 		return (y + getLocation().getRow()) - (getMovementLeft());
 	}
 	
 	public int getCTB(int x)
 	{
 		return (x + getLocation().getCol()) - (((getMovementLeft() * 2) + 1)/2);
 	}
 	public Tile<?> getTileTB(int x, int y)
 	{
 		Tile<?> temps = getManager().getTile(getRTB(y),getCTB(x));
 		return temps;
 	}
 	
 	public boolean isPossibleMoveChoiceTB(int x, int y)
 	{		
 		boolean horizontal = true;
 		boolean vertical = true;
 		for(int p = 0 ; p < getWidth(); p++){
 			if((getTileTB(x+p,y)!=null)  && !getTileTB(x+p,y).getEntity().equals(this) || (getManager().getTilePercentLand(getRTB(y),getCTB(x+p)) > Game.Settings.waterThresholdBarrier)){
 			horizontal = false;
 			}
 		}
 		
 		for(int q = 0 ; q < getWidth(); q++){
 			if((getTileTB(x,y-q)!=null)  && !getTileTB(x,y-q).getEntity().equals(this) || (getManager().getTilePercentLand(getRTB(y-q),getCLR(x)) > Game.Settings.waterThresholdBarrier)){
 				vertical = false;
 			}
 		}
 			
 		return (horizontal == true || vertical == true);
 	}
 	
 	public boolean isPossibleAttackChoice(int x, int y)
 	{
 		boolean good = false;
 		if(getManager().getTile(y,x)!=null && !getManager().getTile(y,x).getEntity().equals(this))
 			good = true;
 		return good;
 	}
 		
 	public boolean isInMoveRange(int chx, int chy){
 		if(!showMove)
 			return false;
 		boolean flag = false;
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			int minr = getLocation().getRow() - getMovementLeft();
 			int maxr = getLocation().getRow() + getMovementLeft();
 			int minc = getLocation().getCol() - getMovementLeft();
 			int maxc = getLocation().getCol() + getMovementLeft();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		else {
 			int minr = getLocation().getRow() - getMovementLeft();
 			int maxr = getLocation().getRow() + getMovementLeft();
 			int minc = getLocation().getCol() - getMovementLeft();
 			int maxc = getLocation().getCol() + getMovementLeft();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		return flag;
 	}
 	
 	public boolean isInPrimaryRange(int chx, int chy){
 		if(!showPrimary)
 			return false;
 		boolean flag = false;
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			int minr = getLocation().getRow() - getPrimaryRange();
 			int maxr = getLocation().getRow() + getPrimaryRange();
 			int minc = getLocation().getCol() - getPrimaryRange();
 			int maxc = getLocation().getCol() + getPrimaryRange();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		else {
 			int minr = getLocation().getRow() - getPrimaryRange();
 			int maxr = getLocation().getRow() + getPrimaryRange();
 			int minc = getLocation().getCol() - getPrimaryRange();
 			int maxc = getLocation().getCol() + getPrimaryRange();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		return flag;
 	}
 	
 	public boolean isInSecondaryRange(int chx, int chy){
 		if(!showSecondary)
 			return false;
 		boolean flag = false;
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			int minr = getLocation().getRow() - getSecondaryRange();
 			int maxr = getLocation().getRow() + getSecondaryRange();
 			int minc = getLocation().getCol() - getSecondaryRange();
 			int maxc = getLocation().getCol() + getSecondaryRange();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		else {
 			int minr = getLocation().getRow() - getSecondaryRange();
 			int maxr = getLocation().getRow() + getSecondaryRange();
 			int minc = getLocation().getCol() - getSecondaryRange();
 			int maxc = getLocation().getCol() + getSecondaryRange();
 			if(chx<=maxc && chx>=minc && chy<=maxr && chy>=minr)
 				flag = true;
 		}
 		return flag;
 	}
 	
 	public int getMovementLeft() {
 		return maxMovement-moved;
 	}
 	
 	public int getPrimaryRange(){
 		return primaryRange;
 	}
 	
 	public int getSecondaryRange(){
 		return secondaryRange;
 	}
 	
 	public void resetMovement(){
 		moved = 0;
 	}
 	
 	public void resetAttack(){
 		usedGuns=false;
 		usedMissiles=false;
 	}
 	
 	public int getPercentHealth(){
 		return (int)((double)currentHealth/(double)maxHealth*100.0);
 	}
 	
 	public int getMaxMovement(){
 		return maxMovement;
 	}
 	
 	public int getMoved(){
 		return moved;
 	}
 	
 	public boolean getUsedGuns(){
 		return usedGuns;
 	}
 	
 	public boolean getUsedMissiles(){
		return usedMissiles || !(missileCount>0);
 	}
 	
 	public void addMovement(int num){
 		moved += num;
 	}
 	
 	public void usePrimary(){
 		usedGuns=true;
 	}
 	
 	public void useSecondary(){
 		usedMissiles=true;
 	}
 	
 	public boolean takeDamage(int dealt){		
 		int b4 = getPercentHealth();
 		currentHealth -= dealt;
 		if(currentHealth<=0){
 			currentHealth=0;
 			((NavalManager)getManager()).getTurnManager().removeEntity(this);
 			return dispose();
 		}
 		return((b4>25&&getPercentHealth()<25));
 	}
 	
 	public void hardenHull(){
 		maxHealth *=2;
 		currentHealth *=2;
 		maxMovement/=2;
 		moved*=2;
 	}
 	
 	public void increaseMissile(){
 		missileCount+=5;
 	}
 	
 	public void deflectMissile(){
 		percentEvade+=5;
 	}
 	
 	public void increaseRange(){
 		maxMovement++;
 		rangeLimit--;
 	}
 
 	public void repair(){
 		currentHealth=maxHealth;
 	}
 }
