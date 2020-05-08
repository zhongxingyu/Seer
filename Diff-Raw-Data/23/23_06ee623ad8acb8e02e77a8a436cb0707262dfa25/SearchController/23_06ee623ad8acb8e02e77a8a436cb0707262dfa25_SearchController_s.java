 package svm.logic.implementation.controller;
 
 import svm.domain.abstraction.DomainFacade;
 import svm.domain.abstraction.modelInterfaces.IDepartment;
 import svm.domain.abstraction.modelInterfaces.ILocation;
 import svm.domain.abstraction.modelInterfaces.IMember;
 import svm.logic.abstraction.controller.ISearchController;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.IHasModel;
 import svm.logic.abstraction.transferobjects.ITransferDepartment;
 import svm.logic.abstraction.transferobjects.ITransferLocation;
 import svm.logic.abstraction.transferobjects.ITransferMember;
 import svm.logic.implementation.tranferobjects.TransferDepartment;
 import svm.logic.implementation.tranferobjects.TransferLocation;
 import svm.logic.implementation.tranferobjects.TransferMember;
 import svm.logic.implementation.transferobjectcreator.TransferObjectCreator;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Projectteam: Team C
  * Date: 30.10.12
  */
 public class SearchController implements ISearchController {
 
     private Integer sessionId;
 
     @Override
     public void start() {
         this.sessionId = DomainFacade.generateSessionId();
     }
 
     @Override
     public void commit() throws NoSessionFoundException {
         DomainFacade.closeSession(sessionId);
     }
 
     @Override
     public void abort() throws NoSessionFoundException {
         DomainFacade.closeSession(sessionId);
     }
 
     /**
      * @param firstName
      * @param lastName
      * @param department
      * @return
      * @throws NoSessionFoundException
      * @throws IllegalGetInstanceException
      */
     @Override
     public List<ITransferMember> getMembers(String firstName, String lastName, ITransferDepartment department) throws NoSessionFoundException, IllegalGetInstanceException {
         List<ITransferMember> result = new LinkedList<ITransferMember>();
         for (IMember member : DomainFacade.getMemberModelDAO().get(sessionId, firstName, lastName, ((IHasModel<IDepartment>) department).getModel())) {
             result.add((ITransferMember) TransferObjectCreator.getInstance(TransferMember.class, member));
         }
         return result;
     }
 
     /**
      * @param birthDateFrom
      * @param birthDateTo
      * @return
      * @throws NoSessionFoundException
      * @throws IllegalGetInstanceException
      */
     @Override
     public List<ITransferMember> getMembers(Date birthDateFrom, Date birthDateTo) throws NoSessionFoundException, IllegalGetInstanceException {
         List<ITransferMember> result = new LinkedList<ITransferMember>();
         for (IMember member : DomainFacade.getMemberModelDAO().get(sessionId, birthDateFrom, birthDateTo)) {
             result.add((ITransferMember) TransferObjectCreator.getInstance(TransferMember.class, member));
         }
         return result;
     }
 
     /**
      * @return
      * @throws IllegalGetInstanceException
      */
     @Override
     public List<ITransferDepartment> getDepartments() throws IllegalGetInstanceException, NoSessionFoundException {
         List<ITransferDepartment> result = new LinkedList<ITransferDepartment>();
         for (IDepartment department : DomainFacade.getDepartmentModelDAO().getAll(sessionId)) {
             result.add((ITransferDepartment) TransferObjectCreator.getInstance(TransferDepartment.class, department));
         }
         return result;
     }
 
     /**
      * @return
      * @throws IllegalGetInstanceException
      */
     @Override
     public List<ITransferLocation> getLocations() throws IllegalGetInstanceException, NoSessionFoundException {
         List<ITransferLocation> result = new LinkedList<ITransferLocation>();
 
         for (ILocation location : DomainFacade.getLocationModelDAO().getAll(sessionId)) {
             result.add((ITransferLocation) TransferObjectCreator.getInstance(TransferLocation.class, location));
         }
         return result;
     }
 }
