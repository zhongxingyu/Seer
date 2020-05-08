 package ca.charland.tanita;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.content.ContentValues;
 import android.os.Bundle;
 import android.widget.Button;
 
 /**
  * The Class AbstractBaseActivity which holds the next button so all the classes don't have to repeat it and allows for entry of your number.
  * 
  * @author mcharland
  */
 public abstract class AbstractBaseActivity extends RoboActivity {
 
 	/** The next button. */
 	@InjectView(R.id.next)
 	private Button next;
 
 	/**
 	 * Gets the layout res id.
 	 * 
 	 * @return the layout res id
 	 */
 	abstract int getLayoutResID();
 
 	/**
 	 * Gets the values to insert into the database.
 	 * 
 	 * @return The values to insert.
 	 */
 	protected abstract ContentValues getValues();
 
 	/** {@inheritDoc} */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(getLayoutResID());
 		next.setOnClickListener(new NextButtonOnClickListener(this));
 	}
 
 	/**
 	 * Gets the next class.
 	 * 
 	 * @return the next class
 	 */
 	protected abstract Class<?> getNextClass();
 }
