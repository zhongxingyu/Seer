 package com.reindeermobile.pexeso.controller;
 
 import com.reindeermobile.pexeso.entity.Record;
 import com.reindeermobile.reindeerorm.EntityManagable;
import com.reindeermobile.reindeerorm.EntityManagerFactory;
 import com.reindeermobile.reindeerorm.exception.EntityMappingException;
 import com.reindeermobile.reindeerutils.mvp.AbstractController;
 import com.reindeermobile.reindeerutils.mvp.Presenter;
 import com.reindeermobile.reindeerutils.mvp.Presenter.ControllerServices;
 import com.reindeermobile.reindeerutils.mvp.Presenter.ViewServices;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class DatabaseController extends AbstractController {
 	public static final String RECORD_LIST = "RECORD_LIST";
 
 	public static final String SAVE_RECORD = "SAVE_RECORD";
 	public static final String GET_RECORD_LIST = "GET_RECORD_LIST";
 	public static final String CLEAR_RECORD_LIST = "CLEAR_RECORD_LIST";
 
 	public static final String SAVE_RECORD_OK = "SAVE_RECORD_OK";
 	public static final String SEND_RECORD_LIST = "SEND_RECORD_LIST";
 
 	@ControllerServices
 	public static final String[] CONTROLLER_SERVICES = new String[] {
 			SAVE_RECORD, GET_RECORD_LIST, CLEAR_RECORD_LIST };
 
 	@ViewServices
 	public static final String[] VIEW_SERVICES = new String[] { SAVE_RECORD_OK,
 			SEND_RECORD_LIST };
 
 	private EntityManagable entityManager;
 
 	@Override
 	public void init(Context context) {
//		Properties properties = new Properties();
//		properties.put("databaseName", "am_pexeso_database1");
		EntityManagerFactory.init(context, Record.class);
		// EntityManagerFactory.INSTANCE.init(context, "am_pexeso_database1", 1,
		// Record.class);
 
 		this.entityManager = EntityManagerFactory.createInstance();
 
 		this.initTasks();
 	}
 
 	@Override
 	protected void initTasks() {
 		super.registerTask(SAVE_RECORD, new IContollerTask() {
 			@Override
 			public void execute(Bundle bundle) {
 				if (bundle != null) {
 					Record record = (Record) bundle.getParcelable(SAVE_RECORD);
 					Log.d(TAG, "execute - before persist: " + record);
 					try {
 						entityManager.persist(record, Record.class);
 					} catch (EntityMappingException exception) {
 						Log.w(TAG, "initTasks - " + SAVE_RECORD, exception);
 					}
 					Log.d(TAG, "execute - after persist: " + record);
 				}
 			}
 		});
 
 		super.registerTask(CLEAR_RECORD_LIST, new IContollerTask() {
 			@Override
 			public void execute(Bundle bundle) {
 				entityManager.createNamedNativeQuery("deleteAll", Record.class)
 						.execute();
 				sendRecords();
 			}
 		});
 
 		super.registerTask(GET_RECORD_LIST, new IContollerTask() {
 			@Override
 			public void execute(Bundle bundle) {
 				sendRecords();
 			}
 		});
 	}
 
 	private void sendRecords() {
 		ArrayList<Record> records = null;
 		records = (ArrayList<Record>) entityManager.createNamedNativeQuery(
 				"listRecords", Record.class).list();
 
 		if (records != null) {
 			Collections.sort(records);
 		}
 
 		Log.d(TAG, "sendRecords - records: " + records);
 
 		Bundle messageBundle = new Bundle();
 		messageBundle.putParcelableArrayList(RECORD_LIST, records);
 
 		Presenter.getInst().sendViewMessage(SEND_RECORD_LIST, messageBundle);
 
 	}
 }
