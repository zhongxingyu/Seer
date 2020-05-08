 package com.hilotec.elexis.kgview.data;
 
 import ch.elexis.data.Konsultation;
 import ch.elexis.data.PersistentObject;
 import ch.rgw.tools.TimeTool;
 
 public class KonsData extends PersistentObject {
	public static final String VERSION = "6";
 	public static final String PLUGIN_ID = "com.hilotec.elexis.kgview";
 	
 	private static final String TABLENAME = "COM_HILOTEC_ELEXIS_KGVIEW_KONSDATA";
 	public static final String FLD_JETZLEIDEN		= "JetzigesLeiden";
 	public static final String FLD_JETZLEIDEN_ICPC	= "JetzigesLeidenICPC";
 	public static final String FLD_ALLGSTATUS 		= "AllgStatus";
 	public static final String FLD_LOKSTATUS 		= "LokStatus";
 	public static final String FLD_PROZEDERE 		= "Prozedere";
 	public static final String FLD_PROZEDERE_ICPC	= "ProzedereICPC";
 	public static final String FLD_DIAGNOSE			= "Diagnose";
 	public static final String FLD_DIAGNOSE_ICPC	= "DiagnoseICPC";
 	public static final String FLD_THERAPIE			= "Therapie";
 	public static final String FLD_VERLAUF			= "Verlauf";
 	public static final String FLD_ROENTGEN			= "Roentgen";
 	public static final String FLD_EKG				= "EKG";
 	public static final String FLD_KONSZEIT			= "KonsZeit";
 	public static final String FLD_KONSBEGINN		= "KonsBeginn";
 
 	static {
 		addMapping(TABLENAME,
 				"JetzigesLeiden=JetzLeiden",
 				"JetzigesLeidenICPC=JetzLeidenICPC",
 				"AllgStatus",
 				"LokStatus",
 				"Prozedere",
 				"ProzedereICPC",
 				"Diagnose",
 				"DiagnoseICPC",
 				"Therapie",
 				"Verlauf",
 				"Roentgen",
 				"EKG",
 				"KonsZeit",
 				"KonsBeginn");
 		checkTable();
 	}
 
 	private static final String create =
 		"CREATE TABLE " + TABLENAME + " ("
 			+ "  ID				VARCHAR(25) PRIMARY KEY, "
 			+ "  lastupdate 	BIGINT, "
 			+ "  deleted		CHAR(1) DEFAULT '0', "
 			+ "  JetzLeiden		TEXT, "
 			+ "  JetzLeidenICPC	TEXT, "
 			+ "  AllgStatus		TEXT, "
 			+ "  LokStatus		TEXT, "
 			+ "  Prozedere		TEXT, "
 			+ "  ProzedereICPC	TEXT, "
 			+ "  Diagnose		TEXT, "
 			+ "  DiagnoseICPC	TEXT, "
 			+ "  Therapie		TEXT, "
 			+ "  Verlauf		TEXT, "
 			+ "  Roentgen		TEXT, "
 			+ "  EKG			TEXT, "
			+ "  KonsZeit 		BIGINT DEFAULT 0, "
 			+ "  KonsBeginn     BIGINT  "
 			+ ");"
 			+ "INSERT INTO " + TABLENAME + " (ID, JetzLeiden) VALUES "
 			+ "	('VERSION', '" + VERSION + "');";
 
 	private static final String up_1to2 =
 		"ALTER TABLE " + TABLENAME
 			+ "  ADD JetzLeidenICPC	TEXT AFTER JetzLeiden,"
 			+ "  ADD ProzedereICPC 	TEXT AFTER Prozedere,"
 			+ "  ADD DiagnoseICPC 	TEXT AFTER Diagnose;"
 			+ "UPDATE " + TABLENAME + " SET JetzLeiden = '2' WHERE"
 			+ "  ID LIKE 'VERSION';";
 	
 	private static final String up_2to3 =
 		"ALTER TABLE " + TABLENAME
 			+ "  ADD Therapie	TEXT AFTER DiagnoseICPC,"
 			+ "  ADD Verlauf 	TEXT AFTER Therapie;"
 			+ "UPDATE " + TABLENAME + " SET JetzLeiden = '3' WHERE"
 			+ "  ID LIKE 'VERSION';";
 	
 	private static final String up_3to4 =
 		"ALTER TABLE " + TABLENAME
 			+ "  ADD Roentgen	TEXT AFTER Verlauf,"
 			+ "  ADD EKG		TEXT AFTER Roentgen;"
 			+ "UPDATE " + TABLENAME + " SET JetzLeiden = '4' WHERE"
 			+ "  ID LIKE 'VERSION';";
 	
 	private static final String up_4to5 =
 		"ALTER TABLE " + TABLENAME
 			+ "  ADD KonsBeginn	BIGINT AFTER KonsZeit;"
 			+ "UPDATE " + TABLENAME + " SET JetzLeiden = '5' WHERE"
 			+ "  ID LIKE 'VERSION';";
 	
	private static final String up_5to6 =
		"ALTER TABLE " + TABLENAME
			+ "  CHANGE KonsZeit KonsZeit BIGINT DEFAULT 0;"
			+ "UPDATE " + TABLENAME + " SET JetzLeiden = '5' WHERE"
			+ "  ID LIKE 'VERSION';";

	
 	private static void checkTable() {
 		KonsData check = load("VERSION");
 		if (!check.exists()) {
 			createOrModifyTable(create);
 		} else {
 			if (check.getJetzigesLeiden().equals("1"))
 				createOrModifyTable(up_1to2);
 			if (check.getJetzigesLeiden().equals("2"))
 				createOrModifyTable(up_2to3);
 			if (check.getJetzigesLeiden().equals("3"))
 				createOrModifyTable(up_3to4);
 			if (check.getJetzigesLeiden().equals("4"))
 				createOrModifyTable(up_4to5);
			if (check.getJetzigesLeiden().equals("5"))
				createOrModifyTable(up_5to6);
 		}
 	}
 
 	public static KonsData load(final String id) {
 		return new KonsData(id);
 	}
 	
 	public static KonsData load(Konsultation kons) {
 		return load(kons.getId());
 	}
 	
 	protected KonsData() {}
 	
 	protected KonsData(final String id) {
 		super(id);
 	}
 	
 	public KonsData(Konsultation kons) {
 		super(kons.getId());
 		if (!exists()) {
 			create(kons.getId());
 			set(FLD_KONSBEGINN,
 				Long.toString(System.currentTimeMillis()));
 		}
 	}
 
 	@Override
 	public String getLabel() {
		return getKonsultation().getLabel();
 	}
 
 	@Override
 	protected String getTableName() {
 		return TABLENAME;
 	}
 
 
 	public String getJetzigesLeiden() {
 		return get(FLD_JETZLEIDEN);
 	}
 	
 	public String getJetzigesLeidenICPC() {
 		return get(FLD_JETZLEIDEN_ICPC);
 	}
 	
 	public String getAllgemeinstatus() {
 		return get(FLD_ALLGSTATUS);
 	}
 	
 	public String getLokalstatus() {
 		return get(FLD_LOKSTATUS);
 	}
 
 	public String getProzedere() {
 		return get(FLD_PROZEDERE);
 	}
 	
 	public String getProzedereICPC() {
 		return get(FLD_PROZEDERE_ICPC);
 	}
 
 	public String getDiagnose() {
 		return get(FLD_DIAGNOSE);
 	}
 	
 	public String getDiagnoseICPC() {
 		return get(FLD_DIAGNOSE_ICPC);
 	}
 	
 	public String getTherapie() {
 		return get(FLD_THERAPIE);
 	}
 	
 	public String getVerlauf() {
 		return get(FLD_VERLAUF);
 	}
 	
 	public String getRoentgen() {
 		return get(FLD_ROENTGEN);
 	}
 	
 	public String getEKG() {
 		return get(FLD_EKG);
 	}
 	
 	public long getKonsZeit() {
 		return Long.parseLong(get(FLD_KONSZEIT));
 	}
 	
 	public String getKonsBeginn() {
 		String ts = get(FLD_KONSBEGINN);
 		if (ts == null || ts.equals("")) return "";
 		
 		TimeTool t = new TimeTool();
 		t.setTimeInMillis(Long.parseLong(ts));
 		return t.toString(TimeTool.TIME_SMALL);
 	}
 
	public Konsultation getKonsultation() {
		return Konsultation.load(getId());
	}
	
 
 	public void setJetzigesLeiden(String txt) {
 		set(FLD_JETZLEIDEN, txt);
 	}
 
 	public void setAllgemeinstatus(String txt) {
 		set(FLD_ALLGSTATUS, txt);
 	}
 	
 	public void setLokalstatus(String txt) {
 		set(FLD_LOKSTATUS, txt);
 	}
 
 	public void setProzedere(String txt) {
 		set(FLD_PROZEDERE, txt);
 	}
 
 	public void setDiagnose(String txt) {
 		set(FLD_DIAGNOSE, txt);
 	}
 	
 	public void setTherapie(String txt) {
 		set(FLD_THERAPIE, txt);
 	}
 	
 	public void setVerlauf(String txt) {
 		set(FLD_VERLAUF, txt);
 	}
 	
 	public void setRoentgen(String txt) {
 		set(FLD_ROENTGEN, txt);
 	}
 	
 	public void setEKG(String txt) {
 		set(FLD_EKG, txt);
 	}
 	
 	public void setKonsZeit(long zeit) {
 		set(FLD_KONSZEIT, Long.toString(zeit));
 	}
 }
