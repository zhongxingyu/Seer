 package com.horsefire.filecabinet;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 
 public class FileCabinet {
 
 	private final QueueProcessor m_queueProcessor;
 	private final Importer m_importer;
 
 	@Inject
 	public FileCabinet(Importer importer, QueueProcessor queueProcessor) {
 		m_importer = importer;
 		m_queueProcessor = queueProcessor;
 	}
 
 	public void run() throws Exception {
 		m_importer.run();
		m_queueProcessor.run();
 	}
 
 	public static void main(String[] args) throws Exception {
 		Options options = new Options();
 		try {
 			JCommander jc = new JCommander(options, args);
 			jc.setProgramName("java -jar filecabinet.jar");
 
 			if (options.help) {
 				jc.usage();
 				return;
 			}
 
 			if (options.version) {
 				String version = FileCabinet.class.getPackage()
 						.getImplementationVersion();
 				System.out.println("File Cabinet " + version);
 				return;
 			}
 
 		} catch (ParameterException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Use --help to display usage");
 			return;
 		}
 
 		Guice.createInjector(new FcModule(options))
 				.getInstance(FileCabinet.class).run();
 	}
 }
