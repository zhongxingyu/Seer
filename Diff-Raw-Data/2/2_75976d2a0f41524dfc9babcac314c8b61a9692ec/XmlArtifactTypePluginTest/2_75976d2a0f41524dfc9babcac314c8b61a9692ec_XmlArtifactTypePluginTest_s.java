 package org.mule.galaxy.impl.plugin;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.artifact.ArtifactType;
 import org.mule.galaxy.index.Index;
 import org.mule.galaxy.plugins.config.jaxb.ConfigurationType;
 import org.mule.galaxy.plugins.config.jaxb.GalaxyArtifactType;
 import org.mule.galaxy.plugins.config.jaxb.IndexType;
 import org.mule.galaxy.plugins.config.jaxb.IndexesType;
 import org.mule.galaxy.plugins.config.jaxb.NamespaceType;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.mule.galaxy.util.DOMUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class XmlArtifactTypePluginTest extends AbstractGalaxyTest {
     protected Dao<ArtifactType> artifactTypeDao;
     
     public void testUpgrade() throws Exception {
         
         GalaxyArtifactType type = new GalaxyArtifactType();
         
         type.setContentType("application/foo");
         type.setDescription("Foo");
         type.setName("Foo");
         
         NamespaceType ns = new NamespaceType();
         ns.setLocalName("foo");
         ns.setUri("http://foo");
         type.getNamespace().add(ns);
         
         IndexType index = new IndexType();
         index.setDescription("Foo Index");
         index.setIndexer("xpath");
         index.setSearchInputType(String.class.getName());
         
         Document d = DOMUtils.createDocument();
         Element root = d.createElement("root");
         d.appendChild(root);
         
         Element prop = d.createElementNS("urn:test", "property");
         prop.appendChild(d.createTextNode("foo"));
         root.appendChild(prop);
         
         Element exp = d.createElementNS("urn:test", "expression");
         exp.appendChild(d.createTextNode("//foo"));
         root.appendChild(exp);
         
         ConfigurationType config = new ConfigurationType();
         config.getAny().add(prop);
         config.getAny().add(exp);
         index.setConfiguration(config);
         
         IndexesType indexes = new IndexesType();
         indexes.getIndex().add(index);
         type.setIndexes(indexes);
         
         XmlArtifactTypePlugin plugin = new XmlArtifactTypePlugin(type);
         plugin.setArtifactTypeDao(artifactTypeDao);
         plugin.setIndexManager(indexManager);
         plugin.setPolicyManager(policyManager);
         plugin.setRegistry(registry);
         plugin.setTypeManager(typeManager);
         
         List<ArtifactType> allArtifactTypes = artifactTypeDao.listAll();
         Collection<Index> allIndexes = indexManager.getIndexes();
         
         plugin.doInstall();
 
         List<ArtifactType> allArtifactTypes2 = artifactTypeDao.listAll();
         assertEquals(allArtifactTypes.size() + 1, allArtifactTypes2.size());
 
         Collection<Index> allIndexes2 = indexManager.getIndexes();
         assertEquals(allIndexes.size() + 1, allIndexes2.size());
         
         ns = new NamespaceType();
         ns.setLocalName("foo");
         ns.setUri("http://foo/v2");
         type.getNamespace().add(ns);
         
        plugin.doUpgrade(previousVersion);
 
         Collection<Index> allIndexes3 = indexManager.getIndexes();
         assertEquals(allIndexes2.size(), allIndexes3.size());
         
         Index idx = indexManager.getIndexByName("Foo Index");
         assertEquals(2, idx.getDocumentTypes().size());
     }
 }
