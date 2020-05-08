 package org.mdissjava.mdisscore.model.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mdissjava.mdisscore.model.dao.impl.UserDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.Address;
 import org.mdissjava.mdisscore.model.pojo.Configuration;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.mdissjava.mdisscore.model.pojo.User.Gender;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class UserDaoImplTest {
 
 	final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 	
 	@Test
 	public void addUserTest(){		
 		this.logger.info("TEST(UserDao) addUser");
 	
 		Address address = new Address();		
 		address.setStreet("Madariaga 6");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("Chesua");
 		user.setActive(true);
 		user.setName("Jessica2");		
 		user.setSurname("Smith2");
 		user.setPhone(944655874);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("nature");
 		user.addPreference("horses");
 		user.addPreference("sunsets");
 		user.setEmail("prueba2@prueba2.com");
 		user.setPass("prueba2");
 
 		UserDao dao = new UserDaoImpl();
 		
 		dao.addUser(user);				
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		
 		dao.deleteUser(user);
 		
 	}
 	
 	@Test
 	public void ReplicationTest() throws Exception{
 		
 		Address address = new Address();		
 		address.setStreet("Madariaga 6");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("Cheseal");
 		user.setActive(true);
 		user.setName("Jessica2");		
 		user.setSurname("Smith2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("nature");
 		user.addPreference("horses");
 		user.addPreference("sunsets");
 		user.setEmail("prueba2@prueba2.com");
 		user.setPass("prueba2");
 
 		UserDao dao = new UserDaoImpl();
 		
 		dao.addUser(user);				
 		
 
 		
 		if(dao.emailAllReadyExists("prueba2@prueba2.com"))
 		{}
 		else
 			throw new Exception("email not found exception");
 		
		if(dao.nickAllReadyExists("jess2"))
 		{}
 		else
 			throw new Exception(" nick not found exception");
 	
 		if(dao.nickAllReadyExists("Chekitua"))
 		{throw new Exception(" nick que no existe Exception");}
 
 		dao.deleteUser(user);
 	}
 	
 	@Test
 	public void deleteUserTest(){
 
 		
 		this.logger.info("TEST(UserDao) deleteUser");
 		
 		Address address = new Address();		
 		address.setStreet("Madariaga 6");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("Prueba001");
 		user.setName("Prueba001");		
 		user.setSurname("Smith2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("nature");
 		user.addPreference("horses");
 		user.addPreference("sunsets");
 		user.setEmail("prueba2@prueba2.com");
 		user.setPass("prueba2");
 		
 		UserDao dao = new UserDaoImpl();
 		
 		dao.addUser(user);		
 
 		dao.deleteUser(user);				
 		assertNull(dao.getUserByNick("Prueba2")); 		
 		
 		
 	}
 
 	@Test
 	public void getByIdTest()throws IllegalArgumentException{
 		
 		this.logger.info("TEST(UserDao) getUserByID");
 
 		Address address = new Address();		
 		address.setStreet("Avda Universidades");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("mdissSuanzer2");
 		user.setName("Java2");		
 		user.setSurname("Master2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("java");
 		user.addPreference("programming");
 		user.addPreference("pojos");
 		user.setEmail("prueba2@prueba2.com");
 		user.setPass("9e2c6781e1d498c41d3b146262158a5803f9724067af0d30e7179856ad66c74f");
 		user.setRole("USER");
 		user.setActive(true);
 		
 		UserDao dao = new UserDaoImpl();
 		dao.addUser(user);
 				
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		System.out.println("EMail original:"+user.getEmail());
 		User user2=dao.getUserByNick(user.getNick());
 		if(!user2.getEmail().equals(user.getEmail()))
 			throw new IllegalArgumentException("error");
 		
 		dao.deleteUser(user);
 	}
 
 	@Test
 	public void updateUserTest(){
 		
 		this.logger.info("TEST(UserDao) updateUser");
 		
 		Address address = new Address();		
 		address.setStreet("Madariaga 64");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 		
 		User user = new User();
 		user.setNick("jessAgain4");
 		user.setActive(true);
 		user.setName("JessicaAgain4");		
 		user.setSurname("SmithAgain4");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("nature");
 		user.addPreference("horses");
 		user.addPreference("sunsets");
 		user.setEmail("pruebaUpdate2@prueba2.com");
 		user.setPass("prueba");
 		
 		UserDao dao = new UserDaoImpl();
 		dao.addUser(user);
 		
 		user.setEmail("pruebaUpdateCorrecta2@prueba2.com");
 		
 		dao.updateUser(user);
 		
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		
 		dao.deleteUser(user);
 		
 	}
 	
 	@Test
 	public void updateUserAdressTest(){
 		this.logger.info("TEST(UserDao) updateUserAdress");
 		
 		Address address = new Address();		
 		address.setStreet("Madariaga 6");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("javiGonzo");
 		user.setActive(true);
 		user.setName("Javier2");		
 		user.setSurname("Gonzalez2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Male);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("Jamon");
 		user.addPreference("Cocina");
 		user.addPreference("Paisajes");
 		user.setEmail("Javier2@prueba2.com");
 		user.setPass("javi2");
 		
 		UserDao dao = new UserDaoImpl();
 		dao.addUser(user);				
 		assertEquals(user, dao.getUserByNick(user.getNick()));	
 		user.getAddress().setCity("Tudela");
 		user.getAddress().setCountry("España");
 		user.getAddress().setState("Navarra");
 		
 		user.getConfiguration().setShowName(false);
 		user.getConfiguration().setShowPhone(true);
 		user.getConfiguration().setShowEmail(true);
 		
 		dao.updateUser(user);
 		
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		
 		dao.deleteUser(user);
 		
 		
 	}
 	
 	@Test
 	public void addFriendTest(){
 		this.logger.info("TEST(UserDao) addFriend");
 		
 		Address address = new Address();		
 		address.setStreet("alcobendas 16");		
 		address.setCity("Madrir");
 		address.setState("Madrid");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 										
 		User user = new User();
 		user.setNick("Raulete4");
 		user.setActive(true);
 		user.setName("Raul2");		
 		user.setSurname("Macua2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Male);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("chorizo");
 		user.addPreference("Cocina");
 		user.addPreference("salsa");
 		user.setEmail("Raul2@prueba2.com");
 		user.setPass("raul2");
 		
 		UserDao dao = new UserDaoImpl();
 		dao.addUser(user);				
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		
 		System.out.println("El id del usuario es : ********************"+ user.getId());
 		
 		Address address2 = new Address();		
 		address2.setStreet("vestigios 32");		
 		address2.setCity("Vitoria");
 		address2.setState("Alava");
 		address2.setCountry("Spain");
 		
 		Configuration conf2 = new Configuration();
 										
 		User user2 = new User();
 		user2.setNick("Maria4");
 		user2.setActive(true);
 		user2.setName("Maria2");		
 		user2.setSurname("Subijana2");
 		user2.setPhone(944655877);
 		user2.setBirthdate(new Date());
 		user2.setGender(Gender.Female);
 		user2.setAddress(address2);
 		user2.setConfiguration(conf2);		
 		user2.addPreference("salsa");
 		user2.addPreference("bailes");
 		user2.addPreference("Paisajes");
 		user2.setEmail("Maria2@prueba2.com");
 		user2.setPass("maria2");
 		
 		
 		dao.addUser(user2);				
 		assertEquals(user2, dao.getUserByNick(user2.getNick()));
 		System.out.println("El id del usuario2 es : ********************"+ user2.getId());
 		user.addFriend(user2);
 		
 		dao.updateUser(user);	
 		assertEquals(user, dao.getUserByNick(user.getNick()));
 		
 		dao.deleteUser(user);
 		dao.deleteUser(user2);
 	}
 		
 	/*
 	@Test
 	public void findFriendTest(){
 		
 		//funciona , pero hay que cambiar el 115 , por un id de un usuario con amigos
 		this.logger.info("TEST(UserDao) findFriend");	
 		UserDao dao = new UserDaoImpl();
 		User user=dao.getUserById(115);
 		List<User> listaAmigos=user.getFriends();
 		System.out.print("el usuario con Id:"+ user.getId() +" tiene estos -");
 		for(int i=0;i<listaAmigos.size();i++)
 		{System.out.println("Amigos Id:"+listaAmigos.get(i).getId());}
 	}
 
 	} */
 	
 	@Test
 	public void getByNickTest()throws IllegalArgumentException{
 		
 		this.logger.info("TEST(UserDao) getUserByName");
 		
 		Address address = new Address();		
 		address.setStreet("Avda Universidades");		
 		address.setCity("Bilbao");
 		address.setState("Vizcaya");
 		address.setCountry("Spain");
 		
 		Configuration conf = new Configuration();
 		
 		User user = new User();
 		user.setNick("mdissWorarkarfer2");
 		user.setName("Java2");		
 		user.setSurname("Master2");
 		user.setPhone(944655877);
 		user.setBirthdate(new Date());
 		user.setGender(Gender.Female);
 		user.setAddress(address);
 		user.setConfiguration(conf);		
 		user.addPreference("java");
 		user.addPreference("programming");
 		user.addPreference("pojos");
 		user.setEmail("prueba2@prueba2.com");
 		user.setPass("9e2c6781e1d498c41d3b146262158a5803f9724067af0d30e7179856ad66c74f");
 		user.setRole("USER");
 		user.setActive(true);
 		
 		UserDao dao = new UserDaoImpl();
 		dao.addUser(user);
 		
 				
 		assertEquals("mdissWorarkarfer2",  dao.getUserByNick("mdissWorarkarfer2").getNick());
 		
 		dao.deleteUser(user);
 		
 	}
 
 }
