 package LinkFuture.Core.ContentManager.Model;
 
 import LinkFuture.Core.Utility;
 import LinkFuture.Init.ObjectExtend.CaseInsensitiveMap;
 
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 12/4/13
  * Time: 11:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ContentResultCollectionInfo extends CaseInsensitiveMap<ContentResultInfo> {
     //public boolean ResourceOnly;
     public String ToContentXml()
     {
         StringBuilder sb = new StringBuilder();
         if (this.size() > 1)
         {
             sb.append("<Root>");
         }
         for (Map.Entry<String,ContentResultInfo> item : this.entrySet())
         {
             if (this.size() > 1)
             {
                 sb.append("<Content");
                 sb.append(" name=\"");
                 sb.append(item.getKey());
                 sb.append("\">");
             }
             ContentResultInfo result = item.getValue();
             Object value = result.Result;
            if(value!=null)
             {
                 if (value instanceof  String)
                 {
                     if(result.ResultType == ContentResultType.Json)
                     {
                         value = "<Root>".concat(Utility.JsonToXml((String)value)).concat("</Root>");
                     }
                     sb.append(Utility.removeXmlDeclaration((String) value));
                 }
                 else
                 {
                     sb.append(Utility.removeXmlDeclaration(Utility.toXml(value)));
                 }
             }
             if (this.size() > 1)
             {
                 sb.append("</Content>");
             }
         }
         if (this.size() > 1)
         {
             sb.append("</Root>");
         }
         return sb.toString();
     }
 }
