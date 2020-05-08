 package edu.gatech.cs7450.xnat;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * An xNAT table result that comes from a <code>&lt;ResultSet&gt;</code> XML response.
  * <p>
  * An <code>XNATResultSet</code> uses the <code>//columns/column</code> contents as 
  * the header values.  Rows come from <code>//rows/row</code> contents, with each <code>&lt;cell&gt;</code> 
  * being a column value.
  * </p><p>
  * In addition to the standard header handling, <code>SearchField</code>s are associated with 
  * the headers, when possible.  It may be preferable to use these for retrieving values as 
  * xNAT does some name-mangling of the normal header names based on how it joins data in the 
  * back-end.
  * </p>
  */
 public class XNATResultSet extends XNATTableResult {
 	/** Logging. */
 	private static final Logger _log = Logger.getLogger(XNATResultSet.class);
 	
 	/** Expected attribute names for search field columns. */
 	@SuppressWarnings("unused")
 	private static class ColAttrs {
 		private static final String 
 			EL_NAME = "element_name",
 			     ID = "id",
 			   TYPE = "type",
 			  XPATH = "xPATH",
 			 HEADER = "header";
 	}
 	
 	/** The root element of the whole result set. */
 	private String rootElement;
 	/** The number of records in the set, or <code>-1</code> if unknown. */
 	private int numRecords = -1;
 	
 	/** 
 	 * Search fields associated with headers, will match <code>headers</code> length 
 	 * but some entries may be <code>null</code>.
 	 */
 	private List<SearchField> headerFields;
 	
 	/** Maps header search fields to the value position. */
 	private Map<SearchField, Integer> headerFieldPosition;
 	
 	/** Search fields required in the response. */
 	private Set<SearchField> requiredHeaderFields = Collections.emptySet();
 	
 	/** Required search field values for rows. */
 	private Set<SearchField> requiredFieldValues = Collections.emptySet();
 
 	public XNATResultSet(String xmlData, Collection<SearchField> requiredHeaders, Collection<SearchField> requiredValues) 
 			throws IOException {
 		if( xmlData == null ) throw new NullPointerException("xmlData is null");
 		
 		if( requiredHeaders != null )
 			this.requiredHeaderFields = new HashSet<SearchField>(requiredHeaders);
 		if( requiredValues != null )
 			this.requiredFieldValues = new HashSet<SearchField>(requiredValues);
 		
 		this.parseData(wrapStringData(xmlData));
 	}
 	
 	public XNATResultSet(String xmlData) throws IOException {
 		if( xmlData == null ) throw new NullPointerException("xmlData is null");
 		this.parseData(wrapStringData(xmlData));
 	}
 
 	public XNATResultSet(InputStream in) throws IOException {
 		if( in == null ) throw new NullPointerException("in is null");
 		this.parseData(in);
 	}
 	
 	public XNATResultSet(InputStream in, Collection<SearchField> requiredHeaders, Collection<SearchField> requiredValues) 
 			throws IOException {
 		if( in == null ) throw new NullPointerException("in is null");
 		
 		if( requiredHeaders != null )
 			this.requiredHeaderFields = new HashSet<SearchField>(requiredHeaders);
 		if( requiredValues != null )
 			this.requiredFieldValues = new HashSet<SearchField>(requiredValues);
 		
 		this.parseData(in);
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 * Extends super to map header fields as well.
 	 */
 	@Override
 	protected void mapHeaders() {
 		super.mapHeaders(); // standard headers
 		
 		// now the same for search fields
 		headerFieldPosition = new HashMap<SearchField, Integer>();
 		for( int i = 0, ilen = headerFields.size(); i < ilen; ++i ) {
 			SearchField field = headerFields.get(i);
 			if( field != null )
 				headerFieldPosition.put(field, i);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * Extends super to check required search fields.
 	 */
 	@Override
 	protected void checkRequiredHeaders() throws XNATException {
 		super.checkRequiredHeaders();
 		
 		HashSet<SearchField> required = new HashSet<SearchField>(requiredHeaderFields);
 		required.removeAll(headerFieldPosition.keySet());
 		if( required.size() > 0 )
 			throw new XNATException("Missing required search fields: " + required);
 	}
 	
 	@Override
 	protected void parseData(InputStream reader) throws IOException {
 		if( reader == null ) throw new NullPointerException("reader is null");
 		
 		final String ERR_MSG = "Error parsing the result.";
 		try {
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 //			dbFactory.setNamespaceAware(true); // TODO do we actually want this here?
 			
 			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
 			Document doc = docBuilder.parse(reader);
 			
 			Element root = (Element)doc.getChildNodes().item(0);
 			rootElement = root.getAttribute("rootElementName");
 			try {
 				numRecords = Integer.parseInt(root.getAttribute("totalRecords"));
 			} catch( NumberFormatException e ) {
 				_log.warn("Could not parse totalRecords: " + root.getAttribute("totalRecords"));
 			}
 
 			// read headers
 			ArrayList<String> headers = new ArrayList<String>();
 		 	ArrayList<SearchField> headerFields = new ArrayList<SearchField>();
 			
 			NodeList columns = root.getFirstChild().getFirstChild().getChildNodes();
 			for( int i = 0, ilen = columns.getLength(); i < ilen; ++i ) {
 				// <column> content as standard header
 				Node column = columns.item(i);
 				String header = column.getTextContent();
 				headers.add(header);
 				
 				// now look for search field info in attributes
 				NamedNodeMap attrs = column.getAttributes();
 				if( attrs == null ) {
 					_log.warn(header + " has no attributes.");
 					headerFields.add(null);
 				} else {
 					Attr elNameAttr = (Attr)attrs.getNamedItem(ColAttrs.EL_NAME);
 					Attr idAttr = (Attr)attrs.getNamedItem(ColAttrs.ID);
 					
 					if( elNameAttr == null || idAttr == null ) {
 						_log.warn(header + " missing element name or id attributes.");
 						headerFields.add(null);
 					} else {
 						String elName = elNameAttr.getValue(),
 						        colId = idAttr.getValue();
 						SearchField searchField = SearchField.findCanonicalSearchField(elName, colId);
 						headerFields.add(searchField);
 						
 						if( searchField == null ) {
 							// log a bunch of data so we can fix this
 							_log.warn(header + " search field unknown: " + elName + "/" + colId);
 							for( int j = 0, jlen = attrs.getLength(); j < jlen; ++j ) {
 								Attr attr = (Attr)attrs.item(j);
 								_log.warn("Attr: " + attr.getName() + "=" + attr.getValue());
 							}
 						}
 					}
 				}
 			}
 			
 			// trim and assign to fields
 			headers.trimToSize();
 			this.headers = headers;
 			headerFields.trimToSize();
 			this.headerFields = headerFields;
 			
 			afterHeaders(); // check and map headers
 			
 			// read data values
 			ArrayList<XNATResultSetRow> rows = new ArrayList<XNATResultSetRow>();
 			NodeList rowNodes = root.getElementsByTagName("row");
 			readLoop: for( int i = 0, ilen = rowNodes.getLength(); i < ilen; ++i ) {
 				Element rowEl = (Element)rowNodes.item(i);
 				NodeList cells = rowEl.getElementsByTagName("cell");
 				
 				String[] values = new String[cells.getLength()];
 				for( int j = 0, jlen = cells.getLength(); j < jlen; ++j ) {
 					String value = cells.item(j).getTextContent();
 					
 					// filter if missing a required field
 					if( value == null || "".equals(value.trim()) ) {
 						if( this.headerFields.get(j) != null && requiredFieldValues.contains(headerFields.get(j)) ) {
 							_log.warn("Missing value for field: " + headerFields.get(j));
 							++filteredRows;
 							continue readLoop;
 						}
 					}
 					
 					values[j] = value;
 				}
 				
 				rows.add(new XNATResultSetRow(values));
 			}
 			rows.trimToSize();
 			this.rows = rows;
 			
 		// exception wrapping
 		} catch( ParserConfigurationException e ) {
 			_log.error(ERR_MSG, e);
 			throw new IOException(ERR_MSG, e);
 		} catch( SAXParseException e ) {
 			_log.error(ERR_MSG, e);
 			_log.error("[pubid=" + e.getPublicId() + ", sysid=" + e.getSystemId() + 
 				", line=" + e.getLineNumber() + ", col=" + e.getColumnNumber() + "]");
 			throw new IOException(ERR_MSG, e);
 		} catch( SAXException e ) {
 			_log.error(ERR_MSG, e);
 			throw new IOException(ERR_MSG, e);
 		}
 	}
 
 	// accessors
 	/** 
 	 * Returns the root (xNAT schema) element of this result set.
 	 * @return the root element
 	 */
 	public String getRootElement() {
 		return rootElement;
 	}
 	
 	/**
 	 * Returns the number of records reported (not calculated) or <code>-1</code> if unknown.
 	 * @return the number of records
 	 */
 	public int getNumRecords() {
 		return numRecords;
 	}
 	
 	/**
 	 * Returns an unmodifiable view of the search fields associated with the headers.
 	 * <p>
 	 * The length of the list will be the same as the number of headers, but some 
 	 * entries may be <code>null</code> if there is no search field associated.
 	 * </p>
 	 * 
 	 * @return the header fields
 	 */
 	public List<SearchField> getHeaderFields() {
 		return Collections.unmodifiableList(headerFields);
 	}
 	
 	/**
 	 * Returns whether the given search field is mapped to a header.
 	 * <p>
 	 * Note: Field equality relies on ALL members of <code>field</code> and mapping 
 	 * is done based on fields in subclasses of {@link XNATConstants}.  If you 
 	 * instantiated your own <code>SearchField</code> without all values filled 
 	 * in, you should use {@link SearchField#toCanonical()} to get the full version 
 	 * and pass that one here.
 	 * </p>
 	 * 
 	 * @param field the field
 	 * @return <code>true</code> if <code>field</code> maps to a column
 	 * @throws NullPointerException if <code>field</code> is <code>null</code>
 	 */
 	public boolean isHeaderField(SearchField field) {
 		if( field == null ) throw new NullPointerException("field is null");
 		return headerFieldPosition.containsKey(field);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<? extends XNATResultSetRow> getRows() {
 		return Collections.unmodifiableList((List<XNATResultSetRow>)rows);
 	}
 	
 	/** Row of data for an XNATResultSet. */
 	public class XNATResultSetRow extends XNATTableRow {
 
 		public XNATResultSetRow(Collection<String> values) {
 			super(values);
 		}
 
 		public XNATResultSetRow(String... values) {
 			super(values);
 		}
 		
 		/** @see XNATResultSet#getHeaderFields() */
 		public List<SearchField> getHeaderFields() {
 			return XNATResultSet.this.getHeaderFields();
 		}
 		
 		/** @see XNATResultSet#isHeaderField(SearchField) */
 		public boolean isHeaderField(SearchField field ) {
 			return XNATResultSet.this.isHeaderField(field);
 		}
 		
 		/**
 		 * Gets the row value by associated search field.
 		 * 
 		 * @param field the search field
 		 * @return the row value
 		 * @throws NullPointerException if <code>field</code> is <code>null</code>
 		 * @throws IllegalArgumentException if <code>field</code> is not mapped to a column
 		 */
 		public String getValue(SearchField field) {
 			if( field == null ) throw new NullPointerException("field is null");
 			Integer pos = headerFieldPosition.get(field);
 			if( pos == null )
 				throw new IllegalArgumentException("field is not mapped to a position.");
 			
 			int p = pos.intValue();
 			assert p >= 0 && p < values.size() : "FIXME: Mapped value is out of range.";
 			return values.get(p);
 		}
 		
 		/**
 		 * Returns the search fields missing a value in this row, where missing 
 		 * means <code>null</code> or blank.
 		 * <p>
 		 * Note this may return fewer results than {@link #getMissingColumns()} and {@link #getMissingHeaders()} 
 		 * when the missing columns are not associated with a search field.
 		 * </p>
 		 * 
 		 * @return the missing search fields
 		 */
 		public List<SearchField> getMissingFields() {
 			List<Integer> cols = getMissingColumns();
 			ArrayList<SearchField> missing = new ArrayList<SearchField>(cols.size());
 			for( Integer col : cols ) {
 				SearchField field = headerFields.get(col);
 				if( field != null )
 					missing.add(field);
 			}
 			return missing;
 		}
 	}
 }
