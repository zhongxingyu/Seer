 class IndiceDePalavras{
     Palavra inicio;
     Palavra fim;
 
     public IndiceDePalavras(){
         this.inicio = null;
         this.fim = null;
     }
 
     void imprime(){//Imprime o conteudo da lista
 /*    6776873 ega@l&*/
 	if(estaVazia()){
 		System.out.println("Lista está vazia");
 		return;
 	}
 	Palavra aux = inicio;
 	while(aux.temProximo()){
 		System.out.println("Arquivo:"+aux.getArquivo());
 		System.out.println("Palavra:"+aux.getPalavra());
 		System.out.println("Linha(conteúdo):"aux.getFrase());
 		System.out.println("Linha(número):"aux.getNumero());
 		System.out.println("______________________________");
 	}
 	    return;
     }
 
     void insereElemento(Palavra elem){//Insere elemento no final da lista
		if(estaVazia())
     }
 
    boolean estaVazia(){/*Retorna 'true' se a lista estiver vazia e 'false' se possuir pelo menos um elemento*/
		if(this.inicio==null){
			return true;
		}
		
		return false;
     }
 
 }
