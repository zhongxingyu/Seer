 package in.co.zybotech.mailer.bulk;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.springframework.mail.MailException;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class Mailer {
 	private Logger logger;
 
 	private JavaMailSender mailSender;
 	private List<String> fields;
 	private String from;
 	private String subject;
 	private String html;
 	private String plain;
 
 	private int emailIndex;
 
 	public void setLogger(Logger logger) {
 		this.logger = logger;
 	}
 
 	public void setSender(JavaMailSender mailSender) {
 		this.mailSender = mailSender;
 	}
 
 	public void setFields(List<String> fields) {
 		this.fields = fields;
 		if (fields != null) {
 			this.emailIndex = fields.indexOf("email");
 		}
 	}
 
 	public void setFrom(String from) {
 		this.from = from;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 
 	}
 
 	public void html(String html) {
 		this.html = html;
 	}
 
 	public void plain(String plain) {
 		this.plain = plain;
 	}
 
 	public void send(File source) throws IOException {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(source));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				try {
 					final Record record = getRecord(line);
 					MimeMessagePreparator preparator = new MimeMessagePreparator() {
 						public void prepare(MimeMessage mimeMessage)
 								throws Exception {
 							MimeMessageHelper messageHelper = new MimeMessageHelper(
 									mimeMessage, true, "UTF-8");
 							messageHelper.setSubject(subject);
 							messageHelper.setTo(record.getEmail());
 							messageHelper.setFrom(from);
							if (StringUtils.isBlank(plain)
									&& StringUtils.isBlank(html)) {
 								messageHelper.setText(
 										processMessage(plain, record),
 										processMessage(html, record));
							} else if (StringUtils.isBlank(plain)) {
 								messageHelper.setText(processMessage(plain,
 										record));
							} else if (StringUtils.isBlank(html)) {
 								messageHelper.setText(
 										processMessage(html, record), true);
 							}
 						}
 					};
 
 					this.mailSender.send(preparator);
 				} catch (MailException ex) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("Error while sending mail to source: "
 								+ line, ex);
 					} else {
 						logger.error("Error while sending mail to source: {}"
 								+ line + ", " + ex.toString());
 					}
 				}
 			}
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 	}
 
 	private String processMessage(String message, Record record)
 			throws IOException, TemplateException {
 		Configuration configuration = new Configuration();
 		configuration.setObjectWrapper(new DefaultObjectWrapper());
 		Template template = new Template("message", new StringReader(message),
 				configuration);
 		return FreeMarkerTemplateUtils.processTemplateIntoString(template,
 				record.getModel());
 	}
 
 	private Record getRecord(String line) {
 		Record record = new Record();
 		if (org.springframework.util.CollectionUtils.isEmpty(fields)) {
 			record.setEmail(line);
 		} else {
 
 			String[] split = StringUtils.split(line, ",");
 			if (split.length <= this.emailIndex
 					&& StringUtils.isNotBlank(split[this.emailIndex])) {
 				throw new IllegalArgumentException(
 						"Unable to find email field in {" + line + "}.");
 			}
 			record.setEmail(StringUtils.trimToEmpty(split[this.emailIndex]));
 
 			for (int i = 0; i < fields.size(); i++) {
 				if (i >= split.length) {
 					break;
 				}
 				record.put(StringUtils.trimToEmpty(fields.get(i)),
 						StringUtils.trimToEmpty(split[i]));
 			}
 		}
 
 		return record;
 	}
 
 }
