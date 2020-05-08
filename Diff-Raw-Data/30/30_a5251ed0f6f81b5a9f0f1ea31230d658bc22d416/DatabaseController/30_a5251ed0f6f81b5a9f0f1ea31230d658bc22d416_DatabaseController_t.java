 package com.reindeermobile.pexeso.controller;
 
 import com.reindeermobile.pexeso.entity.Record;
 import com.reindeermobile.reindeerutils.db.DbAdapterFactory;
 import com.reindeermobile.reindeerutils.db.IDatabaseAdapter;
 import com.reindeermobile.reindeerutils.mvp.AbstractController;
 import com.reindeermobile.reindeerutils.mvp.MessageObject;
 import com.reindeermobile.reindeerutils.mvp.Presenter;
 import com.reindeermobile.reindeerutils.mvp.Presenter.ControllerServices;
 import com.reindeermobile.reindeerutils.mvp.Presenter.ViewServices;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler.Callback;
 
 import java.util.ArrayList;
 
 public class DatabaseController extends AbstractController {
 	public static final String RECORD_LIST = "RECORD_LIST";
 
 	public static final String SAVE_RECORD = "SAVE_RECORD";
 	public static final String GET_RECORD_LIST = "GET_RECORD_LIST";
 
 	public static final String SAVE_RECORD_OK = "SAVE_RECORD_OK";
 	public static final String SEND_RECORD_LIST = "SEND_RECORD_LIST";
 
 	@ControllerServices
 	public static final String[] CONTROLLER_SERVICES = new String[] {
 			SAVE_RECORD, GET_RECORD_LIST };
 
 	@ViewServices
 	public static final String[] VIEW_SERVICES = new String[] { SAVE_RECORD_OK,
 			SEND_RECORD_LIST };
 
 	private IDatabaseAdapter<Record> databaseAdapter;
 
 	@Override
 	public void init(Context context) {
 		DbAdapterFactory.INSTANCE.init(Record.class);
 
 		this.databaseAdapter = DbAdapterFactory.createInstance(Record.class,
 				context, "am_pexeso_database", 1);
 		
 		this.initTasks();
 	}
 
 	@Override
 	protected void initTasks() {
 		super.registerTask(SAVE_RECORD, new ContollerTask() {
 			@Override
 			public void execute(Callback sender, MessageObject messageObject) {
 				if (messageObject != null
 						&& messageObject.hasData(Record.class)) {
 					Record record = (Record) messageObject.getData();
 					databaseAdapter.insert(record);
 				}
 			}
 		});
		
 		super.registerTask(GET_RECORD_LIST, new ContollerTask() {
 			@Override
 			public void execute(Callback sender, MessageObject messageObject) {
 				ArrayList<Record> records = (ArrayList<Record>) databaseAdapter
 						.list();
 
 				Bundle messageBundle = new Bundle();
 				messageBundle.putParcelableArrayList(RECORD_LIST, records);
 
 				Presenter.getInst().sendViewMessage(SEND_RECORD_LIST,
 						messageBundle);
 			}
 		});
 	}
 }
