 package service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.sql.*;
 
 import javax.naming.*;
 import javax.sql.*;
 
 import model.Enquete;
 import model.Question;
 import model.Choice;
 import model.Answer;
 import model.ResultHolder;
 
 public class EnqueteService {
 	private List<Enquete> enquetes;
 
 	public EnqueteService()
 	{
 		this.enquetes = new ArrayList<Enquete>();
 	}
 
 	public int createEnquete(String name) {
 		int id = 0;
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement estmt = conn.createStatement();
 
			ResultSet ers = estmt.executeUpdate("INSERT INTO enquete(name) VALUES('" + name + "')");
 			ResultSet ids = estmt.getGeneratedKeys();
 			while (ids.next()) {
 				id = ids.getInt(1);
 			}
 			ids.close();
			ers.close();
 			estmt.close();
 			conn.close();
 
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return id;
 	}
 
 	public int createQuestion(Question question) {
 		int id = 0;
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement estmt = conn.createStatement();
 
			ResultSet ers = estmt.executeUpdate("INSERT INTO question(type, question, enquete_id) VALUES('" + question.getType() + "', '" + question.getQuestion() + "', '" + question.getEnquete().getId() + "')");
 			ResultSet ids = estmt.getGeneratedKeys();
 			while (ids.next()) {
 				id = ids.getInt(1);
 			}
 			ids.close();
			ers.close();
 			estmt.close();
 			conn.close();
 
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return id;
 	}
 
 	public int createChoice(Choice choice) {
 		int id = 0;
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement estmt = conn.createStatement();
 
			ResultSet ers = estmt.executeUpdate("INSERT INTO choice(question_id, choice) VALUES('" + choice.getQuestion().getId() + "', '" + choice.getChoice() + "')");
 			ResultSet ids = estmt.getGeneratedKeys();
 			while (ids.next()) {
 				id = ids.getInt(1);
 			}
 			ids.close();
			ers.close();
 			estmt.close();
 			conn.close();
 
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return id;
 	}
 
 	public List<Enquete> getAllEnquetes()
 	{
 		if (this.enquetes.isEmpty()) {
 			try {
 				Context initContext = new InitialContext();
 				DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 				Connection conn = ds.getConnection();
 				Statement estmt = conn.createStatement();
 
 				ResultSet ers = estmt.executeQuery("SELECT id, name FROM enquete");
 				while (ers.next()) {
 					Enquete enquete = new Enquete(ers.getInt(1), ers.getString(2));
 					this.enquetes.add(enquete);
 				}
 				ers.close();
 				estmt.close();
 				conn.close();
 			} catch (NamingException ne) {
 				System.out.println("Datasource niet gevonden! "+ne);
 			} catch (SQLException sql) {
 				System.out.println("Fout in sql! "+ sql);
 			}
 		}
 		return this.enquetes;
 	}
 
 	public List<Enquete> getAllEnquetesExcludingFinished(String username)
 	{
 		List<Enquete> enquetes = new ArrayList<Enquete>();
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement estmt = conn.createStatement();
 
 			ResultSet ers = estmt.executeQuery("SELECT id, name FROM enquete WHERE enquete.id NOT IN(SELECT enquete_id FROM finished WHERE username = '" + username + "')");
 			while (ers.next()) {
 				Enquete enquete = new Enquete(ers.getInt(1), ers.getString(2));
 				enquetes.add(enquete);
 			}
 			ers.close();
 			estmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return enquetes;
 	}
 
 
 	public Enquete getEnqueteById(int id)
 	{
 		Enquete enquete = null;
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement estmt = conn.createStatement();
 			Statement qstmt = conn.createStatement();
 			Statement cstmt = conn.createStatement();
 
 			ResultSet ers = estmt.executeQuery("SELECT id, name FROM enquete WHERE id = " + id);
 			while (ers.next()) {
 				enquete = new Enquete(ers.getInt(1), ers.getString(2));
 				ResultSet qrs = qstmt.executeQuery("SELECT id, type, question FROM question WHERE enquete_id = " + ers.getInt(1));
 				while (qrs.next()) {
 					Question question = new Question(qrs.getInt(1), qrs.getInt(2), enquete, qrs.getString(3));
 					if (qrs.getInt(2) == 0) {
 						ResultSet crs = cstmt.executeQuery("SELECT id, choice FROM choice WHERE question_id = " + qrs.getInt(1));
 						while (crs.next()) {
 							Choice choice = new Choice(crs.getInt(1), question, crs.getString(2));
 							question.addChoice(choice);
 						}
 						crs.close();
 						cstmt.close();
 					}
 					enquete.addQuestion(question);
 				}
 				this.enquetes.add(enquete);
 				qrs.close();
 				qstmt.close();
 			}
 			ers.close();
 			estmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return enquete;
 	}
 
 	public List<Enquete> getFavoritesByUser(String username)
 	{
 		List<Enquete> favorites = new ArrayList<Enquete>();
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			ResultSet rs = stmt.executeQuery("SELECT enquete.id, enquete.name FROM favorite INNER JOIN enquete ON favorite.enquete_id = enquete.id WHERE favorite.username = '" + username + "' AND enquete.id NOT IN(SELECT enquete_id FROM finished WHERE username = '" + username + "')");
 			while (rs.next()) {
 				favorites.add(new Enquete(rs.getInt(1), rs.getString(2)));
 			}
 			rs.close();
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return favorites;
 	}
 
 	public boolean addFavoriteByUser(String username, int id)
 	{
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 			ResultSet srs = stmt.executeQuery("SELECT * FROM favorite WHERE username = '" + username + "' AND enquete_id = " + id);
 			if (srs.next()) {
 				srs.close();
 			} else {
 				srs.close();
 				stmt.executeUpdate("INSERT INTO favorite(username, enquete_id) VALUES('" + username + "'," + id + ")");
 			}
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 			return false;
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean removeFavoriteByUser(String username, int id)
 	{
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			stmt.executeUpdate("DELETE FROM favorite WHERE username = '" + username + "' AND enquete_id = " + id + "");
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 			return false;
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 			return false;
 		}
 		return true;
 	}
 
 	public int getIndexOfStartQuestion(String user, int id)
 	{
 		int index = 0;
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			ResultSet rs = stmt.executeQuery("SELECT answer.index FROM answer WHERE answer.username = '" + user + "' AND answer.enquete_id = " + id + " ORDER BY answer.index DESC");
 			if (rs.next()) {
 				System.out.println(rs.getInt(1));
 				index = rs.getInt(1) + 1;
 			}
 			rs.close();
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return index;
 	}
 
 	public boolean saveAnswer(Answer answer)
 	{
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 			ResultSet srs = stmt.executeQuery("SELECT id FROM answer WHERE username = '" + answer.getUsername() + "' AND enquete_id = " + answer.getEnquete().getId() + " AND question_id = " + answer.getQuestion().getId());
 			if (srs.next()) {
 				int id = srs.getInt(1);
 				srs.close();
 				stmt.executeUpdate("UPDATE answer SET answer = '" + answer.getAnswer() + "', extra = '" + answer.getExtra() + "' WHERE id = " + id);
 			} else {
 				srs.close();
 				stmt.executeUpdate("INSERT INTO answer(question_id, enquete_id, answer.index, username, answer, extra) VALUES(" + answer.getQuestion().getId() + "," + answer.getEnquete().getId() + "," + answer.getIndex() + ",'" + answer.getUsername() + "','" + answer.getAnswer() + "','" + answer.getExtra() + "')");
 			}
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 			return false;
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 			return false;
 		}
 		return true;
 	}
 
 	public String getOldGivenAnswer(String username, int enquete_id, int question_id)
 	{
 		String answer = "";
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			ResultSet rs = stmt.executeQuery("SELECT answer.answer FROM answer WHERE answer.username = '" + username + "' AND answer.enquete_id = " + enquete_id + " AND answer.question_id = " + question_id);
 			if (rs.next()) {
 				answer = rs.getString(1);
 			}
 			rs.close();
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return answer;
 	}
 
 	public boolean finishEnquete(String username, int enquete_id)
 	{
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 			ResultSet srs = stmt.executeQuery("SELECT * FROM finished WHERE username = '" + username + "' AND enquete_id = " + enquete_id);
 			if (srs.next()) {
 				srs.close();
 			} else {
 				srs.close();
 				stmt.executeUpdate("INSERT INTO finished(username, enquete_id) VALUES('" + username + "'," + enquete_id + ")");
 			}
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 			return false;
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 			return false;
 		}
 		return true;
 	}
 
 	public List<Enquete> getFinishedByUser(String username)
 	{
 		List<Enquete> finished = new ArrayList<Enquete>();
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			ResultSet rs = stmt.executeQuery("SELECT enquete.id, enquete.name FROM finished INNER JOIN enquete ON finished.enquete_id = enquete.id WHERE finished.username = '" + username + "' ORDER BY enquete.id");
 			while (rs.next()) {
 				finished.add(new Enquete(rs.getInt(1), rs.getString(2)));
 			}
 			rs.close();
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return finished;
 	}
 
 	public List<ResultHolder> getResultsForEnquete(Enquete enquete)
 	{
 		List<ResultHolder> results = new ArrayList<ResultHolder>();
 		List<Question> questions = enquete.getQuestions();
 
 		try {
 			Context initContext = new InitialContext();
 			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/EnqueteDB");
 			Connection conn = ds.getConnection();
 			Statement stmt = conn.createStatement();
 
 			for (Question q : questions) {
 				if (q.getType() == 0) {
 					ResultHolder holder = new ResultHolder();
 					holder.setQuestion(q.getQuestion());
 					holder.setType(0);
 
 					List<Choice> choices = q.getChoices();
 					int last_id = choices.get(choices.size() -1).getId();
 
 					ResultSet rs = stmt.executeQuery("SELECT answer FROM answer WHERE question_id = " + q.getId() + " AND enquete_id = " + enquete.getId() + " AND username IN(SELECT username FROM finished WHERE enquete_id = " + enquete.getId() + ")");
 					int[] total = new int[(last_id + 1)];
 					Arrays.fill(total, 0);
 					int count = 0;
 					while (rs.next()) {
 						total[rs.getInt(1)] += 1;
 						count++;
 					}
 					rs.close();
 					String percentage = "";
 					for (Choice c : choices) {
 						percentage += (((double) ((double) total[c.getId()] / count)) * 100) + "% " + c.getChoice() + " ";
 					}
 					holder.setPercentage(percentage);
 					results.add(holder);
 				}
 				if (q.getType() == 2) {
 					ResultHolder holder = new ResultHolder();
 					holder.setQuestion(q.getQuestion());
 					holder.setType(2);
 
 					ResultSet rs = stmt.executeQuery("SELECT answer FROM answer WHERE question_id = " + q.getId() + " AND enquete_id = " + enquete.getId() + " AND username IN(SELECT username FROM finished WHERE enquete_id = " + enquete.getId() + ")");
 					int total = 0;
 					int count = 0;
 					while (rs.next()) {
 						total += rs.getInt(1);
 						count++;
 					}
 					rs.close();
 					double average = (double) (total / count);
 					holder.setAverage(average);
 					results.add(holder);
 				}
 			}
 
 			stmt.close();
 			conn.close();
 		} catch (NamingException ne) {
 			System.out.println("Datasource niet gevonden! "+ne);
 		} catch (SQLException sql) {
 			System.out.println("Fout in sql! "+ sql);
 		}
 		return results;
 	}
 }
