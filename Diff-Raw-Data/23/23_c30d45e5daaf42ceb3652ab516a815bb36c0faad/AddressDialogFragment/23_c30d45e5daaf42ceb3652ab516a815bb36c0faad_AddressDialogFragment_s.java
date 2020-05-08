 package com.whatstodo.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.whatstodo.R;
 import com.whatstodo.utils.LocationService;
 import com.whatstodo.utils.LocationUtils;
 
 public class AddressDialogFragment extends DialogFragment implements
 		OnClickListener {
 
 	private String address;
 	private EditText addressView;
 
 	/*
 	 * The activity that creates an instance of this dialog fragment must
 	 * implement this interface in order to receive event callbacks. Each method
 	 * passes the DialogFragment in case the host needs to query it.
 	 */
 	public interface NoticeDialogListener {
 		public void onAddressDialogPositiveClick(AddressDialogFragment dialog);
 
 		public void onAddressDialogNegativeClick(AddressDialogFragment dialog);
 
 	}
 
 	// Use this instance of the interface to deliver action events
 	NoticeDialogListener mListener;
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 
 		// Start location service
 		getActivity().startService(
 				new Intent(getActivity(), LocationService.class));
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 
 		View view = inflater.inflate(R.layout.dialog_address, null);
 		addressView = (EditText) view.findViewById(R.id.editAddress);
 		addressView.setText(address);
 		Button getLocation = (Button) view
 				.findViewById(R.id.addressGetLocation);
 		getLocation.setOnClickListener(this);
 
 		Button navigate = (Button) view.findViewById(R.id.addressNavigate);
 		navigate.setOnClickListener(this);
 
 		builder.setView(view);
 
 		// TODO Strings
 		builder.setTitle("Wähle Adresse");
 		builder.setPositiveButton("Speichern",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						mListener
 								.onAddressDialogPositiveClick(AddressDialogFragment.this);
 					}
 				});
 		builder.setNegativeButton(R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						mListener
 								.onAddressDialogNegativeClick(AddressDialogFragment.this);
 					}
 				});
 
 		return builder.create();
 	}
 
 	// Override the Fragment.onAttach() method to instantiate the
 	// NoticeDialogListener
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// Verify that the host activity implements the callback interface
 		try {
 			// Instantiate the NoticeDialogListener so we can send events to the
 			// host
 			mListener = (NoticeDialogListener) activity;
 		} catch (ClassCastException e) {
 			// The activity doesn't implement the interface, throw exception
 			throw new ClassCastException(activity.toString()
 					+ " must implement NoticeDialogListener");
 		}
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.addressGetLocation:
 			getLocation();
 			break;
 
 		case R.id.addressNavigate:
 			Intent i = new Intent(Intent.ACTION_VIEW,
 					Uri.parse("google.navigation:q="
 							+ addressView.getText().toString()));
 			startActivity(i);
 			break;
 		}
 	}
 
 	public void getLocation() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
 		if (LocationService.mostRecentLocation != null) {
 
 			final String[] addresses = LocationUtils
 					.addressFromLocation(LocationService.mostRecentLocation);
 
 			builder.setTitle("Wähle Adresse");
 			builder.setSingleChoiceItems(addresses, 0,
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 						}
 					});
 			builder.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								int checkedPosition = ((AlertDialog) dialog)
 										.getListView().getCheckedItemPosition();
 								addressView.setText(addresses[checkedPosition]);
 								dialog.dismiss();
 							}
 						}
 					});
 
 			builder.setNegativeButton("Abbruch",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								dialog.dismiss();
 							}
 						}
 					});
 			
 			builder.setNeutralButton("Neue Ortung", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which == DialogInterface.BUTTON_NEUTRAL) {
 						dialog.dismiss();
 
 						final ProgressDialog progressDialog = new ProgressDialog(getActivity());
 						progressDialog.setTitle("Position wird ermittelt");
 						progressDialog.setMessage("Aktivieren sie GPS und WLAN für eine genauere Ortung.");
 						progressDialog.show();
 	
 						final Handler handler = new Handler();
 						handler.postDelayed(new Runnable() {
 						  @Override
 						  public void run() {
 							  progressDialog.dismiss();
 							  getLocation();
 						  }
 						}, 3000);
 					}
 				}
 			});
 			
 		} else {
 			builder.setMessage("Ihre momentane Position konnte leider nicht bestimmt werden.");
 			builder.setPositiveButton("Neue Ortung",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								dialog.dismiss();
 								final ProgressDialog progressDialog = new ProgressDialog(getActivity());
 								progressDialog.setTitle("Position wird ermittelt");
 								progressDialog.setMessage("Aktivieren sie GPS und WLAN für eine genauere Ortung.");
 								progressDialog.show();
 								
 								final Handler handler = new Handler();
 								handler.postDelayed(new Runnable() {
 								  @Override
 								  public void run() {
 									  progressDialog.dismiss();
 									  getLocation();
 								  }
 								}, 3000);
 							}
 						}
 					});
 
 			builder.setNegativeButton("Abbrechen",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (which == DialogInterface.BUTTON_POSITIVE) {
 								dialog.dismiss();
 							}
 						}
 					});
 		}
 		builder.create().show();
 	}
 
 	@Override
 	public void onDestroy() {
 		getActivity().stopService(
 				new Intent(getActivity(), LocationService.class));
 		super.onDestroy();
 	}
 
 	public String getAddress() {
 		return addressView.getText().toString();
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 }
