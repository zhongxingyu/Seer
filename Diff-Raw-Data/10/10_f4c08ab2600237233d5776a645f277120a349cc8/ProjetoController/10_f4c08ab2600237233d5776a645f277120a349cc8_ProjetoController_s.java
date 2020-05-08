 package br.com.athat.web.movimentacao.projeto;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.event.ActionEvent;
 
 import org.primefaces.context.RequestContext;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import br.com.athat.core.entity.movimentacao.projeto.Levantamento;
 import br.com.athat.core.entity.movimentacao.projeto.Projeto;
 import br.com.athat.core.entity.pessoa.cliente.Cliente;
 import br.com.athat.core.manager.movimentacao.projeto.LevantamentoManager;
 import br.com.athat.core.manager.movimentacao.projeto.ProjetoManager;
 import br.com.athat.core.vo.projeto.ProjetoVO;
 import br.com.athat.web.utils.AbstractController;
 
 public class ProjetoController extends AbstractController {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Projeto projeto;
 	private ProjetoVO projetoVO;
 	private List<Projeto> projetos;
 	private List<Levantamento> levantamentos;
 	
 	@Autowired
 	private ProjetoManager projetoManager;
 	
 	@Autowired
 	private LevantamentoManager levantamentoManager;
 	
 	public ProjetoController() {
 		init();
 	}
 	
 	public String salvar() {
 		try {
 			projetoManager.salvar(projeto);
 			getMessageCadastroSucesso();
 			init();
 		} catch(Exception e) {
 			getMessageInstabilidade();
 			e.printStackTrace();
 		}
 		return "/pages/projeto/cadastroProjeto?faces-redirect=true";
 	}
 	
 	public void buscar() {
 		projetos = projetoManager.buscar(projetoVO);
 	}
 	
 	public String limpar() {
 		init();
 		return "/pages/projeto/cadastroProjeto?faces-redirect=true";
 	}
 	
 	public void validaCliente(ActionEvent event) {
         RequestContext context = RequestContext.getCurrentInstance();
         Cliente cliente = (Cliente) event.getComponent().getAttributes().get("pessoa");
     	if (cliente == null) {
     		context.addCallbackParam("confirmar", false);
     		setMessage("Cliente não selecionado.");
     	} else {
     		context.addCallbackParam("confirmar", true);                
     		projeto.setCliente(cliente);
     	}
     }
 	
 	public void carregarOrcamentos(Projeto projeto) {
 		this.projeto = projeto;
 		levantamentos = levantamentoManager.buscarOrcApresentacaoProjeto(projeto.getId());
     }
 	
 	private void init() {
 		projeto = new Projeto();
 		projetoVO = new ProjetoVO();
 		projetos = new ArrayList<Projeto>();
 		levantamentos = new ArrayList<Levantamento>();
 	}
 	
 	public Projeto getProjeto() {
 		return projeto;
 	}
 	
 	public void setProjeto(Projeto projeto) {
 		this.projeto = projeto;
 	}
 
 	public List<Levantamento> getLevantamentos() {
 		return levantamentos;
 	}
 
 	public void setLevantamentos(List<Levantamento> levantamentos) {
 		this.levantamentos = levantamentos;
 	}
 
 	public List<Projeto> getProjetos() {
 		return projetos;
 	}
 
 	public void setProjetos(List<Projeto> projetos) {
 		this.projetos = projetos;
 	}
 
 	public ProjetoVO getProjetoVO() {
 		return projetoVO;
 	}
 
 	public void setProjetoVO(ProjetoVO projetoVO) {
 		this.projetoVO = projetoVO;
 	}
 }
