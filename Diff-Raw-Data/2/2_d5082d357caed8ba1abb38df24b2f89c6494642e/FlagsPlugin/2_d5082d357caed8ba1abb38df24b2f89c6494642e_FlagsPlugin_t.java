 package hoplugins;
 /**
  * FlagsPlugin.java
  *
  * @author Daniel Gonz�lez Fisher
  */
 
 import hoplugins.commons.utils.Debug;
 import hoplugins.flagsplugin.FlagCollection;
 import hoplugins.flagsplugin.FlagObject;
 import hoplugins.flagsplugin.FlagRenderer;
 import hoplugins.flagsplugin.FlagUpdater;
 import hoplugins.flagsplugin.Opciones;
 import hoplugins.flagsplugin.PlayersFlagSet;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JWindow;
 import javax.swing.ListSelectionModel;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import plugins.ISpieler;
 import plugins.IXMLParser;
 
 
 public class FlagsPlugin implements plugins.IPlugin, ActionListener, WindowListener, plugins.IOfficialPlugin, plugins.IRefreshable {
     public static final String NAME = "Flags Collector Plugin";
     public static final double INTERNAL_VERSION = 3.24;
     public static final double HO_VERSION = 1.400;
     //public static final double VERSION = HO_VERSION + (INTERNAL_VERSION / 1000.0);
     public static final double VERSION = INTERNAL_VERSION;
     public static final String AUTHOR = "Daniel González F. (Danthor)";
     public static final int PLUGIN_ID = 20;
     public static final String FILENAME_FCAWAY = "flagdata.obj";
     public static final String FILENAME_FCHOME = "flagdata2.obj";
     public static final String FILENAME_CONFIG = "config.obj";
     public static final String DATA_DIR = "hoplugins/flagsplugin/";
     //public static final String COOLNESS_URL = "http://student.science.uva.nl/~lhoek/coolness.php";
     //public static final String COOLNESS_DATA_URL = "http://student.science.uva.nl/~lhoek/coolcsv.php";
     public static final String COOLNESS_URL = "https://ow141.science.uva.nl:8080/~lhoek/coolness.php";
     //public static final String COOLNESS_DATA_URL = "https://ow141.science.uva.nl:8080/~lhoek/coolcsv.php";
     //public static final String COOLNESS_DATA_URL = "http://www.qedgroup.org/Flagsplugin/coolcsv.php";
 
     public static int FLAGS_PER_ROW = 5;
     public static boolean DOUBLECLICK_SELECTION = false;
     public static boolean OWN_FLAG = false;
     public static boolean SORT_LIST_BY_COOLNESS = false;
     public static plugins.IHOMiniModel HOM = null;
     public static FlagsPlugin FLAGSPLUGIN = null;
 
     private JPanel mainPanel, pluginPanel;
     private PlayersFlagSet playersPanel, oldPlayersPanel;
     private plugins.IHOMiniModel hoModel;
     private FlagUpdater flagUpdater;
     private static Properties countries;
     private static Properties leagues;
     private static HashMap<String,Integer> invertCountries;
     private static HashMap<Integer,Integer> teamIdCountry;
     private static HashMap<Integer,Double> coolnessRanking;
     private FlagCollection fcAway, fcHome;
     private JList listAway, listHome;
     private JMenuItem jmiAutoUpdateFlags;
     private JMenuItem jmiUpdateCoolness;
     private JMenuItem jmiOptions;
     private JMenuItem jmiCredits;
     private JLabel coolnessSumHome = new JLabel("");
     private JLabel coolnessSumAway = new JLabel("");
     private Opciones opciones;
     private Vector<FlagObject> allflags = new Vector<FlagObject>();
 
     /** Creates a new instance of FlagsPlugin */
     public FlagsPlugin () {
         FLAGSPLUGIN = this;
         opciones = Opciones.load();
         teamIdCountry = opciones.getTeamIdCountry();
         coolnessRanking = opciones.getCoolnessRanking();
 
         countries = new Properties();
         leagues = new Properties();
         try {
             BufferedInputStream is = new BufferedInputStream(new FileInputStream("hoplugins/flagsplugin/country.list"));
             countries.load(is);
             is.close();
             BufferedInputStream is2 = new BufferedInputStream(new FileInputStream("hoplugins/flagsplugin/league.list"));
             leagues.load(is2);
             is2.close();
         } catch(IOException e) {
         } catch(IllegalArgumentException iae) { }
         hoModel = null;
         invertCountries = new HashMap<String, Integer>();
         allflags = constructPaisesVector(true);
         if (SORT_LIST_BY_COOLNESS) Collections.sort(allflags, new FlagObject.CoolnessComparator());
         else Collections.sort(allflags);
         listAway = new JList(allflags);
         listHome = new JList(allflags);
     }
 
     /**
      * Is called by HO! to start the plugin
      */
     public void start (plugins.IHOMiniModel hOMiniModel) {
         //String sTabName = "";
         HOM = hOMiniModel;
         hoModel = hOMiniModel;
 
         /* Finally, add the panel to HO!s TabbedPane */
         mainPanel = new JPanel(new BorderLayout());
         mainPanel.add(createGUI());
         hoModel.getGUI().addTab("Flags",mainPanel);
 
         /* Add a JMenu to the HO! Mainframe, but ignore all events (no Listener) */
         JMenu menu = new javax.swing.JMenu("Flags Plugin");
         jmiAutoUpdateFlags = new JMenuItem("Auto Update Flags");
         jmiAutoUpdateFlags.addActionListener(this);
         jmiUpdateCoolness = new JMenuItem("Update Coolness Ranking");
         jmiUpdateCoolness.addActionListener(this);
         jmiOptions = new JMenuItem(hoModel.getLanguageString("Optionen"));
         jmiOptions.addActionListener(this);
         jmiCredits = new JMenuItem(hoModel.getLanguageString("Credits"));
         jmiCredits.addActionListener(this);
 
         menu.add(jmiAutoUpdateFlags);
         menu.add(jmiUpdateCoolness);
         menu.addSeparator();
         menu.add(jmiOptions);
         menu.addSeparator();
         menu.add(jmiCredits);
         hoModel.getGUI().addMenu(menu);
 
         /* we'd like to get informed by changes from ho */
         hoModel.getGUI().registerRefreshable(this);
 
         //pluginPanel.addAncestorListener(this);
         /* FOR VERSION 1.20 OF INTERFACE IGUI */
         hoModel.getGUI().addMainFrameListener(this);
     }
     private JScrollPane createGUI() {
         fcAway = new FlagCollection(this, FILENAME_FCAWAY, teamIdCountry, FlagCollection.T_AWAY);
         fcHome = new FlagCollection(this, FILENAME_FCHOME, teamIdCountry, FlagCollection.T_HOME);
         /* create an grass panel Background */
         if (hoModel!=null) pluginPanel = hoModel.getGUI().createImagePanel();
         createPanel(pluginPanel);
 
         //pluginPanel.setBorder(BorderFactory.createLineBorder(Color.RED,1));
         fcAway.loadFlags();
         fcHome.loadFlags();
         fcAway.refreshTitle();
         fcHome.refreshTitle();
         flagUpdater = new FlagUpdater(hoModel, teamIdCountry, fcAway, fcHome);
         return new JScrollPane(pluginPanel);
     }
     public void refreshGUI() {
         mainPanel.removeAll();
         mainPanel.add(createGUI());
         mainPanel.revalidate();
         mainPanel.repaint();
     }
 
     private Container createPanel(Container p) {
         GridBagLayout gbl = new GridBagLayout();
         GridBagConstraints gc = new GridBagConstraints();
         Container panel = null;
         if (p==null) {
             panel = new JPanel(gbl);
         }
         else {
             panel = p;
             p.setLayout(gbl);
         }
 
         JLabel lblVersion = new JLabel("Version " + VERSION);
         gc.anchor = GridBagConstraints.FIRST_LINE_START;
         gc.weightx = 1.0;
         gc.weighty = 0.0;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.gridheight = 1;
         //gc.gridx = 0;
         //gc.gridy = 0;
         gbl.setConstraints(lblVersion,gc);
         panel.add(lblVersion);
 
 //         Component glue3 = Box.createGlue();
 //         gc.gridwidth = GridBagConstraints.REMAINDER;
 //         gc.gridx = GridBagConstraints.RELATIVE;
 //         gc.gridy = GridBagConstraints.RELATIVE;
 //         gbl.setConstraints(glue3,gc);
 //         panel.add(glue3);
 
         // ******* Paises visitados *****************************************************
         Box grid = createGrid(fcAway);
         grid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),"Visited Countries"));
         JPanel panel1 = new JPanel(new BorderLayout());
         panel1.setOpaque(false);
         panel1.add(grid, BorderLayout.CENTER);
         panel1.add(coolnessSumAway, BorderLayout.SOUTH);
         gc.anchor = GridBagConstraints.NORTHEAST;
         //gc.weightx = 0.0;
         gc.weighty = 1.0;
         gc.insets = new Insets(10,10,10,10);
         gc.gridwidth = 1;
         gc.gridheight = GridBagConstraints.RELATIVE;
         gc.fill = GridBagConstraints.NONE;
         gbl.setConstraints(grid,gc);
         panel.add(panel1);
 
         JScrollPane jsp = createScroll(fcAway,listAway);
         //jsp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),"Country List"));
         gc.anchor = GridBagConstraints.NORTH;
         gc.fill = GridBagConstraints.VERTICAL;
         gbl.setConstraints(jsp,gc);
         panel.add(jsp);
 
         // ******* Paises que han venido *****************************************************
         Box grid2 = createGrid(fcHome);
         grid2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),"Hosted Countries"));
         JPanel panel2 = new JPanel(new BorderLayout());
         panel2.setOpaque(false);
         panel2.add(grid2, BorderLayout.CENTER);
         panel2.add(coolnessSumHome, BorderLayout.SOUTH);
         //gc.anchor = GridBagConstraints.NORTHEAST;
         gc.gridwidth = GridBagConstraints.RELATIVE;
         gc.fill = GridBagConstraints.NONE;
         gbl.setConstraints(grid2,gc);
         panel.add(panel2);
 
         JScrollPane jsp2 = createScroll(fcHome,listHome);
         gc.anchor = GridBagConstraints.NORTHWEST;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.fill = GridBagConstraints.VERTICAL;
         gbl.setConstraints(jsp2,gc);
         panel.add(jsp2);
         // ************
 
         Vector<ISpieler> allPlayers = null;
         Vector<ISpieler> allOldPlayers = null;
         try {
             allPlayers = hoModel.getAllSpieler();
             allOldPlayers = hoModel.getAllOldSpieler();
         } catch(NullPointerException v_npe) { }
 
         if (hoModel!=null) playersPanel = new PlayersFlagSet(allPlayers);
         else playersPanel = new PlayersFlagSet();
         playersPanel.setTitle("Current Players Flags");
         gc.weighty = 0.0;
         gc.gridwidth = 2; //GridBagConstraints.RELATIVE;
         gc.gridheight = GridBagConstraints.REMAINDER;
         gc.anchor = GridBagConstraints.NORTHEAST;
         gc.fill = GridBagConstraints.NONE;
         gbl.setConstraints(playersPanel,gc);
         panel.add(playersPanel);
 
         if (hoModel!=null) oldPlayersPanel = new PlayersFlagSet(allOldPlayers);
         else oldPlayersPanel = new PlayersFlagSet();
         oldPlayersPanel.removeRepetidos(playersPanel);
         oldPlayersPanel.setTitle("Old Players Lost Flags");
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.anchor = GridBagConstraints.NORTHWEST;
         gbl.setConstraints(oldPlayersPanel,gc);
         panel.add(oldPlayersPanel);
 
 //         Component glue1 = Box.createGlue();
 //         gc.gridwidth = GridBagConstraints.RELATIVE;
 //         gc.gridheight = GridBagConstraints.REMAINDER;
 //         gc.weightx = 1.0;
 //         gc.weighty = 1.0;
 //         gbl.setConstraints(glue1,gc);
 //         panel.add(glue1);
 
 //         Component glue2 = Box.createGlue();
 //         gc.gridwidth = GridBagConstraints.REMAINDER;
 //         gbl.setConstraints(glue2,gc);
 //         panel.add(glue2);
 
         return panel;
     }
 
     private JScrollPane createScroll(FlagCollection fc, JList lista) {
        FlagRenderer frender = new FlagRenderer(hoModel);
         frender.setFlagCollection(fc);  // ACA OJO!!!!!!!!!
         frender.setPreferredSize(new Dimension(200,20));
         lista.setCellRenderer(frender);
         lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         Color fondo = new Color(214,214,192);
         lista.setSelectionForeground(Color.WHITE);
         lista.setSelectionBackground(fondo.darker());
         //lista.setForeground();
         lista.setBackground(fondo);
         fc.setLista(lista);
         lista.addMouseListener(fc);
 
         JScrollPane scrollPane = new JScrollPane(lista);
         return scrollPane;
     }
     private Box createGrid(FlagCollection fc) {
         Box box = new Box(BoxLayout.Y_AXIS);
         //Dimension lsdim = new Dimension(100,1);
 //         JLabel lblSep = new JLabel(" ");
         //JPanel p0 = new JPanel();
         //p0.setPreferredSize(lsdim);
         //box.add(p0);
         box.add(Box.createHorizontalStrut(25 * FLAGS_PER_ROW));
         //box.add(new JScrollPane(fc));
         box.add(fc);
         box.add(Box.createVerticalGlue());
 
         //box.setBorder(BorderFactory.createLineBorder(Color.RED,1));
         return box;
     }
 
     /* return pluginName */
     public String getName() {
         return NAME;
     }
 
     protected Vector<FlagObject> constructPaisesVector(boolean firstTime) {
         Vector<FlagObject> paises = new Vector<FlagObject>();
         for (int i=1; i<190; i++) {
             if (countries.containsKey(Integer.toString(i))) {
                 Integer I = new Integer(i);
                 String S = countries.getProperty(I.toString(),"???");
                 if (firstTime) invertCountries.put(S,I);
                 double d = 0.0;
                 Object obj = coolnessRanking.get(I);
                 if (obj != null) {
                     d = ((Double)obj).doubleValue();
                 }
                 paises.add(new FlagObject(i,S,d));
             }
         }
         return paises;
     }
 
 //    public boolean updateCoolnessRanking_Old() {
 //        String str = getCoolnessRankingFromWeb();
 //        if (str == null || str.equals("")) return false;
 //        StringTokenizer tokens = new StringTokenizer(str);
 //        while (tokens.hasMoreTokens()) {
 //            String triplet = tokens.nextToken();
 //            if (triplet.charAt(0) == '#') continue;
 //            String [] valor = triplet.split(";");
 //            int countryId = getCountryIdFromLeague(valor[0]);
 //            double coolness = 0;
 //            if (valor[1].equals("infinite")) coolness = Double.POSITIVE_INFINITY;
 //            else try {
 //                coolness = Double.parseDouble(valor[1]);
 //            } catch (NumberFormatException nfe) { }
 //            coolnessRanking.put(new Integer(countryId), new Double(coolness));
 //        }
 //        //opciones.setCoolnessRanking(cool);
 //        return true;
 //    }
 
     public boolean updateCoolnessRanking() {
         String str = getCoolnessRankingFromWeb();
         if (str == null || str.equals("")) return false;
         IXMLParser xp = hoModel.getXMLParser();
         Document doc = xp.parseString(str);
 
         Element ele = null;
         Element root = null;
         NodeList list = null;
         if (doc == null) {
             return false;
         }
         root = doc.getDocumentElement();
         try {
         	// read active users from each country and store it in a temp. structure
             // count overall amount of users
         	HashMap<Integer, Integer> tmpCountries = new HashMap<Integer, Integer>(); //countryID - active users
         	int userCount = 0;
             root = (Element) root.getElementsByTagName("LeagueList").item(0);
             list = root.getElementsByTagName("League");
             for (int i = 0; (list != null) && (i < list.getLength()); i++) {
                 root = (Element) list.item(i);
                 //ele = (Element) root.getElementsByTagName("LeagueID").item(0);
                 //String leagueID = xp.getFirstChildNodeValue(ele);
                 ele = (Element) root.getElementsByTagName("ActiveUsers").item(0);
                 int active = Integer.parseInt(xp.getFirstChildNodeValue(ele));
                 root = (Element) root.getElementsByTagName("Country").item(0);
                 ele = (Element) root.getElementsByTagName("CountryID").item(0);
                 String countryID = xp.getFirstChildNodeValue(ele);
                 tmpCountries.put(new Integer(Integer.parseInt(countryID)), new Integer(active));
                 userCount += active;
             }
 
             // loop temp structure, calc coolness from user numbers
             for (Iterator<Integer> j=tmpCountries.keySet().iterator(); j.hasNext(); ) {
             	Integer country = j.next();
             	Integer active = tmpCountries.get(country);
             	if (country == null || active == null) {
             		continue;
             	} else if (active.intValue() == 0) { // no active users yet
             		coolnessRanking.put(country, new Double(Double.POSITIVE_INFINITY));
             	} else {
             		coolnessRanking.put(country, new Double((double)userCount/(double)active.intValue()));
             	}
             }
         } catch (Exception e) {
         	e.printStackTrace();
         }
 
         return true;
     }
 
     protected String getCoolnessRankingFromWeb() {
         // maybe check last time here.
         JWindow pbar = hoModel.getGUI().createWaitDialog(null);
         pbar.setVisible(true);
         String coolstr = "";
         try {
             //coolstr = hoModel.getDownloadHelper().getUsalWebPage(COOLNESS_DATA_URL, false);
         	coolstr = hoModel.getDownloadHelper().getHattrickXMLFile("/chppxml.axd?file=worlddetails");
             pbar.setVisible(false);
             pbar.dispose();
         } catch (IOException ioex) { return null; }
         pbar.setVisible(false);
         pbar.dispose();
         return coolstr;
     }
 
     public void reSortJLists() {
         allflags = constructPaisesVector(false);
         if (SORT_LIST_BY_COOLNESS) Collections.sort(allflags, new FlagObject.CoolnessComparator());
         else Collections.sort(allflags);
         listAway.setListData(allflags);
         listHome.setListData(allflags);
         listAway.repaint();
         listHome.repaint();
     }
 
 //     protected String getCool() {
 //         // maybe check last time here.
 //         BufferedReader brin = null;
 //         try {
 //             //brin = new BufferedReader(new InputStreamReader(new FileInputStream("coolcsv.txt")));
 //             brin = new BufferedReader(new FileReader("coolcsv.txt"));
 //         } catch (IOException ioex) { return "0"; }
 //         int c = 0;
 //         StringBuffer str = new StringBuffer(1024);
 //         try {
 //             while (c != -1) {
 //                 c = brin.read();
 //                 if (c==-1) break;
 //                 str.append((char)c);
 //             }
 //         } catch (IOException ioex) { }
 //         String r = str.toString();
 //         //r  = r.replace(' ', '\n');
 //         return r;
 //     }
 
 
     /* ============ STATIC METHODS ============ */
     public static String unencodePais(String s) {
         if (s == null) return s;
         if (s.charAt(0) == '\u00cd') return "I" + s.substring(1) + "*";
         if (s.charAt(0) == '\u00d6') return "O" + s.substring(1) + "*";
         if (s.charAt(0) == 'C' && s.charAt(1) == '\u030c') return "C" + s.substring(2) + "*";
         if (s.charAt(0) == 'L' && s.charAt(1) == '\u00eb') return "Le" + s.substring(2) + "*";
         if (s.equals("\u65e5\u672c")) return "Nippon*";
         return s;
     }
 //     public static String encodePais(String s) {
 //         if (s == null) return s;
 //         if (s.charAt(s.length()-1) != '*') return s;
 //         if (s.charAt(0) == 'I') return "\u00cd" + s.substring(1,s.length()-1);
 //         if (s.charAt(0) == 'O') return "\u00d6" + s.substring(1,s.length()-1);
 //         if (s.charAt(0) == 'C') return "C\u030c" + s.substring(1,s.length()-1);
 //         if (s.charAt(0) == 'L') return "L\u00eb" + s.substring(1,s.length()-1);
 //         // Nippon == \u65e5\u672c
 //         if (s.equals("Nippon*")) return "\u65e5\u672c";
 //         return s;
 //     }
 
     /* ********************************************************************************** */
     public static String getCountryName(int idx) {
         if (countries == null) return null;
         return countries.getProperty(Integer.toString(idx));
     }
 
     public static double getCoolnessRanking(int id) {
         if (coolnessRanking == null) return 0d;
         Object obj = coolnessRanking.get(new Integer(id));
         if (obj != null && obj instanceof Double) {
             return ((Double)obj).doubleValue();
         }
         return 0d;
     }
 
     public static int getCountryId(String pais) {
         if (invertCountries == null) return 0;
         int idx = 0;
         try {
             idx = invertCountries.get(pais).intValue();
         } catch (Exception e) { }
         return idx;
     }
     public static int getCountryIdFromLeague(String countryId) {
         if (leagues == null) return 0;
         int c = 0;
         try {
             c = Integer.parseInt(leagues.getProperty(countryId));
         } catch (NumberFormatException nfe) { }
         return c;
     }
 
     public void setCoolnessSum(int type, double sum) {
     	if (type == FlagCollection.T_HOME) {
     		coolnessSumHome.setText("Coolness: " + FlagRenderer.numberFormat.format(sum));
     	} else if (type == FlagCollection.T_AWAY) {
     		coolnessSumAway.setText("Coolness: " + FlagRenderer.numberFormat.format(sum));
     	}
     }
     /* ********************************************************************************** */
     /* ********************************************************************************** */
     /* interface Refreshable */
     public void refresh() {
         if (hoModel == null) return;;
         Vector<ISpieler> allPlayers = null;
         Vector<ISpieler> allOldPlayers = null;
         try {
             allPlayers = hoModel.getAllSpieler();
             allOldPlayers = hoModel.getAllOldSpieler();
         } catch(NullPointerException v_npe) { }
         playersPanel.setPlayers(allPlayers);
         oldPlayersPanel.setPlayers(allOldPlayers);
         oldPlayersPanel.removeRepetidos(playersPanel);
         playersPanel.refreshTitle();
         oldPlayersPanel.refreshTitle();
     }
     /* ********************************************************************************** */
     /* interface IOfficialPlugin */
     public int getPluginID() {  // A unique pluginID.
         return PLUGIN_ID;
     }
     public String getPluginName() { // The original pluginname without version.
         return NAME;
     }
     public File [] getUnquenchableFiles() {  // The files the PluginUpdater is not allowed to delete.
 //         File datadir = new File(Opciones.DATA_DIR);
 //         return datadir.listFiles();
         File [] arrfile = { new File(DATA_DIR + FILENAME_FCAWAY) ,
                             new File(DATA_DIR + FILENAME_FCHOME) ,
                             new File(DATA_DIR + FILENAME_CONFIG) };
         return arrfile;
     }
     public double getVersion() {
         return VERSION;
     }
     /* ********************** Interface ActionListener ******************************** */
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(jmiOptions)) {
             fcAway.saveFlags();
             fcHome.saveFlags();
             JDialog jdOpc = opciones.createDialogoOpciones();
             jdOpc.pack();
             jdOpc.setVisible(true);
         }
         else if (e.getSource().equals(jmiAutoUpdateFlags)) {
             try {
                 if (flagUpdater.updateFlags()) {
                     listAway.repaint();
                     listHome.repaint();
                 }
             } catch (Exception exx) {
                 hoModel.getGUI().getInfoPanel().setLangInfoText("ERROR! " + exx.toString(), Color.RED);
                 Debug.log("Flag: ERROR! " + exx);
                 Debug.logException(exx);
             }
         }
         else if (e.getSource().equals(jmiUpdateCoolness)) {
             if (updateCoolnessRanking()) {
                 if (SORT_LIST_BY_COOLNESS) reSortJLists();
                 hoModel.getGUI().getInfoPanel().setLangInfoText("Coolness Ranking updated OK!");
             }
             else hoModel.getGUI().getInfoPanel().setLangInfoText("Error connecting to " + COOLNESS_URL, Color.RED);
         }
         else if (e.getSource().equals(jmiCredits)) {
             String creditos = NAME + " v" + VERSION + "\nby " + AUTHOR +
             		"\n\nCoolness Ranking page by CHPP-ste1n\n" + COOLNESS_URL;
             JOptionPane.showMessageDialog(hoModel.getGUI().getOwner4Dialog(), creditos, hoModel.getLanguageString("Credits"), JOptionPane.INFORMATION_MESSAGE);
         }
     }
     /* ********************** Interface WindowListener ******************************** */
     public void windowOpened(WindowEvent e) { }
     public void windowClosing(WindowEvent e) {
         fcAway.saveFlags();
         fcHome.saveFlags();
         opciones.save();
     }
     public void windowClosed(WindowEvent e) { }
     public void windowIconified(WindowEvent e) { }
     public void windowDeiconified(WindowEvent e) { }
     public void windowActivated(WindowEvent e) { }
     public void windowDeactivated(WindowEvent e) { }
     /* ********************** Interface AncestorListener ******************************** */
 //     public void ancestorAdded(AncestorEvent e) {
 //         if (hoModel != null) hoModel.getGUI().getInfoPanel().setLangInfoText(NAME + " version " + VERSION + " by " + AUTHOR);
 //     }
 //     public void ancestorRemoved(AncestorEvent e) {
 //         if (hoModel != null) hoModel.getGUI().getInfoPanel().setLangInfoText("");
 //     }
 //     public void ancestorMoved(AncestorEvent event) { }
 
 
 //     public static void main(String args[]) {
 //         System.out.println(NAME + " version " + VERSION + " by " + AUTHOR);
 //         System.out.println("Int. version " + INTERNAL_VERSION);
 //         System.out.println("Ho. version " + HO_VERSION);
 
 
 //         FlagsPlugin ff = new FlagsPlugin();
 
 //         JFrame frame = new JFrame("Flags");
 //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //         //frame.getContentPane().setLayout(new FlowLayout());
 //         //frame.getContentPane().add(ff.createPanel(null));
 //         frame.getContentPane().add(ff.createGUI());
 
 //         frame.pack();
 //         frame.setSize(600,400);
 //         frame.setVisible(true);
 //     }
 
 }//end class FlagsPlugin
 
