 package main.jadrin.tools;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import main.jadrin.ontology.Drink;
 import main.jadrin.ontology.Ingredient;
 import main.jadrin.ontology.Recipe;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 public class Parser_drinkuj_pl implements PageParser{
 
 	static public String PAGE_TO_PARSE = "http://www.drinkuj.pl/spis.php";
 	
 	static ArrayList<Drink> drinkCache = null;
 			
 	
 	static public Drink parseDrinkPage(String pageAddr)
 	{
 		try {
 			Drink drink = new Drink();
 			Document doc = Jsoup.connect(pageAddr).get();
 			String titleFromAddr = pageAddr.replace("http://www.drinkuj.pl/drink","");
 			titleFromAddr = titleFromAddr.replace(".html","");
 			titleFromAddr = titleFromAddr.replace("_"," ");
 			titleFromAddr = titleFromAddr.substring(1);
 			drink.setName(titleFromAddr);
 			Element main = doc.getElementById("main");
 			Element right = main.child(2);
 			Element recipeContainer = right.child(1);
 			String ingrText =  recipeContainer.html();
 			ingrText = ingrText.substring(0,ingrText.indexOf("<script"));
 			// two time delete <br>
 			ingrText = ingrText.substring(0,ingrText.lastIndexOf("<br"));;
 			if (ingrText.length() > 10)
 			{
 			ingrText = ingrText.substring(0,ingrText.lastIndexOf("<br"));;
 			ingrText = AlphabetNormalizer.unAccent(ingrText.replace("&oacute;", "o"));
 			String list[] = ingrText.split("<br />");
 			//String ingList = "\nIlosci skladnikow:\n";
 			ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
 			for (String l : list)
 			{
 				//ingList +=l + "\n";
 				Ingredient ingredient = new Ingredient();
 				String ingName = l;
 				int index = l.indexOf("(");
 				if (-1 != index)
 				{
 					ingName = l.substring(0,index);
 				}
 				ingName = ingName.trim();
 				ingredient.setName(ingName);
 				ingredients.add(ingredient);
 			}
 			
 			if (ingredients.size() == 0)
 			{
 				return null;
 			}
 			else
 			{
 				drink.setIngredients(ingredients);
 			}
 			List<Element> potencialRecipe = recipeContainer.getElementsByClass("style1");
 			Element recipe = potencialRecipe.get(0);
 
 			String recipeStr = AlphabetNormalizer.unAccent(recipe.ownText().replace("&oacute;", "o"));
			recipeStr.replace("'", "\'");
 			Recipe recipeToDrink = new Recipe();
 			//recipeToDrink.setContent(ingList+"\n"+"Sposob przygotowania:\n"+recipeStr);
 			recipeToDrink.setContent(recipeStr);
 			drink.setRecipe(recipeToDrink);
 			return drink;
 			}
 			else
 			{
 				System.out.println("Uwaga: Źle sformatowana strona: " + pageAddr);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	static public ArrayList<Drink> parseListPage()
 	{
 		ArrayList<Drink> drinks = new ArrayList<Drink>();
 		try {
 	
 			Document doc = Jsoup.connect("http://www.drinkuj.pl/spis.php").get();
 			
 			Element main = doc.getElementById("main");
 			Element element = main.child(0);
 			List<Element> elements = element.getElementsByClass("header");
 			for (Element elem : elements)
 			{
 				Element container = elem.nextElementSibling();
 				List<Element> kols = container.children();
 				for (Element kol : kols)
 				{
 					List<Element> links = kol.getElementsByTag("a");
 					for (Element link : links)
 					{
 						Drink drink = parseDrinkPage(link.attr("href"));
 						if (drink!=null)
 						{
 							drinks.add(drink);
 						}
 					}
 				}
 				
 			}
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return drinks;
 	}
 	
 	public static void main(String [] args)
 	{
 		System.out.println("Parsuję stronę " + PAGE_TO_PARSE  );
 		System.out.println("Proszę czekać..");
 		ArrayList<Drink> list = Parser_drinkuj_pl.parseListPage();
 		for (Drink drink : list)
 		{
 			System.out.println("Nazwa: " + drink.getName());
 			System.out.println("Ilość składników: " + drink.getIngredients().size());
 			System.out.println("Opis: " + drink.getRecipe().getContent());
 		}
 	}
 
 	@Override
 	public ArrayList<Drink> parsePage() {
 		if (drinkCache == null)
 		{
 			drinkCache = Parser_drinkuj_pl.parseListPage();
 		}
 		return drinkCache;
 	}
 }
