 package uk.ac.shef.dynamicdocument.gui;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import uk.ac.shef.dynamicdocument.Word;
 import uk.ac.shef.dynamicdocument.WordSet;
 import uk.ac.shef.dynamicdocument.modifier.BlankModifier;
 import uk.ac.shef.dynamicdocument.modifier.Modifier;
 
 public class DocumentPanel extends JTextPane
 {
 	protected WordSet doc;
 	// This maps from each word to the corresponding location in text (WORDSx2)
 	protected int[][] wordMap;
 
 	int currCenterPoint;
 
 	long expireTime = -1;
 	double gamma = 0.1;
 
	Color HIGHLIGHT_COLOR = Color.black;
 	Map<Integer, AttributeSet> highlightAttributeMap = new TreeMap<Integer, AttributeSet>();
 	// We hold the center line for 1 second at the moment
 	long HOLD_TIME = 1000;
 
 	Modifier mod = new BlankModifier();
 
 	StyledDocument paneDoc;
 
 	MutableAttributeSet plainText;
 
 	public DocumentPanel()
 	{
 		paneDoc = this.getStyledDocument();
 		plainText = new SimpleAttributeSet();
 
 		this.setEditable(false);
 	}
 
 	public WordSet getDynDocument()
 	{
 		return doc;
 	}
 
 	public int getPixelLocation(double timeInSeconds)
 	{
 		int pixelLoc = -1;
 
 		// Get the bestMatch
 		int closestWordIndex = getClosestWordIndex(timeInSeconds);
 		int caratStart = wordMap[closestWordIndex][0];
 
 		// Attempt to compute the pixel location
 		try
 		{
 			// Use the middle of the model rectangle as the pixel point
 			Rectangle rect = modelToView(caratStart);
 
 			if (rect != null)
 				pixelLoc = rect.y + (rect.height) / 2;
 		}
 		catch (BadLocationException e)
 		{
 			e.printStackTrace();
 		}
 
 		return pixelLoc;
 	}
 
 	public double getTimeInSeconds(int pixX, int pixY)
 	{
 		double retTime = -1;
 
 		int caratLocation = viewToModel(new Point(pixX, pixY));
 
 		// This assumes that the words are in time order
 		for (int i = 0; i < wordMap.length; i++)
 			if (wordMap[i][1] >= caratLocation)
 			{
 				retTime = doc.collapseToWords().get(i).getStartTime();
 				break;
 			}
 
 		return retTime;
 	}
 
 	public List<Integer> getWordIndicies(double timeInSeconds)
 	{
 		List<Integer> indicies = new Vector<Integer>();
 
 		List<Word> wrds = doc.collapseToWords();
 		for (int i = 0; i < wrds.size(); i++)
 			if (wrds.get(i).getStartTime() <= timeInSeconds && wrds.get(i).getEndTime() >= timeInSeconds)
 				indicies.add(i);
 
 		return indicies;
 	}
 	
	public List<Integer> getWordIndicies(int index)
 	{
 		List<Integer> indicies = new Vector<Integer>();
 
 		List<Word> wrds = doc.collapseToWords();
 		for (int i = 0; i < wrds.size(); i++)
			if (wrds.get(i).getIndex() == index)
 				indicies.add(i);
 
 		return indicies;
 	}
 
 	public int[][] getWordMap()
 	{
 		int[][] copy = new int[wordMap.length][];
 		for (int i = 0; i < wordMap.length; i++)
 		{
 			wordMap[i] = new int[wordMap[i].length];
 			for (int j = 0; j < wordMap[i].length; j++)
 				copy[i][j] = wordMap[i][j];
 		}
 		return copy;
 	}
 
 	public void highlightTime(double timeInSeconds)
 	{
 		unHighlightWords();
 		highlightWords(getWordIndicies(timeInSeconds));
 	}
 
	public void highlightIndex(int index)
 	{
 		unHighlightWords();
 		highlightWords(getWordIndicies(index));
 	}
 	
 	public void scrollToTime(double timeInSeconds)
 	{
 		int bestMatch = getClosestWordIndex(timeInSeconds);
 
 		// Now scroll to that time
 		try
 		{
 			int caratStart = wordMap[bestMatch][0];
 			this.scrollRectToVisible(this.modelToView(caratStart));
 		}
 		catch (BadLocationException e)
 		{
 			// Shouldn't reach here - something's gone horribly wrong if we do!
 			e.printStackTrace();
 		}
 	}
 
 	public void setDocument(WordSet in)
 	{
 		doc = in;
 
 		// Clear the current document
 		this.setText("");
 
 		layoutDocument();
 	}
 
 	public void setGamma(double in)
 	{
 		gamma = in;
 
 		// We only need to relayout the document if it's structure has changed
 		// int mapPoint = getCenterLineCarat();
 
 		// relayoutDocument();
 		// centerPoint(mapPoint);
 
 		reapplyMods();
 	}
 
 	public void setModifier(Modifier in)
 	{
 		mod = in;
 	}
 
 	private int getClosestWordIndex(double timeInSeconds)
 	{
 		// Go through each entry in the word map
 		List<Word> words = doc.collapseToWords();
 
 		/*
 		 * Loop through the words (in time order) then break when we find a word
 		 * which startsafter the given time
 		 */
 		int bestMatch = 0;
 		for (int i = 0; i < words.size(); i++)
 			if (words.get(i).getStartTime() < timeInSeconds)
 				bestMatch = i;
 			else
 				break;
 
 		return bestMatch;
 	}
 
 	private void highlightWords(List<Integer> indices)
 	{
 		for (Integer integer : indices)
 		{
 			highlightAttributeMap.put(integer, paneDoc.getCharacterElement(wordMap[integer][0] + 1).getAttributes());
 			SimpleAttributeSet sas = new SimpleAttributeSet();
 			StyleConstants.setBackground(sas, HIGHLIGHT_COLOR);
 
 			int[] startAndEnd = wordMap[integer];
 			paneDoc.setCharacterAttributes(startAndEnd[0], startAndEnd[1] - startAndEnd[0], sas, false);
 		}
 	}
 	
	public void highlight(List<Integer> indices)
 	{
 		for(Integer integer: indices)
 		{
 			highlightAttributeMap.put(integer, paneDoc.getCharacterElement(wordMap[integer][0] + 1).getAttributes());
 			SimpleAttributeSet sas = new SimpleAttributeSet();
 			StyleConstants.setBackground(sas, HIGHLIGHT_COLOR);
 
 			int[] startAndEnd = wordMap[integer];
 			paneDoc.setCharacterAttributes(startAndEnd[0], startAndEnd[1] - startAndEnd[0], sas, false);
 		}
 	}
 
 	private void layoutDocument()
 	{
 		List<Word> allwords = doc.collapseToWords();
 
 		wordMap = new int[allwords.size()][2];
 
 		try
 		{
 			int caratLocation = 0;
 			for (int i = 0; i < allwords.size(); i++)
 			{
 				Word word = allwords.get(i);
 				String textRep = mod.modifyWord(word, gamma);
 				MutableAttributeSet set = mod.modifyWord(word, plainText, gamma, wordMap[i]);
 
 				StyleConstants.setBold(set, word.isBold());
 				StyleConstants.setFontSize(set, word.getFontSize());
 				if (word.getBackColor() != null)
 					StyleConstants.setBackground(set, word.getBackColor());
 				
 				
 				if (i != allwords.size() - 1 && allwords.get(i + 1).getConnect() != Word.TO_PREVIOUS
 						&& word.getConnect() != Word.TO_FOLLOWING)
 				{
 					wordMap[i][0] = caratLocation;
 					
 					paneDoc.insertString(caratLocation, textRep + " ", set);
 					caratLocation += textRep.length() + 1;
 					wordMap[i][1] = caratLocation;
 				}
 				else
 				{
 					wordMap[i][0] = caratLocation;
 					paneDoc.insertString(caratLocation, textRep, set);
 					caratLocation += textRep.length();
 					wordMap[i][1] = caratLocation;
 				}
 
 			}
 		}
 		catch (BadLocationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void reapplyMods()
 	{
 		// I put this into a swing thread to prevent flickering
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				if (doc != null)
 				{
 					List<Word> words = doc.collapseToWords();
 					for (int i = 0; i < words.size(); i++)
 					{
 						AttributeSet set = mod.modifyWord(words.get(i), plainText, gamma, wordMap[i]);
 						if (set != null)
 							paneDoc.setCharacterAttributes(wordMap[i][0], wordMap[i][1] - wordMap[i][0], set, true);
 					}
 				}
 			}
 
 		});
 
 	}
 
 	private void unHighlightWords()
 	{
 		for (Integer index : highlightAttributeMap.keySet())
 		{
 			int[] startAndEnd = wordMap[index];
 			paneDoc.setCharacterAttributes(startAndEnd[0], startAndEnd[1] - startAndEnd[0],
 					highlightAttributeMap.get(index), true);
 		}
 
 		// Clear the map
 		highlightAttributeMap.clear();
 	}
 
 	protected void centerCaratPoint(int carat)
 	{
 		try
 		{
 			Rectangle cRect = this.modelToView(carat);
 
 			// Stretch this rectangle vertically to ensure the carat point is
 			// centered rather than just on-screen
 			int cPoint = (cRect.y + cRect.height) / 2;
 			int yStartPoint = cPoint - this.getVisibleRect().height / 2;
 			int yHeight = this.getVisibleRect().height;
 
 			this.scrollRectToVisible(new Rectangle(cRect.x, yStartPoint, cRect.width, yHeight));
 		}
 		catch (BadLocationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	protected void centerPoint(int centerPoint)
 	{
 		/*
 		 * try { Rectangle cRect = this.modelToView(wordMap[centerPoint][0]);
 		 * 
 		 * //Stretch this rectangle vertically to ensure the carat point is
 		 * centered rather than just on-screen int cPoint =
 		 * (cRect.y2+cRect.height)/2; int yStartPoint =
 		 * cPoint-this.getVisibleRect().height/2; int yHeight =
 		 * this.getVisibleRect().height; final Rectangle centRect = new
 		 * Rectangle(cRect.x,yStartPoint,cRect.width,yHeight);
 		 * 
 		 * //We add this to the swing thread in order to order it correctly
 		 * SwingUtilities.invokeLater(new Runnable(){ public void run() { //
 		 * scrollRectToVisible(centRect); } }); } catch (BadLocationException e) {
 		 * e.printStackTrace(); }
 		 */
 	}
 
 	protected int getCenterLineCarat()
 	{
 		// First check that the current center line hasn't expired
 		if (System.currentTimeMillis() < expireTime)
 		{
 			// Reset the expire time
 			expireTime = System.currentTimeMillis() + HOLD_TIME;
 			return currCenterPoint;
 		}
 
 		Rectangle visRect = this.getVisibleRect();
 		Point cPointLeft = new Point(0, (visRect.y * 2 + visRect.height) / 2);
 		int caratPoint = this.viewToModel(cPointLeft);
 
 		/*
 		 * MutableAttributeSet set = new SimpleAttributeSet();
 		 * StyleConstants.setForeground(set, Color.red);
 		 * paneDoc.setCharacterAttributes(caratPoint-5, 10, set, true);
 		 */
 
 		int wPoint = -1;
 		// Now link this carat point down to a point in the text
 		for (int i = 0; i < wordMap.length && wPoint == -1; i++)
 			if (wordMap[i][0] <= caratPoint && wordMap[i][1] >= caratPoint)
 				wPoint = i;
 
 		if (wPoint == -1)
 			wPoint = wordMap.length - 1;
 
 		currCenterPoint = wPoint;
 		expireTime = System.currentTimeMillis() + HOLD_TIME;
 
 		return wPoint;
 	}
 }
