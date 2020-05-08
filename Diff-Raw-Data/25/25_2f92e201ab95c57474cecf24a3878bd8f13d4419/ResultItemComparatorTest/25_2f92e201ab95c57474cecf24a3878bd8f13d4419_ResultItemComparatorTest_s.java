 package com.bananity.util;
 
 
 // Main Class
 import com.bananity.util.ResultItemComparator;
 import com.bananity.util.SearchTerm;
 
 // Java Utils
 import java.util.ArrayList;
 
 // Junit
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 
 /**
  * @author Andreu Correa Casablanca
  */
 @RunWith(JUnit4.class)
 public class ResultItemComparatorTest
 {
 	@Test
 	public void test_compare () {
 		SearchTerm _Bananity_St 	= new SearchTerm("Bananity");
 		SearchTerm _bananity_St 	= new SearchTerm("bananity");
 		SearchTerm _Bananity_On_St 	= new SearchTerm("Bananity On");
 		SearchTerm _Xananity_St 	= new SearchTerm("Xananity");
 
 		ResultItemComparator c1 = new ResultItemComparator(_Bananity_St);
 		ResultItemComparator c2 = new ResultItemComparator(_bananity_St);
 
 		// Equality comparisons
 		Assert.assertEquals(0, c1.compare(_Bananity_St, _Bananity_St));
 		Assert.assertEquals(0, c1.compare(_Xananity_St, _Xananity_St));
 		
 		Assert.assertEquals(0, c2.compare(_Bananity_St, _Bananity_St));
 		Assert.assertEquals(0, c2.compare(_Xananity_St, _Xananity_St));
 
 		// More Interesting comparisons
 		Assert.assertEquals(1, c1.compare(_Bananity_On_St, _Bananity_St));
 		Assert.assertEquals(-1, c1.compare(_Bananity_St, _Bananity_On_St));
 	}
 
 	@Test
 	public void test_compare_regression1 () {
 		SearchTerm _el_viaje_de_la_pegatina_St 	= new SearchTerm("el viaje de la pegatina");
 		SearchTerm _El_Viaje_De_La_Pegatina_St 	= new SearchTerm("El Viaje De La Pegatina");
 		SearchTerm _De_la_St 					= new SearchTerm("De la");
 
 		SearchTerm _huevos_fritos_St 			= new SearchTerm("huevos fritos");
 		SearchTerm _Fritos_St 					= new SearchTerm("Fritos");
 
 		ResultItemComparator c1 = new ResultItemComparator(_el_viaje_de_la_pegatina_St);
 		Assert.assertEquals(-1, c1.compare(_El_Viaje_De_La_Pegatina_St, _De_la_St));
 
 		ResultItemComparator c2 = new ResultItemComparator(_huevos_fritos_St);
 		Assert.assertEquals(-1, c2.compare(_huevos_fritos_St, _Fritos_St));
 	}
 
 	@Test
 	public void test_compare_regression_2 () {
 		SearchTerm _FC_Barcelona_St 	= new SearchTerm("Barça");
 		SearchTerm _fc_barcelona_St 	= new SearchTerm("barça");
 		SearchTerm _Barca_St 			= new SearchTerm("Barca");
 		SearchTerm _barca_St 			= new SearchTerm("barca");
 
 		ResultItemComparator _FC_Barcelona_Cmp 	= new ResultItemComparator(_FC_Barcelona_St);
 		ResultItemComparator _fc_barcelona_Cmp 	= new ResultItemComparator(_fc_barcelona_St);
 		ResultItemComparator _Barca_Cmp 		= new ResultItemComparator(_Barca_St);
 		ResultItemComparator _barca_Cmp 		= new ResultItemComparator(_barca_St);
 
 		// The next 4 blocks check 'special chars' comparisons
 		Assert.assertEquals(-1, _FC_Barcelona_Cmp.compare(_FC_Barcelona_St, _Barca_St));
 		Assert.assertEquals(-1, _FC_Barcelona_Cmp.compare(_FC_Barcelona_St, _barca_St));
 		Assert.assertEquals(-1, _FC_Barcelona_Cmp.compare(_fc_barcelona_St, _Barca_St));
 		Assert.assertEquals(-1, _FC_Barcelona_Cmp.compare(_fc_barcelona_St, _barca_St));
 
 		Assert.assertEquals(-1, _fc_barcelona_Cmp.compare(_FC_Barcelona_St, _Barca_St));
 		Assert.assertEquals(-1, _fc_barcelona_Cmp.compare(_FC_Barcelona_St, _barca_St));
 		Assert.assertEquals(-1, _fc_barcelona_Cmp.compare(_fc_barcelona_St, _Barca_St));
 		Assert.assertEquals(-1, _fc_barcelona_Cmp.compare(_fc_barcelona_St, _barca_St));
 
 		Assert.assertEquals(1, _FC_Barcelona_Cmp.compare(_Barca_St, _FC_Barcelona_St));
 		Assert.assertEquals(1, _FC_Barcelona_Cmp.compare(_barca_St, _FC_Barcelona_St));
 		Assert.assertEquals(1, _FC_Barcelona_Cmp.compare(_Barca_St, _fc_barcelona_St));
 		Assert.assertEquals(1, _FC_Barcelona_Cmp.compare(_barca_St, _fc_barcelona_St));
 
 		Assert.assertEquals(1, _fc_barcelona_Cmp.compare(_Barca_St, _FC_Barcelona_St));
 		Assert.assertEquals(1, _fc_barcelona_Cmp.compare(_barca_St, _FC_Barcelona_St));
 		Assert.assertEquals(1, _fc_barcelona_Cmp.compare(_Barca_St, _fc_barcelona_St));
 		Assert.assertEquals(1, _fc_barcelona_Cmp.compare(_barca_St, _fc_barcelona_St));
 
 		// The next 4 blocks check 'special chars + case' comparisons
		//Assert.assertEquals(-1, _FC_Barcelona_Cmp.compare(_FC_Barcelona_St, _fc_barcelona_St));
		//Assert.assertEquals(1, _FC_Barcelona_Cmp.compare(_fc_barcelona_St, _FC_Barcelona_St));
 	}
 }
