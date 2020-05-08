 package rate.controller.benchmark;
 
 import rate.engine.benchmark.GeneralBenchmark;
 import rate.engine.benchmark.OneClassImposterBenchmark;
 import rate.engine.benchmark.SLSBBenchmark;
 import rate.model.BenchmarkEntity;
 import rate.model.ViewEntity;
 import rate.util.DebugUtil;
 
 /**
  * Created by XianRan
  * Email: xranthoar@gmail.com
  * Date: 13-3-26
  * Time: 下午12:04
  */
 public class CreateAction extends BenchmarkActionBase {
     public void setBenchmark(BenchmarkEntity benchmark) {
         this.benchmark = benchmark;
     }
 
     private String viewUuid;
 
     private ViewEntity view;
 
     public String getViewUuid() {
         return viewUuid;
     }
 
     public void setViewUuid(String viewUuid) {
         this.viewUuid = viewUuid;
         view = (ViewEntity)session.createQuery("from ViewEntity where uuid=:uuid").setParameter("uuid", viewUuid)
         .list().get(0);
         benchmark.setView(view);
     }
 
     public void setBenchmarkType() {
         String generatorStr = benchmark.getGenerator();
         if (generatorStr.matches("(SMALL)|(MEDIUM)|((VERY_)?LARGE)|.*Imposter")) {
             benchmark.setType("General");
        } else if (generatorStr.equals("SLSB(100C)") || generatorStr.equals("SLSB(1000C)")) {
             benchmark.setType("SLSB");
         }
     }
 
     public String execute() throws Exception {
         session.beginTransaction();
         setBenchmarkType();
         session.save(benchmark);
 
         String generatorStr = benchmark.getGenerator();
         if (generatorStr.matches("(SMALL)|(MEDIUM)|((VERY_)?LARGE)")) {
             GeneralBenchmark generalBenchmark = new GeneralBenchmark();
 
             generalBenchmark.setBenchmark(benchmark);
             // This should depend on user's option
             generalBenchmark.setScale(generatorStr);
             benchmark = generalBenchmark.generate();
         } else if (generatorStr.equals("OneClassImposter")) {
             OneClassImposterBenchmark generator = new OneClassImposterBenchmark();
             generator.setBenchmark(benchmark);
             benchmark = generator.generate();
         } else if (generatorStr.equals("SLSB(100C)") || generatorStr.equals("SLSB(1000C)")) {
             SLSBBenchmark generator = new SLSBBenchmark();
             generator.setBenchmark(benchmark);
             generator.setB4Far(20);
             generator.setB4Frr(20);
             generator.setK(10);
             generator.setAlpha(0.1);
             generator.setSampleCount(3);
             // This should depend on user's option
             if (generatorStr.equals("SLSB(100C)")) {
                 generator.setClassCount(100);
             } else {
                 generator.setClassCount(1000);
             }
             benchmark = generator.generate();
         } else {
             return ERROR;
         }
 
        DebugUtil.debug(benchmark.getType());
         session.update(benchmark);
         session.getTransaction().commit();
         return SUCCESS;
     }
 }
