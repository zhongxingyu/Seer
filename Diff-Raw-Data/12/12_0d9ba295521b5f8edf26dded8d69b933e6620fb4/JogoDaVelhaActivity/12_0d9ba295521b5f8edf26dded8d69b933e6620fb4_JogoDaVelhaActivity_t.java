 package br.com.jugms.jogodavelha;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 
 public class JogoDaVelhaActivity extends Activity {
     /** Called when the activity is first created. */
     
 	private TextView jogador;
 	private ImageButton btn1;
 	private ImageButton btn2;
 	private ImageButton btn3;
 	private ImageButton btn4;
 	private ImageButton btn5;
 	private ImageButton btn6;
 	private ImageButton btn7;
 	private ImageButton btn8;
 	private ImageButton btn9;
 	private Button btnNovoJogo;
 	private int[] pontos;
 	private Button btnRetry;
 	private Button btnRetry2;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         comecarJogo();
     }
 	
 	private void comecarJogo(){
 		pontos = zerarTudo();
         
         jogador = (TextView) findViewById(R.id.textView);
         btn1 = (ImageButton) findViewById(R.id.btn_1);
         btn2 = (ImageButton) findViewById(R.id.btn_2);
         btn3 = (ImageButton) findViewById(R.id.btn_3);
         btn4 = (ImageButton) findViewById(R.id.btn_4);
         btn5 = (ImageButton) findViewById(R.id.btn_5);
         btn6 = (ImageButton) findViewById(R.id.btn_6);
         btn7 = (ImageButton) findViewById(R.id.btn_7);
         btn8 = (ImageButton) findViewById(R.id.btn_8);
         btn9 = (ImageButton) findViewById(R.id.btn_9);
         btnNovoJogo = (Button) findViewById(R.id.novo_jogo);
         
         
         btn1.setOnClickListener(click(btn1));
         btn2.setOnClickListener(click(btn2));
         btn3.setOnClickListener(click(btn3));
         btn4.setOnClickListener(click(btn4));
         btn5.setOnClickListener(click(btn5));
         btn6.setOnClickListener(click(btn6));
         btn7.setOnClickListener(click(btn7));
         btn8.setOnClickListener(click(btn8));
         btn9.setOnClickListener(click(btn9));
         btnNovoJogo.setOnClickListener(novoJogo());
 	}
 
 	private OnClickListener retry() {
 		return new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				setContentView(R.layout.main);
 				setBotoesComoVazio();
 				jogador.setText("Jogador 1");
 				setBotoesClickableTrue();
 				comecarJogo();
 			}
 		};
 	}
 
 	private int[] zerarTudo() {
 		int[] pontos = {0,0,0,0,0,0,0,0,0};
 		return pontos;
 	}
 
 	private OnClickListener click(final ImageButton btn) {
 		return new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				if(isJogador1()){
 					btn.setBackgroundResource(R.drawable.x);
 					marcarPontoJogador(btn);
 					if(verificaGanhador(1))
 						jogador1Ganho();
					else if (deuVelha()){
						empate();
					}
 				} else {
 					btn.setBackgroundResource(R.drawable.circulo);
 					marcarPontoJogador(btn);
 					if(verificaGanhador(2))
 						jogador2Ganho();
					else if (deuVelha()){
						empate();
					}
 				}
				
 				trocaJogador();
 				btn.setClickable(false);
 			}
 
 		};
 	}
 	
 	private void empate() {
 		setContentView(R.layout.empate);
 		btnRetry2 = (Button) findViewById(R.id.retry_2);
 		btnRetry2.setOnClickListener(retry());
 	}
 
 	
 	private boolean deuVelha() {
 		boolean retorno = true;
 		
 		for(int i=0;i<9;i++){
 			if(pontos[i]==0)
 				retorno = false;
 		}
 		
 		return retorno;
 	}
 	
 	private OnClickListener novoJogo(){
 		return new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				setBotoesComoVazio();
 				jogador.setText("Jogador 1");
 				setBotoesClickableTrue();
 				pontos = zerarTudo();
 			}
 		};
 	}
 	
 	private void marcarPontoJogador(ImageButton btn){
 		
 		int var;
 		if(isJogador1())
 			var = 1;
 		else
 			var = 2;
 		
 		if(btn.equals(btn1)){
 			pontos[0] = var;
 		} else if (btn.equals(btn2)){
 			pontos[1] = var;
 		} else if (btn.equals(btn3)){
 			pontos[2] = var;
 		} else if (btn.equals(btn4)){
 			pontos[3] = var;
 		} else if (btn.equals(btn5)){
 			pontos[4] = var;
 		} else if (btn.equals(btn6)){
 			pontos[5] = var;
 		} else if (btn.equals(btn7)){
 			pontos[6] = var;
 		} else if (btn.equals(btn8)){
 			pontos[7] = var;
 		} else if (btn.equals(btn9)){
 			pontos[8] = var;
 		}
 		
 	}
 	
 	private void setBotoesComoVazio() {
 		btn1.setBackgroundResource(R.drawable.branco);
 		btn2.setBackgroundResource(R.drawable.branco);
 		btn3.setBackgroundResource(R.drawable.branco);
 		btn4.setBackgroundResource(R.drawable.branco);
 		btn5.setBackgroundResource(R.drawable.branco);
 		btn6.setBackgroundResource(R.drawable.branco);
 		btn7.setBackgroundResource(R.drawable.branco);
 		btn8.setBackgroundResource(R.drawable.branco);
 		btn9.setBackgroundResource(R.drawable.branco);
 	}
 	
 	private void setBotoesClickableTrue(){
 		btn1.setClickable(true);
 		btn2.setClickable(true);
 		btn3.setClickable(true);
 		btn4.setClickable(true);
 		btn5.setClickable(true);
 		btn6.setClickable(true);
 		btn7.setClickable(true);
 		btn8.setClickable(true);
 		btn9.setClickable(true);		
 	}
 	
 	private void setBotoesClickableFalse(){
 		btn1.setClickable(false);
 		btn2.setClickable(false);
 		btn3.setClickable(false);
 		btn4.setClickable(false);
 		btn5.setClickable(false);
 		btn6.setClickable(false);
 		btn7.setClickable(false);
 		btn8.setClickable(false);
 		btn9.setClickable(false);		
 	}
 	
 	private boolean verificaGanhador(int n){
 		
 		if(pontos[0]==n && pontos[1]==n && pontos[2]==n)
 			return true;
 		if(pontos[0]==n && pontos[4]==n && pontos[8]==n)
 			return true;
 		if(pontos[0]==n && pontos[3]==n && pontos[6]==n)
 			return true;
 		if(pontos[1]==n && pontos[4]==n && pontos[7]==n)
 			return true;
 		if(pontos[2]==n && pontos[5]==n && pontos[8]==n)
 			return true;
 		if(pontos[2]==n && pontos[4]==n && pontos[6]==n)
 			return true;
 		if(pontos[3]==n && pontos[4]==n && pontos[5]==n)
 			return true;
 		if(pontos[6]==n && pontos[7]==n && pontos[8]==n)
 			return true;
 		
 		
 		return false;
 	}
 	
 	private boolean isJogador1() {
 		String txt = (String) jogador.getText();
 		if(txt.equals("Jogador 1"))
 			return true;
 		else
 			return false;
 	}
 	
 	private void trocaJogador(){
 		if(isJogador1())
 			jogador.setText("Jogador 2");
 		else
 			jogador.setText("Jogador 1");
 	}
 	
 	private void jogador2Ganho() {
 		setBotoesClickableFalse();
 		setContentView(R.layout.game_over);
 		TextView jogadorNome = (TextView) findViewById(R.id.vencedor);
 		jogadorNome.setText("Jogador 2 win's");
 		btnRetry = (Button) findViewById(R.id.btn_retry);
         btnRetry.setOnClickListener(retry());
 		
 	}
 
 	private void jogador1Ganho() {
 		setContentView(R.layout.game_over);
 		TextView jogadorNome = (TextView) findViewById(R.id.vencedor);
 		jogadorNome.setText("Jogador 1 win's");
 		btnRetry = (Button) findViewById(R.id.btn_retry);
         btnRetry.setOnClickListener(retry());
 	}
 
 }
