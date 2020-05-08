 package bim;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.Preferences;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 /**
  * * Compute the Body Mass Index Copyright (C) 2012 - OTIKA –
  * hanynowsky@gmail.com
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see http://www.gnu.org/licenses/.
  *
  * Bomico - Copyright (C) 2012 <a href="hanynowsky@gmail.com">OTIKA</a>. This
  * program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are
  * welcome to redistribute it under certain conditions.
  *
  * @version 1.0
  * @author Hanynowsky
  */
 public class Utilities implements ActionListener {
 
     static String p = System.getProperty("user.home");
     String separator = File.separator;
     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
     DateFormat monthFormat = new SimpleDateFormat("MM");
     DateFormat yearFormat = new SimpleDateFormat("yyyy");
     DateFormat dayFormat = new SimpleDateFormat("dd");
     Date date = new Date();
     String year = yearFormat.format(date).toString();
     String month = monthFormat.format(date).toString();
     String day = dayFormat.format(date).toString();
     String time = timeFormat.format(date).toString();
     Calendar cal = Calendar.getInstance();
     private String version;
     private String vendor;
     private String title;
     private String build;
     protected static String metallaf = "javax.swing.plaf.metal.MetalLookAndFeel";
     protected static String gtklaf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
     protected static String nimbuslaf = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
     protected static String motiflaf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
     protected static String laf = UIManager.getSystemLookAndFeelClassName();
     protected static String pantherlaf = "com.birosoft.liquid.LiquidLookAndFeel";
     protected static String aluminiumlaf = "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel";
     protected static String smartlaf = "com.jtattoo.plaf.smart.SmartLookAndFeel";
     int i = 0;
     static String localpath;
     static File xmlFile;
     public Map manMap = new HashMap();
     public Map womanMap = new HashMap();
     private String ibmirange = "";
     public Preferences prefs = Preferences.userNodeForPackage(bim.Bim.class);
     final String AGE_PREF = "age_pref";
     final String HEIGHT_PREF = "height_pref";
     final String WEIGHT_PREF = "weight_pref";
     final String GENDER_PREF = "gender_pref";
     final String ACTIVITY_PREF = "activity_pref";
     public final String LAF_PREF = "laf_pref";
     final String MORPH_PREF = "morph_pref";
     final String FORMULA_PREF = "formula_pref";
     final String ORIGIN_PREF = "origin_pref";
     final String WAIST_PREF = "waist_pref";
     final String WRIST_PREF = "wrist_pref";
     final String HIP_PREF = "tip_pref";
     final String ELBOW_PREF = "elbow_pref";
     final String NECK_PREF = "neck_pref";
     final String WRISTELBOW_PREF = "wristelbow_pref";
     final String GUIDE_PREF = "guide_pref";
     final String SOUND_PREF = "sound_pref";
     final public String LOCALE_PREF = "locale_pref";
     AudioClip sound;
    static String executable = System.getProperty("java.class.path");
 
     /**
      * Creates a new instance of this object class
      */
     public Utilities() {
     }
 
     /**
      * Sets the preference value from a component in GUI
      *
      * @param PREF Preference variable
      * @param value Value of Preference
      */
     public void setPreferences(String PREF, Object value) {
         prefs.put(PREF, value.toString());
     }
 
     public String getIbmirange() {
         return ibmirange;
     }
 
     public void setIbmirange(String ibmirange) {
         this.ibmirange = ibmirange;
     }
 
     public Map getManMap() {
         return manMap;
     }
 
     public void setManMap(Map manMap) {
         this.manMap = manMap;
     }
 
     public Map getWomanMap() {
         return womanMap;
     }
 
     public void setWomanMap(Map womanMap) {
         this.womanMap = womanMap;
     }
 
     public static String getLocalpath() {
         localpath = p + File.separator + "bomico"
                 + File.separator + "config" + File.separator + "followup.xml";
         return localpath;
     }
 
     public static File getXmlFile() {
         xmlFile = new File(getLocalpath());
         return xmlFile;
     }
 
     public static String getMotiflaf() {
         return motiflaf;
     }
 
     public static void setMotiflaf(String motiflaf) {
         Utilities.motiflaf = motiflaf;
     }
 
     public static String getNimbuslaf() {
         return nimbuslaf;
     }
 
     public static void setNimbuslaf(String nimbuslaf) {
         Utilities.nimbuslaf = nimbuslaf;
     }
 
     public static String getLaf() {
 
         return laf;
     }
 
     public static void setLaf(String laf) {
         Utilities.laf = laf;
     }
 
     public String getVendor() {
         // vendor = Bim.class.getPackage().getImplementationVendor();
         vendor = "Otika";
         return vendor;
     }
 
     public String getBuild() {
         build = "20";
         return build;
     }
 
     public String getTitle() {
         title = "Bomico";
         return title;
     }
 
     public String getVersion() {
         // version = Bim.class.getPackage().getImplementationVersion();
         version = "1.0";
         return version;
     }
 
     public String getDefaultLaf() {
         return laf;
     }
 
     public String getGtklaf() {
         return gtklaf;
     }
 
     public void setGtklaf(String gtklaf) {
         Utilities.gtklaf = gtklaf;
     }
 
     public String getMetallaf() {
         return metallaf;
     }
 
     public void setMetallaf(String metallaf) {
         Utilities.metallaf = metallaf;
     }
 
     /**
      * An HTML String for the About Dialog.
      *
      * @return Application and System information as String
      */
     public String getAppInfos() {
 
         String infos = "<html><b>Version</b>: " + getVersion() + "<html><br></br>" + "<html><b>Application:</b> "
                 + getTitle() + "<html><br></br>" + "<html><b>Vendor:</b> " + getVendor() + "<html><br></br>"
                 + "<html><b>Build:</b> " + getBuild() + "<html><br></br>" + "<html><b>User Home:</b> " + p + "<html><br></br>"
                 + "<html><b>User Directory:</b> " + System.getProperty("user.dir")
                 + "<html><br></br>" + "<html><b>System:</b> " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                 + " " + System.getProperty("os.arch") + "<html><br></br>" + "<html><b>Java:</b> " + System.getProperty("java.version")
                 + " " + System.getProperty("java.vendor");
         return infos;
     }
 
     /**
      * Creates Follow-up XML file.
      */
     public void createFile() {
         try {
             if (!xmlFile.exists()) {
                 new File(p + separator + "bomico" + separator + "config").mkdirs();
                 xmlFile.createNewFile();
                 FileWriter fstream = new FileWriter(xmlFile, true);
                 try (BufferedWriter out = new BufferedWriter(fstream)) {
                     String xmlString = "<?xml version=\"1.0\" "
                             + "encoding=\"UTF-8\" standalone=\"no\"?>"
                             + "<followup></followup>";
                     out.write(xmlString);
                 }
                 System.out.println("- Followup.xml File Created in " + localpath);
             }
         } catch (IOException ex) {
             Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Appends user information related to BMI into Follow-up XML file.
      *
      * @param bmival BMI value
      */
     public void appendInXML(String bmival) {
 
         System.out.println(dateFormat.format(cal.getTime()));
         try {
             /*
              * InputStream input =
              * Utilities.class.getResourceAsStream("/config/followup.xml");
              * ClassLoader classLoader =
              * Thread.currentThread().getContextClassLoader(); URL aurl =
              * classLoader.getResource("/config/followup.xml"); URL url =
              * getClass().getResource("/config/followup.xml"); File xFile = new
              * File(url.toURI()); System.out.println("---");
              * System.out.println(xFile.getPath());
              * System.out.println(xFile.getAbsolutePath());
              * System.out.println(xFile.getCanonicalPath());
              * System.out.println("---");
              */
 
             createFile(); // Check if the file exists, if not create it.
 
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
             Document document = documentBuilder.parse(xmlFile); // xmlFile
             //Get the root element of the xml Document;
             Element documentElement = document.getDocumentElement();
             System.out.println("documentElement:" + documentElement.toString());
             Element userNode = document.createElement("user");
             Element valueNode = document.createElement("value");
             Element yearNode = document.createElement("year");
             Element monthNode = document.createElement("month");
             Element dayNode = document.createElement("day");
             Element timeNode = document.createElement("time");
             //Text value = document.createTextNode("2");
             userNode.setTextContent(System.getProperty("user.name"));
             valueNode.setTextContent(bmival);
             yearNode.setTextContent(year);
             monthNode.setTextContent(month);
             dayNode.setTextContent(day);
             timeNode.setTextContent(time);
             //Create a Node element
             Element nodeElement = document.createElement("bmi");
 
             try {
 
                 int idv = document.getLastChild().getChildNodes().getLength();
                 String iv;
                 if (document.getLastChild() != null) {
 
                     if (idv == 0) {
                         i = 1;
                     } else {
                         i = idv + 1; // i = (idv + 1) / 2;
                     }
                     iv = String.valueOf(i);
 
                     nodeElement.setAttribute("id", iv);
                     System.out.println("- XML Document Child nodes length is: " + idv);
                     System.out.println("- Added Element is " + iv);
                     System.out.println("- Last Child is: " + document.getLastChild().getLocalName());
                     System.out.println("- Doc URI: " + document.getDocumentURI());
                     System.out.println("- Doc Element: " + document.getDocumentElement());
                 } else {
                     System.err.println("- Something is wrong with XML parsing/Appending : " + idv);
                 }
             } catch (NumberFormatException | DOMException e) {
                 System.err.println(e);
             }
 
             //append textNode to Node element;
             nodeElement.appendChild(userNode);
             nodeElement.appendChild(valueNode);
             nodeElement.appendChild(yearNode);
             nodeElement.appendChild(monthNode);
             nodeElement.appendChild(dayNode);
             nodeElement.appendChild(timeNode);
             //append Node to rootNode element
             documentElement.appendChild(nodeElement);
             document.replaceChild(documentElement, documentElement);
             Transformer tFormer = TransformerFactory.newInstance().newTransformer();
             //  Set output file to xml
             tFormer.setOutputProperty(OutputKeys.METHOD, "xml");
             // Write the document back to the file
             Source source = new DOMSource(document);
             Result result = new StreamResult(xmlFile); // xmlFile
             tFormer.transform(source, result);
 
         } catch (TransformerException | SAXException | IOException | ParserConfigurationException ex) {
             Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("- Success: Info appended in XML");
     }
 
     /**
      * Reads Follow-up file XML data. Make sure the XML sub-root elements have
      * not changed. If so, modify this method accordingly.
      *
      * @param v
      * @param y
      * @param m
      * @param d
      * @param t
      */
     public void readXML(String v, String y, String m, String d, String t) {
 
         try {
             File xFile = new File(p + separator + "bomico"
                     + separator + "config" + separator + "followup.xml");
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(xFile);
             doc.getDocumentElement().normalize();
 
             System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
             NodeList nList = doc.getElementsByTagName("bmi");
             System.out.println("Getting Sub Root -----------------------" + nList.item(0));
 
 
             for (i = 0; i < nList.getLength(); i++) {
                 Node nNode = nList.item(i);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) nNode;
 
                     NodeList userList = element.getElementsByTagName("user");
                     Element userElement = (Element) userList.item(0);
                     NodeList textuserList = userElement.getChildNodes();
                     System.out.println(v + ((Node) textuserList.item(0)).getNodeValue().trim());
 
                     NodeList valueList = element.getElementsByTagName("value");
                     Element valueElement = (Element) valueList.item(0);
                     NodeList textvalueList = valueElement.getChildNodes();
                     System.out.println(v + ((Node) textvalueList.item(0)).getNodeValue().trim());
 
                     NodeList yearList = element.getElementsByTagName("year");
                     Element yearElement = (Element) yearList.item(0);
                     NodeList textyearList = yearElement.getChildNodes();
                     System.out.println(y + ((Node) textyearList.item(0)).getNodeValue().trim());
 
                     NodeList monthList = element.getElementsByTagName("month");
                     Element monthElement = (Element) monthList.item(0);
                     NodeList textmonthList = monthElement.getChildNodes();
                     System.out.println(m + ((Node) textmonthList.item(0)).getNodeValue().trim());
 
                     NodeList dayList = element.getElementsByTagName("day");
                     Element dayElement = (Element) dayList.item(0);
                     NodeList textdayList = dayElement.getChildNodes();
                     System.out.println(d + ((Node) textdayList.item(0)).getNodeValue().trim());
 
                     NodeList timeList = element.getElementsByTagName("time");
                     Element timeElement = (Element) timeList.item(0);
                     NodeList texttimeList = timeElement.getChildNodes();
                     System.out.println(t + ((Node) texttimeList.item(0)).getNodeValue().trim());
 
                     System.out.println("\n------------------\n");
 
                     /**
                      * System.out.println("First Name : " +
                      * getTagValue("firstname", element));
                      * System.out.println("Last Name : " +
                      * getTagValue("lastname", element));
                      * System.out.println("Nick Name : " +
                      * getTagValue("nickname", element));
                      * System.out.println("Salary : " + getTagValue("salary",
                      * element));
                      */
                 }
             }
         } catch (SAXException | IOException | ParserConfigurationException ex) {
             Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
     /**
      * Copy Splash Picture File in local folder.
      */
     public void pasteSplashFile() {
         InputStream is = getClass().getResourceAsStream("/resource/Bomico.png");
         File splash = new File(p + separator + "bomico" + separator + "splash" + separator + "Bomico.png");
         if (!splash.exists()) {
             try {
                 new File(p + separator + "bomico" + separator + "splash").mkdirs();
                 splash.createNewFile();
                 try (OutputStream output = new FileOutputStream(splash)) {
                     int read = 0; // Assigned value is never used?
                     byte[] bytes = new byte[1024];
                     while ((read = is.read(bytes)) != -1) {
                         output.write(bytes, 0, read);
                     }
                     is.close();
                     output.flush();
                 }
                 System.out.println("New Splash file created!");
             } catch (IOException ex) {
                 Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * Pastes the <em>Synth</em> LAF XML file into local folder. ($USERHOME /
      * bomico).
      */
     public void pasteSynthFile() {
         InputStream is = getClass().getResourceAsStream("/config/synthlaf.xml");
         File synth = new File(p + separator + "bomico" + separator + "config" + separator + "synthlaf.xml");
         if (!synth.exists()) {
             try {
                 new File(p + separator + "bomico" + separator + "config").mkdirs();
                 synth.createNewFile();
                 try (OutputStream output = new FileOutputStream(synth)) {
                     int read;
                     byte[] bytes = new byte[1024];
                     while ((read = is.read(bytes)) != -1) {
                         output.write(bytes, 0, read);
                     }
                     is.close();
                     output.flush();
                 }
                 System.out.println("New Synth file created!");
             } catch (IOException ex) {
                 Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * Parses the BMI correspondence table (CSV File) and sets the BMI Range.
      *
      * @param gender Gender (String: Male or Female)
      * @param height Height as double (in centimeters. ex: 175)
      */
     public void readIBMITABLE(int gender, double height) {
         BufferedReader bufRdr;
         try {
 
             File f = new File(System.getProperty("user.home"), separator + "bomico" + separator + "config" + separator + "ibmitable.csv");
             if (!f.exists()) {
                 InputStream inputStream = getClass().getResourceAsStream("/config/ibmitable.csv");
                 OutputStream out = new FileOutputStream(f);
                 byte buf[] = new byte[1024];
                 int len;
                 while ((len = inputStream.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
                 System.out.println("\nibmitable.csv File is created..............");
             }
             // File file = new File(System.getProperty("user.home"), "/Desktop/ibmitable.csv");
             bufRdr = new BufferedReader(new FileReader(f)); //
             String line;
             int row = 0;
             //  StringTokenizer drove me nuts so I kicked it off
             while ((line = bufRdr.readLine()) != null) {
                 String[] content = line.split(";", 3); // Three columns
                 // Height | Man  | Woman
                 manMap.put(content[0], content[1]);
                 womanMap.put(content[0], content[2]);
                 row++;
             }
             bufRdr.close();
 
             //  System.out.println(manMap.entrySet());
             //    System.out.println(womanMap.entrySet());
             // ibmirange = "";
 
             int g = gender;
 
             double j = 0;
             double e = 0;
             double s;
             double arg = height;
             double diff;
             double dim;
             double ah = 0;
             Iterator kset = manMap.keySet().iterator();
             for (int k = 0; k < manMap.keySet().size(); k++) {
                 while (kset.hasNext()) {
                     s = Double.parseDouble(kset.next().toString());
                     diff = Math.max(s, arg);
                     dim = Math.min(s, arg);
                     // 0.013 is the maximum difference between two ordered height values
                     //  System.out.println(" here is FIRST  diff: " + diff);
                     if (diff - arg <= 0.013 && diff != arg) {
                         j = diff;
                         //  System.out.println("here is diff: " + diff);
                     }
 
                     if (arg - dim <= 0.013 && dim != arg) {
                         e = dim;
                         //  System.out.println("here is dim: " + dim);
                     }
                 }
             }
 
             //   System.out.println(manMap.keySet());
 
             // System.err.println(j + " " + (j - arg));
             // System.err.println(e + " " + (arg - e));
             if ((arg - e) < (j - arg)) {
                 ah = e;
             }
             if ((arg - e) > (j - arg)) {
                 ah = j;
             }
 
             //    System.err.println("And here is final ah: " + ah);
 
             height = ah;
             String h = String.valueOf(height);
             if (g == 0) { // 0 for male and 1 for female
                 setIbmirange(String.valueOf(manMap.get(h)));
                 // System.err.println(String.valueOf(manMap.get(h)));
             }
             if (g == 1) {
                 setIbmirange(String.valueOf(womanMap.get(h)));
             }
 
             //    System.err.println(String.valueOf(manMap.get(height)));
         } catch (IOException ex) {
             Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
             System.err.println("iBMI Ranges CSV File is not present or corrupted. "
                     + "Please delete it manually and relaunch the application.");
         }
     }
 
     /**
      * Exact center or 25% upper left.
      *
      * @param c Component in GUI (ex: JDialog)
      * @param absolute decides if Component position on screen is absolute or
      * centered
      */
     public void centerOnScreen(final Component c, final boolean absolute) {
         final int width = c.getWidth();
         final int height = c.getHeight();
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         int x = (screenSize.width / 2) - (width / 2);
         int y = (screenSize.height / 2) - (height / 2);
         if (!absolute) {
             x /= 2;
             y /= 2;
         }
         c.setLocation(x, y);
     }
 
     /**
      * Center on parent (absolute true/false (exact center or 25% upper left)).
      *
      * @param child Dialog or Frame (Window)
      * @param absolute
      */
     public void centerOnParent(final Window child, final boolean absolute) {
         child.pack();
         boolean useChildsOwner = child.getOwner() != null ? ((child.getOwner() instanceof JFrame) || (child.getOwner() instanceof JDialog)) : false;
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         final Dimension parentSize = useChildsOwner ? child.getOwner().getSize() : screenSize;
         final Point parentLocationOnScreen = useChildsOwner ? child.getOwner().getLocationOnScreen() : new Point(0, 0);
         final Dimension childSize = child.getSize();
         childSize.width = Math.min(childSize.width, screenSize.width);
         childSize.height = Math.min(childSize.height, screenSize.height);
         child.setSize(childSize);
         int x;
         int y;
         if ((child.getOwner() != null) && child.getOwner().isShowing()) {
             x = (parentSize.width - childSize.width) / 2;
             y = (parentSize.height - childSize.height) / 2;
             x += parentLocationOnScreen.x;
             y += parentLocationOnScreen.y;
         } else {
             x = (screenSize.width - childSize.width) / 2;
             y = (screenSize.height - childSize.height) / 2;
         }
         if (!absolute) {
             x /= 2;
             y /= 2;
         }
         child.setLocation(x, y);
     }
 
     /**
      * Converts a centimeter value to feet & inches in one string.
      *
      * @param height Height in centimeters
      * @return Measure in Feet & Inches.
      */
     public static String convertHeight(double height) {
         double feet = height * 0.39370079 * 0.08333333;
         int f = (int) feet;
         double r = (feet - f) * 12;
         String fi = new DecimalFormat("#").format(r);
         return f + " ft " + fi + " in";
     }
 
     /**
      * Converts kilograms to pounds.
      *
      * @param weight Weight in kilograms
      * @return Formatted weight in pounds.
      */
     public static double convertWeight(double weight) {
         double w = weight * 2.2;
         return Math.round(w);
         //return Double.parseDouble(new DecimalFormat("#.##").format(w));
     }
 
     /**
      * Convert centimeters to inches.
      *
      * @param cm Value in Centimeters.
      * @return Measure in inches
      */
     public static double convertCMtoINCH(double cm) {
         return cm * 0.393700787;
     }
 
     /**
      * Plays audio files in AU format, that correspond to BMI status. <br/> For
      * some reason, the play method of Applet class does not play the whole
      * audio file while the loop method does, that is why meanwhile as a
      * workaround we use the loop method instead, along with a timer that calls
      * the audio stop method after a short delay of time.
      *
      * @param status BMI Status
      * @param soundEnabled Enable Sound playing or not
      */
     public void playAudio(String status, boolean soundEnabled) {
         status = Computer.getSoundStatus();
         if (soundEnabled) {
             String audioname = "/resource/" + status.replace(" ", "").trim().toLowerCase() + ".au";
             // System.err.println("Audio file name is: " + audioname);
             URL soundurl = getClass().getResource(audioname);
             if (!status.equals("") & status != null) {
 
                 if (status.toLowerCase().contains("class") || status.toLowerCase().contains("thinness")) {
                     // System.err.println("Audio file name is: " + audioname);
                     sound = Applet.newAudioClip(soundurl);
                     sound.loop();
                     javax.swing.Timer timer = new javax.swing.Timer(1700, this);
                     timer.setInitialDelay(1700);
                     timer.setRepeats(false);
                     timer.start();
                 } else {
                     sound = Applet.newAudioClip(soundurl);
                     sound.play();
                 }
             }
         } else {
         }
     } // end of method
 
     @Override
     public void actionPerformed(ActionEvent e) {
         sound.stop();
         // System.out.println("Inside Action performed UTILITIES");
     }
 
     /**
      * Used to set text field foreground color.
      *
      * @param weight Weight in Kilograms
      * @param iweight Ideal Weight in Kilograms
      * @return A Color (Green if weight equals ideal weight, else blue).
      */
     public static Color setColor(double weight, double iweight) {
         if (weight == (int) Math.round(iweight)) {
             Color greencolor = new Color(0, 153, 51);
             return greencolor;
         } else {
             return Color.BLUE;
         }
     }
 
     public static String showSystemPath() {
         String path = "";
         try {
             String path1 = Bim.class.getProtectionDomain().getCodeSource().getLocation().getPath();
             path = URLDecoder.decode(path1, "UTF-8");
 
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
         }
         return path;
     }
 
     /**
      * Handles Application Restart. Restarts the application. Works only when
     * the application is executed as jar or 'exe'.
      */
     public static void restartApplication() {

         ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(2);
         Callable<Process> callable = new Callable<Process>() {
 
             @Override
            public Process call() throws Exception {
                 String command = null;
                 if (executable.endsWith("jar")) {
                     command = "java -jar ";
                 } else if (executable.endsWith("exe")) {
                    if (executable.contains(".exe")) {
                        executable = executable.substring(0, executable.length() - 4);
                    }
                     command = "";
                 } else if (executable.endsWith("sh")) {
                     command = "sh ";
                 } else if (executable.endsWith("bin")) {
                     command.concat("");
                 }
                 Process p = Runtime.getRuntime().exec(command + executable);
                System.out.println("Trying to execute " + command + executable);
                 return p;
             }
         };
         FutureTask<Process> futureTask = new FutureTask<>(callable);
         schedulerExecutor.submit(futureTask);
         System.out.println("Restarting Application.");
         System.exit(0);
     }
 
     /**
      * Sets the default locale by its String representation <i>(e.g.
      * "fr_FR").</i>
      *
      * @param locale String representation of a given Locale
      */
     public static void changeLocale(String locale) {
         Locale loc = new Locale(locale.substring(0, 2) + "", locale.substring(3).toUpperCase());
         ResourceBundle.clearCache();
         Locale.setDefault(loc);
         ResourceBundle.clearCache();
         // Test: Use these for VM run : -Duser.language=fr -Duser.country=FR
     }
 
     /**
      * Retrieves the value of Text Anti-aliasing used by the desktop. (ex:
      * {Text-specific anti-aliasing enable key=LCD HRGB anti-aliasing text mode}
      * as used in most Linux desktops)
      *
      * @return Desktop Hint Information
      */
     public static Object getDesktopHint() {
         Object hintInfo = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
         return hintInfo;
     }
 } // END OF CLASS
 
