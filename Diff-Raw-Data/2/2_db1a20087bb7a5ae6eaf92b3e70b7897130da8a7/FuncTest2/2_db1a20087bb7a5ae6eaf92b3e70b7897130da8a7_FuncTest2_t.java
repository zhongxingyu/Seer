 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package evostrattest;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import net.mkonrad.evostrat.EvoOptimizable;
 import net.mkonrad.evostrat.EvoParam;
 import net.mkonrad.evostrat.EvoParamConditionMinMax;
 import net.mkonrad.evostrat.EvoParamProperties;
 
 /**
  *
  * @author markus
  */
 public class FuncTest2 implements EvoOptimizable {
     private HashMap<String, EvoParam> params;
     private HashSet<EvoParamProperties> paramProps;
 
     public FuncTest2() {
         params = new HashMap<String,EvoParam>();
         paramProps = new HashSet<EvoParamProperties>();
         
         EvoParamProperties propX = new EvoParamProperties("x", 0.0f, 1.0f, 0.9f, 0.1f);
         EvoParamProperties propY = new EvoParamProperties("y", 0.0f, 1.0f, 0.9f, 0.1f);
         
         propX.addParamCondition(new EvoParamConditionMinMax(0.0f, 1.0f));
         paramProps.add(propX);
         propY.addParamCondition(new EvoParamConditionMinMax(0.0f, 1.0f));
         paramProps.add(propY);
     }
     
     @Override
     public HashSet<EvoParamProperties> getParamPropertiesSet() {
         return paramProps;
     }
 
     @Override
     public void setParam(EvoParam param) {
         params.put(param.name, param);
     }
 
     @Override
     public float makeTestRun() {
         float x = params.get("x").val;
         float y = params.get("y").val;
         
         float n = 9.0f;
         
        // maximum at x = 0.5, y = 0.5 with z = 0.878
         float z = (float)Math.pow(
                   15.0f * x * y * (1.0f - x) * (1.0f - y)
                 * (float)Math.sin(n * Math.PI * x)
                 * (float)Math.sin(n * Math.PI * y),
                   2);
         
         return z;
     }
 
     @Override
     public void setParamSet(HashSet<EvoParam> params) {
         for (EvoParam p : params) {
             setParam(p);
         }
     }
     
 }
