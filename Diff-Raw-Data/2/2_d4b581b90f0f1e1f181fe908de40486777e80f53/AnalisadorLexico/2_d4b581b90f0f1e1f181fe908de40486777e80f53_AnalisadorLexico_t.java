 package compilador.analisador.lexico;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import compilador.analisador.semantico.TabelaPalavrasReservadas;
 import compilador.analisador.semantico.TabelaSimbolos;
 import compilador.helper.ArrayHelper;
 
 public class AnalisadorLexico {
 	
 	/**
 	 * Autmato que reconhece os tokens da linguagem.
 	 */
 	private Transdutor transdutor;
 	
 	/**
 	 * Aponta para o caracter atualmente a ser processo.
 	 */
 	private int ch;
 	
 	/**
 	 * Aponta para o caracter seguinte ao atualmente em processamento.
 	 */
 	private int lookahead;
 	
 	/**
 	 * Buffer para armazenar a representao do token.
 	 */
 	private int[] buffer;
 	
 	/**
 	 * Indexa o buffer.
 	 */
 	private int indice;
 	
 	/**
 	 * Indica se o armazenamento de caracteres no buffer est ativado.
 	 */
 	private boolean bufferAtivo;
 	
 	/**
 	 * Indica se os Tokens do arquivo j se esgotaram.
 	 */
 	private boolean haMaisTokens; 
 	
 	/**
 	 * FileInputStream que aponta para o arquivo do cdigo-fonte.
 	 */
 	private FileInputStream arquivoCondigoFonte;
 	
 	/**
 	 * A linha atualmente em processamento no arquivo do cdigo-fonte.
 	 */
 	private int linha;
 	
 	/**
 	 * A coluna atualmente em processamento no arquivo do cdigo-fonte
 	 */
 	private int coluna;
 	
 	/**
 	 * Indica se o final do arquivo foi atingido.
 	 */
 	private boolean finalDoArquivo;
 	
 	public AnalisadorLexico() {
 		this.buffer = new int[100];
 		this.transdutor = new Transdutor();
 		this.haMaisTokens = true;
 		this.finalDoArquivo = false;
 		this.bufferAtivo = true;
 		this.indice = 0;
 		this.ch = -1;
 		this.lookahead = -1;
 		this.linha = 1;
 		this.coluna = 1;
 	}
 	
 	/**
 	 * Ativa ou desativa o armazenamento de caracteres em buffer, dependendo da situao do compilador.
 	 */
 	private void gerenciarAtividadeBuffer() {
 		if(this.transdutor.estaNoEstadoComentario()) {
 			this.bufferAtivo = false;
 			
 			/* Limpa o buffer se eventualmente estiver com lixo. */
 			for(int i = 0; i < 100; i++)
 				this.buffer[i] = -1;
 			
 			this.indice = 0;
 		} else if(this.ch == (int)' ' || this.ch == (int)'\n' || this.ch == (int)'\t') {
 			this.bufferAtivo = false;
 		} else {
 			this.bufferAtivo = true;
 		}
 	}
 	
 	/**
 	 * Abre o arquivo que contm o cdigo-fonte.
 	 */
 	public void carregarCodigoFonte(String arquivo) {
 		try {
 			this.arquivoCondigoFonte = new FileInputStream(new File(arquivo));
 		} catch(FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * L o cdigo-fonte buscando o prximo Token.
 	 * 
 	 * @return o prximo Token encontrado.
 	 */
 	public Token proximoToken() throws IOException {
 		// inicializa o buffer.
 		for(int i = 0; i < 100; i++)
 			this.buffer[i] = -1;
 		this.indice = 0;
 		
 		int classe; // Armazenar a sada do autmato para uma dada transio.
 		int chave; // Chave do Token na tabela correspondente  sua classe.
 		Token token; // Armazenar o token reconhecido.
 		int[] bufferMinimo;
 		
 		// Coloca o primeiro carater no lookahead, pois ainda no iniciou o processamento.
 		// Executa somente na primeira vez.
 		if(this.lookahead == -1) {
 			this.lookahead = this.lerProximoCaracter();
 			
 			if(this.lookahead == -1) {
 				// Atingiu o final do arquivo.
 				this.finalDoArquivo = true;
 			}
 		}
 		
 		int linhaToken = this.linha;
 		int colunaToken = this.coluna - 1;
 		
 		while(true) {
 			if(this.finalDoArquivo && this.ch == -1) {
 				// Atingiu o final do arquivo e terminou de processar os caracteres.
 				this.haMaisTokens = false;
 				//break;
 			}
 			
 			if(this.ch == -1) {
 				this.ch = this.lookahead;
 				this.lookahead = this.lerProximoCaracter();
 				
 				if(this.lookahead == -1)
 					this.finalDoArquivo = true;
 			}
 			
 			// Executa uma transio no autmato.
 			classe = transdutor.transicao(this.ch);
 			
 			// Verifica se  necessrio ativar ou desativar o armazenamento em buffer.
 			this.gerenciarAtividadeBuffer();
 			
 			switch(classe) {
 				case Token.CLASSE_TOKEN_NAO_FINALIZADO:
 					// L mais caracteres do cdigo-fonte.
 					if(this.bufferAtivo) {
 						// Se o armazenamento estiver ativo, guarda o caracter no buffer.
 						this.buffer[indice] = this.ch;
 						indice++;
 					}
 					this.ch = -1; // Sinaliza que j processou o caracter.
 					break;
 				case Token.CLASSE_PALAVRA_RESERVADA:
 					bufferMinimo = ArrayHelper.alocarVetor(this.buffer);
 					
 					chave = TabelaPalavrasReservadas.getInstance().recuperarChave(bufferMinimo);
 					token = new Token(Token.CLASSE_PALAVRA_RESERVADA, chave);
 					token.setLinha(linhaToken);
 					token.setColuna(colunaToken);
 					return token;
 				case Token.CLASSE_IDENTIFICADOR:
 					bufferMinimo = ArrayHelper.alocarVetor(this.buffer);
 					
 					chave = TabelaPalavrasReservadas.getInstance().recuperarChave(bufferMinimo);
 					
 					if(chave == -1) {
 						// No est na tabela de palavras reservadas.
 						
 						chave = TabelaSimbolos.recuperarChave(bufferMinimo);
 						if(chave == -1) {
 							// No est na tabela de smbolos.
 							chave = TabelaSimbolos.inserirSimbolo(bufferMinimo);
 						}
 						
 						token = new Token(Token.CLASSE_IDENTIFICADOR, chave);
 					} else {
 						//  uma palavra reservada.
 						token = new Token(Token.CLASSE_PALAVRA_RESERVADA, chave);
 					}
 					
 					token.setLinha(linhaToken);
 					token.setColuna(colunaToken);
 					return token;
 				case Token.CLASSE_CARACTER_ESPECIAL:
 					token = new Token(Token.CLASSE_CARACTER_ESPECIAL, this.buffer[0]);
 					token.setLinha(linhaToken);
 					token.setColuna(colunaToken);
 					return token;
 				case Token.CLASSE_NUMERO_INTEIRO:
 					int numero = 0;
 					
 					for(int i = 0; i < this.buffer.length && this.buffer[i] != -1; i++) {
						numero = 10*numero + Integer.valueOf(String.valueOf((char)this.buffer[i]));
 					}
 					
 					token = new Token(Token.CLASSE_NUMERO_INTEIRO, numero);
 					token.setLinha(linhaToken);
 					token.setColuna(colunaToken);
 					return token;
 				case Token.CLASSE_STRING:
 					bufferMinimo = ArrayHelper.alocarVetor(this.buffer);
 					
 					chave = TabelaSimbolos.recuperarChave(bufferMinimo);
 					
 					if(chave == -1) {
 						// No est na tabela de smbolos.
 						chave = TabelaSimbolos.inserirSimbolo(bufferMinimo);
 					}
 					
 					token = new Token(Token.CLASSE_STRING, chave);
 					token.setLinha(linhaToken);
 					token.setColuna(colunaToken);
 					return token;
 			}
 			
 			if(!this.haMaisTokens)
 				break;
 		}
 		
 		return new Token(Token.CLASSE_TOKEN_INVALIDO, -1);
 	}
 	
 	/**
 	 * Verifica se h mais tokens no cdigo-fonte.
 	 * @return <code>true</code>, caso haja mais Tokens. <code>false</code>, caso contrrio.
 	 */
 	public boolean haMaisTokens() {
 		return this.haMaisTokens;
 	}
 	
 	/**
 	 * L o prximo caracter do cdigo-fonte e conta o nmero de linhas e colunas processadas.
 	 * @return o prximo caracter do cdigo-fonte.
 	 */
 	private int lerProximoCaracter() throws IOException {
 		int caracter = arquivoCondigoFonte.read();
 		
 		if(caracter == (int)'\n') {
 			this.linha++;
 			this.coluna = 0;
 		} else if(caracter == (int)'\t') {
 			this.coluna += 4;
 		} else if(caracter >= 32) {
 			this.coluna++;
 		}
 		
 //		System.out.println((char)caracter + " - " + caracter + " - linha: " + this.linha + " - coluna: " + this.coluna);
 		
 		return caracter;
 	}
 	
 	public static void main(String[] args) {
 		AnalisadorLexico lexico = new AnalisadorLexico();
 		lexico.carregarCodigoFonte("src/compilador/testes/source.ling");
 		
 		try{
 			Token token;
 			
 			while(lexico.haMaisTokens()) {
 				token = lexico.proximoToken();
 				System.out.println("Token.classe: " + token.getClasse());
 				System.out.println("Token.representacao: " + token.getID() + "\n");
 			}
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
