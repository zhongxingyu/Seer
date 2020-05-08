 package com.bubble.db.subscription;
 
 import javax.inject.Inject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class JdbcSubsRepository implements SubsRepository {
 
 	private final static Logger logger = LoggerFactory.getLogger(SubsRepository.class);
 
 	private final JdbcTemplate jdbcTemplate;
 
 	@Inject
 	public
 	JdbcSubsRepository(JdbcTemplate jdbcTemplate) {
 		this.jdbcTemplate = jdbcTemplate;
 	}
 
 	public void createSubscription(int topic, String user, boolean subscribed){
		System.out.println("top"+topic + " " + "user" + user + " " + "subscr" + subscribed);
		jdbcTemplate.update("update subscriptions set subscribed=?, datecreated=now() " +
 				"where userid=? and topic=?;", 
 				subscribed, user, topic);
 		try {
 			jdbcTemplate.update(
 					"insert into subscriptions (topic, userid, subscribed) " +
 							"select ?, ?, ? where (?,?) not in (select topic, user from subscriptions);",
 							topic, user, subscribed, topic, user);
 		} catch (DuplicateKeyException e) {}
 		logger.debug(user + "subscribed/unsubcribed to/from the topic with id " + topic);
 	}
 
 	public boolean isSubscribed(String user, int topic) {
 		Boolean subscribed;
 		try {
 			subscribed = jdbcTemplate.queryForObject("SELECT subscribed FROM subscriptions " +
 					"WHERE userid = ? and topic=?;", 
 					Boolean.class, user, topic);
 		} catch (EmptyResultDataAccessException e) {
 			return false;
 		}
 		return (boolean) subscribed;
 	}
 }
