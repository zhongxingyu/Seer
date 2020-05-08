 package cl.own.usi.service.impl;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import cl.own.usi.dao.ScoreDAO;
 import cl.own.usi.dao.UserDAO;
 import cl.own.usi.exception.UserAlreadyLoggedException;
 import cl.own.usi.model.Answer;
 import cl.own.usi.model.AuditAnswer;
 import cl.own.usi.model.AuditAnswers;
 import cl.own.usi.model.User;
 import cl.own.usi.service.UserService;
 
 @Service
 public class UserServiceImpl implements UserService {
 
 	@Autowired
 	private UserDAO userDAO;
 
 	@Autowired
 	private ScoreDAO scoreDAO;
 
 	@Override
 	public boolean insertUser(String email, String password, String firstname,
 			String lastname) {
 
 		if (email == null || password == null || firstname == null
 				|| lastname == null) {
 			throw new IllegalArgumentException("an argument is null.");
 		} else {
 			User user = new User();
 			user.setEmail(email);
 			user.setPassword(password);
 			user.setFirstname(firstname);
 			user.setLastname(lastname);
 			return userDAO.insertUser(user);
 		}
 	}
 
 	public String login(String email, String password)
 			throws UserAlreadyLoggedException {
 
 		if (email == null || password == null) {
 			return null;
 		} else {
 			return userDAO.login(email, password);
 		}
 	}
 
 	public void insertRequest(String userId, int questionNumber) {
 		userDAO.insertRequest(userId, questionNumber);
 	}
 
 	public void insertAnswer(String userId, int questionNumber,
 			Integer answerNumber) {
 
 		// List<Answer> answers = userDAO.getAnswers(userId);
 		//
 		// if (answers.size() >= questionNumber
 		// && answers.get(questionNumber - 1) != null) {
 		// throw new IllegalArgumentException(
 		// "User has already answered this question.");
 		// } else {
 		Answer answer = new Answer();
 		answer.setQuestionNumber(questionNumber);
 		answer.setUserId(userId);
 		answer.setAnswerNumber(answerNumber);
 		userDAO.insertAnswer(answer);
 		// }
 	}
 
 	@Override
 	public boolean logout(String userId) {
 		User user = getUserFromUserId(userId);
 		if (user == null) {
 			return false;
 		} else {
 			userDAO.logout(userId);
 			return true;
 		}
 	}
 
 	@Override
 	public User getUserFromUserId(String userId) {
 		return userDAO.getUserById(userId);
 	}
 
 	@Override
 	public void flushUsers() {
 		userDAO.flushUsers();
 		scoreDAO.flushUsers();
 	}
 
 	@Override
 	public AuditAnswers getAuditAnswers(final String userEmail,
 			final List<Integer> goodAnswers) {
 
 		// TODO: what if the user cannot be found?
 		final List<Answer> answers = userDAO.getAnswersByEmail(userEmail);
 		Collections.sort(answers, new AnswerByNumberComparator());
 
 		final AuditAnswers auditAnswers = new AuditAnswers(goodAnswers);
 
 		for (Answer answer : answers) {
			auditAnswers.getUserAnswers().add(answer.getAnswerNumber());
 		}
 
 		return auditAnswers;
 	}
 
 	@Override
 	public AuditAnswer getAuditAnswerFor(final String userEmail,
 			final int questionNumber, final String question,
 			final int goodAnswer) {
 
 		// TODO: what if the user cannot be found?
 		final List<Answer> answers = userDAO.getAnswersByEmail(userEmail);
 
 		for (Answer answer : answers) {
 			if (answer.getQuestionNumber() == questionNumber) {
 				return new AuditAnswer(answer.getAnswerNumber(), goodAnswer,
 						question);
 			}
 		}
 
 		// TODO: handle this case correctly
 		return null;
 	}
 
 	private static class AnswerByNumberComparator implements Comparator<Answer> {
 
 		@Override
 		public int compare(final Answer answer1, final Answer answer2) {
 			// TODO: handle null correctly
 
			final int a1 = answer1.getAnswerNumber();
			final int a2 = answer2.getAnswerNumber();
 
 			if (a1 < a2) {
 				return -1;
 			} else if (a1 > a2) {
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 	}
 
 	@Override
 	public void gameCreated() {
 		userDAO.gameCreated();
 	}
 }
