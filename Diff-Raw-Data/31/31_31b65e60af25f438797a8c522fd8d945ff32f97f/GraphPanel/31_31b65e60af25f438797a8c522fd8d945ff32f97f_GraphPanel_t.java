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
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.Point;
 import java.awt.RadialGradientPaint;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
 import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
 import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
 import org.mcuosmipcuter.orcc.soundvis.Mixin;
 import org.mcuosmipcuter.orcc.soundvis.Renderer;
 
 
 /**
  * The central panel where we draw the video in play as well as export mode.
  * This is perfect for the GUI but probably needs refactoring if we want to
  * support head less rendering.
  * @author Michael Heinzelmann
  */
 public class GraphPanel extends JPanel implements Renderer, CanvasBackGround {
 	
 	/**
 	 * Type of background to use
 	 * @author Michael Heinzelmann
 	 */
 	public enum BGImageType {
 		/**
 		 * Solid color back ground 
 		 */
 		COLOR, 
 		/**
 		 * User selected image as back ground
 		 */
 		IMAGE, 
 		/**
 		 * No background, this means all graphics add up
 		 */
 		NONE
 	}
 
 	private BGImageType bgImageType = BGImageType.COLOR;
 	private static final long serialVersionUID = 1L;
 	private Mixin mixin;
 	private SoundCanvas soundCanvas; // canvas to work with
 	
 	private BufferedImage frameImage;
 	private Graphics2D graphics;
 	private VideoOutputInfo videoOutputInfo;
 	
 	private float zoomFactor = 0.5f;
 	private boolean useWaterMark;
 	private String watermarkText;
 	
 	private BufferedImage bgImage;
 	private Color bgColor = Color.WHITE;
 	private int bgX;
 	private int bgY;
 	private int xMoveStart;
 	private int yMoveStart;
 	
 	/**
 	 * Constructor, adds the mouse drag handling for the back ground image
 	 */
 	public GraphPanel() {
 		
 		setBackground(Color.LIGHT_GRAY);
 		drawDefaultBackGround();
 		
 		addMouseMotionListener(new MouseAdapter() {
 			
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				
 				if(bgImage != null) {
 					bgX += e.getX() - xMoveStart;
 					bgY += e.getY() - yMoveStart;
 					graphics.setColor(Color.GRAY);
 					int width = Context.getVideoOutputInfo().getWidth();
 					int height = Context.getVideoOutputInfo().getHeight();
 					graphics.fillRect(0, 0, width, height);
 					graphics.drawImage(bgImage, bgX, bgY, Color.BLACK, null);
 					repaint();
 					xMoveStart = e.getX();
 					yMoveStart = e.getY();
 				}
 			}
 		});
 		addMouseListener(new MouseAdapter() {
 
 			Cursor cursor;
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if(bgImage != null) {
 				xMoveStart = e.getX();
 				yMoveStart = e.getY();
 				cursor = getCursor();
 				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
 				}
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 
 				if( cursor != null) {
 					setCursor(cursor);
 				}
 			}
 			
 		});
		Context.addListener(new Listener() {
			@Override
			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.Watermark.equals(propertyName)) {
					GraphPanel.this.watermarkText = Context.getVideoOutputInfo().getWaterMarkText();
				}
			}
		});
 	}
 	
 	// our 'logo'
 	private void drawDefaultBackGround() {
 		int width = Context.getVideoOutputInfo().getWidth();
 		int height = Context.getVideoOutputInfo().getHeight();
 		
 		frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
 		graphics = frameImage.createGraphics();
 		graphics.setColor(bgColor);
 		graphics.setPaint(new RadialGradientPaint(new Point(0, 0), height, new float[] {0.0f, 0.5f}, new Color[] {Color.WHITE, Color.GRAY}, CycleMethod.REFLECT));
 		graphics.fillRect(0, 0, width, height);
 	}
 	
 	@Override
 	public void drawBackGround() {
 		if(bgImageType == BGImageType.IMAGE && bgImage != null) {
 			graphics.drawImage(bgImage, bgX, bgY, Color.BLACK, null);
 		}
 		else if(bgImageType == BGImageType.COLOR) {
 			graphics.setColor(bgColor);
 			graphics.fillRect(0, 0, videoOutputInfo.getWidth(), videoOutputInfo.getHeight());
 		}
 		// else nothing, canvas is responsible for the background
 		
 	}
 	/**
 	 * Displays the preview for the canvas set in the context 
 	 */
 	public void preView() {
 		drawDefaultBackGround();
 		VideoOutputInfo voi = Context.getVideoOutputInfo();
 		Context.getSoundCanvas().preView(voi.getWidth(), voi.getHeight(), graphics);
 		repaint();
 	}
 
 	/**
 	 * Sets the canvas in the context to this context and prepares it 
 	 * @see SoundCanvas#prepare(AudioInputInfo, VideoOutputInfo, Graphics2D, CanvasBackGround)
 	 */
 	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
 		this.videoOutputInfo = videoOutputInfo;
 		frameImage = new BufferedImage(videoOutputInfo.getWidth(), videoOutputInfo.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
 		graphics = frameImage.createGraphics();
 		soundCanvas = Context.getSoundCanvas();
 
 		soundCanvas.prepare(audioInputInfo, videoOutputInfo, graphics, this);
 
 		watermarkText = videoOutputInfo.getWaterMarkText();
 		mixin.start(audioInputInfo, videoOutputInfo);
 	}
 
 	/**
 	 * Draws the frame image, both still images preview as well as video animated
 	 */
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if(zoomFactor != 1) {
 			((Graphics2D)g).scale(zoomFactor, zoomFactor);
 		}
 		g.drawImage(frameImage, 0, 0, Color.BLACK, null);
 		if(zoomFactor != 1) {
 			((Graphics2D)g).scale(1, 1);
 		}
 
 	}
 
 	@Override
 	public boolean nextSample(int[] amplitudes, byte[] rawData) {
 		soundCanvas.nextSample(amplitudes);
 		if(mixin != null) {
 			mixin.nextSample(amplitudes, rawData);
 		}
 		return true;
 	}
 
 	@Override
 	public void newFrame(long frameCount) {
 		
 		soundCanvas.newFrame(frameCount);
 		if(useWaterMark) {
 			//watermark.newFrame(frameCount);
 			drawWatermark(graphics);
 		}
 		this.repaint();
 		
 		if(mixin != null) {
 			mixin.newFrame(frameCount);
 		}
 
 	}
 	/**
 	 * Draws the automatic watermark using black and white XOR
 	 * @param g the graphics to draw on
 	 */
 	public void drawWatermark(Graphics2D g) {
 		if(watermarkText == null || watermarkText.length() == 0) {
 			return;
 		}
 		 float fontSize = 32f;
 		Font f = g.getFont().deriveFont(fontSize);
 		g.setFont(f);
 		int l = g.getFontMetrics().getLeading();
 		int d = g.getFontMetrics().getDescent();
 		g.setXORMode(Color.BLACK);
 		g.setColor(Color.WHITE);
 		int len = g.getFontMetrics().stringWidth(watermarkText);
 		int width = Context.getVideoOutputInfo().getWidth();
 		int height = Context.getVideoOutputInfo().getHeight();
 		g.drawString(watermarkText, (width - len) / 2, height - l - d);
 		g.setPaintMode();
 	}
 
 	public BufferedImage getFrameImage() {
 		return frameImage;
 	}
 
 	public void setMixin(Mixin mixin) {
 		this.mixin = mixin;
 	}
 
 	public SoundCanvas getSoundCanvas() {
 		return soundCanvas;
 	}
 
 	public synchronized float getZoomFactor() {
 		return zoomFactor;
 	}
 
 	public synchronized void setZoomFactor(float zoomFactor) {
 		this.zoomFactor = zoomFactor;
 	}
 
 	public synchronized boolean isUseWaterMark() {
 		return useWaterMark;
 	}
 
 	public synchronized void setUseWaterMark(boolean useWaterMark) {
 		this.useWaterMark = useWaterMark;
 	}
 
 	public synchronized BGImageType getBgImageType() {
 		return bgImageType;
 	}
 
 	public synchronized void setBgImageType(BGImageType bgImageType) {
 		this.bgImageType = bgImageType;
 	}
 
 	public synchronized BufferedImage getBgImage() {
 		return bgImage;
 	}
 
 	public synchronized void setBgImage(BufferedImage bgImage) {
 		this.bgImage = bgImage;
 		graphics.drawImage(bgImage, bgX, bgY, Color.BLACK, null);
 		repaint();
 	}
 
 	public synchronized Color getBgColor() {
 		return bgColor;
 	}
 
 	public synchronized void setBgColor(Color bgGroundColor) {
 		this.bgColor = bgGroundColor;
 	}
 
 }
