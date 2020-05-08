 package bu.edu.cs673.edukid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import bu.edu.cs673.edukid.learn.LearnContentView;
 import bu.edu.cs673.edukid.learn.LearnType;
 import bu.edu.cs673.edukid.settings.SettingsView;
 import bu.edu.cs673.edukid.settings.utils.MathProblem;
 import bu.edu.cs673.edukid.settings.utils.MathProblemGenerator;
 
 public class EDUkid extends Activity implements OnClickListener,
 		DialogInterface.OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edukid);
 
 		setupLearnButtons();
 	}
 
 	@Override
 	public void onClick(View v) {
 		LearnType learnType;
 
 		switch (v.getId()) {
 		case R.id.learnAlphabetButton:
 			learnType = LearnType.ALPHABET;
 			break;
 		case R.id.learnNumbersButton:
 			learnType = LearnType.NUMBERS;
 			break;
 		case R.id.learnShapesButton:
 			learnType = LearnType.SHAPES;
 			break;
 		case R.id.learnColorsButton:
 			learnType = LearnType.COLORS;
 			break;
 		default:
 			// TODO: need to send which custom type to the learn content view
 			learnType = LearnType.CUSTOM;
 			break;
 		}
 
 		Intent intent = new Intent(this, LearnContentView.class);
 		intent.putExtra("LearnType", learnType);
 		startActivity(intent);
 	}
 
 	private void setupLearnButtons() {
 		ImageButton learnAlphabetButton = (ImageButton) findViewById(R.id.learnAlphabetButton);
 		learnAlphabetButton.setOnClickListener(this);
 
 		ImageButton learnNumbersButton = (ImageButton) findViewById(R.id.learnNumbersButton);
 		learnNumbersButton.setOnClickListener(this);
 
 		ImageButton learnShapesButton = (ImageButton) findViewById(R.id.learnShapesButton);
 		learnShapesButton.setOnClickListener(this);
 
 		ImageButton learnColorsButton = (ImageButton) findViewById(R.id.learnColorsButton);
 		learnColorsButton.setOnClickListener(this);
 	}
 
 	public void onExitClick(View view) {
 		finish();
 	}
 
 	public void onSettingsClick(View view) {
 
 		final MathProblem mathProblem = MathProblemGenerator
 				.generateMathProblem();
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Please answer this question to access the Settings:");
 		alert.setMessage(mathProblem.getQuestion());
 		final EditText input = new EditText(this);
 		alert.setView(input);
		alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				int userAnswer = Integer.MAX_VALUE;
 
 				try {
 					userAnswer = Integer.parseInt(input.getText().toString());
 				} catch (Exception e) {
 					// TODO: call alert here too
 					return;
 				}
 				if (userAnswer == mathProblem.getAnswer()) {
 
 					startActivity(new Intent(EDUkid.this, SettingsView.class));
 				} else {
 					System.out.print("call your mommmy");
 				}
 			}
 		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 			}
 		});
 
 		alert.create();
 		alert.show();
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		// TODO Auto-generated method stub
 		switch (which) {
 		case DialogInterface.BUTTON_POSITIVE:
 			startActivity(new Intent(this, SettingsView.class));
 			break;
 		case DialogInterface.BUTTON_NEGATIVE:
 			System.out.print("Go call your mommy");
 			break;
 		}
 	}
 
 }
