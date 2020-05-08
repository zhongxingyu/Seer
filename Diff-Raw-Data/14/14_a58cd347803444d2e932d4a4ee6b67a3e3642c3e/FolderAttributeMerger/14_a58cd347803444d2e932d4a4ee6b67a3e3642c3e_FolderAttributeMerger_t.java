 package com.gentics.cr.lucene.indexer.transformer.other;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 /**
  * 
  * @author Friedreich Bernhard
  *
  */
 public class FolderAttributeMerger extends ContentTransformer {
 
 	private static final String FOLDERATTRIBUTE = "folder_id";
 	
 	private static final String CONTENTATTRIBUTES_KEY = "contentattributes";
 	private static final String FOLDERATTRIBUTES_KEY = "folderattributes";
 	private static final String TARGETATTRIBUTE_KEY = "targetattribute";
 	
 	private ArrayList<String> folderAttributes = new ArrayList<String>();
 	private ArrayList<String> contentAttributes = new ArrayList<String>();
 	
 	private String targetAttribute = new String();
 	
 	private static Logger logger = Logger.getLogger(FolderAttributeMerger.class);
 	
 	/**
 	 * Creates instance of FolderAttributeMerger.
 	 * @param config Configuration for Transformer
 	 */
 	public FolderAttributeMerger(final GenericConfiguration config) {
 		super(config);
 		Object folderAttr = config.get(FOLDERATTRIBUTES_KEY);
 		if (folderAttr != null) {
 			Collections.addAll(folderAttributes, folderAttr.toString().split(";"));
 		}
 
 		Object contentAttr = config.get(CONTENTATTRIBUTES_KEY);
 		if (contentAttr != null) {
 			Collections.addAll(contentAttributes, contentAttr.toString().split(";"));
 		}
 
 		Object target = config.get(TARGETATTRIBUTE_KEY);
 		if (target != null) {
 			targetAttribute = target.toString();
 		}
 	}

 	@Override
 	public void processBean(CRResolvableBean contentBean) {
 		String targetAttributeValues = "";
 		
 		Resolvable contentResolvable = contentBean.getResolvable();
 		CRResolvableBean folderBean = (CRResolvableBean) contentResolvable.get(FOLDERATTRIBUTE);
 		Resolvable folderResolvable = folderBean.getResolvable();
 		if (folderResolvable != null) {
 			for (String attribute : folderAttributes) {
 				Object attributeObject = folderResolvable.getProperty(attribute);
 				if (attributeObject != null) {
 					String folderAttributesString = attributeObject.toString();
 					targetAttributeValues += folderAttributesString;
 				}
 			}
 		}

 		for (String attribute : contentAttributes) {
 			Object attributeObject = contentBean.get(attribute);
 			if (attributeObject != null) {
 				String contentAttributesString = attributeObject.toString();
 				targetAttributeValues += contentAttributesString;
 			}
 		}

 		if (!targetAttributeValues.equals("")) {
 			String contentid = contentBean.getContentid();
 			String folderid = "";
 			if (folderBean != null) {
 				folderid = folderBean.getContentid();
 			}
 			logger.debug("contentid: " + contentid + " " 
 					+ " - folder: " + folderid + " - targetattribute: " + targetAttributeValues);
 			contentBean.set(targetAttribute, targetAttributeValues);
 		}
 	}
 
 	@Override
 	public void destroy() {

 	}
 
 }
