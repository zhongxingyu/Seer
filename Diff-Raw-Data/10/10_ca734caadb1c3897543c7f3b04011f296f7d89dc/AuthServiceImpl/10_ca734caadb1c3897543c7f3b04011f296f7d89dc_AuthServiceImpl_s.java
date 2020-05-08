 package edu.ibs.core.operation.logic;
 
 import edu.ibs.common.dto.AccountDTO;
 import edu.ibs.common.dto.UserDTO;
 import edu.ibs.common.enums.AccountRole;
 import edu.ibs.common.exceptions.IbsServiceException;
 import edu.ibs.common.interfaces.IAuthService;
 import edu.ibs.core.entity.Account;
 import edu.ibs.core.entity.User;
 import edu.ibs.core.gwt.EntityTransformer;
 import edu.ibs.core.operation.AdminOperations;
 import edu.ibs.core.operation.UserOperations;
 import edu.ibs.core.utils.ServerConstants;
 import edu.ibs.core.utils.ServletUtils;
 import edu.ibs.core.utils.ValidationUtils;
 import nl.captcha.Captcha;
 import org.apache.log4j.Logger;
 
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 
 public class AuthServiceImpl implements IAuthService {
 
 	private static final String EMPTY_CREDENTIALS_MSG = "Логин/Пароль не могут быть пустыми.";
 	private static final String INCORRECT_CAPTCHA_TXT = "Вы ввели неверные символы с картинки.";
 	private static final String PASSWORD_NOT_EQUAL_MSG = "Пароль не соответствует введённому.";
 	private static final String CANNOT_LOGIN_MSG = "Неверный логин или пароль.";
 	private static final String NO_PERMISSION_MSG = "У вас нет прав для выполнения данной операции.";
 
 	private UserOperations userLogic;
 	private AdminOperations adminLogic;
 
 	@Override
 	public AccountDTO login(String name, String pass) throws IbsServiceException {
 		AccountDTO dto = null;
 		// Если есть куки
 		if (name.equals(ServletUtils.getRequest().getSession().getAttribute(ServerConstants.SESSION_LOGIN))) {
             dto  = (AccountDTO) ServletUtils.getRequest().getSession().getAttribute(ServerConstants.SESSION_ACC);
 			return dto;
 		} else  if (!ValidationUtils.isEmpty(name) && !ValidationUtils.isEmpty(pass)) {
             try {
 				Account account = userLogic.login(name, pass);
 				if (account != null) {
 					dto = EntityTransformer.transformAccount(account);
 					ServletUtils.getRequest().getSession().setAttribute(ServerConstants.SESSION_LOGIN, name);
                     ServletUtils.getRequest().getSession().setAttribute(ServerConstants.SESSION_ACC, dto);
 					ServletUtils.getRequest().getSession().setAttribute(ServerConstants.ADMIN_ATTR,
 							AccountRole.ADMIN.equals(dto.getRole()));
 					return dto;
 				} else {
 					throw new IbsServiceException(CANNOT_LOGIN_MSG);
 				}
             } catch (NoResultException e) {
                 throw new IbsServiceException(CANNOT_LOGIN_MSG);
             }
 		}
 		return dto;
 	}
 
 	@Override
 	public void logout(final String login) throws IbsServiceException{
 		ServletUtils.getRequest().getSession().setAttribute(ServerConstants.SESSION_LOGIN, "");
         ServletUtils.getRequest().getSession().setAttribute(ServerConstants.ADMIN_ATTR, false);
		Logger.getLogger(this.getClass()).debug(String.format("Выполнен выход пользователем %s", login));
 	}
 
 	@Override
 	public AccountDTO register(String name, String password, String passwordConfirm, String captchaText)
 			throws IbsServiceException {
 
 		if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(password)
 				|| ValidationUtils.isEmpty(passwordConfirm) /*|| ValidationUtils.isEmpty(captchaText)*/) {
 			throw new IbsServiceException(EMPTY_CREDENTIALS_MSG);
 		} else if (!password.equals(passwordConfirm)) {
 			throw new IbsServiceException(PASSWORD_NOT_EQUAL_MSG);
 		} else {
 			try {
 			// Верификация текста капчи
 //			Captcha captcha = (Captcha) ServletUtils.getRequest().getSession().getAttribute(Captcha.NAME);
 //			if (captcha != null && captcha.isCorrect(captchaText)) {
 				return EntityTransformer.transformAccount(register(name, password));
 //			} else {
 //				throw new IbsServiceException(INCORRECT_CAPTCHA_TXT);
 //			}
 			} catch (IllegalArgumentException e) {
 				throw new IbsServiceException("Вы ввели недопустимый e-mail или ваш e-mail уже занят.");
 			} catch (Throwable t) {
 				throw new IbsServiceException("Ошибка при регистрации.");
 			}
 		}
 	}
 
 	@Override
 	public AccountDTO create(AccountRole role, String email, String password) throws IbsServiceException {
 		if (ValidationUtils.isEmpty(email) || ValidationUtils.isEmpty(password)) {
 			throw new IbsServiceException(EMPTY_CREDENTIALS_MSG);
 		} else if (!Boolean.TRUE.equals(
 				ServletUtils.getRequest().getSession().getAttribute(ServerConstants.ADMIN_ATTR))) {
 			throw new IbsServiceException(NO_PERMISSION_MSG);
 		} else {
 			try {
 				return EntityTransformer.transformAccount(adminLogic.createAccount(role, email, password));
 			} catch (IllegalArgumentException e) {
 				throw new IbsServiceException("Введён некорректный e-mail адрес или данный e-mail уже используется.");
 			} catch (Throwable t) {
 				throw new IbsServiceException("Произошла ошибка при создании пользователя.");
 			}
 		}
 	}
 
 	@Override
 	public UserDTO setUser(AccountDTO accountDTO, String firstName, String lastName, String passportNumber)
 			throws IbsServiceException {
 
 		UserDTO userDTO = null;
 		if (firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0
 				&& passportNumber != null && passportNumber.length() > 0) {
 			try {
 				User user = new User(firstName, lastName, passportNumber);
 				accountDTO.setUser(EntityTransformer.transformUser(user));
 				Account acc = new Account(accountDTO);
 				userLogic.update(acc);
 				userDTO = EntityTransformer.transformUser(acc.getUser());
 			} catch (IllegalArgumentException e) {
 				throw new IbsServiceException(
 						"Неверный номер паспорта. Должно быть заполнено 2 латинских буквы и 7 цифр без пробелов.");
 			} catch (Throwable e) {
 				throw new IbsServiceException("Не удалось обновить информацию о пользователе.");
 			}
 		}
 		return userDTO;
 	}
 
 	private Account register(String email, String passwd) throws PersistenceException {
 		return userLogic.register(email, passwd);
 	}
 
 	public UserOperations getUserLogic() {
         return userLogic;
     }
 
     public void setUserLogic(UserOperations userLogic) {
         this.userLogic = userLogic;
     }
 
     public AdminOperations getAdminLogic() {
         return adminLogic;
     }
 
     public void setAdminLogic(AdminOperations adminLogic) {
         this.adminLogic = adminLogic;
     }
 }
