 package com.mdsgpp.eef.visao;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.mdsgpp.eef.R;
 
 public abstract class Comparacao extends Activity{
 	private Spinner estadosSpinner01;
 	private Spinner estadosSpinner;
 	private ArrayAdapter<String> estadosAdapter01;
 	private ArrayAdapter<String> estadosAdapter02;
 	private ArrayList<String> estados01;
 	private ArrayList<String> estados02;
 	private ArrayList<String> todosEstados;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tela_comparacao);
 
 		inicializaSpinners();
		preencheEstados(estados01);
		preencheEstados(estados02);
		preencheEstados(todosEstados);
 
 		setAdapterSpinner01();
 		setAdapterSpinner02();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu_telas, menu);
 		return true;
 	}
 
 	private void inicializaSpinners() {
 		estados01 = new ArrayList<String>();
 		estados02 = new ArrayList<String>();
 		todosEstados = new ArrayList<String>();
 
 		estadosSpinner01 = (Spinner) findViewById(R.id.spinner1);
 		estadosSpinner01
 				.setOnItemSelectedListener(new OnItemSelectedListener() {
 					@Override
 					public void onItemSelected(AdapterView<?> adapter,
 							View view, int posicao, long id) {
 						atualizaValoresSpinner02(estadosSpinner01
 								.getSelectedItem().toString());
 					}
 
 					@Override
 					public void onNothingSelected(AdapterView<?> arg0) {
 						// nada a fazer
 					}
 				});
 
 		estadosSpinner = (Spinner) findViewById(R.id.spinner2);
 		estadosSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapter, View view,
 					int posicao, long id) {
 				atualizaValoresSpinner01(estadosSpinner.getSelectedItem()
 						.toString());
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// nada a fazer
 			}
 		});
 
 	}
 
 	private void setAdapterSpinner01() {
 		estadosAdapter01 = new ArrayAdapter<String>(this,
 				R.layout.spinner_item, estados01);
 		estadosAdapter01
 				.setDropDownViewResource(android.R.layout.simple_list_item_1);
 
 		estadosSpinner01.setAdapter(estadosAdapter01);
 	}
 
 	private void setAdapterSpinner02() {
 		estadosAdapter02 = new ArrayAdapter<String>(this,
 				R.layout.spinner_item, estados02);
 		estadosAdapter02.setDropDownViewResource(android.R.layout.simple_list_item_1);
 
 		// Define o adapter para os spinners
 		estadosSpinner.setAdapter(estadosAdapter02);
 	}
 
	private void preencheEstados(ArrayList<String> estados) {
 		estados.clear();
 
 		estados.add("Acre");
 		estados.add("Alagoas");
 		estados.add("Amap");
 		estados.add("Amazonas");
 		estados.add("Bahia");
 		estados.add("Cear");
 		estados.add("Distrito Federal");
 		estados.add("Esprito Santo");
 		estados.add("Gois");
 		estados.add("Maranho");
 		estados.add("Mato Grosso");
 		estados.add("Mato Grosso do Sul");
 		estados.add("Minas Gerais");
 		estados.add("Par");
 		estados.add("Paran");
 		estados.add("Pernambuco");
 		estados.add("Piau");
 		estados.add("Rio de Janeiro");
 		estados.add("Rio Grande do Norte");
 		estados.add("Rio Grande do Sul");
 		estados.add("Rondnia");
 		estados.add("Roraima");
 		estados.add("Santa Catarina");
 		estados.add("So Paulo");
 		estados.add("Sergipe");
 		estados.add("Tocantins");
 	}
 
 	private void atualizaValoresSpinner01(String nome) {
 		String estadoSelecionado = estadosSpinner01.getSelectedItem().toString();
		preencheEstados(estados01);
 		estados01.remove(nome);
 		estadosAdapter01.notifyDataSetChanged();
 		estadosSpinner01.setSelection(estados01.indexOf(estadoSelecionado));
 	}
 
 	private void atualizaValoresSpinner02(String nome) {
 		String estadoSelecionado = estadosSpinner.getSelectedItem().toString();
		preencheEstados(estados02);
 		estados02.remove(nome);
 		estadosAdapter02.notifyDataSetChanged();
 		estadosSpinner.setSelection(estados02.indexOf(estadoSelecionado));
 	}
 
 	public abstract void clickBotaoComparacaoEstados(View view);
 	
 	public void clickBotaoSobreCompara(View view) {
 		Intent intent = new Intent(this, TelaSobreConsulta.class);
 		startActivity(intent);
 	}	
 	
 	public Spinner getEstadosSpinner01() {
 		return estadosSpinner01;
 	}
 
 	public Spinner getEstadosSpinner() {
 		return estadosSpinner;
 	}
 
 	public ArrayList<String> getTodosEstados() {
 		return todosEstados;
 	}
 }
