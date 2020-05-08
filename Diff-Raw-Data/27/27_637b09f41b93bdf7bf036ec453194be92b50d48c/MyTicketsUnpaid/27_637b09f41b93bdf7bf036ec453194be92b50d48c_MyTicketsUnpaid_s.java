 package pt.traincompany.tickets;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import pt.traincompany.main.R;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Connection;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MyTicketsUnpaid extends Activity {
 
 	ProgressDialog dialog;
 	ArrayList<Ticket> userTickets = new ArrayList<Ticket>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_tickets_unpaid);
 
 		dialog = ProgressDialog.show(MyTicketsUnpaid.this, "",
 				"A comunicar com o servidor...", true);
 		dialog.setCancelable(true);
 
 		GetTicketsByUserId tickets = new GetTicketsByUserId();
 		new Thread(tickets).start();
 
 		final ListView list = (ListView) findViewById(R.id.myTicketsUnpaid);
 
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				if (position > 0) {
 					Intent i = new Intent(MyTicketsUnpaid.this,
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
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.GETTICKETSBYID);
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 			uri.appendQueryParameter("user_id", Configurations.userId + "");
 
 			String response = null;
 
 			try {
 
 				response = Connection.getJSONLine(uri.build());
 				JSONArray info = new JSONArray(response);
 
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
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						dialog.dismiss();
 						TicketAdapter adapter = new TicketAdapter(
 								MyTicketsUnpaid.this, R.layout.ticket_row,
 								userTickets.toArray(new Ticket[userTickets
 										.size()]));
 
 						final ListView list = (ListView) findViewById(R.id.myTicketsUnpaid);
 
						View header = (View) getLayoutInflater().inflate(
								R.layout.ticket_header, null);

						list.addHeaderView(header);
 						list.setAdapter(adapter);
 					}
 				});
 
 			} catch (Exception e) {
 				communicationProblem();
 				MyTicketsUnpaid.this.finish();
 			}
 		}
 
 	}
 
 	private void communicationProblem() {
 		dialog.dismiss();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(MyTicketsUnpaid.this,
 						"A comunicação com o servidor falhou...",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 }
