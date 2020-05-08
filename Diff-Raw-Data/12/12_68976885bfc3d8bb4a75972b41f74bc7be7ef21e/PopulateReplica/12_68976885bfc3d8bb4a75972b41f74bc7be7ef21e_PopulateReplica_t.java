 package org.daum.library.demos;
 
 import org.daum.common.model.api.Demand;
 import org.daum.library.ormH.persistence.PersistenceConfiguration;
 import org.daum.library.ormH.persistence.PersistenceSession;
 import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
 import org.daum.library.ormH.store.ReplicaStore;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.cache.ReplicaService;
 import org.kevoree.ContainerRoot;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.handler.ModelListener;
 import org.kevoree.framework.AbstractComponentType;
 import org.sitac.*;
 import org.sitac.impl.InterventionImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import scala.Some;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 04/07/12
  * Time: 13:42
  * To change this template use File | Settings | File Templates.
  */
 
 @Library(name = "JavaSE", names = {"Android"})
 @ComponentType
 @Requires({
         @RequiredPort(name = "service", type = PortType.SERVICE, className = ReplicaService.class, optional = false) })
 public class PopulateReplica extends AbstractComponentType {
 
     public PersistenceConfiguration configuration=null;
     private PersistenceSessionFactoryImpl factory=null;
     private ReplicaService replicaService =  null;
     private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean  called= false;
 
 
     public void init()
     {
         if(replicaService == null )
         {
             try
             {
                 configuration = new PersistenceConfiguration(getNodeName());
                 replicaService =   this.getPortByName("service", ReplicaService.class);
                 ReplicaStore store = new ReplicaStore(replicaService);
                 configuration.setStore(store);
                 configuration.addPersistentClass(Demand.class);
 
                 for (Class c : SitacFactory.classes()) configuration.addPersistentClass(c);
 
                 factory = configuration.getPersistenceSessionFactory();
 
 
             } catch (PersistenceException e)
             {
                 logger.error("init : ",e);
             }
         }
     }
 
 
     @Stop
     public void stop()
     {
 
 
     }
 
 
     @Update
     public void update()
     {
 
 
     }
     @Start
     public void start()
     {
         getModelService().registerModelListener(new ModelListener() {
             @Override
             public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                 return true;
             }
 
             @Override
             public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                 return true;
             }
 
             @Override
             public void modelUpdated() {
                 init();
 
                if(called == false)
                {
                 PersistenceSession session = null;
                 try
                 {
                     session = factory.getSession();
 
                     if(session != null)
                     {
 
                         SitacModel sitacModel = SitacFactory.createSitacModel();
 
 
                         Agent agentnoel =SitacFactory.createAgent();
                         agentnoel.setNom("PLOUZEAU");
                         agentnoel.setPrenom("NOEL");
                         agentnoel.setMatricule("nplouzeau");
                         agentnoel.setPassword("nplouzeau");
                         sitacModel.addPersonnes(agentnoel);
 
                         Agent agentERWAN =  SitacFactory.createAgent();
                         agentERWAN.setNom("DAUBERT");
                         agentERWAN.setPrenom("Erwan");
                         agentERWAN.setMatricule("edaubert");
                         agentERWAN.setPassword("edaubert");
                         sitacModel.addPersonnes(agentERWAN);
 
                         Agent agentMaxime = SitacFactory.createAgent();
                         agentMaxime.setNom("TRICOIRE");
                         agentMaxime.setPrenom("Maxime");
                         agentMaxime.setMatricule("mtricoire");
                         agentMaxime.setPassword("mtricoire");
                         sitacModel.addPersonnes(agentMaxime);
 
                         Agent agentjed = SitacFactory.createAgent();
                         agentjed.setNom("DARTOIS");
                         agentjed.setPrenom("JEAN-EMILE");
                         agentjed.setMatricule("jedartois");
                         agentjed.setPassword("jedartois");
                         sitacModel.addPersonnes(agentjed);
 
                         Personne requerant = SitacFactory.createPersonne();
                         requerant.setNom("Nom requerant");
                         requerant.setPrenom("Prenom requerant");
 
                         Personne vitc1 = SitacFactory.createPersonne();
                         vitc1.setNom("Nom victime1");
                         vitc1.setPrenom("Prenom victime1");
 
                         Personne vitc2 = SitacFactory.createPersonne();
                         vitc2.setNom("Nom victime2");
                         vitc2.setPrenom("Prenom victime2");
 
                         Intervention interventionfake =SitacFactory.createIntervention();
 
                         GpsPoint position = SitacFactory.createGpsPoint();
                         position.setLong(4811534);
                         position.setLat(-1638336);
 
 
                         interventionfake.setPosition(new Some((Position)position));
 
                         interventionfake.setRequerant(new Some(requerant));
 
                         interventionfake.addVictimes(vitc1);
                         interventionfake.addVictimes(vitc2);
 
 
 
                         Moyen   moyen1 = SitacFactory.createMoyen();
                         moyen1.setChef(new Some((agentnoel)));
                         moyen1.addPersonnels(agentERWAN);
                         moyen1.addPersonnels(agentjed);
                         moyen1.addPersonnels(agentMaxime);
 
 
                         Vehicule fpt =SitacFactory.createVehicule();
                         fpt.setVehiculeType(VehiculeType.FPT);
                         moyen1.addMateriel(fpt);
 
                         Vehicule fpt2 =SitacFactory.createVehicule();
                         fpt2.setVehiculeType(VehiculeType.FPT);
                         moyen1.addMateriel(fpt2);
 
 
 
                         Detachement detachement = SitacFactory.createDetachement();
 
                         Affectation affectation = SitacFactory.createAffectation();
                         affectation.setMoyen(new Some(moyen1));
                         detachement.addAffectation(affectation);
 
 
                         interventionfake.addDetachements(detachement);
                         sitacModel.addInterventions(interventionfake);
 
                         session.save(sitacModel);
 
 
                         logger.warn(""+session.getAll(InterventionImpl.class).size());
 
 
 
 
                     }
                 } catch (PersistenceException ex)
                 {
                     logger.error("",ex);
                     replicaService = null;
                 }
                 finally
                 {
                     if (session != null) session.close();
 
                 }
                    called = true;
            }
             }
         });



     }
 }
