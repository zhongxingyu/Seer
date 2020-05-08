 package exemplo.jsf.richfaces4.managedbean;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 
 import exemplo.jsf.richfaces4.modelo.Fornecedor;
 
public class FornecedorHandler implements Serializable {

	private static final long serialVersionUID = 2660872390886543397L;
 
 	private Fornecedor fornecedor = new Fornecedor();
 
 	private List<Fornecedor> fornecedores = new ArrayList<Fornecedor>();
 	
 	private long count = 0;
 	
 	public Fornecedor getFornecedor() {
 		return fornecedor;
 	}
 
 	public void setFornecedor(Fornecedor fornecedor) {
 		this.fornecedor = fornecedor;
 	}
 
 	public List<Fornecedor> getFornecedores() {
 		return fornecedores;
 	}
 
 	public void setFornecedores(List<Fornecedor> fornecedores) {
 		this.fornecedores = fornecedores;
 	}
 
 	public long getCount() {
 		return count;
 	}
 
 	public void setCount(long count) {
 		this.count = count;
 	}
 	
 	public void salva() {
 		System.out.println("Adicionando: " + fornecedor.getNome());
 		if (fornecedor.getId() == null) {
 			this.fornecedor.setId(++count);
 			this.fornecedores.add(fornecedor);
 		}
 		this.fornecedor = new Fornecedor();
 	}
 	
 	public String escolheFornecedor() {
 		System.out.println("Escolhendo fornecedor para visualizacao");
 		FacesContext fc = FacesContext.getCurrentInstance();
 		Fornecedor f = (Fornecedor) fc.getExternalContext().getRequestMap().get("f");
 		Long id = f.getId();
 		for (Fornecedor fo : this.fornecedores) {
 			if (fo.getId().equals(id)) {
 				System.out.println("Achei " + fo);
 				this.fornecedor = fo;
 			}
 		}
 		return null;
 	}
 	
 	public String removeFornecedor() {
 		System.out.println("Escolhendo fornecedor para remover");
 		FacesContext fc = FacesContext.getCurrentInstance();
 		Fornecedor f = (Fornecedor) fc.getExternalContext().getRequestMap().get("f");
 		this.fornecedores.remove(f);
 		return null;
 	}
 }
