 package at.owlsoft.owl.communication.corba;
 
 import org.omg.PortableServer.POAPackage.ServantNotActive;
 import org.omg.PortableServer.POAPackage.WrongPolicy;
 
 import at.owlsoft.owl.business.SystemUserSearchController;
 import at.owlsoft.owl.corbamodel.user.ICorbaSystemUser;
 import at.owlsoft.owl.corbamodel.user.ICorbaSystemUserHelper;
 import at.owlsoft.owl.model.user.SystemUser;
 
 public class SystemUserApi extends ICorbaSystemUserApiPOA
 {
 
     private ApiFactory                 _factory;
     private SystemUserSearchController _controller;
 
     public SystemUserApi(ApiFactory apiFactory)
     {
         _factory = apiFactory;
         _controller = _factory.getContext().getSystemUserSearchController();
     }
 
     @Override
     public ICorbaSystemUser getSystemUserByCardID(int cardId)
     {
         try
         {
             SystemUser user = _factory.getContext()
                     .getSystemUserSearchController().search(cardId);
 
             CorbaSystemUser cuser = new CorbaSystemUser();
             cuser.setRootPOA(_factory.getRootPOA());
             cuser.setUser(user);
             org.omg.CORBA.Object ref = _factory.getRootPOA()
                    .servant_to_reference(new CorbaSystemUser());
             return ICorbaSystemUserHelper.narrow(ref);
         }
         catch (ServantNotActive e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException(e);
         }
         catch (WrongPolicy e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
 }
