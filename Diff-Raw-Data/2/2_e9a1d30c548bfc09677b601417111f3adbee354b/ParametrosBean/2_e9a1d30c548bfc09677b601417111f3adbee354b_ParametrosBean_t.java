 package br.com.sw2.gac.bean;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.event.ActionEvent;
 
 import br.com.sw2.gac.business.ParametroBusiness;
 import br.com.sw2.gac.exception.BusinessException;
 import br.com.sw2.gac.vo.ParametroVO;
 
 /**
  * <b>Descrição: Controller da tela de configuracao de parametros.</b> <br>
  * .
  * @author: SW2
  * @version 1.0 Copyright 2012 SmartAngel.
  */
 @ManagedBean
 @ViewScoped
 public class ParametrosBean extends BaseBean {
 
     /** Constante serialVersionUID. */
     private static final long serialVersionUID = 4107789141198966008L;
 
     private ParametroBusiness parametroBusiness = new ParametroBusiness();
 
     /** Atributo parametro. */
     private ParametroVO parametro;
 
     /**
      * Construtor Padrao Instancia um novo objeto ParametrosBean.
      */
     public ParametrosBean() {
         this.parametro = this.parametroBusiness.recuperarParametros();
         if (null == parametro) {
             parametro = new ParametroVO();
         }
     }
 
     /**
      * Nome: salvar Salvar.
      * @param event the event
      * @see
      */
     public void salvar(ActionEvent event) {
         this.getLogger().debug("***** Iniciando método salvar *****");
         this.getLogger().debug("Dias bem estar: " + this.parametro.getDiasBemEstar());
         this.getLogger().debug("Dias dados: " + this.parametro.getDiasDados());
         this.getLogger().debug("Total Rotina Cliente: " + this.parametro.getToleraRotinaCliente());
         // Criar o novo parametro com os dados informados pelo usuario
 
         try {
             this.parametroBusiness.adicionarNovoParametro(this.parametro);
            //Atualiza para recuperar o ID;
            this.parametro = this.parametroBusiness.recuperarParametros();
             setFacesMessage("message.parametros.save.sucess");
         } catch (BusinessException e) {
             setFacesMessage("message.generic.system.unavailable");
             this.getLogger().error(e);
         }
         this.getLogger().debug("***** Finalizando método salvar *****");
     }
 
     /**
      * Nome: getParametro Recupera o valor do atributo 'parametro'.
      * @return valor do atributo 'parametro'
      * @see
      */
     public ParametroVO getParametro() {
         return parametro;
     }
 
     /**
      * Nome: setParametro Registra o valor do atributo 'parametro'.
      * @param parametro valor do atributo parametro
      * @see
      */
     public void setParametro(ParametroVO parametro) {
         this.parametro = parametro;
     }
 }
