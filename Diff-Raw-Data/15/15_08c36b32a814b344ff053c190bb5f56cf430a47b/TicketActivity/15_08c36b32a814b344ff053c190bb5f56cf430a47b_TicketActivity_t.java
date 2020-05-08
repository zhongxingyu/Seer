 package pt.traincompany.tickets;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import pt.traincompany.account.Card;
 import pt.traincompany.account.CardAdapter;
 import pt.traincompany.main.Home;
 import pt.traincompany.main.R;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Connection;
 import pt.traincompany.utility.Utility;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TicketActivity extends Activity {
 
 	Ticket ticket;
 	Bitmap bitmap;
 	ProgressDialog dialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ticket);
 
 		Ticket t = (Ticket) getIntent().getExtras().get("ticket");
 		this.ticket = t;
 
 		TextView id = (TextView) findViewById(R.id.id);
 		id.setText(ticket.id + "");
 
 		TextView departure = (TextView) findViewById(R.id.departureTime);
 		departure.setText(ticket.departureTime);
 
 		TextView arrival = (TextView) findViewById(R.id.arrivalTime);
 		arrival.setText(ticket.arrivalTime);
 
 		TextView data = (TextView) findViewById(R.id.date);
 		data.setText(ticket.date);
 
 		TextView from = (TextView) findViewById(R.id.from);
 		from.setText(ticket.from);
 
 		TextView to = (TextView) findViewById(R.id.to);
 		to.setText(ticket.to);
 
 		TextView price = (TextView) findViewById(R.id.price);
 		price.setText(ticket.price + " €");
 
 		TextView duration = (TextView) findViewById(R.id.duration);
 		duration.setText(ticket.duration);
 
 		ImageView paid = (ImageView) findViewById(R.id.paid);
 		if (ticket.paid) {
 			Button cancel = (Button) findViewById(R.id.btnDeleteTicket);
 			cancel.setVisibility(View.INVISIBLE);
 			Button pay = (Button) findViewById(R.id.btnPayTicket);
 			pay.setVisibility(View.INVISIBLE);
 			paid.setImageResource(R.drawable.pago);
 		} else
 			paid.setImageResource(R.drawable.delete);
 
 		dialog = ProgressDialog.show(TicketActivity.this, "",
 				"A gerar identificador...", true);
 		dialog.setCancelable(true);
 
 		new Thread(new Runnable() {
 			public void run() {
 
 				URL newurl;
 				try {
 					newurl = new URL(Configurations.QRCODE + ticket.id);
 					bitmap = BitmapFactory.decodeStream(newurl.openConnection()
 							.getInputStream());
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 					dialog.dismiss();
 					Toast.makeText(TicketActivity.this,
 							"Ocorreu um erro ao gerar o QR Code...",
 							Toast.LENGTH_SHORT).show();
 				} catch (IOException e) {
 					e.printStackTrace();
 					dialog.dismiss();
 					runOnUiThread(new Runnable() {
 						public void run() {
 							Toast.makeText(TicketActivity.this,
 									"Ocorreu um erro ao gerar o QR Code...",
 									Toast.LENGTH_SHORT).show();
 						}
 					});
 					
 				}
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						ImageView qrCode = (ImageView) findViewById(R.id.qrCode);
 						qrCode.setImageBitmap(bitmap);
 						dialog.dismiss();
 					}
 				});
 			}
 		}).start();
 
 		Button cancel = (Button) findViewById(R.id.btnDeleteTicket);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						TicketActivity.this);
 				builder.setMessage(
 						"Tem a certeza que pretende cancelar o bilhete?")
 						.setTitle("Cancelar bilhete")
 						.setPositiveButton("Sim",
 								new DialogInterface.OnClickListener() {
 									public void onClick(
 											DialogInterface dialog2, int id2) {
 										dialog = ProgressDialog
 												.show(TicketActivity.this,
 														"",
 														"A cancelar o bilhete...",
 														true);
 										dialog.setCancelable(true);
 										CancelTicket search = new CancelTicket();
 										new Thread(search).start();
 									}
 								}).setNegativeButton("Não", null);
 				builder.show();
 			}
 		});
 
 		Button pay = (Button) findViewById(R.id.btnPayTicket);
 		pay.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				dialog = ProgressDialog.show(TicketActivity.this, "",
 						"A comunicar com o servidor...", true);
 				dialog.setCancelable(true);
 				GetCardsByUserId get = new GetCardsByUserId();
 				new Thread(get).start();
 			}
 		});
 
 	}
 
 	class CancelTicket implements Runnable {
 
 		public void run() {
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.CANCELTICKET)
 					.appendQueryParameter("id", ticket.id + "").build();
 
 			try {
 				Connection.getJSONLine(uri.build());
 				makeToast("Bilhete cancelado com sucesso...");
 				Utility.from_cancel_ticket = true;
 				Intent myIntent = new Intent(TicketActivity.this, Home.class);
 				TicketActivity.this.startActivity(myIntent);
 			} catch (Exception e) {
 				communicationProblem();
 			}
 		}
 
 	}
 
 	private void communicationProblem() {
 		if (dialog != null)
 			dialog.dismiss();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(TicketActivity.this,
 						"A comunicação com o servidor falhou...",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	private void makeToast(final String message) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				if (dialog != null)
 					dialog.dismiss();
 				Toast.makeText(TicketActivity.this, message, Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 	}
 
 	public class GetCardsByUserId implements Runnable {
 
 		public void run() {
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.GETCARDSBYID);
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 			uri.appendQueryParameter("userId", Configurations.userId + "");
 
 			String response = null;
 
 			try {
 				response = Connection.getJSONLine(uri.build());
 
 				JSONArray info = new JSONArray(response);
 
 				for (int i = 0; i < info.length(); i++) {
 					JSONObject card = info.getJSONObject(i);
 					String number = card.getString("number");
 					int id = card.getInt("id");
 					Card c = new Card(id, number);
 					Utility.user_cards = new ArrayList<Card>();
 					Utility.user_cards.add(c);
 				}
				
				if(Utility.user_cards.size() == 0) {
					makeToast("Adicione primeiro um cartão...");
					return;
				}
 
 				Configurations.cardsLoaded = true;
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						dialog.dismiss();
 						CardAdapter adapter = new CardAdapter(
 								TicketActivity.this, R.layout.creditcard_row2,
 								R.drawable.ic_launcher,
 								Utility.user_cards
 										.toArray(new Card[Utility.user_cards
 												.size()]));
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								TicketActivity.this);
 						builder.setTitle("Escolha um cartão").setAdapter(
 								adapter, new DialogInterface.OnClickListener() {
 									public void onClick(
 											DialogInterface dialog2, int which) {
 										dialog = ProgressDialog.show(
 												TicketActivity.this, "",
 												"A pagar o bilhete...", true);
 										PayTicket pt = new PayTicket();
 										new Thread(pt).start();
 									}
 								});
 						builder.show();
 
 					}
 				});
 
 			} catch (Exception e) {
 				communicationProblem();
 			}
 		}
 
 	}
 
 	public class PayTicket implements Runnable {
 
 		public void run() {
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.PAYTICKET);
 			uri.appendQueryParameter("id", ticket.id + "");
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 
 			String response = null;
 
 			try {
 				response = Connection.getJSONLine(uri.build());
 				JSONArray info = new JSONArray(response);
 				if (info.getString(0).equals("paid")) {
 					ticket.paid = true;
 					runOnUiThread(new Runnable() {
 						public void run() {
 							ImageView paid = (ImageView) findViewById(R.id.paid);
 							paid.setImageResource(R.drawable.pago);
 
 							Button cancel = (Button) findViewById(R.id.btnDeleteTicket);
 							cancel.setVisibility(View.INVISIBLE);
 
 							Button pay = (Button) findViewById(R.id.btnPayTicket);
 							pay.setVisibility(View.INVISIBLE);
 
 							// save ticket to paid list (db)
 							ContentValues value = new ContentValues();
 							value.put("id", ticket.id);
 							value.put("date", ticket.date);
 							value.put("departure", ticket.from);
 							value.put("arrival", ticket.to);
 							value.put("duration", ticket.duration);
 							value.put("price", ticket.price);
 							value.put("departureTime", ticket.departureTime);
 							value.put("arrivalTime", ticket.arrivalTime);
 
 							SQLiteDatabase db = Configurations.databaseHelper.getWritableDatabase();
 							db.insert("Ticket", null, value);
 							db.close();
 
 							makeToast("Bilhete pago com sucesso...");
 
 						}
 					});
 
 				} else
 					makeToast("O pagamento foi recusado...");
 			} catch (Exception e) {
 				makeToast("Occorreu um erro com a operação...");
 			}
 		}
 	}
 }
