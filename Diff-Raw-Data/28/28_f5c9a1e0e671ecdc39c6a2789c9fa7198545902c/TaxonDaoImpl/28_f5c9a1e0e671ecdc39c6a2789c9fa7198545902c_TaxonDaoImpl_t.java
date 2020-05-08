 package com.danielvaughan.taxonomy.server.daos.impl;
 
 import com.danielvaughan.taxonomy.server.ServerConstants.IndexId;
 import com.danielvaughan.taxonomy.server.ServerErrorMessages;
 import com.danielvaughan.taxonomy.server.ServerInfoMessages;
 import com.danielvaughan.taxonomy.server.daos.TaxonDao;
 import com.danielvaughan.taxonomy.shared.model.Taxon;
 import com.danielvaughan.taxonomy.shared.model.Taxon.TaxonField;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.graphdb.index.IndexHits;
 import org.neo4j.graphdb.index.IndexManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 @Component
 public class TaxonDaoImpl implements TaxonDao {
 
   @Autowired
   private GraphDatabaseService graphDbService;
 
   private final IndexId taxIdIndexId = IndexId.TAXID_INDEX;
   private final IndexId textIndexId = IndexId.TEXT_INDEX;
   private final Log log = LogFactory.getLog(TaxonDaoImpl.class);
 
   private Index<Node> taxIdIndex;
   private Index<Node> textIndex;
 
   @Transactional
   @Override
   public void batchAddTaxons(List<Taxon> taxons) {
     for (Taxon taxon : taxons) {
       addTaxon(taxon);
     }
   }
 
   @Transactional
   @Override
   public long addTaxon(Taxon taxon) {
     final Node taxonNode = getNodeByTaxId(taxon.getTaxId());
     if (taxonNode == null) {
       log.info(ServerInfoMessages.ADDING_TAXON + taxon.getTaxId());
       return createTaxonNode(taxon).getId();
     } else {
       log.info(ServerInfoMessages.RETRIEVED_TAXON + taxon.getTaxId());
       return taxonNode.getId();
     }
   }
 
   @Transactional
   public Taxon getTaxonByTaxId(String taxId) {
     final Node seedNode = getNodeByTaxId(taxId);
     return buildModel(seedNode);
   }
 
   private Taxon buildModel(Node node) {
     final String taxId = (String) node.getProperty(TaxonField.TAX_ID.name(), "");
     final String scientificName = (String) node.getProperty(TaxonField.SCIENTIFIC_NAME.name(), "");
     final String commonName = (String) node.getProperty(TaxonField.COMMON_NAME.name(), "");
     final Taxon taxon = new Taxon(taxId, scientificName, commonName);
     return taxon;
   }
 
   private Node createIndexedNode(String key) {
     final Node node = createNode();
     key = key.toLowerCase();
     getTaxIdIndex().add(node, taxIdIndexId.name(), key);
     log.debug("Indexed key: " + key + " in index " + taxIdIndexId.name());
     return node;
   }
 
   private Node createNode() {
     final Node node = graphDbService.createNode();
     try {
       log.debug("Created node " + node.getId());
     } catch (final Exception e) {
       log.error("Error creating node", e);
     }
     return node;
   }
 
   private Node createTaxonNode(Taxon taxon) {
     final String externalKey = taxon.getTaxId();
     final Node node = createIndexedNode(externalKey);
     addTextToIndex(node, taxon);
     try {
       node.setProperty(TaxonField.TAX_ID.name(), taxon.getTaxId());
       node.setProperty(TaxonField.SCIENTIFIC_NAME.name(), taxon.getScientificName());
       if (taxon.getCommonName() != null) {
         node.setProperty(TaxonField.COMMON_NAME.name(), taxon.getCommonName());
       }
       return node;
     } catch (final Exception e) {
       log.error(ServerErrorMessages.TAXON_NODE_CREATION_FAILED, e);
       return node;
     }
   }
 
   private void addTextToIndex(Node node, Taxon taxon) {
     getTextIndex().add(node, textIndexId.name(), taxon.getTaxId());
     if (taxon.getCommonName() != null) {
       getTextIndex().add(node, textIndexId.name(), taxon.getCommonName().toLowerCase());
     }
     getTextIndex().add(node, textIndexId.name(), taxon.getScientificName().toLowerCase());
   }
 
   private Node getNodeByTaxId(String taxId) {
     Node node = null;
     try {
       log.debug("Looking for " + taxId + " in " + taxIdIndexId.name());
       final IndexHits<Node> nodes = getTaxIdIndex().get(taxIdIndexId.name(), taxId);
       if (nodes.size() > 1) {
         log.error("More than one node found for " + taxId);
       }
       node = nodes.getSingle();
       if (node == null) {
         log.debug("node not found");
       } else {
         log.debug("found node");
       }
     } catch (final NoSuchElementException e) {
       log.error("Exception finding unique node", e);
     }
     return node;
   }
 
   private Index<Node> getTaxIdIndex() {
     if (taxIdIndex == null) {
       final IndexManager index = graphDbService.index();
       taxIdIndex = index.forNodes(taxIdIndexId.name());
     }
     return taxIdIndex;
   }
 
   private Index<Node> getTextIndex() {
     if (textIndex == null) {
       final IndexManager index = graphDbService.index();
       textIndex = index.forNodes(textIndexId.name());
     }
    return textIndex;
   }
 
   @Override
   public List<Taxon> searchTaxons(String searchString, int resultLimit) {
     final List<Taxon> taxons = new ArrayList<Taxon>();
     final Set<Node> taxonNodes = getNodesByPartialKey(searchString);
     int count = 0;
     for (final Node taxonNode : taxonNodes) {
       if (count <= resultLimit) {
         taxons.add(buildModel(taxonNode));
         count++;
       }
     }
     return taxons;
   }
 
   private Set<Node> getNodesByPartialKey(String partialKey) {
     try {
       final Set<Node> nodes = new HashSet<Node>();
       if (partialKey.trim().equals("")) {
         log.debug("Empty key sent");
       } else {
        String query = partialKey.toLowerCase().concat("*");
        Index<Node> textIndex = getTextIndex();
        final IndexHits<Node> hits = textIndex.query(textIndexId.name(), query);
         for (final Node hit : hits) {
           nodes.add(hit);
         }
       }
       return nodes;
     } catch (final RuntimeException re) {
       log.warn("Runtime exception when using key: " + partialKey, re);
       return null;
     }
   }
 }
