 package plugins.SiteToolPlugin.fproxy.dav.sampleimpl;
 
 import java.io.IOException;
 import java.net.URI;
 
 import plugins.SiteToolPlugin.fproxy.dav.api.IMimeTyper;
 import plugins.SiteToolPlugin.fproxy.dav.api.IResourceLocks;
 import plugins.SiteToolPlugin.fproxy.dav.api.ITransaction;
 import plugins.SiteToolPlugin.fproxy.dav.api.IWebDAVStore;
 import plugins.SiteToolPlugin.fproxy.dav.api.WebDAVStatus;
 import plugins.SiteToolPlugin.fproxy.dav.exceptions.WebDAVException;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoCopy;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoDelete;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoGet;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoHead;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoLock;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoMkcol;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoMove;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoNotImplemented;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoOptions;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoPropfind;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoProppatch;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoPut;
 import plugins.SiteToolPlugin.fproxy.dav.methods.DoUnlock;
 import plugins.fproxy.lib.PluginContext;
 import plugins.fproxy.lib.WebInterfaceToadlet;
 import freenet.clients.http.RedirectException;
 import freenet.clients.http.Toadlet;
 import freenet.clients.http.ToadletContext;
 import freenet.clients.http.ToadletContextClosedException;
 import freenet.clients.http.annotation.AllowData;
 import freenet.support.Logger;
 import freenet.support.api.HTTPRequest;
 
 public class DAVToadlet extends WebInterfaceToadlet {
 
 	private static volatile boolean logDEBUG;
 
 	static {
 		Logger.registerClass(DAVToadlet.class);
 	}
 
 	enum METHODS { GET, OPTIONS, PROPFIND, PROPPATCH, MKCOL, COPY,
 		MOVE, LOCK, UNLOCK, HEAD, PUT, DELETE }
 
 	private final IWebDAVStore _store;
 
 	private final Toadlet _showAsToadlet;
 
 	private DoGet doGet;
 	private DoHead doHead;
 	private DoDelete doDelete;
 	private DoCopy doCopy;
 	private DoLock doLock;
 	private DoUnlock doUnlock;
 	private DoMove doMove;
 	private DoMkcol doMkcol;
 	private DoOptions doOptions;
 	private DoPut doPut;
 	private DoPropfind doPropfind;
 	private DoProppatch doProppatch;
 	private DoNotImplemented doNotImplemented;
 
 	private String dftIndexFile;
 
 	private String insteadOf404;
 
 	private IResourceLocks _resLocks;
 
 	private IMimeTyper mimeTyper;
 
 	private int nocontentLenghHeaders;
 
 	private final boolean _readOnly;
 
 	private boolean lazyFolderCreationOnPut;
 
 	private ITransaction _transaction;
 
 	public DAVToadlet(PluginContext stCtx, String pluginURL, String pageName, Toadlet showAsToadlet, IWebDAVStore store, IResourceLocks resLocks, boolean readOnly) {
 		super(stCtx, pluginURL, pageName);
 		_showAsToadlet = showAsToadlet;
 		_store = store;
 		_resLocks = resLocks;
 		_readOnly = readOnly;
 		init();
 	}
 
 	private void init() {
 		// TODO / FIXME
 		mimeTyper = new IMimeTyper() {
             public String getMimeType(String path) {
                return "mimetype";
             }
         };
         doGet = new DoGet(this, _store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders);
         doHead = new DoHead(this, _store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders);
         doDelete = new DoDelete(this, _store, _resLocks, _readOnly);
         doCopy = new DoCopy(this, _store, _resLocks, doDelete, _readOnly);
         doLock = new DoLock(this, _store, _resLocks, _readOnly);
         doUnlock = new DoUnlock(this, _store, _resLocks, _readOnly);
         doMove = new DoMove(this, _resLocks, doDelete, doCopy, _readOnly);
         doMkcol = new DoMkcol(this, _store, _resLocks, _readOnly);
         doOptions = new DoOptions(this, _store, _resLocks);
         doPut = new DoPut(this, _store, _resLocks, _readOnly, lazyFolderCreationOnPut);
         doPropfind = new DoPropfind(this, _store, _resLocks, mimeTyper);
         doProppatch = new DoProppatch(this, _store, _resLocks, _readOnly);
         doNotImplemented = new DoNotImplemented(_readOnly);
 	}
 
 	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.GET, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodOPTIONS(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, 	RedirectException {
 		handle(METHODS.OPTIONS, _transaction, uri, req, ctx);
 	}
 
 	@AllowData(true)
 	public void handleMethodPROPFIND(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.PROPFIND, _transaction, uri, req, ctx);
 	}
 
 	@AllowData(true)
 	public void handleMethodPROPPATCH(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.PROPPATCH, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodMKCOL(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.MKCOL, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodCOPY(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, 	RedirectException {
 		handle(METHODS.COPY, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodMOVE(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, 	RedirectException {
 		handle(METHODS.MOVE, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodLOCK(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, 	RedirectException {
 		handle(METHODS.LOCK, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodUNLOCK(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, 	RedirectException {
 		handle(METHODS.UNLOCK, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodHEAD(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.HEAD, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodPUT(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.PUT, _transaction, uri, req, ctx);
 	}
 
 	public void handleMethodDELETE(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 		handle(METHODS.DELETE, _transaction, uri, req, ctx);
 	}
 
 	private void handle(METHODS method, ITransaction transaction, URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
 
 		try {
 			switch (method) {
 				case GET:		doGet.handle(_transaction, uri, req, ctx); break;
 				case OPTIONS:	doOptions.handle(_transaction, uri, req, ctx); break;
 				case PROPFIND:	doPropfind.handle(_transaction, uri, req, ctx); break;
 				case PROPPATCH:	doProppatch.handle(_transaction, uri, req, ctx); break;
 				case MKCOL:		doMkcol.handle(_transaction, uri, req, ctx); break;
 				case COPY:		doCopy.handle(_transaction, uri, req, ctx); break;
 				case MOVE:		doMove.handle(_transaction, uri, req, ctx); break;
 				case LOCK:		doLock.handle(_transaction, uri, req, ctx); break;
 				case UNLOCK:	doUnlock.handle(_transaction, uri, req, ctx); break;
 				case HEAD:		doHead.handle(_transaction, uri, req, ctx); break;
 				case PUT:		doPut.handle(_transaction, uri, req, ctx); break;
 				case DELETE:	doDelete.handle(_transaction, uri, req, ctx); break;
 				default:		doNotImplemented.handle(_transaction, uri, req, ctx);
 			}
 		} catch (WebDAVException we) {
 			Logger.error(this, "500 Internal failure", we);
 			ctx.sendReplyHeaders(WebDAVStatus.SC_INTERNAL_SERVER_ERROR, WebDAVStatus.getStatusText(WebDAVStatus.SC_INTERNAL_SERVER_ERROR), null, null, -1);
 		}
 
 	}
 
 	@Override
 	public Toadlet showAsToadlet() {
 		return _showAsToadlet;
 	}
 
 }
