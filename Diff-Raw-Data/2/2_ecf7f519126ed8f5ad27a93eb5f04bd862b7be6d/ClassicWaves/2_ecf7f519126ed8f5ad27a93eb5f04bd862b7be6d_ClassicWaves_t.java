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
 
 import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
 import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
 import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
 
 
 /**
  * Classic audio wave representation
  * @author Michael Heinzelmann
  */
 public class ClassicWaves implements SoundCanvas {
 	
 	@UserProperty(description="color of the waves")
 	private Color foreGroundColor = Color.BLUE;
 	@UserProperty(description="whether to draw filled bottom")
 	private boolean fillBottom = false;
 	@UserProperty(description="whether to draw horizontal lines")
 	private boolean drawHorizontal = false;
 	
 	// parameters automatically set
 	private float amplitudeDivisor;
 	private float amplitudeMultiplicator;
 	private int leftMargin;
 	private int height;
 	private int width;
 	
 	private AmplitudeHelper amplitude;
 	private int factor;
 	private long samplecount;
 	int max;
 	
 	// state
 	private int counterInsideFrame;
 	private int[] amplitudes;
 	private int prevAmplitude;
 	
 	@Override
 	public void nextSample(int[] amplitudes) {
 
 		int mono = amplitude.getSignedMono(amplitudes);
 		int amp = amplitudeDivisor > 1 ? (int)(mono / amplitudeDivisor) : (int)(mono * amplitudeMultiplicator);
 		if(factor == 1 || Math.abs(amp) > Math.abs(max)) {
 			max = amp;
 		}
 		
 		if(samplecount % factor == 0) {
 			this.amplitudes[counterInsideFrame] = max;
 			counterInsideFrame++;
 			max = 0;
 		}
 		samplecount++;
 
 	}
 
 	@Override
 	public void newFrame(long frameCount, Graphics2D graphics) {	
 		counterInsideFrame = 0;
 		graphics.setColor(foreGroundColor);
 		int x = 1;
 		for(int amp : amplitudes) {
 			if(drawHorizontal) {
 				graphics.drawLine(0, height / 2 - amp , width, height / 2 - amp);
 			}
 			else {
 			int y2 = fillBottom ? height : height / 2 - prevAmplitude;
 			graphics.drawLine(leftMargin + x, height / 2 - amp , leftMargin + x, y2);
 			prevAmplitude = amp;
 			}
 			x++;
 		}
 	}
 
 	@Override
 	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
 		int frameRate = videoOutputInfo.getFramesPerSecond();
 		float sampleRate = audioInputInfo.getAudioFormat().getSampleRate(); 
 		int pixelLengthOfaFrame = (int)Math.ceil(sampleRate / (float)frameRate); // e.g. 44100 / 25 = 1764
 		factor = (int)(pixelLengthOfaFrame / videoOutputInfo.getWidth()) + 1;
		int pixelsUsed = (int)Math.ceil((float)pixelLengthOfaFrame / (float)factor);
 		amplitudes = new int[pixelsUsed];
 		//System.err.println(pixelsUsed + " used factor " + factor);
 		leftMargin =  (videoOutputInfo.getWidth() - pixelsUsed) / 2;
 		this.height = videoOutputInfo.getHeight();
 		this.width = videoOutputInfo.getWidth();
 		counterInsideFrame = 0;
 		amplitude = new AmplitudeHelper(audioInputInfo);
 		amplitudeDivisor = (amplitude.getAmplitudeRange() / height);
 		if(amplitudeDivisor < 1){
 			amplitudeMultiplicator = height / amplitude.getAmplitudeRange();
 		}
 	}
 
 	@Override
 	public int getPreRunFrames() {
 		// exactly 1 frame
 		return 1;
 	}
 
 	@Override
 	public void postFrame() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 }
