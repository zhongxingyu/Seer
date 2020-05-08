 package pig;
 
 
 /**
  * @author vitor
  *
  */
 public class Tabuleiro {
 	
 	protected Jogador jogadorLocal;
 	protected Jogador jogadorRemoto;
 	protected boolean partidaEmAndamento;
 	protected int pontuacaoLance;
 	protected Dado dado1;
 	protected Dado dado2;
 	
 	public Tabuleiro() { }
 
 	public Jogador getJogadorLocal() {
 		return jogadorLocal;
 	}
 
 	public void setJogadorLocal(Jogador jogadorLocal) {
 		this.jogadorLocal = jogadorLocal;
 	}
 
 	public Jogador getJogadorRemoto() {
 		return jogadorRemoto;
 	}
 
 	public void setJogadorRemoto(Jogador jogadorRemoto) {
 		this.jogadorRemoto = jogadorRemoto;
 	}
 
 	public boolean isPartidaEmAndamento() {
 		return partidaEmAndamento;
 	}
 
 	public void setPartidaEmAndamento(boolean partidaEmAndamento) {
 		this.partidaEmAndamento = partidaEmAndamento;
 	}
 
 	public int getPontuacaoLance() {
 		return pontuacaoLance;
 	}
 
 	public void setPontuacaoLance(int pontuacaoLance) {
 		this.pontuacaoLance = pontuacaoLance;
 	}
 	
 	public void criarJogador(String nome, TipoPorco tipo) {
 		
 		if ((this.jogadorLocal != null) && (this.jogadorRemoto != null)) {
 			this.jogadorRemoto = null;
 			this.jogadorLocal = new Jogador(nome, tipo);
 		} else {
 			if (this.jogadorLocal == null) {
 				this.jogadorLocal = new Jogador(nome, tipo);
 			} else {
 				this.jogadorRemoto = new Jogador(nome, tipo);
 			}
 		}
 	}
 
 	public void definirOPrimeiro(boolean primeiro) {
 
 		setPartidaEmAndamento(true);
 		this.jogadorLocal.reiniciar();
 		this.jogadorRemoto.reiniciar();
 		if (primeiro) {
 			this.jogadorLocal.setDaVez(true);
 		} else {
 			this.jogadorRemoto.setDaVez(true);
 		}
 	}
 
 	public void iniciar() {
 		
 		this.dado1 = new Dado();
 		this.dado2 = new Dado();
 		this.partidaEmAndamento = true;
 		this.jogadorLocal.setPontuacao(0);
 		this.jogadorRemoto.setPontuacao(0);
 	}
 	
 	public Jogador informarJogadorDaVez() {
 		
 		boolean vezLocal = this.jogadorLocal.isDaVez();
 		
 		if (vezLocal) {
 			return this.jogadorLocal;
 		} else {
 			return this.jogadorRemoto;
 		}
 		
 	}
 
 	public void alternarVez(boolean haVencedor, boolean remetenteServidor) {
 		if (haVencedor) {
 			jogadorLocal.setDaVez(false);
 			jogadorRemoto.setDaVez(false);
 		} else if (remetenteServidor) {
 			jogadorLocal.setDaVez(true);
 			jogadorRemoto.setDaVez(false);
 		} else {
 			jogadorLocal.setDaVez(false);
 			jogadorRemoto.setDaVez(true);
 		}
 	}
 	
 	public int procederLance(boolean realizarJogada) {
 
 		if (realizarJogada) {
 			int valorDado1 = dado1.rolar();
 			int valorDado2 = dado2.rolar();
 			if(valorDado1 == 1 && valorDado2 == 1) {				
 				pontuacaoLance = 0;
 				return Situacao.PERDEU_TUDO;									 // 3 - zera pontuacao do jogador
 			} else if(valorDado1 == 1 || valorDado2 == 1) {
 				pontuacaoLance = 0;
 				return Situacao.PERDEU;                                    // 2 - zera pontuacao da vez
 			} else {
 				pontuacaoLance += valorDado1 + valorDado2;
 				return Situacao.JOGADA;                                    // 1 - atualiza pontuacao da vez 
 			}	
 		} else {
 			return Situacao.PASSOU;      									// 0 - atualiza pontuacao do jogador e passa
 		}
 	}
 	
 	public int tratarLance(int pontuacaoDaVez, int dado1, int dado2, int situacao, boolean doServidor){
 
 		this.dado1.setValor(dado1);
 		this.dado2.setValor(dado2);
 		switch(situacao) {
 
 		case Situacao.PASSOU:   // 0 - atualiza pontuacao do jogador e passa
 			boolean vezLocal = jogadorLocal.isDaVez(); 
 			if (vezLocal) {
 				int pontosVez = jogadorLocal.getPontuacao() + pontuacaoDaVez;
 				jogadorLocal.setPontuacao(pontosVez);
 			} else {
 				int pontosLance = jogadorRemoto.getPontuacao() + pontuacaoLance;
 				jogadorRemoto.setPontuacao(pontosLance);
 			}
			this.pontuacaoLance = pontuacaoDaVez;
 			alternarVez(verificaVencedor(),doServidor);			
 			break;
 
 		case Situacao.JOGADA:   // 1 - atualiza pontuacao da vez
			this.pontuacaoLance = pontuacaoDaVez;
 			break;
 
 		case Situacao.PERDEU:   // 2 - zera pontuacao da vez
			this.pontuacaoLance = pontuacaoDaVez;
 			alternarVez(verificaVencedor(),doServidor);
 			break;
 
 		case Situacao.PERDEU_TUDO:   // 3 - zera pontuacao do jogador
 			boolean vezJogadorLocal = jogadorLocal.isDaVez(); 
 			if (vezJogadorLocal) {
 				jogadorLocal.setPontuacao(0);
 			} else {
 				jogadorRemoto.setPontuacao(0);
 			}
			this.pontuacaoLance = pontuacaoDaVez;
 			alternarVez(verificaVencedor(),doServidor);
 			break;
 
 		default:
 			break;
 		}
 				
 		return situacao;		
 	}
 	
 	public boolean verificaVencedor(){
 		
 		boolean vezLocal = jogadorLocal.isDaVez(); 
 		
 		if (vezLocal 
 				&& (jogadorLocal.getPontuacao() + pontuacaoLance >=50
 				|| jogadorLocal.getPontuacao() >= 50)){
 			jogadorLocal.setVencedor(true);
 			setPartidaEmAndamento(false);
 			return true;
 		} else if(!vezLocal && 
 				(jogadorRemoto.getPontuacao() + pontuacaoLance >=50
 				|| jogadorRemoto.getPontuacao() >= 50)) {
 			jogadorRemoto.setVencedor(true);
 			setPartidaEmAndamento(false);
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
