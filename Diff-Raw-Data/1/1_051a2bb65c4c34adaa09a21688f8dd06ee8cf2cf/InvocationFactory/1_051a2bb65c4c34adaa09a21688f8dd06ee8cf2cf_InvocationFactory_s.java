 package org.analogweb;
 
 import java.util.List;
 
 /**
  * {@link Invocation}を生成するファクトリです。
  * 通常、{@link Invocation}はリクエスト毎にこのファクトリを
  * 通じてインスタンスが生成されます。
  * @author snowgoose
  */
 public interface InvocationFactory extends Module {
 
     /**
      * 新しい{@link Invocation}のインスタンスを生成します。
      * @param instanceProvider リクエストに一致する実行対象のインスタンスプロバイダ
      * @param metadata {@link InvocationMetadata}
     * @param resultAttributes {@link ResultAttributes}
      * @param context {@link RequestContext}
      * @param converters {@link TypeMapperContext}
      * @param processors 生成される{@link Invocation}に適用される全ての{@link InvocationProcessor}
      * @param handlers {@link AttributesHandlers}
      * @return　生成された{@link Invocation}
      */
     Invocation createInvocation(ContainerAdaptor instanceProvider, InvocationMetadata metadata,
             RequestContext context, TypeMapperContext converters,
             List<InvocationProcessor> processors, AttributesHandlers handlers);
 
 }
