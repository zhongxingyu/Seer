 package com.marthym.oikonomos.server.services;
 
 import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.annotation.Secured;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.marthym.oikonomos.main.client.presenter.DashboardPresenterFactory.ContentPanelType;
 import com.marthym.oikonomos.main.client.services.AccountService;
 import com.marthym.oikonomos.main.client.services.DashboardDataService;
 import com.marthym.oikonomos.shared.exceptions.OikonomosException;
 import com.marthym.oikonomos.shared.exceptions.OikonomosUnathorizedException;
 import com.marthym.oikonomos.shared.model.Account;
 import com.marthym.oikonomos.shared.model.User;
 import com.marthym.oikonomos.shared.view.data.AccountsListData;
 import com.marthym.oikonomos.shared.view.data.ContentPanelData;
 import com.marthym.oikonomos.shared.view.data.DashboardData;
 import com.marthym.oikonomos.shared.view.data.EditAccountData;
 import com.marthym.oikonomos.shared.view.data.EntityType;
 import com.marthym.oikonomos.shared.view.data.LeftMenuData;
 import com.marthym.oikonomos.shared.view.data.TopNavigationData;
 
 @Repository
 @Service("dashboardDataService")
 public class DashboardDataServiceImpl extends RemoteServiceServlet implements DashboardDataService {
 	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardDataServiceImpl.class);
 
 	@Autowired
 	AccountService accountService;
 	
 	@Override
 	@Secured("ROLE_USER")
 	public DashboardData getDashboardData() throws OikonomosException {
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		if (authentication == null) throw new OikonomosUnathorizedException("error.message.user.unauthorized", "No authentification found !");
 		
 		DashboardData data = new DashboardData();
 		data.setTopNavigation(getTopNavigationData());
 		data.setLeftMenuData(getLeftMenuData());
 
 		return data;
 	}
 
 	@Override
 	@Secured("ROLE_USER")
 	public LeftMenuData getLeftMenuData() throws OikonomosException {
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		if (authentication == null) throw new OikonomosUnathorizedException("error.message.user.unauthorized", "No authentification found !");
 		
 		LeftMenuData leftMenuData = new LeftMenuData();
 		leftMenuData.addEntityCount(EntityType.ACCOUNT, (int)accountService.getCount());
 		//TODO: Add all entity found
 		
 		return leftMenuData;
 	}
 
 	@Override
 	@Secured("ROLE_USER")
 	public TopNavigationData getTopNavigationData() throws OikonomosException {
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		if (authentication == null) throw new OikonomosUnathorizedException("error.message.user.unauthorized", "No authentification found !");
 		User authentifiedUser = (User)authentication.getPrincipal();
 		TopNavigationData topNavigationData = new TopNavigationData();
 		topNavigationData.setAuthentifiedUser(authentifiedUser);
 		return topNavigationData;
 	}
 
 	@Override
 	public ContentPanelData getContentPanelData(ContentPanelType type, List<String> parameters) throws OikonomosException {
 		try {
 			switch (type) {
 			case ACCOUNTS:
 			case DASHBOARD:
 				return new AccountsListData(accountService.getList(true));
 			case ACCOUNT:
 				Account wantedAccount = null;
 				if (!parameters.isEmpty()) {
 					String wantedAccountId = parameters.get(0);
 					if(LOGGER.isDebugEnabled()) LOGGER.debug("Load account "+wantedAccountId);
 					wantedAccount = accountService.getEntity(Long.parseLong(wantedAccountId));
 				}
 				return new EditAccountData(wantedAccount);
 			}
 			return null;
 		} catch (Exception e) {
 			LOGGER.error(e.getClass()+": "+e.getLocalizedMessage());
 			if (LOGGER.isDebugEnabled()) LOGGER.debug("STACKTRACE", e);
 			throw new OikonomosException("error.message.unexpected", e.getClass()+": "+e.getLocalizedMessage());
 		}
 	}
 
 }
