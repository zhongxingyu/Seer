 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui.component;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.persistence.PersistenceException;
 import javax.swing.AbstractCellEditor;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.dao.EntityDAO;
 import eu.lp0.cursus.db.data.Entity;
 import eu.lp0.cursus.i18n.Messages;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.DatabaseError;
 
 public abstract class DeleteDatabaseColumn<T extends Entity> extends DatabaseColumn<T, String> {
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	private final EntityDAO<T> dao;
 	private final String action;
 	private final HeaderRenderer headerRenderer_;
 	private JTable table;
 	private JTableHeader header;
 	private DatabaseTableModel<T> model;
 
 	public DeleteDatabaseColumn(DatabaseWindow win, EntityDAO<T> dao, String action) {
 		super(null, win, dao);
 		this.dao = dao;
 		this.action = action;
 
		setMinWidth(25);
		setPreferredWidth(25);
		setMaxWidth(25);

 		headerRenderer = headerRenderer_ = new HeaderRenderer();
 		cellRenderer = new CellRenderer();
 		cellEditor = new CellEditor();
 	}
 
 	@Override
 	public void setupModel(JTable table, DatabaseTableModel<T> model, TableRowSorter<? super TableModel> sorter) {
 		super.setupModel(table, model, sorter);
 
 		this.table = table;
 		this.header = table.getTableHeader();
 		this.model = model;
 
 		header.addMouseListener(headerRenderer_);
 		header.addMouseMotionListener(headerRenderer_);
 		header.addKeyListener(headerRenderer_);
 
 		sorter.setSortable(getModelIndex(), false);
 	}
 
 	protected abstract T newRow();
 
 	public void addRow() {
 		assert (SwingUtilities.isEventDispatchThread());
 		T row = null;
 
 		win.getDatabase().startSession();
 		try {
 			DatabaseSession.begin();
 
 			row = newRow();
 			dao.persist(row);
 
 			DatabaseSession.commit();
 		} catch (PersistenceException e) {
 			if (row != null) {
 				log.error(String.format("Unable to save row: %s#%d", row.getClass().getSimpleName(), row.getId()), e); //$NON-NLS-1$
 			}
 			DatabaseError.errorSaving(win.getFrame(), Constants.APP_NAME, e);
 		} finally {
 			win.getDatabase().endSession();
 		}
 
 		int mRow = model.getRowCount();
 		model.addRow(row);
 		int vRow = table.convertRowIndexToView(mRow);
 		table.getSelectionModel().setSelectionInterval(vRow, vRow);
 		table.scrollRectToVisible(table.getCellRect(mRow, table.convertColumnIndexToView(getModelIndex()), true));
 	}
 
 	protected boolean confirmDelete(T row) {
 		String value = getValue(row, false);
 		switch (JOptionPane.showConfirmDialog(win.getFrame(), Messages.getString(action + ".confirm", value), //$NON-NLS-1$
 				Messages.getString(action) + Constants.EN_DASH + value, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
 		case JOptionPane.YES_OPTION:
 			return true;
 		case JOptionPane.NO_OPTION:
 		case JOptionPane.CLOSED_OPTION:
 		default:
 			return false;
 		}
 	}
 
 	protected boolean deleteRow(T row) {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		win.getDatabase().startSession();
 		try {
 			DatabaseSession.begin();
 
 			T item = dao.get(row);
 			dao.remove(item);
 
 			DatabaseSession.commit();
 			return true;
 		} catch (PersistenceException e) {
 			log.error(String.format("Unable to delete row: row=%s#%d", row.getClass().getSimpleName(), row.getId()), e); //$NON-NLS-1$
 			DatabaseError.errorSaving(win.getFrame(), Constants.APP_NAME, e);
 			return false;
 		} finally {
 			win.getDatabase().endSession();
 		}
 	}
 
 	private class HeaderRenderer extends DefaultTableCellRenderer implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
 		private final CellJButton button = new CellJButton("+"); //$NON-NLS-1$
 
 		public HeaderRenderer() {
 			button.addActionListener(this);
 		}
 
 		@Override
 		@SuppressWarnings("hiding")
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int vRow, int vCol) {
 			button.setSelected(isSelected);
 			button.setFocus(hasFocus);
 			return button;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			addRow();
 		}
 
 		private boolean ourColumn(MouseEvent me) {
 			return table.isEnabled() && getModelIndex() == table.convertColumnIndexToModel(header.columnAtPoint(me.getPoint()));
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent me) {
 			// If the mouse moves onto the header, do nothing as the mouseMoved event will be triggered
 		}
 
 		@Override
 		public void mousePressed(MouseEvent me) {
 			if (me.isConsumed()) {
 				return;
 			}
 			// Only depress the button if the mouse is over our column
 			if (ourColumn(me)) {
 				button.getModel().setArmed(true);
 				button.getModel().setPressed(true);
 				me.consume();
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent me) {
 			if (me.isConsumed()) {
 				return;
 			}
 			// Always depress the button as the mouse may now be over another column
 			button.getModel().setPressed(false);
 			me.consume();
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent me) {
 			// Do nothing as the button should have been successfully pressed while armed
 		}
 
 		@Override
 		public void mouseExited(MouseEvent me) {
 			if (me.isConsumed()) {
 				return;
 			}
 			// If the mouse moves off the header, disarm the button
 			button.getModel().setArmed(false);
 			me.consume();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent me) {
 			if (me.isConsumed()) {
 				return;
 			}
 			// Can't check the column here:
 			// it would work for the original location,
 			// but also for the new location... so it'd
 			// be clicked on column moves
 			button.getModel().setArmed(false);
 			me.consume();
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent me) {
 			if (me.isConsumed()) {
 				return;
 			}
 			// If the mouse moves over our column, arm the button
 			if (ourColumn(me)) {
 				button.getModel().setArmed(true);
 			} else {
 				button.getModel().setArmed(false);
 			}
 			me.consume();
 		}
 
 		@Override
 		public void keyTyped(KeyEvent ke) {
 			if (table.isEnabled() && button.hasFakeFocus()) {
 				button.dispatchEvent(ke);
 			}
 		}
 
 		@Override
 		public void keyPressed(KeyEvent ke) {
 			// There's a side effect here in that the button won't
 			// get the key press that was used to change focus
 			if (table.isEnabled() && button.hasFakeFocus()) {
 				// This works but the button's not really visible
 				// so there's no visual feedback when activated
 				button.dispatchEvent(ke);
 			}
 		}
 
 		@Override
 		public void keyReleased(KeyEvent ke) {
 			// There's a side effect here in that the button won't
 			// get the key release that was used to change focus
 			if (table.isEnabled() && button.hasFakeFocus()) {
 				button.dispatchEvent(ke);
 			}
 		}
 	}
 
 	@Override
 	public TableCellRenderer getCellRenderer() {
 		return new CellRenderer();
 	}
 
 	private class CellJButton extends JButton {
 		private final Color fg = getForeground();
 		private final Color bg = getBackground();
 		private boolean focus;
 		private boolean painting;
 
 		public CellJButton(String text) {
 			super(text);
 		}
 
 		public void setFocus(boolean focus) {
 			this.focus = focus;
 		}
 
 		public boolean hasFakeFocus() {
 			return focus;
 		}
 
 		@Override
 		public void paint(Graphics g) {
 			painting = true;
 			super.paint(g);
 			painting = false;
 		}
 
 		@Override
 		public boolean isFocusOwner() {
 			return painting && focus || (isFocusable() && super.isFocusOwner());
 		}
 
 		public void setSelected(JTable table, boolean selected) {
 			if (selected) {
 				setForeground(table.getSelectionForeground());
 				setBackground(table.getSelectionBackground());
 			} else {
 				setForeground(fg);
 				setBackground(bg);
 			}
 		}
 
 		@Override
 		public void revalidate() {
 		}
 
 		@Override
 		public void validate() {
 		}
 	}
 
 	private class CellRenderer extends DefaultTableCellRenderer {
 		private final CellJButton button = new CellJButton("-"); //$NON-NLS-1$
 
 		public CellRenderer() {
 			setOpaque(true);
 			button.setFocusable(false);
 		}
 
 		@Override
 		@SuppressWarnings("hiding")
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int vRow, int vCol) {
 			button.setSelected(table, isSelected);
 			button.setFocus(hasFocus);
 			return button;
 		}
 	}
 
 	@Override
 	protected boolean setValue(T row, String value) {
 		throw new UnsupportedOperationException();
 	}
 
 	private class CellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
 		private final CellJButton button = new CellJButton("-"); //$NON-NLS-1$
 		private Integer mRow;
 		private T mVal;
 
 		public CellEditor() {
 			button.setFocus(true);
 			button.addActionListener(this);
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public Component getTableCellEditorComponent(@SuppressWarnings("hiding") JTable table, Object value, boolean isSelected, int vRow, int vCol) {
 			mRow = table.convertRowIndexToModel(vRow);
 			mVal = (T)value;
 			return button;
 		}
 
 		@Override
 		public Object getCellEditorValue() {
 			return mVal;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (confirmDelete(mVal)) {
 				if (deleteRow(mVal)) {
 					super.stopCellEditing();
 					model.deleteRow(mRow);
 				}
 			}
 		}
 
 		@Override
 		public void cancelCellEditing() {
 			super.cancelCellEditing();
 			model = null;
 			mRow = null;
 			mVal = null;
 		}
 
 		@Override
 		public boolean stopCellEditing() {
 			try {
 				return super.stopCellEditing();
 			} finally {
 				model = null;
 				mRow = null;
 				mVal = null;
 			}
 		}
 	}
 }
