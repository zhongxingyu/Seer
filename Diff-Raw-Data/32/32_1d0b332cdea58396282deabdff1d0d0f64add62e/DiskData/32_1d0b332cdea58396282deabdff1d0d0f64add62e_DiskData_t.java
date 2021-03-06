 package org.basex.data;
 
 import static org.basex.data.DataText.*;
 import java.io.IOException;
 import org.basex.core.Prop;
 import org.basex.index.FTFuzzy;
 import org.basex.index.Index;
 import org.basex.index.IndexToken;
 import org.basex.index.Names;
 import org.basex.index.Values;
 import org.basex.index.FTTrie;
 import org.basex.io.DataAccess;
 import org.basex.io.DataInput;
 import org.basex.io.DataOutput;
 import org.basex.io.TableAccess;
 import org.basex.io.TableDiskAccess;
 import org.basex.io.TableMemAccess;
 import org.basex.util.Array;
 import org.basex.util.Token;
 
 /**
  * This class stores and organizes the node table and the index structures for
  * textual content. All nodes in the table are accessed by their
  * implicit pre value. Some restrictions on the data are currently given:
  * <ul>
  * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
  * <li>A maximum of 2^16 different tag and attribute names is allowed</li>
  * <li>A tag can have a maximum of 256 attributes</li>
  * </ul>
  * Each node occupies 128 bits. The current storage layout looks like follows:
  *
  * <pre>
  * ELEMENT NODES:
  * - Byte     0:  KIND: Node kind (ELEM)
  * - Byte   1-2:  NSPC: Namespace (4), NS Definition flag (1) and Name (11 bits)
  * - Byte     3:  ATTS: Number of attributes
  * - Byte  4- 7:  DIST: Distance to parent node
  * - Byte  8-11:  SIZE: Number of descendants
  * - Byte 12-15:  UNID: Unique Node ID
  * DOCUMENT NODES:
  * - Byte     0:  KIND: Node kind (DOC)
  * - Byte  3- 7:  TEXT: Text reference
  * - Byte  8-11:  SIZE: Number of descendants
  * - Byte 12-15:  UNID: Unique Node ID
  * TEXT NODES:
  * - Byte     0:  KIND: Node kind (TEXT/COMM/PI)
  * - Byte  3- 7:  TEXT: Text reference
  * - Byte  8-11:  DIST: Distance to parent node
  * - Byte 12-15:  UNID: Unique Node ID
  * ATTRIBUTE NODES:
  * - Byte     0:  KIND: Node kind (ATTR)
  * - Byte   1-2:  NSPC: Namespace (4) and Name (11 bits)
  * - Byte  3- 7:  TEXT: Attribute value reference
  * - Byte    11:  DIST: Distance to parent node
  * - Byte 12-15:  UNID: Unique Node ID
  * </pre>
  *
  * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
  * @author Christian Gruen
  * @author Tim Petrowsky
  */
 public final class DiskData extends Data {
   /** Table access file. */
   private final TableAccess table;
   /** Texts access file. */
   private final DataAccess texts;
   /** Values access file. */
   private final DataAccess values;
   
   /**
    * Default Constructor.
    * @param db name of database
    * @throws IOException IO Exception
    */
   public DiskData(final String db) throws IOException {
     this(db, true);
   }
 
   /**
    * Constructor, specifying if indexes are to be opened as well.
    * @param db name of database
    * @param index open indexes
    * @throws IOException IO Exception
    */
   public DiskData(final String db, final boolean index) throws IOException {
     DataInput in = null;
     try {
       in = new DataInput(db, DATAINFO);
       meta = new MetaData(db);
       meta.read(in);
   
       // read indexes
       tags = new Names(in);
       atts = new Names(in);
       path = new PathSummary(this, in);
       ns = new Namespaces(in);
   
       // main memory mode.. keep table in memory
       table = Prop.mainmem ? new TableMemAccess(db, DATATBL, meta.size) :
         new TableDiskAccess(db, DATATBL);
       texts = new DataAccess(db, DATATXT);
       values = new DataAccess(db, DATAATV);
   
       if(index) {
         if(meta.txtindex) txtindex = new Values(this, db, true);
         if(meta.atvindex) atvindex = new Values(this, db, false);
         if(meta.ftxindex) ftxindex =
           meta.ftfz ? new FTFuzzy(this, db) : new FTTrie(this, db);
       }
       initNames();
     } catch(final IOException ex) {
       throw ex;
     } finally {
       try { if(in != null) in.close(); } catch(final IOException e) { }
     }
   }
 
   @Override
   public synchronized void flush() {
     try {
       table.flush();
       texts.flush();
       values.flush();
       // write meta data...
       final DataOutput out = new DataOutput(meta.dbname, DATAINFO);
       meta.write(out);
       tags.write(out);
       atts.write(out);
       path.write(out);
       ns.write(out);
       out.close();
       meta.dirty = false;
     } catch(final IOException e) {
       e.printStackTrace();
     }
   }
 
   @Override
   public synchronized void close() throws IOException {
     if(meta.dirty) flush();
     cls();
   }
 
   /**
    * Closes the database without writing data back to disk.
    * @throws IOException I/O exception
    */
   public void cls() throws IOException {
     table.close();
     texts.close();
     values.close();
     closeIndex(IndexToken.Type.TXT);
     closeIndex(IndexToken.Type.ATV);
     closeIndex(IndexToken.Type.FTX);
   }
 
   @Override
   public void closeIndex(final IndexToken.Type index) throws IOException {
     switch(index) {
       case TXT: if(txtindex != null) txtindex.close(); break;
       case ATV: if(atvindex != null) atvindex.close(); break;
       case FTX: if(ftxindex != null) ftxindex.close(); break;
       default: break;
     }
   }
 
   @Override
   public void setIndex(final IndexToken.Type type, final Index index) {
     switch(type) {
       case TXT: if(meta.txtindex) txtindex = index; break;
       case ATV: if(meta.atvindex) atvindex = index; break;
       case FTX: if(meta.ftxindex) ftxindex = index; break;
       default: break;
     }
   }
 
   @Override
   public int id(final int pre) {
     return table.read4(pre, 12);
   }
 
   @Override
   public int pre(final int id) {
     // find pre value in table
     for(int p = id; p < meta.size; p++) if(id == id(p)) return p;
     for(int p = 0; p < id; p++) if(id == id(p)) return p;
     // id not found
     return -1;
   }
 
   @Override
   public int kind(final int pre) {
     return table.read1(pre, 0);
   }
 
   @Override
   public int parent(final int pre, final int kind) {
     return pre - dist(pre, kind);
   }
 
   /**
    * Returns the distance of the specified node.
    * @param pre pre value
    * @param kind node kind
    * @return distance
    */
   private int dist(final int pre, final int kind) {
    switch(kind) {
       case ELEM: return table.read4(pre, 4);
       case TEXT:
       case COMM:
       case PI:   return table.read4(pre, 8);
       case ATTR: return table.read1(pre, 11);
       default:   return pre + 1;
     }
   }
 
   @Override
   public int attSize(final int pre, final int kind) {
     return kind == ELEM ? table.read1(pre, 3) : 1;
   }
 
   @Override
   public int size(final int pre, final int k) {
     return k == ELEM || k == DOC ? table.read4(pre, 8) : 1;
   }
 
   @Override
   public int tagID(final int pre) {
     return table.read2(pre, 1) & 0x07FF;
   }
 
   @Override
   public int tagNS(final int pre) {
     return (table.read1(pre, 1) >>> 4) & 0x0F;
   }
 
   @Override
   public int[] ns(final int pre) {
     return (table.read1(pre, 1) & 0x08) != 0 ? ns.get(pre) : Array.NOINTS;
   }
 
   @Override
   public int attNameID(final int pre) {
     return table.read2(pre, 1) & 0x07FF;
   }
 
   @Override
   public int attNS(final int pre) {
     return (table.read1(pre, 1) >>> 4) & 0x0F;
   }
 
   @Override
   public byte[] text(final int pre) {
     return txt(pre, texts);
   }
 
   @Override
   public byte[] attValue(final int pre) {
     return txt(pre, values);
   }
 
   /**
    * Returns the text of a text/attribute value.
    * @param pre pre value
    * @param da text reference
    * @return disk offset
    */
   private byte[] txt(final int pre, final DataAccess da) {
     final long o = textOff(pre);
     return (o & 0x8000000000L) != 0 ? token(o) : da.readToken(o);
   }
 
   @Override
   public double textNum(final int pre) {
     return txtNum(pre, texts);
   }
 
   @Override
   public double attNum(final int pre) {
     return txtNum(pre, values);
   }
 
   /**
    * Returns the double value of the specified pre value.
    * @param pre pre value
    * @param da text reference
    * @return disk offset
    */
   private double txtNum(final int pre, final DataAccess da) {
     final long off = textOff(pre);
     return (off & 0x8000000000L) != 0 ? (int) off :
       Token.toDouble(da.readToken(off));
   }
 
   @Override
   public int textLen(final int pre) {
     return txtLen(pre, texts);
   }
 
   @Override
   public int attLen(final int pre) {
     return txtLen(pre, values);
   }
 
   /**
    * Returns the disk offset of a text/attribute value.
    * @param pre pre value
    * @param da text reference
    * @return disk offset
    */
   private int txtLen(final int pre, final DataAccess da) {
     final long off = textOff(pre);
     return (off & 0x8000000000L) != 0 ? Token.numDigits((int) off) :
       da.readNum(off);
   }
 
   /**
    * Returns the disk offset of a text/attribute value.
    * @param pre pre value
    * @return disk offset
    */
   private long textOff(final int pre) {
     return table.read5(pre, 3);
   }
 
   /**
    * Converts the specified long value into a byte array.
    * @param i int value to be converted
    * @return byte array
    */
   private static byte[] token(final long i) {
     int n = (int) i;
     if(n == 0) return Token.ZERO;
     int j = Token.numDigits(n);
     final byte[] num = new byte[j];
 
     // faster division by 10 for values < 81920 (see {@link Integer#getChars}
     while(n > 81919) {
       final int q = n / 10;
       num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
       n = q;
     }
     while(n != 0) {
       final int q = (n * 52429) >>> 19;
       num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
       n = q;
     }
     return num;
   }
 
   @Override
   public void update(final int pre, final byte[] val) {
     if(kind(pre) == ELEM) {
       tagID(pre, tags.index(val, null, false));
     } else {
       update(pre, val, true);
     }
   }
 
   @Override
   public void update(final int pre, final byte[] name, final byte[] val) {
     update(pre, val, false);
     attNameID(pre, atts.index(name, val, false));
   }
 
   /**
    * Updates the specified text or attribute value.
    * @param pre pre value
    * @param val content
    * @param txt text flag
    */
   private void update(final int pre, final byte[] val, final boolean txt) {
     final long v = Token.toSimpleInt(val);
     if(v != Integer.MIN_VALUE) {
       textOff(pre, v | 0x8000000000L);
     } else {
       long off = textOff(pre);
       final boolean replace = (off & 0x8000000000L) == 0 &&
         val.length <= (txt ? textLen(pre) : attLen(pre));
       final DataAccess da = txt ? texts : values;
 
       // default: append new text to the end of file
       if(replace) {
         // new text is shorter than last one; replace it
         da.writeBytes(off, val);
       } else {
         // if current text is placed last, replace it with new one
         if(da.readNum(off) + da.pos() != da.length()) off = da.length();
         da.writeBytes(off, val);
         textOff(pre, off);
       }
     }
   }
 
   @Override
   public void delete(final int pre) {
     // size of the subtree to delete
     int k = kind(pre);
     int s = size(pre, k);
 
     // reduce size of ancestors
     int par = pre;
 
     // check if we are an attribute (different size counters)
     if(k == ATTR) {
       par = parent(par, ATTR);
       attSize(par, ELEM, attSize(par, ELEM) - 1);
       size(par, ELEM, size(par, ELEM) - 1);
       k = kind(par);
     }
 
     // reduce size of remaining ancestors
     while(par > 0 && k != DOC) {
       par = parent(par, k);
       k = kind(par);
       size(par, k, size(par, k) - s);
     }
 
     // preserve empty root node
     int p = pre;
     boolean empty = p == 0 && s == meta.size;
     if(empty) {
       p++;
       s = size(p, kind(p));
     }
     
     // delete node from table structure and reduce document size
     table.delete(p, s);
     meta.size -= s;
     updateDist(p, -s);
 
     if(empty) {
       size(0, DOC, 1);
       update(0, Token.EMPTY, true);
     }
   }
 
   /**
    * This method is called after a table modification. It updates the
    * size values of the ancestors and the distance values of the
    * following siblings.
    * @param pre root node
    * @param par parent node
    * @param s size to be added
    */
   private void updateTable(final int pre, final int par, final int s) {
     // increase sizes
     int p = par;
     while(p >= 0) {
       final int k = kind(p);
       size(p, k, size(p, k) + s);
       p = parent(p, k);
     }
     updateDist(pre + s, s);
   }
 
   /**
    * This method updates the distance values of the specified pre value
    * and the following siblings.
    * @param pre root node
    * @param s size to be added/removed
    */
   private void updateDist(final int pre, final int s) {
     int p = pre;
     while(p < meta.size) {
       final int k = kind(p);
       dist(p, k, dist(p, k) + s);
       p += size(p, kind(p));
     }
   }
 
   @Override
   public void insert(final int pre, final int par, final byte[] val,
       final int kind) {
 
     if(kind == ELEM) {
       insertElem(pre - 1, pre - par, val, 1, 1);
     } else if(kind == DOC) {
       insertDoc(pre - 1, 1, val);
     } else {
       insertText(pre - 1, pre - par, val, kind);
     }
     updateTable(pre, par, 1);
   }
 
   @Override
   public void insert(final int pre, final int par, final byte[] name,
       final byte[] val) {
 
     // insert attribute and increase attSize of parent element
     insertAttr(pre - 1, pre - par, name, val);
     attSize(par, ELEM, attSize(par, ELEM) + 1);
     updateTable(pre, par, 1);
   }
 
   @Override
   public void insert(final int pre, final int par, final Data dt) {
     // first source node to be copied; if input is a document, skip first node
     final int sa = dt.kind(0) == DOC && par > 0 ? 1 : 0;
     // number of nodes to be inserted
     final int ss = dt.size(sa, dt.kind(sa));
     
     // copy database entries
     for(int s = sa; s < sa + ss; s++) {
       final int k = dt.kind(s);
       final int r = dt.parent(s, k);
       // recalculate distance for root nodes
       // [CG] Updates/Insert: test collections
       final int d = r < sa ? pre - par : s - r;
       final int p = pre + s - sa - 1;
 
       switch(k) {
         case ELEM:
           // add element
           insertElem(p, d, dt.tag(s), dt.attSize(s, k), dt.size(s, k));
           break;
         case DOC:
           // add document
           insertDoc(p, dt.size(s, k), dt.text(s));
           break;
         case TEXT:
         case COMM:
         case PI:
           // add text
           insertText(p, d, dt.text(s), k);
           break;
         case ATTR:
           // add attribute
           insertAttr(p, d, dt.attName(s), dt.attValue(s));
           break;
       }
     }
     // update table if no document was inserted
     if(par != 0) updateTable(pre, par, ss);
     
     // delete old empty root node
     if(size(0, DOC) == 1) delete(0);
   }
 
   /**
    * Inserts an element node without updating the size and distance values
    * of the table.
    * @param pre insert position
    * @param dis parent distance
    * @param tag tag name index
    * @param as number of attributes
    * @param s node size
    */
   private void insertElem(final int pre, final int dis, final byte[] tag,
       final int as, final int s) {
 
     final long id = ++meta.lastid;
    final int t = tags.index(tag, null, false);
     table.insert(pre, new byte[] { ELEM, (byte) (t >> 8), (byte) t, (byte) as,
         (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis, 
         (byte) (s >> 24), (byte) (s >> 16), (byte) (s >> 8), (byte) s,
         (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
     meta.size++;
   }
 
   /**
    * Insert text node without updating the size and distance values
    * of the table.
    * @param pre insert position
    * @param s node size
    * @param val tag name or text node
    */
   private void insertDoc(final int pre, final int s, final byte[] val) {
     // build and insert new entry
     final long id = ++meta.lastid;
     final long txt = texts.length();
     texts.writeBytes(txt, val);
 
     table.insert(pre, new byte[] { DOC, 0, 0, (byte) (txt >> 32),
         (byte) (txt >> 24), (byte) (txt >> 16), (byte) (txt >> 8), (byte) txt,
         (byte) (s >> 24), (byte) (s >> 16), (byte) (s >> 8), (byte) s,
         (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
     meta.size++;
   }
 
   /**
    * Insert text node updating the size and distance values
    * of the table.
    * @param pre insert position
    * @param dis parent distance
    * @param val tag name or text node
    * @param kind node kind
    */
   private void insertText(final int pre, final int dis, final byte[] val,
       final int kind) {
 
     // build and insert new entry
     final long id = ++meta.lastid;
     final long txt = texts.length();
     texts.writeBytes(txt, val);
 
     table.insert(pre, new byte[] { (byte) kind, 0, 0, (byte) (txt >> 32),
         (byte) (txt >> 24), (byte) (txt >> 16), (byte) (txt >> 8), (byte) txt,
         (byte) (dis >> 24), (byte) (dis >> 16), (byte) (dis >> 8), (byte) dis,
         (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
     meta.size++;
   }
 
   /**
    * Insert attribute updating the size and distance values
    * of the table.
    * @param pre pre value
    * @param dis parent distance
    * @param name attribute name
    * @param val attribute value
    */
   private void insertAttr(final int pre, final int dis, final byte[] name,
       final byte[] val) {
 
     // add attribute to text storage
     final long len = values.length();
     values.writeBytes(len, val);
 
     // build and insert new entry
    final int att = atts.index(name, val, false);
     final long id = ++meta.lastid;
     table.insert(pre, new byte[] { ATTR, (byte) (att >> 8), (byte) att,
         (byte) (len >> 32), (byte) (len >> 24), (byte) (len >> 16),
         (byte) (len >> 8), (byte) len, 0, 0, 0, (byte) dis,
         (byte) (id >> 24), (byte) (id >> 16), (byte) (id >> 8), (byte) id });
     meta.size++;
   }
 
   /**
    * Writes the distance for the specified node.
    * @param pre pre value
    * @param kind node kind
    * @param v value
    */
   private void dist(final int pre, final int kind, final int v) {
     if(kind == ATTR) table.write1(pre, 11, v);
     else if(kind != DOC) table.write4(pre, kind == ELEM ? 4 : 8, v);
   }
 
   /**
    * Writes the tag ID.
    * @param pre pre value
    * @param v tag id
    */
   private void tagID(final int pre, final int v) {
     table.write2(pre, 1, v);
   }
 
   /**
    * Writes the attribute name ID.
    * @param pre pre value
    * @param v attribute name ID
    */
   private void attNameID(final int pre, final int v) {
     table.write2(pre, 1, v);
   }
 
   /**
    * Writes the disk offset of a text/attribute value.
    * @param pre pre value
    * @param off offset
    */
   private void textOff(final int pre, final long off) {
     table.write5(pre, 3, off);
   }
 
   /**
    * Writes the attribute size.
    * @param pre pre value
    * @param kind node kind
    * @param v value
    */
   private void attSize(final int pre, final int kind, final int v) {
     if(kind == ELEM) table.write1(pre, 3, v);
   }
 
   /**
    * Writes the size values.
    * @param pre pre value
    * @param kind node kind
    * @param v value
    */
   private void size(final int pre, final int kind, final int v) {
     if(kind == ELEM || kind == DOC) table.write4(pre, 8, v);
   }
 }
