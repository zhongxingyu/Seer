 /**
  *
  */
 package de.saumya.gwt.persistence.client;
 
 import java.sql.Date;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.XMLParser;
 
 public abstract class AbstractResource<E extends AbstractResource<E>> {
 
     static final String INDENT = "   ";
 
     protected enum State {
         NEW, TO_BE_CREATED, TO_BE_UPDATED, UP_TO_DATE, TO_BE_DELETED, DELETED, TO_BE_LOADED, STALE;
     }
 
     private final Set<ResourceChangeListener<E>> listeners = new HashSet<ResourceChangeListener<E>>();
 
     final Repository                             repository;
 
     final AbstractResourceFactory<E>             factory;
 
     protected State                              state     = State.NEW;
 
     protected AbstractResource(final Repository repository,
             final AbstractResourceFactory<E> factory) {
         this.repository = repository;
         this.factory = factory;
     }
 
     public final boolean isImmutable() {
         return this.factory.isImmutable();
     }
 
     public boolean isNew() {
         return this.state == State.NEW; // || this.state == State.TO_BE_CREATED;
     }
 
     public boolean isUptodate() {
         return this.state == State.UP_TO_DATE;
     }
 
     public boolean isDeleted() {
         return this.state == State.DELETED;
     }
 
     public void save() {
         switch (this.state) {
         case NEW:
         case TO_BE_CREATED:
             this.state = State.TO_BE_CREATED;
             post();
             break;
         case UP_TO_DATE:
         case TO_BE_UPDATED:
         case TO_BE_DELETED:
             this.state = State.TO_BE_UPDATED;
             put();
             break;
         default:
             throw new IllegalStateException("can not save in state "
                     + this.state);
         }
     }
 
     public void save(final String verb) {
         switch (this.state) {
         case UP_TO_DATE:
         case TO_BE_UPDATED:
             this.state = State.TO_BE_UPDATED;
             put(verb);
             break;
         default:
             throw new IllegalStateException("can not save with verb " + verb
                     + " in state " + this.state);
         }
 
     }
 
     public void destroy() {
         switch (this.state) {
         case UP_TO_DATE:
         case TO_BE_DELETED:
             this.state = State.TO_BE_DELETED;
             delete();
             break;
         default:
             throw new IllegalStateException("can not delete with state "
                     + this.state);
         }
     }
 
     public void fromXml(final String xml) {
         final Document doc = XMLParser.parse(xml);
         fromRootElement(doc.getDocumentElement());
     }
 
     void fromRootElement(final Element root) {
         fromElement(root);
     }
 
     public String toXml() {
         final StringBuilder buf = new StringBuilder();
         toXml(buf);
         return buf.toString();
     }
 
     /**
      * bit strange from object hierarchy point of view but convenient to share
      * the same methods in all implementations !!!
      */
     protected void appendXml(final StringBuilder buf, final String name,
             final Resource<?> value) {
         if (value != null) {
             buf.append("<").append(name).append(">");
             value.appendXml(buf, "id", value.id);
             value.appendXml(buf);
             buf.append("</").append(name).append(">");
         }
     }
 
     /**
      * bit strange from object hierarchy point of view but convenient to share
      * the same methods in all implementations !!!
      */
     protected void appendXml(final StringBuilder buf, final String name,
             final SingletonResource<?> value) {
         if (value != null) {
             buf.append("<").append(name).append(">");
             value.appendXml(buf);
             buf.append("</").append(name).append(">");
         }
     }
 
     /**
      * bit strange from object hierarchy point of view but convenient to share
      * the same methods in all implementations !!!
      */
     protected void appendXml(final StringBuilder buf, final String name,
             final AnonymousResource<?> value) {
         if (value != null) {
             buf.append("<").append(name).append(">");
             value.appendXml(buf);
             buf.append("</").append(name).append(">");
         }
     }
 
     protected void appendXml(final StringBuilder buf, final String name,
             final ResourceCollection<?> value) {
         if (value != null) {
             value.toXml(buf);
         }
         else {
             buf.append("<").append(name).append(">");
             buf.append("</").append(name).append(">");
         }
     }
 
     protected void appendXml(final StringBuilder buf, final String name,
             final String value) {
         // follow what the browser does with empty strings: do not send them
         if (value != null && !"".equals(value)) {
             buf.append("<")
                     .append(name)
                     .append(">")
                     .append(value)
                     .append("</")
                     .append(name)
                     .append(">");
         }
     }
 
     protected void appendXml(final StringBuilder buf, final String name,
             final Object value) {
         if (value != null) {
             appendXml(buf, name, value.toString());
         }
     }
 
     protected void toString(final String indent, final StringBuilder buf,
             final String name, final AbstractResource<?> value) {
         if (value != null) {
             buf.append("\n")
                     .append(indent)
                     .append(":")
                     .append(name)
                     .append(" => ");
             value.toStringRoot(indent, buf);
         }
     }
 
     protected void toString(final String indent, final StringBuilder buf,
             final String name, final ResourceCollection<?> value) {
         if (value != null) {
             buf.append("\n")
                     .append(indent)
                     .append(":")
                     .append(name)
                     .append(" => [\n");
             boolean first = true;
             final String nextIndent = indent + INDENT;
             for (final AbstractResource<?> resource : value) {
                 if (first) {
                    buf.append(indent).append(INDENT);
                     first = false;
                 }
                 else {
                     buf.append(",\n");
                 }
                 resource.toStringRoot(nextIndent, buf);
             }
             buf.append(first ? "" : "\n").append(indent).append("]");
         }
     }
 
     protected void toString(final String indent, final StringBuilder buf,
             final String name, final String value) {
         if (value != null) {
             buf.append("\n")
                     .append(indent)
                     .append(":")
                     .append(name)
                     .append(" => ")
                     .append(value);
         }
     }
 
     protected void toString(final String indent, final StringBuilder buf,
             final String name, final Object value) {
         if (value != null) {
             toString(indent, buf, name, value.toString());
         }
     }
 
     protected Timestamp getTimestamp(final Element root, final String name) {
         return getTimestamp(root, name, null);
     }
 
     protected Timestamp getTimestamp(final Element root, final String name,
             final Timestamp defaultValue) {
         final String value = getString(root, name);
         return value == null ? defaultValue : (value.matches("[0-9]*")
                 ? new Timestamp(Long.parseLong(value))
                 : new TimestampFactory(value).toTimestamp());
     }
 
     protected Date getDate(final Element root, final String name) {
         final String value = getString(root, name);
         return value == null ? null : Date.valueOf(value);
     }
 
     protected Time getTime(final Element root, final String name) {
         final String value = getString(root, name);
         return value == null ? null : Time.valueOf(value);
     }
 
     protected int getInt(final Element root, final String name) {
         final String value = getString(root, name);
         return value == null ? 0 : Integer.parseInt(value);
     }
 
     protected boolean getBoolean(final Element root, final String name) {
         final String value = getString(root, name);
         return "true".equals(value);
     }
 
     protected String getString(final Element root, final String name) {
         if (root == null) {
             return null;
         }
         final NodeList list = root.getElementsByTagName(name);
         for (int i = 0; i < list.getLength(); i++) {
             final Node node = list.item(i);
             if (node.getParentNode().equals(root)) {
                 return node.getFirstChild() == null
                         ? null
                         : node.getFirstChild().getNodeValue();
             }
         }
         return null;
     }
 
     public void addResourceChangeListener(
             final ResourceChangeListener<E> listener) {
         if (listener != null) {
             this.listeners.add(listener);
         }
     }
 
     public void removeResourceChangeListener(
             final ResourceChangeListener<E> listener) {
         this.listeners.remove(listener);
     }
 
     @SuppressWarnings("unchecked")
     void fireResourceChangeEvents() {
         for (final ResourceChangeListener<E> listener : this.listeners) {
             listener.onChange((E) this);
         }
         this.listeners.clear();
     }
 
     @SuppressWarnings("unchecked")
     void fireResourceErrorEvents(final int status, final String statusText) {
         for (final ResourceChangeListener<E> listener : this.listeners) {
             listener.onError(status, statusText, (E) this);
         }
         this.listeners.clear();
     }
 
     protected abstract void fromElement(Element root);
 
     protected abstract void appendXml(StringBuilder buf);
 
     protected abstract void toString(String indent, StringBuilder buf);
 
     abstract void toStringRoot(final String indent, final StringBuilder buf);
 
     protected abstract void post();
 
     protected abstract void put();
 
     protected abstract void put(String verb);
 
     protected abstract void delete();
 
     public abstract void reload();
 
     public abstract void toXml(final StringBuilder buf);
 
     public abstract String display();
 }
