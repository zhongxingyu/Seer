 package org.lpgtug.farmacias;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.lpgtug.farmacias.models.DatabaseHelper;
 import org.lpgtug.farmacias.models.Farmacia;
 import org.lpgtug.farmacias.models.Turno;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.field.DataType;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.Toast;
 import java.util.Date;
 public class TurnoService extends IntentService {
 
 	  /** 
 	   * A constructor is required, and must call the super IntentService(String)
 	   * constructor with a name for the worker thread.
 	   */
 	  final static String LOG_TAG = "TurnoService";
 	  private DatabaseHelper databaseHelper = null;
 
 	  public TurnoService() {
 	      super("TurnoService");
 	  }
 
 	  /**
 	   * The IntentService calls this method from the default worker thread with
 	   * the intent that started the service. When this method returns, IntentService
 	   * stops the service, as appropriate.
 	   */
 	  @Override
 	  protected void onHandleIntent(Intent intent) {
 	      // Normally we would do some work here, like download a file.
 		  Log.i(LOG_TAG, "START TURNOS SERCIVE!!!");
 		  this.getFarmaciaData();
 	  }
 	  private void getFarmaciaData() {
 		  	DefaultHttpClient client = new DefaultHttpClient(); 
 			String url = "http://farmacias-de-turno.appspot.com/api/v1/turno/?format=json&limit=1000";
 								
 			//String soqlQuery = "Select Id, Name, BillingStreet, BillingCity, BillingState From Farmacia limit 20";
 	    	RuntimeExceptionDao<Farmacia, Integer> daoFarmacia = getHelper().getFarmaciaDao();
 	    	RuntimeExceptionDao<Turno, Integer> daoTurno = getHelper().getTurnoDao();
 	    	
 	     	
 	    	
 
 	    
 
 
 		/*	try
 			{
 				url += URLEncoder.encode(soqlQuery, "UTF-8");
 			}
 			catch(UnsupportedEncodingException e){}
 			*/
 			HttpGet getRequest = new HttpGet(url);
 			try {
 				HttpResponse response = client.execute(getRequest);			
 				String result = EntityUtils.toString(response.getEntity()); 
 				JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
 				JSONArray records = object.getJSONArray("objects");
 				for (int i=0;i<records.length();i++) {
 					JSONObject record = (JSONObject) records.get(i);
 					JSONObject farmacia = (JSONObject) record.getJSONObject("farmacia");
 					
 					int farmaciaId = farmacia.getInt("id");
 					Farmacia objetoFarmacia = new Farmacia(farmacia.getInt("id"),farmacia.getString("nombre"),farmacia.getString("direccion"),
 							farmacia.getString("descripcion"),farmacia.getString("telefono"),farmacia.getDouble("lat"),farmacia.getDouble("lon"));
 						
 					if (daoFarmacia.queryForId(farmaciaId) == null){		
 						daoFarmacia.create(objetoFarmacia);		
 					}
					Integer turnoId = record.getInt("id");
 					if (daoTurno.queryForId(turnoId) == null){		
 						Turno objetoTurno = new Turno(objetoFarmacia);
 						final Calendar cal = Calendar.getInstance();
 						cal.setTimeInMillis((long) record.getInt("inicio") * 1000);
 						objetoTurno.setInicio(cal.getTime());
 						cal.setTimeInMillis((long) record.getInt("fin") * 1000);
 						objetoTurno.setFin(cal.getTime());
 						objetoTurno.setId(turnoId);
 						daoTurno.create(objetoTurno);
 					}	 
 				}					
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}	
 	}
 	    @Override
 	    public void onDestroy() {
 	        super.onDestroy();
 	        if (databaseHelper != null) {
 	            OpenHelperManager.releaseHelper();
 	            databaseHelper = null;
 	        }
 	    }
 
 	    private DatabaseHelper getHelper() {
 	        if (databaseHelper == null) {
 	            databaseHelper =
 	                OpenHelperManager.getHelper(this, DatabaseHelper.class);
 	        }
 	        return databaseHelper;
 	    }
 	  }
