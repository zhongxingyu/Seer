 package polly.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.literals.BooleanLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ChannelLiteral;
 import de.skuzzle.polly.parsing.tree.literals.CommandLiteral;
 import de.skuzzle.polly.parsing.tree.literals.DateLiteral;
 import de.skuzzle.polly.parsing.tree.literals.FractionLiteral;
 import de.skuzzle.polly.parsing.tree.literals.HelpLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ListLiteral;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
 import de.skuzzle.polly.parsing.tree.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.tree.literals.StringLiteral;
 import de.skuzzle.polly.parsing.tree.literals.TimespanLiteral;
 import de.skuzzle.polly.parsing.tree.literals.UserLiteral;
 import de.skuzzle.polly.parsing.types.ListType;
 import de.skuzzle.polly.parsing.types.Type;
 import de.skuzzle.polly.sdk.Types;
 
 /**
  * This class maps from parser literals and types to sdk types.
  * 
  * @author Simon
  */
 public class TypeMapper {
 
 	
 	
 	/**
 	 * Maps a sdk type to a parser type, discarding the <tt>types</tt> value.
 	 * @param types The sdk Type
 	 * @return The corresponding parser type.
 	 */
 	public static Type typesToType(Types types) {
 		if (types instanceof Types.BooleanType) {
 			return Type.BOOLEAN;
 		} else if(types instanceof Types.ChannelType) {
 			return Type.CHANNEL;
         } else if (types instanceof Types.TimespanType) {
             return Type.TIMESPAN;
 		} else if (types instanceof Types.DateType) {
 			return Type.DATE;
 		} else if (types instanceof Types.NumberType) {
 			return Type.NUMBER;
 		} else if (types instanceof Types.StringType) {
 			return Type.STRING;
 		} else if (types instanceof Types.UserType) {
 			return Type.USER;
 		} else if (types instanceof Types.CommandType) {
 			return Type.COMMAND;
 		} else if (types instanceof Types.AnyType) {
 			return Type.ANY;
 		} else if (types instanceof Types.HelpType) {
 		    return Type.HELP;
 		} else if (types instanceof Types.ListType) {
 			Types.ListType lt = (Types.ListType) types;
 			return new ListType(TypeMapper.typesToType(lt.getElementType()));
 		}
 		
 		throw new IllegalArgumentException("Invalid type");
 	}
 	
 	
 	
 	/**
 	 * Maps a parser type to a sdk type with empty values.
 	 * @param type The parser type.
 	 * @return The corresponding sdk type.
 	 */
 	public static Types typeToTypes(Type type) {
 		if (type == Type.BOOLEAN) {
 			return new Types.BooleanType(false);
 		} else if (type == Type.CHANNEL) {
 			return Types.CHANNEL;
         } else if (type == Type.TIMESPAN) {
             return Types.TIMESPAN;
 		} else if (type == Type.DATE) {
 			return Types.DATE;
 		} else if (type == Type.NUMBER) {
 			return Types.NUMBER;
 		} else if (type == Type.STRING) {
 			return Types.STRING;
 		} else if (type == Type.USER) {
 			return Types.USER;
 		} else if (type == Type.COMMAND) {
 			return Types.COMMAND;
 		} else if (type == Type.ANY) {
 			return Types.ANY;
 		} else if (type == Type.HELP) {
 		    return Types.HELP;
 		} else if (type == Type.LIST) {
 			ListType lt = (ListType) type;
 			return new Types.ListType(TypeMapper.typeToTypes(lt.getSubType()));
 		}
 		
 		throw new IllegalArgumentException("Invalid type");
 	}
 	
 	
 	
 	/**
 	 * Maps a sdk type to a parser literal with the same value and empty position.
 	 * @param types The sdk type.
 	 * @return A corresponding parser literal.
 	 */
 	public static Literal typesToLiteral(Types types) {
 		if (types instanceof Types.BooleanType) {
 			Types.BooleanType bt = (Types.BooleanType) types;
 			return new BooleanLiteral(bt.getValue());
 			
 		} else if(types instanceof Types.ChannelType) {
 			Types.ChannelType bt = (Types.ChannelType) types;
 			return new ChannelLiteral(bt.getValue());
 			
         } else if (types instanceof Types.TimespanType) {
             Types.TimespanType ts = (Types.TimespanType) types;
             return new TimespanLiteral(ts.getSpan());
             
 		} else if (types instanceof Types.DateType) {
 			Types.DateType ct = (Types.DateType) types;
 			return new DateLiteral(ct.getValue());
 			
 		} else if (types instanceof Types.NumberType) {
 			Types.NumberType bt = (Types.NumberType) types;
 			return new NumberLiteral(bt.getValue());
 			
 		} else if (types instanceof Types.StringType) {
 			Types.StringType bt = (Types.StringType) types;
 			return new StringLiteral(bt.getValue());
 			
 		} else if (types instanceof Types.UserType) {
 			Types.UserType bt = (Types.UserType) types;
 			return new UserLiteral(bt.getValue());
 			
 		} else if (types instanceof Types.CommandType) {
 			Types.CommandType ct = (Types.CommandType) types;
 			return new CommandLiteral(ct.getValue());
 			
 		} else if (types instanceof Types.HelpType) {
 		    return new HelpLiteral();
 		    
 		} else if (types instanceof Types.ListType) {
 			Types.ListType lt = (Types.ListType) types;
 			ArrayList<Expression> elements = new ArrayList<Expression>();
 			for (Types t : lt.getElements()) {
 				elements.add(TypeMapper.typesToLiteral(t));
 			}
 			return new ListLiteral(elements);
 		}
 		
 		throw new IllegalArgumentException("Invalid type");
 	}
 	
 	
 	
 	/**
 	 * Maps a parser literal to a sdk type with the same value. 
 	 * @param literal The parser literal.
 	 * @return A corresponding sdk type with same value.
 	 */
 	public static Types literalToTypes(Expression literal) {
 		if (literal instanceof BooleanLiteral) {
 			BooleanLiteral bl = (BooleanLiteral) literal;
 			return new Types.BooleanType(bl.getValue());
 			
 		} else if (literal instanceof ChannelLiteral) {
 			ChannelLiteral bl = (ChannelLiteral) literal;
 			return new Types.ChannelType(bl.getChannelName());
 			
         } else if (literal instanceof TimespanLiteral) {
             TimespanLiteral ts = (TimespanLiteral) literal;
             return new Types.TimespanType(ts.getValue(), ts.getTargetFromNow());
             
 		} else if (literal instanceof DateLiteral) {
 			DateLiteral dt = (DateLiteral) literal;
 			return new Types.DateType(dt.getValue());
 			
 		} else if (literal instanceof FractionLiteral) {
 		    FractionLiteral fl = (FractionLiteral) literal;
 		    return new Types.FractionType(fl.getNumerator(), 
		        fl.getDenominator(), fl.getValue(), fl.isIllegal());
 		    
 		} else if (literal instanceof NumberLiteral) {
 			NumberLiteral bl = (NumberLiteral) literal;
 			return new Types.NumberType(bl.getValue(), bl.getRadix());
 			
 		} else if (literal instanceof StringLiteral) {
 			StringLiteral bl = (StringLiteral) literal;
 			return new Types.StringType(bl.getValue());
 			
 		} else if (literal instanceof UserLiteral) {
 			UserLiteral bl = (UserLiteral) literal;
 			return new Types.UserType(bl.getUserName());
 			
 		} else if (literal instanceof CommandLiteral) {
 			CommandLiteral cl = (CommandLiteral) literal;
 			return new Types.CommandType(cl.getCommandName());
 			
 		} else if (literal instanceof HelpLiteral) {
 		    return Types.HELP;
 		    
 		} else if (literal instanceof ListLiteral) {
 			ListLiteral lt = (ListLiteral) literal;
 			List<Types> elements = new ArrayList<Types>();
 			for (Expression lit : lt.getElements()) {
 				elements.add(TypeMapper.literalToTypes(lit));
 			}
 			return new Types.ListType(elements);
 		}
 		
 		throw new IllegalArgumentException("Invalid type");
 	}
 }
