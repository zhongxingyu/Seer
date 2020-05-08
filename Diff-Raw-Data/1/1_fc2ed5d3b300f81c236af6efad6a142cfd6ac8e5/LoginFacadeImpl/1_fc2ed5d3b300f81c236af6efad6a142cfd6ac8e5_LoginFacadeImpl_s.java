 /**
  * 
  */
 package br.com.fwcenter.model.facade.login;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import br.com.fwcenter.model.dao.ClienteDAO;
import br.com.fwcenter.model.vo.ClienteDTO;
 import br.com.fwcenter.model.vo.UsuarioVO;
 
 @Service
 public class LoginFacadeImpl implements LoginFacade{
 	
 	@Autowired
 	private ClienteDAO clienteDAO;
 	
 	public boolean isPossuiAcesso(UsuarioVO pUsuarioVO){
 		//TODO Implementar lgica para autenticar usurio.
 		return true;
 	}
 	
 	
 	
 	/**
 	 * Retorna o valor do campo 'clienteDAO'
 	 * @return O valor de 'clienteDAO'
 	 */
 	public ClienteDAO getClienteDAO() {
 		return clienteDAO;
 	}
 	
 	/**
 	 * Define o valor para 'clienteDAO'
 	 * @param clienteDAO - Valor que ser usado para definir o campo 'clienteDAO'
 	 */
 	public void setClienteDAO(ClienteDAO clienteDAO) {
 		this.clienteDAO = clienteDAO;
 	}
 }
