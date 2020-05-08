 package jp.mulodo.demoapput.controller;
 
 import java.util.ArrayList;
 
 import jp.mulodo.demoapput.database.DataBase;
 import jp.mulodo.demoapput.error.ErrorAdd;
 import jp.mulodo.demoapput.error.ErrorBase;
 import jp.mulodo.demoapput.error.ErrorDelete;
 import jp.mulodo.demoapput.error.ErrorLogin;
 import jp.mulodo.demoapput.object.CustomerInfo;
 import jp.mulodo.demoapput.object.User;
 
 public class UserController {
 	
 	/**
 	 * Login in system
 	 * @throws ErrorBase 
 	 * @throws ErrorLogin if error
 	 */
 	public static boolean login(String UserName,String Password ) throws ErrorBase,NullPointerException
 	{
 		for (User user : DataBase.lUsers) {
 			boolean checkUserName = user.getUserName().equals(UserName);
 			boolean checkPassword = user.getPassword().equals(Password);
 			if (checkPassword && checkUserName )
 			{
 				return true;
 			}
 		}
 		ErrorBase error = new ErrorLogin();
 		throw error;
 	}
 	public static boolean add (CustomerInfo customer) throws ErrorBase
 	{
 		if (customer != null)
 		{
 			DataBase.lCutomers.add(customer);
 			return true;
 		}
 		ErrorBase error = new ErrorAdd();
 		throw error;
 	}
 	
 	
	public static boolean delete (int ID) throws ErrorDelete
 	{
		DataBase.lCutomers.remove(ID);
		return true;
 	}
 	
 	public static ArrayList<CustomerInfo> getAllCustomer() throws NullPointerException
 	{
 		return DataBase.lCutomers;
 	}
 	
 	public static ArrayList<User> getAllUser() throws NullPointerException
 	{
 		return DataBase.lUsers;
 	}
 	
 	
 	
 }
