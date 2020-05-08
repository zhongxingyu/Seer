 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.composer.model;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.queries.FileEncodingQuery;
 import org.netbeans.editor.BaseDocument;
 import org.netbeans.modules.editor.structure.api.DocumentModel;
 import org.netbeans.modules.editor.structure.api.DocumentModelException;
 import org.netbeans.modules.javafx.fxd.composer.misc.ByteArrayBuffer;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 import org.openide.filesystems.FileAlreadyLockedException;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.NbBundle;
 import com.sun.javafx.tools.fxd.FXDNode;
 import com.sun.javafx.tools.fxd.container.FXZFileContainerImpl;
 import com.sun.javafx.tools.fxd.container.builder.FXZContainerBuilder;
 import javax.swing.JEditorPane;
 import javax.swing.text.EditorKit;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataLoader;
 
 /**
  *
  * @author Pavel Benes
  */
 public final class FXZArchive extends FXZFileContainerImpl implements TableModel {
     private final    FXZDataObject            m_dObj;
     private final    List<FXZArchiveEntry>    m_entries;
     private          FXDFileModel             m_fileModel = null;
     private final    List<TableModelListener> m_tableListeners;
     private volatile int                      m_changeTicker = 0;  
     
     private final EntryValue [] m_values = new EntryValue[] {
         new EntryValue( "name", String.class, true) {  //NOI18N
             @Override
             public Object getValue(int row) {
                 return m_entries.get(row).getName();
             }
         @Override
             public void setValue( int row, Object value) {
                 m_entries.get(row).setName( (String) value);
             }
         },
         new EntryValue("size", String.class, false) {  //NOI18N
             @Override
             public Object getValue(int row) {
                 return m_entries.get(row).getSize();
             }
         },
         new EntryValue("compressedSize", String.class, false) {  //NOI18N
             @Override
             public Object getValue(int row) {
                 return m_entries.get(row).getCompressedSize();
             }
         }
     };
                 
     protected abstract class EntryValue {
         protected final String  displayName;
         protected final Class   clazz;
         protected final boolean isEditable;
         
         protected EntryValue( String name, Class clazz, boolean isEditable) {
             this.displayName = NbBundle.getMessage( FXZArchive.class, "Table.column." + name); //NOI18N
             this.clazz       = clazz;
             this.isEditable  = isEditable;
         }
                 
         public abstract Object getValue(int row);
         public          void   setValue(int row, Object value) {};
     }
     
     protected static final class CountingOutputStream extends OutputStream {
         private int m_size = 0;
         
         @Override
         public void write(int b) throws IOException {
             m_size++;
         }
         
         public int getSize() {
             return m_size;
         }        
     }
     
     protected final class FXZArchiveEntry {
         protected String          m_name;
         protected long            m_size;
         protected long            m_compressedSize;
         protected ByteArrayBuffer m_buffer;
 
         public FXZArchiveEntry( String name) throws IOException {
             m_name   = name;
             
             ZipEntry entry = m_zip.getEntry(m_name);
             m_size = entry.getSize();
             m_compressedSize = entry.getCompressedSize();
             
            System.err.println("Name: " + m_name);
            System.err.println("Size: " + m_size);
            System.err.println("CSize: " + m_compressedSize);
            
             //TODO Do not read file int the AWT thread
             m_buffer = read( m_zip.getInputStream( entry), m_size);
         }
 
         public FXZArchiveEntry( final File file) throws FileNotFoundException, IOException {
             m_name = file.getName();
             m_size = file.length();
             m_buffer = null;
             m_compressedSize = -1;
             
             Thread th = new Thread() {
                 @Override
                 public void run() {
                     FileInputStream     fin = null;
                     BufferedInputStream bin = null;
                     ZipOutputStream     zout = null;
                             
                     try {
                         fin = new FileInputStream(file);
                         bin = new BufferedInputStream( fin);
                         int b;
 
                         ByteArrayBuffer buffer = new ByteArrayBuffer( (int) m_size);
                         CountingOutputStream cout = new CountingOutputStream();
                         zout = new ZipOutputStream(cout);
                         zout.setMethod( ZipOutputStream.DEFLATED);
                     
                         synchronized(FXZArchiveEntry.this) {
                             zout.putNextEntry( new ZipEntry(m_name));
                             while( (b=bin.read()) != -1) {
                                 buffer.write(b);
                                 zout.write(b);
                             }
                             m_buffer = buffer;
                             zout.closeEntry();
                             zout.finish();                                   
                             m_compressedSize = cout.getSize();
                             fireTableChanged( new TableModelEvent( FXZArchive.this, 0, m_entries.size()-1));
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         try {
                             fin.close();
                             bin.close();
                             zout.close();
                         } catch( Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }
             };
             
             th.setPriority( Thread.MIN_PRIORITY);
             th.start();
         }
                 
         public String getName() {
             return m_name;
         }
         
         public void setName( String name) {
             if ( name != null && !name.equals(m_name)) {
                 m_name = name;
                 incrementChangeTicker();
             }
         }
         
         public String getSize() {
             return getSizeText(m_size);
         }
         
         public String getCompressedSize() {
             return getSizeText(m_compressedSize);
         }
                 
         public synchronized InputStream open() throws IOException {
             return m_buffer.newInputStream();
         }
         
         public synchronized void write(OutputStream out) throws IOException {
             m_buffer.writeTo(out);
         }
         
         private ByteArrayBuffer read( InputStream in, long size) throws IOException {
            if ( size <= 0) {
                 size = 8192;
             } 
             ByteArrayBuffer buffer = new ByteArrayBuffer( (int)size);
             BufferedInputStream bin = new BufferedInputStream( in);
             
             try {
                 int b;
                 
                 while( (b=bin.read()) != -1) {
                     buffer.write(b);
                 }
             } finally {
                 bin.close();
             }
             
             return buffer;
         }
     }
         
     public FXZArchive( FXZDataObject dObj) throws FileNotFoundException, IOException {
         super(FileUtil.toFile(dObj.getPrimaryFile()));
         m_dObj    = dObj;
         m_entries = new ArrayList<FXZArchiveEntry>();
         for ( String entryName : m_entryNames) {
            FXZArchiveEntry entry = new FXZArchiveEntry(entryName);
            if (entry.m_size > 0) {
                m_entries.add( entry);
            }
         }
         m_tableListeners = new ArrayList<TableModelListener>();
     }
         
     public synchronized void documentOpened( StyledDocument doc) {
         if ( m_fileModel == null) {
             try {
                 m_fileModel = new FXDFileModel(this, DocumentModel.getDocumentModel(doc));
             } catch (DocumentModelException ex) {
                 ex.printStackTrace();
             }
         } else {
             System.err.println("Document reopened!");  //NOI18N
         }
     }
     
     protected static BaseDocument loadDocument(InputStream in) throws IOException, DocumentModelException, BadLocationException {
         EditorKit kit = JEditorPane.createEditorKitForContentType(FXZDataLoader.REQUIRED_MIME);
         try {
             BaseDocument doc = (BaseDocument) kit.createDefaultDocument();
             kit.read(in, doc, 0);
             return doc;
         } finally {
             in.close();
         }
     }
     
     public synchronized FXDFileModel getFileModel() {
         if ( m_fileModel == null) {
             InputStream in = null;
             try {
                 try {
                     System.err.println("Loading the document ...");
                     in = open();
                     BaseDocument doc = loadDocument(in);
                     m_fileModel = new FXDFileModel(this, DocumentModel.getDocumentModel(doc));
                 } finally {
                     in.close();
                 }
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
         return m_fileModel;
     }
 
     public synchronized long getSize() {
         long sum = 0;
         for ( FXZArchiveEntry entry : m_entries) {
             sum += entry.m_compressedSize;
         }
         return sum;
     }
 
     public synchronized FXZArchiveEntry add( final File file) throws FileNotFoundException, IOException {
         FXZArchiveEntry entry = new FXZArchiveEntry(file);
         int index = m_entries.size();
         m_entries.add( entry);
         incrementChangeTicker();
         fireTableChanged( new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
         return entry;
     }
     
     public synchronized void remove(final String [] entryNames) {
         int size = m_entries.size();
         boolean changed = false;
         try {
             for ( String name : entryNames) {
                 int index = getEntryIndex(name);
                 if ( index != -1) {
                     m_entries.remove(index);
                     changed = true;
                 } else {
                     throw new IllegalArgumentException( "The entry " + name + " not found!");  //NOI18N
                 }
             }
         } finally {
             if ( changed) {
                 incrementChangeTicker();
                 fireTableChanged( new TableModelEvent( this, 0, size, 
                         TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
             }
         }
     }
     
     public int getChangeTicker() {
         return m_changeTicker;
     }
     
     public void incrementChangeTicker() {
         m_changeTicker++;
     }
     
     public synchronized void save() throws FileAlreadyLockedException, IOException, BadLocationException {        
         FileObject fo = m_dObj.getPrimaryFile();
         FXZContainerBuilder builder = new FXZContainerBuilder( fo.getOutputStream());
         
         OutputStream out = builder.add( MAIN_CONTENT);
         OutputStreamWriter writer = new OutputStreamWriter(out, FileEncodingQuery.getEncoding( fo));        
         BaseDocument doc = m_fileModel.getDocument();
         doc.write(writer, 0, doc.getLength());
         writer.close();
         
         //System.err.println("Entry num: " + m_entries.size());
         for (FXZArchiveEntry entry : m_entries) {
             if ( !MAIN_CONTENT.equals( entry.m_name)) {
                 //System.err.println("Adding entry: " + entry.getName());  //NOI18N
                 out = builder.add( entry.getName());
                 entry.write(out);
                 out.close();
             }
         }
         builder.close();
         load();
     }    
     
     protected int getEntryIndex( String name) {
         assert name != null;
         for ( int i = m_entries.size() - 1; i >= 0; i--) {
             FXZArchiveEntry entry = m_entries.get(i);
             if ( name.equals(entry.getName())) {
                 return i;
             }
         }
         return -1;
     }
 
     @Override
     public synchronized InputStream open(String entryName) throws FileNotFoundException, IOException {
         //System.err.println("Opening the entry " + entryName);
         entryName = removeMagicDirVar(entryName);
         //System.err.println("New value: " + entryName);
         if ( MAIN_CONTENT.equals(entryName)) {
             return super.open(entryName);
         } else {
             int index = getEntryIndex(entryName);
             if (index != -1) {
                 return m_entries.get(index).open();
             } else {
                 throw new FileNotFoundException("The entry '" + entryName + "' not found!"); //NOI18N
             }
         }
     }
 
     @Override
     public FXDNode getRoot( final String entryName) {
         assert m_fileModel != null;
         return m_fileModel.getRootNode();
     }
 
     public int getRowCount() {
         return m_entries.size();
     }
 
     public int getColumnCount() {
         return m_values.length;
     }
 
     public String getColumnName(int columnIndex) {
         return m_values[columnIndex].displayName;
     }
 
     public Class<?> getColumnClass(int columnIndex) {
         return m_values[columnIndex].clazz;
     }
 
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         return m_values[columnIndex].isEditable;
     }
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         return m_values[columnIndex].getValue(rowIndex);
     }
 
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         m_values[columnIndex].setValue(rowIndex, aValue);
     }
 
     public void addTableModelListener(TableModelListener l) {
         m_tableListeners.add(l);
     }
 
     public void removeTableModelListener(TableModelListener l) {
         m_tableListeners.add(l);
     }
     
     protected void fireTableChanged(TableModelEvent e) {
         for ( TableModelListener listener : m_tableListeners) {
             listener.tableChanged(e);
         }
     }   
     
     public static String getSizeText( long size) {
         if ( size < 0) {
             return "Calculating...";  //NOI18N
         } else if ( size < 1024) {
             return size + " Bytes"; //NOI18N
         } else if ( size < 1024 * 1024) {
             return (Math.round(size / 102.4) / 10.0) + " KBytes"; //NOI18N
         } else {
             return (Math.round(size / (102.4 * 1024)) / 10.0) + " MBytes"; //NOI18N
         }
     }    
 }
