 package br.com.mendes.view;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 
 import br.com.mendes.model.Cliente;
 import br.com.mendes.model.Item;
 import br.com.mendes.model.ItemPedido;
 import br.com.mendes.model.Pedido;
 import br.com.mendes.model.Produto;
 import br.com.mendes.model.Servico;
 import br.com.mendes.model.StatusPedido;
 import br.com.mendes.model.TipoItem;
 import br.com.mendes.service.ClienteService;
 import br.com.mendes.service.ItemService;
 import br.com.mendes.service.PedidoService;
 import br.com.mendes.service.ProdutoService;
 import br.com.mendes.service.ServicoService;
 import br.com.mendes.utils.MBUtil;
 
 @ManagedBean(name = "pedidoMB")
 @ViewScoped
 public class PedidoMB implements Serializable {
 
 	private static final long serialVersionUID = -2031182511996672906L;
 
 	@ManagedProperty(name = "pedidoService", value = "#{pedidoService}")
 	private PedidoService pedidoService;
 
 	@ManagedProperty(name = "clienteService", value = "#{clienteService}")
 	private ClienteService clienteService;
 
 	@ManagedProperty(name = "produtoService", value = "#{produtoService}")
 	private ProdutoService produtoService;
 
 	@ManagedProperty(name = "servicoService", value = "#{servicoService}")
 	private ServicoService servicoService;
 
 	@ManagedProperty(name = "itemService", value = "#{itemService}")
 	private ItemService itemService;
 
 	private Long codCliente;
 	private Long codItem;
 
 	private TipoItem tipoItem;
 
 	private Pedido selectedPedido;
 
 	private Integer quantidade;
 
 	private Double total;
 
 	private List<Pedido> pedidos;
 	private List<Cliente> clientes;
 	private List<Produto> produtos;
 	private List<Servico> servicos;
 	private List<TipoItem> tiposItem;
 	private List<Item> itens;
 	private List<ItemPedido> itensPedido;
 
 	private PedidosLazyDataModel pedidosLazyDataModel;
 
 	@PostConstruct
 	public void iniciar() {
 
 		this.pedidos = this.pedidoService.obterTodosPedidos();
 		this.clientes = this.clienteService.obterTodosCliente();
 		this.produtos = this.produtoService.obterTodosProduto();
 		this.servicos = this.servicoService.obterTodosServicos();
 		this.tiposItem = Arrays.asList(TipoItem.values());
 
 		resetDados();
 	}
 
 	public void loadItensDialog() {
 		this.selectedPedido = this.pedidoService.obterPedidoPorCod(this.selectedPedido.getCod());
 	}
 
 	public void limparLista() {
 		this.itensPedido = new ArrayList<ItemPedido>();
 		this.total = 0D;
 	}
 
 	public void pesquisa() {
 		this.pedidosLazyDataModel.setRowIndex(-1);
 	}
 
 	public PedidoMB() {
 
 		this.tiposItem = Arrays.asList(TipoItem.values());
 	}
 
 	public void resetDados() {
 
 		this.codCliente = null;
 		this.codItem = null;
 		this.quantidade = 1;
 		this.itensPedido = new ArrayList<ItemPedido>();
 		this.total = 0.0;
 
		this.pedidosLazyDataModel = new PedidosLazyDataModel(this.pedidoService);
		this.tipoItem = TipoItem.PRODUTO;
		this.itens = this.itemService.buscarTodos(this.tipoItem);

 	}
 
 	public void adicionarProduto() {
 
 		if (this.itensPedido == null) {
 			this.itensPedido = new ArrayList<ItemPedido>();
 		}
 
 		if (this.codItem == null) {
 			MBUtil.addWarn("Item não foi selecionado.");
 			return;
 		}
 
 		boolean encontrado = false;
 
 		for (ItemPedido ip : this.itensPedido) {
 
 			if (ip.getItem().getCod().equals(this.codItem)) {
 				ip.setQuantidade(this.quantidade);
 				encontrado = true;
 				MBUtil.addWarn("Produto já estava na lista, sua quantidade foi alterada.");
 			}
 
 		}
 
 		if (!encontrado) {
 
 			Item item = this.itemService.buscarPorCodigo(this.codItem);
 
 			ItemPedido itemPedido = new ItemPedido();
 			itemPedido.setItem(item);
 			itemPedido.setQuantidade(this.quantidade);
 			this.itensPedido.add(itemPedido);
 		}
 
 		calcularTotal();
 	}
 
 	public Double calcularTotal() {
 
 		this.total = 0.0;
 
 		for (ItemPedido ip : this.itensPedido) {
 			this.total += ip.getItem().getPrecoVenda() * ip.getQuantidade();
 		}
 
 		return this.total;
 	}
 
 	public void escolherTipoItem() {
 		this.itens = this.itemService.buscarTodos(this.tipoItem);
 	}
 
 	public void atualizarDados() {
 		this.pedidos = this.pedidoService.obterPedidoPorCliente(this.codCliente);
 	}
 
 	public String iniciarEdicao(Long codCliente) {
 
 		this.codCliente = codCliente;
 
 		atualizarDados();
 
 		return "/paginas/consultaPedidos.xhtml";
 	}
 
 	public void salvarPedido() {
 
 		Pedido pedido = new Pedido();
 		pedido.setStatus(StatusPedido.PAGO);
 		pedido.setDataEmissao(new Date());
 		pedido.setCliente(new Cliente(this.codCliente));
 		pedido.setValorTotal(calcularTotal());
 
 		if (!this.itensPedido.isEmpty() && this.itensPedido != null) {
 			this.pedidoService.criarPedido(pedido);
 
 			for (ItemPedido ip : this.itensPedido) {
 				ip.setPedido(pedido);
 				this.pedidoService.criarAlterarItemPedido(ip);
 			}
 
 			MBUtil.addInfo("Cadastrado com sucesso.");
 		} else {
 			MBUtil.addError("Lista de pedidos está vazia!");
 		}
 
 		resetDados();
 	}
 
 	public List<Pedido> getPedidos() {
 		return this.pedidos;
 	}
 
 	public void setOrcamentos(List<Pedido> pedidos) {
 		this.pedidos = pedidos;
 	}
 
 	public List<Cliente> getClientes() {
 		return this.clientes;
 	}
 
 	public void setClientes(List<Cliente> clientes) {
 		this.clientes = clientes;
 	}
 
 	public List<Produto> getProdutos() {
 		return this.produtos;
 	}
 
 	public void setProdutos(List<Produto> produtos) {
 		this.produtos = produtos;
 	}
 
 	public List<Servico> getServicos() {
 		return this.servicos;
 	}
 
 	public void setServicos(List<Servico> servicos) {
 		this.servicos = servicos;
 	}
 
 	public TipoItem getTipoItem() {
 		return this.tipoItem;
 	}
 
 	public void setTipoItem(TipoItem tipoItem) {
 		this.tipoItem = tipoItem;
 	}
 
 	public List<TipoItem> getTiposItem() {
 		return this.tiposItem;
 	}
 
 	public void setTiposItem(List<TipoItem> tiposItem) {
 		this.tiposItem = tiposItem;
 	}
 
 	public List<Item> getItens() {
 		return this.itens;
 	}
 
 	public void setItens(List<Item> itens) {
 		this.itens = itens;
 	}
 
 	public List<ItemPedido> getItensPedido() {
 		return this.itensPedido;
 	}
 
 	public void setItensPedido(List<ItemPedido> itensPedido) {
 		this.itensPedido = itensPedido;
 	}
 
 	public Double getTotal() {
 		return this.total;
 	}
 
 	public void setTotal(Double total) {
 		this.total = total;
 	}
 
 	public Integer getQuantidade() {
 		return this.quantidade;
 	}
 
 	public void setQuantidade(Integer quantidade) {
 		this.quantidade = quantidade;
 	}
 
 	public Long getCodItem() {
 		return this.codItem;
 	}
 
 	public void setCodItem(Long codItem) {
 		this.codItem = codItem;
 	}
 
 	public Long getCodCliente() {
 		return this.codCliente;
 	}
 
 	public void setCodCliente(Long codCliente) {
 		this.codCliente = codCliente;
 	}
 
 	public PedidoService getPedidoService() {
 		return this.pedidoService;
 	}
 
 	public void setPedidoService(PedidoService pedidoService) {
 		this.pedidoService = pedidoService;
 	}
 
 	public ClienteService getClienteService() {
 		return this.clienteService;
 	}
 
 	public void setClienteService(ClienteService clienteService) {
 		this.clienteService = clienteService;
 	}
 
 	public ProdutoService getProdutoService() {
 		return this.produtoService;
 	}
 
 	public void setProdutoService(ProdutoService produtoService) {
 		this.produtoService = produtoService;
 	}
 
 	public Pedido getSelectedPedido() {
 		return this.selectedPedido;
 	}
 
 	public void setSelectedPedido(Pedido selectedPedido) {
 		this.selectedPedido = selectedPedido;
 	}
 
 	public ServicoService getServicoService() {
 		return this.servicoService;
 	}
 
 	public void setServicoService(ServicoService servicoService) {
 		this.servicoService = servicoService;
 	}
 
 	public ItemService getItemService() {
 		return this.itemService;
 	}
 
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 
 	public void setPedidos(List<Pedido> pedidos) {
 		this.pedidos = pedidos;
 	}
 
 	public PedidosLazyDataModel getPedidosLazyDataModel() {
 		return this.pedidosLazyDataModel;
 	}
 
 	public void setPedidosLazyDataModel(PedidosLazyDataModel pedidosLazyDataModel) {
 		this.pedidosLazyDataModel = pedidosLazyDataModel;
 	}
 
 }
