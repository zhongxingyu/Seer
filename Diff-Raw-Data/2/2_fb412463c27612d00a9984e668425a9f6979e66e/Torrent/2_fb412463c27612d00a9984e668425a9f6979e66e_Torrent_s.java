 package lt.asinica.lm.objects;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.List;
 
 import lt.asinica.lm.R;
 import lt.asinica.lm.TorrentDescription;
 import lt.asinica.lm.exceptions.ExternalStorageNotAvaliableException;
 import lt.asinica.lm.exceptions.InvalidTokenException;
 import lt.asinica.lm.exceptions.NotLoggedInException;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Torrent {
 	
 	private Bundle mInfo = new Bundle(15);
 	// class vars
 	/*private String id;
 	private String fileName;
 	private String title;
 	private boolean freeLeech = false;
 	private boolean newTorrent = false;
 	private String descriptionUrl;
 	private String categoryUrl;
 	private String category;
 	private String downloadUrl;
 	private String dateAdded;
 	private String size;
 	private String seeders;
 	private String leechers;
 	private String fullDescription;*/
 	private static String baseUrl = "http://www.linkomanija.net/";
 	// getters
 	public String getId() { return mInfo.getString("id"); }
 	public String getFileName() { return mInfo.getString("fileName"); }
 	public String getTitle() { return mInfo.getString("title"); }
 	public boolean isFreeLeech() { return mInfo.getBoolean("freeLeech"); }
 	public boolean isNew() { return mInfo.getBoolean("new"); }
 	public String getDescriptionUrl() { return baseUrl+mInfo.getString("descriptionUrl"); }
 	public String getCategory() { return mInfo.getString("category"); }
 	public String getCategoryUrl() { return baseUrl+mInfo.getString("categoryUrl"); }
 	public String getDownloadUrl() { return baseUrl+mInfo.getString("downloadUrl"); }
 	public String getDateAdded() { return mInfo.getString("dateAdded"); }
 	public String getSize() { return mInfo.getString("size"); }
 	public String getSeeders() { return mInfo.getString("seeders"); }
 	public String getLeechers() { return mInfo.getString("leechers"); }
 	public String getFullDescription() { return mInfo.getString("fullDescription"); }
 	
 	public Torrent() {
 		
 	}
 	public Torrent(Element row) {
 		parseTableRow(row);
 	}
 	
 	public void parseTableRow(Element row) {
 		try {			
 			Elements cells = row.select("td");
 			
 			// title and link
 			Element titleCell = cells.get(1);
 			Element anchor = titleCell.select("a").first();
 			mInfo.putString("title", anchor.text());
 			mInfo.putString("descriptionUrl", anchor.attributes().get("href"));
 			// downlaod url
 			String downloadUrl = titleCell.select("a.index").first().attributes().get("href");
 			mInfo.putString("downloadUrl", downloadUrl);
 			// id and file name
 			try {
 				URI u = new URI(baseUrl+downloadUrl);
 				List<NameValuePair> pieces = URLEncodedUtils.parse(u, "UTF-8");
 				NameValuePair pair = pieces.get(0);
 				mInfo.putString("id", pair.getValue());
 				pair = pieces.get(1);
 				mInfo.putString("fileName", pair.getValue());
 			} catch (Exception e) {
 				Log.e("DEBUG", "Failed to parse download URL into id and filename strings. "+e.getMessage());
 				e.printStackTrace();
 			}
 			// free leech
 			mInfo.putBoolean("freeLeech", titleCell.select("a:not(.index) img").size()>0 );
 			// new
 			mInfo.putBoolean("new", titleCell.select("font").size()>0 );
 			//<font color="red"
 				
 			// category
 			String categoryUrl = cells.get(0).select("a").first().attributes().get("href");
 			mInfo.putString("categoryUrl", categoryUrl);
 			mInfo.putString("category", categoryUrl.substring(categoryUrl.lastIndexOf("=") + 1));
 			// Date
 			mInfo.putString("dateAdded", cells.get(4).text());
 			// Size
 			mInfo.putString("size", cells.get(5).text());
 			// Seeders
 			mInfo.putString("seeders", cells.get(7).text());
 			// Leechers
 			mInfo.putString("leechers", cells.get(8).text());
 		} catch (NullPointerException e) {
 			Log.e("DEBUG", "Can't parse torrent data to an object. "+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	public void parseTorrentInfo(Document doc) {
     	try {
 	    	Elements rows = doc.select("#content table tr");
 	    	// apraymo tr > antras td
 	    	int row = isFreeLeech() ? 3 : 2;
 	    	Element descrip = rows.get(row).select("td").get(1);
 	    	mInfo.putString("fullDescription", descrip.html());
     	} catch (NullPointerException e) {
     		Log.e("DEBUG", "Can not parse document to torrent info. "+e.getMessage());
     		e.printStackTrace();
     	}		
 	}
 	
 	public void view(Activity context) {
 		Intent intent = new Intent(context, TorrentDescription.class);
 		intent.putExtra("torrent", pack());
 		context.startActivity(intent);
 	}
 	
 	public void open(Activity context) {
 		String str = String.format( context.getString(R.string.lm_file_downloading), getFileName() );
 		final ProgressDialog progressDialog = ProgressDialog.show(context,    
   	          context.getString(R.string.please_wait), str, true);
 		final Activity cntxt = context;
 		
 	    Runnable openTorrents = new Runnable() {
 	        @Override
 	        public void run() {
 	        	String error = null;
 				try {
 					final File torrent = LM.getInstance().downloadFile(Torrent.this);
 					progressDialog.dismiss();
 					
 					final String msg = String.format( cntxt.getString(R.string.lm_file_downloaded_prompt), torrent.getAbsolutePath() );
 					
 					cntxt.runOnUiThread(new Runnable() { public void run() {					
 						new AlertDialog.Builder(cntxt)
 					        .setTitle(R.string.lm_file_downloaded)
 					        .setMessage(msg)
 					        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 					            public void onClick(DialogInterface dialog, int which) {
 									Intent intent = new Intent();
 						    		intent.setAction(android.content.Intent.ACTION_VIEW);
 						    		intent.setDataAndType(Uri.fromFile(torrent), "application/x-bittorrent");
 						    		try {
 						    			cntxt.startActivity(intent);
 						    		} catch(ActivityNotFoundException e) {
 						    			Toast.makeText(cntxt, R.string.no_application_associated, Toast.LENGTH_LONG).show();
 						    			Log.e("DEBUG", "Activity associated with this file type not found");
 						    		}
 					            }
 					        })
 					        .setNegativeButton(R.string.no, null)
 					        .show();
 					}});
 				} catch (MalformedURLException e) {
 					String str = "While opening torrent MalformedURLException. "+e.getMessage();
 					Log.e("DEBUG", str);
 					error = str;
 					e.printStackTrace();
 				} catch (ExternalStorageNotAvaliableException e) {
 					String str = cntxt.getString(R.string.sd_card_not_avaliable)+" "+e.getMessage();
 					Log.e("DEBUG", "External storage not avaliable. "+e.getMessage());
 					error = str;
 					e.printStackTrace();
 				} catch (NotLoggedInException e) {
 					error = cntxt.getString(R.string.lm_not_logged_in);
 					e.printStackTrace();
 				} catch (IOException e) {
 					error = cntxt.getString(R.string.lm_no_connectivity)+" "+e.getMessage();
 					e.printStackTrace();
 				} finally {
 					progressDialog.dismiss();
 				}
 				if(error!=null) {
 					Log.e("DEBUG", error);
 					final String err = error;
 					cntxt.runOnUiThread(new Runnable() {
 						public void run() {
 							Toast.makeText(cntxt, err, Toast.LENGTH_LONG).show();
 						}
 					});
 				}
 	        }
 	    };
 	    
 	    Thread thread =  new Thread(null, openTorrents, "Torrent Opener Thread");
 	    thread.start();
 	}
 	public void sendToUTorrent(Activity context) {
 		String str = String.format( context.getString(R.string.ut_send_in_progress), getFileName() );
 		final ProgressDialog progressDialog = ProgressDialog.show(context,    
 	  	          context.getString(R.string.please_wait), str, true);
 		final Activity cntxt = context;
 		
 	    Runnable sendTorrents = new Runnable() {
 	        @Override
 	        public void run() {
 	        	String msg = null;
 				try {
 					UTorrent ut = UTorrent.getInstance();
 					String cookie = "login="+LM.getInstance().getSecret();
 					
 					ut.addTorrent(getDownloadUrl(), cookie);
 					msg = String.format( cntxt.getString(R.string.ut_send_success), ut.getLabel() );
 				} catch (InvalidTokenException e) {
 					msg = cntxt.getString(R.string.ut_unexpected_response);
 					e.printStackTrace();
 				} catch (NotLoggedInException e) {
 					msg = cntxt.getString(R.string.lm_not_logged_in);
 					e.printStackTrace();
				} catch (IOException e) {
 					msg = cntxt.getString(R.string.ut_cant_connect) + " " + e.getMessage();
 					e.printStackTrace();
 				} finally {
 					progressDialog.dismiss();
 				}
 				if(msg!=null) {
 					Log.i("DEBUG", msg);
 					final String m = msg;
 					cntxt.runOnUiThread(new Runnable() { public void run() {
 						Toast.makeText(cntxt, m, Toast.LENGTH_LONG).show();
 					}});
 				}
 	        }
 	    };
 	    
 	    Thread thread =  new Thread(null, sendTorrents, "Torrent Sender Thread");
 	    thread.start();		
 	}
 	
 	public Bundle pack() {
 		return mInfo;
 	}
 	
 	public void unpack(Bundle pack) {
 		mInfo = pack;
 	}
 }
