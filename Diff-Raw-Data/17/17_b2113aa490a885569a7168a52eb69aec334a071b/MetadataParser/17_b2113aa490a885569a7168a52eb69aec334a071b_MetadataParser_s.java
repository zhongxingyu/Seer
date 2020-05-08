 /*
  File: MetadataParser.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.data.readers;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.bind.JAXBException;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.CyAttributes;
 import cytoscape.generated2.Date;
 import cytoscape.generated2.Description;
 import cytoscape.generated2.Format;
 import cytoscape.generated2.Identifier;
 import cytoscape.generated2.ObjectFactory;
 import cytoscape.generated2.RdfDescription;
 import cytoscape.generated2.RdfRDF;
 import cytoscape.generated2.Source;
 import cytoscape.generated2.Title;
 import cytoscape.generated2.Type;
 
 /**
  * Manipulates network meta data for loading and saving.
  * 
  * @author kono
  * 
  */
 public class MetadataParser {
 
 	public static final String DEFAULT_NETWORK_METADATA_LABEL = "Network Metadata";
 
 	private String metadataLabel;
 	private CyNetwork network;
 	private RdfRDF metadata;
 
 	private CyAttributes networkAttributes;
 	Map mapRDF;
 
 	// Metadata used in cytoscape. This is a subset of dublin core.
 	private static String[] defaultLabels = { "Title", "Identifier", "Source",
 			"Type", "Format", "Date", "Description" };
 
 	// Default values for new meta data. Maybe changed later...
 	private static final String DEF_URI = "http://www.cytoscape.org/";
 	private static final String DEF_TYPE = "Protein-Protein Interaction";
 	private static final String DEF_FORMAT = "Cytoscape-XGMML";
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param network
 	 *            Target network for editing metadata.
 	 */
 	public MetadataParser(CyNetwork network) {
 		this(network, DEFAULT_NETWORK_METADATA_LABEL);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param network
 	 *            Target network
 	 * @param metadataLabel
 	 *            Label used as a tag for this attribute.
 	 */
 	public MetadataParser(CyNetwork network, String metadataLabel) {
 		this.metadataLabel = metadataLabel;
 		this.network = network;
 		networkAttributes = Cytoscape.getNetworkAttributes();
 
 		// Extract Network Metadata from CyAttributes
 		mapRDF = networkAttributes.getAttributeMap(network.getIdentifier(),
 				metadataLabel);
 	}
 
 	/**
 	 * Build new metadata RDF structure based on given network information.
 	 * 
 	 * Data items in "defaultLabels" will be created and inserted into RDF
 	 * structure.
 	 * 
 	 * @throws URISyntaxException
 	 * 
 	 */
 	public Map makeNewMetadataMap() throws URISyntaxException {
 
 		Map dataMap = new HashMap();
 
 		// Extract default values from property
 		String defSource = CytoscapeInit.getProperties().getProperty(
 				"defaultMetadata.source");
 		String defType = CytoscapeInit.getProperties().getProperty(
 				"defaultMetadata.type");
 		String defFormat = CytoscapeInit.getProperties().getProperty(
 				"defaultMetadata.format");
 
 		for (int i = 0; i < defaultLabels.length; i++) {
 			if (defaultLabels[i] == "Date") {
 				java.util.Date now = new java.util.Date();
 				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				dataMap.put(defaultLabels[i], df.format(now));
 			} else if (defaultLabels[i] == "Title") {
 				dataMap.put(defaultLabels[i], network.getTitle());
 			} else if (defaultLabels[i] == "Source") {
 				URI sourceURI = null;
 				if (defSource == null) {
 					sourceURI = new URI(DEF_URI);
 				} else {
 					sourceURI = new URI(defSource);
 				}
 				dataMap.put(defaultLabels[i], sourceURI.toASCIIString());
 			} else if (defaultLabels[i] == "Type") {
 				if (defType == null) {
 					dataMap.put(defaultLabels[i], DEF_TYPE);
 				} else {
 					dataMap.put(defaultLabels[i], defType);
 				}
 			} else if (defaultLabels[i] == "Format") {
 				if (defFormat == null) {
 					dataMap.put(defaultLabels[i], DEF_FORMAT);
 				} else {
 					dataMap.put(defaultLabels[i], defFormat);
 				}
 			} else {
 				dataMap.put(defaultLabels[i], "N/A");
 			}
 		}
 		return dataMap;
 	}
 
 	/**
 	 * Get metadata as an RDF object
 	 * 
 	 * @return Network metadata in RdfRDF object
 	 * @throws JAXBException
 	 * @throws URISyntaxException
 	 */
 	public RdfRDF getMetadata() throws JAXBException, URISyntaxException {
 		ObjectFactory objFactory = new ObjectFactory();
 		metadata = objFactory.createRdfRDF();
 		RdfDescription dc = objFactory.createRdfDescription();
 
 		// Set "about" for RDF
 		dc.setAbout(DEF_URI);
 
 		if (mapRDF == null || mapRDF.keySet().size() == 0) {
 			mapRDF = makeNewMetadataMap();
 		} else {
 			mapRDF = makeMetadataMap();
 		}
 
 		Set labels = mapRDF.keySet();
 		Object value = null;
 		String key = null;
 
 		Iterator it = labels.iterator();
 		while (it.hasNext()) {
 			key = (String) it.next();
 			value = mapRDF.get(key);
			dc.getDcmes().add(set(key.trim(), value));
 		}
 
 		metadata.getDescription().add(dc);

 		// Put the data in CyAttributes
 		networkAttributes.setAttributeMap(network.getIdentifier(),
 				metadataLabel, mapRDF);
 
 		return metadata;
 	}
 
 	/**
 	 * Get Network Metadata as Map object
 	 * 
 	 * @return
 	 * @throws URISyntaxException
 	 */
 	public Map getMetadataMap() throws URISyntaxException {
 		if (mapRDF == null || mapRDF.keySet().size() == 0) {
 			mapRDF = makeNewMetadataMap();
 		} else {
 			mapRDF = makeMetadataMap();
 		}
 		return mapRDF;
 	}
 
 	/**
 	 * Set data to JAXB-generated objects
 	 * 
 	 * @param label
 	 * @param value
 	 * @return
 	 * @throws JAXBException
 	 */
 	private Object set(String label, Object value) throws JAXBException {
 		ObjectFactory objF = new ObjectFactory();
 		Object newObj = null;
 
 		if (label == "Date") {
 			Date dt = objF.createDate();
 			dt.getContent().add(value);
 			return dt;
 		} else if (label == "Title") {
 			Title tl = objF.createTitle();
 			tl.getContent().add(value);
 			return tl;
 		} else if (label == "Identifier") {
 			newObj = objF.createIdentifier();
 			((Identifier) newObj).getContent().add(value);
 		} else if (label == "Description") {
 			Description dsc = objF.createDescription();
 			dsc.getContent().add(value);
 			return dsc;
 		} else if (label == "Source") {
 			newObj = objF.createSource();
 			((Source) newObj).getContent().add(value);
 		} else if (label == "Type") {
 			newObj = objF.createType();
 			((Type) newObj).getContent().add(value);
 		} else if (label == "Format") {
 			newObj = objF.createFormat();
 			((Format) newObj).getContent().add(value);
 		} else {
 			return null;
 		}
 		return newObj;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws URISyntaxException
 	 */
 	private Map makeMetadataMap() throws URISyntaxException {
 
 		Map dataMap = new HashMap();
 
 		for (int i = 0; i < defaultLabels.length; i++) {
 			if (defaultLabels[i] == "Date") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Date"));
 			} else if (defaultLabels[i] == "Title") {
 				dataMap.put(defaultLabels[i], network.getTitle());
 			} else if (defaultLabels[i] == "Source") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Source"));
 			} else if (defaultLabels[i] == "Type") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Type"));
 			} else if (defaultLabels[i] == "Format") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Format"));
 			} else if (defaultLabels[i] == "Description") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Description"));
 			} else if (defaultLabels[i] == "Identifier") {
 				dataMap.put(defaultLabels[i], mapRDF.get("Identifier"));
 			}
 		}
 		return dataMap;
 	}
 }
