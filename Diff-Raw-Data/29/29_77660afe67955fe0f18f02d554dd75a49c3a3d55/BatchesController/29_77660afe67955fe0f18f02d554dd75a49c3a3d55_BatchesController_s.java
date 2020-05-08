 package com.andrewma.vision.webserver.controllers;
 
 import android.content.Context;
 
 import com.andrewma.vision.database.DatabaseHelper;
 import com.andrewma.vision.models.Batch;
 import com.andrewma.vision.webserver.core.Controller;
 import com.andrewma.vision.webserver.core.NanoHTTPD;
 import com.andrewma.vision.webserver.core.VisionHTTPD;
 import com.andrewma.vision.webserver.core.annotations.Action;
 
 public class BatchesController extends Controller {
 
 	final DatabaseHelper dbHelper;
 
 	public BatchesController(Context context) {
 		super();
 		dbHelper = DatabaseHelper.getInstance(context);
 	}
	
 	@Action
 	public Result Get(int id) {
 		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON,
 				dbHelper.get(Batch.class, id));
 	}
 
 	@Action
	public Result Add(Batch glasses) {
		dbHelper.insert(glasses);
		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON, true);
 	}
 }
