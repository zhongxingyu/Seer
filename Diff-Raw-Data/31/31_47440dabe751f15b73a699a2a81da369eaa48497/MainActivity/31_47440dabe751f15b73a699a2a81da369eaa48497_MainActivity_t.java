 package secret.app.baccarat;
 
 import secret.algo.random.RandomAlgo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private Button bankerButton;
 	private Button playerButton;
 	private Button drawButton;
 
 	private TextView betCount;
 	private TextView latest3rdRecord;
 	private TextView latest2ndRecord;
 	private TextView latest1stRecord;
 	private TextView storingBinaryRecord;
 	private TextView currentRandomBinary;
 	private TextView storedBinary;
 
 	private final int bankerButtonId = R.id.buttonBetBanker;
 	private final int playerButtonId = R.id.buttonBetPlayer;
 	private final int drawButtonId = R.id.buttonBetDraw;
 	private final int betCountId = R.id.textViewBetCount;
 	private final int latest1stRecordId = R.id.textViewLatest1stRecord;
 	private final int latest2ndRecordId = R.id.textViewLatest2ndRecord;
 	private final int latest3rdRecordId = R.id.textViewLatest3rdRecord;
 	private final int storeBinaryRecordId = R.id.textViewStoredBinaryArray;
 	private final int currentRandomRecordId = R.id.textViewCurrentRandomBinary;
 
 	private char[] randomBinaryArray;
 	private RandomAlgo random;	
 	private char[] storedPostFlop;
 //	private char[] historyRecord;
 	
 	private final int SCOPE_MAX = 128;
 	private final int BANK = 49;
 	private final int PLAY = 48;
 	private final int DRAW = 84;
 	private final int MAX_RANDOM_BINARY_SCOPE = Integer.toBinaryString(SCOPE_MAX - 1).length();
 	
 	private final int WIN = 87;
 	private final int LOSE = 76;
 
 	private final int MAX_STORED_RANDOM_SCOPE = 12;
 	
 	private int betCursorInBinaryArray = 0;
 	private int flopCursorInStoredArray = 0;
 //	private int recordCursorInHistoryArray = 0;
 	private int totalBetCount = 0;
 	private boolean crazyMode = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		baccaratInit();
 	}
 	
 	private void baccaratInit() {
 		displayedWedgetInit();
 		
 		checkBinary();
 		showBet();
 		
 		storedPostFlop = new char[MAX_RANDOM_BINARY_SCOPE];
 
 		showRecord();
 	}
 
 	private void displayedWedgetInit() {
 		bankerButton = (Button) findViewById(bankerButtonId);
 		bankerButton.setOnClickListener(buttonListener);
 		playerButton = (Button) findViewById(playerButtonId);
 		playerButton.setOnClickListener(buttonListener);
 		drawButton = (Button) findViewById(drawButtonId);
 		drawButton.setOnClickListener(buttonListener);
 
 		betCount = (TextView) findViewById(betCountId);

 		latest1stRecord = (TextView) findViewById(latest1stRecordId);
 		latest2ndRecord = (TextView) findViewById(latest2ndRecordId);
 		latest3rdRecord = (TextView) findViewById(latest3rdRecordId);
 		storingBinaryRecord = (TextView) findViewById(storeBinaryRecordId);
 		currentRandomBinary = (TextView) findViewById(currentRandomRecordId);
 		
 		storedBinary = (TextView) findViewById(R.id.textViewStoredBinaryList);
 	}
 
 	private Button.OnClickListener buttonListener = new Button.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			updateDisplayBetData(v.getId());
 		}
 	};
 
 	private void updateDisplayBetData(int id) {
 		// 1.Random binary create
 		//  1.1 If lose more than 7th bet or win, recreate random binary; or show next bet
 		//  1.2 If binary exist, show money bet via binary array; or create
 		//   1.2.1 If stored binary array is not full, create via RandomAlgo and display
 		//   1.2.2 If stored binary array full, choose from stored binary display and never back
 		// 2.Store current post flop
 		//  2.1 If not draw, also store into stored binary; or empty stored binary
 		//  2.2 Whatever, stored into history
 		// 3.Show latest 3 records
 		
 		totalBetCount++;
 		
 		int postFlopStatus = 1024;
 		
 		switch (id) {
 			case bankerButtonId:
 				postFlopStatus = BANK;
 				break;
 			case playerButtonId:
 				postFlopStatus = PLAY;
 				break;
 			case drawButtonId:
 				postFlopStatus = DRAW;
 				break;
 			default:
 				// TODO: Add reminder on screen
 				return;
 		}
 		
 		if ((DRAW == postFlopStatus) || 
 			(postFlopStatus == randomBinaryArray[betCursorInBinaryArray - 1])) {
 			storeBetRecord(postFlopStatus, WIN);
 			betCursorInBinaryArray = MAX_RANDOM_BINARY_SCOPE;
 			checkBinary();
 			showBet();
 		} else {
 			storeBetRecord(postFlopStatus, LOSE);
 			if (betCursorInBinaryArray < MAX_RANDOM_BINARY_SCOPE) {
 				showBet();
 			} else if (betCursorInBinaryArray == MAX_RANDOM_BINARY_SCOPE){
 				checkBinary();
 				showBet();
				Toast.makeText(this, "Oh No! JB", Toast.LENGTH_LONG).show(); 
 			}
 		}
 		showRecord();
 	}
 
 	private void checkBinary() {
 		if ((randomBinaryArray == null) || (randomBinaryArray.length < MAX_RANDOM_BINARY_SCOPE)
 				|| (betCursorInBinaryArray == MAX_RANDOM_BINARY_SCOPE)) {
 			randomBinaryArray = new char[MAX_RANDOM_BINARY_SCOPE];
 			random = new RandomAlgo();
 			
 			int storedBinaryLength = BetRecordHandle.storedPostFlopList.size();
 
 			if (crazyMode) {
 				if (MAX_STORED_RANDOM_SCOPE == storedBinaryLength) {
 					BetRecordHandle.crazyModeBinaryList.addAll(BetRecordHandle.storedPostFlopList);
 					BetRecordHandle.storedPostFlopList.clear();

 				}
 
 				if (BetRecordHandle.crazyModeBinaryList.size() != 0) {
 					randomBinaryArray = BetRecordHandle.crazyModeBinaryList
 							.get(random.getRandomDecimal(MAX_STORED_RANDOM_SCOPE + 1))
 							.toCharArray();
 				}
				
				betCursorInBinaryArray = 0;
				
 			} else {
 				if (storedBinaryLength < MAX_STORED_RANDOM_SCOPE) {
 					randomBinaryArray = random.getRandomBinary(SCOPE_MAX);
 					betCursorInBinaryArray = 0;
 				} else {
 					crazyMode = true;
 				}
 			}
 		}
 	}
 
 	private void showBet() {
 		if (!(betCursorInBinaryArray > -1 && betCursorInBinaryArray < 7)) {
 			//TODO: add reminder or return label for analysis
 			return;
 		}
 
 		try {
 			if (randomBinaryArray[betCursorInBinaryArray] == BANK) {
 				bankerButton
 						.setText(String
 								.valueOf(BetRecordHandle.betMoney[betCursorInBinaryArray][0]));
 				playerButton.setText("0");
 				drawButton
 						.setText(String
 								.valueOf(BetRecordHandle.betMoney[betCursorInBinaryArray][2]));
 			} else if (randomBinaryArray[betCursorInBinaryArray] == PLAY) {
 				bankerButton.setText("0");
 				playerButton
 						.setText(String
 								.valueOf(BetRecordHandle.betMoney[betCursorInBinaryArray][1]));
 				drawButton
 						.setText(String
 								.valueOf(BetRecordHandle.betMoney[betCursorInBinaryArray][2]));
 			}
 		} catch (ArrayIndexOutOfBoundsException e) {
 			// TODO: add more test here
 			e.printStackTrace();
 			throw e;
 		}
 
 		betCursorInBinaryArray++;
 	}
 	
 	private void storeBetRecord(int postFlop, int winOrLose) {
 
 		if (WIN == winOrLose || (betCursorInBinaryArray == MAX_RANDOM_BINARY_SCOPE)) {
 			BetRecordHandle.betRecordHistory.add(String.valueOf(randomBinaryArray));
 		}
 		
 		if (DRAW != postFlop) {
 			storedPostFlop[flopCursorInStoredArray] = (char)postFlop;
 			flopCursorInStoredArray ++;
 		} else {
 			storedPostFlop = new char[MAX_RANDOM_BINARY_SCOPE];
 			flopCursorInStoredArray = 0;
 		}
 		
 		if (flopCursorInStoredArray == MAX_RANDOM_BINARY_SCOPE) {
 			BetRecordHandle.storedPostFlopList.
 				add(String.valueOf(storedPostFlop));
 			storedPostFlop = new char[MAX_RANDOM_BINARY_SCOPE];
 			flopCursorInStoredArray = 0;
 		}
 	}
 
 	private void showRecord() {
 		betCount.setText("Total Bet: " + String.valueOf(totalBetCount));
 		
 		int historySize = BetRecordHandle.betRecordHistory.size();
 		if (historySize == 1) {
 			latest1stRecord.setText((historySize) + " Record: " + BetRecordHandle.betRecordHistory.get(0));
 		} else if (historySize == 2) {
 			latest1stRecord.setText((historySize) + " Record: " + BetRecordHandle.betRecordHistory.get(1));
 			latest2ndRecord.setText((historySize - 1) + " Record: " + BetRecordHandle.betRecordHistory.get(0));
 		} else if (historySize == 3) {
 			latest1stRecord.setText((historySize) + " Record: " + BetRecordHandle.betRecordHistory.get(2));
 			latest2ndRecord.setText((historySize - 1) + " Record: " + BetRecordHandle.betRecordHistory.get(1));
 			latest3rdRecord.setText((historySize - 2) + " Record: " + BetRecordHandle.betRecordHistory.get(0));
 		} else if (historySize > 3) {
 			latest1stRecord.setText((historySize) + " Record: " + BetRecordHandle.betRecordHistory.get(historySize - 1));
 			latest2ndRecord.setText((historySize - 1) + " Record: " + BetRecordHandle.betRecordHistory.get(historySize - 2));
 			latest3rdRecord.setText((historySize - 2) + " Record: " + BetRecordHandle.betRecordHistory.get(historySize - 3));
 		}
 
 		storingBinaryRecord.setText("Storing Binary: " + String.valueOf(storedPostFlop));
 		currentRandomBinary.setText("Random Binary: " + String.valueOf(randomBinaryArray));
 		
 		if(crazyMode) {
 			currentRandomBinary.setBackgroundColor(0xfff000cc);
 		} else {
 			currentRandomBinary.setBackgroundColor(Color.TRANSPARENT);
 		}
 		
 		if ((BetRecordHandle.storedPostFlopList.size() == 0) & (BetRecordHandle.crazyModeBinaryList.size() != 0)) {
 			storedBinary.setText("Stored Binary Library: \r\n"
 					+ BetRecordHandle.crazyModeBinaryList.toString().replaceAll("[\\[\\]]", "").replaceAll(",", " "));
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			new AlertDialog.Builder(this).setMessage(
 				this.getText(R.string.Back_Dialog_Sure_To_Exit).toString())
 					.setPositiveButton(R.string.Back_Dialog_OK,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 									finish();
 								}
 							})
 					.setNegativeButton(R.string.Back_Dialog_Cancel,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 								}
 							}).create().show();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		System.exit(0);
 	}
 }
