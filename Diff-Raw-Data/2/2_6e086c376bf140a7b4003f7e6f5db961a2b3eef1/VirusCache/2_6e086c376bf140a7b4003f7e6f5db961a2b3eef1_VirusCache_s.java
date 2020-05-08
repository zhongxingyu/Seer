 package org.iucn.sis.client.api.caches;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.models.Virus;
 
 import com.solertium.lwxml.gwt.utils.ClientDocumentUtils;
 import com.solertium.lwxml.shared.GWTConflictException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class VirusCache {
 	
 	public static final VirusCache impl = new VirusCache();
 	
 	private Map<Integer, Virus> cache;
 	
 	private VirusCache() {
 		cache = null;
 	}
 	
 	private void init(final SimpleListener callback) {
 		if (cache != null) {
 			callback.handleEvent();
 			return;
 		}
 		
 		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getVirusBase() + "/viruses", new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				cache = new LinkedHashMap<Integer, Virus>();
 				
 				final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
 				for (int i = 0; i < nodes.getLength(); i++) {
 					final NativeNode node = nodes.item(i);
 					if ("virus".equals(node.getNodeName())) {
 						Virus virus = Virus.fromXML(node);
 						cache.put(virus.getId(), virus);
 					}
 				}
 				
 				callback.handleEvent();
 			}
 			public void onFailure(Throwable caught) {
 				callback.handleEvent();
 			}
 		});
 	}
 	
 	public void get(Integer id, final ComplexListener<Virus> callback) {
 		List<Integer> list = new ArrayList<Integer>();
 		list.add(id);
 	
 		get(list, new ComplexListener<List<Virus>>() {
 			public void handleEvent(List<Virus> eventData) {
 				if (eventData.isEmpty())
 					callback.handleEvent(null);
 				else
 					callback.handleEvent(eventData.get(0));
 			}
 		});
 	}
 	
 	public void get(final List<Integer> ids, final ComplexListener<List<Virus>> callback) {
 		init(new SimpleListener() {
 			public void handleEvent() {
 				List<Virus> list = new ArrayList<Virus>();
 				for (Integer id : ids)
 					if (cache.containsKey(id))
 						list.add(cache.get(id));
 				callback.handleEvent(list);
 			}
 		});
 	}
 	
 	public void list(final ComplexListener<List<Virus>> callback) {
 		init(new SimpleListener() {
 			public void handleEvent() {
 				callback.handleEvent(new ArrayList<Virus>(cache.values()));
 			}
 		});
 	}
 	
 	public void add(final Virus virus, final GenericCallback<Virus> callback) {
 		if (cache.containsKey(virus.getId())) {
 			callback.onFailure(new GWTConflictException("Virus already in list."));
 			return;
 		}
 		
 		init(new SimpleListener() {
 			public void handleEvent() {
 				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 				document.put(UriBase.getInstance().getVirusBase() + "/viruses", virus.toXML(), new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						Virus newVirus = Virus.fromXML(document.getDocumentElement());
 						
 						cache.put(newVirus.getId(), newVirus);
 						
 						callback.onSuccess(newVirus);
 					}
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 				});
 			}
 		});
 	}
 	
 	public void remove(final Virus virus, final GenericCallback<Virus> callback) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.delete(UriBase.getInstance().getVirusBase() + "/viruses/" + virus.getId(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				cache.remove(virus.getId());
 				
 				callback.onSuccess(virus);
 			}
 			public void onFailure(Throwable caught) {
 				if (caught instanceof GWTConflictException)
 					WindowUtils.errorAlert(ClientDocumentUtils.parseStatus(document));
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	public void update(final Virus virus, final GenericCallback<Virus> callback) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.put(UriBase.getInstance().getVirusBase() + "/viruses/" + virus.getId(), virus.toXML(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				callback.onSuccess(virus);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 
 }
