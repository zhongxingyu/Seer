 package cs.honours.idw.reader.managers;
 
 import java.io.Reader;
 import java.util.ListIterator;
 import java.util.Locale.Category;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 import cs.honours.idw.reader.GameObject;
 import cs.honours.idw.reader.LibraryScreen;
 import cs.honours.idw.reader.ReaderScreen;
 
 
 
 public class ScreenManager {
 
 	public enum screenState {
 		//Login states
 		HomeScreen,
 		LoggedInScreen,
 		LoginSkippedScreen,
 		CurrentlyReadingScreen, 
 		LibraryScreen, 		
 		//Library screen states
 		CategoryScreen, 	 
 		TitlesScreen,	
 		AuthorsScreen,	 
 		GenreScreen,	 
 		RecommendedScreen,	 
 		ReadLaterScreen,	 
 		RecentBooksScreen,
 		SearchScreen,
 		SearchAuthorScreen,
 		//Reading states		
 		ReadingScreen, 
 		ReadingWithToolbar
 	}
 
 
 	public static screenState currentState = screenState.HomeScreen;
 
 	ReaderScreen homeScreen;
 	ReaderScreen currentlyReadingHomeScreen;
 	ReaderScreen loggedInScreen;
 	ReaderScreen loginSkippedScreen;
	public LibraryScreen libraryScreen;
 	ReaderScreen searchScreen;
 	ReaderScreen readingScreen;
 
 	ReaderScreen favouritesScreen;
 
 	public static ReaderScreen currentScreen;
 	
 	public static boolean onPage1 = true;
 	public boolean currentlyReadingSomething = false;
 	public boolean toolbarActive = false;
 
 	public ScreenManager(){
 		//Homescreen init
 		//Application background
 
 		GameObject bg = new GameObject(new Vector2(0,0),TextureManager.appBG);
 		//Not yet logged in
 
 
 		GameObject bottomSwipeBG = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toLibraryBG);		
 		bottomSwipeBG.setNextTapState(screenState.LibraryScreen);
 		bottomSwipeBG.setNextSwipeLeftState(screenState.LibraryScreen);
 		
 		GameObject toLibrary = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toLibraryBG);		
 		
 
 		GameObject toCurrentBook = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toLibraryBG);		
 		toCurrentBook.setNextTapState(screenState.ReadingScreen);
 		toCurrentBook.setNextSwipeLeftState(screenState.ReadingScreen);
 
 
 
 		//Home screen before login
 		homeScreen=new ReaderScreen();	
 		homeScreen.setPreviousState(screenState.HomeScreen);
 		homeScreen.addScreenElement(bg);//
 		//homeScreen.addScreenElement(toLib);//
 		homeScreen.addScreenElement(new GameObject(new Vector2(90,90),TextureManager.welcome));
 		homeScreen.addScreenElement(new GameObject(new Vector2(90,170),TextureManager.loginUsername));
 		homeScreen.addScreenElement(new GameObject(new Vector2(90,250),TextureManager.loginPassword));
 
 		GameObject loginFB = new GameObject(new Vector2(90,360),TextureManager.loginFacebook);
 		loginFB.setNextTapState(screenState.LoggedInScreen);
 		homeScreen.addScreenElement(loginFB);		
 
 		GameObject loginSk = new GameObject(new Vector2(90,440),TextureManager.loginSkip);
 		loginSk.setNextTapState(screenState.LoginSkippedScreen);
 		homeScreen.addScreenElement(loginSk);
 
 		//Logged in Screen
 		loggedInScreen = new ReaderScreen();
 		loggedInScreen.setPreviousScreen(homeScreen);
 		loggedInScreen.setPreviousState(screenState.HomeScreen);
 		loggedInScreen.addScreenElement(bg);//
 		loggedInScreen.addScreenElement(new GameObject(new Vector2(90,60),TextureManager.welcomeMuneeb));
 		loggedInScreen.addScreenElement(new GameObject(new Vector2(60,120),TextureManager.loginUserPicture));
 		loggedInScreen.addScreenElement(bottomSwipeBG);
 					
 		//Login Skipped screen
 
 		loginSkippedScreen = new ReaderScreen();
 		loginSkippedScreen.setPreviousScreen(homeScreen);
 		loginSkippedScreen.setPreviousState(screenState.HomeScreen);
 		loginSkippedScreen.addScreenElement(bg);//
 		loginSkippedScreen.addScreenElement(new GameObject(new Vector2(90,60),TextureManager.welcomeUser));
 		loginSkippedScreen.addScreenElement(new GameObject(new Vector2(60,120),TextureManager.loginUserPicture));
 		loginSkippedScreen.addScreenElement(bottomSwipeBG);
 
 		
 
 
 
 
 		//Currently reading a book, screen.
 		currentlyReadingHomeScreen=new ReaderScreen();	
 		currentlyReadingHomeScreen.addScreenElement(bg);//
 
 		GameObject bcbg = new GameObject(new Vector2(95,150),TextureManager.bookCoverBG);
 		bcbg.scale=1.5f;
 		bcbg.setBounds();
 
 		GameObject bcfg = new GameObject(new Vector2(95,150),TextureManager.bookCoverFG);
 		bcfg.scale=1.5f;
 		bcfg.setBounds();
 		bcfg.setNextTapState(screenState.ReadingScreen);
 
 		currentlyReadingHomeScreen.addScreenElement(bcbg);//
 		currentlyReadingHomeScreen.addScreenElement(bcfg);//
 		currentlyReadingHomeScreen.addScreenElement(bottomSwipeBG);//
 
 		//Library Screen
 
 		libraryScreen=new LibraryScreen();
 		
 		
 		//in book screen
 		
 		
 		readingScreen=new ReaderScreen();
		readingScreen.setPreviousScreen(libraryScreen.categoryScreen);
		readingScreen.setPreviousState(screenState.CategoryScreen);
 		
 	
 
 
 	}
 
 	public void toPreviousScreen(){
 		try{
 		currentScreen = currentScreen.getPreviousScreen();
 		setState(currentScreen.getPreviousState());
 		}
 		catch(Exception e){}
 	}
 
 	public screenState getState(){
 		return currentState;		
 	}
 
 	public static void setState(screenState newState){
 		if(newState!=null)
 			currentState = newState;		
 	}
 
 	public void update(){
 		switch(currentState){
 		case HomeScreen:			
 			currentScreen=homeScreen;
 			break;
 		case LoggedInScreen:			
 			currentScreen=loggedInScreen;
 			break;
 		case LoginSkippedScreen:			
 			currentScreen=loginSkippedScreen;
 			break;
 		case CurrentlyReadingScreen:
 			System.out.println("CurrentlyReadingScreen");
 			break;			
 		case ReadingScreen:
 			currentlyReadingSomething=true;					
 			currentScreen=readingScreen;
 			System.out.println("ReadingScreen");
 			break;
 		case ReadingWithToolbar:	
 			toolbarActive=!toolbarActive;			
 			if(!toolbarActive){			
 				System.out.println(toolbarActive);
 			}
 			System.out.println("ReadingWithToolbar");
 			break;
 		default:
 			if(currentlyReadingSomething){
 				currentScreen = readingScreen;				
 				currentState=screenState.ReadingScreen;
 			}			
 			else {
 				currentScreen=libraryScreen;
 				libraryScreen.currentLibraryState=currentState;
 				libraryScreen.update();
 			}			
 			break;	
 		}
 	}
 
 	public void draw(SpriteBatch spriteBatch){
 		ListIterator<GameObject> iter = currentScreen.getScreenElements();
 		int count = 0;
 		while(iter.hasNext()){
 			iter.next().getSprite().draw(spriteBatch);
 		}
 		
 		
 		//Same bar, same action, different response and thus different foreground labels
 		if(currentState==screenState.LibraryScreen||currentState==screenState.LoginSkippedScreen||currentState==screenState.LoggedInScreen){
 			GameObject fg = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toolbarActivator);;
 			if(!currentlyReadingSomething){
 				if(currentState!=screenState.LibraryScreen)
 					fg = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toLibraryFG);	
 			}
 			else 
 			{
 				 fg = new GameObject(new Vector2(0,800-TextureManager.toLibraryBG.getHeight()),TextureManager.toLifeOfPiFG);	
 			}
 			fg.getSprite().draw(spriteBatch);
 		}
 		
 		if(currentState==screenState.ReadingScreen){
 			if(onPage1)
 				readingScreen.addScreenElement(new GameObject(new Vector2(0,0), TextureManager.readPage1));
 			else
 				readingScreen.addScreenElement(new GameObject(new Vector2(0,0), TextureManager.readPage2));
 		}
 			
 				
 
 		//new Sprite(TextureManager.appBG).draw(spritebatch);
 	}
 
 }
