 package com.cysnake.syncapp.act;
 
 import java.util.List;
 
 import com.cysnake.syncapp.dao.ContactDao;
 import com.cysnake.syncapp.dao.PersonDao;
 import com.cysnake.syncapp.po.ContactPO;
 import com.cysnake.syncapp.po.PersonPO;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class StartAct extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_start);
 		importPersonFromContact(this);
 		modify();
 	}
 
 	private void modify() {
 
 	}
 
 	private void importPersonFromContact(Activity act) {
 		ContactDao contactDao = new ContactDao(act);
 		List<ContactPO> allContacts = contactDao.findAll();
 		PersonDao personDao = new PersonDao(act);
 		personDao.open();
 		personDao.beginTransaction();
 		for (ContactPO contact : allContacts) {
 			PersonPO person = new PersonPO(contact);
 			if (!personDao.isExist(person)) {
 				personDao.insert(person);
			}else{
 				personDao.update(person);
 			}
 		}
 		personDao.setTransactionSuccessful();
 		personDao.endTransaction();
 		personDao.close();
 
 	}
 }
