 package com.szas.server;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.szas.sync.SyncedElementsHolder;
 import com.szas.sync.ToSyncElementsHolder;
 import com.szas.sync.WrongObjectThrowable;
 
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 public class SyncNoAuthServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
		// TODO check incomming content type
 		
 		BufferedReader bufferedReader = req.getReader();
 		ArrayList<ToSyncElementsHolder> toSyncElementsHolders =
 			new JSONDeserializer<ArrayList<ToSyncElementsHolder>>().deserialize(bufferedReader);
 		
 		responseSync(resp, toSyncElementsHolders);
 	}
 	private void responseSync(HttpServletResponse resp,
 			ArrayList<ToSyncElementsHolder> toSyncElementsHolders)
 			throws IOException{
 		try {
 			ArrayList<SyncedElementsHolder> syncedElementsHolders = StaticSyncer.getSyncHelper().sync(toSyncElementsHolders);
 			resp.setContentType("application/json");
 			PrintWriter printWriter = resp.getWriter();
 			new JSONSerializer().include("*").serialize(syncedElementsHolders,printWriter);
 		} catch (WrongObjectThrowable e) {
 			e.printStackTrace();
 			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
             	"Error while syncing data.");
 		}
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		ArrayList<ToSyncElementsHolder> toSyncElementsHolders = 
 			new ArrayList<ToSyncElementsHolder>();
 		ToSyncElementsHolder toSyncElementsHolder = 
 			new ToSyncElementsHolder();
 		toSyncElementsHolders.add(toSyncElementsHolder);
 		toSyncElementsHolder.className = "users";
 		toSyncElementsHolder.lastTimestamp = -1;
 		toSyncElementsHolder.elementsToSync =
 			new ArrayList<Object>();
 		responseSync(resp, toSyncElementsHolders);
 	}
 }
