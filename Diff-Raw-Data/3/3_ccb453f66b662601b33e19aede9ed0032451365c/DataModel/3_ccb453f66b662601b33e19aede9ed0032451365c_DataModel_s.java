 package model;
 
 import java.util.ArrayList;
 import java.util.List;
 import model.entities.Applicant;
 import model.entities.Company;
 import model.entities.Employment;
 import model.entities.Skill;
 import model.entities.User;
 import model.entities.UserType;
 import model.petition.Entity;
 import model.petition.Petition;
 import model.petition.PetitionParam;
 import model.response.Response;
 import model.response.ResponseObject;
 import model.response.Status;
 
 public class DataModel 
 {
     private static DataModel dataModel;
     
     private DataModel()
     {
     }
     public static DataModel getInstance()
     {
         if(dataModel == null)
             dataModel = new DataModel();
         
         return dataModel;
     }
     
     
     public Response execute(Petition petition)
     {
         Response response = new Response(petition);
         
         switch(petition.getFunction())
         {
             case login:
                 
                 if(petition.getEntity()==Entity.user)
                 {
                     User user = this.loginUser(petition);
                     response.set(ResponseObject.user, user);
                     if(user==null)
                         response.setStatus(Status.InvalidCredentials);
                    
                    if(user.getUserType()==UserType.admin)
                         response.set(ResponseObject.users, DummyObjects.getRegisteredUsers());
                 }
                 
                 break;
             case add:
                 
                 if(petition.getEntity()==Entity.user)
                     response.set(ResponseObject.user, addUser(petition));
                 
                 break;
             case apply:
                 break;
             case delete:
                 
                 if(petition.getEntity()==Entity.user)
                     response.set(ResponseObject.user, deleteUser(petition));
                 
                 break;
             case modify:
                 
                 if(petition.getEntity()==Entity.applicant)
                     response.set(ResponseObject.user, modifyApplicant(petition));
                 if(petition.getEntity()==Entity.company)
                     response.set(ResponseObject.user, modifyCompany(petition));
                 
                 break;
             case logout:
                 
                 if(petition.getEntity()==Entity.user)
                     response.set(ResponseObject.user, logoutUser(petition));
                 
                 break;
             case get:
                 
                 if(petition.getEntity()==Entity.skill)
                     response.set(ResponseObject.skills, getSkill(petition));
                 if(petition.getEntity()==Entity.employment)
                     response.set(ResponseObject.employments, getEmployment(petition));
                 
                 break;
             default:
                 response.setStatus(Status.PetitionNotFound);
                 break;
         }
         
         return response;
     }
     
     private User addUser(Petition petition)
     {
         User user = new User();
         
         user.setUserId(Integer.parseInt(String.valueOf(Math.round(Math.random()*1000))));
         user.setUserName(petition.get(PetitionParam.username).toString());
         user.setUserType( UserType.get(petition.get(PetitionParam.userType).toString()));
         user.setPassword(petition.get(PetitionParam.password).toString());
         user.setEmail(petition.get(PetitionParam.email).toString());
         
         DummyObjects.addRegisteredUser(user);
         
         return user;
     }
     private User loginUser(Petition petition)
     {
         String username = petition.get(PetitionParam.username).toString();
         User user = new User();
         
         user.setUserName(username);
         user.setPassword(petition.get(PetitionParam.password).toString());
         
         for(User search : DummyObjects.getRegisteredUsers())
         {
             if(search.equals(user))
                 return search;
         }
         
         return null;
     }
     private User logoutUser(Petition petition)
     {
         User user = new User();
         return user;
     }
     private User deleteUser(Petition petition)
     {
         User user = new User();
         user.setUserId(Integer.parseInt(petition.get(PetitionParam.userId).toString()));
         DummyObjects.removeRegisteredUser(user);
         return user;
     }
 
     private User modifyApplicant(Petition petition)
     {
         Applicant user = new Applicant();
         
         user.setUserId(Integer.parseInt(petition.get(PetitionParam.userId).toString()));
         user.setUserName(petition.get(PetitionParam.username).toString());
         user.setPassword(petition.get(PetitionParam.password).toString());
         user.setUserType(UserType.get(petition.get(PetitionParam.userType).toString()));
         user.setName(petition.get(PetitionParam.name).toString());
         user.setPhone(petition.get(PetitionParam.phone).toString());
         if(!petition.get(PetitionParam.age).toString().equals(""))
             user.setAge(Integer.parseInt(petition.get(PetitionParam.age).toString()));
         user.setAddress(petition.get(PetitionParam.address).toString());
         user.setDescription(petition.get(PetitionParam.description).toString());
         user.setContact(petition.get(PetitionParam.contact).toString());
         user.setEmail(petition.get(PetitionParam.email).toString());
         
         DummyObjects.updateRegisteredUser(user);
         
         return user;
     }
     private User modifyCompany(Petition petition)
     {
         Company user = new Company();
         
         user.setUserId(Integer.parseInt(petition.get(PetitionParam.userId).toString()));
         user.setUserName(petition.get(PetitionParam.username).toString());
         user.setPassword(petition.get(PetitionParam.password).toString());
         user.setUserType(UserType.get(petition.get(PetitionParam.userType).toString()));
         user.setName(petition.get(PetitionParam.name).toString());
         user.setPhone(petition.get(PetitionParam.phone).toString());
         user.setAddress(petition.get(PetitionParam.address).toString());
         user.setDescription(petition.get(PetitionParam.description).toString());
         user.setContact(petition.get(PetitionParam.contact).toString());
         user.setEmail(petition.get(PetitionParam.email).toString());
         
         DummyObjects.updateRegisteredUser(user);
         return user;
     }
     
     private String getSkill(Petition petition)
     {
         List<Skill> skills = DummyObjects.getSkills();
         return Skill.toJsonArray(skills);
     }
     private String getEmployment(Petition petition)
     {
         List<Employment> employments = DummyObjects.getEmployments();
         return Employment.toJsonArray(employments);
     }
     
     
 }
