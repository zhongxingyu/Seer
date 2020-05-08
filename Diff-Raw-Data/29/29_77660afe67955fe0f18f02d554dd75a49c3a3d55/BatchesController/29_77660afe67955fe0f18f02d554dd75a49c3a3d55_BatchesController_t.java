 package com.andrewma.vision.webserver.controllers;
 
import java.util.List;

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
	public Result New() {
		final List<Batch> lastBatch = dbHelper.executeSql(Batch.class,
				"SELECT * FROM Batches ORDER BY [BatchId] DESC LIMIT 1");
		final Batch newBatch = new Batch();
		if(lastBatch == null || lastBatch.size() == 0)
			newBatch.BatchId = 1;
		else
			newBatch.BatchId = lastBatch.get(0).BatchId + 1;
		newBatch.Name = newBatch.Glasses = "";

		dbHelper.insert(newBatch);
		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON, newBatch);
 	}
 }
