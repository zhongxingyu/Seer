 package acervo.producao;
 
 import java.util.ArrayList;
 import java.util.List;
 
public class Acervo<Tipo> implements TipoAcervo<Tipo> {
 
	private List<Tipo> acervo;
 
 	public Acervo() {
		acervo = new ArrayList<Tipo>();
 	}
 
 	public int tamanho() {
 		return acervo.size();
 	}
 
	public boolean adicionar(Tipo t) {
 		return acervo.add(t);
 	}
 
	public Tipo obter(int i) {
 		return acervo.get(i);
 	}
 
	public Tipo remover(int i) {
 		return acervo.remove(i);
 	}
 
	public List<Tipo> obterLista(){
 		return acervo;
 	}
 
 }
