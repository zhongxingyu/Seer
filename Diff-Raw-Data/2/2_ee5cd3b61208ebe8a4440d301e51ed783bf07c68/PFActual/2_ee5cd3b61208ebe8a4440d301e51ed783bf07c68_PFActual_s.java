 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animi.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import org.animotron.animi.*;
 import org.animotron.animi.acts.Subtraction;
 import org.animotron.animi.cortex.*;
 
 /**
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  *
  */
 public class PFActual implements Imageable, InternalFrameListener {
 
 	static List<Field> cnFds = new ArrayList<Field>();
 	static List<Field> snFds = new ArrayList<Field>();
 	
 	int zoom = 5;
 
 	static {
 		Field[] fields = NeuronComplex.class.getFields();
 		for (int i = 0; i < fields.length; i++) {
 			Field field = fields[i];
 			if (field.isAnnotationPresent(RuntimeParam.class))
 				cnFds.add(field);
 		}
 
 		fields = NeuronSimple.class.getFields();
 		for (int i = 0; i < fields.length; i++) {
 			Field field = fields[i];
 			if (field.isAnnotationPresent(RuntimeParam.class))
 				snFds.add(field);
 		}
 	}
 	
 	CortexZoneComplex zone;
 	Point point;
 	NeuronComplex cn;
 	
 	public PFActual(Object[] objs) {
 		zone = (CortexZoneComplex) objs[0];
 		point = (Point) objs[1];
 		cn = zone.col[point.x][point.y];
 	}
 
 	@Override
 	public String getImageName() {
 		return "running params";
 	}
 
 	@Override
 	public BufferedImage getImage() {
 		
 		calcBoxSize();
 		
 		BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
         Graphics g = image.getGraphics();
         g.setColor(Color.WHITE);
 
 		int textY = g.getFontMetrics(g.getFont()).getHeight();
 		int x = 0, y = 0;
 		
 		y += textY;
 		g.drawString("column [ "+point.x+" : "+point.y+" ]", x, y);		
 
 		x = 0;
 		y += textY;
 
 		int rY = y;
 		
 		y += textY;
         g.drawString("RF", x, y);
         
 		BufferedImage img = drawRF();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		x = 0;
 		y += 2+img.getHeight()*zoom;
 
 		y += textY;
         g.drawString("Total RF", x, y);
 
 		img = drawTotalRF();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		
 		y = rY; x = boxSize*zoom + 2;
 		
 		y += textY;
         g.drawString("Original", x, y);
 
 		img = drawIn();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
				img.getScaledInstance(img.getWidth()*10, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		x = boxSize*zoom + 2;
 		y += 2+img.getHeight()*zoom;
 
 		y += textY;
         g.drawString("Minus", x, y);
 
 		img = drawMinus();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 
 		//next block
 		y = rY; x = 2*(boxSize*zoom + 2);
 		
 		y += textY;
         g.drawString("Inhibitory RF", x, y);
 
 		img = drawInhibitoryRF();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		x = 2*(boxSize*zoom + 2);
 		y += 2+img.getHeight()*zoom;
 
 		y += textY;
         g.drawString("Total inhibitory RF", x, y);
 
 		img = drawTotalInhibitoryRF();
 		g.drawRect(x, y, 2+(img.getWidth()*zoom), 2+(img.getHeight()*zoom));
 		g.drawImage(
 				img.getScaledInstance(img.getWidth()*zoom, img.getHeight()*zoom, Image.SCALE_AREA_AVERAGING),
 				x+1, y+1, null);
 
 		x = 0;
 		y += 2+img.getHeight()*zoom;
 
 		for (Field f : cnFds) {
 			y += textY;
 	        g.drawString(getName(f), x, y);		
 
 	        y += textY;
 	        g.drawString(getValue(f, cn), x, y);		
 		}
 //		
 //		for (int dx = -1; dx <= 1; dx++) {
 //			for (int dy = -1; dy <= 1; dy++) {
 //
 //		        x = 0;
 //				y += textY;
 //				g.drawString(""+dx+" : "+dy, x, y);
 //				
 //				for (Field f : snFds) {
 //					y += textY;
 //			        x = 0;
 //			        
 //					for (int z = 0; z < zone.deep; z++) {
 //						final NeuronSimple sn = zone.s[point.x+dx][point.y+dy][z];
 //
 //						String str = getValue(f, sn);
 //						if (str.length() > 3)
 //							str = str.substring(0, 3);
 //						
 //						g.setColor(sn.isOccupy() ? Color.WHITE : Color.YELLOW);
 //				        g.drawString(str, x, y);		
 //						x += 35;
 //				        g.setColor(Color.WHITE);
 //					}
 //			        g.drawString(getName(f), x, y);		
 //				}
 //		        x = 0;
 //				y += textY;
 //				for (int z = 0; z < zone.deep; z++) {
 //					final NeuronSimple sn = zone.s[point.x+dx][point.y+dy][z];
 //			        g.setColor(sn.isOccupy() ? Color.WHITE : Color.YELLOW);
 //					
 //			        Link lnk = null;
 //					for (Link l : sn.a_links) {
 //						if (l.axon == cn) {
 //							lnk = l;
 //							break;
 //						}
 //					}
 //					if (lnk != null) {
 //						String str = String.valueOf(lnk.w);
 //						if (str.length() > 3)
 //							str = str.substring(0, 3);
 //					
 //						g.drawString(str, x, y);
 //					}
 //					x += 35;
 //			        g.setColor(Color.WHITE);
 //				}
 //		        g.drawString("w to CN", x, y);		
 //			}
 //		}
 
 		return image;
 	}
 	
 	int boxSize = 0;
 
 	private void calcBoxSize() {
 		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
 		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
 
         for (Link cl : cn.s_links) {
             for (Link sl : cl.synapse.s_links) {
             	minX = Math.min(minX, sl.synapse.x);
             	minY = Math.min(minY, sl.synapse.y);
 
             	maxX = Math.max(maxX, sl.synapse.x);
             	maxY = Math.max(maxY, sl.synapse.y);
             }
         }
         boxSize = Math.max(maxX - minX, maxY - minY) + 2;
 	}
 
 	private BufferedImage drawRF() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
 		for (LinkQ link : cn.Qs.values()) {
         	pX = (boxSize / 2) + (link.synapse.x - cn.x);
 			pY = (boxSize / 2) + (link.synapse.y - cn.y);
                     	
 			if (       pX > 0 
         			&& pX < boxSize 
         			&& pY > 0 
         			&& pY < boxSize) {
 	                    	
             	int c = Utils.calcGrey(image, pX, pY);
 				c += 255 * link.q * 10; // * Q2
 				if (c > 255) c = 255;
 				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
         	}
         }
         return image;
 	}
 	
 //	private BufferedImage drawTotalRF() {
 //        BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 //
 //        int pX, pY;
 //        for (Link cl : cn.s_links) {
 //        	final NeuronSimple sn = (NeuronSimple) cl.synapse;
 //            for (Link sl : sn.s_links) {
 //            	pX = (boxSize / 2) + (sl.synapse.x - cl.axon.x);
 //				pY = (boxSize / 2) + (sl.synapse.y - cl.axon.y);
 //            	if (pX >= 0 && pX < boxSize 
 //            			&& pY >= 0 && pY < boxSize) {
 //                	
 //                	int c = Utils.calcGrey(image, pX, pY);
 //					c += 50;//255 * sl.w;
 //					if (c > 255) c = 255;
 //					image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 //                }
 //            }
 //        }
 //        return image;
 //	}
 
 	private BufferedImage drawTotalRF() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
         for (LinkQ link : cn.Qs.values()) {
         	pX = (boxSize / 2) + (link.synapse.x - cn.x);
 			pY = (boxSize / 2) + (link.synapse.y - cn.y);
         	if (pX >= 0 && pX < boxSize 
         			&& pY >= 0 && pY < boxSize) {
             	
             	int c = Utils.calcGrey(image, pX, pY);
 				c += 255;
 				if (c > 255) c = 255;
 				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
             }
         }
         return image;
 	}
 
 	private BufferedImage drawInhibitoryRF() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
 		for (Link link : cn.s_inhibitoryLinks) {
         	pX = (boxSize / 2) + (link.synapse.x - cn.x);
 			pY = (boxSize / 2) + (link.synapse.y - cn.y);
                     	
 			if (       pX > 0 
         			&& pX < boxSize 
         			&& pY > 0 
         			&& pY < boxSize) {
 	                    	
             	int c = Utils.calcGrey(image, pX, pY);
 				c += 255 * link.w;
 				if (c > 255) c = 255;
 				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
         	}
         }
         return image;
 	}
 
 	private BufferedImage drawTotalInhibitoryRF() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         int pX, pY;
 		for (Link link : cn.s_inhibitoryLinks) {
         	pX = (boxSize / 2) + (link.synapse.x - cn.x);
 			pY = (boxSize / 2) + (link.synapse.y - cn.y);
                     	
 			if (       pX > 0 
         			&& pX < boxSize 
         			&& pY > 0 
         			&& pY < boxSize) {
 	                    	
             	int c = Utils.calcGrey(image, pX, pY);
 				c += 255;
 				if (c > 255) c = 255;
 				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
         	}
         }
         return image;
 	}
 
 	private BufferedImage drawIn() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
 		int pX, pY = 0;
 		for (Link cl : cn.s_links) {
 			
         	final Neuron sn = cl.synapse;
 
 			for (Link sl : sn.s_links) {
 
             	pX = (boxSize / 2) + (sl.synapse.x - cn.x);
 				pY = (boxSize / 2) + (sl.synapse.y - cn.y);
             	
 				if (       pX >= 0 
             			&& pX < boxSize 
             			&& pY >= 0 
             			&& pY < boxSize) {
                 	
                 	int c = Utils.calcGrey(image, pX, pY);
 					c += 255 * sl.synapse.activity;
 					if (c > 255) c = 255;
 					image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
 				} else {
 					System.out.println("WRONG "+pX+" "+pY);
                 }
         	}
         }
         return image;
 	}
 
 	private BufferedImage drawMinus() {
         BufferedImage image = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
 
         NeuronComplex[][] ms = Subtraction.process(zone, cn.x, cn.y);
         
 		int pX, pY = 0;
 		for (LinkQ link : cn.Qs.values()) {
         	pX = (boxSize / 2) + (link.synapse.x - cn.x);
 			pY = (boxSize / 2) + (link.synapse.y - cn.y);
         	
 			if (       pX >= 0 
         			&& pX < boxSize 
         			&& pY >= 0 
         			&& pY < boxSize) {
             	
             	int c = Utils.calcGrey(image, pX, pY);
 				c += 255 * ms[link.synapse.x][link.synapse.y].posActivity;
 				if (c > 255) c = 255;
 				if (c < 0) c = 0;
 				image.setRGB(pX, pY, Utils.create_rgb(255, c, c, c));
         	}
         }
         return image;
 	}
 
 	private String getName(Field f) {
 		return f.getName();
 //		return f.getAnnotation(RuntimeParam.class).name();
 	}
 
 	private String getValue(Field f, Object obj) {
 		try {
 			return f.get(obj).toString();
 		} catch (Exception e) {
 		}
 		return "???";
 	}
 
 	@Override
 	public Object whatAt(Point point) {
 		return null;
 	}
 
 	@Override
 	public void focusGained(Point point) {
 	}
 
 	@Override
 	public void focusLost(Point point) {
 	}
 
 	@Override
 	public void closed(Point point) {
 	}
 
 	@Override
 	public void internalFrameOpened(InternalFrameEvent e) {
 		zone.getCRF().focusGained(point);
 	}
 
 	@Override
 	public void internalFrameClosing(InternalFrameEvent e) {
 		zone.getCRF().focusLost(point);
 	}
 
 	@Override
 	public void internalFrameClosed(InternalFrameEvent e) {
 		zone.getCRF().closed(point);
 	}
 
 	@Override
 	public void internalFrameIconified(InternalFrameEvent e) {
 	}
 
 	@Override
 	public void internalFrameDeiconified(InternalFrameEvent e) {
 	}
 
 	@Override
 	public void internalFrameActivated(InternalFrameEvent e) {
 		zone.getCRF().focusGained(point);
 	}
 
 	@Override
 	public void internalFrameDeactivated(InternalFrameEvent e) {
 		zone.getCRF().focusLost(point);
 	}
 }
