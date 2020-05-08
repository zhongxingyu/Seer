 /*
  *	This file is part of Web-CAT Eclipse Plugins.
  *
  *	Web-CAT is free software; you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation; either version 2 of the License, or
  *	(at your option) any later version.
  *
  *	Web-CAT is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public License
  *	along with Web-CAT; if not, write to the Free Software
  *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package net.sf.webcat.eclipse.submitter.internal.protocols;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.mail.Authenticator;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import net.sf.webcat.eclipse.submitter.core.IPackager;
 import net.sf.webcat.eclipse.submitter.core.IPackagerRegistry;
 import net.sf.webcat.eclipse.submitter.core.IProtocol;
 import net.sf.webcat.eclipse.submitter.core.ITarget;
 import net.sf.webcat.eclipse.submitter.core.SubmissionParameters;
 import net.sf.webcat.eclipse.submitter.core.SubmissionTargetException;
 import net.sf.webcat.eclipse.submitter.core.SubmitterCore;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableContext;
 
 /**
  * A protocol for the "mailto" URI scheme that supports sending the submitted
  * file as an e-mail attachment.
  * 
  * @author Tony Allowatt (Virginia Tech Computer Science)
  */
 public class MailtoProtocol implements IProtocol
 {
 	private class MailAuthenticator extends Authenticator
 	{
 		private String username;
 		private String password;
 		
 		public MailAuthenticator(String user, String pass)
 		{
 			username = user;
 			password = pass;
 		}
 		
 		protected PasswordAuthentication getPasswordAuthentication()
 		{
 			return new PasswordAuthentication(username, password);
 		}
 	}
 
 	public void submit(IRunnableContext context,
 			IProgressMonitor monitor, SubmissionParameters params,
 			URI transport) throws CoreException, IOException,
 			InterruptedException
 	{
 		try
 		{
 			// Create the archive in a temp file.
 			File zipFile = File.createTempFile("submitter_", ".zip");
 			FileOutputStream outStream = new FileOutputStream(zipFile);
 	
 			IPackagerRegistry manager = SubmitterCore.getDefault().getPackagerRegistry();
 			IPackager packager = manager.getPackager(params.getAssignment().getPackager(context));
 			packager.pack(context, params, outStream);
 	
 			outStream.close();
 	
 			Properties props = System.getProperties();
 			props.put("mail.smtp.host", SubmitterCore.getDefault()
 					.getOption(SubmitterCore.IDENTIFICATION_SMTPSERVER));
 			
 			ITarget asmt = params.getAssignment();
 	
 			String authString = (String)asmt.getTransportParams(context).get("auth");
 			boolean auth = false;
 			if(authString != null)
				auth = Boolean.parseBoolean(authString);
 	
 			Session session;
 			if(auth)
 			{
 				props.put("mail.smtp.auth", "true");
 				session = Session.getDefaultInstance(props,
 						new MailAuthenticator(params.getUsername(), params.getPassword()));
 			}
 			else
 				session = Session.getDefaultInstance(props, null);
 	
 			try
 			{
 				Message message = new MimeMessage(session);
 				message.setFrom(new InternetAddress(SubmitterCore.getDefault()
 						.getOption(SubmitterCore.IDENTIFICATION_EMAILADDRESS)));
 	
 				String to = params.resolveParameter(transport.getSchemeSpecificPart());
 				String subject = params.resolveParameter(
 						(String)asmt.getTransportParams(context).get("subject"));
 	
 				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
 				message.setSubject(subject);
 				message.setSentDate(new Date());
 	
 				Multipart multiPart = new MimeMultipart();
 	
 				Set transportParams = asmt.getTransportParams(context).entrySet();
 				for(Iterator it = transportParams.iterator(); it.hasNext(); )
 				{
 					Map.Entry entry = (Map.Entry)it.next();
 					String paramName = (String)entry.getKey();
 					String paramValue = (String)entry.getValue();
 					String convertedValue = params.resolveParameter(paramValue);
 	
 					if(paramName.startsWith("$file."))
 					{
 						MimeBodyPart filePart = new MimeBodyPart();
 						filePart.setFileName(convertedValue);
 						filePart.setDataHandler(new DataHandler(new FileDataSource(
 								zipFile)));
 						multiPart.addBodyPart(filePart);
 					}
 				}
 	
 				message.setContent(multiPart);
 				Transport.send(message);
 			}
 			catch(MessagingException e)
 			{
 				throw new IOException(e.getMessage());
 			}
 	
 			zipFile.delete();
 		}
 		catch(SubmissionTargetException e)
 		{
 		}
 	}
 
 	public boolean hasResponse()
 	{
 		return false;
 	}
 
 	public String getResponse()
 	{
 		return null;
 	}
 }
