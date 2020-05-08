 package com.example.learningprogramming;
 
 import game.Gambit;
 import game.GambitCondition;
 import game.GambitMotion;
 import game.GameManeger;
 import game.Map;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 
 import com.example.learningprogramming.view.StageActivityMoveSurfaceView;
 import com.example.learningprogramming.view.StageActivitySurfaceView;
 
 public class StageActivity extends Activity {
 
 	private GestureDetector myGestureDetector;
 	private SurfaceView surfaceview;
 	private StageActivitySurfaceView stageSurface;
 	private StageActivityMoveSurfaceView stageMoveSurface;
 	private GameManeger _maneger;
 	private ArrayList<Gambit> _gambits = new ArrayList<Gambit>();
	private Button startButton;
	private Button resetButton;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.activity_stage3);
		startButton = (Button)findViewById(R.id.button1);
		resetButton = (Button)findViewById(R.id.button2);
		resetButton.setEnabled(false);
 		Spinner spinner;
 		spinner = (Spinner) findViewById(R.id.Action1);
 		System.out.println("spinnnnnnnnnnnnnner");
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item);
 		String[] spinnerItemArray = {"ガンビット１", "ガンビット２", "ガンビット３"};
 		for (String string : spinnerItemArray) {
 			adapter.add(string);
 		}
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		// Map map = new StageCreator().createStage002();
 
 		_maneger = new GameManeger();
 		_maneger.setMap(Map.getSelectedMap());
 
 		// SurfaceViewを参照
 		surfaceview = (SurfaceView) findViewById(R.id.SurfaceViewMain);
 
 		// 作成したMainSurfaceViewクラスをインスタンス化
 		stageMoveSurface = new StageActivityMoveSurfaceView(this, surfaceview,
 				_maneger);
 
 		// stageSurface = new StageActivitySurfaceView(this, surfaceview);
 		myGestureDetector = new GestureDetector(new MyGestureListener());
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Thread.interrupted();
 	}
 
 	// private BufferedReader loadStageFile(String filename) throws IOException
 	// {
 	// InputStream is = null;
 	// BufferedReader br = null;
 	//
 	// try {
 	// is = this.openFileInput(filename);
 	// if (is != null) {
 	// br = new BufferedReader(new InputStreamReader(is));
 	// }
 	// } catch (FileNotFoundException e) {
 	// Toast.makeText(this, "ファイルが存在しません", Toast.LENGTH_SHORT).show();
 	// } catch (NullPointerException e) {
 	// Toast.makeText(this, "ぬるぽ", Toast.LENGTH_SHORT).show();
 	// }
 	// return br;
 	// }
 
 	public void move(View view) {
 		/*
 		 * _gambits.add(new Gambit(false, GambitCondition.CanRightAndLeft,
 		 * GambitMotion.Left)); _gambits.add(new Gambit(true,
 		 * GambitCondition.CanLeft, GambitMotion.Left)); _gambits.add(new
 		 * Gambit(false, GambitCondition.CanForward,GambitMotion.Forward));
 		 * _gambits.add(new Gambit(false,
 		 * GambitCondition.CanBack,GambitMotion.Back));
 		 */
		startButton.setEnabled(false);
		resetButton.setEnabled(true);
 		Spinner[] conditoins = {(Spinner) findViewById(R.id.Condition1),
 				(Spinner) findViewById(R.id.Condition2),
 				(Spinner) findViewById(R.id.Condition3),
 				(Spinner) findViewById(R.id.Condition4),
 				(Spinner) findViewById(R.id.Condition5),
 				(Spinner) findViewById(R.id.Condition6)};
 		Spinner[] actions = {(Spinner) findViewById(R.id.Action1),
 				(Spinner) findViewById(R.id.Action2),
 				(Spinner) findViewById(R.id.Action3),
 				(Spinner) findViewById(R.id.Action4),
 				(Spinner) findViewById(R.id.Action5),
 				(Spinner) findViewById(R.id.Action6)};
 		CheckBox[] checkboxes = {(CheckBox) findViewById(R.id.Checkbox1),
 				(CheckBox) findViewById(R.id.Checkbox2),
 				(CheckBox) findViewById(R.id.Checkbox3),
 				(CheckBox) findViewById(R.id.Checkbox4),
 				(CheckBox) findViewById(R.id.Checkbox5),
 				(CheckBox) findViewById(R.id.Checkbox6)};
 		for (int i = 0; i < conditoins.length; i++) {
 			if (!conditoins[i].getSelectedItem().toString().equals("なし")
 					|| !actions[i].getSelectedItem().toString().equals("なし")) {
 				if (checkboxes[i].isChecked()) {
 					_gambits.add(new Gambit(true, selectCondition(conditoins[i]
 							.getSelectedItem().toString()),
 							selectAction(actions[i].getSelectedItem()
 									.toString())));
 				} else {
 					_gambits.add(new Gambit(false,
 							selectCondition(conditoins[i].getSelectedItem()
 									.toString()), selectAction(actions[i]
 									.getSelectedItem().toString())));
 				}
 			}
 
 		}
 
 		stageMoveSurface.move(_gambits);
 	}
 
 	private GambitMotion selectAction(String action) {
 		if (action.equals("前へ進む")) {
 			return GambitMotion.Forward;
 		} else if (action.equals("後ろへ進む")) {
 			return GambitMotion.Back;
 		} else if (action.equals("右へ進む")) {
 			return GambitMotion.Right;
 		} else if (action.equals("左へ進む")) {
 			return GambitMotion.Left;
 		}
 		return null;
 	}
 
 	public GambitCondition selectCondition(String condition) {
 		if (condition.equals("右に進めたら")) {
 			return GambitCondition.CanRight;
 		} else if (condition.equals("左に進めたら")) {
 			return GambitCondition.CanLeft;
 		} else if (condition.equals("後ろに進めたら")) {
 			return GambitCondition.CanBack;
 		} else if (condition.equals("前に進めたら")) {
 			return GambitCondition.CanForward;
 		} else if (condition.equals("前と右に進めたら")) {
 			return GambitCondition.CanForwardAndRight;
 		} else if (condition.equals("前と左に進めたら")) {
 			return GambitCondition.CanForwardAndLeft;
 		} else if (condition.equals("左右に進めたら")) {
 			return GambitCondition.CanRightAndLeft;
 		} else if (condition.equals("前と左右に進めたら")) {
 			return GambitCondition.CanRightAndLeftAndForward;
 		} else if (condition.equals("全方向に進めたら")) {
 			return GambitCondition.CanALL;
 		}
 		return null;
 	}
 
 	public void reset(View view) {
		startButton.setEnabled(true);
		resetButton.setEnabled(false);
 		stageMoveSurface.reset();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO 自動生成されたメソッド・スタブ
 		if (myGestureDetector.onTouchEvent(event))
 			return true;
 		else
 			return false;
 	}
 
 	private class MyGestureListener extends SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			// TODO 自動生成されたメソッド・スタブ
 			if (velocityX < 0) {
 				Intent subactivity = new Intent(StageActivity.this,
 						com.example.learningprogramming.GambitActivity.class);
 				startActivity(subactivity);
 			}
 			return false;
 		}
 	}
 }
