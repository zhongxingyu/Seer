 package LinkFuture.Core.ContentManager.ContentResource;
 
 import LinkFuture.Core.ContentManager.Model.*;
 import LinkFuture.Core.UriBuilder;
 import LinkFuture.Core.WebClient.*;
 import LinkFuture.Init.Extensions.StringExtension;
 import LinkFuture.Init.ObjectExtend.NameValuePair;
 import LinkFuture.Core.Utility;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Cyokin Zhang
  * Date: 10/1/13
  * Time: 10:48 AM
  */
 public class WebContentResource extends ContentBeanBaseResource<WebContentResourceMetaInfo> {
     static final String ContentType = "Content-Type";
     public String ResourceUrl;
     public WebContentResource(ResourceInfo currentResource) throws Exception {
         super(currentResource,WebContentResourceMetaInfo.class);
     }
     @Override
     public ContentResultInfo RetrieveResource(ContentItemInfo content, ContentParameterCollectionInfo parameters) throws Exception {
         this.ResourceUrl = this.BuildAbsoluteUri(parameters,true);
         if(StringExtension.HasReplacement(this.ResourceUrl))
         {
             throw new IllegalArgumentException("Specific url is incorrect:" + this.ResourceUrl);
         }
         ContentResultInfo contentResult = new ContentResultInfo();
         if(StringExtension.IsUrl(this.ResourceUrl))
         {
             ArrayList<NameValuePair> postList = this.BuildPostArguments(parameters,true);
             ArrayList<NameValuePair> headList = this.BuildHeadArguments(parameters,true);
             WebRequestInfo meta = new WebRequestInfo();
             meta.RequestURL = new URL(this.ResourceUrl);
             meta.RequestMethod = this.Meta.Method;
             meta.UserName = this.Meta.UserName;
             meta.Password = this.Meta.Password;
             for (NameValuePair item :postList)
             {
                 if(item.value instanceof WebRequestFileInfo)
                 {
                     meta.addPostFile((WebRequestFileInfo)item.value);
                 }
                 else
                 {
                     meta.addPostData(item.id, item.value);
                 }
             }
             for (NameValuePair item :headList)
             {
                 meta.addHead(item.id, item.value);
             }
             meta.ConnectionTimeout = this.Meta.ConnectionTimeout;
             meta.ReadTimeout = this.Meta.ReadTimeout;
             if(this.Meta.UseCaches!=null){
                 meta.UseCaches = this.Meta.UseCaches;
             }
             if(this.Meta.AutoRedirect!=null){
                 meta.AutoRedirect = this.Meta.AutoRedirect;
             }
             WebClientResultInfo result = WebClient.sendRequest(meta,this.Meta.RetryTimes);
             contentResult.ErrorCode = result.code;
             if(result.success)
             {
                 contentResult.Success = true;
                 contentResult.Result =  result.response;
                 if(result.HeaderFields.containsKey(ContentType) && result.HeaderFields.get(ContentType).toLowerCase().contains("json"))
                 {
                     contentResult.ResultType = ContentResultType.Json;
                 }
                 else
                 {
                     contentResult.ResultType = ContentResultType.Xml;
                 }
             }
             else
             {
                 contentResult.Success = false;
                 contentResult.Error = new IOException(result.errorMessage);
             }
         }
         else {
             contentResult.Success = true;
             contentResult.ErrorCode = 500;
             contentResult.Result = Utility.getStringFromFile(this.ResourceUrl);
             contentResult.ErrorCode = 200;
             //TODO:hard code for now, as local file may not xml type
             contentResult.ResultType = ContentResultType.Xml;
         }
         return contentResult;
     }
     private String BuildAbsoluteUri(ContentParameterCollectionInfo parameters, boolean throwException)
     {
         String targetUri = this.Meta.FeedUri;
         if (parameters != null)
         {
             targetUri = parameters.ParamReplace(targetUri);
         }
         if(this.Meta.UrlParameters!=null)
         {
             UriBuilder uriBuilder = UriBuilder.create(targetUri);
             ArrayList<NameValuePair> list = BuildUrlArguments(parameters,throwException);
             for(NameValuePair item :list)
             {
                 uriBuilder.setQuery(item.id,item.value.toString());
             }
             targetUri = uriBuilder.toString();
         }
         return targetUri;
     }
     private ArrayList<NameValuePair> BuildHeadArguments(ContentParameterCollectionInfo parameters, boolean throwException)
     {
         return BuildArguments(parameters,this.Meta.Headers,throwException);
     }
     private ArrayList<NameValuePair> BuildPostArguments(ContentParameterCollectionInfo parameters, boolean throwException)
     {
         return BuildArguments(parameters,this.Meta.PostParameters,throwException);
     }
     private ArrayList<NameValuePair> BuildUrlArguments(ContentParameterCollectionInfo parameters, boolean throwException)
     {
         return BuildArguments(parameters,this.Meta.UrlParameters,throwException);
     }
     private static ArrayList<NameValuePair> BuildArguments(ContentParameterCollectionInfo parameters,List<WebContentResourceParameterInfo> needPostParameters, boolean throwException)
     {
         ArrayList<NameValuePair> list = new ArrayList<>();
         if(needPostParameters!=null)
         {
             for(WebContentResourceParameterInfo param:needPostParameters)
             {
                 Object  paramValue = null;
                 if(parameters.containsKey(param.Name))
                 {
                     paramValue = parameters.get(param.Name);
                 }
                 else
                 {
                     if(!StringExtension.IsNullOrEmpty(param.DefaultValue))
                     {
                         paramValue = param.DefaultValue;
                     }
                 }
                 if(paramValue==null || (paramValue instanceof String && StringExtension.IsNullOrEmpty((String)paramValue)))
                 {
                     //it may null if value come from selector when BuildResourceIdentity, since we don;t have specific resource when start, so we need ignore error
                     if(param.Required && throwException)
                     {
                         throw new IllegalArgumentException(param.Name + " is required");
                     }
                     else
                     {
                         continue;
                     }
                 }
                 NameValuePair item = new NameValuePair();
                 if(StringExtension.IsNullOrEmpty(param.QueryName))
                 {
                     item.id = param.Name;
                 }
                 else
                 {
                     item.id = param.QueryName;
                 }
                 item.value = paramValue;
                 list.add(item);
             }
         }
         return list;
     }
     @Override
     public String BuildResourceIdentity(ContentItemInfo content, ContentParameterCollectionInfo parameters) {
         StringBuilder argsSb = new StringBuilder();
         argsSb.append("$WebContentResource$");
         argsSb.append(BuildAbsoluteUri(parameters,false));
         if (this.Meta.Method == HttpMethod.Post)
         {
             ArrayList<NameValuePair> args = this.BuildPostArguments(parameters,false);
             if(args.size()>0)
             {
                 argsSb.append("$From$");
                 for(NameValuePair item :args)
                 {
                     argsSb.append(String.format("%s:%s", item.id, item.value));
                 }
             }
         }
         //ignore http head, as it should be same for cache logic, ignore for now
         return argsSb.toString();
     }
 
     @Override
     public void Verify(ResourceInfo currentResource){
          if(StringExtension.IsNullOrEmpty(this.Meta.FeedUri))
          {
              throw new IllegalArgumentException("Invalid feed uri");
          }
     }
 }
