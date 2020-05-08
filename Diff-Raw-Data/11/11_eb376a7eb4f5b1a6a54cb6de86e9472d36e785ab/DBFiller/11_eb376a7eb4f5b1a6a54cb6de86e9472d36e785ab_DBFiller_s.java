 package javahive.infrastruktura;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.fixy.Fixy;
 import com.fixy.JpaFixyBuilder;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 
 @Component
 public class DBFiller implements ApplicationContextAware{
 
 	//@PersistenceContext
 	//private EntityManager entityManager;
 
 	@Inject
     Finder finder;
 
 	@PostConstruct
 	public void fill(){
 
 		EntityManager entityManager = applicationContext.getBean(EntityManagerFactory.class).createEntityManager();
 		EntityTransaction t = entityManager.getTransaction();
 		t.begin();
		Fixy fixtures = new JpaFixyBuilder(entityManager).withDefaultPackage("javafxapplication2").useFieldAccess().build();
 		//fixtures.load("javafxapplication2/Studenci.yaml","javafxapplication2/Przedmioty.yaml","javafxapplication2/Oceny.yaml");
 		//fixtures.load("javafxapplication2/Studenci.yaml","pet_types.yaml","pets.yaml");
 		//fixtures.load("javafxapplication2/Studenci.yaml","javafxapplication2/Przedmioty.yaml","javafxapplication2/Oceny.yaml","pet_types.yaml","pets.yaml");
		fixtures.load("javafxapplication2/Studenci.yaml");
		fixtures.load("javafxapplication2/Przedmioty.yaml");
		fixtures.load("javafxapplication2/Oceny.yaml");
        fixtures.load("javafxapplication2/Indeksy.yaml");
 		//System.out.printf("załadowano %d studentów\n",finder.findAll(Student.class).size());
 		//System.out.printf("załadowano %d ocen\n",finder.findAll(Ocena.class).size());
 		//for(Student s:finder.findAll(Student.class)){
 			//System.out.printf("student %s ma %d ocen \n",s.getImie(),s.getOceny().size());
 		//}
 		//for(Ocena o:finder.findAll(Ocena.class)){
 			//System.out.printf("ocena %s należy do studenta %s \n",o.getWysokosc(),o.getStudent().getImie());
 		//}		
 
 		t.commit();
 		entityManager.close();
 		System.out.println("----------------------------------------------------------------------");
 	}
 
 
 
 	private ApplicationContext applicationContext;
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		this.applicationContext=applicationContext;
 	}
 }
