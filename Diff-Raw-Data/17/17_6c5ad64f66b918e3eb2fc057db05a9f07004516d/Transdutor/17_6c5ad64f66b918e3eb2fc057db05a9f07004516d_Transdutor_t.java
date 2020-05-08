 package compilador.analisador.lexico;
 
 public class Transdutor {
 	
 	/**
 	 * Indica um estado invlido.
 	 */
 	public static final int ESTADO_INVALIDO = -1;
 	
 	/**
 	 * Indica o estado inicial de qualquer autmato.
 	 */
 	public static final int ESTADO_INICIAL = 0;
 	
 	/**
 	 * Indica um estado que trata comentrios do cdigo-fonte.
 	 */
 	public static final int ESTADO_COMENTARIO = 4; //TODO: deixar mais flexvel.
 	
 	/**
 	 * Nmero de estados do autmato.
 	 */
 	private int estados;
 	
 	/**
 	 * Indica o estado atual no qual o autmato se encontra.
 	 */
 	private int estadoAtual;
 	
 	/**
 	 * Tabela com as transies do autmato.
 	 */
 	private int[][] tabelaTransicao;
 	
 	/**
 	 * Tabela contendo as classes de tokens para cada estado do autmato.
 	 * Se o estado no for final, a classe adotada ser a <code>Token.CLASSE_TOKEN_INVALIDO</code>.
 	 */
 	private int[] tabelaClasses;
 	
 	/**
 	 * Indica se o transdutor est ou no no estado que trata comentrios.
 	 */
 	private boolean estadoComentario;
 	
 	public Transdutor() {
 		this.carregaTabelaTransicao();
 		this.carregaTabelaClasses();
 		this.estadoAtual = ESTADO_INICIAL;
 		this.estadoComentario = false;
 	}
 	
 	/**
 	 * Carrega as tabela de transio de estados.
 	 */
 	private void carregaTabelaTransicao() {
 		this.estados = 8;
 		this.tabelaTransicao = new int[this.estados][256];
 		
 		for(int i = 0; i < this.estados; i++) {
 			for(int j = 0; j < 256; j++) {
 				this.tabelaTransicao[i][j] = -1;
 			}
 		}
 		
 		/* Transies do estado 0. */
 		for(int i = (int)'A'; i <= (int)'Z'; i++) {
 			this.tabelaTransicao[0][i] = 1;
 		}
 		
 		for(int i = (int)'a'; i <= (int)'z'; i++) {
 			this.tabelaTransicao[0][i] = 1;
 		}
 		
 		for(int i = (int)'0'; i <= (int)'9'; i++) {
 			this.tabelaTransicao[0][i] = 2;
 		}
 		
 		this.tabelaTransicao[0][(int)' '] = 0;
 		this.tabelaTransicao[0][(int)'\n'] = 0;
 		this.tabelaTransicao[0][(int)'\t'] = 0;
 		
 		this.tabelaTransicao[0][(int)';'] = 3;
 		this.tabelaTransicao[0][(int)','] = 3;
 		this.tabelaTransicao[0][(int)'+'] = 3;
 		this.tabelaTransicao[0][(int)'-'] = 3;
 		this.tabelaTransicao[0][(int)'*'] = 3;
 		this.tabelaTransicao[0][(int)'/'] = 3;
 		this.tabelaTransicao[0][(int)'('] = 3;
 		this.tabelaTransicao[0][(int)')'] = 3;
		this.tabelaTransicao[0][(int)'['] = 3;
		this.tabelaTransicao[0][(int)']'] = 3;
 		this.tabelaTransicao[0][(int)'{'] = 3;
 		this.tabelaTransicao[0][(int)'}'] = 3;
 		this.tabelaTransicao[0][(int)'.'] = 3;
 		this.tabelaTransicao[0][(int)'>'] = 3;
 		this.tabelaTransicao[0][(int)'='] = 3;
 		this.tabelaTransicao[0][(int)'<'] = 3;
 		this.tabelaTransicao[0][(int)'%'] = 3;
 		this.tabelaTransicao[0][(int)'!'] = 3;
 		
 		this.tabelaTransicao[0][(int)'"'] = 6;
 		
 		for(int i = 0; i < 256; i++) {
 			if(this.tabelaTransicao[0][i] == -1)
 				this.tabelaTransicao[0][i] = 5;
 		}
 		
 		/* Transies do estado 1. */
 		for(int i = (int)'A'; i <= (int)'Z'; i++) {
 			this.tabelaTransicao[1][i] = 1;
 		}
 		
 		for(int i = (int)'a'; i <= (int)'z'; i++) {
 			this.tabelaTransicao[1][i] = 1;
 		}
 		
 		for(int i = (int)'0'; i <= (int)'9'; i++) {
 			this.tabelaTransicao[1][i] = 1;
 		}
 		
 		/* Transies do estado 2 */
 		for(int i = (int)'0'; i <= (int)'9'; i++) {
 			this.tabelaTransicao[2][i] = 2;
 		}
 		
 		/* Transies do estado 3. */
 		this.tabelaTransicao[3][(int)'/'] = 4;
 		
 		this.tabelaTransicao[3][(int)'='] = 1; // Para o caso de ==, <=, >= e !=.
 		
 		/* Transies do estado 4. */
 		for(int i = 0; i < 256; i++) {
 			this.tabelaTransicao[4][i] = 4;
 		}
 		this.tabelaTransicao[4][(int)'\n'] = 0;
 		
 		/* Transies do estado 5. */
 		
 		/* Transies do estado 6. */
 		for(int i = 0; i < 256; i++) {
 			this.tabelaTransicao[6][i] = 6;
 		}
 		
 		this.tabelaTransicao[6][(int)'"'] = 7;
 		
 		/* Transies do estado 7. */
 		
 	}
 	
 	/**
 	 * Carrega a tabela de classes dos estados finais do autmato.
 	 */
 	private void carregaTabelaClasses() {
 		this.tabelaClasses = new int[this.estados];
 		for(int i = 0; i < this.tabelaClasses.length; i++) {
 			this.tabelaClasses[i] = Token.CLASSE_TOKEN_INVALIDO;
 		}
 		
 		/* Registra os estados finais. */
 		this.tabelaClasses[1] = Token.CLASSE_IDENTIFICADOR;
 		this.tabelaClasses[2] = Token.CLASSE_NUMERO_INTEIRO;
 		this.tabelaClasses[3] = Token.CLASSE_CARACTER_ESPECIAL;
 		this.tabelaClasses[7] = Token.CLASSE_STRING;
 	}
 	
 	/**
 	 * Recebe uma entrada e executa uma transio no autmato.
 	 * 
 	 * @param entrada cdigo ASCII decimal do caracter de entrada.
 	 * @return a classe do token reconhecido at o momento.
 	 */
 	public int transicao(int entrada) {
 		int proximoEstado; 
 		int saida;
 		
 		/* Verifica se a entrada  um caracter ASCII vlido. */
 		if(entrada < 0){
 			proximoEstado = ESTADO_INVALIDO;
 		} else {
 			// Efetua uma transio determinstica.
 			proximoEstado = this.tabelaTransicao[this.estadoAtual][entrada];
 		}
 		
 		/* Decide qual a classe de token correspondente  situao atual do autmato. */
 		if(proximoEstado == ESTADO_INVALIDO) {
 			// Terminou o reconhecimento do token, pois no achou transio.
 			saida = this.tabelaClasses[this.estadoAtual];
 			
 			// Faz transio com cadeia vazia para o estado inicial.
 			this.estadoAtual = ESTADO_INICIAL;
 		} else {
 			// Ainda no chegou ao final do reconhecimento.
 			saida = Token.CLASSE_TOKEN_NAO_FINALIZADO;
 			this.estadoAtual = proximoEstado;
 		}
 		
 		/* Se foi para um estado de comentrio, sinaliza isso. */
 		if(this.estadoAtual == ESTADO_COMENTARIO) {
 			this.estadoComentario = true;
 		} else {
 			this.estadoComentario = false;
 		}
 		
 		return saida;
 	}
 	
 	/**
 	 * Verfica qual a classe dos tokens que so reconhecidos pelo atual estado do autmato
 	 * @param estado
 	 * @return a classe do token. Se o estado no for final, retorna -1.
 	 */
 	public int classeEstadoAtual() {
 		return this.tabelaClasses[this.estadoAtual];
 	}
 	
 	/**
 	 * Verifica se o transdutor est no estado de comentrio.
 	 * 
 	 * @return <code>true</code>, caso o transdutor esteja em uma estado de comentrio. <code>false</code>, caso contrrio.
 	 */
 	public boolean estaNoEstadoComentario() {
 		return this.estadoComentario;
 	}
 	
 }
