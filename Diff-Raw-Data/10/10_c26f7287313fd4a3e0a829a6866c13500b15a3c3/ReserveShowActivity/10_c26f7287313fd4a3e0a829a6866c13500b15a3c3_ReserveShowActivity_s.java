 package com.cinemar.phoneticket;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.cinemar.phoneticket.model.ItemOperation;
 import com.cinemar.phoneticket.model.prices.PriceInfo;
 import com.cinemar.phoneticket.reserveandbuy.OperationConstants;
 import com.cinemar.phoneticket.reserveandbuy.ReserveBuyAPI;
 import com.cinemar.phoneticket.theaters.TheatresClientAPI;
 import com.cinemar.phoneticket.util.AppCommunicator;
 import com.cinemar.phoneticket.util.NotificationUtil;
 
 import com.cinemar.phoneticket.util.ProcessBarUtil;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 public class ReserveShowActivity extends AbstractApiConsumerActivity {
 
 	private String idReserve;
 	private String idShow;
 	private PriceInfo priceInfo;
 	private int quantityOfSeats;
 	private String seats;
 	
 	private TextView mTitle;
 	private TextView mCinema;
 	private TextView mDate;
 	private TextView mSeating;
 	private TextView mCode;
 	private TextView mCongratulations;
 	private String mShareUrl;
 	private Long mSchedulableDate;
 	private boolean mNewOperation;
 	
 	private ProcessBarUtil bar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reserve_show);
 		
 		getUIElement();
 		loadData();
 		
 	}
 
 	public void cancelReserve(View view) {
 		
 		AlertDialog alert = createWindowConfirmation();
 		alert.show();
 	}
 	
 	private void doTheCancellation() {
 		
 		final ReserveShowActivity activity = this;
 		
 		bar.setMessage("Procesando...");
 		bar.showProgress(true);
 
 		ReserveBuyAPI api = new ReserveBuyAPI();
 		
 		Log.i("CANCELAR RESERVA", "Por cancelar " + idReserve );
 
 		api.sendCancelation(idReserve, new JsonHttpResponseHandler() {
 		@Override
 		public void onSuccess(JSONObject object) {
 			NotificationUtil.showSimpleAlert("", "Cancelación exitosa de la reserva", activity, 
 					new DialogInterface.OnClickListener() {			
 						public void onClick(DialogInterface dialog, int which) {
 							activity.finish();
 						}
 					});
 		}
 		
 		@Override
 		public void onFailure(Throwable exception, JSONObject errorResponse) {
 			
 			if (errorResponse != null) {
 				NotificationUtil.showSimpleAlert(getString(R.string.error), 
 						exception.getMessage(), activity);
 			}
 		}
 
 		@Override
 		public void onFailure(Throwable arg0, String arg1) {
 			NotificationUtil.showSimpleAlert(getString(R.string.error), 
 					"No pudo realizarse la cancelación. Intente más tarde.", activity);
 		}
 
 		@Override
 		public void onFinish() {
 			bar.showProgress(false);
 		}
 	});
 
 		Log.i("FIN DE CANCELAR RESERVA", "Por cancelar " + idReserve );
 
 	}
 
 	private void buyReserve() {
 		
 		Intent intent = new Intent(this, SelectTicketsActivity.class);
 		
 		intent.putExtra(OperationConstants.ID_SHOW, idShow);
 		intent.putExtra(OperationConstants.ID_RESERVE, idReserve);
 		intent.putExtra("priceInfo", priceInfo);
 		intent.putExtra("isReserve", false);
 		intent.putExtra("seatsCount", quantityOfSeats);
 		
		if (seats != null)
			intent.putStringArrayListExtra("selectedSeats", new ArrayList<String>(Arrays.asList(seats.split(ItemOperation.SEPARADOR))));
 
 		startActivity(intent);
 		finish();
 	}
 	
 	public void shareWithTwitter(View view) {		
 		AppCommunicator sharer = new AppCommunicator(this);
		Intent shareIntent= sharer.getTwitterIntent("Reservé esta peli", mShareUrl);
 
 		if (shareIntent == null ) {
 
 			NotificationUtil.showSimpleAlert(getString(R.string.no_way_title),getString(R.string.missingApplication),this);
 
 			return;
 		}		 			 
 		startActivity(Intent.createChooser(shareIntent, "Share..."));
 		
 	}
 	
 	public void shareWithFacebook(View view) {
 		
 		AppCommunicator sharer = new AppCommunicator(this);
 		Intent shareIntent= sharer.getFacebookIntent(mShareUrl);
 
 		if (shareIntent == null ) {
 
 			NotificationUtil.showSimpleAlert(getString(R.string.no_way_title),getString(R.string.missingApplication),this);
 
 			return;
 		}		 			 
 		startActivity(Intent.createChooser(shareIntent, "Share..."));
 	}
 	
 	public void schedule(View view){	
 		
 		AppCommunicator sharer = new AppCommunicator(this);
 		String title = mTitle.getText().toString();
 		String description = title + " show";
 		String location = mCinema.getText().toString();
 		
 		Log.i("INICIO DE AGENDAR", "Por agendar " + description );
 
 		boolean success = sharer.scheduleCalendar(title,description,location,mSchedulableDate);
 		if (success)	
 			NotificationUtil.showSimpleAlert(getString(R.string.reminder_success_title), getString(R.string.reminder_success_desc), this);
 		else
 			NotificationUtil.showSimpleAlert(getString(R.string.no_way_title), getString(R.string.missingCalendar), this);
 		
 		Log.i("FIN DE AGENDAR", "Ya ta agendado" + description );
 
 	}
 
 	private AlertDialog createWindowConfirmation() {
 		
 		AlertDialog.Builder builder = NotificationUtil.createDialog("Confirmar", 
 				"¿Está seguro que desea cancelar esta reserva?",
 				this);
 
 		builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				doTheCancellation();
 			}
 
 		});
 
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 
 		return builder.create();
 	}
 	
 	private void loadData() {
 		
 		Intent intent = getIntent();
 		
 		idReserve = intent.getStringExtra(OperationConstants.CODE);
 		idShow = intent.getStringExtra(OperationConstants.ID_SHOW);
 		mTitle.setText(intent.getStringExtra(OperationConstants.TITLE));
 		mCinema.setText(intent.getStringExtra(OperationConstants.CINEMA));
 		mDate.setText(intent.getStringExtra(OperationConstants.DATE));
 		mCode.setText("Cód.: " + idReserve );
 		mShareUrl = intent.getStringExtra(OperationConstants.SHARE_URL);
 		mSchedulableDate = intent.getLongExtra(OperationConstants.SCHEDULABLE_DATE,0);
 		mNewOperation = intent.getBooleanExtra(OperationConstants.NEW_OPERATION, false);
 
 		quantityOfSeats = ItemOperation.getCountSeats(intent.getStringExtra(OperationConstants.SEATING));
 		
 		if (intent.getBooleanExtra(OperationConstants.IS_NUMERED, false) == true) {
 			seats = intent.getStringExtra(OperationConstants.SEATING);
 			mSeating.setText("Asientos: " + seats);
 		} 
 		else {
 			seats = null;
 		}
 	}
 	
 	private void getUIElement() {
 
 		mTitle = (TextView) findViewById(R.id.accountReserveTitle);
 		mCinema = (TextView) findViewById(R.id.accountReserveCinema);
 		mDate = (TextView) findViewById(R.id.accountReserveDate);
 		mSeating = (TextView) findViewById(R.id.accountReserveSeating);
 		mCode = (TextView) findViewById(R.id.accountReserveCode);
 		mCongratulations = (TextView)findViewById(R.id.congratulations);
 		
 		if(!mNewOperation)
 			mCongratulations.setVisibility(View.GONE);
 		
 		bar = new ProcessBarUtil(findViewById(R.id.accountReserveForm),
 				findViewById(R.id.accountReserveBar),
 				(TextView) findViewById(R.id.accountReserveMessageStatus),
 				this);
 		
 	}
 
 	public void getPriceInfo(View view) {
 		
 		TheatresClientAPI api = new TheatresClientAPI();
 		api.getShowSeats(idShow, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject roomInfo) {
 				try {
 					priceInfo = new PriceInfo(roomInfo);
 					buyReserve();
 					
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onFinish() {
 			}
 		});
 	}
 }
