 package com.maton.tools.crusher;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.maton.tools.crusher.model.BNode;
 import com.maton.tools.crusher.model.NodeSize;
 import com.maton.tools.crusher.model.PositionedObject;
 import com.maton.tools.crusher.model.SmartBNode;
 
 public class Crusher {
 
 	protected ArrayList<IPart> parts;
 	protected int totalArea;
 	protected int sizeIdx;
 	protected List<PositionedObject<IPart>> pparts;
 	protected int size;
 
 	public Crusher() {
 		parts = new ArrayList<IPart>();
 		totalArea = 0;
 		sizeIdx = 0;
 	}
 
 	public void addPart(IPart part) {
 		parts.add(part);
 		totalArea += part.getArea();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void run() {
 		Collections.sort(parts);
 
 		int i = (int) Math.sqrt(totalArea) / 4;
 		if (i == 0) {
 			i = 1;
 		}
 		while (i > 0) {
 			size = i * 4;
 			int area = size * size;
 			if (area >= totalArea) {
 				pparts = distribute(size);
 				if (pparts.size() == parts.size()) {
 					return;
 				}
 			}
 			i++;
 		}
 
 		throw new RuntimeException("Not enough space for the total area of "
 				+ totalArea + " pixels");
 	}
 
 	protected List<PositionedObject<IPart>> distribute(int width) {
 		SmartBNode<IPart> sbnode = new SmartBNode<IPart>(width, width, true);
 
 		for (int i = 0; i < parts.size(); i++) {
 			IPart part = parts.get(i);
 			sbnode.append(part, new NodeSize(part.getWidth(), part.getHeight()));
 		}
 
 		BNode<IPart> wbnode = sbnode.getWinningBNode();
 		ArrayList<PositionedObject<IPart>> pparts = wbnode.getObjectPositions();
 
 		return pparts;
 	}
 
 	public ArrayList<IPart> getParts() {
 		return parts;
 	}
 
 	public int getTotalArea() {
 		return totalArea;
 	}
 
 	public int getSizeIdx() {
 		return sizeIdx;
 	}
 
 	public List<PositionedObject<IPart>> getPParts() {
 		return pparts;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public Image buildImage() {
 		BufferedImage img = new BufferedImage(size, size,
 				BufferedImage.TYPE_4BYTE_ABGR_PRE);
 		Graphics2D g = img.createGraphics();
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		for (PositionedObject<IPart> pos : pparts) {
 			IPart part = pos.getObject();
 
 			g.drawImage(part.getImage(), pos.getX() + 1, pos.getY() + 1, null);
 			
 			part.setOutputX(pos.getX() + 1);
 			part.setOutputY(pos.getY() + 1);
 		}
 
 		g.dispose();
 
 		return img;
 	}
 
 }
