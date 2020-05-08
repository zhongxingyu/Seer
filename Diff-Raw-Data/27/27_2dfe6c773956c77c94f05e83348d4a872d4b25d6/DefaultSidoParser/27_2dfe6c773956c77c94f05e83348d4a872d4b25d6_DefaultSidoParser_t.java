 package net.sf.sido.parser.support;
 
import java.util.ArrayList;
 import java.util.Collection;
 
 import net.sf.jstring.Localizable;
 import net.sf.jstring.MultiLocalizable;
 import net.sf.sido.parser.SidoParser;
 import net.sf.sido.parser.model.XSchema;
 import net.sf.sido.parser.parboiled.XAction;
 import net.sf.sido.parser.parboiled.XParser;
 import net.sf.sido.schema.SidoContext;
 import net.sf.sido.schema.SidoSchema;
 
 import org.parboiled.Parboiled;
 import org.parboiled.buffers.DefaultInputBuffer;
 import org.parboiled.errors.ParseError;
 import org.parboiled.parserunners.ParseRunner;
 import org.parboiled.parserunners.ReportingParseRunner;
 import org.parboiled.support.ParsingResult;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 public class DefaultSidoParser implements SidoParser {
 	
 	private final SidoContext context;
 	
 	public DefaultSidoParser(SidoContext context) {
 		this.context = context;
 	}
 	
 	@Override
 	public SidoContext getContext() {
 		return context;
 	}
 	
 	@Override
 	public Collection<SidoSchema> parse(Collection<String> inputs) {
 		// Parses all inputs
		Collection<XSchema> xSchemas = new ArrayList<XSchema>();
		for (String input : inputs) {
			XSchema xschema = xparse(input);
			xSchemas.add(xschema);
			
		}
 		// Creation of schemas
 		return build (xSchemas);
 	}
 
 	protected XSchema xparse (String input) {
 		// Input buffer
 		DefaultInputBuffer inputBuffer = new DefaultInputBuffer(input.toCharArray());
 		// Creates the action support
 		XAction action = new XAction();
 		// Creates the parser
 		XParser parser = Parboiled.createParser(XParser.class, action);
 		// Creates the parser runner
 		ParseRunner<String> runner = new ReportingParseRunner<String>(parser.schema());
 		// Runs the parser
 		ParsingResult<String> result = runner.run(inputBuffer);
 		// Checks the result
 		if (result.hasErrors()) {
 			MultiLocalizable list = new MultiLocalizable(Collections2.transform(result.parseErrors, new Function<ParseError, Localizable>() {
 				@Override
 				public Localizable apply(ParseError error) {
 					return localize (error);
 				}
 			}));
 			throw new SidoParseException(list);
 		}
 		// OK
 		else {
 			// Gets the X schema
 			XSchema xSchema = action.getSchema();
 			// OK
 			return xSchema;
 		}
 	}
 	
 	protected Localizable localize(ParseError error) {
 		return new SidoParseExceptionDetail(error.getStartIndex(), error.getEndIndex(), error.getErrorMessage());
 	}
 
 	protected Collection<SidoSchema> build(Collection<XSchema> xSchemas) {
 		return Builder.create(context).build(xSchemas);
 	}
 
 }
