 package se.atrosys.birds.main;
 
 import org.hibernate.cfg.Configuration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.hibernate3.HibernateTransactionManager;
 import org.springframework.stereotype.Component;
 import se.atrosys.birds.exception.CouldNotFindDetailsException;
 import se.atrosys.birds.exception.CouldNotFindNamesElementException;
 import se.atrosys.birds.exception.NoFamilyException;
 import se.atrosys.birds.exception.NoSuchLanguageException;
 import se.atrosys.birds.factory.BirdModelListFactory;
 import se.atrosys.birds.model.BirdModel;
 import se.atrosys.birds.service.BirdService;
 import se.atrosys.birds.service.SoundService;
 
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 12/11/11
  * Time: 8:00 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class DbMain {
 	@Autowired BirdModelListFactory birdModelListFactory;
 	@Autowired BirdService birdService;
 	@Autowired private SoundService soundService;
 
 	public void importIntoDb() throws CouldNotFindNamesElementException, JAXBException, IOException, NoFamilyException, NoSuchLanguageException, CouldNotFindDetailsException {
//		List<BirdModel> birdModels = birdModelListFactory.scrapeFromAviBase("/home/ola/code/birds/avibase-short.html");
 		List<BirdModel> birdModels = birdModelListFactory.scrapeFromAviBase("/home/ola/code/birds/avibase.html");
 		birdService.saveAll(birdModels);
 	}
 
 	public void enrichBirdsWithSounds() {
 		List<BirdModel> list = birdService.findAll();
 		soundService.enrichAll(list);
 	}
 
 	public static void main(String[] args) throws CouldNotFindNamesElementException, JAXBException, IOException, NoSuchLanguageException, NoFamilyException, CouldNotFindDetailsException {
 //		final ApplicationContext ac =  new ClassPathXmlApplicationContext(new String[] {"classpath:/META-INF/spring/context.xml"});
 		final ClassPathXmlApplicationContext ac =  new ClassPathXmlApplicationContext(new String[] {"classpath:/META-INF/spring/context.xml"});
 		Configuration configuration = new Configuration();
 
 /*		configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
 		configuration.setProperty("hibernate.hbm2ddl.auto", "create");
 		configuration.setProperty("dataSource", "birdDbDS");
 		
 		ac.getBean("transactionManager", HibernateTransactionManager.class).setSessionFactory(configuration.buildSessionFactory());*/
 		final DbMain launcher = ac.getBean("dbMain", DbMain.class);
 		launcher.importIntoDb();
 	}
 }
