 /**
  * Copyright 2011 Kevin J. Jones (http://www.kevinjjones.co.uk)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package uk.co.kevinjjones;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import javax.swing.*;
 import uk.co.kevinjjones.model.*;
 import uk.co.kevinjjones.vehicle.SpeedStream;
 
 /**
  * Manager for all run data
  */
 public class RunManager implements ParamHandler {
 
     /**
      * A stream wrapper for RunManager records
      */
     public class RunStream implements ROStream {
 
         private ROStream _stream;
         private int _start;
         private int _size;
         private boolean _rebase;
         private double _base;
         private ArrayList<Pair<String, String>> _meta = new ArrayList();
 
         public RunStream(ROStream stream, int start, int size) {
             _stream = stream;
             _start = start;
             _size = size;
             _rebase = stream.getMeta("rebase").equals("true");
         }
 
         @Override
         public String name() {
             return _stream.name();
         }
 
         @Override
         public String description() {
             return _stream.description();
         }
 
         @Override
         public String axis() {
             return _stream.axis();
         }
 
         @Override
         public String units() {
             return _stream.units();
         }
 
         @Override
         public int size() {
             return _size;
         }
 
         @Override
         public String getString(int position) {
             assert (position < _size);
             return _stream.getString(_start + position);
         }
 
         @Override
         public long getTick(int position) throws ParseException {
             assert (position < _size);
             return _stream.getTick(_start + position);
 
         }
 
         @Override
         public double getNumeric(int position) throws NumberFormatException {
             assert (position < _size);
             if (_rebase) {
                 _base = _stream.getNumeric(_start + 0);
                 _rebase = false;
             }
             return _stream.getNumeric(_start + position) - _base;
         }
 
         @Override
         public String[] getStringSet() {
             return _stream.getStringSet();
         }
 
         @Override
         public Double[] toArray() throws NumberFormatException {
             return _stream.toArray();
         }
 
         @Override
         public void setMeta(String id, String value) {
             _meta.add(new Pair(id, value));
         }
 
         @Override
         public String getMeta(String id) {
             int i = 0;
             while (i < _meta.size()) {
                 if (_meta.get(i).first().equals(id)) {
                     return _meta.get(i).second();
                 }
             }
             return "";
         }
     }
 
     /**
      * A single Run wrapper
      */
     public class Run {
 
         private String _prefix;
         private Log _log;
         private int _start;
         private int _end;
         private boolean _isSplit;
 
         public Run(String prefix, Log log, int start, int end, boolean isSplit) {
             _prefix = prefix;
             _log = log;
             _start = start;
             _end = end;
             _isSplit = isSplit;
         }
 
         public Log log() {
             return _log;
         }
 
         public String name() {
             if (_prefix.isEmpty()) {
                 return _log.name();
             } else {
                 return _prefix + " " + _log.name();
             }
         }
 
         public int length() {
             return _end - _start;
         }
 
         public boolean isSplit() {
             return _isSplit;
         }
 
         public int streamCount() {
             return _log.streamCount();
         }
 
         public boolean hasStream(String name) {
             return _log.hasStream(name);
         }
 
         public ROStream getStream(String name) {
             if (!_log.hasStream(name)) {
                 return null;
             }
             return new RunStream(_log.getStream(name), _start, _end - _start);
         }
 
         public ROStream getStream(int index) {
             if (index < 0 || index >= _log.streamCount()) {
                 return null;
             }
             return new RunStream(_log.getStream(index), _start, _end - _start);
         }
         
         /*
          * Saving for later...
          */
 
         /*
          * public double degrees(int i) {
          *
          * if (_log.hasLeftSpeed() && _log.hasRightSpeed()) {
          *
          * double ls = _log.leftSpeed(_start + i); double rs =
          * _log.rightSpeed(_start + i);
          *
          * Car c = Car.getInstance(); boolean rightTurn = (ls > rs); if
          * (rightTurn) { return c.getDegrees(ls, rs); } else { return
          * -c.getDegrees(rs, ls); } } return 0; }
          *
          * public double latAccel(int i) {
          *
          * if (_log.hasLeftSpeed() && _log.hasRightSpeed()) {
          *
          * double ls = _log.leftSpeed(_start + i); double rs =
          * _log.rightSpeed(_start + i);
          *
          * Car c = Car.getInstance(); boolean rightTurn = (ls > rs); if
          * (rightTurn) { return c.getLatAccel(ls, rs); } else { return
          * -c.getLatAccel(rs, ls); } } return 0; }
          *
          * public double lambda(int index) { if (_lambdaData == null) {
          *
          * // Populate with shift _lambdaData = new double[length()]; for (int
          * j = 0; j < _lambdaData.length; j++) { int idx = (j + lambdaDelay()) %
          * (_end - _start); _lambdaData[j] = _log.lambda(_start + idx); }
          *
          * // Exclude 'bad' section where MAP<80 || map is falling // by 3%
          * over 0.5 seconds. for (int i = 0; i < _lambdaData.length; i++) {
          *
          * // Scan for a switch to bad int r = i; int startBad = 0; while (r +
          * 1 < _lambdaData.length) { double m = map(r); if (m < 80) { startBad =
          * r; break; } double d = (map(r + 1) - m) / m; if (d < -0.01) {
          * startBad = r; break; } r++; }
          *
          * if (startBad != 0) { double m = map(startBad); if (m < 80) { while (m
          * < 80 && startBad<_lambdaData.length) { _lambdaData[startBad] = 0;
          * startBad++; m = map(startBad); } i=startBad; } else { // Test for
          * sufficient fall double low = 0.97 * map(startBad); for (int k =
          * startBad; k < startBad + 5 && k<_lambdaData.length; k++) { if (map(k)
          * < low) { // Walk until map increases again while (map(k + 1) <
          * map(k)) { k++; }
          *
          * // Got one, so zero out for (int p = startBad - 1; p <= k+1; p++) {
          * _lambdaData[p] = 0; } i = k; break; } } } } } }
          *
          * return _lambdaData[index]; }
          *
          */
     }
     private static RunManager _instance = null;
     private static Frame _frame = null;
     private List<Log> _logs = new LinkedList<Log>();
     private List<Run> _runs = new LinkedList<Run>();
     private boolean _autoSplit = false;
     private int _KPH = 0;
 
     protected RunManager() {
 
         // Load preferences
         Preferences prefs = Preferences.userNodeForPackage(RunManager.class);
         _KPH = prefs.getInt("KPH2", 0);
         _autoSplit = prefs.getBoolean("AutoSplit", false);
     }
 
     // Singleton access
     public static RunManager getInstance() {
         if (_instance == null) {
             _instance = new RunManager();
         }
         return _instance;
     }
 
     // Set UI frame for allow dialog boxes to be setup
     public void setFrame(Frame frame) {
         _frame = frame;
     }
 
     @Override
     public String getParameter(String name, boolean prompt) {
         if (name.equals("isKPH")) {
             if (_KPH == 0 && prompt) {
                 JDialog dialog = new JDialog(_frame, "DTA System Units", true);
                 Container content = dialog.getContentPane();
                 OptionsDialog oDlg = new OptionsDialog();
                 content.add(oDlg);
 
                 dialog.pack();
                 dialog.setLocationRelativeTo(_frame);
                 dialog.setVisible(true);
             }
             if (_KPH == 1) {
                 return "true";
             }
             if (_KPH == 2) {
                 return "false";
             }
         }
         return null;
     }
 
     public void addLogfile(File file, WithError<Boolean, BasicError> ok) throws IOException {
 
         // Parse logile and load runs from it
         Log l = new Log(file, this, ok);
         if (ok.value()) {
             _logs.add(l);
             addSessions(l, ok);
         }
     }
 
     public Log[] getLogs() {
         return _logs.toArray(new Log[0]);
     }
 
     public Run[] getRuns() {
         return _runs.toArray(new Run[0]);
     }
 
     private void addSessions(Log l, WithError<Boolean, BasicError> ok) {
 
         // Now look for multiple sessions
         ROStream sess = l.getStream(Log.SESSION_STREAM);
         String c = sess.getString(0);
         int begin = 1;
         int end = 1;
         int s = 1;
         int runs = 0;
         for (; end < sess.size(); end++) {
             if (!sess.getString(end).equals(c)) {
                 runs += addRuns(l, s++, begin, end - 1 - begin);
                 begin = end;
                 c = sess.getString(begin);
             }
         }
         runs += addRuns(l, s++, begin, end - 1 - begin);
         if (isAutoSplit() && runs == 0) {
             ok.addError(new BasicError(BasicError.WARN, "No runs detected, try "
                     + "loading file with auto spliting turned off"));
         } else if (isAutoSplit()) {
             ok.addError(new BasicError(BasicError.INFO, "Loaded " + (s - 1)
                     + " sessions from " + l.name() + ", containing " + runs
                     + " runs."));
         } else {
             ok.addError(new BasicError(BasicError.INFO, "Loaded " + (s - 1)
                     + " sessions from " + l.name() + "."));
         }
     }
 
     private int addRuns(Log l, int session, int begin, int size) {
 
         // Should we also split?
         int endSession = begin + size;
         int id = 1;
         ROStream speed = l.getStream(SpeedStream.NAME);
         if (speed != null && isAutoSplit()) {
 
             // Look for runs
             int at = begin;
             while (true) {
                 int start = findStartPoint(speed, at, endSession);
                 if (start == -1) {
                     break;
                 }
                 int end = findEndPoint(speed, start, endSession);
                 if (end == -1) {
                     break;
                 }
 
                 _runs.add(new Run("S" + session + "#" + id++, l, start, end, true));
                 at = end;
             }
             return id - 1;
         } else {
             _runs.add(new Run("S" + session, l, begin, begin + size, false));
             return 1;
         }
     }
 
     /**
      * Test if the current row is a crossing point to the next speed unit. We
      * use two datums here, crossing from 0->1 kph and crossing over a
      * speedBucket
      *
      * @param data
      * @param start
      * @param index
      * @return
      */
     private static int findStartPoint(ROStream speed, int start, int endSession) {
 
         // Loop speed until >60kph
         int r = start;
         for (; r < endSession; r++) {
             if (speed.getNumeric(r) > 60) {
                 break;
             }
         }
         // Didn't find 
         if (r == endSession) {
             return -1;
         }
 
         // Now loop back to locate the start at lowest speed <10kph
         while (r > start) {
             double s = speed.getNumeric(r);
             double low = s;
             if (s < 10) {
                 // In right area, find first lowest
                 int t = r - 1;
                 while (t > 0) {
                     double s2 = speed.getNumeric(t);
                     if (s2 < low) {
                         low = s2;
                         r = t;
                     } else {
                         return r;
                     }
                     t--;
                 }
             }
             r--;
         }
 
         // Not found;
         return -1;
     }
 
     private static int findEndPoint(ROStream speed, int start, int endSession) {
 
         // Loop speed until >60kph
         int r = start;
         for (; r < endSession; r++) {
             if (speed.getNumeric(r) > 60) {
                 break;
             }
         }
         // Didn't find 
         if (r == endSession) {
             return -1;
         }
 
         // Continue loop until < 5km/h
         for (; r < endSession; r++) {
             if (speed.getNumeric(r) < 5) {
                 return r;
             }
         }
         return -1;
     }
 
     private void flushPrefs() {
         Preferences prefs = Preferences.userNodeForPackage(RunManager.class);
         prefs.putInt("KPH2", _KPH);
         prefs.putBoolean("AutoSplit", _autoSplit);
         try {
             prefs.flush();
            throw new BackingStoreException("Test");
         } catch (BackingStoreException ex) {
             final JDialog dialog = new JDialog(_frame, "Error", true);
             JPanel displayArea = new JPanel();
             displayArea.setLayout(new BoxLayout(displayArea, BoxLayout.PAGE_AXIS));
 
             JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
             displayArea.add(panel);
             panel.add(new JLabel("Message:"));
             panel.add(new JLabel("Failed to save user preferences"));
 
             JPanel epanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
             displayArea.add(epanel);
 
             epanel.add(new JLabel("Exception:"));
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw, true);
             ex.printStackTrace(pw);
             pw.flush();
             sw.flush();
             epanel.add(new JLabel("<html>" + sw.toString().replaceAll("\n", "<br>")));
 
             JPanel buttonArea = new JPanel();
             FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT);
             buttonArea.setLayout(buttonLayout);
             JButton okBtn = new JButton("OK");
             buttonArea.add(okBtn);
 
             okBtn.addActionListener(
                     new ActionListener() {
 
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             dialog.setVisible(false);
                         }
                     });
 
             JPanel dialogPanel = new JPanel();
             BorderLayout dialogLayout = new BorderLayout();
             dialogPanel.setLayout(dialogLayout);
 
             dialogPanel.add(displayArea, BorderLayout.CENTER);
             dialogPanel.add(buttonArea, BorderLayout.PAGE_END);
 
             Container content = dialog.getContentPane();
             content.add(dialogPanel);
 
             dialog.pack();
             dialog.setLocationRelativeTo(_frame);
             dialog.setVisible(true);
         }
     }
 
     public boolean isAutoSplit() {
         return _autoSplit;
     }
 
     public void setAutoSplit(boolean isAutoSplit) {
         _autoSplit = isAutoSplit;
         flushPrefs();
     }
 
     public int getKPH() {
         return _KPH;
     }
 
     public void setKPH(boolean isKPH) {
         _KPH = isKPH ? 1 : 2;
         flushPrefs();
     }
 
     public void unsetKPH() {
         _KPH = 0;
         flushPrefs();
     }
 }
