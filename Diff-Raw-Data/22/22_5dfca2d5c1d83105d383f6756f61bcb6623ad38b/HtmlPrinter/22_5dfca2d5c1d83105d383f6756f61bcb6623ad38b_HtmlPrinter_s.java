 package simon.sms;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class HtmlPrinter {
 
 	public static final Charset cs = Charset.forName("UTF8");
 	
 	public static void main(String[] args) {
 		PrintStream log = null;
 		try {
 			log = new PrintStream(new FileOutputStream("log.txt"));
 			System.setOut(log);
 			main0(args);
 		} catch(Exception e) {
 			if (log != null) {
 				e.printStackTrace();
 			}
 		} finally {
 			if (log != null) {
 				log.close();
 			}
 		}
 	}
 	
 	public static void main0(String[] args) throws IOException {
 		List<String> contents = new ArrayList<String>();
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				HtmlPrinter.class.getClassLoader().getResourceAsStream("simon/sms/html_template.html"), cs
 		));
 		
 		try {
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if ("//$".equals(line)) {
 					Collection<Message> messages = getMessages();
 					for (Message message : messages) {
 						contents.add(message.toString());
 					}
 				} else {
 					contents.add(line);
 				}
 			}
 		} finally {
 			reader.close();
 		}
 		
 		Calendar cal = Calendar.getInstance();
 		String fileName = String.format("sms-%04d%02d%02d.html", 
 				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
 		File outFile = new File(fileName);
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), cs));
 		try {
 			for (String content : contents) {
 				writer.write(content);
 				writer.newLine();
 			}
 		} finally {
 			writer.close();
 		}
 	}
 	
 	// parse the files with name pattern sms-*.txt in the current folder,
 	// return the messages sorted by date.
 	private static Collection<Message> getMessages() {
 		URL location = HtmlPrinter.class.getProtectionDomain().getCodeSource().getLocation();
		File currentFolder = new File(location.getFile()).getParentFile();
 		
 		File[] messageFiles = currentFolder.listFiles(new FilenameFilter() {
 			private Pattern p = Pattern.compile("^sms-\\d+.txt$");
 			@Override
 			public boolean accept(File dir, String name) {
 				return p.matcher(name).matches();
 			}
 		});
 		
		List<Message> messages = new ArrayList<Message>();
 		if (messageFiles != null) {
 			for (File messageFile : messageFiles) {
 				System.out.println("Parsing " + messageFile.getPath());
 				try {
 					messages.addAll(MessageReader.read(messageFile, cs));
 				} catch(IOException e) {
 					e.printStackTrace();
 				}
 			}
 			Collections.sort(messages);
 		}
 		return messages;
 	}
 }
