 package org.iucn.sis.client.api.caches;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.data.WorkingSetParser;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 /**
  * Class that represents the working set cache. Main member is the static
  * representation of the class, to be used to represent the current working set.
  * 
  * @author liz.schwartz
  * 
  */
 public class WorkingSetCache {
 
 	public static class WorkingSetComparator extends PortableAlphanumericComparator {
 		public WorkingSetComparator() {
 			super();
 		}
 
 		@Override
 		public int compare(Object ol, Object or) {
 			WorkingSet ws1 = (WorkingSet) ol;
 			WorkingSet ws2 = (WorkingSet) or;
 
 			return super.compare(ws1.getName(), ws2.getName());
 		}
 	}
 
 	public static final WorkingSetCache impl = new WorkingSetCache();
 	/**
 	 * A hashmap of working sets <Integer id, WorkingSet>
 	 */
 	private Map<Integer, WorkingSet> workingSets;
 
 	private WorkingSet currentWorkingSet;
 
 	/**
 	 * an arraylist of working set data that is possible to subscribe to.
 	 */
 	private ArrayList<WorkingSet> subscribableWorkingSets;
 
 	private WorkingSetCache() {
 		workingSets = new HashMap<Integer, WorkingSet>();
 		currentWorkingSet = null;
 		subscribableWorkingSets = new ArrayList<WorkingSet>();
 	}
 
 	/**
 	 * adds the given working set data object to the public working set pool.
 	 * Assumes that the id of the working set is either null or "", will send to
 	 * edit if the id matches another working set, and fails otherwise.
 	 * 
 	 * @param ws
 	 * @param wayBack
 	 */
 	public void createWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 
 		if (ws.getId() == 0) {
 
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.putAsText(UriBase.getInstance() + "/workingSet/public/" + SISClientBase.currentUser.getUsername(), ws
 					.toXML(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayBack.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					ws.setId(Integer.valueOf(ndoc.getText()));
 					workingSets.put(Integer.valueOf(ws.getId()), ws);
 					wayBack.onSuccess(arg0);
 				}
 			});
 		} else
 			wayBack.onFailure(new Throwable("Don't know what to do because user dosen't have rights to specify"
 					+ " the working set id."));
 	}
 
 	private void addToWorkingSetCache(ArrayList<WorkingSet> ws) {
 		for (WorkingSet curWS : ws)
 			addToWorkingSetCache(curWS);
 	}
 
 	private void addToWorkingSetCache(WorkingSet ws) {
 		workingSets.put(ws.getId(), ws);
 	}
 
 	public void createTaxaList(Integer id, final GenericCallback<String> wayback) {
 		if (getWorkingSet(id) != null) {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.getAsText(UriBase.getInstance().getSISBase() + "/workingSet/taxaList/"
 					+ SISClientBase.currentUser.getUsername() + "/" + id, new GenericCallback<String>() {
 
 				public void onFailure(Throwable caught) {
 					// TODO Auto-generated method stub
 					wayback.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
 					// DOWNLOAD THE FILE
 					wayback.onSuccess(ndoc.getText());
 				}
 
 			});
 		} else
 			wayback.onFailure(new Throwable("Not a valid working set"));
 	}
 
 	public void createTaxaListWithCats(Integer id, AssessmentFilter filter, final GenericCallback<String> wayback) {
 		if (getWorkingSet(id) != null) {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.postAsText(UriBase.getInstance().getSISBase() + "/workingSet/taxaList/"
 					+ SISClientBase.currentUser.getUsername() + "/" + id, "<root>" + filter.toXML() + "</root>",
 					new GenericCallback<String>() {
 
 						public void onFailure(Throwable caught) {
 							// TODO Auto-generated method stub
 							wayback.onFailure(caught);
 						}
 
 						public void onSuccess(String arg0) {
 							// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
 							// DOWNLOAD THE FILE
 							wayback.onSuccess(ndoc.getText());
 						}
 
 					});
 		} else
 			wayback.onFailure(new Throwable("Not a valid working set"));
 	}
 
 	public void unsubscribeToWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/workingSet/unsubscribe/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				workingSets.remove(ws.getId());
 				wayBack.onSuccess("YAY");
 			}
 		});
 
 	}
 
 	public void deleteWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/workingSet/public/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				workingSets.remove(ws.getId());
 				wayBack.onSuccess("YAY");
 			}
 		});
 
 	}
 
 	public void doLogout() {
 		workingSets.clear();
 		currentWorkingSet = null;
 	}
 
 	public void editWorkingSet(final WorkingSet ws, final GenericCallback<String> wayback) {
 		if (workingSets.containsKey(ws.getId())) {
 			NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.post(UriBase.getInstance().getSISBase() + "/workingSet/public/"
 					+ SISClientBase.currentUser.getUsername() + "/", ws.toXML(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayback.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					workingSets.put(ws.getId(), ws);
 					wayback.onSuccess(arg0);
 				}
 			});
 
 		} else {
 			wayback.onFailure(new Throwable("Not currently in the workingSet"));
 		}
 	}
 
 	public void editTaxaInWorkingSet(final WorkingSet ws, final Collection<Taxon> taxonToAdd,
 			final Collection<Taxon> taxonToRemove, final GenericCallback<String> callback) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		StringBuilder posting = new StringBuilder("<taxa>");
 		if (taxonToAdd != null) {
 			for (Taxon taxon : taxonToAdd)
 				posting.append("<add>" + taxon.getId() + "</add>");
 		}
 		if (taxonToRemove != null) {
 			for (Taxon taxon : taxonToRemove)
 				posting.append("<remove>" + taxon.getId() + "</remove>");
 		}
 		posting.append("</taxa>");
 		ndoc.post(UriBase.getInstance().getSISBase() + "/workingSet/editTaxa/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), posting.toString(),
 				new GenericCallback<String>() {
 
 					@Override
 					public void onSuccess(String result) {
 						if (taxonToRemove != null)
 							ws.getTaxon().removeAll(taxonToRemove);
 						if (taxonToAdd != null)
 							ws.getTaxon().addAll(taxonToAdd);
 						callback.onSuccess(result);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 
 					}
 				});
 
 	}
 
 	public void exportWorkingSet(final Integer workingSetID, final boolean lock, final GenericCallback<String> wayBack) {
 		if (workingSets.containsKey(workingSetID)) {
 			WorkingSet ws = workingSets.get(workingSetID);
 			String mode = "public";
 
 			if (workingSets.containsKey(workingSetID)) {
 				final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 				ndoc.getAsText(UriBase.getInstance().getSISBase() + "/workingSetExporter/" + mode + "/"
 						+ SISClientBase.currentUser.getUsername() + "/" + workingSetID + "?lock=" + lock,
 						new GenericCallback<String>() {
 
 							public void onFailure(Throwable caught) {
 								WindowUtils.errorAlert("Server error while exporting working set."
 										+ "<br>Error as follows: " + ndoc.getText());
 								wayBack.onFailure(caught);
 							}
 
 							public void onSuccess(String arg0) {
 								String body = ndoc.getText();
 								int firstLineBreak = body.indexOf("\r\n", 0);
 								final String path = body.substring(0, firstLineBreak);
 
 								if (lock && body.contains("<div>")) {
 									final Window w = WindowUtils.getWindow(true, false, "Assessments Locked");
 									w.getButtonBar().add(new Button("Close", new SelectionListener<ButtonEvent>() {
 										public void componentSelected(ButtonEvent ce) {
 											wayBack.onSuccess(path);
 											w.close();
 										};
 									}));
 									w.add(new Html(body.substring(firstLineBreak)));
 									w.show();
 									w.setSize(500, 400);
 									w.center();
 								} else
 									wayBack.onSuccess(path);
 							}
 						});
 			} else {
 				wayBack.onFailure(new Throwable("Not a valid working set."));
 			}
 
 		} else
 			wayBack.onFailure(new Throwable("oh no, how did you get here, "
 					+ "the working set is not in your working sets"));
 	}
 
 	public void fetchTaxaForWorkingSet(final WorkingSet ws, final GenericCallback<String> wayback) {
 		TaxonomyCache.impl.fetchList(ws.getSpeciesIDs(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayback.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				List<Integer> toRemove = new ArrayList<Integer>();
 				for (Integer cur : ws.getSpeciesIDs()) {
 					if (!TaxonomyCache.impl.contains(cur))
 						toRemove.add(cur);
 				}
 
 				if (toRemove.size() > 0)
 					ws.getSpeciesIDs().removeAll(toRemove);
 
 				wayback.onSuccess(arg0);
 			}
 		});
 	}
 
 	/**
 	 * gets all public working sets that the user can subscribe to
 	 * 
 	 * @param wayBack
 	 */
 	public void getAllSubscribableWorkingSets(final GenericCallback<String> wayBack) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/workingSet/subscribe/"
 				+ SISClientBase.currentUser.getUsername(), new GenericCallback<String>() {
 
 			public void onFailure(Throwable caught) {
 				subscribableWorkingSets.clear();
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				subscribableWorkingSets.clear();
 				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(WorkingSet.ROOT_TAG);
 				for (int i = 0; i < list.getLength(); i++) {
 					NativeElement element = (NativeElement) list.item(i);
 					WorkingSet ws = WorkingSet.fromXMLMinimal(element);
 
 					if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.READ, ws)) {
 						subscribableWorkingSets.add(ws);
 					}
 				}
 				wayBack.onSuccess(arg0);
 			}
 
 		});
 	}
 
 	public WorkingSet getCurrentWorkingSet() {
 		return currentWorkingSet;
 	}
 
 	public int getNumberOfWorkingSets() {
 		return workingSets.size();
 	}
 
 	public ArrayList<WorkingSet> getSubscribable() {
 		return subscribableWorkingSets;
 	}
 
 	/**
 	 * given the working set id, returns the working set associated with it,
 	 * null if it doesn't exist
 	 * 
 	 * @param id
 	 */
 	public WorkingSet getWorkingSet(Integer id) {
 		return workingSets.get(id);
 	}
 
 	public Map<Integer, WorkingSet> getWorkingSets() {
 		return workingSets;
 	}
 
 	public void resetCurrentWorkingSet() {
 		setCurrentWorkingSet((WorkingSet) null);
 	}
 
 	public void setCurrentWorkingSet(Integer id) {
 		
 		setCurrentWorkingSet(workingSets.get(id));
 		
 	}
 
 	public void setCurrentWorkingSet(WorkingSet ws) {
 		WorkingSet oldCurrent = currentWorkingSet;
 		currentWorkingSet = ws;
		if (oldCurrent == null && currentWorkingSet != null || oldCurrent != null && currentWorkingSet == null || oldCurrent != null && currentWorkingSet != null && !oldCurrent.equals(currentWorkingSet)){
 			SISClientBase.getInstance().onWorkingSetChanged();
		}
 	}
 
 	/**
 	 * given the workingSetID, adds the id to your list of working sets that you
 	 * are subscribed to
 	 * 
 	 * @param workingSetID
 	 * @param wayBack
 	 */
 	public void subscribeToWorkingSet(final String workingSetID, final GenericCallback<String> wayBack) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.put(UriBase.getInstance().getSISBase() + "/workingSet/subscribe/"
 				+ SISClientBase.currentUser.getUsername() + "/" + workingSetID, "", new GenericCallback<String>() {
 
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				workingSets.clear();
 				resetCurrentWorkingSet();
 				update(wayBack);
 			}
 
 		});
 	}
 
 	/**
 	 * given the workingSetIDs in csv form, removes the ids to your list of
 	 * working sets that you are subscribed to
 	 * 
 	 * @param workingSetID
 	 * @param wayBack
 	 */
 	public void unsubscribeToWorkingSets(final String workingSetID, final GenericCallback<String> wayBack) {
 
 	}
 
 	public void update(final GenericCallback<String> backInTheDay) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/workingSet/list/" + SISClientBase.currentUser.getUsername(),
 				new GenericCallback<String>() {
 
 					public void onFailure(Throwable caught) {
 						backInTheDay.onFailure(caught);
 					}
 
 					public void onSuccess(String arg0) {
 						try {
 							WorkingSetParser parser = new WorkingSetParser();
 							parser.parseWorkingSetXML(ndoc);
 							addToWorkingSetCache(parser.getWorkingSets());
 							backInTheDay.onSuccess(arg0);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				});
 	}
 
 }
