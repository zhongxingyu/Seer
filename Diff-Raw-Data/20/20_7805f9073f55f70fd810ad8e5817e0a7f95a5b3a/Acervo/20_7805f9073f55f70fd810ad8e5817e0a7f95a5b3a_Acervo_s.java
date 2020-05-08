 package acervo.producao;
 
 import java.util.ArrayList;
 import java.util.List;
 
public class Acervo<Arquivavel> implements TipoAcervo<Arquivavel> {
 
	private List<Arquivavel> acervo;
 
 	public Acervo() {
		acervo = new ArrayList<Arquivavel>();
 	}
 
 	public int tamanho() {
 		return acervo.size();
 	}
 
	public boolean adicionar(Arquivavel t) {
 		return acervo.add(t);
 	}
 
	public Arquivavel obter(int i) {
 		return acervo.get(i);
 	}
 
	public Arquivavel remover(int i) {
 		return acervo.remove(i);
 	}
 
	public List<Arquivavel> obterLista(){
 		return acervo;
 	}
 
 }
