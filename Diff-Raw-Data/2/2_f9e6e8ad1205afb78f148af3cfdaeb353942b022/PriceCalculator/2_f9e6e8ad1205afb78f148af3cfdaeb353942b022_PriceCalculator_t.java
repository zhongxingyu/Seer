 package com.mckinsey;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.mckinsey.cart.Cart;
 import com.mckinsey.cart.Item;
 import com.mckinsey.cart.ItemType;
 import com.mckinsey.cart.user.User;
 import com.mckinsey.cart.user.UserBuilder;
 import com.mckinsey.cart.user.UserType;
 import com.mckinsey.discounts.DiscountFilter;
 
 public class PriceCalculator {
 	public static void main(String[] args) throws Exception {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				System.in));
 		User user = buildUser(reader);
 		Cart cart = buildCart(reader);
 		DiscountFilter filter = DiscountFilter.createFilter(user);
 		double discount = filter.computeDiscount(cart);
 		System.out.println("##################################");
 		System.out.println("Receipt for : " + user);
 		System.out.println(cart);
		System.out.println("Total Discount ### " + discount);
 	}
 
 	private static Cart buildCart(BufferedReader reader) throws Exception {
 		System.out.println("###### Creating Cart for specified User ######");
 		Cart cart = new Cart();
 		String more;
 		do {
 			System.out.print("Specify item name:");
 			String name = reader.readLine();
 			System.out.println("Item Types :");
 			for (ItemType type : ItemType.values()) {
 				System.out.println("### " + type);
 			}
 			System.out.print("Specify type:");
 			String type = reader.readLine();
 
 			System.out.print("Please specify  price:");
 			String price = reader.readLine();
 			cart.addItem(new Item(name, ItemType.valueOf(type.toUpperCase()),
 					Double.valueOf(price)));
 			System.out.print("Add more items [y/n]:");
 			more = reader.readLine();
 		} while ("Y".equalsIgnoreCase(more));
 		return cart;
 	}
 
 	private static User buildUser(BufferedReader reader) throws Exception {
 		System.out.print("Please enter User name :");
 		String name = reader.readLine();
 		System.out.println("Types of User :");
 		for (UserType type : UserType.values()) {
 			System.out.println("### " + type);
 		}
 		System.out.print("Please select a type:");
 		String type = reader.readLine();
 		Date membershipDate = null;
 		if (UserType.CUSTOMER.name().equalsIgnoreCase(type)) {
 			System.out.print("Member since[dd/mm/yyyy]:");
 			String date = reader.readLine();
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
 			membershipDate = dateFormat.parse(date);
 		}
 		return new UserBuilder(name)
 				.setUserType(UserType.valueOf(type.toUpperCase()))
 				.setMemberShipDate(membershipDate).createUser();
 	}
 }
