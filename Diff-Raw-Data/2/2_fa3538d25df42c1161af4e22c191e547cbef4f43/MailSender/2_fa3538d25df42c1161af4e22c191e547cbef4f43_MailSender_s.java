 package util;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.Date;
 
 import javax.annotation.Resource;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 
 import usersManagement.User;
 import businessLayer.Agreement;
 import businessLayer.AgreementInstallment;
 import businessLayer.Contract;
 import businessLayer.Installment;
 import daoLayer.UserDaoBean;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 @Named
 @SessionScoped
 public class MailSender implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Resource(lookup = "java:jboss/mail/JaMail")
 	private Session mailSession;
 
 	// @Inject
 	// @Logged
 	// private User loggedUser;
 
 	// TODO prevedere integrazione col LDAP!
 	@Inject
 	private UserDaoBean userDao;
 
 
 	@Deprecated
 	private void send(String recipientEmail, String subject, String text) {
 		//TODO eliminare
 		
 
 		// MimeMessage message = new MimeMessage(mailSession);
 		// try {
 		// message.setRecipient(RecipientType.TO, new
 		// InternetAddress(recipientEmail));
 		// message.setSubject(subject);
 		// message.setText(text);
 		// message.saveChanges();
 		//
 		// Transport.send(message);
 		//
 		// } catch (MessagingException e) {
 		// e.printStackTrace();
 		// }
 
 		if (recipientEmail != null) {
 
 			String host = "smtp.gmail.com";
 			String username = "jama.mail.services";
 			String password = "pastrullo";
 
 			MimeMessage message = new MimeMessage(mailSession);
 			try {
 
 				message.setRecipient(RecipientType.TO, new InternetAddress(recipientEmail));
 				message.setSubject(subject);
 				message.setText(text);
 				message.saveChanges();
 
 				Transport t = mailSession.getTransport("smtps");
 				try {
 					t.connect(host, username, password);
 					t.sendMessage(message, message.getAllRecipients());
 				} finally {
 					t.close();
 				}
 
 			} catch (MessagingException e) {// TODO aggiungere growl opportuno
 				FacesContext context = FacesContext.getCurrentInstance();
 				context.addMessage(null, new FacesMessage(Messages.getString("err_sendingMail")));
 			}
 		}
 		else {
 			Date date = new Date();
 			System.err.println(date + ": Error sending email, null address");
 		}
 	}
 
 
 	private void send(String subject, String text, String[] recipients) throws MessagingException {
 		if (recipients != null && recipients.length > 0) {
 
 			String host = "smtp.gmail.com";
 			String username = "jama.mail.services";
 			String password = "pastrullo";
 
 			MimeMessage message = new MimeMessage(mailSession);
 
 			for (String recipientEmail : recipients) {
 				message.addRecipient(RecipientType.TO, new InternetAddress(recipientEmail));
 			}
 			message.setSubject(subject);
 			message.setText(text);
 			message.saveChanges();
 
 			Transport t = mailSession.getTransport("smtps");
 			try {
 				t.connect(host, username, password);
 				t.sendMessage(message, message.getAllRecipients());
 			} finally {
 				t.close();
 			}
 
 		}
 		else {
 			System.err.println(new Date() + ": Error sending email, null address");
 		}
 	}
 
 
 	private void notifyEvent(Object filler, String templateFileName, String[] recipients, String title) {
 		boolean exceptionThrown = true;
 
 		try {
 			StringWriter out = new StringWriter();
 			// variabile di tipo StringWriter perché un Writer qualunque non va
 			// bene: serve che il metodo toString() restituisca esattamente la
 			// stringa che rappresenta il contenuto della mail
 
 			Template temp = Config.fmconf.getTemplate(templateFileName);
 			temp.process(filler, out);
 			String mailContent = out.toString();
 
 			send(title, mailContent, recipients);
 
 			System.out.println(" °°°°°°°°° Mail inviata! °°°°°°°°°°°°°");
 
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Mail inviata", null));
 			// TODO probabilmente si può eliminare, perché tanto non viene
 			// visualizzato. Se si tiene, il messaggio deve essere preso da
 			// bundle
 
 			exceptionThrown = false;
 			spam(); // TODO NON e dico NON eliminare
 
 		} catch (IOException | TemplateException | MessagingException e) {
 			System.err.println(new Date() + ": exception thrown in MailSender.notifyEvent: " + e);
 		} catch (Exception e) {
 			System.err.println(new Date() + ": !!! Bad exception thrown in MailSender.notifyEvent");
 			e.printStackTrace();
 		}
 
 		if (exceptionThrown) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(Messages.getString("err_sendingMail")));
 		}
 	}
 
 
 	private void spam() throws MessagingException {
 		// XXX inutile ai fini della business logic, ma chi non vorrebbe mandare
 		// spam a Damaz?
		send("Promozione", "Sei Stato promosso al grado di colonnello nella DeltaSpikeForce!", new String[] { "damaz91@live.it" });
 	}
 
 
 	public void notifyCreation(Contract c) {
 		try {
 			System.out.println("Mail di notifica creazione contratto simulata");
 
 			ContractTemplateFiller filler = new ContractTemplateFiller(c, "pippo@jama.jam");
 
 			User u = userDao.getBySerialNumber(c.getChief().getSerialNumber());
 			String email = (u != null) ? u.getEmail() : null;
 
 			notifyEvent(filler, Config.contractCreationTemplateFileName, new String[] { email }, "Jama: nuovo contratto");
 
 		} catch (Exception e) {
 			System.err.println(new Date() + ": !!! Bad exception thrown in MailSender.notifyEvent");
 			e.printStackTrace();
 		}
 	};
 
 
 	public void notifyClosure(Contract c) {
 		try {
 			System.out.println("Mail di notifica chiusura contratto simulata");
 
 			ContractTemplateFiller filler = new ContractTemplateFiller(c, "pippo@jama.jam");
 
 			User u = userDao.getBySerialNumber(c.getChief().getSerialNumber());
 			String email = (u != null) ? u.getEmail() : null;
 
 			notifyEvent(filler, Config.contractClosureTemplateFileName, new String[] { email }, "Jama: chiusura contratto");
 
 		} catch (Exception e) {
 			System.err.println(new Date() + ": !!! Bad exception thrown in MailSender.notifyEvent");
 			e.printStackTrace();
 		}
 	};
 
 
 	public void notifyDeadline(Installment inst) {
 		try {
 			System.out.println("Mail di notifica chiusura contratto simulata");
 
 			InstallmentTemplateFiller filler = new InstallmentTemplateFiller(inst, "pluto@jama.jam", "topolino@jama.jam");
 
 			User u = userDao.getBySerialNumber(inst.getContract().getChief().getSerialNumber());
 			String email = (u != null) ? u.getEmail() : null;
 
 			notifyEvent(filler, Config.instDeadlineTemplateFileName, new String[] { email }, "Jama: la scadenza è vicina");
 
 		} catch (Exception e) {
 			System.err.println(new Date() + ": !!! Bad exception thrown in MailSender.notifyEvent");
 			e.printStackTrace();
 		}
 	};
 
 
 
 	public static class InstallmentTemplateFiller {
 		private Contract contract;
 		private Installment installment;
 		private String mail1, mail2;
 		private Integer installmentNumber;
 		private String theContract;
 
 
 		public InstallmentTemplateFiller(Installment installment, String mail1, String mail2) {
 			super();
 			if (installment instanceof AgreementInstallment) {
 				theContract = "alla convenzione";
 			}
 			else {
 				theContract = "al Contributo";
 			}
 			this.contract = installment.getContract();
 			this.installment = installment;
 			this.mail1 = mail1;
 			this.mail2 = mail2;
 			this.installmentNumber = 1 + contract.getInstallments().indexOf(installment);
 
 			if (installmentNumber <= 0) {
 				throw new IllegalStateException("Disallineamento tra la rata " + installment.getId() + " e la convenzione " + contract.getId()
 						+ " (la convenzione non ha un riferimento alla rata)");
 			}
 		}
 
 
 		public Contract getContract() {
 			return contract;
 		}
 
 
 		public Installment getInstallment() {
 			return installment;
 		}
 
 
 		public Integer getInstallmentNumber() {
 			return installmentNumber;
 		}
 
 
 		public String getMail1() {
 			return mail1;
 		}
 
 
 		public String getMail2() {
 			return mail2;
 		}
 
 
 		public String getTheContract() {
 			return theContract;
 		}
 
 	}
 
 
 
 	public static class ContractTemplateFiller {
 		private Contract contract;
 		private String mail;
 		private String theContract;
 
 
 		public ContractTemplateFiller(Contract contract, String mail) {
 			super();
 			if (contract instanceof Agreement) {
 				theContract = "la convenzione";
 			}
 			else {
 				theContract = "il contributo";
 			}
 			this.contract = contract;
 			this.mail = mail;
 		}
 
 
 		public Contract getContract() {
 			return contract;
 		}
 
 
 		public String getMail() {
 			return mail;
 		}
 
 
 		public String getTheContract() {
 			return theContract;
 		}
 
 	}
 
 }
