 package be.noselus.repository;
 
 import be.noselus.db.DatabaseHelper;
 import be.noselus.model.Eurovoc;
 import be.noselus.model.Question;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class QuestionRepositoryInDatabase implements QuestionRepository {
 
     private static final Logger logger = LoggerFactory.getLogger(QuestionRepositoryInDatabase.class);
     private final AssemblyRegistry assemblyRegistry;
 
     private QuestionMapper mapper;
 
     @Inject
     public QuestionRepositoryInDatabase(final AssemblyRegistry assemblyRegistry) {
         this.assemblyRegistry = assemblyRegistry;
         mapper = new QuestionMapper(this.assemblyRegistry);
     }
 
     @Override
     public List<Question> getQuestions() {
         List<Question> results = Lists.newArrayList();
         Map<Integer, Question> tempQuestionMapper = new TreeMap<>();
         Map<Integer, Eurovoc> eurovocMappers = new TreeMap<>();
 
         try {
             Connection db = DatabaseHelper.getInstance().getConnection(false, true);
             PreparedStatement stat = db.prepareStatement("SELECT * FROM written_question ORDER BY date_asked DESC LIMIT 50;");
 
             stat.execute();
 
             while (stat.getResultSet().next()) {
                 final Question question = mapper.map(stat.getResultSet());
                 results.add(question);
                 tempQuestionMapper.put(question.id, question);
             }
 
             PreparedStatement eurovocs = db.prepareStatement("SELECT "
             		+ "written_question_eurovoc.id_written_question as written_question_id, "
             		+ "eurovoc.label as eurovoc_label, "
             		+ "eurovoc.id as eurovoc_id "
             		+ "FROM written_question_eurovoc "
             		+ "join eurovoc on written_question_eurovoc.id_eurovoc = eurovoc.id"
             		+ "");
 
             eurovocs.execute();
 
             while(eurovocs.getResultSet().next()){
             	Integer written_question_id = eurovocs.getResultSet().getInt("written_question_id");
             	String written_question_label = eurovocs.getResultSet().getString("eurovoc_label");
             	Integer eurovoc_id = eurovocs.getResultSet().getInt("eurovoc_id");
 
             	Eurovoc eurovoc;
 
             	if (eurovocMappers.get(eurovoc_id) == null) {
             		eurovoc = new Eurovoc(eurovoc_id, written_question_label);
             		eurovocMappers.put(eurovoc.id, eurovoc);
             	} else {
             		eurovoc = eurovocMappers.get(eurovoc_id);
             	}
 
             	if (tempQuestionMapper.get(written_question_id) != null) {
             		Question q = tempQuestionMapper.get(written_question_id);
             		q.addEurovoc(eurovoc);
             	}
 
 
             }
             eurovocs.close();
             stat.close();
             db.close();
 
         } catch (SQLException e) {
             logger.error("Error loading person from DB", e);
         }
         return results;
     }
 
     @Override
     public Question getQuestionById(final int id) {
         Question result = null;
         try {
             Connection db = DatabaseHelper.getInstance().getConnection(false, true);
             PreparedStatement stat = db.prepareStatement("SELECT * FROM written_question WHERE id = ?;");
 
             stat.setInt(1, id);
             stat.execute();
             stat.getResultSet().next();
 
             result = mapper.map(stat.getResultSet());
 
             this.addEurovocsToQuestion(result, db);
 
             stat.close();
             db.close();
 
         } catch (SQLException e) {
             logger.error("Error loading person from DB", e);
         }
 
         return result;
     }
 
     @Override
     public List<Question> searchByKeyword(final String... keywords) {
         List<Question> results = Lists.newArrayList();
         try {
             Connection db = DatabaseHelper.getInstance().getConnection(false, true);
             final StringBuffer sql = new StringBuffer("SELECT * FROM written_question WHERE lower(title) LIKE ");
             for (int i = 0; i < keywords.length; i++) {
                 String keyword = keywords[i];
                 sql.append("lower('%");
                 sql.append(keyword);
                 sql.append("%')");
                 if (i < keywords.length - 1){
                    sql.append(" OR title LIKE  ");
                 }
 
             }
             sql.append(";");
             PreparedStatement stat = db.prepareStatement(sql.toString());
 
             stat.execute();
 
             while (stat.getResultSet().next()) {
                 final Question question = mapper.map(stat.getResultSet());
                 this.addEurovocsToQuestion(question, db);
                 results.add(question);
             }
 
             stat.close();
             db.close();
 
         } catch (SQLException e) {
             logger.error("Error loading person from DB", e);
         }
         return results;
     }
 
     @Override
     public List<Integer> questionIndexAskedBy(final int askedById) {
         try {
             Connection db = DatabaseHelper.getInstance().getConnection(false, true);
             PreparedStatement questionsStat = db.prepareStatement("SELECT id FROM written_question WHERE asked_by = ?;");
             questionsStat.setInt(1, askedById);
 
             questionsStat.execute();
             List<Integer> questionsAskedBy = Lists.newArrayList();
             while (questionsStat.getResultSet().next()){
                 questionsAskedBy.add(questionsStat.getResultSet().getInt("id"));
             }
             questionsStat.close();
             return questionsAskedBy;
         } catch (SQLException e) {
             logger.error("Error loading questions asked by " + askedById, e);
         }
         return Collections.emptyList();
     }
 
 
     private void addEurovocsToQuestion(Question q, Connection db) {
     	try {
     		PreparedStatement stat = db.prepareStatement("SELECT label, id from eurovoc JOIN written_question_eurovoc "
     				+ "ON written_question_eurovoc.id_eurovoc = eurovoc.id "
     				+ "WHERE written_question_eurovoc.id_written_question = ? ");
     		stat.setInt(1, q.id);
 
     		stat.execute();
 
     		while(stat.getResultSet().next()){
     			String label  = stat.getResultSet().getString("label");
     			Integer eurovoc_id = stat.getResultSet().getInt("id");
 
     			Eurovoc eurovoc = new Eurovoc(eurovoc_id, label);
 
     			q.addEurovoc(eurovoc);
     		}
 
     		stat.close();
     	} catch (SQLException e) {
     		logger.error("ERROR while loading eurovocs from question "+ q.id.toString(), e );
     	}
     }
 
 
 	@Override
 	public List<Question> questionAskedBy(int askedById) {
         try {
             Connection db = DatabaseHelper.getInstance().getConnection(false, true);
             PreparedStatement questionsStat = db.prepareStatement("SELECT * FROM written_question WHERE asked_by = ?;");
             questionsStat.setInt(1, askedById);
 
             questionsStat.execute();
             List<Question> questionsAskedBy = Lists.newArrayList();
 
             QuestionMapper mapper = new QuestionMapper(assemblyRegistry);
 
             while (questionsStat.getResultSet().next()){
             	Question q = mapper.map(questionsStat.getResultSet());
             	this.addEurovocsToQuestion(q, db);
                 questionsAskedBy.add(q);
             }
             questionsStat.close();
             return questionsAskedBy;
         } catch (SQLException e) {
             logger.error("Error loading questions asked by " + askedById, e);
         }
         return Collections.emptyList();
 	}
 
 	@Override
 	public List<Question> questionAssociatedToEurovoc(int eurovocId) {
 		try {
 			Connection db = DatabaseHelper.getInstance().getConnection(false, true);
 			PreparedStatement questionsStat = db.prepareStatement(""
 					+ "SELECT * from written_question "
 					+ "JOIN written_question_eurovoc "
 					+ "ON written_question_eurovoc.id_written_question = written_question.id "
 					+ "WHERE written_question_eurovoc.id_eurovoc = ? ");
 			questionsStat.setInt(1, eurovocId);
 			questionsStat.execute();
 
 			List<Question> questionAssociatedToEurovoc = Lists.newArrayList();
 
 			QuestionMapper mapper = new QuestionMapper(this.assemblyRegistry);
 
 			while (questionsStat.getResultSet().next()) {
 				Question q = mapper.map(questionsStat.getResultSet());
 				this.addEurovocsToQuestion(q, db);
 				questionAssociatedToEurovoc.add(q);
 			}
 
 			questionsStat.close();
 
 			return questionAssociatedToEurovoc;
 
 		} catch (SQLException e) {
 			logger.error("Error loading questions asked by " + eurovocId, e);
 		}
 		return Collections.emptyList();
 	}
 
     @Override
     public void insertOrUpdateQuestion(final Question question) {
         Connection db = null;
         try {
             db = DatabaseHelper.getInstance().getConnection(false, true);
             if (questionIsPresent(db, question)) {
                 updateQuestion(db, question);
             } else {
                 insertQuestion(db, question);
             }
             db.commit();
             db.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     private void insertQuestion(final Connection db, final Question question) throws SQLException {
         logger.debug("Inserting question " + question.assembly.getLabel() + " " + question.assembly_ref);
 
         String sql =
                 "INSERT INTO written_question (session, year, number, date_asked, date_answer, title, question_text, answer_text, asked_by, asked_to, assembly_ref, assembly_id) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
         PreparedStatement stat = db.prepareStatement(sql);
         int idx = 1;
         stat.setString(idx++, question.session);
         stat.setInt(idx++, question.year);
         stat.setString(idx++, question.number);
         stat.setDate(idx++, new java.sql.Date(question.date_asked.toDate().getTime()));
         if (question.date_answered != null) {
             stat.setDate(idx++, new java.sql.Date(question.date_answered.toDate().getTime()));
         } else {
             stat.setNull(idx++, java.sql.Types.DATE);
         }
         stat.setString(idx++, question.title);
         stat.setString(idx++, question.question_text);
         stat.setString(idx++, question.answer_text);
         stat.setInt(idx++, question.asked_by);
         stat.setInt(idx++, question.asked_to);
         stat.setString(idx++, question.assembly_ref);
         stat.setInt(idx++, question.assembly.getId());
 
         stat.execute();
         stat.close();
     }
 
     private void updateQuestion(final Connection db, final Question question) throws SQLException {
         logger.debug("Updating question " + question.assembly.getLabel() + " " + question.assembly_ref);
 
         String sql =
                 "UPDATE written_question SET session = ?,  year = ? , number = ?, date_asked = ?, date_answer = ?, title = ?, question_text = ?, answer_text = ?, asked_by = ?, asked_to = ? "
                         + "WHERE assembly_ref = ? AND assembly_id = ?";
         PreparedStatement stat = db.prepareStatement(sql);
         int idx = 1;
         stat.setString(idx++, question.session);
         stat.setInt(idx++, question.year);
         stat.setString(idx++, question.number);
         stat.setDate(idx++, new java.sql.Date(question.date_asked.toDate().getTime()));
         if (question.date_answered != null) {
             stat.setDate(idx++, new java.sql.Date(question.date_answered.toDate().getTime()));
         } else {
             stat.setNull(idx++, java.sql.Types.DATE);
         }
         stat.setString(idx++, question.title);
         stat.setString(idx++, question.question_text);
         stat.setString(idx++, question.answer_text);
         stat.setInt(idx++, question.asked_by);
         stat.setInt(idx++, question.asked_to);
         stat.setString(idx++, question.assembly_ref);
         stat.setInt(idx++, question.assembly.getId());
 
         stat.execute();
         stat.close();
     }
 
     private boolean questionIsPresent(final Connection db, final Question question) throws SQLException {
         logger.debug("Checking if question is present " + question.assembly.getLabel() + " " + question.assembly_ref);
         String sql = "SELECT id FROM written_question WHERE assembly_ref = ? AND assembly_id = ?";
         PreparedStatement stat = db.prepareStatement(sql);
         int idx = 1;
         stat.setString(idx++, question.assembly_ref);
         stat.setInt(idx++, question.assembly.getId());
         stat.execute();
 
         final boolean next = stat.getResultSet().next();
         stat.close();
         return next;
     }
 
 }
