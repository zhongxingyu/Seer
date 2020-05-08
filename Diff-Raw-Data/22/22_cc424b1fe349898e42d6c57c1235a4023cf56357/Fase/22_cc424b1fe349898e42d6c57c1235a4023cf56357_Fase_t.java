 package jogo;
 
 public class Fase {
 
 	private int altura;
 	private int largura;
 	private boolean completo;// atributo se passou da fase ou nao
 	private int qtd_plataforma;
 	public Inimigo inimigo[];
 	public Player player;
 	public Cenario cenario;
 
	public Fase() {
 		this.altura = 0;
 		this.largura = 0;
 		this.completo = false;
 		this.qtd_plataforma = 0;
 	}

	public Fase(int altura, int largura, boolean completo, int qtd_plataforma, Inimigo[] inimigo, Player player,
			Cenario cenario) {
 		this.altura = altura;
 		this.largura = largura;
 		this.completo = completo;
 		this.qtd_plataforma = qtd_plataforma;
		this.inimigo = inimigo;
		this.player = player;
		this.cenario = cenario;
 	}
 
 	public int getAltura() {
 		return altura;
 	}
 
 	public void setAltura(int altura) {
 		this.altura = altura;
 	}
 
 	public int getLargura() {
 		return largura;
 	}
 
 	public void setLargura(int largura) {
 		this.largura = largura;
 	}
 
 	public boolean getCompleto() {
 		return completo;
 	}
 
 	public void setCompleto(boolean completo) {
 		this.completo = completo;
 	}
 
 	public int getQtd_plataforma() {
 		return qtd_plataforma;
 	}
 
 	public void setQtd_plataforma(int qtd_plataforma) {
 		this.qtd_plataforma = qtd_plataforma;
 	}
 
 	public Inimigo[] getInimigos() {
 		return inimigo;
 	}
 
 	public void setInimigos(Inimigo[] inimigo) {
 		this.inimigo = inimigo;
 	}

 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}

 	public Cenario getCenario() {
 		return cenario;
 	}
 
 	public void setCenario(Cenario cenario) {
 		this.cenario = cenario;
 	}
 
 }
