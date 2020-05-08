 package game.interaction;
 
 import ioio.examples.hello.GameActivity;
 import ioio.examples.hello.R;
 import android.content.Context;
 import android.graphics.Point;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 
 public class InteractivePopupWindowHandler {
 
 	private GameActivity ga;
 	private TextView popText;
 	private PopupWindow popupWindow;
 	private ImageView bg;
 	private View v_pop;
     //private Drawable smile;
     private showPopWindowThread showThread;
 	public InteractivePopupWindowHandler(GameActivity ga){
 		this.ga = ga;
 		this.bg = (ImageView)ga.findViewById(R.id.background);
 	    //smile = ga.getResources().getDrawable(R.drawable.smile);
 		initPopWindow();
 	}
 	
 	private void initPopWindow(){
 		 Context mContext = ga;   
 		 LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		 v_pop = mLayoutInflater.inflate(R.layout.interactive_show_window, null,true);
 		 v_pop.setOnClickListener(new PopWindowOnClickListener());
 		 Point p = ga.getSize();
 		 int width_max = (int) (p.x * 0.8);
 		 int width = 400;
 		 if (width > width_max)
 			 width = width_max;
 		 int height = width * 11/8;
 		 popupWindow = new PopupWindow(v_pop,width,height);
 		 showThread = new showPopWindowThread();
 		 popText = (TextView)v_pop.findViewById(R.id.game_pop_text);
 	}
 	private class PopWindowOnClickListener implements View.OnClickListener{
 		public void onClick(View v) {
 		    popupWindow.dismiss();
 		}
 	}
 	public void showPopWindow(String pid){
		if (popupWindow.isShowing() == false){
			String cn = ga.getInteractiveGameHandler().getCodeNameByPID(pid);
			popText.setText(cn + "\n為您加油");
			popupWindow.setOutsideTouchable(false);
			bg.post( showThread);
		}
 	}
 	
 	private class showPopWindowThread implements Runnable{
 		@Override
 		public void run() {
 				popupWindow.showAtLocation( bg, Gravity.CENTER, 0, 0);
 				popupWindow.setFocusable(true);
 		        popupWindow.update();
 		}
 
 	}
 
 		
 
 }
