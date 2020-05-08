 package org.nohope.rpc.exception;
 
 import org.nohope.rpc.protocol.RPC.Error;
 import com.google.protobuf.ServiceException;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.protobuf.GeneratedMessage.GeneratedExtension;
 import static java.util.Map.Entry;
 
 /**
  * This exception should be thrown by service implementation in case of expected exception.
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 8/22/13 1:40 PM
  */
 public class ExpectedServiceException extends ServiceException {
     private static final long serialVersionUID = 1L;
     private final transient Error.Builder builder;
 
     private ExpectedServiceException(final Throwable cause, final Error.Builder builder) {
         super(cause);
         this.builder = builder;
     }
 
     public Error.Builder getErrorBuilder() {
         return builder;
     }
 
     public static <T> ExpectedServiceException wrap(final Throwable e,
                                                     final GeneratedExtension<Error, T> extension,
                                                     final T value) {
         return new ExpectedServiceException.Builder(e).addExtension(extension, value).build();
     }
 
     public static final class Builder {
         private final Map<GeneratedExtension<Error, Object>, Object> simpleExtensions = new HashMap<>();
         private final Map<GeneratedExtension<Error, List<Object>>, List<?>> listExtensions = new HashMap<>();
 
         private final Throwable throwable;
 
         public Builder(final Throwable throwable) {
             this.throwable = throwable;
         }
 
         @SuppressWarnings("unchecked")
         public Builder addExtension(final GeneratedExtension<Error, ?> extension, final Object value) {
             simpleExtensions.put((GeneratedExtension<Error, Object>) extension, value);
             return this;
         }
 
         @SuppressWarnings("unchecked")
         public <T> Builder addListExtension(final GeneratedExtension<Error, List<T>> extension,
                                             final T... values) {
             // kinda black magic here, yep
             final GeneratedExtension casted = (GeneratedExtension) extension;
             listExtensions.put((GeneratedExtension<Error, List<Object>>) casted, Arrays.asList(values));
             return this;
         }
 
         public ExpectedServiceException build() {
             final Error.Builder builder = Error.newBuilder();
             for (final Entry<GeneratedExtension<Error, Object>, Object> e : simpleExtensions.entrySet()) {
                 builder.setExtension(e.getKey(), e.getValue());
             }
 
             for (final Entry<GeneratedExtension<Error, List<Object>>, List<?>> e : listExtensions.entrySet()) {
                 int i = 0;
                 for (final Object o : e.getValue()) {
                     builder.setExtension(e.getKey(), i++, o);

                 }
             }
 
             return new ExpectedServiceException(throwable, builder);
         }
     }
 }
