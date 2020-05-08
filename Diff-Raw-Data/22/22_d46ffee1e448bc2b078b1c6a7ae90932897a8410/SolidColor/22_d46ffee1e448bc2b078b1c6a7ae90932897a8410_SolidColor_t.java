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
 import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
 import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
 import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
 import org.mcuosmipcuter.orcc.api.util.ColorHelper;
 
 /**
  * Displays a solid color
  * @author Michael Heinzelmann
  */
 public class SolidColor implements SoundCanvas {
 	
 	@UserProperty(description="color of the area")
 	private Color color = Color.WHITE;
 	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
 	@UserProperty(description="alpha of the color")
 	int alpha = 255;
 	@LimitedIntProperty(description="frequency cannot be below 0", minimum=0)
	@UserProperty(description="repaint every <frameFrequency> frame")
	int repaintFrameFrequency = 1;
 	@LimitedIntProperty(description="threshold must be between 0 and 100", minimum=0, maximum = 100)
	@UserProperty(description="amplitudes above this threshold will trigger a repaint, value in percent of maximum amplitude")
	private int repaintThreshold;
 	
 	private ColorHelper colorHelper = new ColorHelper(alpha);
 	protected AmplitudeHelper amplitudeHelper;
 	
 	private int width;
 	private int height;
 	
 	private boolean thresholdExceeded;
 
 	@Override
 	public void nextSample(int[] amplitudes) {
		if(repaintThreshold > 0) {
 			int mono = amplitudeHelper.getSignedMono(amplitudes);
 			int percent = amplitudeHelper.getSignedPercent(Math.abs(mono));	
			if(percent > repaintThreshold) {
 				thresholdExceeded = true;
 			}
 		}
 	}
 
 	@Override
 	public void newFrame(long frameCount, Graphics2D graphics2D) {
		if(	frameCount == 1 ||
			(repaintThreshold == 0 && repaintFrameFrequency == 1) || 
			(repaintFrameFrequency > 0 && frameCount % repaintFrameFrequency == 0) || 
			repaintThreshold > 0 && thresholdExceeded) {
			
 			colorHelper.setColorWithAlpha(alpha, color, graphics2D);
 			graphics2D.fillRect(0, 0, width, height);
 		}
 	}
 
 	@Override
 	public void prepare(AudioInputInfo audioInputInfo,
 			VideoOutputInfo videoOutputInfo) {
 		width = videoOutputInfo.getWidth();
 		height = videoOutputInfo.getHeight();
 		amplitudeHelper = new AmplitudeHelper(audioInputInfo);
 	}
 
 	@Override
 	public int getPreRunFrames() {
 		// this canvas just fills a color area, no pre run needed
 		return 0;
 	}
 
 	@Override
 	public void postFrame() {
 		thresholdExceeded = false;
 	}
 
 	@Override
 	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
 		graphics.setColor(color);
 		graphics.fillRect(0, 0, width, height);
 	}
 
 
 }
