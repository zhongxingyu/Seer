 package supermecado;

import supermercado.anotacao.Validacao;
 					  
 public class Produto {
	
	@Validacao(requerido=true)
 	private String nome;
	
 	private float qtde;
 	private float valorUnitario;
 	private String localizacao;
	
	@Validacao(requerido=true)	
	private UnidadeMedida unidadeMedida; //agrega��o
	private TipoProduto tipoProduto; //agrega��o
 	
 	public Produto() {
 		super();
 		this.nome = "";
 		this.qtde = 0;
 		this.valorUnitario = 0;
 		this.localizacao = "";
 		this.unidadeMedida = null;
 		this.tipoProduto = null;
 	}
 	
 	public Produto(String nome, float qtde, float valorUnitario,
 			String localizacao, UnidadeMedida unidadeMedida,
 			TipoProduto tipoProduto) {
 		super();
 		this.nome = nome;
 		this.qtde = qtde;
 		this.valorUnitario = valorUnitario;
 		this.localizacao = localizacao;
 		this.unidadeMedida = unidadeMedida;
 		this.tipoProduto = tipoProduto;
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public float getQtde() {
 		return qtde;
 	}
 
 	public float getValorUnitario() {
 		return valorUnitario;
 	}
 
 	public String getLocalizacao() {
 		return localizacao;
 	}
 
 	public UnidadeMedida getUnidadeMedida() {
 		return unidadeMedida;
 	}
 
 	public TipoProduto getTipoProduto() {
 		return tipoProduto;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public void setQtde(float qtde) {
 		this.qtde = qtde;
 	}
 
 	public void setValorUnitario(float valorUnitario) {
 		this.valorUnitario = valorUnitario;
 	}
 
 	public void setLocalizacao(String localizacao) {
 		this.localizacao = localizacao;
 	}
 
 	public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
 		this.unidadeMedida = unidadeMedida;
 	}
 
 	public void setTipoProduto(TipoProduto tipoProduto) {
 		this.tipoProduto = tipoProduto;
 	}
 	
 	public String toString() {
 		return nome + " (" + qtde + " " + unidadeMedida + ")" + " - R$ " + valorUnitario + "\n"; 
 	}
 	
	public static void main(String[] args){
		Produto p = new Produto();
		p.setUnidadeMedida(BancoDeDados.getUnidadeMedidaBySigla("KG"));
		p.setTipoProduto(new TipoProduto("Tipo X"));
		try {
			Util.valida(p);
		} catch (Exception e) {
			Util.showError(e.getMessage());
		}
	}
	
 }
 
