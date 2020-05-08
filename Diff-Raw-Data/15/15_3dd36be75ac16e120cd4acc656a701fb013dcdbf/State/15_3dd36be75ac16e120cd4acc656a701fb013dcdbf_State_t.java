 package edu.cmu.square.client.navigation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.client.Cookies;
 
 import edu.cmu.square.client.model.AsquareCase;
 import edu.cmu.square.client.model.GWTAppProperties;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtPackage;
 import edu.cmu.square.client.model.GwtSoftwarePackage;
 import edu.cmu.square.client.model.ProjectRole;
 
 /**
  * The state object will keep the state across different panes as they are
  * swapped across during navigation
  * 
  * @param <H>
  * 
  */
 public class State extends StateHandler
 {
 	private String sessionID = ""; // A unique session Id when a user logs on
 	private String userName = "anonymous"; // The name the user logs in with
 	private String fullName = "";
 	private String currentView = "LogInPane"; // The name of the current pane
 	// being displayed
 	private boolean isAuthenticated = false;
 	private boolean isSiteAdministrator = false; // This is a tool role, that
 	private boolean skipTeachStep = false; // Skip the teach step when navigating
 	private int userId; // indicates whether the
 	// user is Site
 	// Administrator.
 	private boolean isAccountLocked = false;
 
 	private ProjectRole userProjectRole = ProjectRole.None;
 
 	private GwtModesType mode = GwtModesType.ReadOnly;// Read, Write and Hide
 
 	private List<GwtSoftwarePackage> topPackageList = new ArrayList<GwtSoftwarePackage>();
 
 	private int currentRiskID = -1; // Contains the risk to be edited or view
 	private int riskCommand = 0;
 
 	private int projectID;
 	private int caseID;
 	private int totalPrioritizations;
 
 	//private String caseName ="";
 //	private AsquareCase caseName = null;
 	private String caseName = "";
 	private String projectName = "";
 	private String projectInspectionStatus = null;
 	private GWTAppProperties appProperties;
 
 	public State()
 		{
 
 		}
 	public void clear()
 	{
 		setSessionID("");
 		setUserName("anonymous");
 		setProjectName("");
 		
 		setCaseName("");
 		setCurrentView("LogInPane");
 		setAuthenticated(null);
 		setSiteAdministrator(false);
 		setAccountLocked(false);
 		setSkipTeachStep(false);
 		setFullName("");
 		setProjectInspectionStatus(null);
 
 		setUserProjectRole(null);
 
 		setMode(GwtModesType.NoAccess);
 
 		topPackageList.clear();
 		this.topPackageList = new ArrayList<GwtSoftwarePackage>();
 
 		setCurrentRisk(-1);
 		setRiskCommand(0);
 
 		setProjectID(-1);
 		setCaseID(-1);
 		setAppProperties(null);
 
 	}
 
 	public String getSessionID()
 	{
 		return sessionID;
 	}
 
 	public void setSessionID(String sessionID)
 	{
 		this.sessionID = sessionID;
 		Cookies.setCookie("sessionID", sessionID);
 	}
 
 	public String getUserName()
 	{
 		return userName;
 	}
 
 	public void setUserName(String userName)
 	{
 		this.userName = userName;
 		Cookies.setCookie("userName", userName);
 	}
 
 	public String getFullName()
 	{
 		return this.fullName;
 	}
 	public void setFullName(String value)
 	{
 		Cookies.setCookie("FullName", String.valueOf(value));
 		this.fullName = value;
 	}
 
 	public String getCurrentView()
 	{
 		return currentView;
 	}
 
 	public void setCurrentView(String currentView)
 	{
 		Cookies.setCookie("currentView", currentView);
 		this.currentView = currentView;
 	}
 
 	public Boolean isAuthenticated()
 	{
 		return isAuthenticated;
 	}
 
 	public void setAuthenticated(Boolean isAuthenticated)
 	{
 		if (isAuthenticated != null)
 		{
 			this.isAuthenticated = isAuthenticated;
 			Cookies.setCookie("isAuthenticated", String.valueOf(isAuthenticated));
 			super.fireEvent("authenticated", isAuthenticated);
 		}
 		else
 		{
 			this.isAuthenticated = false;
 			Cookies.setCookie("isAuthenticated", String.valueOf(false) );
 		}
 
 	}
 
 	public ProjectRole getUserProjectRole()
 	{
 		return userProjectRole;
 	}
 
 	public void setUserProjectRole(ProjectRole userProjectRole)
 	{
 		if (userProjectRole != null)
 		{
 			Cookies.setCookie("projectRole", userProjectRole.name());
 			this.userProjectRole = userProjectRole;
 			fireEvent("role", userProjectRole);
 		}
 		else
 		{
 			Cookies.setCookie("projectRole", ProjectRole.None.name());
 			this.userProjectRole = ProjectRole.None;
 		}
 
 	}
 
 	
 	/**
 	 * @param 
 	 *            Skip the teach step
 	 */
 	public void setSkipTeachStep(boolean skipTeachStep)
 	{
 		Cookies.setCookie("skipTeachStep", String.valueOf(skipTeachStep));
 		this.skipTeachStep = skipTeachStep;
 	}
 
 	/**
 	 * @return the isSiteAdministrator
 	 */
 	public boolean getSkipTeachSetp()
 	{
 		return this.skipTeachStep;
 	}
 	
 	/**
 	 * @param isSiteAdministrator
 	 *            the isSiteAdministrator to set
 	 */
 	public void setSiteAdministrator(boolean isSiteAdministrator)
 	{
 		Cookies.setCookie("isSiteAdministrator", String.valueOf(isSiteAdministrator));
 		this.isSiteAdministrator = isSiteAdministrator;
 	}
 
 	/**
 	 * @return the isSiteAdministrator
 	 */
 	public boolean isSiteAdministrator()
 	{
 		return isSiteAdministrator;
 	}
 
 	/**
 	 * @param isAccountLocked
 	 *            the isAccountLocked to set
 	 */
 	public void setAccountLocked(boolean accountLocked)
 	{
 		this.isAccountLocked = accountLocked;
 	}
 
 	/**
 	 * @return the isAccountLocked
 	 */
 	public boolean isAccountLocked()
 	{
 		return isAccountLocked;
 	}
 
 	public void setAppProperties(GWTAppProperties appProperties)
 	{
 		this.appProperties = appProperties;
 	}
 
 	public GWTAppProperties getAppProperties()
 	{
 		return appProperties;
 	}
 
 	public void setProjectID(int projectID)
 	{
 		this.projectID = projectID;
 		Cookies.setCookie("projectID", String.valueOf(projectID));
 	}
 
 	public int getProjectID()
 	{
 		return projectID;
 	}
 	
 	public void setCaseID(int caseID)
 	{
 		this.caseID = caseID;
 		Cookies.setCookie("caseID", String.valueOf(caseID));
 	}
 
 	public int getCaseID()
 	{
 		return caseID;
 	}
 
 	public void setMode(GwtModesType mode)
 	{
 		this.mode = mode;
 		Cookies.setCookie("mode", mode.name());
 	}
 
 	public GwtModesType getMode()
 	{
 		return mode;
 	}
 
 	public void setTopPackageList(List<GwtSoftwarePackage> topPackageList)
 	{
 		this.topPackageList = topPackageList;
 	}
 
 	public List<GwtSoftwarePackage> getTopTechniqueList()
 	{
 		return topPackageList;
 	}
 
 	public void setCurrentRisk(int currentRisk)
 	{
 		this.currentRiskID = currentRisk;
 		Cookies.setCookie("currentRiskID", String.valueOf(currentRiskID));
 	}
 
 	public int getCurrentRisk()
 	{
 		return currentRiskID;
 	}
 
 	public void setRiskCommand(int riskCommand)
 	{
 		this.riskCommand = riskCommand;
 		Cookies.setCookie("riskCommand", String.valueOf(riskCommand));
 	}
 
 	public int getRiskCommand()
 	{
 		return riskCommand;
 	}
 
 	public void setProjectName(String projectName)
 	{
 		this.projectName = projectName;
 		Cookies.setCookie("projectName", projectName);
 		if (projectName.trim().length() > 0)
 		{
 			super.fireEvent("projectName", projectName);
 		}
 
 	}
 
 	public String getProjectName()
 	{
 		return projectName;
 	}
 	
 	public void setCaseName(String caseName)
 	{
 		this.caseName = caseName;
 		Cookies.setCookie("caseName", caseName);
 		if (caseName.trim().length() > 0)
 		{
 			super.fireEvent("caseName", caseName);
 		}
 	}
 
 	public String getCaseName()
 	{
 		return caseName;
 	}
 
 	/**
 	 * This methods is used from the entry point class to load the state from
 	 * the cookies after the user hit refresh. When the user hits refresh in the
 	 * browser all the client state is lost.
 	 */
 	public void loadStateFromCookies()
 	{
 		this.userName = Cookies.getCookie("userName");
 		this.fullName = Cookies.getCookie("FullName");
 		this.sessionID = Cookies.getCookie("sessionID");
 		this.currentView = Cookies.getCookie("currentView");
 		this.projectName = Cookies.getCookie("projectName");
		this.caseName = Cookies.getCookie("caseName");
		this.caseID = Integer.valueOf(Cookies.getCookie("caseID"));
 		this.projectInspectionStatus = Cookies.getCookie("projectInspectionStatus");
 		if (Cookies.getCookie("isSiteAdministrator") != null)
 		{
 			this.isSiteAdministrator = Boolean.parseBoolean(Cookies.getCookie("isSiteAdministrator"));
 		}
 		else
 		{
 			this.isSiteAdministrator = false;
 		}
 		
 		if (Cookies.getCookie("skipTeachStep") != null)
 		{
 			this.skipTeachStep = Boolean.parseBoolean(Cookies.getCookie("skipTeachStep"));
 		}
 		else
 		{
 			this.skipTeachStep = false;
 		}
 
 		if (Cookies.getCookie("isAuthenticated") != null)
 		{
 			this.isAuthenticated = Boolean.parseBoolean(Cookies.getCookie("isAuthenticated"));
 		}
 		else
 		{
 			this.isAuthenticated = false;
 		}
 
 		if (Cookies.getCookie("mode") != null)
 		{
 			this.mode = GwtModesType.valueOf(Cookies.getCookie("mode"));
 		}
 		else
 		{
 			this.mode = GwtModesType.NoAccess;
 		}
 
 		if (Cookies.getCookie("projectRole") != null)
 		{
 			this.userProjectRole = ProjectRole.valueOf(Cookies.getCookie("projectRole"));
 		}
 		else
 		{
 			this.userProjectRole = ProjectRole.None;
 		}
 
 		if (Cookies.getCookie("projectID") != null)
 		{
 			this.projectID = Integer.parseInt(Cookies.getCookie("projectID"));
 		}
 		else
 		{
 			this.projectID = -1;
 		}
 
 		if (Cookies.getCookie("userId") != null)
 		{
 			this.userId = Integer.parseInt(Cookies.getCookie("userId"));
 		}
 		else
 		{
 			this.userId = -1;
 		}
 
 		if (Cookies.getCookie("totalPrioritizations") != null)
 		{
 			this.totalPrioritizations = Integer.parseInt(Cookies.getCookie("totalPrioritizations"));
 		}
 		else
 		{
 			this.totalPrioritizations = -1;
 		}
 
 		if (Cookies.getCookie("currentRiskID") != null)
 		{
 			this.currentRiskID = Integer.parseInt(Cookies.getCookie("currentRiskID"));
 		}
 
 		if (Cookies.getCookie("riskCommand") != null)
 		{
 			this.riskCommand = Integer.parseInt(Cookies.getCookie("riskCommand"));
 		}
 	}
 
 	/**
 	 * @param userId
 	 *            the userId to set
 	 */
 	public void setUserId(int userId)
 	{
 		this.userId = userId;
 		Cookies.setCookie("userId", userId + "");
 	}
 
 	/**
 	 * @return the userId
 	 */
 	public int getUserId()
 	{
 		return userId;
 	}
 
 	/**
 	 * @param totalPrioritizations
 	 *            the totalPrioritizations to set
 	 */
 	public void setTotalPrioritizations(int totalPrioritizations)
 	{
 		this.totalPrioritizations = totalPrioritizations;
 		Cookies.setCookie("totalPrioritizations", totalPrioritizations + "");
 	}
 
 	/**
 	 * @return the totalPrioritizations
 	 */
 	public int getTotalPrioritizations()
 	{
 		return totalPrioritizations;
 	}
 
 	public void setProjectInspectionStatus(String projectInspectionStatus)
 	{
 
 		Cookies.setCookie("projectInspectionStatus", projectInspectionStatus);
 		this.projectInspectionStatus = projectInspectionStatus;
 	}
 
 	public String getProjectInspectionStatus()
 	{
 		return projectInspectionStatus;
 	}
 
 }
