 package svm.logic.abstraction.controller;
 
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.ITransferDepartment;
 import svm.logic.abstraction.transferobjects.ITransferLocation;
 import svm.logic.abstraction.transferobjects.ITransferMember;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Projectteam: Team C
  * Date: 30.10.12
  */
 public interface ISearchController extends IController {
 
     List<ITransferMember> getMembers(String firstName, String lastName, ITransferDepartment department) throws NoSessionFoundException, IllegalGetInstanceException;
 
    List<ITransferMember> getMembers(String firstName, String lastName) throws NoSessionFoundException, IllegalGetInstanceException;

     List<ITransferMember> getMembers(Date birthDateFrom, Date birthDateTo) throws NoSessionFoundException, IllegalGetInstanceException;
 
     List<ITransferDepartment> getDepartments() throws IllegalGetInstanceException, NoSessionFoundException;
 
     List<ITransferLocation> getLocations() throws IllegalGetInstanceException, NoSessionFoundException;
 }
