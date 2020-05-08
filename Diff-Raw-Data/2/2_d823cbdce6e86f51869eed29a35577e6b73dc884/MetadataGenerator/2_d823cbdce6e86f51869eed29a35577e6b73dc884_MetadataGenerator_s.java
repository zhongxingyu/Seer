 package org.fao.fi.gis.metadata;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.fao.fi.gis.metadata.association.GeographicMetaObject;
 import org.fao.fi.gis.metadata.association.GeographicMetaObjectImpl;
 import org.fao.fi.gis.metadata.collection.eez.EezEntity;
 import org.fao.fi.gis.metadata.collection.eez.FLODEezEntity;
 import org.fao.fi.gis.metadata.collection.rfb.FLODRfbEntity;
 import org.fao.fi.gis.metadata.collection.rfb.RfbEntity;
 import org.fao.fi.gis.metadata.collection.species.FLODSpeciesEntity;
 import org.fao.fi.gis.metadata.collection.species.SpeciesEntity;
 import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
 import org.fao.fi.gis.metadata.entity.EntityAddin;
 import org.fao.fi.gis.metadata.entity.GeographicEntity;
 import org.fao.fi.gis.metadata.model.MetadataConfig;
 import org.fao.fi.gis.metadata.publisher.Publisher;
 import org.fao.fi.gis.metadata.util.FeatureTypeUtils;
 import org.fao.fi.gis.metadata.utils.CollectionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.JsonObject;
 
 /**
  * Main App to launch the batch data/metadata publication
  * 
  */
 public class MetadataGenerator {
 
 	private static Logger LOGGER = LoggerFactory.getLogger(MetadataGenerator.class);
 
 	static Map<String, Map<EntityAddin, String>> set = null;
 	
 	
 	/**
 	 * Main
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 
 		//Read the configuration
 		LOGGER.info("(1) Loading the configuration file");
		MetadataConfig config = MetadataConfig.fromXML(new File("c:/gis/metadata/config/species.xml"));
 		
 		//read the codelists
 		LOGGER.info("(2) Loading the reference list");
 		String collectionType = config.getSettings().getPublicationSettings().getCollectionType();
 		LOGGER.info("Collection type = "+collectionType);
 		if(collectionType.matches("species")){
 			set = CollectionUtils.parseSpeciesList(config.getSettings()
 					.getPublicationSettings().getCodelistURL());
 			
 		}else if(collectionType.matches("eez")){
 			set = CollectionUtils.parseEezList(config.getSettings()
 					.getPublicationSettings().getCodelistURL());
 			
 		}else if(collectionType.matches("rfb")){
 			set = CollectionUtils.parseRfbList(config.getSettings()
 					.getPublicationSettings().getCodelistURL());
 		}
 		
 		//only for test (apply to the first element)
 		if(config.getSettings().getPublicationSettings().isTest()){
 			String testCode = (String) set.keySet().toArray()[0];
 			Map<EntityAddin,String> addin = set.get(testCode);
 			set = new HashMap<String, Map<EntityAddin,String>>();
 			set.put(testCode, addin);
 		}
 			
 		// configure the publisher
 		Publisher publisher = new Publisher(config);
 
 		int size = 0;
 
 		// UTILS
 		Set<String> metalist = new HashSet<String>();
 
 		List<String> existingLayers = publisher.getDataPublisher().GSReader
 				.getLayers().getNames(); // read once, improve performance
 
 		// iteration on the entities
 		LOGGER.info("(3) Start metadata creation & publication");
 		Iterator<String> entityIterator = set.keySet().iterator();
 		while (entityIterator.hasNext()) {
 
 			String code = entityIterator.next();
 			LOGGER.info("==============");
 			LOGGER.info("Publishing single layer & metadata for: "+code);
 
 			Map<FeatureTypeProperty, Object> geoproperties = null;
 			GeographicEntity entity = null;
 
 			boolean exist = existingLayers.contains(config.getSettings().getGeographicServerSettings().getTargetLayerPrefix() + "_" + code);
 			
 			String action = config.getSettings().getPublicationSettings().getAction();
 			LOGGER.info("== ACTION: "+action+" == ");
 			
 			//configure FLOD entity
 			JsonObject flodResponse = null;
 			if(collectionType.matches("species")){
 				FLODSpeciesEntity flodEntity = new FLODSpeciesEntity(code);
 				flodResponse = flodEntity.getFlodContent();
 				
 			}else if(collectionType.matches("eez")){
 				FLODEezEntity flodEntity = new FLODEezEntity(code);
 				flodResponse = flodEntity.getFlodContent();
 				
 			}else if(collectionType.matches("rfb")){
 				FLODRfbEntity flodEntity = new FLODRfbEntity(code);
 				flodResponse = flodEntity.getFlodContent();
 			}
 			
 			//CHECK ACTION
 			if (action.matches("CHECK")) {	
 				if (!exist) {
 					if (flodResponse != null) {
 						metalist.add(code);
 					}
 				}
 				
 			} else{
 				
 				// calculate geoproperties
 				if (action.matches("PUBLISH")){
 					while (geoproperties == null) {
 						geoproperties = FeatureTypeUtils
 								.computeFeatureTypeProperties(config.getSettings().getGeographicServerSettings(),
 															code, config.getSettings().getPublicationSettings().getBuffer());
 					}
 				}
 					
 				//configure entity
 				if (flodResponse != null) {
 					if(collectionType.matches("species")){			
 						entity = new SpeciesEntity(code, set.get(code), config);
 	
 					}else if(collectionType.matches("eez")){
 						entity = new EezEntity(code, set.get(code), config);	
 						
 					}else if(collectionType.matches("rfb")){
 						entity = new RfbEntity(code, set.get(code), config);
 						
 					}
 					
 					boolean figis = config.getSettings().getPublicationSettings().isFigis();
 					GeographicMetaObject metaObject = new GeographicMetaObjectImpl(Arrays.asList(entity), geoproperties, null, set.get(code), config);
 					
 					// PUBLISH ACTION
 					if (action.matches("PUBLISH")) {
 						String style = set.get(code).get(EntityAddin.Style);
 						LOGGER.debug(style);
 						publisher.publish(metaObject, style, exist);
 						size = size + 1;	
 						LOGGER.info(size + " published metalayers");
 					
 					// UNPUBLISH ACTION
 					}else if (action.matches("UNPUBLISH")) {
 						publisher.unpublish(metaObject, exist);
 					}
 					
 				}else{
 					metalist.add(code);
 				}
 			}
 		}
 
 		// list all items for which the metadata production was not performed
 		if(metalist.size() > 0){
 			LOGGER.info("=======================================");
 			LOGGER.info("=========== NOT PRODUCED ==============");
 			LOGGER.info("==================|====================");
 			LOGGER.info("==================V====================");
 			LOGGER.info("=======================================");
 			Iterator<String> it2 = metalist.iterator();
 			while (it2.hasNext()) {
 				LOGGER.info(it2.next());
 			}
 		}
 	}
 }
