 /*
 * sones GraphDB Java Client Library 
 * Copyright (C) 2007-2011 sones GmbH - http://www.sones.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
 
 package de.sones.GraphDBClient;
 
 import java.io.BufferedReader;
import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import sun.misc.BASE64Encoder;
 import com.sun.xml.internal.ws.util.ASCIIUtility;
 import org.jdom.Element;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.xml.sax.SAXException;
 
 import de.sones.GraphDBClient.Objects.BinaryProperty;
 import de.sones.GraphDBClient.Objects.HyperEdge;
 import de.sones.GraphDBClient.Objects.IEdge;
 import de.sones.GraphDBClient.Objects.IHyperEdge;
 import de.sones.GraphDBClient.Objects.ISingleEdge;
 import de.sones.GraphDBClient.Objects.Property;
 import de.sones.GraphDBClient.Objects.SingleEdge;
 import de.sones.GraphDBClient.Objects.Vertex;
 import de.sones.GraphDBClient.QueryResult.QueryResult;
 import de.sones.GraphDBClient.QueryResult.ResultType;
 
 
 @SuppressWarnings("restriction")
 public class GraphDBClient {
 	
 	/**
 	 * private member
 	 */
 	private HttpURLConnection connection;
 	
 	private String username;
 	
 	private String password;
 	
 	private String host;
 	
 	private int port;
 	
 	private String GQLPATTERN = "/gql?";
 	
 	private String restUri;
 	
 	private Namespace sones = Namespace.getNamespace("http://sones.com/QueryResultSchema.xsd");
 	
 	
 	/**
 	 * Constructor for a GraphDS REST Client. Instantiate one per GraphDB instance.
 	 * @param myHost The host to connect (e.g. 'localhost')
 	 * @param myUsername The username to the GarphDB
 	 * @param myPassword The password to the GraphDB
 	 * @param myPort The current port of the REST interface
 	 */
 	public GraphDBClient(String myHost, String myUsername, String myPassword, int myPort){
 		
 		setHost(myHost);
 		username = myUsername;
 		password = myPassword;
 		port = myPort;
 		
 		restUri = host + ":" + port + GQLPATTERN;
 		
 	}
 	
 	/**
 	 * Sets the host in the correct format
 	 * @param myHost
 	 */
 	public void setHost(String myHost){
 		if(!myHost.contains("http://")){
 			host = "http://" + myHost;
 		}
 		else{
 			host = myHost;
 		}
 	}
 	/**
 	 * 
 	 * @return the host used for the connection
 	 */
 	public String getHost(){return host;}
 	
 	
 	
 	/**
 	 * 
 	 * @param myGQLString
 	 * @return QueryResutlt which contains all information about the result of the query
 	 * @throws IOException
 	 * @throws JDOMException
 	 * @throws SAXException 
 	 */
 	public QueryResult Query(String myGQLString)throws IOException, JDOMException, SAXException{
 				
 		//fetch xml from the database
 		String responseXML = FetchGraphDBOutput(myGQLString);
 		
 		if(!"".equals(responseXML) && responseXML != null){
 		
 		
 		
 		Document xmlDoc = null;
 		
 		SAXBuilder builder = new SAXBuilder(false);
 		
 		xmlDoc =  builder.build(new StringReader(responseXML));
 		 
 		//add a working validation logic
         boolean isvalide = false; 
         
         
 		 
 		Element result = xmlDoc.getRootElement();
 		Element query = result.getChild("Query",sones);
 		
 		String language = query.getAttributeValue("Language");
 		String querystring = query.getAttributeValue("Value");		
 		ResultType resulttype = Enum.valueOf(ResultType.class, query.getAttributeValue("ResultType"));
 		long duration = Long.parseLong(query.getAttributeValue("Duration"));
 		long vertexcount = Long.parseLong(query.getAttributeValue("VerticesCount"));
 		String errormessage = null;
 		
 		if(resulttype.equals(ResultType.Failed)){
 			errormessage = query.getAttributeValue("Error");	
 		}
 		
 		Element vertexviewlist = result.getChild("VertexViews",sones);
 		return new QueryResult(querystring, language, resulttype, duration, errormessage, ParseVertexViews(vertexviewlist));
 		}
 		return null;
 	}
 	
 	/**
 	 * Receives the GraphDSREST output
 	 * @param myQueryString the GQL statement
 	 * @return The response string from the REST interface of the GraphDS
 	 * @throws IOException
 	 */
 	private String FetchGraphDBOutput(String myQueryString) throws IOException{
 		
 		/**
 		 * Connection settings
 		 */
		URL url = new URL(restUri);
 					
 		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST");
 		connection.setDoOutput(true);
 		connection.setReadTimeout(10000);
 		connection.setRequestProperty("User-Agent", "Java GraphDBClient");
 		
 		//just xml is supported to parse
 		connection.setRequestProperty("Accept", "application/xml");
 		
 		
 		
 		 //set the used credentials
 		String login = username + ":" + password;
 				
 		
 		BASE64Encoder encoder = new BASE64Encoder();			
 		String encodedlogin = encoder.encode(ASCIIUtility.getBytes(login));
 		
 		//add login information to the request header
 		connection.setRequestProperty("Authorization", "Basic " + encodedlogin);
		
		DataOutputStream outStream = new DataOutputStream(connection.getOutputStream()); 
		
		outStream.writeBytes(myQueryString); 
        outStream.flush(); 
        outStream.close(); 
		
 		String responseString = "";						
 		BufferedReader in = null;	
 		//Fetch the response stream
		
		
		
 		try{
 		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 		String inputLine;
 		StringBuilder result = new StringBuilder();	
 		
 		while((inputLine = in.readLine()) != null)
 		{
 			result.append(inputLine);
 			
 		}
 		
 		
 		
 		
 		responseString = result.toString();	
 		}
 		catch (Exception e) {
 			// TODO: handle exception
 		}
 		finally{
 			if(in != null){
 				in.close();
 			}
 			connection.disconnect();
 		}
 		//HACK remove Byte Order Mark (BOM) (http://de.wikipedia.org/wiki/Byte_Order_Mark)		
 		responseString = responseString.substring(responseString.indexOf('<'), responseString.length());
 			
 		return responseString;
 		
 	}
 	
 	/**
 	 * Some parser utilities
 	 */
 	
 	/**
 	 * Parser for each VertexViewList
 	 * @param myElement The xml node of the VertexViewList
 	 * @return List<Vertex> a list of vertices
 	 */
 	private List<Vertex> ParseVertexViews(Element myVertexViewList){
 		
 		List<Element> pendingVertices = myVertexViewList.getChildren("VertexView", sones);
 		List<Vertex> verticesList = new ArrayList<Vertex>(pendingVertices.size());
 		for (Element element : pendingVertices) {
 			verticesList.add(ParseVertexView(element));
 		}
 		
 		
 		return verticesList;
 	}
 	/**
 	 * Parser for each Vertex
 	 * @param myElement The xml node of the vertex
 	 * @return Vertex
 	 */
 	private Vertex ParseVertexView(Element myElement) {
 		List<Property> properties = null;
 		List<BinaryProperty> binproper = null;
 		List<IEdge> edges = null;
 		
 		properties = ParsePropertyList(myElement.getChild("Properties",sones));
 		
 		binproper = ParseBinaryPropertyList(myElement.getChild("BinaryProperties", sones));
 		
 		edges = ParseEdge(myElement.getChild("Edges", sones));
 		
 		
 		return new Vertex(properties,binproper,edges);
 	}
 	/**
 	 * Parser for each Edge
 	 * @param myEdges The xml node of the edge tupel
 	 * @return List<EdgeTupel> a list of edge tupel
 	 */
 	private List<IEdge> ParseEdge(Element myEdges) {
 		List<Element> pendingEdges = myEdges.getChildren("Edge", sones);
 		List<IEdge> edges = new ArrayList<IEdge>(pendingEdges.size());
 		
 		
 				
 		for (Element edge : pendingEdges) {
 			String type = edge.getAttributeValue("type",Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance")); 
 			if(type.equals("HyperEdgeView")){
 				edges.add(ParseHyperEdge(edge));
 			}
 			else if(type.equals("SingleEdgeView")){
 				edges.add(ParseSingleEdges(edge));
 			}
 		}
 													
 		return edges;
 	}
 	
 	/**
 	 * Parser for each single edge
 	 * @param mySingleEdge the xml node of the current single edge
 	 * @return SingleEdge
 	 */
 	private ISingleEdge ParseSingleEdges(Element mySingleEdge) {
 
 		String name = mySingleEdge.getChildText("Name",sones);
 		List<Property> edgeProperties = ParsePropertyList(mySingleEdge.getChild("Properties", sones));
 		Vertex targetvertex = ParseVertexView(mySingleEdge.getChild("TargetVertex", sones));
 		return new SingleEdge(name, edgeProperties, targetvertex);
 		
 	}
 
 	/**
 	 * Parser for each hyper edge
 	 * @param myHyperEdge the xml node of the current hyper edge
 	 * @return HyperEdge
 	 */
 	private IHyperEdge ParseHyperEdge(Element myHyperEdge) {
 		
 		String name = myHyperEdge.getChildText("Name",sones);
 		List<Property> edgeProperties = ParsePropertyList(myHyperEdge.getChild("Properties", sones));
 		List<Element> edges = myHyperEdge.getChildren("SingleEdge", sones);
 		List<ISingleEdge> singleedges = new ArrayList<ISingleEdge>(edges.size());
 		
 		for (Element element : edges) {
 			singleedges.add(ParseSingleEdges(element));
 		}
 		
 		return new HyperEdge(name,edgeProperties,singleedges);
 	}
 
 	/**
 	 * Parser for each BinaryProperty
 	 * @param myEdgeTupel The xml node of the binary property
 	 * @return List<EdgeTupel> a list of binary property
 	 */	
 	private List<BinaryProperty> ParseBinaryPropertyList(Element myBinaryProperty) {
 		List<Element> pendingBinProperties = myBinaryProperty.getChildren("BinaryProperty", sones);
 		
 		List<BinaryProperty> payload = new ArrayList<BinaryProperty>(pendingBinProperties.size());
 		for (Element binproperty : pendingBinProperties) {
 			String id = binproperty.getChildText("ID", sones);
 			String content = binproperty.getChildText("Content", sones);
 			payload.add(new BinaryProperty(id, content));
 		}
 		return payload;
 	}
 	/**
 	 * Parser for each Property
 	 * @param myEdgeTupel The xml node of the property
 	 * @return List<EdgeTupel> a list of property
 	 */
 	private List<Property> ParsePropertyList(Element myProperties) {
 
 		List<Element> pendingProperties = myProperties.getChildren("Property", sones);
 		
 		List<Property> payload  = new ArrayList<Property>(pendingProperties.size());
 		for (Element property : pendingProperties) {
 			String id = property.getChildText("ID", sones);
 			String type = property.getChildText("Type", sones);
 			Object value = parseAttribute(type,property.getChildText("Value", sones));
 			
 			payload.add(new Property(id, type, value));
 		}
 		return payload;
 	}
 	
 	
 	
 	/**
 	 * Parser for Attribute - types
 	 * @param myAttributeType
 	 * @param myAttributeValue
 	 * @return
 	 */
 	private Object parseAttribute(String myAttributeType, String myAttributeValue)
 	{
 		if("Double".equals(myAttributeType))
 		{
 			return Double.parseDouble(myAttributeValue);
 		} 
 		else if("Int64".equals(myAttributeType))
 		{
 			return Long.parseLong(myAttributeValue);
 		} 
 		else if("Int32".equals(myAttributeType))
 		{
 			return Integer.parseInt(myAttributeValue);
 		} 
 		else if("UInt64".equals(myAttributeType))
 		{
 			return Long.parseLong(myAttributeValue);
 		} 
 		else if("DateTime".equals(myAttributeType))
 		{
 			DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");			
 			Date date = null;
 			
 			try
 			{
 				date = formatter.parse(myAttributeValue);
 			} catch(ParseException ex)
 			{
 				return new Date();
 			}
 			return date;
 		} 
 		else if("Boolean".equals(myAttributeType))
 		{
 			return Boolean.parseBoolean(myAttributeValue);
 		} 
 		else if(myAttributeType.contains("ListCollectionWrapper")){
 			
 			//cheap hack!
 			//parse ListCollectionWrapper with regex an store it in an untyped list
 			String type = (String) myAttributeType.subSequence(myAttributeType.indexOf('(') + 1, myAttributeType.indexOf(')'));
 			
 			List collection = new ArrayList(0);
 			
 			Pattern pattern = Pattern.compile("(\\[(/?[^\\]]+)\\])");
 			Matcher matcher = pattern.matcher(myAttributeValue);
 			String match = "";
 			while(matcher.find()){
 				match = matcher.group();
 				Object in = parseAttribute(type,match.substring(1, match.length() - 1));
 				collection.add(in);
 			}
 						
 			return collection;
 			
 		}
 		else
 		{
 			return myAttributeValue;
 		}				
 	}
 	
 	
 	
 }
