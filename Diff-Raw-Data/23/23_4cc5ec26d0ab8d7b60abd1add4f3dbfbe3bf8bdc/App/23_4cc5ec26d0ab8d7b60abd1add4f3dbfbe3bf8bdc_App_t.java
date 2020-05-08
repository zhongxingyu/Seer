 class App
 {
 	public static void main(String args[])
 	{
 		ProductManager pm = new ProductManager();
		LeitorProdutosAdapter adapter;
 
		adapter = new AdapterA();
		pm.addProduto(adapter.getNewProduct());

		adapter = new AdapterB();
		pm.addProduto(adapter.getNewProduct());
 
 		for(Produto p: pm.getListaProdutos()) {
 			System.out.println(p.getDescricao());
 		}
 	}
 }
