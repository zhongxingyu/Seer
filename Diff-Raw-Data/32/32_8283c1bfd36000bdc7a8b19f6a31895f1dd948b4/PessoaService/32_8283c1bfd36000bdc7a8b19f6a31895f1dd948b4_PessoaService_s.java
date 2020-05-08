 package br.com.teste.negocio.servico.pessoa;
 
 import java.util.List;
 
 import br.com.teste.negocio.dominio.pessoa.Pessoa;
 import br.com.teste.persistencia.pessoa.PessoaDAO;
 
 /**
  * Em java,  um pattern (padro) considerar que a camada de negcio (que tem as
  * regras de negcio) seja chamada de camada Service (de servio)
  */
 
 public class PessoaService {
 	
 	private PessoaDAO pessoaDAO;
 	
 	public PessoaService() {
 		 pessoaDAO = new PessoaDAO();
 	}
 	
 	/**
 	 * Obtem a lista de pessoas do sistema
 	 * 
 	 * @return Lista de pessoas do sistema
 	 */
 	public List<Pessoa> obterPessoas() {
 		return pessoaDAO.obterPessoas(); 
 	}
 	
 	public void gravar(Pessoa pessoa) {
 		pessoaDAO.gravar(pessoa);
 	}
 	
 	public void apagar(Pessoa pessoa) {
 		pessoaDAO.apagar(pessoa);
 	}
 	
 	public void atualizar(Pessoa pessoa) {
 		pessoaDAO.atualizar(pessoa);
 	}
	
	public void criar() {
		pessoaDAO.criar();
	}
 }
