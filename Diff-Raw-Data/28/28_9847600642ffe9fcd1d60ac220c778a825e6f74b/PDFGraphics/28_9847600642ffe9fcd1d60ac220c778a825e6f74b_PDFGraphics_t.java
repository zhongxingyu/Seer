 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.runner;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.widgets.Display;
 
 import com.lowagie.text.BadElementException;
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.MarkupAttributes;
 import com.lowagie.text.pdf.BaseFont;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfWriter;
 
 /**
  * This class is used to render draw2d figures into a PDF file.
  * <p>
  * Ultimately, this class belongs in its own package; it has more to
  * do with GEF draw2d than Slideshow. Once we have evolved this class
  * to a point where it has enough functionality as to be more generally
  * useful, we will move it.
  */
 public class PDFGraphics extends Graphics {
 
 	private final Document document;
 
 	private PdfWriter writer;
 	private PdfContentByte contentByte;
 	private Stack<State> stateStack = new Stack<State>();
 
 	class State implements Cloneable {
 		private int dx;
 		private int dy;
 		public String fontFamily;
 		public int fontStyle;
 		public int fontHeight;
 		public float lineWidth = 1.0f;
 		public java.awt.Color foregroundColor = new java.awt.Color(0, 0, 0);
 		public java.awt.Color backgroundColor = new java.awt.Color(0xFF, 0xFF, 0xFF);
 		
 		protected State cloneState() {
 			try {
 				return (State) clone();
 			} catch (CloneNotSupportedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	public PDFGraphics(Document document, PdfWriter writer) throws DocumentException {
 		this.document = document; 
 		this.writer = writer;
 		contentByte = writer.getDirectContent();
 		//contentByte.concatCTM(1f, 0f, 0f, -1f, 0f, document.getPageSize().height());
 		//contentByte.concatCTM(0.5f, 0f, 0f, 0.5f, 0f, 200f); // Scale and shift up
 		stateStack.push(new State());
 	}
 
 	@Override
 	public void clipRect(Rectangle r) {
 		notImplemented("clipRect");
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 
 	@Override
 	public void drawArc(int x, int y, int w, int h, int offset, int length) {
 		notImplemented("drawArc");
 	}
 
 	@Override
 	public void drawFocus(int x, int y, int w, int h) {
 		notImplemented("drawFocus");
 	}
 
 	@Override
 	public void drawImage(Image srcImage, int x, int y) {
 		org.eclipse.swt.graphics.Rectangle bounds = srcImage.getBounds();
 		int width = bounds.width;
 		int height = bounds.height;
 		drawImage(srcImage, 0, 0, width, height, x, y, width, height);
 	}
 
 	@Override
 	public void drawImage(Image srcImage, int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
 		File file = null;
 		FileOutputStream out = null;
 		try {
 			file = File.createTempFile("pdf", ".png");
 			out = new FileOutputStream(file);
 			ImageLoader loader = new ImageLoader();
 			loader.data = new ImageData[] {srcImage.getImageData()};
 			loader.save(out, SWT.IMAGE_PNG);	
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} finally {
 			try {
 				out.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}		
 		
 		try {
 			com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(file.getAbsolutePath());
 			contentByte.addImage(image, w2,0,0,h2, x2+getState().dx, document.getPageSize().height() - (y2+getState().dy + h2), true);
 		} catch (BadElementException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void drawLine(int x1, int y1, int x2, int y2) {
 		notImplemented("drawLine");
 	}
 
 	@Override
 	public void drawOval(int x, int y, int w, int h) {
 		notImplemented("drawOval");
 	}
 
 	@Override
 	public void drawPolygon(PointList points) {
 		notImplemented("drawPolygon");
 	}
 
 	@Override
 	public void drawPolyline(PointList points) {
 		notImplemented("drawPolyList");
 	}
 
 	@Override
 	public void drawRectangle(int x, int y, int width, int height) {
 		contentByte.setLineWidth(getState().lineWidth);
 		contentByte.rectangle(x+getState().dx, document.getPageSize().height()-(y+getState().dy), width, -height);
 		contentByte.stroke();
 	}
 
 	@Override
 	public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
 		notImplemented("drawRoundRectangle");
 	}
 
 	@Override
 	public void drawString(String s, int x, int y) {
 		drawText(s, x, y);
 	}
 
 	@Override
 	public void drawText(String string, int x, int y) {
 		float fontHeight = getState().fontHeight * 4 / 3;
 		com.lowagie.text.Font font = FontFactory.getFont(getState().fontFamily, fontHeight, getState().fontStyle);
 		BaseFont bf = font.getBaseFont();
 		
 		contentByte.beginText();
 		contentByte.setColorFill(getState().foregroundColor);
 		contentByte.setFontAndSize(bf, fontHeight);
 		contentByte.setTextMatrix(x+getState().dx, document.getPageSize().height() - (y+getState().dy));
 		contentByte.showText(string);
 		contentByte.endText();
 	}
 
 	@Override
 	public void fillArc(int x, int y, int w, int h, int offset, int length) {
 		notImplemented("fillArc");
 	}
 
 	@Override
 	public void fillGradient(int x, int y, int w, int h, boolean vertical) {
 		notImplemented("fillGradient");
 	}
 
 	@Override
 	public void fillOval(int x, int y, int w, int h) {
 		notImplemented("fillOval");
 	}
 
 	@Override
 	public void fillPolygon(PointList points) {
 		notImplemented("fillPolygon");
 	}
 
 	@Override
 	public void fillRectangle(int x, int y, int width, int height) {
 		notImplemented("fillRectangle");
 	}
 
 	@Override
 	public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
 		notImplemented("fillRoundRectangle");
 	}
 
 	@Override
 	public void fillString(String s, int x, int y) {
 		drawText(s, x, y);
 	}
 
 	@Override
 	public void fillText(String s, int x, int y) {
 		drawText(s, x, y);
 	}
 
 	@Override
 	public Rectangle getClip(Rectangle rect) {
 		com.lowagie.text.Rectangle pageSize = document.getPageSize();
 		rect.x = (int) pageSize.left();
 		rect.y = 0; //(int) pageSize.top();
 		rect.width = (int) pageSize.width();
 		rect.height = (int) pageSize.height();		
 		
 		return rect;
 	}
 
 	@Override
 	public Font getFont() {
 		return Display.getCurrent().getSystemFont(); // FIXME
 	}
 
 	@Override
 	public FontMetrics getFontMetrics() {
 		GC gc = new GC(Display.getCurrent());
 		gc.setFont(getFont());
 		FontMetrics fontMetrics = gc.getFontMetrics();
 		gc.dispose();
 		return fontMetrics;
 	}
 
 	@Override
 	public Color getForegroundColor() {
 		notImplemented("getForegroundColor");
 		return null;
 	}
 	
 	@Override
 	public Color getBackgroundColor() {
 		notImplemented("getBackgroundColor");
 		return null;
 	}
 
 	@Override
 	public int getLineStyle() {
 		notImplemented("getLineStyle");
 		return 0;
 	}
 
 	@Override
 	public int getLineWidth() {
 		notImplemented("getLineWidth");
 		return 0;
 	}
 
 	@Override
 	public float getLineWidthFloat() {
 		return getState().lineWidth ;
 	}
 
 	@Override
 	public boolean getXORMode() {
 		notImplemented("getXORMode");
 		return false;
 	}
 
 	@Override
 	public void popState() {
 		contentByte.restoreState();
 		stateStack.pop();
 	}
 
 	@Override
 	public void pushState() {
 		contentByte.saveState();
 		stateStack.push((State) getState().cloneState());
 	}
 
 	@Override
 	public void restoreState() {
 		/*
 		 * Since we configure our state with each graphics operation,
 		 * there's nothing to do here.
 		 */
 	}
 
 	@Override
 	public void scale(double amount) {
 		contentByte.concatCTM(0.5f, 0f, 0f, 0.5f, 0f, 0f);
 	}
 
 	@Override
 	public void setClip(Rectangle r) {
 		notImplemented("setClip");
 	}
 
 	@Override
 	public void setFont(Font f) {
 		FontData fontData = f.getFontData()[0];
 		getState().fontFamily = fontData.getName();
 		int style = fontData.getStyle();
 		getState().fontStyle = 0;
 		if ((style & SWT.NORMAL) > 0) getState().fontStyle |= com.lowagie.text.Font.NORMAL;
 		if ((style & SWT.BOLD) > 0) getState().fontStyle |= com.lowagie.text.Font.BOLD;
 		if ((style & SWT.ITALIC) > 0) getState().fontStyle |= com.lowagie.text.Font.ITALIC;
 		getState().fontHeight = fontData.getHeight();
 	}
 
 	@Override
 	public void setForegroundColor(Color rgb) {
 		getState().foregroundColor = new java.awt.Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
 	}
 
 	@Override
 	public void setBackgroundColor(Color rgb) {
 		getState().backgroundColor = new java.awt.Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
 	}
 
 	@Override
 	public void setLineMiterLimit(float miterLimit) {
 		notImplemented("setLineMiterLimit");
 	}
 
 	@Override
 	public void setLineStyle(int style) {
 		 notImplemented("setLineStyle");
 	}
 
 	@Override
 	public void setLineWidth(int width) {
 		setLineWidthFloat(width);
 	}
 
 	@Override
 	public void setLineWidthFloat(float width) {
 		getState().lineWidth = width;
 	}
 
 	@Override
 	public void setXORMode(boolean b) {
 		notImplemented("setXORMode");
 	}
 
 	@Override
 	public void translate(int dx, int dy) {
 		getState().dx += dx;
 		getState().dy += dy;
 	}
 
 	private State getState() {
 		return stateStack.peek();
 	}
 
 	/**
 	 * This field is used to keep track of the names of any
 	 * methods that an attempt has been made to use despite
 	 * the fact that the method has not yet been implemented.
 	 * 
 	 * @see #notImplemented(String)
 	 */
 	Set<String> notImplemented = new HashSet<String>();
 	
 	/**
 	 * This method logs the fact that an attempt has been made
 	 * to use a particular method but the method has not yet been
 	 * implemented. Note that each instance of the class will
 	 * only log the missing method once. Note further that
 	 * this functionality is temporary; it will be removed when
 	 * the required functionality has been implemented.
 	 * 
 	 * @param name the name of the method. Must not be <code>null</code>.
 	 */
 	private void notImplemented(String name) {
 		if (notImplemented.contains(name)) return;
 		notImplemented.add(name);
 		
 		String message =  "PDF Graphics does not implement: " + name;
 		if (Activator.getDefault() != null) {
 			if (Activator.getDefault().getLog() == null) {
 				Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message));
 				return;
 			}
 		}
 	}
 }
