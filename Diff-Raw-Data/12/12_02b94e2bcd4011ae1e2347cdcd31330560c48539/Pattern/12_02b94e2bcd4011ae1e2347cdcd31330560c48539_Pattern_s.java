 package pattern;
 
 import graphics.AcceleratedImage;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import main.Information;
 
 import utils.ExceptionHandler;
 
 /**
  * Represents a single Pattern made up of alive and dead cells arranged in a two dimensional array of booleans.
  * Each pattern has a name that is most commonly used when space is limited, an expanded name that is occasionally used when space is not, and a category.
  * Additionally, every pattern had an AcceleratedImage that is used as a visual representation of the Pattern.
  * 
  * @author Dominic
  */
 public class Pattern 
 {
 	public AcceleratedImage img = null;
 	
 	public boolean[][] pattern;
 	private boolean fillBlack;
 	
 	private Information info;
 	
 	public int width;			// the width of the pattern array, equal to pattern[0].length
 	public int height;			// the height of the pattern array, equal to pattern.length
 	public int imageWidth;		// the width of the pattern's image, in pixels
 	public int imageHeight;		// the height of the pattern's image, in pixels
 	private int aliveIndex;
 	
 	public String name;
 	public String expandedName;
 	public String category;
 	
 	/**
 	 * Creates a new Pattern with the given variables and Information.
 	 * The expanded name is set to the given name.
 	 * The size of the image is set to 0, 0 and the image is not initialized until setSize() is called.
 	 * 
 	 * @param pattern - the array of booleans that make up the pattern
 	 * @param name - the name of this pattern
 	 * @param category - the name of this pattern's category
 	 * @param info - the current Information
 	 */
 	public Pattern(boolean[][] pattern, String name, String category, Information info)
 	{
 		this.pattern = pattern;
 		this.name = name;
 		this.category = category;
 		this.info = info;
 		expandedName = name;
 		width = pattern[0].length;
 		height = pattern.length;
 		fillBlack = false;
 		aliveIndex = info.imageLoader.getIndex("alive");
 		setSize(0, 0);
 	}
 	
 	/**
 	 * Creates a new Pattern with the given variables and Information.
 	 * The expanded name is set to the given name.
 	 * The size of the image is set to the given dimensions.
 	 * 
 	 * @param pattern - the array of booleans that make up the pattern
 	 * @param name - the name of this pattern
 	 * @param category - the name of this pattern's category
 	 * @param imageWidth - the width of the image made for this pattern, in pixels
 	 * @param imageHeight - the height of the image made for this pattern, in pixels
 	 * @param info - the current Information
 	 */
 	public Pattern(boolean[][] pattern, String name, String category, int imageWidth, int imageHeight, Information info)
 	{
 		this.pattern = pattern;
 		this.category = category;
 		this.name = name;
 		this.info = info;
 		expandedName = name;
 		width = pattern[0].length;
 		height = pattern.length;
 		fillBlack = false;
 		aliveIndex = info.imageLoader.getIndex("alive");
 		setSize(imageWidth, imageHeight);
 	}
 	
 	/**
 	 * Creates a new Pattern with the given variables and Information.
 	 * The expanded name is set to the given one.
 	 * The size of the image is set to 0, 0 and the image is not initialized until setSize() is called.
 	 * 
 	 * @param pattern - the array of booleans that make up the pattern
 	 * @param name - the name of this pattern
 	 * @param expandedName - the full name of this pattern
 	 * @param category - the name of this pattern's category
 	 * @param info - the current Information
 	 */
 	public Pattern(boolean[][] pattern, String name, String expandedName, String category, Information info)
 	{
 		this.pattern = pattern;
 		this.category = category;
 		this.name = name;
 		this.expandedName = expandedName;
 		this.info = info;
 		width = pattern[0].length;
 		height = pattern.length;
 		fillBlack = false;
 		aliveIndex = info.imageLoader.getIndex("alive");
 		setSize(0, 0);
 	}
 	
 	/**
 	 * Creates a new Pattern with the given variables and Information.
 	 * The expanded name is set to the given one.
 	 * The size of the image is set to the given dimensions.
 	 * 
 	 * @param pattern - the array of booleans that make up the pattern
 	 * @param name - the name of this pattern
 	 * @param expandedName - the full name of this pattern
 	 * @param category - the name of this pattern's category
 	 * @param imageWidth - the width of the image made for this pattern, in pixels
 	 * @param imageHeight - the height of the image made for this pattern, in pixels
 	 * @param info - the current Information
 	 */
 	public Pattern(boolean[][] pattern, String name, String expandedName, String category, int imageWidth, int imageHeight, Information info)
 	{
 		this.pattern = pattern;
 		this.category = category;
 		this.name = name;
 		this.info = info;
 		this.expandedName = expandedName;
 		this.width = pattern[0].length;
 		this.height = pattern.length;
 		fillBlack = false;
 		aliveIndex = info.imageLoader.getIndex("alive");
 		setSize(imageWidth, imageHeight);
 	}
 	
 	/**
 	 * Sets the width and height of the image representing this pattern to the given dimensions.
 	 * This erases the any previous drawing to the image, so calls to generateImage() and generateFullSizeImage() must be recalled.
 	 * If setFillBlack() had been called with a true value, the background of the pattern is filled with black.
 	 * If the sizes given are not both greater than 0, the image will not be initialized.
 	 * 
 	 * @param imageWidth - the width of the image made for this pattern, in pixels
 	 * @param imageHeight - the height of the image made for this pattern, in pixels
 	 */
 	public void setSize(double imageWidth, double imageHeight)
 	{
 		setSize((int)imageWidth, (int)imageHeight);
 	}
 	
 	/**
 	 * Sets the width and height of the image representing this pattern to the given dimensions.
 	 * This erases the any previous drawing to the image, so calls to generateImage() and generateFullSizeImage() must be recalled.
 	 * If setFillBlack() had been called with a true value, the background of the pattern is filled with black.
 	 * If the sizes given are not both greater than 0, the image will not be initialized.
 	 * 
 	 * @param imageWidth - the width of the image made for this pattern, in pixels
 	 * @param imageHeight - the height of the image made for this pattern, in pixels
 	 */
 	public void setSize(int imageWidth, int imageHeight)
 	{
 		this.imageWidth = (int)imageWidth;
 		this.imageHeight = (int)imageHeight;
 		if ((imageWidth > 0) && (imageHeight > 0))
 		{
 			img = new AcceleratedImage(new BufferedImage((int)imageWidth, (int)imageHeight, BufferedImage.TYPE_INT_ARGB), BufferedImage.TRANSLUCENT);
 			if (fillBlack)
 			{
 				Graphics g = img.getGraphics();
 				g.setColor(Color.black);
 				g.fillRect(0, 0, img.getWidth(), img.getHeight());
 			}
 		}
 	}
 	
 	/**
 	 * Sets whether or not the background of the image should be filled with black.
 	 * setSize() is then called, immediately applying the change with the same imageWidth and imageHeight.
 	 * 
 	 * @param fill - true if the background should be black, false otherwise
 	 */
 	public void setFillBlack(boolean fill)
 	{
 		fillBlack = fill;
 		setSize(imageWidth, imageHeight);
 	}
 	
 	/**
 	 * Generates the image used to display the pattern within the selector with the given size (width/height dimension).
 	 * A size for each cell is calculated based on the maximum of the width and height and this is used to scale the alive image.
 	 * x- and y-shifts are calculated in case the pattern is not square and does not completely fill the space in one direction.
 	 * The image is left blank for dead cells and appropriately scaled and positioned "alive" images are used for alive cells.
 	 */
 	public void generateImage()
 	{
 		if (img != null)
 		{
 			double cellWidth = imageWidth/width;
 			double cellHeight = imageHeight/height;
 			double cellSize = Math.min(cellWidth, cellHeight);
 			double xShift = (imageWidth - width*cellSize)/2;
 			double yShift = (imageHeight - height*cellSize)/2;
 			for (int i = 0; i < width; i++)
 			{
 				for (int j = 0; j < height; j++)
 				{
 					if (pattern[j][i])
 					{
 						img.getGraphics().drawImage(info.imageLoader.get(aliveIndex).getBufferedImage(), (int)(i*cellSize + xShift), (int)(j*cellSize + yShift),
 								(int)cellSize, (int)cellSize, null);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns a duplicate of the current pattern with the same array, category, name, and Information.
 	 */
 	public Pattern clone()
 	{
 		return new Pattern(pattern, name, expandedName, category, imageWidth, imageHeight, info);
 	}
 	
 	/**
 	 * Generates an image that is scaled only based on the current zoom value,
 	 *  used after the pattern has been selected and is now being dragged around the window.
 	 * The image size is set to the necessary size before any drawing is done.
 	 */
 	public void generateFullSizeImage()
 	{
 		setSize(width*info.grid.zoom, height*info.grid.zoom);
 		for (int x = 0; x < width; x++)
 		{
 			for (int y = 0; y < height; y++)
 			{
 				if (pattern[y][x])
 				{
 					img.getGraphics().drawImage(info.imageLoader.get(aliveIndex).getBufferedImage(), (int)(x*info.grid.zoom), (int)(y*info.grid.zoom),
 							(int)info.grid.zoom, (int)info.grid.zoom, null);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Determines whether the given character represents true or false.
 	 * If the character is 't', it represents true, any other value represents false, though 'f' is used by convention.
 	 * 
 	 * @param c - the character to be tested
 	 * @return value - true if the character is 't', false otherwise
 	 */
 	public static boolean value(char c)
 	{
 		if (c == 't')
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns the Pattern found at the given filename, initialized with the given Information.
 	 * The size of image of the loaded Pattern is set to the given width and height.
 	 * The loading format is as follows:<br>
 	 * the first folder in the execution folder contains folders named after the category names of the patterns inside<br>
 	 * these category folders contain the patterns, named after their name (non-expanded), with the .txt file extension<br>
 	 * the pattern files contain two sections:<br>
 	 * first, there is a 2D grid of t's and f's containing the pattern layout, followed by the standard break (###)<br>
 	 * last, there is the expanded name, all on one line - optional, if not included, the expanded name will be the same as the normal name<br>
 	 * 
 	 * @param filename - the full location of the Pattern relative to the execution folder with the .txt file extension
 	 * @param imageWidth - the width of the image representing the Pattern, in pixels
 	 * @param imageHeight - the height of the image representing the Pattern, in pixels
 	 * @param info - the current Information
 	 * @return Pattern - the Pattern, fully initialized, at the given filename, null if the loading encountered errors
 	 */
 	public static Pattern load(String filename, int imageWidth, int imageHeight, Information info)
 	{
 		try
 		{
			StringTokenizer tokenizer = new StringTokenizer(filename, "\\");
 			String category;
 			String name;
 			String expandedName;
 			boolean[][] pattern;
 			if (tokenizer.hasMoreTokens())
 			{
 				tokenizer.nextToken();
 				if (tokenizer.hasMoreTokens())
 				{
 					category = tokenizer.nextToken();
 					if (tokenizer.hasMoreTokens())
 					{
 						name = tokenizer.nextToken();
 						tokenizer = new StringTokenizer(name, ".");
 						name = tokenizer.nextToken();
 						expandedName = name;
 					}
 					else
 					{
 						System.out.println("A pattern was loaded with a faulty filename: no pattern name found.");
 						System.out.println("The filename was " + filename);
 						return null;
 					}
 				}
 				else
 				{
 					System.out.println("A pattern was loaded with a faulty filename: no pattern category found.");
 					System.out.println("The filename was " + filename);
 					return null;
 				}
 			}
 			else
 			{
 				System.out.println("A pattern was loaded with a faulty filename: no folder.");
 				System.out.println("The filename was " + filename);
 				return null;
 			}
 			BufferedReader in = new BufferedReader(new FileReader(filename));
 			ArrayList<String> lines = new ArrayList<String>();
 			String line = new String();
 			while (true)
 			{
 				line = in.readLine();
 				if (line == null)
 				{
 					break;
 				}
 				if (line.equals("###"))
 				{
 					line = in.readLine();
 					expandedName = line;
 					break;
 				}
 				else
 				{
 					lines.add(line);
 				}
 
 			}
 			int height = lines.size();
 			int width = 0;
 			for (int i = 0; i < lines.size(); i++)
 			{
 				width = Math.max(lines.get(i).length(), width);
 			}
 			pattern = new boolean[height][width];
 			for (int i = 0; i < width; i++)
 			{
 				for (int j = 0; j < height; j++)
 				{
 					try
 					{
 						pattern[j][i] = value(lines.get(j).charAt(i));
 					}
 					catch (Exception ex)
 					{
 						pattern[j][i] = false;
 					}
 				}
 			}
 			return new Pattern(pattern, name, expandedName, category, imageWidth, imageHeight, info);
 		}
 		catch (IOException ex)
 		{
 			ExceptionHandler.receive(ex, filename, "Error thrown when loading the specified Pattern.");
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the Pattern found at the given filename, initialized with the given Information.
 	 * The size of image of the loaded Pattern is set to the 0, 0 so the image is not initialized.
 	 * 
 	 * @param filename - the full location of the Pattern relative to the execution folder with the .txt file extension
 	 * @param info - the current Information
 	 * @return Pattern - the Pattern, fully initialized, at the given filename, null if the loading encountered errors
 	 */
 	public static Pattern load(String filename, Information info)
 	{
 		return load(filename, 0, 0, info);
 	}
 }
