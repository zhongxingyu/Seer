 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2010 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.dataentry;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.TransferHandler;
 import javax.swing.border.LineBorder;
 import javax.swing.event.TableColumnModelEvent;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 import org.wwscc.dataentry.TableBase.SimpleDataTransfer;
 import org.wwscc.storage.Database;
 import org.wwscc.storage.Run;
 import org.wwscc.util.MT;
 import org.wwscc.util.MessageListener;
 import org.wwscc.util.Messenger;
 
 
 /**
  * Table used for DataEntry 
  */
 public class RunsTable extends TableBase implements MessageListener, ActionListener
 {
 	public RunsTable(EntryModel m)
 	{
 		super(m, new TimeRenderer(), new RunsTransferHandler(), 2, Integer.MAX_VALUE);
 		
 		InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cut"); // delete is same as Ctl+X
 		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter Time");
 		
 		registerKeyboardAction(
 			this,
 			"Enter Time",
 			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
 			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
 		);
 		
 		Messenger.register(MT.TIME_ENTERED, this);
 		Messenger.register(MT.COURSE_CHANGED, this);
 	}
 
 
 	public void setColumnSizes(TableColumnModelEvent e)
 	{
 		TableColumnModel tcm = (TableColumnModel)e.getSource();
 		for (int ii = 0; ii < tcm.getColumnCount(); ii++)
 		{
 			setColumnWidths(tcm.getColumn(ii), 70, 95, 200);
 		}
 		doLayout();
 	}
 
 
 	public void setSelectedRun(Run r) throws IndexOutOfBoundsException
 	{
 		int row = getSelectedRow();
 		int col = getSelectedColumn();
 
 		if ((row < 0) || (col < 0))
 			throw new IndexOutOfBoundsException("No table cell selected");
 		if ((row >= getRowCount()) || (col >= getColumnCount()))
 			throw new IndexOutOfBoundsException("Selection outside of table range");
 
 		setValueAt(r, row, col);
 
 		/* Advanced the selection point, to next driver with an open cell, select that cell */
 		int startrow = ++row;
 		int rowcount = getRowCount();
 
 		for (int jj = 0; jj < rowcount; jj++)
 		{
 			int selectedrow = (startrow + jj) % rowcount;
 
			for (int ii = 0; ii < getColumnCount(); ii++)
 			{
 				if (getValueAt(selectedrow, ii) == null)
 				{
 					getSelectionModel().setSelectionInterval(selectedrow, selectedrow);
 					getColumnModel().getSelectionModel().setSelectionInterval(ii, ii);
 					scrollTable(selectedrow, ii);
 					Messenger.sendEvent(MT.NEXT_TO_FINISH, getModel().getValueAt(selectedrow, 0)); // use model to get driver
 					return;
 				}
 			}
 		}
 		
 		/* Nowhere to go, clear selection */
 		getSelectionModel().clearSelection();
 		getColumnModel().getSelectionModel().clearSelection();
 		Messenger.sendEvent(MT.NEXT_TO_FINISH, null);
 	} 
 
 
 	@Override
 	public void event(MT type, Object o)
 	{
 		switch (type)
 		{
 			case TIME_ENTERED:
 				setSelectedRun((Run)o);
 				break;
 				
 			case COURSE_CHANGED:
 				JTableHeader h = getTableHeader();
 				if (Database.d.getCurrentCourse() > 1)
 				{
 					h.setForeground(Color.BLUE);
 					h.setBorder(new LineBorder(Color.BLUE));
 				}
 				else
 				{
 					h.setForeground(Color.BLACK);
 					h.setBorder(new LineBorder(Color.GRAY, 1));
 				}
 				break;
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Messenger.sendEvent(MT.TIME_ENTER_REQUEST, null);
 	}
 }
 
 
 /**
  * Cell Renderer for the Run type
  */
 class TimeRenderer extends DefaultTableCellRenderer
 {
 	private Color backgroundSelect;
 	private Color backgroundSelectNoFocus;
 	private Color backgroundDone;
 	private Color backgroundBest;
 	private NumberFormat df;
 
 	public TimeRenderer()
 	{
 		super();
 		backgroundSelect = new Color(0, 0, 255);
 		backgroundSelectNoFocus = new Color(190,190,255);
 		backgroundDone = new Color(200, 200, 200);
 		backgroundBest = new Color(255, 190, 80);
 	
 		setHorizontalAlignment(CENTER);
 		df = NumberFormat.getNumberInstance();
 		df.setMinimumFractionDigits(3);
 		df.setMaximumFractionDigits(3);
 	}
 
 
 	@Override
 	public Component getTableCellRendererComponent (JTable t, Object o, boolean isSelected, boolean hasFocus, int row, int column) 
 	{
 		Component cell = super.getTableCellRendererComponent(t, o, isSelected, hasFocus, row, column);
 
 		if (isSelected && t.hasFocus())
 		{
 			setBackground(backgroundSelect);
 		}
 		else if (isSelected)
 		{
 			setBackground(backgroundSelectNoFocus);
 			setBorder(new LineBorder(Color.RED, 2));
 		}
 		else
 		{
 			setBackground(Color.WHITE);
 		}
 
 		if (o instanceof Run)
 		{
 			Run r = (Run)o;
 			EntryModel m = (EntryModel)t.getModel();
 
 			if (!isSelected)
 			{
 				if (r.getNetOrder() == 1)
 					setBackground(backgroundBest);
 				else if (m.rowIsFull(row))
 					setBackground(backgroundDone);
 			}
 
 			String display = df.format(r.getRaw()) + " (" + r.getCones() + "," + r.getGates() + ")";
 			if (!r.isOK())
 				display= "<HTML><center>" + r.getStatus() + "<br><FONT size=-2>" + display;
 
 			setText(display);
 
 		}
 		else if (o != null)
 		{
 			setBackground(Color.red); /* This shouldn't happen */
 			setText(o.toString());
 		}
 		else
 		{
 			setText("");
 		}
 
 		return cell;
 	}
 }
 
 
 
 /**
  * Class to enable special DnD handling in our JTable.
  * Basically, this has boiled down to allow only drag movements (insertions)
  * in the driver column and copy/cut/paste in the runs columns
  */
 class RunsTransferHandler extends TransferHandler
 {
 	private static final Logger log = Logger.getLogger(RunsTransferHandler.class.getCanonicalName());
 	private static DataFlavor flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=org.wwscc.storage.Run", "RunData");
 	private int[] rowsidx = null;
 	private int[] colsidx = null;
 	private boolean isCut = false;
 
 	@Override
 	public int getSourceActions(JComponent c)
 	{
 		return COPY_OR_MOVE;
 	}
 
 	@Override
 	public void exportAsDrag(JComponent comp, InputEvent e, int action)
 	{
 		isCut = false;
 		super.exportAsDrag(comp, e, action);
 	} 
 
 	@Override
 	public void exportToClipboard(JComponent comp, Clipboard cb, int action)
 	{
 		isCut = true;
 		super.exportToClipboard(comp, cb, action);
 	}
 
 	/******* Export Side *******/
 
 	/* Create data from the selected rows and columns */
 	@Override
 	protected Transferable createTransferable(JComponent c)
 	{
 		JTable table = (JTable)c;
 		rowsidx = table.getSelectedRows();
 		colsidx = table.getSelectedColumns();
 
 		Run store[][] = new Run[rowsidx.length][colsidx.length];
 		for (int ii = 0; ii < rowsidx.length; ii++)
 			for (int jj = 0; jj < colsidx.length; jj++)
 				store[ii][jj] = (Run)table.getValueAt(rowsidx[ii], colsidx[jj]);
 
 		return new SimpleDataTransfer(flavor, store);
 	}
 
 	
 	@Override
 	protected void exportDone(JComponent c, Transferable data, int action)
 	{
 		if ((colsidx == null) || (rowsidx == null))
 			return;
 		if ((colsidx.length == 0) || (rowsidx.length == 0))
 			return;
 
 		/* MOVE means Drag or cut (use isCut to determine) */
 		if ((action == MOVE) && (isCut))
 		{
 			RunsTable t = (RunsTable)c;
 			log.log(Level.FINE, "cut run {0},{1}", new Object[]{rowsidx.length, colsidx.length});
 			for (int ii = 0; ii < rowsidx.length; ii++)
 				for (int jj = 0; jj < colsidx.length; jj++)
 					t.setValueAt(null, rowsidx[ii], colsidx[jj]);
 		}
 
 		rowsidx = null;
 		colsidx = null;
 	}
 
 
 	/******* Import Side *******/
 
 	/* Called to allow drop operations */
 	@Override
 	public boolean canImport(TransferHandler.TransferSupport support)
 	{
 		return false;
 	}
 
 
 	/* Called for drop and paste operations */
 	@Override
 	public boolean importData(TransferHandler.TransferSupport support)
 	{
 		try
 		{
 			Run newdata[][] = (Run[][])support.getTransferable().getTransferData(flavor);
 			JTable target = (JTable)support.getComponent();
 			int dr,dc;
 
 			if (!support.isDrop())
 			{
 				/* Set the data */
 				dr = target.getSelectedRow();
 				dc = target.getSelectedColumn();
 
 				for (int ii = 0; ii < newdata.length; ii++)
 					for (int jj = 0; jj < newdata[0].length; jj++)
 						target.setValueAt((newdata[ii][jj]).clone(), dr+ii, dc+jj);
 			}
 
 			return true;
 		}
 		catch (UnsupportedFlavorException ufe) { log.warning("Sorry, you pasted data I don't work with"); }
 		catch (IOException ioe) { log.log(Level.WARNING, "I/O Error during paste:{0}", ioe); }
 		catch (Exception e) { log.log(Level.WARNING, "General error during paste:{0}", e); }
 
 		return false;
 	}
 }
