 package me.smecsia.testtask.calculator.res;
 
 import me.smecsia.testtask.calculator.backend.to.Expression;
 import me.smecsia.testtask.calculator.backend.to.Result;
 import me.smecsia.testtask.calculator.common.components.BasicResource;
 import me.smecsia.testtask.calculator.math.ExpressionEvaluator;
 import me.smecsia.testtask.calculator.math.ExpressionBuilder;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 
 import static me.smecsia.testtask.calculator.common.util.ExceptionUtil.formatStackTrace;
 
 /**
  * @author Ilya Sadykov
 *         Date: 17.12.12
 *         Time: 9:53
  */
 @Path("/calc")
 @Produces(MediaType.APPLICATION_XML)
 @Consumes(MediaType.APPLICATION_XML)
 public class CalculatorResource extends BasicResource {
 
     @POST
     @Path("/by/syntaxtree")
     public Result syntaxTree(Expression expression) {
         Result res = new Result();
         try {
             res.setData(new ExpressionEvaluator().evaluate(expression));
             res.setSuccess(true);
         } catch (Exception e) {
             log(e);
             res.setMessage(formatStackTrace(e));
             res.setSuccess(false);
         }
         return res;
     }
 
     @GET
     @Path("/sample")
     public Expression sample() {
         return ExpressionBuilder.sample();
     }
 }
