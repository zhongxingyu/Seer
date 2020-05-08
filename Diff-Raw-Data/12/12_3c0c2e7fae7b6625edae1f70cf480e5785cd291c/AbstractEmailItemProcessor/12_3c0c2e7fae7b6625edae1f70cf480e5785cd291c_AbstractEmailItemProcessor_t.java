 /**
  * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
  * under the Apache License Version 2.0 (release version 0.7.0)
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *                   Copyright (c) Hoteia, 2012-2013
  * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
  *
  */
 package fr.hoteia.qalingo.app.notification.job.email;
 
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.sql.Blob;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.batch.item.ItemProcessor;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.util.Assert;
 
 import fr.hoteia.qalingo.core.batch.CommonProcessIndicatorItemWrapper;
 import fr.hoteia.qalingo.core.dao.EmailDao;
 import fr.hoteia.qalingo.core.domain.Email;
 import fr.hoteia.qalingo.core.util.impl.MimeMessagePreparatorImpl;
 
 
 /**
  * 
  */
 public abstract class AbstractEmailItemProcessor<T> implements ItemProcessor<CommonProcessIndicatorItemWrapper<Long, Email>, Email>, InitializingBean {
 
 	private final Logger LOG = LoggerFactory.getLogger(getClass());
 
 	private JavaMailSender mailSender;
 	
 	protected EmailDao emailDao;
 
 	public final void afterPropertiesSet() throws Exception {
 		Assert.notNull(mailSender, "You must provide a JavaMailSender.");
 		Assert.notNull(emailDao, "You must provide an EmailDao.");
 	}
 
 	public Email process(CommonProcessIndicatorItemWrapper<Long, Email> wrapper) throws Exception {
 		Email email = wrapper.getItem();
 		Blob emailcontent = email.getEmailContent();
 		
 		InputStream is = emailcontent.getBinaryStream();
 	    ObjectInputStream oip = new ObjectInputStream(is);
 	    Object object = oip.readObject();
 	    
 	    MimeMessagePreparatorImpl mimeMessagePreparator = (MimeMessagePreparatorImpl) object;
 	    
 	    oip.close();
 	    is.close();
 
 	    try {
 	    	// SANITY CHECK
 	    	if(email.getStatus().equals(Email.EMAIl_STATUS_PENDING)){
 				mailSender.send(mimeMessagePreparator);
 				email.setStatus(Email.EMAIl_STATUS_SENDED);
 	    	} else {
 	    		LOG.warn("Batch try to send email was already sended!");
 	    	}
             
         } catch (Exception e) {
         	LOG.error("Fail to send email! Exception is save in database, id:" + email.getId());
     		email.setStatus(Email.EMAIl_STATUS_ERROR);
     		emailDao.handleEmailException(email, e);
         }
 
 	    return email;
     }
 	
 	public void setMailSender(JavaMailSender mailSender) {
 	    this.mailSender = mailSender;
     }

	public void setEmailDao(EmailDao emailDao) {
	    this.emailDao = emailDao;
    }
 	
 }
