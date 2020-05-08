 package ar.noxit.ehockey.web.app;
 
 import ar.noxit.ehockey.exception.PlanillaNoDisponibleRuntimeException;
 import ar.noxit.ehockey.main.StartJetty;
 import ar.noxit.ehockey.model.Rol;
 import ar.noxit.ehockey.service.IPartidoService;
 import ar.noxit.ehockey.web.pages.HomePage;
 import ar.noxit.ehockey.web.pages.authentication.AuthSession;
 import ar.noxit.ehockey.web.pages.authentication.LoginPage;
 import ar.noxit.ehockey.web.pages.authentication.RolRenderableStrategy;
 import ar.noxit.ehockey.web.pages.base.MensajePage;
 import ar.noxit.ehockey.web.pages.buenafe.EditarListaBuenaFePage;
 import ar.noxit.ehockey.web.pages.buenafe.ListaBuenaFePage;
 import ar.noxit.ehockey.web.pages.buenafe.VerListaBuenaFePage;
 import ar.noxit.ehockey.web.pages.fechahora.FechaHoraPage;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorAltaPage;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorBajaPage;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorModificarPage;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorPage;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorVerPage;
 import ar.noxit.ehockey.web.pages.planilla.ModificarPlanillaPage;
 import ar.noxit.ehockey.web.pages.planilla.PlanillaPage;
 import ar.noxit.ehockey.web.pages.planilla.PlanillaPrecargadaPage;
 import ar.noxit.ehockey.web.pages.planilla.PlanillaPrinterFriendly;
 import ar.noxit.ehockey.web.pages.tablaposiciones.TablaPosicionesPage;
 import ar.noxit.ehockey.web.pages.torneo.ListadoTorneoPage;
 import ar.noxit.ehockey.web.pages.torneo.NuevoTorneoPage;
 import ar.noxit.ehockey.web.pages.torneo.ReprogramacionPartidoPage;
 import ar.noxit.ehockey.web.pages.torneo.TorneoPage;
 import ar.noxit.ehockey.web.pages.torneo.VerPartidosPage;
 import ar.noxit.ehockey.web.pages.usuarios.AltaUsuarioPage;
 import ar.noxit.ehockey.web.pages.usuarios.EditarUsuarioPage;
 import ar.noxit.ehockey.web.pages.usuarios.ListaUsuariosPage;
 import ar.noxit.exceptions.NoxitException;
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.Page;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.Response;
 import org.apache.wicket.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
 import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
 import org.apache.wicket.injection.web.InjectorHolder;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.WebRequestCycle;
 import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Application object for your web application. If you want to run this
  * application without deploying, run the Start class.
  * 
  * @see StartJetty.myproject.Start#main(String[])
  */
 public class EHockeyApplication extends AuthenticatedWebApplication {
 
     private static final class EHockeyWebRequestCycle extends WebRequestCycle {
 
         @SpringBean
         private transient IPartidoService partidoService;
         private static final Logger logger = LoggerFactory.getLogger(EHockeyWebRequestCycle.class);
 
         public EHockeyWebRequestCycle(WebApplication application, WebRequest request, Response response) {
             super(application, request, response);
             InjectorHolder.getInjector().inject(this);
 
             try {
                 partidoService.actualizarEstados();
             } catch (NoxitException e) {
                 logger.warn("error actualizando estados", e);
             }
 
             partidoService = null;
         }
     }
 
     private final class EHockeyRequestCycleProcessor extends WebRequestCycleProcessor {
 
         public EHockeyRequestCycleProcessor() {
         }
 
         @Override
         protected Page onRuntimeException(Page page, RuntimeException e) {
             if (e.getCause() instanceof PlanillaNoDisponibleRuntimeException) {
                 return new MensajePage("Planilla no disponible",
                         "La planilla no se encuentra disponible. La misma se puede acceder a"
                                 + " partir de una semana antes del partido");
             }
             return super.onRuntimeException(page, e);
         }
     }
 
     private final String appMode;
 
     /**
      * Constructor
      */
     public EHockeyApplication(String appMode) {
         Validate.notNull(appMode, "application mode cannot be null");
         this.appMode = appMode;
     }
 
     @Override
     protected void init() {
         super.init();
 
         addComponentInstantiationListener(new SpringComponentInjector(this));
 
         mount(new HybridUrlCodingStrategy("/listabuenafe", ListaBuenaFePage.class, false));
         mount(new HybridUrlCodingStrategy("/listabuenafe/ver", VerListaBuenaFePage.class, false));
         mount(new HybridUrlCodingStrategy("/listabuenafe/editar", EditarListaBuenaFePage.class, false));
         mount(new HybridUrlCodingStrategy("/planillas/final", PlanillaPage.class, false));
         mount(new HybridUrlCodingStrategy("/planillas/print", PlanillaPrinterFriendly.class, false));
         mount(new HybridUrlCodingStrategy("/planillas/precargada", PlanillaPrecargadaPage.class, false));
         mount(new HybridUrlCodingStrategy("/planillas/modificar", ModificarPlanillaPage.class, false));
         mount(new HybridUrlCodingStrategy("/torneos", TorneoPage.class, false));
         mount(new HybridUrlCodingStrategy("/torneos/crear", NuevoTorneoPage.class, false));
         mount(new HybridUrlCodingStrategy("/torneos/listado", ListadoTorneoPage.class, false));
         mount(new HybridUrlCodingStrategy("/torneos/ver", VerPartidosPage.class, false));
         mount(new HybridUrlCodingStrategy("/partidos/reprogramar", ReprogramacionPartidoPage.class, false));
         mount(new HybridUrlCodingStrategy("/jugadores", JugadorPage.class, false));
         mount(new HybridUrlCodingStrategy("/jugadores/alta", JugadorAltaPage.class, false));
         mount(new HybridUrlCodingStrategy("/jugadores/baja", JugadorBajaPage.class, false));
         mount(new HybridUrlCodingStrategy("/jugadores/modificar", JugadorModificarPage.class, false));
         mount(new HybridUrlCodingStrategy("/jugadores/ver", JugadorVerPage.class, false));
         mount(new HybridUrlCodingStrategy("/tablaposiciones", TablaPosicionesPage.class, false));
         mount(new HybridUrlCodingStrategy("/usuarios/listado", ListaUsuariosPage.class, false));
         mount(new HybridUrlCodingStrategy("/usuarios/alta", AltaUsuarioPage.class, false));
         mount(new HybridUrlCodingStrategy("/usuarios/modificar", EditarUsuarioPage.class, false));
         mount(new HybridUrlCodingStrategy("/resultado", MensajePage.class, false));
         mount(new HybridUrlCodingStrategy("/fechahora", FechaHoraPage.class, false));
         mount(new HybridUrlCodingStrategy("/login", LoginPage.class, false));
        // getApplicationSettings().setAccessDeniedPage(accessDeniedPage)
 
         RoleAuthorizationStrategy strategy = new RoleAuthorizationStrategy(this);
         strategy.add(new RolRenderableStrategy());
         getSecuritySettings().setAuthorizationStrategy(strategy);
 
         authorizationStrategy();
     }
 
     private void authorizationStrategy() {
         MetaDataRoleAuthorizationStrategy.authorize(HomePage.class, Rol.HOME);
 
         MetaDataRoleAuthorizationStrategy.authorize(ListaBuenaFePage.class, Rol.BUENA_FE);
         MetaDataRoleAuthorizationStrategy.authorize(VerListaBuenaFePage.class, Rol.VER_BUENA_FE);
         MetaDataRoleAuthorizationStrategy.authorize(EditarListaBuenaFePage.class, Rol.EDITAR_BUENA_FE);
 
         MetaDataRoleAuthorizationStrategy.authorize(TorneoPage.class, Rol.TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(NuevoTorneoPage.class, Rol.CREAR_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(ListadoTorneoPage.class, Rol.VER_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(VerPartidosPage.class, Rol.VER_PARTIDOS_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPage.class, Rol.VER_PLANILLAS_FINALES_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPage.class, Rol.VER_PLANILLAS_PRECARGADAS_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPrinterFriendly.class, Rol.VER_PLANILLAS_FINALES_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPrinterFriendly.class,
                 Rol.VER_PLANILLAS_PRECARGADAS_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPrecargadaPage.class, Rol.VER_PLANILLAS_FINALES_TORNEOS);
         MetaDataRoleAuthorizationStrategy
                 .authorize(PlanillaPrecargadaPage.class, Rol.VER_PLANILLAS_PRECARGADAS_TORNEOS);
         MetaDataRoleAuthorizationStrategy.authorize(PlanillaPrecargadaPage.class, Rol.VER_PLANILLAS_FINALES_TORNEOS);
         MetaDataRoleAuthorizationStrategy
                 .authorize(PlanillaPrecargadaPage.class, Rol.VER_PLANILLAS_PRECARGADAS_TORNEOS);
 
         // MetaDataRoleAuthorizationStrategy.authorize(
         // ReprogramacionPartidoPage.class, false));
         MetaDataRoleAuthorizationStrategy.authorize(JugadorPage.class, Rol.JUGADORES);
         MetaDataRoleAuthorizationStrategy.authorize(JugadorAltaPage.class, Rol.ALTA_JUGADORES);
         MetaDataRoleAuthorizationStrategy.authorize(JugadorBajaPage.class, Rol.BAJA_JUGADORES);
         MetaDataRoleAuthorizationStrategy.authorize(JugadorModificarPage.class, Rol.ACTUALIZACION_JUGADORES);
         MetaDataRoleAuthorizationStrategy.authorize(JugadorVerPage.class, Rol.LISTA_JUGADORES);
 
         MetaDataRoleAuthorizationStrategy.authorize(TablaPosicionesPage.class, Rol.TABLA_POSICIONES);
 
         MetaDataRoleAuthorizationStrategy.authorize(ListaUsuariosPage.class, Rol.USUARIOS);
         MetaDataRoleAuthorizationStrategy.authorize(AltaUsuarioPage.class, Rol.ALTA_USUARIOS);
         MetaDataRoleAuthorizationStrategy.authorize(EditarUsuarioPage.class, Rol.MODIF_USUARIOS);
 
         MetaDataRoleAuthorizationStrategy.authorize(MensajePage.class, Rol.MENSAJE);
 
         MetaDataRoleAuthorizationStrategy.authorize(FechaHoraPage.class, Rol.FECHA_HORA);
 
     }
 
     @Override
     protected IRequestCycleProcessor newRequestCycleProcessor() {
         return new EHockeyRequestCycleProcessor();
     }
 
     @Override
     public RequestCycle newRequestCycle(Request request, Response response) {
         return new EHockeyWebRequestCycle(this, (WebRequest) request, (WebResponse) response);
     }
 
     /**
      * @see wicket.Application#getHomePage()
      */
     @Override
     public Class<? extends WebPage> getHomePage() {
         return HomePage.class;
     }
 
     @Override
     public String getConfigurationType() {
         return appMode;
     }
 
     @Override
     protected Class<? extends WebPage> getSignInPageClass() {
         return LoginPage.class;
     }
 
     @Override
     protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
         return AuthSession.class;
     }
 }
