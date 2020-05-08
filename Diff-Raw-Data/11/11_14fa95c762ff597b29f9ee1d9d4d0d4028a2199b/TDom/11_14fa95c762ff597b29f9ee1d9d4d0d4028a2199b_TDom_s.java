 /**
  * Base class to create and manipulate TDom instances.
  *
  */
 
 package org.tdom;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.io.PrintWriter;
 
 public abstract class TDom
 {
     /**
      * Create a new TTagNode with the given name as a tag.
      *
      * @param name is the name of the node
      * @param extra is a set of TDom instances that will be appended
      * to the returned node.
      */
     public final static TTagNode n(String name, TDom... extra)
     {
         TTagNode ret = new TTagNode(name);
         for (int i=0; i<extra.length; i++) {
             ret.append(extra[i]);
         }
         return ret;
     }
 
     /**
      * @param name is the name for the attribute
      * @param value is any object -- String.valueOf() will be called
      * on a non-null object and used for the attribute's value.
      */
     public final static TAttr a(String name, Object value)
     { return new TAttr(name, value); }
 
 
     /**
      * @param o is any object -- String.valueOf() will be called
      * on this object, and a TText instance will be initialized with
      * this result.
      */
     public final static TText t(Object o)
     { return new TText(o); }
 
     /**
      * Render this node into the provided printstream.
      * @param pw is the printstream where the textual output
      * is dumped.
      */
     public void dump(final PrintWriter pw)
     { visit(new HTMLVisitor(pw)); }
 
     /**
      * This interface allows you to visit TDom objects, so you
      * may render text in a different way if you find appropriate.
      *
      * Please note that you are in charge of the visit sequence by
      * explicitly calling visit() on any TDom child nodes you want
      * continue visiting.
      */
     public interface TVisitor
     {
         void visitText(TText t);
         void visitAttr(TAttr attr);
         void visitTagNode(TTagNode n);
         void visitList(TList l);
     }
 
     public abstract void visit(TVisitor v);
 
     /**
      * @return a deep copy of this object.
      */
     public abstract TDom dup();
 
     public final static class TText extends TDom
     {
         public TText(Object o)
         { m_text = String.valueOf(o); }
         public String getText()
         { return m_text; }
         public void visit(TVisitor v)
         { v.visitText(this); }
         public TText dup()
         { return this; }
 
         private final String m_text;
     }
 
     public final static class TAttr extends TDom
     {
         public TAttr(String name, Object value)
         {
             m_name = name;
             m_value = value==null?null:String.valueOf(value);
         }
         public String getName()
         { return m_name; }
         public String getValue()
         { return m_value; }
         public void visit(TVisitor v)
         { v.visitAttr(this); }
 
         public TAttr dup()
         { return this; }
 
         private final String m_name;
         private final String m_value;
     }
 
     public static abstract class TNode extends TDom
     {
 
         /**
          * @return the parent(s) of this node.
          */
         public abstract TNode up();
 
         /**
          * Insert the provided thing at the desired index.
          * Note that if TDom is actually a TAttr, it is simply
          * added to the attribute set.
          * @param idx index to add the element.
          * @param thing to add to this node.
          */
         public abstract TNode addAt(int idx, TDom thing);
 
         /**
          * Select all TTagNodes that match the given selector string.
          * 
          * A very simple selector syntax is available. Tagged classes and
          * ids can be used. (eg: <tt>"div.content"</tt>, <tt>"span#id"</tt>,
          * <tt>"body"</tt> etc. Simple attribute-based selectors can be used,
          * eg: <tt>"span[class=~'copyright']"</tt>, <tt>a[name]</tt> etc.
         * Finally, descendent selectors can be used, eg:
         * <tt>div.content p.author a[href]</tt> etc. Note that attribute
          * selectors use the single quote (') to avoid backslashitis in java.
          * @return a TList containing the selected TTagNodes.
          */
         public abstract TList select(String selector);
 
         public TNode append(TDom thing)
         { return addAt(-1, thing); }
         public TNode append(String selector, TDom thing)
         { select(selector).append(thing); return this; }
         public TNode prepend(TDom thing)
         { return addAt(0, thing); }
         public TNode prepend(String selector, TDom thing)
         { select(selector).prepend(thing); return this; }
 
         /**
          * Remove the provided object from these nodes if it
          * exists.
          * @param thing is the object to be removed.
          */
         public abstract TNode remove(TDom thing);
 
         /**
          * Entirely remove these nodes from the tree.
          */
         public abstract TNode remove();
         public TNode remove(String selector)
         { select(selector).remove(); return this; }
         public TNode remove(String selector, TDom thing)
         { select(selector).remove(thing); return this; }
 
         /**
          * Insert the provided object before these nodes. (ie, after
          * the operation, thing will become a sibling to each node, and
          * located just before it.)
          *
          * @param thing is an instance to insert before this node in the tree.
          * @throws IllegalStateException if thing already has a parent, or
          * if a node has no parent.
          */
         public abstract TNode before(TDom thing);
         public TNode before(String selector, TDom thing)
         { select(selector).before(thing); return this; }
 
         /**
          * Insert the provided object after these nodes. (ie, after
          * the operation, thing will become a sibling, just after
          * each node.)
          *
          * @param thing is an instance to insert before this node in the tree.
          * @throws IllegalStateException if thing already has a parent, or
          * if this node has no parent.
          */
         public abstract TNode after(TDom thing);
         public TNode after(String selector, TDom thing)
         { select(selector).after(thing); return this; }
     }
 
     public final static class TTagNode extends TNode
     {
         public TTagNode(String name)
         { m_name = name; }
 
         private void setParent(TTagNode n)
         {
             if (m_parent != null) {
                 throw new IllegalStateException
                     ("Cannot reparent <"+getName()+">");
             }
             m_parent = n;
         }
 
         public TTagNode up()
         { return m_parent; }
 
         public TTagNode dup()
         {
             TTagNode ret = new TTagNode(m_name);
             for (TAttr attr: m_attrs.values()) {
                 ret.append(attr.dup());
             }
             for (TDom child: m_children) {
                 ret.append(child.dup());
             }
             return ret;
         }
 
         public TTagNode before(TDom thing)
         {
             if (m_parent == null) {
                 throw new IllegalStateException
                     ("No parent for <"+getName()+">");
             }
 
             List<TDom> siblings = m_parent.m_children;
             for (int i=siblings.size()-1; i>=0; i--) {
                 if (siblings.get(i) == this) {
                     m_parent.addAt(i, thing);
                     return this;
                 }
             }
             // This is unexpected.
             throw new IllegalStateException
                 ("Unexpected -- parent doesn't contain child.");
         }
 
         public TTagNode after(TDom thing)
         {
             if (m_parent == null) {
                 throw new IllegalStateException
                     ("No parent for <"+getName()+">");
             }
 
             List<TDom> siblings = m_parent.m_children;
             for (int i=siblings.size()-1; i>=0; i--) {
                 if (siblings.get(i) == this) {
                     if (i == (siblings.size()-1)) {
                         m_parent.addAt(-1, thing);
                     }
                     else {
                         m_parent.addAt(i+1, thing);
                     }
                     return this;
                 }
             }
             // This is unexpected.
             throw new IllegalStateException
                 ("Unexpected -- parent doesn't contain child.");
         }
 
         public TTagNode addAt(int idx, TDom thing)
         {
             if (thing instanceof TAttr) {
                 TAttr attr = (TAttr) thing;
                 m_attrs.put(attr.getName().toLowerCase(), attr);
             }
             else if (thing instanceof TList) {
                 for (TNode entry: ((TList) thing).getEntries()) {
                     addAt(idx, entry);
                     if (idx >= 0) { idx++; }
                 }
             }
             else {
                 // First ensure we can reparent.
                 if (thing instanceof TTagNode) {
                     ((TTagNode) thing).setParent(this);
                 }
                 if (idx >= 0) {
                     m_children.add(idx, thing);
                 }
                 else {
                     m_children.add(thing);
                 }
             }
             return this;
         }
 
         public TTagNode remove(TDom thing)
         {
             if (thing instanceof TAttr) {
                 m_attrs.remove(((TAttr) thing).getName().toLowerCase());
             }
             else if (thing instanceof TList) {
                 for (TTagNode entry: ((TList) thing).getEntries()) {
                     m_children.remove(entry);
                 }
             }
             else {
                 m_children.remove(thing);
             }
             return this;
         }
 
         public TTagNode remove()
         {
             if (m_parent != null) { m_parent.remove(this); }
             return this;
         }
 
         public TList select(String selector)
         {
             String[] fields = selector.split("\\s+");
             if (fields.length > 1) { // handle hierarchy.
                 TList ret = new TList(this);
                 for (int i=0;i<fields.length;i++) { ret=ret.select(fields[i]); }
                 return ret;
             }
 
             Matcher m = ATTR.matcher(selector);
             if (m.matches()) { // tag[attr='value']
                 String typ = m.group(4); // null, ~ or |
                 return selectField
                     (m.group(1), m.group(2), m.group(5),
                      (typ==null)?0:(typ.equals("~")?' ':'-'));
             }
             else if ((m = CLS.matcher(selector)).matches()) {
                 String typ = m.group(3); // null, # or .
                 return selectField
                     (m.group(1),
                      (typ==null)?null:(typ.equals("#")?"id":"class"),
                      m.group(4), ' ');
             }
             else {
                 throw new IllegalArgumentException
                     ("Unknown selector '"+selector+"'");
             }
         }
 
         public boolean matchAttr(String name, String value, char sep)
         {
             if (name == null) { return true; }
             TAttr attr = m_attrs.get(name);
             if (attr == null) { return false; }
             if (value == null) { return true; }
             String aval = attr.getValue();
             if (aval == null) { return false; }
             if (sep == 0) { return value.equals(aval); }
             int sidx = aval.indexOf(value);
             if (sidx < 0) { return false; }
             if ((sidx > 0) && (sep != aval.charAt(sidx-1))) { return false; }
             sidx += value.length();
             if ((sidx < aval.length()) && (sep != aval.charAt(sidx))) {
                 return false;
             }
             return true;
         }
 
         public String getName()
         { return m_name; }
         public Map<String,TAttr> getAttrs()
         { return m_attrs; }
         public List<TDom> getChildren()
         { return m_children; }
         public void visit(TVisitor v)
         { v.visitTagNode(this); }
 
         private TList selectField
             (String tag, String attr, String val, char sep)
         { return selectField(tag, attr, val, sep, new TList()); }
 
         private TList selectField
             (String tag, String attr, String val, char sep, TList accum)
         {
             if (((tag == null) || (tag.equals(getName())))
                 && matchAttr(attr, val, sep)) {
                 accum.concat(this);
             }
             for (TDom child: m_children) {
                 if (child instanceof TTagNode) {
                     ((TTagNode) child).selectField(tag,attr,val,sep,accum);
                 }
             }
             return accum;
         }
 
         private final String m_name;
         private TTagNode m_parent = null;
         private final List<TDom> m_children = new ArrayList<TDom>();
         private final Map<String,TAttr> m_attrs = new HashMap<String,TAttr>();
         private final static Pattern ATTR = Pattern.compile
             ("(\\w+)?\\[(\\w+)(=(~)?'(\\w+)')?\\]");
         private final static Pattern CLS = Pattern.compile
             ("(\\w+)?((#|\\.)(\\w+))?");
     }
 
     public final static class TList extends TNode
     {
         public TList(TNode... nodes)
         {
             m_entries = new ArrayList<TTagNode>();
             concat(nodes);
         }
 
         public TList concat(TNode... nodes)
         {
             for (int i=0; i<nodes.length; i++) {
                 TNode cur = nodes[i];
                 if (cur instanceof TTagNode) { merge((TTagNode) cur); }
                 else { merge(((TList) cur).getEntries()); }
             }
             return this;
         }
 
         public TList up()
         {
             TList ret = new TList();
             for (TTagNode entry: m_entries) { ret.concat(entry.up()); }
             return ret;
         }
 
         public TList addAt(int idx, TDom thing)
         {
             boolean first = true;
             for (TTagNode entry: m_entries) {
                 entry.addAt(idx, first?thing:thing.dup());
                 first = false;
             }
             return this;
         }
 
         public TList remove(TDom thing)
         {
             for (TTagNode entry: m_entries) { entry.remove(thing); }
             return this;
         }
 
         public TList remove()
         {
             for (TTagNode entry: m_entries) { entry.remove(); }
             return this;
         }
 
         public TList select(String selector)
         {
             TList ret = new TList();
             for (TTagNode entry: m_entries) {
                 ret.merge(entry.select(selector).getEntries());
             }
             return ret;
         }
 
         public TList before(TDom thing)
         {
             boolean first = true;
             for (TTagNode entry: m_entries) {
                 entry.before(first?thing:thing.dup());
                 first = false;
             }
             return this;
         }
 
         public TList after(TDom thing)
         {
             boolean first = true;
             for (TTagNode entry: m_entries) {
                 entry.after(first?thing:thing.dup());
                 first = false;
             }
             return this;
         }
 
         public TList dup()
         {
             TList ret = new TList();
             for (TTagNode entry: m_entries) {
                 ret.m_entries.add(entry.dup());
             }
             return ret;
         }
 
         public TTagNode nth(int idx)
         { return m_entries.get(idx); }
         public TTagNode last()
         { return m_entries.get(m_entries.size()-1); }
         public void visit(TVisitor v)
         { v.visitList(this); }
         public List<TTagNode> getEntries()
         { return m_entries; }
         private void merge(List<TTagNode> l)
         { for (TTagNode e: l) { merge(e); } }
         private void merge(TTagNode e)
         { if (!m_entries.contains(e)) { m_entries.add(e); } }
 
         private final List<TTagNode> m_entries;
     }
 
     private final static class HTMLVisitor
         implements TVisitor
     {
         HTMLVisitor(PrintWriter pw)
         { m_pw = pw; }
 
         public void visitText(TText t)
         { escape(t.getText(), false); }
 
         private void escape(String s, boolean alsoquotes)
         {
             char chars[] = s.toCharArray();
             for (int i=0; i<chars.length; i++) {
                 char c = chars[i];
                 switch (c) {
                 case '&' : m_pw.write("&amp;"); break;
                 case '>' : m_pw.write("&gt;"); break;
                 case '<' : m_pw.write("&lt;"); break;
 
                 default:
                     if (alsoquotes && (c == '"')) { m_pw.print("&quot;"); }
                     else if (c < 127) { m_pw.write(c); }
                     else { m_pw.write("&#"+((int)c)+";"); }
                     break;
                 }
             }
         }
 
         public void visitAttr(TAttr attr)
         {
             m_pw.print(attr.getName());
             String v = attr.getValue();
             if (v != null) {
                 m_pw.print("=\"");
                 escape(attr.getValue(), true);
                 m_pw.print("\"");
             }
         }
 
         public void visitList(TList l)
         {
             for (TNode n: l.getEntries()) {
                 n.visit(this);
             }
         }
 
         public void visitTagNode(TTagNode n)
         {
             m_pw.print("<");
             m_pw.print(n.getName());
             for (TAttr attr: n.getAttrs().values()) {
                 m_pw.print(" ");
                 attr.visit(this);
             }
 
             List<TDom> children = n.getChildren();
             if (children.size() == 0) {
                 if (NO_ABBREV.contains(n.getName())) {
                     m_pw.print("></"+n.getName()+">");
                 }
                 else {
                     m_pw.print(" />");
                 }
             }
             else {
                 m_pw.print(">");
                 for (TDom child: children) {
                     child.visit(this);
                 }
                 m_pw.print("</");
                 m_pw.print(n.getName());
                 m_pw.print(">");
             }
         }
 
         private final PrintWriter m_pw;
         private final static Set<String> NO_ABBREV;
         static
         {
             NO_ABBREV = new HashSet<String>();
             NO_ABBREV.add("div");
             NO_ABBREV.add("a");
             NO_ABBREV.add("script");
         }
     }
 }
