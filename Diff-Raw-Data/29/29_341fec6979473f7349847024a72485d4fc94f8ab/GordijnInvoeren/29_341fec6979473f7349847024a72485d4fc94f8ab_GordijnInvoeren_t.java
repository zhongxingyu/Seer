 package com.mvanveggel.gordijnapp;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class GordijnInvoeren extends Activity implements OnClickListener, TextWatcher, OnItemSelectedListener {
 
 	Order order;
 	Gordijn gordijn;
 
 	String typeGordijnNaam;
 	int gordijnID;
 	boolean toevoegen;
 
 	Spinner spPlooi, spZoom, spVerdeling;
 	EditText etVertrek, etStofMaat, etStofPrijs, etPatroon, etBreedte, etHoogte, etVerdelingLinks, etVerdelingRechts, etAantalBanen, etKnipmaat,
 			etStofTotaal, etStofKosten, etMaakKosten, etTotaalKosten, etMemo;
 	TextView tvArtikel, tvArtikelFooter, tvPlooi, tvZoom, tvVertrek, tvStofMaat, tvStofPrijs, tvPatroon, tvBreedte, tvHoogte, tvVerdeling,
 			tvVerdelingLinks, tvVerdelingRechts, tvAantalBanen, tvKnipmaat, tvStofTotaal, tvStofKosten, tvMaakKosten, tvTotaalKosten;
 	Button btToevoegen;
 
 	String[] verdelingLijst = { "Gelijk stel", "Ongelijk stel", "Stuk links", "Stuk rechts" };
 
 	String Artikelnummer;
 	String artikelHeader = "";
 	String artikelFooter = "";
 
 	DataManager sqlData;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gordijninvoeren);
 
 		initVariables();
 		initLayout();
 		removeObjectsFromLayout();
 		setUpLayout();
 		Thread timer = new Thread() {
 			public void run() {
 				try {
 					sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
 					initListeners();
 				}
 			}
 		};
 		timer.start();
 
 	}
 
 	private void initVariables() {
 		order = OrderOverview.getOrder();
 		sqlData = new DataManager(this);
 
 		toevoegen = getIntent().getBooleanExtra("toevoegen", true);
 
 		gordijnID = getIntent().getIntExtra("gordijnid", -1);
 		// Als gordijnid niet -1 is dan is deze al aanwezig in order
 		if (gordijnID == -1) {
 			typeGordijnNaam = getIntent().getStringExtra("typegordijnnaam");
 			Artikelnummer = getIntent().getStringExtra("artikelnummer");
 			if (Artikelnummer != null) {
 				sqlData.open();
 				String artikelValues[] = sqlData.getEntryStof(Artikelnummer);
 				String maakwijzeValues[][] = sqlData.getEntryMaakwijze(typeGordijnNaam);
 				sqlData.close();
 				GordijnData gordijnData = new GordijnData(artikelValues, maakwijzeValues);
 				gordijn = new Gordijn(gordijnData, sqlData);
 				artikelHeader = "Artikelnr: " + gordijnData.getArtikelnummer() + " - " + gordijnData.getOmschrijving();
 				artikelFooter = gordijnData.getAanv_Omschrijving() + " - Levr: " + gordijnData.getLevernr() + " - " + gordijnData.getPrijs();
 			} else {
 				String typestofnaam = getIntent().getStringExtra("typestofnaam");
 				if (typestofnaam == null) {
 					sqlData.open();
 					String maakwijzeValues[][] = sqlData.getEntryMaakwijze(typeGordijnNaam);
 					sqlData.close();
 					GordijnData gordijnData = new GordijnData(null, maakwijzeValues);
 					gordijn = new Gordijn(gordijnData, sqlData);
 				} else {
 					sqlData.open();
 					String maakwijzeValues[][] = sqlData.getEntryMaakwijze(typeGordijnNaam, typestofnaam);
 					sqlData.close();
 					GordijnData gordijnData = new GordijnData(null, maakwijzeValues);
 					gordijn = new Gordijn(gordijnData, sqlData);
 				}
 			}
 		} else {
 			gordijn = order.getGordijn(gordijnID);
 			typeGordijnNaam = gordijn.getMaakwijzeNaam();
 			Artikelnummer = gordijn.getArtikelnummer();
 			if (Artikelnummer.length() > 0) {
 				sqlData.open();
 				String artikelValues[] = sqlData.getEntryStof(Artikelnummer);
 				String maakwijzeValues[][] = sqlData.getEntryMaakwijze(typeGordijnNaam);
 				sqlData.close();
 				GordijnData gordijnData = new GordijnData(artikelValues, maakwijzeValues);
 				artikelHeader = "Artikelnr: " + gordijnData.getArtikelnummer() + " - " + gordijnData.getOmschrijving();
 				artikelFooter = gordijnData.getAanv_Omschrijving() + " - Levr: " + gordijnData.getLevernr() + " - " + gordijnData.getPrijs();
 			}
 		}
 
 		spPlooi = (Spinner) findViewById(R.id.spPlooi);
 		spZoom = (Spinner) findViewById(R.id.spZoom);
 		spVerdeling = (Spinner) findViewById(R.id.spVerdeling);
 
 		tvArtikel = (TextView) findViewById(R.id.tvArtikel);
 		tvArtikelFooter = (TextView) findViewById(R.id.tvArtikelFooter);
 		tvPlooi = (TextView) findViewById(R.id.tvPlooi);
 		tvZoom = (TextView) findViewById(R.id.tvZoom);
 		tvVertrek = (TextView) findViewById(R.id.tvVertrek);
 		tvStofMaat = (TextView) findViewById(R.id.tvStofBreedte);
 		tvStofPrijs = (TextView) findViewById(R.id.tvStofPrijs);
 		tvPatroon = (TextView) findViewById(R.id.tvPatroon);
 		tvBreedte = (TextView) findViewById(R.id.tvBreedte);
 		tvHoogte = (TextView) findViewById(R.id.tvHoogte);
 		tvVerdeling = (TextView) findViewById(R.id.tvVerdeling);
 		tvVerdelingLinks = (TextView) findViewById(R.id.tvVerdelingLinks);
 		tvVerdelingRechts = (TextView) findViewById(R.id.tvVerdelingRechts);
 		tvAantalBanen = (TextView) findViewById(R.id.tvAantalBanen);
 		tvKnipmaat = (TextView) findViewById(R.id.tvKnipmaat);
 		tvStofTotaal = (TextView) findViewById(R.id.tvStofTotaal);
 		tvStofKosten = (TextView) findViewById(R.id.tvStofKosten);
 		tvMaakKosten = (TextView) findViewById(R.id.tvMaakKosten);
 		tvTotaalKosten = (TextView) findViewById(R.id.tvTotaalKosten);
 
 		etVertrek = (EditText) findViewById(R.id.etVertrek);
 		etStofMaat = (EditText) findViewById(R.id.etStofMaat);
 		etStofPrijs = (EditText) findViewById(R.id.etStofPrijs);
 		etPatroon = (EditText) findViewById(R.id.etPatroon);
 		etBreedte = (EditText) findViewById(R.id.etBreedte);
 		etHoogte = (EditText) findViewById(R.id.etHoogte);
 		etVerdelingLinks = (EditText) findViewById(R.id.etVerdelingLinks);
 		etVerdelingRechts = (EditText) findViewById(R.id.etVerdelingRechts);
 		etAantalBanen = (EditText) findViewById(R.id.etAantalBanen);
 		etKnipmaat = (EditText) findViewById(R.id.etKnipmaat);
 		etStofTotaal = (EditText) findViewById(R.id.etStofTotaal);
 		etStofKosten = (EditText) findViewById(R.id.etStofKosten);
 		etMaakKosten = (EditText) findViewById(R.id.etMaakKosten);
 		etTotaalKosten = (EditText) findViewById(R.id.etTotaalKosten);
 		etMemo = (EditText) findViewById(R.id.etMemo);
 		btToevoegen = (Button) findViewById(R.id.btToevoegen);
 	}
 
 	private void initListeners() {
 		etStofPrijs.addTextChangedListener(this);
 		etPatroon.addTextChangedListener(this);
 		etStofMaat.addTextChangedListener(this);
 		etBreedte.addTextChangedListener(this);
 		etHoogte.addTextChangedListener(this);
 		etVerdelingLinks.addTextChangedListener(this);
 		etAantalBanen.addTextChangedListener(this);
 
 		btToevoegen.setOnClickListener(this);
 
 		spPlooi.setOnItemSelectedListener(this);
 		spZoom.setOnItemSelectedListener(this);
 		spVerdeling.setOnItemSelectedListener(this);
 	}
 
 	private void disableListeners() {
 		etStofPrijs.removeTextChangedListener(this);
 		etPatroon.removeTextChangedListener(this);
 		etStofMaat.removeTextChangedListener(this);
 		etBreedte.removeTextChangedListener(this);
 		etHoogte.removeTextChangedListener(this);
 		etVerdelingLinks.removeTextChangedListener(this);
 		etAantalBanen.removeTextChangedListener(this);
 	}
 
 	private void enableListeners() {
 		etStofPrijs.addTextChangedListener(this);
 		etPatroon.addTextChangedListener(this);
 		etStofMaat.addTextChangedListener(this);
 		etBreedte.addTextChangedListener(this);
 		etHoogte.addTextChangedListener(this);
 		etVerdelingLinks.addTextChangedListener(this);
 		etAantalBanen.addTextChangedListener(this);
 	}
 
 	private void initLayout() {
 		ArrayList<String> plooiNamenLijst = new ArrayList<String>();
 		ArrayList<String> zoomenLijst = new ArrayList<String>();
 
 		if (!toevoegen)
 			btToevoegen.setText(getResources().getText(R.string.buttonToevoegenOpslaan));
 
 		tvArtikel.setText(artikelHeader);
 		tvArtikelFooter.setText(artikelFooter);
 
 		setTitle("Invoeren " + gordijn.getMaakwijzeNaam() + " " + gordijn.getTypeStofNaam());
 
 		plooiNamenLijst = gordijn.getPlooiNamen();
 		ArrayAdapter<String> adapterPlooi = new ArrayAdapter<String>(GordijnInvoeren.this, android.R.layout.simple_spinner_item, plooiNamenLijst);
 		adapterPlooi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spPlooi.setAdapter(adapterPlooi);
 
 		zoomenLijst = gordijn.getZoomNamen();
 		ArrayAdapter<String> adapterZoom = new ArrayAdapter<String>(GordijnInvoeren.this, android.R.layout.simple_spinner_item, zoomenLijst);
 		adapterZoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spZoom.setAdapter(adapterZoom);
 
 		if (gordijn.getStofMaat() > 0)
 			etStofMaat.setText(String.format(Locale.US, "%.1f", gordijn.getStofMaat()));
 		if (gordijn.getStofPrijs() > 0)
 			etStofPrijs.setText(String.format(Locale.US, "%.2f", gordijn.getStofPrijs()));
 		if (gordijn.getPatroon() > 0)
 			etPatroon.setText(String.format(Locale.US, "%.1f", gordijn.getPatroon()));
 		if (gordijn.getBreedte() > 0)
 			etBreedte.setText(String.format(Locale.US, "%.1f", gordijn.getBreedte()));
 		if (gordijn.getHoogte() > 0)
 			etHoogte.setText(String.format(Locale.US, "%.1f", gordijn.getHoogte()));
 		if (gordijn.getVerdelingLinks() > 0)
 			etVerdelingLinks.setText(String.format(Locale.US, "%.2f", gordijn.getVerdelingLinks()));
 		if (gordijn.getVerdelingRechts() > 0)
 			etVerdelingRechts.setText(String.format(Locale.US, "%.2f", gordijn.getVerdelingRechts()));
 		if (gordijn.getAantalBanen() > 0)
 			etAantalBanen.setText(String.format(Locale.US, "%.2f", gordijn.getAantalBanen()));
 		if (gordijn.getKnipMaat() > 0)
 			etKnipmaat.setText(String.format(Locale.US, "%.2f", gordijn.getKnipMaat()));
 		if (gordijn.getStofmaatTotaal() > 0)
 			etStofTotaal.setText(String.format(Locale.US, "%.2f", gordijn.getStofmaatTotaal()));
 		if (gordijn.getStofKosten() > 0)
 			etStofKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getStofKosten()));
 		if (gordijn.getMaakKosten() > 0) {
 			if (gordijn.requiresManualMaakkosten())
 				etMaakKosten.setText(String.format(Locale.US, "%.2f", gordijn.getMaakKosten()));
 			else
 				etMaakKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getMaakKosten()));
 		}
 		if (gordijn.getTotaalKosten() > 0)
 			etTotaalKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getTotaalKosten()));
 
 		etVertrek.setText(gordijn.getVertrek());
 		etMemo.setText(gordijn.getMemo());
 
 		ArrayAdapter<String> adapterVerdeling = new ArrayAdapter<String>(GordijnInvoeren.this, android.R.layout.simple_spinner_item, verdelingLijst);
 		adapterVerdeling.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spVerdeling.setAdapter(adapterVerdeling);
 
 		spPlooi.setSelection(gordijn.getPlooiPosition());
 		spZoom.setSelection(gordijn.getZoomPosition());
 		spVerdeling.setSelection(gordijn.getVerdelingPosition());
 
 		switch (gordijn.getVerdelingPosition()) {
 		case 0:
 			etVerdelingLinks.setFocusableInTouchMode(true);
 			etVerdelingLinks.setEnabled(false);
 			break;
 		case 1:
 			etVerdelingLinks.setFocusableInTouchMode(true);
 			etVerdelingLinks.setEnabled(true);
 			break;
 		case 2:
 			etVerdelingLinks.setFocusableInTouchMode(true);
 			etVerdelingLinks.setEnabled(false);
 			break;
 		case 3:
 			etVerdelingLinks.setFocusableInTouchMode(true);
 			etVerdelingLinks.setEnabled(false);
 			break;
 		}
 	}
 
 	private void removeObjectsFromLayout() {
 		spPlooi.setVisibility(View.GONE);
 		spZoom.setVisibility(View.GONE);
 		spVerdeling.setVisibility(View.GONE);
 
 		tvArtikel.setVisibility(View.GONE);
 		tvArtikelFooter.setVisibility(View.GONE);
 		tvPlooi.setVisibility(View.GONE);
 		tvZoom.setVisibility(View.GONE);
 		tvStofMaat.setVisibility(View.GONE);
 		tvStofPrijs.setVisibility(View.GONE);
 		tvPatroon.setVisibility(View.GONE);
 		tvVerdeling.setVisibility(View.GONE);
 		tvVerdelingLinks.setVisibility(View.GONE);
 		tvVerdelingRechts.setVisibility(View.GONE);
 		tvAantalBanen.setVisibility(View.GONE);
 		tvKnipmaat.setVisibility(View.GONE);
 		tvStofTotaal.setVisibility(View.GONE);
 		tvStofKosten.setVisibility(View.GONE);
 		tvMaakKosten.setVisibility(View.GONE);
 		tvTotaalKosten.setVisibility(View.GONE);
 
 		etStofMaat.setVisibility(View.GONE);
 		etStofPrijs.setVisibility(View.GONE);
 		etPatroon.setVisibility(View.GONE);
 		etVerdelingLinks.setVisibility(View.GONE);
 		etVerdelingRechts.setVisibility(View.GONE);
 		etAantalBanen.setVisibility(View.GONE);
 		etKnipmaat.setVisibility(View.GONE);
 		etStofTotaal.setVisibility(View.GONE);
 		etStofKosten.setVisibility(View.GONE);
 		etMaakKosten.setVisibility(View.GONE);
 		etTotaalKosten.setVisibility(View.GONE);
 	}
 
 	private void setUpLayout() {
 		// Volgorde instellen
 		etVertrek.setNextFocusDownId(R.id.etBreedte);
 		etStofMaat.setNextFocusDownId(R.id.etBreedte);
 		etPatroon.setNextFocusDownId(R.id.etBreedte);
 		etBreedte.setNextFocusDownId(R.id.etHoogte);
 
 		if (artikelHeader.length() > 0)
 			tvArtikel.setVisibility(View.VISIBLE);
 		if (artikelFooter.length() > 0)
 			tvArtikelFooter.setVisibility(View.VISIBLE);
 
 		if (gordijn.requiresPlooi()) {
 			tvPlooi.setVisibility(View.VISIBLE);
 			spPlooi.setVisibility(View.VISIBLE);
 		}
 		if (gordijn.requiresZoom()) {
 			tvZoom.setVisibility(View.VISIBLE);
 			spZoom.setVisibility(View.VISIBLE);
 		}
 
 		if (gordijn.requiresVerdeling()) {
 			tvVerdeling.setVisibility(View.VISIBLE);
 			spVerdeling.setVisibility(View.VISIBLE);
 			tvVerdelingLinks.setVisibility(View.VISIBLE);
 			tvVerdelingRechts.setVisibility(View.VISIBLE);
 			etVerdelingLinks.setVisibility(View.VISIBLE);
 			etVerdelingRechts.setVisibility(View.VISIBLE);
 		}
 		if (gordijn.requiresStofMaat()) {
 			tvStofMaat.setVisibility(View.VISIBLE);
 			etStofMaat.setVisibility(View.VISIBLE);
 			if (Artikelnummer == null) {
 				tvStofPrijs.setVisibility(View.VISIBLE);
 				etStofPrijs.setVisibility(View.VISIBLE);
 			}
 		}
 		if (gordijn.requiresPatroon()) {
 			tvPatroon.setVisibility(View.VISIBLE);
 			etPatroon.setVisibility(View.VISIBLE);
 		}
 		if (gordijn.requiresBanen()) {
 			tvAantalBanen.setVisibility(View.VISIBLE);
 			etAantalBanen.setVisibility(View.VISIBLE);
 			tvKnipmaat.setVisibility(View.VISIBLE);
 			etKnipmaat.setVisibility(View.VISIBLE);
 
 			etHoogte.setNextFocusDownId(R.id.etAantalBanen);
 			etAantalBanen.setNextFocusDownId(View.NO_ID);
 			etKnipmaat.setNextFocusDownId(R.id.btToevoegen);
 			etVerdelingLinks.setNextFocusDownId(R.id.etAantalBanen);
 			etVerdelingRechts.setNextFocusDownId(R.id.etAantalBanen);
 		}
 		if (gordijn.requiresStofTotaal()) {
 			tvStofTotaal.setVisibility(View.VISIBLE);
 			etStofTotaal.setVisibility(View.VISIBLE);
 			etStofTotaal.setNextFocusDownId(R.id.btToevoegen);
 
 			tvStofKosten.setVisibility(View.VISIBLE);
 			tvMaakKosten.setVisibility(View.VISIBLE);
 			tvTotaalKosten.setVisibility(View.VISIBLE);
 			etStofKosten.setVisibility(View.VISIBLE);
 			etMaakKosten.setVisibility(View.VISIBLE);
 			etTotaalKosten.setVisibility(View.VISIBLE);
 		}
 
 		if (gordijn.requiresManualMaakkosten()) {
 			tvMaakKosten.setVisibility(View.VISIBLE);
 			etMaakKosten.setVisibility(View.VISIBLE);
 			etMaakKosten.setEnabled(true);
 			etMaakKosten.setFocusableInTouchMode(true);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		gordijn.setVertrek(etVertrek.getText().toString());
 		gordijn.setMemo(etMemo.getText().toString());
 		if (gordijn.requiresManualMaakkosten() && etMaakKosten.getText().length() > 0)
 			gordijn.setMaakKosten(etMaakKosten.getText().toString());
 		if (toevoegen) {
 			order.addGordijn(gordijn);
 		} else {
 			order.setGordijnIngevoerd(gordijnID, true);
 		}
 		finish();
 	}
 
 	@Override
 	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 		EditText view = (EditText) getCurrentFocus();
 		if (view != null) {
 			if (view.getText().toString().equals("."))
 				return;
 			switch (view.getId()) {
 			case R.id.etBreedte:
 				if (etBreedte.getText().length() > 0) {
 					float breedte = Float.valueOf(etBreedte.getText().toString());
 					if (breedte > gordijn.getMaxBreedte())
						etBreedte.setError("Max breedte " + gordijn.getMaxBreedte() + "cm");
 					else {
 						etBreedte.setError(null);
 						gordijn.setBreedte(breedte);
 						Calculate();
 					}
 				}
 				break;
 			case R.id.etHoogte:
 				if (etHoogte.getText().length() > 0) {
 					float hoogte = Float.valueOf(etHoogte.getText().toString());
 					if (hoogte > gordijn.getMaxHoogte())
						etHoogte.setError("Max hoogte " + gordijn.getMaxHoogte() + "cm");
 					else {
 						etHoogte.setError(null);
 						gordijn.setHoogte(hoogte);
 						Calculate();
 					}
 					Calculate();
 				}
 				break;
 			case R.id.etAantalBanen:
 				if (etAantalBanen.getText().length() > 0) {
 					gordijn.setAantalBanen(Float.valueOf(etAantalBanen.getText().toString()));
 					setBanen();
 				}
 				break;
 			case R.id.etStofMaat:
 				if (etStofMaat.getText().length() > 0) {
 					gordijn.setStofMaat(Float.valueOf(etStofMaat.getText().toString()));
 					Calculate();
 				}
 				break;
 			case R.id.etStofPrijs:
 				if (etStofPrijs.getText().length() > 0) {
 					gordijn.setStofPrijs(Float.valueOf(etStofPrijs.getText().toString()));
 					Calculate();
 				}
 				break;
 			case R.id.etPatroon:
 				if (etPatroon.getText().length() > 0) {
 					gordijn.setPatroon(Float.valueOf(etPatroon.getText().toString()));
 					Calculate();
 				}
 				break;
 			case R.id.etVerdelingLinks:
 				if (etVerdelingLinks.getText().length() > 0) {
 					gordijn.setVerdelingLinks(Float.valueOf(etVerdelingLinks.getText().toString()));
 					setBanenLinks();
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> view, View arg1, int arg2, long arg3) {
 		switch (view.getId()) {
 		case R.id.spPlooi:
 			gordijn.setPlooiPosition(spPlooi.getSelectedItemPosition());
 			Calculate();
 			break;
 		case R.id.spZoom:
 			gordijn.setZoomPosition(spZoom.getSelectedItemPosition());
 			Calculate();
 			break;
 		case R.id.spVerdeling:
 			gordijn.setVerdelingPosition(spVerdeling.getSelectedItemPosition());
 			switch (spVerdeling.getSelectedItemPosition()) {
 			case 0:
 				etVerdelingLinks.setFocusableInTouchMode(true);
 				etVerdelingLinks.setEnabled(false);
 				break;
 			case 1:
 				etVerdelingLinks.setFocusableInTouchMode(true);
 				etVerdelingLinks.setEnabled(true);
 				break;
 			case 2:
 				etVerdelingLinks.setFocusableInTouchMode(true);
 				etVerdelingLinks.setEnabled(false);
 				break;
 			case 3:
 				etVerdelingLinks.setFocusableInTouchMode(true);
 				etVerdelingLinks.setEnabled(false);
 				break;
 			}
 			break;
 		}
 	}
 
 	private void Calculate() {
 		if (gordijn.Calculate())
 			refreshResults();
 	}
 
 	private void setBanen() {
 		if (gordijn.setBanen())
 			refreshResults();
 	}
 
 	private void setBanenLinks() {
 		if (gordijn.setNewVerdeling())
 			refreshResults();
 	}
 
 	private void refreshResults() {
 		View view = getCurrentFocus();
 
 		disableListeners();
 		if (gordijn.getTypeStof() == 0) {
 			if (view.getId() != R.id.etAantalBanen)
 				etAantalBanen.setText(String.valueOf(gordijn.getAantalBanen()));
 			etKnipmaat.setText(String.valueOf(gordijn.getKnipMaat()));
 		}
 		if (view.getId() != R.id.etVerdelingLinks)
 			etVerdelingLinks.setText(String.valueOf(gordijn.getVerdelingLinks()));
 		etVerdelingRechts.setText(String.valueOf(gordijn.getVerdelingRechts()));
 		etStofTotaal.setText(String.valueOf(gordijn.getStofmaatTotaal()));
 
 		if (gordijn.getStofKosten() > 0)
 			etStofKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getStofKosten()));
 		if (gordijn.getMaakKosten() > 0)
 			if (gordijn.requiresManualMaakkosten())
 				etMaakKosten.setText(String.format(Locale.US, "%.2f", gordijn.getMaakKosten()));
 			else
 				etMaakKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getMaakKosten()));
 		if (gordijn.getTotaalKosten() > 0)
 			etTotaalKosten.setText("" + String.format(Locale.US, "%.2f", gordijn.getTotaalKosten()));
 
 		enableListeners();
 	}
 
 	@Override
 	public void afterTextChanged(Editable arg0) {
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 	}
 }
