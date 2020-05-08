 
 package com.ideas.producerconsumer;
 
 import java.io.File;
 
 import com.ideas.producerconsumer.mediator.Queue;
 import com.ideas.producerconsumer.object.Email;
 import com.ideas.producerconsumer.object.EmailAttachment;
 import com.ideas.producerconsumer.producer.EmailProducer;
 import com.ideas.producerconsumer.resume.ResumeStrategy;
 import com.ideas.producerconsumer.strategy.FileRejectionStrategy;
 import com.ideas.producerconsumer.strategy.FileResumeStrategy;
 import com.ideas.producerconsumer.task.Task;
 
 public class ProducerConsumerTest
 {
 	private static final String	FILE_1	= "C:" + File.separator + "Users" + File.separator + "Owner" + File.separator + "Desktop" + File.separator + "PC Test files" + File.separator + "websites.txt";
 	private static final String	FILE_2	= "C:" + File.separator + "Users" + File.separator + "Owner" + File.separator + "Desktop" + File.separator + "PC Test files" + File.separator + "Passport application.pdf";
 	private static final String	FILE_3	= "C:" + File.separator + "Users" + File.separator + "Owner" + File.separator + "Desktop" + File.separator + "PC Test files" + File.separator + "ip.png";
 
 	public static void main(String[] args)
 	{
 		ResumeStrategy resumeStrategy = new FileResumeStrategy();
 		final Queue queue = new Queue(new FileRejectionStrategy(), resumeStrategy);
 		queue.start();
 		Runnable run = new Runnable()
 		{
 			public void run()
 			{
 				Task<Email> producer = new EmailProducer();
 				Email email = getEmail();
 				producer.set(email);
 				queue.submit(producer);
 			}
 		};
		for(int i = 0; i < 2000; i++)
 		{
 			try
 			{
 				new Thread(run).start();
 			}
 			catch(Exception e)
 			{
 				// Catching exceptions if any to avoid a hung process.
 				e.printStackTrace();
 			}
 		}
 		System.out.println("initiating shutdown");
 		//Kill system manually or sleep thread for appropriate amount of time to allow test to complete.
 		try
 		{
			Thread.sleep(3*60000);
 		}
 		catch (InterruptedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		queue.shutdown();
 	}
 
 	/**
 	 * @return
 	 */
 	private static Email getEmail()
 	{
 		Email email = new Email();
 		email.setAttachments(getAttachments());
 		email.setTo(new String[] {"achar_varun@hotmail.com"});
 		email.setFrom("varunach@gmail.com");
 		email.setSubject("Testing producer consumer");
 		return email;
 	}
 
 	/**
 	 * @return
 	 */
 	private static EmailAttachment[] getAttachments()
 	{
 		EmailAttachment[] attachments = new EmailAttachment[3];
 		EmailAttachment attachment = new EmailAttachment();
 		attachment.setExtension("txt");
 		attachment.setName("Text File");
 		attachment.setFile(new File(FILE_1));
 		attachments[0] = attachment;
 
 		EmailAttachment attachment1 = new EmailAttachment();
 		attachment1.setExtension("pdf");
 		attachment1.setName("PDF File");
 		attachment1.setFile(new File(FILE_2));
 		attachments[1] = attachment1;
 
 		EmailAttachment attachment2 = new EmailAttachment();
 		attachment2.setExtension("png");
 		attachment2.setName("Image file");
 		attachment2.setFile(new File(FILE_3));
 		attachments[2] = attachment2;
 		return attachments;
 	}
 }
