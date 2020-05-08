 package nl.proteon.liferay.surfnet.security.auth;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.util.DateUtil;
 import com.liferay.portal.kernel.util.LocaleUtil;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.portal.kernel.util.StringUtil;
 import com.liferay.portal.model.PasswordPolicy;
 import com.liferay.portal.model.User;
 import com.liferay.portal.security.auth.AutoLogin;
 import com.liferay.portal.security.auth.AutoLoginException;
 import com.liferay.portal.service.PasswordPolicyLocalServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.portlet.expando.model.ExpandoColumn;
 import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoValue;
 import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
 import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
 import com.liferay.util.portlet.PortletProps;
 import nl.proteon.liferay.surfnet.expando.ExpandoStartupAction;
 import nl.proteon.liferay.surfnet.security.opensocial.exceptions.ScribeException;
 import nl.proteon.liferay.surfnet.security.scribe.OAuth2ServiceUtil;
 import org.scribe.model.Token;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ConextAutoLogin implements AutoLogin {
 
 	@Override
 	public String[] login(HttpServletRequest request,
 			HttpServletResponse response) throws AutoLoginException {
 		String[] credentials = null;
 		String accessToken = null;
 		User user = null;
 		final long companyId = PortalUtil.getCompanyId(request);
 		final String oAuthCode = request.getParameter("code");
 
 		if (oAuthCode != null && !"".equals(oAuthCode)) {
 			// handle callback from authorization request
 			Token token;
 			try {
 				_log.info("get access token");
 				token = OAuth2ServiceUtil.getAccesToken(oAuthCode);
 				accessToken = token.getToken();
 				credentials = updateLiferayUser(request, companyId);
 				if (credentials != null && !"".equals(credentials[0])) {
 					saveAccessToken(companyId, Long.parseLong(credentials[0]),
 							accessToken);
 				}
 			} catch (ScribeException e) {
 				_log.error(e, e);
 			}
 		} else {
 			// handle initial requests
 			credentials = updateLiferayUser(request, companyId);
 			user = getUser(Long.parseLong(credentials[0]));
 			// get access token from database
 			if (user != null) {
 				ExpandoTable table = null;
 				try {
 					table = ExpandoStartupAction.expandoTable(companyId);
 					final long tableId = table.getTableId();
 					final ExpandoColumn column = ExpandoColumnLocalServiceUtil
 							.getColumn(tableId,
 									ExpandoStartupAction.ACCESS_TOKEN_NAME);
 					if (column == null) {
 						_log.error("Column for field "
 								+ ExpandoStartupAction.ACCESS_TOKEN_NAME
 								+ " not in table " + table);
 						throw new SystemException();
 					}
 					final long columnId = column.getColumnId();
					ExpandoValue accessTokenValue = ExpandoValueLocalServiceUtil
							.getValue(tableId, columnId, user.getPrimaryKey());
					if (accessTokenValue != null) {
						accessTokenValue.getString();
					}
 				} catch (PortalException e) {
					_log.error("123 get expendo goes bad: " + e.getMessage());
 					_log.error(e, e);
 				} catch (SystemException e) {
					_log.error("get expendo goes bad: " + e.getMessage());
 					_log.error(e, e);
 				}
 			}
			_log.error("******** access token: " + accessToken);
 			// redirect to authorization URL to get an oAuth code
 			if (accessToken == null || "".equals(accessToken)) {
 				try {
 					_log.info("redirect to authorization URL");
 					OAuth2ServiceUtil.authorize(response);
 				} catch (IOException e) {
 					_log.error(e, e);
 				} catch (ScribeException e) {
 					_log.error(e, e);
 				}
 				return null;
 			}
 		}
 
 		if (user != null) {
 			// update Liferay groups based on groups from IDP
			// ConextGroupLocalServiceUtil.updateGroups(companyId, user,
			// accessToken);
 		}
 
 		return credentials;
 	}
 
 	private User addUser(long companyId, String screenName,
 			String emailAddress, String openId, String firstName,
 			String middleName, String lastName) {
 
 		User user = null;
 		// defaults for new user
 		boolean autoPassword = true;
 		String password1 = "ASDF7890jkl";
 		String password2 = "ASDF7890jkl";
 		boolean autoScreenName = true;
 		long facebookId = 0;
 		int prefixId = -1;
 		int suffixId = -1;
 		boolean male = true;
 		int birthdayMonth = 1;
 		int birthdayDay = 1;
 		int birthdayYear = 1970;
 		long[] groupIds = null;
 		long[] organizationIds = null;
 		long[] roleIds = null;
 		long[] userGroupIds = null;
 		boolean sendEmail = false;
 		long creatorUserId = 0;
 		Locale locale = LocaleUtil.getDefault();
 		String jobTitle = "";
 
 		ServiceContext serviceContext = new ServiceContext();
 		try {
 			user = UserLocalServiceUtil.addUser(creatorUserId, companyId,
 					autoPassword, password1, password2, autoScreenName,
 					screenName, emailAddress, facebookId, openId, locale,
 					firstName, middleName, lastName, prefixId, suffixId, male,
 					birthdayMonth, birthdayDay, birthdayYear, jobTitle,
 					groupIds, organizationIds, roleIds, userGroupIds,
 					sendEmail, serviceContext);
 
 			PasswordPolicy passwordPolicy = user.getPasswordPolicy();
 			if (passwordPolicy.getChangeRequired()
 					|| passwordPolicy.getChangeable()) {
 				_log.info("Setting password policy password change required to false for user "
 						+ user.getOpenId());
 				passwordPolicy.setChangeRequired(false);
 				passwordPolicy.setChangeable(false);
 				PasswordPolicyLocalServiceUtil
 						.updatePasswordPolicy(passwordPolicy);
 			}
 			user.setPasswordReset(false);
 			user = UserLocalServiceUtil.updateUser(user);
 		} catch (PortalException e) {
 			_log.error(e, e);
 		} catch (SystemException e) {
 			_log.error(e, e);
 		}
 		return user;
 	}
 
 	private User getUser(long id) {
 		User user = null;
 		try {
 			user = UserLocalServiceUtil.getUser(id);
 		} catch (Exception e) {
 			_log.error("could not find user with id " + id);
 			_log.error(e, e);
 		}
 		return user;
 	}
 
 	private User getUserByOpenId(long companyId, String openId) {
 		User user = null;
 		try {
 			user = UserLocalServiceUtil.getUserByOpenId(companyId, openId);
 		} catch (Exception e) {
 			_log.info("could not find user with openId " + openId);
 		}
 		return user;
 	}
 
 	private void saveAccessToken(Long companyId, Long userId, String accessToken) {
 		final User user = getUser(userId);
 		if (user != null) {
 			try {
 				ExpandoValueLocalServiceUtil.addValue(companyId, User.class
 						.getName(), ExpandoStartupAction
 						.expandoTable(companyId).getName(),
 						ExpandoStartupAction.ACCESS_TOKEN_NAME, user
 								.getPrimaryKey(), accessToken);
 			} catch (PortalException e) {
 				_log.error(e, e);
 			} catch (SystemException e) {
 				_log.error(e, e);
 			}
 		}
 	}
 
 	private String[] updateLiferayUser(HttpServletRequest request,
 			long companyId) {
 		String[] credentials = null;
 		if (request.getHeader(PortletProps.get("saml2.header.mapping.email")) != null) {
 			try {
 				PasswordPolicy passwordPolicy = PasswordPolicyLocalServiceUtil
 						.getDefaultPasswordPolicy(companyId);
 
 				if (passwordPolicy.getChangeRequired()
 						|| passwordPolicy.getChangeable()) {
 					_log.error("Setting password policy password change required to: false");
 					passwordPolicy.setChangeRequired(false);
 					passwordPolicy.setChangeable(true);
 					PasswordPolicyLocalServiceUtil
 							.updatePasswordPolicy(passwordPolicy);
 				}
 
 				String emailAddress = StringPool.BLANK;
 				String firstName = StringPool.BLANK;
 				String lastName = StringPool.BLANK;
 				String middleName = StringPool.BLANK;
 				String screenName = StringPool.BLANK;
 				String openId = StringPool.BLANK;
 
 				User user = null;
 
 				if (!(request.getHeader(PortletProps
 						.get("saml2.header.mapping.email")).equals(""))) {
 					emailAddress = request.getHeader(PortletProps
 							.get("saml2.header.mapping.email"));
 				}
 				if (!(request.getHeader(PortletProps
 						.get("saml2.header.mapping.screenname")).equals(""))) {
 					screenName = request.getHeader(PortletProps
 							.get("saml2.header.mapping.screenname"));
 					screenName = StringUtil.replace(screenName, new String[] {
 							StringPool.SLASH, StringPool.UNDERLINE,
 							StringPool.SPACE }, new String[] {
 							StringPool.PERIOD, StringPool.PERIOD,
 							StringPool.PERIOD });
 				}
 				if (!(request.getHeader(PortletProps
 						.get("saml2.header.mapping.id")).equals(""))) {
 					openId = request.getHeader(PortletProps
 							.get("saml2.header.mapping.id"));
 				}
 				if (!("".equals(request.getHeader(PortletProps
 						.get("saml2.header.mapping.fullname"))))) {
 					String fullName = request.getHeader(PortletProps
 							.get("saml2.header.mapping.fullname"));
 					firstName = fullName.substring(0, fullName.indexOf(" "));
 					middleName = "";
 					lastName = fullName
 							.substring(fullName.lastIndexOf(" ") + 1);
 					firstName = StringUtil.upperCaseFirstLetter(firstName);
 					lastName = StringUtil.upperCaseFirstLetter(lastName);
 				}
 				user = getUserByOpenId(companyId, openId);
 				if (user != null) {
 					user.setCompanyId(companyId);
 					user.setCreateDate(DateUtil.newDate());
 					user.setEmailAddress(emailAddress);
 					user.setFirstName(firstName);
 					user.setMiddleName(middleName);
 					user.setLastName(lastName);
 					user.setScreenName(screenName);
 					user.setPasswordReset(false);
 					UserLocalServiceUtil.updateUser(user);
 				} else {
 					user = addUser(companyId, screenName, emailAddress, openId,
 							firstName, middleName, lastName);
 				}
 				_log.info("user " + user.getOpenId() + " logged in");
 
 				credentials = new String[3];
 				credentials[0] = String.valueOf(user.getUserId());
 				credentials[1] = user.getPassword();
 				credentials[2] = Boolean.TRUE.toString();
 			} catch (Exception e) {
 				_log.error(e, e);
 			}
 		}
 		return credentials;
 	}
 
 	private static Log _log = LogFactoryUtil.getLog(ConextAutoLogin.class);
 }
