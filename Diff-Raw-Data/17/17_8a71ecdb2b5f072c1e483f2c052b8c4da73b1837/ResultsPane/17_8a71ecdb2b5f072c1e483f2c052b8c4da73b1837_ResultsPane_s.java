 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 
 package org.wwscc.dataentry;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.LineBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumnModel;
 import net.miginfocom.swing.MigLayout;
 import org.wwscc.storage.Database;
 import org.wwscc.storage.Entrant;
 import org.wwscc.storage.EventResult;
 import org.wwscc.storage.Run;
 
 
 /** 
  * Overall class that represents the panel in the list of tabs
  */
 public class ResultsPane extends JPanel
 {
 	static final NumberFormat df;
 	static
 	{
 		df = NumberFormat.getNumberInstance();
 		df.setMinimumFractionDigits(3);
 		df.setMaximumFractionDigits(3);
 	}
 
 	JLabel nameLabel;
 	JLabel detailsLabel;
 	JLabel classLabel;
 	
 	JTable nameTable;
 	JTable details;
 	JTable classTable;
 
 	EntrantResultModel nameModel;
 	RandomThoughtsModel detailsModel;
 	EventResultModel classModel;
 
 	HighlightRenderer renderer;
 	DecimalFormat diffForm;
 
 	public ResultsPane()
 	{
 		super(new MigLayout("gap 0, ins 2", "fill", "fill"));
 		Color back = new Color(200, 200, 244);
 		
 		diffForm = new DecimalFormat("##0.000");
 		diffForm.setPositivePrefix("+");
 
 		nameLabel = new JLabel("Driver", SwingConstants.CENTER);
 		nameLabel.setFont(new Font("dialog", Font.BOLD, 16));
 		nameLabel.setBorder(new LineBorder(Color.GRAY));
 		nameLabel.setBackground(back);
 		nameLabel.setOpaque(true);
 
 		nameModel = new EntrantResultModel();
 		
 		nameTable = new JTable(nameModel);
 		nameTable.setDefaultRenderer(Object.class, new EntrantResultRenderer());
 		nameTable.setRowHeight(20);
 		nameTable.setRowSelectionAllowed(false);
 		nameTable.setColumnSelectionAllowed(false);
 		TableColumnModel cm1 = nameTable.getColumnModel();
 		cm1.getColumn(0).setPreferredWidth(250);
 		cm1.getColumn(1).setPreferredWidth(50);
 		cm1.getColumn(2).setPreferredWidth(50);
 		cm1.getColumn(3).setPreferredWidth(250);
 
 		detailsLabel = new JLabel("Last Run", JLabel.CENTER);
 		detailsLabel.setFont(new Font("dialog", Font.BOLD, 16));
 		detailsLabel.setBorder(new LineBorder(Color.GRAY));
 		detailsLabel.setBackground(back);
 		detailsLabel.setOpaque(true);
 
 		detailsModel = new RandomThoughtsModel();
 		
 		details = new JTable(detailsModel);
 		details.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
 			Font myfont = ((Font)UIManager.get("Label.font")).deriveFont(Font.PLAIN, 12.0f);
 			public Font getFont() { return myfont; }
 		});
 		details.setTableHeader(null);
 		details.setRowHeight(22);
 		details.setRowSelectionAllowed(false);
 		details.setColumnSelectionAllowed(false);
 		TableColumnModel cm2 = details.getColumnModel();
 		cm2.getColumn(0).setPreferredWidth(250);
 		cm2.getColumn(1).setPreferredWidth(250);
 
 		classLabel = new JLabel("", SwingConstants.CENTER);
 		classLabel.setFont(new Font("dialog", Font.BOLD, 16));
 		classLabel.setBorder(new LineBorder(Color.GRAY));
 		classLabel.setBackground(back);
 		classLabel.setOpaque(true);
 		classLabel.setText("Class");
 
 		classModel = new EventResultModel();
 		classTable = new JTable(classModel);
 
 		renderer = new HighlightRenderer();
 		classTable.setDefaultRenderer(Object.class, renderer);
 		classTable.setRowHeight(20);
 		classTable.setRowSelectionAllowed(false);
 		classTable.setColumnSelectionAllowed(false);
 
 		TableColumnModel cm = classTable.getColumnModel();
 		cm.getColumn(0).setPreferredWidth(50);
 		cm.getColumn(1).setPreferredWidth(500);
 		cm.getColumn(2).setPreferredWidth(80);
 		cm.getColumn(3).setPreferredWidth(250);
 
 		add(nameLabel, "wrap");
 		add(nameTable.getTableHeader(), "wrap");
 		add(nameTable, "wrap");
 		add(detailsLabel, "gaptop 10px, wrap");
 		add(details, "wrap");
 		add(classLabel, "gaptop 10px, wrap");
 		JScrollPane scroller = new JScrollPane(classTable);
 		scroller.setColumnHeader(null);
 		scroller.setColumnHeaderView(null);
 		add(scroller, "grow");
 		
 		setOpaque(true); 
 		setBackground(Color.WHITE);
 	}
 
 	
 	public void updateDisplayData(Entrant e, boolean showLast)
 	{
 		String classcode = e.getClassCode();
 		List<EventResult> erlist = Database.d.getResultsForClass(classcode);
 		
 		nameLabel.setText(e.getName());
 		nameModel.setData(e);
 		if (showLast)
 			detailsLabel.setText("Last Run");			
 		else
 			detailsLabel.setText("Difference");
 
 		detailsModel.setData(erlist, e, showLast);
 		
 		classLabel.setText(classcode);
 		classModel.setData(erlist);
 		renderer.setHighlightValue(e.getFirstName() + " " + e.getLastName());
 	}
 
 
 	/**
 	 * render for entrant results table
 	 */
 	class EntrantResultRenderer extends DefaultTableCellRenderer
 	{
 		String match;
 		Font regular;
 		Font bold;
 		Color mygray;
 		Color myred;
 
 		public EntrantResultRenderer()
 		{
 			super();
 			match = "";
 			regular = (Font)UIManager.get("Label.font");
 			bold = ((Font)UIManager.get("Label.font")).deriveFont(Font.BOLD, 12.0f);
 			mygray = new Color(240,240,240);
 			myred = new Color(240,170,170);
 		}
 
 		public Component getTableCellRendererComponent (JTable t, Object o, boolean is, boolean hf, int r, int c)
 		{
 			JLabel cell = (JLabel)super.getTableCellRendererComponent(t, o, is, hf, r, c);
 			cell.setHorizontalAlignment(JLabel.CENTER);
 
 			EntrantResultModel m = (EntrantResultModel)t.getModel();
 			Run run = m.getRun(r);
 			if ((run != null) && (run.getNetOrder() == 1))
 			{
 				cell.setFont(bold);
 				cell.setBackground(mygray);
 			}
 			else if ((run != null) && (run.getRawOrder() == 1) && c == 0)
 			{
 				cell.setFont(bold);
 				cell.setBackground(myred);
 			}
 			else
 			{
 				cell.setFont(regular);
 				cell.setBackground(Color.WHITE);
 			}
 
 			return cell;
 		}
 	}
 
 
 	/**
 	 * Special render that causes the row that was just updated to be bold and of a larger font
 	 */
 	class HighlightRenderer extends DefaultTableCellRenderer
 	{
 		String match;
 		Font regular;
 		Font bold;
 		Color mygray;
 
 		public HighlightRenderer()
 		{
 			super();
 			match = "";
 			regular = (Font)UIManager.get("Label.font");
 			bold = ((Font)UIManager.get("Label.font")).deriveFont(Font.BOLD, 12.0f);
 			mygray = new Color(240,240,240);
 		}
 
 		public void setHighlightValue(String s)
 		{
 			match = s;
 		}
 
 		public Component getTableCellRendererComponent (JTable t, Object o, boolean is, boolean hf, int r, int c)
 		{
 			Component cell = super.getTableCellRendererComponent(t, o, is, hf, r, c);
 
 			EventResultModel m = (EventResultModel)t.getModel();
 			String s = (String)m.getValueAt(r, 1);
 			if (s.equals(match))
 			{
 				cell.setFont(bold);
 				cell.setBackground(mygray);
 			}
 			else
 			{
 				cell.setFont(regular);
 				cell.setBackground(Color.WHITE);
 			}
 
 			return cell;
 		}
 	}
 
 
 	/**
 	 * Data model to hold results for a single entrant
 	 */
 	class EntrantResultModel extends AbstractTableModel
 	{
 		Entrant data;
 
 		public EntrantResultModel()
 		{
 			data = null;
 		}
 
 		public void setData(Entrant e)
 		{
 			data = e;
 			fireTableDataChanged();
 		}
 
 		public Run getRun(int row)
 		{
 			if (data == null)
 				return null;
 			return data.getRun(row+1);
 		}
 
 		public int getRowCount()
 		{
 			if (data == null) return 0;
 			return data.runCount();
 		}
 
 		public int getColumnCount()
 		{
 			return 4;
 		}
 
 		public String getColumnName(int col)
 		{
 			switch (col)
 			{
 				case 0: return "Raw";
 				case 1: return "C";
 				case 2: return "G";
 				case 3: return "Net";
 			}
 			return "";
 		}
 
 		public Object getValueAt(int row, int col)
 		{
 			Run r = null;
 			if (data == null) return "";
 			r = data.getRun(row+1);
 			if (r == null) return "";
 
 			switch (col)
 			{
 				case 0: return df.format(r.getRaw());
 				case 1: return r.getCones();
 				case 2: return r.getGates();
 				case 3: return df.format(r.getNet());
 			}
 			return null;
 		}
 	}
 
 	private static final int MOVE=2, FIRST=1, NEXT=0, RAW=3, NET=4; 
 	class RandomThoughtsModel extends AbstractTableModel
 	{
 		Integer origpos = null;
 		Double rawImprovement = null, netImprovement = null;
 		EventResult myresult = null;
 		EventResult topresult = null;
 		boolean showLast = true;
 		
 		@Override
 		public int getRowCount() { return showLast ? 5 : 2; }
 		@Override
 		public int getColumnCount() { return 2; }
 		@Override
 		public Object getValueAt(int row, int col)
 		{
 			if (col == 0)
 			{
 				System.out.println("");
 				switch (row)
 				{
 					case  MOVE: return "Movement";
 					case FIRST: return "From First (Raw)";
 					case  NEXT: return "From Next (Raw)";
 					case   RAW: return "Raw Improvement";
 					case   NET: return "Net Improvement";
 				}
 			}
 			else
 			{
 				if (myresult == null)
 					return "";
 				switch (row)
 				{
 					case MOVE: 
 						if ((origpos != null) && (origpos != myresult.getPosition()))
 							return origpos + " to " + myresult.getPosition();
 						return "none";
 					case NEXT:
 						if (myresult.getPosition() != 1)
 							return df.format(myresult.getDiff());
 						return "";
 					case FIRST: 
 						if (myresult.getPosition() != 1)
 							return df.format((myresult.getSum() - topresult.getSum())/myresult.getIndex());
 						return "";
 					case RAW: 
 						if ((rawImprovement != null))
 							return df.format(rawImprovement);
 						return "";
 					case NET: 
 						if ((netImprovement != null))
 							return df.format(netImprovement);
 						return "";
 				}
 			}
 
 			return "";
 		}
 		
 		public void setData(List<EventResult> erlist, Entrant entrant, boolean showlast)
 		{
 			origpos = null;
 			rawImprovement = null;
 			netImprovement = null;
 			myresult = null;
 			topresult = erlist.get(0);
 			showLast = showlast;
 			
 			Run[] runs = entrant.getRuns();
 			Run one, two, last;
 			if (runs.length <= 1)
 			{
 				fireTableDataChanged();
 				return;
 			}
 
 			one = two = last = runs[runs.length-1];
 			for (int ii = 0; ii < runs.length; ii++)
 			{
 				if (runs[ii] == null)
 					continue;
 
 				if (runs[ii].getNetOrder() == 1)
 					one = runs[ii];
 				else if (runs[ii].getNetOrder() == 2)
 					two = runs[ii];
 			}
 
 			for (EventResult er : erlist) {
 				if (er.getCarId() == entrant.getCarId()) {
 					myresult = er;
 					break;
 			}}
 						
 			if (last.run() == one.run())
 			{
 				for (EventResult er : erlist) {
 					if (er.getSum() > two.getNet()) {
 						origpos = er.getPosition();
 						break;
 				}}
 				if (origpos == null)
 					origpos = erlist.size();
 			}
 
 			if (last.run() != one.run())
 			{
 				double rdiff = last.getRaw() - one.getRaw();
 				if (rdiff < 0)
 					rawImprovement = -rdiff;
 			}
 			else
 			{
 				double ndiff = two.getNet() - last.getNet();
 				double rdiff = two.getRaw() - last.getRaw();
 				if (rdiff < 0)
 				{
 					netImprovement = ndiff;
 				}
 				else
 				{
 					rawImprovement = rdiff;
 					netImprovement = ndiff;
 				}
 			}
 			
 			fireTableDataChanged();
 		}
 	}
 	
 	/**
 	 * Data model to hold results for a single class
 	 */
 	class EventResultModel extends AbstractTableModel
 	{
 		List<EventResult> data;
 		List<Double> toFirstRaw;
 		List<Double> toFirstIndex;
 
 		public EventResultModel()
 		{
 			data = new ArrayList<EventResult>();
 			toFirstRaw = new ArrayList<Double>();
 			toFirstIndex = new ArrayList<Double>();
 		}
 
 		public void setData(List<EventResult> v)
 		{
 			if (v != null)
 			{
 				data = v;
 				toFirstRaw = new ArrayList<Double>();
 				toFirstIndex = new ArrayList<Double>();
				double topindex = data.get(0).getSum();
 				
 				for (EventResult r : data)
 				{
 					double tofirst = r.getSum() - topindex;
 					toFirstIndex.add(tofirst);
 					toFirstRaw.add(tofirst/r.getIndex());
 				}
 			}
 			fireTableDataChanged();
 		}
 
 		public int getRowCount()
 		{
 			return data.size();
 		}
 
 		public int getColumnCount()
 		{
 			return 4;
 		}
 
 		public String getColumnName(int col)
 		{
 			switch (col)
 			{
 				case 0: return "";
 				case 1: return "Name";
 				case 2: return "";
 				case 3: return "Net";
 			}
 			return "";
 		}
 
 		public Object getValueAt(int row, int col)
 		{
 			EventResult e = data.get(row);
 			switch (col)
 			{
 				case 0: return new Integer(row+1);
 				case 1: return e.getFullName();
 				case 2: return e.getIndexCode();
 				case 3: return df.format(e.getSum());
 			}
 			return null;
 		}
 	}
 }
 
 
