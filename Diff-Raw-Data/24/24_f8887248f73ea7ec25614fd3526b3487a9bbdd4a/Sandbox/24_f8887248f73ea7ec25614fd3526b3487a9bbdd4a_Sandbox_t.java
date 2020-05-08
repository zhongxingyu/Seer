 package test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.InvocationTargetException;
 
 import persistence.dto.Documento;
 import persistence.dto.UserAccount;
 
 import business.DAO.login.ProfileDAO;
 import business.DAO.login.UserDAO;
 import business.EJB.documents.DocumentEJB;
 import business.exceptions.documents.DocumentNotFoundException;
 import business.exceptions.login.ProfileNotFoundException;
 import business.exceptions.login.UnreachableDataBaseException;
 import business.exceptions.login.UserNotFoundException;
 
 @SuppressWarnings("unused")
 public class Sandbox {
 
 	/**
 	 * @param args
 	 * @throws SecurityException 
 	 * @throws NoSuchMethodException 
 	 * @throws InvocationTargetException 
 	 * @throws IllegalArgumentException 
 	 * @throws IllegalAccessException 
 	 * @throws UnreachableDataBaseException 
 	 * @throws ProfileNotFoundException 
 	 * @throws UserNotFoundException 
 	 * @throws DocumentNotFoundException 
 	 */
 	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, UnreachableDataBaseException, ProfileNotFoundException, UserNotFoundException, DocumentNotFoundException {
 		DocumentEJB e = new DocumentEJB();
 		UserDAO d = new UserDAO();
 		ProfileDAO pd = new ProfileDAO();
 		pd.createProfile("teste");
 		d.addUser("a@b.c.d", "nome", "12345", pd.findProfileByName("teste"));
 		UserAccount a = d.findUserByEmail("a@b.c.d");
 		e.registerNewDocument("título legal", 
 				"APEP",
 				"1234", 
 				"unifesp", 
 				"www.com.br",
 				"Era uma vez um gato chinês", 
 				null, a, 
 				"codCodiceCaixa", 
 				"tituloCodiceCaixa", 
 				"1225", 
 				"1226", 
 				"autor", 
 				"ocupacaoAutor", 
 				"destinatario", 
 				"ocupacaoDestinatario", 
 				"tipoDocumento", 
 				"descricaoDoTipoDocumento", 
				"palavraChave1",
 				"palavraChave2",
				"palavraChave3");
 		Documento doc = (Documento) e.findDocuments(null, null, null, null, null, null, null, null, "autor", null, null, null, null, null, null, null, null, null).get(0);
 		System.out.println(doc.getCod());
 		
 	}
 
 }
