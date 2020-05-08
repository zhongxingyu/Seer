 package sk.gista.medobs.view;
 
 import java.util.List;
 
 import sk.gista.medobs.R;
 import sk.gista.medobs.Reservation;
 import sk.gista.medobs.Reservation.Status;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 
 public class ReservationAdapter extends ArrayAdapter<Reservation>{
 
 	private static final int[] colors = {
 		0, // empty
 		Color.parseColor("#939DAC"), //disabled
 		//Color.parseColor("#80B3FF"), //enabled
 		Color.parseColor("#FFFFFF"), //enabled
 		Color.parseColor("#71C837"), //booked
 		Color.parseColor("#C83737")  //in held
 	};
 	private LayoutInflater inflater;
 	
 	public ReservationAdapter(Context context, List<Reservation> reservations) {
 		super(context, R.layout.reservation_item, reservations);
 		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (view == null) {
 			if (getItemViewType(position) == 1) {
 				view = inflater.inflate(R.layout.reservation_booked_item, parent, false);
 			} else {
 				view = inflater.inflate(R.layout.reservation_item, parent, false);
 			}
 		}
 		Reservation reservation = getItem(position);
 		String bookedDetail = null;
 		if (reservation.getBookedBy().length() > 0 || reservation.getBookedAt().length() > 0) {
 			bookedDetail = reservation.getBookedBy() + " " + reservation.getBookedAt();
 		}
 		TextView patientText = (TextView) view.findViewById(R.id.patient);
 		TextView timeText = (TextView) view.findViewById(R.id.time_text);
 		timeText.setText(reservation.getTime());
 		view.setBackgroundColor(colors[reservation.getStatus().numCode]);
 		
 		if (getItemViewType(position) == 1) {
			TextView examKindView = (TextView) view.findViewById(R.id.examination_kind);
 			TextView phoneView = (TextView) view.findViewById(R.id.patient_phone);
 			TextView emailView = (TextView) view.findViewById(R.id.patient_email);
 			TextView bookedView = (TextView) view.findViewById(R.id.booked);
 			phoneView.setText("Phone: "+reservation.getPatientPhoneNumber());
 			if (reservation.getPatientEmail().length() > 0) {
 				emailView.setText("Email: "+reservation.getPatientEmail());
 				emailView.setVisibility(View.VISIBLE);
 			} else {
 				emailView.setVisibility(View.GONE);
 			}
 			if (bookedDetail != null) {
 				bookedView.setText("["+bookedDetail+"]");
 				bookedView.setVisibility(View.VISIBLE);
 			} else {
 				emailView.setVisibility(View.GONE);
 			}
			if (reservation.getExaminationKind().length() > 0) {
				examKindView.setText(reservation.getExaminationKind());
			} else {
				examKindView.setVisibility(View.GONE);
			}
 			patientText.setText(reservation.getpatient());
 		} else {
 			if (bookedDetail != null) {
 				patientText.setText("["+bookedDetail+"]");
 			} else {
 				patientText.setText(reservation.getpatient());
 			}
 		}
 		return view;
 	}
 
 	@Override
 	public int getItemViewType(int position) {
 		Status reservationStatus = getItem(position).getStatus();
 		return reservationStatus == Status.booked? 1 : 0;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 2;
 	}
 }
