 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.browser;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
import org.openide.ErrorManager;
 import org.openide.util.Lookup;
 import ovation.*;
 import us.physion.ovation.interfaces.ConnectionProvider;
 import us.physion.ovation.interfaces.IEntityWrapper;
 
 /**
  *
  * @author huecotanks
  */
 public class EntityWrapper implements IEntityWrapper {
     
     private String uri;
     private Class type;
     private String displayName;
     
     public EntityWrapper(IEntityBase e)
     {
         uri = e.getURIString();
         type = e.getClass();
         displayName = EntityWrapper.inferDisplayName(e);
     }
     
     //used by the PerUserEntityWrapper object
     protected EntityWrapper(String name, Class clazz, String uri)
     {
         type = clazz;
         displayName = name;
         this.uri = uri;
     }
     
     @Override
     public IEntityBase getEntity(){
        IEntityBase b = null;
        try{
            IAuthenticatedDataStoreCoordinator dsc = Lookup.getDefault().lookup(ConnectionProvider.class).getConnection();
            if (dsc == null)
            {
                return null;
            }
            DataContext c = dsc.getContext();
            b = c.objectWithURI(uri);
        
        } catch (RuntimeException e)
         {
            ErrorManager.getDefault().notify(e);
            throw e;
         }
        catch(Exception e)
        {
            ErrorManager.getDefault().notify(e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return b;
     }
     @Override
     public String getURI()
     {
         return uri;
     }
     @Override
     public String getDisplayName() {return displayName;}
     @Override
     public Class getType() { return type;}
 
    
     public static String inferDisplayName(IEntityBase e) {
 	Class type = e.getClass();
         if (type.isAssignableFrom(Source.class))
         {
             return ((Source)e).getLabel();
         }
         else if (type.isAssignableFrom(Project.class))
         {
             return ((Project)e).getName();
         }else if (type.isAssignableFrom(Experiment.class))
         {
             return ((Experiment)e).getStartTime().toString("MM/dd/yyyy-hh:mm:ss");
         }
         else if (type.isAssignableFrom(EpochGroup.class))
         {
             return ((EpochGroup)e).getLabel();
         }
         else if (type.isAssignableFrom(Epoch.class))
         {
             return ((Epoch)e).getProtocolID();
         }
         else if (type.isAssignableFrom(Response.class) || type.isAssignableFrom(URLResponse.class) )
         {
             return ((Response)e).getExternalDevice().getName();
         }
         else if (type.isAssignableFrom(Stimulus.class))
         {
             return ((Stimulus)e).getExternalDevice().getName();
         }
         else if (type.isAssignableFrom(DerivedResponse.class))
         {
             return ((DerivedResponse)e).getName();
         }
         else if (type.isAssignableFrom(AnalysisRecord.class))
         {
             return ((AnalysisRecord)e).getName();
         }
         return "<no name>";
     }
 }
