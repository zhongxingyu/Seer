 package com.cinemar.phoneticket;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.cinemar.phoneticket.films.SeatOnClickListener;
 import com.cinemar.phoneticket.model.ItemOperation;
 import com.cinemar.phoneticket.model.Room;
 import com.cinemar.phoneticket.model.Room.Seat;
 import com.cinemar.phoneticket.model.SeatStatus;
 import com.cinemar.phoneticket.model.prices.PriceInfo;
 import com.cinemar.phoneticket.reserveandbuy.OperationConstants;
 import com.cinemar.phoneticket.reserveandbuy.ReserveBuyAPI;
 import com.cinemar.phoneticket.reserveandbuy.ReserveRequest;
 import com.cinemar.phoneticket.reserveandbuy.ReserveResponseHandler;
 import com.cinemar.phoneticket.reserveandbuy.ReserveResponseHandler.Fields;
 import com.cinemar.phoneticket.reserveandbuy.ReserveResponseHandler.PerformReserveListener;
 import com.cinemar.phoneticket.theaters.TheatresClientAPI;
 import com.cinemar.phoneticket.util.NotificationUtil;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class SelectSeatsActivity extends AbstractApiConsumerActivity implements
 		PerformReserveListener {
 
 	private static final int SELECT_TICKETS_TRANSACTION = 0;
 	private String showId;
 	private boolean isReserve;
 	private Room showRoom;
 	private PriceInfo priceInfo;
 	private TableLayout cinemaLayout;
 	private Map<String, ImageView> seatsImages;
 	private final LinkedList<Seat> SelectedSeats = new LinkedList<Seat>();
 	private int maxSeatsToTake = Integer.MAX_VALUE;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_select_seats);
 
 		setTitle(getIntent().getStringExtra("title"));
 
 		// ** Important to get in order to use the showProgress method**//
 		mMainView = findViewById(R.id.salaView);
 		mStatusView = findViewById(R.id.sala_status);
 		mStatusMessageView = (TextView) findViewById(R.id.sala_status_message);
 
 		showId = getIntent().getStringExtra("showId");
 		isReserve = getIntent().getBooleanExtra("isReserve", false);
 		if (getIntent().getStringExtra("maxSelections") != null)
 			// supongamos que viene dicho de afuera si hay una pantalla extra
 			// donde se dice que cantidad de butacas se va a comprar
 			maxSeatsToTake = Integer.parseInt(getIntent().getStringExtra(
 					"maxSelections"));
 
 		cinemaLayout = (TableLayout) findViewById(R.id.cinemalayout);
 		seatsImages = new HashMap<String, ImageView>();
 
 		this.requestRoomLayout();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.select_seats, menu);
 		return true;
 	}
 
 	private void requestRoomLayout() {
 		mStatusMessageView.setText(R.string.getting_sala);
 		showProgress(true);
 
 		TheatresClientAPI api = new TheatresClientAPI();
 		api.getShowSeats(showId, new JsonHttpResponseHandler() {
 
 			@Override
 			public void onSuccess(JSONObject roomInfo) {
 				Log.i("SelectSeats Activity", "Complejos Recibidos");
 				try {
 					showRoom = new Room(roomInfo);
 					priceInfo = new PriceInfo(roomInfo); // just to validate
 															// parsing at this
 															// moment
 					Log.i("SelectSeats Activity", "Seats" + roomInfo
 							+ "recibido");
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				displaySeats();
 			}
 
 			@Override
 			public void onFailure(Throwable e, JSONObject errorResponse) {
 				Log.i("SelectSeats Activity", "Failure recibiendo Seats");
 				if (errorResponse != null) {
 					showSimpleAlert(errorResponse.optString("error"));
 				} else {
 					showSimpleAlert(e.getMessage());
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable arg0, String arg1) {
 				showSimpleAlert(getString(R.string.error_connection));
 			};
 
 			@Override
 			public void onFinish() {
 				showProgress(false);
 			}
 
 		});
 
 	}
 
 	private void displaySeats() {
 		ImageView seatView;
 
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT);
 		params.setMargins(10, 15, 10, 15);
 		params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
 		int lastLeftColumn = showRoom.getLeftWidth();
 		int lastMiddleColumn = showRoom.getMiddleWidth()
 				+ showRoom.getLeftWidth();
 
 		for (Integer i = 0; i < showRoom.getRowsLength(); i++) {
 
 			TableRow fila = new TableRow(this);
 			fila.setPadding(15, 10, 15, 10);
 			fila.setLayoutParams(params);
 			cinemaLayout.addView(fila);
 
 			for (Integer j = 0; j < showRoom.getColumnsLength(); j++) {
 				Seat seatModel = showRoom.getSeat(i, j);
 
 				seatView = new ImageView(this);
 				seatView.setPadding(2, 0, 2, 0);
 				if ((j + 1 == lastLeftColumn) || (j + 1 == lastMiddleColumn)) {
 					seatView.setPadding(2, 0, 40, 0);
 				}
 
 				seatsImages.put(seatModel.getId(), seatView);
 
 				if (seatModel.getStatus().equals(SeatStatus.NON_EXISTENT)) {
 					// podria ser cualquier otro(es solo para que ocupe el lugar
 					// vacio)
 					seatView.setImageResource(R.drawable.seat_available);
 					seatView.setVisibility(View.INVISIBLE); // la hago invisible
 															// aunque quizas no
 															// sea lo mejor
 				}
 
 				else if (seatModel.getStatus().equals(SeatStatus.AVAILABLE)) {
 					seatView.setImageResource(R.drawable.seat_available);
 					seatView.setOnClickListener(new SeatOnClickListener(
 							seatModel, seatsImages, true, SelectedSeats,
 							maxSeatsToTake));
 				} else if (seatModel.getStatus().equals(SeatStatus.OCCUPIED)) {
 					seatView.setImageResource(R.drawable.seat_occupied);
 					seatView.setOnClickListener(new SeatOnClickListener(
 							seatModel, seatsImages, false, SelectedSeats,
 							maxSeatsToTake));
 				}
 
 				fila.addView(seatView);
 			}
 
 		}
 
 	}
 
 	public void readySelectingSeats(View view) {
 		if (SelectedSeats.isEmpty()) {
 			showSimpleAlert(getString(R.string.no_seats_selected));
 			return;
 		}
 
 		ArrayList<String> seatsIds = new ArrayList<String>();
 		for (Seat seat : SelectedSeats) {
 			seatsIds.add(seat.getId());
 		}
 
 		// Hago la reserva de dichos asientos (no hay nada mas que cargar)
 		if (isReserve) {
 			ReserveRequest reserve = new ReserveRequest();
 			SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
 			reserve.setEmail(settings.getString("email", null));
 			reserve.setShowId(showId);
 			reserve.setSeats(seatsIds);
 
 			ReserveBuyAPI api = new ReserveBuyAPI();
 			ReserveResponseHandler reserveResponseHandler = new ReserveResponseHandler(this);
 			api.performNumberedReserve(this,reserve, reserveResponseHandler);
 		} else {
 			// Caso de compra (faltan ingresar varios datos)
 			Intent intent = new Intent(this, SelectTicketsActivity.class);
 			intent.putExtra("showId", showId);
 			intent.putExtra("priceInfo", priceInfo);
 			intent.putExtra("isReserve", isReserve);
 
 			intent.putStringArrayListExtra("selectedSeats", seatsIds);
 
 			startActivityForResult(intent, SELECT_TICKETS_TRANSACTION);
 		}
 	}
 
 	public void onReserveOk(String msg,JSONObject result) {
 
 		setResult(PeliculasFuncionActivity.TRANSACTION_OK);
 
 		Intent intent = new Intent(this, ReserveShowActivity.class);
 		ItemOperation item ;
 		try {
 			item = new ItemOperation(result);
 			intent.putExtra(OperationConstants.TITLE, item.getTitle());
 			intent.putExtra(OperationConstants.CINEMA, item.getCinema());
 			intent.putExtra(OperationConstants.DATE, item.getDateToString());
 			intent.putExtra(OperationConstants.SEATING, item.getSeatingToString());
 			intent.putExtra(OperationConstants.TICKETS_TYPE, item.getTicketsType());
 			intent.putExtra(OperationConstants.CODE, item.getId()); // para las reservas el id es el cod.
 			intent.putExtra(OperationConstants.SHARE_URL, item.getShareUrl());
 			intent.putExtra(OperationConstants.SCHEDULABLE_DATE, item.getDate().getTime());
 			intent.putExtra(OperationConstants.NEW_OPERATION, true);
 
 			final Intent intentFinal = intent;
 
 			NotificationUtil.showSimpleAlert("", msg, this, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) { //para que espere a que el usuario toque la pantalla, sino salta un error en la consola
 					startActivity(intentFinal);
 					finish();
 				}
 			});
 
 		} catch (JSONException e) {
 			this.showSimpleAlert("Error parseando compra respuesta");
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == SELECT_TICKETS_TRANSACTION) {
 			switch (resultCode) {
 			case SelectTicketsActivity.TRANSACTION_OK:
 				setResult(SelectTicketsActivity.TRANSACTION_OK);
 				finish();
 				break;
 
 			case SelectTicketsActivity.TRANSACTION_SEATS_PROBLEM:
 				showSimpleAlert("Los asientos seleccionados ya no se encuentran disponibles.\nPor favor, seleccione nuevamente.");
 				cinemaLayout.removeAllViews();
 				requestRoomLayout();
 				break;
 			case SelectTicketsActivity.TRANSACTION_SHOW_PROBLEM:
 				setResult(SelectTicketsActivity.TRANSACTION_SHOW_PROBLEM);
 				finish();
 				break;
 
 			default:
 				break;
 			}
 		}
 	}
 
 	public void onErrorWhenReserving(String msg) {
 		showSimpleAlert(msg);
 
 	}
 
 	public void onValidationError(Fields field, String error) {
 		switch (field) {
 		case seats:
 			showSimpleAlert("Los asientos seleccionados ya no se encuentran disponibles.\nPor favor, seleccione nuevamente.");
 			cinemaLayout.removeAllViews();
 			requestRoomLayout();
 			break;
 
 		case seats_count:
 			// Shouldn't happend (here we only select seats and not seats_count)
 			showSimpleAlert("No quedan suficiente cantidad de asientos.\nPor favor, seleccione una cantidad menor.");
 			break;
 
 		case show_id:
 			showSimpleAlert(error, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					setResult(SelectTicketsActivity.TRANSACTION_SHOW_PROBLEM);
 					finish();
 				}
 			});
 			break;
 
 		default:
 			break;
 		}
 	}
 
 }
