 /*
  * Created on 29.08.2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package hoplugins.playerCompare;
 
 import hoplugins.PlayerCompare;
import plugins.IHOMiniModel;
 import plugins.ISpieler;
 import plugins.ISpielerPosition;
 /**
  * @author KickMuck
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Player
 {
 	private ISpieler spieler;
 	
 	private String name;
 	private int alter;
 	private int id;
 	private int gehalt;
 	private int tsi;
 	private int nation;
 	private int fuehrung;
 	private int erfahrung;
 	private int kondi;
 	private int form;
 	private int tw;
 	private int ve;
 	private int sp;
 	private int ps;
 	private int fl;
 	private int ts;
 	private int st;
 	private int erfahrungOld;
 	private int kondiOld;
 	private int formOld;
 	private int twOld;
 	private int veOld;
 	private int spOld;
 	private int psOld;
 	private int flOld;
 	private int tsOld;
 	private int stOld;
 	private float posWertTW;
 	private float posWertIV;
 	private float posWertIV_A;
 	private float posWertIV_O;
 	private float posWertAV;
 	private float posWertAV_I;
 	private float posWertAV_O;
 	private float posWertAV_D;
 	private float posWertMI;
 	private float posWertMI_A;
 	private float posWertMI_O;
 	private float posWertMI_D;
 	private float posWertFL;
 	private float posWertFL_I;
 	private float posWertFL_O;
 	private float posWertFL_D;
 	private float posWertST;
 	private float posWertST_D;
 	private float posWertTWOld;
 	private float posWertIVOld;
 	private float posWertIV_AOld;
 	private float posWertIV_OOld;
 	private float posWertAVOld;
 	private float posWertAV_IOld;
 	private float posWertAV_OOld;
 	private float posWertAV_DOld;
 	private float posWertMIOld;
 	private float posWertMI_AOld;
 	private float posWertMI_OOld;
 	private float posWertMI_DOld;
 	private float posWertFLOld;
 	private float posWertFL_IOld;
 	private float posWertFL_OOld;
 	private float posWertFL_DOld;
 	private float posWertSTOld;
 	private float posWertST_DOld;
 	private byte bestPosition;
 	private float bestPosStaerke;
 	private byte bestPositionOld;
 	private float bestPosStaerkeOld;
 	private String gruppe;
 	private int spezialitaet;
 	
 	//Konstruktor
 	public Player(ISpieler player)
 	{
 		spieler = player;
 
 		setSpielerDaten();
 		setOldSkillValues();
 		setOldPositionValues();
 		setNewSkillValues();
 		setNewPositionValues();
 		setSpielerDaten();
 	}
 	
 	public Player()
 	{
 		
 	}
 	
 	/* @function getPositionCompareAsString(int position)
 	 * liefert einen String zur�ck in der Form ("1.35;0.15") wobei
 	 * die 1.35 f�r den neuen Positionswert steht und die 0.15 f�r die Ver�nderung
 	 * zum Originalwert des Spielers 
 	 * 
 	 * int position: Wert, f�r die Ermittlung der Position
 	 * 
 	 * return: String
 	 */
 	public String getPositionCompareAsString(int position)
 	{
 		String s = "";
 		switch(position)
 		{
 			case 0:{
 				s += getPosWertTW() + ";" + (getPosWertTW() - getPosWertTWOld());
 				break;
 			}
 			case 1:{
 				s += getPosWertIV() + ";" + (getPosWertIV() - getPosWertIVOld());
 				break;
 			}
 			case 2:{
 				s += getPosWertIV_A() + ";" + (getPosWertIV_A() - getPosWertIV_AOld());
 				break;
 			}
 			case 3:{
 				s += getPosWertIV_O() + ";" + (getPosWertIV_O() - getPosWertIV_OOld());
 				break;
 			}
 			case 4:{
 				s += getPosWertAV() + ";" + (getPosWertAV() - getPosWertAVOld());
 				break;
 			}
 			case 5:{
 				s += getPosWertAV_I() + ";" + (getPosWertAV_I() - getPosWertAV_IOld());
 				break;
 			}
 			case 6:{
 				s += getPosWertAV_O() + ";" + (getPosWertAV_O() - getPosWertAV_OOld());
 				break;
 			}
 			case 7:{
 				s += getPosWertAV_D() + ";" + (getPosWertAV_D() - getPosWertAV_DOld());
 				break;
 			}
 			case 8:{
 				s += getPosWertMI() + ";" + (getPosWertMI() - getPosWertMIOld());
 				break;
 			}
 			case 9:{
 				s += getPosWertMI_A() + ";" + (getPosWertMI_A() - getPosWertMI_AOld());
 				break;
 			}
 			case 10:{
 				s += getPosWertMI_O() + ";" + (getPosWertMI_O() - getPosWertMI_OOld());
 				break;
 			}
 			case 11:{
 				s += getPosWertMI_D() + ";" + (getPosWertMI_D() - getPosWertMI_DOld());
 				break;
 			}
 			case 12:{
 				s += getPosWertFL() + ";" + (getPosWertFL() - getPosWertFLOld());
 				break;
 			}
 			case 13:{
 				s += getPosWertFL_I() + ";" + (getPosWertFL_I() - getPosWertFL_IOld());
 				break;
 			}
 			case 14:{
 				s += getPosWertFL_O() + ";" + (getPosWertFL_O() - getPosWertFL_OOld());
 				break;
 			}
 			case 15:{
 				s += getPosWertFL_D() + ";" + (getPosWertFL_D() - getPosWertFL_DOld());
 				break;
 			}
 			case 16:{
 				s += getPosWertST() + ";" + (getPosWertST() - getPosWertSTOld());
 				break;
 			}
 			case 17:{
 				s += getPosWertST_D() + ";" + (getPosWertST_D() - getPosWertST_DOld());
 				break;
 			}
 		}
 		return s;
 	}
 	
 	/* @function getSkillCompareAsDouble(int skill)
 	 * liefert einen Double zur�ck in der Form (5.06) wobei
 	 * die 5 f�r den neuen Skillwert steht und die 06 f�r den alten Wert
 	 * multipliziert mit 0.01 
 	 * 
 	 * int skill: Wert, f�r die Ermittlung des Skills
 	 * 
 	 * return: double
 	 */
 	public double getSkillCompareAsDouble(int skill)
 	{
 		double combined = 0;
 		switch(skill)
 		{
 			case 0:{
 				combined = getErfahrung() + (getErfahrungOld() * 0.01);
 				break;
 			}
 			case 1:{
 				combined = getForm() + (getFormOld() * 0.01);
 				break;
 			}
 			case 2:{
 				combined = getKondi() + (getKondiOld() * 0.01);
 				break;
 			}
 			case 3:{
 				combined = getTw() + (getTwOld() * 0.01);
 				break;
 			}
 			case 4:{
 				combined = getVe() + (getVeOld() * 0.01);
 				break;
 			}
 			case 5:{
 				combined = getSp() + (getSpOld() * 0.01);
 				break;
 			}
 			case 6:{
 				combined = getPs() + (getPsOld() * 0.01);
 				break;
 			}
 			case 7:{
 				combined = getFl() + (getFlOld() * 0.01);
 				break;
 			}
 			case 8:{
 				combined = getTs() + (getTsOld() * 0.01);
 				break;
 			}
 			case 9:{
 				combined = getSt() + (getStOld() * 0.01);
 				break;
 			}
 		}
 		return combined;
 	}
 	
 	/* @function getSkillCompareAsString(int skill)
 	 * liefert einen String zur�ck in der Form ("5;-1") wobei
 	 * die 5 f�r den neuen Skillwert steht und die -1 f�r die Ver�nderung
 	 * zum Originalwert des Spielers 
 	 * 
 	 * int skill: Wert, f�r die Ermittlung des Skills
 	 * 
 	 * return: String
 	 */
 	public String getSkillCompareAsString(int skill)
 	{
 		String s = "";
 		switch(skill)
 		{
 			case 0:{
 				s += getErfahrung() + ";" + (getErfahrung() - getErfahrungOld());
 				break;
 			}
 			case 1:{
 				s += getForm() + ";" + (getForm() - getFormOld());
 				break;
 			}
 			case 2:{
 				s += getKondi() + ";" + (getKondi() - getKondiOld());
 				break;
 			}
 			case 3:{
 				s += getTw() + ";" + (getTw() - getTwOld());
 				break;
 			}
 			case 4:{
 				s += getVe() + ";" + (getVe() - getVeOld());
 				break;
 			}
 			case 5:{
 				s += getSp() + ";" + (getSp() - getSpOld());
 				break;
 			}
 			case 6:{
 				s += getPs() + ";" + (getPs() - getPsOld());
 				break;
 			}
 			case 7:{
 				s += getFl() + ";" + (getFl() - getFlOld());
 				break;
 			}
 			case 8:{
 				s += getTs() + ";" + (getTs() - getTsOld());
 				break;
 			}
 			case 9:{
 				s += getSt() + ";" + (getSt() - getStOld());
 				break;
 			}
 		}
 		return s;
 	}
 	
 	/* @function changeSkill(int skill, int wert)
 	 * Funktion, die die Werte des Spielers in der Datenbank �ndert.
 	 * Wird von changePlayerSkillValues() aufgerufen. 
 	 * 
 	 * int skill: Wert, f�r die Ermittlung des zu �ndernden Skills
 	 * int wert: Neuer Wert, der in die Datenbank geschrieben wird
 	 */
 	public void changeSkill(int skill, int wert)
 	{
 		switch(skill)
 		{
 			case 0:{
 				spieler.setErfahrung(wert);
 				break;
 			}
 			case 1:{
 				spieler.setForm(wert);
 				break;
 			}
 			case 2:{
 				spieler.setKondition(wert);
 				break;
 			}
 			case 3:{
 				spieler.setTorwart(wert);
 				break;
 			}
 			case 4:{
 				spieler.setVerteidigung(wert);
 				break;
 			}
 			case 5:{
 				spieler.setSpielaufbau(wert);
 				break;
 			}
 			case 6:{
 				spieler.setPasspiel(wert);
 				break;
 			}
 			case 7:{
 				spieler.setFluegelspiel(wert);
 				break;
 			}
 			case 8:{
 				spieler.setTorschuss(wert);
 				break;
 			}
 			case 9:{
 				spieler.setStandards(wert);
 				//break;
 			}
 		}
 	}
 	
 	/* @function changePlayerSkillValues(boolean richtung)
 	 * Funktion, die die Werte des Spielers ermittelt, um sie in der 
 	 * Datenbank zu �ndern. �bergibt diese Werte an changeSkill().
 	 * Wird ben�tigt, um die St�rke auf den einzelnen Position mit 
 	 * Spieler.calcPositionValue() zu berechnen
 	 * 
 	 * boolean richtung: bezeichnet, ob neue Werte oder die originalen Werte
 	 * 		in die DB eingetragen werden sollen
 	 * 		true: es werden die neuen Werte eingetragen
 	 * 		false: es werden die Originalwerte eingetragen
 	 */
 	public void changePlayerSkillValues(boolean richtung)
 	{
 		// Array f�r die Originalwerte
 		int[] alteWerte = getOldSkillWerte();
 		
 		if(richtung == true)
 		{
 			// Array f�r die neugesetzten Werte (passabel, gut, usw.)
 			int[] neueWerte = PlayerCompare.getNewStaerke();
 			// Array f�r die neugesetzten Werte (+1,+2, usw.)
 			int[] changedSkills = PlayerCompare.getChangeStaerkeBy();
 			
 			for(int j = 0; j < 10; j++)
 			{
 				if(neueWerte[j] == 0)
 				{
 					if(changedSkills[j] != 0)
 					{
 						setNewSkillValues(j, (alteWerte[j] + changedSkills[j]));
 						changeSkill(j, (alteWerte[j] + changedSkills[j]));
 					}
 				}
 				else
 				{
 					setNewSkillValues(j, neueWerte[j]);
 					changeSkill(j, neueWerte[j]);
 				}
 			}
 			setNewPositionValues();
 		}
 		else
 		{
 			for(int i = 0; i < 10; i++)
 			{
 				changeSkill(i, alteWerte[i]);
 			}
 		}
 		
 	}
 	
 	public void resetPlayers()
 	{
 		int[] alteWerte = getOldSkillWerte();
 		for(int i = 0; i < 10; i++)
 		{
 			changeSkill(i, alteWerte[i]);
 			setNewSkillValues(i,alteWerte[i]);
 		}
 	}
 	public void setSpielerDaten()
 	{
 		setId(spieler.getSpielerID());
 		setName(spieler.getName());
 		setAlter(spieler.getAlter());
 		setGehalt(spieler.getGehalt());
 		setTsi(spieler.getTSI());
 		setNation(spieler.getNationalitaet());
 		setFuehrung(spieler.getFuehrung());
 		setGruppe(spieler.getTeamInfoSmilie());
 		setSpezialitaet(spieler.getSpezialitaet());
 	}
 	
 	public void setOldSkillValues()
 	{
 		setErfahrungOld(spieler.getErfahrung());
 		setFormOld(spieler.getForm());
 		setKondiOld(spieler.getKondition());
 		setTwOld(spieler.getTorwart());
 		setVeOld(spieler.getVerteidigung());
 		setSpOld(spieler.getSpielaufbau());
 		setPsOld(spieler.getPasspiel());
 		setFlOld(spieler.getFluegelspiel());
 		setTsOld(spieler.getTorschuss());
 		setStOld(spieler.getStandards());
 	}
 	
 	public int[] getOldSkillWerte()
 	{
 		int[] zurueck = new int[10];
 		zurueck[0] = this.getErfahrungOld();
 		zurueck[1] = this.getFormOld();
 		zurueck[2] = this.getKondiOld();
 		zurueck[3] = this.getTwOld();
 		zurueck[4] = this.getVeOld();
 		zurueck[5] = this.getSpOld();
 		zurueck[6] = this.getPsOld();
 		zurueck[7] = this.getFlOld();
 		zurueck[8] = this.getTsOld();
 		zurueck[9] = this.getStOld();
 		return zurueck;
 	}
 	public void setNewSkillValues()
 	{
 		setErfahrung(spieler.getErfahrung());
 		setForm(spieler.getForm());
 		setKondi(spieler.getKondition());
 		setTw(spieler.getTorwart());
 		setVe(spieler.getVerteidigung());
 		setSp(spieler.getSpielaufbau());
 		setPs(spieler.getPasspiel());
 		setFl(spieler.getFluegelspiel());
 		setTs(spieler.getTorschuss());
 		setSt(spieler.getStandards());
 	}
 	
 	public void setNewSkillValues(int skill, int wert)
 	{
 		switch(skill)
 		{
 			case 0:{
 				setErfahrung(wert);
 				break;
 			}
 			case 1:{
 				setForm(wert);
 				break;
 			}
 			case 2:{
 				setKondi(wert);
 				break;
 			}
 			case 3:{
 				setTw(wert);
 				break;
 			}
 			case 4:{
 				setVe(wert);
 				break;
 			}
 			case 5:{
 				setSp(wert);
 				break;
 			}
 			case 6:{
 				setPs(wert);
 				break;
 			}
 			case 7:{
 				setFl(wert);
 				break;
 			}
 			case 8:{
 				setTs(wert);
 				break;
 			}
 			case 9:{
 				setSt(wert);
 				break;
 			}
 		}
 	}
 	
 	public void setOldPositionValues()
 	{
 		setPosWertTWOld(spieler.calcPosValue(ISpielerPosition.KEEPER,true));
 		setPosWertIVOld(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,true));
 		setPosWertIV_AOld(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,true));
 		setPosWertIV_OOld(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,true));
 		setPosWertAVOld(spieler.calcPosValue(ISpielerPosition.BACK,true));
 		setPosWertAV_IOld(spieler.calcPosValue(ISpielerPosition.BACK_TOMID,true));
 		setPosWertAV_OOld(spieler.calcPosValue(ISpielerPosition.BACK_OFF,true));
 		setPosWertAV_DOld(spieler.calcPosValue(ISpielerPosition.BACK_DEF,true));
 		setPosWertMIOld(spieler.calcPosValue(ISpielerPosition.MIDFIELDER,true));
 		setPosWertMI_OOld(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_OFF,true));
 		setPosWertMI_DOld(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_DEF,true));
 		setPosWertMI_AOld(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,true));
 		setPosWertFLOld(spieler.calcPosValue(ISpielerPosition.WINGER,true));
 		setPosWertFL_DOld(spieler.calcPosValue(ISpielerPosition.WINGER_DEF,true));
 		setPosWertFL_IOld(spieler.calcPosValue(ISpielerPosition.WINGER_TOMID,true));
 		setPosWertFL_OOld(spieler.calcPosValue(ISpielerPosition.WINGER_OFF,true));
 		setPosWertSTOld(spieler.calcPosValue(ISpielerPosition.FORWARD,true));
 		setPosWertST_DOld(spieler.calcPosValue(ISpielerPosition.FORWARD_DEF,true));
 		setBestPositionOld(spieler.getIdealPosition());
 		setBestPosStaerkeOld(spieler.getIdealPosStaerke(true));
 	}
 	
 	public void setNewPositionValues()
 	{
 		setPosWertTW(spieler.calcPosValue(ISpielerPosition.KEEPER,true));
 		setPosWertIV(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER,true));
 		setPosWertIV_A(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_TOWING,true));
 		setPosWertIV_O(spieler.calcPosValue(ISpielerPosition.CENTRAL_DEFENDER_OFF,true));
 		setPosWertAV(spieler.calcPosValue(ISpielerPosition.BACK,true));
 		setPosWertAV_I(spieler.calcPosValue(ISpielerPosition.BACK_TOMID,true));
 		setPosWertAV_O(spieler.calcPosValue(ISpielerPosition.BACK_OFF,true));
 		setPosWertAV_D(spieler.calcPosValue(ISpielerPosition.BACK_DEF,true));
 		setPosWertMI(spieler.calcPosValue(ISpielerPosition.MIDFIELDER,true));
 		setPosWertMI_O(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_OFF,true));
 		setPosWertMI_D(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_DEF,true));
 		setPosWertMI_A(spieler.calcPosValue(ISpielerPosition.MIDFIELDER_TOWING,true));
 		setPosWertFL(spieler.calcPosValue(ISpielerPosition.WINGER,true));
 		setPosWertFL_D(spieler.calcPosValue(ISpielerPosition.WINGER_DEF,true));
 		setPosWertFL_I(spieler.calcPosValue(ISpielerPosition.WINGER_TOMID,true));
 		setPosWertFL_O(spieler.calcPosValue(ISpielerPosition.WINGER_OFF,true));
 		setPosWertST(spieler.calcPosValue(ISpielerPosition.FORWARD,true));
 		setPosWertST_D(spieler.calcPosValue(ISpielerPosition.FORWARD_DEF,true));
 		setBestPosition(spieler.getIdealPosition());
 		setBestPosStaerke(spieler.getIdealPosStaerke(true));
 		
 		//Aufruf zum Zur�cksetzen der Skillwerte
 		changePlayerSkillValues(false);
 	}
 	
 	public int getAlter() {
 		return alter;
 	}
 	public void setAlter(int alter) {
 		this.alter = alter;
 	}
 	public byte getBestPosition() {
 		return bestPosition;
 	}
 	public void setBestPosition(byte bestPosition) {
 		this.bestPosition = bestPosition;
 	}
 	public byte getBestPositionOld() {
 		return bestPositionOld;
 	}
 	public void setBestPositionOld(byte bestPositionOld) {
 		this.bestPositionOld = bestPositionOld;
 	}
 	public float getBestPosStaerke() {
 		return bestPosStaerke;
 	}
 	public void setBestPosStaerke(float bestPosStaerke) {
 		this.bestPosStaerke = bestPosStaerke;
 	}
 	public float getBestPosStaerkeOld() {
 		return bestPosStaerkeOld;
 	}
 	public void setBestPosStaerkeOld(float bestPosStaerkeOld) {
 		this.bestPosStaerkeOld = bestPosStaerkeOld;
 	}
 	public int getErfahrung() {
 		return erfahrung;
 	}
 	public void setErfahrung(int erfahrung) {
 		this.erfahrung = erfahrung;
 	}
 	public int getErfahrungOld() {
 		return erfahrungOld;
 	}
 	public void setErfahrungOld(int erfahrungOld) {
 		this.erfahrungOld = erfahrungOld;
 	}
 	public int getFl() {
 		return fl;
 	}
 	public void setFl(int fl) {
 		this.fl = fl;
 	}
 	public int getFlOld() {
 		return flOld;
 	}
 	public void setFlOld(int flOld) {
 		this.flOld = flOld;
 	}
 	public int getForm() {
 		return form;
 	}
 	public void setForm(int form) {
 		this.form = form;
 	}
 	public int getFormOld() {
 		return formOld;
 	}
 	public void setFormOld(int formOld) {
 		this.formOld = formOld;
 	}
 	public int getFuehrung() {
 		return fuehrung;
 	}
 	public void setFuehrung(int fuehrung) {
 		this.fuehrung = fuehrung;
 	}
 	public int getGehalt() {
 		return gehalt;
 	}
 	public void setGehalt(int gehalt) {
 		this.gehalt = gehalt;
 	}
 	public String getGruppe() {
 		return gruppe;
 	}
 	public void setGruppe(String gruppe) {
 		this.gruppe = gruppe;
 	}
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getKondi() {
 		return kondi;
 	}
 	public void setKondi(int kondi) {
 		this.kondi = kondi;
 	}
 	public int getKondiOld() {
 		return kondiOld;
 	}
 	public void setKondiOld(int kondiOld) {
 		this.kondiOld = kondiOld;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getNation() {
 		return nation;
 	}
 	public void setNation(int nation) {
 		this.nation = nation;
 	}
 	public float getPosWertAV() {
 		return posWertAV;
 	}
 	public void setPosWertAV(float posWertAV) {
 		this.posWertAV = posWertAV;
 	}
 	public float getPosWertAV_D() {
 		return posWertAV_D;
 	}
 	public void setPosWertAV_D(float posWertAV_D) {
 		this.posWertAV_D = posWertAV_D;
 	}
 	public float getPosWertAV_DOld() {
 		return posWertAV_DOld;
 	}
 	public void setPosWertAV_DOld(float posWertAV_DOld) {
 		this.posWertAV_DOld = posWertAV_DOld;
 	}
 	public float getPosWertAV_I() {
 		return posWertAV_I;
 	}
 	public void setPosWertAV_I(float posWertAV_I) {
 		this.posWertAV_I = posWertAV_I;
 	}
 	public float getPosWertAV_IOld() {
 		return posWertAV_IOld;
 	}
 	public void setPosWertAV_IOld(float posWertAV_IOld) {
 		this.posWertAV_IOld = posWertAV_IOld;
 	}
 	public float getPosWertAV_O() {
 		return posWertAV_O;
 	}
 	public void setPosWertAV_O(float posWertAV_O) {
 		this.posWertAV_O = posWertAV_O;
 	}
 	public float getPosWertAV_OOld() {
 		return posWertAV_OOld;
 	}
 	public void setPosWertAV_OOld(float posWertAV_OOld) {
 		this.posWertAV_OOld = posWertAV_OOld;
 	}
 	public float getPosWertAVOld() {
 		return posWertAVOld;
 	}
 	public void setPosWertAVOld(float posWertAVOld) {
 		this.posWertAVOld = posWertAVOld;
 	}
 	public float getPosWertFL() {
 		return posWertFL;
 	}
 	public void setPosWertFL(float posWertFL) {
 		this.posWertFL = posWertFL;
 	}
 	public float getPosWertFL_D() {
 		return posWertFL_D;
 	}
 	public void setPosWertFL_D(float posWertFL_D) {
 		this.posWertFL_D = posWertFL_D;
 	}
 	public float getPosWertFL_DOld() {
 		return posWertFL_DOld;
 	}
 	public void setPosWertFL_DOld(float posWertFL_DOld) {
 		this.posWertFL_DOld = posWertFL_DOld;
 	}
 	public float getPosWertFL_I() {
 		return posWertFL_I;
 	}
 	public void setPosWertFL_I(float posWertFL_I) {
 		this.posWertFL_I = posWertFL_I;
 	}
 	public float getPosWertFL_IOld() {
 		return posWertFL_IOld;
 	}
 	public void setPosWertFL_IOld(float posWertFL_IOld) {
 		this.posWertFL_IOld = posWertFL_IOld;
 	}
 	public float getPosWertFL_O() {
 		return posWertFL_O;
 	}
 	public void setPosWertFL_O(float posWertFL_O) {
 		this.posWertFL_O = posWertFL_O;
 	}
 	public float getPosWertFL_OOld() {
 		return posWertFL_OOld;
 	}
 	public void setPosWertFL_OOld(float posWertFL_OOld) {
 		this.posWertFL_OOld = posWertFL_OOld;
 	}
 	public float getPosWertFLOld() {
 		return posWertFLOld;
 	}
 	public void setPosWertFLOld(float posWertFLOld) {
 		this.posWertFLOld = posWertFLOld;
 	}
 	public float getPosWertIV() {
 		return posWertIV;
 	}
 	public void setPosWertIV(float posWertIV) {
 		this.posWertIV = posWertIV;
 	}
 	public float getPosWertIV_A() {
 		return posWertIV_A;
 	}
 	public void setPosWertIV_A(float posWertIV_A) {
 		this.posWertIV_A = posWertIV_A;
 	}
 	public float getPosWertIV_AOld() {
 		return posWertIV_AOld;
 	}
 	public void setPosWertIV_AOld(float posWertIV_AOld) {
 		this.posWertIV_AOld = posWertIV_AOld;
 	}
 	public float getPosWertIV_O() {
 		return posWertIV_O;
 	}
 	public void setPosWertIV_O(float posWertIV_O) {
 		this.posWertIV_O = posWertIV_O;
 	}
 	public float getPosWertIV_OOld() {
 		return posWertIV_OOld;
 	}
 	public void setPosWertIV_OOld(float posWertIV_OOld) {
 		this.posWertIV_OOld = posWertIV_OOld;
 	}
 	public float getPosWertIVOld() {
 		return posWertIVOld;
 	}
 	public void setPosWertIVOld(float posWertIVOld) {
 		this.posWertIVOld = posWertIVOld;
 	}
 	public float getPosWertMI() {
 		return posWertMI;
 	}
 	public void setPosWertMI(float posWertMI) {
 		this.posWertMI = posWertMI;
 	}
 	public float getPosWertMI_A() {
 		return posWertMI_A;
 	}
 	public void setPosWertMI_A(float posWertMI_A) {
 		this.posWertMI_A = posWertMI_A;
 	}
 	public float getPosWertMI_AOld() {
 		return posWertMI_AOld;
 	}
 	public void setPosWertMI_AOld(float posWertMI_AOld) {
 		this.posWertMI_AOld = posWertMI_AOld;
 	}
 	public float getPosWertMI_D() {
 		return posWertMI_D;
 	}
 	public void setPosWertMI_D(float posWertMI_D) {
 		this.posWertMI_D = posWertMI_D;
 	}
 	public float getPosWertMI_DOld() {
 		return posWertMI_DOld;
 	}
 	public void setPosWertMI_DOld(float posWertMI_DOld) {
 		this.posWertMI_DOld = posWertMI_DOld;
 	}
 	public float getPosWertMI_O() {
 		return posWertMI_O;
 	}
 	public void setPosWertMI_O(float posWertMI_O) {
 		this.posWertMI_O = posWertMI_O;
 	}
 	public float getPosWertMI_OOld() {
 		return posWertMI_OOld;
 	}
 	public void setPosWertMI_OOld(float posWertMI_OOld) {
 		this.posWertMI_OOld = posWertMI_OOld;
 	}
 	public float getPosWertMIOld() {
 		return posWertMIOld;
 	}
 	public void setPosWertMIOld(float posWertMIOld) {
 		this.posWertMIOld = posWertMIOld;
 	}
 	public float getPosWertST() {
 		return posWertST;
 	}
 	public void setPosWertST(float posWertST) {
 		this.posWertST = posWertST;
 	}
 	public float getPosWertST_D() {
 		return posWertST_D;
 	}
 	public void setPosWertST_D(float posWertST_D) {
 		this.posWertST_D = posWertST_D;
 	}
 	public float getPosWertST_DOld() {
 		return posWertST_DOld;
 	}
 	public void setPosWertST_DOld(float posWertST_DOld) {
 		this.posWertST_DOld = posWertST_DOld;
 	}
 	public float getPosWertSTOld() {
 		return posWertSTOld;
 	}
 	public void setPosWertSTOld(float posWertSTOld) {
 		this.posWertSTOld = posWertSTOld;
 	}
 	public float getPosWertTW() {
 		return posWertTW;
 	}
 	public void setPosWertTW(float posWertTW) {
 		this.posWertTW = posWertTW;
 	}
 	public float getPosWertTWOld() {
 		return posWertTWOld;
 	}
 	public void setPosWertTWOld(float posWertTWOld) {
 		this.posWertTWOld = posWertTWOld;
 	}
 	public int getPs() {
 		return ps;
 	}
 	public void setPs(int ps) {
 		this.ps = ps;
 	}
 	public int getPsOld() {
 		return psOld;
 	}
 	public void setPsOld(int psOld) {
 		this.psOld = psOld;
 	}
 	public int getSp() {
 		return sp;
 	}
 	public void setSp(int sp) {
 		this.sp = sp;
 	}
 	public int getSpezialitaet() {
 		return spezialitaet;
 	}
 	public void setSpezialitaet(int spezialitaet) {
 		this.spezialitaet = spezialitaet;
 	}
 	public int getSpOld() {
 		return spOld;
 	}
 	public void setSpOld(int spOld) {
 		this.spOld = spOld;
 	}
 	public int getSt() {
 		return st;
 	}
 	public void setSt(int st) {
 		this.st = st;
 	}
 	public int getStOld() {
 		return stOld;
 	}
 	public void setStOld(int stOld) {
 		this.stOld = stOld;
 	}
 	public int getTs() {
 		return ts;
 	}
 	public void setTs(int ts) {
 		this.ts = ts;
 	}
 	public int getTsi() {
 		return tsi;
 	}
 	public void setTsi(int tsi) {
 		this.tsi = tsi;
 	}
 	public int getTsOld() {
 		return tsOld;
 	}
 	public void setTsOld(int tsOld) {
 		this.tsOld = tsOld;
 	}
 	public int getTw() {
 		return tw;
 	}
 	public void setTw(int tw) {
 		this.tw = tw;
 	}
 	public int getTwOld() {
 		return twOld;
 	}
 	public void setTwOld(int twOld) {
 		this.twOld = twOld;
 	}
 	public int getVe() {
 		return ve;
 	}
 	public void setVe(int ve) {
 		this.ve = ve;
 	}
 	public int getVeOld() {
 		return veOld;
 	}
 	public void setVeOld(int veOld) {
 		this.veOld = veOld;
 	}
 }
