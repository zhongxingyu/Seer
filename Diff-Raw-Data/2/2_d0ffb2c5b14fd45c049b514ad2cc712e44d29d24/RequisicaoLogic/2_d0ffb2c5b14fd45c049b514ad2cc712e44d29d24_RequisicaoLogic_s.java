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
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.vraptor.annotations.Component;
 import org.vraptor.annotations.In;
 import org.vraptor.annotations.InterceptedBy;
 import org.vraptor.annotations.Out;
 import org.vraptor.annotations.Parameter;
 import org.vraptor.i18n.Message;
 import org.vraptor.scope.ScopeType;
 import org.vraptor.validator.ValidationErrors;
 
 import br.edu.uncisal.almoxarifado.dao.AlmoxarifadoDao;
 import br.edu.uncisal.almoxarifado.dao.DaoFactory;
 import br.edu.uncisal.almoxarifado.dao.DepartamentoDao;
 import br.edu.uncisal.almoxarifado.dao.ItemDao;
 import br.edu.uncisal.almoxarifado.dao.ItemEstoqueDao;
 import br.edu.uncisal.almoxarifado.dao.NotaEntradaDao;
 import br.edu.uncisal.almoxarifado.dao.RequisicaoDao;
 import br.edu.uncisal.almoxarifado.dao.TipoSaidaDao;
 import br.edu.uncisal.almoxarifado.dao.UsuarioDao;
 import br.edu.uncisal.almoxarifado.dao.UsuarioDepartamentoDao;
 import br.edu.uncisal.almoxarifado.model.Almoxarifado;
 import br.edu.uncisal.almoxarifado.model.Departamento;
 import br.edu.uncisal.almoxarifado.model.Item;
 import br.edu.uncisal.almoxarifado.model.ItemEntrada;
 import br.edu.uncisal.almoxarifado.model.ItemEstoque;
 import br.edu.uncisal.almoxarifado.model.ItemRequisicao;
 import br.edu.uncisal.almoxarifado.model.ItemSaida;
 import br.edu.uncisal.almoxarifado.model.NotaEntrada;
 import br.edu.uncisal.almoxarifado.model.Perfil;
 import br.edu.uncisal.almoxarifado.model.Requisicao;
 import br.edu.uncisal.almoxarifado.model.TipoSaida;
 import br.edu.uncisal.almoxarifado.model.TipoStatus;
 import br.edu.uncisal.almoxarifado.model.Usuario;
 import br.edu.uncisal.almoxarifado.model.UsuarioDepartamento;
 import br.edu.uncisal.almoxarifado.util.NotFoundException;
 
 @Component("requisicao")
 @InterceptedBy({DaoInterceptor.class, AutorizadorInterceptor.class})
 public class RequisicaoLogic {
 
     /**
      * Id do usuário e Id do departamento cocatenados por um "-".
      */
     @Parameter(create = true)
     @Out
     private UsuarioDepartamento usuarioDepartamento;
     
     @Parameter(create = true)
     private List<ItemSaida> itensEnviados;
     
     /**
      * Representa as quantidades dos itens na avaliação da requisição.
      * Como o vraptor 2 não suporta conversores para BigDecimal estou criando um array de floats para passar os dados para o command.
      */
     @Parameter(create = true)
     private List<String> iEQtd; 
 
     @Parameter(create = true)
     @Out(scope = ScopeType.SESSION)
     @In(scope = ScopeType.SESSION, required = false)
     private Requisicao requisicao;
     @Parameter
     @In(scope = ScopeType.SESSION)
     @Out(scope = ScopeType.SESSION)
     private Usuario authUser;
     @Parameter
     private ItemRequisicao itemRequisicao;
     @Parameter
     private String q;
     @Parameter
     @Out
     private Item item = new Item();
     @Out
     private List<Item> itens;
     
     @Parameter
     private String qtdRequisitada;
     
     @Parameter
     private Long aval;
     @Parameter
     @Out
     private String comentario;
     @Out
     private List<Requisicao> requisicoes;
     @Out
     private Collection<Usuario> consumidores;
     @Out
     private Collection<TipoSaida> tiposSaida;
     @Out
     private String msgErro;
     @Out
     private String message;
     @Out
     private String alert;
     @Out
     private Long almoxarifadoId;
     @Out
     private Collection<Departamento> departamentos;
     @Out
     private Departamento departamento;
     @Out
     private Collection<Almoxarifado> almoxarifados;
     @Out
     private Requisicao requisicaoGravada;
     @Out
     private Collection<ItemEstoque> itensEstoque;
     
     private RequisicaoDao dao;
     private ItemDao iDao;
     private UsuarioDao uDao;
     private ItemEstoqueDao iEDao;
     private AlmoxarifadoDao aDao;
     private DepartamentoDao dDao;
     private TipoSaidaDao tDao;
     private UsuarioDepartamentoDao uDDao;
     private NotaEntradaDao nDao;
 
     public RequisicaoLogic(DaoFactory daoFactory, Usuario authUser) {
     	dao = daoFactory.getRequisicaoDao();
     	iDao = daoFactory.getItemDao();
     	uDao = daoFactory.getUsuarioDao();
     	iEDao = daoFactory.getItemEstoqueDao();
     	aDao = daoFactory.getAlmoxarifadoDao();
     	dDao = daoFactory.getDepartamentoDao();
     	tDao = daoFactory.getTipoSaidaDao();
     	uDDao = daoFactory.getUsuarioDepartamentoDao();
     	nDao = daoFactory.getNotaEntradaDao();
     }
 
     public String formulario1() {
         Perfil perfilAtual = authUser.getUsuarioDepartamentoAtivo().getPerfil();
         
         if (perfilAtual.getId().equals(Perfil.ADMINISTRADOR_GERAL)) {
             almoxarifados = aDao.listAll();
         } else if (perfilAtual.getId().equals(Perfil.ADMINISTRADOR_LOCAL)) {
             almoxarifados = aDao.listByOrgao(authUser.getUsuarioDepartamentoAtivo().getDepartamento().getUnidade().getOrgao());        
         } else {
         	almoxarifados = dDao.getById(authUser.getUsuarioDepartamentoAtivo().getDepartamento().getId()).getAlmoxarifados();
         }
 
         requisicao = new Requisicao();
         return "ok";
     }
 
     public String formulario2() {
         requisicao.setAlmoxarifado(aDao.getById(requisicao.getAlmoxarifado().getId()));
         tiposSaida = tDao.listAll();
         return "ok";
     }
     
     public String notaSaida() {
         requisicao = new Requisicao();
         requisicao.setAlmoxarifado(aDao.getById(authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado().getId()));
         departamentos = dDao.listByAlmoxarifado(requisicao.getAlmoxarifado());
         tiposSaida = tDao.listAll();
         return "ok";
     }
 
     public String notaSaidaGravar() {
 		requisicao.setUsuario(uDao.getById(requisicao.getUsuario().getId()));
         requisicao.gerarNotaSaida(comentario);          
      
         for (ItemSaida is : requisicao.getItensEnviados()) {        	
         	ItemEstoque ie = iEDao.findByExample(is.getItem(), requisicao.getAlmoxarifado());
         	is.setValorUnitario(ie.getCustoMedio());
 		}
         
         dao.save(requisicao);
         
         for (ItemSaida is : requisicao.getItensEnviados()) {        	
         	iEDao.atualizaEstoque(is.getItem(), requisicao.getAlmoxarifado());
 		}
 
         
         message = "A nota de saída foi salva. Está na fila de entrega.";
         requisicao = new Requisicao();
         return "ok";
     }
 
     public void validateNotaSaidaGravar(ValidationErrors errors) {
         if (requisicao.getItensRequisitados().size() == 0) {
             errors.add(new Message("aviso", "Nenhum item requisitado."));
         }
 
         if (requisicao.getItensRequisitados().size() > 0) {
             for (ItemRequisicao ie : requisicao.getItensRequisitados()) {
                 if (ie.getQtdRequisitada().compareTo(new BigDecimal(0)) == 0) {
                     errors.add(new Message("aviso", "Existem itens com a quantidade requisitada igual a zero."));
                     break;
                 }
             }
         }
         
         //Verifica o estoque
         for (ItemRequisicao ir : requisicao.getItensRequisitados()) {
             ItemEstoque ie = iEDao.findByExample(ir.getItem(), requisicao.getAlmoxarifado());
             if (ie.getEstoque().compareTo(ir.getQtdRequisitada()) < 0) {
                 errors.add(new Message("aviso", "Não existem itens suficientes para o estoque. A quantidade para o item: " + ie.getItem().getNome() + " é: " + ie.getEstoque()));
             }
         }
         
         if(requisicao.getDepartamento().getId().equals(0L))
         	errors.add(new Message("aviso", "Departamento não escolhido."));
         
         if(requisicao.getUsuario().getId().equals(0L))
         	errors.add(new Message("aviso", "Consumidor não escolhido."));        
 
         if (errors.size() > 0) {            
             tiposSaida = tDao.listAll();
             departamentos = dDao.listByAlmoxarifado(requisicao.getAlmoxarifado());
             
             if(!requisicao.getDepartamento().getId().equals(0L))
             	consumidores = uDao.loadByDepartamento(requisicao.getDepartamento());            
         }
     }
 
     public String gravar() {
     	requisicao.setDepartamento(authUser.getUsuarioDepartamentoAtivo().getDepartamento());
         requisicao.setUsuario(authUser);
        requisicao.setData(new Date());
         requisicao.requerer();
         dao.save(requisicao);     
         message = "A requisição foi efetuada com sucesso. O almoxarifado irá avaliar a sua requisição.";
         return "ok";
     }
 
     public void validateGravar(ValidationErrors errors) {
         if (requisicao.getItensRequisitados().size() == 0) {
             errors.add(new Message("aviso", "Nenhum item requisitado."));
         }
 
         if (requisicao.getItensRequisitados().size() > 0) {
             for (ItemRequisicao ie : requisicao.getItensRequisitados()) {
                 if (ie.getQtdRequisitada().compareTo(new BigDecimal(0)) == 0) {
                     errors.add(new Message("aviso", "Existem itens com a quantidade requisitada igual a zero."));
                     break;
                 }
             }
         }
         
         if (errors.size() > 0) {            
             tiposSaida = tDao.listAll();          
         }       
         
     }
 
     public String avaliacaoForm() {
         this.requisicao = dao.getById(this.requisicao.getId());
         return "ok";
     }
 
     /**
      * Onde isto está sendo usado?
      */
     @Deprecated
     public String verItem() {
         ItemEstoque ie = iEDao.findByExample(item, requisicao.getAlmoxarifado());
         if (ie != null) {
             item = iDao.getById(ie.getItem().getId());
         }
         return "ok";
     }
 
     public String addItem() {
     	//Devido a problemas com o vraptor 2 o atributo quantidade deve ser inserido manualmente.
     	item = iDao.getById(item.getId());
         requisicao.addItemRequisicao(item, new BigDecimal(qtdRequisitada));
         return "ok";
     }
     
     //FIXME ver pq não está sendo validada a quantidade!
     public void validateAddItem(ValidationErrors errors) {
     	if(item.getId() == null || item.getId().equals(0L))
     		 errors.add(new Message("aviso", "Informe o item."));
     	if(qtdRequisitada.equals("0.00"))
     		errors.add(new Message("aviso", "Informe a quantidade."));
     }
     
     public String remItem() {
         requisicao.remItemRequisicao(item);
         return "ok";
     }
 
     public String listAguardando() {
         requisicoes = dao.findByStatus(TipoStatus.AGUARDANDO, authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());        
         return "ok";
     }
 
     public String listAprovadas() throws NotFoundException {        
         requisicoes = dao.findByStatus(TipoStatus.APROVADO, authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());    	
         return "ok";
     }
 
     public String listEntregues() throws NotFoundException {
     	requisicoes = dao.findByStatus(TipoStatus.ENTEGUE, authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());        
         return "ok";
     }
 
     public String avaliar() throws NotFoundException {
     	requisicao = dao.getById(requisicao.getId());          
         if (aval.equals(TipoStatus.BLOQUEADO)) {
         	requisicao.bloquear(comentario);
         	dao.update(requisicao);
             message = "A requisição nº " + requisicao.getId() + " foi bloqueada. Veja abaixo a situação desta requisição.";
             requisicao = dao.getById(requisicao.getId()); 
             return "bloqueado";
         } else {
         	//Está carregando os itensEstoque para informações sobre o estoque na hora da avaliação.
         	itensEstoque = new ArrayList<ItemEstoque>();
         	for (ItemRequisicao ir : requisicao.getItensRequisitados())
 				itensEstoque.add(iEDao.findByExample(ir.getItem(), requisicao.getAlmoxarifado()));
         	
             alert = "Avalie a quantidade de itens que podem ser entregue pelo almoxarifado para esta requisição.";
             return "autorizarForm";
         }
     }
 
     public void validateAvaliar(ValidationErrors errors) {
         if (aval.equals(TipoStatus.BLOQUEADO) && comentario.equals("")) {
             errors.add(new Message("aviso", "Para bloquear uma requisição é necessário fazer uma justificativa."));
         }
 
         if (errors.size() > 0) {
             this.requisicao = dao.getById(requisicao.getId());
         }
     }
 
     public String autorizar() {
         requisicao = dao.getById(requisicao.getId());
         requisicao.autorizar(itensEnviados, comentario);
         
         for (ItemSaida is : requisicao.getItensEnviados()) {        	
         	ItemEstoque ie = iEDao.findByExample(is.getItem(), requisicao.getAlmoxarifado());
         	is.setValorUnitario(ie.getCustoMedio());
 		}
         
         dao.save(requisicao);
         
         for (ItemSaida is : requisicao.getItensEnviados()) {        	
         	iEDao.atualizaEstoque(is.getItem(), requisicao.getAlmoxarifado());
 		}        
         
         message = "A requisição nº " + requisicao.getId() + " foi autorizada.";
         return "ok";    	
     }
 
     public void validateAutorizar(ValidationErrors errors) {
     	//Insere manualmente os valores das quantidades de saída devido a limitações do vraptor 2    	
     	for(int i = 0; i < itensEnviados.size(); i++) {
     		String qtd = iEQtd.get(i).replace(".", "").replace(",", ".");    		
     		itensEnviados.get(i).setQuantidade(new BigDecimal(qtd));
     	}   	
  	
         for (ItemSaida is : itensEnviados) {        	
             ItemEstoque ie = iEDao.findByExample(is.getItem(), requisicao.getAlmoxarifado());
             if(ie == null) {
             	Item i = iDao.getById(is.getItem().getId());
             	errors.add(new Message("aviso", "O item: " + i.getNome() + " não está no estoque."));            	
             } else if (ie.getEstoque().compareTo(is.getQuantidade()) < 0) {
                 errors.add(new Message("aviso", "Não existem itens suficientes para o estoque. A quantidade para o item: " + ie.getItem().getNome() + " é: " + ie.getEstoque()));
             }
         }
         
         //Libera o conteúdo
         iEQtd = null;
     }
 
     public String entregarForm() {    	
         requisicao = dao.getById(requisicao.getId());        
         return "ok";
     }
 
     public String entregar() {
         requisicao = dao.getById(requisicao.getId());
         requisicao.entregar();
         dao.update(requisicao);
         message = "O material foi entregue pelo almoxarifado. A requisição foi finalizada.";
         return "ok";
     }
 
     public String acompanhar() {        
         requisicoes = dao.listarParaAcompanhar(authUser);
         return "ok";
     }
 
     public String acompanharDetalhes() {
         requisicao = dao.getById(requisicao.getId());
         return "ok";
     }
     
     public String loadConsumidores(Departamento d) {
     	consumidores = uDao.loadByDepartamento(d);
     	return "ok";
     }
     
     public String transferir() {
     	requisicao = dao.getById(requisicao.getId());
     	NotaEntrada n = requisicao.transferir();
     	
     	for (ItemEntrada ie : n.getItensEntrada()) {
 			//Se não achar um item estoque deste item pra este almoxarifado adiciona o item estoque.
 			ItemEstoque itemEstoque = iEDao.findByExample(ie.getItem(), n.getAlmoxarifado());    			
 			if(itemEstoque == null) {   				
 				iEDao.add(new ItemEstoque(ie.getItem(), n.getAlmoxarifado(), new BigDecimal("0")));
 				itemEstoque = iEDao.findByExample(ie.getItem(), n.getAlmoxarifado());
 			}
 			
 			//Atualiza preços unitários dos itens.
 			//Pega toda as notas de entrada que possuem o item naquele almoxarifado.
 			NotaEntrada nExample = new NotaEntrada();
 			nExample.addItemEntrada(new ItemEntrada(ie.getItem()));
 			nExample.setAlmoxarifado(n.getAlmoxarifado());
 			nExample.setData(null);
 			nExample.setTipoEntrada(null);
 
 			itemEstoque.atualizaEstoque(dao.findAprovadasByItem(n.getAlmoxarifado(), ie.getItem()), nDao.list(nExample));		
 		}
     	
     	nDao.add(n);
     	dao.update(requisicao);
     	requisicao = dao.getById(requisicao.getId());
     	message = "Transferência realizada com sucesso!";
     	return "ok";
     }
     
     public String cancelarForm() {
     	return "ok";
     }
     
     public String cancelarLoad() {
     	requisicao = dao.getById(requisicao.getId());    	
     	return "ok";
     }
     
     //Cancela uma requisição já aprovada pelo almoxarife.
     public String cancelar() {    	
     	requisicao = dao.getById(requisicao.getId());
     	dao.remove(requisicao);
     	atualizaEstoque(requisicao.cancelar(), requisicao.getAlmoxarifado());
     	message = "Requisição cancelada!";
     	return "ok";  	
     }
     
     public void validateCancelar(ValidationErrors errors) {
     	requisicao = dao.getById(requisicao.getId());
     	if(
     			requisicao.getStatusAtual().getTipoStatus().getId().equals(TipoStatus.AGUARDANDO) ||
     			requisicao.getStatusAtual().getTipoStatus().getId().equals(TipoStatus.BLOQUEADO)
     	)
     		errors.add(new Message("aviso", "Esta nota não pode ser cancelada devido a seu status."));
     }
     
     private void atualizaEstoque(Collection<Item> itens, Almoxarifado a) {
     	for (Item item : itens) {
 			ItemEstoque itemEstoque = iEDao.findByExample(item, a);
 			
 			//Atualiza preços unitários dos itens.
 			//Pega toda as notas de entrada que possuem o item naquele almoxarifado.
 			NotaEntrada nExample = new NotaEntrada();
 			nExample.addItemEntrada(new ItemEntrada(item));
 			nExample.setAlmoxarifado(a);
 			nExample.setData(null);
 			nExample.setTipoEntrada(null);			
 			itemEstoque.atualizaEstoque(dao.findAprovadasByItem(a, item), nDao.list(nExample));
 		}    	
     }
     
 }
