 import java.util.Random;
 
 public class Principal {
 
 	public static void main(String[] args) {
 		compara(preencheMatriz());
 	}
 
 	public static int [][]preencheMatriz(){
 
 		int matriz[][] = new int [5][5];
 		int gerado = 0;
 		Random randomGenerator = new Random();
 
 		for (int i = 0; i< 5; i++){
 			for (int j = 0; j < 5; j++){
 				int flag = 0;
 				//------------------------------------------------------------------------------------------
 				if(j == 0){
 					gerado = randomGenerator.nextInt(16)+1;
 					while((flag == 0)){
 						if (verificadorDeDuplicados(gerado, matriz, j)){
 							matriz[i][j] = gerado;
 							flag = 1;
 						}else{
 							gerado = randomGenerator.nextInt(16)+1;
 							flag = 0;
 						}
 					}
 				}
 				//------------------------------------------------------------------------------------------
 				if(j == 1){
 					gerado = randomGenerator.nextInt(16)+17;
 					while((flag == 0)){
 						if (verificadorDeDuplicados(gerado, matriz, j)){
 							matriz[i][j] = gerado;
 							flag = 1;
 						}else{
 							gerado = randomGenerator.nextInt(16)+17;
 							flag = 0;
 						}
 					}
 				}
 				//------------------------------------------------------------------------------------------
 				if(j == 2){
 					gerado = randomGenerator.nextInt(16)+33;
 					while((flag == 0)){
 						if (verificadorDeDuplicados(gerado, matriz, j)){
 							matriz[i][j] = gerado;
 							flag = 1;
 						}else{
 							gerado = randomGenerator.nextInt(16)+33;
 							flag = 0;
 						}
 					}
 				}
 				//------------------------------------------------------------------------------------------
 				if(j == 3){
 					gerado = randomGenerator.nextInt(16)+48;
 					while((flag == 0)){
 						if (verificadorDeDuplicados(gerado, matriz, j)){
 							matriz[i][j] = gerado;
 							flag = 1;
 						}else{
 							gerado = randomGenerator.nextInt(16)+48;
 							flag = 0;
 						}
 					}
 				}
 				//------------------------------------------------------------------------------------------
 
 				if(j == 4){
 					gerado = randomGenerator.nextInt(16)+65;
 					while((flag == 0)){
 						if (verificadorDeDuplicados(gerado, matriz, j)){
 							matriz[i][j] = gerado;
 							flag = 1;
 						}else{
 							gerado = randomGenerator.nextInt(16)+65;
 							flag = 0;
 						}
 					}
 				}
 				//------------------------------------------------------------------------------------------
 			}
 		}
 		return matriz;
 	}
 
 	public static boolean verificadorDeDuplicados(int gerado,int matriz[][], int coluna){
 		for (int linha = 0; linha < 5; linha++){
 			if(gerado == matriz[linha][coluna]){
 				return false; //ja tem
 			}
 		}
 		return true;
 	}
 
 	public static int []jogo(){
 		int jogo[] = new int[81];
 		int gerado = 0;
 
 		Random randomGenerator = new Random();
 		gerado = randomGenerator.nextInt(80) +1;
 
 		for (int i = 1; i < 81; i++){
			gerado = randomGenerator.nextInt(80) +1;	
			if(verificadorDeDuplicadosJogo(jogo, gerado)){ //false se ja tem
				jogo[i] = gerado;
			}else{
				i--;
			}
 		}

 		//Imprime valor no vetor jogo
 		for (int j=1; j< 81; j++){
 			System.out.print(jogo[j] + " ");
 		}
 
 		System.out.println("Se o numero foi acertado, um 0 vai aparecer no tabuleiro");
 		System.out.println("O Jogador ganha quando o tabuleiro todo for preenchido com zeros");
 		System.out.println();
 
 		return jogo;
 	}

 	public static boolean verificadorDeDuplicadosJogo(int vetor[], int numero){
 		for (int linha = 0; linha < 80; linha++){
 			if(numero == vetor[linha]){
				return false; //ja tem
 			}
 		}
		return true;
 	}
 
 	public static int compara(int matriz[][]){
 
 		int jogo[] = new int[80];
 		jogo = jogo();
 		int acertos = 0;
 
 		for (int i=1; i <= 80; i++){ //vetor
 
 			for (int j = 0; j < 5; j++){
 				for(int g = 0; g < 5; g++){	
 					if(matriz[j][g] == jogo[i]){//
 						matriz[j][g] = 0; // 99 = dupe
 						acertos++;
 						mostraMatriz(matriz);
 						System.out.println();

 						if (acertos == 25){
 							System.out.println("Opa, Ganhou na ultima rodada, tabuleiro limpo:");
 							mostraMatriz(matriz);
 							return 0;
 						}

 					}
 				}
 			}
 		}
 		return 0;
 	}
 
 	public static void mostraMatriz(int matriz[][]){
 		for (int i=0; i<5; i++){
 			for (int j=0; j<5; j++){
 				System.out.print(matriz[i][j] + (" "));
 			}
 			System.out.println();
 		}
 	}
 }
