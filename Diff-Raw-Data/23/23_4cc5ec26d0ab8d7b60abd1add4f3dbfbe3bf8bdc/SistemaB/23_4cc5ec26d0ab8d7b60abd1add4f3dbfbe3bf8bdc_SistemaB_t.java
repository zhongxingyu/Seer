 class SistemaB
 {
 	private Produto produto;
 
 	public SistemaB()
 	{
		this.produto = new Produto();
 		this.setProduto();
 	}
 
 	public Produto readProduto()
 	{
 		return this.produto;
 	}
 
 	public void setProduto()
 	{
 		this.produto.setId(2);
 		this.produto.setDescricao("Produto do Sistema B");
 		this.produto.setPreco(20.00f);
 	}
 }
