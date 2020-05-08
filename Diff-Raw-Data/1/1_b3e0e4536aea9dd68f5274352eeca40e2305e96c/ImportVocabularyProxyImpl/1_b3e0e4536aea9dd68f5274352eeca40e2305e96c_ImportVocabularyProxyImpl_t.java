 package eu.europeana.uim.gui.cp.server;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.definitions.jibx.LiteralType;
 import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.dereference.exceptions.VocabularyNotFoundException;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 import eu.europeana.corelib.dereference.impl.Extractor;
 import eu.europeana.corelib.dereference.impl.RdfMethod;
 import eu.europeana.corelib.dereference.impl.VocabularyMongoServer;
 import eu.europeana.uim.gui.cp.client.services.ImportVocabularyProxy;
 import eu.europeana.uim.gui.cp.server.util.PropertyReader;
 import eu.europeana.uim.gui.cp.server.util.UimConfigurationProperty;
 import eu.europeana.uim.gui.cp.shared.ControlledVocabularyDTO;
 import eu.europeana.uim.gui.cp.shared.EdmFieldDTO;
 import eu.europeana.uim.gui.cp.shared.MappingDTO;
 import eu.europeana.uim.gui.cp.shared.OriginalFieldDTO;
 
 public class ImportVocabularyProxyImpl extends
 		IntegrationServicesProviderServlet implements ImportVocabularyProxy {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static ControlledVocabularyImpl controlledVocabulary;
 
 	private final static String MONGO_HOST = PropertyReader
 			.getProperty(UimConfigurationProperty.MONGO_HOSTURL);
 	private final static int MONGO_PORT = Integer.parseInt(PropertyReader
 			.getProperty(UimConfigurationProperty.MONGO_HOSTPORT));
 	private final static String MONGO_DB = PropertyReader.getProperty(UimConfigurationProperty.MONGO_DB_VOCABULARY);
 	private static Extractor extractor;
 	private final static String repository = PropertyReader.getProperty(UimConfigurationProperty.UIM_REPOSITORY);
 	private static VocabularyMongoServer mongo;
 
 	{
 		try {
 			mongo = new VocabularyMongoServer(
 					new Mongo(MONGO_HOST, MONGO_PORT), MONGO_DB);
 
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public ControlledVocabularyDTO importVocabulary(
 			ControlledVocabularyDTO vocabulary) {
 		controlledVocabulary = new ControlledVocabularyImpl(
 				vocabulary.getName());
 
 		controlledVocabulary.setURI(vocabulary.getUri());
 		controlledVocabulary.setLocation(repository
 				+ vocabulary.getLocation());
 		if (StringUtils.isNotEmpty(vocabulary.getSuffix())) {
 			controlledVocabulary.setSuffix(vocabulary.getSuffix());
 		}
 
 		controlledVocabulary.setRules(vocabulary.getRules());
 		controlledVocabulary.setIterations(vocabulary.getIterations());
 		
 		controlledVocabulary.setReplaceUrl(vocabulary.getReplaceUrl()!=null?vocabulary.getReplaceUrl():null);
 		extractor = new Extractor(controlledVocabulary, mongo);
 		vocabulary.setMapping(convertEdmMap(extractor.readSchema(repository+vocabulary
 				.getName())));
 		return vocabulary;
 	}
 
 	public MappingDTO mapField(String originalField, String mappedField) {
 
 		extractor = new Extractor(controlledVocabulary, mongo);
 		if(!StringUtils.equals(mappedField,"")){
			
 		extractor.setMappedField(originalField,
 				EdmLabel.getEdmLabel(StringUtils.split(mappedField, "@")[0]));
 		} else {
 			extractor.setMappedField(originalField, null);
 		}
 		OriginalFieldDTO originalFieldDTO = new OriginalFieldDTO();
 		originalFieldDTO.setField(originalField);
 		EdmFieldDTO edmFieldDTO = new EdmFieldDTO();
 		edmFieldDTO.setField(mappedField);
 		MappingDTO mapping = new MappingDTO();
 		List<EdmFieldDTO> lst = new ArrayList<EdmFieldDTO>();
 		lst.add(edmFieldDTO);
 		mapping.setMapped(lst);
 		mapping.setOriginal(originalFieldDTO);
 		return mapping;
 	}
 
 	public boolean saveMapping() {
 		if (extractor != null) {
 			return extractor.saveMapping();
 		}
 		return false;
 	}
 
 	private List<MappingDTO> convertEdmMap(Map<String, List<EdmLabel>> readSchema) {
 		List<MappingDTO> returnMap = new ArrayList<MappingDTO>();
 
 		for (String key : readSchema.keySet()) {
 			MappingDTO map = new MappingDTO();
 			OriginalFieldDTO original = new OriginalFieldDTO();
 			original.setField(key);
 			map.setOriginal(original);
 			List<EdmFieldDTO> lst = new ArrayList<EdmFieldDTO>();
 			if(readSchema.get(key)!=null){
 			for(EdmLabel entry:readSchema.get(key)){
 				EdmFieldDTO edmField = new EdmFieldDTO();
 				edmField.setField(entry.toString());
 				lst.add(edmField);
 			}
 			}
 			map.setMapped(lst);
 			returnMap.add(map);
 		}
 		return returnMap;
 	}
 
 	@Override
 	public List<ControlledVocabularyDTO> retrieveVocabularies() {
 		extractor = new Extractor(controlledVocabulary, mongo);
 		List<ControlledVocabularyImpl> controlledVocabularies = new ArrayList<ControlledVocabularyImpl>();
 		try {
 			controlledVocabularies = extractor.getControlledVocabularies();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		List<ControlledVocabularyDTO> vocabularyDTOs = new ArrayList<ControlledVocabularyDTO>();
 		for (ControlledVocabularyImpl controlledVocabulary : controlledVocabularies) {
 			ControlledVocabularyDTO vocabularyDTO = new ControlledVocabularyDTO();
 			vocabularyDTO.setName(controlledVocabulary.getName());
 			vocabularyDTO.setUri(controlledVocabulary.getURI());
 			vocabularyDTO.setLocation(controlledVocabulary.getLocation());
 			vocabularyDTO.setSuffix(controlledVocabulary.getSuffix());
 			vocabularyDTO.setMapping(convertEdmMap(controlledVocabulary
 					.getElements()));
 			vocabularyDTO.setRules(controlledVocabulary.getRules());
 			vocabularyDTO.setReplaceUrl(controlledVocabulary.getReplaceUrl());
 			vocabularyDTOs.add(vocabularyDTO);
 		}
 		return vocabularyDTOs;
 	}
 
 	@Override
 	public List<EdmFieldDTO> retrieveEdmFields() {
 		List<EdmFieldDTO> edmFields = new ArrayList<EdmFieldDTO>();
 		for (RdfMethod edmField : RdfMethod.values()) {
 			EdmFieldDTO edmFieldDTO = new EdmFieldDTO();
 			edmFieldDTO.setField(edmField.getSolrField());
 			edmFields.add(edmFieldDTO);
 			if (edmField.getClazz().getSuperclass()
 					.isAssignableFrom(ResourceOrLiteralType.class)
 					|| edmField.getClazz().getSuperclass()
 							.isAssignableFrom(LiteralType.class)) {
 				EdmFieldDTO edmFieldDTOAttr = new EdmFieldDTO();
 				edmFieldDTOAttr.setField(edmField.getSolrField() + "@xml:lang");
 				edmFields.add(edmFieldDTOAttr);
 			}
 		}
 		return edmFields;
 	}
 
 	@Override
 	public boolean removeVocabulary(String vocabularyName) {
 		if (extractor != null) {
 			extractor.removeVocabulary(vocabularyName);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setVocabulary(String vocabularyName, String vocabularyUri) {
 		try{
 			controlledVocabulary = extractor.findVocabularyByName(vocabularyName, vocabularyUri);
 			return true;
 		} catch (VocabularyNotFoundException e){
 			return false;
 		}
 		
 	}
 	
 }
