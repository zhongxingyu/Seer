 package org.dpi.reparticion;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.dpi.configuracionAsignacionCreditos.AdministradorCreditosService;
 import org.dpi.creditsEntry.CambiosMultiplesEstadoMovimientosForm;
 import org.dpi.creditsEntry.CreditsEntry;
 import org.dpi.creditsEntry.CreditsEntry.GrantedStatus;
 import org.dpi.creditsEntry.CreditsEntryQueryFilter;
 import org.dpi.creditsEntry.CreditsEntryService;
 import org.dpi.creditsEntry.CreditsEntryServiceImpl;
 import org.dpi.creditsEntry.CreditsEntryVO;
 import org.dpi.creditsPeriod.CreditsPeriod;
 import org.dpi.creditsPeriod.CreditsPeriod.Status;
 import org.dpi.creditsPeriod.CreditsPeriodQueryFilter;
 import org.dpi.creditsPeriod.CreditsPeriodService;
 import org.dpi.employment.Employment;
 import org.dpi.employment.EmploymentCreditsEntriesService;
 import org.dpi.employment.EmploymentCreditsEntriesServiceImpl;
 import org.dpi.employment.EmploymentQueryFilter;
 import org.dpi.employment.EmploymentService;
 import org.dpi.employment.EmploymentStatus;
 import org.dpi.employment.EmploymentVO;
 import org.dpi.person.PersonService;
 import org.dpi.security.AccountSettings;
 import org.dpi.security.UserAccessService;
 import org.dpi.security.UserSettingsFactory;
 import org.dpi.security.UserSettingsFactoryImpl;
 import org.janux.bus.persistence.EntityNotFoundException;
 import org.janux.bus.security.Account;
 import org.janux.bus.security.AccountImpl;
 import org.janux.bus.security.AccountService;
 import org.janux.bus.security.Role;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.samples.travel.AjaxUtils;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 @Controller
 public class ReparticionController {
 
 
 	static Logger log = LoggerFactory.getLogger(ReparticionController.class);
 
 	/**  name of the http Request parameter which we use to pass an Reparticion's code: 'reparticionCode' */
 	public static final String PARAM_REPARTICION_ID = "reparticionId";
 
 	public static final String PARAM_CURR_PATH = "currPath";
 
 	public static final String KEY_CURRENT_REPARTICION_ID = "currentReparticionId";
 
 
 	/** 
 	 * key that we use when placing an Reparticion instance in a model: 'reparticion'
 	 * TODO: make private; have clients use session getters/putters
 	 */
 	public static final String KEY_REPARTICION = "reparticion";
 
 	/** 
 	 * The key by which we store in the Session the list of Reparticions that the
 	 * Principal is authorized to view: "myReparticions"
 	 */
 	private static final String KEY_REPARTICION_LIST = "myReparticiones";
 
 	/** name of a generic view used to display messages: 'MessagePage' */
 	public static final String VIEW_MESSAGE = "MessagePage";
 
 	/** key that we use when placing a Message string in a model: 'message' */
 	public static final String KEY_MESSAGE = "message";
 
 	private ReparticionService reparticionService;
 
 	@Resource(name = "accountServiceGeneric")
 	private AccountService accountService;
 
 	@Resource(name = "employmentService")
 	private EmploymentService employmentService;
 	
 	@Resource(name = "employmentCreditsEntriesService")
 	private EmploymentCreditsEntriesService employmentCreditsEntriesService;
 
 	@Resource(name = "personService")
 	private PersonService personService;
 
 
 	@Resource(name = "administradorCreditosService")
 	private AdministradorCreditosService administradorCreditosService;
 
 	@Resource(name = "creditsEntryService")
 	private CreditsEntryService creditsEntryService;
 	
 	@Resource(name = "creditsPeriodService")
 	private CreditsPeriodService creditsPeriodService;
 
 	@Resource(name = "accountSettingsFactory")
 	private UserSettingsFactory settingsFactory;	
 
 	@Inject
 	public ReparticionController(ReparticionService reparticionService) {
 		this.reparticionService = reparticionService;
 	}
 
 
 	public AdministradorCreditosService getAdministradorCreditosService() {
 		return administradorCreditosService;
 	}
 
 	public void setAdministradorCreditosService(
 			AdministradorCreditosService administradorCreditosService) {
 		this.administradorCreditosService = administradorCreditosService;
 	}
 
 	public EmploymentService getEmploymentService() {
 		return employmentService;
 	}
 
 
 	public void setEmploymentService(EmploymentService employmentService) {
 		this.employmentService = employmentService;
 	}
 
 	public static Reparticion getCurrentReparticion(HttpServletRequest request)
 	{
 		return (Reparticion) request.getAttribute(ReparticionController.KEY_REPARTICION);
 	}
 
 	@RequestMapping(value = "/reparticiones/reparticion/showCreditos", method = RequestMethod.GET)
 	public String showCreditos(HttpServletRequest request, HttpServletResponse response, Model model) {
 
 		// get the current reparticion in the session
 		final Reparticion reparticion = ReparticionController.getCurrentReparticion(request);
 
 		if (reparticion != null){
 
 			if (reparticion instanceof Reparticion) 
 			{
 				model.addAttribute(PARAM_REPARTICION_ID, reparticion.getId());
 			}
 			
 			CreditsPeriod currentCreditsPeriod = creditsPeriodService.getCurrentCreditsPeriod();
 			
 			Long creditosDisponiblesInicioPeriodoActual = this.administradorCreditosService.getCreditosDisponiblesAlInicioPeriodo(currentCreditsPeriod,reparticion.getId());
 			model.addAttribute("creditosDisponiblesInicioPeriodoActual", creditosDisponiblesInicioPeriodoActual);
 			
 			
 			Long creditosAcreditadosPorBajaDurantePeriodoActual = this.administradorCreditosService.getCreditosPorBajasDeReparticion(currentCreditsPeriod,reparticion.getId());
 			model.addAttribute("creditosAcreditadosPorBajaDurantePeriodoActual", creditosAcreditadosPorBajaDurantePeriodoActual);
 			
 			
 			
 			Long creditosConsumidosPorIngresosOAscensosSolicitadosPeriodoActual = this.administradorCreditosService.getCreditosPorIngresosOAscensosSolicitados(currentCreditsPeriod, reparticion.getId());
 			
 			model.addAttribute("creditosConsumidosPorIngresosOAscensosSolicitadosPeriodoActual", creditosConsumidosPorIngresosOAscensosSolicitadosPeriodoActual);
 			
 
 			Long creditosPorIngresosOAscensosOtorgadosPeriodoActual = this.administradorCreditosService.getCreditosPorIngresosOAscensosOtorgados(currentCreditsPeriod, reparticion.getId());
 			
 			model.addAttribute("creditosPorIngresosOAscensosOtorgados", creditosPorIngresosOAscensosOtorgadosPeriodoActual);
 
 			
 			
 
 			Long creditosDisponiblesSegunSolicitadoPeriodoActual = this.administradorCreditosService.getCreditosDisponiblesSegunSolicitado(currentCreditsPeriod,reparticion.getId());
 			model.addAttribute("creditosDisponiblesSegunSolicitadoPeriodoActual", creditosDisponiblesSegunSolicitadoPeriodoActual);
 			
 			Long creditosDisponiblesSegunOtorgadoPeriodoActual = this.administradorCreditosService.getCreditosDisponiblesSegunOtorgado(currentCreditsPeriod,reparticion.getId());
 			model.addAttribute("creditosDisponiblesSegunOtorgadoPeriodoActual", creditosDisponiblesSegunOtorgadoPeriodoActual);
 			
 						
 			
 			//historicos 2012
 					
 			Long creditosAcreditadosPorCargaInicial2012 = this.administradorCreditosService.getCreditosPorCargaInicialDeReparticion(currentCreditsPeriod.getPreviousCreditsPeriod(),reparticion.getId());
 			model.addAttribute("creditosAcreditadosPorCargaInicial2012", creditosAcreditadosPorCargaInicial2012);
 						
 			
 			Long creditosAcreditadosPorBajas2012 = this.administradorCreditosService.getCreditosPorBajasDeReparticion(currentCreditsPeriod.getPreviousCreditsPeriod(),reparticion.getId());
 			model.addAttribute("creditosAcreditadosPorBajas2012", creditosAcreditadosPorBajas2012);
 						
 			
 			Long creditosConsumidosPorIngresosOAscensosOtorgados2012 = this.administradorCreditosService.getCreditosPorIngresosOAscensosOtorgados(currentCreditsPeriod.getPreviousCreditsPeriod(), reparticion.getId());
 			
 			model.addAttribute("creditosConsumidosPorIngresosOAscensosOtorgados2012", creditosConsumidosPorIngresosOAscensosOtorgados2012);
 			
 			
 			Long saldoCreditosAlFinalPeriodo2012 = creditosAcreditadosPorCargaInicial2012+creditosAcreditadosPorBajas2012-creditosConsumidosPorIngresosOAscensosOtorgados2012;
 			
 			model.addAttribute("saldoCreditosAlFinalPeriodo2012", saldoCreditosAlFinalPeriodo2012);
 
 		}
 		return "reparticiones/show";
 	}
 
 
 	@RequestMapping(value = "/reparticiones/reparticion/showEmpleos", method = RequestMethod.GET)
 	public String showEmpleos(	HttpServletRequest request, 
 			HttpServletResponse response,
 			Model model) {
 		// get the current reparticion in the session
 		final Reparticion reparticion = ReparticionController.getCurrentReparticion(request);
 
 		if (reparticion != null){
 			
 			EmploymentQueryFilter empleoQueryFilter = new EmploymentQueryFilter();
 			empleoQueryFilter.setReparticionId(String.valueOf(reparticion.getId()));
 			empleoQueryFilter.addEmploymentStatus(EmploymentStatus.ACTIVO);
 			
 			List<Employment> activeEmployments = employmentService.find(empleoQueryFilter);
 			
 			//canAccountPromotePerson
 			Object accountObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			Account currenUser = (Account)accountObj;
 			List<EmploymentVO> employmentsVO = employmentCreditsEntriesService.buildEmploymentsVO(activeEmployments,reparticion.getId(),currenUser);
 			
 			model.addAttribute("activeEmployments", employmentsVO);
 			
 			
 			Account currentUser = (Account)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			model.addAttribute("canAccountProposeNewEmployment", EmploymentCreditsEntriesServiceImpl.canAccountProposeNewEmployment(currentUser));
 			
 
 
 		}
 		return "reparticiones/edit";
 	}
 
 	@RequestMapping(value = "/reparticiones/{reparticionId}", method = RequestMethod.POST)
 	public String edit(ReparticionImpl reparticion, BindingResult result,
 			@RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
 		if (result.hasErrors()) {
 			return "reparticiones/edit";
 		}
 		reparticionService.saveOrUpdate(reparticion);
 		return (AjaxUtils.isAjaxRequest(requestedWith)) ? "reparticiones/show" : "redirect:/reparticiones/" + reparticion.getId();
 	}
 
 	@RequestMapping(value = "/reparticiones/reparticion/showMovimientos/{creditsPeriodName}", method = RequestMethod.GET)
 	public String showMovimientos(
 			HttpServletRequest request, 
 			HttpServletResponse response,
 			@PathVariable Long creditsPeriodName,
 			Model model) {
 
 		// get the current reparticion in the session
 		final Reparticion reparticion = ReparticionController.getCurrentReparticion(request);
 		
 		
 		if (reparticion != null){
 			
 			//by default use current period
 			long currentPeriodName = creditsPeriodService.getCurrentCreditsPeriodYear();
 			
 			if(creditsPeriodName!=null){
 					if(creditsPeriodName<2012){
 						creditsPeriodName=currentPeriodName;
 					}else
 						if(creditsPeriodName>currentPeriodName){
 							creditsPeriodName=currentPeriodName;
 						}
 			}
 			else{//default to current period
 				creditsPeriodName=currentPeriodName;//TODO get year from current date or better the ACTIVE period
 			}
 			
 
 
 			EmploymentQueryFilter empleoQueryFilter = new EmploymentQueryFilter();
 			empleoQueryFilter.setReparticionId(reparticion.getId().toString());
 
 			CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
 			creditsEntryQueryFilter.setEmploymentQueryFilter(empleoQueryFilter);
 
 			CreditsPeriodQueryFilter creditsPeriodQueryFilter = new CreditsPeriodQueryFilter();
 			creditsPeriodQueryFilter.setName(String.valueOf(creditsPeriodName));
 			
 			List<CreditsPeriod> creditsPeriods = creditsPeriodService.find(creditsPeriodQueryFilter);
 			CreditsPeriod creditsPeriod = creditsPeriods.get(0);
 			creditsEntryQueryFilter.setIdCreditsPeriod(creditsPeriod.getId());
 			
 			List<CreditsEntry> creditsEntryReparticion = creditsEntryService.find(creditsEntryQueryFilter);
 			
 			Object accountObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			Account currentUser = (Account)accountObj;
 					
 			List<CreditsEntryVO> creditsEntryVOReparticion = creditsEntryService.buildCreditsEntryVO(creditsEntryReparticion,currentUser);
 					
			model.addAttribute("movimientos", creditsEntryVOReparticion);
 			
 			//------------------ Should we build the form for editing status? -----------------------------
 			
 			model.addAttribute("canAccountChangeCreditsEntryStatusOfPeriod",CreditsEntryServiceImpl.canChangeCreditsEntryStatus(currentUser, creditsPeriod));
 			
 			CambiosMultiplesEstadoMovimientosForm cambiosMultiplesEstadoMovimientosForm =  new CambiosMultiplesEstadoMovimientosForm();
 			for(CreditsEntryVO creditsEntryVO :creditsEntryVOReparticion){
 				cambiosMultiplesEstadoMovimientosForm.getMovimientos().add(creditsEntryVO.getCreditsEntry());
 			}
 			
 			model.addAttribute("grantedStatuses", GrantedStatus.values());
 			model.addAttribute("cambiosMultiplesEstadoMovimientosForm", cambiosMultiplesEstadoMovimientosForm);
 			
 			
 			
 			//------------------ Should we show the report generation button? -----------------------------			
 			
 			
 			boolean showReportGenerationButton = false;
 			boolean canGenerateReport = false;
 			//TODO create a permission by report generation
 			for(Role role : currentUser.getRoles()){
 				if(role.getName().equals("RESPONSABLE_REPARTICION")){
 					canGenerateReport = true;
 					break;
 				}	
 
 			}
 			
 			if(canGenerateReport){
 				model.addAttribute("canGenerateReport", canGenerateReport);
 				if(creditsPeriod.getStatus()==Status.Active){
 					Long creditosDisponiblesSegunSolicitadoPeriodoActual = this.administradorCreditosService.getCreditosDisponiblesSegunSolicitado(creditsPeriodService.getCurrentCreditsPeriod(),reparticion.getId());
 					showReportGenerationButton = creditosDisponiblesSegunSolicitadoPeriodoActual >=0;
 				}
 			}
 			
 			
 			model.addAttribute("showReportGenerationButton", showReportGenerationButton);
 			
			
			
			
			
			
 		}
 		return "reparticiones/creditsentries";
 	}
 	
 	
 
 	@RequestMapping(value = "/reparticiones/reparticion/movimientos/{movimientoId}/borrar", method = RequestMethod.GET)
 	public String borrarMovimiento(
 			HttpServletRequest request, 
 			HttpServletResponse response,
 			@PathVariable Long movimientoId,
 			Model model) {
 
 		// get the current reparticion in the session
 		final Reparticion reparticion = ReparticionController.getCurrentReparticion(request);
 
 		if (reparticion != null){
 			CreditsEntryQueryFilter creditsEntryQueryFilter = new CreditsEntryQueryFilter();
 			creditsEntryQueryFilter.setId(movimientoId);
 			EmploymentQueryFilter empleoFilter = new EmploymentQueryFilter();
 			empleoFilter.setReparticionId(reparticion.getId().toString());
 			creditsEntryQueryFilter.setEmploymentQueryFilter(empleoFilter);
 
 			CreditsEntry movimiento = creditsEntryService.find(creditsEntryQueryFilter).get(0);
 
 			if(movimiento!=null){
 				creditsEntryService.delete(movimiento);	
 			}
 		}
 
 		return "redirect:/reparticiones/reparticion/showMovimientos/"+creditsPeriodService.getCurrentCreditsPeriodYear();
 
 	}
 
 	/*@ExceptionHandler(MissingServletRequestParameterException.class)
 	public String handleMyException(Exception  exception) {
 		return "yourErrorViewName";
 	}*/
 
 
 	public CreditsEntryService getCreditsEntryService() {
 		return creditsEntryService;
 	}
 
 
 	public void setCreditsEntryService(
 			CreditsEntryService creditsEntryService) {
 		this.creditsEntryService = creditsEntryService;
 	}
 
 
 
 	/**
 	 * Reloads the list of reparticiones that a user is allowed to edit via the AccessService.
 	 * If the reparticiones list for a user changes, the changes will be seen immediately
 	 * @param request
 	 * @param accessService
 	 */
 	public static void refreshRequestReparticionList(HttpServletRequest request,UserAccessService accessService)
 	{
 		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		String accountName = ((Account) principal).getName();
 		Set<ReparticionAdminInfo> reparticiones = accessService.getReparticionListForAccount(accountName, null);
 
 		request.setAttribute(ReparticionController.KEY_REPARTICION_LIST,reparticiones);
 	}
 
 	/** 
 	 * Loads the Reparticion in the Request, and checks that the current account has the proper
 	 * permissions to view that Reparticion
 	 */
 	public static void refreshRequestReparticion(HttpServletRequest request,ReparticionService reparticionService,
 			UserAccessService accessService,boolean checkAccess)
 	{
 		boolean createSessionIfNeeded = true;
 		HttpSession session = request.getSession(createSessionIfNeeded);
 		Long reparticionId = (Long) session.getAttribute(ReparticionController.KEY_CURRENT_REPARTICION_ID);
 
 		AccountSettings settings = UserSettingsFactoryImpl.getSettingsForPrincipal();
 
 		checkAccess = checkAccess || (reparticionId == null); // first time in
 
 		if(reparticionId == null){
 			if(StringUtils.hasText(settings.getLastHotel())){
 				reparticionId = Long.parseLong(settings.getLastHotel());	
 			}
 		}
 
 		Reparticion reparticion = null;
 
 		if (reparticionId == null)
 		{
 			log.warn("There is currently no Reparticion active in the session");
 		}
 		else 
 		{
 			try
 			{ 
 				reparticion = reparticionService.findById(reparticionId);
 			}
 			catch (EntityNotFoundException e)
 			{
 				log.error(e.toString());
 				reparticion = null;
 			}
 
 			if (checkAccess)
 			{
 				Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 				Account account = (Account) principal;
 				boolean hasAccess = accessService.hasAccessToReparticion(account, reparticionId); 
 
 				if (!hasAccess)
 				{
 					reparticion = null;
 					settings.setLastHotel(null);
 					log.warn("Account: '" + account.getName() + "' attempting to access page without authorization");
 				}
 			}
 		}
 
 		request.setAttribute(ReparticionController.KEY_REPARTICION,reparticion);
 	}
 
 
 	@RequestMapping(value = "/reparticiones/select", method = RequestMethod.GET)
 	public ModelAndView selectReparticion(HttpServletRequest request, 
 			HttpServletResponse response,
 			@RequestParam("reparticionId") Long reparticionId,
 			@RequestParam("currPath") String currPath)
 					throws Exception 
 					{
 		ModelAndView returnView = null;
 
 		if (this.doSelectReparticion(request,reparticionId))
 		{
 			if (log.isDebugEnabled()) {log.debug("Doing redirect to: '" + currPath + "'");}
 			returnView = new ModelAndView(new RedirectView("/home",true));
 
 		}	
 		else
 		{
 			returnView = new ModelAndView(VIEW_MESSAGE, KEY_MESSAGE, "Unable to load Reparticion with code '" + reparticionId + "'");
 		}
 
 		return returnView;
 	}
 
 	/** 
 	 * deep loads an Reparticion based on a code passed in the query string parameter denoted by
 	 * ReparticionInfoController.PARAM_REPARTICION_ID and forwards to a summary display page 
 	 * @returns whether selection was successful (empty code allowed; for non-empty code, reparticion must
 	 * be able to be loaded.) 
 	 */
 	public boolean doSelectReparticion(HttpServletRequest request, Long id) 
 	{
 		boolean ok = true;
 		Reparticion reparticion = null;
 
 		if (id!=null)
 		{
 			reparticion = reparticionService.findById(id);
 
 			if (reparticion != null)
 			{
 
 				Account januxUserDetailsAccount = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
 				Account account = accountService.loadAccountByName(januxUserDetailsAccount.getName());
 
 				AccountSettings accountSettings = (AccountSettings)settingsFactory.getSettingsForAccount(account);
 
 				accountSettings.setLastHotel(String.valueOf(id));
 
 				accountService.saveOrUpdateAccount((AccountImpl)account);
 				request.getSession().setAttribute(KEY_CURRENT_REPARTICION_ID, reparticion.getId());
 				request.setAttribute(KEY_REPARTICION, reparticion);
 				if (log.isDebugEnabled()) {log.debug("Completed doing doSelectReparticion with ok status");}
 			}
 			else {
 				ok = false;
 			}
 		}
 
 		return ok;
 	}
 
 
 }
