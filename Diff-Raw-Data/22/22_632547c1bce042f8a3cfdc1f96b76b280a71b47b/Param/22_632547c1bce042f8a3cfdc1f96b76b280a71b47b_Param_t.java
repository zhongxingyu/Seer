 package reflect.android.api;
 
 public class Param extends NamedArtifact {
 
 	public static String proposeName(String sig, int position) {
 		int index = 0;
 		if (sig == null || sig.isEmpty())
 			return "arg" + position;
 		while (sig.charAt(index) == '[')
 			index++;
 
 		final String Array = index > 0 ? "Array" : "";
 		switch (sig.charAt(index))
 		{
 		case 'Z': return "bool" + Array + position;
 		case 'B': return "byte" + Array + position;
 		case 'C': return "char" + Array + position;
 		case 'S': return "short" + Array + position;
 		case 'I': return "int" + Array + position;
 		case 'J': return "long" + Array + position;
 		case 'F': return "float" + Array + position;
 		case 'D': return "double" + Array + position;
 		case 'V': return "void" + Array + position;
 		case 'L':
 			int dot = sig.lastIndexOf('.');
 			if (dot == -1)
 				return "object" + Array + position;
			String name = sig.substring(dot + 1, sig.length()-1);
 			if (name.isEmpty())
 				return "object" + Array + position;
 			name = name.substring(0, 1).toLowerCase() + name.substring(1);
 			return name + Array + position;
 		}
 		if (!Array.isEmpty())
 			return "array" + position;
 		return "arg" + position;
 	}
 
 	public Param(String signature, int position) {
 		super(1, signature, proposeName(signature, position));
 	}
 }
