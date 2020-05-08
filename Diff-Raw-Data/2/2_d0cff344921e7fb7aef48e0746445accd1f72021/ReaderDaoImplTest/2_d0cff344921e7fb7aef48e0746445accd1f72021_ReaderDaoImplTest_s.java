 package com.tsekhan.rssreader.dao;
 
 import com.tsekhan.rssreader.persistance.Account;
 import com.tsekhan.rssreader.persistance.Channel;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author Mikola Tsekhan <tsekhan@gmail.com>
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:/applicationContext.xml")
 public class ReaderDaoImplTest {
     
     @Autowired
     ReaderDao readerDao;
     
     private Channel getTestChannel(String channelLink) {
         Channel testChannel = new Channel();
         testChannel.setSourceLink(channelLink);
         return testChannel;
     }
     
     private Account getTestAccount(String accountLogin) {
         Account testAccount = new Account();
         testAccount.setAccountLogin(accountLogin);
         return testAccount;
     }
     
     @Test
     public void testAccountOperations() {
        readerDao.addAcount(getTestAccount("test_login"));
     }
 }
