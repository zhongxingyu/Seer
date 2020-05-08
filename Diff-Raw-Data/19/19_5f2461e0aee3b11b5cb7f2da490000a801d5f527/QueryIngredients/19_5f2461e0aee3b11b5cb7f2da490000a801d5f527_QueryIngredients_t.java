 package com.github.cmput301w13t04.food.model.query;
 
 import java.util.ArrayList;
 
 import com.github.cmput301w13t04.food.model.Ingredient;
 
 /**
  * Holds the query for multiple ingredients
  * 
  */
 public class QueryIngredients {
 	private Query query;
 
 	public QueryIngredients(ArrayList<Ingredient> ingredients) {
 		this.query = new Query(ingredients);
 	}
 
 	public Query getQuery() {
 		return this.query;
 	}
 
 	public void setQuery(Query query) {
 		this.query = query;
 	}
 
 	public class Query {
 		private Bool bool;
 
 		public Query(ArrayList<Ingredient> ingredients) {
 			this.bool = new Bool(ingredients);
 		}
 
 		public Bool getBool() {
 			return this.bool;
 		}
 
 		public void setBool(Bool bool) {
 			this.bool = bool;
 		}
 
 		public class Bool {
 			private ArrayList<Must> must;
 
 			public Bool(ArrayList<Ingredient> ingredients) {
 				must = new ArrayList<Must>();
 				for (int i = 0; i < ingredients.size(); i++) {
 					must.add(new Must(ingredients.get(i).getName()));
 				}
 			}
 
 			public ArrayList<Must> getMust() {
 				return this.must;
 			}
 
 			public void setMust(ArrayList<Must> must) {
 				this.must = must;
 			}
 
 			public class Must {
 				private Query_string query_string;
 
 				public Must(String name) {
 					query_string = new Query_string(name);
 				}
 
 				public Query_string getQuery_string() {
 					return this.query_string;
 				}
 
 				public void setQuery_string(Query_string query_string) {
 					this.query_string = query_string;
 				}
 
 				public class Query_string {
 					private String default_field = "recipe.ingredients.name";
 					private String query;
 
 					public Query_string(String name) {
 						this.query = name;
 					}
 
 					public String getDefault_field() {
 						return this.default_field;
 					}
 
 					public void setDefault_field(String default_field) {
 						this.default_field = default_field;
 					}
 
 					public String getQuery() {
 						return this.query;
 					}
 
 					public void setQuery(String query) {
 						this.query = query;
 					}
 				}
 			}
 		}
 	}
 }
