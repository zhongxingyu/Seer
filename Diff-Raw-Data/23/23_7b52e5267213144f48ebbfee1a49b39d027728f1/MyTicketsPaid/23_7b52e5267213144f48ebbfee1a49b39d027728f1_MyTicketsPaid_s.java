 package pt.traincompany.tickets;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import pt.traincompany.main.DatabaseHelper;
 import pt.traincompany.main.R;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Connection;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class MyTicketsPaid extends Activity {
 
 	ProgressDialog dialog;
 	ArrayList<Ticket> userTickets = new ArrayList<Ticket>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_tickets_paid);
 
 		dialog = ProgressDialog.show(MyTicketsPaid.this, "",
 				"A comunicar com a base de dados local...", true);
 		dialog.setCancelable(true);
 
 		GetTicketsByUserId tickets = new GetTicketsByUserId();
 		new Thread(tickets).start();
 
 		final ListView list = (ListView) findViewById(R.id.myTicketsPaid);
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				if (position > 0) {
 					Intent i = new Intent(MyTicketsPaid.this,
 							TicketActivity.class);
 					i.putExtra("ticket",
 							(Ticket) list.getItemAtPosition(position));
 					startActivity(i);
 				}
 			}
 		});
 		
 		View header = (View) getLayoutInflater().inflate(
 				R.layout.ticket_header, null);
 		
 		list.addHeaderView(header);
 
 	}
 	
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		dialog = ProgressDialog.show(MyTicketsPaid.this, "",
 				"A comunicar com a base de dados local...", true);
 		dialog.setCancelable(true);
 
 		GetTicketsByUserId tickets = new GetTicketsByUserId();
 		new Thread(tickets).start();
 
 		final ListView list = (ListView) findViewById(R.id.myTicketsPaid);
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				if (position > 0) {
 					Intent i = new Intent(MyTicketsPaid.this,
 							TicketActivity.class);
 					i.putExtra("ticket",
 							(Ticket) list.getItemAtPosition(position));
 					startActivity(i);
 				}
 			}
 		});
 	}
 
 	class GetTicketsByUserId implements Runnable {
 
 		public void run() {
 			
 			userTickets = new ArrayList<Ticket>();
 			
 			// FILL USER TICKETS FROM DB
 			DatabaseHelper helper = Configurations.databaseHelper;
 			SQLiteDatabase db = helper.getWritableDatabase();
 			Cursor cursor = db
 					.query("Ticket",
 							new String[] { "id, date, departure, arrival, duration, price, departureTime, arrivalTime" },
 							"userId = ?", new String[] {Configurations.userId+""}, null, null, null);
 			if (cursor.moveToFirst()) {
 				do {
 					Ticket t = new Ticket(cursor.getInt(0),
 							cursor.getString(6), cursor.getString(7),
 							cursor.getString(1), cursor.getString(2),
 							cursor.getString(4), cursor.getString(3),
 							cursor.getDouble(5), true);
 					userTickets.add(t);
 				} while (cursor.moveToNext());
 			}
 
 			db.close();
 
 			runOnUiThread(new Runnable() {
 				public void run() {
 					dialog.dismiss();
 					TicketAdapter adapter = new TicketAdapter(
 							MyTicketsPaid.this, R.layout.ticket_row,
 							userTickets.toArray(new Ticket[userTickets.size()]));
 
 					final ListView list = (ListView) findViewById(R.id.myTicketsPaid);
 
 					list.setAdapter(adapter);
 				}
 			});
 
 		}
 
 	}
 
 	public void loadFromServer() {
 		dialog = ProgressDialog.show(MyTicketsPaid.this, "",
 				"A comunicar com o servidor...", true);
 		GetTicketsByUserIdFromServer thread = new GetTicketsByUserIdFromServer();
 		new Thread(thread).start();
 	}
 
 	class GetTicketsByUserIdFromServer implements Runnable {
 
 		public void run() {
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.GETPAIDTICKETSBYID);
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 			uri.appendQueryParameter("user_id", Configurations.userId + "");
 
 			String response = null;
 
 			try {
 
 				response = Connection.getJSONLine(uri.build());
 				JSONArray info = new JSONArray(response);
 				
 				userTickets.clear();
 
 				for (int i = 0; i < info.length(); i++) {
 					JSONObject ticket = info.getJSONObject(i);
 
 					int id = ticket.getInt("id");
 					String departureTime = ticket.getString("departureTime");
 					String arrivalTime = ticket.getString("arrivalTime");
 					String arrival = ticket.getString("arrival");
 					String departure = ticket.getString("departure");
 					String date = ticket.getString("date");
 					String duration = ticket.getString("duration");
 					double price = ticket.getDouble("price");
 					boolean paid = ticket.getBoolean("paid");
 
 					Ticket t = new Ticket(id, departureTime, arrivalTime, date,
 							departure, duration, arrival, price, paid);
 					userTickets.add(t);
 					
 					
 				}
 				
 				SQLiteDatabase db = Configurations.databaseHelper.getWritableDatabase();
				db.execSQL("DELETE FROM Ticket;");
 				
 				for(Ticket ti : userTickets) {
 					
 					ContentValues value = new ContentValues();
 					value.put("id", ti.id);
 					value.put("date", ti.date);
 					value.put("departure", ti.from);
 					value.put("arrival", ti.to);
 					value.put("duration", ti.duration);
 					value.put("price", ti.price);
 					value.put("departureTime", ti.departureTime);
 					value.put("arrivalTime", ti.arrivalTime);
 					value.put("userId", Configurations.userId+"");
 
 					db = Configurations.databaseHelper.getWritableDatabase();
 					db.insert("Ticket", null, value);
 					db.close();
 					
 				}
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						dialog.dismiss();
 						TicketAdapter adapter = new TicketAdapter(
 								MyTicketsPaid.this, R.layout.ticket_row,
 								userTickets.toArray(new Ticket[userTickets
 										.size()]));
 
 						final ListView list = (ListView) findViewById(R.id.myTicketsPaid);
 
 						
 						list.setAdapter(adapter);
 					}
 				});
 
 			} catch (Exception e) {
 				communicationProblem();
 			}
 		}
 
 	}
 	
 	private void communicationProblem() {
 		dialog.dismiss();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(MyTicketsPaid.this,
 						"A comunicação com o servidor falhou...",
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 
 }
