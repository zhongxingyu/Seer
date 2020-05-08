 package com.github.scratch_android;
 
 import com.github.scratch_android.ButtonArea.OnToggleListener;
 import com.github.scratch_android.DropArea.OnCodeBlockDroppedListener;
 import com.github.scratch_android.MainActivity.ActivityToDragAreaListener;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.view.Menu;
 
 public class MainActivity extends Activity implements OnCodeBlockDroppedListener, OnToggleListener {
 
 	FragmentManager fm;
 	DragArea drag;
 	ActivityManager manager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		fm = getFragmentManager();
 		manager = new ActivityManager(fm);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onCodeBlockDropped(CodeBlock cb) {
 		manager.onCodeBlockDropped(cb);
 	}
 
 	public interface ActivityToDragAreaListener {
 		public void onCodeBlockDropped(CodeBlock cb);
 	}
 
 	@Override
 	public void onToggle(int fragment) {
 		manager.toggle_fragment(fragment);
 	}
 }
 
 class ActivityManager {
 	private ActivityToDragAreaListener activity_to_drag;
 	private FragmentManager fm;
 	private int[] drag_list = { R.layout.motion_blocks, R.layout.looks_blocks, R.layout.sound_blocks, 
 			R.layout.pen_blocks, R.layout.control_blocks, R.layout.sensing_blocks,
 			R.layout.operators_blocks, R.layout.variables_blocks };
 
 	public ActivityManager(FragmentManager fm) {
 		this.fm = fm;
 		add_fragnment(0);
 	}
 
 	private void add_fragnment(int fragment) {
 		FragmentTransaction ft = fm.beginTransaction();
 		Bundle bundle = new Bundle();
 		bundle.putInt("block_id", drag_list[fragment]);
 		DragArea drag = new DragArea();
		activity_to_drag = (ActivityToDragAreaListener) drag;
 		drag.setArguments(bundle);
 		ft.add(R.id.drag_area_fragment, drag, "dynamic_drag_fragment");
 		ft.commit();
 	}
 
 	public void toggle_fragment(int fragment) {
 		FragmentTransaction ft = fm.beginTransaction();
 		Bundle bundle = new Bundle();
 		bundle.putInt("block_id", drag_list[fragment]);
 		DragArea drag = new DragArea();
		activity_to_drag = (ActivityToDragAreaListener) drag;
 		drag.setArguments(bundle);
 		ft.replace(R.id.drag_area_fragment, drag, "dynamic_drag_fragment");
 		ft.commit();
 	}
 
 	public void onCodeBlockDropped(CodeBlock cb) { 
 		activity_to_drag.onCodeBlockDropped(cb);
 	}
 }
