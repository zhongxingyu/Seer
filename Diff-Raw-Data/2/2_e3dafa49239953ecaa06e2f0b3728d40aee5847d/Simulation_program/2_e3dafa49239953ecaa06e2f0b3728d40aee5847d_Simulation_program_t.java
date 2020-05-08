 package Analyzing_simulation_program;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextField;
 import javax.swing.plaf.FileChooserUI;
 
 
 public class Simulation_program extends JFrame implements ActionListener
 {
 	JPanel pane = new JPanel();
 	JTextField numPointsField;
 	JTextField xSentrumField;
 	JTextField ySentrumField;
 	JTextField zSentrumField;
 	JTextField xRangeField;
 	JTextField yRangeField;
 	JTextField zRangeField;
 	JTextField sleepField;
 	JProgressBar bar;
 	String [] args;
 	boolean running = false;
 	Thread t;
 
 	Simulation_program(String [] arg) // the frame constructor method
 	{
 		super("Simulation program"); 
 		setBounds(100,100,300,300);
 		this.args = arg;
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		Container con = this.getContentPane(); // inherit main frame
 		con.add(pane); // add the panel to frame
 
 		pane.add(new JLabel("(x,y,z) \\ (Sentrum, Range):"));
 
 		xSentrumField = new JTextField(14);
 		xSentrumField.setText("50");
 		pane.add(xSentrumField);
 		xRangeField = new JTextField(5);
 		xRangeField.setText("10");
 		pane.add(xRangeField);
 
 		ySentrumField = new JTextField(14);
 		ySentrumField.setText("50");
 		pane.add(ySentrumField);
 		yRangeField = new JTextField(5);
 		yRangeField.setText("10");
 		pane.add(yRangeField);
 
 		zSentrumField = new JTextField(14);
 		zSentrumField.setText("50");
 		pane.add(zSentrumField);
 		zRangeField = new JTextField(5);
 		zRangeField.setText("10");
 		pane.add(zRangeField);
 
 		pane.add(new JLabel("Number of points to submit:"));
 
 		numPointsField = new JTextField(20);
 		numPointsField.setText("10");
 		pane.add(numPointsField);
 
 		pane.add(new JLabel("Time between points:"));
 
 		sleepField = new JTextField(20);
 		sleepField.setText("200");
 		pane.add(sleepField);
 
 		bar = new JProgressBar();
 		bar.setPreferredSize(new Dimension(225, 15));
 		bar.setStringPainted(true);
 		pane.add(bar);
 
 		JButton start = new JButton("Start");
 		start.addActionListener(this);
 		pane.add(start);
 
 		JButton stop = new JButton("Stop");
 		stop.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(t != null){
 					t.interrupt();
 				}
 			}
 
 		});
 		pane.add(stop);
 
 		JButton file = new JButton("Run file");
 		file.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(!running){
 					t = new Thread(new Runnable(){
 
 						@Override
 						public void run() {
 							JFileChooser fc = new JFileChooser();
 							int answer = fc.showOpenDialog(new JPanel());
 							if(answer == JFileChooser.APPROVE_OPTION ){
 								File file = fc.getSelectedFile();
 								runFile(file);
 							}
 						}
 
 					});
 					t.start();
 				}
 			}
 
 		});
 		pane.add(file);
 
 		setVisible(true); // display this frame
 		if(args.length == 1){
 			t = new Thread(new Runnable(){
 
 				@Override
 				public void run() {
 					File file = new File(args[0]);
 					if(file.isFile())
 						runFile(file);
 				}
 				
 			});
 			t.start();
 		}
 	}
 	
 
 	private void runFile(File file){
 		running = true;
 
 		try {
 			FileInputStream fs = new FileInputStream(file);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fs)); 
 
 			int chr;
 			int numline = 0;
 			System.out.println("Kalkulerer antall linjer.");
 			while((chr = br.read()) != -1){
 				if(((char) chr) == '\n') numline++;
 			}
 
 			bar.setMinimum(0);
 			bar.setMaximum(numline);
 
 			br.close();
 			fs = new FileInputStream(file);
 			br = new BufferedReader(new InputStreamReader(fs));
 			String line = br.readLine();
 			long time = getTime();
 			long nowtime = 0;
 			long lasttime = 0;
 			int j = 0;
 			while(line != null){
 				String[] props = line.split(",");
 				String[] names = {"idtag", "time", "x", "y", "z", "a", "b", "img"};
 				String query = "";
 				for(int i = 0; i < props.length; i++){
 					String value = props[i].trim();
 					value = value.replaceAll("\"", "");
 					if(value.equals("0") || value.equals(""))
 						continue;
 
 					String key = names[i];
 					query += key + "=" + value + "&"; 
 				}
 				nowtime = Long.parseLong(props[1].trim());
 
 				if(lasttime != 0){
 					long usedtime = getTime() - time;
 					long neededtime = nowtime - lasttime;
 					long wait = neededtime - usedtime;
 					System.out.println("M vente i " + neededtime + " ms");
 					System.out.println("Brukte " + usedtime + " ms");
 					System.out.println("Ventet i " + wait + " ms");
 					if(wait > 0)
 						Thread.sleep(wait);
 					else
 						System.err.println("Brukte lengre tid enn tildelt!!!!!!!!!");
 				}
 				// new time round
 				lasttime = Long.parseLong(props[1].trim());
 				time = getTime();
 
 				String path = "http://localhost:8888/register/trigger/xml?";
 				URL url;
 				URLConnection urlConnection = null;
 				DataInputStream inStream;
 
 				url = new URL(path + query);
 				long contime = getTime();
 				urlConnection = url.openConnection();
 				((HttpURLConnection)urlConnection).setRequestMethod("GET");
 				urlConnection.setDoOutput(true);
 				//System.out.println(url.getQuery());
 				inStream = new DataInputStream(urlConnection.getInputStream());
 				String buffer;
 				while((buffer = inStream.readLine()) != null) {
 					//System.out.println(buffer);
 				}
 				inStream.close();
 				System.out.println("Tilkoblingstid " + (getTime()-contime) + " ms");
 
 				line = br.readLine();
 				j++;
 				bar.setValue(j);
 
 				if(t.isInterrupted()) break;
 			}
 			br.close();
 			fs.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 
 		}
 
 
 		running = false;
 	}
 
 	public void actionPerformed(ActionEvent evt) {
 		if(!running){
 			running = true;
 
 			xSentrum = Integer.parseInt(xSentrumField.getText());
 			xRange = Integer.parseInt(xRangeField.getText());
 
 			ySentrum = Integer.parseInt(ySentrumField.getText());
 			yRange = Integer.parseInt(yRangeField.getText());
 
 			zSentrum = Integer.parseInt(zSentrumField.getText());
 			zRange = Integer.parseInt(zRangeField.getText());
 
 			String text = numPointsField.getText();
 			numberOfTrigerpoints = Integer.parseInt(text);
 
 			timeToSleep = Integer.parseInt(sleepField.getText());
 
 			t = new Thread(new StartConection());
 			t.start();
 		}
 
 	}
 
 	private static int numberOfTrigerpoints;
 	static int x;
 	static int y; 
 	static int z;
 	static int xRange;
 	static int yRange; 
 	static int zRange;
 	static int xSentrum;
 	static int ySentrum; 
 	static int zSentrum;
 	static long timestamp;
 	static int timeToSleep;
 
 	public static void main(String[] args) throws Exception {
 		new Simulation_program(args);
 
 	}
 
 	public void setNewTriggerpoint(){
 		x = RandomClusterNumber(xSentrum, xRange);
 		y = RandomClusterNumber(ySentrum, yRange);
 		z = RandomClusterNumber(zSentrum, zRange);
 		timestamp = getTime();
 	}
 	public int RandomNumber(int max){
		return (int)(Math.random()*max);
 
 	}
 
 	public int RandomClusterNumber(int base, int maxRange){
 		return base + (RandomNumber(maxRange*2) - maxRange);
 	}
 
 	public long getTime(){
 		Calendar cal = Calendar.getInstance();
 		return cal.getTimeInMillis();
 	}
 
 
 	private class StartConection implements Runnable {
 
 		@Override
 		public void run() {
 			try{
 				String path = "http://localhost:8888/register/trigger/xml?";
 				URL url;
 				URLConnection urlConnection = null;
 				DataInputStream inStream;
 
 				bar.setMinimum(0);
 				bar.setMaximum(numberOfTrigerpoints);
 				int i=0;
 				while(i<numberOfTrigerpoints){
 					setNewTriggerpoint();
 					url = new URL(path+"x="+x+"&y="+y+"&z="+z+"&time="+timestamp);
 					urlConnection = url.openConnection();
 					((HttpURLConnection)urlConnection).setRequestMethod("GET");
 					urlConnection.setDoOutput(true);
 					System.out.println(url.getQuery());
 
 					inStream = new DataInputStream(urlConnection.getInputStream());
 
 					//					String buffer;
 					//					while((buffer = inStream.readLine()) != null) {
 					//						if(buffer.contains("false")){
 					//							System.out.println(buffer);
 					//						}
 					//					}
 					inStream.close();
 					i++;
 					Thread.sleep(timeToSleep);
 					bar.setValue(i);
 
 					if(t.isInterrupted()) break;
 				}
 			}catch (Exception e) {}
 
 			//			OpenBrowser openBrowser = new OpenBrowser();
 			//			URI uri = null;
 			//			try {
 			//				uri = new URI("http://127.0.0.1:8888");
 			//			} catch (URISyntaxException e) {
 			//				// TODO Auto-generated catch block
 			//				e.printStackTrace();
 			//			}
 			//			openBrowser.openWebpage(uri);
 			running = false;
 		}
 
 	}
 
 }
 
 
 
 
