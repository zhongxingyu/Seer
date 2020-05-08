 package com.hael.beans;
 
 import java.util.List;
import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hael.wsClient.IOException_Exception;
 import com.hael.wsClient.JumpCalculator;
 import com.hael.wsClient.JumpCalculatorImplService;
 import com.hael.wsClient.NoPathExistsException_Exception;
 import com.hael.wsClient.NotARealSolarSystemException;
 import com.hael.wsClient.NotEnoughArgumentsException;
 import com.hael.wsClient.SolarSystem;
 
 /**
  * The backing bean for the index page.
  * 
  * @author alecross
  * 
  */
 @ManagedBean
 @SessionScoped
 public class AutoCompleteBean {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(AutoCompleteBean.class);
 	private String currentSystem, toSystem;
 	private float minSecurity = -1f;
 	private float maxSecurity = 1f;
 	private List<SolarSystem> route;
 	private JumpCalculatorImplService service = new JumpCalculatorImplService();
 	private JumpCalculator jumpCalculator = service.getJumpCalculatorImplPort();
 
 	public AutoCompleteBean() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> requestHeaders = context.getExternalContext().getRequestHeaderMap();
		String trust = requestHeaders.get("eve_trusted");
		if ( trust != null && trust.equals("Yes")) {
			log.debug("Trust has been granted.");
			setCurrentSystem(requestHeaders.get("eve_solarsystemname"));
		}
		else {
			log.debug("Trust not granted.");
		}
 	}
 
 	/**
 	 * Set destination system.
 	 * 
 	 * @param toSystem
 	 *            destination solar system name
 	 */
 	public void setToSystem(String toSystem) {
 		this.toSystem = toSystem;
 	}
 
 	/**
 	 * Returns the name of the destination system.
 	 * 
 	 * @return the name of the destination system.
 	 */
 	public String getToSystem() {
 		return toSystem;
 	}
 
 	/**
 	 * Sets the current system.
 	 * 
 	 * @param currentSystem
 	 *            name of the current system.
 	 */
 	public void setCurrentSystem(String currentSystem) {
 		this.currentSystem = currentSystem;
 	}
 
 	/**
 	 * Returns the name of the current system.
 	 * 
 	 * @return the name of the current system.
 	 */
 	public String getCurrentSystem() {
 		return currentSystem;
 	}
 
 	/**
 	 * Returns a list of solar system names that start with the given string.
 	 * 
 	 * @param query
 	 *            the string to be matched
 	 * @return A list of solar system names that match the query
 	 * @throws IOException_Exception if db access fails
 	 */
 	public List<String> complete(String query) throws IOException_Exception {
 		log.trace("Autocompleting " + query);
 		return jumpCalculator.systemNameLookup(query);
 	}
 
 	/**
 	 * Sets the route.
 	 * 
 	 * @param route
 	 *            A list of SolarSystems describing a route through the galaxy.
 	 */
 	public void setRoute(List<SolarSystem> route) {
 		this.route = route;
 	}
 
 	/**
 	 * Get the route.
 	 * 
 	 * @return The last saved route.
 	 */
 	public List<SolarSystem> getRoute() {
 		return this.route;
 	}
 
 	/**
 	 * Find the shortest path between currentSystem and toSystem.
 	 * 
 	 * @throws NotEnoughArgumentsException
 	 *             if one or both arguments are missing/empty
 	 * @throws NotARealSolarSystemException
 	 *             if one or both arguments don't represent real solar systems
 	 * @throws IOException_Exception if db access fails
 	 * @throws NoPathExistsException_Exception
 	 *             when the destination is not reachable from the source
 	 */
 	public void findShortestPath() throws IOException_Exception {
 
 		try {
 			this.route = jumpCalculator.shortestRoute(this.currentSystem,
 					this.toSystem, minSecurity, maxSecurity);
 		} catch (NoPathExistsException_Exception e) {
 			FacesMessage error = new FacesMessage("There is no path that satisfies these criteria");
 			error.setSeverity(FacesMessage.SEVERITY_ERROR);
 			FacesContext.getCurrentInstance().addMessage(null, error);
 		} catch (NotARealSolarSystemException e) {
 			FacesMessage error = new FacesMessage(e.getMessage());
 			error.setSeverity(FacesMessage.SEVERITY_ERROR);
 			FacesContext.getCurrentInstance().addMessage(null, error);
 		} catch (NotEnoughArgumentsException e) {
 			FacesMessage error = new FacesMessage("Please fill in both to and from");
 			error.setSeverity(FacesMessage.SEVERITY_ERROR);
 			FacesContext.getCurrentInstance().addMessage(null, error);
 		}
 	}
 
 	/**
 	 * @return the minSecurity
 	 */
 	public float getMinSecurity() {
 		return minSecurity;
 	}
 
 	/**
 	 * @param minSecurity the minSecurity to set
 	 */
 	public void setMinSecurity(float minSecurity) {
 		this.minSecurity = minSecurity;
 	}
 
 	/**
 	 * @return the maxSecurity
 	 */
 	public float getMaxSecurity() {
 		return maxSecurity;
 	}
 
 	/**
 	 * @param maxSecurity the maxSecurity to set
 	 */
 	public void setMaxSecurity(float maxSecurity) {
 		this.maxSecurity = maxSecurity;
 	}
 
 }
