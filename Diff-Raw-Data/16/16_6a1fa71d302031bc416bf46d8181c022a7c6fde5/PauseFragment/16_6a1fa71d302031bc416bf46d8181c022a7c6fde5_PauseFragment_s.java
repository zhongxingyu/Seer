 package nz.ac.otago.linguistics.spre;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * A page where the user can take a break for a while.
  * 
  * @author Tonic Artos
  */
 public class PauseFragment extends Fragment {
 	public static final int MODE_BREAK = 0;
 	public static final int MODE_EXIT = 1;
 
 	protected ExperimentActivity main;
 	protected int mode;
 	private int blocksToGo;
 
 	private View.OnClickListener advClickListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			if (mode == MODE_EXIT) {
 				main.finish();
 			} else {
 				main.showNextSentence();
 			}
 		}
 	};
 
 	public static PauseFragment newInstance(ExperimentActivity main, int mode, int blocksToGo) {
 		PauseFragment f = new PauseFragment();
 		f.main = main;
 		f.blocksToGo = blocksToGo;
 		f.mode = mode;
 		return f;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.text_page, container, false);
 		if (mode == MODE_BREAK) {
			((TextView) v.findViewById(R.id.text)).setText(R.string.text_break_prefix + blocksToGo + R.string.text_break_suffix);
 		} else {
 			((TextView) v.findViewById(R.id.text)).setText(R.string.text_thanks);
 		}
 		((Button) v.findViewById(R.id.adv_button)).setOnClickListener(advClickListener);
 		return v;
 	}
 }
