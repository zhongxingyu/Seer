 package com.example.reflections;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.Random;
 import android.util.DisplayMetrics;
 
 
 class Quote {
 	
 	String  quote = "";
 	int active = 0;
 	
 	Quote(String q,int a) {
 		
 		quote = q;
 		active = a;
 		
 	}
 	
 }
 
 class savedState {
 	
 	int quote = -1;
 	
 }
 
 public class reflections extends Activity {
 	
 	int currentQuote = -1;
 	Quote[] quotes;
 	
 	public static final int INSERT_ID = Menu.FIRST;
 	
 	
 	public void showRandomQuote(){
 		
 		final Random myRandom = new Random();
 		
 		this.currentQuote = myRandom.nextInt(quotes.length);
 		
 		DisplayMetrics dm = new DisplayMetrics(); 
     	getWindowManager().getDefaultDisplay().getMetrics(dm); 
     	int screenHeight = (int) dm.heightPixels;
     	int screenWidth = (int) dm.widthPixels;
     	
     	if (screenHeight > screenWidth){
     		setContentView(R.layout.main_portrait);
     	} else {
     		setContentView(R.layout.main);
     	}
     	
     	TextView labelText = (TextView) findViewById(R.id.label_edittext);
     	
     	labelText.setText(quotes[this.currentQuote].quote);	
 
     	this.setupTriggers("All");
     	
 	}
 	
 	public void showNextQuote(){
 		
 		if (this.currentQuote + 1 == quotes.length)
   			return;
 		
 		this.currentQuote++;
   		
 		DisplayMetrics dm = new DisplayMetrics(); 
     	getWindowManager().getDefaultDisplay().getMetrics(dm); 
     	int screenHeight = (int) dm.heightPixels;
     	int screenWidth = (int) dm.widthPixels;
     	
     	if (screenHeight > screenWidth){
     		setContentView(R.layout.main_portrait);
     	} else {
     		setContentView(R.layout.main);
     	}
     	
     	TextView labelText = (TextView) findViewById(R.id.label_edittext);
     	
     	labelText.setText(quotes[this.currentQuote].quote);	
     	
     	
     	this.setupTriggers("All");
     	
 	}
 	
 	public void showPrevQuote(){
 		
 		if (this.currentQuote == 0) 
     		return;
 		
 		this.currentQuote = this.currentQuote - 1 ;
     	
     	
 		DisplayMetrics dm = new DisplayMetrics(); 
     	getWindowManager().getDefaultDisplay().getMetrics(dm); 
     	int screenHeight = (int) dm.heightPixels;
     	int screenWidth = (int) dm.widthPixels;
     	
     	if (screenHeight > screenWidth){
     		setContentView(R.layout.main_portrait);
     	} else {
     		setContentView(R.layout.main);
     	}
 
     	
     	TextView labelText = (TextView) findViewById(R.id.label_edittext);
     	
     	labelText.setText(quotes[this.currentQuote].quote);	
 	
     	this.setupTriggers("All");
     	
 	}	
 	
 	public void showCurrentQuote(){
 		
 		DisplayMetrics dm = new DisplayMetrics(); 
     	getWindowManager().getDefaultDisplay().getMetrics(dm); 
     	int screenHeight = (int) dm.heightPixels;
     	int screenWidth = (int) dm.widthPixels;
     	
     	if (screenHeight > screenWidth){
     		setContentView(R.layout.main_portrait);
     	} else {
     		setContentView(R.layout.main);
     	}
 
     	
     	TextView labelText = (TextView) findViewById(R.id.label_edittext);
     	
     	labelText.setText(quotes[this.currentQuote].quote);	
 	
     	this.setupTriggers("All");
     	
 	}
 	
 	public void showAboutUs(){
 		
		
 		setContentView(R.layout.about_us);
 		this.setupTriggers("Random");
     	
 	}	
 	
 	public void setupTriggers(String ButtonsToTrigger) {
 		
         /* button to go to the previous view */
 		// if(strVowels.compareTo("AEIOU") == 0)
 		if (ButtonsToTrigger.compareTo("Previous") ==0  || ButtonsToTrigger.compareTo("All") == 0 ) {
 	        final Button buttonPrev = (Button) findViewById(R.id.prev_button_id);
 	        buttonPrev.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the previous button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showPrevQuote();
 	            }
 	        });        
 	        
 	        
 	        final Button buttonPrevBottom = (Button) findViewById(R.id.prev_button_id_bottom);
 	        buttonPrevBottom.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the previous button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showPrevQuote();
 	            }
 	        }); 
 		}
         
         /* button to go to the next view */
 		if (ButtonsToTrigger.compareTo("Next") ==0  || ButtonsToTrigger.compareTo("All") == 0 ) {
 	        final Button button = (Button) findViewById(R.id.next_button_id);
 	        button.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the next button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showNextQuote();
 	            }
 	        });		
 	        
 	        final Button buttonNextBottom = (Button) findViewById(R.id.next_button_id_bottom);
 	        buttonNextBottom.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the next button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showNextQuote();
 	            }
 	        });	
 		}
 		
         /* button to go to the next random view */
 		if (ButtonsToTrigger.compareTo("Random") ==0  || ButtonsToTrigger.compareTo("All") == 0 ) {
 	        final Button buttonRandom = (Button) findViewById(R.id.random_button_id);
 	        buttonRandom.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the next button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showRandomQuote();
 	            }
 	        });		
 	        
 	        
 	        if (ButtonsToTrigger.compareTo("All") == 0 ) {
 		        final Button buttonRandomBottom = (Button) findViewById(R.id.random_button_id_bottom);
 		        buttonRandomBottom.setOnClickListener(new View.OnClickListener() {
 		            public void onClick(View v) {
 		                // Perform action on click
 		            	// Toast.makeText(reflections.this, "You have clicked the next button - good on you", Toast.LENGTH_SHORT).show();
 		            	reflections.this.showRandomQuote();
 		            }
 		        });
 	        }
 		}
 		
 		if (ButtonsToTrigger.compareTo("Random") ==0 ) {
 	        final Button buttonBack = (Button) findViewById(R.id.back_button_id);
 	        buttonBack.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                // Perform action on click
 	            	// Toast.makeText(reflections.this, "You have clicked the next button - good on you", Toast.LENGTH_SHORT).show();
 	            	reflections.this.showCurrentQuote();
 	            }
 	        });	
 			
 		}
 		
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		
 		int currentQuote = this.currentQuote;
 		
 		final savedState currentState = new savedState();
 		
 		currentState.quote = this.currentQuote;
 		
 	    return currentState;
 	}
 	
     /** Called when the activity is first created or when application changes orientation */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     
     	
     	// setup a basic array
     	
     	quotes = new Quote[] {
     			new Quote("The Master was allergic to ideologies. \n\n'In a war of ideas' he said, 'it is people who are the casualties.' \n\nLater he elaborated: 'People kill for money or for power. But the most ruthless murderers are those who kill for their ideas.\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			// new Quote("",1),
     			new Quote("To be a good person you have to think bad thoughts but choose not to follow through with the associated action. A person who does not think bad thoughts is merely innocent or naive.",1),
    			new Quote("There are 4 steps to committing an atrocity:\n\n1/ You have to label yourself\n\n2/ You label other people as not like you - 'outsiders'\n\n3/ You start to forget that these 'outsiders' are human beings as well - just like you... And you forget that you would be horrified if something like this was going to happen to your family. \n\nA lot of people stop at this step. They might not actively participate but would accept that this is just.\n\n4/ The atrocity happens, perpetuated by people who are so strong in their beliefs and labelling that they can wipe away the simple fact that all people are human beings. It's funny, kind of, that if the 'outsider' did this atrocity to you and your family then justice would be on your side to stop them from doing it again.\n\n\nHow do you stop this? It's very easy and very hard. You have to remove the labels from yourself. That's very difficult...",1),
     			new Quote("'When you speak about Reality,' said the Master, 'you are attempting to put the Inexpressible into words, so your words are certain to be misunderstood. Thus people who read that expression of Reality called the Scriptures become stupid and cruel for they follow, not their common sense, but what they think their Scriptures say.' \n\nHe had the perfect parable to show this: \n\nA village blacksmith found an apprentice willing to work hard at low pay. The smith immediately began his instructions to the lad: 'When I take the metal out of the fire, I'll lay it on the anvil; and when I nod my head you hit it with the hammer.' The apprentice did precisely what he thought he was told. Next day he was the village blacksmith\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'What is the secret of your serenity? \n\nSaid the Master 'Wholehearted cooperation with the inevitable.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'Name one practical, down-to-earth effect of spirituality,' said the skeptic who was ready for an argument. \n\n'Here's one,' said the Master. 'When someone offends you, you can raise your spirits to heights where offenses cannot reach.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("Be aware of what is actually happening in Reality instead of what is happening within your head. \n\nPeople tend to project their strengths and weaknesses onto others by assuming they think the exact same way. They don't. \n\nIf you want to know what someone is thinking the adult way is to ask them.",1),
     			new Quote("The Master once referred to the Hindu notion that all creation is 'leela' — God's play — and the universe is his playground. The aim of spirituality, he claimed, is to make all life play.\n\nThis seemed too frivolous for a puritanical visitor. 'Is their no room then for work?'\n\n'Of course there is. But work becomes spiritual only when it is transformed into play.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'What is my identity?'\n\n'Nothing,' said the Master.\n\n'You mean that I am an emptiness and a void?' said the incredulous disciple.\n\n'Nothing that can be labeled.' said the Master.\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("The master enjoined not austerity, but moderation. If we truly enjoyed things, he claimed, we would be spontaneously moderate.\n\nAsked why he was so opposed to ascetical practices, he replied, 'Because they produce pleasure-haters who always become people-haters — rigid and cruel.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("Slow down. Make your everyday actions deliberate by being in the moment and you might make your tension about the future disappear.",1),
     			new Quote("Segregation goes hand in hand with labels. Label yourself and you inherently start to segregate people. This can lead to atrocities if it is taken far enough.\n\nThe only label you can give yourself that will not create segregation with other people is if you label yourself a human being.",1),
     			new Quote("It is adult behaviour to ask for something you want.  It is also adult behaviour to accept the situation of someone refusing your request.",1),
     			new Quote("It is adult behaviour to voice your opinion.  It is also adult behaviour to accept if no-one agrees with your opinion.",1),
     			new Quote("The way to do more good deeds is to slow down enough to have the spare time to do them.",1),
     			new Quote("A religious belief… is not a statement about Reality, but a hint, a clue about something that is a mystery, beyond the grasp of human thought. In short, a religious belief is only a finger pointing to the moon. \n\nSome religious people never get beyond the study of the finger. \n\nOthers are engaged in sucking it. \n\nOthers yet use the finger to gouge their eyes out. These are the bigots whom religion has made blind. \n\nRare indeed is the religionist who is sufficiently detached from the finger to see what it is indicating — these are those who, having gone beyond belief, are taken for blasphemers.\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'What can I do to see Reality as it is?'\n\nThe master smiled and said, 'I have good news and bad news for you, my friend.'\n\n'What's the bad news?'\n\n'There's nothing you can do to see — it is a gift.'\n\n'And what's the good news?'\n\n'There's nothing you can do to see — it is a gift.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'I seek the meaning of existence.' said the stranger. \n\n'You are of course, assuming.' said the Master, 'that existence has a meaning.'\n\n'Doesn't it?'\n\n'When you experience existence as it is — not as you think it is — you will discover that your question has no meaning.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("'Isn't there such a thing as social liberation?' \n\n'Of course there is,' said the Master.\n\n'How would you describe it?'\n\n'Liberation from the need to belong to the herd.'\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("One always treads with a joyful step when one has dropped the burden called the ego.\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("Happiness is our natural state. Happiness is the natural state of little children, to whom the kingdom belongs until they have been polluted and contaminated by the stupidity of society and culture. \n\nTo acquire happiness you don't have to do anything, because happiness cannot be acquired. Does anybody know why? Because we have it already. \n\nHow can you acquire what you already have? Then why don't you experience it? Because you've got to drop something. You've got to drop illusions. You don't have to add anything in order to be happy; you've got to drop something. \n\nLife is easy, life is delightful. It's only hard on your illusions, your ambitions, your greed, your cravings. Do you know where these things come from? From having identified with all kinds of labels!\n\n -- Anthony De Mello, Awareness : The Perils and Oppurtunities of Reality",1),
     			new Quote("Suffering is a sign that you're out of touch with the truth. \n\nSuffering is given to you that you might open your eyes to the truth, that you might understand that there's falsehood somewhere, just as physical pain is given to you so you will understand that there is disease or illness somewhere. \n\nSuffering points out that there is falsehood somewhere. Suffering occurs when you clash with reality. \n\nWhen your illusions clash with reality when your falsehoods clash with the truth, then you have suffering. \n\nOtherwise there is no suffering.\n\n -- Anthony De Mello, Awareness : The Perils and Oppurtunities of Reality",1),
     			new Quote("Step by step, let whatever happens happen. Real change will come when it is brought about, not by your ego, but by reality. Awareness releases reality to change you.\n\n -- Anthony De Mello, Awareness : The Perils and Oppurtunities of Reality",1),
     			new Quote("As soon as you look at the world through an ideology you are finished. No reality fits an ideology. Life is beyond that. \n\nThat is why people are always searching for a meaning to life. But life has no meaning; it cannot have meaning because meaning is a formula; meaning is something that makes sense to the mind. \n\nEvery time you make sense out of reality, you bump into something that destroys the sense you made. \n\nMeaning is only found when you go beyond meaning. \n\nLife only makes sense when you perceive it as mystery and it makes no sense to the conceptualizing mind.\n\n -- Anthony De Mello, Awareness : The Perils and Oppurtunities of Reality",1),
     			new Quote("This is what Wisdom means: To be changed without the slightest effort on your part, to be transformed, believe it or not, merely by waking to the reality that is not words, that lies beyond the reach of words. \n\nIf you are fortunate enough to be Awakened thus, you will know why the finest language is the one that is not spoken, the finest action is the one that is not done and the finest change is the one that is not willed.\n\n -- Anthony De Mello, One Minute Nonsense",1),
     			new Quote("To a disciple who was forever complaining about others the Master said, 'If it is peace you want, seek to change yourself, not other people. It is easier to protect your feet with slippers than to carpet the whole of the earth.'\n\n -- Anthony De Mello, One Minute Wisdom",1),
     			new Quote("To a visitor who asked to become his disciple the Master said, 'You may live with me, but don't become my follower.'\n\n'Whom, then, shall I follow?' \n\n'No one. The day you follow someone you cease to follow Truth.'\n\n -- Anthony De Mello, One Minute Wisdom",1),
     			new Quote("'You are only a disciple because your eyes are closed. The day you open them you will see there is nothing you can learn from me or anyone.'\n\n'What then is a Master for?' \n\n'To make you see the uselessness of having one.'\n\n -- Anthony De Mello, One Minute Wisdom",1),
     			new Quote("When you are guilty, it is not your sins you hate but yourself.\n\n -- Anthony De Mello, One Minute Wisdom",1),
     			new Quote("When you come to see you are not as wise today as you thought you were yesterday, you are wiser today.\n\n -- Anthony De Mello, One Minute Wisdom",1),
     			new Quote("What would you do if you knew you were going to die tomorrow? \n\nWhat would you do if you knew you were going to die next week? \n\nWhat would you do if you were going to die next year? \n\nWhat would you do if you didn't know you were going to die? \n\nBecause not knowing when you are going to die is the situation you are currently in.",1),
     			new Quote("Just remember it is whatever you want it to be. \n\nLife, parenting, school, work, relationships. \n\nYou can either choose to consider it a boon or a burden. It's whatever you want it to be.",1),
     			new Quote("Expectations that are not met turns into annoyance, frustration and anger. Change the expectations and you change the outcome.",1),
     			new Quote("How can you want more than to be content? If you are not content with being content you will never be content.",1),
     			new Quote("People tend to have this idea that children are controllable. But no one can be controlled without their permission.  The best you can do is to provide a good influence through your behaviour and discussions and hope for the best.",1),
     			new Quote("If your highs are very high then your lows are probably very low. Moderation in an emotional sense can bring stability to your life and your relationships.",1),
     			new Quote("Comparing yourself to others is a one way street to disappoinment.  Compare yourself to where you were beforehand - that is the only sane way to judge your performance",1),
     			new Quote("Be careful if you are a rescuer as you can end up treating the other person as a victim. Also, by trying to help to gain a reward of thanks you are increasing your chances of being bitter if no thanks arrives. Sometimes this can even turn you into a persecuter.  \n\nInstead, only help if you recognise that each person has the ability to fix their own problems and you don't expect to be thanked but still want to help.",1),
     			new Quote("Make a decision when you are calm, composed and know what your long term goals are. It may not always be the right decision but it will be the best you can do at the time. \n\nRemember to review your decisions regularly too. A former bad decision can be made worse if it's not recognised and changed.",1),
     			new Quote("Recognising you are going to die isn't being pessimistic, it is being realistic.\n\nMost people try to avoid this - but eventually they have to face it all at once with the death of a loved one or their own mortality.",1),
     			new Quote("The real test of a holy book is to read it out in it's entirety to it's believers without their knowlege of who wrote the book, but change the words to say the exact opposite.  If the believers think this 'new' book is good, then it probably is.",1),
     			new Quote("'Tell me,' said the atheist, 'Is there a God — really?' Said the master, 'If you want me to be perfectly honest with you, I will not answer.' \n\nLater the disciples demanded to know why he had not answered. 'Because the question is unanswerable,' said the Master. 'So you are an atheist?' 'Certainly not. \n\nThe atheist makes the mistake of denying that of which nothing may be said... and the theist makes the mistake of affirming it.\n\n -- Anthony De Mello, One Minute Nonsense",1)  
     		};
     	
     	
     	
     	final Object data = getLastNonConfigurationInstance();
         
         // The activity is starting for the first time, load the photos from Flickr
         if (data == null) {
         	// do the first quote
         	this.showNextQuote();
             
         } else {
         	final savedState state = (savedState) data;
         	
         	this.currentQuote = state.quote; 
        	
        	this.showCurrentQuote();
         }
     	
     	
         
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
         menu.add(0, INSERT_ID, 0, R.string.menu_insert_about);
         return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         reflections.this.showAboutUs();
         return true;
     }
     
 
     
     
     
     
 }
