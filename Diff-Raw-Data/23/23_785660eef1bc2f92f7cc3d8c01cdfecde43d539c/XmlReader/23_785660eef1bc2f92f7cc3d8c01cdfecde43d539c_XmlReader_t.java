 /*
  * Copyright 2012 Robert Philipp
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.freezedry.persistence.readers;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.freezedry.persistence.PersistenceEngine;
 import org.freezedry.persistence.tests.Division;
 import org.freezedry.persistence.tree.InfoNode;
 import org.freezedry.persistence.utils.Constants;
 import org.freezedry.persistence.utils.DomUtils;
 import org.freezedry.persistence.writers.XmlWriter;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * Reads the specified XML input stream into an {@link InfoNode} tree. The {@link InfoNode} tree
  * is populated with content and type information specified in the XML, but no more. Use the 
  * {@link PersistenceEngine} to convert the {@link InfoNode} to an object. 
  * 
  * @author Robert Philipp
  */
 public class XmlReader implements PersistenceReader {
 
 	private static final Logger LOGGER = Logger.getLogger( XmlReader.class );
 	
 	private boolean isRemoveEmptyTextNodes;
 	
 	/**
 	 * Default no-arg constructor
 	 */
 	public XmlReader()
 	{
 		isRemoveEmptyTextNodes = true;
 	}
 	
 	/**
 	 * Set to true to remove whitespace characters that are used for formatting; false to leave them
 	 * @param isRemove Set to true to remove whitespace characters that are used for formatting; 
 	 * false to leave them
 	 */
 	public void setRemoveEmptyTextNodes( final boolean isRemove )
 	{
 		isRemoveEmptyTextNodes = isRemove;
 	}
 	
 	/**
 	 * @return true if the reader removes whitespace characters used to format the XML; false otherwise
 	 */
 	public boolean getRemoveEmptyTextNodes()
 	{
 		return isRemoveEmptyTextNodes;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.readers.PersistenceReader#read(java.lang.Class, java.io.InputStream)
 	 */
 	@Override
 	public InfoNode read( final Class< ? > clazz, final Reader input )
 	{
 		// load the xml file into a DOM document node
 		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		Document document = null;
 		try
 		{
 			// create the document builder that creates the DOM tree
 			final DocumentBuilder builder = factory.newDocumentBuilder();
 			
 			// parse the input stream into a DOM tree and remove the empty text nodes
 			// that may have been in the XML due to formatting (we have to do this unless
 			// we use a validating builder, for which we need an XML schema).
 			final ReaderInputStream inputStream = new ReaderInputStream( input );
 			document = builder.parse( inputStream );
 			if( isRemoveEmptyTextNodes )
 			{
 				document = DomUtils.removeFormattingTextNodes( document );
 			}
 		}
 		catch( ParserConfigurationException e )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "Unable to create the DOM document builder:" + Constants.NEW_LINE );
 			message.append( "  Class Name: " + clazz.getName() );
 			LOGGER.error( message.toString() );
 			throw new IllegalStateException( message.toString(), e );
 		}
 		catch( SAXException | IOException e )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "Unable to parse the input stream into a DOM document (root node):" + Constants.NEW_LINE );
 			message.append( "  Class Name: " + clazz.getName() );
			LOGGER.error( message.toString(), e );
 			throw new IllegalStateException( message.toString(), e );
 		}
 		
 		// log the DOM tree
 		if( LOGGER.isInfoEnabled() )
 		{
 			LOGGER.info( DomUtils.toString( document ) );
 		}
 
 		// convert the DOM tree to an InfoNode tree (from which we can build the object)
 		final InfoNode node = buildInfoNode( clazz, document );
 		
 		return node;
 	}
 	
 	/**
 	 * Recursively build the {@link InfoNode} tree representation of the DOM tree
 	 * @param rootClass The root {@link Class} represented by the XML file
 	 * @param document The root node of the DOM
 	 * @return The {@link InfoNode} tree representing the DOM
 	 * @throws ClassNotFoundException
 	 */
 	public static InfoNode buildInfoNode( final Class< ? > rootClass, final Document document ) 
 	{
 		// grab the document's root element
 		final Node rootNode = getRootDomNode( document );
 		
 		// create the root info node from the document's root element
 		final InfoNode rootInfoNode = createRootInfoNode( rootClass, rootNode );
 		
 		// recursively build out the InfoNode tree from the DOM tree
 		buildInfoNode( rootNode, rootInfoNode );
 		
 		return rootInfoNode;
 	}
 
 	/*
 	 * Recursive algorithm for building the {@link InfoNode} from the DOM tree
 	 * @param domNode The DOM node from which to build the {@link InfoNode}
 	 * @param infoNode The {@link InfoNode} to which to add the new {@link InfoNode}
 	 * @throws ClassNotFoundException
 	 */
 	private static void buildInfoNode( final Node domNode, final InfoNode infoNode )
 	{
 		// grab the sub nodes for the current DOM and match them against the fields of the class
 		final NodeList domSubnodes = domNode.getChildNodes();
 		for( int i = 0; i < domSubnodes.getLength(); ++i )
 		{
 			// grab the sub node for the index, create an info node, and add it to the parent
 			final Node domSubnode = domSubnodes.item( i );
 			final InfoNode newInfoNode = createInfoNode( domSubnode );
 			infoNode.addChild( newInfoNode );
 		}
 	}
 	
 	/*
 	 * Creates the {@link InfoNode} from the information available in the DOM node
 	 * @param domNode The DOM node from which to create the {@link InfoNode}
 	 * @return The {@link InfoNode} representing the DOM node
 	 * @throws ClassNotFoundException
 	 */
 	private static InfoNode createInfoNode( final Node domNode )
 	{
 		// grab the persistence name associated with the node
 		final String persistName = domNode.getNodeName();
 		
 		// if the type attribute has been specified, then add it to the node
 		final NamedNodeMap attributes = domNode.getAttributes();
 		Class< ? > type = null;
 		if( attributes != null )
 		{
 			final Node attributeNode = attributes.getNamedItem( XmlWriter.TYPE_ATTRIBUTE );
 			if( attributeNode != null )
 			{
 				final String typeName = attributeNode.getNodeValue();
 				try
 				{
 					type = Class.forName( typeName );
 				}
 				catch( ClassNotFoundException e )
 				{
 					final StringBuffer message = new StringBuffer();
 					message.append( "Unable to instantiate class." + Constants.NEW_LINE );
 					message.append( "  Type Name: " + typeName + Constants.NEW_LINE );
 					message.append( "  Persist Name: " + persistName + Constants.NEW_LINE );
 					LOGGER.error( message.toString() );
 					throw new IllegalStateException( message.toString() );
 				}
 			}
 		}
 		
 		// we assume that the node either contains an element or text. nodes that contain
 		// elements are compound nodes, and nodes that contain text are leaf nodes.
 		final NodeList domSubnodes = domNode.getChildNodes();
 		String nodeValue = null;
 		final int numNodes = domSubnodes.getLength();
 		for( int i = 0; i < numNodes; ++i )
 		{
 			final Node subnode = domSubnodes.item( i );
 			if( subnode.getNodeType() == Node.TEXT_NODE )
 			{
 				nodeValue = subnode.getNodeValue();
 				break;
 			}
 		}
 		
 		// validate our assumption
 		if( numNodes > 1 && nodeValue != null )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "Nodes can either have elements or one text element." );
 			LOGGER.error( message.toString() );
 			throw new IllegalStateException( message.toString() );
 		}
 		
 		InfoNode infoNode = null;
 		// compound node (no text value and there are subnodes
 		if( nodeValue == null && numNodes > 0 )
 		{
 			// create the compound info node and then recursively to build out its children 
 			infoNode = InfoNode.createCompoundNode( null, persistName, type );
 			buildInfoNode( domNode, infoNode );
 		}
 		// leaf node (text value empty and there are no subnodes
 		else if( nodeValue == null && numNodes <= 0 )
 		{
 			infoNode = InfoNode.createLeafNode( null, "", persistName, type );
 		}
 		// leaf node (text value, we assume that there can be no subnodes)
 		else
 		{
 			infoNode = InfoNode.createLeafNode( null, nodeValue, persistName, type );
 		}
 		
 		return infoNode;
 	}
 	
 	/*
 	 * Retrieves the root DOM node from the specified document
 	 * @param document The document node representing the XML document
 	 * @return The root DOM node 
 	 */
 	private static Node getRootDomNode( final Document document )
 	{
 		// grab the root node out of the document, which is the child of the document
 		final NodeList nodes = document.getChildNodes();
 		if( nodes.getLength() != 1 )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "XML document must have exactly one root node. Parser shouldn't have allowed this." );
 			message.append( Constants.NEW_LINE );
 			message.append( "  Number of nodes: " + nodes.getLength() );
 			LOGGER.error( message.toString() );
 			throw new IllegalStateException( message.toString() );
 		}
 		final Node rootNode = nodes.item( 0 );
 		
 		return rootNode;
 	}
 	
 	/*
 	 * Creates the root {@link InfoNode} which is root DOM node representation
 	 * @param rootClass The {@link Class} representing the root node
 	 * @param rootNode The root DOM node
 	 * @return The {@link InfoNode} representation of the root DOM node
 	 * @throws ClassNotFoundException
 	 */
 	private static InfoNode createRootInfoNode( final Class< ? > rootClass, final Node rootNode )
 	{
 		// create the root info node (at this point the persist node is the same
 		// as the field name....to be changed TODO
 		final String persistName = rootNode.getNodeName();
 		final String fieldName = persistName;
 		
 		// take the rootClass as the class for which to create the root node, unless the
 		// xml "type" attribute has a different value.
 		Class< ? > clazz = rootClass;
 		if( rootNode.hasAttributes() )
 		{
 			final Node attributeNode = rootNode.getAttributes().getNamedItem( XmlWriter.TYPE_ATTRIBUTE );
 			if( attributeNode != null )
 			{
 				final String typeName = attributeNode.getNodeValue();
 				try
 				{
 					clazz = Class.forName( typeName );
 				}
 				catch( ClassNotFoundException e )
 				{
 					final StringBuffer message = new StringBuffer();
 					message.append( "Unable to instantiate class." + Constants.NEW_LINE );
 					message.append( "  Type Name: " + typeName + Constants.NEW_LINE );
 					message.append( "  Persist Name: " + persistName + Constants.NEW_LINE );
 					LOGGER.error( message.toString() );
 					throw new IllegalStateException( message.toString() );
 				}
 			}
 		}
 		
 		return InfoNode.createRootNode( fieldName, clazz );
 	}
 	
 	/**
 	 * 
 	 * @param args
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws SecurityException
 	 * @throws ReflectiveOperationException
 	 */
 	public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException, SecurityException, ReflectiveOperationException
 	{
 		DOMConfigurator.configure( "log4j.xml" );
 		
 		final XmlReader reader = new XmlReader();
 //		reader.setRemoveEmptyTextNodes( false );
 		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.xml" ) );
 		final Reader input = new InputStreamReader( inputStream );
 		final InfoNode infoNode = reader.read( Division.class, input );
 		System.out.println( infoNode.simpleTreeToString() );
 		
 		final PersistenceEngine engine = new PersistenceEngine();
 		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
 		System.out.println( reperson );
 	}
 }
