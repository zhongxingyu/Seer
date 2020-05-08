 package org.virtual.sr;
 
 import static org.virtualrepository.spi.ImportAdapter.*;
 import static org.virtualrepository.spi.PublishAdapter.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;

 import org.virtual.sr.transforms.Asset2Rdf;
 import org.virtual.sr.transforms.Sdmx2Table;
 import org.virtual.sr.transforms.Sdmx2Xml;
 import org.virtual.sr.transforms.XmlTransform;
 import org.virtual.sr.transforms.codelist.Rdf2SdmxCodelist;
 import org.virtualrepository.Asset;
 import org.virtualrepository.impl.Type;
 import org.virtualrepository.sdmx.SdmxCodelist;
import org.virtualrepository.spi.*;
 import org.virtualrepository.tabular.Table;
 
 /**
  * The {@link ServiceProxy} for the Semantic Repository.
  * 
  * @author Fabio Simeoni
  *
  */
 public class RepositoryProxy implements ServiceProxy, Lifecycle {
 
 	/**
 	 * The name of the configuration file used by this proxy.
 	 */
 	private static final String CONFIGURATION_FILE = "sr.properties";
 
 	private RepositoryBrowser browser;
 	
 	private final List<Publisher<?,?>> publishers = new ArrayList<Publisher<?,?>>();
 	private final List<Importer<?,?>> importers = new ArrayList<Importer<?,?>>();
 	
 	@Override
 	public void init() throws Exception {
 	
 		Properties properties = new Properties();
 		
 		try {
 			properties.load(RepositoryProxy.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE));
 		}
 		catch(Exception e) {
 			throw new IllegalStateException("missing configuration: configuration file "+CONFIGURATION_FILE+" not on classpath");
 		}
 		
 		RepositoryConfiguration configuration = null;
 		try {
 			configuration = new RepositoryConfiguration(properties);
 		}
 		catch(Exception e) {
 			throw new IllegalStateException("invalid configuration (see cause) ",e);	
 		}
 		
 		
 		browser = new RepositoryBrowser(configuration);
 		publishers.add(publisherFor(SdmxCodelist.type,new Sdmx2Xml(),configuration));
 		
 		//base rdf codelist importer
 		RdfImporter<SdmxCodelist> rdfCodelistImporter = new RdfImporter<SdmxCodelist>(SdmxCodelist.type,configuration);
 		
 		//sdmx codelist importer
		Importer<SdmxCodelist,CodelistBean> sdmxCodelistImporter = ImportAdapter.adapt(rdfCodelistImporter, new Rdf2SdmxCodelist()); 
 		importers.add(sdmxCodelistImporter);
 		

                Importer<SdmxCodelist,Table> tableCodelistImporter = adapt(sdmxCodelistImporter,new Sdmx2Table()); 
 		importers.add(tableCodelistImporter);
 		
 	}
 	
 	
 	@Override
 	public Browser browser() {
 		return browser;
 	}
 
 	@Override
 	public List<? extends Importer<?, ?>> importers() {
 		return importers;
 	}
 
 	@Override
 	public List<? extends Publisher<?, ?>> publishers() {
 		return publishers;
 	}
 
 	//helper
 	private <A extends Asset,API> Publisher<A,API> publisherFor(Type<A> type, XmlTransform<API> transform, RepositoryConfiguration configuration) {
 		RepositoryPublisher<A> p = new RepositoryPublisher<A>(type, configuration);
 		return adapt(p,new Asset2Rdf<A,API>(transform));
 	}
 	
 }
