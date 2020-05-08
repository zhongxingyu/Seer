 package at.owlsoft.owl.communication.ejb;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Stateful;
 
 import at.owlsoft.owl.business.OwlApplicationContext;
 
 /**
  * Session Bean implementation class ApiProviderBean
  */
 @Stateful(mappedName = ApiProviderBeanRemote.JNDI_NAME)
 public class ApiProviderBean implements ApiProviderBeanRemote
 {
     @EJB(mappedName = RentalApiRemote.JNDI_NAME)
     private RentalApiRemote         _rentalApi;
 
     @EJB(mappedName = ReservationApiRemote.JNDI_NAME)
     private ReservationApiRemote    _reservationApi;
 
     @EJB(mappedName = SearchApiRemote.JNDI_NAME)
     private SearchApiRemote         _searchApi;
 
    @EJB(mappedName = SystemUserApi.JNDI_NAME)
     private SystemUserApi           _systemUserApi;
 
     @EJB(mappedName = ConfigurationApiRemote.JNDI_NAME)
     private ConfigurationApiRemote  _configurationApi;
 
     @EJB(mappedName = AuthenticationApiRemote.JNDI_NAME)
     private AuthenticationApiRemote _authenticationApi;
 
     @EJB(mappedName = MessagingApiRemote.JNDI_NAME)
     private MessagingApiRemote      _messagingApi;
 
     private OwlApplicationContext   _context;
 
     @PostConstruct
     public void initialize()
     {
         _context = new OwlApplicationContext();
     }
 
     @Override
     public RentalApiRemote createRentalApi()
     {
         _rentalApi.setContext(_context);
         return _rentalApi;
     }
 
     @Override
     public ReservationApiRemote createReservationApi()
     {
         _reservationApi.setContext(_context);
         return _reservationApi;
     }
 
     @Override
     public SearchApiRemote createSearchApi()
     {
         _searchApi.setContext(_context);
         return _searchApi;
     }
 
     @Override
     public SystemUserApiRemote createSystemUserApi()
     {
         _searchApi.setContext(_context);
         return _systemUserApi;
     }
 
     @Override
     public ConfigurationApiRemote createConfigurationApi()
     {
         _configurationApi.setContext(_context);
         return _configurationApi;
     }
 
     @Override
     public AuthenticationApiRemote createAuthenticationApi()
     {
         _configurationApi.setContext(_context);
         return _authenticationApi;
     }
 
     @Override
     public MessagingApiRemote createMessagingApi()
     {
         _messagingApi.setContext(_context);
         return _messagingApi;
     }
 
 }
