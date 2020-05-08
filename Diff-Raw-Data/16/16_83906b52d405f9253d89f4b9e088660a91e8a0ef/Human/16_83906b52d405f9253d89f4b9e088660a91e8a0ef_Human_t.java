 package org.capitalism;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Human implements ITransactor, IProducer {
 
 	private final double minProfit = new Random().nextDouble() + 1;
 	Money worth = Money.MoneySupply.getMoney(50);
 	ArrayList<Product> products = new ArrayList<Product>();
 	double productEquity = 0;
	private final double productValue = new Random().nextDouble()*worth.getValue()*.1;
 	private final double minOfferPercentage = new Random().nextDouble() + 1;
 	private final double maxOfferPercentage = minOfferPercentage + .15;
 
 	@Override
 	public Money deduct(double transactionValue) {
 
 		return worth.split(transactionValue);
 	}
 
 	@Override
 	public void increment(Money deduction) {
 		worth.add(deduction);
 	}
 
 	@Override
 	public double getWorth() {
 		return worth.getValue();
 	}
 	
 	public double getEquity() {
 		
 		return worth.getValue() + productEquity;
 	}
 	
 	@Override
 	public void createConsumable() {
 
 		Money money = deduct(productValue);
 		Product p = new Product(money);
 		products.add(p);
 		productEquity += p.getValue();
 
 	}
 
 	public void live(ArrayList<Human> nearbyHumans) {
 
 		// the sole purpose of a human is to maximize their worth
 		produce();
 		consume(chooseSeller(nearbyHumans));
 
 	}
 
 	private ITransactor chooseSeller(ArrayList<Human> humans) {
 
 		if(humans.size() == 1) {
 			return null;
 		}
 		
 		int transactorIndex = new Random().nextInt(humans.size());
 		Human human = humans.get(transactorIndex);
 		if(human == this) {
 			transactorIndex++;
 			transactorIndex%= humans.size();
 			human = humans.get(transactorIndex);
 		}
 		
 		return human;
 	}
 	
 //TODO: This code is hard to follow
 	private void consume(ITransactor seller) {
 
 		final int numItemsForSale = seller.getItemsForSale().size();
 
 		if (numItemsForSale > 0) {
 
 			//pick item to purchase
 			IConsumable itemToPurchase = seller.getItemsForSale().get(
 					new Random().nextInt(numItemsForSale));
 
 			double offerPrice = calculateTransactionOfferPrice(itemToPurchase);
 			
 			TransactionTerms terms = new TransactionTerms(offerPrice,
 					itemToPurchase.getId());
 			
 			TransactionAgreement agreement = proposeTransaction(seller, terms);
 			new Transaction(this, seller, agreement);
 		}
 	}
 
 
 	private double calculateTransactionOfferPrice(IConsumable itemToPurchase) {
 
 		double itemPrice = itemToPurchase.getPrice();
 		double offerPrice = itemPrice*(new Random().nextDouble()*(maxOfferPercentage - minOfferPercentage) + minOfferPercentage);
 		return offerPrice;
 	}
 
 	private void produce() {
 		if (new Random().nextBoolean()) {
 			createConsumable();
 		}
 
 	}
 
 	@Override
 	public TransactionAgreement proposeTransaction(ITransactor seller,
 			TransactionTerms transactionTerms) {
 		return new TransactionAgreement(this, seller, transactionTerms);
 	}
 
 	@Override
 	public boolean acceptTransaction(TransactionTerms terms) {
 
 		IConsumable consumable = get(terms.consumableId);
 		if(consumable != null) {
 			final double consumableValue = consumable.getValue();
 			if(terms.transactionValue >= consumableValue*minProfit ) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public ArrayList<Product> getItemsForSale() {
 
 		// for now everything is on sale
 		return products;
 	}
 
 	@Override
 	public IConsumable remove(int consumableId) {
 		for (int i = 0; i < products.size(); i++) {
 			if (products.get(i).getId() == consumableId) {
 				productEquity -= products.get(i).getValue();
 				return products.remove(i);
 			}
 		}
 		return null;
 	}
 	
 	public IConsumable get(int consumableId) {
 		for (int i = 0; i < products.size(); i++) {
 			if (products.get(i).getId() == consumableId) {
 				return products.get(i);
 			}
 		}
 		return null;
 	}	
 
 	@Override
 	public void give(IConsumable consumable) {
 
 		if (consumable != null) {
 			products.add((Product) consumable);
 			productEquity += consumable.getValue();
 		}
 
 	}
 
 	public int getNumConsumables() {
 
 		return products.size();
 	}
 
 }
