 package org.iucn.sis.shared.conversions;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.hibernate.Hibernate;
 import org.iucn.sis.server.api.io.RelationshipIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.helpers.WorkingSetData;
 import org.iucn.sis.shared.helpers.WorkingSetParser;
 
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.vfs.VFSPath;
 import com.solertium.vfs.VFSPathToken;
 
 public class UserWorkingSetConverter extends GenericConverter<VFSInfo> {
 	
 	private UserIO userIO;
 	private RelationshipIO relationshipIO;
 	private TaxonIO taxonIO;
 	
 	public UserWorkingSetConverter() {
 		super();
 		setClearSessionAfterTransaction(true);
 	}
 	
 	/**
 	 * Assumes that:
 	 *  - Users have already been converted
 	 *  - Working Sets have already been converted
 	 *  - Working Set IDs have remained the same.
 	 */
 	protected void run() throws Exception {
 		userIO = new UserIO(session);
 		relationshipIO = new RelationshipIO(session);
 		taxonIO = new TaxonIO(session);
 		
 		if (isTestMode())
 			print("### RUNNING IN TEST MODE ###");
 		
 		final AtomicInteger count = new AtomicInteger(0);
 		final WorkingSetIO wsIO = new WorkingSetIO(session);
 		
 		for (VFSPathToken token : data.getOldVFS().list(new VFSPath("/users"))) {
 			final User user = userIO.getUserFromUsername(token.toString());
 			if (user == null)
 				printf("## No user exists for data directory %s ==", token);
 			else {
 				Hibernate.initialize(user.getSubscribedWorkingSets());
 				
 				final VFSPath workingSetPath = new VFSPath("/users/" + user.getUsername() + "/workingSet.xml");
 				final NativeDocument document = new JavaNativeDocument();
 				try {
 					document.parse(data.getOldVFS().getString(workingSetPath));
 				} catch (Exception e) {
 					continue;
 				}
 				
 				printf("== Finding working sets for %s (%s) ==", user.getUsername(), user.getId());
 				
 				final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
 				for (int i = 0; i < nodes.getLength(); i++) {
 					NativeNode node = nodes.item(i);
 					if ("public".equals(node.getNodeName())) {
 						NativeNodeList children = node.getChildNodes();
 						for (int k = 0; k < children.getLength(); k++) {
 							NativeNode child = children.item(k);
 							if ("workingSet".equals(child.getNodeName())) {
 								NativeElement el = (NativeElement)child;
 								String id = el.getAttribute("id");
 								String creator = el.getAttribute("creator");
 								
 								if (!user.getUsername().equals(creator)) {
 									boolean success = wsIO.subscribeToWorkingSet(Integer.valueOf(id), user);
 									if (!success)
 										printf("# Failed to subscribe to ws %s", id);
 									else {
 										printf("Subscribed to ws %s", id);
										if (count.incrementAndGet() % 50 == 0) {
 											printf("Converted %s working set subscriptions...", count.get());
 											commitAndStartTransaction();
										}
 									}
 								}
 							}
 						}
 					}
 					else if ("private".equals(node.getNodeName())) {
 						NativeNodeList children = node.getChildNodes();
 						for (int k = 0; k < children.getLength(); k++) {
 							NativeNode child = children.item(k);
 							if ("workingSet".equals(child.getNodeName())) {
 								WorkingSetData data = new WorkingSetParser().parseSingleWorkingSet((NativeElement)node);
 								if ("".equals(data.getId())) {
 									continue;
 								}
 								WorkingSet privateWS = 
 									WorkingSetConverter.convertWorkingSetData(data, session, relationshipIO, userIO, taxonIO, user);
 								if (privateWS != null) {
 									printf("Created private working set %s", privateWS.getName());
 									session.save(privateWS);
 								}		
 							}
 						}
 					}
 				}
 			}
			session.evict(user);
 		}
 		
		commitAndStartTransaction();
 	}
 	
 	private boolean isTestMode() {
 		return "true".equals(parameters.getFirstValue("test"));
 	}
 	
 	@Override
 	protected void commitAndStartTransaction() {
 		if (isTestMode())
 			session.clear();
 		else
 			super.commitAndStartTransaction();
 	}
 
 }
