 package fr.esiea.web_dev.miammiam.controllers;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.sql.Timestamp;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import fr.esiea.web_dev.miammiam.db.tables.daos.RecipeDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.RecipeHistoryDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.SessionDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.UserDao;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.Recipe;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.RecipeHistory;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.Session;
 
 public class RecipeController extends DynamicPage {
 
 	private final RecipeDao recipeTable;
 	private final RecipeHistoryDao historyTable;
 	
 	public RecipeController(SessionDao sessionDao, UserDao userDao, RecipeDao recipeDao, RecipeHistoryDao historyDao, String jsp) {
 		super(sessionDao, userDao, jsp, true);
 		
 		this.recipeTable = recipeDao;
 		this.historyTable = historyDao;
 	}
 
 	private void redirectToAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		request.setAttribute("action", "admin");
 		
 		request.getRequestDispatcher("/MiamServlet").forward(request, response);
 	}
 	
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 	
 		Gson json = new Gson();
 		
 		switch((String)request.getAttribute("action")) {
 		
 			case "new_recipe" : 
 			
 				System.out.println("plop " + super.jspPage);
 			
 				super.execute(request, response);
 				return;
 		
 		
 			case "recipe" : {
 				
 				Integer recipeId = Integer.parseInt(request.getParameter("id"));
 				
 				Recipe selected = this.recipeTable.fetchOneById(recipeId);
 				
 				if(selected==null) {
 					super.redirectToHome(request, response);
 					return;
 				}
 				
 				request.setAttribute("recipe", selected);
 				
 				
 				Gson gson = new Gson();
 				Type mapType = new TypeToken<Map<String, String>>(){}.getType();
 				
 				
 				Map<String, String> ingredients = (Map<String, String>) gson.fromJson(selected.getIngredients(), mapType);
 				
 				List<String> translated = newArrayList();
 				
 				NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
 				
 				for(Entry<String, String> i : ingredients.entrySet()) {
 					
 					String value = i.getValue();
 					String ingredient = i.getKey();
 					
 					Number quantity = 0;
 					
 					try {
 						quantity = format.parse(value);
 					} catch (ParseException e) {}
 					
 					switch(ingredient) {
 					
 						case "fruit" :
 						case "other" : translated.add(value); break;
 						
 						case "sugar" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sg de sucre.", value));
 							break;
 						case "milk" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sL de lait.", value));
 							break;
 						case "egg" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%s oeuf(s).", value));
 							break;
 						case "coconut" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sg de noix de coco.", value));
 							break;
 						case "flour" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sg de farine.", value));
 							break;
 						case "oil" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%s c.s d'huile.", value));
 							break;
 						case "butter" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sg de beurre.", value));
 							break;
 						case "vanilla" : 
 							if(quantity.floatValue() > 0)
 								translated.add(String.format("%sg de vanille.", value));
 							break;
 							
 					
 					}
 					
 				}
 				
 				
 				request.setAttribute("ingredients", translated);
 				
 				super.jspPage = "recipe.jsp";
 				
 				super.isAdminOnly = false;
 				
 				super.execute(request, response);
 				
 				return;
 			}
 				
 			case "search_recipe" : {
 				
 				Map<String, String[]> args = request.getParameterMap();
 				
 				Type mapType = new TypeToken<Map<String, String>>(){}.getType();
 				
 				System.out.println(json.toJson(args));
 				
 				Recipe selected=null;
 				int matching = 0;
 				int temp = 0;
 				
 				for(Recipe r : this.recipeTable.findAll()) {
 					
 					Map<String, String> ingredients = (Map<String, String>) json.fromJson(r.getIngredients(), mapType);
 					
 					NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
 					
 					for(Entry<String, String[]> s : args.entrySet())
 						if(s.getValue()[0].equalsIgnoreCase("on"))
 							try {
 								
 								if(!ingredients.get(s.getKey()).equals("non")) {
 									temp++;
 									continue;
 								}
 								
 								if(format.parse(ingredients.get(s.getKey())).floatValue() > 0)
 									temp++;
 							} catch (ParseException e) {
 								e.printStackTrace();
 							}
 						
 					if(temp>matching) {
 						selected = r;
 						matching = temp;
 						temp = 0;
 					}
 					
 				}
 				
 				request.setAttribute("recipe", selected);
 				
 				Session storedSession = super.getStoredSession(request.getSession());
 				
 				if(storedSession == null || selected == null) {
 					super.redirectToHome(request, response);
 					return;
 				}
 				
 				RecipeHistory newElt = new RecipeHistory(
 				  		null,
 						storedSession.getUser(), 
 						selected.getId(), 
 						new Timestamp(System.currentTimeMillis())
 						);
 				
 				this.historyTable.insert(newElt);
 				
 				System.out.println("plop");
 				
 				super.jspPage = "recipe.jsp";
 				
 				super.isAdminOnly = false;
 				
 				super.execute(request, response);
 				
 				return;
 			}
 				
 				
 			case "add_recipe" :
 				
 				Map<String, String[]> args = request.getParameterMap();
 				
 				Map<String, String> ingredients = newHashMap();
 				
 				ingredients.put("fruit", args.get("fruit")[0]);
 				ingredients.put("coconut", args.get("coconut")[0]);
 				ingredients.put("egg", args.get("egg")[0]);
 				ingredients.put("oil", args.get("oil")[0]);
 				ingredients.put("flour", args.get("flour")[0]);
 				ingredients.put("butter", args.get("butter")[0]);
 				ingredients.put("milk", args.get("milk")[0]);
 				ingredients.put("vanilla", args.get("vanilla")[0]);
 				ingredients.put("vanilla_sugar", args.get("vanilla_sugar")[0]);
 				ingredients.put("sugar", args.get("sugar")[0]);
				
				ingredients.put("other", args.get("other")[0].replace("script", "").replace("img", ""));
 				
 				Recipe newRecipe = new Recipe(
 						null, 
						args.get("name")[0].replace("script", "").replace("img", ""), 
						args.get("photo")[0].replace("script", "").replace("img", ""), 
 						Integer.parseInt(args.get("nb_people")[0]),
 						Integer.parseInt(args.get("prep_time")[0]), 
 						Integer.parseInt(args.get("cooking_time")[0]), 
 						json.toJson(ingredients), 
						args.get("steps")[0].replace("script", "").replace("img", ""));
 
 				this.recipeTable.insert(newRecipe);
 				
 				this.redirectToAdmin(request, response);
 				
 				break;
 				
 			case "delete_recipe" : 
 				
 				Integer id = Integer.parseInt(request.getParameter("id"));
 				
 				this.recipeTable.deleteById(id);
 				
 				this.redirectToAdmin(request, response);
 				
 				return;
 		
 		}
 		
 		
 		
 	}
 
 }
