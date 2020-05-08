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
 package org.mcuosmipcuter.orcc.soundvis.defaultcanvas;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
 import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
 import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
 
 /**
  * @author Michael Heinzelmann
  *
  */
 public class RotatingAmplitudes implements SoundCanvas {
 	
 	@UserProperty(description="degrees per frame speed")
 	private int degreesPerFrame = 30;
 	@UserProperty(description="foreground color")
 	private Color foreGround = Color.BLACK;
 	@UserProperty(description="whether to draw xor")
 	private boolean xor = false;
 	@UserProperty(description="frames to keep")
 	int size = 360;
 	@UserProperty(description="x distance from center")
 	int shiftX = 0;
 	@UserProperty(description="y distance from center")
 	int shiftY = 0;
 	
 	private int centerX;
 	private int centerY;
 
 	private float amplitudeDivisor;
 	private float amplitudeMultiplicator;
 	private AmplitudeHelper amplitude;	
 	long degrees;
 	Deque<Point> deque = new ArrayDeque<Point>();
 	int max;
 	int samplesPerFrame;
 	long sampleCount;
 	
 	/* (non-Javadoc)
 	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
 	 */
 	@Override
 	public void nextSample(int[] amplitudes) {
 
 		int mono = amplitude.getUnSignedMono(amplitudes);
 		int amp = amplitudeDivisor > 1 ? (int)(mono / amplitudeDivisor) : (int)(mono * amplitudeMultiplicator);
 		if(amp > max) {
 			max = amp;
 		}
 		if(degreesPerFrame == 0) {
 			return; // do nothing
 		}
 		int modul =  samplesPerFrame / degreesPerFrame;
 		
 		if(modul == 0 || sampleCount % modul == 0) {
 			int x = centerX  + (int)(max/2 * Math.cos(degrees * (Math.PI / 180)));
 			int y = centerY + (int)(max/2 * Math.sin(degrees * (Math.PI / 180)));
 			
 			if(deque.size() == size) {
 				deque.removeFirst();
 			}
 			if(deque.size() > size) {
 				while(deque.size() >= size) {
 					deque.removeFirst();
 				}
 			}
 			deque.addLast(new Point(x, y));
 			max = 0;
 			if(degreesPerFrame > 0) {
 				this.degrees++;
 			}
 			else {
 				this.degrees--;
 			}
 			
 		}
 		sampleCount++;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
 	 */
 	@Override
 	public void newFrame(long frameCount, Graphics2D graphics2D) {
 		
 		if(xor) {
 			graphics2D.setXORMode(new Color(255 - foreGround.getRed(), 255 - foreGround.getGreen(), 255 - foreGround.getBlue()) );
 		}
 		graphics2D.setColor(foreGround);
 		for(Point p : deque) {
 			graphics2D.drawLine(centerX + shiftX, centerY + shiftY, p.x + shiftX, p.y + shiftY);
 		}
 		if(xor) {
 			graphics2D.setPaintMode();
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
 	 */
 	@Override
 	public void prepare(AudioInputInfo audioInputInfo,
 			VideoOutputInfo videoOutputInfo) {
 		int frameRate = videoOutputInfo.getFramesPerSecond();
 		int sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
 		samplesPerFrame = sampleRate / frameRate; // e.g. 44100 / 25 = 1764
 		centerX = videoOutputInfo.getWidth() / 2;
 		centerY = videoOutputInfo.getHeight() / 2;
 
 		amplitude = new AmplitudeHelper(audioInputInfo);
 		amplitudeDivisor = (amplitude.getAmplitudeRange() / videoOutputInfo.getHeight());
 		if(amplitudeDivisor < 1){
 			amplitudeMultiplicator = videoOutputInfo.getHeight() / amplitude.getAmplitudeRange();
 		}
 		degrees = 0;
 		deque.clear();
 	}
 
 	@Override
 	public int getPreRunFrames() {
 		// depends on the amount of history we are keeping, a big size and a slow degree speed need a big pre-run
		return degreesPerFrame != 0 ? size / degreesPerFrame : 0;
 	}
 
 	@Override
 	public void postFrame() {
 		
 	}
 
 	@Override
 	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
