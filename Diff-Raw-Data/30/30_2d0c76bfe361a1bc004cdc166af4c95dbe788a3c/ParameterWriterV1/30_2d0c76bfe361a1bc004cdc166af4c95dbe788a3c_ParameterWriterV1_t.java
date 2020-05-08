 package confdb.converter;
 
 import confdb.data.PSetParameter;
 import confdb.data.Parameter;
 import confdb.data.ScalarParameter;
 import confdb.data.VPSetParameter;
 import confdb.data.VectorParameter;
 
 public class ParameterWriterV1  implements IParameterWriter {
 
 	public String toString( Parameter parameter, Converter converter, String indent ) 
 	{
 		String str = indent + (parameter.isTracked() ? "" : "untracked " )
 			 + parameter.type() + " " + parameter.name() + " = ";
 		
 		if ( parameter instanceof ScalarParameter )
 			str += parameter.valueAsString();
 		else if ( parameter instanceof PSetParameter )
 		{
 			PSetParameter pset = (PSetParameter)parameter;
 			if ( pset.parameterCount() == 0 )
 				str += "{}";
 			else
 			{
 				str += "{" + converter.getNewline(); 
 				for ( int i = 0; i < pset.parameterCount(); i++ )
 					str += toString( (Parameter)pset.parameter(i), converter, indent + "  " );
 				str += indent + "}"; 
 			}
 		}
 		else if ( parameter instanceof VectorParameter )
 		{
			str += "{ " + parameter.valueAsString() + " }"; 
		}
		else if ( parameter instanceof VPSetParameter )
		{
			str += "{" + converter.getNewline(); 
			VPSetParameter vpset = (VPSetParameter)parameter;
			for ( int i = 0; i < vpset.parameterSetCount() - 1; i++ )
				str += addComma( toString( (Parameter)vpset.parameterSet(i), converter, indent + "  " ), converter );
			if ( vpset.parameterSetCount() >  0)
				str += toString( (Parameter)vpset.parameterSet( vpset.parameterSetCount() - 1), converter, indent + "  " );
			str += indent + "}"; 
 		}
 		
 		str += converter.getNewline();
 		return str;
 	}
 
	
	protected String addComma( String text, Converter converter )
	{
		if ( !text.endsWith( converter.getNewline() )  )
			return text + ",";
		return text.substring(0, text.length() - 1) + "," + converter.getNewline(); 
	}
	
 }
