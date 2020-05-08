 package org.iucn.sis.client.api.caches;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.xml.client.Node;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class TaxonomyCache {
 
 	public static final TaxonomyCache impl = new TaxonomyCache();
 
 	private Taxon currentNode = null;
 
 	/**
 	 * ArrayList<Taxon>
 	 */
 	private ArrayList<Taxon> recentlyAccessed;
 
 	/**
 	 * HashMap<String, Taxon>();
 	 */
 	private HashMap<Integer, Taxon> cache;
 
 	/**
 	 * HashMap<String, NativeDocument>();
 	 */
 	private HashMap<String, NativeDocument> pathCache;
 
 	/**
 	 * HashMap<String, ArrayList> - NodeID request, and list of callbacks
 	 * waiting for its return
 	 */
 	private HashMap<Integer, List<GenericCallback<Taxon>>> requested;
 
 	private TaxonomyCache() {
 		cache = new HashMap<Integer, Taxon>();
 		pathCache = new HashMap<String, NativeDocument>();
 		recentlyAccessed = new ArrayList<Taxon>();
 		requested = new HashMap<Integer, List<GenericCallback<Taxon>>>();
 	}
 
 	public void clear() {
 		cache.clear();
 		pathCache.clear();
 	}
 
 	public boolean containsNode(Taxon node) {
 		return contains(node.getId());
 	}
 
 	public boolean contains(int id) {
 		return cache.containsKey(Integer.valueOf(id));
 	}
 
 	public boolean contains(Integer id) {
 		return cache.containsKey(id);
 	}
 
 	public Object remove(int id) {
 		return cache.remove(Integer.valueOf(id));
 	}
 
 	public Object remove(Integer id) {
 		return cache.remove(id);
 	}
 
 	private void doListFetch(final String payload, final GenericCallback<String> wayBack) {
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		doc.post(UriBase.getInstance().getSISBase() + "/browse/nodes/list", payload, new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				try{
 				WindowUtils.loadingBox.updateText("Fetch successful. Processing...");
 				List<Taxon> taxa = processDocumentOfTaxa(doc);
 				WindowUtils.hideLoadingAlert();
 				wayBack.onSuccess(Arrays.toString(taxa.toArray()));
 				} catch (Throwable e) {
 					Debug.println(e);
 				}
 			}
 		});
 	}
 
 	public void doLogout() {
 		cache.clear();
 		pathCache.clear();
 		recentlyAccessed.clear();
 
 		currentNode = null;
 	}
 
 	/**
 	 * Evicts the nodes from the cache, and then calls evictPaths();
 	 * 
 	 * @param csvIDs
 	 */
 	public void evict(String csvIDs) {
 		String[] ids = csvIDs.split(",");
 		for (int i = 0; i < ids.length; i++) {
 			Taxon node = (Taxon) remove(Integer.parseInt(ids[i]));
 			if (node != null) {
 				recentlyAccessed.remove(node);
 			}
 		}
 		evictPaths();
 	}
 
 	/**
 	 * Evicts ALL taxonomic hierarchy paths from local cache.
 	 * 
 	 * @param csvIDs
 	 */
 	public void evictPaths() {
 		pathCache.clear();
 	}
 
 	/**
 	 * Returns taxon nodes of species or lower levels. Takes an xml document of
 	 * the form: <br>
 	 * (ids)<br>
 	 * (id)1(/id)<br>
 	 * (id)2(/id)<br>
 	 * (/ids)<br>
 	 * 
 	 * 
 	 * @param id
 	 * @param wayback
 	 *            - returns the ids of the lowest level taxa
 	 */
 	public void fetchLowestLevelTaxa(final String ids, final GenericCallback<List<Taxon>> wayback) {
 		if (ids == null) {
 			wayback.onSuccess(new ArrayList<Taxon>());
 			return;
 		}
 		
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() + "/browse/lowestTaxa", ids, new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayback.onFailure(caught);
 			}
 			public void onSuccess(String arg0) {
 				if (!arg0.equals("204")) {
 					List<Taxon> ids = new ArrayList<Taxon>();
 					NativeElement docElement = ndoc.getDocumentElement();
 					if (docElement.getNodeName().equalsIgnoreCase("nodes")) {
 						NativeNodeList nodes = docElement.getChildNodes();
 						for (int i = 0; i < nodes.getLength(); i++) {
 							if (nodes.item(i).getNodeType() == Node.TEXT_NODE)
 								continue;
 						
 							Taxon newNode = Taxon.fromXML(nodes.elementAt(i));
 							putTaxon(newNode);
 							ids.add(newNode);
 						}
 					} else {
 						Taxon newNode = Taxon.fromXML(docElement);
 						putTaxon(newNode);
 						ids.add(newNode);
 					}
 					wayback.onSuccess(ids);
 				} else {
 					//FIXME: Should this be a failure?
 					wayback.onSuccess(new ArrayList<Taxon>());
 				}
 			}
 		});
 	}
 	
 
 	public void fetchList(final List<Integer> ids, final GenericCallback<String> wayBack) {
 		if (ids == null || ids.isEmpty()) {
 			wayBack.onSuccess("OK");
 			return;
 		}
 		
 		final StringBuilder idsToFetch = new StringBuilder("<ids>");
 		int toFetch = 0;
 		boolean needToFetch = false;
 
 		if (ids.size() == 1) {
 			if (!contains(ids.get(0)))
 				fetchTaxon(ids.get(0), false, new GenericCallback<Taxon>() {
 					public void onFailure(Throwable caught) {
 						wayBack.onFailure(caught);
 					}
 
 					public void onSuccess(Taxon result) {
 						wayBack.onSuccess(result.getId() + "");
 					}
 				});
 			else
 				wayBack.onSuccess("OK");
 		} else {
 			for (Integer curID : ids) {
 				if (!contains(curID)) {
 					needToFetch = true;
 
 					idsToFetch.append("<id>");
 					idsToFetch.append(curID);
 					idsToFetch.append("</id>");
 					toFetch++;
 				}
 			}
 			idsToFetch.append("</ids>");
 
 			if (!needToFetch) {
 				wayBack.onSuccess("OK");
 				return;
 			}
 			
 			WindowUtils.showLoadingAlert("Fetching " + toFetch + " taxa from the server. Please wait.");
 			doListFetch(idsToFetch.toString(), wayBack);
 		}
 	}
 
 	public void fetchTaxon(final Integer id, final boolean asCurrent, GenericCallback<Taxon> wayback) {
 		fetchTaxon(id, asCurrent, true, wayback);
 	}
 	
 	public void fetchTaxon(final Integer id, final boolean asCurrent, final boolean saveIfNecessary, final GenericCallback<Taxon> wayback) {
 		if (getTaxon(id) != null) {
 			if (asCurrent)
 				setCurrentTaxon(getTaxon(id), saveIfNecessary);
 
 			wayback.onSuccess(getTaxon(id));
 		} else {
 			if (requested.containsKey(id))
 				requested.get(id).add(wayback);
 			else {
 				requested.put(id, new ArrayList<GenericCallback<Taxon>>());
 				requested.get(id).add(wayback);
 
 				final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 				ndoc.load(UriBase.getInstance().getSISBase() + "/browse/nodes/" + id, new GenericCallback<String>() {
 					public void onFailure(Throwable caught) {
 						invokeFailureCallbacks(id, caught);
 					}
 
 					public void onSuccess(String arg0) {
 						Taxon defaultNode;
 
 						// ___ FIXME ___ LOOKS LIKE I LOST TO A RACE CONDITION ... JUST
 						// RETURNING THE NODE
 						if (contains(id)) {
 							defaultNode = getTaxon(id);
 						} else {
 							List<Taxon> list = processDocumentOfTaxa(ndoc);
 							defaultNode = list.get(0);
 							putTaxon(defaultNode);
 						}
 
 						if (asCurrent) {
 							setCurrentTaxon(defaultNode, saveIfNecessary);
 						}
 
 						invokeCallbacks(id, defaultNode);
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param kingdom
 	 *            - the kingdom of the node
 	 * @param otherInfo
 	 *            - the other information that you know, either phylum, class,
 	 *            order ...
 	 * @param asCurrent
 	 * @param wayback
 	 */
 	public void fetchTaxonWithKingdom(String kingdom, String otherInfo, final boolean asCurrent,
 			final GenericCallback<Taxon> wayback) {
 		if (!kingdom.trim().equals("") && !otherInfo.trim().equals("")) {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.get(URL.encode(UriBase.getInstance().getSISBase() + "/browse/taxonName/" + kingdom + "/" + otherInfo),
 					new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayback.onFailure(caught);
 				}
 				public void onSuccess(String arg0) {
 					Taxon defaultNode = Taxon.fromXML(ndoc.getDocumentElement().getElementByTagName(Taxon.ROOT_TAG));
 					if (!contains(defaultNode.getId())) {
 						putTaxon(defaultNode);
 						if (asCurrent)
 							setCurrentTaxon(defaultNode);
 					} else if (asCurrent) {
 						setCurrentTaxon(getTaxon(defaultNode.getId()));
 					}
 					wayback.onSuccess(getTaxon(defaultNode.getId()));
 				}
 			});
 		} else {
 			wayback.onFailure(new Throwable("Nothing to fetch"));
 		}
 	}
 
 	public void fetchPath(final String path, final GenericCallback<NativeDocument> wayback) {
 		if (pathCache.containsKey(path)) {
 			wayback.onSuccess(pathCache.get(path));
 		} else {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.load(UriBase.getInstance().getSISBase() + "/browse/taxonomy/" + path, new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayback.onFailure(caught);
 				}
 
 				public void onSuccess(String result) {
 					pathCache.put(path, ndoc);
 					wayback.onSuccess(ndoc);
 				}
 			});
 		}
 	}
 
 	public void fetchPathWithID(final String id, final GenericCallback<NativeDocument> wayback) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/browse/hierarchy/" + id, new GenericCallback<String>() {
 
 			public void onFailure(Throwable caught) {
 				wayback.onFailure(caught);
 			}
 
 			public void onSuccess(String result) {
 				try {
 					String path = ndoc.getDocumentElement().getElementByTagName("footprint").getText();
 					pathCache.put(path, ndoc);
 					wayback.onSuccess(ndoc);
 				} catch (Exception e) {
 					wayback.onFailure(new Throwable());
 				}
 
 			}
 
 		});
 	}
 
 	/**
 	 * Gets all immediate children of the node referred to by nodeID, processes
 	 * them and caches them locally, then returns as an argument in the
 	 * GenericCallback<String>an ArrayList(Taxon) that contains the child nodes
 	 * themselves.
 	 * 
 	 * @param nodeID
 	 * @param wayback
 	 */
 	public void getTaxonChildren(final String nodeID, final GenericCallback<List<Taxon>> wayback) {
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		doc.get(UriBase.getInstance().getSISBase() + "/browse/children/" + nodeID, new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayback.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				if (doc.getPeer() == null) {
 					wayback.onSuccess(new ArrayList<Taxon>());
 				} else
 					wayback.onSuccess(processDocumentOfTaxa(doc));
 			}
 		});
 	}
 
 	public Taxon getCurrentTaxon() {
 		return currentNode;
 	}
 
 	public Taxon getTaxon(String id) {
 		return cache.get(Integer.valueOf(id));
 	}
 
 	public Taxon getTaxon(int id) {
 		return cache.get(Integer.valueOf(id));
 	}
 
 	public Taxon getTaxon(Integer id) {
 		return cache.get(id);
 	}
 
 	public ArrayList<Taxon> getRecentlyAccessed() {
 		return recentlyAccessed;
 	}
 
 	public Object invalidatePath(Integer pathID) {
 		return pathCache.remove(pathID);
 	}
 
 	private void invokeCallbacks(Integer id, Taxon defaultNode) {
 		for (GenericCallback<Taxon> curCallback : requested.get(id))
 			curCallback.onSuccess(defaultNode);
 
 		requested.remove(id);
 	}
 
 	private void invokeFailureCallbacks(Integer id, Throwable caught) {
 		for (GenericCallback<Taxon> curCallback : requested.get(id))
 			curCallback.onFailure(caught);
 
 		requested.remove(id);
 	}
 
 	private List<Taxon> processDocumentOfTaxa(final NativeDocument doc) {
 		List<Taxon> taxaList = new ArrayList<Taxon>();
 		
 		if (doc.getDocumentElement().getNodeName().trim().equalsIgnoreCase("nodes")) {
 			NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName(Taxon.ROOT_TAG);
 			int numNodes = nodes.getLength();
 
 			for (int i = 0; i < numNodes; i++) {
 				Taxon newNode = Taxon.fromXML(nodes.elementAt(i));
 				putTaxon(newNode);
 				taxaList.add(newNode);
 			}
 			
 		} else if (doc.getDocumentElement().getNodeName().equalsIgnoreCase("parsererror")) {
 			Window.alert("Error -- Someone probably modified an xml file by hand, and SIS "
 					+ "is unable to parse the file.");
 		} else {
 			try {
 				Taxon newNode = Taxon.fromXML(doc.getDocumentElement());
 				putTaxon(newNode);
 				taxaList.add(newNode);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		return taxaList;
 	}
 
 	public void putTaxon(Taxon node) {
 		cache.put(Integer.valueOf(node.getId()), node);
 	}
 
 	public void resetCurrentTaxon() {
 		setCurrentTaxon(null, false);
 	}
 	
 	public void saveReferences(Taxon taxon, final GenericCallback<String> callback) {
 		final StringBuilder out = new StringBuilder();
 		out.append("<root>");
 		for (Reference ref : taxon.getReference())
 			out.append("<reference id=\"" + ref.getId() + "\" />");
 		out.append("</root>");
 		
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		doc.post(UriBase.getInstance().getSISBase() + "/browse/nodes/" + taxon.getId() + "/references", 
 				out.toString(), callback);
 	}
 
 	public void saveTaxon(Taxon taxon, final GenericCallback<String> callback) {
 		/*
 		 * TODO: do we need to check permissions?
 		 */
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		doc.put(UriBase.getInstance().getSISBase() + "/browse/nodes/" + taxon.getId(), taxon.toXML(),
 				new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 			public void onSuccess(String result) {
 				callback.onSuccess(result);
 			}
 		});
 	}
 
 	/**
 	 * Saves the taxon and makes it the current taxon
 	 * 
 	 * @param node
 	 * @param callback
 	 */
 	public void saveTaxonAndMakeCurrent(final Taxon node, final GenericCallback<String> callback) {
 		saveTaxon(node, new GenericCallback<String>() {
 
 			public void onSuccess(String result) {
 				TaxonomyCache.impl.setCurrentTaxon(node);
 				callback.onSuccess(result);
 
 			}
 
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 
 	}
 
 	public void setCurrentTaxon(Taxon newCurrent) {
 		setCurrentTaxon(newCurrent, true);
 	}
 	
 	public void setCurrentTaxon(Taxon newCurrent, boolean saveIfNecessary) {
 		setCurrentTaxon(newCurrent, saveIfNecessary, null);
 	}
 	
 	public void setCurrentTaxon(final Taxon newCurrent, boolean saveIfNecessary, final SimpleListener afterChange) {
 		SimpleListener callback = new SimpleListener() {
 			public void handleEvent() {
 				currentNode = newCurrent;
 				if (currentNode != null) {
 					recentlyAccessed.remove(currentNode);
 					recentlyAccessed.add(0, currentNode);
 					SISClientBase.getInstance().onTaxonChanged();
 					if (afterChange != null)
 						afterChange.handleEvent();
 				}
 				else {
 					AssessmentCache.impl.resetCurrentAssessment();
 					SISClientBase.getInstance().onTaxonChanged();
 				}
 			}
 		};
 		if (saveIfNecessary)
 			AssessmentClientSaveUtils.saveIfNecessary(callback);
 		else
 			callback.handleEvent();
 	}
 	
 	public void getTaggedTaxa(final String tag, final GenericCallback<List<Taxon>> callback) {
 		final String url = UriBase.getInstance().getSISBase() + "/tagging/taxa/" + tag;
 		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 		document.get(url, new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				final List<Taxon> results = new ArrayList<Taxon>();
 				final NativeNodeList nodes = document.
 					getDocumentElement().getElementsByTagName("taxon");
 				for (int i = 0; i < nodes.getLength(); i++) {
 					final Taxon taxon = Taxon.fromXML(nodes.elementAt(i));
 					if (!containsNode(taxon))
 						putTaxon(taxon);
 					
					results.add(taxon);
 				}
 				callback.onSuccess(results);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	public void tagTaxa(final String tag, final List<Taxon> taxa, final GenericCallback<Object> callback) {
 		doTaxaMarking(tag, taxa, true, callback);
 	}
 	
 	public void untagTaxa(final String tag, final List<Taxon> taxa, final GenericCallback<Object> callback) {
 		doTaxaMarking(tag, taxa, false, callback);
 	}
 	
 	private boolean isTagged(final String tag, final Taxon taxon) {
 		if ("invasive".equals(tag))
 			return taxon.getInvasive();
 		else if ("feral".equals(tag))
 			return taxon.getFeral();
 		else
 			return false;
 	}
 	
 	private void doTaxaMarking(final String tag, final List<Taxon> taxa, final boolean isMarked, 
 			final GenericCallback<Object> callback) {
 		boolean found = false;
 		
 		final StringBuilder builder = new StringBuilder();
 		builder.append("<root>");
 		for (Taxon taxon : taxa) {
 			if (taxon.getId() != 0 && isTagged(tag, taxon) != isMarked) {
 				found = true;
 				builder.append("<taxon id=\"" + taxon.getId() + "\" />");
 			}
 		}
 		builder.append("</root>");
 		
 		if (!found)
 			callback.onSuccess(null);
 		else {
 			final String url = UriBase.getInstance().getSISBase() + 
 				"/tagging/taxa/" + tag + (isMarked ? "/marked" : "/unmarked");
 			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 			document.post(url, builder.toString(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					for (Taxon taxon : taxa) {
 						if ("feral".equals(tag))
 							taxon.setFeral(isMarked);
 						else if ("invasive".equals(tag))
 							taxon.setInvasive(isMarked);
 					}
 					
 					callback.onSuccess(null);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 	}
 	
 	public void deleteSynonymn(final Taxon taxon, final Synonym synonym, final GenericCallback<String> callback) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/synonym/" + synonym.getId(), new GenericCallback<String>() {
 		
 			@Override
 			public void onSuccess(String result) {
 				Synonym toRemove = null;
 				for (Synonym s : taxon.getSynonyms())
 					if (s.getId() == synonym.getId()) {
 						toRemove = s;
 						break;
 					}
 				taxon.getSynonyms().remove(toRemove);
 				callback.onSuccess(result);
 			}
 		
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	public void addOrEditSynonymn(final Taxon taxon, final Synonym synonym, final GenericCallback<String> callback) {
 		String uri = UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/synonym";
 		if (synonym.getId() != 0) {
 			uri += "/" + synonym.getId();
 		}
 		
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		if (synonym.getId() != 0) {
 			ndoc.postAsText(uri, synonym.toXML(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		else {
 			ndoc.putAsText(uri, synonym.toXML(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					String newId = ndoc.getText();
 					synonym.setId(Integer.parseInt(newId));
 					taxon.getSynonyms().add(synonym);
 					
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);	
 				}
 			});
 		}
 		
 	}
 	
 	public void deleteCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId(), new GenericCallback<String>() {
 		
 			@Override
 			public void onSuccess(String result) {
 				taxon.getCommonNames().remove(commonName);
 				callback.onSuccess(result);
 			}
 		
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	public void deleteNoteOnCommonNames(final Taxon taxon, final CommonName commonName, final Notes note, final GenericCallback<String> callback) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 				commonName.getNotes().remove(note);
 				callback.onSuccess(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		
 		});
 	}
 	
 	public void addNoteToCommonName(final Taxon taxon, final CommonName commonName, final Notes note, final GenericCallback<String> callback) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.putAsText(UriBase.getInstance().getNotesBase() + "/notes/commonName/" + commonName.getId(), note.toXML(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				note.setCommonName(commonName);
 				result = ndoc.getText();
 				note.setId(Integer.parseInt(result));
 				commonName.getNotes().add(note);
 				callback.onSuccess(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		
 		});
 	}
 	
 	public void addReferencesToCommonName(final Taxon taxon, final CommonName commonName, final Collection<Reference> refs, final GenericCallback<Object> callback) {
 		StringBuilder xml = new StringBuilder("<references>");
 		for (Reference ref : refs) {
 			xml.append("<action id=\"" + ref.getId() + "\">action</action>");
 		}		
 		xml.append("</references>");
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId() + "/reference", xml.toString(), new GenericCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 				commonName.getReference().addAll(refs);
 				callback.onSuccess(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		
 		});
 	}
 	
 	public void removeReferencesToCommonName(final Taxon taxon, final CommonName commonName, final Collection<Reference> refs, final GenericCallback<Object> callback) {
 		StringBuilder xml = new StringBuilder("<references>");
 		for (Reference ref : refs) {
 			xml.append("<action id=\"" + ref.getId() + "\">remove</action>");
 		}		
 		xml.append("</references>");
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId() + "/reference", xml.toString(), new GenericCallback<String>() {
 
 			@Override
 			public void onSuccess(String result) {
 				commonName.getReference().removeAll(refs);
 				callback.onSuccess(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		
 		});
 		
 		
 	}
 	
 	public void editCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.postAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname/" + commonName.getId(), commonName.toXML(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				CommonName toRemove = null;
 				for (CommonName c : taxon.getCommonNames()) {
 					if (c.getId() == commonName.getId()) {
 						toRemove = c;
 						break;
 					}
 				}
 				taxon.getCommonNames().remove(toRemove);
 				taxon.getCommonNames().add(commonName);
 				callback.onSuccess(null);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	public void addCommonName(final Taxon taxon, final CommonName commonName, final GenericCallback<String> callback) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.putAsText(UriBase.getInstance().getSISBase() + "/taxon/" + taxon.getId() + "/commonname", commonName.toXML(), new GenericCallback<String>() {
 		
 			@Override
 			public void onSuccess(String result) {
 				String newId = ndoc.getText();
 				if (commonName.getId() == 0) {
 					commonName.setId(Integer.parseInt(newId));
 					
 				} else {
 					CommonName toRemove = null;
 					for (CommonName c : taxon.getCommonNames()) {
 						if (c.getId() == commonName.getId()) {
 							toRemove = c;
 							break;
 						}
 					}
 					taxon.getCommonNames().remove(toRemove);
 				}
 				taxon.getCommonNames().add(commonName);
 				callback.onSuccess(newId);
 		
 			}
 		
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 		
 			}
 		});
 		
 	}
 	
 	/**
 	 * Fetch Working sets for related to the taxon
 	 * 
 	 * @param taxon
 	 * @param callback
 	 */
 	public void fetchWorkingSetsForTaxon(final Taxon taxon,final GenericCallback<List<WorkingSet>> wayBack) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/browse/workingSets/" + taxon.getId(), new GenericCallback<String>() {
 
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				List<WorkingSet> workingSets;
 				workingSets = new ArrayList<WorkingSet>();
 				
 				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(WorkingSet.ROOT_TAG);
 				for (int i = 0; i < list.getLength(); i++) {
 					NativeElement element = (NativeElement) list.item(i);
 					WorkingSet ws = WorkingSet.fromXMLMinimal(element);
 					workingSets.add(ws);	
 				}
 				wayBack.onSuccess(workingSets);
 			}
 
 		});
 	}
 
 }
