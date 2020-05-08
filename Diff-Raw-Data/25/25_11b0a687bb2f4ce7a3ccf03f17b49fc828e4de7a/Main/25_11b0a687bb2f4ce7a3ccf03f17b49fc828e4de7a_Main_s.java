 package com.robserob.ytgh;
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Font;
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.Response;
 import javax.swing.JTextArea;
 import javax.swing.JProgressBar;
 import javax.swing.DropMode;
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 public class Main {
 
 	private JFrame frmYoutubeGiveawayHelper;
 	private JTextField youtubeURL;
 	private JPanel animationFrame;
 	private JButton goButton;
 	private JCheckBox shouldSkipAnimation;
 	private JLabel progress;
 	private JProgressBar progressBar;
 	private Timer timer;
 	private String winnerLog = "";
 	private ContestantBag contestantBag = new ContestantBag();
 	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
 	int numberOfComments = 0;
 	private JLabel lblAuthor;
 	private JLabel lblHttprobserobdk;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Main window = new Main();
 					window.frmYoutubeGiveawayHelper.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Main() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmYoutubeGiveawayHelper = new JFrame();
 		frmYoutubeGiveawayHelper.setTitle("Youtube Giveaway Helper 2.0");
 		frmYoutubeGiveawayHelper.setBounds(100, 100, 550, 372);
 		frmYoutubeGiveawayHelper.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmYoutubeGiveawayHelper.getContentPane().setLayout(null);
 		
 		youtubeURL = new JTextField();
 		youtubeURL.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				if (youtubeURL.getText().indexOf("?v=") > 0 || youtubeURL.getText().length() == 11)
 					goButton.setEnabled(true);
 				else
 					goButton.setEnabled(false);
 			}
 		});
 		youtubeURL.setBounds(123, 11, 301, 29);
 		frmYoutubeGiveawayHelper.getContentPane().add(youtubeURL);
 		youtubeURL.setColumns(10);
 		
 		JLabel lblNewLabel = new JLabel("Youtube ID/URL:");
 		lblNewLabel.setBounds(10, 15, 93, 21);
 		frmYoutubeGiveawayHelper.getContentPane().add(lblNewLabel);
 		
 		final JCheckBox noDoubleComments = new JCheckBox("No double comments");
 		noDoubleComments.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				contestantBag.setFilterDoubleComments(noDoubleComments.isSelected());
 			}
 		});
 		noDoubleComments.setBounds(275, 47, 149, 23);
 		frmYoutubeGiveawayHelper.getContentPane().add(noDoubleComments);
 		
 		animationFrame = new JPanel();
 		animationFrame.setBounds(10, 77, 514, 230);
 		frmYoutubeGiveawayHelper.getContentPane().add(animationFrame);
 		
 		goButton = new JButton("Go!");
 		goButton.setEnabled(false);
 		goButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
 		goButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (contestantBag.getNumberOfContestants() > 0) {
 					Object[] options = {"Yes use existing data",
                     "No redownload the data"};
 					int n = JOptionPane.showOptionDialog(frmYoutubeGiveawayHelper,
 					    "Would you want to reuse the downloaded data?",
 					    "Reuse allready downloaded data?",
 					    JOptionPane.YES_NO_OPTION,
 					    JOptionPane.QUESTION_MESSAGE,
 					    null,     //do not use a custom Icon
 					    options,  //the titles of buttons
 					    options[0]); //default button title
 					if (n == JOptionPane.YES_OPTION) {
 						progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
 						findWinner();
 					} else {
 						progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
 						contestantBag.emptyBag();
 						Go();
 					}
 				} else {
 					Go();
 				}
 			}
 		});
 		goButton.setBounds(435, 11, 89, 59);
 		frmYoutubeGiveawayHelper.getContentPane().add(goButton);
 		animationFrame.setLayout(null);
 		
 		progress = new JLabel();
 		progress.setBounds(0, 0, 514, 230);
 		progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
 		progress.setHorizontalAlignment(SwingConstants.CENTER);
 		progress.setVerticalAlignment(SwingConstants.CENTER);
 		progress.setHorizontalTextPosition(SwingConstants.CENTER);
 		animationFrame.add(progress);
 		
 		progressBar = new JProgressBar();
 		progressBar.setValue(0);
 		progressBar.setVisible(false);
 		progressBar.setBounds(10, 194, 494, 25);
 		animationFrame.add(progressBar);
 		
 		JButton btnVisLog = new JButton("Show winner log");
 		btnVisLog.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFrame results = new JFrame();
 				results.setBounds(100, 100, 400, 300);
 				results.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				results.getContentPane().setLayout(new BorderLayout(0, 0));
 				
 				JTextArea textArea = new JTextArea();
 				textArea.setText(winnerLog);
 				JScrollPane sp = new JScrollPane(textArea); 
 				results.setVisible(true);
 				results.getContentPane().add(sp, BorderLayout.CENTER);
 			}
 		});
 		btnVisLog.setBounds(10, 47, 132, 23);
 		frmYoutubeGiveawayHelper.getContentPane().add(btnVisLog);
 		
 		lblAuthor = new JLabel("Author");
 		lblAuthor.setBounds(10, 311, 46, 14);
 		frmYoutubeGiveawayHelper.getContentPane().add(lblAuthor);
 		
 		lblHttprobserobdk = new JLabel("http://robserob.dk");
 		lblHttprobserobdk.setBounds(415, 311, 109, 14);
 		frmYoutubeGiveawayHelper.getContentPane().add(lblHttprobserobdk);
 		
 		shouldSkipAnimation = new JCheckBox("Skip animation");
 		shouldSkipAnimation.setBounds(158, 47, 115, 23);
 		frmYoutubeGiveawayHelper.getContentPane().add(shouldSkipAnimation);
 	}
 	
 	private String getYoutubeID() {
 		if (youtubeURL.getText().length() == 11) {
 			return youtubeURL.getText();
 		} else if (youtubeURL.getText().indexOf("?v=") > 0) {
 			int start = youtubeURL.getText().indexOf("?v=")+3;
 			int end = start+11;
 			return youtubeURL.getText().substring(start, end);
 		}		
 		return "";
 	}
 	
 	private void Go() {		
 		progress.setText("Getting the total number of comments.");
 		goButton.setEnabled(false);
 	    try {
 			Response response = asyncHttpClient.prepareGet("http://gdata.youtube.com/feeds/api/videos/"+getYoutubeID()+"/comments").execute().get();
 			String body = response.getResponseBody();
 	    	int startIndex = body.indexOf("<openSearch:totalResults>") + "<openSearch:totalResults>".length();
 	    	int endIndex = body.indexOf("</openSearch:totalResults>");
 	    	numberOfComments = Integer.parseInt(body.substring(startIndex, endIndex));
 	    	progress.setText("<html><body>There are " + numberOfComments + " comments!<br>Downloading them now....</body></html>");
 	    	StartDownload();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void StartDownload() {
 		progressBar.setValue(0);
 		progressBar.setVisible(true);
 		int numberOfPages = (numberOfComments/500)+1;
 		int currentPage = 1;
 		downloadNextPage(numberOfPages, currentPage);
 	}
 	
 	private void downloadNextPage(final int numberOfPages, final int currentPage) {
 		System.out.println("Downloading page number " + currentPage + " out of " + numberOfPages + " pages");
 		try {
 			asyncHttpClient.prepareGet("http://www.youtube.com/all_comments?v="+getYoutubeID()+"&page="+currentPage).execute(new AsyncCompletionHandler<Response>(){
 
 			    @Override
 			    public Response onCompleted(Response response) throws Exception{
 			    	System.out.println("Page " + currentPage + " downloaded!");
 			    	String body = response.getResponseBody();
 			    	byte[] b = body.getBytes("ISO-8859-1");
 			    	body = new String(b, "UTF-8");
 			    	Document doc = Jsoup.parse(body);
 			    	Elements comments = doc.select(".comment:not(.removed)");
 			    	for (Element comment : comments) {
 			    		Element userElement = comment.select(".author a").get(0);
 			    		String profileURL = userElement.attr("href");
 			    		String username = userElement.html();
			    		int messageID = contestantBag.getNumberOfContestants()+1;
			    		Element messageElement = comment.select(".comment-text p").get(0);
 			    		String message = messageElement.html();
 			    		Contestant contestant = new Contestant(username, message, messageID, profileURL);
 			    		contestantBag.addContestant(contestant);
 			    	}
 			    	progress.setText("<html><body>Downloading... "+contestantBag.getNumberOfContestants()+" added to the bag</body></html>");
 			    	if (currentPage < numberOfPages) {
 			    		int progressEachPage = 100/numberOfPages;
 			    		progressBar.setValue(progressEachPage*currentPage);
 			    		downloadNextPage(numberOfPages, currentPage+1);
 			    	}
 			    	else { 
 			    		findWinner();
 			    		progressBar.setVisible(false);
 			    	}
 			        return response;
 			    }
 
 			    @Override
 			    public void onThrowable(Throwable t){
 			    	System.out.println("An error occured oh shit!");
 			    	System.out.println("Just trying again...");
 			    	downloadNextPage(numberOfPages, currentPage);
 			        // Something wrong happened.
 			    }
 			});
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void findWinner() {
 		progress.setText("<html><body>We have "+contestantBag.getNumberOfContestants()+" comments in the bag!</body></html>");
 		ActionListener taskPerformer = new ActionListener() {
 			int commentCount = 0;
 			int fase = 0;
 			Random random = new Random();
 	        public void actionPerformed(ActionEvent evt) {
 	        	if (shouldSkipAnimation.isSelected()) {
 	        		goButton.setEnabled(true);
 	        		Contestant randomContestant = contestantBag.getRandomContestant();
 	        		progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
 	        		progress.setText("<html><body>Congratulations "+randomContestant.username+"!<br>Profile URL: "+randomContestant.profileURL+")<br>You won with the message number "+randomContestant.messageID+":<br>"+randomContestant.message+"</body></html>");
 	        		winnerLog = winnerLog+ randomContestant.username+"\n"+randomContestant.profileURL+"\n\n";
 	        		timer.stop();
 	    		} else {
 		        	if (fase == 0) {
 		        		progress.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		        		if (commentCount > 100) {
 		        			fase=1;
 		        			timer.setDelay(25);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 15));
 		        		}
 		        	} else if (fase == 1) {
 		        		if (commentCount > 75) {
 		        			fase=2;
 		        			timer.setDelay(50);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 17));
 		        		}
 		        	} else if (fase == 2) {
 		        		if (commentCount > 50) {
 		        			fase=3;
 		        			timer.setDelay(100);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 20));
 		        		}
 		        	} else if (fase == 3) {
 		        		if (commentCount > 25) {
 		        			fase=4;
 		        			timer.setDelay(200);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 22));
 		        		}
 		        	} else if (fase == 4) {
 		        		if (commentCount > 10) {
 		        			fase=5;
 		        			timer.setDelay(500);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 25));
 		        		}
 		        	} else if (fase == 5) {
 		        		if (commentCount > random.nextInt(50)) {
 		        			fase=6;
 		        			timer.setDelay(1000);
 		        			commentCount = 0;
 		        			progress.setFont(new Font("Tahoma", Font.PLAIN, 29));
 							timer.stop();
 		        		}
 		        	}
 		        	
 		        	commentCount++;
 		        	if (fase == 6) {	        		
 		        		goButton.setEnabled(true);
 		        		Contestant randomContestant = contestantBag.getRandomContestant();
 		        		progress.setFont(new Font("Tahoma", Font.PLAIN, 16));
 		        		progress.setText("<html><body>Congratulations "+randomContestant.username+"!<br>Profile URL: "+randomContestant.profileURL+")<br>You won with the message number "+randomContestant.messageID+":<br>"+randomContestant.message+"</body></html>");
 		        		winnerLog = winnerLog+ randomContestant.username+"\n"+randomContestant.profileURL+"\n\n";
 		        	} else {
 		        		Contestant randomContestant = contestantBag.getRandomContestant();
 	        			progress.setText("<html><body>"+randomContestant.username+"</body></html>");
 		        	}
 	    		}
 	        }
         };
         timer = new Timer( 10 , taskPerformer);
         timer.setRepeats(true);
         timer.setInitialDelay(2500);
         timer.start();
 	}
 }
