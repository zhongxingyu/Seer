 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.maty.j2ee.shiro;
 
 import java.io.Serializable;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.SimpleAuthenticationInfo;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authz.AuthorizationInfo;
 import org.apache.shiro.authz.SimpleAuthorizationInfo;
 import org.apache.shiro.codec.Hex;
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.session.Session;
 import org.apache.shiro.subject.PrincipalCollection;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.util.ByteSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.code.kaptcha.Constants;
 import com.maty.j2ee.entity.BaseUser;
 import com.maty.j2ee.service.BaseUserService;
 import com.maty.j2ee.service.exception.CaptchaException;
 import com.maty.j2ee.service.exception.LoginException;
 
 /**
  * manage login
  * 
  * @author calvin
  * 
  */
 @Service
 public class ShiroRealm extends AuthorizingRealm {
 
 	private static final Logger LOG = LoggerFactory.getLogger(ShiroRealm.class);
 
 	private BaseUserService baseUserService;
 
 	/**
 	 * 认证回调函数,登录时调用.
 	 */
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
 		CaptchaUsernamePasswordToken token = (CaptchaUsernamePasswordToken) authcToken;
 		if (StringUtils.isBlank(token.getUsername())) {
 			LOG.warn("Null usernames are not allowed by this realm.");
 			throw new LoginException("Null usernames are not allowed by this realm.", "login.error.username.null");
 		}
 		BaseUser user = baseUserService.findUserByLoginName(token.getUsername());
 		if (null == user) {
 			LOG.error("will throw UnknownAccountException!");
 			// super will throw UnknownAccountException
 			return null;
 		}
 		// 当错误次数达到一定时候需要验证码
 		// TODO:调回正常值
		if (user.getErrorCount() >= 12) {
 			if (StringUtils.isBlank(token.getCaptcha())) {
 				LOG.error("Null captcha is not allowed by this realm.");
 				throw new CaptchaException("Null captcha is not allowed by this realm.", "login.error.captcha.null");
 			}
 			Subject currentUser = SecurityUtils.getSubject();
 			Session session = currentUser.getSession();
 			Object sessionCaptcha = session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
 			if (null == sessionCaptcha) {
 				LOG.error("The captcha is invalid! Please re-enter the new captcha!");
 				throw new CaptchaException("The captcha is invalid! Please re-enter the new captcha!",
 						"login.error.captcha.overdue");
 			}
 			if (!token.getCaptcha().equalsIgnoreCase((String) sessionCaptcha)) {
 				LOG.error("The captcha is not correct, please enter again!");
 				throw new CaptchaException("The captcha is not correct, please enter again!",
 						"login.error.captcha.wrong");
 			}
 			// 移除验证码，不能用同一个验证码重复提交来试探密码
 			session.removeAttribute(Constants.KAPTCHA_SESSION_KEY);
 		}
 
 		if (StringUtils.isBlank(user.getPassword())) {
 			throw new UnknownAccountException("No account found for user [" + user.getAccount() + "]");
 		}
 
 		return new SimpleAuthenticationInfo(new ShiroUser(user.getAccount(), user.getRealName()), user.getPassword(),
 				ByteSource.Util.bytes(Hex.decode(user.getSalt())), getName());
 	}
 
 	/**
 	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
 	 */
 	@Override
 	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
 		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
 		BaseUser user = baseUserService.findUserByLoginName(shiroUser.loginName);
 
 		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
 		// for (Role role : user.get()) {
 		// //基于Role的权限信息
 		// info.addRole(role.getName());
 		// //基于Permission的权限信息
 		// info.addStringPermissions(role.getPermissionList());
 		// }
 		return info;
 	}
 
 	/**
 	 * @param baseUserService
 	 *            the baseUserService to set
 	 */
 	@Autowired
 	public void setBaseUserService(BaseUserService baseUserService) {
 		this.baseUserService = baseUserService;
 	}
 
 	/**
 	 * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
 	 */
 	public static class ShiroUser implements Serializable {
 		private static final long serialVersionUID = -1373760761780840081L;
 		private String loginName;
 		private String name;
 
 		/**
 		 * @param loginName
 		 * @param name
 		 */
 		public ShiroUser(String loginName, String name) {
 			this.loginName = loginName;
 			this.name = name;
 		}
 
 		/**
 		 * @return
 		 */
 		public String getName() {
 			return name;
 		}
 
 		/**
 		 * 本函数输出将作为默认的<shiro:principal/>输出.
 		 */
 		@Override
 		public String toString() {
 			return loginName;
 		}
 
 		/**
 		 * 重载equals,只计算loginName;
 		 */
 		@Override
 		public int hashCode() {
 			return HashCodeBuilder.reflectionHashCode(this, "loginName");
 		}
 
 		/**
 		 * 重载equals,只比较loginName
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			return EqualsBuilder.reflectionEquals(this, obj, "loginName");
 		}
 	}
 }
