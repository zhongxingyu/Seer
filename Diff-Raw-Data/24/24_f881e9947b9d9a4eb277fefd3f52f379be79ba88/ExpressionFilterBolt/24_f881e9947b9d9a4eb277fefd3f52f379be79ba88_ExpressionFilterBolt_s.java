 package ch.uzh.ddis.katts.bolts.filter;
 
 import java.util.Map;
 
 import org.springframework.expression.Expression;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.expression.spel.support.StandardEvaluationContext;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import ch.uzh.ddis.katts.bolts.AbstractVariableBindingsBolt;
 import ch.uzh.ddis.katts.bolts.Event;
 import ch.uzh.ddis.katts.bolts.VariableBindings;
 import ch.uzh.ddis.katts.query.stream.Stream;
 import ch.uzh.ddis.katts.query.stream.Variable;
 
 /**
  * The ExpressionFilterBolt provides a generic filter facility for streams. The bolt can be configured by an expression.
  * If the variable bindings match the criteria it is handled over to the next bolt. In the other case, it is thrown
  * away.
  * 
  * The expression can be defined with SpEL. All the variables inside the variable bindings can be used. No expression
  * can be used between multiple variable bindings.
  * 
  * @author Thomas Hunziker
  * 
  */
 public class ExpressionFilterBolt extends AbstractVariableBindingsBolt {
 
 	private static final long serialVersionUID = 1L;
 	private ExpressionFilterConfiguration configuration;
 	private Expression expression;
 
 	@Override
 	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 		ExpressionParser parser = new SpelExpressionParser();
 		expression = parser.parseExpression(this.getConfiguration().getExpression());
 	}
 
 	@Override
 	public void execute(Event event) {
 		StandardEvaluationContext context = new StandardEvaluationContext();
 
 		for (Variable var : event.getEmittedOn().getStream().getAllVariables()) {
 			context.setVariable(var.getName(), event.getVariableValue(var));
 		}
 
 		boolean result = (Boolean) expression.getValue(context);
 
 		// If the expression evaluates to true, then we keep the variable binding
 		// and emit it on the stream.
 		if (result) {
 
 			for (Stream stream : this.getStreams()) {
 				VariableBindings binding = getEmitter().createVariableBindings(stream, event);
 
				// Copy Variables from the inherit stream
				for (Variable variable : stream.getInheritFrom().getAllVariables()) {
					binding.add(variable, event.getVariableValue(variable));
				}

 				// Overwrite inherited values with the variables configured for this stream.
				for (Variable var : event.getEmittedOn().getStream().getAllVariables()) {
 					binding.add(var, event.getVariableValue(var));
 				}
 
 				binding.emit();
 			}
 		}
 
 		event.ack();
 	}
 
 	public ExpressionFilterConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(ExpressionFilterConfiguration configuration) {
 		this.configuration = configuration;
 	}
 
 	@Override
 	public String getId() {
 		return this.getConfiguration().getId();
 	}
 
 }
