 package biz.c24.io.spring.sink;
 
 import biz.c24.io.api.presentation.JsonSink;
 import biz.c24.io.api.presentation.Sink;
 
 /**
  * User: mvickery
  * Date: 23/04/2012
  */
public class JSonSinkFactory extends AbstractSinkFactory {
 
     @Override
     protected Sink createSink() {
         return new JsonSink();
     }
 }
