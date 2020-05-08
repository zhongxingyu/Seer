 package it.opencontent.android.ocparchitn.fragments;
 
 import it.opencontent.android.ocparchitn.Constants;
 import it.opencontent.android.ocparchitn.R;
 import it.opencontent.android.ocparchitn.activities.MainActivity;
 import it.opencontent.android.ocparchitn.db.OCParchiDB;
 import it.opencontent.android.ocparchitn.db.entities.Area;
 import it.opencontent.android.ocparchitn.db.entities.RecordTabellaSupporto;
 import it.opencontent.android.ocparchitn.db.entities.Struttura;
 import it.opencontent.android.ocparchitn.utils.Utils;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Base64;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * 
  * @author Marco Albarelli <info@marcoalbarelli.eu>
  */
 
 public class RilevazioneAreaFragment extends Fragment implements ICustomFragment{
 
 	private static final String TAG = RilevazioneAreaFragment.class.getSimpleName();
 
 	private ArrayAdapter<RecordTabellaSupporto> adapterTipoPavimentazione;
 	private boolean siamoEditabili =true;
 	private int selectTriggerEventsCount = 0;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 
 		OCParchiDB db = new OCParchiDB(getActivity().getApplicationContext());
 		View view = inflater.inflate(R.layout.rilevazione_area, container, false);
 		
 		/**
 		 * Setup spinners
 		 */
 		List<RecordTabellaSupporto> records = db.tabelleSupportoGetAllRecords(Constants.TABELLA_TIPO_PAVIMENTAZIONI);
 		db.close();
 		
 		if(records != null){
 			RecordTabellaSupporto recordNullo = new RecordTabellaSupporto();
 			recordNullo.codice = 0;
 			recordNullo.numeroTabella = Constants.TABELLA_TIPO_PAVIMENTAZIONI;
 			recordNullo.descrizione = "Non impostato";
 			records.add(0,recordNullo);
 			setupSpinnerTipiPavimentazione(view, records);
 		}
 		
 		
 		
 		return view;
 	}
 
 
 
 	private void setupSpinnerTipiPavimentazione(View view, List<RecordTabellaSupporto> records) {
 		adapterTipoPavimentazione = new ArrayAdapter<RecordTabellaSupporto>(getActivity(), R.layout.default_spinner_layout,records);
 		adapterTipoPavimentazione.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		final Spinner spinnerTipoPavimentazione = (Spinner) view.findViewById(R.id.display_area_tipoPavimentazione);
 		spinnerTipoPavimentazione.setAdapter(adapterTipoPavimentazione);
 		
 		
 		spinnerTipoPavimentazione.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				RecordTabellaSupporto r = (RecordTabellaSupporto) spinnerTipoPavimentazione.getAdapter().getItem(arg2);
 				Area a = MainActivity.getCurrentArea();
				if(selectTriggerEventsCount > 1 && a!=null){
 					a.tipoPavimentazione = r.codice;
 					MainActivity.setCurrentArea(a);
 					salvaModifiche(null);
 				}
 				selectTriggerEventsCount++;
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				
 			}
 		});
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState){
 		super.onActivityCreated(savedInstanceState);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(MainActivity.getCurrentArea() != null){
 			showStrutturaData(MainActivity.getCurrentArea());
 			selectTriggerEventsCount++;
 		}
 	}
 
 
 	public void salvaModifiche(View v){
 		Log.d(TAG,"salvamodifiche nel fragment");
 		saveLocal(MainActivity.getCurrentArea());
 	}
 	
 	private void updateText(int viewId, String text){
 		TextView t = (TextView) getActivity().findViewById(viewId);
 		if(t != null){
 			t.setText(text);
 		}
 	}
 	
 	public void editMe(View v){
 		Log.d(TAG,"editme nel fragment");	
 		if(siamoEditabili){
 		switch(v.getId()){
 		case R.id.display_area_tipoPavimentazione:
 			break;
 		default: 
 			TextView t = (TextView) v;
 			changeTextValueThroughAlert(t);
 			break;
 		}
 		}
 	}
 
 	/**
 	 * @param t La textview creata nel @see editMe 
 	 */
 	private void changeTextValueThroughAlert(TextView t) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
 
 		alert.setTitle("Modifica il dato");
 		alert.setMessage("ID");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(getActivity());
 		input.setText(t.getText());
 		input.setTag(R.integer.tag_view_id, t.getId());
 		final int viewId =Integer.parseInt(input.getTag(R.integer.tag_view_id).toString());
 		switch(viewId){
 		case R.id.display_gioco_gpsx:
 		case R.id.display_gioco_gpsy:
 		case R.id.display_area_spessore:
 		case R.id.display_area_superficie:
 			input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER);
 			break;
 			
 		}
 		
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				
 				try {
 					updateText(viewId,value);
 					Area a = MainActivity.getCurrentArea();
 					switch(viewId){
 					case R.id.display_gioco_posizione_rfid:
 						a.posizioneRfid = value;
 						break;
 					case R.id.display_gioco_nota:
 						a.note = value;
 						break;
 					case R.id.display_area_spessore:
 						a.spessore = value;
 						break;
 					case R.id.display_area_superficie:
 						a.superficie = value;
 						break;
 					case R.id.display_area_descrizione:
 						a.descrizioneArea = value;
 						break;
 					}
 					saveLocal(a);
 				} catch (NumberFormatException nfe) {
 
 				}
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 
 					}
 				});
 
 		alert.show();
 	}
 	public void placeMe(View v){
 		Log.d(TAG,"Nessun piazzamento necessario per l'area");
 	}
 	public void showError(HashMap<String,String> map){
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int returnCode, Intent intent) {
 		switch (requestCode) {
 
 		
 		default:
 			break;
 		}
 	}
 	
 	private long saveLocal(Area area){
 
 		OCParchiDB db = new OCParchiDB(getActivity().getApplicationContext());
 		long id = db.salvaStrutturaLocally(area);
 		db.close();
 		if(id > 0){
 			Toast.makeText(getActivity().getApplicationContext(),"Area salvata localmente", Toast.LENGTH_SHORT).show();
 			MainActivity ma = (MainActivity) getActivity();
 			ma.updateCountDaSincronizzare();
 		} else if (id == -2){
 			//constraint error
 		}
 		return id;
 	}
 	
 
 	public void showStrutturaData(Struttura a) {
 		selectTriggerEventsCount = 0;
 		Area area = (Area) a;
 		TextView v;
 		v = (TextView) getActivity().findViewById(R.id.display_area_id);
 		v.setText(""+area.idArea);
 		v = (TextView) getActivity().findViewById(R.id.display_gioco_nota);
 		v.setText(area.note);
 		v = (TextView) getActivity().findViewById(R.id.display_area_rfid);
 		v.setText(area.rfidArea+"");
 		Button b = (Button) getActivity().findViewById(R.id.pulsante_associa_rfid_a_area);
 		if(area.rfidArea>0){
 			b.setEnabled(false);
 		} else {
 			b.setEnabled(true);
 		}
 
 		v = (TextView) getActivity().findViewById(R.id.display_area_spessore);
 		v.setText(area.spessore + "");
 		v = (TextView) getActivity().findViewById(R.id.display_area_superficie);
 		v.setText(area.superficie + "");
 		v = (TextView) getActivity().findViewById(R.id.display_area_descrizione);
 		v.setText(area.descrizioneArea + "");
 		v = (TextView) getActivity().findViewById(R.id.display_gioco_posizione_rfid);
 		v.setText(area.posizioneRfid + "");
 		
 		Spinner s = (Spinner) getActivity().findViewById(R.id.display_area_tipoPavimentazione);
 		OCParchiDB db = new OCParchiDB(getActivity().getApplicationContext());
 		RecordTabellaSupporto tipoPavimentazione = db.tabelleSupportoGetRecord(Constants.TABELLA_TIPO_PAVIMENTAZIONI, area.tipoPavimentazione);
 		int position = Math.max(getAdapterRecordPosition(tipoPavimentazione),0);
 		s.setSelection(position);
 		
 		setupSnapshots(area);
 		db.close();
 	}
 
 	
 	
 	private int getAdapterRecordPosition(RecordTabellaSupporto record){
 		if(record!=null){
 			int count = adapterTipoPavimentazione.getCount();
 			for(int i = 0; i < count ; i++){
 				RecordTabellaSupporto r = adapterTipoPavimentazione.getItem(i);
 				if( r.codice == record.codice){
 					return i;
 				}			
 			}
 		}
 		return -1;
 	}
 	
 
 	private void setupSnapshots(Struttura gioco) {
 		int width = 150;
 		int height= 150;
 		ImageView v;
 		for (int i = 0; i < Constants.MAX_SNAPSHOTS_AMOUNT; i++) {
 			switch(i){
 			case 0:
 				v = (ImageView) getActivity().findViewById(R.id.snapshot_gioco_0);
 				v.setImageBitmap(Utils.decodeSampledBitmapFromResource(Base64.decode(gioco.foto0, Base64.DEFAULT),getResources(),1,width,height));
 				break;
 			case 1:
 				v = (ImageView) getActivity().findViewById(R.id.snapshot_gioco_1);
 				v.setImageBitmap(Utils.decodeSampledBitmapFromResource(Base64.decode(gioco.foto1, Base64.DEFAULT),getResources(),2,width,height));
 				break;
 			}
 			
 		}
 	}
 
 	@Override
 	public void clickedMe(View v) {
 		
 	}
 	
 	
 
 }
