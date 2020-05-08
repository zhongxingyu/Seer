 package com.octo.red.newsql.services;
 
 import com.octo.red.newsql.model.*;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.math.BigDecimal;
 import java.util.List;
 
 @ContextConfiguration("classpath:com/octo/rnd/PersistenceTests-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class ServiceTest {
 
 	@PersistenceContext
 	EntityManager entityManager;
 	@Autowired
 	TransactionService transactionService;
 	@Autowired
 	TurnoverService turnoverService;
 	@Autowired
 	InventoryService inventoryService;
 	
 	@Test
 	@Transactional
 	public void testBuyFirstProduct() {
 		//Given
 		final BigDecimal unitPrice = BigDecimal.TEN;
 		final BigDecimal vatRate = new BigDecimal("0.1");
 		VAT vat = SampleFactory.buildVAT();
 		Country country = SampleFactory.buildCountry();
 		entityManager.persist(country);
 		country.addVat(vat);
 		vat.setVatRate(vatRate);
 		entityManager.persist(vat);
 		CategoryFamily cf = SampleFactory.buildCategoryFamily();
 		cf.addVat(vat);
 		entityManager.persist(cf);
 		Product product = SampleFactory.buildProduct();
 		product.setSellPrice(unitPrice);
 		entityManager.persist(product);
 		product.setCategoryFamily(cf);
 		Store store = SampleFactory.buildStore();
 		entityManager.persist(store);
 		Stock stock = SampleFactory.buildStock();
 		store.addStock(stock);
 		stock.setProduct(product);
 		entityManager.persist(stock);
 		entityManager.flush();
 		// Otherwise the query returns the existing order (and we didn't set the
 		// parent in the item)...
 		entityManager.clear();
 		//When
 		SaleOperation operation = transactionService.buy(country.getAlpha3Code(), product.getId(), store.getId(), null);
 		//Then
 		Assert.assertNotNull(operation);
 		final BigDecimal totalPrice = unitPrice.multiply(vatRate.add(BigDecimal.ONE));
 		Assert.assertEquals(0, totalPrice.compareTo(operation.getAmount()));
 		
 		@SuppressWarnings("unchecked")
 		List<SaleTransaction> sts = (List<SaleTransaction>) entityManager.createQuery("select st from SaleTransaction st").getResultList();
 		Assert.assertNotNull(sts);
 		Assert.assertEquals(1, sts.size());
 		SaleTransaction st = sts.get(0);
 		Assert.assertEquals(0, totalPrice.compareTo(st.getTotalAmount()));
 	}
 	
 	@Test
 	@Transactional
 	public void testBuySecondProduct() {
 		//Given
 		final BigDecimal unitPrice = BigDecimal.TEN;
 		final BigDecimal vatRate = new BigDecimal("0.1");
 		final BigDecimal initialTotalAmount = BigDecimal.valueOf(14);
 		VAT vat = SampleFactory.buildVAT();
 		Country country = SampleFactory.buildCountry();
 		entityManager.persist(country);
 		country.addVat(vat);
 		vat.setVatRate(vatRate);
 		entityManager.persist(vat);
 		CategoryFamily cf = SampleFactory.buildCategoryFamily();
 		cf.addVat(vat);
 		entityManager.persist(cf);
 		Product product = SampleFactory.buildProduct();
 		product.setSellPrice(unitPrice);
 		entityManager.persist(product);
 		product.setCategoryFamily(cf);
 		Store store = SampleFactory.buildStore();
 		entityManager.persist(store);
 		Stock stock = SampleFactory.buildStock();
 		store.addStock(stock);
 		stock.setProduct(product);
 		entityManager.persist(stock);
 		entityManager.flush();
 		SaleTransaction saleTransaction = SampleFactory.buildSaleTransaction();
 		saleTransaction.setTotalAmount(initialTotalAmount);
 		entityManager.persist(saleTransaction);
 		final long txId = saleTransaction.getId();
 		SaleOperation saleOperation = SampleFactory.buildSaleOperation();
 		saleOperation.setSaleTransaction(saleTransaction);
 		entityManager.persist(saleOperation);
 		entityManager.persist(vat);
 		entityManager.flush();
 		// Otherwise the query returns the existing order (and we didn't set the
 		// parent in the item)...
 		entityManager.clear();
 		//When
 		SaleOperation operation = transactionService.buy(country.getAlpha3Code(), product.getId(), store.getId(), txId);
 		//Then
 		Assert.assertNotNull(operation);
 		//Ignore the conversion rate for now
 		final BigDecimal totalPrice = unitPrice.multiply(vatRate.add(BigDecimal.ONE)).add(initialTotalAmount);
 		
 		@SuppressWarnings("unchecked")
 		List<SaleTransaction> sts = (List<SaleTransaction>) entityManager.createQuery("select st from SaleTransaction st").getResultList();
 		Assert.assertNotNull(sts);
 		Assert.assertEquals(1, sts.size());
 		SaleTransaction st = sts.get(0);
 		Assert.assertEquals(0, totalPrice.compareTo(st.getTotalAmount()));
 	}
 	
 	@Test
 	@Transactional
 	public void testTotal() {
 		//Given
 		final BigDecimal initialTotalAmount = BigDecimal.valueOf(14);
 		SaleTransaction saleTransaction = SampleFactory.buildSaleTransaction();
 		saleTransaction.setTotalAmount(initialTotalAmount);
 		entityManager.persist(saleTransaction);
 		final long txId = saleTransaction.getId();
 		entityManager.flush();
 		// Otherwise the query returns the existing order (and we didn't set the
 		// parent in the item)...
 		entityManager.clear();
 		//When
 		final TotalVo total = transactionService.computeTotal(txId);
 		//Then
 		Assert.assertEquals("EUR", total.getCurrency());
 		//Ignore the transaction rate for now
 		Assert.assertEquals(0, initialTotalAmount.compareTo(total.getAmount()));
 	}
 	
 	@Test
 	@Transactional
 	public void testComputeTurnover() {
 		//Given
 		final SaleOperation[] saleOperations = SampleFactory.buildSaleOperationsSet();
 		entityManager.persist(saleOperations[0]);
 		entityManager.persist(saleOperations[1]);
 		entityManager.persist(saleOperations[2]);
 		entityManager.persist(saleOperations[3]);
 		entityManager.persist(saleOperations[4]);
 		entityManager.flush();
 		// Otherwise the query returns the existing order (and we didn't set the
 		// parent in the item)...
 		entityManager.clear();
		turnoverService.setActualTestDate(SampleFactory.buildDate(2012,02,01));
 		//When
 		final List<TurnoverVo> turnovers = turnoverService.computeTurnover(1);
 		//Then
 		Assert.assertNotNull(turnovers);
 		Assert.assertEquals(2, turnovers.size());
 		final TurnoverVo toEur = turnovers.get(0);
 		Assert.assertEquals(0, new BigDecimal("101.1").compareTo(toEur.getTotalAmount()));
 		Assert.assertEquals("EUR", toEur.getCurrency());
 		Assert.assertEquals(1, toEur.getGroupId());
 		final TurnoverVo toUsd = turnovers.get(1);
 		Assert.assertEquals(0, new BigDecimal("3.3").compareTo(toUsd.getTotalAmount()));
 		Assert.assertEquals("USD", toUsd.getCurrency());
 		Assert.assertEquals(1, toUsd.getGroupId());
 	}
 	
 	@Test
 	@Transactional
 	public void testListInventory() {
 		//Given
 		Store store = SampleFactory.buildStore();
 		
 		CategoryFamily cat1 = SampleFactory.buildCategoryFamily();
 		entityManager.persist(cat1);
 		
 		Product product1 = SampleFactory.buildProduct("Produit 1");
 		product1.setCategoryFamily(cat1);
 		entityManager.persist(product1);
 		Stock stock1 = SampleFactory.buildStock();
 		stock1.setProduct(product1);
 		stock1.setQuantity(1);
 		entityManager.persist(stock1);
 		
 		Product product2 = SampleFactory.buildProduct("Produit 2");
 		product2.setCategoryFamily(cat1);
 		entityManager.persist(product2);
 		Stock stock2 = SampleFactory.buildStock();
 		stock2.setProduct(product2);
 		stock2.setQuantity(2);
 		entityManager.persist(stock2);
 		
 		store.addStock(stock1);
 		store.addStock(stock2);
 		entityManager.persist(store);
 		
 		entityManager.flush();
 		entityManager.clear();
 		
 		//When
 		List<InventoryRecord> inventory = inventoryService.list(store.getId());
 
 		//Then
 		Assert.assertEquals((int) 2, (int) inventory.size());
 	}
 }
