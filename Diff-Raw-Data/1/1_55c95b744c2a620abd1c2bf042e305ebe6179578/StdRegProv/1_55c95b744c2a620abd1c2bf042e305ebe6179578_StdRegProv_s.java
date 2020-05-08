 // Copyright (C) 2012 jOVAL.org.  All rights reserved.
 
 package jwsmv.cim;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import org.dmtf.wsman.AttributableDuration;
 import org.dmtf.wsman.AttributableURI;
 import org.dmtf.wsman.OptionSet;
 import org.dmtf.wsman.OptionType;
 
 import jwsmv.Constants;
 import jwsmv.wsman.FaultException;
 import jwsmv.wsman.Port;
 
 /**
  * A WSMV-based implementation of the WMI StdRegProv class.
  *
  * @author David A. Solin
  * @version %I% %G%
  */
 public class StdRegProv implements Constants {
     public static final long HKEY_CLASSES_ROOT	= 0x80000000L;
     public static final long HKEY_CURRENT_USER	= 0x80000001L;
     public static final long HKEY_LOCAL_MACHINE	= 0x80000002L;
     public static final long HKEY_USERS		= 0x80000003L;
     public static final long HKEY_CURRENT_CONFIG= 0x80000005L;
     public static final long HKEY_DYN_DATA	= 0x80000006L;
 
     public static final int REG_NONE		= 0;
     public static final int REG_DWORD		= 1;
     public static final int REG_BINARY		= 2;
     public static final int REG_SZ		= 3;
     public static final int REG_EXPAND_SZ	= 4;
     public static final int REG_MULTI_SZ	= 5;
     public static final int REG_QWORD		= 6;
 
     static final String CLASS_URI = "http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/StdRegProv";
     static final DocumentBuilder BUILDER;
     static {
 	try {
 	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	    dbf.setNamespaceAware(true);
 	    BUILDER = dbf.newDocumentBuilder();
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
     }
 
     private Port port;
     private Document doc;
     private String arch = null;
 
     /**
      * Create a new Registry, using the default provider architecture.
      */
     public StdRegProv(Port port) {
 	this.port = port;
 	doc = BUILDER.newDocument();
     }
 
     /**
      * Create a new Registry using the specified provider architecture.
      *
      * @param view Use 32 or 64.
 //
 // DAS: One cannot select the provider architecture via MS-WSMV, on account of Microsoft internal defect
 //      ID #SR112120710065406, so for now, I have commented out this constructor.
 //
     public StdRegProv(Port port, int view) throws IllegalArgumentException {
 	this(port);
 	switch(view) {
 	  case 32:
 	    arch = "32";
 	    break;
 
 	  case 64:
 	    arch = "64";
 	    break;
 
 	  default:
 	    throw new IllegalArgumentException(Integer.toString(view));
 	}
     }
      */
 
     /**
      * List all the subkeys under a registry key.
      *
      * @param hive one of the HKEY_* constants
      * @param subkey the path of the subkey to enumerate
      */
     public String[] enumKey(long hive, String subkey) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 
 	Element params = doc.createElementNS(CLASS_URI, "EnumKey_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 
 	Object result = port.dispatch(CLASS_URI + "/EnumKey", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("EnumKey_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "sNames");
 		    int len = nodes.getLength();
 		    ArrayList<String> subkeys = new ArrayList<String>(len);
 		    for (int i=0; i < len; i++) {
 			subkeys.add(nodes.item(i).getTextContent());
 		    }
 		    return subkeys.toArray(new String[len]);
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * List all the values (and their types) stored under a registry key.
      *
      * @param hive one of the HKEY_* constants
      * @param subkey the path of the subkey whose values will be enumerated
      */
     public Value[] enumValues(long hive, String subkey) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 
 	Element params = doc.createElementNS(CLASS_URI, "EnumValues_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 
 	Object result = port.dispatch(CLASS_URI + "/EnumValues", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("EnumValues_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "sNames");
 		    int len = nodes.getLength();
 		    ArrayList<Value> values = new ArrayList<Value>(len);
 		    for (int i=0; i < len; i++) {
 			Value value = new Value();
 			value.name = nodes.item(i).getTextContent();
 			values.add(value);
 		    }
 		    nodes = elt.getElementsByTagNameNS(CLASS_URI, "Types");
 		    for (int i=0; i < len; i++) {
 			values.get(i).type = Integer.parseInt(nodes.item(i).getTextContent());
 		    }
 		    return values.toArray(new Value[len]);
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_BINARY value from the registry.
      */
     public byte[] getBinaryValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetBinaryValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetBinaryValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetBinaryValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "uValue");
 		    int len = nodes.getLength();
 		    byte[] data = new byte[len];
 		    for (int i=0; i < len; i++) {
 			data[i] = (byte)(0xFF & Short.parseShort(nodes.item(i).getTextContent()));
 		    }
 		    return data;
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_DWORD value from the registry.
      */
     public int getDwordValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetDWORDValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetDWORDValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetDWORDValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "uValue");
 		    int len = nodes.getLength();
 		    if (len == 1) {
 			return Integer.parseInt(nodes.item(0).getTextContent());
 		    } else {
 			throw new Exception("Unexpected return value quantity: " + len);
 		    }
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_EXPAND_SZ value from the registry. The returned value will be expanded.
      */
     public String getExpandedStringValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetExpandedStringValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetExpandedStringValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetExpandedStringValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "sValue");
 		    int len = nodes.getLength();
 		    if (len == 1) {
 			return nodes.item(0).getTextContent();
 		    } else {
 			throw new Exception("Unexpected return value quantity: " + len);
 		    }
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_MULTI_SZ value from the registry.
      */
     public String[] getMultiStringValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetMultiStringValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetMultiStringValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetMultiStringValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "sValue");
 		    int len = nodes.getLength();
 		    if (len == 0) {
 			return null;
 		    } else {
 			String[] data = new String[len];
 			for (int i=0; i < len; i++) {
 			    data[i] = nodes.item(i).getTextContent();
 			}
 			return data;
 		    }
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_SZ value from the registry.
      */
     public String getStringValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetStringValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetStringValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetStringValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "sValue");
 		    int len = nodes.getLength();
 		    if (len == 1) {
 			return nodes.item(0).getTextContent();
 		    } else {
 			throw new Exception("Unexpected return value quantity: " + len);
 		    }
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Get a REG_QWORD value from the registry.
      */
     public long getQwordValue(long hive, String subkey, String value) throws Exception {
 	Element defKey = doc.createElementNS(CLASS_URI, "hDefKey");
 	defKey.setTextContent(Long.toString(hive));
 	Element subKeyName = doc.createElementNS(CLASS_URI, "sSubKeyName");
 	subKeyName.setTextContent(subkey);
 	Element valueName = doc.createElementNS(CLASS_URI, "sValueName");
 	valueName.setTextContent(value);
 
 	Element params = doc.createElementNS(CLASS_URI, "GetQWORDValue_INPUT");
 	params.appendChild(defKey);
 	params.appendChild(subKeyName);
 	params.appendChild(valueName);
 
 	Object result = port.dispatch(CLASS_URI + "/GetQWORDValue", getDispatchHeaders(), params);
 	if (result instanceof Element) {
 	    Element elt = (Element)result;
 	    if ("GetQWORDValue_OUTPUT".equals(elt.getLocalName())) {
 		int hResult = getHResult(elt);
 		switch(hResult) {
 		  case 0:
 		    NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "uValue");
 		    int len = nodes.getLength();
 		    if (len == 1) {
 			return Long.parseLong(nodes.item(0).getTextContent());
 		    } else {
 			throw new Exception("Unexpected return value quantity: " + len);
 		    }
 
 		  default:
 		    throw new Exception("Unexpected result code: " + hResult);
 		}
 	    } else {
 		throw new Exception("Unexpected element: " + elt.getLocalName());
 	    }
 	} else {
 	    throw new Exception("Unexpected return type: " + result.getClass().getName());
 	}
     }
 
     /**
      * Container for information about a registry value.
      */
     public class Value {
 	private int type;
 	private String name;
 
 	private Value() {}
 
 	public String getName() {
 	    return name;
 	}
 
 	/**
 	 * The REG_* constant corresponding to the type of this value.
 	 */
 	public int getType() {
 	    return type;
 	}
 
 	@Override
 	public String toString() {
 	    String sType = null;
 	    switch(type) {
 	      case StdRegProv.REG_NONE:
 		sType = "REG_NONE      ";
 		break;
 	      case StdRegProv.REG_DWORD:
 		sType = "REG_DWORD     ";
 		break;
 	      case StdRegProv.REG_BINARY:
 		sType = "REG_BINARY    ";
 		break;
 	      case StdRegProv.REG_SZ:
 		sType = "REG_SZ	      ";
 		break;
 	      case StdRegProv.REG_EXPAND_SZ:
 		sType = "REG_EXPAND_SZ ";
 		break;
 	      case StdRegProv.REG_MULTI_SZ:
 		sType = "REG_MULTI_SZ  ";
 		break;
 	      case StdRegProv.REG_QWORD:
 		sType = "REG_QWORD     ";
 		break;
 	    }
 	    return new StringBuffer(sType).append(name).toString();
 	}
     }
 
     // Private
 
     /**
      * Get the call result code from the element.
      */
     private int getHResult(Element elt) throws IllegalArgumentException {
 	NodeList nodes = elt.getElementsByTagNameNS(CLASS_URI, "ReturnValue");
 	int len = nodes.getLength();
 	if (len == 1) {
 	    return Integer.parseInt(nodes.item(0).getTextContent());
 	} else {
 	    throw new IllegalArgumentException("Unexpected return value quantity: " + len);
 	}
     }
 
     /**
      * Get dispatch headers for invoking methods of the StdRegProv WMI class.
      */
     private List<Object> getDispatchHeaders() {
 	List<Object> headers = new ArrayList<Object>();
 
 	AttributableURI uri = Factories.WSMAN.createAttributableURI();
 	uri.setValue(CLASS_URI);
 	uri.getOtherAttributes().put(MUST_UNDERSTAND, "true");
 	headers.add(Factories.WSMAN.createResourceURI(uri));
 
 	//
 	// Set the appropriate provider architecture using an OptionSet, if one was specified.
 	//
 	if (arch != null) {
 	    OptionSet options = Factories.WSMAN.createOptionSet();
 	    headers.add(options);
 
 	    OptionType architecture = Factories.WSMAN.createOptionType();
 	    architecture.setName("wmi:__ProviderArchitecture");
 	    architecture.setType(new QName(XMLNS, "int"));
 	    architecture.setValue(arch);
 	    options.getOption().add(architecture);
 	}
 
 	AttributableDuration duration = Factories.WSMAN.createAttributableDuration();
 	duration.setValue(Factories.XMLDT.newDuration(60000));
 	headers.add(Factories.WSMAN.createOperationTimeout(duration));
 
 	return headers;
     }
 }
