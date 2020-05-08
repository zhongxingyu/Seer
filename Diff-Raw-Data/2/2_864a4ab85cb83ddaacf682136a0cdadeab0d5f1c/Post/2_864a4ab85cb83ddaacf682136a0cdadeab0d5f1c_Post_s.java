 /**
  * Semantic Crawler Library
  *
  * Copyright (C) 2010 by Networld Project
  * Written by Alex Oberhauser <oberhauseralex@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.scrawler.sioc;
 
 import java.net.URL;
 import java.util.List;
 import java.util.Vector;
 
 import org.dom4j.Element;
 
 import to.networld.scrawler.common.RDFParser;
 import to.networld.scrawler.interfaces.*;
 
 /**
  * @author Alex Oberhauser
  *
  */
 public class Post extends RDFParser implements ISIOCPost {
 	
 	/**
 	 * 
 	 * @param _url The URL that points to a valid SIOC file
 	 * @throws Exception Generic exception, doesn't matter what error occurs the agent could not be instantiated.
 	 */
 	public Post(URL _url) throws Exception {
 		super(_url);
 		this.namespace.put("dive", "http://scubadive.networld.to/dive.rdf#");
 		this.namespace.put("foaf", "http://xmlns.com/foaf/0.1/");
 		this.namespace.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
 		this.namespace.put("dc", "http://purl.org/dc/elements/1.1/");
 		this.namespace.put("sioc", "http://rdfs.org/sioc/ns#");
 		this.namespace.put("dcterms", "http://purl.org/dc/terms/");
 		this.namespace.put("content", "http://purl.org/rss/1.0/modules/content/");
 
 		this.setQueryPrefix();
 	}
 	
 	/**
 	 * Set the query prefix that handles the node of the post that is described by the SIOC file.
 	 */
 	private void setQueryPrefix() {
 		List<Element> nameNodes = this.getLinkNodes("/rdf:RDF/foaf:Document/foaf:primaryTopic");
 		if (nameNodes.size() > 0) {
 			this.queryPrefix = "/rdf:RDF/sioc:Post[@*='" + nameNodes.get(0).valueOf("@rdf:resource") + "']";
 			if ( this.getLinkNodes(this.queryPrefix).size() > 0 )
 				return;
 			this.queryPrefix = "/rdf:RDF/sioc:Post[@*='" + nameNodes.get(0).valueOf("@rdf:resource").replace("#", "") + "']";
 			if ( this.getLinkNodes(this.queryPrefix).size() > 0 )
 				return;
 		}
 	}
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getLink()
 	 */
 	@Override
 	public String getLink() { return this.getSingleNodeResource("sioc:link", "rdf:resource"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getContent()
 	 */
 	@Override
 	public String getContent() { return this.getSingleNode("sioc:content"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getCreationDate()
 	 */
 	@Override
 	public String getCreationDate() { return this.getSingleNode("dcterms:created"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getTitle()
 	 */
 	@Override
 	public String getTitle() { return this.getSingleNode("dc:title"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getEncodedContent()
 	 */
 	@Override
 	public String getEncodedContent() { return this.getSingleNode("content:encoded"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getTopics()
 	 */
 	@Override
 	public Vector<String> getTopics() { return this.getNodesResource("sioc:topic", "rdfs:label"); }
 
 	/**
	 * @see to.networld.scrawler.interfaces.ISIOCPost#getCreator()
 	 */
 	@Override
 	public Vector<String> getCreators() { return this.getNodesResource("sioc:has_creator/sioc:User", "rdfs:label"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getMakers()
 	 */
 	@Override
 	public Vector<String> getMakers() { return this.getNodesResource("foaf:maker/foaf:Person", "rdfs:label"); }
 
 	/**
 	 * @see to.networld.scrawler.interfaces.ISIOCPost#getLinksTo()
 	 */
 	@Override
 	public Vector<String> getLinksTo() { return this.getNodesResource("sioc:links_to", "rdf:resource"); }
 
 }
