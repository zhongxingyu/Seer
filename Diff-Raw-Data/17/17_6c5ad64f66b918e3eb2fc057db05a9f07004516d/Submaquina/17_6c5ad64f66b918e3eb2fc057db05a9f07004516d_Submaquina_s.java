 package compilador.analisador.sintatico;
 
 import compilador.analisador.lexico.Token;
 import compilador.estruturas.ListaLigada;
 import compilador.estruturas.String;
 
 public class Submaquina {
 	
 	/**
 	 * Indica um estado invlido.
 	 */
 	public static final int ESTADO_INVALIDO = -1;
 	
 	/**
 	 * Indica que a transio ocorreu normalmente.
 	 */
 	public static final int TRANSICAO_OK = 10;
 	
 	/**
 	 * Indica que a transio normal falhou. Deve ocorrer ento um retorno ou chamada de submquina.
 	 */
 	public static final int TRANSICAO_FALHOU = 11;
 	
 	/**
 	 * O nome da submquina.
 	 */
 	private String nome;
 	
 	/**
 	 * O estado inicial da submquina.
 	 */
 	private int estadoInicial;
 	
 	/**
 	 * Array com os estados iniciais da submquina.
 	 */
 	private int[] estadosFinais;
 	
 	/**
 	 * Estado atual da submquina.
 	 */
 	private int estadoAtual;
 	
 	/**
 	 * Tabela de transio de estados da submquina.
 	 */
 	private int[][][] tabelaTransicao;
 	
 	/**
 	 * Tabela com a relao de quais submquinas podem ser chamadas em cada estado.
 	 */
 	private int[][] tabelaChamadaSubmaquinas;
 	
 	public Submaquina(String nome, int estadoInicial, int[] estadosFinais, int[][][] tabelaTransicao, int[][] tabelaChamadaSubmaquinas) {
 		this.nome = nome;
 		this.estadoInicial = this.estadoAtual = estadoInicial;
 		this.estadosFinais = estadosFinais;
 		this.tabelaTransicao = tabelaTransicao;
 		this.tabelaChamadaSubmaquinas = tabelaChamadaSubmaquinas;
 	}
 	
 	/**
 	 * @return o nome da submquina.
 	 */
 	public String getNome() {
 		return this.nome;
 	}
 	
 	/**
 	 * @return o estado inicial da submquina.
 	 */
 	public int getEstadoInicial() {
 		return this.estadoInicial;
 	}
 	
 	/**
 	 * @return o array de estados finais.
 	 */
 	public int[] getEstadosFinais() {
 		return this.estadosFinais;
 	}
 	
 	/**
 	 * @return o estado atual da submquina.
 	 */
 	public int getEstadoAtual() {
 		return this.estadoAtual;
 	}
 	
 	/**
 	 * Seta o estado atual da sumquina.
 	 * 
 	 * @param estadoAtual
 	 */
 	public void setEstadoAtual(int estadoAtual) {
 		this.estadoAtual = estadoAtual;
 	}
 	
 	/**
 	 * Executa uma transio na submquina.
 	 * 
 	 * @param token
 	 * @return
 	 */
 	public int transicao(Token token) {
 		int proximoEstado = this.tabelaTransicao[this.estadoAtual][token.getClasse()][token.getID()];
 		
//		System.out.println("Prox estado : " + proximoEstado);
//		System.out.println();
		
 		if(proximoEstado == ESTADO_INVALIDO) {
 			// Deve tentar chamar uma submquina.
 			return TRANSICAO_FALHOU;
 		} else {
 			// H transio disponvel.
 			this.estadoAtual = proximoEstado;
 			return TRANSICAO_OK;
 		}
 	}
 	
 	/**
 	 * Identifica qual submquina deve ser chamada.
 	 * 
 	 * @param token
 	 * @return o identificador da submquina a ser chamada.
 	 */
 	public ListaLigada<Integer> possiveisSubmaquinas(Token token) {
 		ListaLigada<Integer> possiveisSubmaquinas = new ListaLigada<Integer>();
 		
 		// Identificar as submquinas que podem ser chamadas neste estado.
 		for(int i = 0; i < this.tabelaChamadaSubmaquinas[this.estadoAtual].length; i++)
 			if(this.tabelaChamadaSubmaquinas[this.estadoAtual][i] != -1)
 				possiveisSubmaquinas.put(i);
 		
 		return possiveisSubmaquinas;
 	}
 	
 	/**
 	 * Verifica se a submquina possui transio no estado atual para o token dado.
 	 * 
 	 * @param token 
 	 * @return <code>true</code>, caso exista a transio. <code>false</code>, caso no exista.
 	 */
 	public boolean haTransicao(Token token) {
 		int proximoEstado = this.tabelaTransicao[this.estadoAtual][token.getClasse()][token.getID()];
 		
 		if(proximoEstado == ESTADO_INVALIDO)
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Verifica se a submquina possui, no estado atual, chamada de outra submquina.
 	 * 
 	 * @return <code>true</code>, caso exista a chamada. <code>false</code>, caso no exista.
 	 */
 	public boolean haChamadaSubmaquina() {
 		for(int i = 0; i < this.tabelaChamadaSubmaquinas[this.estadoAtual].length; i++)
 			if(this.tabelaChamadaSubmaquinas[this.estadoAtual][i] != ESTADO_INVALIDO)
 				return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Executa uma transio de estado baseada em chamada de submquina.
 	 * No chama efetivamente outra submquina, apenas verifica a possibilidade e muda para o estado de retorno.
 	 * 
 	 * @param idSubmaquina
 	 * @return <code>true</code>, caso seja possivel chamar a submquina. <code>false</code>, caso no exista.
 	 */
 	public boolean chamarSubmaquina(int idSubmaquina) {
 		int proximoEstado = this.tabelaChamadaSubmaquinas[this.estadoAtual][idSubmaquina];
 		
 		if(proximoEstado == ESTADO_INVALIDO)
 			return false;
 		
 		this.estadoAtual = proximoEstado;
 		return true;
 	}
 }
