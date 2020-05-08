 package neto.viajante;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**Classe responsável pela tela 2, que faz calculo parcial da viagem, finaliza a viagem, faz novos abastecimentos etc...
  * 
  * @author n3t0
  *
  */
 public class Tela2 extends Activity {
 
 	private Button parcialViagem, confirmarChegada, novoAbastecimento, finalizarViagem;
 	private int escolheTela;
 	private TextView saida, destino, distanciaDestino;
 	private EditText etKmAtual;
 	static private final String TELA2 = "tela2";
 	static private final int RETORNO_TELA2 = 1;
 	private String horaAtual, tempoViagem, velocidadeMedia, mediaConsumo;
 	private int diaChegada, mesChegada, anoChegada;
 	private Calculo calc = new Calculo();
 	private Notifica notifica = new Notifica();
 	private double distanciaPercorrida;
 	private double combustivelGasto;
 	private String tempDistanciaDestino;
 	private int escolheTeladoBanco;
 	boolean escolheNotification;	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.d(TELA2, "Executou o onCreate:");
 
 		try {
 			Tela1.mDbHelper = new TravellerDbAdapter(this);
 
 			Tela1.mDbHelper.open();
 
 			Log.d(TELA2, "abriu banco");
 
 		} catch (Exception e) {
 			Log.v(TELA2, "DEU PAU");
 		}
 
 		setContentView(R.layout.tela2);
 		
 
 		novoAbastecimento = (Button) findViewById(R.id.novo_abastecimento);
 		finalizarViagem = (Button) findViewById(R.id.fim_viagem);
 		confirmarChegada = (Button) findViewById(R.id.confirmar_chegada);
 		parcialViagem = (Button) findViewById(R.id.calc_parcial);
 		saida = (TextView) findViewById(R.id.TV_hsaida);
 		destino = (TextView) findViewById(R.id.TV_destino);
 		distanciaDestino = (TextView) findViewById(R.id.TV_dist_destino);
 		etKmAtual = (EditText) findViewById(R.id.ET_km_atual);
 		
 		
 		
 		escolheTela = getTela();
 		Log.w(TELA2, "Valor do escolhe Tela: :)) "+ escolheTela);
 		
		if (escolheTela == 69) {
 			//etKmAtual.setEnabled(false);
 			confirmarChegada.setEnabled(false);
 			parcialViagem.setEnabled(false);
 		}
 		
 		
 		
 		// valores zerados pro caso de salvar, ou apertar a tecla home sem
 		// nenhum dado preenchido
 		horaAtual = "";
 		tempoViagem = "";
 		velocidadeMedia = "";
 		//dinheiroGasto = 0;
 		mediaConsumo = "";
 		diaChegada = 0;
 		mesChegada = 0;
 		anoChegada = 0;
 		
 
 		Tela1.mRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(TravellerDbAdapter.KEY_ROWID);
 		if (Tela1.mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			Tela1.mRowId = extras != null ? extras
 					.getLong(TravellerDbAdapter.KEY_ROWID) : null;
 		}
 
 		populateFieldsTela2();
 		tempDistanciaDestino = distanciaDestino.getText().toString();
 
 		final Intent iTela3 = new Intent(this, Tela3.class);// usado pra chamar a tela 3
 		final Intent iNovoAbastecimento = new Intent(this, Abastecimento.class); //usado pra chamar a tela de abastecimento
 
 		parcialViagem.setOnClickListener(new View.OnClickListener() {// botão do// calculo// da// parcial// da// viagem
 
 					public void onClick(View v) {
 
 						if (etKmAtual.getText().toString().equals("")) {// se// o// campo// km// estiver// em// branco// exibe// um// alerta
 
 							notifica.toast(getApplicationContext(), "Por favor preencha o campo Km Atual");
 						}
 						else if(Double.parseDouble(tempDistanciaDestino) < verificaDistanciaPercorrida()){
 							
 							notifica.toast(getApplicationContext(), "O Km Atual não pode ser maior que o Destino");	
 							
 						} else {
 
 							calculaFimViagem();
 
 							iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,
 									Tela1.mRowId);
 
 							startActivityForResult(iTela3, RETORNO_TELA2);
 
 						}
 					}
 				});
 
 		confirmarChegada.setOnClickListener(new View.OnClickListener() {
 																	
 					public void onClick(View v) {
 						
 						verificaDistanciaFinal2();
 						calculaFimViagem();
 						
 
 					}
 				});
 
 		novoAbastecimento.setOnClickListener(new View.OnClickListener() {
 																			
 					public void onClick(View v) {
 
 						startActivity(iNovoAbastecimento);
 
 					}
 				});
 
 		finalizarViagem.setOnClickListener(new View.OnClickListener() {// botão
 																		
 																		
 					public void onClick(View v) {
 						
 																				
 						if(!finalizaViagem()){//se exibir alguma notificação do finaliza viagem não executa o que tá dentro do if
 							verificaDistanciaFinal();	
 							Log.w(TELA2, "ESTROU no if");
 							Log.w(TELA2, "Valor do escolhe notificação"+escolheNotification);
 							
 						}
 						
 						
 					}
 				});
 	}
 
 	/**
 	 * Método responsável por calcular o fim da viagem esse método é usado tanto
 	 * para calcular o fim efetivo da viagem como a parcial da viagem
 	 */
 	private void calculaFimViagem() {
 
 		Cursor viagensCursor;// cursor usado pra pegar valores do banco de dados
 		viagensCursor = Tela1.mDbHelper.fetchNote(Tela1.mRowId);
 		startManagingCursor(viagensCursor);
 
 		Tempo time = new Tempo();
 
 		distanciaPercorrida = verificaDistanciaPercorrida();// verifica a distancia percorrida
 		
 		
 		horaAtual = time.horaCompleta();
 		diaChegada = time.getDia();
 		mesChegada = time.getMes();
 		anoChegada = time.getAno();
 
 		// usado pra pegar a hora do banco quebra em pedaços depois tranformar em double pra passar pro método intervaloTempo();
 		String horaBanco1 = "";
 		String minutoBanco1 = "";
 		String horaBanco2 = "";// na verdade pega o valor da variavel hora
 		String minutoBanco2 = "";
 		String auxHora1 = "";
 
 		try {// pega a hora de saida da viagem
 			auxHora1 = viagensCursor.getString(viagensCursor
 					.getColumnIndex(TravellerDbAdapter.KEY_HSAIDA));
 			horaBanco1 = auxHora1.substring(0, 2);
 
 			minutoBanco1 = auxHora1.substring(3, 5);
 
 		} catch (Exception e) {
 			Log.e(TELA2, "Deu pau na budega: " + e);
 		}
 
 		try {// pega a hora da chegada
 
 			horaBanco2 = horaAtual.substring(0, 2);
 
 			minutoBanco2 = horaAtual.substring(3, 5);
 
 		} catch (Exception e) {
 			Log.w(TELA2, "Deu pau na budega: " + e);
 		}
 
 		double hora1 = Double.parseDouble(horaBanco1);
 		double minuto1 = Double.parseDouble(minutoBanco1);
 		double hora2 = Double.parseDouble(horaBanco2);
 		double minuto2 = Double.parseDouble(minutoBanco2);
 
 		int ano1 = viagensCursor.getInt(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_ANO_SAIDA));
 		int mes1 = viagensCursor.getInt(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_MES_SAIDA));
 		int dia1 = viagensCursor.getInt(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_DIA_SAIDA));
 
 		int ano2 = anoChegada;
 		int mes2 = mesChegada;
 		int dia2 = diaChegada;
 
 		time.setIntervaloTempo(hora1, minuto1, dia1, mes1, ano1, hora2,
 				minuto2, dia2, mes2, ano2);
 		
 		
 		int intervaloHora = time.getIntervaloHora();
 		int intervaloMinuto = time.getIntervaloMin();
 		
 		Log.i(TELA2, "IntervaloHora: "+ intervaloHora);
 		Log.i(TELA2, "IntervaloHora: "+ intervaloMinuto);
 		Log.i(TELA2, "Distancia Percorrida: "+ distanciaPercorrida);
 
 		tempoViagem = time.pad(intervaloHora) + ":"
 				+ time.pad(intervaloMinuto);
 
 		velocidadeMedia = String.valueOf(calc.Vm(intervaloHora, intervaloMinuto, distanciaPercorrida));
 		
 		Log.i(TELA2, "Velocidade Média: "+ velocidadeMedia);
 
 		combustivelGasto = viagensCursor.getDouble(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_COMBUSTIVELGASTO));
 
 		mediaConsumo = (String.valueOf(calc.mediaConsumo(distanciaPercorrida,
 				combustivelGasto)));
 	}
 	
 	/**
 	 * Método responsável por fechar a viagem depois que já foi confirmada a chegada e depois do último abastecimento
 	 * 
 	 */
 	private void fechaViagem() {
 
 		Cursor viagensCursor;// cursor usado pra pegar valores do banco de dados
 		viagensCursor = Tela1.mDbHelper.fetchNote(Tela1.mRowId);
 		startManagingCursor(viagensCursor);
 
 
 		distanciaPercorrida = verificaDistanciaPercorrida();// verifica a distancia percorrida
 		
 		
 		combustivelGasto = viagensCursor.getDouble(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_COMBUSTIVELGASTO));
 
 		mediaConsumo = (String.valueOf(calc.mediaConsumo(distanciaPercorrida,
 				combustivelGasto)));
 		
 		escolheTela = 7;
 		
 		setResult(RESULT_OK);
 		
 		finish();
 		
 		final Intent iTela3 = new Intent(this, Tela3.class);// usado pra chamar a tela 3
 		
 		iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 		startActivityForResult(iTela3,RETORNO_TELA2);
 		
 	}
 
 	/**
 	 * Método responsável por retornar a distancia percorrida
 	 * 
 	 * @return distanciaPercorrida
 	 */
 	private double verificaDistanciaPercorrida() {
 
 		double distPercorrida, kmAtual, kmIncial;
 
 		Cursor viagensCursor;// cursor usado pra pegar valores do banco de dados
 		viagensCursor = Tela1.mDbHelper.fetchNote(Tela1.mRowId);
 		startManagingCursor(viagensCursor);
 
 		kmIncial = viagensCursor.getDouble(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_KM_INICIAL));// pegando o
 																	// km
 																	// inicial
 																	// do banco
 
 		if (etKmAtual.getText().toString().equals("")) {// se o campo do km
 														// atual estiver em
 														// branco
 			distPercorrida = 0;
 		} else {
 			kmAtual = Double.parseDouble(etKmAtual.getText().toString());
 			distPercorrida = (kmAtual - kmIncial);
 		}
 
 		return distPercorrida;
 	}
 
 	/**
 	 * Metodo responsavel por verificar se a distancia percorrida na hora de
 	 * finalizar a viagem é maior, menor ou igual a distancia do destino, se for
 	 * maior exibe um dialog pra o usuario confirmando se ele quer realmente
 	 * finalizar a viagem com o esse valor se for menor também exibe o dialog e
 	 * ser for igual não exibe nada e finaliza a viagem normalmente
 	 */
 	private void verificaDistanciaFinal() {//TODO: VERIFICAR O OUTRO BRACH DESSE MÉTODO, O BUG TÁ AQUI POIS TÁ INVOCANDO O MÉTODO VERIFICADISTGANCIAFINAL()
 
 		double destino = Double.parseDouble(tempDistanciaDestino);
 		double distanciaFinal = verificaDistanciaPercorrida();
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(Tela2.this);
 		final Intent iTela3 = new Intent(this, Tela3.class);// usado pra chamar a tela 3
 		
 		if ((etKmAtual.getText().toString().equals(""))) {// se// o// campo// km// estiver// em// branco// exibe// um// alerta
 
 			builder.setMessage(
 					"Por favor, preencha o Km Atual")
 					.setCancelable(false)
 					.setPositiveButton(
 							"Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(
 										DialogInterface dialog,
 										int id) {
 									dialog.cancel();// cancela o dialog
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		}else if (distanciaFinal > destino) {// se a distancia percorrida for maior que o destino e o usuario confirmar a distancia do destino, ficará com o valor da distancia percorrida
 
 			builder.setMessage(
 					"A distância final é maior que o destino, está certo disso?")
 					.setCancelable(false)
 					.setPositiveButton("Sim",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,int id) {
 									
 									Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 									if(escolheTela == 6){
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										
 										escolheTela = 69;
 										
 										calculaFimViagem();									
 
 										iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 										startActivityForResult(iTela3,RETORNO_TELA2);
 									}else{
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										fechaViagem();
 									}
 									
 
 								}
 							})
 					.setNegativeButton("Não",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else if (distanciaFinal < destino) {// se a distancia percorrida for menor que o destino e o usuario confirmar a distancia do destino,ficará com o valor da distancia percorrida
 
 			builder.setMessage(
 					"A distância final é menor que o destino, está certo disso?")
 					.setCancelable(false)
 					.setPositiveButton("Sim",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 									if(escolheTela == 6){
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										
 										escolheTela = 69;
 										
 										calculaFimViagem();									
 
 										iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 										startActivityForResult(iTela3,RETORNO_TELA2);
 									}else{
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										fechaViagem();
 									}
 								}
 							})
 					.setNegativeButton("Não",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else {
 			Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 			if(escolheTela == 6){
 				tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 				
 				escolheTela = 69;
 				
 				calculaFimViagem();									
 
 				iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 				startActivityForResult(iTela3,RETORNO_TELA2);
 			}else{
 				tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 				fechaViagem();
 			}
 
 		}
 	}
 	//**Mesmo método verificaDistanciaFinal só que sem o método calculadistanciafinal() que causava um bug de tempo
 	
 	private void verificaDistanciaFinal2() {
 
 		double destino = Double.parseDouble(tempDistanciaDestino);
 		double distanciaFinal = verificaDistanciaPercorrida();
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(Tela2.this);
 		final Intent iTela3 = new Intent(this, Tela3.class);// usado pra chamar a tela 3
 		
 		if ((etKmAtual.getText().toString().equals(""))) {// se// o// campo// km// estiver// em// branco// exibe// um// alerta
 
 			builder.setMessage(
 					"Por favor, preencha o Km Atual")
 					.setCancelable(false)
 					.setPositiveButton(
 							"Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(
 										DialogInterface dialog,
 										int id) {
 									dialog.cancel();// cancela o dialog
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 		}else if (distanciaFinal > destino) {// se a distancia percorrida for maior que o destino e o usuario confirmar a distancia do destino, ficará com o valor da distancia percorrida
 
 			builder.setMessage(
 					"A distância final é maior que o destino, está certo disso?")
 					.setCancelable(false)
 					.setPositiveButton("Sim",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,int id) {
 									
 									Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 									if(escolheTela == 6){
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										
 										escolheTela = 69;
 										
 										//calculaFimViagem();									
 
 										iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 										startActivityForResult(iTela3,RETORNO_TELA2);
 									}else{
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										fechaViagem();
 									}
 									
 
 								}
 							})
 					.setNegativeButton("Não",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else if (distanciaFinal < destino) {// se a distancia percorrida for menor que o destino e o usuario confirmar a distancia do destino,ficará com o valor da distancia percorrida
 
 			builder.setMessage(
 					"A distância final é menor que o destino, está certo disso?")
 					.setCancelable(false)
 					.setPositiveButton("Sim",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 									if(escolheTela == 6){
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										
 										escolheTela = 69;
 										
 										//calculaFimViagem();									
 
 										iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 										startActivityForResult(iTela3,RETORNO_TELA2);
 									}else{
 										tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 										fechaViagem();
 									}
 								}
 							})
 					.setNegativeButton("Não",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else {
 			Log.d(TELA2, "vALOR DO ESCOLHE TELA "+escolheTela);
 			if(escolheTela == 6){
 				tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 				
 				escolheTela = 69;
 				
 				//calculaFimViagem();									
 
 				iTela3.putExtra(TravellerDbAdapter.KEY_ROWID,Tela1.mRowId);
 
 				startActivityForResult(iTela3,RETORNO_TELA2);
 			}else{
 				tempDistanciaDestino = String.valueOf(verificaDistanciaPercorrida());
 				fechaViagem();
 			}
 
 		}
 	}
 
 	/**
 	 * Método responsavel por verificar se realmente o usuario quer finalizar a
 	 * viagem evitando assim finalizar a viagem sem querer
 	 */
 	private boolean finalizaViagem() {
 		
 		Cursor viagensCursor;// cursor usado pra pegar valores do banco de dados
 		viagensCursor = Tela1.mDbHelper.fetchNote(Tela1.mRowId);
 		startManagingCursor(viagensCursor);
 		
 		combustivelGasto = viagensCursor.getDouble(viagensCursor
 				.getColumnIndex(TravellerDbAdapter.KEY_COMBUSTIVELGASTO));
 		
 		
 		 if(escolheTela != 69){
 			AlertDialog.Builder builder = new AlertDialog.Builder(Tela2.this);
 			builder.setMessage(
 					"Por favor confirme a chegada ao destino antes de finalizar a viagem")
 					.setCancelable(false)
 					.setPositiveButton(
 							"Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(
 										DialogInterface dialog,
 										int id) {
 									escolheNotification = true;
 									Log.d(TELA2, "ESTROU NA PRIMEIRA NOTIFICAÇÃO");
 									Log.d(TELA2, "Valor do escolhe notificação"+escolheNotification);
 									dialog.cancel();// cancela o dialog
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		
 			
 			
 		}else if(combustivelGasto == 0){
 			AlertDialog.Builder builder = new AlertDialog.Builder(Tela2.this);
 			builder.setMessage(
 					"Por favor faça o último abastecimento para finalizar a viagem")
 					.setCancelable(false)
 					.setPositiveButton(
 							"Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(
 										DialogInterface dialog,
 										int id) {
 									escolheNotification = true;
 									dialog.cancel();// cancela o dialog
 									Log.v(TELA2, "Valor de finaliza viagem"+escolheNotification);
 									Log.v(TELA2, "ESTROU NA SEGUNDA NOTIFICAÇÃO");
 								}
 							});
 
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		}else{
 			
 			return false;
 			
 			
 		}
 		
 		
 		
 
 	}
 	
 	
 /**Método que prenche os editText saida, distanciaDestino e kmAtual
  * 
  */
 	private void populateFieldsTela2() {// pega os campos do bando de dados e
 										// mostra na tela
 		if (Tela1.mRowId != null) {
 			Cursor viagem = Tela1.mDbHelper.fetchNoteTela2(Tela1.mRowId);
 			startManagingCursor(viagem);
 			saida.setText(viagem.getString(viagem
 					.getColumnIndexOrThrow(TravellerDbAdapter.KEY_HSAIDA)));
 			destino.setText(viagem.getString(viagem
 					.getColumnIndexOrThrow(TravellerDbAdapter.KEY_DESTINO)));
 			distanciaDestino.setText(viagem.getString(viagem
 					.getColumnIndexOrThrow(TravellerDbAdapter.KEY_DISTANCIA)));
 			etKmAtual.setText(viagem.getString(viagem
 					.getColumnIndexOrThrow(TravellerDbAdapter.KEY_KM_ATUAL)));
 
 		}
 	}
 	
 	
 	/**Método que retorna o numero da tela armazenada no banco
 	 * 
 	 * @return numero da tela
 	 */
 	private int getTela(){
 		if (Tela1.mRowId != null) {
 			Cursor viagem = Tela1.mDbHelper.fetchNoteTela2(Tela1.mRowId);
 			startManagingCursor(viagem);
 			
 			escolheTeladoBanco = viagem.getInt(viagem.getColumnIndexOrThrow(TravellerDbAdapter.KEY_ESCOLHETELA));
 		}
 		return escolheTeladoBanco;
 		
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState();
 		outState.putSerializable(TravellerDbAdapter.KEY_ROWID, Tela1.mRowId);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d(TELA2, "Executou o onPause:");
 		saveState();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(TELA2, "Executou o onResume:");
 		populateFieldsTela2();
		if (escolheTela == 69) {
 			//etKmAtual.setEnabled(false);
 			confirmarChegada.setEnabled(false);
 			parcialViagem.setEnabled(false);
 		}
 		
 	}
 	
 	
 	/**Método que atualiza e salva as informações no banco
 	 * 
 	 */
 	private void saveState() {// atualiza o banco de dados salvando
 								// informações da tela
 
 		String sDistancia = tempDistanciaDestino;
 		String sDistanciaPercorrida = String.valueOf(verificaDistanciaPercorrida());
 		String sKmAtual = etKmAtual.getText().toString();
 		String sHoraChegada = horaAtual;
 		String sTempoViagem = tempoViagem;
 		String sVelocidadeMedia = velocidadeMedia;
 		String sMediaConsumo = mediaConsumo;
 		int sDiaChegada = diaChegada;
 		int sMesChegada = mesChegada;
 		int sAnoChegada = anoChegada;
 
 		/*Log.e(TELA2, "distancia s: " + sDistancia);
 		Log.e(TELA2, "distancia percorrida: " + sDistanciaPercorrida);
 		Log.e(TELA2, "km atual s: " + sKmAtual);
 		Log.e(TELA2, "hora chegada s: " + sHoraChegada);
 		Log.e(TELA2, "tempo viagem s: " + sTempoViagem);
 		Log.e(TELA2, "velocidade media s: " + sVelocidadeMedia);
 		Log.e(TELA2, "Valor dia chegada s: " + sDiaChegada);
 		Log.e(TELA2, "Valor mes chegada s: " + sMesChegada);
 		Log.e(TELA2, "Valor ano chegada s: " + sAnoChegada);
 		*/
 		int sEscolheTela = escolheTela;// recebe o valor de escolhe tela que
 										// fica no button salvar
 		Log.e(TELA2, "escolhe tela: " + sEscolheTela);
 
 		Tela1.mDbHelper.updateNoteTela2(Tela1.mRowId, sDistancia,
 				sDistanciaPercorrida, sKmAtual, sHoraChegada, sTempoViagem,
 				sVelocidadeMedia, sMediaConsumo, sEscolheTela, sDiaChegada,
 				sMesChegada, sAnoChegada);
 		Log.e(TELA2, "mbdHelper: " + Tela1.mDbHelper);
 		Log.e(TELA2, "mRowId: " + Tela1.mRowId);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		// nÃ£o to fazendo nada aqui
 	}
 	
 	
 }
