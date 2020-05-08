 package br.org.aappe.erp.dao;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import br.com.caelum.vraptor.ioc.Component;
 
 import br.org.aappe.erp.bean.Funcionario;
 import br.org.aappe.erp.enums.Role;
 import br.org.aappe.erp.enums.Status;
 import br.org.aappe.erp.repository.FuncionarioRepository;
 import br.org.aappe.erp.util.Utilities;
 
 /**
  * @author Phelipe Melanias
  */
 @Component
 public class FuncionarioDAO extends DAO<Funcionario> implements FuncionarioRepository {
 
     public FuncionarioDAO(EntityManager manager) {
         super(manager);
     }
 
     @Override
     public Funcionario authenticate(String login, String senha) {
         try {
             return (Funcionario) manager.createQuery("FROM Funcionario f WHERE f.login = ? AND f.senha = ? AND status = ? AND perfil <> ? AND demissao IS NULL")
                                         .setParameter(1, login)
                                        .setParameter(2, Utilities.md5(senha))
                                         .setParameter(3, Status.ATIVO)
                                         .setParameter(4, Role.REPRESENTANTE)
                                         .getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     @Override
     public boolean hasEmployee() {
         return !super.listAllById().isEmpty();
     }
 
     @Override
     public boolean isUniqueRg(Funcionario funcionario) {
         return funcionario.getId() > 0 ? manager.createQuery("SELECT f FROM Funcionario f WHERE f.id <> ? AND f.rg = ?")
                                                 .setParameter(1, funcionario.getId())
                                                 .setParameter(2, funcionario.getRg())
                                                 .getResultList().isEmpty()
                                        : manager.createQuery("SELECT f FROM Funcionario f WHERE f.rg = ?")
                                                 .setParameter(1, funcionario.getRg())
                                                 .getResultList().isEmpty();
     }
 
     @Override
     public boolean isUniqueCpf(Funcionario funcionario) {
         return funcionario.getId() > 0 ? manager.createQuery("SELECT f FROM Funcionario f WHERE f.id <> ? AND f.cpf = ?")
                                                 .setParameter(1, funcionario.getId())
                                                 .setParameter(2, funcionario.getCpf())
                                                 .getResultList().isEmpty()
                                        : manager.createQuery("SELECT f FROM Funcionario f WHERE f.cpf = ?")
                                                 .setParameter(1, funcionario.getCpf())
                                                 .getResultList().isEmpty();
     }
 
     @Override
     public boolean isUniqueName(Funcionario funcionario) {
         return funcionario.getId() > 0 ? manager.createQuery("SELECT f FROM Funcionario f WHERE f.id <> ? AND lower(f.nome) = ?")
                                                 .setParameter(1, funcionario.getId())
                                                 .setParameter(2, funcionario.getNome().toLowerCase())
                                                 .getResultList().isEmpty()
                                        : manager.createQuery("SELECT f FROM Funcionario f WHERE lower(f.nome) = ?")
                                                 .setParameter(1, funcionario.getNome().toLowerCase())
                                                 .getResultList().isEmpty();
     }
 
     @Override
     public boolean isUniqueMail(Funcionario funcionario) {
         return funcionario.getId() > 0 ? manager.createQuery("SELECT f FROM Funcionario f WHERE f.id <> ? AND f.email = ?")
                                                 .setParameter(1, funcionario.getId())
                                                 .setParameter(2, funcionario.getEmail())
                                                 .getResultList().isEmpty()
                                        : manager.createQuery("SELECT f FROM Funcionario f WHERE f.email = ?")
                                                 .setParameter(1, funcionario.getEmail())
                                                 .getResultList().isEmpty();
     }
 
     @Override
     public boolean isUniqueLogin(Funcionario funcionario) {
         return funcionario.getId() > 0 ? manager.createQuery("SELECT f FROM Funcionario f WHERE f.id <> ? AND lower(f.login) = ?")
                                          .setParameter(1, funcionario.getId())
                                          .setParameter(2, funcionario.getLogin().toLowerCase())
                                          .getResultList().isEmpty()
                                 : manager.createQuery("SELECT f FROM Funcionario f WHERE lower(f.login) = ?")
                                          .setParameter(1, funcionario.getLogin().toLowerCase())
                                          .getResultList().isEmpty();
     }
 }
