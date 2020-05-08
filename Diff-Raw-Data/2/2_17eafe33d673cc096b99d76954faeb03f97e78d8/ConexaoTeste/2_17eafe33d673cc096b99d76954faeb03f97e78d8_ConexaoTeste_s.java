 import org.hibernate.cfg.Configuration;
 import Domain.Repository.IApontamentoRepository;
 import Domain.Models.Apontamento;
 import org.hibernate.Session;
 import Infrastructure.Repository.ApontamentoRepository;
 import Domain.Repository.IBaseRepository;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Dyego
  */
 public class ConexaoTeste {
     
     private Session session;
     
     public ConexaoTeste() {
     }
 
     @Before
     public void setUp() {
         
        session = new Configuration().configure().buildSessionFactory().openSession();
     }
     
     @Test
     public void hello() {
         // Arrange
         IApontamentoRepository apontamentoRepository = new ApontamentoRepository(session);        
         int id = 1;
         
         // Act
         Apontamento apontamento = apontamentoRepository.obterPorId(id);
         
         // Assert
         assertNotNull(apontamento);
     }
 }
