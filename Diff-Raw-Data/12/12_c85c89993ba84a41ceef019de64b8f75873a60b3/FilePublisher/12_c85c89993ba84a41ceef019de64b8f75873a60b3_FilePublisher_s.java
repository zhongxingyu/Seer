 package pt.com.broker.core;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.text.Collator;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.messaging.BrokerProducer;
 import pt.com.broker.messaging.MQ;
 import pt.com.broker.xml.SoapEnvelope;
 import pt.com.broker.xml.SoapSerializer;
 import pt.com.gcs.conf.GcsInfo;
 
 public class FilePublisher
 {
 	private static Logger log = LoggerFactory.getLogger(FilePublisher.class);
 
 	private static FilePublisher instance = new FilePublisher();
 
 	private static final long INITIAL_DELAY = 10L; // 10 segundos
 
 	private static final long DEFAULT_CHECK_DELAY = 60L; // 60 segundos
 
 	private final String dir;
 
 	private final boolean isEnabled;
 
 	private final long check_interval;
 
 	private File dropBoxDir;
 
 	private FilePublisher()
 	{
 		isEnabled = GcsInfo.isDropboxEnabled();
 		dir = GcsInfo.getDropBoxDir();
 		check_interval = GcsInfo.getDropBoxCheckInterval() > 0 ? GcsInfo.getDropBoxCheckInterval() : DEFAULT_CHECK_DELAY;
 
 		log.debug("DropBox Monitor, dir: {}", dir);
 		log.debug("DropBox Monitor, enabled: {}", isEnabled);
 		log.debug("DropBox Monitor, check interval: {}", check_interval);
 
 	}
 
 	final FileFilter fileFilter = new FileFilter()
 	{
 		public boolean accept(File file)
 		{
 			return file.getName().endsWith(".good");
 		}
 	};
 
 	final Comparator<File> fileComparator = new Comparator<File>()
 	{
 		private Collator c = Collator.getInstance(Locale.ENGLISH);
 
 		public int compare(File f1, File f2)
 		{
 			if (f1 == f2)
 				return 0;
 
 			if (f1.isDirectory() && f2.isFile())
 				return -1;
 			if (f1.isFile() && f2.isDirectory())
 				return 1;
 
 			return c.compare(f1.getName(), f2.getName());
 		}
 	};
 
 	final Runnable publisher = new Runnable()
 	{
 		public void run()
 		{
 			log.debug("Checking for files in the DropBox.");
 			try
 			{
 				File[] files = dropBoxDir.listFiles(fileFilter);
 
 				if ((files != null) && (files.length > 0))
 				{
 					if (log.isDebugEnabled())
 					{
 						log.debug("Will try to publish " + files.length + " file(s).");
 					}
 
 					Arrays.sort(files, fileComparator);
 
 					for (File msgf : files)
 					{
 						FileInputStream fis = new FileInputStream(msgf);
 						SoapEnvelope soap = null;
 						boolean isFileValid = false;
 						try
 						{
 							soap = SoapSerializer.FromXml(fis);
 							isFileValid = true;
 						}
 						catch (Throwable e)
 						{
 							log.error("Error processing file \"" + msgf.getAbsolutePath() + "\". Error message: " + ErrorAnalyser.findRootCause(e).getMessage());
 						}
 
 						if (isFileValid)
 						{
 							try
 							{
 								if (soap.body.publish != null)
 								{
 									BrokerProducer.getInstance().publishMessage(soap.body.publish, MQ.requestSource(soap));
 								}
 								else if (soap.body.enqueue != null)
 								{
 									BrokerProducer.getInstance().enqueueMessage(soap.body.enqueue, MQ.requestSource(soap));
 								}
 							}
 							catch (Throwable e)
 							{
 								log.error("Error publishing file \"" + msgf.getAbsolutePath() + "\". Error message: " + ErrorAnalyser.findRootCause(e).getMessage());
 							}
 							finally
 							{
								
 								try
 								{
 									fis.close();
									//msgf.delete();
									File badFile = new File(msgf.getAbsolutePath() + ".bad");
									msgf.renameTo(badFile);
 								}
								catch (Throwable e)
 								{
									//ignore since we are cleaning
 								}
 							}							
 
 						}
 						else
 						{
 							fis.close();
 							File badFile = new File(msgf.getAbsolutePath() + ".bad");
 							msgf.renameTo(badFile);
 						}
 					}
 				}
 				else
 				{
 					log.debug("No files to publish.");
 				}
 			}
 			catch (Throwable e)
 			{
 				log.error(e.getMessage(), e);
 			}
 		}
 	};
 
 	public static void init()
 	{
 		if (instance.isEnabled)
 		{
 			instance.dropBoxDir = new File(instance.dir);
 			if (instance.dropBoxDir.isDirectory())
 			{
 				BrokerExecutor.scheduleWithFixedDelay(instance.publisher, INITIAL_DELAY, instance.check_interval, TimeUnit.SECONDS);
 				log.info("Drop box functionality enabled.");
 			}
 			else
 			{
 				abort();
 			}
 		}
 		else
 		{
 			abort();
 		}
 	}
 
 	private static void abort()
 	{
 		log.info("Drop box functionality disabled.");
 	}
 }
