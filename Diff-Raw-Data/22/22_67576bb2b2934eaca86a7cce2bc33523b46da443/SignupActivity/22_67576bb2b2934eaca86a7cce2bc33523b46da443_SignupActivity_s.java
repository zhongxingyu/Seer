 package org.ivyweb.android.cslearn;
 
 import java.util.List;
 
 import org.ivyweb.android.cslearn.models.User;
 
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SignupActivity extends DatabaseActivity {
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.signup);
         
         Button btn = (Button) findViewById(R.id.btn_signup);
         btn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				EditText etMail = (EditText) findViewById(R.id.et_email);
 				EditText etPasswd = (EditText) findViewById(R.id.et_passwd);
 				String mail = etMail.getText().toString();
 				String passwd = etPasswd.getText().toString();
 				if (newUser(mail, passwd)) {
 					Toast.makeText(SignupActivity.this, "Signup successfully.", Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(SignupActivity.this, "Signup failed.", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 	
 	private boolean newUser(String mail, String passwd) {
 		User user = new User(mail, passwd);
 		RuntimeExceptionDao<User, Integer> userDao = getHelper().getUserREDao();
		List<User> matches = userDao.queryForEq("mail", mail);
		if (!matches.isEmpty()) {
 			int id = userDao.create(user);
 			if (id > 0) return true;
 		}
 		return false;
 	}
 	
 }
