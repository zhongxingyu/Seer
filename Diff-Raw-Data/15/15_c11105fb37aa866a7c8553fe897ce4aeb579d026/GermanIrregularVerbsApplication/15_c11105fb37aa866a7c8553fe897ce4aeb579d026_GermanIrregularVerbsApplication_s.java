 package com.jozeflang.android.germanirregularverbs.main;
 
 import java.util.logging.Logger;
 
 import com.jozeflang.android.germanirregularverbs.db.VerbProvider;
 
 import android.app.Application;
 
 /** 
 * Entry point activity
  * @author Jozef Lang (developer@jozeflang.com)
  */
 public class GermanIrregularVerbsApplication extends Application {
 
 	private final Logger logger = Logger.getLogger(GermanIrregularVerbsApplication.class.getName());
 	
 	private VerbProvider verbProvider;
 	private Question activeQuestion;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		verbProvider = new VerbProvider(this);
 		
 		// Generate first question
 		getQuestion(true);
 	}
 
 	@Override
 	public void onTerminate() {
 		verbProvider.closeProvider();
 	}
 	
 	public VerbProvider getVerbProvider() {
 		return verbProvider;
 	}
 	
 	public Question getQuestion() {
 		return activeQuestion;
 	}
 	
 	public Question getQuestion(boolean newQuestion) {
 		if (newQuestion) {
 			logger.info(String.format("Generating new question"));
 			activeQuestion = verbProvider.getQuestion();
 		}
 		return activeQuestion;
 	}
 	
 }
