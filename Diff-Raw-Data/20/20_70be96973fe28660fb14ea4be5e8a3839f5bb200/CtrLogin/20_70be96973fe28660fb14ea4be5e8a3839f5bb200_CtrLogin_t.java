 package controller;
 
 import java.util.concurrent.ExecutionException;
 
 import utils.CustomerRequester;
 import utils.Functions;
 import model.User;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import dataBase.DBConn2;
 
 public class CtrLogin {
 	/*public boolean validaUser(Context context, User u){
 		DBConn2 conn = new DBConn2(context);
 		String[] allColumns = { "username", "password"};
 		SQLiteDatabase db = conn.getDatabase();
 		Cursor cursor = db.query("User", allColumns, "username = '"+u.getLogin()+"' and password='"+u.getPass()+"'", null, null, null, null);
 		boolean answer = false;
     	if (cursor.moveToFirst())
     		answer = true;
     	cursor.close();
     	conn.Close(db);
     	return answer;
     }*/
 	public boolean validaUser(Context context, User u){
 		try {
 			User regUser = new CustomerRequester().execute(u.getLogin()).get();
 			System.out.println("RegUser pass "+regUser.getPass());
			System.out.println("User pass "+u.getPass()+" _MD5: "+Functions.md5(u.getPass()));
 			if(regUser.getPass() == Functions.md5(u.getPass()))
 				return true;
 			else
 				return false;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
