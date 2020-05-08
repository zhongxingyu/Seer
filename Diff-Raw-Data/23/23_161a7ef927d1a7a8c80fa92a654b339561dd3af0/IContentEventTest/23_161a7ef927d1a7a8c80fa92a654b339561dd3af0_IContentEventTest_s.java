 package LinkFuture.Core.ContentManager.ContentEvent;
 
 import LinkFuture.Core.ContentManager.Model.AddInfo;
 import LinkFuture.Core.ContentManager.Model.ContentParameterCollectionInfo;
 import LinkFuture.Core.ContentManager.Model.ContentResultInfo;
 import LinkFuture.Core.Debugger;
 
 /**
  * LinkFuture.
  * User: Cyokin Zhang
  * Date: 12/15/13
  * Time: 10:01 PM
  */
 public class IContentEventTest implements IContentEvent {
     @Override
     public void OnTrigger(AddInfo currentAdd, Object currentContext, ContentParameterCollectionInfo parameters) {
        if(currentContext instanceof ContentResultInfo)
         {
            ContentResultInfo result = (ContentResultInfo)currentContext;
             if (!result.Success)
             {
                  System.err.println("IContentEventTest trigger error " + result.Error.toString());
             }
             else
             {
                 System.out.println("IContentEventTest trigger success ");
             }
         }
     }
 }
