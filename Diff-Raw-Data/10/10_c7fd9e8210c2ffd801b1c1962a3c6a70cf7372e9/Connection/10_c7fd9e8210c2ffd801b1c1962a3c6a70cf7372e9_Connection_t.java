 package com.utt.scd.modele;
 
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class Connection 
 {
 
 	public Gson gson;
 	
 	private static Connection instance;
 	
 	private boolean isInitialized = false;	
 	
 	
 	private Connection() 
 	{
 		super();
 	}
 	
 	public final static Connection getInstance() 
 	{
 		if (Connection.instance == null) 
 		{
 			synchronized (Connection.class) 
 			{
 				if (Connection.instance == null) 
 				{
 					Connection.instance = new Connection();
 				}
 			}
 		}
 
 		return Connection.instance;
 	}
 	
 	public Connection initialize()
 	{
 		
 		GsonBuilder gsonBuilder = new GsonBuilder();
 
 		// TODO : Handle timezones properly
 		this.gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
 		
 		
 		this.isInitialized = true;
 		
 
 		return (Connection.getInstance());
 		
 		
 	}
 	
 	
 	public List<ParseObject> rechercheSimple(String chaine) throws ConnectionNotInitializedException, ParseException
 	{
 		ParseQuery query = new ParseQuery("Livre");
 		
 		query.whereMatches("Titre", regexRecherche(chaine));
 		
 		if (!this.isInitialized) 
 		{
 			throw new ConnectionNotInitializedException("Connection has not been initialized");
 		}
 		else
 		{
 			return query.find();
 		}
 	}
 	
 	public List<ParseObject> rechercheAvancee(String titre, String auteur, String support, String langue, String uv) throws ConnectionNotInitializedException, ParseException
 	{
 		ParseQuery query = new ParseQuery("Livre");
 		
 		if (!titre.equals(""))
 		{
 			query.whereMatches("Titre", regexRecherche(titre));
 		}
 		if (!auteur.equals(""))
 		{
 			query.whereMatches("Auteur", regexRecherche(auteur));
 		}
 		if (!support.equals(""))
 		{
			query.whereMatches("Support", regexRecherche(support));
 		}
 		if (!langue.equals(""))
 		{
			query.whereMatches("Langue", regexRecherche(langue));
 		}
 		if (!uv.equals(""))
 		{
			query.whereMatches("UV", regexRecherche(uv));
 		}
 		
 		
 		if (!this.isInitialized) 
 		{
 			throw new ConnectionNotInitializedException("Connection has not been initialized");
 		}
 		else
 		{
 			return query.find();
 		}
 	}
 	
 	
 	public String regexRecherche(String chaine)
 	{
 		String[] mots = chaine.split(" ");
 		String regex="";
 		
 		regex += ".*";
 		for (int i = 0 ; i < mots.length ; i++)
 		{
 			
 			for (int j = 0 ; j < mots[i].length() ; j++)
 			{
 				regex += "[";
 				if (Character.isUpperCase(mots[i].charAt(j)))
 				{
 					regex += Character.toString(mots[i].charAt(j)) + Character.toString(Character.toLowerCase(mots[i].charAt(j)));
 				}
 				else if (Character.isLowerCase(mots[i].charAt(j)))
 				{
 					regex += Character.toString(mots[i].charAt(j)) + Character.toString(Character.toUpperCase(mots[i].charAt(j)));
 				}
 				else
 				{
 					regex += Character.toString(mots[i].charAt(j));
 				}
 				regex += "]";
 			}
 			if(i != mots.length)
 			{
 				regex += ".*";
 			}
 			
 		}
 
 		return regex;
 	}
 
 	
 	
 	
 	
 }
