 package com.katesoft.gserver.domain;
 
 import static org.junit.Assert.assertTrue;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.dao.DataAccessException;
 import org.springframework.data.redis.connection.RedisConnection;
 import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
 import org.springframework.data.redis.core.RedisCallback;
 import org.springframework.data.redis.core.StringRedisTemplate;
 
 public class DomainRepositoryTest {
     JedisConnectionFactory cf;
     StringRedisTemplate template;
     DomainRepository repo;
 
     @Before
     public void before() {
         cf = new JedisConnectionFactory();
         cf.afterPropertiesSet();
 
         template = new StringRedisTemplate( cf );
         template.afterPropertiesSet();
 
         repo = new DomainRepository( template );
         template.execute( new RedisCallback<Void>() {
             @Override
             public Void doInRedis(RedisConnection connection) throws DataAccessException {
                 connection.flushAll();
                 return null;
             }
         } );
     }
 
     @After
     public void after() {
         cf.destroy();
     }
 
     @Test
     public void addUserAccountWorks() {
         UserAccount acc = new UserAccount();
         acc.setFirstname( "gserver_firstname" );
         acc.setLastname( "gserver_lastname" );
         acc.setEmail( "gserver@gmail.com" );
         acc.setProvider( "facebook" );
         acc.setProviderUserId( String.valueOf( System.currentTimeMillis() ) );
         acc.setPassword( "gserver_password" );
         acc.setUsername( "gserver_username" );
 
         repo.saveUserAccount( acc );
         UserAccount copy = repo.findUserAccount( acc.getPrimaryKey() ).get();
         assertTrue( EqualsBuilder.reflectionEquals( acc, copy, false ) );
 
        repo.saveUserAccount( acc );
         repo.deleteUserAccount( acc );
     }
 }
