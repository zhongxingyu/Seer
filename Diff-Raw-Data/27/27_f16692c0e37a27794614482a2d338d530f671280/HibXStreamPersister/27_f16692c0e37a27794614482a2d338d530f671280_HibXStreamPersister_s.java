 /**
  * 
  */
 package org.geoserver.hibernate;
 
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.MetadataLinkInfo;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.catalog.hibernate.beans.LayerGroupInfoImplHb;
 import org.geoserver.catalog.hibernate.beans.LayerInfoImplHb;
 import org.geoserver.config.ContactInfo;
 import org.geoserver.config.GeoServerInfo;
 import org.geoserver.config.LoggingInfo;
 import org.geoserver.config.hibernate.beans.ContactInfoImplHb;
 import org.geoserver.config.hibernate.beans.GeoServerInfoImplHb;
 import org.geoserver.config.hibernate.beans.LoggingInfoImplHb;
 import org.geoserver.config.hibernate.beans.MetadataLinkInfoImplHb;
 import org.geoserver.config.util.XStreamPersister;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.collections.CollectionConverter;
 import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
 
 /**
  * @author Francesco
  * 
  */
 public class HibXStreamPersister extends XStreamPersister {
 
 	public HibXStreamPersister() {
 		super();
 	}
 
 	public HibXStreamPersister(HierarchicalStreamDriver streamDriver) {
 		super(streamDriver);
 	}
 
 	@Override
 	protected void init(XStream xs) {
                 super.init(xs);
 	         xs.registerLocalConverter(impl(StoreInfo.class),
 				"connectionParameters", new HibernateMapConverter());
 
 		xs.registerConverter(new HibernateMapConverter());
 		xs.registerConverter(new HibernateListConverter(xs));
         }
 
 	@Override
 	protected void initImplementationDefaults(XStream xs) {
 		super.initImplementationDefaults(xs);
 
 		xs.addDefaultImplementation(LayerInfoImplHb.class, LayerInfo.class);
 		xs.addDefaultImplementation(LayerGroupInfoImplHb.class,
 				LayerGroupInfo.class);
 		xs.addDefaultImplementation(ContactInfoImplHb.class, ContactInfo.class);
 		xs.addDefaultImplementation(GeoServerInfoImplHb.class,
 				GeoServerInfo.class);
 		xs.addDefaultImplementation(LoggingInfoImplHb.class, LoggingInfo.class);
 		xs.addDefaultImplementation(MetadataLinkInfoImplHb.class,
 				MetadataLinkInfo.class);
 
 		xs.addDefaultImplementation(
 				org.hibernate.collection.PersistentMap.class,
 				java.util.Map.class);

		

 	}
 
 	public class HibernateMapConverter extends BreifMapConverter {
 
 		public HibernateMapConverter() {
 			super();
 		}
 
 		@Override
 		public boolean canConvert(Class type) {
 			return type.equals(org.hibernate.collection.PersistentMap.class);
 		}
 	}
 
 	public class HibernateListConverter extends CollectionConverter implements
 			Converter {
 
 		public HibernateListConverter(XStream xs) {
 			super(xs.getMapper());
 		}
 
 		@Override
 		public boolean canConvert(Class type) {
 			return type.equals(org.hibernate.collection.PersistentList.class);
 		}
 
 	}
 }
