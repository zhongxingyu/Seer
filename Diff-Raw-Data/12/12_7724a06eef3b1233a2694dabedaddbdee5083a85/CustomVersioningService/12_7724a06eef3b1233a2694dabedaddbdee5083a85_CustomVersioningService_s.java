 package org.nuxeo.sample.versioning;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import org.nuxeo.ecm.core.api.DocumentException;
 import org.nuxeo.ecm.core.model.Document;
 import org.nuxeo.ecm.core.versioning.StandardVersioningService;
 
 public class CustomVersioningService extends StandardVersioningService {
 
 	@Override
 	public void doPostCreate(Document doc, Map<String, Serializable> options) {
 		if (doc.isVersion() || doc.isProxy()) {
 			return;
 		}
 		try {
 			Boolean comesFromImport = (Boolean) options.get("comesFromImport");
			if (comesFromImport != null && !"".equals(comesFromImport)) {
 				setVersion(doc, 12, 0);
 			} else {
 				setInitialVersion(doc);
 			}
 		} catch (DocumentException e) {
			// ignore
 		}
 	}
 }
