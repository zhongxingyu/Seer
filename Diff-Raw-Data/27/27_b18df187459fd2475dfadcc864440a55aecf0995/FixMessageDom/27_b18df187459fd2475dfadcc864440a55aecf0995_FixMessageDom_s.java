 package org.tomac.tools.messagegen;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.dom4j.Attribute;
 import org.dom4j.Element;
 
 public class FixMessageDom {
 
 	public String major;
 	public String type;
 	public String servicepack;
 	public String minor;
 	public String flavour;
 	public ArrayList<DomFixMessage> domFixMessages = new ArrayList<DomFixMessage>();
 	public ArrayList<DomFixComponent> domFixComponents = new ArrayList<DomFixComponent>();
 	public HashMap<String, DomFixComponent> domFixNamedComponents = new HashMap<String, DomFixComponent>();
 	public DomFixComponent domFixHeader = new DomFixComponent();
 	public DomFixComponent domFixTrailer = new DomFixComponent();
 	public ArrayList<DomFixField> domFixFields = new ArrayList<DomFixField>();
 	public HashMap<String, DomFixField> domFixNamedFields = new HashMap<String, DomFixField>();
 	public String	packageName;
 	public String	packageNameBase;
 	public static final int	UNKNOWN				= 0;
 	public static final int	INT					= 1;
 	public static final int	LENGTH				= 11;
 	public static final int	TAGNUM				= 12;
 	public static final int	SEQNUM				= 13;
 	public static final int	NUMINGROUP			= 14;
 	public static final int	DAYOFMOUNTH			= 15;
 	public static final int	FLOAT				= 2;
 	public static final int	PRICE				= 21;
 	public static final int	QTY					= 22;
 	public static final int	PRICEOFFSET			= 23;
 	public static final int	AMT					= 24;
 	public static final int	PERCENTAGE			= 25;
 	public static final int	CHAR				= 3;
 	public static final int	BOOLEAN				= 31;
 	public static final int	STRING				= 4;
 	public static final int	MULTIPLECHARVALUE	= 41;
 	public static final int	MULTIPLESTRINGVALUE	= 42;
 	public static final int	COUNTRY				= 43;
 	public static final int	CURRENCY			= 44;
 	public static final int	EXCHANGE			= 45;
 	public static final int	MONTHYEAR			= 46;
 	public static final int	UTCTIMESTAMP		= 47;
 	public static final int	UTCTIMEONLY			= 48;
 	public static final int	UTCDATEONLY			= 49;
 	public static final int	LOCALMKTDATE		= 410;
 	public static final int	TZTIMEONLY			= 411;
 	public static final int	TZTIMESTAMP			= 412;
 	public static final int	DATA				= 413;
 	public static final int	XMLDATA				= 414;
 	public static final int	LANGUAGE			= 415;
 
 	public static final int toInt(final String type) {
 		if (type.equals("INT"))
 			return INT;
 		if (type.equals("LENGTH"))
 			return LENGTH;
 		if (type.equals("TAGNUM"))
 			return TAGNUM;
 		if (type.equals("SEQNUM"))
 			return SEQNUM;
 		if (type.equals("NUMINGROUP"))
 			return NUMINGROUP;
 		if (type.equals("NUMINGRP"))
 			return NUMINGROUP; // <field name="NoRiskSecurityAltID" number="1540" type="NUMINGRP"/> fixprotocol.org bug
 		if (type.equals("DAYOFMOUNTH"))
 			return DAYOFMOUNTH;
 		if (type.equals("FLOAT"))
 			return FLOAT;
 		if (type.equals("PRICE"))
 			return PRICE;
 		if (type.equals("QTY"))
 			return QTY;
 		if (type.equals("PRICEOFFSET"))
 			return PRICEOFFSET;
 		if (type.equals("AMT"))
 			return AMT;
 		if (type.equals("PERCENTAGE"))
 			return PERCENTAGE;
 		if (type.equals("CHAR"))
 			return CHAR;
 		if (type.equals("BOOLEAN"))
 			return BOOLEAN;
 		if (type.equals("STRING"))
 			return STRING;
 		if (type.equals("MULTIPLECHARVALUE"))
 			return MULTIPLECHARVALUE;
 		if (type.equals("MULTIPLESTRINGVALUE"))
 			return MULTIPLESTRINGVALUE;
 		if (type.equals("COUNTRY"))
 			return COUNTRY;
 		if (type.equals("CURRENCY"))
 			return CURRENCY;
 		if (type.equals("EXCHANGE"))
 			return EXCHANGE;
 		if (type.equals("MONTHYEAR"))
 			return MONTHYEAR;
 		if (type.equals("UTCTIMESTAMP"))
 			return UTCTIMESTAMP;
 		if (type.equals("UTCTIMEONLY"))
 			return UTCTIMEONLY;
 		if (type.equals("UTCDATEONLY"))
 			return UTCDATEONLY;
 		if (type.equals("LOCALMKTDATE"))
 			return LOCALMKTDATE;
 		if (type.equals("TZTIMEONLY"))
 			return TZTIMEONLY;
 		if (type.equals("TZTIMESTAMP"))
 			return TZTIMESTAMP;
 		if (type.equals("DATA"))
 			return DATA;
 		if (type.equals("XMLDATA"))
 			return XMLDATA;
 		if (type.equals("LANGUAGE"))
 			return LANGUAGE;
 		return UNKNOWN;
 	}
 
 	public FixMessageDom() {
 		packageName = System.getProperty("packageName", "org.tomac.protocol.fix.messaging");
 		final String[] ss = packageName.split("[.]");
 
 		packageNameBase = ss[0];
 		for (int i = 1; i < ss.length - 1; i++)
 			packageNameBase = packageNameBase + "." + ss[i];
 
 	}
 
 	private void addUnique(final DomFixField f, final DomFixField.DomFixValue e) {
 		DomFixField.DomFixValue q = null;
 
 		for (int i = 0; i < f.domFixValues.size(); i++) {
 			q = f.domFixValues.get(i);
 			if (q.fixEnum.equals(e.fixEnum) && q.description.equals(e.description))
 				return;
 		}
 
 		f.domFixValues.add(e);
 
 	}
 
 	/*
 	 * <fix major="4" minor="2" servicepack="0" type="FIX">
 	 */
 	@SuppressWarnings({ "unchecked" })
 	public void buildFrom(final Element fix) throws Exception {
 
 		for (final Iterator<Attribute> i = fix.attributeIterator(); i.hasNext();) {
 			final Attribute attribute = i.next();
 
 			if (attribute.getName().equals("type")) {
 				final String s = attribute.getValue().toLowerCase();
 				type = s.substring(0, 1).toUpperCase() + s.substring(1);
 			}
 			if (attribute.getName().equals("major"))
 				major = attribute.getValue();
 			if (attribute.getName().equals("minor"))
 				minor = attribute.getValue();
 			if (attribute.getName().equals("servicepack"))
 				servicepack = attribute.getValue();
 			if (attribute.getName().equals("package"))
 				packageName = attribute.getValue();
 			final String[] ss = packageName.split("[.]");
 			packageNameBase = ss[0];
 			for (int j = 1; j < ss.length - 1; j++)
 				packageNameBase = packageNameBase + "." + ss[j];
 			if (attribute.getName().equals("flavour"))
 				flavour = attribute.getValue();
 
 		}
 
 		// The order is important. Start from tag move up to component and message
 
 		getFields(fix);
 
 		for (final Iterator<Element> i = fix.elementIterator("header"); i.hasNext();) {
 			final Element component = i.next();
 			getComponent(component, domFixHeader, "header");
 		}
 
 		for (final Iterator<Element> i = fix.elementIterator("trailer"); i.hasNext();) {
 			final Element component = i.next();
 			getComponent(component, domFixTrailer, "trailer");
 		}
 
 		for (final Iterator<Element> i = fix.elementIterator("components"); i.hasNext();) {
 
 			final Element components = i.next();
 			//domFixNamedComponents.clear();
 
 			// repeating groups
 			for (final Iterator<Element> j = components.elementIterator("component"); j.hasNext();) {
 
 				DomFixComponent c;
 
 				final Element component = j.next();
 
 				String name = null;
 				for (final Iterator<Attribute> a = component.attributeIterator(); a.hasNext();) {
 					final Attribute attribute = a.next();
 					if (attribute.getName().equals("name"))
 						name = attribute.getValue();
 				}
 
 				for (final Iterator<Element> k = component.elementIterator("group"); k.hasNext();) {
 
 					final Element group = k.next();
 
 					String noInGroupTag = null;
 					for (final Iterator<Attribute> a = group.attributeIterator(); a.hasNext();) {
 						final Attribute attribute = a.next();
 						if (attribute.getName().equals("name"))
 							noInGroupTag = attribute.getValue();
 					}
 
 					// we may commabout a sub commponent  before the main is found, then it will already be in the named list
 					if (domFixNamedComponents.get(name) != null) {
 						c = domFixNamedComponents.get(name);
 						//System.out.println("Create, non existing component.. " + name);
 					} else {
 						c = new DomFixComponent();
 					}
 					c.isRepeating = true;
 					c.noInGroupTag = noInGroupTag;
 					
 					getComponent(group, c, name, noInGroupTag);
 
 					if (domFixNamedComponents.get(name) == null)
 						domFixNamedComponents.put(name, c);
 				}
 			}
 
 			// non repeating groups
 			for (final Iterator<Element> j = components.elementIterator("component"); j.hasNext();) {
 
 				DomFixComponent c;
 
 				final Element component = j.next();
 
 				String name = null;
 				for (final Iterator<Attribute> a = component.attributeIterator(); a.hasNext();) {
 					final Attribute attribute = a.next();
 					if (attribute.getName().equals("name"))
 						name = attribute.getValue();
 				}
 
 				// we may commabout a sub commponent  before the main is found, then it will already be in the named list
 				if (domFixNamedComponents.get(name) != null) {
 					c = domFixNamedComponents.get(name);
 					//System.out.println("Create, non existing component.. " + name);
 				} else 
 					c = new DomFixComponent();
 
 				getComponent(component, c, name);
 
 				if (domFixNamedComponents.get(name) == null)
 					domFixNamedComponents.put(name, c);
 			}
 
 		}
 
 		// set key for fix components
 		for (final DomFixComponent c : domFixNamedComponents.values()) {
 
 			final DomFixField f = domFixNamedFields.get(c.name);
 			domFixComponents.add(c);
 		}
 
 		getMessages(fix);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void getComponent(final Element component, final DomFixComponent c, final String componentName) {
 		c.name = new String(componentName);
 
 		for (final Iterator<Element> j = component.elementIterator(); j.hasNext();) {
 
 			final Element field = j.next();
 
 			if (field.getName().equals("field")) {
 				String name = null;
 				String required = "N";
 
 				for (final Iterator<Attribute> k = field.attributeIterator(); k.hasNext();) {
 					final Attribute attribute = k.next();
 
 					if (attribute.getName().equals("name"))
 						name = attribute.getValue();
 					if (attribute.getName().equals("required"))
 						required = attribute.getValue();
 				}
				final DomFixField f = new DomFixField(domFixNamedFields.get(name), required, component.indexOf(field));
 				c.fields.add(f);
 				c.fieldsAndComponents.add(f);
 			} else if (field.getName().equals("component")) {
 				String name = null;
 				String required = "N";
 
 				for (final Iterator<Attribute> k = field.attributeIterator(); k.hasNext();) {
 					final Attribute attribute = k.next();
 
 					if (attribute.getName().equals("name"))
 						name = attribute.getValue();
 					if (attribute.getName().equals("required"))
 						required = attribute.getValue();
 				}
 
 				if (domFixNamedComponents.get(name) == null) {
 					//System.out.println("What, non existing component.. " + name);
 				} 
 
 				
 				DomFixComponentRef cc = new DomFixComponentRef(name, required, component.indexOf(field));
 
 				c.components.add(cc);
 				c.fieldsAndComponents.add(cc);
 
 			}
 
 		}
 
 	}
 
 	/*
 	 * <header> <field name="BeginString" required="Y"/> .. </header> <trailer> <field name="CheckSum" required="Y"/> ..
 	 */
 	private void getComponent(final Element component, final DomFixComponent c, final String componentName, final String noInGroupTag) {
 		c.noInGroupTag = noInGroupTag;
 		c.isRepeating = true;
 
 		getComponent(component, c, componentName);
 	}
 
 	/*
 	 * <fields> <field name="PegDifference" number="211" type="PRICEOFFSET"/> <field name="ExecTransType" number="20" type="CHAR"> <value enum="0" description="NEW"/> ..
 	 */
 	@SuppressWarnings("unchecked")
 	private void getFields(final Element fix) {
 		for (final Iterator<Element> i = fix.elementIterator("fields"); i.hasNext();) {
 
 			final Element fields = i.next();
 
 			for (final Iterator<Element> j = fields.elementIterator("field"); j.hasNext();) {
 				final DomFixField f = new DomFixField();
 
 				final Element field = j.next();
 
 				for (final Iterator<Attribute> k = field.attributeIterator(); k.hasNext();) {
 					final Attribute attribute = k.next();
 
 					if (attribute.getName().equals("name"))
 						f.name = attribute.getValue();
 					if (attribute.getName().equals("number"))
 						f.number = attribute.getValue();
 					if (attribute.getName().equals("length"))
 						f.length = Integer.valueOf(attribute.getValue());
 					if (attribute.getName().equals("type"))
 						f.type = attribute.getValue();
 				}
 
 				for (final Iterator<Element> k = field.elementIterator("value"); k.hasNext();) {
 					String fixEnum = null;
 					String description = null;
 					final Element value = k.next();
 
 					for (final Iterator<Attribute> l = value.attributeIterator(); l.hasNext();) {
 						final Attribute attribute = l.next();
 
 						if (attribute.getName().equals("enum"))
 							fixEnum = attribute.getValue();
 						if (attribute.getName().equals("description"))
 							description = attribute.getValue();
 					}
 					final DomFixField.DomFixValue e = f.new DomFixValue(fixEnum, description);
 					addUnique(f, e);
 				}
 
 				domFixFields.add(f);
 				domFixNamedFields.put(f.name, f);
 
 			}
 		}
 	}
 
 	/*
 	 * <messages> <message name="AcceptedCancelReplace" msgcat="app" msgtype="8"> <field name="AvgPx" required="Y"/> ..
 	 */
 	@SuppressWarnings("unchecked")
 	private void getMessages(final Element fix) {
 		for (final Iterator<Element> i = fix.elementIterator("messages"); i.hasNext();) {
 
 			final Element messages = i.next();
 
 			for (final Iterator<Element> j = messages.elementIterator("message"); j.hasNext();) {
 				final DomFixMessage m = new DomFixMessage();
 
 				final Element message = j.next();
 
 				for (final Iterator<Attribute> k = message.attributeIterator(); k.hasNext();) {
 					final Attribute attribute = k.next();
 
 					if (attribute.getName().equals("name"))
 						m.name = attribute.getValue();
 					if (attribute.getName().equals("msgcat"))
 						m.msgcat = attribute.getValue();
 					if (attribute.getName().equals("msgtype"))
 						m.msgtype = attribute.getValue();
 					if (attribute.getName().equals("msgsubtype"))
 						m.msgsubtype = attribute.getValue();
 				}
 
 				for (final Iterator<Element> k = message.elementIterator("field"); k.hasNext();) {
 					String name = null;
 					String required = "N";
 
 					final Element value = k.next();
 
 					for (final Iterator<Attribute> l = value.attributeIterator(); l.hasNext();) {
 						final Attribute attribute = l.next();
 
 						if (attribute.getName().equals("name"))
 							name = attribute.getValue();
 						if (attribute.getName().equals("required"))
 							required = attribute.getValue();
 					}
 
 					if (domFixNamedFields.get(name) != null) {
 						final DomFixField f = new DomFixField(domFixNamedFields.get(name), required, message.indexOf(value));
 						m.fields.add(f);
 						m.fieldsAndComponents.add(f);
 					} else
 						System.out.println("domFixNamedFields: " + name);
 				}
 
 				for (final Iterator<Element> k = message.elementIterator("component"); k.hasNext();) {
 					String name = null;
 					String required = null;
 
 					final Element value = k.next();
 
 					for (final Iterator<Attribute> l = value.attributeIterator(); l.hasNext();) {
 						final Attribute attribute = l.next();
 
 						if (attribute.getName().equals("name"))
 							name = attribute.getValue();
 						if (attribute.getName().equals("required"))
 							required = attribute.getValue();
 					}
 
 					final DomFixComponentRef c = new DomFixComponentRef(name, required,  message.indexOf(value));
 					m.components.add(c);
 					m.fieldsAndComponents.add(c);
 					
 				}
 				
 				domFixMessages.add(m);
 			}
 		}
 	}
 	
 	// supporting classes
 	
 	public abstract class DomBase implements Comparable<DomBase> {
 		public int position;
 		public String name;
 
 		//@Override
 		public int compareTo(DomBase o) {
 			final DomBase q = (DomBase) o;
 			if (q.position > position) {
 				return -1;
 			}
 			if (q.position < position) {
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 
 		public abstract String getKeyTag();
 
 	}	
 	
 	/*
 	 <field number="4" name="AdvSide" type="CHAR">
 	 <value enum="B" description="BUY"/>
 	 <value enum="S" description="SELL"/>
 	 <value enum="T" description="TRADE"/>
 	 <value enum="X" description="CROSS"/>
 	 </field>
 	 */
 	public class DomFixField extends DomBase {
 
 		public class DomFixValue {
 			public String fixEnum;
 			public String description;
 
 			public DomFixValue(String fixEnum, String description) {
 				this.fixEnum = new String(fixEnum);
 				this.description = new String(description);
 			}
 
 		}
 
 		public String reqd;
 
 		public String number;
 		public String name;
 		public String type;
 		public ArrayList<DomFixValue> domFixValues = new ArrayList<DomFixValue>();
 		public boolean belongsToMessage = false;
 
 		public int length;
 
 		public DomFixField() {
 		}
 
 		public DomFixField(DomFixField q, String req, int position) {
 			number = q.number;
 			name = new String(q.name);
 			type = q.type;
 			length = q.length;
 			domFixValues = q.domFixValues;
 			belongsToMessage = q.belongsToMessage;
 			reqd = new String(req != null ? req : "0");
 			this.position = position;
 		}
 		
 		public String getKeyTag() {
 			return name;
 		}
 		
 	}
 	
 	/*
 	 * <component name="HopGrp" required="N"/>
 	 */
 	public class DomFixComponentRef extends DomBase {
 		String reqd;
 		
 		public DomFixComponentRef(String name, String reqd, int position) {
 			this.name = new String(name);
 			this.reqd = new String(reqd);
 			this.position = position;
 		}
 
 		@Override 
 		public String getKeyTag() {
 			DomFixComponent  c = domFixNamedComponents.get(name);
 			if (c.isRepeating) return c.noInGroupTag;
 			
 			if (c.fieldsAndComponents.isEmpty()) {
 				return null;
 			}
 			
 			if (c.fieldsAndComponents.first() instanceof DomFixComponentRef)
 				return ((DomFixComponentRef)c.fieldsAndComponents.first()).getKeyTag();
 			if (c.fieldsAndComponents.first() instanceof DomFixField)
 				return ((DomFixField)c.fieldsAndComponents.first()).getKeyTag();
 			return null;
 		}
 
 		public String getKeyTagHierarchy() {
 			DomFixComponent  c = domFixNamedComponents.get(name);
 			if (c.isRepeating) return c.noInGroupTag;
 			
 			if (c.fieldsAndComponents.isEmpty()) {
 				return null;
 			}
 			
 			if (c.fieldsAndComponents.first() instanceof DomFixComponentRef)
 				return ((DomFixComponentRef)c.fieldsAndComponents.first()).name + "." + FixMessageGenerator.uncapFirst(((DomFixComponentRef)c.fieldsAndComponents.first()).getKeyTag());
 			if (c.fieldsAndComponents.first() instanceof DomFixField)
 				return ((DomFixField)c.fieldsAndComponents.first()).getKeyTag();
 			return null;
 		}
 		
 		
 		public boolean isRepeating() {
 			DomFixComponent  c = domFixNamedComponents.get(name);
 			if (c.isRepeating) return true;
 			return false;
 		}
 
 		public String noInGroupTag() {
 			DomFixComponent  c = domFixNamedComponents.get(name);
 			if (c.isRepeating) return c.noInGroupTag;
 			return null;
 		}
 
 		
 	}
 	
 	/*
 	 <component name="Parties">
 	 <group name="NoPartyIDs" required="N">
 	 <field name="PartyID" required="N"/>
 	 <field name="PartyIDSource" required="N"/>
 	 <field name="PartyRole" required="N"/>
 	 <group name="NoPartySubIDs" required="N">
 	 <field name="PartySubID" required="N"/>
 	 <field name="PartySubIDType" required="N"/>
 	 </group>
 	 </group>
 	 </component>
 	 */
 	public class DomFixComponent {
 
 		public String name;
 		public SortedSet<DomBase> fieldsAndComponents;
 		public ArrayList<DomFixField> fields;
 		public ArrayList<DomFixComponentRef> components;
 		public boolean isRepeating;
 		public String noInGroupTag;
 
 		public DomFixComponent() {
 			fieldsAndComponents = new TreeSet<DomBase>();
 			fields = new ArrayList<DomFixField>();
 			components = new ArrayList<DomFixComponentRef>();
 		}
 
 	}
 	
 	/* <message name="Reject" msgcat="admin" msgtype="3"> */
 	public class DomFixMessage {
 		public String name;
 		public String msgcat;
 		public String msgtype;
 		public String msgsubtype = "";
 		public SortedSet<DomBase> fieldsAndComponents = new TreeSet<DomBase>();
 		public ArrayList<DomFixField> fields = new ArrayList<DomFixField>();
 		public ArrayList<DomFixComponentRef> components = new ArrayList<DomFixComponentRef>();
 		public String msgId;
 		public String specialization;
 
 	}	
 
 
 	
 	
 }
