 package engenhoka.balloons;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import javafx.animation.Animation;
 import javafx.animation.KeyFrame;
 import javafx.animation.KeyValue;
 import javafx.animation.Timeline;
 import javafx.animation.TimelineBuilder;
 import javafx.application.Application;
 import javafx.beans.property.BooleanProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Bounds;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.Control;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.effect.DropShadow;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.scene.transform.Transform;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 public class BalloonsGame extends Application {
 	private static final int LOGO_SHOLD_BE_REMOVED = 0x8000;
 	private static final int BONUS_INDEX = Resources.LOGO_COUNT;
 	private static final int INITIAL_VELOCITY = 550;
 	private static final int LOGO_HEIGHT = 114;
 	private static final int LOGO_WIDTH = 261;
 	private static final int MAX_TIME = 30;
 	private static final double BALLOON_SCALE_Y = 0.5;
 	private static final double BALLOON_SCALE_X = 0.5;
 
 	private static final double TIME_MILLIS = 20.0;
 	private static final double DELTA_TIME = TIME_MILLIS / 1000.0;
 
 	public static BalloonsGame game;
 
 	private List<Balloon> balloons = new ArrayList<Balloon>();
 
 	private int[] score = new int[Resources.LOGO_COUNT];
 	private GameState state;
 	private Group currentScene;
 
 	private Group root;
 	private double timeSpan;
 
 	private Random random = new Random(System.currentTimeMillis());
 
 	private Timeline balloonsTimeline;
 	private Timeline clockTimeline;
 
 	private double velocity = INITIAL_VELOCITY;
 
 	private int countDown;
 
 	private Scene scene;
 
 	private int bonusCount;
 	private long currentTimestamp;
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 
 	@Override
 	public void start(final Stage stage) throws Exception {
 		game = this;
 
 		stage.setTitle("Balloons Game");
 
 		stage.setWidth(1024);
 		stage.setHeight(768);
 		stage.setResizable(false);
 		stage.setFullScreen(true);
 		stage.getIcons().add(Resources.icon);
 
 		// force fullScreen !!!
 		stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() { @Override public void changed(ObservableValue<? extends Boolean> observable,	Boolean oldValue, Boolean newValue) {
 			 if (!newValue)
 				 stage.setFullScreen(true);
 		}});
 
 		root = new Group();
 		scene = new Scene(root);
 		scene.setFill(Color.BLUE);
 		stage.setScene(scene);
 
 		background = new Background();
 		background.widthProperty().bind(scene.widthProperty());
 		background.heightProperty().bind(scene.heightProperty());
 		root.getChildren().add(background);
 
 		final ImageView logo = new ImageView(Resources.logo);
 		final double logoScale = 0.6;
 		logo.setScaleX(logoScale);
 		logo.setScaleY(logoScale);
 		logo.translateXProperty().bind(scene.widthProperty().divide(2).subtract(Resources.logo.getWidth() * 0.5));
 		logo.translateYProperty().bind(scene.heightProperty().subtract(
 				Resources.logo.getHeight() * logoScale	+ Resources.logo.getHeight() * logoScale * 0.5));
 		logo.setDisable(true);
 		logo.setMouseTransparent(true);
 
 		root.getChildren().add(logo);
 
 		balloonsTimeline = new Timeline();
 		balloonsTimeline.setCycleCount(Timeline.INDEFINITE);
 
 		KeyFrame kf = new KeyFrame(Duration.millis(TIME_MILLIS), new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			update();
 			logo.toFront();
 		}});
 
 		balloonsTimeline.getKeyFrames().add(kf);
 
 		changeState(GameState.INTRO);
 
 		stage.show();
 	}
 
 	private void changeState(GameState newState) {
 		if (currentScene != null)
 			currentScene.getChildren().clear();
 
 		root.getChildren().remove(currentScene);
 
 		switch (newState) {
 		case INTRO:
 			currentScene = createIntro();
 			break;
 
 		case MENU:
 			currentScene = createMenu();
 			break;
 
 		case PLAYING:
 			currentScene = startGame();
 			break;
 
 		case WINNING:
 			currentScene = winningGame();
 			break;
 
 		case LOSE:
 			currentScene = loseGame();
 			break;
 
 		default:
 			break;
 		}
 
 		state = newState;
 		root.getChildren().add(currentScene);
 	}
 
 	private Group createIntro() {
 		Group group = new Group();
 
 		ImageView engenhoka = new ImageView(Resources.engenhoka);
 		engenhoka.translateXProperty().bind(scene.widthProperty().divide(2).subtract(Resources.engenhoka.getWidth() / 2));
 		engenhoka.translateYProperty().bind(scene.heightProperty().divide(2).subtract(Resources.engenhoka.getHeight() / 2));
 		engenhoka.setOpacity(0);
 		group.getChildren().add(engenhoka);
 
 		Timeline timeline = new Timeline();
 		timeline.setCycleCount(1);
 
 		KeyFrame kf0 = new KeyFrame(Duration.millis(3000), new KeyValue(engenhoka.opacityProperty(), 1));
 		KeyFrame kf1 = new KeyFrame(Duration.millis(6000), new KeyValue(engenhoka.opacityProperty(), 1));
 		KeyFrame kf2 = new KeyFrame(Duration.millis(7000), new KeyValue(engenhoka.opacityProperty(), 0));
 
 		timeline.getKeyFrames().addAll(kf0, kf1, kf2);
 
 		timeline.setOnFinished(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			changeState(GameState.MENU);
 		}});
 
 		timeline.play();
 
 		return group;
 	}
 
 	private Group loseGame() {
 		Resources.trumpet.play();
 		clockTimeline.stop();
 		balloonsTimeline.stop();
 
 		Group group = new Group();
 
 		DropShadow dropShadow = new DropShadow();
 		dropShadow.setRadius(5.0);
 		dropShadow.setOffsetX(3.0);
 		dropShadow.setOffsetY(3.0);
 		dropShadow.setColor(Color.color(0.2, 0, 0));
 
 		Text text = new Text("N\u00E3o foi desta vez!!");
 		text.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
 		text.setFill(Color.RED);
 		text.setEffect(dropShadow);
 		text.setStroke(Color.BLACK);
 		center(text);
 		group.getChildren().add(text);
 
 		final Button playButton = new Button("Voltar");
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 		center(playButton);
 		group.getChildren().add(playButton);
 
 		final BooleanProperty hardModeSelected = showModes(group, playButton, 30);
 
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			hardMode = hardModeSelected.get();
 			changeState(GameState.MENU);
 		}});
 
 		ImageView iwatinha = new ImageView(Resources.iwatinha_sad);
 		iwatinha.setTranslateX(10);
		iwatinha.translateYProperty().bind(text.translateYProperty());
 		group.getChildren().add(iwatinha);
 
 		Credits credits = new Credits();
 		credits.translateXProperty().bind(scene.widthProperty().subtract(400));
 		credits.translateYProperty().bind(scene.heightProperty().subtract(250));
 		group.getChildren().add(credits);
 
 		return group;
 	}
 
 	private BooleanProperty showModes(Group group, final Button playButton,	double xAxisDeviation) {
 		final ToggleGroup tgroup = new ToggleGroup();
 
 		final ToggleButton easy = new ToggleButton();
 		easy.setToggleGroup(tgroup);
 		easy.setMinSize(100, 50);
 		easy.setStyle("-fx-base: lightgreen;");
 		easy.setSelected(!hardMode);
 		easy.setVisible(false);
 		easy.translateXProperty().bind(playButton.translateXProperty().add(xAxisDeviation));
 		easy.translateYProperty().bind(playButton.translateYProperty().subtract(-100));
 
 		final ToggleButton hard = new ToggleButton();
 		hard.setToggleGroup(tgroup);
 		hard.setMinSize(100, 50);
 		hard.setStyle("-fx-base: red;");
 		hard.setSelected(hardMode);
 		hard.setVisible(false);
 		hard.translateXProperty().bind(easy.translateXProperty().add(120));
 		hard.translateYProperty().bind(easy.translateYProperty());
 
 		easy.setOnMouseClicked(new EventHandler<MouseEvent>() {	@Override public void handle(MouseEvent evt) {
 			hard.setSelected(!easy.isSelected());
 		}});
 		hard.setOnMouseClicked(new EventHandler<MouseEvent>() {	@Override public void handle(MouseEvent evt) {
 			easy.setSelected(!hard.isSelected());
 		}});
 
 		group.getChildren().add(easy);
 		group.getChildren().add(hard);
 
		final Rectangle hiddenSquare = new Rectangle(100, 100);
 		hiddenSquare.setFill(Color.BLUE);
 		hiddenSquare.setOpacity(0);
 
		hiddenSquare.translateYProperty().bind(scene.heightProperty().subtract(100));
 
 		hiddenSquare.setOnMouseClicked(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent evt) {
 			if (evt.getButton().equals(MouseButton.PRIMARY)) {
 				if (evt.getClickCount() >= 2) {
 					easy.setVisible(!easy.isVisible());
 					hard.setVisible(!hard.isVisible());
 				}
 			}
 		}});
 
 		group.getChildren().add(hiddenSquare);
 
 		return hard.selectedProperty();
 	}
 
 	private Group winningGame() {
 		Resources.applause.play();
 		clockTimeline.stop();
 		balloonsTimeline.stop();
 
 		final Group group = new Group();
 		
 		final DropShadow dropShadow = new DropShadow();
 		dropShadow.setRadius(5.0);
 		dropShadow.setOffsetX(5.0);
 		dropShadow.setOffsetY(5.0);
 		dropShadow.setColor(Color.DARKOLIVEGREEN);
 		
 		final Text text = new Text("Voc\u00EA ganhou!!");
 		text.setFont(Font.font("Verdana", FontWeight.BOLD, 110));
 		text.setEffect(dropShadow);
 		text.setFill(Color.DARKGREEN);
 		text.setStroke(Color.BLACK);
 		center(text);
 		
 		final Timeline shadowTimeline = TimelineBuilder.create()
 				.cycleCount(Animation.INDEFINITE)
 				.autoReverse(true)
 				.keyFrames(new KeyFrame(Duration.seconds(3),
 						new KeyValue(dropShadow.radiusProperty(), 0),
 						new KeyValue(dropShadow.colorProperty(), Color.BLACK),
 						new KeyValue(text.fillProperty(), Color.YELLOWGREEN),
 						new KeyValue(text.strokeProperty(), Color.WHITESMOKE)
 						))
 						.build();
 		
 		final ImageView balloonsWin = new ImageView(Resources.balloonsWin);
 		
 		balloonsWin.setTranslateX(random.nextDouble() * 1024);
 		balloonsWin.setTranslateY(background.heightProperty().add(Resources.balloonsWin.getHeight()).get());
 
 		final Timeline balloonsWinTimeline = TimelineBuilder.create()
 				.cycleCount(Animation.INDEFINITE)
				.keyFrames(new KeyFrame(Duration.seconds(3),
 						new KeyValue(balloonsWin.translateYProperty(), -Resources.balloonsWin.getHeight()),
 						new KeyValue(balloonsWin.opacityProperty(), .25),
 						new KeyValue(balloonsWin.scaleXProperty(), .25),
 						new KeyValue(balloonsWin.scaleYProperty(), .25)
 				))
 				.build();
		
 		final Button playButton = new Button("Voltar");
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 		center(playButton, 20);
 		
 		group.getChildren().add(playButton);
 		group.getChildren().add(balloonsWin);
 		group.getChildren().add(text);
 		
 		shadowTimeline.play();
 		balloonsWinTimeline.play();
 
 		final BooleanProperty hardModeSelected = showModes(group, playButton, 30);
 
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override	public void handle(MouseEvent e) {
 			shadowTimeline.stop();
 			balloonsWinTimeline.stop();
 			hardMode = hardModeSelected.get();
 			changeState(GameState.MENU);
 		}});
 
 		ImageView iwatinha = new ImageView(Resources.iwatinha_happy);
 		iwatinha.setTranslateX(10);
		iwatinha.translateYProperty().bind(text.translateYProperty());
 		group.getChildren().add(iwatinha);
 
 		Credits credits = new Credits();
 		credits.translateXProperty().bind(scene.widthProperty().subtract(400));
 		credits.translateYProperty().bind(scene.heightProperty().subtract(250));
 		group.getChildren().add(credits);
 
 		return group;
 	}
 
 	private Group startGame() {
 		Group group = new Group();
 
 		currentTimestamp = System.currentTimeMillis();
 		countDown = MAX_TIME;
 		velocity = INITIAL_VELOCITY;
 		Arrays.fill(score, 0);
 		bonusCount = 0;
 
 		final Text clockText = new Text("30");
 		clockText.translateXProperty().bind(scene.widthProperty().subtract(150));
 		clockText.setTranslateY(100);
 
 		clockText.setFont(Font.font("Verdana", FontWeight.BOLD, 100));
 		clockText.setFill(Color.WHITE);
 		clockText.setStroke(Color.BLACK);
 
 		group.getChildren().add(clockText);
 
 		KeyFrame kf = new KeyFrame(Duration.seconds(1),	new EventHandler<ActionEvent>() { @Override	public void handle(ActionEvent event) {
 			clockText.toFront();
 			clockText.setText(String.valueOf(countDown));
 			countDown--;
 
 			if (countDown < 0) {
 				clockTimeline.stop();
 				balloonsTimeline.stop();
 
 				changeState(GameState.LOSE);
 			}
 			// debug
 			// else if (countDown < 28) changeState(GameState.WINNING);
 		}});
 
 		clockTimeline = new Timeline();
 		clockTimeline.setCycleCount(Timeline.INDEFINITE);
 		clockTimeline.getKeyFrames().add(kf);
 
 		clockTimeline.play();
 		balloonsTimeline.play();
 
 		System.out.println("Playing level " + (hardMode ? "HARD" : "EASY"));
 
 		return group;
 	}
 
 	private Group createMenu() {
 		Group menu = new Group();
 
 		final Button playButton = new Button("Vamos come\u00E7ar");
 		center(playButton);
 		playButton.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
 
 		menu.getChildren().add(playButton);
 
 		final BooleanProperty hardModeSelected = showModes(menu, playButton, 10);
 
 		playButton.setOnMousePressed(new EventHandler<MouseEvent>() { @Override public void handle(MouseEvent e) {
 			hardMode = hardModeSelected.get();
 			changeState(GameState.PLAYING);
 		}});
 
 		return menu;
 	}
 
 	private void center(Control control) {
 		center(control, 0);
 	}
 	
 	private void center(Control control, double yDelta) {
 		control.translateXProperty().bind(scene.widthProperty().divide(2).subtract(control.widthProperty().divide(2)));
 		control.translateYProperty().bind(scene.heightProperty().divide(2).subtract(control.heightProperty().divide(2)).add(yDelta));
 	}
 
 	private void center(Text control) {
 		Bounds bounds = control.getLayoutBounds();
 
 		control.translateYProperty().bind(scene.heightProperty().divide(2).subtract(bounds.getHeight() / 2));
 		control.translateXProperty().bind(scene.widthProperty().divide(2).subtract(bounds.getWidth() / 2));
 	}
 
 	private boolean hardMode = false;
 	private Background background;
 
 	private void createBalloon() {
 		bonusCount++;
 
 		int colorIndex = random.nextInt(Resources.balloons.length);
 		int logoIndex = random.nextInt(Resources.LOGO_COUNT);
 
 		if (bonusCount == 10) {
 			logoIndex = BONUS_INDEX;
 			bonusCount = 0;
 		}
 
 		// Balloon balloon = new Balloon(colorIndex, logoIndex, velocity, state != GameState.WINNING, currentTimestamp);
 		double speed = hardMode ? velocity * 1.2 : velocity;
 		Balloon balloon = new Balloon(colorIndex, logoIndex, speed,	state != GameState.WINNING, currentTimestamp);
 
 		balloon.setScaleX(BALLOON_SCALE_X);
 		balloon.setScaleY(BALLOON_SCALE_Y);
 		balloon.setTranslateY(root.getScene().getHeight());
 		balloon.setTranslateX(random.nextDouble() * (root.getScene().getWidth() - Resources.balloons[colorIndex].getWidth()));
 		balloon.autosize();
 		currentScene.getChildren().add(balloon);
 		balloons.add(balloon);
 	}
 
 	private void update() {
 		timeSpan += DELTA_TIME;
 
 		if (timeSpan > 1.0) {
 			timeSpan = 0;
 			createBalloon();
 		}
 
 		Iterator<Balloon> iterator = balloons.iterator();
 		while (iterator.hasNext()) {
 			Balloon balloon = iterator.next();
 
 			if (!balloon.isAlive())
 				continue;
 
 			balloon.setTranslateY(balloon.getTranslateY() - balloon.getVelocity() * DELTA_TIME);
 
 			if (balloon.getTranslateY()	+ balloon.getBoundsInLocal().getHeight() < 0) {
 				currentScene.getChildren().remove(balloon);
 				iterator.remove();
 			}
 		}
 	}
 
 	public void animateBalloon(final int logoIndex, final ImageView logoView, final Transform transform, long gameTimestamp) {
 		if (state != GameState.PLAYING)	return;
 		if (currentTimestamp != gameTimestamp) return; // workaround
 
 		double sx = transform.getMxx();
 		double sy = transform.getMyy();
 		double tx = transform.getTx();
 		double ty = transform.getTy();
 
 		currentScene.getChildren().add(logoView);
 		logoView.setScaleX(sx);
 		logoView.setScaleY(sy);
 		logoView.setTranslateX(tx);
 		logoView.setTranslateY(ty);
 
 		Timeline timeline = new Timeline();
 		timeline.setCycleCount(1);
 
 		double offsetX;
 		double offsetY;
 
 		if (logoIndex == BONUS_INDEX) {
 			offsetX = scene.getWidth() - 150;
 			offsetY = 0;
 		} else {
 			int x = logoIndex % 6;
 			int y = logoIndex / 6;
 
 			offsetX = x * LOGO_WIDTH * BALLOON_SCALE_X + x * 15 - 50;
 			offsetY = y * LOGO_HEIGHT * BALLOON_SCALE_Y + y * 15 - 10;
 		}
 
 		timeline.setOnFinished(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent event) {
 			if (logoIndex == BONUS_INDEX)
 				currentScene.getChildren().remove(logoView);
 			else if (score[logoIndex] > 0) {
 				if ((score[logoIndex] & LOGO_SHOLD_BE_REMOVED) != 0)
 					currentScene.getChildren().remove(logoView);
 				score[logoIndex] |= LOGO_SHOLD_BE_REMOVED;
 			}
 		}});
 
 		KeyFrame kf0 = new KeyFrame(Duration.millis(1000), 
 				new KeyValue(logoView.translateXProperty(), offsetX), 
 				new KeyValue(logoView.translateYProperty(), offsetY)
 		);
 
 		timeline.getKeyFrames().addAll(kf0);
 		timeline.play();
 	}
 
 	public void incrementScore(int logoIndex, Transform transform, long gameTimestamp) {
 		if (state != GameState.PLAYING)
 			return;
 
 		double tx = transform.getTx();
 		double ty = transform.getTy();
 
 		if (logoIndex == BONUS_INDEX) {
 			countDown += 5;
 
 			Text text = new Text("+5");
 			text.setFont(Font.font("Verdana", FontWeight.BOLD, 100));
 			text.setTranslateX(tx);
 			text.setTranslateY(ty);
 			text.setFill(Color.WHITE);
 			text.setStroke(Color.BLACK);
 			currentScene.getChildren().add(text);
 
 			Timeline timeline = new Timeline();
 			KeyFrame kf0 = new KeyFrame(Duration.millis(1500), new KeyValue(text.opacityProperty(), 1));
 			KeyFrame kf1 = new KeyFrame(Duration.millis(2000), new KeyValue(text.opacityProperty(), 0));
 			timeline.getKeyFrames().addAll(kf0, kf1);
 			timeline.play();
 		} else {
 			score[logoIndex]++;
 
 			int count = 0;
 			for (int v : score)
 				if (v != 0)
 					count++;
 
 			if (count == Resources.LOGO_COUNT)
 				changeState(GameState.WINNING);
 		}
 	}
 }
