 class SistemaA
 {
 	private Produto produto;
 
 	public SistemaA()
 	{
		this.produto = new Produto();
 		this.setProduto();
 	}
 
 	public Produto getProduto()
 	{
 		return this.produto;
 	}
 
 	public void setProduto()
 	{
 		this.produto.setId(1);
 		this.produto.setDescricao("Produto do Sistema A");
 		this.produto.setPreco(10.00f);
 	}
 }
