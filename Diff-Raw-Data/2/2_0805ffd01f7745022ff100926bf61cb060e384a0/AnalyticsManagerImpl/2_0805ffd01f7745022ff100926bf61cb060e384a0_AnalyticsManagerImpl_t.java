 package org.ccci.obiee.client.rowmap.impl;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.ccci.obiee.client.rowmap.AnalyticsManager;
 import org.ccci.obiee.client.rowmap.DataRetrievalException;
 import org.ccci.obiee.client.rowmap.Query;
 import org.ccci.obiee.client.rowmap.ReportColumn;
 import org.ccci.obiee.client.rowmap.ReportDefinition;
 import org.ccci.obiee.client.rowmap.RowmapConfigurationException;
 import org.ccci.obiee.client.rowmap.SortDirection;
 import org.ccci.obiee.rowmap.annotation.ReportParamVariable;
 import org.ccci.obiee.rowmap.annotation.ReportPath;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.siebel.analytics.web.soap.v5.ReportEditingServiceSoap;
 import com.siebel.analytics.web.soap.v5.SAWSessionServiceSoap;
 import com.siebel.analytics.web.soap.v5.XmlViewServiceSoap;
 import com.siebel.analytics.web.soap.v5.model.QueryResults;
 import com.siebel.analytics.web.soap.v5.model.ReportParams;
 import com.siebel.analytics.web.soap.v5.model.ReportRef;
 import com.siebel.analytics.web.soap.v5.model.Variable;
 import com.siebel.analytics.web.soap.v5.model.XMLQueryExecutionOptions;
 import com.siebel.analytics.web.soap.v5.model.XMLQueryOutputFormat;
 
 /**
  * 
  * @author Matt Drees
  * @author William Randall
  *
  */
 public class AnalyticsManagerImpl implements AnalyticsManager
 {
 
     private final String sessionId;
     private final SAWSessionServiceSoap sawSessionService;
     private final XmlViewServiceSoap xmlViewService;
     private final XPathFactory xpathFactory;
     private XPathExpression xsdElementExpression;
     private XPathExpression rowExpression;
     private final DocumentBuilder builder;
     private final ConverterStore converterStore;
     private final ReportEditingServiceSoap reportEditingService;
     
     private boolean closed = false;
 
     /**
      * Assumes that the caller has logged us in to OBIEE already.  
      * 
      * @param sessionId used for logout
      * @param sawSessionService used for logout
      * @param xmlViewService used for retrieving report queries
      * @param converterStore
      */
     public AnalyticsManagerImpl(String sessionId, 
                                 SAWSessionServiceSoap sawSessionService,
                                 XmlViewServiceSoap xmlViewService,
                                 ReportEditingServiceSoap reportEditingService,
                                 ConverterStore converterStore)
     {
         this.sessionId = sessionId;
         this.sawSessionService = sawSessionService;
         this.xmlViewService = xmlViewService;
         this.reportEditingService = reportEditingService;
         this.converterStore = converterStore;
 
         xpathFactory = XPathFactory.newInstance();
         buildXpathExpressions();
         
         DocumentBuilderFactory factory;
         factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         try
         {
             builder = factory.newDocumentBuilder();
         }
         catch (ParserConfigurationException e)
         {
             throw new RowmapConfigurationException("unable to build document builder", e);
         }
     }
 
     private void buildXpathExpressions()
     {
         XPath xpath = xpathFactory.newXPath();
         xpath.setNamespaceContext(new RowsetNamespaceContext());
         try
         {
             xsdElementExpression = xpath.compile("/rowset:rowset/xsd:schema/xsd:complexType[@name='Row']/xsd:sequence/xsd:element");
             rowExpression = xpath.compile("/rowset:rowset/rowset:Row");
         }
         catch (XPathExpressionException e)
         {
             throw new RuntimeException("bad xpath", e);
         }
     }
 
     public void close()
     {
         checkOpen();
         closed = true;
         sawSessionService.logoff(sessionId);
     }
 
     private void checkOpen()
     {
         if (closed) throw new IllegalStateException("already closed");
     }
     
     public <T> Query<T> createQuery(ReportDefinition<T> reportDefinition)
     {
         checkOpen();
         if (reportDefinition == null)
            throw new NullPointerException("reportDefinition is null");
         return new QueryImpl<T>(reportDefinition);
     }
     
     class QueryImpl<T> implements Query<T>
     {
 
         private final ReportDefinition<T> reportDefinition;
         private Object selection;
         private ReportColumn<T> sortColumn;
         private SortDirection direction;
 
         public QueryImpl(ReportDefinition<T> reportDefinition)
         {
             this.reportDefinition = reportDefinition;
         }
 
         public Query<T> withSelection(Object selection)
         {
             if (selection == null) 
                 throw new NullPointerException("selection is null");
             if(!annotatedFieldsExist(selection))
             	throw new RowmapConfigurationException("You forgot to annotate the filter variables");
             this.selection = selection;
             return this;
         }
         
         public Query<T> orderBy(ReportColumn<T> sortColumn, SortDirection direction) 
         {
 			if (sortColumn == null)
 				throw new NullPointerException("sortColumn cannot be null.");
 			if (!reportDefinition.getColumns().contains(sortColumn))
 			{
 			    throw new IllegalArgumentException(String.format(
 			        "Sort column %s does not appear to be a column of report %s", 
 			        sortColumn, 
 			        reportDefinition.getName()
 		        ));
 			}
 			this.direction = direction;
 			this.sortColumn = sortColumn;
 			
 			return this;
 		}
 
         public List<T> getResultList()
         {
         	return query(reportDefinition.getRowType(), selection, sortColumn, direction);
         }
 
         public T getSingleResult()
         {
             List<T> resultList = getResultList();
             if (resultList.size() == 0)
                 //TODO: this message could be nicer, including the selection criteria
                 throw new DataRetrievalException("No rows were returned");
             if (resultList.size() > 1)
                 throw new DataRetrievalException("More than one row was returned");
             return resultList.get(0);
         }
         
         private boolean annotatedFieldsExist(Object selection)
         {
         	Class<?> clazz = selection.getClass();
             
         	for(Field field: clazz.getDeclaredFields())
         	{
                 if(field.getAnnotation(ReportParamVariable.class) != null)
         		{
                 	return true;
         		}
         	}
         	return false;
         }
     }
     
     private <T> List<T> query(Class<T> rowType, Object selector, ReportColumn<T> sortColumn, SortDirection direction)
     {
     	checkOpen();
     	ReportPath reportPathConfiguration = rowType.getAnnotation(ReportPath.class);
 
     	RowBuilder<T> rowBuilder;
     	NodeList rows;
     	ReportParams params = buildReportParams(selector);
         if(sortColumn != null)
         {
         	if(direction == null)
         	{
         		direction = SortDirection.ASCENDING;
         	}
         	
         	String metadata = queryForMetadata(reportPathConfiguration, params);
 	        Document metadataDoc = buildDocument(metadata);
 	        
             rowBuilder = buildRowBuilder(rowType, metadataDoc);
             String displayFormula = findSortDisplayFormula(sortColumn, metadataDoc);
             
             String sqlUsed = setupSqlForQuery(reportPathConfiguration, params, displayFormula, direction);
 	        String data = sqlQueryForData(sqlUsed, sessionId);
 	        Document dataDocument = buildDocument(data);
 	        
 	        rows = getRows(dataDocument);
         }
         else
         {
         	String rowset = queryForMetadataAndData(reportPathConfiguration, selector);
         	Document doc = buildDocument(rowset);
         	rowBuilder = buildRowBuilder(rowType, doc);
         	rows = getRows(doc);
         }
         
         List<T> results = new ArrayList<T>();
         for (Node row : each(rows))
         {
             T rowInstance = rowBuilder.buildRowInstance(row);
             results.add(rowInstance);
         }
         
         return results;
 	}
     
     private <T> String findSortDisplayFormula(ReportColumn<T> sortColumn, Document metadataDoc)
     {
         NodeList columnDefinitionXsdElements = getColumnSchemaNodesFromPreamble(metadataDoc);
         
         ReportColumnId sortColumnId = ReportColumnId.buildColumnId(sortColumn.getField());
         
         for (Node node : each(columnDefinitionXsdElements) )
         {
             ReportColumnId potentialColumnId = new ReportColumnId(
                 node.getAttributes().getNamedItem("saw-sql:tableHeading").getNodeValue(), 
                 node.getAttributes().getNamedItem("saw-sql:columnHeading").getNodeValue());
             if (sortColumnId.equals(potentialColumnId))
             {
                 return node.getAttributes().getNamedItem("saw-sql:displayFormula").getNodeValue();
             }
         }
         throw new DataRetrievalException("metadata does not indicate such a sort column exists: " + sortColumnId);
     }
 
     private ReportParams buildReportParams(Object selector)
     {
         ReportParams params = new ReportParams();
         if(selector != null)
         {
             Class<?> clazz = selector.getClass();
             
         	for(Field field: clazz.getDeclaredFields())
         	{
         		Object value = getValue(selector, field);
         		ReportParamVariable reportParamVar = field.getAnnotation(ReportParamVariable.class);
                 if(reportParamVar != null && value != null)
         		{
         			Variable var = createVariable(field, value, reportParamVar);
         			params.getVariables().add(var);
         		}
         	}
         }
         return params;
     }
 
     private Variable createVariable(Field field, Object value, ReportParamVariable reportParamVar)
     {
         Class<?> fieldType = field.getType();
         Variable var = new Variable();
         if(reportParamVar.name().equals(""))
         {
         	var.setName(field.getName());
         }
         else
         {
         	var.setName(reportParamVar.name());
         }
         if(fieldType.equals(String.class))
         {
         	var.setValue(value.toString());
         }
         else if(fieldType.equals(LocalDate.class))
         {
         	LocalDate ld = (LocalDate)value;
         	DateTime dTime = ld.toDateTime(new LocalTime(1,0,0,0));
         	Date dt = dTime.toDate();
         	var.setValue(dt);
         }
         else if(fieldType.equals(DateTime.class))
         {
         	DateTime dTime = (DateTime)value;
         	Date dt = dTime.toDate();
         	var.setValue(dt);
         }
         else if(fieldType.equals(Set.class))
         {
         	String stringValue = "";
         	
         	try
         	{
         		for(String s: (Set<String>)value)
             	{
             		stringValue = stringValue + "'" + s + "',";
             	}
             	stringValue = stringValue.substring(0,stringValue.length() - 1);
             	var.setValue(stringValue);
         	}
         	catch(RuntimeException e)
         	{
         		throw new RowmapConfigurationException("Unexpected data type passed in - field: " + field);
         	}
         }
         else
         {
         	throw new RowmapConfigurationException("Unexpected data type passed in - field: " + field);
         }
         return var;
     }
 
     private Object getValue(Object reportParams, Field field) throws AssertionError
     {
         field.setAccessible(true);
         Object value;
         try
         {
             value = field.get(reportParams);
         }
         catch(IllegalAccessException e)
         {
             AssertionError assertionError = new AssertionError("We called field.setAccessible(true)");
             assertionError.initCause(e);
             throw assertionError;
         }
         return value;
     }
     
     private String setupSqlForQuery(ReportPath reportPathConfiguration, ReportParams params, String sortFormula, SortDirection direction)
     {
     	ReportRef report = new ReportRef();
         report.setReportPath(reportPathConfiguration.value());
         
         String sqlUsed = reportEditingService.generateReportSQL(report, params, sessionId);
         
         return prepareSql(sqlUsed, sortFormula, direction);
     }
     
     private String queryForMetadata(ReportPath reportPathConfiguration, ReportParams reportParams)
     {
     	XMLQueryOutputFormat outputFormat = XMLQueryOutputFormat.SAW_ROWSET_SCHEMA;
         XMLQueryExecutionOptions executionOptions = new XMLQueryExecutionOptions();
         executionOptions.setMaxRowsPerPage(-1);
         executionOptions.setPresentationInfo(true);
         
         ReportRef report = new ReportRef();
         report.setReportPath(reportPathConfiguration.value());
 
         QueryResults results;
         try
         {
         	results = xmlViewService.executeXMLQuery(
         	    report, 
         	    outputFormat, 
         	    executionOptions, 
         	    reportParams, 
         	    sessionId);
         }
         catch(RuntimeException e)
         {
             throw new DataRetrievalException(
                 String.format(
                     "unable to query metadata for report %s with %s", 
                     reportPathConfiguration.value(),
                     formatParamsAsString(reportParams)), 
                 e);
         }
     	return results.getRowset();
     }
     
     private String sqlQueryForData(String sqlUsed, String sessionId)
     {
     	XMLQueryOutputFormat outputFormat = XMLQueryOutputFormat.SAW_ROWSET_DATA;
       
     	QueryResults results;
     	try
     	{
     		results = xmlViewService.executeSQLQuery(sqlUsed, outputFormat, new XMLQueryExecutionOptions(), sessionId);
     	}
     	catch (RuntimeException e)
         {
         	throw new DataRetrievalException(
         			String.format("unable to query with sql: ", sqlUsed), e);
         }
         return results.getRowset();
     }
     
     private String queryForMetadataAndData(ReportPath reportPathConfiguration, Object reportParams)
     {
         ReportRef report = new ReportRef();
         report.setReportPath(reportPathConfiguration.value());
         
         XMLQueryOutputFormat outputFormat = XMLQueryOutputFormat.SAW_ROWSET_SCHEMA_AND_DATA;
         XMLQueryExecutionOptions executionOptions = new XMLQueryExecutionOptions();
         executionOptions.setMaxRowsPerPage(-1);
         executionOptions.setPresentationInfo(true);
         
         ReportParams params = buildReportParams(reportParams);
         
         QueryResults queryResults;
         try
         {
     		queryResults = xmlViewService.executeXMLQuery(
                 report, 
                 outputFormat, 
                 executionOptions, 
                 params, 
                 sessionId);
         }
         catch (RuntimeException e)
         {
         	throw new DataRetrievalException(
                     String.format(
                         "unable to query report %s with %s", 
                         reportPathConfiguration.value(),
                         formatParamsAsString(params)), 
                     e);
         }
         
         return queryResults.getRowset();
     }
 
 	/**
 	 * Formats the SQL statement to be used for sorting.
 	 * @param sqlUsed
 	 * @param tableHead
 	 * @param colName
 	 * @return
 	 */
 	private String prepareSql(String sqlUsed, String sortFormula, SortDirection direction)
 	{
 		if(sqlUsed != null)
 		{
 			sqlUsed = removeOrderBy(sqlUsed);
 			sqlUsed = sqlUsed.concat(" ORDER BY " + sortFormula + " " + direction.toCode());
 		}
 		return sqlUsed;
 	}
 	
 	private String removeOrderBy(String sqlUsed)
 	{
 		if(!sqlUsed.contains("ORDER BY"))
 		{
 			return sqlUsed;
 		}
 		
 		int index = sqlUsed.indexOf(" ORDER BY");
 		sqlUsed = sqlUsed.substring(0,index);
 		return sqlUsed;
 	}
 	
 	private String formatParamsAsString(ReportParams params)
     {
         return String.format("[variables=%s]", asMap(params.getVariables()));
     }
 
     private Map<String, Object> asMap(List<Variable> variables)
     {
         Map<String, Object> variableMap = new HashMap<String, Object>();
         for (Variable variable : variables)
         {
             variableMap.put(variable.getName(), variable.getValue());
         }
         return variableMap;
     }
     
     <T> RowBuilder<T> buildRowBuilder(Class<T> rowType, Document doc)
     {
         NodeList columnDefinitionXsdElements = getColumnSchemaNodesFromPreamble(doc);
         
         Map<ReportColumnId, XPathExpression> columnValueExpressions = new HashMap<ReportColumnId, XPathExpression>();
         
         for (Node node : each(columnDefinitionXsdElements) )
         {
             String elementName = node.getAttributes().getNamedItem("name").getNodeValue();
             String tableHeading = node.getAttributes().getNamedItem("saw-sql:tableHeading").getNodeValue();
             String columnHeading = node.getAttributes().getNamedItem("saw-sql:columnHeading").getNodeValue();
             XPath xpath = xpathFactory.newXPath();
             xpath.setNamespaceContext(new RowsetNamespaceContext());
             
             XPathExpression columnValueExpression;
             try
             {
                 columnValueExpression = xpath.compile("rowset:" + elementName + "/text()");
             }
             catch (XPathExpressionException e)
             {
                 throw new RuntimeException(e);
             }
             
             columnValueExpressions.put(new ReportColumnId(tableHeading, columnHeading), columnValueExpression);
         }
         return new RowBuilder<T>(columnValueExpressions, rowType, converterStore);
     }
     
 
     Document buildDocument(String rowset)
     {
         InputSource inputsource = new InputSource(new StringReader(rowset));
         try
         {
             return builder.parse(inputsource );
         }
         catch (SAXParseException e)
         {
             throw new DataRetrievalException(
                 String.format(
                     "cannot parse rowset from OBIEE; error on line %s and column %s", 
                     e.getLineNumber(),
                     e.getColumnNumber()), 
                 e);
         }
         catch (SAXException e)
         {
             throw new DataRetrievalException("cannot parse rowset from OBIEE", e);
         }
         catch (IOException e)
         {
             throw new DataRetrievalException("cannot parse rowset from OBIEE", e);
         }
     }
 
     NodeList getColumnSchemaNodesFromPreamble(Document doc)
     {
         try
         {
             return (NodeList) xsdElementExpression.evaluate(doc, XPathConstants.NODESET);
         }
         catch (XPathExpressionException e)
         {
             throw new RuntimeException("unable to evaluate xpath expression on document", e);
         }
         
     }
     
     NodeList getRows(Document doc)
     {
         try
         {
             return (NodeList) rowExpression.evaluate(doc, XPathConstants.NODESET);
         }
         catch (XPathExpressionException e)
         {
             throw new RuntimeException("unable to evaluate xpath expression on document", e);
         }
         
     }
 
     static class RowsetNamespaceContext implements NamespaceContext {
 
         public String getNamespaceURI(String prefix) {
             if (prefix == null) throw new NullPointerException("Null prefix");
             else if ("rowset".equals(prefix)) return "urn:schemas-microsoft-com:xml-analysis:rowset";
             else if ("xsd".equals(prefix)) return XMLConstants.W3C_XML_SCHEMA_NS_URI;
             else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
             return XMLConstants.NULL_NS_URI;
         }
 
         // This method isn't necessary for XPath processing.
         public String getPrefix(String uri) {
             throw new UnsupportedOperationException();
         }
 
         // This method isn't necessary for XPath processing either.
         public Iterator<?> getPrefixes(String uri) {
             throw new UnsupportedOperationException();
         }
     }
 
     private Iterable<Node> each(final NodeList nodeList)
     {
         return new Iterable<Node>()
         {
             
             public Iterator<Node> iterator()
             {
                 return new Iterator<Node>()
                 {
                     int index = 0;
 
                     public boolean hasNext()
                     {
                         return nodeList.getLength() > index;
                     }
 
                     public Node next()
                     {
                         if (index == nodeList.getLength())
                             throw new NoSuchElementException();
                         Node next = nodeList.item(index);
                         index++;
                         return next;
                     }
 
                     public void remove()
                     {
                         throw new UnsupportedOperationException();
                     }
                 };
             }
         };
     }
 }
