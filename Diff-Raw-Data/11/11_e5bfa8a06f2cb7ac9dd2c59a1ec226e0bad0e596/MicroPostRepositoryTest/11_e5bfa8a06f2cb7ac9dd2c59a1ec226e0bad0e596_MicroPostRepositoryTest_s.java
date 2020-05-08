 package de.niles;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import javax.ejb.EJB;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(Arquillian.class)
 public class MicroPostRepositoryTest {
     @Deployment
     public static Archive<?> createDeployment() {
         return ShrinkWrap.create(JavaArchive.class)
                 .addClasses(MicroPostRepository.class, MicroPost.class)
                 .addAsManifestResource("META-INF/persistence.xml", "persistence.xml");
     }
 
     @EJB
     MicroPostRepository microPostRepository;
 
     @Before
     public void bla() {
        System.out.println("blubb");
     }
 
     @Test
     public void addAndFindAll() {
        microPostRepository.clear();
        microPostRepository.add(new MicroPost("Niels", "Ich habe Hunger!"));
         assertThat(microPostRepository.findAll().size(), is(1));
     }
 
     @Test
     public void clear() {
        microPostRepository.add(new MicroPost("Niels", "Ich habe Hunger!"));
         microPostRepository.clear();
        assertThat(microPostRepository.findAll().size(), is(1));
     }
 
 }
