 package uk.co.richardgoater.stats.persistence;
 
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 @Entity
 @Table(name = "GAMEDATA_DEFENSE")
 public class DefenseGameData extends GameData {
 
 	private int tckl;
 	private int solo;
 	private int assist;
 	private double sck;
 	private int sckYds;
 	private int qbHurry;
 	private int ints;
 	private int intYds;
 	private int bp;
 	private int ff;
 	private int fr;
 	private int frYds;
 	private int td;
 	private int safety;
 	private int bk;
 
 	@Transient
 	public String[] getVisibleColumns() {
 		String[] columns = new String[] { 
 				"tckl", 
 				"solo", 
 				"assist", 
 				"sck", 
 				"sckYds",
 				"qbHurry", 
 				"ints", 
 				"intYds", 
 				"bp", 
 				"ff", 
 				"fr", 
 				"frYds", 
 				"td",
 				"safety", 
 				"bk" };
 		
 		return combinedPlayerAndStatColumns(columns);
 	}
 	
 	public DefenseGameData() {}
 	
 	public DefenseGameData(Number playerid, Number tckl, Number solo,
 			Number assist, Number sck, Number sckYds, Number qbHurry,
 			Number ints, Number intYds, Number bp, Number ff, Number fr,
 			Number frYds, Number td, Number safety, Number bk) 
 	{
 		this.playerid = playerid.intValue();
 		this.tckl = tckl.intValue();
 		this.solo = solo.intValue();
 		this.assist = assist.intValue();
 		this.sck = sck.doubleValue();
		this.sckYds = sck.intValue();
 		this.qbHurry = qbHurry.intValue();
 		this.ints = ints.intValue();
 		this.intYds = intYds.intValue();
 		this.bp = bp.intValue();
 		this.ff = ff.intValue();
 		this.fr = fr.intValue();
 		this.frYds = frYds.intValue();
 		this.td = td.intValue();
 		this.safety = safety.intValue();
 		this.bk = bk.intValue();
 	}
 	
 	@Column(name = "TCKL")
 	public int getTckl() {
 		return tckl;
 	}
 
 	public void setTckl(int tckl) {
 		this.tckl = tckl;
 	}
 
 	@Column(name = "SOLO")
 	public int getSolo() {
 		return solo;
 	}
 
 	public void setSolo(int solo) {
 		this.solo = solo;
 	}
 
 	@Column(name = "ASSIST")
 	public int getAssist() {
 		return assist;
 	}
 
 	public void setAssist(int assist) {
 		this.assist = assist;
 	}
 
 	@Column(name = "SCK")
 	public double getSck() {
 		return sck;
 	}
 
 	public void setSck(double sck) {
 		this.sck = sck;
 	}
 
 	@Column(name = "FF")
 	public int getFf() {
 		return ff;
 	}
 
 	public void setFf(int ff) {
 		this.ff = ff;
 	}
 
 	@Column(name = "FR")
 	public int getFr() {
 		return fr;
 	}
 
 	public void setFr(int fr) {
 		this.fr = fr;
 	}
 
 	@Column(name = "INTS")
 	public int getInts() {
 		return ints;
 	}
 
 	public void setInts(int ints) {
 		this.ints = ints;
 	}
 
 	@Column(name = "TD")
 	public int getTd() {
 		return td;
 	}
 
 	public void setTd(int td) {
 		this.td = td;
 	}
 
 	@Column(name = "SCKYDS")
 	public int getSckYds() {
 		return sckYds;
 	}
 
 	public void setSckYds(int sckYds) {
 		this.sckYds = sckYds;
 	}
 
 	@Column(name = "QBHURRY")
 	public int getQbHurry() {
 		return qbHurry;
 	}
 
 	public void setQbHurry(int qbHurry) {
 		this.qbHurry = qbHurry;
 	}
 
 	@Column(name = "INTYDS")
 	public int getIntYds() {
 		return intYds;
 	}
 
 	public void setIntYds(int intYds) {
 		this.intYds = intYds;
 	}
 
 	@Column(name = "BP")
 	public int getBp() {
 		return bp;
 	}
 
 	public void setBp(int bp) {
 		this.bp = bp;
 	}
 
 	@Column(name = "FRYDS")
 	public int getFrYds() {
 		return frYds;
 	}
 
 	public void setFrYds(int frYds) {
 		this.frYds = frYds;
 	}
 
 	@Column(name = "SAFETY")
 	public int getSafety() {
 		return safety;
 	}
 
 	public void setSafety(int safety) {
 		this.safety = safety;
 	}
 
 	@Column(name = "BK")
 	public int getBk() {
 		return bk;
 	}
 
 	public void setBk(int bk) {
 		this.bk = bk;
 	}
 
 }
