 package de.uni.stuttgart.informatik.ToureNPlaner.UI.ConstraintFragments;
 
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockFragment;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 
 import java.io.Serializable;
 
 public class ConstraintFragment extends SherlockFragment {
 	protected Constraint constraint;
 	protected int index;
 	protected boolean dirty;
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable("constraint", constraint);
 		outState.putInt("index", index);
 		outState.putBoolean("dirty", dirty);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// If we get created for the first time we get our data from the intent
 		Bundle data = savedInstanceState != null ? savedInstanceState : getArguments();
 		constraint = (Constraint) data.get("constraint");
 		index = data.getInt("index");
 		dirty = data.getBoolean("dirty", false);
 	}
 
 	public boolean isDirty() {
 		return dirty;
 	}
 
 	public Serializable getValue() {
 		return constraint.getValue();
 	}
 
 	public int getIndex() {
 		return index;
 	}
 }
