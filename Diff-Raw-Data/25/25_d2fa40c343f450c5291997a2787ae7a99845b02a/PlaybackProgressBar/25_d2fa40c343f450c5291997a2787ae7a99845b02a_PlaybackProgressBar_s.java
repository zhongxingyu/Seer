 package com.robonobo.gui.components;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.frames.RobonoboFrame;
 
 /** Maintains two 'progress' indicators. The first, 'availableData', displays how much data we have downloaded by means
  * of a light blue bar, and is the limit to how far we can seek by dragging the slider. The second, 'trackPosition', is
  * our playback position within the track and displayed by means of the slider position.
  * 
  * @author macavity */
 @SuppressWarnings("serial")
 public class PlaybackProgressBar extends JProgressBar {
 	private static final int TOTAL_WIDTH = 325;
 	private static final int SLIDER_TOTAL_WIDTH = 65;
 	private static final int SLIDER_OPAQUE_WIDTH = 62;
 	private RobonoboFrame frame;
 	// Locked means no current track - can't drag slider, no progress indicators
 	private boolean locked = true;
 	private boolean dragging;
 	/** In reference to the slider thumb */
 	private Point mouseDownPt;
 	private List<Listener> listeners;
 	private JButton sliderThumb;
 	private JLabel startLabel, endLabel;
 	private long trackLengthMs;
 	private long trackPositionMs;
 	private float dataAvailable;
 	Log log = LogFactory.getLog(getClass());
 	private Timer pauseTimer;
 	boolean started = false;
 
 	public PlaybackProgressBar(final RobonoboFrame frame) {
 		this.frame = frame;
 		setName("robonobo.playback.progressbar");
 		listeners = new ArrayList<Listener>();
 		setMinimum(0);
 		setMaximum(TOTAL_WIDTH);
 		setPreferredSize(new Dimension(325, 24));
 		// Absolute positioning of elements
 		setLayout(null);
 		sliderThumb = new JButton();
 		sliderThumb.setName("robonobo.playback.progressbar.thumb");
 		sliderThumb.setFont(RoboFont.getFont(11, false));
 		sliderThumb.setFocusable(false);
 		sliderThumb.setLocation(0, 0);
 		add(sliderThumb);
 		startLabel = new JLabel("-0:00");
 		startLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
 		startLabel.setSize(startLabel.getPreferredSize());
 		startLabel.setFont(RoboFont.getFont(11, false));
 		startLabel.setForeground(Color.WHITE);
 		add(startLabel);
 		endLabel = new JLabel();
 		endLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
 		endLabel.setSize(endLabel.getPreferredSize());
 		endLabel.setFont(RoboFont.getFont(11, false));
 		add(endLabel);
 		addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent e) {
 				// auto adjust thumb size
 				sliderThumb.setSize(new Dimension(SLIDER_TOTAL_WIDTH, getHeight()));
 				// update the thumb's position
 				setTrackPosition(0);
 				// auto adjust the labels' position
 				startLabel.setLocation(0, ((getHeight() - startLabel.getHeight()) / 2));
 				endLabel.setLocation(getWidth() - endLabel.getWidth(), ((getHeight() - endLabel.getHeight()) / 2) + 1);
 			}
 		});
 		// mouse event processing
 		sliderThumb.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				// Don't allow seeking while we're still in initial buffering
 				if (!started)
 					return;
 				dragging = true;
 				mouseDownPt = e.getPoint();
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				if (dragging) {
 					dragging = false;
 					mouseDownPt = null;
 					frame.getController().getExecutor().execute(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							for (Listener l : listeners) {
 								l.sliderReleased(trackPositionMs);
 							}
 						}
 					});
 				}
 			}
 		});
 		sliderThumb.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				// Don't allow seeking while we're still in initial buffering
 				if (!started)
 					return;
 				// Get position relative to progress bar
 				Point progressBarPos = SwingUtilities.convertPoint(sliderThumb, e.getPoint(), PlaybackProgressBar.this);
 				// Take into account which part of the slider they're dragging by using mouseDownPt
 				int thumbX = progressBarPos.x - mouseDownPt.x;
 				// thumbX <= 0, trackPosition = 0
 				// thumbX >= maximum - thumbWidth, trackPosition = trackLength
 				int maxX = getMaximum() - SLIDER_TOTAL_WIDTH;
 				float relPos = (float) thumbX / maxX;
 				if (relPos > dataAvailable)
 					relPos = dataAvailable;
 				long trackPos = (long) (relPos * trackLengthMs);
 				if (trackPos < 0)
 					trackPos = 0;
 				if (trackPos > trackLengthMs)
 					trackPos = trackLengthMs;
 				setTrackPosition(trackPos, true);
 			}
 		});
 		addListener(new Listener() {
 			@Override
 			public void sliderReleased(long trackPositionMs) {
 				frame.getController().seek(trackPositionMs);
 			}
 		});
 	}
 
 	/** pos = 0, trackPosition = 0 <br/>
 	 * pos = (maximum - thumbWidth), trackPosition = trackLength */
 	private void setThumbPosition(int pos) {
 		if (pos < 0)
 			pos = 0;
 		if (pos > (getMaximum() - SLIDER_OPAQUE_WIDTH))
 			pos = getMaximum() - SLIDER_OPAQUE_WIDTH;
 		sliderThumb.setLocation(pos, 0);
 	}
 
 	public void starting() {
 		started = false;
 		setThumbPosition(0);
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				sliderThumb.setForeground(Color.BLACK);
 				sliderThumb.setName("robonobo.playback.progressbar.thumb");
 			}
 		});
 		startThumbTextFlashing();
 	}
 
 	public void play() {
 		started = true;
 		stopThumbTextFlashing();
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				sliderThumb.setForeground(Color.BLACK);
 				sliderThumb.setName("robonobo.playback.progressbar.thumb");
 			}
 		});
 	}
 
 	public void pause() {
 		startThumbTextFlashing();
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				sliderThumb.setName("robonobo.playback.progressbar.thumb.paused");
 			}
 		});
 	}
 
 	private void startThumbTextFlashing() {
 		if (pauseTimer == null) {
 			pauseTimer = new Timer(500, new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (sliderThumb.getForeground().equals(Color.BLACK))
 						sliderThumb.setForeground(Color.WHITE);
 					else
 						sliderThumb.setForeground(Color.BLACK);
 				}
 			});
 		}
 		pauseTimer.start();
 	}
 
 	private void stopThumbTextFlashing() {
 		if (pauseTimer != null)
 			pauseTimer.stop();
 	}
 
 	public void setOrientation(int newOrientation) {
 		if (newOrientation != JProgressBar.HORIZONTAL)
 			throw new RuntimeException("PlaybackProgressBar only support horizontal orientation");
 	}
 
 	public void addListener(Listener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(Listener listener) {
 		listeners.remove(listener);
 	}
 
 	/** Locks the component until setTrackLength() is called */
 	public void lock() {
 		locked = true;
 		setEndText("");
 		setTrackPosition(0);
 		setDataAvailable(0f);
 		doRepaint();
 	}
 
 	public void setTrackDuration(long lengthMs) {
 		if (locked)
 			locked = false;
 		this.trackLengthMs = lengthMs;
 		setEndText(timeLblFromMs(lengthMs));
 	}
 
 	public void setTrackPosition(long positionMs) {
 		setTrackPosition(positionMs, false);
 	}
 
 	private void setTrackPosition(long positionMs, boolean viaDrag) {
 		// Don't update if we're dragging
 		if (dragging && !viaDrag)
 			return;
 		trackPositionMs = positionMs;
 		String newSliderText = timeLblFromMs(positionMs);
 		if (newSliderText.equals(getSliderText()))
 			return;
 		// pos = 0, trackPosition = 0
 		// pos = (maximum - thumbWidth), trackPosition = trackLength
 		int thumbPos = (int) ((getMaximum() - SLIDER_OPAQUE_WIDTH) * ((float) positionMs / trackLengthMs));
 		setSliderText(newSliderText);
 		long msLeft = trackLengthMs - trackPositionMs;
 		setStartText("-" + timeLblFromMs(msLeft));
 		setThumbPosition(thumbPos);
 		doRepaint();
 	}
 
 	public long getTrackPosition() {
 		return trackPositionMs;
 	}
 
 	public void setDataAvailable(final float available) {
 		// Colour in progress bar value to illustrate seek limit - take into account thumb width
 		if (this.dataAvailable != available) {
 			this.dataAvailable = available;
			SwingUtilities.invokeLater(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					int max = getMaximum() - SLIDER_OPAQUE_WIDTH;
 					int val = SLIDER_OPAQUE_WIDTH + (int) (available * max);
 					setValue(val);
 					doRepaint();
 				}
 			});
 		}
 	}
 
 	private void setStartText(String text) {
 		startLabel.setText(text);
 		startLabel.setSize(startLabel.getPreferredSize());
 	}
 
 	private void setEndText(String text) {
 		endLabel.setText(text);
 		endLabel.setSize(endLabel.getPreferredSize());
 		endLabel.setLocation(getWidth() - endLabel.getWidth(), (getHeight() - endLabel.getHeight()) / 2);
 	}
 
 	private void setSliderText(String text) {
 		sliderThumb.setText(text);
 	}
 
 	private String getSliderText() {
 		return sliderThumb.getText();
 	}
 
 	private String timeLblFromMs(long ms) {
 		int totalSec = Math.round(ms / 1000f);
 		int hours = totalSec / 3600;
 		int minutes = (totalSec % 3600) / 60;
 		int seconds = (totalSec % 60);
 		if (hours > 0)
 			return String.format("%d:%d:%02d", hours, minutes, seconds);
 		else
 			return String.format("%d:%02d", minutes, seconds);
 	}
 
 	private void doRepaint() {
 		RepaintManager.currentManager(this).markCompletelyDirty(this);
 	}
 
 	public interface Listener {
 		public void sliderReleased(long trackPositionMs);
 	}
 }
