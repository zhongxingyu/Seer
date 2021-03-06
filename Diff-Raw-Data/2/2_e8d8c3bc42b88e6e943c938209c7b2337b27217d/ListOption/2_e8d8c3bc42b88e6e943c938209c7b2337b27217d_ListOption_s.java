 package net.sf.freecol.common.option;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  * An option for a list of something.
  * @param <T> The type of objects to store in the list.
  */
 public class ListOption<T> extends AbstractOption {
 
     @SuppressWarnings("unused")
     private static Logger logger = Logger.getLogger(ListOption.class.getName());
 
     private ListOptionSelector<T> selector;
     private List<T> value;
 
     /**
      * Creates a new <code>ListOption</code>.
      * @param in The <code>XMLStreamReader</code> containing the data. 
      */
     public ListOption(ListOptionSelector<T> selector, XMLStreamReader in) throws XMLStreamException {
         super(NO_ID);
         value = new ArrayList<T>();
         this.selector = selector;
         readFromXML(in);
     }
 
     /**
      * Creates a new <code>ListOption</code>.
      *
      * @param id The identifier for this option. This is used when the object should be
      *           found in an {@link OptionGroup}.
      * @param defaultValues The default values of this option.
      */
     public ListOption(ListOptionSelector<T> selector, String id, T... defaultValues) {
         this(selector, id, null, defaultValues);
     }
 
     /**
      * Creates a new <code>ListOption</code>.
      *
      * @param id The identifier for this option. This is used when the object should be
      *           found in an {@link OptionGroup}.
      * @param optionGroup the OptionGroup this option belongs to.
      * @param defaultValues The default values of this option.
      */
     public ListOption(ListOptionSelector<T> selector, String id, OptionGroup optionGroup, T... defaultValues) {
         super(id, optionGroup);
         value = new ArrayList<T>();
         this.selector = selector;
         for (T s : defaultValues) {
             value.add(s);
         }
     }
 
     /**
      * Gets the delegate responsible for getting a list of
      * objects that can be selected by this option.
      * 
      * @return The <code>ListOptionSelector</code> for this
      *      option.
      */
     public ListOptionSelector<T> getListOptionSelector() {
         return selector;
     }
 
     /**
      * Gets the current value of this <code>Option</code>.
      * @return The value.
      */
     public List<T> getValue() {
         return new ArrayList<T>(value);
     }
     
     
     /**
      * Sets the current value of this <code>Option</code>.
      * @param value The value.
      */
     public void setValue(List<T> value) {
         final List<T> oldValue = this.value;
         this.value = value;
         
         if (value != oldValue && isDefined) {
             firePropertyChange("value", oldValue, value);
         }
         isDefined = true;
     }
 
     private List<String> getValueIds() {
         final List<String> ids = new ArrayList<String>(value.size());
         for (T t : value) {
             ids.add(selector.getId(t));
         }
         return ids;
     }
     
     private void setValueIds(final List<String> ids) {
         final List<T> value = new ArrayList<T>(ids.size());
         for (String id : ids) {
             value.add(selector.getObject(id));
         }
         setValue(value);
     }
     
     /**
      * This method writes an XML-representation of this object to
      * the given stream.
      *  
      * @param out The target stream.
      * @throws XMLStreamException if there are any problems writing
      *      to the stream.
      */
     protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
         // Start element:
         out.writeStartElement(getXMLElementTagName());
 
         out.writeAttribute("id", getId());
         toListElement("value", getValueIds(), out);
 
         out.writeEndElement();
     }
 
     /**
      * Initialize this object from an XML-representation of this object.
      * @param in The input stream with the XML.
      * @throws XMLStreamException if a problem was encountered
      *      during parsing.
      */
     protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
         final String id = in.getAttributeValue(null, "id");
 
         if (id == null && getId().equals("NO_ID")){
             throw new XMLStreamException("invalid <" + getXMLElementTagName() + "> tag : no id attribute found.");
         }
        
         setValueIds(readFromListElement("value", in, String.class));
         
         in.nextTag();
     }
 
 
     /**
      * Gets the tag name of the root element representing this object.
      * @return "listOption".
      */
     public static String getXMLElementTagName() {
         return "listOption";
     }
 }
