 package compilador.analisador.sintatico;
 
 import java.io.IOException;
 
 import compilador.analisador.lexico.AnalisadorLexico;
 import compilador.analisador.lexico.Token;
 import compilador.estruturas.ListaLigada;
 
 public class AnalisadorSintatico {
 	
 	/**
 	 * O autmato de pilha estruturado do analisador sinttico.
 	 */
 	private AutomatoPilhaEstruturado ape;
 	
 	/**
 	 * O analisador lxico do compilador.
 	 */
 	private AnalisadorLexico lexico;
 	
 	/**
 	 * Lista que armazena os erros sintticos do cdigo-fonte.
 	 */
 	private ListaLigada<ErroSintatico> erros;
 	
 	public AnalisadorSintatico() {
 		// Inicializa o analisador lxico.
 		this.lexico = new AnalisadorLexico();
 		this.lexico.carregarCodigoFonte("src/compilador/testes/source.ling");
 		
 		// Inicializa o APE.
 		this.ape = new AutomatoPilhaEstruturado();
 		
 		// Inicializa a lista de erros sintticos.
 		this.erros = new ListaLigada<ErroSintatico>();
 	}
 	
 	/**
 	 * Processa o cdigo fonte.
 	 * 
 	 * @return <code>true</code> caso o cdigo-fonte esteja sintaticamente correto. <code>false</code> caso contrrio.
 	 */
 	public boolean processarCodigoFonte() { 
 		try{
 			Token token;
 			
 			while(lexico.haMaisTokens()) {
 				token = lexico.proximoToken();
 				this.ape.consumirToken(token);
 				
 				// Verifica se houve erro sinttico no autmato de pilha.
 				ErroSintatico erro = this.ape.getUltimoErroSintatico(); 
 				if(erro != null)
 					this.erros.put(erro);
 			}
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		
 		// Condio para que o cdigo esteja sintaticamente correto.
 		if(this.ape.estaNoEstadoAceitacao() && this.ape.pilhaVazia() && this.erros.vazia())
 			return true;
 		
 		// Imprime os erros encontrados.
		for(int i = 0; i < this.erros.tamanho(); i++) {
 			System.out.print("Token ");
 			Token.getClasseTokenString(this.erros.get(i).getClasseToken()).imprimir();
 			System.out.println(" inesperado. Linha " + this.erros.get(i).getLinha() + " - Coluna: " + this.erros.get(i).getColuna());
 		}
 		
 		return false;
 	}
 	
 	public static void main(String[] args) {
 		AnalisadorSintatico sintatico = new AnalisadorSintatico();
 		boolean resultado = sintatico.processarCodigoFonte();
 		
 		if(resultado)
 			System.out.println("\nRESULTADO: O programa esta sintaticamente correto.");
 		else
 			System.out.println("\nRESULTADO: O programa nao esta sintaticamente correto.");
 	}
 }
