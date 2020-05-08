 /*Trabalho de POO simulador de estacionamento 
 *
 * Integrantes: 
 * Andre G. Peil, Daniel K. Retzlaff, Guilherme P. B. Cousin,  Murilo F. Schmalfuss
 *
 */
 
import java.util.Random
 
public class Simulador_Garagem{
 
 	public static ListaDeAcoes carregarListaDeSimulacao(){
 		ListaDeAcoes temp = new ListaDeAcoes();
 		return temp;
 	}
 
 	/**
 	* Método que recebe o objeto que deve ser manipulado segundo a sua
 	* 	especificação
 	* @param Acao acao
 	* @return Boolean {true - se executou com sucesso | false - se não conseguiu executar }
 	*/
 	public static boolean executarAcao(Evento acao){
 		acao.execucao();
 		return true;
 	}
 
 	/**
 	* Execução de todas as ações
 	* @param args the command line arguments
 	*/
 	public static void main(String[] args){
 		Random random = new Random();
 		Relogio relogio = Relogio.getRelogio();
 		Evento tempAcao;
 		ListaDeAcoes lista = carregarListaDeSimulacao();
 		
 		double duracao;
		
		duracao = random.nextDouble(30);		
		lista.setAcao(AlocaVaga(duracao));		
 		
 		if(lista == null){
 			System.out.printf("Não há ações para simular");
 			System.exit(0);
 		}
 
 		while(!lista.isEmpty()){
 			tempAcao = new Evento(lista.getAcao());
 			if(tempAcao != null){
				if(executarAcao(tempAcao())){
 					relogio.increaseTime(tempAcao.getDuracao());
 					lista.setAcao(tempAcao.gerarProximo());
 				}
 			}
 			else{
 				System.out.printf("Ação não executada");
 			}
 		}
		Estatisca.geraRelatorio();
 	}
}
