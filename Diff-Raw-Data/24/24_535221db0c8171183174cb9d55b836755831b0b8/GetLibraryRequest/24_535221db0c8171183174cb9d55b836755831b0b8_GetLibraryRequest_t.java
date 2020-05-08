 package com.robonobo.midas.client;
 
 import java.util.Date;
 
import com.robonobo.core.api.model.Library;
 import com.robonobo.core.api.proto.CoreApi.LibraryMsg;
 import com.robonobo.core.metadata.LibraryHandler;
 import com.robonobo.midas.client.Params.Operation;
 
 public class GetLibraryRequest implements Request {
 	private MidasClientConfig cfg;
 	private Long userId;
 	private Date lastUpdated;
 	private LibraryHandler handler;
 
 	public GetLibraryRequest(MidasClientConfig cfg, long userId, Date lastUpdated, LibraryHandler handler) {
 		this.cfg = cfg;
 		this.userId = userId;
 		this.lastUpdated = lastUpdated;
 		this.handler = handler;
 	}
 
 	@Override
 	public int remaining() {
 		if (userId == null)
 			return 0;
 		return 1;
 	}
 
 	@Override
 	public Params getNextParams() {
 		Params p = new Params(Operation.Get, null, LibraryMsg.newBuilder(), cfg.getLibraryUrl(userId, lastUpdated), userId);
 		userId = null;
 		return p;
 	}
 
 	@Override
 	public void success(Object obj) {
		LibraryMsg msg = (LibraryMsg) obj;
 		if(handler != null)
			handler.success(new Library(msg));
 	}
 
 	@Override
 	public void error(Params p, Exception e) {
 		if(handler != null) {
 			Long uid = (Long) p.obj;
 			handler.error(uid, e);
 		}
 	}
 }
