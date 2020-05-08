 package ar.noxit.ehockey.web.app;
 
 import ar.noxit.ehockey.main.StartJetty;
 import ar.noxit.ehockey.web.pages.HomePage;
 import ar.noxit.ehockey.web.pages.authentication.AuthSession;
 import ar.noxit.ehockey.web.pages.authentication.LoginPage;
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
 import ar.noxit.ehockey.web.pages.partido.PartidoPage;
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
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 
 /**
  * Application object for your web application. If you want to run this
  * application without deploying, run the Start class.
  * 
  * @see StartJetty.myproject.Start#main(String[])
  */
 public class EHockeyApplication extends AuthenticatedWebApplication {
 
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
         mount(new HybridUrlCodingStrategy("/partidos", PartidoPage.class, false));
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
