 package svm.rmi.implementation.rmiController;
 
 import svm.domain.abstraction.exception.DomainAttributeException;
 import svm.domain.abstraction.exception.DomainParameterCheckException;
 import svm.logic.abstraction.controller.IMemberController;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.ITransferLocation;
 import svm.logic.abstraction.transferobjects.ITransferMember;
 import svm.persistence.abstraction.exceptions.ExistingTransactionException;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 import svm.persistence.abstraction.exceptions.NoTransactionException;
 import svm.rmi.abstraction.controller.IRMIMemberController;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Date;
 
 /**
  * Projectteam : Team C
  * Date: 03.11.12
  */
 public class RMIMemberController extends UnicastRemoteObject implements IRMIMemberController {
 
     IMemberController controller;
 
     public RMIMemberController(IMemberController memberController) throws RemoteException {
         super();
         this.controller = memberController;
     }
 
     @Override
    public ITransferMember getMember() {
         return controller.getMember();
     }
 
     @Override
     public void setTitle(String s) throws DomainAttributeException, RemoteException {
         controller.setTitle(s);
     }
 
     @Override
     public void setFirstName(String s) throws DomainAttributeException, RemoteException {
         controller.setFirstName(s);
     }
 
     @Override
     public void setLastName(String s) throws DomainAttributeException, RemoteException {
         controller.setLastName(s);
     }
 
     @Override
     public void setSocialNumber(String s) throws DomainAttributeException, RemoteException {
         controller.setSocialNumber(s);
     }
 
     @Override
     public void setBirthDate(Date date) throws DomainParameterCheckException, RemoteException {
         controller.setBirthDate(date);
     }
 
     @Override
     public void setGender(String s) throws DomainParameterCheckException, DomainAttributeException, RemoteException {
         controller.setGender(s);
     }
 
     @Override
     public void setEntryDate(Date date) throws DomainParameterCheckException, RemoteException {
         controller.setEntryDate(date);
     }
 
     @Override
     public void setPhone1(String s) throws DomainAttributeException, RemoteException {
         controller.setPhone1(s);
     }
 
     @Override
     public void setPhone2(String s) throws DomainAttributeException, RemoteException {
         controller.setPhone2(s);
     }
 
     @Override
     public void setEmail1(String s) throws DomainAttributeException, RemoteException {
         controller.setEmail1(s);
     }
 
     @Override
     public void setEmail2(String s) throws DomainAttributeException, RemoteException {
         controller.setEmail2(s);
     }
 
     @Override
     public void setFax(String s) throws DomainAttributeException, RemoteException {
         controller.setFax(s);
     }
 
     @Override
     public void setStreet(String s) throws DomainAttributeException, RemoteException {
         controller.setStreet(s);
     }
 
     @Override
     public void setStreetNumber(String s) throws DomainAttributeException, RemoteException {
         controller.setStreetNumber(s);
     }
 
     @Override
     public void setLat(String s) throws DomainAttributeException, RemoteException {
         controller.setLat(s);
     }
 
     @Override
     public void setLong(String s) throws DomainAttributeException, RemoteException {
         controller.setLong(s);
     }
 
     @Override
     public void setLocation(ITransferLocation iTransferLocation) throws DomainAttributeException, RemoteException {
         controller.setLocation(iTransferLocation);
     }
 
     @Override
     public void start() throws NoSessionFoundException, IllegalGetInstanceException, RemoteException {
         controller.start();
     }
 
     @Override
     public void commit() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException, RemoteException {
         controller.commit();
     }
 
     @Override
     public void abort() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException, RemoteException {
         controller.abort();
     }
 }
