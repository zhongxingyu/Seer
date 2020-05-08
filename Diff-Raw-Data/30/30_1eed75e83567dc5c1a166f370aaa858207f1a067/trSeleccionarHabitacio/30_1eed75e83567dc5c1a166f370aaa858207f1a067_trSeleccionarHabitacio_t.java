 package domainControllers;
 
 import java.util.Date;
 import tupleTypes.HabitacioTipusPreu;
 
public class trSeleccionarHabitacio extends Transaccio {
 
     private HabitacioTipusPreu dades;
     private String tipusHab;
     private Date dIni;
     private Date dFi;
     
     @Override
     public void executa() {
         // TODO
     }
     
     public HabitacioTipusPreu obtenirResultat() {
         // TODO
         dades = null;
         return dades;
     }
 }
