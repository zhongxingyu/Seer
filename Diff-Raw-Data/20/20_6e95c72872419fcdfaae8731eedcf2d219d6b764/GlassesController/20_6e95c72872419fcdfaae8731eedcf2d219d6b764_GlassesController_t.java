 package com.andrewma.vision.webserver.controllers;
 
import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.util.FloatMath;
 
 import com.andrewma.vision.database.DatabaseHelper;
 import com.andrewma.vision.models.Glasses;
 import com.andrewma.vision.webserver.core.Controller;
 import com.andrewma.vision.webserver.core.NanoHTTPD;
 import com.andrewma.vision.webserver.core.VisionHTTPD;
 import com.andrewma.vision.webserver.core.annotations.Action;
 
 public class GlassesController extends Controller {
 
 	final DatabaseHelper dbHelper;
 
 	public GlassesController(Context context) {
 		super();
 		dbHelper = DatabaseHelper.getInstance(context);
 	}
 
 	@Action
 	public Result Search(Glasses query) {
 		return GetAll();
 	}
 
 	@Action
 	public Result GetAll() {
 		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON,
 				dbHelper.getAll(Glasses.class));
 	}
 
 	@Action
 	public Result Get(int id) {
 		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON,
 				dbHelper.get(Glasses.class, id));
 	}
 
 	@Action
 	public synchronized Result Add(Glasses glasses) {
 		// figure out which group this pair goes to. the algorithm for this is
 		// rounding the OD (right) Sph value away from 0
 		boolean positive = (glasses.OD_Spherical >= 0);
 		glasses.Group = (int) FloatMath.ceil(Math.abs(glasses.OD_Spherical));
 		// anything over 10 gets grouped into the 20 group
 		if (glasses.Group > 10)
 			glasses.Group = 20;
 		if (!positive)
 			glasses.Group *= -1;
 
 		// find number
 		final List<Glasses> glassesInGroup = dbHelper.executeSql(Glasses.class,
 				"SELECT * FROM Glasses WHERE [Group] = " + glasses.Group);
 		int max = 0;
 		for(Glasses g : glassesInGroup) {
 			if(g.Number > max)
 				max = g.Number;
 		}
 		glasses.Number = max + 1;
		
		glasses.AddedDate = new Date();
 
 		glasses.GlassesId = (int) dbHelper.insert(glasses);
		
		// if database couldn't insert glasses, it will return -1 as the ID
		if(glasses.GlassesId == -1) {
			return ErrorResult("Couldn't add glasses. Error on db.insert.");
		}
 		return Result(NanoHTTPD.HTTP_OK, VisionHTTPD.MIME_JSON, glasses);
 	}
 }
