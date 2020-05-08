 package haushalt.auswertung.domain;
 
 import haushalt.daten.Datum;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.Properties;
 
 public class HaushaltDefinition {
 	public static final String COPYRIGHT = "jHaushalt v2.6 * (C)opyright 2002-2011 Lars H. Hahn";
 	public static final String VERSION = "2.6";
 	public static final String PROPERTIES_FILENAME = ".jhh";
 
 	private static final String KEY_JHH_FOLDER_NAME = "jhh.ordner";
 	private static final String KEY_JHH_FILENAME = "jhh.dateiname";
 	private Properties properties;
 	
 	private MainWindowProperties mainWindowProperties;
 	private ColumnModelProperties columnModelProperties;
 	
 	
 	public HaushaltDefinition(Properties properties) {
 		this.properties = properties;
 		mainWindowProperties = new MainWindowProperties(getProperties());
		columnModelProperties = new ColumnModelProperties(getProperties());
 	}
 	
 	public String getJhhFileName() {
 		return properties.getProperty(KEY_JHH_FILENAME);
 	}
 
 	public void setJhhFileName(String jhhFileName) {
 		this.properties.setProperty(KEY_JHH_FILENAME, jhhFileName);
 	}
 	
 	public String getJhhFolder() {
 		return this.properties.getProperty(KEY_JHH_FOLDER_NAME);
 	}
 	
 	public void setJhhFolder(String jhhFolderName) {
 		this.properties.setProperty(KEY_JHH_FOLDER_NAME, jhhFolderName);
 	}
 	
 	public String getUserOrSystemLocale() {
 		return getProperty("jhh.opt.sprache", "" + Locale.getDefault());
 	}
 	
 	public Color getSelectionColor() {
 		final int farbe = new Integer(getProperty("jhh.opt.selektion", "12632256")).intValue(); // #c0c0c0
 		return new Color(farbe);
 	}
 
 	public Color getGridColor() {
 		final int farbe = new Integer(getProperty("jhh.opt.gitter", "10066329")).intValue(); // #999999
 		return new Color(farbe);
 	}
 
 	public Color getFarbeZukunft() {
 		final int farbe = new Integer(getProperty("jhh.opt.zukunft", "16777088")).intValue(); // #ffff80
 		return new Color(farbe);
 	}
 
 	public String getFontName() {
 		return getProperty("jhh.opt.font", "SansSerif");
 	}
 
 	public int getFontSize() {
 		return new Integer(getProperty("jhh.opt.punkt", "12")).intValue();
 	}
 
 	
 	
 	@Deprecated
 	private String getProperty(String key) {
 		return this.properties.getProperty(key);
 	}
 
 	@Deprecated
 	public String getProperty(String key, String defaultValue) {
 		String value = getProperty(key);
 		return (value != null && "".equals(value))? value : defaultValue;
 	}
 
 	public MainWindowProperties getMainWindowProperties() {
 		return mainWindowProperties;
 	}
 	
 	public ColumnModelProperties getColumnModelProperties() {
 		return columnModelProperties;
 	}
 	
 	public Properties getDlgOptionProperties() {
 		return getProperties();
 	}
 	
 	public Properties getDlgCreateProperties() {
 		return getProperties();
 	}
 	
 	@Deprecated
 	public Properties getProperties() {
 		return this.properties;
 	}
 
 	public void setProperty(String key, String value) {
 		this.properties.setProperty(key, value);
 	}
 	
 	public void save() throws HaushaltDefinitionException {
 		// Speichert die individuellen Programmeigenschaften in die Datei
 		// <i>PROPERTIES_FILENAME</i>.
 		final String userHome = System.getProperty("user.home");
 		final File datei = new File(userHome, HaushaltDefinition.PROPERTIES_FILENAME);
 		try {
 			final FileOutputStream fos = new FileOutputStream(datei);
 			properties.store(fos, "Properties: " + HaushaltDefinition.VERSION);
 			fos.close();
 		} catch (FileNotFoundException e1) {
 			throw new HaushaltDefinitionException();
 		} catch (IOException e) {
 			throw new HaushaltDefinitionException();
 		}
 	}
 
 	public Properties createJHHDialogProperties() {
 		return getProperties();
 	}
 
 	public Properties getDateRendererProperties() {
 		return getProperties();
 	}
 
 	public String getTabPlacement() {
 		return getProperty("jhh.opt.reiter", "BOTTOM");
 	}
 
 	public Datum getTransactionStartDate() {
 		return new Datum(getProperty("jhh.opt.startdatum", "01.01.00"));
 	}
 	
 	public boolean haveExistingTransactions() {
 		// FIXME what the fuck is that? I mean, what's this property defining?
 		return Boolean.valueOf(getProperty("jhh.opt.gemerkte", "true")).booleanValue();
 	}
 
 	
 	public int setDeleteKeyCode() {
 		int idx;
 		try {
 			idx = Integer.parseInt(getProperty("jhh.opt.deltaste", "0"));
 		} catch (final NumberFormatException e) {
 			// Kann auftreten, da Version < 2.5 noch kein Index, sondern
 			// Klartext gespeichert hat
 			idx = 0;
 			setProperty("jhh.opt.deltaste", "0");
 		}
 		switch (idx) {
 			case 1:
 			case 2:
 			case 3:
 				return idx; // compare to InputEvent.ALT_MASK et al.
 			default:
 				return 0;
 		}
 	}
 
 	public String getCustomColorCodes() {
 		return getProperty("jhh.opt.custom", "16776960");
 	}
 	
 	public int getEvaluationWidth() {
 		return new Integer(getProperty("jhh.auswertung.breite", "600")).intValue();
 	}
 
 	public int getEvaluationHeight() {
 		return new Integer(getProperty("jhh.auswertung.hoehe", "400")).intValue();
 	}
 	
 	public boolean isDataImportInEuroCurrency() {
 		return Boolean.valueOf(getProperty("jhh.opt.euroimport", "true")).booleanValue();
 	}
 
 }
