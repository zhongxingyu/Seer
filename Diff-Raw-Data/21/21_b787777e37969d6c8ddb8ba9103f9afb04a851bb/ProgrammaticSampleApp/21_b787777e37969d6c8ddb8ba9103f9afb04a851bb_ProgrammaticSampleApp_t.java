 package vinna.samples.programmatic;
 
 import vinna.Vinna;
 
 public class ProgrammaticSampleApp extends Vinna {
 
     @Override
     protected void routes() {
         get("/user/{id: \\d+}")
                 .withController(Controller.class)
                 .test(param("id").asInt());
 
         get("/user/{id: \\d+}/{name}")
                 .withController(Controller.class)
                 .test2(param("name").asString(), param("id").asInt());
 
         get("/hello/{hello: true|false}/{id}/{name}")
                 .withController(Controller.class)
                 .test2(param("name").asString(), param("hello").asBoolean(), param("id").asInt());
 
         get("/constant/{test}")
                 .withController(Controller.class)
                 .test2(param("test").asString(), constant(17));
 
         get("/header")
                 .withController(Controller.class)
                 .writeHeader(req.header("Accept").asString());
 
         get("/headers")
                 .withController(Controller.class)
                 .writeHeaders(req.header("Accept").asCollection(String.class));
         get("/allheaders")
                 .withController(Controller.class)
                 .printHeaders(req.headers());

        get("/params?{a}&{b}&{c: \\d+}")
                .withController(Controller.class)
                .params(param("a").asString(), param("b").asString(), param("c").asInt());
     }
 }
