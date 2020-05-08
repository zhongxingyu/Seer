 package rdproject.serviceImpl;
 
 import org.jasypt.util.password.StrongPasswordEncryptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Errors;
 
 import rdproject.dao.RdUserDao;
 import rdproject.form.RdCreateAccountForm;
 import rdproject.model.User;
 import rdproject.service.RdAccountService;
 
 /**
  * 
  * @author Samuel Aquino
  * 
  */
 @Service
 public class RdAccountServiceImpl implements RdAccountService
 {
 	
 	@Autowired
 	@Qualifier("passwordEncryptor")
 	private StrongPasswordEncryptor encryptor;
 	
 	@Autowired
 	private RdUserDao userDao;
 
 	/**
 	 * Authenticates a user.
 	 */
 	@Override
 	public User authenticate(String username, String password, Errors errors)
 	{
 		User user = userDao.getUser(username);
 		
 		if(user != null)
 		{
 			if(!encryptor.checkPassword(password, user.getPassword()))
 			{
 				user = null;
 				errors.reject("error.credentials", "error.credentials");
 			}
 		}
 		else
 		{
 			errors.reject("error.credentials", "error.credentials");
 		}
 		
 		return user;
 	}
 
 	@Override
 	public void createUserAcct(RdCreateAccountForm form, Errors errors) 
 	{
 		/** Make new account code*/
 		User user = new User();
 		if (!checkUsername(form,errors))
 		{
 			user = null;
 			errors.reject("errors.dupeuser", "errors.dupeuser");
 		}
 		else
 		{
 			user.setUsername(form.getUsername());
 		}
 		if (form.getPassword() != form.getRePassword())
 		{
 			user = null;
 			errors.reject("errors.repassword", "errors.repassword");
 		}
 		else
 		{
 			user.setPassword(encryptor.encryptPassword(form.getPassword()));
 		}
 		userDao.saveUser(user);
 	}
 
 	/**
 	 * try to search for a result
 	 * getSingleResult() in userDao.getUser()
 	 * catches an exception if no result is found
 	 */
 	@Override
 	public Boolean checkUsername(RdCreateAccountForm form, Errors errors) 
 	{
 		Boolean bool = false;
 			
 		if (userDao.getUser(form.getUsername()) != null)
 		{
 			bool = true;			
 		}
 		else
 		{
 			bool = false;
 		}
 		return bool;
 	}
 }
