 package com.worxforus.ctg.net;
 
 import junit.framework.Assert;
 import android.content.Context;
 
 import com.worxforus.Command;
 import com.worxforus.Result;
 import com.worxforus.SyncEntry;
 import com.worxforus.Utils;
 import com.worxforus.app.CITListNotifier;
 import com.worxforus.app.CTListNotifier;
 import com.worxforus.app.RCIListNotifier;
 import com.worxforus.app.RCListNotifier;
 import com.worxforus.app.TagListNotifier;
 import com.worxforus.ctg.CTGChecklistItemTemplate;
 import com.worxforus.ctg.CTGChecklistTemplate;
 import com.worxforus.ctg.CTGConstants;
 import com.worxforus.ctg.CTGRunChecklist;
 import com.worxforus.ctg.CTGRunChecklistItem;
 import com.worxforus.ctg.CTGTag;
 import com.worxforus.ctg.TablePool;
 import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
 import com.worxforus.ctg.db.CTGChecklistTemplateTable;
 import com.worxforus.ctg.db.CTGRunChecklistItemTable;
 import com.worxforus.ctg.db.CTGRunChecklistTable;
 import com.worxforus.ctg.db.CTGTagTable;
 import com.worxforus.db.TableManager;
 import com.worxforus.net.NetAuthentication;
 import com.worxforus.net.SyncTableManager;
 
 public class SyncNetworkUploadCommand implements Command {
 
 	public static final String MESSAGE_ABORT_SYNC="Sync operation has received an abort command.";
 	private volatile boolean abort = false;
 	private Context c;
 	private int itemsPerPage;
 	private volatile int state= Command.STATE_WAITING;
 	
 	public SyncNetworkUploadCommand(Context c, int itemsPerPage) {
 		this.c = c;
 		this.itemsPerPage = itemsPerPage;
 	}
 	
 	@Override
 	public synchronized Result execute() {
 		state = Command.STATE_RUNNING;
 		//Clear the sync db
 		//do any other needed cleanup to reset for a different user
 		Assert.assertNotNull(c);
 			
 		Result r = new Result();
 		//check last sync dates
 		SyncEntry ctagInfo = TableManager.getTableSyncInfo(c, CTGConstants.DATABASE_NAME, CTGTagTable.DATABASE_TABLE);
 		CTGTagTable ctgTable = TablePool.getTagTable(c);
 		SyncEntry ctInfo = TableManager.getTableSyncInfo(c, CTGConstants.DATABASE_NAME, CTGChecklistTemplateTable.DATABASE_TABLE);
 		CTGChecklistTemplateTable ctTable = TablePool.getCTTable(c);
 		SyncEntry citInfo = TableManager.getTableSyncInfo(c, CTGConstants.DATABASE_NAME, CTGChecklistItemTemplateTable.DATABASE_TABLE);
 		CTGChecklistItemTemplateTable citTable = TablePool.getCITTable(c);
 		SyncEntry rcInfo = TableManager.getTableSyncInfo(c, CTGConstants.DATABASE_NAME, CTGRunChecklistTable.DATABASE_TABLE);
 		CTGRunChecklistTable rcTable = TablePool.getRCTable(c);
 		SyncEntry rciInfo = TableManager.getTableSyncInfo(c, CTGConstants.DATABASE_NAME, CTGRunChecklistItemTable.DATABASE_TABLE);
 		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(c);
 		
 		SyncTableManager sm = new SyncTableManager();
 		Utils.LogD(this.getClass().getName(), "Starting CTG Network sync.");
 		//This took 13 seconds to download the entire site for user after using compiled statements for only CITs.
 		//This took 16 seconds to download the entire site for user after using compiled statements for all data tables.
 		long startTime = System.nanoTime()/1000000;
 
 		
 		if (checkAbort(r)) {
 			return r;
 		}
 		//Don't attempt to load pages that require login if we can't login
 		if(NetAuthentication.isReadyForLogin()) {
 			r =  sm.handleSyncTableUpload(c, CTGWebHelper.getHost(), CTGConstants.DATABASE_NAME, ctgTable, new CTGTag(), itemsPerPage);
			//Display data isn't changing here on upload - so don't update list
//			TagListNotifier.getNotifier().updateList();
 			if (checkAbort(r)) {
 				return r;
 			}
 			r.add_results_if_error(sm.handleSyncTableUpload(c, CTGWebHelper.getHost(), CTGConstants.DATABASE_NAME, ctTable, new CTGChecklistTemplate(), itemsPerPage),"");
			//Display data isn't changing here on upload - so don't update list
//			CTListNotifier.getNotifier().updateList();
 			if (checkAbort(r)) {
 				return r;
 			}
 			r.add_results_if_error(sm.handleSyncTableUpload(c, CTGWebHelper.getHost(), CTGConstants.DATABASE_NAME, citTable, new CTGChecklistItemTemplate(), itemsPerPage), "");
			//Display data isn't changing here on upload - so don't update list
//			CITListNotifier.getNotifier().updateList();
 		
 			r.add_results_if_error(sm.handleSyncTableUpload(c, CTGWebHelper.getHost(), CTGConstants.DATABASE_NAME, rcTable, new CTGRunChecklist(), itemsPerPage), "");
			//Display data isn't changing here on upload - so don't update list
//			RCListNotifier.getNotifier().updateList();
 			if (checkAbort(r)) {
 				return r;
 			}
 
 			r.add_results_if_error(sm.handleSyncTableUpload(c, CTGWebHelper.getHost(), CTGConstants.DATABASE_NAME, rciTable, new CTGRunChecklistItem(), itemsPerPage), "");
			//Display data isn't changing here on upload - so don't update list
//			RCIListNotifier.getNotifier().updateList();
 		}
 		long finishTime = System.nanoTime()/1000000;
 		Utils.LogD(this.getClass().getName(), "Finish CTG Upload sync - took "+(finishTime-startTime)+" ms");
 		state = Command.STATE_FINISHED;
         return r;
 		
 	}
 
 	private boolean checkAbort(Result r) {
 		if (abort) {
 			r.success = false;
 			r.technical_error = MESSAGE_ABORT_SYNC;
 			state = Command.STATE_FINISHED;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void abort() {
 		this.abort = true;
 	}
 	
 	@Override
 	public synchronized void attach(Context c) {
 		this.c = c;
 	}
 
 	@Override
 	public synchronized void release() {
 		this.c = null;
 	}
 
 	@Override
 	public int getState() {
 		return state;
 	}
 
 }
