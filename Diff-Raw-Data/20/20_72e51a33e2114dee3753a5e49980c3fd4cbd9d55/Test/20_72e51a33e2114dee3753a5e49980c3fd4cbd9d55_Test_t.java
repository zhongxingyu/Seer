 package com.san.redis;
 
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Test {
 	public static void main(String[] args) {
 		BeanFactory factory = new ClassPathXmlApplicationContext("resources/applicationContext.xml");
 		UserRedisDao userRedisDao = (UserRedisDao)factory.getBean("userRedisDaoProxy");
 		userRedisDao.clearAll();
 		System.out.println(userRedisDao.logSignInAttempt(new CounterEntity(){{setEmailAddress("abcd@mail.com");}}));
		System.out.println(userRedisDao.logSignInAttempt(new CounterEntity(){{setEmailAddress("abcd@mail.com");}}));
		System.out.println(userRedisDao.logSignInAttempt(new CounterEntity(){{setEmailAddress("abcd@mail.com");}}));
		System.out.println(userRedisDao.logSignInAttempt(new CounterEntity(){{setEmailAddress("abcd@mail.com");}}));
 	}
 }
