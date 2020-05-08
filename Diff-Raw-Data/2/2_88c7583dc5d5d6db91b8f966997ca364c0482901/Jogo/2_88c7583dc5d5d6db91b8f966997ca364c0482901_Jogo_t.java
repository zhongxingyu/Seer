 package br.ufc.gc.github.jogo;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 
 public class Jogo {
 	
 	ArrayList<String> armas;
 	ArrayList<String> pessoas;
 	ArrayList<String> lugares;
 	int lugar, culpado, arma, lugarCrime;
 	
 	public void GerarJogo(){
 		culpado = (int) (Math.random()*5);
 		arma = (int) (Math.random()*5);
 		lugarCrime = (int) (Math.random()*8);
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Jogo jogo = new Jogo();
 		jogo.Verifica();
 	}
 	
 	public Jogo () {
 		
 		this.armas = new ArrayList<String>();
 		armas.add("cano");
 		armas.add("faca");
 		armas.add("corda");
 		armas.add("castical");
 		armas.add("pistola");
 		armas.add("bazuca");
 		this.pessoas = new ArrayList<String>();
 		this.pessoas.add("Capitao Mostarda");
 		this.pessoas.add("Senhor Marinho");
 		this.pessoas.add("Dona Branca");
 		this.pessoas.add("Senhorita Rosa");
 		this.pessoas.add("Dona Violeta");
 		this.pessoas.add("Professor Black");
 		this.lugares = new ArrayList<String>();
 		this.lugares.add("Sala de Jogos");
 		this.lugares.add("Sala de Estar");
 		this.lugares.add("Sala de Jantar");
 		this.lugares.add("Biblioteca");
 		this.lugares.add("Escritorio");
 		this.lugares.add("Hall");
 		this.lugares.add("Cozinha");
 		this.lugares.add("Salao de Festas");
 		this.lugares.add("Sala de Musica");	
 		this.lugar = 6;
 		GerarJogo();
 	}
 	public int InformaArma(){
 		int aux = -1;
 		System.out.println("Jogador escolha a arma.");
 		menuJogo(armas);
 		try {  
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
 
 			// Le entao a palagra SAIR nao seja digitada  
 			String linha = "";  
 
 			linha = reader.readLine();  
 			aux	= Integer.parseInt(linha);
 				
 			
 		}catch (IOException e) {  
 			System.out.println("Erro: "+ e);  
 		}  
 	return aux;
 	}
 	public void menuJogo(ArrayList<String>Menu){
 		
 		for(int i = 0; i<Menu.size(); i++){
 		System.out.println("Opcao "+(i+1)+" - " +Menu.get(i));
 		}
 		
 	}
 	
 	public int InformaPessoa(){
 		int aux = -1;
 		System.out.println("Jogador escolha o pessoa.");
 		menuJogo(pessoas);
 		try {  
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
 
 			// Le entao a palagra SAIR nao seja digitada  
 			String linha = "";  
 
 			linha = reader.readLine();  
 			aux	= Integer.parseInt(linha);
 				
 			
 		}catch (IOException e) {  
 			System.out.println("Erro: "+ e);  
 		}  
 	return aux;
 	}
 	
 	public int InformaLugar(){
 		int aux = -1;
 		System.out.println("Jogador escolha o lugar.");
 		menuJogo(lugares);
 		try {  
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
 
 			// Le entao a palagra SAIR nao seja digitada  
 			String linha = "";  
 
 			linha = reader.readLine();  
 			aux	= Integer.parseInt(linha);
 				
 			
 		}catch (IOException e) {  
 			System.out.println("Erro: "+ e);  
 		}  
 	return aux;
 	}
 	
 	public void Verifica(){
 		
 		int opcao=0;
 		System.out.println("Deseja dar um palpite ou mudar de local? \n Opcao 1 - Dar Palpite \n Opcao 2 - Mudar de Sala\n");
 		try {  
 			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  
 
 			// Le entao a palagra SAIR nao seja digitada  
 			String linha = "";  
 
 			linha = reader.readLine();  
 			opcao	= Integer.parseInt(linha);
 
 
 		}catch (IOException e) {  
 			System.out.println("Erro: "+ e);  
 		}  
 
 		if(opcao == 1){
 			int arma = InformaArma();
 			while( arma > 6 || arma < 0){ 
 				System.out.println("Opcao invalida.Tente novamente");
 				arma = InformaArma();
 
 			}
 			int pessoa = InformaPessoa(); 
			while( pessoa > 6 || pessoa <= 0){ 
 				System.out.println("Opcao invalida.Tente novamente");
 				pessoa = InformaPessoa();
 
 			}
 			System.out.println("seu palpite: lugar: " + (this.lugar) + " pessoa " + (pessoa) + " arma " + (arma));
 			System.out.println("a resposta era: lugar: " + this.lugarCrime + " pessoa " + this.culpado + " arma " + this.arma);
 			if(lugar == this.lugarCrime){
 				if(arma == this.arma){
 					if(pessoa == this.culpado){
 						System.out.println("Voce acertou");
 					}else System.out.println("Pessoa Errada");
 				}
 				else System.out.println("\n Arma Errada");
 			}
 			else System.out.println("\n Lugar Errado");
 		}
 
 		else{
 			this.lugar = InformaLugar();
 			Verifica();
 		}
 	}
 	
 }
