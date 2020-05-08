 package tests;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 
 import indeece.AbstractRankCalculationStrategy;
 import indeece.CorpusBuilder;
 import indeece.ICosineRankCalculationStrategy;
 import indeece.Indeece;
 import indeece.LncLtcNormalizationStrategy;
 import indeece.Model;
 import indeece.NaturalRankCalculationStrategy;
 import indeece.SubLinearRankCalculationStrategy;
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class SerializationTests extends TestCase {
 
 	final static String PathName = "C:\\Work\\TUDelft\\Course\\IR\\IndeceSearch\\IndeeceSearch\\tempCorpus";
 	 
 	public void testBooleanQuery()
 	 {
 		 	Indeece.setCosineRankingStrategy( new NaturalRankCalculationStrategy());	
 		 	try {
 				Indeece.createIndex(new CorpusBuilder(PathName), true, true);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			Indeece.initModels(false);
 			Indeece.setActive("Boolean");
 			
 			Collection<Model.Result> results	= null;
 			String query = "a*e*i*a"; // america 
 			try {
 				results = Indeece.activeModel().search(query);
 				System.out.println("search results .... Query: ["+ query + "]");
 				// print results
 				
 				for(Model.Result result : results)
 				{
 					System.out.println("[Title]: "+ result.getDocument().getTitle().trim() + " \t [Body] : " +result.getDocument().getBody().trim() + " \t [Score] : "+ result.getScore());  
 				}
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	 }
 	
 	 public void testSerialization()
 	 {
 		 	Indeece.setCosineRankingStrategy( new NaturalRankCalculationStrategy());	
 		 	try {
 				Indeece.createIndex(new CorpusBuilder(PathName), true, false);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			Indeece.initModels(false);
 			Indeece.setActive("Vector");
 			
 			Collection<Model.Result> results	= null;
 			String query = "g*d";
 			try {
 				results = Indeece.activeModel().search(query);
 				System.out.println("search results .... Query: ["+ query + "]");
 				// print results
 				
 				for(Model.Result result : results)
 				{
 					System.out.println("[Title]: "+ result.getDocument().getTitle().trim() + " \t [Body] : " +result.getDocument().getBody().trim() + " \t [Score] : "+ result.getScore());  
 				}
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	 }
 	 
 	 public void testSerialization2()
 	 {
		 	Indeece.setCosineRankingStrategy( new SubLinearRankCalculationStrategy() , true);	
 		 	try {
 				Indeece.createIndex(new CorpusBuilder(PathName), false, false);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			Indeece.initModels(false);
 			Indeece.setActive("Vector");
 			
 			Collection<Model.Result> results	= null;
			String query = "fire";
 			try {
 				results = Indeece.activeModel().search(query);
 				System.out.println("search results .... Query: ["+ query + "]");
 				// print results
 				
 				for(Model.Result result : results)
 				{
 					System.out.println("[Title]: "+ result.getDocument().getTitle().trim() + " \t [Body] : " +result.getDocument().getBody().trim() + " \t [Score] : "+ result.getScore());  
 				}
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	 }
 	 
 	 public void testSerialization3()
 	 {
 		 	Indeece.setCosineRankingStrategy( new LncLtcNormalizationStrategy());	
 		 	try {
 				Indeece.createIndex(new CorpusBuilder(PathName), false, false);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			Indeece.initModels(false);
 			Indeece.setActive("Vector");
 			
 			Collection<Model.Result> results	= null;
 			String query = "gold silver truck";
 			try {
 				results = Indeece.activeModel().search(query);
 				System.out.println("search results .... Query: ["+ query + "]");
 				// print results
 				
 				for(Model.Result result : results)
 				{
 					System.out.println("[Title]: "+ result.getDocument().getTitle().trim() + " \t [Body] : " +result.getDocument().getBody().trim() + " \t [Score] : "+ result.getScore());  
 				}
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	 }
 	 
 	 public void testSwitchingIndece()
 	 {
 		 Indeece.setCosineRankingStrategy( new LncLtcNormalizationStrategy());	
 		 
 		 String [] strategies = Indeece.getCosineRankingStrategiesString();
 		 
 		 Indeece.setCosineRankingStrategyFromName(strategies[0] , true);
 		 
 		 Assert.assertTrue(Indeece.getCosineStrategy() instanceof  NaturalRankCalculationStrategy);
 		 Assert.assertTrue(((AbstractRankCalculationStrategy)Indeece.getCosineStrategy()).UseFastCosineStrategy);
 		 
 		 
 		 
 	 }
 	
 }
