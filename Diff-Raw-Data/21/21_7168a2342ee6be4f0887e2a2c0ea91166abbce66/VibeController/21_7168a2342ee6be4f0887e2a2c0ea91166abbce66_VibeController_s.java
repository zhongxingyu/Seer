 package vibe;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 
 import earcompiler.EARCompiler;
 import earcompiler.EARException;
 import earfuck.Parser;
 
 /**
  * This class manages the behind-the-scenes action of the IDE.
  * @author Ryan Norris
  *
  */
 public class VibeController implements ActionListener {
 	/**
 	 * The application window JFrame.
 	 */
 	private MainFrame mFrame;
 	
 	/**
 	 * The EAR Compiler used to take the code in the middle (EAR) box
 	 * and compile it into raw EF code.<br/>
 	 * The compiler also should return a suitable array of line markings
 	 * to allow live code highlighting during playback.
 	 */
 	private EARCompiler mEARCompiler;
 	
 	/**
 	 * The EF Parser to use to playback the compiled EF code.
 	 */
 	private Parser mParser;
 	
 	/**
 	 * Path of the currently opened file.<br/>
 	 * Used for Save functionality.<br/>
 	 * Should only be modified when creating a new file, opening a file,
 	 * saving a file by a new name, or when the program starts up.
 	 */
 	private String mOpenFilePath;
 	
 	/**
 	 * A worker that runs the EF parser on a background thread.<br/>
 	 * This worker runs the EF code until told to stop by a call to mWorker.cancel()
 	 */
 	private ParserWorker mWorker;
 	
 	/**
 	 * A worker that runs the EF parser on a background thread.<br/>
 	 * This worker runs a single EF command and then stops.
 	 */
 	private StepForwardWorker mStepWorker;
 	
 	/**
 	 * The mode describes which type of code we're currently editing.<br/>
 	 * It can be HIGHLEVEL, EAR or EF.
 	 */
 	private VibeMode mMode;
 	
 	/**
 	 * The PlayState describes what state the program is currently in.<br/>
 	 * It can be PLAYING, PAUSED or STOPPED.
 	 */
 	private PlayState mPlayState;
 	
 	/**
 	 * This array contains the start positions (in terms of EF commands)
 	 * of each EAR command. <br/>
 	 * Used for code highlighting.
 	 */
 	private ArrayList<Integer> mEARLineStartPositions;
 	
 	/**
 	 * The mode describes which type of code we're currently editing.<br/>
 	 * It can be HIGHLEVEL, EAR or EF.
 	 */
 	enum VibeMode {
 		HIGHLEVEL, EAR, EF;
 	}
 	
 	/**
 	 * The PlayState describes what state the program is currently in.<br/>
 	 * It can be PLAYING, PAUSED or STOPPED.
 	 */
 	enum PlayState {
 		PLAYING, PAUSED, STOPPED;
 	}
 	
 	/**
 	 * Creates a new VibeController
 	 * @param frame The JFrame of the entire application window you want the
 	 * controller to manage.
 	 */
 	public VibeController(MainFrame frame) {
 		mFrame = frame;
 		mEARCompiler = new EARCompiler();
 		mOpenFilePath = null;
 		mParser = new Parser();
 		mWorker = null;
 		mStepWorker = null;
 		mPlayState = PlayState.STOPPED;
 		mEARLineStartPositions = new ArrayList<Integer>();
 	}
 
 	/**
 	 * Controls what happens when actions are called by the window.
 	 * (Button presses etc)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent action) {
 		if (action.getActionCommand().equals("compile")) {
 			compileEARCode();
 			return;
 		}
 		if (action.getActionCommand().equals("exit")) {
 			exitProgram();
 			return;
 		}
 		if (action.getActionCommand().equals("open")) {
 			openFile();
 			return;
 		}
 		if (action.getActionCommand().equals("save")) {
 			save();
 			return;
 		}
 		if (action.getActionCommand().equals("saveas")) {
 			saveAs();
 			return;
 		}
 		if (action.getActionCommand().equals("new")) {
 			newFile();
 			return;
 		}
 		if (action.getActionCommand().equals("play")) {
 			play();
 			return;
 		}
 		if (action.getActionCommand().equals("pause")) {
 			pause();
 			return;
 		}
 		if (action.getActionCommand().equals("step")) {
 			step();
 			return;
 		}
 		if (action.getActionCommand().equals("stop")) {
 			stop();
 			return;
 		}
 	}
 	
 	/**
 	 * Uses mEARCompiler to compile the code in EAR box to EF code.
 	 * Puts the EF code into the EF box.
 	 */
 	private void compileEARCode() {
 		String EARCode = mFrame.getEARCode();
 		String EFCode = null;
 		if (mPlayState==PlayState.STOPPED) {
 			try {
 				EFCode = mEARCompiler.compile(EARCode);
 				mFrame.setEFCode(EFCode);
 				mEARLineStartPositions = mEARCompiler.getCommandStartPositions();
 			} catch (EARException e) {
 				mFrame.setEFCode(e.getMessage());
 			}
 		}
 	}
 	
 	/**
 	 * Safely (I hope!) exits the program.
 	 */
 	private void exitProgram() {
 		pause();
 		mFrame.dispose();
 		System.exit(0);
 	}
 	
 	/**
 	 * Opens an EF, EAR or Higher Level language file. <br/>
 	 * Should intelligently switch mode and put the code into the correct box
 	 * depending on file extension. <br/>
 	 * Currently only inputs to the EAR box.
 	 */
 	private void openFile() {
 		stop();
 		JFileChooser fc = new JFileChooser();
 		fc.setFileFilter(new EFCodeFilter());
 		int returnVal = fc.showOpenDialog(mFrame);
 		
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			mOpenFilePath = file.getPath();
 			BufferedReader br = null;
 			try {
 				br = new BufferedReader(new FileReader(file));
 			
 			
 				StringBuilder builder = new StringBuilder();
 				
 				String currentLine = null;
 				while ((currentLine = br.readLine()) != null) {
 					builder.append(currentLine+'\n');
 				}
 				
 				br.close();
 				mFrame.setEARCode(builder.toString());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Saves the given text to the given file.
 	 * @param file
 	 * @param text
 	 */
 	private void saveFile(File file, String text) {
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 			bw.append(text);
 			bw.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Saves the EAR code to the already open file if possible.
 	 * Otherwise loads the saveAs dialog.<br/>
 	 * ** TO DO ** work more generally to save different code types
 	 * depending on mode.
 	 */
 	private void save() {
 		if (mOpenFilePath==null) {
 			saveAs();
 			return;
 		}
 		File file = new File(mOpenFilePath);
 		saveFile(file,mFrame.getEARCode());
 	}
 	
 	/**
 	 * Prompts user to choose where to save code.
 	 * And then saves it.
 	 * ** TO DO ** work more generally for different modes.
 	 */
 	private void saveAs() {
 		JFileChooser fc = new JFileChooser();
 		fc.setFileFilter(new EFCodeFilter());
 		int returnVal = fc.showSaveDialog(mFrame);
 		
 		if (returnVal==JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			saveFile(file, mFrame.getEARCode());
 			
 			mOpenFilePath = file.getPath();
 		}
 	}
 	
 	/**
 	 * Creates a "new" workspace by clearing all textboxes and forgetting 
 	 * current filename. <br/>
 	 * ** TO DO ** prompt user to select which kind of file they want to create
 	 * and switch modes accordingly.
 	 */
 	private void newFile() {
 		stop();
 		mOpenFilePath = null;
 		mFrame.setEARCode("");
 		mFrame.setEFCode("");
 		mFrame.setHighLevelCode("");
 	}
 	
 	/**
 	 * Begin playing the current program (from current location)
 	 * until told to stop.
 	 */
 	private void play() {
 		String EFCode = mFrame.getEFCode();
 		
 		if (mPlayState==PlayState.STOPPED) {
 			mParser.refreshState();
 		}
 		mPlayState = PlayState.PLAYING;
 		mParser.giveMusic(EFCode);
 		mWorker = new ParserWorker(this);
 		mWorker.execute();
 		
 		setPlayState(PlayState.PLAYING);
 	}
 	
 	/**
 	 * Pauses playback.
 	 */
 	private void pause() {
 		mWorker.cancel(true);
 		setPlayState(PlayState.PAUSED);
 	}
 	
 	/**
 	 * Stop playback. Call to setPlayState resets program state.
 	 */
 	private void stop() {
 		if (mWorker!=null) {
 			mWorker.cancel(true);
 		}
 		setPlayState(PlayState.STOPPED);
 	}
 	
 	/**
 	 * Steps the EF program forward one command.
 	 */
 	private void step() {
 		if (mPlayState==PlayState.STOPPED) {
 			mParser.refreshState();
 		}
 		if (mParser.getPiece().length==0) {
 			mParser.giveMusic(mFrame.getEFCode());
 		}
 		mStepWorker = new StepForwardWorker(this);
 		mStepWorker.execute();
 		setPlayState(PlayState.PLAYING);
 	}
 	
 	/**
 	 * Sets the current play state. <br/>
 	 * Enables & disables Play/Pause/Stop/Step buttons accordingly. <br/>
 	 * Also prevents the user from editing the code whilst it's running.
 	 * This is essential for code highlighting to work consistently.
 	 * @param state PlayState to change to.
 	 */
 	public void setPlayState(PlayState state) {
 		mPlayState = state;
 		if (state==PlayState.PLAYING) {
 			mFrame.setButtonEnabled(1,false); //Play
 			mFrame.setButtonEnabled(2,true); //Pause
 			mFrame.setButtonEnabled(3,true); //Stop
 			mFrame.setButtonEnabled(4,false); //Step
 			mFrame.setCodeEditable(false);
 			return;
 		}
 		if (state==PlayState.PAUSED) {
 			mFrame.setButtonEnabled(1,true); //Play
 			mFrame.setButtonEnabled(2,false); //Pause
 			mFrame.setButtonEnabled(3,true); //Stop
 			mFrame.setButtonEnabled(4,true); //Step
 			mFrame.setCodeEditable(false);
 			return;
 		}
 		if (state==PlayState.STOPPED) {
 			mFrame.setButtonEnabled(1,true); //Play
 			mFrame.setButtonEnabled(2,false); //Pause
 			mFrame.setButtonEnabled(3,false); //Stop
 			mFrame.setButtonEnabled(4,true); //Step
 			mFrame.setCodeEditable(true);
 			return;
 		}
 	}
 	
 	/**
 	 * Sets the mode we're working in. <br/>
 	 * Call to mFrame.setupEditPane hides irrelevant text boxes.
 	 * @param mode
 	 */
 	public void setMode(VibeMode mode) {
 		mMode = mode;
 		mFrame.setupEditPane(mode);
 	}
 	
 	/**
 	 * Returns the current working mode.
 	 * @return
 	 */
 	public VibeMode getMode() {
 		return mMode;
 	}
 	
 	/**
 	 * Returns the application JFrame.
 	 * @return
 	 */
 	public MainFrame getFrame() {
 		return mFrame;
 	}
 	
 	/**
 	 * Returns the EF code parser.
 	 * @return
 	 */
 	public Parser getParser() {
 		return mParser;
 	}
 	
 	/**
 	 * Returns the EAR line start positions. <br/>
 	 * Used by workers to do code highlighting.
 	 * @return
 	 */
 	public ArrayList<Integer> getEARCommandStartPositions() {
 		return mEARLineStartPositions;
 	}
 }
