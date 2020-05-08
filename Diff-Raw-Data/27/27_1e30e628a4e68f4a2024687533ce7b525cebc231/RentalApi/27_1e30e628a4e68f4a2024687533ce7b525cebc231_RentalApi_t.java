 package at.owlsoft.owl.communication.corba;
 
 import java.util.Date;
 import java.util.UUID;
 
 import org.omg.PortableServer.POAPackage.ServantNotActive;
 import org.omg.PortableServer.POAPackage.WrongPolicy;
 
 import at.owlsoft.owl.corbamodel.accounting.ICorbaRental;
 import at.owlsoft.owl.corbamodel.accounting.ICorbaRentalHelper;
 import at.owlsoft.owl.corbamodel.media.ICorbaMediumExemplar;
 import at.owlsoft.owl.corbamodel.user.ICorbaSystemUser;
 import at.owlsoft.owl.corbamodel.user.ICorbaSystemUserHelper;
 import at.owlsoft.owl.corbamodel.validation.ICorbaValidationMessage;
 import at.owlsoft.owl.corbamodel.validation.ICorbaValidationMessageHelper;
 import at.owlsoft.owl.model.NoPermissionException;
 import at.owlsoft.owl.model.user.SystemUser;
 import at.owlsoft.owl.usecases.ExtensionController;
 import at.owlsoft.owl.usecases.RentalController;
 import at.owlsoft.owl.usecases.ReturnController;
 import at.owlsoft.owl.validation.ValidationMessage;
 
 public class RentalApi extends ICorbaRentalApiPOA
 {
 
     private RentalController    _controller;
 
     private ExtensionController _extensionController;
     private ReturnController    _returnController;
 
     private ApiFactory          _factory;
 
     public RentalApi(ApiFactory factory)
     {
        _factory = factory;
         _controller = factory.getContext().getRentalController();
         _extensionController = factory.getContext().getExtensionController();
         _returnController = factory.getContext().getReturnController();
     }
 
     @Override
     public ICorbaSystemUser getRentalsForSystemUserCardId(int cardID)
     {
         try
         {
             SystemUser user = _factory.getContext()
                     .getSystemUserSearchController().search(cardID);
 
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
 
     @Override
     public void newRental()
     {
         try
         {
             _controller.newRental();
         }
         catch (NoPermissionException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ICorbaSystemUser setCustomer(int cardId)
     {
         SystemUser user = _factory.getContext().getSystemUserSearchController()
                 .search(cardId);
         try
         {
             _controller.setCustomer(user);
         }
         catch (NoPermissionException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException(e);
         }
         CorbaSystemUser cuser = new CorbaSystemUser();
         cuser.setRootPOA(_factory.getRootPOA());
         cuser.setUser(user);
         org.omg.CORBA.Object ref;
         try
         {
             ref = _factory.getRootPOA().servant_to_reference(
                     new CorbaSystemUser());
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
 
     @Override
     public ICorbaMediumExemplar setMediumExemplar(int exemplarId)
     {
         // TODO Auto-generated method stub
 
         return null;
     }
 
     @Override
     public ICorbaValidationMessage[] getValidationMessages()
     {
         try
         {
             ICorbaValidationMessage[] temp = new ICorbaValidationMessage[_controller
                     .getMessages().size()];
             int index = 0;
             for (ValidationMessage message : _controller.getMessages())
             {
                 CorbaValidationMessage cMessage = new CorbaValidationMessage();
                 cMessage.setValidationMessage(message);
                 cMessage.setRootPOA(_factory.getRootPOA());
                 org.omg.CORBA.Object ref = _factory.getRootPOA()
                         .servant_to_reference(cMessage);
                 temp[index] = ICorbaValidationMessageHelper.narrow(ref);
                 index++;
             }
             return temp;
         }
         catch (ServantNotActive e)
         {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
         catch (WrongPolicy e)
         {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void createNewExtension(String uuid)
     {
 
         try
         {
             _extensionController.extend(UUID.fromString(uuid));
         }
         catch (NoPermissionException e)
         {
             throw new RuntimeException(e);
         }
 
     }
 
     @Override
     public void returnRental(String uuid)
     {
         _returnController.returnMediumCopy(UUID.fromString(uuid));
 
     }
 
     @Override
     public ICorbaRental getRental()
     {
         try
         {
             CorbaRental cRental = new CorbaRental();
             cRental.setRental(_controller.getRental());
             cRental.setRootPOA(_factory.getRootPOA());
             org.omg.CORBA.Object ref;
             ref = _factory.getRootPOA().servant_to_reference(cRental);
             return ICorbaRentalHelper.narrow(ref);
         }
         catch (ServantNotActive e)
         {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
         catch (WrongPolicy e)
         {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void setStartDate(long time)
     {
         try
         {
             _controller.setStartDate(new Date(time));
         }
         catch (NoPermissionException e)
         {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
 
     }
 
     @Override
     public boolean store()
     {
         try
         {
             _controller.save();
         }
         catch (NoPermissionException e)
         {
             throw new RuntimeException(e);
         }
         return _controller.getMessages().isEmpty();
     }
 
 }
