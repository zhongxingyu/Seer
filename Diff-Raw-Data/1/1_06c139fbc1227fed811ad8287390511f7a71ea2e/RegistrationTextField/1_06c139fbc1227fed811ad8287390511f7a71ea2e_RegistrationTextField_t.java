 package enduro.gui;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JTextField;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 import enduro.Registration;
 import enduro.network.client.EnduroClient;
 import enduro.racer.Time;
 
 /**
  * The text field in which the user enters text.
  * 
  */
 @SuppressWarnings("serial")
 public class RegistrationTextField extends JTextField implements ActionListener {
 	private RegistrationTextArea registrationTextArea;
 	private Registration registration;
 	private StoredTime storedTime;
 	private UndoButton undo;
 	private EnduroClient client;
 	private boolean networkMode;
 	private String fileName;
 
 	/**
 	 * Creates a new RegistrationTextField with the specified Font and reference
 	 * to the RegistrationTextArea.
 	 * 
 	 * @param font
 	 *            The font to use in this text field.
 	 * @param registrationTextArea
 	 *            The RegistrationTextArea to add new stuff to.
 	 */
 	public RegistrationTextField(Font font,
 			RegistrationTextArea registrationTextArea, StoredTime storedTime) {
 		super(5);
 		setName("Input");
 		setFont(font);
 		this.registrationTextArea = registrationTextArea;
 		this.storedTime = storedTime;
 		addActionListener(this);
 		networkMode = false;
 	}
 
 	public RegistrationTextField(Font font,
 			RegistrationTextArea registrationTextArea, StoredTime storedTime,
 			String[] args) {
 		super(5);
 		setName("Input");
 		setFont(font);
 		this.registrationTextArea = registrationTextArea;
 		this.storedTime = storedTime;
 		addActionListener(this);
 		if (args[3].equals("Start"))
 			fileName = "starttider.txt";
 		else if (args[3].equals("Goal"))
 			fileName = "maltider.txt";
 		else
 			fileName = "times.txt";
		networkMode = false;
 		if (args[0].equals("true")) {
 			try {
 				client = new EnduroClient(args[1], Integer.parseInt(args[2]),
 						args[3], fileName);
 				networkMode = true;
 				client.registerFile();
 			} catch (Exception e) {
 
 			}
 		}
 
 	}
 
 	/**
 	 * Registers multiple racers if the input stream is written as first - last
 	 * where first is the first racer's number and last is the last racer's
 	 * number. Otherwise the racer is registered according to the input number.
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		String currentTime = getTime();
 		String[] input = getText().split(",", Integer.MAX_VALUE);
 		boolean error = false;
 
 		Pattern p = Pattern
 				.compile("((\\d+)|(\\d+-\\d+))((,\\d+((,\\d+)|(-\\d+))?))*");
 		if (!p.matcher(getText()).matches())
 			error = true;
 
 		p = Pattern.compile("((\\d+)(-(\\d+))?)");
 		for (int i = 0; i < input.length && !error; ++i) {
 			Matcher m = p.matcher(input[i]);
 			m.matches();
 			String start = m.group(2);
 			String end = (m.group(4) == null) ? start : m.group(4);
 			if (Integer.parseInt(start) > Integer.parseInt(end)) {
 				start = end;
 				end = m.group(2);
 			}
 			String rows = makeRow(currentTime, start, end);
 			registrationTextArea.update(rows);
 		}
 
 		if (error) {
 			if (storedTime.isEmpty()) {
 				storeTime(currentTime);
 				undo.setVisible(true);
 			}
 		} else {
 			storedTime.empty();
 			undo.setVisible(false);
 			deleteStoredTimeFile();
 		}
 		setText("");
 		requestFocus();
 	}
 
 	/**
 	 * Creates a new row in sb.
 	 * 
 	 * @param currentTime
 	 * @param dashSeparated
 	 * @param sb
 	 */
 	private String makeRow(String currentTime, String start, String end) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = Integer.parseInt(end); i >= Integer.parseInt(start); --i) {
 			sb.append(i + "; ");
 			sb.append((storedTime.isEmpty() ? currentTime : getStoredTime()));
 			sb.append('\n');
 			saveToFile(i, (storedTime.isEmpty() ? new Time(currentTime)
 					: getStoredTime()));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Saves the current time to a text file.
 	 */
 	private void storeTime(String time) {
 		try {
 			PrintWriter out = new PrintWriter(new BufferedWriter(
 					new FileWriter(".temp")));
 			out.println(time);
 			out.close();
 			System.gc(); // run garbage collector, absolutely needed to be able
 			// to delete the file on Windows
 		} catch (IOException e) {
 
 		}
 		storedTime.setText(time);
 	}
 
 	/**
 	 * Deletes the file storedTimeOfUnknownDriver.txt.
 	 */
 	public void deleteStoredTimeFile() {
 		File f = new File(".temp");
 		f.delete();
 		storedTime.empty();
 	}
 
 	/**
 	 * Checks if storedTimeOfUnknownDriver.txt file exists.
 	 */
 	public void checkForSavedTimeFile() {
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new FileReader(
 					".temp"));
 			storedTime.setText(in.readLine());
 			undo.setVisible(true);
 		} catch (FileNotFoundException e1) {
 
 		} catch (IOException e) {
 
 		}
 	}
 
 	/**
 	 * Reads the temporary stored file, containing the last entered
 	 * unknown-driver time.
 	 * 
 	 * @return
 	 */
 	public Time getStoredTime() {
 		BufferedReader in;
 		try {
 			in = new BufferedReader(new FileReader(
 					".temp"));
 			return new Time(in.readLine());
 		} catch (FileNotFoundException e1) {
 
 		} catch (IOException e) {
 
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the current time as a String. Probably exists in the back end.
 	 * 
 	 * @return The current time as a String.
 	 */
 	private String getTime() {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
 		return sdf.format(cal.getTime());
 	}
 
 	/**
 	 * Saves the input time for the entered number to file.
 	 * 
 	 * @param number
 	 *            the racer's number
 	 * @param t
 	 *            the time for the racer.
 	 */
 
 	private void saveToFile(int number, Time t) {
 		try {
 			registration = new Registration(fileName);
 			registration.registerTime(number, t);
 			registration.close();
 			if (networkMode)
 				client.registerLine(number + "; " + t);
 		} catch (IOException e) {
 
 		}
 	}
 
 	/**
 	 * Used by JTextField and creates a new numberDocument object.
 	 */
 	protected Document createDefaultModel() {
 		return new numberDocument();
 	}
 
 	/**
 	 * A class that contains a method that is called every time a new character
 	 * is entered in the text field. Checks if the character is a digit or ","
 	 * or "-".s
 	 */
 	private class numberDocument extends PlainDocument {
 
 		public void insertString(int offs, String str, AttributeSet a)
 				throws BadLocationException {
 			char[] number = str.toCharArray();
 			for (int i = 0; i < number.length; i++) {
 				if (!Character.isDigit(number[i]) && number[i] != ','
 						&& number[i] != '-')
 					return;
 			}
 
 			super.insertString(offs, new String(number), a);
 		}
 
 	}
 
 	public void setRegretButton(UndoButton regret) {
 		this.undo = regret;
 	}
 
 	public void closeConnection() {
 		if (networkMode)
 			client.shutDown();
 
 	}
 
 }
