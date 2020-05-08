 package br.com.capelli.secretsanta.controller;
 
 import java.io.Serializable;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.log4j.Logger;
 
 import br.com.capelli.secretsanta.dao.ResultadoDAO;
 import br.com.capelli.secretsanta.exception.DAOException;
 import br.com.capelli.secretsanta.manager.SorteioManager;
 import br.com.capelli.secretsanta.modelo.Resultado;
 import br.com.capelli.secretsanta.modelo.Usuario;
 import br.com.capelli.secretsanta.util.LoggedIn;
 import br.com.capelli.secretsanta.util.Util;
 
 @Named("sorteioController")
 @SessionScoped
 public class SorteioController implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(SorteioController.class);
 
 	private @Inject
 	SorteioManager sorteioManager;
 	private @Inject
 	ResultadoDAO resultadoDAO;
 	@Inject
 	@LoggedIn
 	private Usuario usuarioLogado;
 
 	private Resultado resultado = null;
 	private String codigoPessoal = null;
 	private boolean next = Boolean.FALSE;;
 
 	public void pesquisar() {
 		try {
			String codigoLimpo = Util.retiraCaracteres(codigoPessoal.replace(" ", ""));
 			
 			resultado = sorteioManager.obtemResultado(codigoLimpo);
 			if (resultado == null) {
 				FacesContext
 						.getCurrentInstance()
 						.addMessage(
 								null,
 								new FacesMessage(
 										FacesMessage.SEVERITY_ERROR,
 										"Nenhum resultado encontrado com esse código: "
 												+ codigoPessoal
 												+ ". Confirme se digitou corretamente.",
 										""));
 			} else if (resultado.getVisualizado()) {
 
 				FacesContext
 						.getCurrentInstance()
 						.addMessage(
 								null,
 								new FacesMessage(
 										FacesMessage.SEVERITY_ERROR,
 										"Esse código pessoal já foi utilizado para visualizar o amigo secreto. Dúvidas procure o Bernardo Capelli.",
 										""));
 
 			} else {
 				next = Boolean.TRUE;
 			}
 
 		} catch (Exception e) {
 			FacesContext.getCurrentInstance().addMessage(
 					null,
 					new FacesMessage(FacesMessage.SEVERITY_ERROR,
 							"Erro inesperado, tente mais tarde.", ""));
 		}
 
 	}
 
 	public void visualizado() {
 
 		try {
 			resultado.setVisualizado(Boolean.TRUE);
 			resultadoDAO.update(resultado);
 		} catch (DAOException e) {
 			logger.error("NAO ATUALIZOU O RESULTADO.", e);
 		}
 
 		limparCampos();
 	}
 
 	public void limparCampos() {
 		codigoPessoal = null;
 		next = Boolean.FALSE;
 		resultado = null;
 	}
 
 	public void gerarSorteio() {
 
 		try {
 
 			logger.info("INICIO SORTEIO: " + usuarioLogado.getLogin());
 			logger.info("Amigos vazio?: " + usuarioLogado.getAmigos().isEmpty());
 
 			sorteioManager.gerarSorteio(usuarioLogado.getLogin());
 
 			FacesContext.getCurrentInstance()
 					.addMessage(
 							null,
 							new FacesMessage(FacesMessage.SEVERITY_INFO,
 									"SUCESSO.", ""));
 		} catch (Exception e) {
 			FacesContext.getCurrentInstance().addMessage(
 					null,
 					new FacesMessage(FacesMessage.SEVERITY_ERROR,
 							"Erro ao gerar sorteio.", ""));
 		}
 
 	}
 
 	public Resultado getResultado() {
 		return resultado;
 	}
 
 	public void setResultado(Resultado resultado) {
 		this.resultado = resultado;
 	}
 
 	public boolean isNext() {
 		return next;
 	}
 
 	public void setNext(boolean next) {
 		this.next = next;
 	}
 
 	public String getCodigoPessoal() {
 		return codigoPessoal;
 	}
 
 	public void setCodigoPessoal(String codigoPessoal) {
 		this.codigoPessoal = codigoPessoal;
 	}
 
 }
