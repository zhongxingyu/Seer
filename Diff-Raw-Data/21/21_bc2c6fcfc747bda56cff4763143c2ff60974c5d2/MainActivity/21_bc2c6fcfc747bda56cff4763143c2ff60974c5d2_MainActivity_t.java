 package br.android.olhai;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.MenuInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.TextView;
import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnTouchListener {
	private static final int ESPACO_DE_ARRASTE = 30;

 	private SharedPreferences preferences;
 
 	CardapioSemana cardapioDaSemana;
 	int diaDaSemana = 0;
 	int universidadeSelecionada = 1;
	private float downEventX, downEventY;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setOnTouchListeners();
 		// Get the xml/preferences.xml preferences
 		this.preferences = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		int idUniversidadeSelecionada = getIdUniversidadeSelecionada();
 		cardapioDaSemana = new CardapioSemana();
 		mostrarData();
 		if (idUniversidadeSelecionada == 0) {
 			executandoPelaPrimeiraVez();
 		}
 	}
 
 	private void setOnTouchListeners() {
 		findViewById(R.id.layoutGlobal).setOnTouchListener(
 				(OnTouchListener) this);
 		findViewById(R.id.cardapioSegunda).setOnTouchListener(
 				(OnTouchListener) this);
 		findViewById(R.id.cardapioTerca).setOnTouchListener(
 				(OnTouchListener) this);
 		findViewById(R.id.cardapioQuarta).setOnTouchListener(
 				(OnTouchListener) this);
 		findViewById(R.id.cardapioQuinta).setOnTouchListener(
 				(OnTouchListener) this);
 		findViewById(R.id.cardapioSexta).setOnTouchListener(
 				(OnTouchListener) this);
 	}
 
 	@Override
 	protected void onResume() {
 		requistarCardapio();
 		setAdapters();
 		super.onResume();
 	}
 
 	/**
 	 * Método responsável por exibir a primeira configuração do usuário onde ele
 	 * irá escolher qual universidade ele deseja seguir;
 	 * 
 	 * @author thiagodnf
 	 */
 	private void executandoPelaPrimeiraVez() {
 		final String items[] = getResources().getStringArray(
 				R.array.universidadesParticipantes_descricao);
 
 		AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
 		ab.setTitle("Universidade");
 
 		/*
 		 * Callback executado SOMENTE quando o usuário clica em algum elemento
 		 * da lista
 		 */
 		ab.setSingleChoiceItems(items, 0,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,
 							int itemSelecionado) {
 						MainActivity.this.universidadeSelecionada = itemSelecionado + 1;
 					}
 				});
 
 		ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				/* Salva a escolha nas preferências */
 				SharedPreferences.Editor editor = MainActivity.this.preferences
 						.edit();
 				String idUniversidade = String
 						.valueOf(MainActivity.this.universidadeSelecionada);
 				editor.putString("universidadesParticipantesListPreference",
 						idUniversidade);
 
 				editor.commit();
 			}
 		});
 
 		ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				MainActivity.this.finish();
 			}
 		});
 
 		/* Quando o usuário clica no botão voltar */
 		ab.setOnCancelListener(new DialogInterface.OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				MainActivity.this.finish();
 			}
 		});
 
 		ab.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(android.view.Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_principal, menu);
 		return true;
 	};
 
 	@Override
 	public boolean onOptionsItemSelected(android.view.MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuConfiguracoes:
 
 			Intent settingsActivity = new Intent(getBaseContext(),
 					Preferences.class);
 
 			startActivity(settingsActivity);
 			break;
 		case R.id.menuSobre:
 			Dialog dialog = new Dialog(MainActivity.this);
 
 			dialog.setContentView(R.layout.menu_sobre);
 			dialog.setTitle("Sobre");
 			dialog.setCancelable(true);
 
 			dialog.show();
 			break;
 		default:
 			Log.e("Erro", "Nenhuma opção selecionada no Menu Principal");
 			break;
 		}
 		return true;
 	};
 
 	/**
 	 * Verifica se o usuário deixou a sincronização automática
 	 * 
 	 * @author thiagodnf
 	 * @return Verdadeiro caso o checkbox esteja ativa. Falso, otherwise
 	 */
 	@SuppressWarnings("unused")
 	private boolean isCardapioAutomatico() {
 		if (this.preferences != null) {
 			return this.preferences.getBoolean("cardapioAutomaticoCheckBox",
 					true);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Retorna o id da Universidade Selecionada
 	 * 
 	 * @return Retorna 0 Caso não tenha sido selecionado, e retorna o Id caso
 	 *         tenha sido selecionado
 	 * @author thiagodnf *
 	 */
 	private int getIdUniversidadeSelecionada() {
 		if (this.preferences != null) {
 			String idUniversidade = this.preferences.getString(
 					"universidadesParticipantesListPreference", "0");
 			return Integer.parseInt(idUniversidade);
 		}
 		return 0;
 	}
 
 	private void setAdapters() {
 		((ListaCardapio) findViewById(R.id.cardapioSegunda))
 				.setInformaçõesPratos(cardapioDaSemana.getDscNomePrato(0),
 						cardapioDaSemana.getDetalhesPrato(0));
 		((ListaCardapio) findViewById(R.id.cardapioTerca))
 				.setInformaçõesPratos(cardapioDaSemana.getDscNomePrato(1),
 						cardapioDaSemana.getDetalhesPrato(1));
 		((ListaCardapio) findViewById(R.id.cardapioQuarta))
 				.setInformaçõesPratos(cardapioDaSemana.getDscNomePrato(2),
 						cardapioDaSemana.getDetalhesPrato(2));
 		((ListaCardapio) findViewById(R.id.cardapioQuinta))
 				.setInformaçõesPratos(cardapioDaSemana.getDscNomePrato(3),
 						cardapioDaSemana.getDetalhesPrato(3));
 		((ListaCardapio) findViewById(R.id.cardapioSexta))
 				.setInformaçõesPratos(cardapioDaSemana.getDscNomePrato(4),
 						cardapioDaSemana.getDetalhesPrato(4));
 	}
 
 	private void mostrarData() {
 		TextView dia = ((TextView) findViewById(R.id.textViewHoje));
 		switch (diaDaSemana) {
 		case 0:
 			dia.setText("Segunda-feira");
 			break;
 		case 1:
 			dia.setText("Terça-feira");
 			break;
 		case 2:
 			dia.setText("Quarta-feira");
 			break;
 		case 3:
 			dia.setText("Quinta-feira");
 			break;
 		case 4:
 			dia.setText("Sexta-feira");
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void requistarCardapio() {
 		try {
 			cardapioDaSemana.carregarDeArquivo();
 		} catch (Exception e) {
 			Log.i("JSON", e.getMessage());
 		}
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			downEventX = event.getX();
			downEventY = event.getY();
 			break;
 		case MotionEvent.ACTION_UP:
			float upEventY = event.getY();
			if (Math.abs(downEventY - upEventY) > ESPACO_DE_ARRASTE) {
				return false;
			}
 			float upEventX = event.getX();
			if (downEventX - ESPACO_DE_ARRASTE > upEventX) {
 				if (diaDaSemana < 4) {
 					ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperCardapio);
 					vf.setInAnimation(inFromRightAnimation());
 					vf.setOutAnimation(outToLeftAnimation());
 					vf.showNext();
 					diaDaSemana++;
 				}
			} else if (downEventX + ESPACO_DE_ARRASTE < upEventX) {
 				if (diaDaSemana > 0) {
 					ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipperCardapio);
 					vf.setInAnimation(inFromLeftAnimation());
 					vf.setOutAnimation(outToRightAnimation());
 					vf.showPrevious();
 					diaDaSemana--;
 				}
 			} else {
 				return false;
 			}
 			mostrarData();
 			return true;
 
 		default:
 			break;
 		}
 		return false;
 	}
 
 	private Animation inFromRightAnimation() {
 
 		Animation inFromRight = new TranslateAnimation(
 				Animation.RELATIVE_TO_PARENT, +1.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f);
 		inFromRight.setDuration(500);
 		inFromRight.setInterpolator(new AccelerateInterpolator());
 		return inFromRight;
 	}
 
 	private Animation outToLeftAnimation() {
 		Animation outtoLeft = new TranslateAnimation(
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, -1.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f);
 		outtoLeft.setDuration(500);
 		outtoLeft.setInterpolator(new AccelerateInterpolator());
 		return outtoLeft;
 	}
 
 	private Animation inFromLeftAnimation() {
 		Animation inFromLeft = new TranslateAnimation(
 				Animation.RELATIVE_TO_PARENT, -1.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f);
 		inFromLeft.setDuration(500);
 		inFromLeft.setInterpolator(new AccelerateInterpolator());
 		return inFromLeft;
 	}
 
 	private Animation outToRightAnimation() {
 		Animation outtoRight = new TranslateAnimation(
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, +1.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f,
 				Animation.RELATIVE_TO_PARENT, 0.0f);
 		outtoRight.setDuration(500);
 		outtoRight.setInterpolator(new AccelerateInterpolator());
 		return outtoRight;
 	}
 }
