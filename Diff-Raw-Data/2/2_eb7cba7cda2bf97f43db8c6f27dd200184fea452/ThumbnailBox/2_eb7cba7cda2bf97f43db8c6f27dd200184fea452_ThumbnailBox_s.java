 package edu.cmu.cs.diamond.snapfind2;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 
 import edu.cmu.cs.diamond.opendiamond.DoubleComposer;
 import edu.cmu.cs.diamond.opendiamond.Result;
 import edu.cmu.cs.diamond.opendiamond.Search;
 import edu.cmu.cs.diamond.opendiamond.ServerStatistics;
 
 public class ThumbnailBox extends JPanel {
     volatile protected int nextEmpty = 0;
 
     final static private int ROWS = 3;
 
     final static private int COLS = 3;
 
     final protected ResultViewer[] pics = new ResultViewer[ROWS * COLS];
 
     final protected JButton nextButton = new JButton("Next");
 
     volatile protected Thread resultGatherer;
 
     volatile protected boolean running;
 
     protected Search search;
 
     final protected Object fullSynchronizer = new Object();
 
     private Annotator annotator;
 
     protected Decorator decorator;
 
     final protected StatisticsBar stats = new StatisticsBar();
 
     final protected Map<String, Double> globalSessionVariables;
 
     final protected AbstractTableModel sessionVariablesTableModel;
 
     final protected Timer statsTimer = new Timer(500, new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             // because it is Swing Timer, this is called from the
             // AWT dispatch thread
             ServerStatistics[] serverStats = search.getStatistics();
             boolean hasStats = false;
             for (ServerStatistics s : serverStats) {
                 if (s.getTotalObjects() != 0) {
                     hasStats = true;
                     break;
                 }
             }
             if (hasStats) {
                 stats.update(serverStats);
             } else {
                 stats.setIndeterminateMessage("Waiting for First Results");
             }
         }
     });
 
     final protected Timer sessionVarsTimer = new Timer(
             SnapFind2.INITIAL_SESSION_VARIABLES_UPDATE_INTERVAL * 1000,
             new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     search.mergeSessionVariables(globalSessionVariables,
                             composer);
                     sessionVariablesTableModel.fireTableDataChanged();
                 }
             });
 
     private DoubleComposer composer;
 
     private volatile boolean searchRunning;
     
    private volatile boolean updateSessionVars;
     
 
     public ThumbnailBox(Map<String, Double> globalSessionVariables,
             AbstractTableModel sessionVariablesTableModel) {
         super();
 
         this.globalSessionVariables = globalSessionVariables;
 
         this.sessionVariablesTableModel = sessionVariablesTableModel;
 
         Box v = Box.createVerticalBox();
         add(v);
         // v.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.BLUE));
         Box h = null;
         for (int i = 0; i < pics.length; i++) {
             boolean addBox = false;
             if (i % COLS == 0) {
                 h = Box.createHorizontalBox();
                 addBox = true;
             }
 
             ResultViewer b = new ResultViewer();
 
             h.add(b);
             pics[i] = b;
 
             if (addBox) {
                 v.add(h);
             }
         }
 
         h = Box.createHorizontalBox();
         h.setAlignmentX(Component.CENTER_ALIGNMENT);
 
         h.add(stats);
         h.add(Box.createHorizontalStrut(10));
 
         h.add(nextButton);
         nextButton.setEnabled(false);
         nextButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // next is clicked
                 nextButton.setEnabled(false);
                 synchronized (fullSynchronizer) {
                     clearAll();
                     fullSynchronizer.notify();
                 }
             }
         });
 
         v.add(h);
     }
 
     public void setAnnotator(Annotator a) {
         annotator = a;
     }
 
     public void setDecorator(Decorator d) {
         decorator = d;
     }
 
     public void setDoubleComposer(DoubleComposer c) {
         composer = c;
     }
 
     protected boolean isFull() {
         return nextEmpty >= pics.length;
     }
 
     protected void clearAll() {
         nextEmpty = 0;
         for (ResultViewer r : pics) {
             r.setResult(null);
             r.commitResult();
         }
     }
 
     protected void fillNext(final Result r) {
         System.out.println("fillNext " + r);
         if (!running) {
             return;
         }
 
         // update
         final ResultViewer v = pics[nextEmpty++];
 
         // loading message
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 v.setText("Loading");
             }
         });
 
         final String annotation;
         final String tooltipAnnotation;
         if (annotator != null) {
             annotation = annotator.annotate(r);
             tooltipAnnotation = annotator.annotateTooltip(r);
         } else {
             annotation = null;
             tooltipAnnotation = null;
         }
 
         // do slow activity of loading the item
         v.setResult(new AnnotatedResult(r, annotation, tooltipAnnotation,
                 decorator));
 
         // update GUI
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 v.setText(null);
                 v.commitResult();
             }
         });
     }
 
     protected class ResultsGatherer implements Runnable {
         public void run() {
             try {
                 while (running) {
                     // wait for next item
                     System.out.println("wait for next item...");
                     Result r = search.getNextResult();
                     System.out.println(" " + r);
 
                     if (r != null) {
                         // we have data
 
                         if (isFull()) {
                             // wait
                             synchronized (fullSynchronizer) {
                                 if (isFull()) {
                                     setNextEnabledOnAWT(true);
                                 }
                                 while (isFull()) {
                                     fullSynchronizer.wait();
                                 }
 
                                 // no longer full
                                 fillNext(r);
                             }
                         } else {
                             // not full
                             fillNext(r);
                         }
                     } else {
                         // no more objects
                         running = false;
                     }
                 }
             } catch (InterruptedException e) {
                 System.out.println("INTERRUPTED !");
             } finally {
                 running = false;
 
                 System.out.println("FINALLY stopping search");
                 search.stop();
                 System.out.println(" done");
 
                 // clear anything not shown
                 setNextEnabledOnAWT(false);
 
                 // clean up
                 resultGatherer = null;
                 
                 searchRunning = false;
                 updateTimers();
 
                 // one more stats
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         stats.update(search.getStatistics());
                     }
                 });
 
                 System.out.println("done with finally");
             }
         }
     }
 
     public void stop() {
         running = false;
         search.stop();
 
         Thread rg = resultGatherer;
         if (rg != null) {
             // interrupt anything
             rg.interrupt();
 
             // // wait for exit
             // try {
             // System.out.print("joining...");
             // System.out.flush();
             // resultGatherer.join();
             // System.out.println(" done");
             // } catch (InterruptedException e) {
             // e.printStackTrace();
             // }
         }
     }
 
     public void updateTimers() {
         if (searchRunning) {
             statsTimer.start();
             if (updateSessionVars) {
                 sessionVarsTimer.start();
             } else {
                 sessionVarsTimer.stop();
             }
         } else {
             statsTimer.stop();
             sessionVarsTimer.stop();
         }
     }
 
     public void start(Search s) {
         search = s;
 
         running = true;
 
         clearAll();
 
         stats.setIndeterminateMessage("Initializing Search");
 
         new Thread(new Runnable() {
             public void run() {
                 System.out.println("start search");
                 search.start();
 
                 searchRunning = true;
                 updateTimers();
                 (resultGatherer = new Thread(new ResultsGatherer())).start();
             }
         }).start();
     }
 
     protected void setNextEnabledOnAWT(final boolean state) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 nextButton.setEnabled(state);
             }
         });
     }
 
     public void setSessionVariableUpdateInterval(int value) {
         if (value == -1) {
             System.out.println("Stopping session vars timer");
             updateSessionVars = false;
             updateTimers();
         } else {
             System.out.println("Setting session vars timer to " + value);
             updateSessionVars = true;
             sessionVarsTimer.setDelay(value * 1000);
             updateTimers();
         }
     }
 }
