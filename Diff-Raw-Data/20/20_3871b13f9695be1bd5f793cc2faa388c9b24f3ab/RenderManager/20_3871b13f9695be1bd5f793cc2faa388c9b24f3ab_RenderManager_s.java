 package com.moomoohk.Grame.Graphics;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import com.moomoohk.Grame.Essentials.Base;
 import com.moomoohk.Grame.Essentials.Coordinates;
 import com.moomoohk.Grame.Essentials.GrameManager;
 import com.moomoohk.Grame.Essentials.GrameUtils;
 import com.moomoohk.Grame.Essentials.GrameUtils.MessageLevel;
 import com.moomoohk.Grame.Graphics.PostProcessing.Label;
 import com.moomoohk.Grame.Interfaces.Render;
 
 /**
  * Manages all the rendering operations.
  * 
  * @author Meshulam Silk <moomoohk@ymail.com>
  * @version 1.0
  * @since 2013-04-05
  */
 public class RenderManager
 {
 	private static HashMap<Integer, Render> renders;
 	private static HashMap<Integer, TextLayer> text;
 	private static ArrayList<Label> labels;
 	private static JFrame mainFrame;
 	private static Canvas mainCanvas;
 	private static int mainBase;
 	private static boolean drawCoordinates;
 	private static int squareSize = 30;
 	private static Graphics2D g2;
 	private static BufferedImage img;
 
 	public static void initialize()
 	{
 		renders = new HashMap<Integer, Render>();
 		text = new HashMap<Integer, TextLayer>();
 		labels = new ArrayList<Label>();
 		loadRenders();
 		mainFrame = new JFrame();
 		mainCanvas = null;
 		mainBase = -1;
 		drawCoordinates = false;
 		GrameUtils.print("Initialized successfully.", MessageLevel.NORMAL);
 	}
 
 	/**
 	 * Renders a {@link Base}.
 	 * 
 	 * @param bID
 	 *            The {@link Base#ID} of the {@link Base} to render.
 	 */
 	public static void render(int bID)
 	{
 		render(bID, renders.get(bID));
 	}
 
 	/**
 	 * Renders a {@link Base} using a given {@link Render}.
 	 * 
 	 * @param bID
 	 *            The {@link Base#ID} of the {@link Base} to render.
 	 * @param render
 	 *            The {@link Render} to use.
 	 */
 	public static void render(final int bID, Render render)
 	{
 		if (bID < 0)
 			return;
 		setupFrame(bID);
 		if (bID != mainBase || GrameManager.findBase(bID) == null)
 			GrameManager.setMainBase(bID);
 		mainBase = bID;
 		mainFrame.setTitle(GrameManager.getGameName());
 		if (render == null)
 			render = GrameManager.getDefaultRender();
 		if (!render.equals(renders.get(bID)))
 			GrameManager.setMainRender(render);
 		renders.put(bID, render);
 		BufferStrategy bs = mainCanvas.getBufferStrategy();
 		if (bs == null)
 		{
 			if (mainFrame.isVisible())
 				mainCanvas.createBufferStrategy(3);
 			return;
 		}
 		img = new BufferedImage(mainCanvas.getWidth(), mainCanvas.getHeight(), BufferedImage.TYPE_INT_RGB);
 		int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
 		try
 		{
 			pixels = render.getPixels(pixels, GrameManager.findBase(bID), mainCanvas.getWidth(), mainCanvas.getHeight());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 		g2 = (Graphics2D) bs.getDrawGraphics().create();
 
 		g2.setFont(new Font("LucidaTypewriter", 1, 8));
 		int squaresize = mainCanvas.getWidth() / GrameManager.findBase(bID).getColumns();
 		for (int x = 0; x < GrameManager.findBase(bID).getColumns(); x++)
 			for (int y = 0; y < GrameManager.findBase(bID).getRows(); y++)
 				if (text.get(bID) != null)
 				{
 					if (text.get(bID).getText(new Coordinates(x, y)) != null && text.get(bID).getText(new Coordinates(x, y)).trim().length() != 0)
 					{
 						g2.setColor(text.get(bID).getColor(new Coordinates(x, y)));
 						g2.drawString(text.get(bID).getText(new Coordinates(x, y)), y * squaresize, (x + 1) * (squaresize) - ((squaresize / 2) - 5));
 					}
 				}
 		if (!GrameManager.paused)
 			g2.drawImage(img, 0, 0, mainCanvas.getWidth(), mainCanvas.getHeight(), null);
 		else
 		{
 			applyGaussianBlur();
 			setOverlayColor(Color.gray.brighter());
 			for (Label l : labels)
 				drawLabel(l);
 		}
 
 		if (drawCoordinates)
 		{
 			g2.setColor(Color.blue);
 			for (int x = 0; x < GrameManager.findBase(bID).getColumns(); x++)
 				for (int y = 0; y < GrameManager.findBase(bID).getRows(); y++)
 					g2.drawString("(" + y + ", " + x + ")", y * squaresize, (x + 1) * (squaresize) - ((squaresize / 2) - 5));
 		}
 		g2.dispose();
 		bs.show();
 	}
 
 	public static void applyGaussianBlur()
 	{
 		g2.drawImage(new GaussianFilter().filter(img), 0, 0, mainCanvas.getWidth(), mainCanvas.getHeight(), null);
 	}
 
 	public static void drawLabel(Label l)
 	{
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g2.setFont(l.getFont());
 		FontMetrics fm = g2.getFontMetrics();
 		Rectangle2D textBounds = fm.getStringBounds(l.getText(), g2);
 		Rectangle fixed = new Rectangle((int) (l.getCenterX() - (textBounds.getWidth() / 2)), (int) (l.getCenterY() - (textBounds.getHeight() / 2)), (int) textBounds.getWidth(), (int) textBounds.getHeight());
 
 		Rectangle back = new Rectangle((int) fixed.getX() - l.getPaddingLeft(), (int) fixed.getY() - l.getPaddingTop(), (int) (fixed.getWidth()) + l.getPaddingRight() + l.getPaddingLeft(), (int) (fixed.getHeight()) + l.getPaddingBottom() + l.getPaddingTop());
		g2.setColor(new Color(0, 0, 0, 200));
 		g2.fillRoundRect((int) back.getX(), (int) back.getY(), (int) back.getWidth(), (int) back.getHeight(), 10, 10);
		g2.setColor(new Color(l.getTextColor().getRed(), l.getTextColor().getGreen(), l.getTextColor().getBlue(), 200));
 		g2.drawString(l.getText(), (int) (fixed.getX()), (int) (fixed.getY() + Math.max(fm.getMaxAscent(), fm.getMaxDescent())));
 
 		//					g2.setColor(Color.cyan);
 		//					g2.draw(new Ellipse2D.Double(textCenterX - 5, textCenterY - 5, 10, 10));
 		//					g2.setColor(Color.blue);
 		//					g2.draw(back);
 		//					g2.setColor(Color.green);
 		//					g2.draw(textBounds);
 		//					g2.setColor(Color.red);
 		//					g2.draw(fixed);
 		//					g2.fill(new Ellipse2D.Double(fixed.getCenterX() - 2, fixed.getCenterY() - 2, 4, 4));
 	}
 
 	public static void setOverlayColor(Color color)
 	{
 		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 127));
 		g2.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
 	}
 
 	/**
 	 * Generates a blank {@link Canvas} which is of the required size.
 	 * 
 	 * @param bID
 	 *            {@link Base#ID} of the {@link Base} of which this {@link Canvas} is.
 	 * @return A {@link Canvas}.
 	 */
 	public static Canvas generateCanvas(int bID)
 	{
 		Canvas c = new Canvas();
 		int width = Math.min(squareSize * GrameManager.findBase(bID).getColumns(), Toolkit.getDefaultToolkit().getScreenSize().width), height = Math.min(squareSize * GrameManager.findBase(bID).getRows(), Toolkit.getDefaultToolkit().getScreenSize().height - 50);
 		c.setSize(width, height);
 		return c;
 	}
 
 	/**
 	 * Sets up the main window.
 	 * 
 	 * @param bID
 	 *            {@link Base#ID} of the {@link Base} which is being rendered.
 	 */
 	public static void setupFrame(int bID)
 	{
 		if (bID != mainBase)
 		{
 			if (mainCanvas == null)
 				mainCanvas = generateCanvas(bID);
 			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			mainFrame.setResizable(false);
 			mainFrame.add(mainCanvas);
 			mainFrame.pack();
 			mainFrame.setLocationRelativeTo(null);
 			mainFrame.addFocusListener(new FocusListener()
 			{
 
 				public void focusLost(FocusEvent paramFocusEvent)
 				{
 					GrameManager.getInputHandler().resetKeys();
 					//					GrameManager.pauseAllGrameObjects(true);
 				}
 
 				public void focusGained(FocusEvent paramFocusEvent)
 				{
 					//					GrameManager.pauseAllGrameObjects(false);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Renders the main {@link Base}.
 	 */
 	public synchronized static void tick()
 	{
 		render(mainBase, renders.get(mainBase));
 		mainFrame.repaint();
 	}
 
 	/**
 	 * Attaches a {@link Render} to a {@link Base} for future reference.
 	 * 
 	 * @param bID
 	 *            {@link Base#ID} of {@link Base} to attach the {@link Render} to.
 	 * @param render
 	 *            The {@link Render} to attach.
 	 */
 	public static void setRender(int bID, Render render)
 	{
 		renders.put(bID, render);
 	}
 
 	/**
 	 * Loads some basic {@link Render}s.
 	 */
 	private static void loadRenders()
 	{
 		GrameManager.addRender(new GridRender());
 		GrameManager.addRender(new PlainGridRender());
 	}
 
 	/**
 	 * Sets the visibility of the main window.
 	 * 
 	 * @param f
 	 *            True to make the main window visible, else false.
 	 */
 	public static void setVisible(boolean f)
 	{
 		mainFrame.setVisible(f);
 	}
 
 	/**
 	 * Checks whether the main window is visible or not.
 	 * 
 	 * @return True if the main window is visible, else false.
 	 */
 	public static boolean isVisible()
 	{
 		return mainFrame.isVisible();
 	}
 
 	/**
 	 * Gets the main window.
 	 * 
 	 * @return The main window.
 	 */
 	public static JFrame getMainFrame()
 	{
 		return mainFrame;
 	}
 
 	/**
 	 * Gets the main {@link Canvas}.
 	 * 
 	 * @return The main {@link Canvas}.
 	 */
 	public static Canvas getMainCanvas()
 	{
 		return mainCanvas;
 	}
 
 	/**
 	 * Gets the {@link Base#ID} of the main {@link Base}.
 	 * 
 	 * @return The {@link Base#ID} of the main {@link Base}.
 	 */
 	public static int getMainBase()
 	{
 		return mainBase;
 	}
 
 	/**
 	 * Gets the {@link Render} {@link HashMap}.
 	 * 
 	 * @return The {@link Render} {@link HashMap}.
 	 */
 	public static HashMap<Integer, Render> getRenders()
 	{
 		return renders;
 	}
 
 	/**
 	 * Sets whether or not to draw coordinates.
 	 * <p>
 	 * This method was created for debug and testing purposes.
 	 * 
 	 * @param f
 	 *            True to draw coordinates, else false.
 	 */
 	public static void drawCoordinates(boolean f)
 	{
 		drawCoordinates = f;
 	}
 
 	public static void setText(int bID, Coordinates pos, String text, Color color)
 	{
 		if (!GrameManager.findBase(bID).isInBase(pos))
 			return;
 		if (RenderManager.text.get(bID) == null)
 			RenderManager.text.put(bID, new TextLayer(GrameManager.findBase(bID).getRows(), GrameManager.findBase(bID).getColumns()));
 		RenderManager.text.get(bID).setText(pos, text, color);
 	}
 
 	public static void clearAllText()
 	{
 		for (Integer i : text.keySet())
 			clearText(i);
 	}
 
 	public static void clearText(int bID)
 	{
 		text.put(bID, new TextLayer(GrameManager.findBase(bID).getRows(), GrameManager.findBase(bID).getColumns()));
 	}
 
 	private static class TextLayer
 	{
 		private String[][] text;
 		private Color[][] color;
 
 		public TextLayer(int row, int col)
 		{
 			this.text = new String[row][col];
 			this.color = new Color[row][col];
 		}
 
 		public void setText(Coordinates pos, String text, Color color)
 		{
 			this.text[pos.getY()][pos.getX()] = text;
 			this.color[pos.getY()][pos.getX()] = color;
 		}
 
 		public String getText(Coordinates pos)
 		{
 			return this.text[pos.getY()][pos.getX()];
 		}
 
 		public Color getColor(Coordinates pos)
 		{
 			return this.color[pos.getY()][pos.getX()];
 		}
 	}
 
 	public static void dispose()
 	{
 		if (mainFrame != null)
 		{
 			mainFrame.removeAll();
 			mainFrame.dispose();
 		}
 	}
 }
