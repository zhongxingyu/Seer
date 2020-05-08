 package de.niles;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import javax.ejb.EJB;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.List;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
 
 @RunWith(Arquillian.class)
 public class MicroPostModelTest {
 
     private MicroPost initialPost;
 
     @Deployment
     public static Archive<?> createDeployment() {
         return ShrinkWrap.create(JavaArchive.class, "test.jar")
                 .addClasses(MicroPostRepository.class, MicroPost.class, MicroPostModel.class)
                 .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                 .addAsManifestResource("META-INF/persistence.xml", "persistence.xml");
     }
 
     @EJB
     MicroPostRepository repository;
 
     @Inject
     MicroPostModel model;
 
     @Inject
     @Named("newPost")
     MicroPost newMicroPost;
 
     @Inject
     List<MicroPost> posts;
 
     @Test
     public void addNew() {
         newMicroPost.setAuthor("Hans");
         newMicroPost.setContent("Ich brauche wurst!!!");
         model.save();
         assertThat(repository.findAll(), hasItem(newMicroPost));
     }
 
     @Test
     public void getPosts() {
        newMicroPost.setAuthor("Hans");
        newMicroPost.setContent("Ich brauche wurst!!!");
        model.save();
        assertThat(model.getPosts(), hasItem(newMicroPost));
     }
 
 }
