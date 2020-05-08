 package base;
 
 import java.util.Iterator;
 
 public class Consumer implements Runnable {
 
 	private EshopV4 eshop;
 	private int total;
 	
 	public Consumer(EshopV4 eshop, int total) {
 		this.eshop = eshop;
 		this.total = total;
 	}
 
 	@Override
 	public void run() {
 		try {
 			int i = 0;
 			while (true) {
 				
 				if(i == total) {
 					break;
 				}
 				
 				if (eshop.getShoppingQueue().isEmpty() == true) {
 					System.out.println("Consumer sleeping 1000 queue is empty!");
 					Thread.sleep(1000);
 				} else {
 					i++;
 					Shopping shopping = eshop.getShoppingQueue().poll();
 
 					Iterator<String> productsIterator = shopping.getProducts()
 							.iterator();
 
 					while (productsIterator.hasNext()) {
 						String isbn = productsIterator.next();
 						Product product = eshop.findProduct(isbn);
 						if (product != null) {
 							eshop.getCart().addProduct(product, 1);
 						}
 					}
 
 					eshop.createOrder(shopping.getName(),
 							shopping.getSurname(), shopping.getCountry(),
 							shopping.getCity(), shopping.getAddress(),
 							shopping.getPostCode());
 					
					eshop.getDesign().updateOrdersOrdersTableRows();
 					System.out.println("Consumer make order!");
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println("Exception: " + e.getMessage());
 		}
 
 	}
 
 }
