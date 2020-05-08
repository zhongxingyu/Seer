 package br.udesc.loman.web.beans;
 
 import br.udesc.controle.UdescException;
 import br.udesc.loman.controle.CadastroProjetosUC;
 import br.udesc.loman.modelo.*;
 import br.udesc.loman.web.AutenticacaoUtil;
 import br.udesc.loman.web.LoManListener;
 import br.udesc.web.CRUD;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 @ManagedBean
 @SessionScoped
 public class CadProjeto extends CRUD<Projeto, String>{
 
     private List<Usuario> usuarios;
     private final CadastroProjetosUC cpuc;
 
     public CadProjeto() {
         super(new CadastroProjetosUC(LoManListener.getDAOFactory()),
                 "projeto", LoManListener.getDAOFactory(), new String[]{"titulo"});
         this.cpuc = (CadastroProjetosUC) cuc;
 
         try {
             this.usuarios = LoManListener.getDAOFactory().getUsuarioDAO().listarTodos();
         } catch (Exception ex) {
             Logger.getLogger(CadProjeto.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public List<Usuario> getUsuarios() {
         return usuarios;
     }
 
     public String novoProjeto() throws Exception {
         novo();
         membro = null;
         unidade = null;
         return "projetoscad";
     }
 
     @Override
     protected String getDadoCampoPesquisa() {
         return this.getRegistro().getTitulo();
     }
 
     @Override
     protected void setDadoCampoPesquisa(String valor) {
         this.getRegistro().setTitulo(valor);
     }
     /**
      * *******************************
      */
     /*
      * Tratamento da caixa de membros
      */
     /**
      * *******************************
      */
     private MembroEquipe copia;
     private MembroEquipe membro;
 
     public MembroEquipe getMembro() {
         if (membro == null) {
             membro = new MembroEquipe(null, PapelEnum.AVALIADOR, null);
         }
         return membro;
     }
 
     public void setMembro(MembroEquipe membro) {
         this.membro = membro;
     }
 
     public List<Usuario> complete(String query) throws Exception {
         return cpuc.listarPossiveisMembros(this.getRegistro(), query);
     }
 
     public void addMember() {
         try {
             cpuc.addMembro(this.getRegistro(), membro.getUsuario(), membro.getPapelEnum());
         } catch (UdescException ex) {
             addMessage(ex);
             return;
         }
         this.membro = null;
 
     }
 
     public void newMember() {
         this.membro.setUsuario(copia.getUsuario());
         this.membro.setPapelEnum(copia.getPapelEnum());
         this.membro = null;
     }
 
     public void updateMember() {
         this.membro = null;
        mensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Usurio alterado.");
     }
 
     public MembroEquipe getSelecMembro() {
         return membro;
     }
 
     public void setSelecMembro(MembroEquipe selecMembro) {
         this.membro = selecMembro;
         this.copia = new MembroEquipe(selecMembro.getUsuario(), selecMembro.getPapelEnum(), selecMembro.getProjeto());
 
     }
 
     public void deleteMember() {
         List<MembroEquipe> eq = this.getRegistro().getEquipe();
         for (MembroEquipe e : eq) {
             if (e.getUsuario().getId() == this.membro.getUsuario().getId()) {
                 this.getRegistro().getEquipe().remove(e);
                 this.membro = null;
                 return;
             }
         }
 
     }
     /**
      * *******************************
      */
     /*
      * Tratamento da matriz de DI
      */
     /**
      * *******************************
      */
     /**
      * * Unidade **
      */
     private Unidade unidade;
     private boolean ultAcaoIncluir = true;
 
     public boolean isUltAcaoIncluir() {
         return ultAcaoIncluir;
     }
 
     public void novaUnid() {
         this.unidade = new Unidade();
         this.ultAcaoIncluir = true;
 
         novoObjetivo();
         novoConteudo();
         novaAtividade();
         novaFormaAvaliacao();
     }
 
     public Unidade getSelecUnidade() {
         if (unidade == null) {
             novaUnid();
         }
         return unidade;
     }
 
     public void setSelecUnidade(Unidade selecUnidade) {
         this.unidade = selecUnidade;
         this.ultAcaoIncluir = false;
     }
     private Ocorrencia ocorrencia = new Ocorrencia();
 
     public Ocorrencia getOcorrencia() {
         return ocorrencia;
     }
 
     public void setOcorrencia(Ocorrencia ocorrencia) {
         this.ocorrencia = ocorrencia;
     }
 
     public void novaOcorrencia() {
         this.ocorrencia = new Ocorrencia();
     }
 
     public void setarOcorrenciaParaUnidade(String descricao) {
         this.ocorrencia.setDataModificacao(new java.util.Date());
         this.ocorrencia.setDescricao(descricao);
         this.ocorrencia.setUsuario(AutenticacaoUtil.getInstance().getUsuarioSessao());
         this.ocorrencia.setUnidade(getSelecUnidade());
         getSelecUnidade().getOcorrencias().add(ocorrencia);
     }
 
     public void addUnidade() throws Exception {
         if (ultAcaoIncluir == false) {
             setarOcorrenciaParaUnidade("Unidade Modificada");
         } else {
             setarOcorrenciaParaUnidade("Unidade Aberta");
         }
         getRegistro().addUnidade(getSelecUnidade());
         novaOcorrencia();
         novaUnid();
     }
 
     public void updateUnidade() {
     }
 
     public void deleteUnidade() {
         getRegistro().getUnidades().remove(unidade);
     }
     /**
      * *******************************
      */
     /*
      * Objetivo
      */
     /**
      * *******************************
      */
     private Objetivo objetivo = new Objetivo();
     private int indexObj = -1;
     private boolean alterarObjetivo = false;
 
     public boolean isAlterarObjetivo() {
         return alterarObjetivo;
     }
 
     public void setAlterarObjetivo(boolean alterarObjetivo) {
         this.alterarObjetivo = alterarObjetivo;
     }
 
     public Objetivo getSelecObjetivo() {
         return objetivo;
     }
 
     public void setSelecObjetivo(Objetivo objetivo) {
         this.objetivo = objetivo;
         this.indexObj = this.getSelecUnidade().getObjetivos().indexOf(objetivo);
         this.alterarObjetivo = true;
     }
 
     public void novoObjetivo() {
         setSelecObjetivo(new Objetivo());
         alterarObjetivo = false;
     }
 
     public void addObjetivo() {
         if (getSelecObjetivo().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             getSelecObjetivo().setUnidade(getSelecUnidade());
             this.getSelecUnidade().getObjetivos().add(getSelecObjetivo());
             novoObjetivo();
         }
     }
 
     public void alterarObjetivo() {
         if (getSelecObjetivo().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             this.getSelecUnidade().getObjetivos().set(indexObj, getSelecObjetivo());
             novoObjetivo();
         }
     }
 
     public void deleteObjetivo() {
         objetivo = this.getSelecUnidade().getObjetivos().get(indexObj);
         getSelecUnidade().getObjetivos().remove(objetivo);
         novoObjetivo();
     }
     /**
      * *******************************
      */
     /*
      * Conteúdo
      */
     /**
      * *******************************
      */
     private Conteudo conteudo = new Conteudo();
     private int indexCon = -1;
     private boolean alterarConteudo = false;
 
     public boolean isAlterarConteudo() {
         return alterarConteudo;
     }
 
     public void setAlterarConteudo(boolean alterarConteudo) {
         this.alterarConteudo = alterarConteudo;
     }
 
     public Conteudo getSelecConteudo() {
         return conteudo;
     }
 
     public void setSelecConteudo(Conteudo conteudo) {
         this.conteudo = conteudo;
         this.indexCon = this.getSelecUnidade().getConteudos().indexOf(conteudo);
         this.alterarConteudo = true;
     }
 
     public void novoConteudo() {
         setSelecConteudo(new Conteudo());
         alterarConteudo = false;
     }
 
     public void addConteudo() {
         if (getSelecConteudo().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             getSelecConteudo().setUnidade(getSelecUnidade());
             this.getSelecUnidade().getConteudos().add(getSelecConteudo());
             novoConteudo();
         }
     }
 
     public void alterarConteudo() {
         if (getSelecConteudo().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             this.getSelecUnidade().getConteudos().set(indexCon, getSelecConteudo());
             novoConteudo();
         }
     }
 
     public void deleteConteudo() {
         conteudo = this.getSelecUnidade().getConteudos().get(indexCon);
         getSelecUnidade().getConteudos().remove(conteudo);
         novoConteudo();
     }
     /**
      * *******************************
      */
     /*
      * Atividades
      */
     /**
      * *******************************
      */
     private Atividade atividade = new Atividade();
     private int indexAti = -1;
     private boolean alterarAtividade = false;
 
     public boolean isAlterarAtividade() {
         return alterarAtividade;
     }
 
     public void setAlterarAtividade(boolean alterarAtividade) {
         this.alterarAtividade = alterarAtividade;
     }
 
     public Atividade getSelecAtividade() {
         return atividade;
     }
 
     public void setSelecAtividade(Atividade atividade) {
         this.atividade = atividade;
         this.indexAti = this.getSelecUnidade().getAtividades().indexOf(atividade);
         this.alterarAtividade = true;
     }
 
     public void novaAtividade() {
         setSelecAtividade(new Atividade());
         alterarAtividade = false;
     }
 
     public void addAtividade() {
         if (getSelecAtividade().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             getSelecAtividade().setUnidade(getSelecUnidade());
             this.getSelecUnidade().getAtividades().add(getSelecAtividade());
             novaAtividade();
         }
     }
 
     public void alterarAtividade() {
         if (getSelecAtividade().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             this.getSelecUnidade().getAtividades().set(indexAti, getSelecAtividade());
             novaAtividade();
         }
     }
 
     public void deleteAtividade() {
         atividade = this.getSelecUnidade().getAtividades().get(indexAti);
         getSelecUnidade().getAtividades().remove(atividade);
         novaAtividade();
     }
     /**
      * *******************************
      */
     /*
      * Formas de Avaliação
      */
     /**
      * *******************************
      */
     private FormaAvaliacao formaAvaliacao = new FormaAvaliacao();
     private int indexForAva = -1;
     private boolean alterarFormaAvaliacao = false;
 
     public boolean isAlterarFormaAvaliacao() {
         return alterarFormaAvaliacao;
     }
 
     public void setAlterarFormaAvaliacao(boolean alterarFormaAvaliacao) {
         this.alterarFormaAvaliacao = alterarFormaAvaliacao;
     }
 
     public FormaAvaliacao getSelecFormaAvaliacao() {
         return formaAvaliacao;
     }
 
     public void setSelecFormaAvaliacao(FormaAvaliacao formaAvaliacao) {
         this.formaAvaliacao = formaAvaliacao;
         this.indexForAva = this.getSelecUnidade().getFormasAvaliacao().indexOf(formaAvaliacao);
         this.alterarFormaAvaliacao = true;
     }
 
     public void novaFormaAvaliacao() {
         setSelecFormaAvaliacao(new FormaAvaliacao());
         alterarFormaAvaliacao = false;
     }
 
     public void addFormaAvaliacao() {
         if (getSelecFormaAvaliacao().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             getSelecFormaAvaliacao().setUnidade(getSelecUnidade());
             this.getSelecUnidade().getFormasAvaliacao().add(getSelecFormaAvaliacao());
             novaFormaAvaliacao();
         }
     }
 
     public void alterarFormaAvaliacao() {
         if (getSelecFormaAvaliacao().getDescricao().trim().equals("")) {
             mensagem(FacesMessage.SEVERITY_ERROR, "Descrição", "Campo descrição é obrigatório!");
         } else {
             this.getSelecUnidade().getFormasAvaliacao().set(indexForAva, getSelecFormaAvaliacao());
             novaFormaAvaliacao();
         }
     }
 
     public void deleteFormaAvaliacao() {
         formaAvaliacao = this.getSelecUnidade().getFormasAvaliacao().get(indexForAva);
         getSelecUnidade().getFormasAvaliacao().remove(formaAvaliacao);
         novaFormaAvaliacao();
     }
 
     public void mensagem(FacesMessage.Severity severity, String titulo, String mensagem) {
         FacesMessage msg = new FacesMessage(severity, titulo, mensagem);
         FacesContext.getCurrentInstance().addMessage(null, msg);
     }
 }
