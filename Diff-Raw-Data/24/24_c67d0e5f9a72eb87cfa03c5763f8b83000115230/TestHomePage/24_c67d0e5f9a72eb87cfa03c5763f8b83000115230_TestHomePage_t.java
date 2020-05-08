 package ar.noxit.ehockey.web;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.LocalDateTime;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import ar.noxit.ehockey.configuration.MenuItem;
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Torneo;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.service.IEquipoService;
 import ar.noxit.ehockey.service.IHorarioService;
 import ar.noxit.ehockey.service.ITablaPosicionesService;
 import ar.noxit.ehockey.service.ITorneoService;
 import ar.noxit.ehockey.service.IUsuarioService;
 import ar.noxit.ehockey.web.pages.HomePage;
 import ar.noxit.ehockey.web.pages.buenafe.EditarListaBuenaFePage;
 import ar.noxit.ehockey.web.pages.buenafe.VerListaBuenaFePage;
 import ar.noxit.ehockey.web.pages.header.IMenuItem;
 import ar.noxit.ehockey.web.pages.header.IMenuItemProvider;
 import ar.noxit.exceptions.NoxitException;
 
 import com.ttdev.wicketpagetest.MockableBeanInjector;
 
 /**
  * Simple test using the WicketTester
  */
 public class TestHomePage extends BaseSpringWicketTest {
 
     @Test
     public void testRenderHomePage() {
         startPageAndTestRendered(HomePage.class);
     }
 
     @Test
     public void testRenderVerListaBuenaFe() throws NoxitException {
         IEquipoService equipoService = createMock(IEquipoService.class);
         IClubService clubService = createMock(IClubService.class);
         List<Club> value = new ArrayList<Club>();
 
        expect(clubService.getAll()).andReturn(value).times(3);
         replay(equipoService);
         replay(clubService);
 
         MockableBeanInjector.mockBean("equipoService", equipoService);
         MockableBeanInjector.mockBean("clubService", clubService);
 
         startPageAndTestRendered(VerListaBuenaFePage.class);
     }
 
     @Test
     public void testRenderEditarListaBuenaFe() throws NoxitException {
         IEquipoService equiposService = createMock(IEquipoService.class);
         replay(equiposService);
         MockableBeanInjector.mockBean("equipoService", equiposService);
 
         IClubService clubService = createMock(IClubService.class);
         List<Club> value = new ArrayList<Club>();
 
        expect(clubService.getAll()).andReturn(value).times(3);
         replay(clubService);
         MockableBeanInjector.mockBean("clubService", clubService);
 
         startPageAndTestRendered(EditarListaBuenaFePage.class);
     }
 
     @BeforeMethod
     public void mockHeaderItems() throws NoxitException {
         // menu item provider
         IMenuItemProvider menuItemProvider = createMock(IMenuItemProvider.class);
 
         ArrayList<IMenuItem> value = new ArrayList<IMenuItem>();
         value.add(new MenuItem("home", HomePage.class, null));
         expect(menuItemProvider.getMenuItems()).andReturn(value);
 
         MockableBeanInjector.mockBean("menuItemProvider", menuItemProvider);
 
         // horario service
         IHorarioService horarioService = createMock(IHorarioService.class);
         expect(horarioService.getHoraSistema()).andReturn(new LocalDateTime());
         MockableBeanInjector.mockBean("horarioService", horarioService);
 
         // torneo service
         ITorneoService torneoService = createMock(ITorneoService.class);
        expect(torneoService.getAll()).andReturn(new ArrayList<Torneo>()).times(3);
         MockableBeanInjector.mockBean("torneoService", torneoService);
 
         // tabla service
         ITablaPosicionesService tablaService = createMock(ITablaPosicionesService.class);
         MockableBeanInjector.mockBean("tablaService", tablaService);
 
         // usuarios service
         IUsuarioService usuariosService = createMock(IUsuarioService.class);
         MockableBeanInjector.mockBean("usuarioService", usuariosService);
 
         // replay
         replay(torneoService, horarioService, menuItemProvider, tablaService);
     }
 }
