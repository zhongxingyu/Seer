 package controllers;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import models.BaseProduct;
 import models.Coop;
 import models.Order;
 import models.Product;
 import models.Member;
 import models.ProductOrder;
 import models.Sale;
 import models.User;
 
 import play.Logger;
 import play.modules.gae.GAE;
 import play.mvc.Before;
 
 import siena.Query;
 
 public class Sales extends ConnectedController {
 
 	public static void details(Long id) {
 		Sale sale = Sale.all().getByKey(id);
 		Order order = Order.all().filter("member", getMember())
 				.filter("sale", sale).get();
 		render(sale, order);
 	}
 
 	public static void edit(Long id) {
 		Sale sale = Sale.all().getByKey(id);
 		Order order = Order.all().filter("member", getMember())
 				.filter("sale", sale).get();
		Query<ProductOrder> productOrders = null;
		if (order != null) {
			productOrders = order.products;
		}
 		Map<Product, String> productTypes = new HashMap<Product, String>();
 		Map<Product, Float> productQuantities = new HashMap<Product, Float>();
 		for (Product product : sale.products.fetch()) {
 			BaseProduct baseProduct = BaseProduct.all().getByKey(product.baseProduct.id);
 			productTypes.put(product, baseProduct.quantityType);
			ProductOrder productOrder = null;
			if (productOrders != null) {
				productOrder = productOrders.filter("product", product).get();
			}
 			productQuantities.put(product, (productOrder == null ? 0f : productOrder.quantity));
 		}
 		render(sale, order, productTypes, productQuantities);
 	}
 
 	public static void save(Long id) {
 		Sale sale = Sale.all().getByKey(id);
 		Member member = getMember();
 		Order order = getOrder(sale, member);
 		List<Product> products = sale.products.fetch();
 		Query<ProductOrder> productOrders = order.products;
 		for (Product product : products) {
 			String key = "quantity-" + product.id;
 			float quantity = Float.parseFloat(params.get(key)); // TODO break this to pieces
 			ProductOrder productOrder = productOrders.filter("product", product).get();
 			if (quantity != 0f) {
 				Logger.info("Ordered quantity " + quantity + " of product " + product);
 				if (productOrder == null) {
 					productOrder = new ProductOrder(product, member, quantity, order);
 				} else if (productOrder.quantity != quantity) {
 					Logger.info("Updating amount of " + product);
 					productOrder.quantity = quantity;
 				}
 				productOrder.save();
 			} else { // zero quantity is ordered - remove order if exists
 				Logger.info("Ordered none of product " + product);
 				if (productOrder != null) {
 					Logger.info("deleting order of product " + product);
 					productOrder.delete();
 				}
 			}
 		}
 		details(id);
 	}
 
 	private static Order getOrder(Sale sale, Member member) {
 		Order order = Order.all().filter("member", member)
 				.filter("sale", sale).get();
 		if (order == null) {
 			order = new Order(new Date(), member, sale);
 		}
 		return order;
 	}
 	
 	public static void total(Long id) {
 		Sale sale = Sale.all().getByKey(id);
 		List<Order> orders = Order.all().filter("sale", sale).fetch();
 		List<Product> products = sale.products.fetch();
 		Set<Member> members = new TreeSet<Member>();
 		Map<Product, Map<Member, ProductOrder>> ordersMap = buildOrdersMap(
 				products, orders, members);
 		render(sale, ordersMap, members);
 	}
 
 	private static Map<Product, Map<Member, ProductOrder>> buildOrdersMap(
 			List<Product> products, List<Order> orders, Set<Member> members) {
 		Map<Product, Map<Member, ProductOrder>> ordersMap = new HashMap<Product, Map<Member, ProductOrder>>();
 		for (Product product : products) {
 			ordersMap.put(product, new HashMap<Member, ProductOrder>());
 		}
 		for (Order order : orders) {
 			Member member = Member.all().getByKey(order.member.id);
 			members.add(member);
 			for (ProductOrder productOrder : order.products.fetch()) {
 				Product product = Product.all().getByKey(
 						productOrder.product.id);
 				ordersMap.get(product).put(member, productOrder);
 			}
 		}
 		return ordersMap;
 	}
 }
