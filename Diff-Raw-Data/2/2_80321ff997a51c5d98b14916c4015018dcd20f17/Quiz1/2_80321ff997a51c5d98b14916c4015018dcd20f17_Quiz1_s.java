 package zamours;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Rectangle;
 
 
 public class Quiz1 implements Screen{
 
 	private float xDoigt, yDoigt;
 	boolean maintenu;
 
 	private SpriteBatch batch;
 	private Texture backgroundBoy;
 	private Texture backgroundGirl;
 	private BitmapFont fontmessage1;
 	private Texture textureRectangle1;
 	private Texture textureRectangle1bis;
 	private Texture textureRectangle2;
 	private Texture textureRectangle2bis;
 	private Texture textureRectangle3;
 	private Texture textureRectangle3bis;
 	private Texture retourMenu;
 	private Rectangle rectangleQuest1;
 	private Rectangle rectangleQuest2;
 	private Rectangle rectangleQuest3;
 	private Circle circleBackButton;
 	private Question quest1;
 	private Reponse rep1;
 	int screenWidth, screenHeight;
 	int spaceBetweenAnswers;
 	int spaceBetweenQuestAnswers;
 	int positionQuestion1;
 	float transparenceNoClick, transparenceClick;
 
 	int[] tabReponseBoy;
 	int[] tabReponseGirl;
 	boolean[] question;
 	
 	int i = 0;
 	boolean yo = false;
 	
 	private String pageSex;	// need pour savoir quel tableau remplir dans touchUp
 	int compteurNombreReponsesBoy = 0;
 	int compteurNombreReponsesGirl = 0;
 	int appuiRep;	// va me donner le numero de reponse sur lequel l'utilisateur a clicked (look touchDown), va me servir pour touchUp
 	
 	private Sound soundTouchDown;
 	
 	
 	private Jeu game;
 
 	Quiz1(Jeu game){
 		this.game = game;
 	}
 	
 	
 
 	@Override
 	public void dispose() {
 		
 	}
 
 	@Override
 	public void hide() {
 		
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 
 	@Override
 	public void render(float arg0) {
 		Gdx.gl.glClearColor(0, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gestionDesEntrees();
 		batch.begin();
 		questionTouched();
 
 		batch.end();
 
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		
 	}
 
 	@Override
 	public void resume() {
 		
 	}
 
 	@Override
 	public void show() {
 		soundTouchDown = Gdx.audio.newSound(Gdx.files.internal("sound/sound_click_down.wav"));
 		Texture.setEnforcePotImages(false);
 		screenWidth = Gdx.graphics.getWidth();
 		screenHeight = Gdx.graphics.getHeight();
 		spaceBetweenAnswers = screenHeight / 6;
 		spaceBetweenQuestAnswers = screenHeight / 4;
 		positionQuestion1 = screenHeight / 2 + screenHeight / 7;
 		batch = new SpriteBatch();
 		/**
 		 * xhdpi: 640x960 px hdpi: 480x800 px mdpi: 320x480 px ldpi: 240x320 px
 		 */
 		backgroundBoy = new Texture(
 				Gdx.files.internal("background_blue_s2.png"));
 		backgroundGirl = new Texture(
 				Gdx.files.internal("background_pink_s2.png"));
 		
 		fontmessage1 =  new BitmapFont(Gdx.files.internal("font/white.fnt"), false);
 
 		quest1 = new Question(1);
 		rep1 = new Reponse(1);
 
 		/********************************** Placement des 3 rectangles ****************************************************************/
 
 
 		textureRectangle1 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle1bis = new Texture(Gdx.files.internal("reponseClick.png"));
 		textureRectangle2 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle2bis = new Texture(Gdx.files.internal("reponseClick.png"));
 		textureRectangle3 = new Texture(Gdx.files.internal("reponse.png"));
 		textureRectangle3bis = new Texture(Gdx.files.internal("reponseClick.png"));
 		/*********************************************************************************************************************************/
 
 		tabReponseBoy = new int[10];
 		tabReponseGirl = new int[10];
 		question = new boolean[10];
 		
 		Jeu.numeroQuestionQuiz = 1;
 		retourMenu = new Texture(Gdx.files.internal("backbutton1.png"));
 	}
 	
 	
 
 	public void afficheQuestionReponses() {
 		switch (Jeu.numeroQuestionQuiz) {
 		case 1:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(0),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(0, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 
 		case 2:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(1),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(1, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 3:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(2),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(2, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 4:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(3),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(3, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 5:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(4),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(4, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 6:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(5),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(5, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 7:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(6),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(6, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 8:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(7),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(7, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 9:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(8),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(8, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 10:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(9),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(9, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 11:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(10),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(10, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 12:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(11),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(11, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 13:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(12),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(12, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 14:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(13),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(13, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 15:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(14),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(14, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 16:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(15),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(15, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 17:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(16),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(16, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 18:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(17),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(17, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 19:
 			pageSex = "girl";
 			batch.draw(backgroundGirl, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(18),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(18, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 		case 20:
 			pageSex = "boy";
 			batch.draw(backgroundBoy, 0, 0);
 			fontmessage1.drawMultiLine(batch, quest1.afficheQuestionQuizz(19),
 					0, screenHeight - screenHeight / 5, screenWidth,
 					HAlignment.CENTER);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 0),
 					screenWidth / 10, positionQuestion1);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 1),
 					screenWidth / 10, positionQuestion1 - spaceBetweenAnswers);
 			fontmessage1.drawMultiLine(batch, rep1.afficheReponseQuizz(19, 2),
 					screenWidth / 10, positionQuestion1 - 2
 							* spaceBetweenAnswers);
 			break;
 
 		}
 
 	}
 	
 	
 	/***************************************/
 	/** renvoi le nombre de reponse juste **/
 	/***************************************/
 	public int compareReponses(){	// attention on renvoie un int
 		int nbRepJuste = 0;
 		for(int i = 0; i < tabReponseBoy.length; i++){
 			if(tabReponseBoy[i] == tabReponseGirl[i]) nbRepJuste++;
 		}
 		Jeu.nbRepJuste = nbRepJuste;
 		return nbRepJuste;
 	}
 
 	/*******************************************/
 	/** renvoi true si 2 meme rep sinon false **/
 	/*******************************************/
 	public boolean memeRep(){
 		if(tabReponseBoy[compteurNombreReponsesBoy-1] == tabReponseGirl[compteurNombreReponsesGirl-1]){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	// ********************************** Gestion des entr�es
 	// ****************************************/
 
 	public void gestionDesEntrees() {
 		Gdx.input.setInputProcessor(new InputProcessor() {
 
 			public boolean mouseMoved(int x, int y) {
 
 				return false;
 			}
 			
 			@Override
 			public boolean touchUp(int x, int y, int arg2, int arg3) {
 				/***************/
 				/***** boy *****/
 				/***************/
 					tabReponseBoy[compteurNombreReponsesBoy] = 1;
 					if (rectangleQuest1.contains(x, y) && maintenu && pageSex.equals("boy") && appuiRep == 1){
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour le mec) numero : " + (compteurNombreReponsesBoy+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse du mec : " + tabReponseBoy[compteurNombreReponsesBoy] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesBoy++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				} else if (rectangleQuest2.contains(x, y) && maintenu && pageSex.equals("boy") && appuiRep == 2){
 					tabReponseBoy[compteurNombreReponsesBoy] = 2;
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour le mec) numero : " + (compteurNombreReponsesBoy+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse du mec : " + tabReponseBoy[compteurNombreReponsesBoy] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesBoy++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				} else if (rectangleQuest3.contains(x, y) && maintenu && pageSex.equals("boy") && appuiRep == 3) {
 					tabReponseBoy[compteurNombreReponsesBoy] = 3;
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour le mec) numero : " + (compteurNombreReponsesBoy+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse du mec : " + tabReponseBoy[compteurNombreReponsesBoy] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesBoy++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				}
 				/***************/
 				/**** girl *****/
 				/***************/
 				if (rectangleQuest1.contains(x, y) && maintenu && pageSex.equals("girl") && appuiRep == 1){
 					tabReponseGirl[compteurNombreReponsesGirl] = 1;
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour la fille) numero : " + (compteurNombreReponsesGirl+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse de la fille : " + tabReponseGirl[compteurNombreReponsesGirl] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesGirl++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				} else if (rectangleQuest2.contains(x, y) && maintenu && pageSex.equals("girl") && appuiRep == 2){
 					tabReponseGirl[compteurNombreReponsesGirl] = 2;
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour la fille) numero : " + (compteurNombreReponsesGirl+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse de la fille : " + tabReponseGirl[compteurNombreReponsesGirl] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesGirl++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				} else if (rectangleQuest3.contains(x, y) && maintenu && pageSex.equals("girl") && appuiRep == 3) {
 					tabReponseGirl[compteurNombreReponsesGirl] = 3;
 					/**********/
 					/** test **/
 					/**********/
 					System.out.print("question (pour la fille) numero : " + (compteurNombreReponsesGirl+1) + "\t");
 					System.out.print("compteur : " + Jeu.numeroQuestionQuiz + "\t");
 					System.out.print("numero reponse de la fille : " + tabReponseGirl[compteurNombreReponsesGirl] + "\n");
 					/**************/
 					/** fin test **/
 					/**************/
 					compteurNombreReponsesGirl++;
 					if (compteurNombreReponsesBoy == compteurNombreReponsesGirl && Jeu.numeroQuestionQuiz != 1){
 						if (memeRep()){	// si ils ont repondu pareil
 							System.out.println("bien jou");
 						} else {
 							System.out.println("loupe");
 						}
 					}
 					Jeu.numeroQuestionQuiz++;
 				}
 				/**********/
 				/** test **/
 				/**********/
 				if (Jeu.quizTermine()) {
 					System.out.println("Fin du quizz ! \n");
 					System.out.println("Affichage reponses boy : ");
 					for(int i = 0; i < tabReponseBoy.length; i++){
 						System.out.println("Reponse question " + (i+1) +" : " + tabReponseBoy[i]);
 					}
 					System.out.println();
 					System.out.println("Affichage reponses girl : ");
 					for(int i = 0; i < tabReponseGirl.length; i++){
 						System.out.println("Reponse question " + (i+1) +" : " + tabReponseGirl[i]);
 					}
 					System.out.println();
 					System.out.println("Le nombre de reponse(s) juste est : " + compareReponses() + "/10");
 					System.out.println();
 					System.out.println("Le numeroQuestionQuiz est : " + Jeu.numeroQuestionQuiz++);
 					game.setScreen(new ResultatDuQuiz(game));	// changement de page, redirection vers la screen ResultatDuQuiz
 				}
 				/**************/
 				/** fin test **/
 				/**************/
 				xDoigt = 0;
 				yDoigt = 0;
 				maintenu = false;
 				return false;
 			}
 
 			@Override
 			public boolean touchDragged(int x, int y, int arg2) {
 				return false;
 			}
 
 			@Override
 			public boolean touchDown(int x, int y, int arg2, int arg3) {
 				if(rectangleQuest1.contains(x, y) || rectangleQuest2.contains(x, y) || rectangleQuest3.contains(x, y)){
 					if (Jeu.getDesactiveSoundTouchDown() == false)
 						soundTouchDown.play();
 				}
 				if (rectangleQuest1.contains(x,y)){
 					appuiRep = 1;
 				} else if (rectangleQuest2.contains(x,y)){
 					appuiRep = 2;
 				} else if (rectangleQuest3.contains(x,y)){
 					appuiRep = 3;
 				}
 				if (circleBackButton.contains(x, y)){
 					game.setScreen(new MainMenu(game));
 				}
 				xDoigt = x;
 				yDoigt = y;
 				maintenu = true;
 				return false;
 			}
 
 			@Override
 			public boolean scrolled(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyUp(int arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyTyped(char arg0) {
 				return false;
 			}
 
 			@Override
 			public boolean keyDown(int arg0) {
 				return false;
 			}
 		});
 	}
 
 	public void questionTouched() {
 		
 		/** bouton retour **/
 		circleBackButton = new Circle( screenWidth/15 + 34, 47, 26);
 		
 		rectangleQuest1 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 - 20, screenWidth - 2
 				* ((screenWidth / 10) - 10), 100);
 		rectangleQuest2 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 + spaceBetweenAnswers - 20, screenWidth - 2
 				* ((screenWidth / 10) - 10), 100);
 		rectangleQuest3 = new Rectangle((screenWidth / 10) - 10, screenHeight
 				- positionQuestion1 + 2 * spaceBetweenAnswers - 20, screenWidth - 2
 				* ((screenWidth / 10) - 10), 100);
 
 		afficheQuestionReponses();
 		
 		batch.draw(retourMenu, screenWidth/15, screenHeight - screenHeight/10);
 		
 		if (rectangleQuest1.contains(xDoigt, yDoigt) && maintenu) {
 
 			batch.draw(textureRectangle1bis, -8, 432);
 
 		} else {
 			batch.draw(textureRectangle1, -5,432);
 		}
 		if (rectangleQuest2.contains(xDoigt, yDoigt) && maintenu) {
 			batch.draw(textureRectangle2bis, -8, 297);
 		} else {
 			batch.draw(textureRectangle2, -5, 297);
 
 		}
 		if (rectangleQuest3.contains(xDoigt, yDoigt) && maintenu) {
			batch.draw(textureRectangle3bis, -8, 297);
 		} else {
 			batch.draw(textureRectangle3, -5, 165);
 		}
 	}
 }
