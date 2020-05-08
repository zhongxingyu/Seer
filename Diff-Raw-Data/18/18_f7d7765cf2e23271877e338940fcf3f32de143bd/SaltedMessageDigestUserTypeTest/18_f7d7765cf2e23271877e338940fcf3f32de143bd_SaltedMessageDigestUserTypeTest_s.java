 /* Copyright 2008-2011 NetRadius, LLC
  * Licensed under the Simplified BSD License
  * You may obtain a copy of this license at http://netradius.com/opensource/simplified-bsd-license
  * http://netradius.com/opensource/hibernate-support
  */
 package com.netradius.hibernate.support;
 
 import com.netradius.commons.bitsnbytes.BitTwiddler;
 import org.apache.commons.dbutils.handlers.BeanHandler;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.annotations.Columns;
 import org.hibernate.annotations.Parameter;
 import org.hibernate.annotations.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import java.io.Serializable;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 
 import static com.netradius.hibernate.support.HibernateUtil.*;
 import static org.testng.Assert.assertEquals;
 
 /**
  * Unit tests for SalesMessageDigestUserType.
  * 
  * @author Erik R. Jensen
  */
 public class SaltedMessageDigestUserTypeTest extends UnitTest {
 
 	private static final Logger log = LoggerFactory.getLogger(SaltedMessageDigestUserTypeTest.class);
 	private static final String TXT = "Lock s-foils in attack position.";
 	private static final Charset UTF8 = Charset.forName("UTF8");
 
 	@BeforeClass
 	public void classSetup() {
 		init(Data.class);
 	}
 
 	@AfterClass
 	public void classTearDown() {
 		close();
 	}
 
 	@Test
 	public void test() throws SQLException, NoSuchAlgorithmException {
 		MessageDigest mdMd5 = MessageDigest.getInstance("MD5");
 		MessageDigest mdSha1 = MessageDigest.getInstance("SHA1");
 
 		Session ses = getSession();
 		Transaction tx = ses.beginTransaction();
 		Data data = new Data(TXT);
 		ses.save(data);
 		tx.commit();
 		Record rec = getRecord(data.getId());
		String md5 = BitTwiddler.tohexstr(mdMd5.digest((TXT + "{" + rec.getMd5salt() + "}").getBytes(UTF8)));
		String sha1 = BitTwiddler.tohexstr(mdSha1.digest((TXT + "{" + rec.getSha1salt() + "}").getBytes(UTF8)));
 		log.info(rec.toString());
 		assertEquals(rec.getMd5(), md5);
 		assertEquals(rec.getSha1(), sha1);
 
 		ses = getSession();
 		tx = ses.beginTransaction();
 		data = (Data)ses.get(Data.class, data.getId());
 		log.info(data.toString());
 		data.setText("test");
 		ses.save(data);
 		tx.commit();
 		rec = getRecord(data.getId());
 		log.info(rec.toString());
 		assertEquals(rec.getMd5(), md5);
 		assertEquals(rec.getSha1(), sha1);
 	}
 
 	private Record getRecord(Long id) throws SQLException {
 		return query("SELECT * FROM data WHERE id = ?", new BeanHandler<Record>(Record.class), id);
 	}
 
 	@Entity
 	@Table(name = "data")
 	@org.hibernate.annotations.Entity(dynamicUpdate = true)
 	public static class Data implements Serializable {
 
 		public Data() {}
 
 		public Data(String text) {
 			this.text = text;
 			this.md5 = text;
 			this.sha1 = text;
 		}
 
 		@Id
 		@GeneratedValue(strategy = GenerationType.AUTO)
 		@Column(name = "id")
 		private Long id;
 
 		@Column(name = "text")
 		private String text;
 
 		@Columns(columns = {
 				@Column(name = "md5"),
 				@Column(name = "md5salt")
 		})
 		@Type(type = SaltedMessageDigestUserType.TYPE,
 				parameters = @Parameter(name = SaltedMessageDigestUserType.PARAM_ALGORITHM, value = "MD5"))
 		private String md5;
 
 		@Column(name = "md5salt", updatable = false, insertable = false)
 		private String md5salt;
 
 		@Columns(columns = {
 				@Column(name = "sha1"),
 				@Column(name = "sha1salt")
 		})
 		@Type(type = SaltedMessageDigestUserType.TYPE,
 				parameters = @Parameter(name = SaltedMessageDigestUserType.PARAM_ALGORITHM, value = "SHA1"))
 		private String sha1;
 
 		@Column(name = "sha1Salt", updatable = false, insertable = false)
 		private String sha1salt;
 
 		public Long getId() {
 			return id;
 		}
 
 		public void setId(Long id) {
 			this.id = id;
 		}
 
 		public String getText() {
 			return text;
 		}
 
 		public void setText(String text) {
 			this.text = text;
 		}
 
 		public String getMd5() {
 			return md5;
 		}
 
 		public void setMd5(String md5) {
 			this.md5 = md5;
 		}
 
 		public String getMd5salt() {
 			return md5salt;
 		}
 
 		public void setMd5salt(String md5salt) {
 			this.md5salt = md5salt;
 		}
 
 		public String getSha1() {
 			return sha1;
 		}
 
 		public void setSha1(String sha1) {
 			this.sha1 = sha1;
 		}
 
 		public String getSha1salt() {
 			return sha1salt;
 		}
 
 		public void setSha1salt(String sha1salt) {
 			this.sha1salt = sha1salt;
 		}
 
 		@Override
 		public String toString() {
 			return new StringBuilder("Data :: ID: ").append(id)
 					.append(" Text: ").append(text)
 					.append(" MD5: ").append(md5)
 					.append(" MD5 Salt: ").append(md5salt)
 					.append(" SHA1: ").append(sha1)
 					.append(" SHA1 Salt: ").append(sha1salt)
 					.toString();
 		}
 	}
 
 	public static class Record {
 
 		private Long id;
 		private String text;
 		private String md5;
 		private String md5salt;
 		private String sha1;
 		private String sha1salt;
 
 		public Long getId() {
 			return id;
 		}
 
 		public void setId(Long id) {
 			this.id = id;
 		}
 
 		public String getText() {
 			return text;
 		}
 
 		public void setText(String text) {
 			this.text = text;
 		}
 
 		public String getMd5() {
 			return md5;
 		}
 
 		public void setMd5(String md5) {
 			this.md5 = md5;
 		}
 
 		public String getMd5salt() {
 			return md5salt;
 		}
 
 		public void setMd5salt(String md5salt) {
 			this.md5salt = md5salt;
 		}
 
 		public String getSha1() {
 			return sha1;
 		}
 
 		public void setSha1(String sha1) {
 			this.sha1 = sha1;
 		}
 
 		public String getSha1salt() {
 			return sha1salt;
 		}
 
 		public void setSha1salt(String sha1salt) {
 			this.sha1salt = sha1salt;
 		}
 
 		@Override
 		public String toString() {
 			return new StringBuilder("Record :: ID: ").append(id)
 					.append(" Text: ").append(text)
 					.append(" MD5: ").append(md5)
 					.append(" MD5 Salt: ").append(md5salt)
 					.append(" SHA1: ").append(sha1)
 					.append(" SHA1 Salt: ").append(sha1salt)
 					.toString();
 		}
 	}
 }
