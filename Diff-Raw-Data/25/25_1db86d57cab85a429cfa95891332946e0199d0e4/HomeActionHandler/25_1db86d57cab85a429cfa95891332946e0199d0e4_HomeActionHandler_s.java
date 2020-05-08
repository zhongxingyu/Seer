 package ddth.dasp.statushetty.actionhandler;
 
 import ddth.dasp.hetty.IUrlCreator;
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.mvc.view.IViewResolver;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 import ddth.dasp.statushetty.DaspBundleConstants;
 
 public class HomeActionHandler extends BaseActionHandler {
 
     private String viewRedirect;
 
     @Override
    protected Object internalHandleRequest(IRequest request, ITopicPublisher topicPublisher) {
         if (viewRedirect == null) {
             IUrlCreator urlCreator = getUrlCreator();
             String url = urlCreator.createUrl(new String[] { DaspBundleConstants.MODULE_NAME,
                     "server" }, null);
             viewRedirect = IViewResolver.REDIRECT_VIEW_PREFIX + url;
         }
         return viewRedirect;
     }
 }
