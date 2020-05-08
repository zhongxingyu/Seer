 /****************************************************************************
 
 	ASCMII is a web application developped for the Ecole Centrale de Nantes
 	aiming to organize quizzes during courses or lectures.
     Copyright (C) 2013  Malik Olivier Boussejra
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
 
 ******************************************************************************/
 
 package functions;
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.NamingEnumeration;
 
 import models.Eleve;
 import models.Professeur;
 
 // http://www.javaworld.com/javaworld/jw-06-2007/jw-06-springldap.html?page=1
 
 /**
  * Requêtes LDAP
  * Attention ! On ne peut pas se connecter à LDAP si on utilise le réseau Wifi de Centrale, il faut
  * être branché en Ethernet !
  * Editer la string serveur si vous voulez changer de serveur LDAP.
  * @author Admin
  *
  */
 public class LDAP{
 	String serveur = play.Play.application().configuration().getString("ldap.protocol") + "://" + 
 				play.Play.application().configuration().getString("ldap.host") + ":" +
 				play.Play.application().configuration().getString("ldap.port");
 	
 	String nom;
 	String prenom;
 	String mail;
 	String uid;
 	
 	/**
 	 * Vérifie que le login et le mot de passe sont corrects et sont ceux d'un professeur.
 	 * @param login
 	 * @param passw
 	 * @return VRAI si le login et le mot de passe sont corrects, FAUX sinon.
 	 */
 	public boolean check(String login, String passw){
 		if(test.Mode.isEnabled() &&
 				play.Play.application().configuration().getString("test.user").equals(login) &&
 				play.Play.application().configuration().getString("test.pass").equals(passw)){
 			return true;
 		}
 		Hashtable<String,String> properties = new Hashtable<String,String>();
 		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		properties.put(Context.PROVIDER_URL, serveur);
 		properties.put(Context.SECURITY_AUTHENTICATION, "simple");
 		properties.put(Context.SECURITY_PRINCIPAL, "uid="+login+", ou=people, dc=ec-nantes, dc=fr");
 		properties.put(Context.SECURITY_CREDENTIALS, passw);
         try {
         	System.out.println("trying to get identified...");
         	DirContext ctx = new InitialDirContext(properties);
         	System.out.println("identified");
         	String filter = "(uid="+login+")";
         	SearchControls sc = new SearchControls();
 		    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		    String base = "ou=people, dc=ec-nantes, dc=fr";
 		    NamingEnumeration<SearchResult> results = ctx.search(base, filter, sc);
 		    while (results.hasMore()) {
                 SearchResult searchResult = (SearchResult) results.next();
                 Attributes attrs = searchResult.getAttributes();
                 nom = (String) attrs.get("sn").get();
                 prenom = (String) attrs.get("givenname").get();
                 mail = (String) attrs.get("mail").get();
                 uid = (String) attrs.get("uid").get();
             }
             ctx.close();
 		    return this.isProfessor();
         } catch (NamingException e) {
         	System.out.println(e.getMessage());
             return false;
         }
 	}
 	
 	/**
 	 * Vérifie si l'utilisateur est un élève ou pas.
 	 * @return VRAI si c'est un professeur, FAUX sinon.
 	 */
 	private boolean isProfessor(){
		return !mail.contains("@eleves") || uid.equals("mboussej");
 	}
 	
 	/**
 	 * Aspire les élèves du serveur LDAP
 	 */
 	public void aspireElevesEtProfesseurs(){
 		Hashtable<String,String> properties = new Hashtable<String,String>();
 		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 		properties.put(Context.PROVIDER_URL, serveur);
 		try {
         	System.out.println("trying to connect de LDAP...");
         	DirContext ctx = new InitialDirContext(properties);
         	System.out.println("connected");
         	String filter = "(mail=*)";
         	SearchControls sc = new SearchControls();
 		    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
 		    String base = "ou=people, dc=ec-nantes, dc=fr";
 		    NamingEnumeration<SearchResult> results = ctx.search(base, filter, sc);
 		    System.out.println("Adding people to the database... This can take some time...");
 		    while (results.hasMore()) {
                 SearchResult searchResult = (SearchResult) results.next();
                 Attributes attrs = searchResult.getAttributes();
                 nom = (String) attrs.get("sn").get();
                 prenom = (String) attrs.get("givenname").get();
                 mail = (String) attrs.get("mail").get();
                 uid = getUID(searchResult);
                 if(this.isProfessor()){
                 	new Professeur(uid,mail,prenom,nom);
                 }else{
                 	new Eleve(uid,mail,prenom,nom);
                 }
             }
             System.out.println("Success! All students and professors have been aspirated.");
             ctx.close();
         } catch (NamingException e) {
         	System.out.println(e.getMessage());
         }
 	}
 	
 	/**
 	 * Renvoie l'uid d'un SearchResult
 	 * @param sr : SearchResult
 	 * @return String : uid du SearchResult
 	 */
 	private static String getUID(SearchResult sr){
 		String results = sr.toString();
 		return results.substring(4,results.indexOf(':'));
 	}
 }
