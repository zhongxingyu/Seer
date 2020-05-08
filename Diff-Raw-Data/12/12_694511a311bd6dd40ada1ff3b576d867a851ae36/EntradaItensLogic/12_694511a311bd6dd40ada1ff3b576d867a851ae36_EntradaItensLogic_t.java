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
 import java.math.MathContext;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
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
 import br.edu.uncisal.almoxarifado.dao.FornecedorDao;
 import br.edu.uncisal.almoxarifado.dao.ItemDao;
 import br.edu.uncisal.almoxarifado.dao.ItemEstoqueDao;
 import br.edu.uncisal.almoxarifado.dao.NotaEntradaDao;
 import br.edu.uncisal.almoxarifado.dao.RequisicaoDao;
 import br.edu.uncisal.almoxarifado.dao.TipoEntradaDao;
 import br.edu.uncisal.almoxarifado.dao.UsuarioDao;
 import br.edu.uncisal.almoxarifado.model.Almoxarifado;
 import br.edu.uncisal.almoxarifado.model.Fornecedor;
 import br.edu.uncisal.almoxarifado.model.Grupo;
 import br.edu.uncisal.almoxarifado.model.Item;
 import br.edu.uncisal.almoxarifado.model.ItemEntrada;
 import br.edu.uncisal.almoxarifado.model.ItemEstoque;
 import br.edu.uncisal.almoxarifado.model.NotaEntrada;
 import br.edu.uncisal.almoxarifado.model.Perfil;
 import br.edu.uncisal.almoxarifado.model.TipoEntrada;
 import br.edu.uncisal.almoxarifado.model.TipoMedida;
 import br.edu.uncisal.almoxarifado.model.Usuario;
 import br.edu.uncisal.almoxarifado.util.EstoqueException;
 
 @SuppressWarnings("unused")
 @Component("entradaItens")
 @InterceptedBy({DaoInterceptor.class, AutorizadorInterceptor.class})
 public class EntradaItensLogic {
 	
 	@Parameter(create = true)
     @Out(scope = ScopeType.SESSION)
     @In(scope = ScopeType.SESSION, required = false)
     private NotaEntrada notaEntrada;    
     @Parameter(create = true)
     @Out
     private ItemEntrada itemEntrada;
     @Parameter
     @In(scope = ScopeType.SESSION)
     @Out(scope = ScopeType.SESSION)
     private Usuario authUser;
     @Parameter
     @Out
     private Item item = new Item();
     @Parameter
     @Out
     private Almoxarifado almoxarifado = new Almoxarifado();
     @Parameter
     private Long almoxId;
     @Parameter
     private String q;
     @Out
     @Parameter
     private String criteria;    
     /**
      * Quantidade do ItemEntrada
      */
     @Parameter
     private String qtd;
     
     /**
      * Valor do ItemEntrada
      */
     @Parameter
     private String vl;
     
     @Parameter
     private Integer index;
     
     /**
      * Itens entrada a serem atualizados
      */
     @Out(scope = ScopeType.SESSION)
     @In(scope = ScopeType.SESSION, required = false)
     private Collection<Item> itensAtualizaveis; 
     
     @Out
     private List<TipoMedida> tiposMedida;
     @Out
     private Collection<TipoEntrada> tiposEntrada;
     @Out
     private List<Grupo> grupos;
     @Out
     private List<NotaEntrada> notaEntradas;
     @Out
     private Collection<Almoxarifado> almoxarifados;
     @Out
     private List<Fornecedor> fornecedores;
     @Out
     private String msgErro;
     @Out
     private String message;
     @Out
     private List<Item> itens;
         
     /**
      * Se o usuário não quiser calcular o valor unitário ele pode selecionar o 
      * checkbox valor total que o sistema faz o cálculo dos valores unitários automaticamente.
      */
     @Parameter
     private Boolean valorTotal;
     
     private NotaEntradaDao dao;
     private ItemDao iDao;
     private FornecedorDao fDao;
     private TipoEntradaDao tipoEntradaDao;
     private ItemEstoqueDao itemEstoqueDao;
     private RequisicaoDao requisicaoDao;
     private AlmoxarifadoDao almoxarifadoDao;
     private UsuarioDao usuarioDao;
     
     private DaoFactory daoFactory;
       
     public EntradaItensLogic(DaoFactory daoFactory, Usuario authUser) {
     	this.daoFactory = daoFactory;
     	
     	dao = daoFactory.getNotaEntradaDao();
     	iDao = daoFactory.getItemDao();
     	fDao = daoFactory.getFornecedorDao();
     	tipoEntradaDao = daoFactory.getTipoEntradaDao();
     	itemEstoqueDao = daoFactory.getItemEstoqueDao();
     	requisicaoDao = daoFactory.getRequisicaoDao();
     	almoxarifadoDao = daoFactory.getAlmoxarifadoDao();
     	usuarioDao = daoFactory.getUsuarioDao();
 
         if (authUser.getUsuarioDepartamentoAtivo().getPerfil().getId().equals(Perfil.ADMINISTRADOR_GERAL)) {
             almoxarifados = almoxarifadoDao.listAll();
         } else if (authUser.getUsuarioDepartamentoAtivo().getPerfil().getId().equals(Perfil.ADMINISTRADOR_LOCAL)) {
             almoxarifados = almoxarifadoDao.listByOrgao(authUser.getUsuarioDepartamentoAtivo().getDepartamento().getUnidade().getOrgao());
         } else {
         	almoxarifados = new ArrayList<Almoxarifado>();
             almoxarifados.add(authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());
         }
     }
 
     public String formulario() {
     	notaEntrada = new NotaEntrada();
     	fornecedores = fDao.listAll();        
         tiposEntrada = tipoEntradaDao.listAll();
         return "ok";
     }
 
     public String filtro() {
         formulario();
         List<Almoxarifado> as = (List<Almoxarifado>) almoxarifados;
         notaEntrada.setAlmoxarifado(as.get(0));
         return "ok";
     }
 
     public String gravar() throws EstoqueException {
     	try {
     		if(itensAtualizaveis == null)
     			itensAtualizaveis = new HashSet<Item>();
     		
     		if(notaEntrada.getId() == null)
     			itensAtualizaveis = notaEntrada.efetuarEntrada();
     		else
     			itensAtualizaveis.addAll(notaEntrada.efetuarEntrada());
     		
     		dao.save(notaEntrada);
     		
     		atualizaEstoque();    		
 
             message = "Nota de Entrada foi gravada com sucesso.";
             notaEntrada = new NotaEntrada();
             return "ok";
     	} catch (EstoqueException e) {
     		fornecedores = fDao.listAll();
             tiposEntrada = tipoEntradaDao.listAll();
  			message = e.getMessage();
  			daoFactory.rollback();
  			return "invalid";			
 		}
     }
     
     //FIXME Isto aqui vai para a validação
 	//ie = itemEstoqueDao.findByExample(i, notaEntrada.getAlmoxarifado());
 	//if(ie.getEstoque() == null || ie.getEstoque().compareTo(new BigDecimal("0")) < 0)
 	//	throw new EstoqueException("Itens insuficientes para o estoque do item: "+ i.getNome());
 	private void atualizaEstoque() throws EstoqueException {
 		for (Item i : itensAtualizaveis) {
 			//Se não achar um item estoque deste item pra este almoxarifado adiciona o item estoque.
 			ItemEstoque ie = itemEstoqueDao.findByExample(i, notaEntrada.getAlmoxarifado());    			
 			if(ie == null) {   				
 				itemEstoqueDao.add(new ItemEstoque(i, notaEntrada.getAlmoxarifado(), new BigDecimal("0")));
 				ie = itemEstoqueDao.findByExample(i, notaEntrada.getAlmoxarifado());
 			}
 			
 			//Atualiza preços unitários dos itens.
 			//Pega toda as notas de entrada que possuem o item naquele almoxarifado.
 			NotaEntrada nExample = new NotaEntrada();
 			nExample.addItemEntrada(new ItemEntrada(i));
 			nExample.setAlmoxarifado(notaEntrada.getAlmoxarifado());
 			nExample.setData(null);
 			nExample.setTipoEntrada(null);
 
 			ie.atualizaEstoque(requisicaoDao.findAprovadasByItem(notaEntrada.getAlmoxarifado(), i), dao.list(nExample));
 		}
 	}
 
     public void validateGravar(ValidationErrors errors) {    	
         if (notaEntrada.getData() == null || notaEntrada.getData().equals("")) {
             errors.add(new Message("aviso", "A data de entrada não foi definida."));
         }
 
         if ((notaEntrada.getNumero() == null || notaEntrada.getNumero().equals("")) && (notaEntrada.getObservacao() == null || notaEntrada.getObservacao().equals(""))) {
             errors.add(new Message("aviso", "Um número para a nota de entrada não foi definido. Favor informar o número ou uma observação."));
         }
         
         if (notaEntrada.getItensEntrada() == null || notaEntrada.getItensEntrada().size() == 0) {
             errors.add(new Message("aviso", "Nenhum item foi selecionado para a nota."));
         } else {
             for (ItemEntrada ie : notaEntrada.getItensEntrada()) {
                 if (ie.getQuantidade() == null || ie.getQuantidade().equals(0.0)) {
                     errors.add(new Message("aviso", "A quantidade para o item: " + ie.getItem().getNome() + " não foi definida"));
                 }
 
                 if (ie.getValorUnitario().compareTo(new BigDecimal(0)) == 0 && !notaEntrada.getTipoEntrada().getId().equals(new Long(0))) {
                     errors.add(new Message("aviso", "O valor unitário para o item: " + ie.getItem().getNome() + " não foi definida"));
                 }
             }
         }
         if (errors.size() > 0) {
             fornecedores = fDao.listAll();
             tiposEntrada = tipoEntradaDao.listAll();
         }
     }
 
     /** Método que carrega uma lista de nota de entrada para o grid ajax da tela */
     public String buscar() {
         try {
             notaEntradas = dao.list(notaEntrada);
         } catch (Exception e) {
             msgErro = "Não foi possível exibir as notas de entrada, devido ao seguinte problema:<br>" + e.getMessage();
         }
         return "ok";
     }
 
     /** Método que carrega uma lista de nota de entrada para o grid da tela */
     public String listAll() {
         try {
             notaEntradas = dao.listByAlmoxarifado(authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());
         } catch (Exception e) {
             msgErro = "Não foi possível exibir as nostas de entrada devido ao seguinte problema:<br>" + e.getMessage();
         }
         return "ok";
     }
 
     /** Método que remove o objeto nota de entrada*/
     public String remove() {
         try {
         	Collection<Item> itens = notaEntrada.remove();
         	dao.remove(notaEntrada);        	    
             
     		//Atualiza os itensEstoque.
             itensAtualizaveis = new HashSet<Item>();
             itensAtualizaveis.addAll(itens);
     		for (Item i : itens) {    			
     			atualizaEstoque();
 			}
     		notaEntrada = null;
             message = "Nota de entrada foi removida com sucesso.";
         } catch (Exception e) {
             e.printStackTrace();
             daoFactory.rollback();
             msgErro = "Não foi possível remover nota de entrada devido ao seguinte problema:<br>" + e.getMessage();
         }
 
         return "ok";
     }
 
     public void validateRemove(ValidationErrors errors) {
         notaEntrada = dao.getById(notaEntrada.getId());
         for (ItemEntrada ie : notaEntrada.getItensEntrada()) {
             if (ie.getPatrimonios().size() > 0) {
                 errors.add(new Message("aviso", "Esta nova não poderá ser removida, pois o item: " + ie.getItem().getNome() + " está tombado."));
             }
         }
     }
 
     /** Método que carrega o objeto nota de entrada de acordo com o id da nota de entrada para tela de alteração */
     public String get() {
     	notaEntrada = dao.getById(notaEntrada.getId());
         fornecedores = fDao.listAll();
         tiposEntrada = tipoEntradaDao.listAll();
         
         itensAtualizaveis = new HashSet<Item>();        
         for (ItemEntrada ie : notaEntrada.getItensEntrada()) {
 			itensAtualizaveis.add(ie.getItem());
 		}        
         return "ok";
     }
 
     public String addItem() {
     	if(itemEntrada.getItem() == null || itemEntrada.getItem().getId() == null) {        	
         	msgErro = "Digite o código ou nome do item.";
         	return "invalid";
         }
     	
     	//Carregando manualmente os valores dos itens e suas respectivas quantidades, pois o vraptor 2 não dá suporte a BigDecimal.
     	itemEntrada.setValorUnitario(new BigDecimal(vl));
     	itemEntrada.setQuantidade(new BigDecimal(qtd));
     	
     	if(valorTotal != null && valorTotal) {
     		itemEntrada.setValorUnitario(itemEntrada.getValorUnitario().divide(itemEntrada.getQuantidade(), MathContext.DECIMAL64));
     	}
     	
         itemEntrada.setItem(iDao.getById(itemEntrada.getItem().getId()));
         
         for (ItemEntrada ie: notaEntrada.getItensEntrada()) {
 			if(itemEntrada.equals(ie)) {
 				msgErro = "Este item já foi inserido nesta nota de entrada";
 				return "invalid";
 			}				
         }
         
         if(itemEntrada.getValorUnitario().equals(new BigDecimal("0.00"))) {        	
         	msgErro = "O item não pode ter o valor zero";
         	return "invalid";
         }
         
         if(itemEntrada.getQuantidade().equals(new BigDecimal("0.00"))) {
         	msgErro = "O item não pode ter a quantidade zero";
         	return "invalid";
         }
         
         notaEntrada.addItemEntrada(itemEntrada); 
         return "ok";
     }
     
     /** Método usado pelo AJAX para remover itemEntrada na notaEntrada  */
     public String remItem() {                
         ItemEntrada ie = notaEntrada.getItensEntrada().get(index);
         notaEntrada.remItemEntrada(ie);
         return "ok";
     }
     
     /** Retorna a lista de Itens conforme parâmetro passado da busca */
     public String buscarNotas() {
     	notaEntradas = dao.buscar(criteria, authUser.getUsuarioDepartamentoAtivo().getAlmoxarifado());
         return "ok";
     }
     
     //Migração para o sistema 1.5
     public String migrar() {
     	Almoxarifado almoxarifado = almoxarifadoDao.getById(new Long(index));   
     	
     		/*Almoxarifado at = new Almoxarifado(6l);
     		Item it = new Item(3622L);
     	
     		Set<Requisicao> rs = requisicaoDao.findAprovadasByItem(at, it);
     		Set<NotaEntrada> ns = dao.list(at, it);
     		
     		ItemEstoque iet = new ItemEstoque();
     		iet.setItem(it);
     		iet.atualizaEstoque(rs, ns);
     		//BigDecimal i = new BigDecimal("0");
     		//for (Requisicao r : rs) {
 			//	i.add(r.)
 			//}
     		return "ok";*/
     		
 			List<ItemEstoque> itensEstoque = itemEstoqueDao.listByAlmoxarifado(almoxarifado);
 			for (ItemEstoque itemEstoque : itensEstoque) {
 				//Atualiza preços unitários dos itens.
 				//Pega toda as notas de entrada que possuem o item naquele almoxarifado.
 				NotaEntrada nExample = new NotaEntrada();
 				nExample.addItemEntrada(new ItemEntrada(itemEstoque.getItem()));
 				nExample.setAlmoxarifado(almoxarifado);
 				nExample.setData(null);
 				nExample.setTipoEntrada(null);
 
 				itemEstoque.atualizaEstoque(requisicaoDao.findAprovadasByItem(almoxarifado, itemEstoque.getItem()), dao.list(almoxarifado, itemEstoque.getItem()));			
 			}
     	
     	return "ok";
     }
 }
