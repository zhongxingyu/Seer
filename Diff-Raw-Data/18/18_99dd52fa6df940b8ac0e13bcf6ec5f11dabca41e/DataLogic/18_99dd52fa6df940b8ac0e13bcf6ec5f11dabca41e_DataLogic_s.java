 package funktionalitet;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import connector.Connector;
 import daoimpl.MySQLDeveloperDAO;
 import daoimpl.MySQLGenreDAO;
 import daoimpl.MySQLLangDAO;
 import daoimpl.MySQLPublisherDAO;
 import daointerfaces.DALException;
 import daointerfaces.DeveloperIDAO;
 import daointerfaces.GenreIDAO;
 import daointerfaces.LangIDAO;
 import daointerfaces.PublisherIDAO;
 import dto.DeveloperDTO;
 import dto.GenreDTO;
 import dto.LangDTO;
 import dto.PublisherDTO;
 
 public class DataLogic implements IDataLogic {
 
 	private GenreIDAO gen;
 	private LangIDAO lang;
 	private DeveloperIDAO dev;
 	private PublisherIDAO pub;
 
 	public DataLogic() throws DALException {
 		try {
 			new Connector();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		gen = new MySQLGenreDAO();
 		lang = new MySQLLangDAO();
 		dev = new MySQLDeveloperDAO();
 		pub = new MySQLPublisherDAO();
 	}
 	public void createGenre(String genre) throws DALException {
 		try{
 			gen.getByGenre(genre);
 		}catch(DALException e){
 			GenreDTO row = new GenreDTO(0,genre);
 			gen.create(row);
 		}
 		throw new DALException("Genre already exists!");
 	}
 	public List<GenreDTO> getListGenre() throws DALException {
 		List<GenreDTO> genList = new ArrayList<GenreDTO>();
 		genList = gen.getList();
 		return genList;
 	}
 	public void createLang(String language) throws DALException {
 		try{
 			lang.getByLang(language);
 		} catch (DALException e){
 			LangDTO row = new LangDTO(0,language);
 			lang.create(row);
 		}
 		throw new DALException("Language already exists!");
 	}
 	public List<LangDTO> getListLang() throws DALException {
 		List<LangDTO> list = new ArrayList<LangDTO>();
 		list = lang.getList();
 		return list;
 	}
 	public void createDev(String deve, String con) throws DALException {
 		try{
 			dev.getByDev(deve);
 		}catch (DALException e){
 			DeveloperDTO row = new DeveloperDTO(0,deve,con);
 			dev.create(row);
 		}
 		throw new DALException("Developer already exists!");
 	}
 	public List<DeveloperDTO> getListDev() throws DALException {
 		List<DeveloperDTO> list = new ArrayList<DeveloperDTO>();
 		list = dev.getList();
 		return list;
 	}
 	public void createPub(String pube, String con) throws DALException {
 		try{
 			pub.getByPub(pube);
 		}catch (DALException e) {
		PublisherDTO row = new PublisherDTO(0,pube,con);
		pub.create(row);
 		}
 		throw new DALException("Publisher already exists!");
 	}
 	public List<PublisherDTO> getListPub() throws DALException {
 		List<PublisherDTO> list = new ArrayList<PublisherDTO>();
 		list = pub.getList();
 		return list;
 	}
 }
