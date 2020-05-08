 package za.ac.sun.cs.intlola.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import za.ac.sun.cs.intlola.IntlolaSender;
 import za.ac.sun.cs.intlola.SendMode;
 import za.ac.sun.cs.intlola.file.IndividualFile;
 import za.ac.sun.cs.intlola.file.IntlolaFile;
 import za.ac.sun.cs.intlola.preferences.PreferenceConstants;
 
 public class MultiSender {
 	static class SendThread implements Runnable {
 		private final String user;
 		private final String passwd;
 		private final ArrayList<String> files;
 
 		public SendThread(final String user, final String passwd,
 				final ArrayList<String> files) {
 			this.user = user;
 			this.passwd = passwd;
 			this.files = files;
 		}
 
 		@Override
 		public void run() {
 			final IntlolaSender sender = new IntlolaSender(user, "Data",
 					SendMode.INDIVIDUAL, PreferenceConstants.LOCAL_ADDRESS,
 					PreferenceConstants.PORT);
 			if (sender.openConnection()) {
 				sender.login(user, passwd);
 				if (sender.loggedIn()) {
 					for (final String file : files) {
 						final IntlolaFile ifile = IndividualFile.read(file);
						System.out.println(ifile.toJSON());
 						sender.sendFile(ifile);
 					}
 					sender.logout();
 					System.out.println("Success");
 				}
 			}
 		}
 	}
 
 	private static Random rand = new Random();
 	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	private static final int LENGTH = 30;
 	private static HashMap<String, Boolean> strings = new HashMap<String, Boolean>();
 
 	public static ArrayList<String> getFiles(final File dir) {
 		final ArrayList<String> files = new ArrayList<String>();
 		for (final File f : dir.listFiles()) {
 			if (f.isFile()) {
 				files.add(f.getAbsolutePath());
 			}
 		}
 		return files;
 	}
 
 	public static Map<String, String> getUsers(final String fname) {
 		final Map<String, String> users = new HashMap<String, String>();
 
 		try {
 			final BufferedReader reader = new BufferedReader(new FileReader(
 					new File(fname)));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				final String[] vals = line.split(":");
 				users.put(vals[0], vals[1]);
 			}
 			reader.close();
 		} catch (final FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public static void main(final String argv[]) {
 		MultiSender.sendFiles();
 	}
 
 	public static String randString() {
 		String gen = "";
 		MultiSender.strings.put(gen, true);
 		while (MultiSender.strings.containsKey(gen)) {
 			final char[] text = new char[MultiSender.LENGTH];
 			for (int i = 0; i < MultiSender.LENGTH; i++) {
 				text[i] = MultiSender.CHARACTERS.charAt(MultiSender.rand
 						.nextInt(MultiSender.CHARACTERS.length()));
 			}
 			gen = new String(text);
 		}
 		MultiSender.strings.put(gen, true);
 		return gen;
 	}
 
 	private static void sendFiles() {
 		final Map<String, String> users = MultiSender.getUsers("users");
 		final ArrayList<String> files = MultiSender.getFiles(new File("data"));
 		final ArrayList<Thread> threads = new ArrayList<Thread>();
 		int c = 0;
 		for (final Map.Entry<String, String> e : users.entrySet()) {
 			threads.add(new Thread(new SendThread(e.getKey(), e.getValue(),
 					files)));
 			c++;
			if (c > 1) {
 				break;
 			}
 		}
 		for (final Thread th : threads) {
 			th.start();
 		}
 	}
 }
