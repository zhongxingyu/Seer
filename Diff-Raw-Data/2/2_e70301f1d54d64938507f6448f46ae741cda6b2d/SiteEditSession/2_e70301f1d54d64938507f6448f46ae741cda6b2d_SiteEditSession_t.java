 package de.saces.fnplugins.SiteToolPlugin.sessions;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 
 
 import com.db4o.ObjectContainer;
 
 import de.saces.fnplugins.SiteToolPlugin.VerboseWaiter;
 
 import freenet.client.DefaultMIMETypes;
 import freenet.client.InsertContext;
 import freenet.client.InsertException;
 import freenet.client.PutWaiter;
 import freenet.client.async.DatabaseDisabledException;
 import freenet.client.async.DefaultManifestPutter;
 import freenet.client.async.ManifestElement;
 import freenet.keys.FreenetURI;
 import freenet.node.RequestClient;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.support.HTMLNode;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 import freenet.support.io.FileBucket;
 import freenet.support.plugins.helpers1.AbstractFCPHandler;
 import freenet.support.plugins.helpers1.PluginContext;
 
 public class SiteEditSession extends AbstractSiteToolSession {
 
 	private HashMap<String, Object> data;
 
 	private FreenetURI insertURI;
 
 	private final PluginContext pluginContext;
 
 	private FreenetURI fetchURI;
 
 	public SiteEditSession(String identifier, PluginContext pctx) {
 		super(identifier);
 		data = new HashMap<String, Object>();
 		pluginContext = pctx;
 	}
 
 	@Override
 	public void cancel() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void destroySession() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void execute(final PluginReplySender replysender) {
 		// insert data
 		VerboseWaiter vw = new VerboseWaiter(replysender, sessionID);
 		try {
 			fetchURI = insert(vw);
 		} catch (InsertException e) {
 			try {
 				AbstractFCPHandler.sendErrorWithTrace(replysender, sessionID, e);
 			} catch (PluginNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return;
 		}
 		SimpleFieldSet sfs = new SimpleFieldSet(true);
 		sfs.putOverwrite("Status", "PutSuccesful");
 		sfs.putSingle("URI", fetchURI.toString(false, false));
 		sfs.putSingle("Identifier", sessionID);
 		try {
 			replysender.send(sfs);
 		} catch (PluginNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void getExtraStatusPanel(HTMLNode node) {
 		node.addChild("#", "<Empty>");
 	}
 
 	@Override
 	public void handleFCP(PluginReplySender replysender, String command, SimpleFieldSet params,
 			Bucket data, int accesstype) throws PluginNotFoundException {
 		String id = params.get("Identifier");
 		if ("AddFileItem".equals(command)) {
 			String fileName = params.get("FileName");
 			File f = new File(fileName);
 			String name = params.get("Name");
 			String mime = params.get("MimeType");
 			if (mime == null)
 				mime = DefaultMIMETypes.guessMIMEType(fileName, false);
 			FileBucket bucket = new FileBucket(f, true, false, false, false, false);
 			addItem(replysender, id, name, mime, bucket, false, true);
 			return;
 		}
 		if ("SetInsertURI".equals(command)) {
 			String uri = params.get("InsertURI");
 
 			try {
 				insertURI = new FreenetURI(uri);
 			} catch (MalformedURLException e) {
 				AbstractFCPHandler.sendError(replysender, 200, id, "Not a valid Freenet URI: "+uri);
 				return;
 				
 			}
 			SimpleFieldSet sfs = new SimpleFieldSet(true);
 			sfs.putOverwrite("Status", "InsertURISet");
 			sfs.put("Code", 0);
 			sfs.putSingle("Identifier", id);
 			sfs.putSingle("Description", "InsertURI set to: "+ insertURI.toString(false, false));
 			replysender.send(sfs);
 			return;
 		}
 		System.out.println("Handle me: "+command);
 		new Error("Hier gehts weiter").printStackTrace();
 		
 	}
 
 	@Override
 	public boolean canRetry() {
 		return false;
 	}
 
 	private FreenetURI insert(PutWaiter pw) throws InsertException {
 		RequestClient rc = new RequestClient() {
 			public boolean persistent() {
 				return false;
 			}
 			public void removeFrom(ObjectContainer container) {
 			}
 			public boolean realTimeFlag() {
 				return true;
 			}
 			
 		};
 		InsertContext iCtx = pluginContext.hlsc.getInsertContext(true);
 		iCtx.compressorDescriptor = "LZMA";
		DefaultManifestPutter dmp = new DefaultManifestPutter(pw, data, (short) 1, insertURI, "index.html", iCtx, false, rc, false, false, null, pluginContext.clientCore.clientContext);
 		if (pw instanceof VerboseWaiter) {
 			iCtx.eventProducer.addEventListener((VerboseWaiter)pw);
 			((VerboseWaiter) pw).setPutter(dmp);
 		}
 		try {
 			pluginContext.clientCore.clientContext.start(dmp);
 		} catch (DatabaseDisabledException e) {
 			// Impossible
 		}
 		FreenetURI result = pw.waitForCompletion();
 		if (pw instanceof VerboseWaiter) {
 			iCtx.eventProducer.removeEventListener((VerboseWaiter)pw);
 		}
 		return result;
 	}
 
 	private synchronized boolean addItem(PluginReplySender replysender, String identifier, String name, String mimeOverride, Bucket item, boolean overwrite, boolean createpath) throws PluginNotFoundException {
 		int i = name.lastIndexOf("/");
 		String dirName;
 		String itemName;
 		HashMap<String, Object> parent;
 		if (i > -1) {
 			dirName = name.substring(0, i);
 			itemName = name.substring(i+1);
 			parent = findDir(data, dirName, createpath);
 		} else {
 			dirName = null;
 			itemName = name;
 			parent = data;
 		}
 
 		if ((!overwrite) && (parent.containsKey(name))) {
 			AbstractFCPHandler.sendError(replysender, 200, identifier, "Duplicate item: "+name);
 			return false;
 		}
 		parent.put(itemName, new ManifestElement(itemName, item, mimeOverride, item.size()));
 		SimpleFieldSet sfs = new SimpleFieldSet(true);
 		sfs.putOverwrite("Status", "FileAdded");
 		sfs.put("Code", 0);
 		sfs.putSingle("Identifier", identifier);
 		sfs.putSingle("Description", "Item added: "+ name);
 		replysender.send(sfs);
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private HashMap<String, Object> findDir(HashMap<String, Object> parent, String name, boolean create) {
 		String[] s = name.split("/", 2); 
 		Object o = parent.get(s[0]);
 		if (o == null) { // not found
 			if (create) {
 				o = new HashMap<String, Object>();
 				parent.put(s[0], o);
 			} else return null;
 		}
 		if (o instanceof HashMap) {
 			if (s.length == 2) {
 				return findDir((HashMap<String, Object>)o, s[1], create);
 			}
 			return (HashMap<String, Object>)o;
 		}
 		// not a dir FIXME
 		return null;
 	}
 }
