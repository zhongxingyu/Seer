 package nz.kapsy.counterexternal1;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import org.puredata.android.io.AudioParameters;
 import org.puredata.android.io.PdAudio;
 import org.puredata.core.PdBase;
 import org.puredata.core.PdReceiver;
 import org.puredata.core.utils.IoUtils;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.Menu;
 
 public class CounterExternal1Main extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		
 		try {
 			initPd();
 		} catch (IOException e) {
 			// TODO 自動生成された catch ブロック
 			e.printStackTrace();
 		}
 		//PdAudio.startAudio(this);
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		PdAudio.startAudio(this);
 	}
 	
 	@Override
 	protected void onStop() {
 		PdAudio.stopAudio();
 		super.onStop();
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private void initPd() throws IOException {
 		
 		Log.d("initPd", "initPD() called");
 		
 		Resources res = getResources();
 		File patchfile = null;
 		
 //		AudioParameters.init(this);
 //		int srate = Math.max(MIN_SAMPLE_RATE, AudioParameters.suggestSampleRate());
 		PdAudio.initAudio(22050, 0, 2, 1, true);
 		
 //		File dir = getFilesDir();
 //		File patchFile = new File(dir, "count_1.pd");
 //		IoUtils.extractZipResource(getResources().openRawResource(R.raw.count_1), dir, true);
 //		PdBase.openPatch(patchFile.getAbsolutePath());
 				
 		PdBase.setReceiver(receiver);
 		PdBase.subscribe("counter");		
 		
 		InputStream in = res.openRawResource(R.raw.count_1);
 		patchfile = IoUtils.extractResource(in, "count_1.pd", getCacheDir());
 		PdBase.openPatch(patchfile);
 		
 	}
 	
 	private void post(final String s) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Log.d("POST", s + ((s.endsWith("\n")) ? "" : "\n"));
 			}
 		});
 	}
 
 	private PdReceiver receiver = new PdReceiver() {
 
 //		private void pdPost(String msg) {
 //			post("Pure Data says, \"" + msg + "\"");
 //		}
 
 		@Override
 		public void print(String s) {
 			post(s);
 		}
 
 		@Override
 		public void receiveBang(String source) {
 			post("bang");
 			//Log.d("PdReceiver", "bang");
 		}
 
 		@Override
 		public void receiveFloat(String source, float x) {
 			post("float: " + x);
 		}
 
 		@Override
 		public void receiveList(String source, Object... args) {
 			post("list: " + Arrays.toString(args));
 		}
 
 		@Override
 		public void receiveMessage(String source, String symbol, Object... args) {
 			post("message: " + Arrays.toString(args));
 		}
 
 		@Override
 		public void receiveSymbol(String source, String symbol) {
 			post("symbol: " + symbol);
 		}
 	};
 	
 }
