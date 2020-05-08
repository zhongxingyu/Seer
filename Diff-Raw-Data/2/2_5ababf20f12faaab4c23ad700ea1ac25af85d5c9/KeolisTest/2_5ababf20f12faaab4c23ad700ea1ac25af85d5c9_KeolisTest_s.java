 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.ybo.opendata.rennes;
 
 
 import fr.ybo.opendata.rennes.modele.bus.Alert;
 import fr.ybo.opendata.rennes.modele.bus.ParkRelai;
 import fr.ybo.opendata.rennes.modele.bus.PointDeVente;
 import fr.ybo.opendata.rennes.modele.bus.StateParkRelai;
 import fr.ybo.opendata.rennes.modele.velos.Station;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static junit.framework.Assert.*;
 
 public class KeolisTest {
 
     private Keolis keolis;
 
     @Before
     public void setup() {
         keolis = new Keolis("key");
     }
 
     @Test
     public void testGetAlerts() throws KeolisReseauException {
         keolis.setConnecteur(new FileConnecteur("/getAlert.xml"));
 
         List<Alert> alerts = keolis.getAlerts();
         assertEquals(2, alerts.size());
         Alert alert1 = alerts.get(0);
         assertEquals("Marché À Bourgbarré", alert1.getTitleFormate());
         assertEquals("2010-09-27T00:00:00+02:00", alert1.getStarttime());
         assertEquals("2012-07-27T00:00:00+02:00", alert1.getEndtime());
         assertEquals(1, alert1.getLines().size());
         assertEquals("74", alert1.getLines().get(0));
         assertFalse(alert1.isMajordisturbance());
         assertNotNull(alert1.getDetail());
         Alert alert2 = alerts.get(1);
         assertEquals(2, alert2.getLines().size());
         assertTrue(alert2.isMajordisturbance());
     }
 
     @Test
     public void testGetStationByNumbers() throws KeolisReseauException {
         keolis.setConnecteur(new FileConnecteur("/getStations.xml"));
 
         List<Station> stations = keolis.getStationByNumbers(Arrays.asList("75", "65", "66"));
 
         assertEquals(1, stations.size());
         Station station1 = stations.get(0);
         assertEquals("75", station1.getNumber());
         assertEquals("ZAC SAINT SULPICE", station1.getName());
         assertEquals("RUE DE FOUGÈRES", station1.getAdresse());
         assertTrue(station1.isState());
         assertEquals(48.1321, station1.getLatitude());
         assertEquals(-1.63528, station1.getLongitude());
         assertEquals(14, station1.getSlotsavailable());
         assertEquals(16, station1.getBikesavailable());
         assertFalse(station1.isPos());
         assertEquals("Maurepas - Patton", station1.getDistrict());
         assertEquals("2011-11-09T19:59:06+01:00", station1.getLastupdate());
     }
 
     @Test
     public void testGetStations() throws KeolisReseauException {
         keolis.setConnecteur(new FileConnecteur("/getStations.xml"));
 
         List<Station> stations = keolis.getStations();
 
         assertEquals(2, stations.size());
         Station station1 = stations.get(0);
         assertEquals("75", station1.getNumber());
         assertEquals("ZAC SAINT SULPICE", station1.getName());
         assertEquals("RUE DE FOUGÈRES", station1.getAdresse());
         assertTrue(station1.isState());
         assertEquals(48.1321, station1.getLatitude());
         assertEquals(-1.63528, station1.getLongitude());
         assertEquals(14, station1.getSlotsavailable());
         assertEquals(16, station1.getBikesavailable());
         assertFalse(station1.isPos());
         assertEquals("Maurepas - Patton", station1.getDistrict());
         assertEquals("2011-11-09T19:59:06+01:00", station1.getLastupdate());
 
     }
 
     @Test
     public void testGetParkRelais() throws KeolisReseauException {
         keolis.setConnecteur(new FileConnecteur("/getParkRelais.xml"));
 
         List<ParkRelai> parkRelais = keolis.getParkRelais();
 
         assertEquals(4, parkRelais.size());
         ParkRelai parkRelai = parkRelais.get(0);
         assertEquals("Henri Fréville", parkRelai.getName());
         assertEquals(48.0875369773, parkRelai.getLatitude());
         assertEquals(-1.6745548715, parkRelai.getLongitude());
         assertEquals(317, parkRelai.getCarParkAvailable().intValue());
         assertEquals(406, parkRelai.getCarParkCapacity().intValue());
         assertEquals("2011-11-09T20:06:07+01:00", parkRelai.getLastupdate());
         assertEquals(StateParkRelai.OUVERT, parkRelai.getState());
         assertEquals(StateParkRelai.FERME, parkRelais.get(1).getState());
         assertEquals(StateParkRelai.COMPLET, parkRelais.get(2).getState());
         assertEquals(StateParkRelai.INDISPONIBLE, parkRelais.get(3).getState());
     }
 
     @Test
     public void testGetPointDeVente() throws KeolisReseauException {
         keolis.setConnecteur(new FileConnecteur("/getPos.xml"));
 
         List<PointDeVente> pointDeVentes = keolis.getPointDeVente();
 
         assertEquals(2, pointDeVentes.size());
         PointDeVente pointDeVente = pointDeVentes.get(0);
         assertEquals("Relais H / Gare SNCF", pointDeVente.getName());
         assertEquals("Tabac Presse", pointDeVente.getType());
         assertEquals("Place de la gare", pointDeVente.getAdresse());
         assertEquals("35000", pointDeVente.getCodePostal());
         assertEquals("RENNES", pointDeVente.getVille());
         assertEquals("Gares", pointDeVente.getDistrict());
        assertEquals(" 02 99 41 91 44", pointDeVente.getTelephone());
         assertEquals("", pointDeVente.getSchedule());
         assertEquals(48.1041574, pointDeVente.getLatitude());
         assertEquals(-1.6726879, pointDeVente.getLongitude());
 
     }
 
 }
