 package com.aerodynelabs.habtk.ui;
// XXX Handle stop time after midnight
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.SwingWorker;
 
 import org.noos.xing.mydoggy.ToolWindowManager;
 
 import com.aerodynelabs.habtk.prediction.Predictor;
 import com.aerodynelabs.map.MapPanel;
 import com.aerodynelabs.map.MapPath;
 import com.aerodynelabs.map.MapPoint;
 
 /**
  * A panel to edit prediction parameters and execute a series of predictions.
  * @author Ethan Harstad
  *
  */
 @SuppressWarnings("serial")
 public class PredictionPanel extends JPanel {
 	
 	private JTextField fFlight;
 	private JTextField fStart;
 	private JTextField fStop;
 	private JSpinner fStep;
 	private JSpinner fDays;
 	private JButton run;
 	private JProgressBar progress;
 	
 	private final ToolWindowManager twm;
 	private Predictor baseFlight;
 	
 	private MapPanel map;
 	private FlightListPanel list;
 	
 	private int nTasks = 0;
 	private int cTasks = 0;
 	
 	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 	
 	public PredictionPanel(ToolWindowManager windowManager) {
 		super();
 		twm = windowManager;
 		
 		SpringLayout layout = new SpringLayout();
 		setLayout(layout);
 		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 		Date now = new Date();
 		
 		JLabel lFlight = new JLabel("Flight:");
 		fFlight = new JTextField();
 		fFlight.setEditable(false);
 		JButton bNew = new JButton("New");
 		bNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Predictor pred = Predictor.create();
 				if(pred != null) {
 					baseFlight = pred; 
 					fFlight.setText(baseFlight.toString());
 				}
 			}
 		});
 		JButton bLoad = new JButton("Open");
 		bLoad.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Predictor pred = Predictor.load();
 				if(pred != null) {
 					baseFlight = pred;
 					fFlight.setText(baseFlight.toString());
 				}
 			}
 		});
 		JButton bEdit = new JButton("Edit");
 		bEdit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(baseFlight == null) return;
 				if(baseFlight.setup()) fFlight.setText(baseFlight.toString());
 			}
 		});
 		layout.putConstraint(SpringLayout.NORTH, lFlight, 6, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.WEST, lFlight, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.NORTH, fFlight, 6, SpringLayout.SOUTH, lFlight);
 		layout.putConstraint(SpringLayout.WEST, fFlight, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, fFlight, -6, SpringLayout.EAST, this);
 		layout.putConstraint(SpringLayout.NORTH, bNew, 6, SpringLayout.SOUTH, fFlight);
 		layout.putConstraint(SpringLayout.NORTH, bLoad, 6, SpringLayout.SOUTH, fFlight);
 		layout.putConstraint(SpringLayout.NORTH, bEdit, 6, SpringLayout.SOUTH, fFlight);
 		layout.putConstraint(SpringLayout.WEST, bNew, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.WEST, bLoad, 6, SpringLayout.EAST, bNew);
 		layout.putConstraint(SpringLayout.WEST, bEdit, 6, SpringLayout.EAST, bLoad);
 		layout.putConstraint(SpringLayout.EAST, bEdit, -6, SpringLayout.EAST, this);
 		add(lFlight);
 		add(fFlight);
 		add(bNew);
 		add(bLoad);
 		add(bEdit);
 		
 		JLabel lStart = new JLabel("Start Time:");
 		fStart = new JTextField(dateTimeFormat.format(now));
 		JButton bStart = new JButton("Pick");
 		bStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DateTimePicker picker;
 				try {
 					picker = new DateTimePicker(DateTimePicker.DATETIME, dateTimeFormat.parse(fStart.getText()));
 				} catch (ParseException e1) {
 					picker = new DateTimePicker(DateTimePicker.DATETIME);
 				}
 				if(!picker.wasAccepted()) return;
 				Date date = picker.getValue();
 				if(date != null) fStart.setText(dateTimeFormat.format(date));
 			}
 		});
 		layout.putConstraint(SpringLayout.NORTH, fStart, 6, SpringLayout.SOUTH, bNew);
 		layout.putConstraint(SpringLayout.BASELINE, lStart, 0, SpringLayout.BASELINE, fStart);
 		layout.putConstraint(SpringLayout.NORTH, bStart, 0, SpringLayout.NORTH, fStart);
 		layout.putConstraint(SpringLayout.SOUTH, bStart, 0, SpringLayout.SOUTH, fStart);
 		layout.putConstraint(SpringLayout.WEST, lStart, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.WEST, fStart, 6, SpringLayout.EAST, lStart);
 		layout.putConstraint(SpringLayout.WEST, bStart, 6, SpringLayout.EAST, fStart);
 		layout.putConstraint(SpringLayout.EAST, this, 6, SpringLayout.EAST, bStart);
 		add(lStart);
 		add(fStart);
 		add(bStart);
 		
 		JLabel lStop = new JLabel("Stop Time:");
 		fStop = new JTextField(timeFormat.format(new Date(now.getTime() + 6*60*60*1000)));
 		JButton bStop = new JButton("Pick");
 		bStop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DateTimePicker picker;
 				try {
 					picker = new DateTimePicker(DateTimePicker.TIME, timeFormat.parse(fStop.getText()));
 				} catch (ParseException e1) {
 					picker = new DateTimePicker(DateTimePicker.TIME);
 				}
 				if(!picker.wasAccepted()) return;
 				Date date = picker.getValue();
 				if(date != null) fStop.setText(timeFormat.format(date));
 			}
 		});
 		layout.putConstraint(SpringLayout.NORTH, fStop, 6, SpringLayout.SOUTH, fStart);
 		layout.putConstraint(SpringLayout.BASELINE, lStop, 0, SpringLayout.BASELINE, fStop);
 		layout.putConstraint(SpringLayout.NORTH, bStop, 0, SpringLayout.NORTH, fStop);
 		layout.putConstraint(SpringLayout.SOUTH, bStop, 0, SpringLayout.SOUTH, fStop);
 		layout.putConstraint(SpringLayout.WEST, lStop, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.WEST, fStop, 6, SpringLayout.EAST, lStop);
 		layout.putConstraint(SpringLayout.EAST, fStop, 0, SpringLayout.EAST, fStart);
 		layout.putConstraint(SpringLayout.WEST, bStop, 6, SpringLayout.EAST, fStop);
 		layout.putConstraint(SpringLayout.EAST, bStop, -6, SpringLayout.EAST, this);
 		add(lStop);
 		add(fStop);
 		add(bStop);
 		
 		JLabel lStep = new JLabel("Interval (hr):");
 		fStep = new JSpinner(new SpinnerNumberModel(3, 1, 24, 1));
 		layout.putConstraint(SpringLayout.NORTH, fStep, 6, SpringLayout.SOUTH, fStop);
 		layout.putConstraint(SpringLayout.BASELINE, lStep, 0, SpringLayout.BASELINE, fStep);
 		layout.putConstraint(SpringLayout.WEST, lStep, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.WEST, fStep, 6, SpringLayout.EAST, lStep);
 		add(lStep);
 		add(fStep);
 		
 		JLabel lDays = new JLabel("Days out:");
 		fDays = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
 		layout.putConstraint(SpringLayout.NORTH, fDays, 6, SpringLayout.SOUTH, fStop);
 		layout.putConstraint(SpringLayout.BASELINE, lDays, 0, SpringLayout.BASELINE, fDays);
 		layout.putConstraint(SpringLayout.WEST, lDays, 6, SpringLayout.EAST, fStep);
 		layout.putConstraint(SpringLayout.WEST, fDays, 6, SpringLayout.EAST, lDays);
 		layout.putConstraint(SpringLayout.EAST, fDays, -6, SpringLayout.EAST, this);
 		add(lDays);
 		add(fDays);
 		
 		progress = new JProgressBar();
 		progress.setStringPainted(true);
 		progress.setMaximum(nTasks + 1);
 		progress.setValue(cTasks + 1);
 		layout.putConstraint(SpringLayout.NORTH, progress, 6, SpringLayout.SOUTH, fDays);
 		layout.putConstraint(SpringLayout.WEST, progress, 6, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, progress, -6, SpringLayout.EAST, this);
 		add(progress);
 		
 		run = new JButton("Run");
 		run.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// Update button states
 //				cancel.setEnabled(true);
 				run.setEnabled(false);
 				
 				// Create main content if needed
 				if(map == null) {
 					MapPoint startPoint = baseFlight.getStart();
 					map = new MapPanel(startPoint.getLatitude(), startPoint.getLongitude(), 9);
 					twm.getContentManager().addContent("Prediction Map", "Prediction Map", null, map, "Map Panel");
 				}
 				if(list == null) {
 					list = new FlightListPanel(map);
 					twm.getContentManager().addContent("Prediction List", "Prediction List", null, list, "Prediction List");
 				}
 				
 				// Retrieve fields
 				Date tempStart;
 				Date endOfDay;
 				int increment, nDays;
 				try {
 					tempStart = dateTimeFormat.parse(fStart.getText());
 					endOfDay = timeFormat.parse(fStop.getText());
 					increment = ((SpinnerNumberModel)fStep.getModel()).getNumber().intValue();
 					nDays = ((SpinnerNumberModel)fDays.getModel()).getNumber().intValue();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 					return;
 				}
 				
 				// Get Model End
 				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 				int modelRun = (cal.get(Calendar.HOUR_OF_DAY) / 12) * 12;
 				cal.set(Calendar.HOUR_OF_DAY, modelRun);
 				cal.set(Calendar.MINUTE, 0);
 				cal.set(Calendar.SECOND, 0);
 				cal.set(Calendar.MILLISECOND, 0);
 				cal.add(Calendar.DAY_OF_YEAR, 8);
 				Date modelEnd = cal.getTime();
 				
 				// Get start/end times
 				Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 				start.setTime(tempStart);
 				cal.setTime(tempStart);
 				Calendar dayEnd = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 				dayEnd.setTime(endOfDay);
 				dayEnd.set(Calendar.YEAR, cal.get(Calendar.YEAR));
 				dayEnd.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR));
 				Calendar stop = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 				stop.setTime(dayEnd.getTime());
 				stop.add(Calendar.DAY_OF_YEAR, nDays - 1);
 				if( cal.before(Calendar.getInstance( TimeZone.getTimeZone("GMT") )) ) cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 				
 				while(cal.before(stop)) {
 					if(cal.getTime().after(modelEnd)) {
 						JOptionPane.showMessageDialog(null, "Prediction timeframe exceeds model timeframe.", "Weather Model Error", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					PredictionTask task = new PredictionTask(baseFlight, cal.getTime());
 					progress.setMaximum((++nTasks) + 1);
 					task.execute();
 					cal.add(Calendar.HOUR_OF_DAY, increment);
 					if(cal.after(dayEnd)) {
 						dayEnd.add(Calendar.DAY_OF_YEAR, 1);
 						start.add(Calendar.DAY_OF_YEAR, 1);
 						cal.setTime(start.getTime());
 					}
 				}
 			}
 		});
 		layout.putConstraint(SpringLayout.NORTH, run, 6, SpringLayout.SOUTH, progress);
 		layout.putConstraint(SpringLayout.EAST, run, -6, SpringLayout.EAST, this);
 		add(run);
 	}
 	
 	class PredictionTask extends SwingWorker<Void, Void> {
 		
 		private Predictor predictor;
 		private MapPath path;
 		
 		public PredictionTask(Predictor flight, Date startTime) {
 			super();
 			predictor = flight.clone();
 			predictor.setStartTime(startTime.getTime() / 1000);
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			path = predictor.runPrediction();
 			return null;
 		}
 		
 		@Override
 		public void done() {
 			list.addFlight(predictor, path);
 			++cTasks;
 			if(cTasks == nTasks) {
 				cTasks = 0;
 				nTasks = 0;
 				progress.setMaximum(nTasks + 1);
 				run.setEnabled(true);
 			}
 			progress.setValue(cTasks + 1);
 		}
 		
 	}
 
 }
