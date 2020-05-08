 package annahack.nethackinformation.nethackplayer;
 
 import annahack.nethackinformation.nethackcreatures.Creature;
 
public class Player extends Creature implements NetHackHero 
 {
 	private PlayerClass pClass;
 	private PlayerRace pRace;
 
 	private int xp;
 	private int level;
 
 	protected Player()
 	{
 		this(new PlayerClass(), new PlayerRace((byte)'v'), true, (byte)1);
 	}
 	
 	protected Player(PlayerClass pclass, PlayerRace race, boolean isFem, byte alignment)
 	{
 		pClass=pclass;
 		pRace=race;
 		isFemale=isFem;
 		isMale=!isFem;
 		align=alignment;
 	}
 	
 	protected void setXp(int xp) {
 		this.xp = xp;
 	}
 
 	protected void setLevel(int level) {
 		this.level = level;
 	}
 
 	public PlayerClass getPClass() {
 		return pClass;
 	}
 
 	public PlayerRace getPRace() {
 		return pRace;
 	}
 
 	public int getXp() {
 		return xp;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public PlayerClass getPlayerClass()
 	{
 		return pClass;
 	}
 	
 	public PlayerRace getPlayerRace()
 	{
 		return pRace;
 	}
 	
 	public byte alignment()
 	{
 		return align;
 	}
 	
 	protected void setHp(int hp) {
 		this.hp = hp;
 	}
 
 	protected void setMaxhp(int maxhp) {
 		this.maxhp = maxhp;
 	}
 
 	protected void setMp(int mp) {
 		this.mp = mp;
 	}
 
 	protected void setMaxmp(int maxmp) {
 		this.maxmp = maxmp;
 	}
 
 	protected void setAlign(byte align) {
 		this.align = align;
 	}
 
 	protected void setSt(int st) {
 		this.st = st;
 	}
 
 	protected void setDx(int dx) {
 		this.dx = dx;
 	}
 
 	protected void setCo(int co) {
 		this.co = co;
 	}
 
 	protected void setCh(int ch) {
 		this.ch = ch;
 	}
 
 	protected void setWi(int wi) {
 		this.wi = wi;
 	}
 
 	protected void setIn(int in) {
 		this.in = in;
 	}
 
 	protected void setFemale(boolean isFemale) {
 		this.isFemale = isFemale;
 	}
 
 	protected void setMale(boolean isMale) {
 		this.isMale = isMale;
 	}
 	
 	protected void setAC(int ac)
 	{
 		this.ac=ac;
 	}
 }
