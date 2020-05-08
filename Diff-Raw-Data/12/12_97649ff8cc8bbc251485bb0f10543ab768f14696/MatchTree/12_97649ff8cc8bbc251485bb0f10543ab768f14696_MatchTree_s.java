 package rxr;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 
 import rxr.MatchTree.MatchTreeModel.GroupNode;
 import rxr.MatchTree.MatchTreeModel.MatchNode;
 
 public class MatchTree extends JTree implements RegexEventListener
 {
 	RegexFieldListener listener;
 
 	MatchTreeModel model;
 
 	boolean autoExpand = false;
 
 	public MatchTree(RegexFieldListener listener)
 	{
 		super();
 		model = new MatchTreeModel(listener);
 		setModel(model);
 		this.listener = listener;
 		listener.addListener(this);
 		setRootVisible(false);
 		setCellRenderer(new MatchTreeNodeRenderer());
 		setShowsRootHandles(true);
 
 		addTreeSelectionListener(new TreeSelectionListener()
 		{
 			@Override
 			public void valueChanged(TreeSelectionEvent e)
 			{
 				Object end = e.getPath().getLastPathComponent();
 				if(end instanceof MatchNode)
 				{
 					MatchNode node = (MatchNode)end;
 					getListener().setOutlineRange(node.start, node.end);
 				}
 				else if(end instanceof GroupNode)
 				{
 					GroupNode node = (GroupNode)end;
 					getListener().setOutlineRange(node.start, node.end);
 				}
 			}
 		});
 	}
 
 	private RegexFieldListener getListener()
 	{
 		return listener;
 	}
 
 	@Override
 	public void regexEvent(Type t)
 	{
 		if(t == Type.BAD_PATTERN)
 		{
 			model.clear();
 			model.recalc();
 		}
 		if(t == Type.RECALC_START)
 		{
 			model.clear();
 			model.recalc();
 		}
 		if(t == Type.RECALC_COMPLETE)
 		{
 			model.matches = listener.getMatches();
 			model.groups = listener.getGroups();
 			model.groupColors = listener.getGroupColors();
 			model.recalc();
 		}
 		if(autoExpand)
 		{
 			expand();
 		}
 	}
 
 	/**
 	 * Completely expands the tree.
 	 */
 	public void expand()
 	{
 		for(int i = 0; i < this.getRowCount(); i++)
 		{
 			this.expandRow(i);
 		}
 	}
 
 	/**
 	 * Completely collapses the tree.
 	 */
 	public void collapse()
 	{
 		for(int i = 0; i < this.getRowCount(); i++)
 		{
 			this.collapseRow(i);
 		}
 	}
 
 	static class MatchTreeModel implements TreeModel
 	{
 		HashSet<TreeModelListener> listeners = new HashSet<TreeModelListener>();
 
 		ArrayList<int[]> matches = new ArrayList<int[]>();
 		ArrayList<int[][]> groups = new ArrayList<int[][]>();
 		ArrayList<Color[]> groupColors = new ArrayList<Color[]>();
 
 		ArrayList<MatchNode> matchNodes = new ArrayList<MatchNode>();
 
 		RegexFieldListener lis;
 
 		public MatchTreeModel(RegexFieldListener lis)
 		{
 			this.lis = lis;
 		}
 
 		public void clear()
 		{
 			matches = new ArrayList<int[]>();
 			groups = new ArrayList<int[][]>();
 			groupColors = new ArrayList<Color[]>();
 		}
 
 		/**
 		 * Recalculates matchNodes from the matches, groups, and groupColors
 		 * lists.
 		 */
 		public void recalc()
 		{
 			matchNodes.clear();
 			for(int i = 0; matches != null && i < matches.size(); i++)
 			{
 				String text = "";
 				try
 				{
 					text = lis.target.getDocument().getText(matches.get(i)[0], matches.get(i)[1] - matches.get(i)[0]);
 				}
 				catch(Exception e)
 				{
 					//do nothing
 				}
 
 				MatchNode mn = new MatchNode(matches.get(i)[0], matches.get(i)[1], i, text);
 
 				int[][] g = groups.get(i);
 
 				for(int j = 0; j < g.length; j++)
 				{
 					text = "";
 					try
 					{
 						text = lis.target.getDocument().getText(g[j][0], g[j][1] - g[j][0]);
 					}
 					catch(Exception e)
 					{
 						//do nothing
 					}
 					mn.groups.add(new GroupNode(g[j][0], g[j][1], j, groupColors.get(i)[j], text));
 				}
 				matchNodes.add(mn);
 			}
 
 			fireModelChanged();
 		}
 
 		public void fireModelChanged()
 		{
 			for(TreeModelListener l : listeners)
 			{
 				l.treeStructureChanged(new TreeModelEvent(this, new Object[] {this}));
 			}
 		}
 
 		@Override
 		public Object getChild(Object parent, int index)
 		{
 			if(parent == this)
 			{
 				return matchNodes.get(index);
 			}
 			else if(parent instanceof MatchNode)
 			{
 				return ((MatchNode)parent).groups.get(index);
 			}
 			else
 			{
 				return null;
 			}
 		}
 
 		@Override
 		public int getChildCount(Object parent)
 		{
 			if(parent == this)
 			{
 				return matchNodes.size();
 			}
 			else if(parent instanceof MatchNode)
 			{
 				return ((MatchNode)parent).groups.size();
 			}
 			else
 			{
 				return 0;
 			}
 		}
 
 		@Override
 		public int getIndexOfChild(Object parent, Object child)
 		{
 			if(parent == this)
 			{
 				return matchNodes.indexOf(child);
 			}
 			else if(parent instanceof MatchNode)
 			{
 				return ((MatchNode)parent).groups.indexOf(child);
 			}
 			else
 			{
 				return -1;
 			}
 		}
 
 		@Override
 		public Object getRoot()
 		{
 			return this;
 		}
 
 		@Override
 		public boolean isLeaf(Object node)
 		{
 			if(node == this)
 			{
 				return false;
 			}
 			else if(node instanceof MatchNode)
 			{
 				return false;
 			}
 			else if(node instanceof GroupNode)
 			{
 				return true;
 			}
 			else
 			{
 				return true;
 			}
 		}
 
 		@Override
 		public void valueForPathChanged(TreePath path, Object newValue)
 		{
 			//do nothing
 		}
 
 		@Override
 		public void addTreeModelListener(TreeModelListener l)
 		{
 			listeners.add(l);
 		}
 
 		@Override
 		public void removeTreeModelListener(TreeModelListener l)
 		{
 			listeners.remove(l);
 		}
 
 		/**
 		 * Represents a single match for a regex, in the target document.
 		 * Contains information on the whole match, as well as information for
 		 * each capture group within the match.
 		 */
 		class MatchNode
 		{
 			int start;
 			int end;
 			ArrayList<GroupNode> groups;
 			int index;
 			String text;
 
 			public MatchNode(int start, int end, int index, String text)
 			{
 				super();
 				this.start = start;
 				this.end = end;
 				this.index = index;
 				groups = new ArrayList<GroupNode>();
 				this.text = text;
 			}
 
 			@Override
 			public String toString()
 			{
 				return (index + 1) + ": " + text.substring(0, Math.min(text.length(), 50));
 			}
 		}
 
 		/**
 		 * Represents one group in a single match.
 		 */
 		class GroupNode
 		{
 			public GroupNode(int start, int end, int index, Color color, String text)
 			{
 				super();
 				this.start = start;
 				this.end = end;
 				this.index = index;
 				this.color = color;
 				this.text = text;
 			}
 
 			int start;
 			int end;
 			int index;
 			Color color;
 			String text;
 
 			@Override
 			public String toString()
 			{
 				return text;
 			}
 		}
 	}
 
 	/**
 	 * Used to render group icons as numbers and colors in the match tree.
 	 */
 	static class MatchTreeNodeRenderer extends DefaultTreeCellRenderer
 	{
 		static ColorIcon groupIcon = new ColorIcon();
 
 		public MatchTreeNodeRenderer()
 		{
 			setLeafIcon(groupIcon);
 		}
 
 		@Override
 		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
 		{
 			Component def = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 
 			if(value instanceof GroupNode)
 			{
 				groupIcon.color = ((GroupNode)value).color;
 				groupIcon.index = ((GroupNode)value).index;
 				setLeafIcon(groupIcon);
 				return this;
 			}
 
 			return def;
 		}
 
 		static class ColorIcon implements Icon
 		{
 			Color color = Color.BLACK;
 			int index = 0;
 
 			@Override
 			public void paintIcon(Component c, Graphics g, int x, int y)
 			{
 				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 				g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 192));
 				g.setColor(color);
 				g.fillOval(x + 1, y + 1, 14, 14);
 
 				g.setColor(new Color(0, 0, 0, 192));
 				g.drawOval(x + 1, y + 1, 14, 14);
 
 				g.setColor(Color.BLACK);
				String s = "" + (index + 1);
 				Rectangle2D b = g.getFontMetrics().getStringBounds(s, g);
 
				g.setFont(new Font("Tahoma", Font.PLAIN, 12));
 
				g.drawString(s, (int)((getIconWidth() - b.getWidth()) / 2) + 1, (int)(b.getHeight() + (getIconHeight() - b.getHeight()) / 2) - 2);
 			}
 
 			@Override
 			public int getIconHeight()
 			{
 				return 16;
 			}
 
 			@Override
 			public int getIconWidth()
 			{
 				return 16;
 			}
 		}
 	}
 }
