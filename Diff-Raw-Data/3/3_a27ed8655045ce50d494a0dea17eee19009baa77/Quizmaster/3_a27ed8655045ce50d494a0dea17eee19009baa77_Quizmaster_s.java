 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.plugins;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Calendar;
 import java.io.File;
 import java.net.URISyntaxException;
 
 import java.net.URL;
 import java.util.Collections;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Schema;
 import javax.xml.validation.Validator;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.XMLConstants;
 import javax.xml.transform.sax.SAXSource;
 import org.xml.sax.InputSource;
 
 import org.xml.sax.Locator;
 import silvertrout.Channel;
 import silvertrout.User;
 import silvertrout.Modes;
 
 import silvertrout.commons.game.ScoreManager;
 import silvertrout.commons.game.Trophy;
 import silvertrout.commons.game.TrophyManager;
 
 /**
  *
  **
  */
 public class Quizmaster extends silvertrout.Plugin {
 
     private enum State { RUNNING, RUNNING_QUESTION, NOT_RUNNING };
 
     /**
      * Question for the Quizmaster plugin.
      *
      */
     private static class Question {
 		// Location information (for report / debug)
         String file         = null;
 		int    row          = -1;
 		// Category
 		String category     = null;
         // Question and hint        
         String questionLine = null;
         String hintLine     = null;
         // Max attempts 
         int attempts        = 100;
 		// Required amount of answers
         int required        = 1;
         // Number of total hints
         int hintCount       = 7;
         
 		// Hint struct
         class Hint {
             String hint      = null;
             int    scoredec  = 1; //?
         }
         // Hint collection
 		ArrayList<Hint>   hints   = new ArrayList<Hint>();
 		
 		// Answer struct
         class Answer {
             String  answer   = null;
             int     score    = 5;
             boolean required = false;
         }
 		// Answer collection
         ArrayList<Answer> answers = new ArrayList<Answer>();
 
     }
 
     /**
      * QuestionReader reads in question files for the Quizmaster plugin.
      * 
      */
 	private static class QuestionReader {
         /**
          * Question handler to convert xml-questions to java-question.
          */
 		private static class QuestionHandler extends DefaultHandler {
 			
 			private String              category  = null;
 			private Question            question  = null;
 			private ArrayList<String>   tags      = new ArrayList<String>();
 			private ArrayList<Question> questions = new ArrayList<Question>();
 			private Locator             locator   = null;
             private String              file      = null;
 			public QuestionHandler(String file) {
 				super();
                 this.file = file;
 			}
 			
 			public Collection<Question> getQuestions() {
 				return questions;
 			}
 			
 			private String currentTag() {
 				if(tags.size() == 0) {
 					return null;
 				} else {
 					return tags.get(tags.size() - 1);
 				}
 			}
 			
 			private String previousTag() {
 				if(tags.size() < 2) {
 					return null;
 				} else {
 					return tags.get(tags.size() - 2);
 				}
 			}
             @Override
             public void setDocumentLocator(Locator locator)
             {
                 this.locator = locator;
             }
             @Override
 			public void startElement (String uri, String name, String qName, Attributes atts)
 			{
 				tags.add(qName);
 				
 				if(qName.equals("questions")) {
 					category = atts.getValue("category") + " - " + atts.getValue("subcategory");
 				} else if(qName.equals("question")) {
 					question = new Question();
 					question.category = category;
                     question.file     = file;
                     question.row      = locator.getLineNumber();
 				} else if(qName.equals("line")) {
 					if(previousTag().equals("hints")) {
 						if(atts.getValue("hints") != null)question.hintCount = Integer.parseInt(atts.getValue("hints")) + question.hints.size(); 
 					}
 				} else if(qName.equals("answers")) {
 					if(atts.getValue("attempts") != null)question.attempts = Integer.parseInt(atts.getValue("attempts"));
 					if(atts.getValue("required") != null)question.required = Integer.parseInt(atts.getValue("required"));
 				} else if(qName.equals("answer")) {
                     //System.out.println(qName + " = " + atts.getValue("required"));
 					Question.Answer newAns = question.new Answer();
 					if(atts.getValue("required") != null
                             && (atts.getValue("required").equals("1")
                             || atts.getValue("required").equals("true")))newAns.required = true;
 					if(atts.getValue("score") != null)newAns.score = Integer.parseInt(atts.getValue("score"));
 					question.answers.add(newAns);
 				} else if(qName.equals("hints")) {
 				
 				} else if(qName.equals("hint")) {
 					Question.Hint newHint = question.new Hint();
 					if(atts.getValue("score-decrease") != null)newHint.scoredec = Integer.parseInt(atts.getValue("score-decrease"));
 					question.hints.add(newHint);
 				}
 			}
             @Override
 			public void characters(char[] ch, int start, int length) {
 				String data = new String(ch, start, length).trim();
 				if(currentTag().equals("line")) {
 					if(previousTag().equals("question")) {
 						question.questionLine = data;
 					} else if(previousTag().equals("hints")) {
 						question.hintLine = data;
 					}
 				} else if(currentTag().equals("hint")) {
 					question.hints.get(question.hints.size() - 1).hint = data;
 				} else if(currentTag().equals("answer")) {
 					question.answers.get(question.answers.size() - 1).answer = data;
 				}
 			}
 
 
             @Override
 			public void endElement (String uri, String name, String qName)
 			{
 				tags.remove(tags.size() - 1);
 				if(qName.equals("question")) {
 					// TODO: fix stuff
 					questions.add(question);
 				}
 			}
 		}
 	    
 		
 		public static Collection<Question> load(File file) {
 			
 			try {    
 				// Set up schema:
 				String        language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
 				SchemaFactory factory  = SchemaFactory.newInstance(language);
                 String        sPath    = "/silvertrout/plugins/Quizmaster/questions.xsd";
                 URL           sURL     = QuestionHandler.class.getResource(sPath);
 				Schema        schema   = factory.newSchema(sURL);
 				// Validate:
 				Validator validator = schema.newValidator();
 				SAXSource source = new SAXSource(new InputSource(new java.io.FileInputStream(file)));
 				validator.validate(source);
 				// Parse:
 				SAXParserFactory sf = SAXParserFactory.newInstance();
 				sf.setNamespaceAware(true); 
 				sf.setValidating(true);        
 				sf.setSchema(schema);
 				SAXParser       sp = sf.newSAXParser();
 				QuestionHandler qh = new QuestionHandler(file.toString());
 				sp.parse(file, qh);
 				
 				return qh.getQuestions();
 			
 			} catch (SAXParseException e) {
                 System.err.println("Error parsing file " + file);
                 System.err.println("At line " + e.getLineNumber() + ", column " + e.getColumnNumber());
  				System.err.println(e.getMessage());
                 System.err.println("==============================");
                 e.printStackTrace();
             } catch(SAXException e) {
                 System.err.println("Error parsing file " + file);
 				System.err.println(e.getMessage());
                 System.err.println("==============================");
                 e.printStackTrace();
 			} catch(java.io.FileNotFoundException e) {
 				System.err.println(e.getMessage());
                 e.printStackTrace();
 			} catch(java.io.IOException e) {
 				System.err.println(e.getMessage());
                 e.printStackTrace();
 			} catch(javax.xml.parsers.ParserConfigurationException e) {
 				System.err.println(e.getMessage());
                 e.printStackTrace();
 			}
             return null;
 		}
 
         private QuestionReader() {
         }
 	
 	}
 	
     // Settings:
     private final int                  voiceInterval        = 60;
     private final int                  hintTime             = 7;
     private final int                  waitTime             = 3;
     private final int                  rankInterval         = 500;
     
     // Variables:
     
     private final LinkedList<Question> questions = new LinkedList<Question>();
     private final Random               rand      = new Random();
     private ScoreManager               scoreManager;
     private TrophyManager              trophyManager;
     private String                     channelName;
         
     private Question                   question;
     //private String                     currentAnswerString;
     private int                        currentHint = 0;
 
     private String[]                   grad = 
             {
             "\"Tought\"", "Cell", "Egg", "Embryo", "Fetus", "Neonate", 
             "Toddler", "Child", 
             
             "Preschooler", "Lower Primary School Student", 
             "Upper Primary School Student", "Lower Secondary School Student",
             "Upper Secondary School Student", "Bachelor Student", 
             "Master Student", 
             
             "Volunteer", "Intern", "Receptionist", "Personal Secretary", 
             "Personal Assistant", "Clerk", "Executive Secretary", 
             "Executive Assistant", "Foreman", "Supervisor", "Manager",
             "Superintendent",
             
             "Associate Vice President", "Senior Vice President", 
             "Executive Vice President", "Chief Officer", 
             "Chief Executive Officer", "Chairman of the Board",
             
             "Apprentice", "Apprentice-Companion", "Brother",
             "Commander", "Master", "Grand Master"
             };
             
 
     
     private int                        startTime;
     private int                        currentTime;
     private int                        endTime;
     
     private int                        statTime             = 0;
     
     private int                        unanswerdQuestions   = 0;
     
     private int                        answerStreak         = 0;
     private String                     answerStreakNickname = new String();
     
     private long                       startMiliTime;
     
     private State                      state                = State.NOT_RUNNING;
 
     /**
      *
      */
     public Quizmaster() {
 
         String scoresPath   = "/silvertrout/plugins/Quizmaster/Scores/Scores";
         String trophiesPath = "/silvertrout/plugins/Quizmaster/Trophies";
 
         try {
             scoreManager  = new ScoreManager(new File(this.getClass().getResource(scoresPath).toURI()));
             trophyManager = new TrophyManager(new File(this.getClass().getResource(trophiesPath).toURI()));
         } catch(URISyntaxException e) {
             e.printStackTrace();
         }
     }
     
     /** Award trophy to user.
      *
      * @param t
      * @param nick
      */
     public void awardTrophy(Trophy t, String nick) {
         if(!trophyManager.haveTrophy(t, nick)) {
             trophyManager.addTrophy(t, nick);
             getNetwork().getConnection().sendPrivmsg(channelName, nick 
                 + ": Du har fått en trofé - " + t.getName());
         }
     }
     
     /**
      *
      * @param nick
      * @param score
      */
     public void awardScore(String nick, int score) {
         // Calculate answer time, in seconds:
         long miliSec = Calendar.getInstance().getTimeInMillis() - startMiliTime;
         double time  = ((double)miliSec / 1000.0);
         
         // Calculate winning streak
         if(answerStreakNickname.equals(nick)) {
             answerStreak++;
         } else {
             answerStreakNickname = nick;
             answerStreak         = 1;
         }
         
         // Update scores:
         int oldScore = scoreManager.getTotalScore(nick);
         int oldPos   = scoreManager.getPosition(nick);
         scoreManager.addScore(nick, question.category, score);
         int newScore = scoreManager.getTotalScore(nick);
         int newPos   = scoreManager.getPosition(nick);
 
         // New rank
         if(oldScore / rankInterval < newScore / rankInterval)
         {
             getNetwork().getConnection().sendPrivmsg(channelName,
                     "Utmärkt jobbat! Din nya rank är: " 
                     + grad[newScore / rankInterval]);
         }
 
         // Print message
         String msg = "Rätt svar var \"" + question.hintLine + "\". ";
         if(answerStreak >= 3)    msg += "(" + answerStreak + " i rad) ";
         if(oldPos == -1)         msg += "(In på listan på placering " + newPos + ") ";
         else if(oldPos < newPos) msg += "(Upp " + (newPos-oldPos) + " placering(ar)) ";
         msg += nick + " (" + time +" sek) fick " + score + "p och har nu " + newScore +"p.";
         getNetwork().getConnection().sendPrivmsg(channelName, msg);
 
         // Check for trophies won
         int year  = Calendar.getInstance().get(Calendar.YEAR);
         int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
         int day   = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
         
         int hour  = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
         int min   = Calendar.getInstance().get(Calendar.MINUTE);
         // First blood trophy
         if(oldScore == 0 && newScore >= 1)
             awardTrophy(trophyManager.getTrophy("First Blood"), nick);
         // Speedster trophy
         if(time < 3.0 && question.hintLine.length() > 5)
             awardTrophy(trophyManager.getTrophy("Speedster"), nick);
         // Chain Reaction
         if(answerStreak >= 5)
             awardTrophy(trophyManager.getTrophy("Chain Reaction"), nick);
         // Chain Overload
         if(answerStreak >= 10)
             awardTrophy(trophyManager.getTrophy("Chain Overload"), nick);
         // Chain Overdose
         if(answerStreak >= 30)
             awardTrophy(trophyManager.getTrophy("Chain Overdose"), nick);
         // Elite!
         if(oldScore < 1337 && newScore >= 1337)
             awardTrophy(trophyManager.getTrophy("Elite!"), nick);
         // Top Ten
         if(newScore > 100 && newPos <= 10)
             awardTrophy(trophyManager.getTrophy("Top Ten"), nick);
         // Top Three
         if(newScore > 300 && newPos <= 3)
             awardTrophy(trophyManager.getTrophy("Top Three"), nick);
         // Top Dog
         if(newScore > 1000 && newPos == 1)
             awardTrophy(trophyManager.getTrophy("Top Dog"), nick);
         // Säg ett datum, vilket som helst!
         if(month == 5 && day == 29)
             awardTrophy(trophyManager.getTrophy("Säg ett datum, vilket som helst!"), nick);
         // Endurance Master
         if(question.hintLine.length() >= 30)
             awardTrophy(trophyManager.getTrophy("Endurance Master"), nick);
     }
 
     /**
      *
      * @param categories
      */
     public void newRound(java.util.Collection<String> categories) {
         try{
             File qdir = new File(this.getClass().getResource(
                     "/silvertrout/plugins/Quizmaster/Questions/").toURI());
 
             for(File d: qdir.listFiles()) {
                 //System.out.println("Begin checking directory: " +d.getName());
                 if(categories == null || categories.contains(d.getName())) {
                     if(d.isDirectory()) {
                         //System.out.println("Checking directory: " +d.getName());
                         for(File f: d.listFiles()) {
                             if(f.getName().endsWith(".xml")) {
                                 System.out.println("Loading questions from " + f.getName());
                                 Collection<Question> qss = QuestionReader.load(f);
                                 questions.addAll(qss);
								//TODO
                                 //System.out.println("Added file: " + f.getName());
                             }
                         }
                     }
                 }
             }
             // Suffle the questions
             Collections.shuffle(questions);
             
             getNetwork().getConnection().sendPrivmsg(channelName, "En ny omgång"
                     + " startas. Totalt finns " + questions.size() + " frågor.");
             
             unanswerdQuestions = 0;
             state              = State.RUNNING;
             newQuestion();
         } catch(URISyntaxException e) {
             e.printStackTrace();
         }
     }
     
     /**
      *
      */
     public void endRound() {
         getNetwork().getConnection().sendPrivmsg(channelName, "Omgången är"
                 + " slut. Skriv !start för att starta en ny omgång.");
         state = State.NOT_RUNNING;
     }
     
     /**
      *
      */
     public void newQuestion() {
         // Pop of question from queue
         try {
             question = questions.removeFirst();
         } catch(java.util.NoSuchElementException e) {
             endRound();
             return;
         }
         // Construct hint line from answers (if there is none)
         if(question.hintLine == null) {
             int requiredAnswers = 0;
             question.hintLine = "";
             for(Question.Answer answer: question.answers) {
                 if(answer.required)requiredAnswers++;
             }
             for(Question.Answer answer: question.answers) {
                 if(answer.required) {
                     question.hintLine += answer.answer + " ";
                 } else if(requiredAnswers >= 0) {
                     requiredAnswers--;
                     question.hintLine += answer.answer + " ";
                 }
             }
             question.hintLine = question.hintLine.trim();
         }
 
         // Construct hints from hint line (if there are hints missing)
         // TODO, improve
         int generate = question.hintCount - question.hints.size();
         //System.out.println("Generating " + generate + " questions (-1, all dots)");
         if(generate > 0) {
             int chars = 0;
             String base = new String();
 
             for(int i = 0; i < question.hintLine.length(); i++) {
                 if(Character.isLetterOrDigit(question.hintLine.charAt(i))) {
                     chars++;
                     base += '.';
                 } else {
                     base += question.hintLine.charAt(i);
                 }
             }
             //System.out.println("hintline contains " + chars + " chars");
             // Add base (only dots)
             Question.Hint h = question.new Hint();
             h.hint = base;
             question.hints.add(h);
             generate--;
 
             final double percentage = 0.70;
 
             double reveal     = ((double)chars * percentage) / (double)generate;
             double revealLeft = 0;
             //System.out.println("revealing " + reveal + ", r+rl: " + (reveal + revealLeft));
             for(int g = 0; g < generate; g++) {
                 for(int r = 1, b = 0; r < reveal + revealLeft && b < 100;) {
                     int index = rand.nextInt(question.hintLine.length());
                     if(base.charAt(index) == '.') {
                         base = base.substring(0, index) + question.hintLine.charAt(index) + base.substring(index + 1);
                         r++;
                     }
                     b++;
                 }
                 
                 if(reveal + revealLeft > 1) {
                     Question.Hint hi = question.new Hint();
                     hi.hint     = base;
                     question.hints.add(hi);
                     //System.out.print("(Added) ");
                 }
                 //System.out.print(base + " - old left " + revealLeft + ", reveal " + reveal);
                 revealLeft = reveal + revealLeft - Math.floor(reveal + revealLeft);
                 //System.out.println(" - new left " + revealLeft);
             }
             //System.out.println("y");
         }
         //System.out.println("x");
         currentHint   = 0;
         startTime     = currentTime;
         startMiliTime = Calendar.getInstance().getTimeInMillis();
         getNetwork().getConnection().sendPrivmsg(channelName, "" + "[" 
                 + question.category + "] " + question.questionLine);
         state   = State.RUNNING_QUESTION;
     }
     
     
     /**
      *
      * @param answered - true iff the question was correctly answered
      */
     public void endQuestion(boolean answered) {
         if(!answered) {
             getNetwork().getConnection().sendPrivmsg(channelName, "Rätt svar"
                     + " var \"" + question.hintLine + "\". Ingen"
                     + " lyckades svara rätt.");
             unanswerdQuestions++;
             answerStreak = 0;
         } else {
             unanswerdQuestions = 0;
         }
         endTime = currentTime;
         state   = State.RUNNING;
     }
     
     /**
      *
      * @param sender
      */
     public void printStats(String sender) {
         String               topten  = new String();
         String               lastone = "Du har inga poäng ='(";
         ScoreManager.Score[] topList = scoreManager.getTop(10);
         
         if(topList.length == 0) {
             topten  = "Ingen är på listan än. Quizza hårdare!";
             lastone = "";
         }
         
         for(int i = 0; i < topList.length; i++) {
             if(topList[i].nick.equals(sender))
                 lastone = printNick(sender) +" har " + topList[i].getTotalScore() + ". Bra jobbat!";
             topten += "#" + (i+1) + " " + topList[i].nick + " " + topList[i].getTotalScore() + "p - ";
         }
         
         
         if(topList.length > 10) {
             int pos   = scoreManager.getPosition(sender);
             int score = scoreManager.getTotalScore(sender);
             if(pos != -1)lastone = "Du har " + score 
                     + " och ligger på placering #" + pos;
         }
         getNetwork().getConnection().sendPrivmsg(channelName, "Top 10: " 
                 + topten + lastone);
     }
     
     @Override
     public void onPrivmsg(User user, Channel channel, String message) {
 
         if(channel != null && channel.getName().equalsIgnoreCase(channelName)) {
 
             if(state == State.RUNNING_QUESTION) {
                 // Answer to question
                 String uanswer      = message.toLowerCase().trim();
                 int    uanswerCount = 0;
                 int    score        = 0;
                 for(Question.Answer answer: question.answers) {
                     // TODO: check surroundings, only non alphanum char surrounds word
                     String cans = answer.answer.toLowerCase();
                     int pos = uanswer.indexOf(cans);
                     char before = pos <= 0 ? ' ': uanswer.charAt(pos - 1);
                     char after  = pos + cans.length() >= uanswer.length() ? ' ': uanswer.charAt(pos + cans.length());
                     
                     //System.out.println(cans + "/" + uanswer + " = " + pos + ", " + before + ", " + after);
                     
                     if(pos >= 0 && !Character.isLetterOrDigit(before) && 
                                 !Character.isLetterOrDigit(after)) { 
                         score += answer.score;
                         uanswerCount++;
                         
                     } else {
                         if(answer.required) {
                             //getNetwork().getConnection().sendPrivmsg(channelName, "missing req answer: " + answer.answer);
                             return;
                         }
                     }
                 }
                 if(uanswerCount >= question.required) {
                     for(int i = 0; i < currentHint; i++)
                        score -= question.hints.get(i).scoredec;
                     awardScore(user.getNickname(), Math.max(1, score));
                     endQuestion(true);
 
                 } else {
                     //getNetwork().getConnection().sendPrivmsg(channelName, "req answer " + question.required + " > " + uanswerCount);
                 }
 
             } else if(state == State.NOT_RUNNING) {
                 // Start new round
                 if(message.startsWith("!start")) {
                     String[] cat = message.substring(6).split("\\s");
                     newRound(null);
                 }
             }
             
             if(message.equals("!stats")) {
                 if(currentTime - statTime > 20) {
                     printStats(user.getNickname());
                     statTime = currentTime;
                 }
             }
             else if(message.equals("!help")) {
                 if(currentTime - statTime > 20)
                     getNetwork().getConnection().sendPrivmsg(channelName, 
                               user.getNickname()
                             +  ", Skriv !start för att starta och !stats för"
                             +  "att se tio i topp-listan och din egna poäng. För"
                             + " att titta vilka troféer du har kan du använda"
                             + " !trophies. Om du vill visa denna hjälp, skriv"
                             + " !help.");
             }
             else if(message.startsWith("!suggest")) {
                 // TODO!
             }
             else if(message.startsWith("!report")) {
                 // TODO!
             }
             else if(message.equals("!trophies")) {
             
                 int have = trophyManager.getTrophies(user.getNickname()).size();
                 int tot    = trophyManager.getTrophies().size();
             
                 String msg = user.getNickname() + ", Du har troféerna: ";
                 for(Trophy t: trophyManager.getTrophies(user.getNickname())) {
                     msg += t.getName() + ", ";
                 }
                 msg += have + "/" + tot + " - forsätt samla!";
                 getNetwork().getConnection().sendPrivmsg(channelName, msg);
             }
             else if(message.equals("!listtrophies")) {
             
                 int tot    = trophyManager.getTrophies().size();
                 String msg = user.getNickname() + ", Följande troféer finns: ";
                 for(Trophy t: trophyManager.getTrophies()) {
                     msg += t.getName() + ", ";
                 }
                 msg +=tot + " stycken - samla alla!";
                 getNetwork().getConnection().sendPrivmsg(channelName, msg);
             }
         }
     }
     
     /**
      *
      */
     public void giveHint() {
 
         if(currentHint == question.hints.size()) {
             //getNetwork().getConnection().sendPrivmsg(channelName, "err, to few hints");
             endQuestion(false);
         } else {
             Question.Hint currentHintObj = question.hints.get(currentHint);
             getNetwork().getConnection().sendPrivmsg(channelName, currentHintObj.hint);
             currentHint++;
         }
 
     }
     
     @Override
     public void onTick(int ticks) {
         currentTime = ticks;
         //System.out.println(currentTime + ": " + state);
         if(state == State.RUNNING_QUESTION) {        
             // If we have a question that no one have answered in a while
             if(currentTime > startTime + hintTime * question.hintCount) {
                 endQuestion(false);
                 if(unanswerdQuestions >= 5) { 
                     endRound();
                 } else {
                     newQuestion();
                 }
             // Or if it is time to give a hint
             } else if(currentTime - startTime == hintTime * question.hintCount) {
                 endQuestion(false);
             } else if((currentTime - startTime) % hintTime == 0) {
                 giveHint();
             }         
         
         } else if(state == State.RUNNING) {
             // Time for a new question
             if(currentTime - endTime == waitTime) {
                 newQuestion();
             }            
         }
         
         // Do every minute
         if(ticks % voiceInterval == 0) {
             
             // Only voice if we are in the channel and are an operator
             if(getNetwork().existsChannel(channelName)) {
             
                 Channel channel  = getNetwork().getChannel(channelName);
                 User    myUser   = getNetwork().getMyUser();
                 boolean operator = channel.getUsers().get(myUser).haveMode('o');
                 
                 if(operator) {
                     LinkedList<String> f     = new LinkedList<String>();
                     Map<User, Modes>   users = channel.getUsers();
                     
                     for(User u: users.keySet()) {
                         if(users.get(u).haveMode('v')){
                             // the user do have voice
                             if(!scoreManager.isTop(10, u.getNickname()))
                                 f.add("-v " + u.getNickname() + " ");
                         } else {
                             // the user do not have voice
                             if(scoreManager.isTop(10, u.getNickname()))
                                 f.add("+v " + u.getNickname() + " ");
                         }
                     }
                     
                     for(int i = 0; i < f.size(); i += 4) {
                         String m = new String();
                         for(int j = i; j < f.size() && j < i + 4; j++) {
                             m += f.get(j);
                         }
                         getNetwork().getConnection().sendRaw("MODE " + channelName + " " + m);
                     }
                 }
             }
         }
     }
     
     private String printNick(String nick){
 
         int s = scoreManager.getTotalScore(nick) / rankInterval;
         if(s > 0) {
             if(s > grad.length)
                 return grad[grad.length-1] + " " + nick;
             else if(s > 0)
                 return grad[s-1] + " " + nick;
         }
         return nick;
     }
     
     @Override
     public void onConnected() {
         // Join quiz channel:
         if(!getNetwork().existsChannel(channelName)) {
             getNetwork().getConnection().join(channelName);
         }
     }
     
     @Override
     public void onLoad(Map<String,String> settings){
         channelName = settings.get("channel");
         if(channelName == null || !channelName.startsWith("#")) channelName = "#superquiz";
     }
 }
