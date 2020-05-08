 class App
 {
 	public static void main(String args[])
 	{
 		ProductManager pm = new ProductManager();
		AdapterA aa = new AdapterA();
		AdapterB ab = new AdapterB();
 
		pm.addProduto(aa.getNewProduct());
		pm.addProduto(ab.getNewProduct());
 
 		for(Produto p: pm.getListaProdutos()) {
 			System.out.println(p.getDescricao());
 		}
 	}
 }
