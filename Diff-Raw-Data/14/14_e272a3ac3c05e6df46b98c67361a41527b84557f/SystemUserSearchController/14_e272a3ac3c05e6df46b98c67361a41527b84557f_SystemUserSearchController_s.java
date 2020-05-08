 package at.owlsoft.owl.business;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import at.owlsoft.owl.dao.DaoManager;
 import at.owlsoft.owl.dao.IDaoFactory;
 import at.owlsoft.owl.dao.ISystemUserDao;
 import at.owlsoft.owl.model.SearchField;
 import at.owlsoft.owl.model.SearchFieldType;
 import at.owlsoft.owl.model.user.SystemUser;
 import at.owlsoft.owl.usecases.SearchController;
 
 public class SystemUserSearchController extends SearchController<SystemUser>
 {
 
     public SystemUserSearchController(OwlApplicationContext context)
     {
         super(context);
     }
 
     @Override
     public List<SystemUser> search(List<SearchField> searchFields)
     {
         ISearchFieldValueConverter converter = getContext()
                 .getServerSearchFieldDefinitionController();
 
         IDaoFactory daoFactory = DaoManager.getInstance();
         ISystemUserDao dao = daoFactory.getSystemUserDao();
 
         return dao.queryByPropertyList(searchFields, converter);
     }
 
     public SystemUser search(int cardId)
     {
 
         // // FIXME --> should come from database
         // SystemUser user = new SystemUser();
         //
         // user.setFirstName("Manuel");
         // user.setLastName("Tscholl");
         // user.setCardID(0);
         // if (true)
         // {
         // return user;
         // }
 
         List<SearchField> fields = new ArrayList<SearchField>();
        fields.add(new SearchField("_cardId", new Integer(cardId).toString(),
                 SearchFieldType.Equals));
 
         List<SystemUser> users = search(fields);
 
         if (users != null && users.size() > 0)
         {
             return users.get(0);
         }
 
         return null;
 
     }
 }
