 package fr.univnantes.alma.hadlm2.composant;
 
 import fr.univnantes.alma.hadlm2.connecteur.ConnecteurSS;
 import fr.univnantes.alma.hadlm2.connecteur.ConnecteurPool;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  *
  * @author Julien Durillon
  */
 public abstract class Configuration extends Composant implements Observer {
 
     protected List<Composant> composants;
     protected ConnecteurPool connecteurs;
 
     public Configuration() {
         composants = new ArrayList<Composant>();
         connecteurs = new ConnecteurPool();
     }
 
     public abstract ConnecteurSS getConnecteur(Composant source, Method roleFrom);
 
     public void addConnecteur(ConnecteurSS conn) {
        this.connecteurs.add(conn);
     }
 
 
     public final void addComposant(Composant comp) {
         composants.add(comp);
         comp.addObserver(this);
         comp.setParent(this);
     }
 
     @Override
     public void update(Observable o, Object arg) {
 
         if (!(o instanceof Composant)) {
             //TODO: add logs
             return;
         }
 
         Composant cSource = (Composant) o;
 
 
         if (!(arg instanceof Object[])) {
             //TODO: add logs
             return;
         }
 
        Object[] args = (Object[]) arg;
        if (args.length < 2) {
            //TODO: add logs
            return;
        }

        String from = (String) args[0];
        Object[] reste = (Object[]) args[1];

        ConnecteurSS conn = this.connecteurs.get(cSource, from);
        conn.glue(reste);
 
 
     }
 }
