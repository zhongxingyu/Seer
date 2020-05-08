 package controllers;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import play.db.jpa.JPA;
 import play.mvc.Controller;
 
 public class Tester extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 	public static void query(String query) throws IllegalArgumentException,
 			IllegalAccessException {
 		flash.put("query", query);
 
		Query exeQuery = JPA.em().createQuery(query);
 		List<Object> exeResult = new ArrayList<Object>();
 		// Try to exe the query
 		try {
 			exeResult.addAll(exeQuery.getResultList());
 		} catch (Exception e) {
 			renderArgs.put("error", e.getMessage());
 			renderTemplate("Tester/queryempty.html");
 		}
 		// The query is empty
 		if (exeResult.size() == 0) {
 			renderArgs.put("error", "The query did not match any items.");
 			renderTemplate("Tester/queryempty.html");
 		}
 		// We got results
 		List<Field> fields = Arrays.asList(exeResult.get(0).getClass()
 				.getFields());
 
 		List<Object> items = new ArrayList<Object>();
 		for (int i = -1; i < exeResult.size(); i++) {
 			List<String> values = new ArrayList<String>();
 			for (int j = 0; j < fields.size(); j++) {
 				if (i == -1) {
 					values.add(fields.get(j).getName());
 				} else {
 					values.add("" + fields.get(j).get(exeResult.get(i)));
 				}
 			}
 			items.add(values);
 		}
 
 		renderArgs.put("results", items);
 
 		renderTemplate("Tester/queryresult.html");
 	}
 }
