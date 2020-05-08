/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package br.ufrn.cerescaico.bsi.sigest.bo;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import br.ufrn.cerescaico.bsi.sigest.dao.CursoJpaController;
 import br.ufrn.cerescaico.bsi.sigest.dao.exceptions.PreexistingEntityException;
 import br.ufrn.cerescaico.bsi.sigest.dao.util.JPAUtil;
 import br.ufrn.cerescaico.bsi.sigest.model.Curso;
 
 /**
  * Classe que representa a Entidade de Négocio para Curso.
  * @author taciano
  */
 public class CursoBO extends AbstractBO {
     
     /**
      * Logger.
      */
 	private static final Logger logger = Logger.getLogger(CursoBO.class.getName());
     
     private CursoJpaController dao;
 
     public CursoBO() {
         this.dao = new CursoJpaController(JPAUtil.EMF);
     }
     
     public Curso inserir(Curso curso) throws NegocioException {
         try {
             return dao.create(curso);
         }
         catch (PreexistingEntityException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
             throw new NegocioException("erro.curso.bo.inserir.PreexistingEntityException",ex);
         }
         catch (Exception ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
             throw new NegocioException("erro.curso.bo.inserir.exception",ex);
         }
     }
     
     public List<Curso> listar() throws NegocioException {
     	try {
             return dao.findCursoEntities();
         }
         catch (Exception ex) {
         	logger.log(Level.SEVERE, ex.getMessage(), ex);
             throw new NegocioException("erro.curso.bo.listar", ex);
         }
     }
 }
