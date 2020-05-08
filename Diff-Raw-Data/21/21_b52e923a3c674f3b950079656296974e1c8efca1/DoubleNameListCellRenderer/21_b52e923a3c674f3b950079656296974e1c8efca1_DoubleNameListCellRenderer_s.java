 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import util.SwingUtil;
 
 public class DoubleNameListCellRenderer extends JPanel implements ListCellRenderer
 {
 	public static interface DoubleNameElement
 	{
 		public String getFirstName();
 
 		public String getSecondName();
 	}
 
 	public static class DefaultDoubleNameElement implements DoubleNameElement
 	{
 
 		private String first;
 		private String second;
 
 		public DefaultDoubleNameElement(String first, String second)
 		{
 			this.first = first;
 			this.second = second;
 		}
 
 		@Override
 		public String getFirstName()
 		{
 			return first;
 		}
 
 		@Override
 		public String getSecondName()
 		{
 			return second;
 		}
 	}
 
 	JLabel l1 = new JLabel();
 	JLabel l2 = new JLabel();
 	private ListModel model;
 	private boolean dirty = true;
 	private int l1Width;
 	private int MAX_L1_WIDTH = Integer.MAX_VALUE;
 
 	public DoubleNameListCellRenderer(ListModel model)
 	{
 		super(new BorderLayout(0, 0));
 		l1.setBorder(new EmptyBorder(1, 1, 1, 1));
 		l2.setBorder(new EmptyBorder(1, 1, 1, 1));
 		l1.setHorizontalTextPosition(SwingConstants.LEFT);
 		l2.setHorizontalTextPosition(SwingConstants.LEFT);
 		setBorder(new EmptyBorder(0, 0, 0, 0));
 		add(l1, BorderLayout.WEST);
 		add(l2, BorderLayout.CENTER);
 		setForeground(getForeground());
 
 		this.model = model;
 		model.addListDataListener(new ListDataListener()
 		{
 
 			@Override
 			public void intervalRemoved(ListDataEvent e)
 			{
 				dirty = true;
 			}
 
 			@Override
 			public void intervalAdded(ListDataEvent e)
 			{
 				dirty = true;
 			}
 
 			@Override
 			public void contentsChanged(ListDataEvent e)
 			{
 				dirty = true;
 			}
 		});
 	}
 
 	public int getRowHeight()
 	{
 		if (l1.getText().length() == 0)
 			l1.setText(" ");
 		return l1.getPreferredSize().height;
 	}
 
 	private void updateLeftLabelSize()
 	{
 		JLabel l = new JLabel();
 		l.setBorder(new EmptyBorder(1, 1, 1, 1));
 		l.setFont(getFontLabel1());
 		l1Width = 0;
 		for (int i = 0; i < model.getSize(); i++)
 		{
 			String val = ((DoubleNameElement) model.getElementAt(i)).getFirstName();
 			if (val != null && val.length() > 0)
 			{
 				l.setText(val + " ");
 				l1Width = (int) Math.min(MAX_L1_WIDTH, Math.max(l1Width, l.getPreferredSize().width));
 			}
 		}
 		dirty = false;
 	}
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
 			boolean cellHasFocus)
 	{
 		if (dirty)
 			updateLeftLabelSize();
 		DoubleNameElement e = ((DoubleNameElement) value);
 		l1.setText(e.getFirstName());
 		l1.setPreferredSize(null);
 		l1.setPreferredSize(new Dimension(l1Width, l1.getPreferredSize().height));
 		l2.setText(e.getSecondName());
 		return this;
 	}
 
 	public void setForeground(Color c)
 	{
 		if (l1 != null)
 		{
 			l1.setForeground(c);
 			l2.setForeground(c);
 		}
 	}
 
 	public void setForegroundLabel1(Color col)
 	{
 		l1.setForeground(col);
 	}
 
 	public Font getFontLabel1()
 	{
 		if (l1 == null)
 			return null;
 		return l1.getFont();
 	}
 
 	public void setFontLabel1(Font font)
 	{
 		l1.setFont(font);
 		dirty = true;
 	}
 
 	public void setForegroundLabel2(Color col)
 	{
 		l2.setForeground(col);
 	}
 
 	public Font getFontLabel2()
 	{
 		if (l2 == null)
 			return null;
 		return l2.getFont();
 	}
 
 	public void setFontLabel2(Font font)
 	{
 		l2.setFont(font);
 	}
 
 	public static void main(String args[])
 	{
 
 		JList l = new JList(new DoubleNameElement[] { new DefaultDoubleNameElement("a", "b"),
 				new DefaultDoubleNameElement("bla", "blue"), new DefaultDoubleNameElement("ene", "mene") });
 		l.setCellRenderer(new DoubleNameListCellRenderer(l.getModel()));
 		SwingUtil.showInDialog(new JScrollPane(l));
 		System.exit(0);
 	}
 
 }
