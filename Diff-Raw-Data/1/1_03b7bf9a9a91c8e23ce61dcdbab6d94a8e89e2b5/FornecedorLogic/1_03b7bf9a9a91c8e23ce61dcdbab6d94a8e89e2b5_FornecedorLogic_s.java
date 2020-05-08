 /**
  * Copyright 2010 UNCISAL Universidade de Ciências em Saúde do Estado de Alagoas.
  * 
  * This file is part of SIAPNET.
  *
  * SIAPNET is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * SIAPNET is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SIAPNET.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package br.edu.uncisal.almoxarifado.logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vraptor.annotations.Component;
 import org.vraptor.annotations.InterceptedBy;
 import org.vraptor.annotations.Out;
 import org.vraptor.annotations.Parameter;
 import org.vraptor.i18n.Message;
 import org.vraptor.validator.ValidationErrors;
 
 import br.edu.uncisal.almoxarifado.dao.DaoFactory;
 import br.edu.uncisal.almoxarifado.dao.FornecedorDao;
 import br.edu.uncisal.almoxarifado.dao.GrupoDao;
 import br.edu.uncisal.almoxarifado.dao.UfDao;
 import br.edu.uncisal.almoxarifado.model.Endereco;
 import br.edu.uncisal.almoxarifado.model.Fornecedor;
 import br.edu.uncisal.almoxarifado.model.Grupo;
 import br.edu.uncisal.almoxarifado.model.Municipio;
 import br.edu.uncisal.almoxarifado.model.TipoFornecedor;
 import br.edu.uncisal.almoxarifado.model.Uf;
 import br.edu.uncisal.almoxarifado.model.Usuario;
 
 /**
 Classe controller para CRUD Fornecedor
  */
 @SuppressWarnings("unused")
 @Component("fornecedor")
 @InterceptedBy({DaoInterceptor.class, AutorizadorInterceptor.class})
 public class FornecedorLogic {
 
     @Parameter
     @Out
     private Fornecedor fornecedor = new Fornecedor();
     @Parameter
     private Endereco endereco = new Endereco();
     @Parameter
     private String modal;
     @Parameter
     @Out
     private String[] gruposArray;
     @Out
     private List<Fornecedor> fornecedores;
     @Out
     private List<Grupo> grupos;
     @Out
     private List<Uf> ufs;
     @Out
     private List<Municipio> municipios;
     @Out
     private String msgErro;
     @Out
     private String message;
     @Out
     @Parameter
     private String criteria;
 
     private UfDao ufDao;
     private GrupoDao grupoDao;
     private FornecedorDao fornecedorDao;
     
     public FornecedorLogic(DaoFactory daoFactory, Usuario usuario) {
     	ufDao = daoFactory.getUfDao();
     	grupoDao = daoFactory.getGrupoDao();
     	
         this.endereco.setMunicipio(new Municipio());
     }
 
     /**
      * Método que inicializa os objetos que são carregados na combobox da página formulario
      */
     public String formulario() {
         grupos = grupoDao.listAll();
         ufs = ufDao.listAll();
         Uf uf = new Uf();
         uf.setId(new Long("27"));
         municipios = ufDao.getById(uf.getId()).getMunicipios();
         return "ok";
     }
 
     /** Método que inicializa os objetos para o formulário, carregado no formModal */
     public String formModal() {
         modal = "modal";
         return formulario();
     }
 
 
     /**
      * Método que carrega o objeto fornecedor do tipo Assistencia técnica na tela do formulário.
      */
     public String formAssistenciaTec() {
         fornecedor.setTipoFornecedor(new TipoFornecedor(new Long(1)));
         return formulario();
     }
 
     /**
      * Método que grava o objeto fornecedor carregado com os campos do formulário.
      */
     public String gravar() {
         /* adiciona os campos do endereço ao fornecedor - objeto endereço */
         this.fornecedor.setEndereco(this.endereco);
 
         /* adiciona os ids selecionados do listbox grupos
         à coleção grupos do objeto fornecedor*/
         List<Grupo> gruposF = new ArrayList<Grupo>();
         Grupo grupoF;
         for (String grupoId : gruposArray) {
             grupoF = new Grupo();
             grupoF.setId(new Long(grupoId));
             gruposF.add(grupoF);
         }
         this.fornecedor.setGrupos(gruposF);
 
         /** grava no sistema o fornecedor cadastrado */
         try {
             fornecedorDao.save(fornecedor);
             message = "Fornecedor foi gravado com sucesso.";
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "ok";
     }
 
     public String gravarModal(){
         return gravar();
     }
 
     //TODO verificar se o fornecedor já existe.
     public void validateGravar(ValidationErrors errors) {
         if (fornecedor.getNome() == null || fornecedor.getNome().equals("")) {
             errors.add(new Message("aviso", "Um nome para o o fornecedor não foi definido."));
         }
 
         if (fornecedor.getCpfCnpj() == null || fornecedor.getCpfCnpj().equals("")) {
             errors.add(new Message("aviso", "Um cpf/cnpj precisa ser definido."));
         }
 
         if (fornecedor.getRazaoSocial() == null || fornecedor.getRazaoSocial().equals("")) {
             errors.add(new Message("aviso", "Um razão social não foi definida."));
         }
 
         if (gruposArray == null || gruposArray.length == 0) {
             errors.add(new Message("aviso", "Pelo menos um grupo deve ser escolhido."));
         }
 
         if (errors.size() > 0) {
             grupos = grupoDao.listAll();
             ufs = ufDao.listAll();
             Uf uf = new Uf();
             uf.setId(new Long("27"));
             municipios = ufDao.getById(uf.getId()).getMunicipios();
         }
     }
 
     public void validateGravarModal(ValidationErrors errors) {
         validateGravar(errors);
     }
 
     /** Lista todos os fornecedores cadastrados no sistema */
     public String listAll() {
         this.fornecedores = fornecedorDao.listAll();
         return "ok";
     }
 
     /** Lista todos os fornecedores para comboBox */
     public String loadFornecedores() {
         this.fornecedores = fornecedorDao.listAll();
         return "ok";
     }
 
     /** Apaga do sistema o fornecedor cujo id foi enviado pela página */
     public String remove() {
         try {
             fornecedor = fornecedorDao.getById(fornecedor.getId());
             if (fornecedor == null) {
                 msgErro = "Não existe registro desse fornecedor para o id informado.";
             } else {
             	fornecedor.setGrupos(null);
             	fornecedorDao.save(fornecedor);
                 fornecedorDao.remove(fornecedor);
             }
             message = "O registro do fornecedor foi removido com sucesso.";
         } catch (Exception e) {
             e.printStackTrace();
             msgErro = "Não foi possível remover fornecedor devido ao seguinte problema:<br>" + e.getMessage();
         }
 
         return "ok";
     }
 
     /** Carrega o objeto fornecedor para a página de alteração cujo id foi enviado pela página anterior */
     public String get() {
         this.fornecedor = fornecedorDao.getById(fornecedor.getId());
         grupos = grupoDao.listAll();
         ufs = ufDao.listAll();
         if (fornecedor.getEndereco() != null) {
             municipios = ufDao.getById(fornecedor.getEndereco().getMunicipio().getUf().getId()).getMunicipios();
         } else {
             Uf uf = new Uf();
             uf.setId(new Long("27"));
             municipios = ufDao.getById(uf.getId()).getMunicipios();
         }
         return "ok";
     }
 
     /** Carrega o objeto fornecedor para a página de visualização */
     public String ver() {
         this.fornecedor = fornecedorDao.getById(fornecedor.getId());
         grupos = grupoDao.listAll();
         return "ok";
     }
 
     /** Carrega o objeto fornecedor para a janela Modal */
     public String verModal() {
         this.fornecedor = fornecedorDao.getById(fornecedor.getId());
         grupos = grupoDao.listAll();
         return "ok";
     }
 
     /** Inicializa a página filtro de busca de fornecedores */
     public String filtro() {
         return "ok";
     }
 
     /** Busca fornecedores pelo critério montado pela página de filtro */
     public String buscar() {
         fornecedor.setNome(((fornecedor.getNome() == null || "".equals(fornecedor.getNome())) ? null : "%".concat(fornecedor.getNome()).concat("%")));
         fornecedor.setCpfCnpj(((fornecedor.getCpfCnpj() == null || "".equals(fornecedor.getCpfCnpj())) ? null : "%".concat(fornecedor.getCpfCnpj()).concat("%")));
         fornecedor.setRazaoSocial(((fornecedor.getRazaoSocial() == null || "".equals(fornecedor.getRazaoSocial())) ? null : "%".concat(fornecedor.getRazaoSocial()).concat("%")));
         fornecedor.setInscricaoEstadual(((fornecedor.getInscricaoEstadual() == null || "".equals(fornecedor.getInscricaoEstadual())) ? null : "%".concat(fornecedor.getInscricaoEstadual()).concat("%")));
         fornecedor.setPessoaJuridica(((fornecedor.getPessoaJuridica() == null || "".equals(fornecedor.getPessoaJuridica())) ? null : fornecedor.getPessoaJuridica()));
         fornecedores = fornecedorDao.findByExample(fornecedor);
         return "ok";
     }
 
     /** Lista todos fornecedores que são assistencia técnica */
     public String listAssistenciasTec() {
         fornecedores = fornecedorDao.listAllbyTipoFornecedor(new TipoFornecedor(TipoFornecedor.ASSISTENCIA_TECNICA));
         return "ok";
     }
     
     /** Retorna a lista de Fornecedor(es) conforme parâmetro passado da busca */
     public String buscarFornecedor() {
     	fornecedores = fornecedorDao.buscar(criteria);
         return "ok";
     }
 }
