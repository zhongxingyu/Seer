 package org.kisst.gft;
 
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 
 import nl.duo.gft.GftDuoModule;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.kisst.cfg4j.Props;
 import org.kisst.cfg4j.SimpleProps;
 import org.kisst.gft.ssh.GenerateKey;
 import org.kisst.jms.ActiveMqSystem;
 import org.kisst.jms.JmsSystem;
 import org.kisst.util.CryptoUtil;
 import org.kisst.util.FileUtil;
 import org.kisst.util.TemplateUtil;
 
 public class GftRunner {
 	private final File configfile;
 	private boolean running=false;
 	private GftContainer gft;
 	
 	public GftRunner(File configfile) {
 		this.configfile = configfile;
 	}
 
 	public void start() {
 		if (gft!=null)
 			throw new RuntimeException("Gft already running");
 		running=true;
 		gft=new GftContainer(configfile);
 		gft.start();
 	}
 
 	public void run() {
 		start();
 		while (running) {
 			gft.join();
 		}
 		gft=null;
 	}
 	public void shutdown() {
 		running=false;
 		if (gft==null)
 			return;
 		gft.stop();
 	}
 	public void restart() {
 		if (gft==null)
 			return;
 		gft.stop();
 	}
 	
 
 	private static Cli cli=new Cli();
 	private static Cli.StringOption config = cli.stringOption("c","config","configuration file", "config/gft.properties");
 	private static Cli.Flag putmsg = cli.flag("p","putmsg", "puts a message on the input queue");
 	private static Cli.StringOption rmmsg = cli.stringOption("r","rmmsg","selector", null);
 	private static Cli.Flag help =cli.flag("h", "help", "show this help");
 	private static Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
 	private static Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
 	public static void main(String[] args) {
 		cli.parse(args);
 		if (help.isSet()) {
 			showHelp();
 			return;
 		}
 		GftDuoModule.setKey();
 		File configfile=new File(config.get());
 		if (putmsg.isSet()) {		// TODO: refactor this code dupplication
 			SimpleProps props=new SimpleProps();
 			props.load(configfile);
 			JmsSystem queueSystem=getQueueSystem(props);
 			String queuename = getQueue(props);
 
 			String data=FileUtil.loadString(new InputStreamReader(System.in));
 			queueSystem.getQueue(queuename).send(data);
 			System.out.println("send the following message to the queue "+queuename);
 			System.out.println(data);
 			return;
 		}
 		if (rmmsg.isSet()) {		// TODO: refactor this code dupplication
 			SimpleProps props=new SimpleProps();
 			props.load(configfile);
 			JmsSystem queueSystem=getQueueSystem(props);
 			String queuename = getQueue(props);
 			String selector=rmmsg.get();
 			System.out.println("removing the following message "+selector);
 			try {
 				QueueSession session = queueSystem.getConnection().createQueueSession(true, Session.SESSION_TRANSACTED);
 				MessageConsumer consumer = session.createConsumer(session.createQueue(queuename), selector);
				Message msg = consumer.receiveNoWait();
 				if (msg==null)
 					System.out.println("Could not find message "+selector);
				else
					msg.acknowledge();
 			}
 			catch (JMSException e) { throw new RuntimeException(e); }
 			return;
 		}
 
 		if (keygen.isSet()) {
 			GenerateKey.generateKey(configfile.getParentFile().getAbsolutePath()+"/ssh/id_dsa_gft"); // TODO: should be from config file
 			return;
 		}
 		if (encrypt.get()!=null) {
 			System.out.println(CryptoUtil.encrypt(encrypt.get()));
 			return;
 		}
 
 		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
 		GftRunner runner= new GftRunner(configfile);
 		runner.run();
 		
 		System.out.println("GFT stopped");
 	}
 
 	private static String getQueue(SimpleProps props) {
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		context.put("global", props.get("gft.global", null));
 		String queuename = TemplateUtil.processTemplate(props.getString("gft.listener.main.queue"), context);
 		return queuename;
 	}
 
 	private static void showHelp() {
 		System.out.println("usage: java -jar gft.jar [options]");
 		System.out.println(cli.getSyntax(""));
 	}
 
 	private static JmsSystem getQueueSystem(Props props) {
 		Props qmprops=props.getProps("gft.queueSystem");
 		String type=qmprops.getString("type");
 		if ("ActiveMq".equals(type))
 			return new ActiveMqSystem(qmprops);
 		else if ("Jms".equals(type))
 			return new JmsSystem(qmprops);
 		else 
 			throw new RuntimeException("Unknown type of queueing system "+type);
 	}
 
 }
