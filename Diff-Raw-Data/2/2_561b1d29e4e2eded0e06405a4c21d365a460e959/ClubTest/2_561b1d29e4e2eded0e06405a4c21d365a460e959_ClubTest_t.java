 package ar.noxit.ehockey.model;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import ar.noxit.ehockey.exception.EquipoInexistenteException;
 import ar.noxit.ehockey.exception.SinClubException;
 import org.joda.time.LocalDate;
 import org.testng.annotations.Test;
 
 public class ClubTest {
 
     @Test
     public void testCrearClub() {
         // creamos
         Club club = new Club("club");
 
         // chequeamos
        assertEquals(club.getNombreCompleto(), "club");
     }
 
     @Test(expectedExceptions = SinClubException.class)
     public void testRelacionarJugadores() throws SinClubException {
         Club club = new Club("club");
         Jugador j = club.crearNuevoJugador("ap", "no", new Sector("s"), new Division("d"), new LocalDate());
 
         assertEquals(j.getClub(), club);
         assertTrue(club.contiene(j));
 
         j.liberar();
         assertFalse(club.contiene(j));
 
         j.getClub();
     }
 
     @Test
     public void testRelacionarJugadores2() throws SinClubException {
         Club club = new Club("club");
         Club club2 = new Club("club");
         Jugador j = club.crearNuevoJugador("ap", "no", new Sector("s"), new Division("d"), new LocalDate());
 
         club2.agregarJugador(j);
 
         assertEquals(j.getClub(), club2);
         assertTrue(club2.contiene(j));
         assertFalse(club.contiene(j));
     }
 
     @Test
     public void testRelacionarJugadores3() throws SinClubException {
         Club club = new Club("club");
         Club club2 = new Club("club2");
         Jugador j = club.crearNuevoJugador("ap", "no", new Sector("s"), new Division("d"), new LocalDate());
 
         j.asignarClub(club2);
 
         assertEquals(j.getClub(), club2);
         assertTrue(club2.contiene(j));
         assertFalse(club.contiene(j));
     }
 
     @Test
     public void testRelacionarEquipos() throws EquipoInexistenteException {
         Club club = new Club("club");
         Equipo equipo = club.crearNuevoEquipo("equipo", new Division("d"), new Sector("s"));
 
         assertEquals(club, equipo.getClub());
         assertEquals(club.iteratorEquipos().next(), equipo);
 
         club.borrarEquipo(equipo);
 
         assertEquals(equipo.getClub(), null);
         assertFalse(club.iteratorEquipos().hasNext());
     }
 }
