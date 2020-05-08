 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parsers.sql.dsql.ui;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.Category;
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Link;
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.appengine.api.datastore.PostalAddress;
 import com.google.appengine.api.datastore.Rating;
 import com.google.appengine.api.datastore.ShortBlob;
 import com.google.appengine.api.datastore.Text;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Rectangle2D;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.AbstractCellEditor;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.TransferHandler;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 import org.vesalainen.parsers.magic.Magic;
 import org.vesalainen.parsers.magic.Magic.MagicResult;
 import org.vesalainen.parsers.sql.dsql.GObjectHelper;
 
 /**
  * @author Timo Vesalainen
  */
 public class DSJTable extends JTable
 {
     private static final Pattern NUMERIC = Pattern.compile("[0-9\\,\\.\\- ]+");
     private static final Magic magic = Magic.getInstance();
     
     private Window owner;
     
     public DSJTable(Object[][] rowData, Object[] columnNames)
     {
         super(rowData, columnNames);
         init();
     }
 
     public DSJTable(Vector rowData, Vector columnNames)
     {
         super(rowData, columnNames);
         init();
     }
 
     public DSJTable(int numRows, int numColumns)
     {
         super(numRows, numColumns);
         init();
     }
 
     public DSJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
     {
         super(dm, cm, sm);
         init();
     }
 
     public DSJTable(TableModel dm, TableColumnModel cm)
     {
         super(dm, cm);
         init();
     }
 
     public DSJTable(TableModel dm)
     {
         super(dm);
         init();
     }
 
     public DSJTable()
     {
         init();
     }
 
     public void setOwner(Window owner)
     {
         this.owner = owner;
     }
 
     private void init()
     {
         MyTransferHandler myTransferHandler = new MyTransferHandler(getTransferHandler());
         setTransferHandler(myTransferHandler);
         ActionMap actionMap = getActionMap();
         Action copyAction = MyTransferHandler.getCopyAction();
         actionMap.put(I18n.get("COPY"), copyAction);
         
         setAutoCreateRowSorter(true);
         setRowSelectionAllowed(true);
         setDragEnabled(true);
         
         setDefaultEditor(ComboBoxModel.class, new ComboBoxModelCellEditor());
         setDefaultEditor(GeoPt.class, new GoogleObjectCellEditor(GeoPt.class));
         setDefaultEditor(ShortBlob.class, new ShortBlobCellEditor());
         setDefaultEditor(Blob.class, new BlobCellEditor());
         setDefaultEditor(Rating.class, new RatingCellEditor());
         setDefaultEditor(PostalAddress.class, new GoogleObjectCellEditor(PostalAddress.class));
         setDefaultEditor(PhoneNumber.class, new GoogleObjectCellEditor(PhoneNumber.class));
         setDefaultEditor(Link.class, new GoogleObjectCellEditor(Link.class));
         setDefaultEditor(Text.class, new TextCellEditor());
         setDefaultEditor(Email.class, new GoogleObjectCellEditor(Email.class));
         setDefaultEditor(Category.class, new GoogleObjectCellEditor(Category.class));
         
         setDefaultRenderer(ComboBoxModel.class, new ComboBoxModelCellRenderer());
         setDefaultRenderer(GeoPt.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(ShortBlob.class, new ShortBlobTableCellRenderer());
         setDefaultRenderer(Blob.class, new BlobTableCellRenderer());
         setDefaultRenderer(Rating.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(PostalAddress.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(PhoneNumber.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(Link.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(Text.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(Email.class, new GoogleObjectTableCellRenderer());
         setDefaultRenderer(Category.class, new GoogleObjectTableCellRenderer());
     }
 
     @Override
     public void print(Graphics g)
     {
         int totalColumnWidth = columnModel.getTotalColumnWidth();
         Graphics2D gg = (Graphics2D) g;
         FontRenderContext fontRenderContext = gg.getFontRenderContext();
         for (int col=0;col<columnModel.getColumnCount();col++)
         {
             TableColumn column = columnModel.getColumn(col);
             int max = 0;
             boolean numeric = true;
             for (int row=0;row<getRowCount();row++)
             {
                 Object value = dataModel.getValueAt(row, col);
                 String str = value != null ? getString(value) : "";
                 Matcher matcher = NUMERIC.matcher(str);
                 if (!matcher.matches())
                 {
                     numeric = false;
                 }
                 TableCellRenderer cellRenderer = getCellRenderer(row, col);
                 Component component = cellRenderer.getTableCellRendererComponent(this, value, false, false, row, col);
                 Font font = component.getFont();
                 Rectangle2D stringBounds = font.getStringBounds(str, fontRenderContext);
                 max = Math.max(max, (int)(1.5*stringBounds.getWidth()));
             }
             if (numeric)
             {
                 column.setMaxWidth(max);
             }
             else
             {
                 column.setMinWidth(0);
                 column.setMaxWidth(max);
             }
         }
         int left = totalColumnWidth - columnModel.getTotalColumnWidth();
         int hiddenTotal = 0;
         for (int col=0;col<columnModel.getColumnCount();col++)
         {
             TableColumn column = columnModel.getColumn(col);
             hiddenTotal += column.getMaxWidth()-column.getWidth();
         }
         float ratio = (float)left/(float)hiddenTotal;
         for (int col=0;col<columnModel.getColumnCount();col++)
         {
             TableColumn column = columnModel.getColumn(col);
             int hidden = column.getMaxWidth()-column.getWidth();
             if (hidden > 0)
             {
                 column.setMinWidth(column.getWidth()+(int)(ratio*(float)hidden));
             }
         }
         totalColumnWidth = columnModel.getTotalColumnWidth();
         revalidate();
         super.paint(g);
     }
 
     private String getString(Object value)
     {
         if (value instanceof Date)
         {
             DateFormat df = DateFormat.getDateTimeInstance();
             return df.format(value);
         }
         return GObjectHelper.getString(value);
     }
     
     private static class MyTransferHandler extends TransferHandler implements ClipboardOwner
     {
         private TransferHandler transferHandler;
         public MyTransferHandler(TransferHandler transferHandler)
         {
             this.transferHandler = transferHandler;
         }
 
         @Override
         protected Transferable createTransferable(JComponent c)
         {
             return new MyTransferable((JTable)c);
         }
 
         @Override
         public void setDragImage(Image img)
         {
             transferHandler.setDragImage(img);
         }
 
         @Override
         public Image getDragImage()
         {
             return transferHandler.getDragImage();
         }
 
         @Override
         public void setDragImageOffset(Point p)
         {
             transferHandler.setDragImageOffset(p);
         }
 
         @Override
         public Point getDragImageOffset()
         {
             return transferHandler.getDragImageOffset();
         }
 
         @Override
         public void exportAsDrag(JComponent comp, InputEvent e, int action)
         {
             transferHandler.exportAsDrag(comp, e, action);
         }
 
         @Override
         public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException
         {
             Transferable transferable = createTransferable(comp);
             clip.setContents(transferable, this);
         }
 
         @Override
         public boolean importData(TransferSupport support)
         {
             return transferHandler.importData(support);
         }
 
         @Override
         public boolean importData(JComponent comp, Transferable t)
         {
             return transferHandler.importData(comp, t);
         }
 
         @Override
         public boolean canImport(TransferSupport support)
         {
             return transferHandler.canImport(support);
         }
 
         @Override
         public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
         {
             return transferHandler.canImport(comp, transferFlavors);
         }
 
         @Override
         public int getSourceActions(JComponent c)
         {
             return transferHandler.getSourceActions(c);
         }
 
         @Override
         public Icon getVisualRepresentation(Transferable t)
         {
             return transferHandler.getVisualRepresentation(t);
         }
 
         @Override
         public void lostOwnership(Clipboard clipboard, Transferable contents)
         {
         }
         
     }
 
     private static class MyTransferable implements Transferable
     {
         private static DataFlavor[] flavors;
         static
         {
             try
             {
                 flavors = new DataFlavor[]{
                 new DataFlavor("text/html;class=java.lang.String"),
                 new DataFlavor("text/html;class=java.io.Reader"),
                 new DataFlavor("text/html;charset=utf-8;class=java.io.InputStream"),
                 new DataFlavor("text/plain;class=java.lang.String"),
                 new DataFlavor("text/plain;class=java.io.Reader"),
                 new DataFlavor("text/plain;charset=utf-8;class=java.io.InputStream"),
                 DataFlavor.stringFlavor
                 };
             }
             catch (ClassNotFoundException ex)
             {
                 throw new UnsupportedOperationException(ex);
             }
                         
         }
         private final String htmlData;
         private final String plainData;
         public MyTransferable(JTable table)
         {
             StringBuilder html = new StringBuilder();
             html.append("<html><body><table>");
             StringBuilder plain = new StringBuilder();
             boolean selectAll = table.getSelectedRowCount() == table.getRowCount();
 
             int columnCount = table.getColumnCount();
             if (selectAll)
             {
                 html.append("<thead><tr>");
                 for (int ii=0;ii<columnCount;ii++)
                 {
                     String columnName = table.getColumnName(ii);
                     html.append("<th>");
                     html.append(columnName);
                     html.append("</th>");
                     plain.append(columnName+"\t");
                 }
                 html.append("</tr></thead>");
                 plain.append("\n");
             }
             html.append("<tbody>");
             int[] selectedRows = table.getSelectedRows();
             TableModel model = table.getModel();
             for (int row : selectedRows)
             {
                 html.append("<tr>");
                 for (int col=0;col<columnCount;col++)
                 {
                     html.append("<td>");
                     Object value = model.getValueAt(row, col);
                     if (value != null)
                     {
                         populate(html, plain, value);
                     }
                     html.append("</td>");
                     plain.append("\t");
                 }
                 html.append("</tr>");
                 plain.append("\n");
             }
             html.append("</tbody></table></body><html>");
             htmlData = html.toString();
             plainData = plain.toString();
         }
 
         @Override
         public DataFlavor[] getTransferDataFlavors()
         {
             return flavors;
         }
 
         @Override
         public boolean isDataFlavorSupported(DataFlavor flavor)
         {
             for (DataFlavor df : flavors)
             {
                 if (df.equals(flavor))
                 {
                     return true;
                 }
             }
             return false;
         }
 
         @Override
         public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
         {
             String data = null;
             if (flavor.getMimeType().startsWith("text/html"))
             {
                 data = htmlData;
             }
             else
             {
                 if (flavor.getMimeType().startsWith("text/plain"))
                 {
                         data = plainData;
                 }
                 else
                 {
                     throw new UnsupportedFlavorException(flavor);
                 }
             }
             switch (flavor.getRepresentationClass().getCanonicalName())
             {
                 case "java.lang.String":
                     return data;
                 case "java.io.Reader":
                     return new StringReader(data);
                 case "java.io.InputStream":
                     return new ByteArrayInputStream(data.getBytes("utf-8"));
                 default:
                     throw new UnsupportedFlavorException(flavor);
             }
         }
 
         private void populate(StringBuilder html, StringBuilder plain, Object value)
         {
             html.append(GObjectHelper.getString(value));
             plain.append(GObjectHelper.getString(value));
         }
     }
 
     public class ComboBoxModelCellEditor extends AbstractCellEditor implements TableCellEditor
     {
         private JComboBox combo = new JComboBox();
         
         @Override
         public Object getCellEditorValue()
         {
             return combo.getModel();
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             if (value != null)
             {
                 ComboBoxModel model = (ComboBoxModel) value;
                 combo.setModel(model);
             }
             else
             {
                 combo.setModel(null);
             }
             return combo;
         }
         
     }
     public class GoogleObjectCellEditor extends AbstractCellEditor implements TableCellEditor
     {
         private Class<?> type;
         private JTextField editor = new JTextField();
 
         public GoogleObjectCellEditor(Class<?> type)
         {
             this.type = type;
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             if (value != null)
             {
                 editor.setText(GObjectHelper.getString(value));
             }
             else
             {
                 editor.setText(null);
             }
             return editor;
         }
 
         @Override
         public Object getCellEditorValue()
         {
             String text = editor.getText();
             if (text != null && !text.isEmpty())
             {
                 return GObjectHelper.valueOf(type, text);
             }
             else
             {
                 return null;
             }
         }
         
     }
     public class ShortBlobCellEditor extends BlobCellEditor
     {
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             columnName = table.getColumnName(column);
             if (value != null)
             {
                 ShortBlob blob = (ShortBlob) value;
                 dialog.setBytes(blob.getBytes());
                 button.setText(dialog.getContentDescription());
             }
             else
             {
                 button.setText(null);
             }
             return button;
         }
 
         @Override
         public Object getCellEditorValue()
         {
             byte[] bytes = dialog.getBytes();
             if (bytes != null)
             {
                 return new ShortBlob(bytes);
             }
             else
             {
                 return null;
             }
         }
 
     }
     public class BlobCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
     {
         protected static final String EDIT = "edit";
         protected JButton button = new JButton();
         protected BytesDialog dialog = new BytesDialog(owner);
         protected MagicResult guess;
         protected String columnName;
         
         public BlobCellEditor()
         {
             button.setActionCommand(EDIT);
             button.addActionListener(this);
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             columnName = table.getColumnName(column);
             if (value != null)
             {
                 Blob blob = (Blob) value;
                 dialog.setBytes(blob.getBytes());
                 button.setText(dialog.getContentDescription());
             }
             else
             {
                 button.setText(null);
             }
             return button;
         }
 
         @Override
         public Object getCellEditorValue()
         {
             byte[] bytes = dialog.getBytes();
             if (bytes != null)
             {
                 return new Blob(bytes);
             }
             else
             {
                 return null;
             }
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (EDIT.equals(e.getActionCommand()))
             {
                 if (dialog.input())
                 {
                     button.setText(dialog.getContentDescription());
                 }
                 fireEditingStopped();
             }
         }
         
     }
     public class RatingCellEditor extends AbstractCellEditor implements TableCellEditor
     {
         private JSpinner editor;
 
         public RatingCellEditor()
         {
             SpinnerNumberModel model = new SpinnerNumberModel(0, Rating.MIN_VALUE, Rating.MAX_VALUE, 1);
             editor = new JSpinner(model);
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             if (value != null)
             {
                 Rating rating = (Rating) value;
                 editor.setValue(rating.getRating());
             }
             else
             {
                 editor.setValue(0);
             }
             return editor;
         }
 
         @Override
         public Object getCellEditorValue()
         {
             return new Rating((Integer)editor.getValue());
         }
         
     }
     public class TextCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
     {
         private static final String EDIT = "edit";
         private JButton button = new JButton();
         private TextDialog dialog = new TextDialog(owner);
 
         public TextCellEditor()
         {
             button.setActionCommand(EDIT);
             button.addActionListener(this);
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
         {
             if (value != null)
             {
                 Text text = (Text) value;
                 button.setText(text.getValue());
             }
             else
             {
                 button.setText(null);
             }
             return button;
         }
 
         @Override
         public Object getCellEditorValue()
         {
             return new Text(button.getText());
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (EDIT.equals(e.getActionCommand()))
             {
                 dialog.setText(button.getText());
                 if (dialog.input())
                 {
                     button.setText(dialog.getText());
                 }
                 fireEditingStopped();
             }
         }
         
     }
     public class ComboBoxModelCellRenderer extends TooltippedTableCellRenderer
     {
         @Override
         protected void setValue(Object value)
         {
             if (value != null)
             {
                 ComboBoxModel model = (ComboBoxModel) value;
                 super.setValue(model.getSelectedItem());
             }
             else
             {
                 super.setValue(value);
             }
         }
     }
     public class GoogleObjectTableCellRenderer extends TooltippedTableCellRenderer
     {
 
         @Override
         protected void setValue(Object value)
         {
             if (value != null)
             {
                 super.setValue(GObjectHelper.getString(value));
             }
             else
             {
                 super.setValue(value);
             }
         }
     }
 
     public class BlobTableCellRenderer extends TooltippedTableCellRenderer
     {
 
         @Override
         protected void setValue(Object value)
         {
             if (value != null)
             {
                 Blob blob = (Blob) value;
                 MagicResult guess = magic.guess(blob.getBytes());
                 if (guess != null)
                 {
                     super.setValue(guess.getDescription());
                 }
                 else
                 {
                     super.setValue("???");
                 }
             }
             else
             {
                 super.setValue(value);
             }
         }
     }
 
     public class ShortBlobTableCellRenderer extends TooltippedTableCellRenderer
     {
 
         @Override
         protected void setValue(Object value)
         {
             if (value != null)
             {
                 ShortBlob blob = (ShortBlob) value;
                 MagicResult guess = magic.guess(blob.getBytes());
                 if (guess != null)
                 {
                     super.setValue(guess.getDescription());
                 }
                 else
                 {
                     super.setValue("???");
                 }
             }
             else
             {
                 super.setValue(value);
             }
         }
     }
 
     public class TooltippedTableCellRenderer extends DefaultTableCellRenderer
     {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
         {
             Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
             setToolTipText(getText());
             return component;
         }
     }
 
 }
