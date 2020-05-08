 package rxr;
 
 import java.awt.*;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.*;
 import javax.swing.text.Highlighter.HighlightPainter;
 
 import rxr.RegexEventListener.Type;
 import rxr.ext.*;
 
 /**
  * RegexFieldListener listens for changes in a Document, and reapplies a regular
  * expression whenever a change occurs.
  * 
  */
 class RegexFieldListener implements DocumentListener
 {
 	protected JTextField source;
 	protected JTextPane target;
 	protected JTextField replaceSource;
 	protected JTextPane replaceTarget;
 	protected HashSet<RegexEventListener> listeners;
 
 	protected Color highlightColor = new Color(255, 230, 0, 128);
 	protected Color selectColor = new Color(128, 0, 255, 128);
 
 	protected ArrayList<int[]> matches;
 	protected ArrayList<int[][]> groups;
 	protected ArrayList<Color[]> groupColors;
 
 	protected boolean autoRecalc = true;
 
 	boolean doReplace = false;
 
 	protected Object selectHighlightHandle;
 
 	/**
 	 * Creates a RegexFieldListener that takes a regex from source, and applies
 	 * it to target.
 	 * 
 	 * @param source
 	 *            the component to get the regex string from
 	 * @param target
 	 *            the component to apply the regex to
 	 */
 	public RegexFieldListener(JTextField source, JTextPane target, JTextField replaceSource, JTextPane replaceTarget)
 	{
 		this.source = source;
 		this.target = target;
 		this.replaceSource = replaceSource;
 		this.replaceTarget = replaceTarget;
 		listeners = new HashSet<RegexEventListener>();
 	}
 
 	/**
 	 * Grabs a regex string from the source component, attempts to compile it,
 	 * and call recalcTarget(). At least regex events will be sent to listeners
 	 * during the execution of this method: RECALC_START at the beginning, and
 	 * either BAD_PATTERN or RECALC_COMPLETE depending on the result.
 	 */
 	public void regex()
 	{
 		fireRegexEvent(Type.RECALC_START);
 
 		final String regex = source.getText();
 
 		if(regex.equals(""))
 		{
 			//reset target
 			recalcTarget(null);
 			//not doing any matching, but we know the result of the replace, so just do that.
 			replaceTarget.setText(target.getText());
 			return;
 		}
 
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					Pattern p = Pattern.compile(regex);
 					Matcher m = p.matcher(target.getDocument().getText(0, target.getDocument().getLength()));
 					recalcTarget(m);
 				}
 				catch(Exception e)
 				{
 					recalcTarget(null);
					replaceTarget.setText(target.getText());
 					fireRegexEvent(Type.BAD_PATTERN);
 					return;
 				}
 			}
 		});
 	}
 
 	/**
 	 * Changes the specific highlight on the target document. This will show up
 	 * as a different color than the highlighting for each match.
 	 * 
 	 * @param start
 	 *            the start index in the target document
 	 * @param end
 	 *            the end index in the target document
 	 */
 	public void setOutlineRange(int start, int end)
 	{
 		Highlighter h = target.getHighlighter();
 		if(selectHighlightHandle == null)
 		{
 			//HighlightPainter ohp = new OutlineHighlighter(Color.BLACK).getPainter();
 			HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(selectColor);
 			try
 			{
 				selectHighlightHandle = h.addHighlight(start, end, hp);
 			}
 			catch(Exception e)
 			{
 				//
 			}
 		}
 		else
 		{
 			try
 			{
 				h.changeHighlight(selectHighlightHandle, start, end);
 			}
 			catch(Exception e)
 			{
 				//
 			}
 		}
 	}
 
 	/**
 	 * Recalculate the matches, groups, and groupColors lists based on the data
 	 * in m. Matches and groups are calculated from m by removing group 0 and
 	 * making that the match for that match number. The groups list is
 	 * zero-indexed, starting with the 1st, not 0th group.
 	 * 
 	 * @param m
 	 *            the Matcher object to get match and group data from
 	 */
 	protected void recalcTarget(Matcher m)
 	{
 		selectHighlightHandle = null;
 		Highlighter h = target.getHighlighter();
 
 		//remove formatting
 		try
 		{
 			h.removeAllHighlights();
 		}
 		catch(Exception e)
 		{
 			return;
 		}
 
 		if(m == null)
 		{
 			fireRegexEvent(Type.RECALC_COMPLETE);
 			return;
 		}
 
 		matches = new ArrayList<int[]>();
 		groups = new ArrayList<int[][]>();
 		groupColors = new ArrayList<Color[]>();
 
 		StringBuffer replaceSB = new StringBuffer();
 		String replaceStr = replaceSource.getText();
 
 		while(m.find())
 		{
 			int groupCount = m.groupCount();
 
 			//grab 0th group (whole match)
 			matches.add(new int[] {m.start(), m.end()});
 
 			int[][] current = new int[groupCount][];
 			Color[] ccolors = new Color[groupCount];
 
 			for(int i = 0; i < groupCount; i++)
 			{
 				ccolors[i] = Color.getHSBColor(i / (float)groupCount, 1f, 1f);
 				current[i] = new int[] {m.start(i + 1), m.end(i + 1)};
 			}
 
 			groups.add(current);
 			groupColors.add(ccolors);
 
 			if(doReplace)
 			{
 				m.appendReplacement(replaceSB, replaceStr);
 			}
 		}
 		m.appendTail(replaceSB);
 		replaceTarget.setText(replaceSB.toString());
 
 		doHighlight();
 
 		fireRegexEvent(Type.RECALC_COMPLETE);
 	}
 
 	/**
 	 * Applies the match and group data from matches, groups, and groupColors to
 	 * the target document.
 	 */
 	private void doHighlight()
 	{
 		Highlighter h = target.getHighlighter();
 		HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);
 
 		for(int i = 0; i < matches.size(); i++)
 		{
 			int[][] gs = groups.get(i);
 			Color[] cs = groupColors.get(i);
 
 			for(int j = 0; j < gs.length; j++)
 			{
 				try
 				{
 					HighlightPainter ghp = new UnderlineHighlighter(cs[j]).getPainter();
 					h.addHighlight(gs[j][0], gs[j][1], ghp);
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 					return;
 				}
 			}
 
 			try
 			{
 				h.addHighlight(matches.get(i)[0], matches.get(i)[1], hp);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * Notifies all listeners of a regex event.
 	 * 
 	 * @param t
 	 *            the type of regex event to send
 	 */
 	protected void fireRegexEvent(RegexEventListener.Type t)
 	{
 		for(RegexEventListener rel : listeners)
 		{
 			rel.regexEvent(t);
 		}
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e)
 	{
 		//do nothing
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e)
 	{
 		if(autoRecalc)
 		{
 			regex();
 		}
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e)
 	{
 		if(autoRecalc)
 		{
 			regex();
 		}
 	}
 
 	/**
 	 * @return the source component
 	 */
 	public JTextField getSource()
 	{
 		return source;
 	}
 
 	public void setSource(JTextField source)
 	{
 		this.source = source;
 	}
 
 	/**
 	 * @return the target component
 	 */
 	public JTextPane getTarget()
 	{
 		return target;
 	}
 
 	public void setTarget(JTextPane target)
 	{
 		this.target = target;
 	}
 
 	/**
 	 * @return if the regex is automatically reapplied on changes to the regex
 	 *         or target document
 	 */
 	public boolean isAutoRecalc()
 	{
 		return autoRecalc;
 	}
 
 	public void setAutoRecalc(boolean autoRecalc)
 	{
 		this.autoRecalc = autoRecalc;
 	}
 
 	/**
 	 * @return a list of whole regex matches from the last calculated match
 	 */
 	public ArrayList<int[]> getMatches()
 	{
 		return matches;
 	}
 
 	/**
 	 * @return a list of each set of groups from the last calculated match
 	 */
 	public ArrayList<int[][]> getGroups()
 	{
 		return groups;
 	}
 
 	/**
 	 * @return a list of each set of colors used for each group
 	 */
 	public ArrayList<Color[]> getGroupColors()
 	{
 		return groupColors;
 	}
 
 	public boolean addListener(RegexEventListener e)
 	{
 		return listeners.add(e);
 	}
 
 	public boolean removeListener(RegexEventListener e)
 	{
 		return listeners.remove(e);
 	}
 
 	public JTextPane getReplaceTarget()
 	{
 		return replaceTarget;
 	}
 
 	public void setReplaceTarget(JTextPane replaceTarget)
 	{
 		this.replaceTarget = replaceTarget;
 	}
 
 	public boolean isDoReplace()
 	{
 		return doReplace;
 	}
 
 	public void setDoReplace(boolean doReplace)
 	{
 		this.doReplace = doReplace;
 	}
 }
