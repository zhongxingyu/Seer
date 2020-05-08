 package com.mikedg.android.sudoku.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
import android.util.Log;
 
 import com.mikedg.android.sudoku.db.SudokuDatabase;
 
 import cz.romario.opensudoku.db.SudokuImportParams;
 import cz.romario.opensudoku.db.SudokuInvalidFormatException;
 import cz.romario.opensudoku.game.SudokuGame;
 
 public class SudokuService extends Service {
 
 	private static boolean isDownloading = false;
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static boolean isDailyDownloading() {
 		return isDownloading;
 	}
 	
 	public synchronized static void startDailyDownload(Context context) {
 		if (!isDownloading) {
 			isDownloading = true;
 			Intent intent = new Intent(context, SudokuService.class);
 			context.startService(intent);
 		}
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 
 		//TODO: on download finish set isDownloading = false
 		//dgsudoku.appspot.com
 		
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 				//FIXME pull the server side stuff first...
 //				String sudokuBoardX = "028409000004650200000000098243001070700000005050300942860000000001063700000705680"; //Tester
 				Vector<String> games = new Vector<String>();
 //				games.add(sudokuBoardX);
 //				games.add(sudokuBoardX);
 				
 				InputStream is = null;
 				BufferedReader buffer = null;
 				try {
 					HttpGet get = new HttpGet("http://dgsudoku.appspot.com/dgsudokuweb"); //Test server
 //					HttpGet get = new HttpGet("http://10.0.2.2:8888/dgsudokuweb"); //Test server
 				
 					DefaultHttpClient httpClient = new DefaultHttpClient();
 					HttpResponse response = httpClient.execute(get);
 					HttpEntity entity = response.getEntity();
 					is = entity.getContent();
 					buffer = new BufferedReader(new InputStreamReader(is));
 					String str = buffer.readLine();
					while (str != null) {
						Log.d("Sudoku", "game: " + str);
 						games.add(str);
 						str = buffer.readLine();
 					}
 					
 				} catch (Exception ex) {
 					ex.printStackTrace();
 
 					return;
 				} finally {
 
 					if (buffer != null) {
 						try {
 
 							buffer.close();
 						} catch (IOException ex) {
 							// eat it
 						}
 					}
 					if (is != null) {
 						try {
 							is.close();
 						} catch (IOException ex) {
 							// eat it
 						}
 					}
 				}
 					
 				SudokuImportParams params = new SudokuImportParams();
 				SudokuDatabase database;
 				database = new SudokuDatabase(SudokuService.this.getApplicationContext());
 				params.state = SudokuGame.GAME_STATE_NOT_STARTED;
 				try {
 					database.beginTransaction();
 	
 					for (String data: games) {
 						params.data = data;
						Log.d("Sudoku", "game import: " + data);
						if (!database.doesGameExist(params)) {
							Log.d("Sudoku", "!exists" );

 							database.importSudoku(4, params);
 						}
 					}
 					database.setTransactionSuccessful();
 				} catch ( SudokuInvalidFormatException e) {
					e.printStackTrace();
 				} finally {
 					database.endTransaction();
 					database.close();
 				}
 				} finally {
 					isDownloading = false;
 					Intent intent = new Intent(ServiceStatusReceiver.BROADCAST_STATUS);
 //					intent.putExtra(HungService.EXTRA_METHOD, HungService.METHOD_CLAIM);
 					SudokuService.this.getApplicationContext().sendBroadcast(intent);
 				}
 			}
 			
 		}).start();
 	}
 }
