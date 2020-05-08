 package br.com.teste.apresentacao.managedbean;
 
 import java.util.List;
 
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 
 import br.com.teste.negocio.dominio.pessoa.Pessoa;
 import br.com.teste.negocio.servico.pessoa.PessoaService;
 
 /**
  * No JSF, o Bean faz o papel de Controller (do modelo MVC), ou seja: ele recebe
  * as aes realizadas pelo JSP e despacha as aes para servios respectivos.
  * Num modelo simples, cada entidade (no caso, Pessoa), possui uma classe DAO
  * (de acesso a dados), uma classe de servio (de regras de negocio, e que se
  * comunica com o DAO) e um Bean.  um modelo que se aplica a boa parte das
  * data-centric application simples. No caso, existe um PessoaBean, que bate num
  * PessoaService, que bate num PessoaDAO, que conversa com o banco e retorna
  * para o PessoaService, que retorna para o PessoaBean, que exibe o status na
  * tela.
  * 
  * Esta  a base, mas existe forma muito mais elegante do que a que estamos
  * usando aqui, nas trs camadas. Com o tempo a gente vai trocando uma coisa por
  * outra, colocando frameworks, para tornar todo esse processo ainda mais
  * simples. Tem framework pra melhorar a interface (com ajax implicito e muito
  * facil de usar), para melhorar a integrao entre as camadas, a navegao
  * entre telas, e at vai dar para tirar o DAO, se a gente quiser.
  * 
  * @author daniel
  * 
  */
 
 public class PessoaBean {
 
 	private List<Pessoa> pessoas;
 	private DataModel<Pessoa> pessoasModel = new ListDataModel<Pessoa>();
 	private PessoaService pessoaService;
 
 	// para edio
 	private Pessoa pessoaSelecionada;
 
 	public PessoaBean() {
 		pessoaService = new PessoaService();
 	}
 
 	/**
 	 * Obtem a lista de pessoas do sistema, num formato de datamodel (para que
 	 * possa iteragir com o modo de seleo do datatable. No futuro, quando
 	 * colocarmos outros frameworks, no vai ser preciso utilizar este datamodel
 	 * (vai ser possivel utilizar o objeto diretamente no datatable)
 	 * 
 	 * @return Lista de pessoas do sistema
 	 */
 	public DataModel<Pessoa> getPessoas() {
 		if (pessoas == null) {
 			pessoas = pessoaService.obterPessoas();
 			pessoasModel.setWrappedData(pessoas);
 		}
 		return pessoasModel;
 	}
 
 	public Pessoa getPessoaSelecionada() {
 		return pessoaSelecionada;
 	}
 
 	public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
 		this.pessoaSelecionada = pessoaSelecionada;
 	}
 
 	public String selecionar() {
 		this.pessoaSelecionada = (Pessoa) pessoasModel.getRowData();
 		return "pessoaSelecionada";
 	}
 
 	public String gravar() {
 
 		pessoaService.gravar(pessoaSelecionada);
 
 		// retorna chave para a navegao
 		return "pessoaAtualizada";
 	}
 	
 	public String apagar() {
 
 		pessoaService.apagar(pessoaSelecionada);
 
 		// retorna chave para a navegao
 		return "pessoaApagada";
 	}
 	
 	public String atualizar() {
 
 		pessoaService.atualizar(pessoaSelecionada);
 
 		// retorna chave para a navegao
 		return "pessoaAtualizada";
 	}
 	
 	public String criar() {
		this.pessoaSelecionada = new Pessoa();
		
		// retorna chave para a navegao
		return "pessoaCriada";		
	}
 }
