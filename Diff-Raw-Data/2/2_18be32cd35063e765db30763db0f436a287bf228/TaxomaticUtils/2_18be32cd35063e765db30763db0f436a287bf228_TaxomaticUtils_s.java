 package org.iucn.sis.client.panels.taxomatic;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.solertium.lwxml.gwt.utils.ClientDocumentUtils;
 import com.solertium.lwxml.shared.GWTResponseException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.api.XMLWritingUtils;
 
 public class TaxomaticUtils {
 
 	public static int CLEAR_DESCRIPTION_DELAY = 10000;
 
 	public static TaxomaticUtils impl = new TaxomaticUtils();
 
 	private GenericCallback<String> getDefaultCallback(final NativeDocument ndoc, 
 			final GenericCallback<String> wayback, final String idToReturn) {
 		return new GenericCallback<String>() {
 
 			public void onFailure(Throwable arg0) {
 				String status = ClientDocumentUtils.parseStatus(ndoc);
 				if (!"".equals(status))
 					WindowUtils.errorAlert(status);
 				else if (ndoc.getStatusText().equals("423"))
 					WindowUtils.errorAlert("Taxomatic In Use", "Sorry, but another " +
 							"taxomatic operation is currently running. Please try " +
 							"again later.");
 				else
 					WindowUtils.errorAlert("An unexpected error caused this operation to fail.  Please try again later.");
 				
 				wayback.onFailure(arg0);
 			}
 
 			public void onSuccess(String arg0) {
 				if (idToReturn != null)
 					afterTaxomaticOperation(Integer.valueOf(idToReturn), new GenericCallback<String>() {
 						public void onFailure(Throwable arg0) {
 							wayback.onSuccess(null);
 						}
 						public void onSuccess(String arg0) {
 							wayback.onSuccess(arg0);
 						}
 					});
 				else
 					wayback.onSuccess(null);
 			}
 
 		};
 	}
 	
 	/**
 	 * Takes in a newNode and attaches it to the parent, if the parent is an
 	 * instanceof either a Taxon  or the TaxonomyTree.
 	 * 
 	 * @param newTaxon
 	 * @param parent
 	 */
 	public void createNewTaxon(final Taxon newTaxon, Taxon parent, final GenericCallback<Taxon> wayback) {
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		doc.putAsText(UriBase.getInstance().getSISBase() + "/taxomatic/new", newTaxon.toXML(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				handleFailure(doc, caught, wayback);
				
				wayback.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				try {
 					newTaxon.setId(Integer.parseInt(doc.getText()));
 					TaxonomyCache.impl.putTaxon(newTaxon);
 					TaxonomyCache.impl.invalidatePath(newTaxon.getParentId());
 					
 					StateManager.impl.setTaxon(newTaxon);
 				} catch (Exception e) {
 					//TaxonomyCache.impl.invalidatePath(newTaxon.getParentId());
 					//TaxonomyCache.impl.setCurrentTaxon(TaxonomyCache.impl.getTaxon(newTaxon.getParentId()));
 					StateManager.impl.setTaxon(TaxonomyCache.impl.getTaxon(newTaxon.getParentId()));
 				}
 
 				wayback.onSuccess(newTaxon);
 			}
 		});
 		
 		
 
 	}
 
 	private TaxomaticUtils() {
 
 	}
 
 	@SuppressWarnings("deprecation")
 	protected void afterTaxomaticOperation(final Integer currentTaxaID, final GenericCallback<String> callback) {
 		TaxonomyCache.impl.clear();
 		//AssessmentCache.impl.resetCurrentAssessment();
 		AssessmentCache.impl.clear();		
 		AssessmentCache.impl.loadRecentAssessments(new GenericCallback<Object>() {
 			public void onFailure(Throwable caught) {
 			}
 
 			public void onSuccess(Object arg) {
 				// Silently be happy.
 
 				TaxonomyCache.impl.fetchTaxon(currentTaxaID, true, new GenericCallback<Taxon >() {
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(Taxon  result) {
 						ClientUIContainer.headerContainer.centerPanel.refreshTaxonView();
 						StateManager.impl.setTaxon(result, true);
 						callback.onSuccess(result.getId() + "");
 					}
 				});
 			}
 		});
 	}
 
 	/**
 	 * Sends a request to the taxomatic restlet to handle lateral moves, where
 	 * you are moving all of the childrenIDs to be under the parentID
 	 * 
 	 * @param parentID
 	 * @param childrenIDs
 	 * @param wayback
 	 */
 	public void lateralMove(final String parentID, List<String> childrenIDs, final GenericCallback<String> wayback) {
 		if (parentID != null && childrenIDs != null && !parentID.equalsIgnoreCase("") && childrenIDs.size() > 0) {
 			StringBuilder xml = new StringBuilder("<xml>\r\n<parent id=\"" + parentID + "\"/>\r\n");
 			for (String childID : childrenIDs) {
 				xml.append("<child id=\"" + childID + "\"/>\r\n");
 			}
 			xml.append("</xml>");
 
 			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 			ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/move", xml.toString(), getDefaultCallback(ndoc, wayback, parentID));
 		} else
 			wayback.onFailure(new TaxonomyException(401, "Invalid parameters specified for lateral move."));
 	}
 
 	public void moveAssessments(final String idMoveAssessmentsOutOF, final String idMoveAssessmentsInto,
 			final List<String> assessmentIDs, final GenericCallback<String> wayback) {
 		StringBuilder xml = new StringBuilder("<moveAssessments>\r\n");
 		xml.append("<oldNode>" + idMoveAssessmentsOutOF + "</oldNode>\r\n");
 		xml.append("<nodeToMoveInto>" + idMoveAssessmentsInto + "</nodeToMoveInto>\r\n");
 		for (String ids : assessmentIDs) {
 			xml.append("<assessmentID>" + ids + "</assessmentID>\r\n");
 		}
 		xml.append("</moveAssessments>");
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		ndoc.put(UriBase.getInstance().getSISBase() +"/taxomatic/moveAssessments", xml.toString(), getDefaultCallback(ndoc, wayback, idMoveAssessmentsOutOF));
 	}
 
 	public void performDemotion(final Taxon  demoteMe, String newParent, final GenericCallback<String> wayback) {
 		String xml = "<xml>\r\n<demoted id=\"" + demoteMe.getId() + "\" >" + newParent + "</demoted>\r\n</xml>";
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/demote", xml, getDefaultCallback(ndoc, wayback, demoteMe.getId()+""));
 	}
 
 	/**
 	 * Merges nodes contained in the ArrayList nodes into newNode, placing it
 	 * under newParent.
 	 * 
 	 * @param nodes
 	 * @param newNode
 	 * @param newParent
 	 */
 	public void performMerge(final ArrayList<Taxon> nodes, final Taxon  newNode, final GenericCallback<String> wayback) {
 		StringBuilder mergedNodes = new StringBuilder();
 		for (Iterator<Taxon> iter = nodes.iterator(); iter.hasNext();) {
 			mergedNodes.append(iter.next().getId());
 			mergedNodes.append(iter.hasNext() ? "," : "");
 		}
 
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		String xml = "<xml>\r\n" + "<merged>" + mergedNodes + "</merged>\r\n";
 		xml += "<main>" + newNode.getId() + "</main>\r\n</xml>";
 		ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/merge", xml, getDefaultCallback(ndoc, wayback, newNode.getId()+""));
 	}
 
 	/**
 	 * Posts to the taxonomy restlet where the taxomatic will merge the given
 	 * infranks with the given species. The subpopulations of the infraranks
 	 * will become subpopulations of the species.
 	 * 
 	 * @param infrarankIDS
 	 * @param speciesID
 	 * @param callback
 	 */
 	public void performMergeUpInfrarank(final List<String> infrarankIDS, final long speciesID, final String name,
 			final GenericCallback<String> wayback) {
 
 		if (infrarankIDS.size() > 0) {
 			StringBuilder infraIds = new StringBuilder();
 			for (Iterator<String> iter = infrarankIDS.iterator(); iter.hasNext(); )
 				infraIds.append(iter.next() + (iter.hasNext() ? "," : ""));
 
 			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 			String xml = "<xml>\r\n" + "<infrarank>" + infraIds.toString()
 					+ "</infrarank>\r\n";
 			xml += "<species>" + speciesID + "</species>\r\n</xml>";
 			ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/mergeupinfrarank", xml, getDefaultCallback(ndoc, wayback, speciesID+""));
 		} else {
 			wayback.onFailure(new Throwable("You must merge at least one infrarank with the species"));
 		}
 
 	}
 
 	/**
 	 * Posts to the taxonomy restlet with the taxomatic will move the
 	 * assessments from the oldNode into the newNode
 	 * 
 	 * @param assessmentIDs
 	 * @param oldNodeID
 	 * @param newNodeID
 	 * @param wayback
 	 */
 	public void performMoveAssessments(final List<String> assessmentIDs, final String oldNodeID,
 			final String newNodeID, final GenericCallback<String> wayback) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("<moveAssessments>\r\n");
 		builder.append("<oldNode>" + oldNodeID + "</oldNode>\r\n");
 		builder.append("<nodeToMoveInto>" + newNodeID + "</nodeToMoveInto>r\n");
 		for (String item : assessmentIDs) {
 			builder.append("<assessmentID>" + item + "</assessmentID>r\n");
 		}
 		builder.append("</moveAssessments>");
 
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/moveAssessments", builder.toString(), getDefaultCallback(ndoc, wayback, oldNodeID));
 	}
 
 	/**
 	 * Promotes a node to reside under the newParent node. Returns a String of
 	 * HTML telling what actions it took to perform the promotion.
 	 * 
 	 * @param promoteMe
 	 * @param newParent
 	 * @return
 	 */
 	public void performPromotion(final Taxon  promoteMe, final GenericCallback<String> wayback) {
 		String xml = "<xml>\r\n<promoted id=\"" + promoteMe.getId() + "\" />\r\n</xml>";
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/promote", xml, getDefaultCallback(ndoc, wayback, promoteMe.getId()+""));
 	}
 
 	/**
 	 * This will assign to each new node a deprecated synonym that is
 	 * oldNode.getId(), and will give each node the proper new parent.
 	 * 
 	 * @param oldNode
 	 * @param newNodes
 	 * @param numSplits
 	 */
 	public void performSplit(final Taxon  oldNode, HashMap<String, ArrayList<String>> parentToChild,
 			final GenericCallback<String> wayback) {
 		String xml = "<root>";
 		xml += XMLWritingUtils.writeTag("current", "" + oldNode.getId());
 
 		for (Map.Entry<String, ArrayList<String>> entry : parentToChild.entrySet()) {
 			xml += "<parent id=\"" + entry.getKey() + "\">";
 
 			ArrayList<String> values = entry.getValue();
 			for (String taxonID : values)
 				xml += XMLWritingUtils.writeTag("child", taxonID);
 			
 			xml += "</parent>";
 		}
 
 		xml += "</root>";
 
 		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/split", xml, getDefaultCallback(ndoc, wayback, oldNode.getId()+""));
 	}
 
 
 	public void saveTaxon(Taxon  node, final GenericCallback<Object> callback) {
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		doc.post(UriBase.getInstance().getSISBase() + "/taxomatic/update/" + node.getId(), node.toXML(),
 				new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				handleFailure(doc, caught, callback);
 			}
 			public void onSuccess(String result) {
 				TaxonomyCache.impl.clear();
 				callback.onSuccess(result);
 			}
 		});
 	}
 	
 	public void deleteTaxon(final Taxon taxon, final GenericCallback<String> wayback) {
 		final String deleteUrl = "/taxomatic/" + taxon.getId();
 		
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		doc.delete(UriBase.getInstance().getSISBase() + deleteUrl, getDefaultCallback(doc, wayback, null));
 	}
 	
 	/*
 	 * I don't care what the callback type is, I just need to call onfailure.
 	 */
 	private void handleFailure(final NativeDocument document, Throwable caught, final GenericCallback<?> callback) {
 		String status = ClientDocumentUtils.parseStatus(document);
 		if ("".equals(status))
 			callback.onFailure(caught);
 		else
 			callback.onFailure(new TaxonomyException(((GWTResponseException)caught).getCode(), status));
 	}
 	
 	public static class TaxonomyException extends GWTResponseException {
 		
 		private static final long serialVersionUID = 1L;
 		
 		public TaxonomyException(int code, String message) {
 			super(code, message);
 		}
 		
 	}
 }
