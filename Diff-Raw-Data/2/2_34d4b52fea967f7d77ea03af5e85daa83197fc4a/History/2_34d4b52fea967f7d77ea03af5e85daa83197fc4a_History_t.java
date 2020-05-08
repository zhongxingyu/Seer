package messaging;
 import java.util.List;
 public class History {
 	private List<HistEl> lista;
 	
 	public HistEl getPreviousElement(int stepsBack) {
 		return lista.get(lista.size()-(stepsBack+1));
 	}
 	public HistEl pop() {
 		return lista.remove(lista.size()-1);
 	}
 	public History push(HistEl n_HistEl)
 	{
 		lista.add(n_HistEl);
 		return this;
 	}
 
 	public boolean contains(HistEl el) {
 		for(HistEl elem: lista) {
 			if(el.getClause() != null)
 				if(el.getClause() != elem.getClause()) {
 					continue;
 				}
 			if(el.getLiteral() != null)
 				if(el.getLiteral() != elem.getLiteral()) {
 					continue;
 				}
 			if(el.getClause() != null)
 				if(el.getAgentId() != elem.getAgentId()) {
 					continue;
 				}
 			return true;
 		}
 		return false;
 	}
 }
