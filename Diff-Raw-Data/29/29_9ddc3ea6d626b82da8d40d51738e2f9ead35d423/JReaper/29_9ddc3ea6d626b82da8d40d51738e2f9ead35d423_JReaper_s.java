 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pulsarhunter.bookkeepr.jreaper;
 
 import bookkeepr.DummyIdAble;
 import bookkeepr.xml.IdAble;
 import bookkeepr.xml.StringConvertable;
 import bookkeepr.xml.XMLAble;
 import bookkeepr.xml.XMLReader;
 import bookkeepr.xml.XMLWriter;
 import bookkeepr.xmlable.CandidateList;
 import bookkeepr.xmlable.CandidateListStub;
 import bookkeepr.xmlable.ClassifiedCandidate;
 import bookkeepr.xmlable.RawCandidate;
 import bookkeepr.xmlable.RawCandidateBasic;
 import bookkeepr.xmlable.ViewedCandidates;
 import bookkeepr.xmlable.ViewedCandidatesIndex;
 import coordlib.Coordinate;
 import coordlib.CoordinateDistanceComparitor;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import bookkeepr.xml.display.GenerateRawCandidatePlot;
 import bookkeepr.xml.display.JPanelCandidatePlot;
 import bookkeepr.xml.display.RasterImageCandidatePlot;
 import bookkeepr.xmlable.JReaperSettings;
 import bookkeepr.xmlable.Psrxml;
 import bookkeepr.xmlable.RawCandidateMatched;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.util.concurrent.ArrayBlockingQueue;
 import javax.sql.rowset.spi.XmlWriter;
 import org.xml.sax.SAXException;
 import pulsarhunter.jreaper.CandClass;
 import pulsarhunter.jreaper.HarmonicType;
 import pulsarhunter.jreaper.Colourmap;
 
 /**
  *
  * @author kei041
  */
 public class JReaper {
 
     public static final String WINDOWTITLE = "JReaper 5: Main Plot";
     private BookKeeprConnection connection;
     private String user = "noone";
     private JFrame currentWindow = null;
     private HashMap<String, ViewedCandidates> viewedCands;
     private ArrayList<ClassifiedCandidate> classifiedCands;
     private ViewedCandidates myViewedCandidates;
     private RawCandidateBasic[] candArray;
     private HashMap<Long, ArrayList<ClassifiedCandidate>> candClasses;
     private HashMap<Long, ArrayList<ClassifiedCandidate>> candPossClasses;
     private HashMap<Long, Psrxml> clistIdToPsrxmlHeaders;
     private HashMap<Long, CandidateListStub> clistIdToCandListHeaders;
     private ArrayBlockingQueue<RawCandidateBasic> recentViewed;
 
     public HashMap<Long, CandidateListStub> getClistIdToCandListHeaders() {
         return clistIdToCandListHeaders;
     }
 
     public HashMap<Long, Psrxml> getClistIdToPsrxmlHeaders() {
         return clistIdToPsrxmlHeaders;
     }
 
     public void start() {
         closeWindow();
 
         connection = new BookKeeprConnection();
         File setfile = new File(System.getProperty("user.home") + File.separator + ".jreaper_con");
         if (setfile.canRead()) {
             XMLAble xmlable;
             try {
                 xmlable = XMLReader.read(new FileInputStream(setfile));
                 if (xmlable instanceof JReaperSettings) {
                     JReaperSettings set = (JReaperSettings) xmlable;
                     try {
                         connection.setUrl(set.getDefaultHost());
                     } catch (URISyntaxException ex) {
                         Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (BookKeeprCommunicationException ex) {
                         Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     this.user = set.getDefaultUser();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SAXException ex) {
                 Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         }
         currentWindow = new JReaperSetupFrame(this, connection);
         currentWindow.setVisible(true);
     }
 
     public void restart() {
         closeWindow();
         this.candArray = null;
         System.gc();
         currentWindow = new JReaperSetupFrame(this, connection);
         currentWindow.setVisible(true);
     }
 
     public void goToPlot(ArrayList<CandidateListStub> clistStubs) {
         this.clistIdToPsrxmlHeaders = new HashMap<Long, Psrxml>();
         this.clistIdToCandListHeaders = new HashMap<Long, CandidateListStub>();
         recentViewed = new ArrayBlockingQueue<RawCandidateBasic>(100);
         JReaperSetupFrame gui = null;
 
         if (this.currentWindow instanceof JReaperSetupFrame) {
             gui = (JReaperSetupFrame) this.currentWindow;
         } else {
             closeWindow();
             gui = new JReaperSetupFrame(this, connection);
             this.currentWindow = gui;
         }
         gui.setVisible(false);
         gui.showLoadingPane();
         gui.setLoadingPane(clistStubs.size(), 0, 0, 0);
 
         gui.setVisible(true);
 
         ArrayList<CandidateList> candlists = new ArrayList<CandidateList>();
         ArrayList<RawCandidateBasic> cands = new ArrayList<RawCandidateBasic>();
         int nclist = 0;
         int nerr = 0;
         int ncands = 0;
 
         for (CandidateListStub stub : clistStubs) {
             this.clistIdToCandListHeaders.put(stub.getId(), stub);
             CandidateList clist = null;
             try {
                 clist = connection.getCandidateList(stub.getId());
             } catch (BookKeeprCommunicationException ex) {
                 Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
                 nerr++;
                 continue;
             }
             for (RawCandidateBasic basic : clist.getRawCandidateBasicList()) {
                 basic.setCandlistCoordinate(clist.getCoordinate());
             }
             candlists.add(clist);
             cands.addAll(clist.getRawCandidateBasicList());
             nclist++;
             ncands += clist.getNcands();
             gui.setLoadingPane(clistStubs.size(), nclist, nerr, ncands);
 
         }
         Collections.sort(cands, IdAble.COMPARATOR);
         this.myViewedCandidates = new ViewedCandidates();
         reSyncViewedWithDatabase();
         reSyncClassifiedWithDatabase();
 
         candArray = cands.toArray(new RawCandidateBasic[0]);
         markViewedCands(user);
         markClassifiedCands();

        gui.setLoadingPaneDone();
     }
 
     public void goToPlotDone() {
         // Make the main view appear!
         closeWindow();
 
         currentWindow = new MainView(candArray, this);
         currentWindow.setVisible(true);
     }
 
     public List<String> getUsers() {
         return new ArrayList<String>(this.viewedCands.keySet());
     }
 
     public void setViewed(RawCandidateBasic cand) {
         cand.setPlotStatus(CandStatus.setViewed(true, cand.getPlotStatus()));
         this.myViewedCandidates.addViewed(cand.getId());
     }
 
     public void clearViewedCands() {
         if (this.candArray != null) {
             for (RawCandidateBasic c : this.candArray) {
                 c.setPlotStatus(CandStatus.setViewed(false, c.getPlotStatus()));
             }
         }
     }
 
     public void markViewedCands(String as) {
         markViewedCands(as, true);
     }
 
     public void markViewedCands(String as, boolean clear) {
         if (clear) {
             clearViewedCands();
         }
         if (as == null) {
             markViewedCands(user, false);
             for (String s : this.viewedCands.keySet()) {
                 if (s == null) {
                     continue;
                 }
                 if (!s.equals(user)) {
                     markViewedCands(s, false);
                 }
             }
         } else {
             ViewedCandidates vc = this.viewedCands.get(as);
             ArrayList<Long> values = null;
             if (as.equals(user)) {
                 values = myViewedCandidates.getViewed();
                 if (vc != null) {
                     values.addAll(vc.getViewed());
                 }
             } else {
                 if (vc != null) {
                     values = vc.getViewed();
                 }
             }
             if (values != null) {
                 Collections.sort(values);
                 for (RawCandidateBasic cand : candArray) {
                     if (Collections.binarySearch(values, cand.getId()) >= 0) {
                         cand.setPlotStatus(CandStatus.setViewed(true, cand.getPlotStatus()));
                     }
 
                 }
             }
         }
     }
 
     public void clearClassifiedCands() {
         if (this.candArray != null) {
             this.candClasses = new HashMap<Long, ArrayList<ClassifiedCandidate>>();
             this.candPossClasses = new HashMap<Long, ArrayList<ClassifiedCandidate>>();
 
             for (RawCandidateBasic c : this.candArray) {
                 c.setPlotStatus(CandStatus.clearCands(c.getPlotStatus()));
             }
         }
     }
 
     public void markClassifiedCands() {
         clearClassifiedCands();
         for (ClassifiedCandidate c : classifiedCands) {
             for (HarmonicType htype : HarmonicType.values()) {
                 for (long l : c.getMatched(htype, true)) {
                     int posn = Arrays.binarySearch(candArray, new DummyIdAble(l), IdAble.COMPARATOR);
                     if (posn >= 0) {
                         int status = candArray[posn].getPlotStatus();
                         if (CandStatus.getHarmonicType(status).getRank() < htype.getRank()) {
                             status = CandStatus.setCandClass(c.getCandClass(), status);
                             status = CandStatus.setHarmonicType(htype, status);
                             status = CandStatus.setPossible(false, status);
                             candArray[posn].setPlotStatus(status);
                         }
 
                         ArrayList<ClassifiedCandidate> list = this.candClasses.get(l);
                         if (list == null) {
                             list = new ArrayList<ClassifiedCandidate>();
                             this.candClasses.put(l, list);
                         }
                         list.add(c);
 
                     }
                 }
                 for (long l : c.getMatched(htype, false)) {
                     int posn = Arrays.binarySearch(candArray, new DummyIdAble(l), IdAble.COMPARATOR);
                     if (posn >= 0) {
                         int status = candArray[posn].getPlotStatus();
                         if (CandStatus.getHarmonicType(status) == HarmonicType.None) {
                             status = CandStatus.setCandClass(c.getCandClass(), status);
                             status = CandStatus.setHarmonicType(htype, status);
                             status = CandStatus.setPossible(true, status);
                             candArray[posn].setPlotStatus(status);
 
                         }
                         ArrayList<ClassifiedCandidate> list = this.candPossClasses.get(l);
                         if (list == null) {
                             list = new ArrayList<ClassifiedCandidate>();
                             this.candPossClasses.put(l, list);
                         }
                         list.add(c);
                     }
                 }
             }
         }
     }
 
     public void connected() {
         File setfile = new File(System.getProperty("user.home") + File.separator + ".jreaper_con");
         JReaperSettings set = new JReaperSettings();
         set.setDefaultHost(connection.getRemoteHost().getUrl());
         set.setDefaultUser(user);
         try {
             XMLWriter.write(new FileOutputStream(setfile), set);
         } catch (IOException ex) {
             Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public String getServerStatus() {
         try {
             this.connection.contact();
         } catch (BookKeeprCommunicationException ex) {
             Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, "Error talking to server", ex);
         }
         return this.connection.getStatus();
     }
 
     public List<ClassifiedCandidate> getClassifiedCandidatesMatching(long candId) {
         List<ClassifiedCandidate> l = this.candClasses.get(candId);
         if (l == null) {
             return null;
         } else {
             return Collections.unmodifiableList(l);
         }
     }
 
     public List<ClassifiedCandidate> getClassifiedCandidatesPossMatching(long candId) {
         List<ClassifiedCandidate> l = this.candPossClasses.get(candId);
         if (l == null) {
             return null;
         } else {
             return Collections.unmodifiableList(l);
         }
     }
 
     public void classifyToExistingCandidate(ClassifiedCandidate cand, RawCandidateBasic basic, HarmonicType htype) throws BookKeeprCommunicationException {
         RawCandidateMatched m = new RawCandidateMatched(htype, basic);
         m.setConfirmed(true);
         connection.postToClassifiedCandidate(cand, m);
     }
 
     public void classifyNewCandidate(RawCandidateBasic cand, String name, CandClass candClass) throws BookKeeprCommunicationException {
         ClassifiedCandidate cc = new ClassifiedCandidate();
         cc.setCandClass(candClass);
         cc.setName(name);
         cc.setOriginalRawCandId(cand.getId());
         cc.setPreferedCandidate(new RawCandidateMatched(HarmonicType.Principal, cand));
 
         cc.setCoordinate(cand.getCandlistCoordinate());
         connection.postClassifiedCandidate(cc);
     }
 
     public static void main(String[] args) {
         JReaper jr = new JReaper();
         jr.start();
 
     }
 
     void clickArea(final double xposn1, final double xposn2, final double yposn1, final double yposn2, final boolean clickSelected) {
         Thread task = new Thread() {
 
             public void run() {
                 if (JReaper.this.currentWindow instanceof MainView) {
                     final MainView mv = (MainView) JReaper.this.currentWindow;
                     final LoadingSplash lsplash = new LoadingSplash();
                     java.awt.EventQueue.invokeLater(new Runnable() {
 
                         public void run() {
                             lsplash.setText("Selecting Candidates... Please wait");
                             lsplash.setVisible(true);
                         }
                     });
 
                     for (RawCandidateBasic c : JReaper.this.candArray) {
                         double xval = mv.getPType().getXval(c);
                         double yval = mv.getPType().getYval(c);
                         if ((xval < xposn1 && xval > xposn2) && (yval > yposn1 && yval < yposn2)) {
                             if (CandStatus.isViewed(c.getPlotStatus())) {
                                 continue;
 //                            if (c.getCandClass() >= 0) {
 //                                continue;
 //                            }
 //
 //                            if (!viewed && c.beenViewed()) {
 //                                continue;
 //                            }
                             }
                             try {
 
                                 mv.clickOnHold(c);
                             } catch (IOException ex) {
                                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                                     public void run() {
 
                                         lsplash.setVisible(false);
                                         lsplash.dispose();
                                     }
                                 });
                                 return;
                             }
 
 
                         }
                     }
 
                     java.awt.EventQueue.invokeLater(new Runnable() {
 
                         public void run() {
                             mv.replot();
                             lsplash.setVisible(false);
                             lsplash.dispose();
                         }
                     });
 
 
                 }
             }
         };
         task.start();
     }
 
     JReaperCandidateFrame getWindow(RawCandidateBasic basic) {
         RawCandidate cand = null;
 
         if (basic != null) {
 
             long clistId = basic.getCandidateListId();
             Psrxml header = this.clistIdToPsrxmlHeaders.get(clistId);
 
             try {
                 cand = connection.getRawCandidate(basic.getId());
                 if (header == null) {
                     CandidateList clist = this.connection.getCandidateList(clistId);
                     header = this.connection.getPsrxmlForCandidateList(clist);
                     this.clistIdToPsrxmlHeaders.put(clistId, header);
                 }
             } catch (BookKeeprCommunicationException ex) {
                 Logger.getLogger(JReaper.class.getName()).log(Level.WARNING, "Could not get raw candidate from database", ex);
                 return null;
             }
 
 
 
 
             JReaperCandidateFrame window = new JReaperCandidateFrame(basic, header, this, this.connection.getRemoteHost().getUrl() + "/cand/" + StringConvertable.ID.toString(basic.getId()));
             JPanelCandidatePlot plot = new JPanelCandidatePlot();
             GenerateRawCandidatePlot.generate(plot, cand, Colourmap.defaultGreyColmap, Color.RED, Color.BLUE);
             window.addDisplayPanel(plot);
             return window;
         }
         return null;
     }
 
     void addToViewedHistory(RawCandidateBasic basic) {
         this.recentViewed.remove(basic);
         while (!this.recentViewed.offer(basic)) {
             this.recentViewed.poll();
         }
     }
 
     RawCandidateBasic[] getViewedHistory() {
         return this.recentViewed.toArray(new RawCandidateBasic[0]);
     }
 
     void clickOn(RawCandidateBasic basic) {
         if (currentWindow instanceof MainView) {
 
             if (basic != null) {
 
                 JReaperCandidateFrame window = this.getWindow(basic);
                 this.addToViewedHistory(basic);
 
 //                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
 //                RasterImageCandidatePlot imgPlot = new RasterImageCandidatePlot(img);
 //                GenerateRawCandidatePlot.generate(imgPlot, cand, Colourmap.defaultGreyColmap, Color.RED, Color.BLUE);
 //
 //                try {
 //                    ImageIO.write(img, "png", new FileOutputStream("test.png"));
 //                } catch (IOException ex) {
 //                    Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
 //                }
                 if (window != null) {
                     window.setVisible(true);
                     window.toFront();
                     this.setViewed(basic);
                     ((MainView) currentWindow).replot();
                 }
             }
         }
     }
 
     void clickOn(double xposn, double yposn) {
         if (currentWindow instanceof MainView) {
             RawCandidateBasic basic = ((MainView) currentWindow).getNearest(xposn, yposn);
 
             clickOn(basic);
         }
     }
 
     void clickOnHold(double xposn, double yposn) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     public void close() {
         closeWindow();
 
 
         Thread[] tarray = new Thread[255];
         int t = Thread.enumerate(tarray);
         for (int i = 0; i < t; i++) {
             Thread tt = tarray[i];
 
             System.out.println(tt.getName() + " " + tt.toString());
             if (!tt.getName().contains("AWT")) {
                 tt.interrupt();
             }
 
         }
         System.exit(0);
     }
 
     public void closeWindow() {
         saveViewedToDatabase();
         if (this.currentWindow != null) {
             if (EventQueue.isDispatchThread()) {
                 currentWindow.setVisible(false);
                 currentWindow.dispose();
             } else {
                 try {
 
                     // Close active window...
                     java.awt.EventQueue.invokeAndWait(new Runnable() {
 
                         public void run() {
                             currentWindow.setVisible(false);
                             currentWindow.dispose();
                             currentWindow = null;
                         }
                     });
 
                 } catch (InvocationTargetException ex) {
                 } catch (InterruptedException ex) {
                 }
             }
 
         }
     }
 
     List<ClassifiedCandidate> getClassifiedCandidatesNear(Coordinate coord, double distmax) {
         CoordinateDistanceComparitor distComp = new CoordinateDistanceComparitor(coord);
 
         ArrayList<ClassifiedCandidate> out = new ArrayList<ClassifiedCandidate>();
         for (ClassifiedCandidate c : this.classifiedCands) {
             if (distComp.difference(c.getCoordinate(), coord) > distmax) {
                 continue;
             }
             out.add(c);
         }
         Collections.sort(out, distComp);
         return out;
 
 
     }
 
     void markAreaDud(double xposn1, double xposn2, boolean unmark) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user.toLowerCase();
     }
 
     public void saveViewedToDatabase() {
         if (myViewedCandidates != null && myViewedCandidates.getViewed().size() > 0) {
             ViewedCandidates vc = myViewedCandidates;
             myViewedCandidates = new ViewedCandidates();
             try {
                 reSyncViewedWithDatabase();
                 ViewedCandidates vcOld = this.viewedCands.get(user);
                 if (vcOld != null) {
                     vc.setId(vcOld.getId());
                 }
                 vc.setName(user);
                 ViewedCandidates newVc = connection.postViewedCandidates(vc);
                 this.viewedCands.remove(user);
                 this.viewedCands.put(user, newVc);
             } catch (BookKeeprCommunicationException ex) {
                 Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
                 myViewedCandidates = vc;
                 return;
             }
         }
     }
 
     public void reSyncViewedWithDatabase() {
 
         // get all the viewed cands
         this.viewedCands = new HashMap<String, ViewedCandidates>();
         try {
             ViewedCandidatesIndex idx = connection.getAllViewedCandidates();
             for (ViewedCandidates vc : idx.getViewedCandidatesList()) {
                 Collections.sort(vc.getViewed());
                 this.viewedCands.put(vc.getName(), vc);
             }
         } catch (BookKeeprCommunicationException ex) {
             Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void reSyncClassifiedWithDatabase() {
 
         // get all the viewed cands
 
         try {
             this.classifiedCands = new ArrayList<ClassifiedCandidate>(connection.getAllClassifiedCandidates().getIndex());
         } catch (BookKeeprCommunicationException ex) {
             Logger.getLogger(JReaper.class.getName()).log(Level.SEVERE, "Error talking to server fetching classified candidates", ex);
         }
     }
 
     public RawCandidateBasic[] refine(RawCandidateBasic[] masterData, Hashtable<PlotType.axisType, Double> minVals, Hashtable<PlotType.axisType, Double> maxVals, ArrayList<Long> excludeCandListIds) {
         ArrayList<RawCandidateBasic> cData = new ArrayList<RawCandidateBasic>();
         Collections.sort(excludeCandListIds);
         PlotType pt = new PlotType(null, null);
 
         for (int i = 0; i < masterData.length; i++) {
 //                    if(masterData[i][j].getSNR() > SNRlimit && masterData[i][j].getDM() > DMmin && Arrays.binarySearch(excludeBeams,masterData[i][j].getBeam().getName(),String.CASE_INSENSITIVE_ORDER) <0)
 //                        cData[i].add(masterData[i][j]);
             boolean add = true;
             for (PlotType.axisType axisType : minVals.keySet()) {
                 if (pt.getVal(masterData[i], axisType) < minVals.get(axisType)) {
                     add = false;
                     break;
                 }
             }
             for (PlotType.axisType axisType : maxVals.keySet()) {
                 if (pt.getVal(masterData[i], axisType) > maxVals.get(axisType)) {
                     add = false;
                     break;
                 }
             }
 
             // don't include if we have the candlist id marked as exclude
             if (Collections.binarySearch(excludeCandListIds, masterData[i].getCandidateListId()) >= 0) {
                 add = false;
             }
             if (add) {
                 cData.add(masterData[i]);
             }
         }
 
         return cData.toArray(new RawCandidateBasic[0]);
     }
 }
