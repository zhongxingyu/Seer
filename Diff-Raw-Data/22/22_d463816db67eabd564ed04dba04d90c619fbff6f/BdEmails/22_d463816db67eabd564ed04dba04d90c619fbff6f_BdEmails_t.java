 
 
 /**
  * <p>1. Nettoyage initial
  * <p>2. Deternimer les noms de domaines
  * <p>3. Determiner les regles de constitution des emails entreprises
  * <p>4. Reconstituer les adresses emails manquants
  * <p>5. Reconstituer nom prenom
  * <p>6. Determiner genre (M, Mme)
  * <p>7. Identifier les emails differents du nom prenom
  * 
  * @author Daniel
  * 
  */
 public class BdEmails {
 	
 	private static String user = "root"; //DB username
 	private static String pwd = "asd123"; //DB password
 	private static String dbName = "jdbc:mysql://localhost:3306/bd_emails"; //DB name
 	public BdEmails(String user, String pwd) {
 		BdEmails.user = user;
 		BdEmails.pwd = pwd;
 	}
 	
 	/**
 	 * Methods are called from here with the desired parameters.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		//Accessing driver from the JAR file
 		
 		// Class.forName("com.mysql.jdbc.Driver");
 		
 		//Creating a variable for the connection called "con"
 		
 		//showEmpty("bd_emails_5");
 		//showEmpty("bd_emails_4");
 		
 		//showColumnNames("bd_emails");
 		
 		//insertNonEmpty("bd_emails", "bd_emails_4");
 		insertEmpty("bd_emails_5","empty_deleted");
 		
 		//countEmptyFirstname("bd_emails_4");
 		//countEmptyLastname("bd_emails_4");
 		//countEmptyEmail("bd_emails_4");
 		//countAll("bd_emails");
 		//countAll("bd_emails_5");
		
 	}
 	
 	private static void countAll(String table) {
 RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = ""; 
 		boolean printRows = false;
 		
 		System.out.println("\nTotal rows in "+table+" : " 
 				+ counter.countFromWhere(table, where, printRows));
 	}
 	
 	/**
 	 * Counts the number of rows without email from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	private static void countEmptyEmail(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = 
 				"(ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_mail IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', " +
 				"'84/12/39/44', '05/65/77/85/37', 'z', 'Z', 'nc', 'NC', 'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 				"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', 'gu', '01.01.1981', 'fr', 'REMPLACE MME SUINOT EN MALADIE', " +
 				"'01 47 68 12 63', " +
 				"'gu', '661261090', 'b', '603707107', " +
 				"'pas d\\'adresse e-mail', 'pe', '630108886', 'pas adresse', " +
 				"'aucune', '664998228', 'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer'))"; 
 		
 		boolean printRows = true;
 		
 		System.out.println("\nTotal rows with empty email in "+table+" returned:" 
 				+ counter.countFromWhere(table, where, printRows));
 	}
 	
 	/**
 	 * Counts the number of rows without first name from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	private static void countEmptyLastname(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = "ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???') OR ct_nom IS NULL"; 
 		boolean printRows = true;
 		
 		System.out.println("\nTotal rows returned:" 
 				+ counter.countFromWhere(table, where, printRows));
 	}
 	
 	/**
 	 * Counts the number of rows without last name from a given table.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	private static void countEmptyFirstname(String table) {
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = "ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir') OR ct_prenom IS NULL"; 
 		boolean printRows = true;
 		
 		System.out.println("\nTotal rows returned:" 
 				+ counter.countFromWhere(table, where, printRows));
 	}
 	
 	/**
 	 * Prints clients with empty <b>first name</b>, <b>family name</b> and <b>email</b> at the same time. 
 	 * <br />Needs a table which is a version of bd_emails and has at least the columns 
 	 * <b>ct_prenom</b>, <b>ct_nom</b> and <b>ct_mail</b>.
 	 * 
 	 * @param table the table that should be checked
 	 */
 	private static void showEmpty(String table){
 		
 		RowCounter counter = new RowCounter(dbName,user,pwd);
 		
 		//we define the condition to select specific rows and to count them
 		String where = 
 				"(ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_mail IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', " +
 				"'84/12/39/44', '05/65/77/85/37', 'z', 'Z', 'nc', 'NC', 'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 				"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', 'gu', '01.01.1981', 'fr', 'REMPLACE MME SUINOT EN MALADIE', " +
 				"'01 47 68 12 63', " +
 				"'gu', '661261090', 'b', '603707107', " +
 				"'pas d\\'adresse e-mail', 'pe', '630108886', 'pas adresse', " +
 				"'aucune', '664998228', 'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer'))"; 
 		boolean printRows = false;
 		
 		System.out.println("\nTotal empty rows in "+table+" : " 
 				+ counter.countFromWhere(table, where, printRows));
 	}
 	
 	/**
 	 * Gives total columns number and prints each column's name of a given table from the database. 
 	 * 
 	 * @param table the table whose columns should be taken into consideration
 	 */
 	private static void showColumnNames(String table){
 		Select select = new Select(dbName, user, pwd);
 		String[] columnsList = select.getColumnsAsArray(table);
 		System.out.println("Total number of columns: " + (columnsList.length-1));
 		for (int i=1;i<columnsList.length;i++){
 			System.out.println("Column " + i + " : " + columnsList[i]);
 		}
 	}
 	
 	/**
 	 * Takes non-empty clients and writes them to a new table, thus avoiding deletion.
 	 * <br />Should be run once per destination table 
 	 * 
 	 * @param fromTable the source table to takes values from
 	 * @param toTable the destination table to write values into
 	 */
 	private static void insertNonEmpty(String fromTable, String toTable){
 		
 		String where = 
 				"(ct_prenom NOT IN ('', 'A DEFINIR', '68', '-', '.', 'definir', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_nom NOT IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_mail NOT IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', " +
 				"'84/12/39/44', '05/65/77/85/37', 'z', 'Z', 'nc', 'NC', 'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 				"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', 'gu', '01.01.1981', 'fr', 'REMPLACE MME SUINOT EN MALADIE', " +
 				"'01 47 68 12 63', " +
 				"'gu', '661261090', 'b', '603707107', " +
 				"'pas d\\'adresse e-mail', 'pe', '630108886', 'pas adresse', " +
 				"'aucune', '664998228', 'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer'))"; 
 		
 		InsertRows insert = new InsertRows(dbName, user, pwd);
 		insert.insertFromWhere(fromTable, toTable, where);
 		
 	}
 	
 	/**
 	 * Takes empty clients and writes them to a new table, allowing export of empty rows.
 	 * <br />Should be <b>run once</b> per destination table.
 	 * 
 	 * @param fromTable
 	 * @param toTable
 	 */
 	private static void insertEmpty(String fromTable, String toTable){
 		
 		String where = 
 				"(ct_prenom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_nom IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', 'z', 'Z', 'nc', 'NC')) " +
 				"AND (ct_mail IN ('', 'A DEFINIR', '68', '-', '.', 'definir', '?', '???', " +
 				"'84/12/39/44', '05/65/77/85/37', 'z', 'Z', 'nc', 'NC', 'non', 'PAS DE MAIL', 'N INDISPONIBLE', 'REPONDEUR', " +
 				"'en retraite', 'n\\'a pas de mail', 'pas de demarchage', 'email', 'gu', '01.01.1981', 'fr', 'REMPLACE MME SUINOT EN MALADIE', " +
 				"'01 47 68 12 63', " +
 				"'gu', '661261090', 'b', '603707107', " +
 				"'pas d\\'adresse e-mail', 'pe', '630108886', 'pas adresse', " +
 				"'aucune', '664998228', 'pas de mail', 'pas d\\'e-mail', 'pas d\\'adresse', 'ne veut pas communiquer'))"; 
 		
 		InsertRows insert = new InsertRows(dbName, user, pwd);
 		insert.insertFromWhere(fromTable, toTable, where);
 		
 	}
 	
 }
