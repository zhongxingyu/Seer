 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.porcupine.psp.model.service;
 
 import com.porcupine.psp.model.dao.DAOFactory;
 import com.porcupine.psp.model.dao.exceptions.InsufficientPermissionsException;
 import com.porcupine.psp.model.dao.exceptions.InvalidAttributeException;
 import com.porcupine.psp.model.dao.exceptions.NonexistentEntityException;
 import com.porcupine.psp.model.dao.exceptions.PreexistingEntityException;
 import com.porcupine.psp.model.dao.exceptions.RequiredAttributeException;
 import com.porcupine.psp.model.entity.Cliente;
 import com.porcupine.psp.model.entity.Contrato;
 import com.porcupine.psp.model.entity.DirComercial;
 import com.porcupine.psp.model.vo.ContratoVO;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityNotFoundException;
 
 /**
  *
  * @author Feanor
  */
 public class ContratoService implements IService<ContratoVO, Short> {
     
     private static ContratoService instance;
     
     public static synchronized ContratoService getInstance() {
         if (instance == null) {
             instance = new ContratoService();
         }
         return instance;
     }
 
     @Override
     public void create(ContratoVO vo) throws PreexistingEntityException, NonexistentEntityException, RequiredAttributeException, InvalidAttributeException, InsufficientPermissionsException {
         Contrato entity = new Contrato();
         
         entity.setCantidadPersonalC(vo.getCantPersonalCont());
         entity.setCelularC(vo.getCelularCont());
         entity.setCostoMensualC(vo.getCostoMensual());
         entity.setFechaInicioC(vo.getFechaInicioCont());
         entity.setFechaRegCon(vo.getFechaRegCont());
         entity.setHorarioC(vo.getHorarioCont());
         entity.setIdContrato(vo.getIdContrato());
         entity.setTelefonoC(vo.getTelefonoCont());
         entity.setTiempoC(vo.getTiempoCont());
         entity.setTipoC(vo.getTipoCont());
         entity.setTipoPersonalC(vo.getTipoPersonalCont());
         entity.setUbicacionC(vo.getUbicacionCont());
         
         if(vo.getIdCliente() != 0){
            Cliente cliente = DAOFactory.getInstance().getClienteDAO().find(vo.getIdCliente());
            entity.setIdcl(cliente);
            cliente.getContratoList().add(entity);
         }
         
         if(vo.getCedulaDirComer() != 0){
             DirComercial dirComercial = DAOFactory.getInstance().getDirComercialDAO().find(vo.getCedulaDirComer());
             entity.setCedulae(dirComercial);
             
             //Despues de hacer debug en este metodo sale la excepcion
            dirComercial.getContratoList().add(entity);
             System.out.println("prueba askldnalsfnaiodfh");
         }
         
         DAOFactory.getInstance().getContratoDAO().create(entity);
     }
 
     @Override
     public ContratoVO find(Short id) throws EntityNotFoundException, InsufficientPermissionsException {
         Contrato contrato = DAOFactory.getInstance().getContratoDAO().find(id);
         if (contrato != null) {
             return contrato.toVO();
         } else {
             return null;
         }
     }
 
     @Override
     public void update(ContratoVO vo) throws NonexistentEntityException, RequiredAttributeException, InvalidAttributeException, InsufficientPermissionsException {
         Contrato entity = new Contrato();
         
         entity.setCantidadPersonalC(vo.getCantPersonalCont());
         entity.setCelularC(vo.getCelularCont());
         entity.setCostoMensualC(vo.getCostoMensual());
         entity.setFechaInicioC(vo.getFechaInicioCont());
         entity.setFechaRegCon(vo.getFechaRegCont());
         entity.setHorarioC(vo.getHorarioCont());
         entity.setIdContrato(vo.getIdContrato());
         entity.setTelefonoC(vo.getTelefonoCont());
         entity.setTiempoC(vo.getTiempoCont());
         entity.setTipoC(vo.getTipoCont());
         entity.setTipoPersonalC(vo.getTipoPersonalCont());
         entity.setUbicacionC(vo.getUbicacionCont());
         
         if(vo.getIdCliente() != 0){
            Cliente cliente = DAOFactory.getInstance().getClienteDAO().find((Short) vo.getIdCliente());
            entity.setIdcl(cliente);
            cliente.getContratoList().set(cliente.getContratoList().get(entity.getIdContrato()).getIdContrato(), entity);
         }
         
         if(vo.getCedulaDirComer() != 0){
             DirComercial dirComercial = DAOFactory.getInstance().getDirComercialDAO().find(vo.getCedulaDirComer());
             entity.setCedulae(dirComercial);
             dirComercial.getContratoList().set(dirComercial.getContratoList().get(entity.getIdContrato()).getIdContrato(), entity);
         }
         
         DAOFactory.getInstance().getContratoDAO().update(entity);
     }
 
     @Override
     public void delete(Short id) throws NonexistentEntityException, InsufficientPermissionsException {
         Contrato contrato = DAOFactory.getInstance().getContratoDAO().find(id);
         if (contrato != null) {
             DAOFactory.getInstance().getContratoDAO().delete(id);
         }
     }
 
     @Override
     public List<ContratoVO> getList() {
         List<ContratoVO> listaVO = new ArrayList<ContratoVO>();
         List<Contrato> lista = DAOFactory.getInstance().getContratoDAO().getList();;
         for (Contrato contrato : lista) {
             ContratoVO contr = contrato.toVO();
             listaVO.add(contr);
         }
         return listaVO;
     }
 
     @Override
     public void removeAll() throws NonexistentEntityException {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
