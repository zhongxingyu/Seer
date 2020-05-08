 //-----------------------------------------------------------------------------
 // Bevster 2012 - Kos deg unge and!
 //-----------------------------------------------------------------------------
 
 package net.bevster.lorensjon;
 
 import net.bevster.lorensjon.adapters.Instillinger_adapter;
 import net.bevster.lorensjon.io.EasyIO;
 import net.bevster.lorensjon.io.LokalData;
 
 import org.taptwo.android.widget.TitleFlowIndicator;
 import org.taptwo.android.widget.ViewFlow;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 
 public class Instillinger extends Activity {
 
 	ActionBar actionBar;
 
 	EasyIO eIO;
 	LokalData lData;
 
 	Intent intent_menu;
 
 	String[] SETTINGS_STUDENT, SETTINGS_UKEPLAN;
 
 	ViewFlow viewFlow;
 
 	Spinner spinner_student, spinner_skole, spinner_klasse, spinner_ukemodus;
 	SeekBar uke_bar;
 	Button btn_lagre, btn_ref_skole, btn_ref_klasse;
 	CheckBox chk_nett;
 	TextView txt_info_ukeplan, txt_info_profil, txt_ukeplan;
 	EditText Edit_Height, Edit_Width;
 	ArrayAdapter<String> spinner_adapter_navn, spinner_adapter_skole, spinner_adapter_klasse, spinner_adapter_ukemodus;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
 
 		setContentView(R.layout.instillinger);
 
 		intent_menu = new Intent(Instillinger.this, HovedAktivitet.class);
 		intent_menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent_menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
 		Instillinger_adapter adapter = new Instillinger_adapter(this);
 		viewFlow.setAdapter(adapter, 0);
 		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
 		indicator.setTitleProvider(adapter);
 		viewFlow.setFlowIndicator(indicator);
 
 		// -----------------------------------------------------------------------------
 		// Purpose: Laste inn view elementer
 		// -----------------------------------------------------------------------------
 
 		actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar.setTitle(R.string.app_name);
 		actionBar.setHomeAction(new IntentAction(Instillinger.this.getBaseContext(), intent_menu, R.drawable.akershus_logo_96));
 
 		// Laster inn tekstseeee
 		txt_info_ukeplan = (TextView) findViewById(R.id.info_skole);
 		txt_info_profil = (TextView) findViewById(R.id.info_student);
 		txt_ukeplan = (TextView) findViewById(R.id.info_ukeplan);
 
 		// Laster inn spinnere
 		spinner_student = (Spinner) findViewById(R.id.spinner_student);
 
 		spinner_skole = (Spinner) findViewById(R.id.Spinner_skole);
 		spinner_klasse = (Spinner) findViewById(R.id.spinner_klasse);
 
 		spinner_ukemodus = (Spinner) findViewById(R.id.spinner_modus);
 
 		// Laster inn for valg av uke
 		uke_bar = (SeekBar) findViewById(R.id.Uke_bar);
 
 		// Sjekkboks for nett eller lokal henting av data-
 		chk_nett = (CheckBox) findViewById(R.id.checkBox_profil_nett);
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		eIO = new EasyIO();
 		lData = new LokalData();
 
 		btn_lagre = (Button) findViewById(R.id.btn_lagre);
 		btn_lagre.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 
 				saveValues();
 				setStatus();
 
 			}
 		});
 		btn_ref_skole = (Button) findViewById(R.id.btn_ref_skole);
 		btn_ref_skole.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (Integer.parseInt(SETTINGS_STUDENT[6].toString()) == 1) {
 
 					Log.e("BADASS_skole_spinner", "Blir kalt ved start");
 
 					SETTINGS_STUDENT[8] = Integer.toString(spinner_skole.getSelectedItemPosition()); // Plass i spinner skole
 					eIO.fileWrite(EasyIO.SETTINGS_STUDENTER, eIO.fromTable(SETTINGS_STUDENT));
 					Log.e("BADASS_skole_spinner", SETTINGS_STUDENT[8]);
 
 					spinner_skole.setAdapter(spinner_adapter_skole);
 					spinner_skole.setSelection(Integer.parseInt(SETTINGS_STUDENT[8].toString()));
 					loadArrayAdapters(false);
 					spinner_klasse.setAdapter(spinner_adapter_klasse);
 					spinner_klasse.setSelection(1);
 					loadArrayAdapters(false);
 					spinner_student.setAdapter(spinner_adapter_navn);
 				}
 			}
 		});
 
 		btn_ref_klasse = (Button) findViewById(R.id.btn_ref_klasse);
 		btn_ref_klasse.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (Integer.parseInt(SETTINGS_STUDENT[6].toString()) == 1) {
 
 					Log.e("BADDASS_KLASSE", "Blir kalt ved start");
 
 					loadArrayAdapters(false);
 					spinner_student.setAdapter(spinner_adapter_navn);
 				}
 			}
 
 		});
 
 		// Masse lort for live info om ukevalg
 		uke_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				// TODO Auto-generated method stub
 
 				txt_ukeplan.setText("");
 				txt_ukeplan.append(Integer.toString(progress));
 
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 
 		loadValuesArrays(); // Fyll tabellene med lagret data
 
 		SETTINGS_STUDENT[8] = SETTINGS_STUDENT[4]; // Spinner skole
 		eIO.fileWrite(EasyIO.SETTINGS_STUDENTER, eIO.fromTable(SETTINGS_STUDENT));
 
 		loadArrayAdapters(true); // Last inn arrayadaptere
 		spinner_skole.setAdapter(spinner_adapter_skole);
 		spinner_klasse.setAdapter(spinner_adapter_klasse);
 		spinner_student.setAdapter(spinner_adapter_navn);
 		spinner_ukemodus.setAdapter(spinner_adapter_ukemodus);
 		loadStoredValues(); // Last inn lagrede data til view elementer
 		setStatus(); // Oppdater status fra lokaldata
 
 	}
 
 	// -----------------------------------------------------------------------------
 	// Purpose: Laste inn fra alle lagrede innstillinger til statusside
 	// -----------------------------------------------------------------------------
 
 	void setStatus() {
 
 		String kulStart = "( \"";
 		String kulSlutt = "\" )";
 
 		StringBuilder sbP = new StringBuilder();
 
 		for (int j = 0; j < SETTINGS_STUDENT.length; j++) {
 
 			if (!SETTINGS_STUDENT[j].toString().equals("null"))
 				sbP.append(j + " " + kulStart + SETTINGS_STUDENT[j] + kulSlutt + "\n");
 		}
 
 		txt_info_profil.setText("");
 		txt_info_profil.append(sbP);
 
 		StringBuilder sbU = new StringBuilder();
 
 		for (int j = 0; j < SETTINGS_UKEPLAN.length; j++) {
 			if (!SETTINGS_UKEPLAN[j].toString().equals("null"))
 				sbU.append(j + " " + kulStart + SETTINGS_UKEPLAN[j] + kulSlutt + "\n");
 		}
 		txt_info_ukeplan.setText("");
 		txt_info_ukeplan.append(sbU);
 	}
 
 	// -----------------------------------------------------------------------------
 	// Purpose: Last inn lagrede verdier til view elementer
 	// -----------------------------------------------------------------------------
 
 	void loadStoredValues() {
 
 		if (lData.fileExist(EasyIO.SETTINGS_STUDENTER)) {
 			if (Integer.parseInt(SETTINGS_STUDENT[6].toString()) == 0) {
 				spinner_skole.setSelection(0);
 				spinner_klasse.setSelection(0);
 				spinner_student.setSelection(0);
 			} else {
 				spinner_skole.setSelection(Integer.parseInt(SETTINGS_STUDENT[4].toString()));
 				spinner_klasse.setSelection(Integer.parseInt(SETTINGS_STUDENT[5].toString()));
 				spinner_student.setSelection(Integer.parseInt(SETTINGS_STUDENT[3].toString()));
 			}
 			spinner_ukemodus.setSelection(Integer.parseInt(SETTINGS_UKEPLAN[1].toString()));
 
 			switch (Integer.parseInt(SETTINGS_STUDENT[6].toString())) {
 			case 0:
 				chk_nett.setChecked(false);
 				break;
 			case 1:
 				chk_nett.setChecked(true);
 				break;
 			default:
 				chk_nett.setChecked(false);
 				break;
 			}
 		}
 
 		if (lData.fileExist(EasyIO.SETTINGS_UKEPLAN)) {
 
 			uke_bar.setProgress(Integer.parseInt(SETTINGS_UKEPLAN[0].toString()));
 			spinner_ukemodus.setSelection((Integer.parseInt(SETTINGS_UKEPLAN[1].toString())));
 
 		}
 	}
 
 	// -----------------------------------------------------------------------------
 	// Purpose: Last inn verdier fra fil til tabell
 	// -----------------------------------------------------------------------------
 
 	void loadValuesArrays() {
 
 		SETTINGS_STUDENT = eIO.getTable(EasyIO.SETTINGS_STUDENTER);
 		SETTINGS_UKEPLAN = eIO.getTable(EasyIO.SETTINGS_UKEPLAN);
 
 	}
 
 	void loadArrayAdapters(boolean ve) {
 
 		loadValuesArrays();
 
 		if (ve) {
 			if (Integer.parseInt(SETTINGS_STUDENT[6].toString()) == 0) {
 
 				spinner_adapter_skole = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { SETTINGS_STUDENT[7].toString() });
 				spinner_adapter_klasse = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { SETTINGS_STUDENT[1].toString() });
 
 				if (Integer.parseInt(SETTINGS_STUDENT[4].toString()) == 2) { // Strommen sine spesielle needs
 					spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Fabeldyrene" });
 
 				} else {
 					spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { SETTINGS_STUDENT[0].toString() });
 				}
 
 			} else {
 				spinner_adapter_skole = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Lrenskog", "Rlingen", "Strmmen" });
 				spinner_adapter_klasse = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lData.returnKlasseArray(isOnline()));
 				if (Integer.parseInt(SETTINGS_STUDENT[4].toString()) == 2) { // Strommen sine spesielle needs
 					spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Fabeldyrene" });
 
 				} else {
					spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lData.returnStudentArray(isOnline(), SETTINGS_STUDENT[1].toString()));
 
 				}
 
 			}
 
 			spinner_adapter_ukemodus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Hy", "Lav" });
 
 			// Automatisk spinneroppdatering
 		} else {
 			spinner_adapter_klasse = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lData.returnKlasseArray(isOnline()));
 
 			if (Integer.parseInt(SETTINGS_STUDENT[8].toString()) == 2) { // Strommen sine spesielle needs
 				spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "Fabeldyrene" });
 
 			} else {
 				spinner_adapter_navn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lData.returnStudentArray(isOnline(), spinner_klasse.getSelectedItem().toString()));
 
 			}
 
 		}
 
 	}
 
 	// -----------------------------------------------------------------------------
 	// Purpose: Store the tabled values to file
 	// -----------------------------------------------------------------------------
 
 	void saveValues() {
 
 		// -----------------------------------------------------------------------------
 		// Studenter
 		// -----------------------------------------------------------------------------
 		if (isOnline()) {
 
 			if (Integer.parseInt(SETTINGS_STUDENT[6].toString()) == 1) {
 
 				SETTINGS_STUDENT[0] = spinner_student.getSelectedItem().toString(); // Navn
 				SETTINGS_STUDENT[1] = spinner_klasse.getSelectedItem().toString(); // Klasse
 				SETTINGS_STUDENT[3] = Integer.toString(spinner_student.getSelectedItemPosition()); // Plass i spinner elev
 				SETTINGS_STUDENT[4] = Integer.toString(spinner_skole.getSelectedItemPosition()); // Plass i spinner skole
 				SETTINGS_STUDENT[5] = Integer.toString(spinner_klasse.getSelectedItemPosition()); // Plass i spinner klasse
 				SETTINGS_STUDENT[7] = spinner_skole.getSelectedItem().toString(); // Skole
 				SETTINGS_STUDENT[8] = Integer.toString(spinner_skole.getSelectedItemPosition()); // Spinner skole
 
 			}
 		}
 
 		// Strommen fiks
 		if (Integer.parseInt(SETTINGS_STUDENT[8].toString()) == 2) {
 			SETTINGS_STUDENT[2] = lData.returnIdFromKlasse(isOnline(), SETTINGS_STUDENT[1].toString()); // ID
 
 		} else {
 			SETTINGS_STUDENT[2] = lData.returnIdFromName(isOnline(), spinner_student.getSelectedItem().toString()); // ID
 
 		}
 
 		if (chk_nett.isChecked())
 			SETTINGS_STUDENT[6] = "1"; // Skole
 		if (!chk_nett.isChecked())
 			SETTINGS_STUDENT[6] = "0"; // Skole
 
 		// -----------------------------------------------------------------------------
 		// Ukeplan
 		// -----------------------------------------------------------------------------
 
 		SETTINGS_UKEPLAN[0] = Integer.toString(uke_bar.getProgress()); // Uke
 		SETTINGS_UKEPLAN[1] = Integer.toString(spinner_ukemodus.getSelectedItemPosition()); // Modus
 
 		// -----------------------------------------------------------------------------
 		// Purpose: Write table to file
 		// -----------------------------------------------------------------------------
 
 		eIO.fileWrite(EasyIO.SETTINGS_UKEPLAN, eIO.fromTable(SETTINGS_UKEPLAN));
 		eIO.fileWrite(EasyIO.SETTINGS_STUDENTER, eIO.fromTable(SETTINGS_STUDENT));
 
 	}
 
 	// -----------------------------------------------------------------------------
 	// Purpose: Er du tilkoblet nettverket?
 	// -----------------------------------------------------------------------------
 
 	public boolean isOnline() {
 
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnected()) {
 			return true;
 		}
 		return false;
 	}
 
 }
