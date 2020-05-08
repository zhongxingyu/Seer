 package brian.canadaShipping;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.event.ActionEvent;
 
 
 import net.objectlab.kit.datecalc.common.DateCalculator;
 import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
 import net.objectlab.kit.datecalc.common.HolidayHandlerType;
 import net.objectlab.kit.datecalc.jdk.DateKitCalculatorsFactory;
 
 
 @ManagedBean(name= "home")
 @ViewScoped
 public class CpstcHomeBean implements Serializable {
 	
 	// VARIABLES
 
 	private static final long serialVersionUID = 1L;
 	public String userOriginDpfName = "";
 	public String userDestinationDpfName = "";
 	public String pageResult = "";
 	private String userOriginPcName = "";
 	private String userDestinationPcName = "";
 	private Date selectedInputDate = new Date(System.currentTimeMillis());
 	private Date selectedOutputDate;
 	@SuppressWarnings("unused") private Date sentDate;
 	private Date calculatedArrivalBeginDate = new Date();
 	private Date calculatedArrivalEndDate = new Date();
 	private DateCalculator<Date> dateCalculator;
 	private boolean rangedArrivalDate = false;
 	
 	private boolean displayOutput = false;
 	
 	// day counts
 	private int baseTime = 0;
 	private final int remoteTime = 4;
 	private int totalTime = 0;
 	
 	// route entries
 	private ArrayList<CpstcRouteEntry> routeEntryList = new ArrayList<CpstcRouteEntry>();
 
 	private CpstcDedicatedProcessingFacility[] dpfGrid = CpstcApplicationBean.getDpfGrid();
 	
 	
 	//GETTERS & SETTERS	
 	
 	public ArrayList<CpstcRouteEntry> getRouteEntryList() {
 		return routeEntryList;
 	}
 	
 	public boolean getDisplayOutput()
 	{
 		return displayOutput;
 	}
 	
 	public Date getSelectedOutputDate()
 	{
 		return selectedOutputDate;
 	}
 	
 	public void setSelectedOutputDate(Date selectedOutputDate)
 	{
 		this.selectedOutputDate = selectedOutputDate;
 	}
 	
 	public Date getCalculatedArrivalBeginDate() {
 		return calculatedArrivalBeginDate;
 	}
 
 	public Date getCalculatedArrivalEndDate() {
 		return calculatedArrivalEndDate;
 	}
 	
 	public Date getSelectedInputDate() {
 		return selectedInputDate;
 	}
 	
     public void setSelectedInputDate(Date selectedDate) {
         this.selectedInputDate = selectedDate;
     }
 
 	
 	public String getUserOriginPcName() {
 		return userOriginPcName;
 	}
 
 	public String getUserDestinationPcName() {
 		return userDestinationPcName;
 	}
 
 	public void setUserDestinationPcName(String userDestinationPcName) {
 		this.userDestinationPcName = userDestinationPcName;
 	}
 
 	public void setUserOriginPcName(String userOriginPcName) {
 		this.userOriginPcName = userOriginPcName;
 	}
 
 	public String getPageResult() {
 		return pageResult;
 	}
 
 	public void setPageResult(String pageResult) {
 		this.pageResult = pageResult;
 	}
 	
 	public String getUserDestinationDpfName() {
 		return userDestinationDpfName;
 	}
 
 	public void setUserDestinationDpfName(String userDestinationDpfName) {
 		this.userDestinationDpfName = userDestinationDpfName;
 	}
 
 	public String getUserOriginDpfName() {
 		return userOriginDpfName;
 	}
 
 	public void setUserOriginDpfName(String userSourceName) {
 		this.userOriginDpfName = userSourceName;
 	}
 
 	
 	// CONSTRUCTOR
 	
 	public CpstcHomeBean()
 	{
 		super();
 		init();
 		System.out.println("Hello, HomeBean!");
 	}
 	
 	
 	// METHODS
 	
 	private void init()
 	{
 		displayOutput = false;
 	}
 	
 	/*
 	 * @called: when "Submit" button is activated
 	 */
 	public void submitAction(ActionEvent evt)
 	{	
 		pageResult = "";
 		totalTime = 0;
 		rangedArrivalDate = false;
 		
 		sentDate = selectedInputDate;
 		CpstcPostalCode userOriginPostalCode = new CpstcPostalCode(userOriginPcName);
 		CpstcPostalCode userDestinationPostalCode = new CpstcPostalCode(userDestinationPcName);
 		
 		userOriginDpfName = userOriginPostalCode.getDpfName();
 		userDestinationDpfName = userDestinationPostalCode.getDpfName();
 		
 		int sourceIndex = CpstcDedicatedProcessingFacility.getIndexOf(userOriginDpfName);
 		try {
 			baseTime = dpfGrid[sourceIndex].getBaseTime(userDestinationDpfName);
 			totalTime += baseTime;
 			pageResult = Integer.toString(baseTime);
 			// for now, do not differentiate between which PC (source or dest.)
 			// is remote, but it would be possible.
 			if (userOriginPostalCode.getRemote() || userDestinationPostalCode.getRemote())
 			{
 				// is remote transaction
 				totalTime += remoteTime;
 				rangedArrivalDate = true;
 			}
 			
 			// handle ranged arrival time due to remote PC
 			calculatedArrivalBeginDate = addBusinessDays(baseTime);
 			if (rangedArrivalDate)
 			{
 				pageResult += " to " + Integer.toString(baseTime + remoteTime);
 				calculatedArrivalEndDate = addBusinessDays(totalTime);
 			} else
 			{
 				calculatedArrivalEndDate = calculatedArrivalBeginDate;
 			}
 
 			// display resulting output
 			selectedOutputDate = calculatedArrivalEndDate;
			pageResult += " business days, or by ";
 			displayOutput = true;
 			
 		} catch(ArrayIndexOutOfBoundsException e)
 		{
 			pageResult = "Source or destination not found.";
 		}
 	}
     
     /*
      * @param daysToAdd - number of business days to add to selectedInputDate
      * @returns selectedInputDate plus daysToAdd counted by business days
      */
     private Date addBusinessDays(int daysToAdd)
     {
         HashSet<Date> holidays = new HashSet<Date>();
 
         DefaultHolidayCalendar<Date> holidayCalendar =
             new DefaultHolidayCalendar<Date>(holidays);
 
         DateKitCalculatorsFactory.getDefaultInstance()
                 .registerHolidays("example", holidayCalendar);
         dateCalculator = DateKitCalculatorsFactory.getDefaultInstance()
                 .getDateCalculator("example", HolidayHandlerType.FORWARD);
         dateCalculator.setStartDate(selectedInputDate);
         return (dateCalculator).moveByBusinessDays(daysToAdd).getCurrentBusinessDate();
     }
 
 
 }
