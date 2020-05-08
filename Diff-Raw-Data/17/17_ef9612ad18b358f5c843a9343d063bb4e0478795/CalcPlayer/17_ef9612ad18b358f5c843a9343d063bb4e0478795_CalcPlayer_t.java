 /*
  * Created on 13.07.2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package hoplugins.playerCompare;
 
 //import java.util.*;
 
 /**
  * @author KickMuck
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class CalcPlayer{
 
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
 	
 	public CalcPlayer(){};
		
 	public void setName(String spName)
 	{
 		name = spName;
 	}
 	public void setAlter(int spAlter)
 	{
 		alter = spAlter;
 	}
 	public void setID(int spID)
 	{
 		id = spID;
 	}
 	public void setGehalt(int spGehalt)
 	{
 		gehalt = spGehalt;
 	}
 	public void setTSI(int spTSI)
 	{
 		tsi = spTSI;
 	}
 	public void setNation(int spNation)
 	{
 		nation = spNation;
 	}
 	public void setFuehrung(int spFuehrung)
 	{
 		fuehrung = spFuehrung;
 	}
 	public void setErfahrung(int spErfahrung)
 	{
 		erfahrung = spErfahrung;
 		//m_miniModel.getAdapter().executeUpdate("update playerCompare_CalcPlayer set erfahrung=" + spErfahrung);
 	}
 	public void setForm(int spForm)
 	{
 		form = spForm;
 	}
 	public void setKondi(int spKondi)
 	{
 		kondi = spKondi;
 	}
 	public void setTorwart(int spTorwart)
 	{
 		tw = spTorwart;
 	}
 	public void setVerteidigung(int spVerteidigung)
 	{
 		ve = spVerteidigung;
 	}
 	public void setSpielaufbau(int spSpielaufbau)
 	{
 		sp = spSpielaufbau;
 	}
 	public void setPasspiel(int spPasspiel)
 	{
 		ps = spPasspiel;
 	}
 	public void setFluegel(int spFluegel)
 	{
 		fl = spFluegel;
 	}
 	public void setTorschuss(int spTorschuss)
 	{
 		ts = spTorschuss;
 	}
 	public void setStandards(int spStandards)
 	{
 		st = spStandards;
 	}
 	public void setOldErfahrung(int spErfahrung)
 	{
 		erfahrungOld = spErfahrung;
 	}
 	public void setOldForm(int spForm)
 	{
 		formOld = spForm;
 	}
 	public void setOldKondi(int spKondi)
 	{
 		kondiOld = spKondi;
 	}
 	public void setOldTorwart(int spTorwart)
 	{
 		twOld = spTorwart;
 	}
 	public void setOldVerteidigung(int spVerteidigung)
 	{
 		veOld = spVerteidigung;
 	}
 	public void setOldSpielaufbau(int spSpielaufbau)
 	{
 		spOld = spSpielaufbau;
 	}
 	public void setOldPasspiel(int spPasspiel)
 	{
 		psOld = spPasspiel;
 	}
 	public void setOldFluegel(int spFluegel)
 	{
 		flOld = spFluegel;
 	}
 	public void setOldTorschuss(int spTorschuss)
 	{
 		tsOld = spTorschuss;
 	}
 	public void setOldStandards(int spStandards)
 	{
 		stOld = spStandards;
 	}
 	public void setTW(float spTW)
 	{
 		posWertTW = spTW;
 	}
 	public void setIV(float spIV)
 	{
 		posWertIV = spIV;
 	}
 	public void setIVoff(float spIVoff)
 	{
 		posWertIV_O = spIVoff;
 	}
 	public void setIVaussen(float spIVaussen)
 	{
 		posWertIV_A = spIVaussen;
 	}
 	public void setAV(float spAV)
 	{
 		posWertAV = spAV;
 	}
 	public void setAVdef(float spAVdef)
 	{
 		posWertAV_D = spAVdef;
 	}
 	public void setAVoff(float spAVoff)
 	{
 		posWertAV_O = spAVoff;
 	}
 	public void setAVinnen(float spAVinnen)
 	{
 		posWertAV_I = spAVinnen;
 	}
 	public void setMI(float spMI)
 	{
 		posWertMI = spMI;
 	}
 	public void setMIaussen(float spMIaussen)
 	{
 		posWertMI_A = spMIaussen;
 	}
 	public void setMIdef(float spMIdef)
 	{
 		posWertMI_D = spMIdef;
 	}
 	public void setMIoff(float spMIoff)
 	{
 		posWertMI_O = spMIoff;
 	}
 	public void setFL(float spFL)
 	{
 		posWertFL = spFL;
 	}
 	public void setFLdef(float spFLdef)
 	{
 		posWertFL_D = spFLdef;
 	}
 	public void setFLoff(float spFLoff)
 	{
 		posWertFL_O = spFLoff;
 	}
 	public void setFLinnen(float spFLinnen)
 	{
 		posWertFL_I = spFLinnen;
 	}
 	public void setST(float spST)
 	{
 		posWertST = spST;
 	}
 	public void setSTdef(float spSTdef)
 	{
 		posWertST_D = spSTdef;
 	}
 	public void setOldTW(float spTW)
 	{
 		posWertTWOld = spTW;
 	}
 	public void setOldIV(float spIV)
 	{
 		posWertIVOld = spIV;
 	}
 	public void setOldIVoff(float spIVoff)
 	{
 		posWertIV_OOld = spIVoff;
 	}
 	public void setOldIVaussen(float spIVaussen)
 	{
 		posWertIV_AOld = spIVaussen;
 	}
 	public void setOldAV(float spAV)
 	{
 		posWertAVOld = spAV;
 	}
 	public void setOldAVdef(float spAVdef)
 	{
 		posWertAV_DOld = spAVdef;
 	}
 	public void setOldAVoff(float spAVoff)
 	{
 		posWertAV_OOld = spAVoff;
 	}
 	public void setOldAVinnen(float spAVinnen)
 	{
 		posWertAV_IOld = spAVinnen;
 	}
 	public void setOldMI(float spMI)
 	{
 		posWertMIOld = spMI;
 	}
 	public void setOldMIaussen(float spMIaussen)
 	{
 		posWertMI_AOld = spMIaussen;
 	}
 	public void setOldMIdef(float spMIdef)
 	{
 		posWertMI_DOld = spMIdef;
 	}
 	public void setOldMIoff(float spMIoff)
 	{
 		posWertMI_OOld = spMIoff;
 	}
 	public void setOldFL(float spFL)
 	{
 		posWertFLOld = spFL;
 	}
 	public void setOldFLdef(float spFLdef)
 	{
 		posWertFL_DOld = spFLdef;
 	}
 	public void setOldFLoff(float spFLoff)
 	{
 		posWertFL_OOld = spFLoff;
 	}
 	public void setOldFLinnen(float spFLinnen)
 	{
 		posWertFL_IOld = spFLinnen;
 	}
 	public void setOldST(float spST)
 	{
 		posWertSTOld = spST;
 	}
 	public void setOldSTdef(float spSTdef)
 	{
 		posWertST_DOld = spSTdef;
 	}
 	public void setBestPosition(byte spBestPosition)
 	{
 		bestPosition = spBestPosition;
 	}
 	public void setBestPositionStaerke(float spBestPosStaerke)
 	{
 		bestPosStaerke = spBestPosStaerke;
 	}
 	public void setOldBestPosition(byte spBestPosition)
 	{
 		bestPositionOld = spBestPosition;
 	}
 	public void setOldBestPositionStaerke(float spBestPosStaerke)
 	{
 		bestPosStaerkeOld = spBestPosStaerke;
 	}
 	public void setGruppe(String spGruppe)
 	{
 		gruppe = spGruppe;
 	}
 	public void setSpezialitaet(int spSpezi)
 	{
 		spezialitaet = spSpezi;
 	}
 	
 	//*** GETTER ***
 	public String getName()
 	{
 		return name;
 	}
 	public int getAlter()
 	{
 		return alter;
 	}
 	public int getID()
 	{
 		return id;
 	}
 	public int getGehalt()
 	{
 		return gehalt;
 	}
 	public int getTSI()
 	{
 		return tsi;
 	}
 	public int getNation()
 	{
 		return nation;
 	}
 	public int getFuehrung()
 	{
 		return fuehrung;
 	}
 	public int getErfahrung()
 	{
 		
 		return erfahrung;
 		
 	}
 	public int getForm()
 	{
 		return form;
 	}
 	public int getKondi()
 	{
 		return kondi;
 	}
 	public int getTorwart()
 	{
 		return tw ;
 	}
 	public int getVerteidigung()
 	{
 		return ve;
 	}
 	public int getSpielaufbau()
 	{
 		return sp;
 	}
 	public int getPasspiel()
 	{
 		return ps;
 	}
 	public int getFluegel()
 	{
 		return fl;
 	}
 	public int getTorschuss()
 	{
 		return ts;
 	}
 	public int getStandards()
 	{
 		return st;
 	}
 	public int getOldErfahrung()
 	{
 		return erfahrungOld;
 	}
 	public int getOldForm()
 	{
 		return formOld;
 	}
 	public int getOldKondi()
 	{
 		return kondiOld;
 	}
 	public int getOldTorwart()
 	{
 		return twOld;
 	}
 	public int getOldVerteidigung()
 	{
 		return veOld;
 	}
 	public int getOldSpielaufbau()
 	{
 		return spOld;
 	}
 	public int getOldPasspiel()
 	{
 		return psOld;
 	}
 	public int getOldFluegel()
 	{
 		return flOld;
 	}
 	public int getOldTorschuss()
 	{
 		return tsOld;
 	}
 	public int getOldStandards()
 	{
 		return stOld;
 	}
 	public float getTW()
 	{
 		return posWertTW;
 	}
 	public float getIV()
 	{
 		return posWertIV;
 	}
 	public float getIVoff()
 	{
 		return posWertIV_O;
 	}
 	public float getIVaussen()
 	{
 		return posWertIV_A;
 	}
 	public float getAV()
 	{
 		return posWertAV;
 	}
 	public float getAVdef()
 	{
 		return posWertAV_D;
 	}
 	public float getAVoff()
 	{
 		return posWertAV_O;
 	}
 	public float getAVinnen()
 	{
 		return posWertAV_I;
 	}
 	public float getMI()
 	{
 		return posWertMI;
 	}
 	public float getMIaussen()
 	{
 		return posWertMI_A;
 	}
 	public float getMIdef()
 	{
 		return posWertMI_D;
 	}
 	public float getMIoff()
 	{
 		return posWertMI_O;
 	}
 	public float getFL()
 	{
 		return posWertFL;
 	}
 	public float getFLdef()
 	{
 		return posWertFL_D;
 	}
 	public float getFLoff()
 	{
 		return posWertFL_O;
 	}
 	public float getFLinnen()
 	{
 		return posWertFL_I;
 	}
 	public float getST()
 	{
 		return posWertST;
 	}
 	public float getSTdef()
 	{
 		return posWertST_D;
 	}
 	public float getOldTW()
 	{
 		return posWertTWOld;
 	}
 	public float getOldIV()
 	{
 		return posWertIVOld;
 	}
 	public float getOldIVoff()
 	{
 		return posWertIV_OOld;
 	}
 	public float getOldIVaussen()
 	{
 		return posWertIV_AOld;
 	}
 	public float getOldAV()
 	{
 		return posWertAVOld;
 	}
 	public float getOldAVdef()
 	{
 		return posWertAV_DOld;
 	}
 	public float getOldAVoff()
 	{
 		return posWertAV_OOld;
 	}
 	public float getOldAVinnen()
 	{
 		return posWertAV_IOld;
 	}
 	public float getOldMI()
 	{
 		return posWertMIOld;
 	}
 	public float getOldMIaussen()
 	{
 		return posWertMI_AOld;
 	}
 	public float getOldMIdef()
 	{
 		return posWertMI_DOld;
 	}
 	public float getOldMIoff()
 	{
 		return posWertMI_OOld;
 	}
 	public float getOldFL()
 	{
 		return posWertFLOld;
 	}
 	public float getOldFLdef()
 	{
 		return posWertFL_DOld;
 	}
 	public float getOldFLoff()
 	{
 		return posWertFL_OOld;
 	}
 	public float getOldFLinnen()
 	{
 		return posWertFL_IOld;
 	}
 	public float getOldST()
 	{
 		return posWertSTOld;
 	}
 	public float getOldSTdef()
 	{
 		return posWertST_DOld;
 	}
 	public byte getBestPosition()
 	{
 		return bestPosition;
 	}
 	public float getBestPositionStaerke()
 	{
 		return bestPosStaerke;
 	}
 	public byte getOldBestPosition()
 	{
 		return bestPositionOld;
 	}
 	public float getOldBestPositionStaerke()
 	{
 		return bestPosStaerkeOld;
 	}
 	public String getGruppe()
 	{
 		return gruppe;
 	}
 	public int getSpezialitaet()
 	{
 		return spezialitaet;
 	}
 }
