 package org.analogweb.core;
 
 import java.io.InputStream;
 import java.io.Reader;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.analogweb.RequestAttributes;
 import org.analogweb.RequestContext;
 import org.analogweb.TypeMapper;
 
 /**
  * JAXBによる変換により、リクエストされたXMLを任意のオブジェクト
  * のインスタンスに変換する{@link TypeMapper}の実装です。<br/>
  * 変換元の値として、リクエストされたXMLを保持する{@link InputStream}
  * (リクエストボディ)が指定されている必要があります。
  * @author snowgoose
  */
 public class XmlTypeMapper implements TypeMapper {
 
     @Override
     public Object mapToType(RequestContext context, RequestAttributes attributes, Object from,
             Class<?> requiredType, String[] formats) {
         if (isXmlType(context)) {
             if (InputStream.class.isInstance(from)) {
                 return unmershall(createUnmarshaller(requiredType), (InputStream) from);
             } else if (Reader.class.isInstance(from)) {
                 return unmershall(createUnmarshaller(requiredType), (Reader) from);
             }
         }
         return null;
     }
 
    private boolean isXmlType(RequestContext context) {
        HttpServletRequest request = context.getRequest();
        String contentType = request.getContentType();
        return contentType.startsWith("text/xml") || contentType.startsWith("application/xml");
    }
 
     private Unmarshaller createUnmarshaller(Class<?> requiredType) {
         try {
             JAXBContext jaxb = JAXBContext.newInstance(requiredType);
             Unmarshaller un = jaxb.createUnmarshaller();
             return un;
         } catch (JAXBException e) {
             return null;
         }
     }
 
     private Object unmershall(Unmarshaller unmarshaller, InputStream in) {
         try {
             return unmarshaller.unmarshal(in);
         } catch (JAXBException e) {
             return null;
         }
     }
 
     private Object unmershall(Unmarshaller unmarshaller, Reader in) {
         try {
             return unmarshaller.unmarshal(in);
         } catch (JAXBException e) {
             return null;
         }
     }
 
 }
