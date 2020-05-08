 
 package edu.common.dynamicextensions.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.util.parser.CategoryGenerator;
 
 /**
  * 
  * @author mandar_shidhore
  *
  */
 public class CategoryCreator
 {
 
 	public static void main(String[] args)
 	{
 		createCategory(args);
 	}
 
 	/**
 	 * @param args
 	 * @return
 	 */
 	public static List<HashMap> createCategory(String[] args)
 	{
 		try
 		{
 			if (args.length == 0)
 			{
 				throw new Exception("PLEASE SPECIFY THE PATH FOR .csv FILE");
 			}
 
 			String filePath = args[0];
 			System.out.println("---- The .csv file path is " + filePath + " ----");
 
 			CategoryGenerator categoryGenerator = new CategoryGenerator(filePath);
 			CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 			boolean isEdited = true;
 			List<HashMap> categories = new ArrayList<HashMap>();
 			for (CategoryInterface category : categoryGenerator.getCategoryList())
 			{
 				if (category.getId() == null)
 				{
 					isEdited = false;
 				}
 
 				categoryHelper.saveCategory(category);
 
 				if (isEdited)
 				{
 					System.out.println("Edited category " + category.getName() + " successfully");
 				}
 				else
 				{
 					System.out.println("Saved category " + category.getName() + " successfully");
 				}
 
 				HashMap<CategoryInterface, Boolean> objCategoryMap = new HashMap<CategoryInterface, Boolean>();
				objCategoryMap.put(category, Boolean.valueOf(isEdited));
 				categories.add(objCategoryMap);
 			}
 
 			return categories;
 		}
 		catch (Exception ex)
 		{
 			System.out.println("\n----------");
 			System.out.println("Exception: ");
 			System.out.println("----------");
 			throw new RuntimeException(ex.getCause().getLocalizedMessage());
 		}
 	}
 
 }
