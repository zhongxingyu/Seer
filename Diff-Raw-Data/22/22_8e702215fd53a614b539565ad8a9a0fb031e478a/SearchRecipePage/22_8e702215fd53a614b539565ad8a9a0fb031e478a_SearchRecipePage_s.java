 package fr.esiea.web_dev.miammiam.controllers;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import fr.esiea.web_dev.miammiam.db.tables.daos.RecipeDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.RecipeHistoryDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.SessionDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.UserDao;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.RecipeHistory;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.Session;
 
 public class SearchRecipePage extends DynamicPage {
 
 	private final RecipeHistoryDao historyTable;
 	private final RecipeDao recipeTable;
 	
 	public SearchRecipePage(SessionDao sessionDao, UserDao userDao, RecipeDao recipes, RecipeHistoryDao historyDao, String jsp, boolean b) {
 		super(sessionDao, userDao, jsp, b);
 		
 		this.historyTable = historyDao;
 		this.recipeTable = recipes;
 	}
 
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
		
 		Session session = super.getStoredSession(request.getSession());
 		
 		List<RecipeHistory> records = this.historyTable.fetchByUser(session.getUser());
 		
 		request.setAttribute("history", records);
 		
 		request.setAttribute("recipes", this.recipeTable);
 		
 		super.execute(request, response);
 	}
 
 	
 	
 }
