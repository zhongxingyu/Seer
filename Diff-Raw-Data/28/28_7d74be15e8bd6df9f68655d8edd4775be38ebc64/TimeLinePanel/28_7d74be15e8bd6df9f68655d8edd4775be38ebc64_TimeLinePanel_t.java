 /**
 *   ORCC rapid content creation for entertainment, education and media production
 *   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.mcuosmipcuter.orcc.soundvis.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.gui.table.CustomTableListener;
 import org.mcuosmipcuter.orcc.soundvis.AudioInput;
 import org.mcuosmipcuter.orcc.soundvis.Context;
 import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
 import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
 import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
 import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
 import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread;
 import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.CallBack;
 import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSample;
 import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSampleData;
 
 /**
  * Panel that displays the timeline and the waveform and sets the song position pointer.
  * @author Michael Heinzelmann
  */
 public class TimeLinePanel extends JPanel implements CustomTableListener {
 
 	private static final long serialVersionUID = 1L;
 
 	// fixed layout
 	private final int margin = 20;
 	private final int marginY = 24;
 	private final int displaySecondsStep = 5;
 	private final static Color selectPointerColor = new Color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 128);
 	private final static Color selectFrameWidthColor =  new Color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 44);
 	private final static Color timeMarkerColor = new Color(136, 166, 166);
 	private final static Color timeNumbersColor = new Color(136, 200, 200);
 	private final static Color selectedCanvasColor = new Color(255, 255, 255, 200);
 	private final static Color canvasColor1 = new Color(146, 166, 176, 44);
 	private final static Color canvasColor2 = new Color(146, 176, 146, 44);
 	
 	// flexible layout screen dependent
 	int widthToUse;
 	int heightToUse = 150;
 	int guiWidth;
 	
 	// user configuration
 	boolean autoZoom = true;
 	int samplesToZoom = 100;
 	private int preRunFrames;
 	
 	// state
 	private boolean loading;
 	private int noOfSamplesLoaded;
 	
 	// calculated data
 	private SuperSampleData superSampleData;
 	private SuperSampleData superSampleDataAutoZoomed;
 	private SuperSampleData superSampleDataFrameZoomed;
 	private int noOfSamples;
 	private int divY = 1;
 	
 	// user and play positions
 	private int selectPos = margin;
 	private long selectFrame;
 	private long samplePosition;
 	private int paintProgressPos;
 	
 	// data from input / output
 	private int samplesPerFrame = 1;
 	private int sampleRate;
 	private long totalSampleLength;
 	private int videoFrameRate;
 	
 	List<SoundCanvasWrapper> currentCanvasList = new ArrayList<SoundCanvasWrapper>();
 	
 	/**
 	 * Sets a gray background and adds listeners
 	 */
 	public TimeLinePanel() {
 		
 		setBackground(Color.GRAY);
 		addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				selectPos = e.getX();
 				if(selectPos < margin) {
 					selectPos = margin;
 				}
 				int pixel = selectPos - margin;
 				selectFrame = pixel * noOfSamples / samplesPerFrame;
 				repaint();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				int pixel = selectPos - margin;
 				final long frame = pixel * noOfSamples / samplesPerFrame;
 				long f = frame - preRunFrames;
 				if(f < 0) {
 					f = 0;
 				}
 				if(Context.getAppState() == AppState.READY) {
 					Context.setSongPositionPointer(f);
 				}
 				selectFrame = frame;
 			}
 			
 		});
 		addMouseMotionListener(new MouseAdapter() {
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				final int oldSelectPos = selectPos;
 				selectPos = e.getX();
 				if(selectPos < margin) {
 					selectPos = margin;
 				}
 				int pixel = selectPos - margin;
 				selectFrame = pixel * noOfSamples / samplesPerFrame;
 				if(selectPos != oldSelectPos) {
 					repaint();
 				}
 			}
 			
 		});
 		Context.addListener(new Listener() {
 			
 			@Override
 			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.AudioInputInfo.equals(propertyName) 
						|| PropertyName.VideoFrameRate.equals(propertyName)) {
 					superSampleDataFrameZoomed = null;
 					superSampleDataAutoZoomed = null; // forces reload
 					setInputOutputData();
 					selectPos = margin;
 					samplePosition = 0;
					selectFrame = 0;
 					Context.setSongPositionPointer(0);
 				}
 				if( Context.PropertyName.SoundCanvasList == propertyName ||
 					Context.PropertyName.SoundCanvasAdded == propertyName || 
 					Context.PropertyName.SoundCanvasRemoved == propertyName) {
 						currentCanvasList.clear();
 						currentCanvasList.addAll(Context.getSoundCanvasList());
 						repaint();
 				}
 				if(PropertyName.AppState.equals(propertyName)) {
 					if(Context.getAppState() == AppState.READY) {
 						Context.setSongPositionPointer(selectFrame - preRunFrames);
 					}
					if(Context.getAppState() == AppState.EXPORTING) {
						Context.setSongPositionPointer(0);
					}
 				}
 			}
 
 		});
 
 	}
 	
 	// paint a message
 	private void paintLoading(Graphics g) {
 		g.setColor(Color.GRAY);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setColor(Color.WHITE);
 		g.drawString("loading ...", Math.max(guiWidth / 2, selectPos), getHeight() / 2);
 	}
 	public void paintProgress() {
 		
 		if(loading) {
 			return;
 		}
 		
 		Graphics g = getGraphics();
		
 		if(g == null) {
 			return;
 		}

 		int currentPos = (int)(samplePosition / noOfSamples);
 		if(paintProgressPos > currentPos) {
 			paintProgressPos = currentPos > 1 ? currentPos - 1 : 0;
 		}
		final int listLength = superSampleData.getList().length;
		for(int pos = paintProgressPos + 1; pos <= currentPos && pos < listLength; pos++) {
			SuperSample s = superSampleData.getList()[pos];
 			int x = margin + 1 + pos;
 			g.setColor(Color.BLACK);
 			g.drawLine(x, heightToUse /2 - s.getMax() / divY, x, heightToUse / 2 - s.getMin() / divY);
 		}
 		paintProgressPos = currentPos;
		
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if(loading) {
 			paintLoading(g);
 			return;
 		}
 		long pos = 0;
 		if(superSampleData != null) {
 			
 			final int h = heightToUse;
 			final int w = widthToUse;
 			
 			
 			int y = 0;
 			int b = h / 2;
 			int count = 0;
 			long [][] layers = new long[currentCanvasList.size()][3];
 			Rectangle selectedCanvas = null;
 			
 			for(SoundCanvasWrapper scw : currentCanvasList) {
 				final long longFrom = scw.getFrameFrom();
 				final long longTo = scw.getFrameTo();
 				final int from = margin + (int)(longFrom * samplesPerFrame / noOfSamples);
 				final int to = longTo == 0 ? w - margin : margin+  (int)(longTo * samplesPerFrame / noOfSamples);
 				Color c = (count % 2 == 0) ? canvasColor1  : canvasColor2;
 				
 				g.setColor(c);
 				int delta = 0;
 
 				int free = count;
 				for(int i = count - 1; i >= 0; i--) {
 					if((longFrom >= layers[i][1] && layers[i][1] !=0) || (longTo != 0 && longTo <= layers[i][0])) {
 						free = i;
 					}
 					else {		
 						free = (int)layers[i][2] + 1;
 						break;
 					}
 
 				}
 				delta = free * 4;// TODO
 				g.fillRoundRect(from, y + delta, to - from, b, 16, 16);
 				layers[count][0] = longFrom;
 				layers[count][1] = longTo;
 				layers[count][2] = free;
 				g.setColor(Color.GRAY);
 				g.drawRoundRect(from, y + delta, to - from, b, 16, 16);
 				if(scw.isSelected()) {
 					selectedCanvas = new Rectangle(from, y + delta, to - from, b);
 				}
 				
 				count++;
 			}
 			if(selectedCanvas != null) {
 				g.setColor(selectedCanvasColor);
 				g.fillRoundRect(selectedCanvas.x, selectedCanvas.y, selectedCanvas.width, selectedCanvas.height, 16, 16);
 			}
 			
 			int strLength = g.getFontMetrics().stringWidth("00:00");
 			int gap = strLength;
 			int numMarkersPossible = (w - margin *2 ) / (strLength + gap);
 			int totalSeconds = (int)(totalSampleLength / sampleRate);
 			int timeMod = ((int)(totalSeconds / numMarkersPossible / displaySecondsStep) + 1) * displaySecondsStep;
 			int accumSecTimeDrawn = -1;
 
 			for(int i = 0; i < w - margin; i ++) {
 				
 				if(i * noOfSamples / samplesPerFrame == selectFrame) {
 					g.setColor(selectFrameWidthColor);
 					g.drawLine(margin + i, 0, margin + i, h);
 				}
 				
 				int accumSec = i * noOfSamples / sampleRate ;
 				
 				if(accumSec % (timeMod) == 0 && accumSec > accumSecTimeDrawn) {
 					accumSecTimeDrawn = accumSec;
 					if(i + margin < w - margin) {
 						g.setColor(timeMarkerColor);
 						g.drawLine(margin + i, h - 20, margin + i, h - 12);
 						int minutes = accumSec / 60;
 						int seconds = accumSec % 60;
 						String minPref = minutes < 10 ? "0" : "";
 						String secPref = seconds < 10 ? "0" : "";
 						g.setColor(timeNumbersColor);
 						g.drawString(minPref + minutes + ":" + secPref + seconds, margin + i + 1, h - 4);
 					}
 				}
 			}
 			
 			int x = margin + 1;
 			g.setColor(Color.BLACK);
 
 			for(SuperSample s : superSampleData.getList()) {
 				g.drawLine(x, h /2 - s.getMax() / divY, x, h / 2 - s.getMin() / divY);
 				if(samplePosition >= pos && samplePosition <= pos + s.getNoOfSamples()) {
 					g.setColor(Color.WHITE);
 				}
 				x++;
 				pos += s.getNoOfSamples();
 			}
 			
 			g.setColor(selectPointerColor);
 			g.drawLine(selectPos, 0, selectPos, h);
 			g.drawString(String.valueOf(selectFrame), selectPos + 1, 16);
 		
 		}
 	}
 	
 	public void setInputOutputData() {
 		if(Context.getAudioInput() == null) {
 			return;
 		}
 		VideoOutputInfo videoOutputInfo = Context.getVideoOutputInfo();
 		AudioInputInfo audioInputInfo = Context.getAudioInput().getAudioInputInfo();
 		videoFrameRate = videoOutputInfo.getFramesPerSecond();
 		sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
 		samplesPerFrame = sampleRate / videoFrameRate; // e.g. 44100 / 25 = 1764
 		AudioInput audioInput = Context.getAudioInput();
 		totalSampleLength = audioInput.getAudioInputInfo().getFrameLength();
 		if(autoZoom) {
 			widthToUse = guiWidth;
 			noOfSamples = (int)(totalSampleLength / (widthToUse - margin*2)) + 1;
 		}
 		else {
 			noOfSamples = samplesToZoom;
 		}
 		if(autoZoom && superSampleDataAutoZoomed != null) {
 			superSampleData = superSampleDataAutoZoomed;
 		}
 		else if(!autoZoom && superSampleDataFrameZoomed != null && noOfSamples == noOfSamplesLoaded) {
 			superSampleData = superSampleDataFrameZoomed;
 		}
 		else {
 			// make the asynchronous sub sampling
 			SubSampleThread superSample = new SubSampleThread(audioInput, noOfSamples, new CallBack() {
 				@Override
 				public void finishedSampling(SuperSampleData superSampleData) {
 					TimeLinePanel.this.superSampleData = superSampleData;
 					divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (heightToUse - marginY * 2)) + 1;
 					if(autoZoom) {
 						superSampleDataAutoZoomed = superSampleData;
 					}
 					else {
 						superSampleDataFrameZoomed = superSampleData;
 					}
 
 					loading = false;
 					TimeLinePanel.this.revalidate();
 					TimeLinePanel.this.repaint();
 				}
 			});
 			loading = true;
 			noOfSamplesLoaded = noOfSamples;
 			
 			superSample.start();
 		}
 		if(autoZoom) {
 			setPreferredSize(new Dimension(600, heightToUse));
 			setSize(600, heightToUse);
 		}
 		else {
 			int widthRequired = (int)(totalSampleLength / (noOfSamples)) + margin * 2;
 			setPreferredSize(new Dimension(widthRequired, heightToUse));
 			setSize(widthRequired, heightToUse);
 			widthToUse = widthRequired;
 
 		}
 		selectPos = (int)(selectFrame * samplesPerFrame /  noOfSamples) + 1 + margin;
 		repaint();
 	}
 
 	/**
 	 * Set the current sample position
 	 * @param samplePosition
 	 */
 	public void setSamplePosition(long samplePosition) {
 		this.samplePosition = samplePosition;
 	}
 
 	@Override
 	public void frameSet() {
 		repaint();
 	}
 
 	@Override
 	public void rowSelected(boolean selected) {
 		repaint();
 	}
 
 	public void setAutoZoom(boolean autoZoom) {
 		this.autoZoom = autoZoom;
 	}
 
 	public void setSamplesToZoom(int samplesToZoom) {
 		this.samplesToZoom = samplesToZoom;
 	}
 
 	public void setGuiWidth(int guiWidth) {
 		this.guiWidth = guiWidth;
 	}
 
 	public void setPreRunFrames(int preRunFrames) {
 		this.preRunFrames = preRunFrames;
 		long f = selectFrame - preRunFrames;
 		if(f < 0) {
 			f = 0;
 		}
 		Context.setSongPositionPointer(f);
 	}
 
 	@Override
 	public long getFrameSelected() {
 		return selectFrame;
 	}
 
 	public int getSelectPos() {
 		return selectPos;
 	}
 	
 	
 
 }
