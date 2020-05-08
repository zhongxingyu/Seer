 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package jipdbs.web.processors;
 
 import iddb.web.security.exceptions.InvalidAccountException;
 import iddb.web.security.exceptions.InvalidCredentialsException;
 import iddb.web.security.exceptions.UserLockedException;
 import iddb.web.security.service.UserService;
 import iddb.web.security.service.UserServiceFactory;
 import iddb.web.security.subject.Subject;
 import jipdbs.web.Flash;
 import jipdbs.web.MessageResource;
 
 import org.apache.commons.lang.StringUtils;
 
 import ar.sgt.resolver.exception.ProcessorException;
 import ar.sgt.resolver.flow.ForceRedirect;
 import ar.sgt.resolver.processor.ResolverContext;
 
 public class LoginProcessor extends FlashResponseProcessor {
 
	//private static final Logger log = LoggerFactory.getLogger(LoginProcessor.class);
 	
 	/* (non-Javadoc)
 	 * @see jipdbs.web.processors.FlashResponseProcessor#processProcessor(ar.sgt.resolver.processor.ResolverContext)
 	 */
 	@Override
 	public String processProcessor(ResolverContext context)
 			throws ProcessorException {
 		UserService userService = UserServiceFactory.getUserService();
 		if ("logout".equals(context.getParameter("type"))) {
 			userService.logout(context.getRequest());
			throw new ForceRedirect(context.getRequest().getContextPath());
 		} else {
 			Subject user = userService.getCurrentUser();
			String next = StringUtils.isEmpty(context.getRequest().getParameter("next")) ? context.getRequest().getContextPath() : context.getRequest().getParameter("next");
			if (user.isAuthenticated()) return next;
 			if (context.isPost()) {
 				try {
 					user = userService.authenticate(context.getRequest(), context.getRequest().getParameter("username"), context.getRequest().getParameter("password"));
 					throw new ForceRedirect(next);
 				} catch (InvalidAccountException e) {
 					Flash.error(context.getRequest(), MessageResource.getMessage("invalid_user_or_password")); 
 				} catch (InvalidCredentialsException e) {
 					Flash.error(context.getRequest(), MessageResource.getMessage("invalid_user_or_password"));
 				} catch (UserLockedException e) {
 					Flash.error(context.getRequest(), MessageResource.getMessage("locked_acount"));
 				}
 			}
 			context.getRequest().setAttribute("next", next);
 		}
 		return null;
 	}
 
 }
