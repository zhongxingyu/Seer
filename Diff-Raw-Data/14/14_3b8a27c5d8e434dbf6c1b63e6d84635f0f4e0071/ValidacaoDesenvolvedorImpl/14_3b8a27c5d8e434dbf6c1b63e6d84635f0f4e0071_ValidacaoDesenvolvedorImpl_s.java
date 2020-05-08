 package validacao.implementacao;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.swing.text.StyledEditorKit.ItalicAction;
 
 import beans.DadosAutenticacao;
 import beans.Solucao;
 import beans.TipoAtividade;
 import beans.Conhecimento;
 import beans.Desenvolvedor;
 import persistencia.implementacao.ServicoAtividadeImplDAO;
 import persistencia.implementacao.ServicoConhecimentoImplDAO;
 import persistencia.implementacao.ServicoDesenvolvedorImplDAO;
 import persistencia.implementacao.ServicoSolucaoImplDAO;
 import persistencia.interfaces.ServicoAtividade;
 import persistencia.interfaces.ServicoConhecimento;
 import persistencia.interfaces.ServicoDesenvolvedor;
 import persistencia.interfaces.ServicoSolucao;
 import excessao.ConhecimentoInexistenteException;
 import excessao.DescricaoInvalidaException;
 import excessao.DesenvolvedorExisteException;
 import excessao.DesenvolvedorInexistenteException;
 import excessao.EmailInvalidoException;
 import excessao.ErroDeAutenticacaoException;
 import excessao.SenhaInvalidaException;
 
 /**
  * 
  * @author RodrigoCMD
  * Essa classe contem metodos para a administracao e validao de desenvolvedores.
  * 
  * ltima modificacao: 09/09/2008 por RodrigoCMD
  */
 
 public class ValidacaoDesenvolvedorImpl {
 	
 	ServicoConhecimento servicoConhecimento;
 	ServicoAtividade servicoAtividade;
 	ServicoSolucao servicoSolucao;
 	ServicoDesenvolvedor servicoDesenvolvedor;
 	
 	public ValidacaoDesenvolvedorImpl() {
 		servicoAtividade = new ServicoAtividadeImplDAO();
 		servicoConhecimento = new ServicoConhecimentoImplDAO();
 		servicoDesenvolvedor = new ServicoDesenvolvedorImplDAO();
 		servicoSolucao = new ServicoSolucaoImplDAO();
 	}
 	
 	/**
 	 * Esse mtodo adiciona um conhecimento previamente cadastrado a um desenvolvedor.
 	 * @param emailDesenvolvedor Email do desenvolvedor.
 	 * @param nomeConhecimento Nome do conhecimento a ser adicionado.
 	 * @return true se o conhecimento foi adicionado ao desenvolvedor.
 	 * @throws DescricaoInvalidaException 
 	 * @throws ConhecimentoInexistenteException 
 	 */
 	public boolean adicionarConhecimentoAoDesenvolvedor(
 			String emailDesenvolvedor, String nomeConhecimento, int grau, int qntResposta) throws DescricaoInvalidaException, ConhecimentoInexistenteException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(emailDesenvolvedor)) throw new DescricaoInvalidaException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.adicionarConhecimentoAoDesenvolvedor(
 				emailDesenvolvedor, nomeConhecimento, grau, qntResposta);
 	}
 	
 	/**
 	 * Esse mtodo atualiza os dados de um desenvolvedor previamente cadastrado na
 	 * base de dados.
 	 * @param email Email do novo desenvolvedor.
 	 * @param novoEmail novo email do desenvolvedor.
 	 * @param nome Nome do novo desenvolvedor.
 	 * @param localidade Local onde o desenvolvedor reside.
 	 * @return true se a atualizacao foi concluida com sucesso.
 	 * @throws DesenvolvedorInexistenteException 
 	 * @throws SenhaInvalidaException 
 	 */
 	public boolean atualizarDesenvolvedor(String email, String novoEmail,
 			String nome, String localidade, String senha) throws DesenvolvedorInexistenteException, SenhaInvalidaException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		if (!ValidacaoUtil.validaSenha(senha)) throw new SenhaInvalidaException();
 		
 		return servicoDesenvolvedor.atualizarDesenvolvedor(email, novoEmail, nome, localidade, senha);
 	}
 	
 	/**
 	 * Esse mtodo verifica se existe relacao entre um conhecimento e um desenvolvedor/
 	 * @param emailDesenvolvedor Email do desenvolvedor que possui tal conhecimento.
 	 * @param nomeConhecimento Nome do conhecimento associado ao desenvolvedor. 
 	 * @return true se a associacao existe.
 	 * @throws DesenvolvedorInexistenteException 
 	 * @throws ConhecimentoInexistenteException 
 	 */
 	public boolean conhecimentoDoDesenvolvedorExiste(String emailDesenvolvedor,
 			String nomeConhecimento) throws DesenvolvedorInexistenteException, ConhecimentoInexistenteException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(emailDesenvolvedor)) throw new DesenvolvedorInexistenteException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.conhecimentoDoDesenvolvedorExiste(emailDesenvolvedor, nomeConhecimento);
 	}
 	
 	/**
 	 * Esse mtodo adiciona um novo desenvolvedor na base de dados.
 	 * @param email Email do novo desenvolvedor.
 	 * @param nome Nome do novo desenvolvedor.
 	 * @param localidade Local onde o desenvolvedor reside.
 	 * @return true se o desenvolvedor foi criado com sucesso.
 	 * @throws DesenvolvedorInexistenteException 
 	 * @throws SenhaInvalidaException 
 	 */
 	public boolean criarDesenvolvedor(String email, String nome,
			String localidade, String senha, HashMap<Conhecimento, Double> hashMap) throws DesenvolvedorExisteException, SenhaInvalidaException {
 		
 		if (servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorExisteException();
 		if (!ValidacaoUtil.validaSenha(senha)) throw new SenhaInvalidaException();
 		
 		boolean teste = servicoDesenvolvedor.criarDesenvolvedor(email, nome, localidade, senha);
 		
 		Set<Conhecimento> setConhecimento = null;
 		if(hashMap != null) {
 			
 			setConhecimento = hashMap.keySet();
 			
 			for(Conhecimento conhecimento: setConhecimento) {
 				Double grau = hashMap.get(conhecimento);
 				servicoDesenvolvedor.adicionarConhecimentoAoDesenvolvedor(email, conhecimento.getNome(), grau, 0);
 			}
 			
 			return teste;
		} 
		
		return false;
		
 		
 	}
 	
 	/**
 	 * Esse mtodo verifica se um dado desenvolvedor est cadastrado no sistema.
 	 * @param email Email do desenvolvedor
 	 * @return true se o desenvolvedro estiver cadastrado no sistema.
 	 */
 	public boolean desenvolvedorExiste(String email) {
 		
 		return servicoDesenvolvedor.desenvolvedorExiste(email);
 	}
 	
 	/**
 	 * Esse mtodo retorna uma lista de atividades atribuidas a um desenvolvedor
 	 * @param email Email do desenvolvedor
 	 * @return ArrayList<TipoAtividade>
 	 * @throws DescricaoInvalidaException 
 	 */
 	public ArrayList<TipoAtividade> getAtividadesDoDesenvolvedor(String email) throws DescricaoInvalidaException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DescricaoInvalidaException();
 		
 		return servicoDesenvolvedor.getAtividadesDoDesenvolvedor(email);
 	}
 	
 	/**
 	 * Esse mtodo retorna uma lista de conhecimentos que o desenvolvedor possui
 	 * @param email Email do desenvolvedor
 	 * @return ArrayList<Conhecimento>
 	 * @throws DescricaoInvalidaException 
 	 */
 	public ArrayList<Conhecimento> getConhecimentosDoDesenvolvedor(String email) 
 		throws DescricaoInvalidaException  {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DescricaoInvalidaException();
 		
 		return servicoDesenvolvedor.getConhecimentosDoDesenvolvedor(email);
 	}
 	
 	/**
 	 * Este mtodo retorna o desenvolvedor que possui o email passado no parametro.
 	 * @param email Email do desenvolvedor.
 	 * @return <Desenvolvedor> 
 	 * @throws DesenvolvedorInexistenteException 
 	 */
 	public Desenvolvedor getDesenvolvedor(String email) throws DesenvolvedorInexistenteException {
 		
 		Desenvolvedor desenvolvedor = servicoDesenvolvedor.getDesenvolvedor(email);
 		if (desenvolvedor == null) throw new DesenvolvedorInexistenteException();
 		
 		return desenvolvedor;
 	}
 	
 	/**
 	 * Esse mtodo remove um conhecimento associado a um desenvolvedor especfico.
 	 * @param emailDesenvolvedor Email do desenvolvedor.
 	 * @param nomeConhecimento Nome do conhecimento a ser removido do desenvolvedor.
 	 * @return true se a associacao foi desfeita.
 	 */
 	public boolean removerConhecimentoDoDesenvolvedor(
 			String emailDesenvolvedor, String nomeConhecimento) {
 		
 		return servicoDesenvolvedor.removerConhecimentoDoDesenvolvedor(emailDesenvolvedor, nomeConhecimento);
 	}
 	
 	/**
 	 * Esse mtodo remove um desenvolvedor da base de dados. 
 	 * @param email Email do desenvolvedor.
 	 * @return true se o desenvolvedor foi removido com sucesso.
 	 */
 	public boolean removerDesenvolvedor(String email) {
 		
 		// Remover Solucoes do Desenvolvedor
 		ArrayList<Solucao> solucoes = servicoSolucao.listarSolucoesDoDesenvolvedor(email);
 		Iterator<Solucao> it1 = solucoes.iterator();
 		
 		while (it1.hasNext()) {
 			Solucao solucao = it1.next();
 			servicoSolucao.removerSolucao(solucao.getId());
 		}
 		
 		// Desassociar Conhecimentos
 		ArrayList<Conhecimento> conhecimentos = servicoDesenvolvedor.getConhecimentosDoDesenvolvedor(email);
 		Iterator<Conhecimento> it2 = conhecimentos.iterator();
 		
 		while (it2.hasNext()) {
 			Conhecimento conhecimento = it2.next();
 			servicoDesenvolvedor.removerConhecimentoDoDesenvolvedor(email, conhecimento.getNome());
 		}
 		
 		return servicoDesenvolvedor.removerDesenvolvedor(email);
 	}
 	
 	/**
 	 * Lista todos os desenvolvedores do banco.
 	 * @return
 	 */
 	public ArrayList<Desenvolvedor> getListaDesenvolvedores() {
 		return servicoDesenvolvedor.getTodosDesenvolvedores();
 	}
 	
 	/**
 	 * Esse mtodo retorna a quantidade de respostas que um desenvolvedor tem em um conhecimento/
 	 * @param email Email do desenvolvedor que possui tal conhecimento.
 	 * @param nomeConhecimento Nome do conhecimento associado ao desenvolvedor. 
 	 * @return quantidade de respostas.
 	 */
 	public int getQntResposta(String email, String nomeConhecimento) 
 		throws ConhecimentoInexistenteException, DesenvolvedorInexistenteException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.getQntResposta(email, nomeConhecimento);
 	}
 	
 	/**
 	 * Esse mtodo retorna o grau de conhecimento que um desenvolvedor tem em um conhecimento/
 	 * @param email Email do desenvolvedor que possui tal conhecimento.
 	 * @param nomeConhecimento Nome do conhecimento associado ao desenvolvedor. 
 	 * @return grau de conhecimento.
 	 */
 	public int getGrau(String email, String nomeConhecimento) 
 		throws ConhecimentoInexistenteException, DesenvolvedorInexistenteException {
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.getGrau(email, nomeConhecimento);
 	}
 	
 	/**
 	 * Esse mtodo altera a quantidade de respostas que um desenvolvedor possui em um conhecimento/
 	 * @param email Email do desenvolvedor que possui tal conhecimento.
 	 * @param nomeConhecimento Nome do conhecimento associado ao desenvolvedor. 
 	 * @return true se a atualizao foi feita com sucesso.
 	 */
 	public boolean updateQntResposta(String email, String nomeConhecimento, int quantidade)
 		throws DesenvolvedorInexistenteException, ConhecimentoInexistenteException{
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.updateQntResposta(email, nomeConhecimento, quantidade);
 		
 	}
 	
 	/**
 	 * Esse mtodo altera o grau de conhecimento de um desenvolvedor em um determinado conhecimento/
 	 * @param email Email do desenvolvedor que possui tal conhecimento.
 	 * @param nomeConhecimento Nome do conhecimento associado ao desenvolvedor. 
 	 * @return true se a atualizao foi feita com sucesso.
 	 */
 	public boolean updateGrau(String email, String nomeConhecimento, int grau)
 		throws DesenvolvedorInexistenteException, ConhecimentoInexistenteException{
 		
 		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		if (!servicoConhecimento.conhecimentoExiste(nomeConhecimento)) throw new ConhecimentoInexistenteException();
 		
 		return servicoDesenvolvedor.updateGrau(email, nomeConhecimento, grau);
 		
 	}
 	
 	/**
 	 * Metodo que autentica um usuario para acesso ao sistema.
 	 * @param email Email do desenvolvedor
 	 * @param senha Senha do desenvolvedor
 	 * @return Desenvolvedor. Retorna o objeto desenvolvedor em caso positivo.
 	 * @throws ErroDeAutenticacaoException EmailInvalidoException, 
 	 * SenhaInvalidaException, DesenvolvedorInexistenteException
 	 */
 	public Desenvolvedor autenticaDesenvolvedor(DadosAutenticacao dados) throws DesenvolvedorInexistenteException,
 			EmailInvalidoException, SenhaInvalidaException, ErroDeAutenticacaoException {
 		
 		String email = dados.getUser();
 		String senha = dados.getPasswd();
 		
 //		if (!ValidacaoUtil.validaEmail(email)) throw new EmailInvalidoException();
 //		if (!ValidacaoUtil.validaSenha(senha)) throw new SenhaInvalidaException();
 //		
 //		if (!servicoDesenvolvedor.desenvolvedorExiste(email)) throw new DesenvolvedorInexistenteException();
 		
 		Desenvolvedor desenvolvedor = servicoDesenvolvedor.autenticaDesenvolvedor(email, senha);
 		
 		if (desenvolvedor == null) throw new ErroDeAutenticacaoException();
 		
 		return desenvolvedor;
 	}
 
 }
