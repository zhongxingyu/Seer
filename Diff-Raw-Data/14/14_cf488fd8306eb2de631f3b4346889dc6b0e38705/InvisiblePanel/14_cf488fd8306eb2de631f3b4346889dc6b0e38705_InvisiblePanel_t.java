 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JPanel;
 
 import network.SendBuffer;
 import tools.ClientToolProperties;
 import utils.ImageWrapper;
 import utils.Protocol;
 
 public class InvisiblePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
 	private SendBuffer buffer;
 	private ImageWrapper bufImage;
 	
 	// Canvas size
 	public static final int SIZE_X = 900;
 	public static final int SIZE_Y = 780;
 
 	// Mouse pointer coordinates
 	private int textStartX = -90000;
 	private int previousX = -90000;
 	private int previousY = -90000;
 	
 	private int colorType = Protocol.BRUSH_COLOR_1;
 	
 	// Information about currently selected tool
 	private ClientToolProperties tp;
 	private PaintPanel paintPanel;
 
 	/**
 	 * A "canvas" used to draw on.
 	 * @param paintpanel 
 	 */
 	public InvisiblePanel(SendBuffer buffer, ClientToolProperties tp, PaintPanel paintpanel) {
 		super();
 		this.paintPanel = paintpanel;
 		this.setPreferredSize(new Dimension(SIZE_X, SIZE_Y));
 		this.setOpaque(false);
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.addKeyListener(this);
 		this.setFocusable(true);
 		this.tp = tp;
 		this.buffer = buffer;
 
 		// Initializing bufImage
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		repaint();
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g; // downcast to Graphics2D
 
 		g2.drawImage(bufImage, null, 0, 0);
 	}
 	
 	private void drawLine(int previousX, int previousY, int currentX, int currentY, int rgb, int width) {
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		bufImage.drawLine(previousX, previousY, currentX, currentY, rgb, width);
 		repaint();
 	}
 
 	private void drawRectangle(int previousX, int previousY, int currentX, int currentY, int rgb) {
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		bufImage.drawRectangle(previousX, previousY, currentX, currentY, rgb);
 		repaint();
 	}
 
 	private void drawEllipse(int previousX, int previousY, int currentX, int currentY, int rgb) {
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		bufImage.drawEllipse(previousX, previousY, currentX, currentY, rgb);
 		repaint();
 	}
 
 	private void drawDotWithBorder(int previousX, int previousY, int currentX, int currentY, int rgb, int borderRgb, int width) {
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		bufImage.drawLine(previousX, previousY, currentX, currentY, borderRgb, width);
 		bufImage.drawLine(previousX, previousY, currentX, currentY, rgb, width-2);
 		repaint();
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawPen(int previousX, int previousY, int currentX, int currentY) {
 		String send = Protocol.DRAW_PEN + " " + 
 					  previousX + " " + 
 					  previousY + " " + 
 					  currentX + " " + 
 					  currentY + " " +
 					  colorType;
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawLine(int previousX, int previousY, int currentX, int currentY) {
 		String send = Protocol.DRAW_LINE + " " + 
 					  previousX + " " + 
 					  previousY + " " + 
 					  currentX + " " + 
 					  currentY + " " +
 					  colorType;
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendErase(int previousX, int previousY, int currentX, int currentY) {
 		String send = Protocol.ERASE + " " + 
 					  previousX + " " + 
 					  previousY + " " + 
 					  currentX + " " + 
 					  currentY;
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawRectangle(int previousX, int previousY, int currentX, int currentY) {
 		String send = Protocol.DRAW_RECTANGLE + " " + 
 					  previousX + " " + 
 					  previousY + " " + 
 					  currentX + " " + 
 					  currentY + " " +
 					  colorType;
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawEllipse(int previousX, int previousY, int currentX, int currentY) {
 		String send = Protocol.DRAW_ELLIPSE + " " + 
 					  previousX + " " + 
 					  previousY + " " + 
 					  currentX + " " + 
 					  currentY + " " +
 					  colorType;
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawText(int x, int y, char c) {
 		String send = Protocol.DRAW_TEXT + " " + 
 					  x + " " + 
 					  y + " " + 
 					  c + " " +
 					  colorType;
 		
 		System.out.println(send);
 
 		buffer.put(send);
 	}
 	
 	/*
 	 * This method doesn't actually draw anything, it just puts a draw command
 	 * into the SendBuffer.
 	 */
 	private void sendDrawText(int x, int y, int i) {
 		String send = Protocol.DRAW_TEXT + " " + 
 					  x + " " + 
 					  y + " " + 
 					  i + " " +
 					  colorType;
 
 		buffer.put(send);
 	}
 	public void mousePressed(MouseEvent e) {
 		// Clear top layer
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		repaint();
 		
 		colorType = Protocol.BRUSH_COLOR_1;
 		System.out.println("Colortype = 1");
 		if (e.getButton() == 2 || e.getButton() == 3) { // getButton returnerar olika beroende på mus.. MouseEvent.BUTTON2 är inte högerklick för mig t.ex.
 			colorType = Protocol.BRUSH_COLOR_2;
 			System.out.println("Colortype = 2");
 		}
 		previousX = e.getX();
 		previousY = e.getY();
 		switch (tp.getTool()) {
 			case ClientToolProperties.BRUSH_TOOL:
 				sendDrawLine(previousX, previousY, e.getX(), e.getY());
 				break;
 			case ClientToolProperties.PEN_TOOL:
 				sendDrawPen(previousX, previousY, e.getX(), e.getY());
 				break;
 			case ClientToolProperties.LINE_TOOL:
 				break;
 			case ClientToolProperties.RECTANGLE_TOOL:
 				break;
 			case ClientToolProperties.COLORPICKER_TOOL:
 				int rgb = paintPanel.getColor(e.getPoint());
 				tp.setColor(rgb,colorType);
 				tp.setTool(ClientToolProperties.BRUSH_TOOL);
 				break;
 			case ClientToolProperties.ERASER_TOOL:
 				sendErase(previousX, previousY, e.getX(), e.getY());
 				break;	
 			case ClientToolProperties.TEXT_TOOL:
 				textStartX = previousX;
 				this.requestFocus();
 				break;	
 		}
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		switch (tp.getTool()) {
 			case ClientToolProperties.BRUSH_TOOL:
 				if (previousX != -90000) {
 					sendDrawLine(previousX, previousY, e.getX(), e.getY());
 				}
 				previousX = e.getX();
 				previousY = e.getY();
 				break;
 			case ClientToolProperties.PEN_TOOL:
 				if (previousX != -90000) {
 					sendDrawPen(previousX, previousY, e.getX(), e.getY());
 				}
 				previousX = e.getX();
 				previousY = e.getY();
 				break;
 			case ClientToolProperties.LINE_TOOL:
 				if (previousX != -90000) {
 					drawLine(previousX, previousY, e.getX(), e.getY(), tp.getColor(colorType), tp.getBrushWidth());
 				}
 				break;
 			case ClientToolProperties.RECTANGLE_TOOL:
 				if (previousX != -90000) {
 					drawRectangle(previousX, previousY, e.getX(), e.getY(), tp.getColor(colorType));
 				}
 				break;
 			case ClientToolProperties.ELLIPSE_TOOL:
 				if (previousX != -90000) {
 					drawEllipse(previousX, previousY, e.getX(), e.getY(), tp.getColor(colorType));
 				}
 				break;
 			case ClientToolProperties.ERASER_TOOL:
 				if (previousX != -90000) {
 					sendErase(previousX, previousY, e.getX(), e.getY());
 				}
 				previousX = e.getX();
 				previousY = e.getY();
 				drawDotWithBorder(e.getX(), e.getY(), e.getX(), e.getY(), Color.WHITE.getRGB(), Color.BLACK.getRGB(), tp.getBrushWidth());
 				break;
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if (previousX != -90000) {
 			switch (tp.getTool()) {
 				case ClientToolProperties.LINE_TOOL:
 					sendDrawLine(previousX, previousY, e.getX(), e.getY());
 					bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 					repaint();
 					break;
 				case ClientToolProperties.RECTANGLE_TOOL:
 					sendDrawRectangle(previousX, previousY, e.getX(), e.getY());
 					bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 					repaint();
 					break;
 				case ClientToolProperties.ELLIPSE_TOOL:
 					sendDrawEllipse(previousX, previousY, e.getX(), e.getY());
 					bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 					repaint();
 					break;
 			}
 		}
 		
 		// Reset previous
 		if (tp.getTool() != ClientToolProperties.TEXT_TOOL) {
 			previousX = -90000;
 			previousY = -90000;
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		switch (tp.getTool()) {
 			case ClientToolProperties.BRUSH_TOOL:
 				drawLine(e.getX(), e.getY(), e.getX(), e.getY(), tp.getColor(Protocol.BRUSH_COLOR_1), tp.getBrushWidth());
 				break;
 			case ClientToolProperties.LINE_TOOL:
 				drawLine(e.getX(), e.getY(), e.getX(), e.getY(), tp.getColor(Protocol.BRUSH_COLOR_1), tp.getBrushWidth());
 				break;
 			case ClientToolProperties.ERASER_TOOL:
 				drawDotWithBorder(e.getX(), e.getY(), e.getX(), e.getY(), Color.WHITE.getRGB(), Color.BLACK.getRGB(), tp.getBrushWidth());
 				break;
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// Clear top layer
 		bufImage = new ImageWrapper(SIZE_X, SIZE_Y);
 		repaint();
 	}
 
 	public void mouseClicked(MouseEvent e) {
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (tp.getTool() != ClientToolProperties.TEXT_TOOL)
 			return;
 		char c = (char)e.getKeyChar();
 		if ((int)c > 32 && (int)c < 127) {
 			sendDrawText(previousX, previousY, c);
 			previousX += tp.getBrushWidth();
 		} else if((int)c == 32) {
 			sendDrawText(previousX, previousY, Protocol.CHAR_SPACE);
 			previousX += tp.getBrushWidth();
 		} else if((int)c == 10) {
 			previousX = textStartX;
 			previousY += tp.getBrushWidth();
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 }
